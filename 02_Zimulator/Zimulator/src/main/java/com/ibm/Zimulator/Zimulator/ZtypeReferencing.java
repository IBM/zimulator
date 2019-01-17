package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.MDA.*;

class ZtypeReferencing
/*
  We keep track here of which A,n point to which 'ztype'.
  A : positive integer starting at 1.
  n : any integer >0.

*/
{
    private ArrayList<ztype> ztypes;
    private SBHH An_indexer;
    private SBHH A_indexer;
	
    public int NumTypes()
    {
	return(A_indexer.size());
    }
    
    public ZtypeReferencing()
    {
	An_indexer = new SBHH();
	A_indexer = new SBHH();
	ztypes = new ArrayList<ztype>();
	An_indexer.getidx("!zeroplaceholder!");  // will be 0.
	A_indexer.getidx("!zeroplaceholder!");  // will be 0.
	ztypes.add(null); // 0 position.
    }

    public String getName(int A)
    {
	return(A_indexer.getstringkey(A));
    }

    public int A_Str_to_int(String S)
    /*
      Get an integer for the given name. 
    */
    {
	return(A_indexer.getidx(S));
    }




    public int An(int A,int n)
    /*
      Used to populate 'An' field in ztype, etc.
    */
    {
	return(An_indexer.getidx(A + "," + n));
    }

    
    /*
      Used only by Zsyntax parser and file IO:
     */
    public void Set_ztype(int A,int n,ztype χ)
    {
	int An = An_indexer.getidx(A + "," + n);
	if (An == An_indexer.size()-1)
	    {
		ztypes.add(χ);
	    }
    }

    public ztype Get_ztype(int An)
    {
	return(ztypes.get(An));
    }
    
    public ztype Get_ztype(int A,int n)
    /*
      This is only used by Zsyntax parser.
    */
    {
	int An = An_indexer.getidx(A + "," + n);
	if ((An<1) || (An>=ztypes.size())) {return(null);}
	return(ztypes.get(An));
    }
}

