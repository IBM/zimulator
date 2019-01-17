package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

/*
  zstops do not get stored in the Syslists [].
 */

class zstop extends zobject implements CompiledFileRW
{
    /*
      Should have φ eor K.
    */
    zbox φ;
    double σ_i;   // defaults 0 if negative.
    double σ_f;   // defaults 1 if negative.

    zpath K;

    String info; // null or else information which is to be included in reporting. 'i' field in documentation.
    
    /*
      i,j are only used in Route.zpathtoRoute().
      They default to -1.
     */  
    int i,j;
        
    public zstop()
    {
	info=null;
	i=-1;j=-1;
	σ_i=-1.0;σ_f=-1.0;
    }

    public zstop(zbox _φ)
    {
	info=null;
	φ=_φ;K=null;i=-1;j=-1;
	σ_i=-1.0;σ_f=-1.0;
    }
    
    public zstop(zbox _φ,zpath _K,int _i,int _j)
    {
	info=null;
	φ=_φ;K=_K;i=_i;j=_j;
	σ_i=-1.0;σ_f=-1.0;
    }
    
    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {
	D.wb('s');  //zstop.
	D.wi(GetFileRefNumber());  // will actually be 0 sinze zstops are not file-referenced.
	if (φ==null) {D.wi(-2);} else {D.wi(φ.GetFileRefNumber());}
	if (K==null) {D.wi(-2);} else {D.wi(K.GetFileRefNumber());}
	D.wd(σ_i);D.wd(σ_f);
	D.wi(i);
	D.wi(j);
	D.ws(info);
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	ORL.add(OR);
	OR.Rafs = new FA_i();
	OR.Rafs.set(0,D.ri()); //  φ
	OR.Rafs.set(1,D.ri()); //  K
	σ_i = D.rd();
	σ_f = D.rd();
	i = D.ri();	
	j = D.ri();
	info = D.rs();
	Label = D.rs();
	//Str.print("zstop '" + Label + "'");
    }
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	φ = (zbox) RR.getref(OR.Rafs.get(0));
	K = (zpath) RR.getref(OR.Rafs.get(1));
	return(true);
    }

    /*
      zstops do not get referenced.
     */
    public void SetFileRefNumber(int n) {}
    public int GetFileRefNumber() {return(0);}
    
    public String toString()
    {
	String I;
	I="[zstop";
	if (Label!=null) { I += " '" + Label +"'";}
	if (φ!=null)
	    {
		I += " φ:"; if (φ.Label!=null) {I += φ.Label;}
		I += "(" + φ.e.A +") ";
	    }
	if (K!=null)
	    {
		I += " K:"; if (K.Label!=null) {I += K.Label;}
	    }
	I += " ]";
	return(I);
    }
}
