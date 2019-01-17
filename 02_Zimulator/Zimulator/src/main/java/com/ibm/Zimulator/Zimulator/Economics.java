package com.ibm.Zimulator.Zimulator;

/*
  See 'PathResolution' for a discussion of how shortest (and other) zpaths
  are determined and stored.

  'Economics' is just a simple class to keep track of how zpaths are valued.
  There are some constants, and a simple routine to return the [probably always simple linear] 
  evaluation of a "graph edge". For what graph edges represent, see 'BoxNetwork'.

 */
import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

class Economics implements CompiledFileRW
{
    double coeff_T;  // Coefficient for time.
    double coeff_C;  // Coefficient for cost.
    double coeff_N;  // Coefficient for containment
    double coeff_K;  // Coefficient for zpath usage
    double coeff_L;  // Coefficient for zlinks.
    
    public Economics()
    /*
      Used in zdemand and zschedule, Inputparser() and below in ReadFromObjFile().
     */
    {
	coeff_T = 0.05; 
	coeff_C = 1.0;  
	coeff_N = 1.0;
	coeff_K = 12.0;
	coeff_L = 0.0;
    }

    public String toString()
    {
	return("T=" + coeff_T + " C=" + coeff_C + " N=" + coeff_N + " K=" + coeff_K + " L=" + coeff_L);
    }
    
    public double EdgeLen(double t, double cost,int zboxen,int zlinks,int zpaths)
    /*
      Returns a 'graph' edge length (for path distribution etc.) given
      some quantities.
      The way it is used, it should be linear in each quantity.
    */
    {
	return( coeff_T*t + coeff_C*cost + coeff_N*zboxen + coeff_L*zlinks + coeff_K*zpaths );
    }







    // ******************************************************************
    // Implementation of 'CompiledFileRW'
    // ******************************************************************
    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {
	D.wb('E');
	D.wd(coeff_T);
	D.wd(coeff_C);
	D.wd(coeff_N);
	D.wd(coeff_K);
	D.wd(coeff_L);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	coeff_T = D.rd();
	coeff_C = D.rd();
	coeff_N = D.rd();
	coeff_K = D.rd();
	coeff_L = D.rd();
    }    
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    { // no refs.
	return(true);
    }
    public void SetFileRefNumber(int n) {}
    public int GetFileRefNumber() {return(0);}    

}
