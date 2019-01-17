package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

/*
  How Compiled Object-File Input-Output works
  -------------------------------------------

  0. All zobjects and others to be written implement the 'CompiledFileRW' interface.

  Writing state to file: CompiledFileIO.WriteStateToZObjFile().
  -------------------------------------------------------------

  Principle: 'zobjects' are written in a way so they can be referenced. 
             Other objects referenced by them are written "in-line".

  1. File is opened and 'PrimitiveDataOut' object created. Magic number written.

  2. ALL zobjects in [] lists are put into a flat list, and given integer labels via SetFileRefNumber().
     (For example, zstops are not referenced in this way. We store them in-line in zpaths; analogous to Route.
     These things are not in [] syslists)

  3. The list is iterated through, and the method WriteToObjFile() is called for each object. Inside 
  that method, numerical and String fields are written using the passed 'PrimitiveDataOut' object. 
  To write references TO ZOBJECTS, write the integer obtained by GetFileRefNumber().

  Reference integers: 1...  = reference to a zobject.
                      0     = referencing Error. The referenced object is missing! (0 is chosen since it is java int[] default.)
		              Objects which should not be referenced in files (zstop, Route, Economics, etc. use this).
		      -1    = Used as end-of-list marker for lists of references which are written to file without indication of length.
		      -2    = null. Sometimes a field in an object is 'null' and this is legitimate.

  Every object written to file starts with a single byte indicating which zobject it is.
  These have been chosen to be unicode in first page. (e.g. 'b' for zbox, 'l' for zlink...)
  Lowercase for [] zobjects, and upper-case for others. '0' is used for EoF.

  The format within each object is entirely free; WriteToObjFile() merely must match RedObjFromFile() 
  for the same object.

  zobjects (for [] lists) are written including their integer reference numbers after the initial 
  byte marker. Other objects have no reference numbers and are written 'in-line' within the zobjects 
  which contain (i.e. reference) them.

  Thus, the first two lines in zbox.WriteToObjFile(PrimitiveDataOut D) are:
	D.wb('b');  //zbox.
	D.wi(GetFileRefNumber());

  Reading system state from file: CompiledFileIO.ReadStateFromZObjFile()
  ----------------------------------------------------------------------
  
  Principle: Same as writing. Reference-resolution should be fast.

  0. We expect an empty (fresh) zsystem (see Zimulator class) with a few things initialised.

  1. File is opened and 'PrimitiveDataIn' object created. Magic number read.
     Empty Reference List initialised.	ArrayList<ObjRef> rlist;

  2. Repeatedly:
     Read indicator byte and then call ReadFromObjFile() for appropriate zobject.
     There is no "type-safe" way to do this, since the file is just a stream of bytes. 
     For each type of zobject, the constructor is called. References to other zobjects (in [])
     are integers, and are placed in a 'ObjRef' object which is added to the Reference List.
     References to other objects (non-zobjects like Economics) are loaded in-line and initialised;
     they also sport a ReadFromObjFile() method, which also adds an ObjRef to the Reference List.
 
  3. Construct a Deferencing Array whose index is the reference integer. 
     Store all object references in this, for fast dereferencing.

  4. Iterate through the full Reference List and for each object therein, call that object's 
     ResolveObjRefs() to fill in any references to zobjects and other objects.

  5. All zobjects loaded are added into the system's syslists [] so that they can be processed.
     zstops are 'in-line' inside zpaths. When we load them, we do not insert them into [] lists.
     
  2* While loading, an indicator byte of '[' denotes to shift to one of the syslists.
     The next byte should be '0' 'S' 'T' or 'Z'.

*/

