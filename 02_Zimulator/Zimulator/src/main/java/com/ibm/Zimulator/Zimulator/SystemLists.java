package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

/*
  Many zobjects exist within one of four 'dynamical' lists, denoted [] or specifically:
  
  [0] [T] [Z] [S]

  0 = Timeless; these are processed for every transition in the system. They are always valid at the system time.
      
  T = With event time. Each of these has an 'expiry time' (or 'next event' time) associated.
  They are valid during an interval [t_0...t_1] whete t_0 ≤ t_sys < t_1
  This list is ordered in increasing expiry time.

  Z = Sleeping.

  S = Static. (things like zpaths). Mostly things do not need to be in the [S] list. This is historical.

  Processing the system at each step involves processing all [0]
  objects in order and the [T] states associates with the first
  time. Objects can be shifted from one list to another when they
  so deserve.

  Examples:
  * Objects in [Z] are 'awakened' and inserted into the [0] section.
  * Zboxen: states S,- are [0] states. States M and D are [T] states. State Z is a [Z] state.
    
  Upon initial loading from source, everything gets added to the [0] list via add().
  Upon loading an object file, everything is loaded into the list it was in when saved.

  [0],[Z],[S] are each implemented as a HashSet. [T] is ordered; it is a TreeSet.
*/

class SystemLists
{
    zsystem Ψ;

    private ArrayList<Set<zobject>> Z_Obj_Lists;
    private HashSet<zobject> ZObs0;
    private TreeSet<zobject> ZObsT;
    private HashSet<zobject> ZObsZ;
    private HashSet<zobject> ZObsS;
    
    private ArrayList<zobject> AdditionBuffer0;  // These get added to [0] after every pass.
    private ArrayList<zobject> AdditionBufferT;  // These get added to [T] after every pass.
    private ArrayList<zobject> AdditionBufferZ;  // These get added to [Z] after every pass.
    private ArrayList<zobject> AdditionBufferS;  // These get added to [S] after every pass.
    private ArrayList<zobject> AdditionBufferD;  // These get Deleted.

    int verbose;
    public SystemLists(int _verbose,   zsystem _Ψ)
    {
	Ψ = _Ψ;
	verbose=_verbose;	
	{
	    Z_Obj_Lists = new ArrayList<Set<zobject>>(4);
	    ZObs0 = new HashSet<zobject>();
	    Z_Obj_Lists.add(ZObs0);
	    //ZObsT = new TreeSet<zobject>( (zobx,zoby) -> ( (zobx.slt < zoby.slt ) ? -1 : ( (zobx.slt > zoby.slt ) ? 1 : 0 ) ) );
	    /*
	      ZObsT = new TreeSet<zobject>( (zobx,zoby) -> ( (zobx == zoby ) ? 0 :
							   ( (zobx.slt == zoby.slt ) ? 0 :
							     (zobx.slt < zoby.slt ) ? -1 : +1 )
							   )
					  );
	    */
	    /*
	    ZObsT = new TreeSet<zobject>( (zobx,zoby) -> ( (zobx == zoby ) ? 0 :
							   (zobx.slt == zoby.slt ) ? -1 :
							   (zobx.slt < zoby.slt ) ? -1 :
							   +1 ) );
	    */
	    ZObsT = new TreeSet<zobject>( new SysList_Tcomparator() );
	    
	    Z_Obj_Lists.add(ZObsT);
	    ZObsZ = new HashSet<zobject>();
	    Z_Obj_Lists.add(ZObsZ);
	    ZObsS = new HashSet<zobject>();
	    Z_Obj_Lists.add(ZObsS);
	}
	AdditionBuffer0 = new ArrayList<zobject>();
	AdditionBufferT = new ArrayList<zobject>();
	AdditionBufferZ = new ArrayList<zobject>();
	AdditionBufferS = new ArrayList<zobject>();
	AdditionBufferD = new ArrayList<zobject>(); // For deletion.
    }


    
    public Iterator<zobject> GetSyslistIterator(int ListID)
    /*
      Returns an iterator for one of the dynamical [] syslists.
      ListID is '0','T','Z','S'
      0,1,2,3 are also accepted.
      Anything else returns null.
      When it came time to write an object file, we used to put
      everyone in a flat list, and lose information.
    */
    {
	Iterator<zobject> wif = null;
	switch(ListID)
	    {
	    case 0 :
	    case '0' :
		wif = ZObs0.iterator();
		break;
	    case 1 :
	    case 'T' :
		wif = ZObsT.iterator();
		break;
	    case 2 :
	    case 'Z' :
		wif = ZObsZ.iterator();
		break;
	    case 3 :
	    case 'S' :
		wif = ZObsS.iterator();
		break;
	    default :
		wif=null;
	    }
	return(wif);
    }


