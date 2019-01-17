package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;


/*
  zobject
  -------
  ProcessState() and parts of the 'CompiledFileRW' implementation should be overridden
  in child objects. The expected behaviour of ProcessState() is described in detail below. 
  
  zobject is never used in a naked sense. Here is only described how
  one might go about adding a new type of object to the system:
  
  Adding a new Object type; zwatermelon.

  InputParser:
  1. Make a new class which extends 'zobject'.
  2. Make a new section === === inside Read_zobject to parse [zwatermelon ... ] input.
  3. Make a new section in ResolveReferences() to resolve references inside a zwatermelon.
  4. Make a new section in ResolveBidirectionalLinks() if necessary.

  zsystem:
  Make some decisions:
  1. Should the watermelon be in any of the [] dynamical lists?
  2. Should the watermelon inherit zobject? (this is usually for embedded things like 'Route')

  CompiledFiles
  1. Should the object be able to be referenced? (zstops cannot, for example)
  2. Implement  CompiledFileRW. (This is overridden if it extends zobject).
     Implement Write, Read, Resolve functions.
     Implement GetFileRefNumber() and GetFileRefNumber() if not a zobject.

*/

/*
  This is just a generic parent.
 */
class zobject implements CompiledFileRW
{
    //2018-10-24: Current system list [0] [T] [Z] [S] which this zobject is in, or null.
    int Current_SysList;  // or -1.

    double slt;   // t for sorting whenever we insert into [T]. See AdditionBufferT in SysLists.
    
    String Label;
    // Select for special verbose attention. Command-line assignment v=Label,Label,...    
    boolean Follow;   

    boolean DeletionFlag;   // if ever set to true, removed from system. See SysLists [].
    
    // Used for filing references.
    private int mark3;  

    // Only utilised for dynamical zobjects:
    public double t;
    public boolean StateChange;

    // Only needed for comparator in SysLists.
    static int UniqueID_Accumulator;    // We are limited to 2G zobjects.
    int uid;

    /*
      Each of the following is overridden by an object-specific method.
    */

    public zobject()
    {
	Current_SysList=-1;
	uid = UniqueID_Accumulator++;   // only used in SysList_Tcomparator.
	Label=null;
	Follow=false;
	DeletionFlag=false;
    }

    // Over-ride java Object hashCode if we require uniqueness. Bad dist'n though.
    /*
      public int hashCode()
      {
        return uid;
      }
    */
    
    public String toString()
    {
	return("");
    }

    public String toString(zsystem Ψ)
    {
	return(toString());
    }
    
    public String reportState()
    /*
      This is for dumping [real-time] simulation output.
     */
    {
	return("");
    }




    public int ProcessState(zsystem Ψ,double maxΔt,int verbose)
    /*
      This method should always use NT.update() to indicate any transitions in the system.

      ------------------------------------------
      This method always returns a SysListCode:
      ------------------------------------------

      0 = Delete from whichever syslist it is in ([0] [T] etc.).
          The method is responsible for disconnecting the zobject from everything.
	  0 just indicates that the zsystem can drop it and not parse it anymore.

      1 = Keep the object in whichever SysList it is in already.
          ( 't' field above must be valid if this is [T]. )
	  Return this if there is NO CHANGE in object's state, 
	  and it is not waiting for another state in [0] to change.
	  If waiting for a 'M' or 'D' zbox state ( i.e. one in [T] ),
	  then it is all right to return 1.

      2 = Move to Syslist [0].  (or stay in it)

      3 = Move to Syslist [T] (or stay in it); it will be inserted in the correct position.
          't' field above must be valid if this is [T].

      4 = Move to Sleeping List [Z]. (or stay in it)   ( Right now, functionally but not conceptually the same as 0 )

      5 = Move to Static List [S]. (or stay in it)  ( Right now, functionally but not conceptually the same as 0 )

     */
    {
	return(5); // If we don't even get overwritten by another (say, zlink) then we are definitely static.
    }    



    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    /*
      This should be overridden by each object type.
      If we forget, we're write a 'z' for generic zobject.
      Take care that it can be expanded later!.
      First byte should be the object type which determines frame size.
     */
    {
	D.wb('z');
	D.wi(GetFileRefNumber());
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	ORL.add(OR);
    }
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	return(true);
    }

    public void SetFileRefNumber(int n)
    {
	mark3 = n;
    }
    public int GetFileRefNumber()
    {
	if (mark3==0)
	    {
		Str.print("REFERENCED as ZERO! Label:" + Label);
		zbox φ = null;
		φ.x ++; // make an exception.
	    }
	return(mark3);
    }


}
