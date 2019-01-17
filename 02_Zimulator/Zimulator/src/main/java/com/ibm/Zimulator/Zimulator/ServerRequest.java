package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

class ServerRequest
/*
  This is intended to contain the information to be provided when a server is consulted.
 */
{
    /*
      Server- consultation is always based on a zbox:
    */
    private zbox φ;       
    private ArrayList<String> S;  // Serialised in plaintext for server.

    public ServerRequest(zsystem Ψ,zbox _φ)
    /*
      Zpath provided if present.
    */
    {
	φ=_φ;
	// Set up string data so it can be iterated out.
	S = new ArrayList<String>();

	S.add("#start");

	// Indicate why we are consulting the server.
	
	S.add("ZimReq");  //Zimulator request.
	
	// Report on the reference zbox:
	S.add(φ.ReportInfo());

	// Include path:

	{
	    if (φ.P!=null)
		{
		    S.add(φ.P.ReportInfo());
		}	
	}
	
	S.add("#end");
    }
    

    public Iterator<String> iterator()
    {
	return(S.iterator());
    }

  
}
