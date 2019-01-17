package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.Network.*;

/*
  A 'zpath' might describe "intent" and in this case it can be resolved into a "plan" which
  is a complete list of zstops which describe every zbox or zpath to be utilised.
  
  SmallAux.Network is used to find the shortest path; 'ZboxNetwork' implements the interface 
  for doing this.

  PathResolution is just constructed with origin and destination, and the ZPD field is filled
  in below, with a distribution of paths (or just one).

  Only shortest paths should be calculated here in the Zimulator core. Other paths should be decided
  by an external `path-resolution server', producing a low-resolution list, and subsequently 
  segments filled in with the shortest paths.

 */

class PathResolution
{
    zsystem Ψ;    
    public zpath ZP;   // The result.
    
    private zbox φ_orig,φ_dest;
    private ztype mover;    // The type of the zbox we are going to try to move from φ_orig to φ_dest.

    private int debug;
    private Economics Eco;
    
    public PathResolution(zsystem _Ψ, zbox _φ_orig,zbox _φ_dest, ztype _mover, Economics _Eco)
    {
	debug=0;
	
        Ψ = _Ψ;φ_orig = _φ_orig;φ_dest = _φ_dest;mover = _mover; Eco = _Eco;

	if (debug==1) {Str.printnrgb("Resolving: " + φ_orig.Label + " -> " + φ_dest.Label + "  ",  2,3,4 );}
	
	ZboxNetwork Net = new ZboxNetwork(Ψ,mover,Eco);
	NetworkCalculator<ZboxNodeWrapper> NetCalc = new NetworkCalculator<ZboxNodeWrapper>(Net,2);
	// Set up wrappers for our two nodes:
	ZboxNodeWrapper nw_orig = Net.NW(φ_orig);
	ZboxNodeWrapper nw_dest = Net.NW(φ_dest);
	{
	    // Get single best path through system:
	    ArrayList<ZboxNodeWrapper> ShortPath = NetCalc.FindShortestPath(nw_orig,nw_dest);
	    if (ShortPath==null)
		{
		    ZP=null;
		    Str.printE("#500# !!! No zpath resolved! Probably disconnected O-D.");
		    return;
		}
	    zpath bestZpath = ZboxNodeWrappers_to_Zpath(ShortPath);
	    bestZpath.E = NetCalc.Path_Length;
	    //ZPD.π.add(bestZpath);
	    if (debug==1) {Str.printnrgb("Resolved: " + bestZpath.toString() ,4,3,2) ;}
	    if (debug==1) {Str.print("  L=" + NetCalc.Path_Length);}
	    ZP = bestZpath;
	}
	// Clean up:
	Net.RemoveAllRefs();	    
    }

    private zpath ZboxNodeWrappers_to_Zpath(ArrayList<ZboxNodeWrapper> ZBNWs) 
    {
	// Convert list of ZboxNodeWrappers into a zpath:
	zpath K = new zpath(Ψ);
	K.t = 1; // Plan type.
	K.e = mover;
	// Now, how many of our zboxnodewrapers are to be translated into zstops? zstops should have φ eor K
	ArrayList<zstop> ourzstops = new ArrayList<zstop>();
	ListIterator<ZboxNodeWrapper> wit = ZBNWs.listIterator();
	zpath lastK=null;
	int p=0;
	while (wit.hasNext())
	    {
		ZboxNodeWrapper nod =(ZboxNodeWrapper) wit.next();

		if (0==1)
		    {
			Str.printEn("ZZZ20180503: #555#" + p + "#500# " + nod.φ.Label);
			if (nod.K != null)
			    {
				Str.printEn(" #543# [" + nod.K.Label +"]");
			    }
			Str.print("");
		    }

		if ( (nod.K != lastK) && (nod.K != null) )
		    {
			// we are joining a zpath.
			zstop zs = new zstop(null,nod.K,-1,-1);
			ourzstops.add(zs);
			lastK=nod.K;
		    }
		if ( (nod.K==null) && (nod.φ.e.CanContain(mover)) )
		    {
			zstop zs = new zstop(nod.φ,null,-1,-1);
			ourzstops.add(zs);
		    }
		lastK=nod.K;
		p++;
	    }
	{ // toArray();
	    K.Λ_λ = new zstop[ourzstops.size()];
	    ListIterator<zstop> wof = ourzstops.listIterator();
	    int j=0; //ourzstops.size()-1;
	    while (wof.hasNext())
		{
		    K.Λ_λ[j] = wof.next();
		    j++; //j--;
		}
	}
	return(K);
    }

}    
