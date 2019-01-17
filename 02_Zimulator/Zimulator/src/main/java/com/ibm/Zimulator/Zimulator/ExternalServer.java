package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;


class ExternalServer
{
    private String Address;
    private zsystem Ψ;
    
    public ExternalServer(zsystem _Ψ,String _Address)
    /*
      _Address should be http://somthing.sth if run from the command line.
      If run otherwise, can be anything at all.
    */
    {
	Ψ = _Ψ;
	Address = _Address;
    }

    public String Desc()
    {
	return("[" + Address + "]");
    }

    public Iterator<String> request()
    { // Test empty request.
	ArrayList<String> Test = new ArrayList<String>();
	Test.add("#start");
	Test.add("ZimTest");  //Zimulator request.	
	Test.add("#end");
	return(Ψ.AllSystemIO.makeZserverRequest(Address,Test.iterator()));
    }

    public Iterator<String> request(ServerRequest SR)
    /*
      Returns Iterator<String> so that it is interchangeable with FileLineIterator.
    */
    {
	return(Ψ.AllSystemIO.makeZserverRequest(Address,SR.iterator()));
    }

    /*
      File reading and writing.
     */
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {
	D.ws(Address);
    }
    public ExternalServer(zsystem _Ψ,PrimitiveDataIn D) throws IOException
    {
	Ψ = _Ψ;
	Address = D.rs();
    }
    
}


