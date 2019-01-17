package com.ibm.Zimulator.SmallAux;

import java.util.*;

//pjsec IntHash

public class IntHash
/*
  Maps int[] to a single integer, just like SBHH does with byte[].

  Really just a wrapper for SBHH.
*/
{
    private SBHH HH;
    public IntHash()
    {
	HH = new SBHH();
    }

    public int getidx(int[] key)
    /*
      Gets index for the key. Makes one if necessary.
     */
    {
	return(getidx(key,true));
    }

    public int chkidx(int[] key)
    /*
      Finds index for the key. -1 if key not used.
     */
    {
	return(getidx(key,false));
    }

    public int getidx(int[] key,boolean allownew)
    /*
      Gets index for the key. 'allownew' controls whether a new one can be generated.
     */
    {
	return(HH.getidx(inttobytekey(key),allownew));	
    }

    private byte[] inttobytekey(int[] key)
    {
	int kl = key.length;
	byte[] bb = new byte[4*kl];
	for (int k=0;k<kl;k++)
	    {
		bb[4*k] = (byte) (0xff & (key[k]>>24));
		bb[4*k+1] = (byte) (0xff & (key[k]>>16));
		bb[4*k+2] = (byte) (0xff & (key[k]>>8));
		bb[4*k+3] = (byte) (0xff & key[k]);
	    }
	return(bb);
    }

}
