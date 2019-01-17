package TravelTimeTool;

import com.ibm.Zimulator.SmallAux.*;

class TimeRoutines
{    
    public static String ISODateTo_DMYSlash(String isod)
    {
	String[] data = Str.split(isod,"-");  //1,2,3

	return(Str.intdig(Str.atoi(data[3]),2) + "/"
	       + Str.intdig(Str.atoi(data[2]),2) + "/" 
	       + Str.intdig(Str.atoi(data[1]),4));
    }
    public static String ISODateTo_DMYSlash_n0p(String isod)
    {
	String[] data = Str.split(isod,"-");  //1,2,3
	return(Str.atoi(data[3]) + "/" + Str.atoi(data[2]) + "/" + Str.atoi(data[1]));
    }

    public static String SecToTime(int sec)
    {
	return(Str.intdig(sec/3600,2) + ":" + Str.intdig((sec/60) % 60,2) + ":" + Str.intdig(sec % 60,2));
    }

    public static int TimeToSec(String tim)
    { // hh:mm:ss -> s.
	String[] tima = Str.split(tim,":");  //1,2,3
	if (tima.length<4) {return(0);}
	//Str.print("tim: " + tim + " == " + tima[1] + ":" + tima[2] + ":" + tima[3]);
	int sec;
	sec = Str.atoi(tima[1]) * 3600 + Str.atoi(tima[2])*60 + Str.atoi(tima[3]);
	return(sec);
    }

    public static String SecToSlot(int sec)
    {
	return(Str.intdig(sec/3600,2) + Str.intdig((sec/60) % 60,2));
    }

    public static int FlexTimeToSec(String tim)  // h:m:s or h:m or s.
    { // hh:mm:ss -> s.
	String[] tima = Str.split0(tim,":");
	if (tima.length<1) {return(0);}
	if (tima.length==1) {return(Str.atoi(tima[0]));} // s
	if (tima.length==2) {return(3600*Str.atoi(tima[0]) + 60*Str.atoi(tima[1]));} // h:m
	int off = tima.length-3;
	return( Str.atoi(tima[off]) * 3600 + Str.atoi(tima[off+1])*60 + Str.atoi(tima[off+2]) );
    }

    public static double FlexFracTimeToSec(String tim)  // h:m:s or h:m or s. where they can be floating.
    { // hh:mm:ss.mmm -> s.
	String[] tima = Str.split0(tim,":");
	if (tima.length<1) {return(0);}
	if (tima.length==1) {return(Str.atof(tima[0]));} // s
	if (tima.length==2) {return(3600*Str.atof(tima[0]) + 60*Str.atof(tima[1]));} // h:m
	int off = tima.length-3;
	return( Str.atof(tima[off]) * 3600 + Str.atof(tima[off+1])*60 + Str.atof(tima[off+2]) );
    }

    
}
