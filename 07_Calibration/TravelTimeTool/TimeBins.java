
package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

class TimeBins
{
    private int FromSec,BinSizeSec,Bins;
    public TimeBins(String TimeBinSpec)   // From,To,Bins.
    {
	String[] BinDef = Str.splitCSV(TimeBinSpec);  // "08:00,09:00,6";
	FromSec = TimeRoutines.FlexTimeToSec(BinDef[1]); // h:m:s or h:m or s.	    
	Bins = Str.atoi(BinDef[3]);
	int ToSec = TimeRoutines.FlexTimeToSec(BinDef[2]);
	BinSizeSec = (ToSec - FromSec)/Bins;
    }

    public int Num()
    {
	return(Bins);
    }
    
    public int GetTimeBinIndex(double timeinday_sec)
    // Returns a bin number or else -1.
    {	
	// int FromSec,BinSize,Bins;
	int n;
	n = MLR.ftoi(timeinday_sec) - FromSec;
	if (n<0) {return(-1);}
	n /= BinSizeSec;
	if (n>=Bins) {return(-1);}
	return(n);
    }

    public double GetStartTimeSec(int binidx)
    // Returns a time or else -1.
    {	
	// int FromSec,BinSize,Bins;
	if ( (binidx<0) || (binidx>=Bins) ) {return(-1.0);}
	double t = binidx*BinSizeSec + FromSec;
	return(t);
    }
    public double GetEndTimeSec(int binidx)
    // Returns a time or else -1.
    {
	double t = GetStartTimeSec(binidx);
	if (t<0.0) {return(t);}
	return(t+BinSizeSec);
    }
}
