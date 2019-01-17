package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;


/*
  zsystem; also called zsystem

  This class holds the system; the system lists [] are inside System_Z_Lists.

  "Verbose" output is defined to be human-readable, to be watched while a simulation is running, for example.
  It is subject to no particular requirements and is usually terminal-coloured and sloppy.

  "Report" output has specific form and all passes through the R() method below. It is intended to be 
  machine-parsed but flexible, so key=value pairs (and sometimes key=value=hvalue to attach some human readability (e.g. times))
  are utilised.

 */


class zsystem implements CompiledFileRW
{
    boolean ReportAllOnceFlag;
    
    zio AllSystemIO;  // 2018-09-13. All IO goes through this.
    Verbose Verb;
    
    String Label;

    Distribution ND;  // We use this to generate velocities from a zsource.
    
    SystemLists System_Z_Lists;  // zboxen in system.
        
    private int mark;  // number for marking. See FreshMark() below.
    private int mark2;  // number for marking. See FreshMark() below.
    
    /*
      For ztypes, the user supplies type Name and Number in input files. 
      Names are resolved to integers using this.
    */
    ZtypeReferencing ZtypeRefs;

    public String ServerConsultationFlags; // Server-consultation flags, in ztype						   

    /*
      Some global parameters.
    */
    public double T__0;   // All times relative to this.
    public double t_0,t_1;
    public double Δt;   // maximum Δt for simulation. Defaults to 10 s.

    public ArrayList<ExternalServer> Ξ;  // List of servers which can be consulted.

    public ConstantValues CV;
    
    ReportFlags R;  // As in zbox or ztype. Reporting for whole system.
    
    int verbose;   // General verbosity; extensive for debugging. Pretty Terminal colours.
    int verboseSS; // Output system counts at each step. nice for progress. Clears terminal each time.
    int verboseSS_zl; // When verboseSS is nonzero, this is how many lines to display. Should be >20 or so. or else 1.
    String[] VerboseList; // List of labels to make verbose, with command-line v= assignment.
    
    int cacheResolvedZpathsFlag;
    int cacheNeighbourhoodsFlag;
    int enableZdemandFlag;
    int allowZboxSleepingFlag;


    public boolean SleepWhenFull;  // see zbox.java for usage. Just set to true.

    public boolean pauseoneveryshift;
    
    public Containment_Controller ConCon;
    
    /*
      System time. 
      All discrete-state objects are valid at t, and all continuous objects span t.
      This is true before and after a call to EvolveΔt().
    */
    double t;

    
    public zsystem(zio _AllSystemIO,int _verbose,boolean verbosetermcolour)
    {

	/*
	  A normal distribution from which to sample.
	*/
	ND = new Distribution(_x_ -> Math.exp(- _x_*_x_/2.0),0.0,1.0,-20,20,1000,1e-5);

	SleepWhenFull = true;   // see zbox.java for usage. This is only set here. false is for debugging.

	pauseoneveryshift = false;
	AllSystemIO = _AllSystemIO;

	Verb = new Verbose(AllSystemIO,verbosetermcolour);
	
	CV = new ConstantValues();

	ConCon = new Containment_Controller();
	
	verboseSS = 0;
	verbose = _verbose;
	cacheNeighbourhoodsFlag = 1;
	cacheResolvedZpathsFlag = 1;
	enableZdemandFlag = 1;
	allowZboxSleepingFlag = 1;
	mark = 1;

	// We do not initialise much here. Look in InputParser class.
	System_Z_Lists = new SystemLists(verbose,  this);
	
	Δt = 10.0; //default.

	ZtypeRefs = new ZtypeReferencing();
    }
    
    public boolean IsInVerboseList(String label)
    /*
      This is a search.
      It is only called when we generate a new label, say in zdemand() or from a prototype,
      so that the user may specify verbosity for zobjects which are not explicitly in the input.
    */
    {
	//Str.printE("         #050#IsInVerboseList(#511#" + label +"#050#)");	
	if (VerboseList==null) {return(false);}
	for (int e=0;e<VerboseList.length;e++)
	    {
		if (VerboseList[e]==null) {continue;}  // e=0 will be null.
		if (Str.equals(VerboseList[e],label)) {return(true);}
	    }
	return(false);
    }
    



