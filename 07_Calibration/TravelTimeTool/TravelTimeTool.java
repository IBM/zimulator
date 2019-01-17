package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

class Veb implements Verbosity
{
    public void v(String S) { Str.print("# " + S);}
    public void hash() { Str.printn("#"); Str.flush();}
}

public class TravelTimeTool
{
    public static void main(String[] argv)
    {
	Veb V = new Veb();
	if (argv.length<1) {Usage(null);return;}
	if (argv.length<2) {Usage(argv[0]);return;}
	
	if ( Str.equals(argv[0],"TTD") )
	    /*
	      Simply find travel-time distribution and output both density and cumulative data.
	     */
	    {
		// TTD From,To,Bins MinTime,MaxTime,Slots movements.zo [movements.zo ...]
		ArrayList<Iterator<String>> InputReporting = new ArrayList<Iterator<String>>();
		TimeBins TB = new TimeBins(argv[1]);
		String HistoParams = argv[2];
		for (int j=3;j<argv.length;j++)
		    {
			InputReporting.add(new FilenameToIterator(argv[j]));
		    }
		/*
		  Slurp in the whole system.......................
		 */
		V.v("*** Loading in System Data from Zim output ***");
		SystemData SD = new SystemData(V,InputReporting,TB);
		V.v("*** System loaded: Number of passengers: " + SD.AllPax.size());
		V.v("     Number of Origins and Destinations: " + SD.O_or_D_indexer.size());
		Str.print("# The total passenger time follows. Unit is Pax·s");
		Str.print("#TotalPassengerTime: " + SD.TotalPassengerTime());		
		/*
		  Find distribution of travel times and dump to terminal.
		*/
		V.v("*** Finding travel-time distribution ***");
		Histo MTT = TravelTimeDistn.Distri(V,SD,HistoParams);
		Str.print("#-----------------------------------------------------");
		MTT.Dump(2," s");
		Str.print("#-----------------------------------------------------");
	    } // TTD mode.

	if ( Str.equals(argv[0],"TTS") )
	    /*
	      Simply find travel-times and output them.
	     */
	    {
		// TTS movements.zo [movements.zo ...]
		ArrayList<Iterator<String>> InputReporting = new ArrayList<Iterator<String>>();
		for (int j=1;j<argv.length;j++)
		    {
			InputReporting.add(new FilenameToIterator(argv[j]));
		    }
		/*
		  Slurp in the whole system.......................
		*/
		V.v("*** Loading in System Data from Zim output ***");
		TimeBins TB = new TimeBins("05:00,06:00,6");  // Will not be used for output.
		SystemData SD = new SystemData(V,InputReporting,TB);
		V.v("*** System loaded: Number of passengers: " + SD.AllPax.size());
		V.v("     Number of Origins and Destinations: " + SD.O_or_D_indexer.size());
		SD.DumpTravelTimes();
		Str.print("#-----------------------------------------------------");
	    } // TTD mode.


	if ( Str.equals(argv[0],"SYN") )
	    /*
	      Produce (in a trivial way) a new synthetic 'ground-truth'.
	    */
	    {
		// SYN mod_μ movements.zo [movements.zo ...]
		TimeBins TB = null; // null TB, so we will not have buckets (we do not need them in SYN mode)
		double mod_μ = Str.atof(argv[1]);
		ArrayList<Iterator<String>> InputReporting = new ArrayList<Iterator<String>>();
		for (int j=2;j<argv.length;j++) {InputReporting.add(new FilenameToIterator(argv[j]));}
		V.v("*** Loading in System Data from Zim output ***");
		SystemData SD = new SystemData(V,InputReporting,TB);
		V.v("*** System loaded: Number of passengers: " + SD.AllPax.size());
		V.v("     Number of Origins and Destinations: " + SD.O_or_D_indexer.size());
		V.v("*** Generating synthetic Reference output  ***");
		GenSynth.SyntheticReference(mod_μ,SD);
		Str.print("#-----------------------------------------------------");
	    } // SYN mode.



	
	if ( Str.equals(argv[0],"TTC") )
	    /*
	      Find error distribution between Zim output and provided reference
	      (Error is absolute* error, in seconds.) *[as opposed to relative; it can be negative]
	    */
	    {
		// TTC From,To,Bins MinTime,MaxTime,Slots RefTrips.csv movements.zo [movements.zo ...]
		TimeBins TB = new TimeBins(argv[1]);
		String HistoParams = argv[2];
		String RefTTFile = argv[3];
		ArrayList<Iterator<String>> InputReporting = new ArrayList<Iterator<String>>();
		for (int j=4;j<argv.length;j++) {InputReporting.add(new FilenameToIterator(argv[j]));}
		V.v("*** Loading in System Data from Zim output ***");
		SystemData SD = new SystemData(V,InputReporting,TB);
		V.v("*** System loaded: Number of passengers: " + SD.AllPax.size());
		V.v("     Number of Origins and Destinations: " + SD.O_or_D_indexer.size());
		V.v("*** Loading in reference data from " + RefTTFile + " ***");
		SD.LoadReferenceData(new FilenameToIterator(RefTTFile));
		V.v("*** Finding travel-time error distribution ***");
		Histo MTTΔ = TravelTimeDistn.ΔDistri(V,SD,HistoParams);
		MTTΔ.Dump(2," s");
		Str.print("#-----------------------------------------------------");
	    } // TTC mode.

	


	if ( Str.equals(argv[0],"OBJ") )
	    /*
	      Φ = measure of the difference between the travel times in the ground-truth file and those in the .zo Zim output.
	        = "difference from TTC output and δ distribution centred at 0."
	    */
	    {
		// OBJ From,To,Bins RefTrips.csv movements.zo...
		TimeBins TB = new TimeBins(argv[1]);
		String RefTTFile = argv[2];
		ArrayList<Iterator<String>> InputReporting = new ArrayList<Iterator<String>>();
		for (int j=3;j<argv.length;j++) {InputReporting.add(new FilenameToIterator(argv[j]));}
		V.v("*** Loading in System Data from Zim output ***");
		SystemData SD = new SystemData(V,InputReporting,TB);
		V.v("*** System loaded: Number of passengers: " + SD.AllPax.size());
		V.v("     Number of Origins and Destinations: " + SD.O_or_D_indexer.size());
		V.v("*** Loading in reference data from " + RefTTFile + " ***");
		SD.LoadReferenceData(new FilenameToIterator(RefTTFile));
		V.v("*** Calculating objective function ***");		
		double Φ = Objective.Φ(V,SD);
		V.v("Found Objective: Φ = " + Φ);
		Str.print("Objective: " + Φ);
	    } // OBJ mode.

	




	/*
	  WalkingTimes WT = new WalkingTimes(GIO,srcs_IWT);
	  WalksOnRoutes WOR = new WalksOnRoutes(GIO,WT,srcs_OPD,FromSec,Bins,BinSize);
	  TravelPaths TP = new TravelPaths(GIO,WOR,srcs_PLNTT);
	  Calculation Calc = new Calculation(GIO,WT,WOR,TT,30,tt_min, tt_max, tt_chg, sec_conv);
	*/

    }    
    

    
    private static void Usage(String mode)
    {
	Str.printE("#345_#TravelTimeTool");
	Str.printE("#333# Usage:");	
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#MODE#555# parameters...");
	Str.printE("#555#    MODE is one of TTD, TTS, SYN, TTC, OBJ  #353#");
	Str.printE("#333# Any resultant output will contain ## comment lines and CSV data.");
	Str.printE("#555*# Run without parameters to get documentation.");    
	Str.printE(""); //"#335#-------------------------------------------------------------------------------------");
	Str.printE("#555# In various modes#333#, there are parameters for two kinds of discretisation:");
	Str.printE("#222#    #355#From,To,Bins#333# -- define a starting and ending time of day (#522#s#333#, #522#h:m:s#333# or #522#h:m#333#) #333#and number of time bins. ");
	Str.printE("   #333# Trips in the system are #/#comparable trips#0##333# when they have same O-D pair and same time bin at O.");
	Str.printE("#222#    #355#MinTime,MaxTime,Slots#353#  -- parameters for [histogram] outputs.");

	if (mode==null) {return;}
	if (mode=="") {return;}
	
	Str.printE("#335#_____________________________________________________________________________________");
	if (Str.equals(mode,"TTD")) {Usage_TTD();}
	if (Str.equals(mode,"TTS")) {Usage_TTS();}
	if (Str.equals(mode,"TTC")) {Usage_TTC();}
	if (Str.equals(mode,"SYN")) {Usage_SYN();}
	if (Str.equals(mode,"OBJ")) {Usage_OBJ();}
    }

