package com.ibm.Zimulator.Zimulator;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.Network.*;

/*
  Here is implemented implement the 'SmallAux.Network' interface for the system of zboxen.
  Instead of feeding the 'Network' routines our nodes directly, they are wrapped in the 'ZboxNodeWrapper' class.
  At a small performance cost, this way there is no need to pollute 'zbox' with 'mark' or 
  'label' functionality, and also it is possible to keep track of the zpaths which are taken. 
  
  However, a ZboxNodeWrapper field in 'zbox' is required in order to reference back. :-/
*/

class ZboxNetwork implements Network<ZboxNodeWrapper>
{
    private ArrayList<ZboxNodeWrapper> NWs;  // Keep track of them all, so the NWs can be deleted later.

    private zsystem Ψ;
    private ztype mover;
    private Economics Eco;
    
    public ZboxNetwork(zsystem _Ψ, ztype _mover,Economics _Eco)
    {
	Ψ = _Ψ;
	mover = _mover;
	Eco = _Eco;
	NWs = new ArrayList<ZboxNodeWrapper>();
    }

    public ZboxNodeWrapper NW(zbox φ)
    {
	if (φ.NodeWrapper==null)
	    {
		φ.NodeWrapper = new ZboxNodeWrapper(φ,null,0);
		NWs.add(φ.NodeWrapper);  // Keep track of them all, so we can delete the zbox NW references later.
	    }
	return(φ.NodeWrapper);
    }

	
    public void RemoveAllRefs()
    {
	// Remove all NodeWrapper references from the zboxen we touched:
	{
	    ListIterator<ZboxNodeWrapper> wit = NWs.listIterator();
	    while (wit.hasNext())
		{
		    wit.next().φ.NodeWrapper=null;
		}
	}	    
    }

    public NetworkNodeList<ZboxNodeWrapper> ConnectedNodes(ZboxNodeWrapper NW_Node)
    {
	NetworkNodeList<ZboxNodeWrapper> NNL = new NetworkNodeList<ZboxNodeWrapper>();
	ZboxNodeWrapper NW =  NW_Node;

	//Str.printE("ZPDCH:  #555# STARTING ZBOX: #533# " + NW.φ.Label);
	// Abstract NODE for 'Network' is Entry to a Zbox, or Shifting through a zbox.
	// Abstract EDGE LENGTH is thus the combined cost from entry to entry.

	// If it is a sink, then we effectively have a dead end:
	if ( (NW.φ.e.m==7) ) {return(NNL);}

	/*
	 What are the connected NODES? (We have rewritten this on 2018-05-02.)
	 Suppose we are in φ:
	 
	 (I). zboxen in which mover can be contained, via zlink shift (including implicit ones). : This is Λ'
	 (II). If a zpath passes through φ, then the subsequent zbox visited by the zpath (in whose followers containment is possible)
	 (III). Any element of Λ^> \ φ through which passes a suitable zpath.
	*/

	{ //  (I) : Λ'
	    HashSet<zbox> NextZboxen = NW.φ.Get_Λp(mover.A,mover.n);
	    if (NextZboxen!=null)
		{
		    for (zbox candidate_φ : NextZboxen)
			{
			    if (candidate_φ.e.q!=0) {continue;} // the container does not support explicit consideration.
			    // We let the 'Network' class handle everything.
			    NW(candidate_φ); // make sure we have a NodeWrapper.
			    // Here is where we determine the edge length; make it simple for now.
			    double EdgeLen = Eco.EdgeLen(
							 candidate_φ.TimeToTraverse(mover),
							 candidate_φ.CostToTraverse(mover),
							 candidate_φ.e.CanContain(mover) ? 1 : 0,
							 1, // Λ' contains zboxen some number of zlinks away... Modify this!
							 0
							 );
			    //Str.printE("ZPDCH:  #550# Λ' connected: #530# " + candidate_φ.Label);				
			    {
				NNL.Nodes.add(candidate_φ.NodeWrapper);
				NNL.Lengths.add(EdgeLen);
			    }
			}
		}
	} // (I)


	
	{ /*
	    zpaths; any suitable passing within φ or Λ^>.
	  */
	    HashSet<zbox> NextZboxen = NW.φ.Get_Λd(mover.A,mover.n); // Λ^>
	    int zbil=1;
	    if (NextZboxen!=null) {zbil += NextZboxen.size();}
	    /*
	      The point of the next few lines is always to include NW.φ in the list of zboxen to check.
	      φ is not always in Λ^>(φ).
	    */
	    Iterator<zbox> wnzb = NextZboxen.iterator();
	    for (int zbi=0;zbi<zbil;zbi++)
		{				
		    zbox pathcand_φ;
		    if (zbi==zbil-1) {pathcand_φ = NW.φ;}
		    else
			{
			    pathcand_φ = wnzb.next();
			    if (pathcand_φ == NW.φ) {continue;}
			}
		    // Str.printE("ZPDCH:  #550# zpath Λ^> candidate: #350# " + pathcand_φ.Label);
		    HashSet<zpath> PathsThroughHere = Ψ.AllActivePathsThroughZbox(pathcand_φ,0);
		    if (PathsThroughHere!=null)
			{
			    Iterator<zpath> pths = PathsThroughHere.iterator();
			    while (pths.hasNext())
				{				
				    zpath PathToConsider = pths.next();
				    if (PathToConsider.e.CanContain(mover))
					{
					    // The zpath needs a Route, or else we cannot consider it.
					    if (PathToConsider.ZR==null)
						{
						    PathToConsider.ZR = new Route(PathToConsider,mover);
						}
					    ArrayList<Integer> NextBoxenidx;
					    // Next zboxen here and there after this one.
					    NextBoxenidx = PathToConsider.ZR.ThisVisited_byIdx(pathcand_φ);
					    if (NextBoxenidx.size()!=0)
						{
						    for(int l=0;l<NextBoxenidx.size();l++)
							{
							    int idx = NextBoxenidx.get(l);
							    if (pathcand_φ == NW.φ) { idx++; } // if zpath goes through φ, then next stop.
							    zbox candidate_φ = PathToConsider.ZR.Zboxen.get(idx);
							    // We let the 'Network' class handle everything.
							    ZboxNodeWrapper nw = NW(candidate_φ); // make sure we have a NodeWrapper.
							    nw.K = PathToConsider;
							    // Here is where we determine the length; make it simple for now.
							    double EdgeLen = Eco.EdgeLen(
											 nw.K.get_zstop_time_interval(idx),
											 0,
											 1,
											 1, // One [implicit] zlink away.
											 (PathToConsider==NW.K) ? 0 : 1
											 // /new/ vehicle (on zpath) is expensive. i.e. transfer
											 );
							    //Str.printE("ZPDCH:  #550# zpath connected: #510# " + nw.φ.Label);
							    NNL.Nodes.add(nw);
							    NNL.Lengths.add(EdgeLen);
							}
						}
					} // contain
				} // paths
			} // has paths
		} // zbox in Λ^>
	} // zpaths
	
	//Str.printE("ZPDCH:  #550# NNL size: #225# " + NNL.Nodes.size() );
	return(NNL);
    }
	
