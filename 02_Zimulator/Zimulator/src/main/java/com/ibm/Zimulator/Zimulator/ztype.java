package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

class ztype extends zobject implements CompiledFileRW
{
    int A;              // User-defined type.
    int n;              // User-defined number (sub-type)
    
    int q;  // containment-consideration.
    
    ReportFlags R;   // Reporting type or null.
    
    int[] C_A;          // List of types we can contain.
    int[] C_n;          // Restriction on C_A.
    int[] C_ξ;          // Server flags for each ztype. Not null. Default to 0.
    zlink χ;            // Template for implied zlink when we're contained.
    int m;              // Containment mode:  1,2,3,4,5,6,7 = Span, Pipe, Shelf, Fifo, Bag, Source, Sink... defined in a String in zsystem.
    int L,W;            // Size inside.  [ZSU]
    int N;              // Capacity inside. -1 for no limit.
    int S;              // Spacing inside [ZSU]
    double V;           // Speed limit inside. <0 for no limit.
    double σ;           // Std. Dev. for generated velocity in Source mode
    int l;              // Size outside. [ZSU]
    //int ξ,n_ξ;  Olde way 2018-09-07
    ServerFlags ξ;
    
    //REMOVED 2018-02-21    int[] c_A;          // List of types we can be contained in.
    //REMOVED 2018-02-21    int[] c_n;          // Restiction on C_A.
    double v;           // Our natural speed in our container; can be overridden by individual zboxen. This is used for path time estimation.
    double BaseCost;        // the base price paid to travel through this zbox.
    double CostFactor;      // the factor to multiply by the container basecost when travelling.

    double ρ;       // Presence-bifurcation constant, if positive. Set to -1 for no bifurcation.
    double ρ_0;     // Threshold. If bifurcation would result in less than this, then 'do not leave any behind'
    int clone_num;  // If a zbox with this ztype is cloned (Presence), this is used to keep track of clone number.

    int[] Z_A;    // sleepers.
    int[] Z_n;    // Just like C_A and C_n.    

