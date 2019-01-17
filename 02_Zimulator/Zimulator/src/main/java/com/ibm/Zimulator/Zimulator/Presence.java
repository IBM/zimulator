package com.ibm.Zimulator.Zimulator;

/*
  Routines here are used to split a 'zbox' into more than one when a decision is to be taken
  (move to the next zbox or not, etc.)

  Presence still works, but is deprecated. It is decribed in more detail within 'zbox'.
 */

public class Presence
{

    public static double TransferFraction(int N_0,double ρ_0,ztype C_0,
					  int N_1,double ρ_1,ztype C_1, double ρ )
    /*
      ( Here j ∊ {0,1}. ) 

      A zbox γ with presence ρ is to shift from zbox φ_0 to zbox φ_1.

      φ_j is of ztype C_j.

      Before the shift, φ_j contains N_j zboxen with a total presence of ρ_j (which need not all be of the same ztype).
      
      γ is to be bifurcated into two zboxen; one will make the shift, and one will not.
      This function is to return the fraction f ∊ [0,1] of ρ to be transferred to the zbox making the shift.      

      The fraction of ρ to be transferred is multiplied by one additional factor 'ρ' from the ztype.
     */
    {
	return(1.0 - 1.0/Math.PI);
    }
    
}