    public LabelReferencing GetExtantLabeledZobjects()
    /*
      Before we modify the system, we need a list of labeled objects.
    */
    {
	LabelReferencing LR = new LabelReferencing();
	for (int l=0;l<4;l++)
	    {
		Iterator<zobject> wif = GetSyslistIterator(l);
		while (wif.hasNext())
		    {
			zobject zo = wif.next();
			if (zo.Label!=null)
			    {
				if (!Str.equals(zo.Label,""))
				    {
					LR.DefineLabel(zo.Label,zo);
				    }
			    }
		    }
	    }
	return(LR);
    };


    private double GetMean_TEST_SysList(Set<zobject> LL)
    {
	double mean_t=0;
	int count_t=0;
	for (int jj=0;jj<10;jj++)
	    {
		Iterator<zobject> zoti = LL.iterator();
		while (zoti.hasNext()) {mean_t += zoti.next().t;count_t++;}
	    }
	mean_t /= count_t;
	return(mean_t);
    }

    
    private void DumpSysList(Set<zobject> C,String Prefix)
    {
	Iterator<zobject> wif = C.iterator();
	int j=0;
	Str.printn(Prefix + "{ ");
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		Str.printn(" ["+j+"] " + zo.Label);
		j++;
	    } 
	Str.print(" }");
    }
    
    
    public void AddFreshlyLoadedObjects(int SysListCode,ArrayList<zobject> zobs)
    /*
      Adds *directly* to [] lists; this should only be called after using InputParser() or CompiledFileIO to load.
      In any dynamical (simulation) case, addition should be done via addToBuffer().

      The zobjects are now filtered. only certain kinds get added to the dynamical lists [].
      In particular, zstop is not added.

      SysListCode is 0,1,2,3 or '0' 'T' 'Z' 'S'
    */
    {
	ArrayList<zobject> zobs_ta = new ArrayList<zobject>();
	ListIterator<zobject> wif = zobs.listIterator();
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		if (zo instanceof zstop) {continue;}
		switch (SysListCode)
		    {
		    case 0:
		    case '0':
			ZObs0.add(zo);
			zo.Current_SysList=0;
			break;
		    case 1:
		    case 'T':
			zo.slt=zo.t;
			ZObsT.add(zo);
			zo.Current_SysList=1;
			break;
		    case 2:
		    case 'Z':
			ZObsZ.add(zo);
			zo.Current_SysList=2;
			break;
		    case 3:
		    case 'S':
			ZObsS.add(zo);
			zo.Current_SysList=3;
			break;
		    }
	    }
    }
    
    public void add_to_Buffer0(zobject zo)
    /*
      Any new objects during simulation get added to the buffer, 
      and then the whole buffer added to the active [0] list.
     */
    {
	AdditionBuffer0.add(zo);
    }
    public void add_to_BufferT(zobject zo)
    {
	AdditionBufferT.add(zo);
    }
    public void add_to_BufferZ(zobject zo)
    {
	AdditionBufferZ.add(zo);
    }
    public void add_to_BufferS(zobject zo)
    {
	AdditionBufferS.add(zo);
    }
    public void add_to_BufferD(zobject zo)
    {
	AdditionBufferD.add(zo);
    }

    private void MoveZobjects(ArrayList<zobject> Objs,int newsyslnum)
    {
	Iterator<zobject> wizt = Objs.iterator();
	while (wizt.hasNext())
	    {
		zobject zo = wizt.next();
		/*
		if (newsyslnum==1)
		    {
   		       if (zo.t < Ψ.t)  // Should never be less than system time.
			    {
				Str.print("PSSS!  Ψ.t=" + Ψ.t +  " zo:" + zo.Label + " zo.t:" + zo.t ); // + "  [" + zo + "]" );
			    }
		    }
		*/
		int syslnum = zo.Current_SysList;
		if (syslnum != -1)
		    {
			Z_Obj_Lists.get(syslnum).remove(zo);
		    }
		zo.slt = zo.t;                      // Only matters for -> [T]
		zo.Current_SysList=newsyslnum;
		if (newsyslnum!=-1)
		    {
			Z_Obj_Lists.get(newsyslnum).add(zo);
		    }
	    }
    }
    public void addAllBuffered()
    {
	//DumpT();
	if (!AdditionBufferD.isEmpty()) { MoveZobjects(AdditionBufferD,-1);AdditionBufferD.clear(); }
	if (!AdditionBuffer0.isEmpty()) { MoveZobjects(AdditionBuffer0,0);AdditionBuffer0.clear(); }
	if (!AdditionBufferT.isEmpty()) { MoveZobjects(AdditionBufferT,1);AdditionBufferT.clear(); }
	if (!AdditionBufferZ.isEmpty()) { MoveZobjects(AdditionBufferZ,2);AdditionBufferZ.clear(); }
	if (!AdditionBufferS.isEmpty()) { MoveZobjects(AdditionBufferS,3);AdditionBufferS.clear(); }
	/*
	if (DumpT())
	    {
		Str.print("!!!!!!!!!!!!!!!!! BAD ORDERING !!!!!!!!!!!!!!!!!!!!!");
		java.lang.System.exit(0);
	    }
	*/	

    }

    public double FirstTimeInTlist()
    {
	double t0 = FirstTimeInTlist_Using_TreeSet();
	/*
	double t1 = FirstTimeInTlist_Brute_Force();
	if (t0!=t1)
	    {
		Str.print("!!!!!!!!!!!!!!!!! BAD ORDERING !!!!!!!!!!!!!!!!!!!!!");		
	    }
	*/
	return(t0);
    }

    public double FirstTimeInTlist_Using_TreeSet()  // Should work. Check.
    {
	return( ZObsT.isEmpty() ?  -1.0 : ZObsT.first().t ) ;
    }

    public double FirstTimeInTlist_Brute_Force()
    {
	if (ZObsT.isEmpty()) {return(-1.0);}
	double tim=-1.0;
	Iterator<zobject> wiz = ZObsT.iterator();
	while (wiz.hasNext())
	    {
		zobject zo = wiz.next();
		if ((tim<0.0) || (zo.t < tim)) {tim=zo.t;}
	    }
	return(tim);
    }



    public int EvolveSysLists(zsystem Ψ,double maxΔt)
    /*
      Process all objects in [0] list until dependencies are resolved.
      Process objects in [T] list corresponding to first time.
      Return # of iterations of [0] list.
     */
    {
	//int verbose = 1;   //DEBUG
	/*
	  Step 1 : Process all zobjects in [0] list.
	  > We Should resolve the discrete state of the [0] list.
	 */
	int M = 0; // How many times to iterate through [0] list.
	int N = 1;   // How many System Changes.
	while (N!=0) // While there are any remaining; this amounts to resolution of the [0] list.
	    {
		if (verbose>0) {V("Processing all zobjects in [0] Syslist");}
		N = ProcessStates(Ψ,ZObs0,maxΔt,true,0);
		addAllBuffered();
		M++;
	    }
	//Str.print("[0]("+ZObs0.size()+") M = " + M);
	/*
	  Step 2: Set the system time to the first in the [T] list.
	  Process Set of objects in [T] who share this first time.
	*/
	{
	    double ft = FirstTimeInTlist();
	    if (ft<0.) {return(-1);}
	    if (ft< Ψ.t) {Str.printE("#543#!!!!!!!!!!!Error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ft="+ft +"   t=" + Ψ.t);}
	    if (verbose>0) {V("SYSTEM TIME: Ψ.t : " + Ψ.t + " -> " + ft +"  :::  " + Str.SecInDayToTime(Ψ.t) +" -> " + Str.SecInDayToTime(ft) );}
	    Ψ.t = ft;
	}
	if (verbose>0) {V("Processing all first zobjects in [T] Syslist");}
	N += ProcessStates(Ψ,ZObsT,maxΔt,false,Ψ.t);
	addAllBuffered();
       
	//DumpToTerminal("# [S]:",ZObsS);	

	if (verbose>0)
	    {
		if (0==1) // This is much too verbosity; it was a test for time-ordering.
		    {
			if (DumpT())
			    {
				Str.print("!!!!!!!!!!!!!!!!! BAD ORDERING !!!!!!!!!!!!!!!!!!!!!");
			    }
		    }
	    }
	if (verbose>0) {V("    . . . " + N + " zobjects processed; [0] iterated " + M + " times; Systime=" + Ψ.t);}
	return(M);
    }

    private int ProcessStates(zsystem Ψ,Set<zobject> C,double maxΔt,boolean AllElements,double Time)
    // Return how many system changes.
    {
	//Str.print("PROCSTA: getting size....");
	//Str.print("PROCSTA: C size = " + C.size());
	int c=0;
	Iterator<zobject> wif = C.iterator();
	while (wif.hasNext())
	    {	    
		zobject zo = wif.next();
		if ( (!AllElements) && (zo.t>Time) ) { break; }

		if (zo instanceof zbox)
		    {
			zbox φ = (zbox) zo;
			φ.mark2 = 0;  // reset mark2 on each pass.
		    }

		zo.StateChange=false;
		int act = zo.ProcessState(Ψ,maxΔt,verbose);
		if (zo.StateChange) { c++; }
		/*
		  0:delete. 1:nochange 2:[]->[0] 3:[]->[T] (with re-sorting) 4:[]->[Z] 5:[]->[S]
		*/

		if ((verbose>0) && (0==1) )
		    {
			if ((act==0)||(act==4)) {V("Removed label:" + zo.Label + " act:" + act);}
			if (act==3) {V(" Label:" + zo.Label + " act:" + act + " ---> [T]");}
		    }
		if (act==0)      {AdditionBufferD.add(zo);}
		else if (act==2) {AdditionBuffer0.add(zo);}
		else if (act==3) {AdditionBufferT.add(zo);}
		else if (act==4) {AdditionBufferZ.add(zo);}
		else if (act==5) {AdditionBufferS.add(zo);}
	    } 
	return(c);
    }



    public boolean DumpT()
    /*
      Returns TRUE if there is an ordering problem!
     */
    {
	Iterator<zobject> wif = ZObsT.iterator();
	int x=0;
	int c=0;
	double LastT = 0;
	boolean PROBLEM=false;
	Str.printn("ZObsT={ ");
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		Str.printn("\t【" + c + "】" + zo.Label + " ( ");
		String rgb="234";
		if (zo.t<LastT) {PROBLEM=true;rgb="500";}	    
		Str.printEn("#"+rgb+"#slt=" + zo.slt +" = " + Str.SecInDayToTime(zo.t));
		Str.printn(" ) ");
		LastT=zo.slt;
		x++; if (x>1) {x=0;Str.printn("\n     ");}
		c++;
	    } 
	Str.print(" } ");
	return(PROBLEM);
    }




    /* ********************************************************************************************* */



    public void PrintAllCounts()
    {
	Str.printnrgb("[0]:" + Str.intdigk(ZObs0.size(),6) ,5,3,3);
	Str.printnrgb(" [T]:" + Str.intdigk(ZObsT.size(),6) ,3,5,3);
	Str.printnrgb(" [Z]:" + Str.intdigk(ZObsZ.size(),6) ,3,3,5);
	Str.printrgb(" [S]:"  + Str.intdigk(ZObsS.size(),6) ,4,4,4);
    }

    public int ReportOnAllZboxen(int r)
    {
	int zc=0;
	zc += ReportOnAllZboxen(ZObs0,r);
	zc += ReportOnAllZboxen(ZObsT,r);
	zc += ReportOnAllZboxen(ZObsZ,r);
	zc += ReportOnAllZboxen(ZObsS,r);
	return(zc);
    }

    private int ReportOnAllZboxen(Set<zobject> C,int r)
    {
	int zc=0;
	Iterator<zobject> wif = C.iterator();
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		if (zo instanceof zbox)
		    {
			zbox φ = (zbox) zo;
			Ψ.Report(DoReportFlags.Channel(φ.R),φ.ReportInfo());
			zc++;
		    }
	    } 
	return(zc);
    }
    
    public void DumpToTerminal()
    {
	DumpToTerminal("# [0]:",ZObs0);
	DumpToTerminal("# [T]:",ZObsT);
	DumpToTerminal("# [Z]:",ZObsZ);
	DumpToTerminal("# [S]:",ZObsS);
    }

	
    public void DumpToTerminal(String Prefix,Set<zobject> C)
    {
	int jtot = C.size();  // Slow.

	Iterator<zobject> wif = C.iterator();
	int j=0;
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		Str.printn(Prefix + j +"/" + jtot+ ": ");
		Str.print(zo.toString());
		j++;
	    }
    }
    

    private void V(String S)
    {
	Ψ.Verb.printnrgb("zoblist",0,5,4);
	Ψ.Verb.printrgb(": " + S ,0,3,5);
    }





    /* -----------------------------------------------------------------------------
       Below routines just for debugging and such.
     */

    public ArrayList<zbox> AllUnContainedZboxen()
    {
	ArrayList<zbox> Uncontained = new ArrayList<zbox>();	
	CollectDump(ZObs0,Uncontained);
	CollectDump(ZObsT,Uncontained);
	CollectDump(ZObsZ,Uncontained);
	CollectDump(ZObsS,Uncontained);
	return(Uncontained);
    }
    private void CollectDump(Set<zobject> C,ArrayList<zbox> Uncontained)
    /*
      Dump zsystem's zboxen to HTML. Maybe this is a reasonable thing to do; maybe not.
    */
    {
	Iterator<zobject> wif = C.iterator();
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		if (zo instanceof zbox)
		    {
			zbox φ = (zbox) zo;
			if (φ.z==null)
			    {
				Uncontained.add(φ);
			    }
		    }
	    }
    }
    


    /*
      USED ONLY FOR DEBUGGING. FINDS A ZBOX. :-d
     */
    
    public zbox Find_a_zbox()
    {
	zbox φ = Find_a_zbox(ZObs0);
	if (φ!=null) {return(φ);}
	φ = Find_a_zbox(ZObsT);
	if (φ!=null) {return(φ);}
	φ = Find_a_zbox(ZObsZ);
	if (φ!=null) {return(φ);}
	//φ = Find_a_zbox(ZObsS);
	//if (φ!=null) {return(φ);}
	return(null);
    }

    private zbox Find_a_zbox(Set<zobject> C)
    {
	Iterator<zobject> wif = C.iterator();
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		if (zo instanceof zbox)
		    {
			zbox φ = (zbox) zo;
			return(φ);
		    }
	    } 
	return(null);
    }





    public void DumpAllFourSysListsZboxen(String Pref)
    {
	for (int sl=0;sl<4;sl++)
	    {
		Iterator<zobject> wif = GetSyslistIterator(sl);
		ArrayList<zobject> ListOfZobs = new ArrayList<zobject>();
		while (wif.hasNext()) {ListOfZobs.add(wif.next());}
		ConnectivityResolution.DumpNonSavedZboxListFields(Pref + " " + sl + " " , ListOfZobs);
	    }
    }
	    
    
    
}