    Economics Eco;  // 2018-08-25 - Moved here from {zdemand}
    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {   // A,n,R,C_A,C_n,χ,m,L,W,N,S,V,σ,l,v,BaseCost,CostFactor,Z_A,Z_n
	D.wb('t');  //ztype
	D.wi(GetFileRefNumber());
	D.wi(A);
	D.wi(n);
	DoReportFlags.WriteToObjFile(D,R);
	D.wb(m);
	if (C_A!=null) {D.wi(C_A.length);
	    for (int j=0;j<C_A.length;j++) { D.wi(C_A[j]);D.wi(C_n[j]);D.wi(C_ξ[j]); }
	}
	else {D.wi(0);}
	if (Z_A!=null) {D.wi(Z_A.length);for (int j=0;j<Z_A.length;j++)    { D.wi(Z_A[j]);D.wi(Z_n[j]);} }
	else {D.wi(0);}
	if (χ==null) {D.wi(-2);} else {	D.wi(χ.GetFileRefNumber());}  // implicit zlink.
	D.wi(L);
	D.wi(W);
	D.wi(N);
	D.wi(S);
	if (Eco!=null)
	    {
		Eco.WriteToObjFile(D);
	    }
	else
	    {
		D.wb(0);	       
	    }
	D.wi(l);
	D.wd(V);
	D.wd(σ);
	D.wd(v);
	D.wd(BaseCost);
	D.wd(CostFactor);
	if (ξ==null)
	    {
		D.wb(0);
	    }
	else
	    {
		D.wb(1);
		ξ.WriteToObjFile(D);
	    }
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	ORL.add(OR);
	A=D.ri();
	n=D.ri();
	R = DoReportFlags.ReadFromObjFile(ORL,D);
	m=D.rb();
	int C_A_l = D.ri();
	if (C_A_l!=0) {C_A=new int[C_A_l];C_n=new int[C_A_l];C_ξ=new int[C_A_l];
	    for (int j=0;j<C_A.length;j++) { C_A[j]=D.ri();C_n[j]=D.ri();C_ξ[j]=D.ri();} }
	int Z_A_l = D.ri();
	if (Z_A_l!=0) {Z_A=new int[Z_A_l];Z_n=new int[Z_A_l];
	    for (int j=0;j<Z_A.length;j++) { Z_A[j]=D.ri(); Z_n[j]=D.ri();} }
	//	Z_A=null; //!!!!!DEBUG
	OR.Rifs = new FA_i();	
	OR.Rifs.set(0,D.ri()); // χ.
	L=D.ri();
	W=D.ri();
	N=D.ri();
	S=D.ri();
	if (D.rb()!=0)
	    {
		Eco = new Economics();
		Eco.ReadFromObjFile(ORL,D);
	    }
	l=D.ri();
	V=D.rd();
	σ=D.rd();
	v=D.rd();
	BaseCost=D.rd();
	CostFactor=D.rd();
	if (D.rb()==1)
	    {
		ξ = new ServerFlags();
		ξ.ReadFromObjFile(null,D);
	    }
	Label=D.rs();
	// Now that it is read in, add it to our references:
    }

    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	χ = (zlink) RR.getref(OR.Rifs.get(0));	
	return(true);
    }



    




    

    public ztype()
    {
	ρ = -1.0;
	ρ_0 = 1.0e-5;
	clone_num=0;
	q=0;
	m=8;   // default Containment Mode is Static.
	C_A=null;
	C_ξ=null;
	Z_A=null;
	v=1.0;
	V=-1;  // no speed limit.
	// These L,S,l are integers now:
	L=0;  // default is no constraint.     2018-09-06   WAS 100 BY DEFAULT.
	W=1;
	l=1;
	S=2;
	Label=null;
	N=-1; // default is no count limit.
	BaseCost=0;
	CostFactor=1.0;
	R=null;  //Quiet is defuault
	ξ=null;
	Eco = new Economics();  // contains defaults.
    }

    public String toString()
    {
	String I = " A:" + A + " n:" + n;
	return(I);
    }
	    
    public String toString(zsystem Ψ)
    {
	String I;
	I="[ztype";
	if (Label!=null) { I += " '" + Label +"'";}
	I += " A:" + A + " n:" + n;
	if (C_A!=null) {I += " C:{";for (int j=0;j<C_A.length;j++) { I += C_A[j] + ","+ C_n[j] + " "; } I+="}";}
	I += " m:" + Ψ.CV.ModeNames[m];
	I += " L:" + L +" σ:" +σ+ " V:" +V+ " l:" +l+ " S:" + S;
	I += " Eco:" + Eco.toString();
	//if (c_A!=null) {I += " c:{";for (int j=0;j<c_A.length;j++) { I += c_A[j] + ","+ c_n[j] + " "; } I+="}";}
	I += "]";
	return(I);
    }

    boolean CanContain(ztype ZT)
    /*
      Is (A,n) type-compatibility such that this can contain a zbox of the specified type?
    */
    {	
	return(CanContain(ZT.A,ZT.n));

	/* REMOVED 2018-02-21
	   
	   if (C_A==null) {return(false);}
	   if (ZT.c_A!=null)
	    {
		boolean Allowed=false;
		for (int j=0;j<ZT.c_A.length;j++)
		    {
			if (ZT.c_A[j]==A)
			    {
				if ((ZT.c_n[j]==0) || (ZT.c_n[j]==n))
				    {Allowed=true;break;}
			    }
		    }
		if (!Allowed) {return(false);}
	    }
	// See if this's list of possible contained zboxen includes ZB2's type:
	{
	    boolean Allowed=false;
	    for (int j=0;j<C_A.length;j++)
		{
			if (C_A[j]==ZT.A)
			    {
				if ((C_n[j]==0) || (C_n[j]==ZT.n))
				    {Allowed=true;break;}
			    }
		}
	    if (!Allowed) {return(false);}
	}
	return(true);
	*/
    }

    boolean CanContain(int A,int n)
    /*
      Is (A,n) type-compatibility such that this can contain a zbox of the specified (A,n) type?
      In this case we only consider restrictions placed by the container.
      To consider container and also contained, use ztype version of this method.
      Specify n=0 to allow any n.
    */
    {
	if (C_A==null) {return(false);}
	// See if this's list of possible contained zboxen includes ZB2's type:
	int jlim=C_A.length;
	for (int j=0;j<jlim;j++)
	    {
		if (C_A[j]==A)
		    {
			if ( (C_n[j]==0) || (C_n[j]==n) || (n==0) )
			    {return(true);}
		    }
	    }
	return(false);
    }

    public int get_ξ_for_ztype(int A,int n)
    /*
      Look through C list and determine if (A,n) has a ξ listed.
     */
    {
	if (C_ξ == null) {return(0);}
	int jlim = C_A.length;
	for (int j=0;j<jlim;j++)
	    {
		if (C_A[j]==A)
		    {
			if ( (C_n[j]==0) || (C_n[j]==n) || (n==0) )
			    {
				//Str.print("DEBUG:    Found C_ξ="+C_ξ[j]+" for type A,n=" + A + "," + n);
				return(C_ξ[j]);
			    }
		    }
	    }
	return(0);
    }


    
}
