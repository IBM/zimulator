package com.ibm.Zimulator.SmallAux.Network;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

/*
  For each node we deal with we wish to
  store additional information in this wrapper.
*/
class NodeInfo<NetNode>
{
    NetNode Node;  // Reference to the real 'user' node.
    double Dist;      // Best distance
    NodeInfo<NetNode> Parent;  // Best parent      Matches Parents[0] when Parents is used.
    boolean Visited;
    ArrayList<NodeInfo<NetNode>> Parents;   // List of possible parents. For standard Djikstra, this is unused.
    ArrayList<Double> Dists;   // List of distances associated with parents.
    private boolean KeepAll;
    public boolean GoodNode;
    public NodeInfo(NetNode _Node,NodeInfo<NetNode> _Parent,double _Dist,boolean _KeepAll)
    {
	GoodNode=false; // until we verify.
	Parent = _Parent;
	Dist = _Dist;
	Node = _Node;
	KeepAll=_KeepAll;
	    
	if (KeepAll)
	    {
		Parents = new ArrayList<NodeInfo<NetNode>>();
		//Parents.add(_Parent);
		Dists = new ArrayList<Double>();
		//Dists.add(_Dist);
	    }
	Visited=false;
    }
    /*
      Updates whom we have for parents given the provided distance, and mark us as visited.
    */
    public int UpdateParents(double newdist,NodeInfo<NetNode> newparent)
    {
	// In any case, we always add to our list of possibilities:
	if (KeepAll)
	    {
		Parents.add(newparent);  // We keep all other possibilities also.
		Dists.add(newdist);	    
	    }
	if ( (!Visited) || (newdist < Dist) ) // we're either unvisited (Dijstra ∞) or else larger than the new dist.
	    {
		Dist = newdist;
		Parent = newparent;
		Visited = true;
		return(1);
	    }
	return(0);
    }

    /*
      In KeepAll mode, sorts parents and associated distances, smallest first.
    */	
    public void SortParents()
    {
	int[] ord = MLR.asort(Dists); // slightly slow.
	ArrayList<NodeInfo<NetNode>> NewParents = new ArrayList<NodeInfo<NetNode>>();
	ArrayList<Double> NewDists = new ArrayList<Double>();
	for (int j=0;j<ord.length;j++)
	    {
		NodeInfo<NetNode> par = Parents.get(ord[j]);
		if (j>0)
		    {
			if (par == Parents.get(ord[j-1]))
			    {
				continue; // never duplicate parents.
			    }
		    }
		NewParents.add(Parents.get(ord[j]));
		NewDists.add(Dists.get(ord[j]));
		// Str.print("Parents: ("+j+") " + NewParents.get(j) +"," + NewDists.get(j));
	    }
	Parents=NewParents;
	Dists=NewDists;
    }
	    
}
