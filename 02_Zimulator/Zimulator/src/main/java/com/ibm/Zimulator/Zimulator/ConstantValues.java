package com.ibm.Zimulator.Zimulator;

/*
  Java has no #define directive.

  This class functions as a holder for integer and String values used in:
  > Calculation ( 0,1,2,3 map to ??? )
  > zsyntax source input (various names)
  > verbose output
  > Reporting output.

 */

import com.ibm.Zimulator.SmallAux.*;

class ConstantValues
{
    
    // Names for the types of containment.
    public String[] ModeNames;  
    String ReportPrefix;

    public ConstantValues()
    {
	// Do not change the following; integer mode values are used throughout the code!
	// String here is used only for verbosity; obviously not for computations!
	//                     1    2    3     4    5   6      7    8
	ModeNames = Str.split("Span Pipe Shelf Fifo Bag Source Sink Static");

	ReportPrefix = "R: ";
	
	
    }

}
