package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

class GenSynth
{
    public static void SyntheticReference(double ΔTT,SystemData SD)
    /*
      In order to demonstrate calibration, synthetic data with adjusted travel times are produced.
      It does not matter exactly how this is done; maybe we will just make the whole system a little faster or slower.

      Since we are only dealing with walkign times, we do not use a factor, but an additive amount:

      ΔTT * (0.95 + 0.1 * random);

      We just send all the same passengers, but modify their travel times:
     */
    {
        Str.print("# O_Label,D_Label,O_time,D_time");
	Iterator<Passenger> ps = SD.AllPax.iterator();
	while (ps.hasNext())
	    {
		Passenger P = ps.next();
		if (!P.CompletedTrip) {continue;}
		// Find origin and destination; slightly sloppy.
		String O_Label = SD.O_or_D_indexer.getstringkey(P.O_idx);
		String D_Label = SD.O_or_D_indexer.getstringkey(P.D_idx);
		double T_T = P.D_t - P.O_t;
		double mod_O_t = P.O_t + ΔTT * 0.1 * 2.0*(Math.random()-0.5);
		double mod_T_T = T_T + ΔTT * (0.95 + 0.1 * Math.random());
		double mod_D_t = P.O_t + mod_T_T;
		//Str.print(O_Label + "," + D_Label + "," + t1 + "," + t2);// + " # " + start_time + " " + end_time);
		//Str.print(O_Label + "," + D_Label + "," + mod_O_t + "," + mod_D_t);// + " # " + start_time + " " + end_time);
		Str.print(O_Label + "," + D_Label + "," + mod_O_t + "," + mod_D_t + ",# Orig:," + P.O_t +","+ P.D_t);
	    } //pax
    } //gen
} //synth

