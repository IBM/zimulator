package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;

public interface zio
/*
  A class implementing this interface is provided to the Zimulator.
  This enables the Zimulator to do five things, (1)-(5) listed below.
  
  Iterators returned are used immediately and completely.

  DataInputStream and DataOutputStream for object files are also accessed "all at once" and closed.
*/
{    
    // (1). Read in initial input zsyntax, when the Zimulatior starts up. May return null.
    Iterator<Iterator<String>> getZsyntaxInputs();

    // (2). Send information to and read information from Zservers. May return null.
    Iterator<String> makeZserverRequest(String ServerIdentifier, Iterator<String> Request);

    // (3). Store and Retrieve the system state using zobject file format.
    DataOutputStream saveZobjFile();
    DataInputStream loadZobjFile();
    
    // (4). Send reporting information from a running system.
    void sendReportLine(int ReportChannel,String ReportLine);
    // As is documentation; 'Reporting' is machine-readable.
    
    // (5). Send verbose output from a running system.
    void sendVerboseLine(String VerboseLine);
}