    private static void Usage_TTD()
    {
	Str.printE("#333# Travel-time distribution: #555# TTD");
	Str.printE(" #555#->#333# Output will be distribution and cumulative distribution of travel times.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#TTD#353# From,To,Bins MinTime,MaxTime,Slots movements.zo [movements.zo ...]");
	Str.printE("#222#    movements.zo #111#-- #333# Zimulator Reporting output; zbox labels must match conditions:");
	Str.printE("#222#       #333# Origins: *_Gt_* Destinations: *_Gt_*_out  Passengers: Pax/x");
	Str.printE("#222#       #333# Walking time is spent in corridors: *Co_*");
	Str.printE("#222#       #333# where '*' denotes general naming, and 'x' denotes a passenger number.");
	Str.printE("#222#       #333# Several files may be specified (i.e. several days)");	
	Str.printE("#333# e.g.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#TTD#353# 05:00,06:00,12 10,4500,40 out/madrid_Monday.zo out/madrid_Tuesday.zo ");	
	Str.printE("#333#  Consider trips leaving between 05:00 and 06:00, grouped into 12 bins.");
	Str.printE("#333#  Times for comparable trips will be averaged.");
	Str.printE("#333#  A histogram will be produced, with travel times between 10 and 4500 seconds, collected into 40 bins.");
	Str.printE("#333#  The mean μ and standard deviation σ of this distribution will appear in comment lines.");
	Str.printE("#333#  Also, the total passenger time for trips between 'From' and 'To' will be output in a comment line");
	Str.printE("#333# beginning with #543_###TotalPassengerTime:#0# ");
    }
    private static void Usage_TTS()
    {
	Str.printE("#333# Travel-time summary: #555# TTS");
	Str.printE(" #555#->#333# Output will be a list of all passengers and their travel times.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#TTS#353# movements.zo [movements.zo ...]");
	Str.printE("#222#    movements.zo #111#-- #333# Zimulator Reporting output; zbox labels must match conditions:");
	Str.printE("#222#       #333# Origins: *_Gt_* Destinations: *_Gt_*_out  Passengers: Pax/x");
	Str.printE("#222#       #333# where '*' denotes general naming, and 'x' denotes a passenger number.");
	Str.printE("#222#       #333# Several files may be specified (i.e. several days)");	
	Str.printE("#333# e.g.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#TTS#353# out/madrid_Monday.zo out/madrid_Tuesday.zo ");	
	Str.printE("#333#  CSV output will be produced:");
	Str.printE("         #234/# O_Label,D_Label,O_time,D_time");
	Str.printE("#335#_____________________________________________________________________________________");
    }
    private static void Usage_SYN()
    {
	Str.printE("#333# Synthetic 'ground-truth' distribution: #555# SYN");
	Str.printE(" #555#->#333# Output will be content of a RefTrips.csv file, as described in TTC mode.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#SYN#353# mod_t movements.zo [movements.zo ...]");
	Str.printE("#222#    movements.zo #111#-- #333# Zimulator Reporting output; zbox labels must match conditions.");
	Str.printE("#222#    mod_t #111#       -- #333# modification of travel time [s].");	
	Str.printE("#333# e.g.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #555#SYN#353# 55 out/madrid_Monday.zo ");	
	Str.printE("#333#  Produces a new summary indicating trips with mean time 55 s longer.");
    }
    private static void Usage_TTC()
    {
	Str.printE("#333# Travel-time comparator: #555# TTC");
	Str.printE("#555# ->#333# Output will be distribution and cumulative distribution of #/#differences#0##333# in travel times.");
	Str.printE("#555# ->#333# Also there will be a 'comment' line #555####555# with #555#Φ=...#555##333# showing objective function.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #343#TTC#353# From,To,Bins MinTime,MaxTime,Slots RefTrips.csv movements.zo [movements.zo ...]");
	Str.printE("#222#    RefTrips.csv #111#-- #333# Reference data for 'ground-truth' trips:");
	Str.printE("         #234/# O_Label,D_Label,O_time,D_time");	
	Str.printE("         #333# O_Label and D_Label must match zbox labels.");	
	Str.printE("         #333# e.g.");
	Str.printE("         #234/# JDL_Gt_1,AEM_Gt_2_out,08:00,09:05");	
	Str.printE("         #333# indicates travel from 'JDL_Gt_1' to 'AEM_Gt_2_out' starting at 08:00 and arriving at 09:05.");
	Str.printE("#222#    movements.zo #111#-- #333# Zimulator Reporting output; zbox labels must match conditions.");
	Str.printE("#333# e.g.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #343#TTC#353# 05:00,08:00,20 -600,600,20 GroundTruth.csv out/madrid_Monday.zo out/madrid_Tuesday.zo ");
	Str.printE("#333#  A histogram will be produced, with a distribution of travel-time error between -600 and 600 seconds.");	
    }
    private static void Usage_OBJ()
    {
	Str.printE("#333# Travel-time objective function: #555# OBJ");
	Str.printE("#555# ->#333#  Output will be (apart from comments) a line with #543_#Objective:#0##333# and a value for Φ.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #343#OBJ#353# From,To,Bins RefTrips.csv movements.zo [movements.zo ...]");
	Str.printE("#222#    RefTrips.csv #111#-- #333# Reference data for 'ground-truth' trips, as in TTC mode.");
	Str.printE("#222#    movements.zo #111#-- #333# Zimulator Reporting output; zbox labels must match conditions.");
	Str.printE("#333# e.g.");
	Str.printE("#555# $ #353#java -jar TravelTimeTool.jar #343#OBJ#353# 05:00,08:00,20 GroundTruth.csv out/madrid_GoodDay.zo");	
    }

}
