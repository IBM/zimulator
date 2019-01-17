package com.ibm.Zimulator.Zimulator;

/*
  A 'NeighHood' (described below) is a way of storin a cache of all zboxen related to a given zbox via zlinks.
  A 'Neighbourhoods' keeps track of a set of 'NeighHood' sets, one for each ztype (A,n).
  While a simulation is running, these are generated when needed and kept in a cache which is invalidated when needed.
  The routines below handle the book-keeping.
  Tthe actual calculation of the Λ sets is handled by zbox.Reachable_ViaZlinks() which is called within 'NeighHood'.
  The Λ sets are documented both in the main Specification document and in that function.
*/


import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.MDA.*;

class Neighbourhoods
/*
  This is a list of Neighbourhoods; one for each (A,n).
  They can be invalidated and generated.
 */
{
    private zsystem Ψ;
    private zbox φ;  // A base used for calculating the Λ sets.
    //    private MDA<NeighHood> ztypes;

    private ArrayList<ArrayList<NeighHood>> NNN;

    private NeighHood get(int A,int n)
    {
	if (NNN.size()<=A) {return(null);}
	ArrayList<NeighHood> NN = NNN.get(A);
	if (NN==null) {return(null);}
	if (NN.size()<=n) {return(null);}
	return(NN.get(n));
    }
    private void set(int A,int n,NeighHood N)
    {
	while (NNN.size()<=A) {NNN.add(null);}
	ArrayList<NeighHood> NN = NNN.get(A);
	if (NN==null)
	    {
		NN = new ArrayList<NeighHood>();
		NNN.set(A,NN);
	    }
	while (NN.size()<=n) {NN.add(null);}
	NN.set(n,N);
    }
    
    public Neighbourhoods(zsystem _Ψ,zbox _φ)
    {
	Ψ = _Ψ;
	φ = _φ;
	//	ztypes = new MDA<NeighHood>(2);
	NNN = new ArrayList<ArrayList<NeighHood>>();
    }

    public NeighHood get_NeighHood(int A,int n)
    /*
      Returns Λ.
     */
    {
	return(FindOrMakeNeigh(A,n));
    }

    private NeighHood FindOrMakeNeigh(int A,int n)
    /*
      Returns one for A,n. Creates if necc.
     */
    {
	NeighHood ZN = get(A,n);
	if (ZN==null)
	    {
		ZN = new NeighHood(Ψ,φ,A,n);
		set(A,n,ZN);
	    }
	return(ZN);
    }
    
    private NeighHood FindNeigh(int A,int n)
    /*
      Returns the specific zneighbournood associated with A,n or else null.
    */
    {
	return(get(A,n));

	// ####   BAD FUNCTION!!

	/*
	if (ztypes==null) {return(null);}
	ListIterator<NeighHood> wit = ztypes.listIterator();
	while (wit.hasNext())
	    {
		NeighHood N = wit.next();
		{
		    if ((N.A==A)&&(N.n==n)) {return(N);}
		}
	    }
	return(null);
	*/
    }
    

    public void invalidate_all_neighbourhoods(int A,int n, int BeforeOrAfter)
    {
	
    }


    
    public void invalidate_from_old_Λt(int A,int n)
    /*
      Removes NeighHood for all zboxen in the Λ῀ set.      
      This can be used before a change is made to the system.
      (i.e. before moving a zbox, invalidate the cache)

      Λ῀ will also be invalidated, so a change can be effected subsequently.
     */
    {
	NeighHood ZN = FindNeigh(A,n);
	if (ZN==null) {return;}
	HashSet<zbox> Λt = ZN.get_Λt();
	for (zbox γ : Λt) // includes φ.
	    {
		//Str.print("           ::INV:: " + γ.Label);
		if (γ.zneighcache!=null)
		    {
			γ.zneighcache.remove(A,n);
		    }
	    }
    }

    public void invalidate_with_new_Λt(int A,int n)
    /*
      Generate fresh Λ~. Then, invalidate Λ, Λ', Λ1, Λd

      This generates a fresh Λ~ set, and then invalidates all the others: Λ,Λ1,Λ'.
      This can be used after a change is made to the system.
      (i.e. after moving a zbox, invalidate the cache)
      
      Λ῀ is generated, and remains valid after.
    */
    {
	NeighHood ZN = FindOrMakeNeigh(A,n);
	HashSet<zbox> Λt = ZN.get_Λt(true); // Force a fresh Λ῀, rather than a cached one.
	for (zbox γ : Λt) // includes φ.
	    {
		if (γ.zneighcache==null) {γ.zneighcache=new Neighbourhoods(Ψ,γ);}
		γ.zneighcache.get_NeighHood(A,n).nul_Λ();
		γ.zneighcache.get_NeighHood(A,n).nul_Λp();
		γ.zneighcache.get_NeighHood(A,n).nul_Λ1();
		γ.zneighcache.get_NeighHood(A,n).nul_Λd();
		γ.zneighcache.get_NeighHood(A,n).set_Λt(Λt); // Or, instead ZN.share_Λt() below.
	    }
	// Now, we should not waste this Λ῀ since we have it now!
 	//ZN.share_Λt();
    }

    public void remove(int A,int n)
    /*
      Removes NeighHood for a specific A,n.
    */
    {
	if (Ψ.cacheNeighbourhoodsFlag==0) {return;}

	NeighHood N = get(A,n);
	if (N!=null)
	    {
		N.nul_Λ();
		N.nul_Λp();
		N.nul_Λ1();
		N.set_Λt(null);
	    }
	return;
	/*
	ListIterator<NeighHood> wit = ztypes.listIterator();
	while (wit.hasNext())
	    {
		NeighHood N = wit.next();
		if (N!=null)// probably don't need this.
		    {
			if ((N.A==A)&&(N.n==n))
			    {
				N.nul_Λ();
				N.nul_Λp();
				N.nul_Λ1();
				N.set_Λt(null);
				//wit.remove();
				break;
			    }
		    }
	    }
	*/
    }
    
}



