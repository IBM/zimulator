package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

public class ReportFlags
{
    /*
      The flags. 
      Numerical bit values used; they ONLY appear in this sourcefile (and .zobj state-storage files).
      Methods here are used to zsyntax parsing, .zobj file storage, and checking reporting flags.
    */
    private int R;

    private int R_Channel;  
    
    public ReportFlags()
    { // used for object file case.
	R=0;R_Channel=0;
    }

    public ReportFlags(int R,int R_Channel)
    { // used for object file case.
	this.R=R;
	this.R_Channel=R_Channel;
    }

    public String toString()  // for verbosity.
    {
	String r="";
	if ((R & 1)!=0) {r += "S";}  //'self'
	if ((R & 2)!=0) {r += "C";}  //'container' 
	if ((R & 4)!=0) {r += "P";}  //'path'
	if ((R & 8)!=0) {r += "d";}  //'details'
	return(r);
    }
    
    public ReportFlags(String spec)
    /*
      Sets up ξ and n_ξ. Used by input parser.
    */	
    {
	R = 0;
	if (Str.index(spec,"S")>=0) {R = R | 1;}
	if (Str.index(spec,"C")>=0) {R = R | 2;}
	if (Str.index(spec,"P")>=0) {R = R | 4;}
	if (Str.index(spec,"d")>=0) {R = R | 8;}
	String sv = Str.removeWhiteSpace(Str.nonDigitsToWS(spec));
	if (Str.length(sv)!=0) {R_Channel = Str.atoi(sv);}
    }

    public boolean Anything() {return(R!=0);}
    
    public boolean Self() {return((R & 1)!=0);}
    public boolean Container() {return((R & 2)!=0);}
    public boolean Path() {return((R & 4)!=0);}
    public boolean Details() {return((R & 8)!=0);}
    public int Channel() {return(R_Channel);}
    

    public void Write_ObjFile(PrimitiveDataOut D) throws IOException
    {   // A,n,R,C_A,C_n,χ,m,L,W,N,S,V,σ,l,v,BaseCost,CostFactor,Z_A,Z_n
	D.wi(R);
	D.wi(R_Channel);
    }

}


