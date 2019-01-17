package com.ibm.Zimulator.SmallAux.Network;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

/*
      --------------------------------------------
      Implement Network interface for use on our graph of paths!
      A 'Node' Object is a 'DegradablePath'
      'edge lengths' are differential lengths for the lengths of our paths.
      --------------------------------------------
*/
class PathDegradationNet<NetNode> implements Network<DegradablePath<NetNode>>
{
    /*
      Given one node, return all adjacent nodes and edge lengths.
    */
    public NetworkNodeList<DegradablePath<NetNode>> ConnectedNodes(DegradablePath<NetNode> Node)
    {
	DegradablePath<NetNode> DP = Node;
	ArrayList<DegradablePath<NetNode>> DPs = DP.Neighbours();
	NetworkNodeList<DegradablePath<NetNode>> NNL = new NetworkNodeList<DegradablePath<NetNode>>();
	ListIterator<DegradablePath<NetNode>> wif = DPs.listIterator();
	while (wif.hasNext())
	    {
		DegradablePath<NetNode> onepath = wif.next();
		double edgelen = onepath.Length() - DP.Length();
		NNL.Nodes.add(onepath);
		NNL.Lengths.add(edgelen);
	    }
	//Str.print("ConnectedNodes(): " + NNL.Nodes.size());
	return(NNL);
    }

    /*
      'Mark' a given node in the network as marked, and assign it an
      integer non-negative label.
    */
    public void MarkNode(DegradablePath<NetNode> Node,int Label)
    {
	// We just do nothing, SINCE the graph is directed such that we never get to a node twice.
	DegradablePath<NetNode> DP = (DegradablePath<NetNode>) Node;
    }

    /*
      Find out if a given node is marked.
      Returns -1 if unmarked, otherwise the integer Label.
    */
    public int IsNodeMarked(DegradablePath<NetNode> Node)
    {
	return(-1);
    }

    /*
      'Unmark' a given node in the network.
    */
    public void UnmarkNode(DegradablePath<NetNode> Node)
    {
    }

    public void Mark2Node(DegradablePath<NetNode> Node,boolean Label)
    {
	// We just do nothing.
	DegradablePath<NetNode> DP = (DegradablePath<NetNode>) Node;
    }
    public boolean IsNodeMarked2(DegradablePath<NetNode> Node)
    {
	// We just do nothing.
	DegradablePath<NetNode> DP = (DegradablePath<NetNode>) Node;
	return(false);
    }

    public boolean IsNodeGood(DegradablePath<NetNode> Node)
    {
	DegradablePath<NetNode> DP = (DegradablePath<NetNode>) Node;

	// We should decide if the node is a loop or anything like that!

	    


	return(true);
    }

	

    /*
      Progress report; just for debugging. Progress() is called
      intermittantly, depending on the calculation being performed.
    */
    public void Progress()
    {
    }
    /*
      For debugging. It is useful to have a way to dump to stdout
      a description of a given node. 
    */
    public void PrintNode(DegradablePath<NetNode> Node)
    {
    }
    /*
      A Double-marked node should behave just like a marked node.
      Progress() might decide to display it differently is all.
    */
    public void DblMarkNode(DegradablePath<NetNode> Node)
    {
    }
}