class NeighHood
{
    int A,n;
    private HashSet<zbox> Λ,Λp,Λt,Λ1;  // Λ, Λ´,Λ῀ 
    private HashSet<zbox> Λd;  // Λ^>
    private zsystem Ψ;
    private zbox φ;
    
    public NeighHood(zsystem _Ψ,zbox _φ,int _A,int _n)
    {
	Ψ=_Ψ;
	φ=_φ;
	A=_A;n=_n;
	Λ=null;
	Λp=null;
	Λt=null;
	Λ1=null;
	Λd=null;
    }

    public void set_Λ(HashSet<zbox> _Λ)   {Λ=_Λ;}
    public void set_Λt(HashSet<zbox> _Λt)   {Λt=_Λt;}
    public void nul_Λ()    {Λ=null;}
    public void nul_Λp()   {Λp=null;}
    public void nul_Λ1()   {Λ1=null;}
    public void nul_Λd()   {Λd=null;}
    public HashSet<zbox> get_Λ()
    {
	if (Λ==null)
	    {
		Λ = NeighbourhoodCalculation.Reachable_ViaZlinks(φ,A,n,true,0,Ψ.FreshMark2());
		// Λ can be shared between all these.
		share_Λ();
	    }
	//DumpList(φ,"Λ",Λ);	
	return(Λ);
    }

    public void share_Λ()
    /*
      Every member zbox in Λ has the same Λ. Share the one we've computed.
     */
    {
	for (zbox γ : Λ)
	    {
		if (γ.zneighcache==null) {γ.zneighcache=new Neighbourhoods(Ψ,γ);}
		γ.zneighcache.get_NeighHood(A,n).set_Λ(Λ);
	    }	
    }
    
    public HashSet<zbox> get_Λp()
    {
	//Λp=null;  //DEBUG
	if (Λp==null)
	    {
		Λp = NeighbourhoodCalculation.Reachable_ViaZlinks(φ,A,n,true,1,Ψ.FreshMark2());
	    }
	//Str.print("::  " + φ.Label + ".Λ´. Types:" + φ.zneighcache.ztypes.size() + " Λp(" + A + "," + n + "):" + Λp.size());

	//	DumpList(φ,"Λ'",Λp);	
	return(Λp);
    }

    public HashSet<zbox> get_Λt()
    {
	return(get_Λt(false));
    }
    public HashSet<zbox> get_Λt(boolean forcefresh)
    {
	if ((Λt==null)||(forcefresh))
	    {
		Λt = NeighbourhoodCalculation.Reachable_ViaZlinks(φ,A,n,false,0,Ψ.FreshMark2());
		// Λt.add(φ);		// always appears by itself.
		// Λt can be shared between all these.
		share_Λt();	
	    }
	//DumpList(φ,"Λ῀",Λt);	
	return(Λt);
    }


    public HashSet<zbox> get_Λd()   // Λ^>
    {
	return(get_Λd(false));
    }
    public HashSet<zbox> get_Λd(boolean forcefresh)
    {
	if ((Λd==null)||(forcefresh))
	    {
		Λd = NeighbourhoodCalculation.Reachable_ViaZlinks(φ,A,n,false,1,Ψ.FreshMark2());
	    }
	//DumpList(φ,"Λ^>",Λd);
	return(Λd);
    }


    public void share_Λt()
    /*
      Every member zbox in Λ῀ has the same Λ῀. Share the one we've computed.
     */
    {
	for (zbox γ : Λt)
	    {
		if (γ!=φ)
		    {
			if (γ.zneighcache==null) {γ.zneighcache=new Neighbourhoods(Ψ,γ);}
			γ.zneighcache.get_NeighHood(A,n).set_Λt(Λt);
		    }
	    }	
    }
    
    public HashSet<zbox> get_Λ1()
    {
	//Λ1=null;  //DEBUG
	if (Λ1==null)
	    {
		Λ1 = NeighbourhoodCalculation.Reachable_ViaZlink(φ,A,n,1,Ψ.FreshMark2());
	    }
	//DumpList(φ,"Λ₁",Λ1);	
	return(Λ1);
    }


    
    /*
      DumpList(φ,"Λ",Λ);
      DumpList(φ,"Λ'",Λp);
      DumpList(φ,"Λ῀",Λt);
      DumpList(φ,"Λ1",Λ1);
    */
    
    private void DumpList(zbox φ,String L,HashSet<zbox> LL)
    {
	String lin = "Zbox ΛΛΛ " + φ.Label + " WRT (" + A + "," + n + ") has " + L + " = {";
	for (zbox lφ : LL)
	    {
		lin = lin + " " + lφ.Label;
	    }
	lin += " }";
	Str.print(lin);
    }
}
