package com.ibm.Zimulator.ZimCLI;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.Zimulator.*;

class ComLineIO implements zio
/*
  The io interface is implemented for command-line usage.
  
  In particular, this is used by the main() method in Zimulate class, 
  which is the command-line entry point.
*/
{
    private ArrayList<String> InputSourceSpecs;   // List of filenames or http addresses.
    private String ReportingFilename;
    private String VerbosityFilename;
    private FileOutputStream out_veb;
    private FileOutputStream out_rep;
    private String InputObjFilename;
    private String OutputObjFilename;

    /*
      Construction
     */
    public ComLineIO()
    {
	InputSourceSpecs = null;
	OutputObjFilename = null;
	ReportingFilename=null;
	VerbosityFilename=null;
	InputObjFilename=null;
	OutputObjFilename=null;
	out_veb=null;
	out_rep=null;
    }
    /*
      Addition of data
     */
    public void setInputSourceSpecs(ArrayList<String> _InputSourceSpecs)
    {
	InputSourceSpecs = _InputSourceSpecs;
    }    
    public void setObjectFileIn(String filename)
    {
	InputObjFilename = filename;
    }
    public void setObjectFileOut(String filename)
    {
	OutputObjFilename = filename;
    }
    public void setVerbosityFile(String filename)
    {
	VerbosityFilename = filename;
	if (filename==null) {return;}
	try {out_veb = new FileOutputStream(VerbosityFilename);}
	catch (FileNotFoundException FNF) {out_veb=null;}
    }
    public void setReportingFile(String filename)
    {
	ReportingFilename = filename;
	if (filename==null) {return;}
	try {out_rep = new FileOutputStream(ReportingFilename);}
	catch (FileNotFoundException FNF) {out_rep=null;}
    }
    /*
      Implementation
     */
    public Iterator<Iterator<String>> getZsyntaxInputs()  //1.
    {
	if (InputSourceSpecs==null) {return(null);}
	ArrayList<Iterator<String>> ZsyntaxSources = new ArrayList<Iterator<String>>();
	Iterator<String> wif = InputSourceSpecs.iterator();
	while (wif.hasNext())
	    {
		String IS = wif.next();
		sendVerboseLine("# Input from. . . . . " + IS);
		Iterator<String> FLI = FileOrHttpLineIterator.get(IS);
		if (FLI!=null)
		    {
			ZsyntaxSources.add(FLI);
		    }
	    }
	return(ZsyntaxSources.iterator());
    }



    // 2. Send information to and read information from Zservers.

    public Iterator<String> makeZserverRequest(String serverspec, Iterator<String> Request)
    /*
      This command-line version only supports http://
      It is for testing.
     */
    {	
	HttpLineIterator HTTPLI;
	try {
	    HTTPLI = new HttpLineIterator(serverspec,Request);
	} catch (Exception anything)
	    {
		HTTPLI = null;
	    }
	return(HTTPLI);
    }

    // 3. Store and Retrieve the system state using zobject file format.

    public DataOutputStream saveZobjFile()
    {
	if (OutputObjFilename==null) {return(null);}
	DataOutputStream ObjDOS = null;
    	FileOutputStream FOS;
	try {FOS = new FileOutputStream(OutputObjFilename);}
	catch (FileNotFoundException FNF) { FOS=null;}
	if (FOS != null)
	    {
		ObjDOS = new DataOutputStream(FOS);
	    }
	return(ObjDOS);
    }

    public DataInputStream loadZobjFile()
    {
	if (InputObjFilename==null) {return(null);}	
	DataInputStream DIS = null;
	FileInputStream FIS = null;
	try {FIS = new FileInputStream(InputObjFilename);}
	catch (FileNotFoundException FNF) { FIS=null;}
	if (FIS != null)
	    {
		DIS = new DataInputStream(FIS);
	    }
	return(DIS);
    }	
    
    // 4. Send reporting information from a running system.
    
    public void sendReportLine(int Channel, String reportline)
    {
	// (We do not care about channel for CLI.)
	if (out_rep==null) {return;}
	String S = reportline + "\n";
	try { out_rep.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem. 
    }
    
    // 5. Send verbose output from a running system.

    public void sendVerboseLine(String verboseline)
    {
	if (out_veb != null)
	    {
		String S = verboseline + "\n";
		try { out_veb.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem. 
	    }
	else
	    {
		Str.print(verboseline);  // stdout.
	    }
    }
}

