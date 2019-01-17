package com.ibm.Zimulator.SmallAux.MDA;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

class iter implements Iterator<int[]>
{
    Iterator<byte[]> I;

    public iter(Iterator<byte[]> I)
    {
	this.I=I;
    }
    
    public boolean hasNext()
    {
	return(I.hasNext());
    }

    public int[] next()
    {
	return(bidx_to_idx(I.next()));
    }

    private int[] bidx_to_idx(byte[] b)
    {
	int dim = b.length/4;
	int[] idx = new int[dim];
	for (int j=0;j<dim;j++)
	    {
		idx[j] = ((b[4*j] << 24) & 0xff000000) | ((b[4*j+1] << 16)&0x00ff0000) | ((b[4*j+2] << 8)&0xff00) | (b[4*j+3] & 0xff);
		//idx[j] = ((b[4*j] << 24)) | ((b[4*j+1] << 16)) | ((b[4*j+2] << 8)) | (b[4*j+3]);
	    }
	return(idx);
    }
}
