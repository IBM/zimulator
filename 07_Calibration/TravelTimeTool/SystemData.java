package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.MDA.*;
import java.util.*;

    
class SystemData
{
    TimeBins TB;        // Describes how to make timebins. t_O -> τ    This might be null in some modes.

    /*
      It is necessary to change various things to indices, as described in the documentation.

      Various things are described by strings (like Origin, Destination, Walking locaiton...)
      We convert them (sometimes with numebrs too) into indices using a SBHH called 'indexer'.
    */

    // Use only two kinds of indices: Location idx and timebin idx.
    // Use an MDA class then; they are sparse and can be iterated over.

    public SBHH O_or_D_indexer;   // Convert an origin or destination to an index. ( _Gt and _Gt_out labels )
    
    // Store certain things using these indices.
    // In the following, the XxxArray internal index is the ODτ index.
    // This is the total time [s] spent for each O-D pair. (i.e. summed over many trips)
    // ( This will remain null if TB is null )
    public MDA<Bucket> buckets;

    // Since passenger activity might be reported in any order, the passenger names need to be
    // kept track of. This is not needed for any computation.
    public SBHH Pax_indexer;
    public ArrayList<Passenger> AllPax;  // indexed by Pax_indexer.


    private double Total_Passenger_Time;  // Total number of paxseconds within the whole time of interest.
    
    Verbosity V;
    public SystemData(Verbosity _V,ArrayList<Iterator<String>> zo,TimeBins _TB)
    {
	V=_V;
	TB=_TB;
	O_or_D_indexer = new SBHH();
	Pax_indexer = new SBHH();
	AllPax = new  ArrayList<Passenger>();

	{ // read in files.
	    int n=0;
	    int FileNum=0;
	    Iterator<Iterator<String>> wif = zo.iterator();
	    while (wif.hasNext())
		{
		    FileNum++;
		    Iterator<String> wig = wif.next();
		    /*
		      Lines we are interested in involve Pax/* entering a zbox.
		      e.g:
		      R: zbox ztype=Pax,9,1 t=18000.0 R=- state=M Z.n=0 label=Pax/28 z.label=JDL_Gt_1 z.L=0 z.W=1 t0=18000.0 x0=0.0 t1=18000.0 x1=0.0 δt=0.0 l=1 L=0 D="⤷ JDL_Gt_1"
		    */
		    while (wig.hasNext())
			{
			    String zol = wig.next();
			    if (!Str.equals(Str.substr(zol,0,2),"R:")) {continue;}  // only real reporting lines.
			    if (!Str.equals(Str.field(zol,2),"zbox")) {continue;}  // only for zboxen.
			    String ztype = Str.GetStrAss(zol,"ztype",null);
			    if (ztype==null) {continue;}
			    if (!Str.equals(Str.substr(ztype,0,4),"Pax,")) {continue;}  // only for Pax type. (do not care about number)
			    String D = Str.GetStrAss(zol,"D",null);
			    if (D==null) {continue;}
			    String[] Da = Str.split(D);
			    if (!Str.equals(Da[1],"⤷")) {continue;}  //not a container entry.
			    String container = Da[2];
			    double t = Str.GetDblAss(zol,"t",0);  // time of entry.
			    if (t==0) {continue;}  // Really should not happen.
			    String PasName = Str.GetStrAss(zol,"label",null);
			    if (PasName==null) {continue;}  // Really should not happen.
			    int pax_idx = Pax_indexer.getidx("F"+FileNum+"/"+PasName);  // Passengers are different on different days.
			    Passenger P;
			    {
				if (pax_idx == AllPax.size()) {P = new Passenger(); AllPax.add(P);}
				else {P = AllPax.get(pax_idx);}
			    }
			    // Is it an origin or destination? O: *_Gt_* D: *_Gt_*_out
			    if (Str.index(container,"_Gt_")!=-1)
				{
				    int placeidx = O_or_D_indexer.getidx(container);
				    if (Str.index(container,"_out") == -1)
					{
					    if (P.O_t==0)
						{
						    P.O_idx = placeidx;
						    P.O_t = t;
						    //Str.print("# O "+pax_idx+" ["+container+"]" + P.toString());
						}
					}
				    else
					{
					    P.D_idx = placeidx;
					    P.D_t = t;
					    //Str.print("# D "+pax_idx+" ["+container+"]" + P.toString());
					    P.CompletedTrip = true;
					}
				}
			    if (V!=null)
				{
				    if ((n % 5000) == 0) {V.hash();}
				    if ((n % 250000) == 0)  // Just so we can see progress.
					{
					    V.v("(" + n + ") transitions: (" + PasName + " -> " + container + " @ t=" + TimeRoutines.SecToTime(MLR.ftoi(t)) + ")");
					}
				}
			    n++;			
			} // next line
		} // next file.
	} // done reading.

	/*
	  Now, 'buckets':
	*/

	if (TB!=null)
	    {
		buckets = new MDA<Bucket>(3);  // 3-D: O,D,τ.
		int cnt=0;
		Total_Passenger_Time = 0;
		Iterator<Passenger> wip = AllPax.iterator();
		while (wip.hasNext())
		    {
			Passenger P = wip.next();
			if (P.CompletedTrip)
			    {  // Process into the statistics.
				int τ = TB.GetTimeBinIndex(P.O_t);
				Bucket B = buckets.get(P.O_idx,P.D_idx,τ);
				if (B == null) {B = new Bucket();buckets.set(P.O_idx,P.D_idx,τ,B);}
				B.n++;
				double TripTime = ( P.D_t - P.O_t );
				B.T += TripTime;
				Total_Passenger_Time += TripTime;
				cnt++;
				if ((cnt % 10000) == 1) { Str.print("# Pax:" + cnt + " Bux:" + buckets.size());}
			    }
		    }
	    }
    }