    public void InsertZsyntax_IntoSystem(Iterator<String> Zsyntax,boolean verb)
    {
	ZsyntaxInputParser Zinpa; // Input parser requires only this scope.
	// Stage I : we need all extant zobjects for referencing.
	LabelReferencing AllSystemLabels = System_Z_Lists.GetExtantLabeledZobjects();
	Zinpa = new ZsyntaxInputParser(AllSystemLabels,verb,true,this);
	// Stage II: Parse the provided input: 
	try
	    {
		Zinpa.ParseInputSource(Zsyntax);
	    }
	catch (IOException ioe)
	    {
		Str.print("================INPUT ERROR; aborting.===================");
		return;
	    }
	SelectVerboseLabels(VerboseList,AllSystemLabels);
	// Stage III: Insert all those objects into our system:
	// t = t_0;   //2018-09-06 Why did we have this?!?!
	Zinpa.InsertIntozsystem(t);
    }




    public static void SelectVerboseLabels(String[] labels,LabelReferencing SystemLabels)
    /*
      v=label,label,bale... may have been provided.
      Set 'Follow' flag in objects which are specified.
      labels array starts at 1.
    */
    {
	if (labels==null) {return;}
	Class C_zobj = null; // new zobject().getClass();
	for (int l=1;l<labels.length;l++)
	    {
		if (labels[l]==null) {return;}
		zobject zo = SystemLabels.ZobjByLabel(labels[l]);
		if (zo!=null)
		    {
			zo.Follow=true;
		    }
	    }
    }





	
    public static void InvalidateCaches(zobject zob,int BeforeOrAfter)
    /*
      Invalidates caches associated with a zbox or zlink.

      Note that this is wasteful currently. 
      A master Λ~~ set equal to the union of all the Λ῀ sets should first be generated,
      and then all zboxen within have their caches eliminated.
      
      ( Treating ztypes carefully, of course. )

    */
    {
	if (zob instanceof zbox)
	    {
		zbox φ = (zbox) zob;
		φ.InvalidateCaches(BeforeOrAfter);   // Invalidates all caches associated with implicit zlink of φ.
	    }
	if (zob instanceof zlink)
	    {
		zlink λ = (zlink) zob;
		for (int j=0;j<λ.A_A.length;j++)
		    {
			int A = λ.A_A[j];
			int n = λ.A_n[j];
			if (λ.μ!=null) {λ.μ.InvalidateCaches(A,n,BeforeOrAfter);}
			if (λ.ν!=null) {λ.ν.InvalidateCaches(A,n,BeforeOrAfter);}
		    }
	    }
    }
    
    public static void InvalidateCaches(ArrayList<zobject> zobs,int BeforeOrAfter)
    /*
      Now, given a number of new or modified objects 'zobs', Λ neighbourhoods must be managed.
      
      The way to accomplish this is:

      1. Consider all affected zobjects. The list 'zobs' is not only the added ones; it is all affected.
         ( 
           BeforeOrAfter = 0 : all which will be affected.
           BeforeOrAfter = 1 : all which have been affected. 
           See zbox.InvalidateCaches() for the meaning of this argument.
         )
      2. For every zbox, call InvalidateCaches() for implied zlink.
      3. For every zbox, also call InvalidateCaches() to invalidate Λ sets for all ztypes.
    */
    {
	Iterator<zobject> wif = zobs.iterator();
	while (wif.hasNext()) { InvalidateCaches(wif.next(),BeforeOrAfter); }
    }
    
    public void AddFreshlyLoadedObjects(int sl,ArrayList<zobject> zobs) // sl=0,1,2,3 := [0],[T],[Z],[S]
    /*
      Give a list of zobjects and also indicate which of the syslists is to be taregeted.
      Whichever of the zobjects is appropriate for list-insertion will be added.

      i.e. zboxen will be added. zstops will not.
    */
    {
	/*
	  It is not possible to do the 'before' step of Λ invalidation here; it is too late!
	  Therefore, each modified zobject had Λ caches invalidated as it was identified: 
	  ZsyntaxInputParser.InvalidateZobjectΛ().
	  
	  Here, then, it is only necessary to perform the 'After' step of Λ invalidation.
	*/
   	System_Z_Lists.AddFreshlyLoadedObjects(sl,zobs);
	InvalidateCaches(zobs,1);  // The 'After' step.	
    }

    public void EvolveUntil()
    {
	EvolveUntil(t_1,Δt);
    }

    public void EvolveUntil(double t_final)
    {
	EvolveUntil(t_final,Δt);
    }

    class Speedometer
    {
	long   OutsideTime0_ns;
	double ZimulatorTime0_s;

	long   OutsideTime_ns;
	double ZimulatorTime_s;