public class CompiledFileIO
{
    public static boolean ReadStateFromZObjFile(zsystem Ψ,DataInputStream dis) throws IOException
    /*
      We should start with a fresh zsystem which has been constructed.
      This performs exactly the same function as InputParser() and then IP.InsertIntozsystem(Ψ).

      Full explanation is above.
    */
    {
	PrimitiveDataIn PDI = new PrimitiveDataIn(dis);
	if (!PDI.valid()) {return(false);}
	
	if (!Str.equals(PDI.rs(),"ZOv001")) {Str.printE("#500#Wrong file type. Aborting.");return(false);}

	ArrayList<ArrayList<zobject>> AllLoadedZobjects = new ArrayList<ArrayList<zobject>>();
	for (int sl=0;sl<4;sl++)
	    {
		AllLoadedZobjects.add(new ArrayList<zobject>());
	    }
	int SysListSelector=0; // 0,1,2,3 := '0','T','Z','S'.

	ObjReferenceList RL = new ObjReferenceList();
	boolean verbose=false;

	boolean EoF = false;
	while(!EoF)
	    {
		byte ObTypeIndicator;
		try {
		    ObTypeIndicator = PDI.rb();
		} catch (IOException ioe) {break;}
		//Str.printEn("#550#'" + Str.setUni(ObTypeIndicator) + "#040#'(" + ObTypeIndicator + ")");
		switch(ObTypeIndicator)
		    {
		    case 'z' :  // zobject
			Str.printE("#115# Undefined zobject. :-/");
			throw(new IOException());
		    case 'W' :  // World.
			Ψ.ReadFromObjFile(RL.rlist,PDI);
			break;
		    case 'b' :  // zbox
			zbox φ = new zbox(Ψ);
			φ.ReadFromObjFile(RL.rlist,PDI);
			AllLoadedZobjects.get(SysListSelector).add(φ);
			break;
		    case 'l' :  // zlink
			zlink χ = new zlink(Ψ);
			χ.ReadFromObjFile(RL.rlist,PDI);
			AllLoadedZobjects.get(SysListSelector).add(χ);
			break;
		    case 't' :  // ztype
			ztype e = new ztype();
			e.ReadFromObjFile(RL.rlist,PDI);
			Ψ.ZtypeRefs.Set_ztype(e.A,e.n,e);                      // Add to our (a,n) -> χ map.
			AllLoadedZobjects.get(SysListSelector).add(e);
			break;
		    case 'p' :  // zpath
			zpath P = new zpath(Ψ);
			P.ReadFromObjFile(RL.rlist,PDI);
			AllLoadedZobjects.get(SysListSelector).add(P);
			break;
			/*
			  case 's' :  // zstop  ** See notes on zstops above. ** Not included directly, since not in syslists.
			*/
		    case 'd' :  // zdemand
			zdemand D = new zdemand(Ψ);
			D.ReadFromObjFile(RL.rlist,PDI);
			AllLoadedZobjects.get(SysListSelector).add(D);
			break;
		    case 'h' :  // zschedule
			zschedule H = new zschedule(Ψ);
			H.ReadFromObjFile(RL.rlist,PDI);
			AllLoadedZobjects.get(SysListSelector).add(H);
			break;
		    case 'o' :  // zsource
			zsource S = new zsource();
			S.ReadFromObjFile(RL.rlist,PDI);
			AllLoadedZobjects.get(SysListSelector).add(S);
			break;
		    case '[' :  // switch to syslist.
			SysListSelector=PDI.rb(); // 0,1,2,3.
			Str.print("::::::::::::::: Reading Syslist: " + SysListSelector);
			break;
		    case '0' :  // EoF.
			EoF = true;
			Str.printE("#040#Reached end of object file. #115#:-)");
			break;
		    default :
			Str.printE("#540#BAD TYPE: '#044#" + Str.setUni(ObTypeIndicator) + "#040#'("+ObTypeIndicator+")");			
			throw(new IOException());
		    }
	    }
	Str.printE("#335#	  Setting up a table to look up integer references.");

	/*
	  Set up Derefencing Array so we can look up integer references quickly.
	 */
	{
	    ListIterator<ObjRef> wob;
	    wob = RL.rlist.listIterator();
	    int MaxIndex = 0;
	    while (wob.hasNext()) { MaxIndex = Math.max(MaxIndex,wob.next().C); }
	    RL.deref = new int[MaxIndex+1];
	    int jlim = RL.rlist.size();
	    for (int j=0;j<jlim;j++)
		{
		    int C = RL.rlist.get(j).C;
		    if (C!=-1)
			{
			    RL.deref[C] = j;
			}
		}
	}

	/*
	  Now, we must iterate once more and resolve all integer references in these objects.
	*/
	Str.printE("#345#          Resolving references in all loaded objects.");
	{
	    ListIterator<ObjRef> wob = RL.rlist.listIterator();
	    while (wob.hasNext())
		{
		    ObjRef OR = wob.next();
		    if (OR.O instanceof zobject)
			{
			    zobject zo = (zobject) OR.O;
			    /*
			    if (zo instanceof zlink)
				{
				    Str.printE("#050# zlink!! CFIO #343#" + zo.toString());				    
				}
			    */
			    //Str.print("__________________________zo" + zo.Label);
			    zo.ResolveObjRefs(RL,OR,Ψ);
			    //Str.print("^^^^^^^^^^^^^^^^^^^^^^^^^^zo");
			}
		    else if (OR.O instanceof Route)
			{
			    Route R = (Route) OR.O;
			    R.ResolveObjRefs(RL,OR,Ψ);
			}
		    else if (OR.O instanceof Economics)
			{
			    Economics E = (Economics) OR.O;
			    E.ResolveObjRefs(RL,OR,Ψ);
			}
		}
	}

	for (int sl=0;sl<4;sl++)
	    {
		ArrayList<zobject> ListOfZobs = AllLoadedZobjects.get(sl);
		ConnectivityResolution.zpaths_zlinks_zboxen(ListOfZobs);
		// ConnectivityResolution.zboxen_containment(ListOfZobs); // not needed since we store them all.
	    }

	/*
	  Now, this is just as InputParser(). We can add them all.
	*/
	Str.printE("#335#	  Adding all fresh objects to system.");	
	
	for (int sl=0;sl<4;sl++)
	    {
		Str.print("::::::::::::::: Adding Syslist: " + sl + " with " +AllLoadedZobjects.get(sl).size()+ " zobjects");
		Ψ.AddFreshlyLoadedObjects(sl,AllLoadedZobjects.get(sl));
	    }

	Str.printE("#335#	  Completed reading system state from object file.");	

	/*
	  DUMP after reading OBJ file. DEBUG.
	*/
	if (0==1)
	    {
		Ψ.System_Z_Lists.DumpAllFourSysListsZboxen("OBJ");
	    }
	
	
	
	
	
	return(true); // All was well.
    }
    