    /*
      Supplement System data from .csv Ground-truth reference file
    */
    public void LoadReferenceData(Iterator<String> ground)
    {
	int n=0;
	Iterator<String> wig = ground;
	while (wig.hasNext())
	    {
		String zol = wig.next();
		if (Str.equals(Str.substr(zol,0,1),"#")) {continue;}  // Comment line.
		String[] fields = Str.splitCSV(zol);
		if (fields.length<5) {continue;}  // funny line :-/
		// O_Label,D_Label,O_time,TravelTime
		String O_Label = fields[1];
		String D_Label = fields[2];
		double O_time = TimeRoutines.FlexTimeToSec(fields[3]); // h:m:s or h:m or s.	    
		double D_time = TimeRoutines.FlexTimeToSec(fields[4]); // h:m:s or h:m or s.	    
		int τ = TB.GetTimeBinIndex(O_time);
		if (τ<0) {continue;}		
		// Only use locations we already have:
		int O_L_idx = O_or_D_indexer.chkidx(O_Label);
		if (O_L_idx<0) {continue;}
		int D_L_idx = O_or_D_indexer.chkidx(D_Label);
		if (D_L_idx<0) {continue;}
		/*
		  See if it is in our set.
		*/
		Bucket B = buckets.get(O_L_idx,D_L_idx,τ);
		if (B == null) {continue;}
		B.ref_n++;
		B.ref_T += ( D_time - O_time );
		n++;
		if ((n % 10000) == 1) { Str.print("# Ref.data " + n);}
	    } // next line	
    } // done reading.
    

    public double TotalPassengerTime()
    {
	return(Total_Passenger_Time);
    }

    
    public void DumpTravelTimes()
    // o,d,t_o,t_d
    {
	Iterator<Passenger> wip = AllPax.iterator();
	while (wip.hasNext())
	    {
		Passenger P = wip.next();

		Str.print( O_or_D_indexer.getstringkey(P.O_idx) +","+
			   O_or_D_indexer.getstringkey(P.D_idx) +","+
			   P.O_t +","+ P.D_t );
	    }    
    }

}
