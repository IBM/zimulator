
package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;


class Location
{
    String label;
    public Location(String l)
    {
	label=l;
	Adjustment_time=0;
    }    

    double Adjustment_time;  // for calibration. This is the walking-time adjustment, in seconds. Only meaningful for 'corridors'.
}
