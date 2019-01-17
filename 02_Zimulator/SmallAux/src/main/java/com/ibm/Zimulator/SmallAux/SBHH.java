package com.ibm.Zimulator.SmallAux;
import java.util.*;

//pjsec SBHH
/*
  # class SBHH

  String-or-Byte Hierarchical Hash.

  * These routines just translate String keys (or byte[] keys) to integer indices for use
  in, for example, an ArrayList.

  * To associate String labels with a set of some type of object,
  consider using the HHH class.

  * After initialising SBHH(), just use the getidx() method to
  translate String hashes to successive integer indices.

  * Go the other direction with getstringkey(), which returns the hash String
  given the integer index.
    
*/

public class SBHH
{
    private int length;      // 1 + largest index generated so far.
    private onelevlist top;  // top-level list.
    private ArrayList<byte[]> flat;  // for going backwards.

    public SBHH()
    {
	length=0;
	top = new onelevlist();
	flat = new ArrayList<byte[]>();
    }
    
    class onelevlist
    {
	public int j;         // Index for sequence which stops at previous level, or -1.
	public onelevlist[] n;  // For next level. [256] or null.
	public onelevlist()
	{
	    j = -1;
	    n = null;
	}
    }

    public int size()
    /*
      Returns the number of keys which have been stored.
     */
    {
	return(length);
    }

    public int getidx(String key)
    /*
     */
    {
	byte[] bb;
	if (key=="")
	    {
	        bb = new byte[0];
	    }
	else
	    {
		bb = key.getBytes();  // Not proper utf-8, but as long as 1-1 then it is fine.
	    }
	return(getidx(bb));
    }
    public int chkidx(String key)
    /*
     */
    {
	return(chkidx(key.getBytes()));  // Not proper utf-8, but as long as 1-1 then it is fine.
    }
    public String getstringkey(int idx)
    /*
      Supposing getidx() was used with a String-valued key, this performs the reverse.
      Otherwise, it will likely produce an error.
     */
    {
	byte[] kk = getkey(idx);
	if (kk == null) {return(null);}
	if (kk.length == 0) {return("");}
	return(Str.setUtf8(kk));
    }

    class skeyi implements Iterator<String>
    {
	Iterator<byte[]> I;
	public skeyi()
	{
	    I = flat.iterator();
	}
	public boolean hasNext()
	{
	    return(I.hasNext());
	}
	public String next()
	{
	    return(Str.setUtf8(I.next()));
	}	
    }
    
    public Iterator<String> stringkeyiterator()
    /*
      Supposing getidx() was used with String-valued keys,
      this returns an iterator over the String keys used.
      (Otherwise, it will likely produce an error.)
      They will be fetched in order, from 0... size()-1.
     */
    {
	return(new skeyi());
    }




    public int getidx(byte[] key)
    /*
      Generate an integer index corresponding to this key, which has
      length >=0.  The same key will always produce the same integer,
      and all integers produced from a given SBHH will be in sequence
      from zero to size()-1.

      If key is null, returns -1. This is not the same as a zero-length key.
    */
    {
	return(getidx(key,true));
    }
    public int chkidx(byte[] key)
    /*
      If this key has been given an index, return the index, otherwise -1.
    */
    {
	return(getidx(key,false));
    }

    public int getidx(byte[] key,boolean allownew)
    /*
      Exactly like getidx() when allownew=true.
      Exactly like chkidx() when allownew=false.
     */
    {
	if (key==null) {return(-1);}
	int klim = key.length;
	onelevlist oll = top;
	boolean newflag = false;
	if (klim>0)
	    {
		// Str.printEn(" #252# key: ");
		for (int k=0;k<klim;k++)
		    {
			int kk = 0xff & key[k];
			//Str.printEn(" #050#" + kk + "("+oll.j+")");
			if (oll.n == null)
			    {
				oll.n = new onelevlist[256];
			    }
			if (oll.n[kk]==null)
			    {
				newflag = true;
				oll.n[kk] = new onelevlist();
			    }
			oll = oll.n[kk];
		    }
		//Str.print("");
	    }
	// k == klim
	int idx = oll.j;
	if (!allownew)
	    {
		return(idx); // might be -1.
	    }
	if (idx==-1)
	    {
		newflag = true;
		idx = length;
		oll.j = idx;
		length++;
	    }
	if (newflag)
	    {
		flat.add(Arrays.copyOf(key,key.length));
	    }
	return(idx);
    }

    public Iterator<byte[]> keyiterator()
    /*
      Returns an iterator over the byte[] keys used.
      They will be fetched in order, from 0... size()-1.
     */
    {
	return(flat.iterator());
    }

    
    
    public byte[] getkey(int idx)
    /*
      Gets a copy of the byte[] key associated with the given index.
      Returns null if there is no key.
    */
    {
	if ((idx<0) || (idx>=length)) {return(null);}
	byte[] ourkey = flat.get(idx);
	byte[] key = Arrays.copyOf(ourkey,ourkey.length);
	return(key);
    }
        
}
