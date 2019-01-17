package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

class zsource extends zobject implements CompiledFileRW
{
    zbox φ;
    int m;   // 1 = One, 2 = From.    
    int o;  // 1,2 = Container, Teleport

    double v_μ,v_σ;  //  mean and std.devof velocities. if μ<0 then ignored.
    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    { // φ,m,o
	D.wb('o');  //zsource
	D.wi(GetFileRefNumber());
	D.wi(φ.GetFileRefNumber());
	D.wb(m);D.wb(o);
	D.wd(v_μ);D.wd(v_σ);
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	OR.Rafs=new FA_i();
	OR.Rafs.add(D.ri());                     // φ
	m = D.rb();
	o = D.rb();
	v_μ = D.rd();v_σ = D.rd();
	Label=D.rs();
	ORL.add(OR);
    }
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	φ = (zbox) RR.getref(OR.Rafs.get(0));
	return(true);
    }
   
    
    public zsource()
    {
	v_μ=-1.0;
	m=0;o=0;
    }

    public String toString()
    {
	String I;
	I="[zsource";
	if (Label!=null) { I += " '" + Label +"'";}
	if (φ!=null)
	    {
		I += " φ:"; if (φ.Label!=null) {I += φ.Label;}
		I += "(" + φ.e.A + "," + φ.e.n +") ";
	    }
	I += " m=" + m + " o=" + o;
	I += " ]";
	return(I);
    }
}
