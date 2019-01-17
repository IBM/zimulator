package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;
import java.util.*;


class Bucket
{
    int n;         // n_ODτ
    double T;      // T_ODτ
    int ref_n;     // n῀_ODτ
    double ref_T;  // Τ῀_ODτ
    public Bucket()
    {
	n=0;T=0.0;
	ref_n=0;ref_T=0.0;
    }
}

class Passenger
{
    double O_t;
    double D_t;
    int O_idx; // value given by O_or_D_indexer.
    int D_idx; // value given by O_or_D_indexer.

    FA_i stops;  // values from walkloc indexer.

    boolean CompletedTrip;
    public Passenger()
    {
	CompletedTrip = false;
	stops = new FA_i();
    }
    public String toString()
    {
	String S = "(" + O_idx + " -> " + D_idx + ") : O_t=" + O_t + "  D_t=" + D_t + " stops: " + stops.size();
	return(S);
    }
}    

