package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

public interface zim
/*
  This is the interface which is implemented by the Zimulator class.

  0. Have a class ready which implements Zimulator.zio interface.

  1. Construct a Zimulation class; Zimulation(zio yours). It will contain defaults.

  [2]. Use adjustParameters() and adjustFlags() if anything but defaults is required.
  
     These contain variable=pairs and are as on commandline.

  4. Call run() to perform the simulation.
*/
{
    // Adjust various flags to control simulation:    
    public void adjustFlags(String[] VarValPairs);
    
    // Adjust parameters to comtrol simulation:
    public void adjustParameters(String[] VarValPairs);
    
    // This initiates actual simulation. It does not return until
    // simulation is finished.  During the simulation, servers might
    // be consulted. Return code is 0 on success.
    public int run();

}
