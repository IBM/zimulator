package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

class zlink extends zobject implements CompiledFileRW
{
    zbox μ,ν;        // The two zboxen.
    
    int[] A_A;
    int[] A_n;
    int[] A_Δ;

    zsystem Ψ; // Convenience 2018-09-14.

    public zlink(zsystem _Ψ)
    {
	Ψ = _Ψ;
	μ = null;
	ν = null;
    }
    
    public void Delete()
    /*
      Deletes a zlink from the system.

      What depends on a zlink?
      1. Λ caches    -- μ and ν need their Λ caches invalidated.
      2. μ,ν         -- μ and v need this zlink removed from their zlinklists.
      3. We might affect zpaths; this is the responsibility of someone else. 
         (This Delete() is usually called by zbox.Delete() which handles zpaths.)
      4. syslist[]   -- we set DeletionFlag, which is picked up by SysList.
    */
    {
	if (DeletionFlag==true) {return;}
	// 1.
	if (μ!=null)   {μ.InvalidateCaches(0);}
	if (ν!=null)   {ν.InvalidateCaches(0);}
	// 2.
	if (μ!=null)   {μ.zlinklist.remove(this);}
	if (ν!=null)   {ν.zlinklist.remove(this);}
	μ=null;
	ν=null;
	// 4.
	DeletionFlag=true;
	Ψ.System_Z_Lists.add_to_BufferD(this);  // Causes [] removal.	
    }

    
    public String concisetypelist()
    {
	String I = "{";
	for (int k=0;k<A_A.length;k++)
	    {
		I += "(" + A_A[k] +"," + A_n[k] + "," + A_Δ[k] + ") ";
	    }
	I += "}";
	return(I);
    }
    
    public void AddLinkRefsToZboxen()
    {
	if (μ!=null)
	    {
		if (μ.zlinklist==null) {μ.zlinklist = new HashSet<zlink>();}
		if (!μ.zlinklist.contains(this))
		    {
			μ.zlinklist.add(this);			
		    }
	    }
	if (ν!=null)
	    {
		if (ν.zlinklist==null) {ν.zlinklist = new HashSet<zlink>();}
		if (!ν.zlinklist.contains(this))
		    {
			ν.zlinklist.add(this);
		    }
	    }
    }
    
    public String toSS()
    // Short version of toString().
    {
	return(μ.Label + "→" + ν.Label);
    }

    public String toString()
    {
	String I;
	I="[zlink";
	if ((Label!=null)&&(Str.length(Label)!=0)) { I += " '" + Label +"'";}
	{
	    if (μ!=null)
		{
		    I += " μ:"; if (μ.Label!=null) {I += μ.Label;}
		    I += "(" + μ.e.A +") ";
		}
	    else
		{
		    I += " -μ";
		}
	    if (ν!=null)
		{
		    I += " ν:"; if (ν.Label!=null) {I += ν.Label;}
		    if (ν.e!=null) // for debugging in deref below.
			{
			    I += "(" + ν.e.A +") ";
			}
		}
	    else
		{
		    I += " -ν";
		}
	}
	I += " A:{";
	for (int k=0;k<A_A.length;k++)
	    {
		I += "(" + A_A[k] +"," +A_n[k] + "," + A_Δ[k] + ") ";
	    }
	I += "} ]";
	return(I);
    }



    boolean CanAllowZboxType(ztype e,int Δ)
    {
	return(CanAllowZboxType(e.A,e.n,Δ));
    }
    
    boolean CanAllowZboxType(int typeA,int typen,int Δ)
    {  // Is type-compatibility such that this can allow φ to pass in direction Δ?
	if (A_A==null) {return(true);}
	for (int j=0;j<A_A.length;j++)
	    {
		if ((A_Δ[j]==0) || (A_Δ[j]==Δ) || (Δ==0))
		    {
			if ( (A_A[j]==0) || ( (A_A[j]==typeA) && ( (A_n[j]==0) || (A_n[j]==typen) ) ) )
			    {
				return(true);
			    }
		    }
	    }
	return(false);
    }





    // ******************************************************************
    // Implementation of 'CompiledFileRW'
    // ******************************************************************

    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {   // μ,ν,A,n,Δ,Label
	D.wb('l');  //zlink
	D.wi(GetFileRefNumber());
	if (μ!=null) {D.wi(μ.GetFileRefNumber());} else {D.wi(-2);}
	if (ν!=null) {D.wi(ν.GetFileRefNumber());} else {D.wi(-2);}
	D.wi(A_A.length);
	for (int j=0;j<A_A.length;j++)
	    {
		D.wi(A_A[j]);	
		D.wi(A_n[j]);
		D.wi(A_Δ[j]);
	    }
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	ORL.add(OR);
	OR.Rifs = new FA_i();
	OR.Rifs.set(0,D.ri()); // μ
	OR.Rifs.set(1,D.ri()); // ν
	int A_len = D.ri();
	A_A = new int[A_len];
	A_n = new int[A_len];
	A_Δ = new int[A_len];
	for (int j=0;j<A_A.length;j++)
	    {
		A_A[j]=D.ri();
		A_n[j]=D.ri();
		A_Δ[j]=D.ri();
	    }
	Label = D.rs();

    	//Str.printE("#500# zlink!!  RFOF() #343#" + toString() + " ref##= " + OR.C + " #550# Added ORL:" + ORL.size());
    }

    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	μ = (zbox) RR.getref(OR.Rifs.get(0));
	ν = (zbox) RR.getref(OR.Rifs.get(1));
	/*
	  Update zboxen, now that we've μ and ν:
	 */
	AddLinkRefsToZboxen();
	//Str.printE("#005# zlink!! ResORs #123#(μ,ν should be resolved) #343#" + toString());
	return(true);
    }

}
