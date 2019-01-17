package com.ibm.Zimulator.Zimulator;
import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

/*
  These are the routines which calculate the Λ sets.

  For explanation of how these are stored and cached, see 'Neighbourhoods'

  Get_Λ*() in 'zbox' either calls these or gets the result from a 'Neighbourhoods' cache.

 */
class NeighbourhoodCalculation
{

    static public HashSet<zbox> Reachable_ViaZlinks(zbox γ,int A,int n,boolean ContFlag,int DirFlag,int Mark2)
    /*
      Find all zboxen reachable via zlinks from this one, for the given ztype.
      Includes all zboxen, whether or not they could contain ztype ZT.
      DirFlag: 1 = respect direction of zlinks. 
               0 = ignore Δ and consider only (A,n).
      ContFlag: true = return only zboxen in which ZT can be contained.
               false = return all zboxen connected by zlinks ZT can traverse.

      Requires a fresh Mark2 from the zsystem.

      Dir=0;Cont=true:  Λ_(A,n)(φ)
      Dir=1;Cont=true:  Λ'_(A,n)(φ)

      Dir=0;Cont=false:  Λ~_(A,n)(φ)
      Dir=1;Cont=false:  Λ^>_(A,n)(φ)

      As defined in Specification: (all with respect to τ)

      Λ_τ(ϕ) := { λ │ ϕ :--:_τ λ } ∪ ϕ   ≡  All zboxen mutually connected by zlinked sequences.   [ useful for sleeping ]
      Λ´_τ(ϕ) := { λ │ ϕ :-->_τ λ }   ≡  The terminating zbox of every zlinked path from ϕ.       [ useful for shifting ]
      Λ῀_τ(ϕ) := ⋃_λ ( ϕ :--:_τ λ ) ∪ ϕ  ≡  Λ_τ(ϕ) ∪ zboxen on the actual paths.                  [ useful for invalidation of Λ]
      Λ^1_τ(ϕ) := { λ │ ϕ -->_τ λ )  ≡  All zboxen one zlink directionally from ϕ.
      Λ^>_τ(ϕ) := { ϕ :-->_τ λ }   ≡  Λ´_τ(ϕ) ∪ zboxen on the actual paths                        [ useful for path-resolution ]
    
      therefore: Λ´ ⊆ Λ ⊆ Λ῀  and  Λ´ ⊆ Λ^> ⊆ Λ_τ. 

      Finally, Λ῀_τ(γ) = Λ῀_τ(ϕ) for all γ ∊ Λ῀_τ(ϕ). (So Λ῀ can be used to identify all zboxen requiring cache updates)

    */
    {
	HashSet<zbox> Acc = new HashSet<zbox>();
	Reachable_ViaZlinks(γ,Acc,A,n,ContFlag,DirFlag,Mark2);
	return(Acc);
    }
    private static void Reachable_ViaZlinks(zbox γ,HashSet<zbox> Acc, int A,int n,boolean ContFlag,int DirFlag,int Mark2)
    {
	HashSet<zbox> L = Reachable_ViaZlink(γ,A,n,DirFlag,Mark2);
	for (zbox φ : L)
	    {
		φ.mark2 = Mark2;
		if (φ.e.CanContain(A,n))
		    {
			Acc.add(φ);
		    }
		else
		    {
			if (!ContFlag) {Acc.add(φ);}
			Reachable_ViaZlinks(φ,Acc,A,n,ContFlag,DirFlag,Mark2);
		    }
	    }
    }
    

    static public HashSet<zbox> Reachable_ViaZlink(zbox γ,int A,int n,int DirFlag,int Mark2)
    /*
      Find all unMark2ed zboxen reachable via a /single/ zlink from γ, for the given ztype (A,n).
      Includes all zboxen, whether or not they could contain ztype ZT.
      DirFlag: 1 = respect direction of zlinks. 
               0 = ignore Δ and consider only (A,n).

      Returned list might contain zboxen more than once; there could be more than one zlink in place.

      ( Note that we may or may not include ourselves in the list, 
         depending on directionality of local zlinks, etc.)
    */
    {
	HashSet<zbox> L = new HashSet<zbox>();
	//Implicit case: to our container:
	if ((γ.e.χ!=null)&&(γ.z!=null))
	    { // link exists from this to z
		if (γ.z.mark2 != Mark2)
		    {
			if (γ.e.χ.CanAllowZboxType(A,n,1))
			    {
				L.add(γ.z);
			    }
		    }
	    }
	//Implicit case: to any of our contained.
	if (γ.Z!=null)
	    {
		// v("Checking for implicit link case: Z with χ to us");
		Iterator<zbox> wif = γ.Z.iterator();
		while (wif.hasNext())
		    {
			zbox _zi = wif.next(); //Z.Z_next;
			zbox zb = _zi;
			if (zb.mark2 != Mark2)
			    {
				if (zb.e.χ!=null)
				    { // link exists from Z[j] to γ
					if (zb.e.χ.CanAllowZboxType(A,n,-DirFlag))  // -Δ
					    {  // We can traverse this zlink.
						L.add(zb);
					    }
				    }
			    }
		    }
	    }
	/*
	  Now, check all explicit zlinks:
	 */
	if (γ.zlinklist!=null)
	    {
		// v("Checking for explicit zlinks.");       
		for (zlink χ : γ.zlinklist)
		    {
			zbox μ,ν;int Δ;
			if (χ.μ==γ) { μ = χ.μ; ν = χ.ν; Δ = DirFlag;} else  { μ = χ.ν; ν = χ.μ; Δ = -DirFlag;}
			if (ν.mark2 != Mark2)
			    {
				if (χ.CanAllowZboxType(A,n,Δ))
				    {  // We can traverse this zlink.
					L.add(ν);
				    }
			    }
		    }
	    }
	return(L);
    }    
}
