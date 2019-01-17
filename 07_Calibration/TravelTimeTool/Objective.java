package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.MDA.*;
import java.util.*;

class Objective
{
    public static double Φ(Verbosity V,SystemData SD)
    {
	double Φ=0;
	Iterator<int[]> Miter = SD.buckets.idxiterator();
	while (Miter.hasNext())   // This is ∑_ODτ
	    {
		int[] ODτ = Miter.next();
		Bucket B = SD.buckets.get(ODτ);
		if (B.ref_n==0) {continue;}  // The ' on ∑'_ODτ
		double zim_mean_tt = B.T / B.n;
		double ref_mean_tt = B.ref_T / B.ref_n;
		double C =  zim_mean_tt - ref_mean_tt;
		Φ += C*C;
	    }
	return(Φ);
    }
}