    /*
      Mark and Mark2
    */
    public void MarkNode(ZboxNodeWrapper NW,int Label)
    {
	NW.mark=1;
	NW.label=Label;
    }
    public int IsNodeMarked(ZboxNodeWrapper NW)
    {
	if (NW.mark==0) {return(-1);}
	return(NW.label);
    }
    public void UnmarkNode(ZboxNodeWrapper NW)
    {
	NW.mark=0;
    }
    public void Mark2Node(ZboxNodeWrapper NW,boolean M)
    {
	NW.mark2=M;
    }
    public boolean IsNodeMarked2(ZboxNodeWrapper NW)
    {
	return(NW.mark2);
    }

    public boolean IsNodeGood(ZboxNodeWrapper NW)
    {
	return(true);
    }

    public void Progress()
    {
	if (0==1)
	    {
		ListIterator<ZboxNodeWrapper> wit = NWs.listIterator();
		Str.print("ᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕᅔᅕ");
		while (wit.hasNext())
		    {
			ZboxNodeWrapper n = wit.next();
			if (n.mark==1) // front. :-)
			    {
				PrintNode(n);
			    }
		    }
	    }
    }
    public void PrintNode(ZboxNodeWrapper NW)
    {
	//Str.print("Network Node:" + NW.toString());
	Str.printn("~" + NW.toString() + "~");
    }
    public 	void DblMarkNode(ZboxNodeWrapper NW)
    {
	NW.mark=2;
    }

}
    
    
    

class ZboxNodeWrapper
{
    /*
      Maybe the mover can be contained in φ, maybe not.
      Maybe φ is on a path for our putative container; in this case, the path is K. otherwise K=null.
      When K !=null, i is the index number in K's zroute.
    */
    public zbox φ;            
    public zpath K;
    public int i;

    public int mark;
    public boolean mark2;
    public int label;
	
    public ZboxNodeWrapper(zbox _φ,zpath _K,int _i)
    {	    
	φ = _φ;
	K = _K;
	i = _i;
	mark=0;label=0;
	φ.NodeWrapper = this;
    }

    public String toString()
    {
	String I = "";
	I += φ.Label;
	if (K!=null) { I += "(" + K.Label + "." + i + ") ";}
	else { I += " ";}
	return(I);
    }
}
    
