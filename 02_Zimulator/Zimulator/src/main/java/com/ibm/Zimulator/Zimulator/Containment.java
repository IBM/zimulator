
package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;


/*
  All we wish to do is maintain insertion order; zboxen may not pass each other.

  This is thus really just a FIFO implementation.
*/


class Containment_Controller
{
    Containment_Xcomparator Comp;
    public Containment_Controller()
    {
	Comp = new Containment_Xcomparator();
    }    
}

class ContainmentList
{
    private int highestrank;
    private TreeSet<zbox> boxen;

    private int N;
    
    public ContainmentList(Containment_Controller CC)
    {
	boxen = new TreeSet<zbox>(CC.Comp);
	N = 0;
	highestrank=0;
    }

    public void isolate(zbox φ)
    {
	boxen.remove(φ);
	N--;
	if (boxen.size()==0)
	    {
		highestrank=0; // Reset.
	    }
    }

    public int size()
    {
	return(N);
	//return(boxen.size());
    }

    public void add(zbox φ)
    {
	highestrank++;
	φ.ContainmentRank = highestrank;  // Must be before add() !
	boxen.add(φ);
	N++;
	//Debug_DumpRanks();
    }

    /*
      The following is slow, and could be improved. 
      ( Improvement would be treeset|order for Shelf,Fifo, treeset|x for Pipe, set|Span,bag )

      Just ad-hoc for correctness so that σ_i functions properly.
      Any zboxen 'Behind' this one in line need to know that this one has been inserted.
      The easiest way is to truncate any moves they have to the insertion time.
    */
    public void add(zbox φ,double instime,BoolMap<zbox> PassingCondition)
    {
	N++;
	if (boxen.size()==0) {add(φ);return;}
	zbox γ = boxen.last();
	while (PassingCondition.decision(γ)) // pass γ ?
	    {
		//Str.print("PC : φ(" + φ.Label + ") passes γ(" + γ.Label +")");
		γ.ContainmentRank++;  // does not change extant order.
		if (γ.ContainmentRank > highestrank) {highestrank = γ.ContainmentRank;}
		γ.CutMoveTime(instime);
		γ = boxen.lower(γ);
		if (γ==null) {break;}
	    }
	// Either insert at the very lowest Rank, or else one rank (above) after γ.
	if (γ==null)
	    {
		φ.ContainmentRank = 1;
	    }
	else
	    {
		φ.ContainmentRank = γ.ContainmentRank + 1;
	    }
	if (φ.ContainmentRank > highestrank) {highestrank = φ.ContainmentRank;}		
	boxen.add(φ);
	//Debug_DumpRanks();
    }

    public Iterator<zbox> iterator()
    {
	return(boxen.iterator());
    }

    private void Debug_DumpRanks()
    {
	Iterator<zbox> wiz = iterator();
	int j=0;
	Str.print("------------------------<");
	while (wiz.hasNext())
	    {
		zbox ζ = wiz.next();
		String L = "?";
		if (ζ.z!=null) {L=ζ.z.Label;}
		Str.print("  " + L + "   "+j+": " + ζ.Label + " rank: " + ζ.ContainmentRank);
		j++;
	    }
	Str.print("------------------------>");	
    }
    
    
    public boolean contains(zbox φ)
    {
	return(boxen.contains(φ));
    }

    public zbox getPrev(zbox φ)
    {  // get zbox AHEAD of this one. (i.e. lower rank)
	zbox φ2 = boxen.lower(φ);
	//if (φ2!=null) {Str.print("This : " + φ.Label + "(" + φ.ContainmentRank + ") Ahead:" + φ2.Label + "("+φ2.ContainmentRank+")"); }
	return(φ2);
    }

    public zbox getFirst()
    {  // Get zbox which has been here the longest. (i.e. lowest rank)
	return(boxen.first());
    }

    public zbox getLast()
    {  // Get zbox which has highest rank (i.e. the newest).
	return(boxen.last());
    }

    
    

}



class Containment_Xcomparator implements Comparator<zbox>
{
    public int compare(zbox φ1,zbox φ2)
    {
	if (φ1 == φ2) {return (0);}
	// First inside has lowest number.
	if (φ1.ContainmentRank < φ2.ContainmentRank) {return(-1);}
	return(+1); // rank will never be equal.
    }
	
}



