package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

/*
  This class is useful for counting all zboxen in the system, typically to produce verbose progress output.

  Construct 'Boxcounter' to count the zboxen in the Dynamical [] lists,
  and then Report() to produce colourful terminal output.
  Little attempt is made to make this particularly fast. It is verbosity.

 */

class BoxCounter
{
    private int verbose;

    private int[] Depth_0_Tab;
    private int[][] Depth_1_Tab;
    private int[][][] Depth_2_Tab;
    private zsystem Ψ;
    private int numtypes;
    
    private int zpaths;
    
    public BoxCounter(zsystem _Ψ,int _verbose)
    {
	Ψ=_Ψ;
	verbose=_verbose;

	zpaths=0;
	
	numtypes = Ψ.ZtypeRefs.NumTypes();
	Depth_0_Tab = new int[4];	
	Depth_1_Tab = new int[numtypes][4];
	Depth_2_Tab = new int[numtypes][numtypes][4];
	
	// Keep track of other things generated:
	int syslistnum=0;
	while (true)
	    {
		Iterator<zobject> wif = Ψ.System_Z_Lists.GetSyslistIterator(syslistnum); // [0] [T] [Z] [S] := 0,1,2,3.
		if (wif==null) {break;}
		while (wif.hasNext())
		    {
			zobject zo = wif.next();
			if (zo instanceof zpath)
			    {
				zpaths++;
				continue;
			    }
			if (zo instanceof zbox)
			    {
				zbox φ = (zbox) zo;
				{
				    Depth_0_Tab[syslistnum]++;
				    Depth_1_Tab[φ.e.A][syslistnum]++;
				    if (φ.z!=null)
					{
					    Depth_2_Tab[φ.z.e.A][φ.e.A][syslistnum]++;
					}	    
				}
			    }
		    } // while in this syslist.
		syslistnum++;
	    }
    }





    private String rgbsysl(int sl)
    { // sl=0...3;
	return("" + (2+sl) + "4" + (5-sl));
    }

    private String FourLists(int[] SLtab)
    {
	String L = "";
	int T = 0;
	for (int sl=0;sl<4;sl++)
	    {
		T += SLtab[sl];
	    }
	if (T==0) {return(null);}
	for (int sl=0;sl<4;sl++)
	    {
		String num = SLtab[sl] == 0 ? "·" :  ("" + SLtab[sl]);
		L += "#" + rgbsysl(sl) + "#" + num + "\t";
		if (sl==2)
		    {
			if ((T!=0)&&(SLtab[sl]!=0))
			    {
				int percentage = (100*SLtab[sl]/T);
				L += "(" + percentage + "%)\t";
			    }
			else
			    {
				L += "  \t";
			    }
		    }
	    }
	L += "#543#" + T ;
	return(L);	
    }
    
    public void Report(String Prefix,int maxlines)
    /*
      maxlines should be >10 or so for a logical display.
     */
    {
	Str.printE("#_#zpaths:#0# " + zpaths);
	Str.printE("#_#zboxen:");
	Str.printE(Prefix +"\t#_#[0]\t[T]\t[Z]\t\t[S]\tTotal\tztype");
	{ //depth 0
	      String FL =  FourLists(Depth_0_Tab);
	      if (FL!=null)
		  {
		      Str.printE(Prefix + "\t" + FL);
		  }
	}
	{ //depth 1
	    for (int j=0;j<numtypes;j++)
		{
		    String FL =  FourLists(Depth_1_Tab[j]);
		    if (FL!=null)
			{
			    maxlines--; if (maxlines==0) {Str.print("...");return;}
			    Str.printE(Prefix + "\t" + FL + "\t#253#" + An_to_Name(j));
			}
		}
	}
	{ //depth 2
	    for (int j=0;j<numtypes;j++)
		{
		    for (int k=0;k<numtypes;k++)
			{
			    String FL = FourLists(Depth_2_Tab[j][k]);
			    if (FL!=null)
				{
				    maxlines--; if (maxlines==0) {Str.print("...");return;}
				    Str.printE(Prefix + "\t" + FL
					       + "\t#253#" + An_to_Name(k)
					       + "\t#444#∊ #255#" + An_to_Name(j));
				}
			}
		}
	}
    }

    
    private String An_to_Name(int A)
    {
	return(Ψ.ZtypeRefs.getName(A));
    }
}
