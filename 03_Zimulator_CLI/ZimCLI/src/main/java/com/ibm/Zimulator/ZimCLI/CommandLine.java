package com.ibm.Zimulator.ZimCLI;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.Zimulator.*;

/*
  This contains the main() function, and does the following:

  1. If help is asked for, calls Help.Usage() and exits. (-h switch)

  2. Command-line options are parsed. They are applied at the appropriate time (before or after loading files, etc.)
     (They modify, activate, or suppress some of the following.)
  3. The main 'zsystem' Ψ is constructed.
  4. Input files I=... and i=... are read, together with stdin if appropriate. Objects are added to the zsystem''.
  5. If other output than simulation is asked for (html, object file, graph output, etc.) then this is handled.
  6. Simulation occurs via zsystem.EvolveUntil().
  7. An object file describing the state of the system is written.

  2018-09-12

  The above is all done via creation of a ComLineIO class, which implements the Zimulator.io interface.
*/

public class CommandLine
{
    public static void main(String[] argv)
    /*
      main() is the command-line version.
    */
    {
	ComLineIO CommandLineIO = new ComLineIO();
	if ((argv.length==0) || Str.IsOptionPresent(argv,"h") || Str.IsOptionPresent(argv,"help") )
	    {
		Version V = new Version();
		Help.Usage(V.V);
		return;
	    }
	int verbosity=0;
	CommandLineIO.setVerbosityFile(null); // null means stdout. Activation is via -v.
	boolean termcolourflag = true;  // Pretty colours on stdout.
	if (Str.IsOptionPresent(argv,"v")) {verbosity=1;}
	{
	    String outfile=Str.GetArgAssignment(argv,"R");
	    if (outfile==null) {outfile="/dev/stdout";}
	    CommandLineIO.setReportingFile(outfile);
	}

	/*
	   verboseSS verboseSS_zl are only for command-line use.
	   They will dump directly to stdout always, not through io.verbosity.
	 */
	int verboseSS = Str.atoi(Str.GetArgAssignment(argv,"z")); // TimeRoutines.ParseTimei(Str.GetArgAssignment(argv,"z"));
	int verboseSS_zl = Str.atoi(Str.GetArgAssignment(argv,"zl"));
	String[] VerboseList = null;
	{
	    String vbl = Str.GetArgAssignment(argv,"v");
	    if (vbl!=null)
	    {
		VerboseList = Str.splitCSV(vbl);
	    }
	}
	CommandLineIO.setObjectFileIn(Str.GetArgAssignment(argv,"i"));
	CommandLineIO.setObjectFileOut(Str.GetArgAssignment(argv,"o"));

	{
	    ArrayList<String> InputSourceSpecs = new ArrayList<String>();
	    int IFN =0;
	    while(true)
		{
		    IFN++;
		    String inpspec = Str.GetArgAssignment(argv,"I",IFN);
		    if (inpspec!=null) {InputSourceSpecs.add(inpspec);}
		    else {break;}
		}	    
	    if (InputSourceSpecs.size()>0)
		{
		    CommandLineIO.setInputSourceSpecs(InputSourceSpecs);
		}
	}
	{
	    Zimulation Z = new Zimulation(CommandLineIO);
	    Z.adjustFlags(argv);
	    Z.adjustParameters(argv);
	    Z.adjustCommandLineCase(Str.IsOptionPresent(argv,"dump"),verboseSS,verboseSS_zl,VerboseList,
				    Str.GetArgAssignment(argv,"html"),Str.GetArgAssignment(argv,"PS"),
				    termcolourflag);
	    Z.run();
	}
    }

}