	double instperf;
	Speedometer()
	{
	    OutsideTime0_ns = System.nanoTime();
	    ZimulatorTime0_s = t;
	    OutsideTime_ns = OutsideTime0_ns;
	    ZimulatorTime_s = ZimulatorTime0_s;
	}
	double update()
	{
	    long OutNow_ns = System.nanoTime();
	    double ZimNow_s = t;
	    double ZimDif = (ZimNow_s - ZimulatorTime_s);
	    double OutDif = (OutNow_ns - OutsideTime_ns) / 1.0e9;
	    double perf=0;
	    if (OutDif>0) {perf = ZimDif/OutDif;}   // We pretend, as is tradition, that 0 ≡ ∞
	    OutsideTime_ns = OutNow_ns;
	    ZimulatorTime_s = ZimNow_s;
	    instperf = perf;
	    ZimDif = (ZimNow_s - ZimulatorTime0_s);
	    OutDif = (OutNow_ns - OutsideTime0_ns) / 1.0e9;
	    perf=0;
	    if (OutDif>0) {perf = ZimDif/OutDif;}
	    return(perf); // avg performance.
	}
	double inst()
	{ // should have called update first.
	    return(instperf);
	}	
    }
    
    public void EvolveUntil(double t_final,double maxΔt)
    {
	Speedometer Speedo = new Speedometer();  // instantaneous
		
	if ((t_final-t)<maxΔt) { maxΔt = t_final-t; }
	int times=0;
	double lastt=-1;
	int N=0;
	boolean finished = false;
	while (!finished)
	    {
		if ((t_final-t)<=maxΔt)
		    {
			maxΔt = t_final-t;
			finished=true;
			if (maxΔt==0)
			    {
				break;
			    }
		    }
		times++;
		R(); // Report.
		lastt = t;
		N = System_Z_Lists.EvolveSysLists(this,maxΔt);  // N is iterations of [0] list.
		
		if (verboseSS != 0)
		    {
			if (MLR.floor(t,verboseSS)!=MLR.floor(lastt,verboseSS))
			    {
				double PerfoFactorA = Speedo.update();
				double PerfoFactorI = Speedo.inst();
				if (verboseSS_zl>1)
				    {    
					Str.printn("\033[2J\033[1;1H"); // Clear terminal & Home.
					Str.printE("#444#X=" + times +" T_0=" + T__0 + " t_0=" + Str.SecInDayToTime(t_0)
						   + " t_1=" + Str.SecInDayToTime(t_1) + "#151*# t=" + Str.SecInDayToTime(t));
					Str.printE("#532#" + MLR.ftos(PercentComplete(),2) + "%  "
						   + "#235#" + MLR.ftos(PerfoFactorI,2) + "⨯ #345#(avg "+MLR.ftos(PerfoFactorA,2)+"⨯)"
						   + "#511# N[0]=" + N + " // marks:" + mark + "," + mark2);
					Str.printEn("#_#zobjects:#0# ");System_Z_Lists.PrintAllCounts();
					BoxCounter M = new BoxCounter(this,0);
					M.Report("| ",verboseSS_zl);
				    }
				if (verboseSS_zl==1)
				    {
					Str.printEn("#444#X=" + times + " t_0=" + Str.SecInDayToTime(t_0) + " t_1=" + Str.SecInDayToTime(t_1)
						    + "#151*# t=" + Str.SecInDayToTime(t) + " #532#" + MLR.ftos(PercentComplete(),2) + "% "
						    + "#235#" + MLR.ftos(PerfoFactorI,2) + "⨯ #345#(avg "+MLR.ftos(PerfoFactorA,2)+"⨯)"
						    + "      \r"); // just carriage return; no LF.
				    }	
				Report(-1," PERF: " + t + "," + PerfoFactorI );
			    }
		    }
		if (N<0)
		    {
			Report(-1,"No new transition time found. System in static state. Halting.");
		    break;
		}
	    }
	// System_Z_Lists.DumpT();	     // For debugging; dumps [T] to terminal.
	// System_Z_Lists.DumpToTerminal();  // For debugging; dump syslists to see if anything weird.
    }

        
    public zbox AddCopyOfPrototype(zbox γ)  
    {
	zbox φ = γ.CopyOf(true);
	return(φ);
    }
    

    public int FreshMark()
    {
	mark++; if (mark>1000000000) {mark=1;}
	return(mark);
    }
    public int FreshMark2()
    {
	mark2++; if (mark2>1000000000) {mark2=1;}
	return(mark2);
    }

        
    HashSet<zpath> AllActivePathsThroughZbox(zbox zb3,int N)
    /*
      Active = with at least N followers.

      Reject marked paths, except for IP.
     */
    {
	if (N==0) {return(zb3.PathsWhoStopHere);}
	if (zb3.PathsWhoStopHere==null) {return(null);}
	HashSet<zpath> L = new HashSet<zpath>();
	for (zpath P : zb3.PathsWhoStopHere)
	    {
		if (P.Followers.size()>=N)
		    {
			L.add(P);
		    }
	    }
	return(L);
    }