    public static int WriteStateToZObjFile(zsystem Ψ,DataOutputStream dos) throws IOException
    {
	/*
	  First, we need a  list of all zobjects in the system.
	  The following tags every zobject save the single zsystem.
	  This is commensurate with InputParser loading all zobjects into an array,
	  except for the empty zsystem it is given to begin with.
	  (except we keep track of who is in [0], [T], [S] and [Z].)
	*/

	Ψ.System_Z_Lists.addAllBuffered();
	{
	    int RefNum=1;
	    for (int syslistnum=0;syslistnum<4;syslistnum++)
		{
		    Iterator<zobject> wif = Ψ.System_Z_Lists.GetSyslistIterator(syslistnum); // [0] [T] [Z] [S]
		    while (wif.hasNext())
			{
			    zobject zo = wif.next();
			    zo.SetFileRefNumber(RefNum);
			    if (0==1) //debug.
				{
				    if (zo.Label!=null)
					{
					    Str.print("[" +syslistnum+ "] zo.Label=" + zo.Label + "  RefNum:" + RefNum);
					}
				}
			    RefNum++;
			}
		}
	}

	/*
	  These now have integer indices for reference.
	  ( If we do not save some to the file, that is all right; they just will not all get used.)
	  So that we can quickly reference back and forth, mark3 is set to the index.
	  It is referenced in the file-writing routines as GetFileRefNumber().
	 */


	/*
	  DUMP before writing OBJ file. DEBUG.
	*/
	if (0==1)
	    {
		Ψ.System_Z_Lists.DumpAllFourSysListsZboxen("OBJ");
	    }
	
	PrimitiveDataOut PDO = new  PrimitiveDataOut(dos);
	if (!PDO.valid()) {return(1);}

	PDO.ws("ZOv001");  // Magic number.
	Ψ.WriteToObjFile(PDO);
	for (int syslistnum=0;syslistnum<4;syslistnum++)
	    {
		Iterator<zobject> wif = Ψ.System_Z_Lists.GetSyslistIterator(syslistnum); // 0,1,2,3 := [0] [T] [Z] [S]
		PDO.wb('[');
		PDO.wb(syslistnum);
		Str.print("::::::::::::::: Writing Syslist: " + syslistnum);
		while (wif.hasNext())
		    {
			zobject zo = wif.next();
			//if ((syslistnum==3)&&(zo.Label!=null)) {Str.print("LABEL:" + zo.Label);}
			zo.WriteToObjFile(PDO);
		    }
	    }
	PDO.wb('0');  // end of .zo file.
	return(0);
    }
    
       
}


class ObjReferenceList implements ResolveRef
{
    ArrayList<ObjRef> rlist;
    int[] deref; // De-referencing array
    
    public ObjReferenceList()
    {
	rlist = new ArrayList<ObjRef>();	    
    }
    
    public Object getref(int idx) 
    {
	if (idx==-2) {return(null);}
	if (idx==0) { Str.print("Bad reference!"); return(null); } 
	return(rlist.get(deref[idx]).O);
    }
}
 
