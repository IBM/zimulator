package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

/*
  Makes a distribution of mean travel times.
 */

class TravelTimeDistn
{
    public static Histo Distri(Verbosity V,SystemData SD,String HistoParams)  // SDg can be null.
    {
	String[] HP = Str.splitCSV(HistoParams);  // "0,7200,10"
	double mint = TimeRoutines.FlexTimeToSec(HP[1]); // h:m:s or h:m or s.	    
	double maxt = TimeRoutines.FlexTimeToSec(HP[2]); // h:m:s or h:m or s.	    
	int tslots = Str.atoi(HP[3]);	

	V.v(" Travel-time distribution will be " + mint + " s --- " + maxt + " s with " + tslots + " bins.");	
	Histo MTT = new Histo(tslots,mint,maxt);

	Iterator<int[]> wi_ODτ = SD.buckets.idxiterator();
	while (wi_ODτ.hasNext())
	    {
		int[] ODτ = wi_ODτ.next();
		Bucket B = SD.buckets.get(ODτ);
		double tmean = B.T / B.n;
		MTT.AddSample(tmean);                     // Distribution of the mean travel time.
	    }
	V.v("μ = " + MTT.Mean() + " s");
	V.v("σ = " + MTT.StdDev() + " s");
	return(MTT);
    }

    public static Histo ΔDistri(Verbosity V,SystemData SD,String HistoParams)  // SDg can be null.
    {
	String[] HP = Str.splitCSV(HistoParams);  // "0,7200,10"
	double mint = TimeRoutines.FlexTimeToSec(HP[1]); // h:m:s or h:m or s.	    
	double maxt = TimeRoutines.FlexTimeToSec(HP[2]); // h:m:s or h:m or s.	    
	int tslots = Str.atoi(HP[3]);	

	V.v(" Travel-time distribution will be " + mint + " s --- " + maxt + " s with " + tslots + " bins.");	
	Histo MTT = new Histo(tslots,mint,maxt);

	Iterator<int[]> wi_ODτ = SD.buckets.idxiterator();
	while (wi_ODτ.hasNext())
	    {
		int[] ODτ = wi_ODτ.next();
		Bucket B = SD.buckets.get(ODτ);
		if (B.ref_n==0) {continue;}  // do not use ODτ unless in both sets.
		double ttmean = B.ref_T / B.ref_n;
		double tmean = B.T / B.n;
		MTT.AddSample(tmean - ttmean);             // Distribution of the mean travel time difference.
	    }
	V.v("μ = " + MTT.Mean() + " s");
	V.v("σ = " + MTT.StdDev() + " s");
	return(MTT);
    }

    
    
}