    /*

    ArrayList<zschedule> AllSchedulesWithPathsThroughZbox(zbox zb3)
    {
	ArrayList<zschedule> L = new ArrayList<zschedule>();	
	Iterator<zobject> wit = System_Z_Lists.getIterator();
	while (wit.hasNext())
	    {
		zobject zo = wit.next();
		if (zo instanceof zschedule)
		    {
			zschedule Sch = (zschedule) zo;
			if (Sch.P.IsVisited(zb3))
			    {
				L.add(Sch);
			    }
		    }
	    }
	return(L);
    }

    /*
      All verbose output for zsystem goes through here.
     */
    private void V(String S)
    {
	Verb.printnrgb("zsystem   t=" + t +"=" + Str.SecInDayToTime(t),0,5,4);
	Verb.printrgb(" : " + S ,0,3,5);
    }

    public double PercentComplete()
    {
	return(100.0 * (t - t_0) / (t_1 - t_0));
    }
    
    private void R()
    {
	if (!DoReportFlags.Anything(R)) {return;}  // only mode 1 'self' for zsystem.
	String lin="";
	
	// 2018-10-17:
	
	lin +="zsystem T_0=" + T__0 + " t=" + t + " %=" + MLR.ftos(PercentComplete(),3);

	// + " T_0H=" + Str.SecInDayToTime(t) + " tH=" + Str.SecInDayToTime(t);
	//lin += " NT.t=" + NT.t + " NT.tH= " + Str.SecInDayToTime(NT.t);
	//if (NT.φ!=null) {lin += " NT.φ=" + NT.φ.Label;}	
	Report(-1,lin);
    }

    /*
      All .zout reporting, from all objects, passes through this.
    */
    public void Report(int Channel,String P)
    {
	if (Channel == -1) {Channel = DoReportFlags.Channel(R);}
	AllSystemIO.sendReportLine(Channel,CV.ReportPrefix + P);
    }

    private void E(String S)
    {
	Str.printnrgb("zsystem t=" + t +" ",5,5,4);
	Str.printrgb(": " + S ,5,3,5);
    }

           


    // ******************************************************************
    // Implementation of 'CompiledFileRW'
    // ******************************************************************    

    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    /*
      Does not go into any object lists; only writes our local things.
      See CompiledFileIO.java.
    */
    { //     Label,TypeNames,T_0,t_0,t_1,Δt,t,R
	D.wb('W');  //zsystem.
	D.ws(Label);     // We write our label first. :-D
	int numtypes = ZtypeRefs.NumTypes();
	D.wi(numtypes);   // 1...this.
	if (numtypes>1)
	    {   // Only the names really need be stored.
		for (int A=1;A<numtypes;A++)
		    {
			D.ws(ZtypeRefs.getName(A)); 
		    }
	    }
	if (Ξ==null) {D.wi(0);} else
	    {
		D.wi(Ξ.size());
		if (Ξ.size()!=0)
		    {
			ListIterator<ExternalServer> wif = Ξ.listIterator();
			while (wif.hasNext())
			    {
				ExternalServer es = wif.next();
				es.WriteToObjFile(D);
			    }
		    }
	    }
	D.wd(T__0);
	D.wd(t_0);
	D.wd(t_1);
	D.wd(Δt);
	D.wd(t);    // This is t_1 if we have finished a simulation. We might stop in the middle for various reasons though.
	DoReportFlags.WriteToObjFile(D,R);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	Label = D.rs();
	int numTypes = D.ri();
	if (numTypes>1)
	    {
		for (int A=1;A<numTypes;A++)
		    {
			ZtypeRefs.A_Str_to_int(D.rs());
		    }
	    }
	/*
	  We intend to continue the simulation which was saved.
	  Therefore, old t_0 -> t_0.
	  Therefore, old t_1 -> t_1.  (we might get a new one which will override this).
	  Therefore, old t -> t.
	*/
	Ξ = new ArrayList<ExternalServer>();
	int Ξn = D.ri();
	for (int j=0;j<Ξn;j++)
	    {
		Ξ.add(new ExternalServer(this,D));
	    }
	T__0 = D.rd();
	t_0 = D.rd();
	t_1 = D.rd();
	Δt = D.rd();
	t = D.rd();
	//Str.print("========================== read in zsystem time t=" + Str.SecInDayToTime(t));
	R = DoReportFlags.ReadFromObjFile(ORL,D);
    }
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	return(true);
    }
    
    public void SetFileRefNumber(int n) {}
    public int GetFileRefNumber() {return(0);}
  

}
