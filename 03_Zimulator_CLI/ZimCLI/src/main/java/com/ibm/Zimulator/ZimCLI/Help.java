package com.ibm.Zimulator.ZimCLI;

/*

  Help.Usage() is called when the Zimulator is run from the command line with -h.
 
  Command-line tools should document themselves.

 */
import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

public class Help
{
    static void Usage(String VersionSpec)
    {
	//            xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
	Str.printE("#543#--------------------------------------------");
	Str.printE("#555*#  Zimulator -- " + VersionSpec);
	Str.printE("#543#--------------------------------------------");
	Str.printE("#234#Function:");
	Str.printE("#344#  Simulate the dynamical system described by inputs; see documentation. ");
	Str.printE("#543#--------------------------------------------");
	Str.printE("#234#Java library usage:");
	Str.printE("#234#See #454/#Source/zim.java#0##234# and #454/#Source/zio.java");	
	Str.printE("#543#--------------------------------------------");
	Str.printE("#234#Command-line Usage:");
	Str.printE("#353#$ #454#java -jar Z.jar [#234#options#454#] [#234#parameters#454#]");
	Str.print("");
	Str.printE("#234#[options] should be specified separately. User options:");
	Str.printE("#335#  -h -help     #353#  --  Print the present output and exit; no simulation.");
	Str.printE("#335#  -ns          #353#  --  Inhibit simulation. Useful with html or dump, or with 'o' for compilation.");	
	Str.printE("#335#  -v           #353#  --  Print verbose output to terminal while simulating (for human).");
	Str.printE("#335#  -pauses      #353#  --  Sleep for 1s on every zbox shift. ('useful' with #335#-v#353#)");
	Str.printE("#335#  -razo        #353#  --  After loading, Report #*353#all#0##353# zboxen once.");
	Str.printE("#335#  -dump        #353#  --  Dump system configuration in human-readable form to stdout, ");
	Str.printE("#353#                     immediately after reading inputs.");
	Str.print("");
	Str.printE("#234#[parameters] are always of the form 'param=value':");
	Str.printE("#335#  t_1   #353#-- Simulation stopping time. Overrides that of the zsystem.");
	Str.printE("#224#  t_0   #242#-- Simulation starting time. Overrides that of the zsystem. Care should be exercised.");
	Str.printE("#335#  R     #353#-- Output filename for reporting; defaults to /dev/stdout. (for Machine)");
	Str.printE("#335#  z     #353#-- Simulated time interval at which to issue terminal reports (for Human)");
	Str.printE("#353#           on stdout; defaults to 0 := no reporting. ");
	Str.printE("#335#  zl    #353#-- When z is nonzero, this is how many lines to display on terminal.");
	Str.printE("        #353#   The special case of zl=1 indicates a status line with no terminal clearing.");
	Str.printE("#354#  #*#i#0##353#     -- Input #/#Object File#0##353#. This can be specified only once,");
	Str.printE("#353#           and will be loaded before any source files.");
	Str.printE("#354#  #*#o#0##353#     -- Output #/#Object File#0##353#. System state will be written at end.");
	Str.printE("#335#  html #353# -- Output html file. System description will be written at end.");
	Str.printE("#335#  PS   #353# -- Output simple PS file of all zboxen on one page; written at end.");
	//Str.printE("#353/#       (Output of these files is post-simulation, at time t_1, unless -ns specified)");
	Str.printE("#335#  I    #353# -- Input Source (zsyntax). If this is specified more than once, then");
	Str.printE("#353#           the sources are loaded in order. These can be files or URLs #533#(e.g. http://)#353#.");
	Str.printE("#335#  v    #353# -- Only effective without -v switch. Activates verbosity only for zboxen in");
	Str.printE("#353#           the provided comma-separated list; e.g. v=Label1,Label2,...");
	Str.printE("#511# All files are read or written sequentially and completely; can therefore be pipes or devices.");
	//Str.printE("#353# Compilation without simulation is effected by o=... and -ns.");
	Str.print("");
	Str.printE("#234#[options] for debugging and development:");
	Str.printE("#335#  -noneighcache #353#-- Disables the neighbourhood Λ caches.");
	Str.printE("#335#  -nozpathcache #353#-- Disables resolved-zpath caching, overriding input specification.");
	Str.printE("#335#  -nozdemand    #353#-- Disables state processing of zdemand objects.");
	Str.printE("#335#  -nosleeping   #353#-- Prevents any zbox from going to sleep.");
	Str.printE("#543#--------------------------------------------");
	return;
    }

    
}


	
