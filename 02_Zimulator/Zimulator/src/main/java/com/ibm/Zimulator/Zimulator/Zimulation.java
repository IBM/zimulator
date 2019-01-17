package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

public class Zimulation implements zim
/*
  This is the class which needs to be constructed to perform simulation.
  
  When invoked via the command line, the CommandLine class calls this.
*/
{
    public Zimulation(zio IO_for_Simulation)
    {
	AllIO = IO_for_Simulation;
	if (AllIO==null)
	    {
		Str.print("! Zimulation() called without zio implementation.");
	    }
	verboseflag=0;
	noneighcache=false;
	nozdemand=false;
	nosleeping=false;
	VerboseList=null;
	StoppingTime_t_1 = null;
	StartingTime_t_0 = null;     // Careful, since an arbitrary starting-time may not make sense, depending on inputs.
	dumpflag=false;
	suppresssimulationflag=false;
	outhtmlfile=null;
	outpsfile=null;
	verbosetermcolour=false;
	sleeponeveryshift=false;
	ReportAllOnceFlag=false;
    }

    public void adjustFlags(String[] VarValPairs)
    {
	String[] vvp = VarValPairs;
	verboseflag = Str.IsOptionPresent(vvp,"v") ? 1 : 0;
	noneighcache = Str.IsOptionPresent(vvp,"noneighcache");
	nozpathcache = Str.IsOptionPresent(vvp,"nozpathcache");
	nozdemand = Str.IsOptionPresent(vvp,"nozdemand");
	nosleeping = Str.IsOptionPresent(vvp,"nosleeping");
	suppresssimulationflag = Str.IsOptionPresent(vvp,"ns");
	sleeponeveryshift = Str.IsOptionPresent(vvp,"pauses");
	ReportAllOnceFlag = Str.IsOptionPresent(vvp,"razo");
    }

    public void adjustParameters(String[] VarValPairs)
    /*
      This allows adjustment of simulation parameters before simulation.
      They are documented elsewhere.
     */
    {
	String[] vvp = VarValPairs;
	StartingTime_t_0 = Str.GetArgAssignment(vvp,"t_0");
	StoppingTime_t_1 = Str.GetArgAssignment(vvp,"t_1");
    }


    public void adjustCommandLineCase(boolean _dumpflag, int _verboseSS, int _verboseSS_zl,
				      String[] _VerboseList,
				      String _outhtmlfile, String _outpsfile,
				      boolean termcolour)
    /*
      This adjusts those parameters which are only intended for command-line debugging use.
     */
    {
	dumpflag = _dumpflag;
	verboseSS = _verboseSS;
	verboseSS_zl = _verboseSS_zl;
	VerboseList = _VerboseList;   // base 1.
	outhtmlfile = _outhtmlfile;
	outpsfile = _outpsfile;
	verbosetermcolour = termcolour;	
    }
    
    /*
      Some of these are only used when called from Command Line.
    */
    private zio AllIO;
    private int verboseflag;
    private boolean noneighcache;
    private boolean nozpathcache;
    private boolean nozdemand;
    private boolean nosleeping;
    private int verboseSS;
    private int verboseSS_zl; 
    private String[] VerboseList;   // or null
    private String StoppingTime_t_1;  // or null
    private String StartingTime_t_0;  // or null
    private boolean dumpflag;
    private boolean suppresssimulationflag;
    private String outhtmlfile;
    private String outpsfile;
    private boolean verbosetermcolour;
    private boolean sleeponeveryshift;
    private boolean ReportAllOnceFlag;   // Not only at beginning, but upon loading.

    public int run()
    /*
      This initiates actual simulation. 
      It does not return until simulation is finished.
     */
    {
	
	zsystem Ψ = new zsystem(AllIO,verboseflag,verbosetermcolour);
	// Options:
	Ψ.verboseSS=verboseSS;Ψ.verboseSS_zl=verboseSS_zl;
	Ψ.VerboseList=VerboseList;
	if (noneighcache) {Ψ.cacheNeighbourhoodsFlag=0;}
	if (nozpathcache) {Ψ.cacheResolvedZpathsFlag=0;}
	if (nozdemand) {Ψ.enableZdemandFlag=0;}
	if (nosleeping) {Ψ.allowZboxSleepingFlag=0;} //added 2018-09-03.

	Ψ.pauseoneveryshift = sleeponeveryshift;
	Ψ.ReportAllOnceFlag=ReportAllOnceFlag;

	boolean LoadedSourceFiles=false;
	boolean LoadedObjFile=false;

	/*
	  Load either OBJECT file or SOURCE file inputs.
	 */
	DataInputStream inpobjfile = AllIO.loadZobjFile();
	if (inpobjfile!=null)
	    {
		try
		    {
			if (!CompiledFileIO.ReadStateFromZObjFile(Ψ,inpobjfile))
			    {
				Str.print("================INPUT Format ERROR; aborting.===================");
				return(-1);
			    }
			LoadedObjFile = true;
		    }
		catch (IOException ioe)
		    {
			if (ioe instanceof EOFException)
			    {
				// Fine.
			    }
			else
			    {
				Str.print("================INPUT File ERROR; aborting.===================" + ioe);
				return(-1);
			    }
		    }		
	    }
	/*
	  The Input Parser. Loads in [] {} <> structured input.
	  Do this even if we have loaded from object file.
	*/		
	{	
	    Iterator<Iterator<String>> inputs = AllIO.getZsyntaxInputs();
	    if (inputs!=null)
		{
		    LoadedSourceFiles = true;
		    ZsyntaxInputParser Zinpa; // Input parser requires only this scope.
		    LabelReferencing AllSystemLabels = Ψ.System_Z_Lists.GetExtantLabeledZobjects();
		    Zinpa = new ZsyntaxInputParser(AllSystemLabels,verboseflag>0,false,Ψ);
		    try {
			Zinpa.ParseInputSources(inputs);
		    }
		    catch (IOException ioe)
			{
			    Str.print("================INPUT ERROR; aborting.===================");
			    return(-3);
			}
		    Ψ.SelectVerboseLabels(Ψ.VerboseList,AllSystemLabels);
		    // Stage III: Insert all those objects into our system:
		    if (!LoadedObjFile)
			{
			    // Since this is afresh simulation, we begin at t_0.
			    // If it were loaded from an obj file, it would come with a t.

			    if (StartingTime_t_0 != null)
				{
				    Ψ.t_0 = TimeRoutines.ParseTime(StartingTime_t_0);
				}

			    Ψ.t = Ψ.t_0;
			}
		    Zinpa.InsertIntozsystem(Ψ.t);
		}
	}

	/*
	  However we got them, let us select the verbose labels.
	  If we loaded from Zsource, we have already done this.
	*/
	if (!LoadedSourceFiles)
	    {
		LabelReferencing AllSystemLabels = Ψ.System_Z_Lists.GetExtantLabeledZobjects();
		Ψ.SelectVerboseLabels(Ψ.VerboseList,AllSystemLabels);		
	    }
	
	/*
	  Possible things to override in zsystem specification:
	 */
	// t_0 also; see just above where Ψ.t_0 is used.
	if (StoppingTime_t_1 != null)
	    {
		Ψ.t_1 = TimeRoutines.ParseTime(StoppingTime_t_1);
	    }

	/*
	  Do a trivial server test on each server.
	 */
	{
	    if (Ψ.Ξ!=null)
		{
		    Iterator<ExternalServer> wif = Ψ.Ξ.listIterator();
		    int j=0;
		    while (wif.hasNext())
			{
			    ExternalServer Test = wif.next();
			    Ψ.Verb.printE("Server #500#" + j + "#0# #555/#" + Test.Desc() + "#0# test: ");
			    Iterator<String> wof = Test.request();
			    if (wof!=null)
				{
				    while (wof.hasNext())
					{
					    String ll = wof.next();
					    // We need some str commands # -> ## or else ignore all subsequent # in printE.
					    Ψ.Verb.printEn("\t#500#|#335/#");
					    Ψ.Verb.print(ll);
					}
				}
			    j++;
			}
		}
	}

	/*
	  Dump out all objects in the zsystem, for debugging.
	  An olde-fashioned postscript option would be nice.
	*/
	if (dumpflag)
	    {
		Ψ.System_Z_Lists.DumpToTerminal();
	    }

	/*
	  Reporting is usually controlled with R= flags in individual ztypes and zboxen.
	  If ReportAllOnce flag is provided, then we force every zbox to
	  make an initial report.
	*/
	if (ReportAllOnceFlag)
	    {
		Ψ.System_Z_Lists.ReportOnAllZboxen(7);   // For now, seven.
	    }


	if (!suppresssimulationflag)
	    {					    
		/*
		  Perform actual simulation.
		*/
		{
		    /*
		      Hardcoded:  07:30-08:00 ,[0], -12:30        5170,216,257,?,1745,2949
		      Hardcoded:  07:30-08:00 ,[0,l],-12:30       5171,216,257,2,1745,2950  

		      Hardcoded:  07:30-08:00 ,[0,p,l],-12:30     3111,234,276,2,274,2324   :-o zpaths are not kept track of?
		      Storing:    07:30-08:00 ,F,   -12:30        3110,234,276,2,274,2323
		      
		      Hardcoded:  07:30-12:30                  5336,216,257,2,1745,3115
		                                               5336,216,257,2,1745,3115
		    */
		    		    
		    Ψ.EvolveUntil(Ψ.t_1);    // Evolve until t_1.		    
		}
	    }

	if (outhtmlfile!=null)
	    {
		HTML_output.Dump(Ψ,outhtmlfile);
	    }

	if (outpsfile!=null)
	    {
		PS_output.Dump(Ψ,outpsfile);
	    }

	DataOutputStream outobjfile = AllIO.saveZobjFile();
	if (outobjfile!=null)
	    {
		try {
		    CompiledFileIO.WriteStateToZObjFile(Ψ,outobjfile);
		}  catch (IOException ioe) {
		    Str.printE("#500# Unable to write state data.");
		    return(-2);
		}
	    }
	return(0);
    }
}
