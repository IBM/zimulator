package com.ibm.Zimulator.Zimulator;

import com.ibm.Zimulator.SmallAux.*;

/*
  This is a static class for flexible time parsing.

  ParseDateTime() does not yet include provision for y-m-d.

 */
public class TimeRoutines
{
    static double ParseDateTime(String ts)
    {// !!! Update to include y-m-d.
	return(ParseTime(ts));
    }

    static double ParseTime(String ts)
    /*
      h:m:s
      h:m
      s
      Each field is a decimal.
      Returns seconds.
    */
    {
	String[] tsa = Str.split(ts,":"); // fields start at [1].
	if (tsa.length==4) // h:m:s
	    {
		return(Str.atof(tsa[1])*3600 + Str.atof(tsa[2])*60 + Str.atof(tsa[3]));
	    }
	if (tsa.length==3) // h:m
	    {
		return( Str.atof(tsa[1])*3600 + Str.atof(tsa[2])*60);
	    }
	if (tsa.length==2) // s
	    {
		return(Str.atof(tsa[1]));
	    }
	return(0);  //error.
    }

    static int ParseTimei(String ts)
    /*
      h:m:s
      h:m
      s
      Each field is a decimal.
      Returns seconds.
    */
    {
	String[] tsa = Str.split(ts,":"); // fields start at [1].
	if (tsa.length==4) // h:m:s
	    {
		return(Str.atoi(tsa[1])*3600 + Str.atoi(tsa[2])*60 + Str.atoi(tsa[3]));
	    }
	if (tsa.length==3) // h:m
	    {
		return( Str.atoi(tsa[1])*3600 + Str.atoi(tsa[2])*60);
	    }
	if (tsa.length==2) // s
	    {
		return(Str.atoi(tsa[1]));
	    }
	return(0);  //error.
    }
}
