package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

class Accommodation
{
    /*
      The quesiton: Could φ be accommodated in γ ?

      CouldBy_xxx() routines check and return a time or a boolean:
      [This is not actually sloppy; we can compare a double exactly with 0.]
      
      t<0 : There is no possibility that can yet be known.
      t=0 : 'φ' can fit inside anytime.
      t>0 : The time at which 'φ' will be able to fit inside, OR a time at which we should again check.


      Shifting to a new zbox requires four things to be checked:
      1. zlink checking.
      2. ztype checking.
      3. Capacity checking.
      4. Geometry checking.

      2,3 can be done by Could_xxx() functions below.
      4. also but it is just a wrapper.
    */

    zsystem Ψ;
    
    zbox γ,φ;   // Could φ be accommodated in γ ?

    int NumInside;   // Number in γ
    int SpaceNeeded; // Needed within γ

    double γ_S;
    
    int verbose;
    public Accommodation(zsystem _Ψ, zbox container, zbox candidate, int _verbose)
    {
	Ψ = _Ψ;
	verbose=_verbose;
	γ = container;
	φ = candidate;

	γ_S = γ.get_S();
	
	NumInside = 0;
	if (γ.Z != null) { NumInside = γ.Z.size();}
	SpaceNeeded = φ.e.l;
	if (NumInside>0) { SpaceNeeded += γ_S; }
    }
    
    public boolean CouldBy_ztype()
    {
	return(!γ.e.CanContain(φ.e));
    }   

    public boolean CouldBy_Capacity()
    {
	boolean debug=false;
	// 'φ' can always fit into a sink:
	if (γ.e.m==7) {return(true);}
	// Check numerical constraint.
	int _n = γ.get_N();
	if (_n==0)
	    {
		//v("No space: N=0");
		return(false);
	    }
	if (_n != -1)
	    {
		if (debug) {γ.V(" Checking number: N=" + _n + "  NumInside=" + NumInside);}
		if (NumInside >= _n)
		    {
			if (debug) {γ.V("No space for ["+φ.Label+"]; already there are " + NumInside + " with max. " + γ.get_N());}
			return(false);
		    } // count limit.
	    }
	// Numerical constraints allow entry. What about sizes?
	// L applies to every containment type save 'Static' and 'Sink'. No-one should be moving into 'Static' anyway.
	if (γ.get_L()>0)
	    {
		if (γ.Space_Inside < SpaceNeeded)
		    {
			if (debug) {γ.V("Need " + SpaceNeeded + " ZSU but only have " + γ.Space_Inside);}
			return(false);
		    }
	    }
	return(true);  // Capacity checks out.
    }
    
    
    public double CouldBy_Geometry()
    {
	return(CouldBy_Geometry(0));
    }
    public double CouldBy_Geometry(double ins_x)  // supply x position.
    /*
      Determine if there is room for 'φ' to begin traversal of
      this one.  Given the way time evolves in the system, this is
      slightly more complicated than boolean.
    */
    {
	boolean debug = false;	
	/*
	  Only 'Pipe' containment type has a continuous position to check.
	  There must be sufficient space AT THE beginning; 
	  this is also the only case in which some of the future can be predicted.
	*/	
	if (γ.e.m != 2)
	    {
		// Since γ is not A pipe, there is no "geometrical" constraint.
		return(0);  // there is space at time t.
	    }

	// Pipe
	if (NumInside==0)
	    {
		return(0);  // There is space if it is empty.
	    }
	/*
	  2018-09-26
	  The impediment will be the last in the containment if x_ins==0.
	  Otherwise, we must search through to find the zboxen which are actually in the way.

	  We want to know about the interval:
	  ins_x . . .  ins_x + SpaceNeeded.
	*/

	double now = Ψ.t;
	double ins_x0 = ins_x;
	double ins_x1 = ins_x + φ.e.l + γ.get_S();

	if (ins_x==0)  // we need only check the first one we meet.
	    {
		zbox β = γ.Z.getLast();
		double β_x0 = β.x_at_t(now);
		if (β_x0>ins_x1) {return(0);}  // no impediment.
		double ct = β.t_at_x(ins_x1);
		if (ct<0.0) {return(-1.0);}  // Cannot tell.
		return(ct);
	    }

	double ClearTime = -1.0;      // AnyImp = true -> this is the time to return. Latest.
	Iterator<zbox> wiz = γ.Z.iterator();
	boolean AnyImpediment=false;
	while (wiz.hasNext())
	    {
		zbox β = wiz.next();
		double β_x0 = β.x_at_t(now);
		double β_x1 = β_x0 + β.e.l + γ_S;
		if ((β_x1>ins_x0)&&(β_x0<ins_x1))
		    { // We have an impediment at time NOW.
			double ct = β.t_at_x(ins_x1);
			if (ct<0.0) {return(-1.0);}  // Cannot tell.
			if ((AnyImpediment==false)||(ct>ClearTime)) {ClearTime = ct;}
			AnyImpediment=true;
		    }
	    }
	if (!AnyImpediment) {return(0);} // We found no impediment.
	return(ClearTime); // When to check in future.
    }
}
