package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

public class ServerFlags implements CompiledFileRW
{
    private int[] ξ;    // Contains a list of scenarios, with bits like C,E,l,f,v,p...
    private int[] n_ξ;  // server number to contact for each of the above. -1 for default (either 0 or up to ztype)
    public ServerFlags()
	{ // used for object file case.
	    ξ=null;n_ξ=null;
	}

    public ServerFlags(String spec)
	/*
	  Sets up ξ and n_ξ. Used by input parser.
	*/	
	{
	    String[] ss = Str.split0(spec,",");
	    ξ = new int[ss.length];
	    n_ξ = new int[ss.length];	    
	    for (int j=0;j<ss.length;j++)
		{
		    ξ[j]=0;n_ξ[j]=-1;		      
		    if (Str.index(ss[j],"C")>=0) {ξ[j] = ξ[j] | val_bit('C');}
		    if (Str.index(ss[j],"E")>=0) {ξ[j] = ξ[j] | val_bit('E');}
		    if (Str.index(ss[j],"f")>=0) {ξ[j] = ξ[j] | val_bit('f');}
		    if (Str.index(ss[j],"l")>=0) {ξ[j] = ξ[j] | val_bit('l');}
		    if (Str.index(ss[j],"v")>=0) {ξ[j] = ξ[j] | val_bit('v');}
		    if (Str.index(ss[j],"p")>=0) {ξ[j] = ξ[j] | val_bit('p');}
		    String sv = Str.removeWhiteSpace(Str.nonDigitsToWS(ss[j]));
		    if (Str.length(sv)!=0)
			{
			    n_ξ[j] = Str.atoi(sv);
			}
		}
	}

    public int size()
    {
	return(ξ.length);
    }

    public int get_n_ξ(int j)
    /*
      Return server number for index j.
     */
    {
	int snum=n_ξ[j];
	if (snum==-1)  { snum=0;}
	return(snum);
    }

    public boolean contains_bit(int j,int bitnamechar,int C_mask)
    /*
      bitanmechar = 'C' for example.
    */
    {
	return( ( ξ[j] & val_bit(bitnamechar) & C_mask ) != 0 );
    }

    public boolean contains_bit(int j,int bitnamechar)
    {
	return( ( ξ[j] & val_bit(bitnamechar)) != 0 );
    }

    public boolean mask_contains_bit(int mask,int bitnamechar)
    {
	return( ( mask & val_bit(bitnamechar)) != 0 );
    }

    private int val_bit(int bitnamechar)
    {
	// Server-consultation flags: All defined here.
	//	" C E f l v p"
	int val=0;
	switch (bitnamechar) {
	case 'C': val=1;break;
	case 'E': val=2;break;
	case 'f': val=4;break;
	case 'l': val=8;break;
	case 'v': val=16;break;
	case 'p': val=32;
	}
	return(val);
    }

    public int ValueFromMask(String mask)
	/*
	  used by input parser.
	  'mask' is some combination of Event Flags (the uppercase ones)
	*/
    {
	int ξ = 0;
	if (Str.index(mask,"C")>=0) {ξ = ξ | val_bit('C');}
	if (Str.index(mask,"E")>=0) {ξ = ξ | val_bit('E');}
	return(ξ);
    }

    

    // ******************************************************************
    // Implementation of 'CompiledFileRW'
    // ******************************************************************
    // There is no 'empty' case.

    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {   // A,n,R,C_A,C_n,χ,m,L,W,N,S,V,σ,l,v,BaseCost,CostFactor,Z_A,Z_n
	D.wi(ξ.length);
	for (int j=0;j<ξ.length;j++)
	    {
		D.wi(ξ[j]);
		D.wi(n_ξ[j]);
	    }
    }

    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	int len = D.ri();
	ξ = new int[len];
	n_ξ = new int[len];
	for (int j=0;j<ξ.length;j++)
	    {
		ξ[j] = D.ri();
		n_ξ[j] = D.ri();
	    }
    }
    
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ) { return(true); } // no refs.
    public void SetFileRefNumber(int n) {}
    public int GetFileRefNumber() {return(0);}    
    
}


