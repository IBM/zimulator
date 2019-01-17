package com.ibm.Zimulator.SmallAux.Network;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

public class NetworkCalculator<NetNode>
/*
  These are some routines to calculate things related to a graph.

  Steps for use:

  1. Set up a class for the graph of interest, implementing a 'Network' interface 'SomeNet'.
     Note that the graph so referenced need not be fully parsed or even fully available, 
     depending on the computation desired.

  2. NetworkCalculator NC = new NetworkCalculator(SomeNet,vl);
      where vl contains verbosity flags: (when verbose, Progress() is called)
     vl = 2: Progress() is called every time the front advances in Djikstra stage. 
             Leaving this on with empty Progress() is fast.
     vl = 1: Progress() is called after multiple-path calculations. SLOW, even if Progress() is empty.
     vl = 4: Progress() called once after Single Djikstra path determined.

  Now, any of the following can be done:

  <>. To find at Num closest nodes in a graph from some Starting point:
     public ArrayList<NetNode> FindClosestNodes(NetNode _StartingNode,int Num)
     which returned an ordered list, closest first.
     If the graph doesn't contain enough nodes, less than 'Num' will be returned.
     More than 'Num' might be returned, depending on connectivity.
     
     In this case, 'mark' and 'unmark' functions are only used to avoid visiting nodes more than once.
     If the graph is directed and non-intersecting, they can be empty.

  <>. To find the shortest path through a graph from Node_1 to Node_2:
     public ArrayList<NetNode> FindShortestPath(NetNode _Node1,NetNode _Node2)  
     Which provides an ordered list from Node1 to Node2.
     The public field double Path_Length will be set to the total path length.

  <>. To find the 'Num' shortest paths through a graph from Node_1 to Node_2:
      public ArrayList<ArrayList<NetNode>> FindShortestPaths(NetNode _Node1,NetNode _Node2,int Num,double Fac)
      The public "double[] Path_Lengths" will be set.

      'Fac' is not yet implemented; see the function.


  4. All nodes which were marked are unmarked at the end of calculation.
     Not only are the nodes left in their original state of marking (though with labels)
     but the 'NetworkCalculator' object can be re-used.
*/
{
    private Network<NetNode> N;  // some implementation of a Network Interface.    
    private int NodeLimit,MarkedNodes,MarkedGoodNodes;
    private int verbosity;
    public NetworkCalculator(Network<NetNode> _N,int vl) // Nodelimit=0 means unlimited.
    {
	N = _N;
	NodeLimit=0;
	MarkedNodes=0;
	MarkedGoodNodes=0;
	Path_Lengths=null;
	verbosity=vl;
    }

    

    /*
      ============================================================================================================
      Djikstra algorithm for shortest path on graph.
      ============================================================================================================
     */

    private NetNode Node1,Node2;
    public double Path_Length;
    public ArrayList<NetNode> FindShortestPath(NetNode _Node1,NetNode _Node2)
    /*
      Returns a list of nodes describing the shortest path from Node1 to Node2.
      If there is no path (i.e. something goes wrong) then returns null.
     */	
    {
	Node1=_Node1;Node2=_Node2;
	KeepAll=false;         // Only best choices.
	NodeLimit=0;
	Djikstra(Node1,Node2); // Stop at Node2.

	// Build Solution Path.
	ArrayList<NetNode> Soln = new ArrayList<NetNode>();
	{
	    ArrayList<NodeInfo<NetNode>> Pni = NodesAlongBestPath();
	    if (Pni==null) {return(null);} // Disconnected graph. :-( Not our fault.
	    ListIterator<NodeInfo<NetNode>> wif = Pni.listIterator();
	    while(wif.hasNext())
		{
		    Soln.add(wif.next().Node);
		}
	}
	Path_Length = NoodsAll.get(N.IsNodeMarked(Node2)).Dist;
	UnmarkAllUsedNodes();
	if ((verbosity & 4) !=0) {ShowPathUsingProgress(Soln);}
	return(Soln);
	
    }
    
    public ArrayList<NodeInfo<NetNode>> NodesAlongBestPath()
    {
	ArrayList<NodeInfo<NetNode>> P = new ArrayList<NodeInfo<NetNode>>();
	{
	    ArrayList<NodeInfo<NetNode>> Solnbk = new ArrayList<NodeInfo<NetNode>>();
	    int idxn2 = N.IsNodeMarked(Node2); // will be marked if we made it there.
	    if (idxn2==-1) {return(null);}  // We did not get to Node2. (disconnected graph)
	    NodeInfo<NetNode> ni = NoodsAll.get(idxn2);
	    while (ni.Node!=Node1)
		{
		    Solnbk.add(ni);
		    ni=ni.Parent;
		}
	    Solnbk.add(ni);  // Node1.
	    // Now, reverse order so we go from 1->2
	    int jlim=Solnbk.size();
	    for (int j=0;j<jlim;j++)
		{
		    NodeInfo<NetNode> nii = Solnbk.get(jlim-1-j);
		    P.add(nii);
		}
	}
	return(P);
    }

    
    private void UnmarkAllUsedNodes()
    { // Unmark all our nodes; clean up.
	ListIterator<NodeInfo<NetNode>> wif = NoodsAll.listIterator();
	while (wif.hasNext())
	    {
		N.UnmarkNode(wif.next().Node);
	    }
    }
    private void ShowPathUsingProgress(ArrayList<NetNode> Soln)
    { // Just debugging: Double-mark our path, Progress() and then un-mark again. :-D
	ListIterator<NetNode> wif = Soln.listIterator();
	while (wif.hasNext())
	    {
		N.DblMarkNode(wif.next());
	    }
	N.Progress();
	wif = Soln.listIterator();
	while (wif.hasNext())
	    {
		N.UnmarkNode(wif.next());
	    }
    }    
    
    private ArrayList<NodeInfo<NetNode>> NoodsAll;
    private boolean KeepAll;

    /*
      Performs Djikstra to mark graph (setting Parents on edges) propagating to Node_D. 
      If Node_D is null, does Djikstra marking on whole graph.
      KeepAll: Keep paths which are not best.

      If NodeLimit is non-zero, then simply quits after marking this many [good] nodes 
      (and maybe a few more, depending on size of propagating front, etc.)
      This is useful in the multi-path algorithm, described below.
    */
    private void Djikstra(NetNode Node_O,NetNode Node_D)
    {
	MarkedNodes=0;
	MarkedGoodNodes=0;
	// This is a list of all we have visited: Index is Label.
	NoodsAll = new ArrayList<NodeInfo<NetNode>>();
	LinkedList<NodeInfo<NetNode>> Front = new LinkedList<NodeInfo<NetNode>>();
	{ // Set up front with single node.
	    NodeInfo<NetNode> FirstNode = new NodeInfo<NetNode>(Node_O,null,0,KeepAll);
	    FirstNode.Visited = true;
	    NoodsAll.add(FirstNode);
	    N.MarkNode(FirstNode.Node,0); MarkedNodes++;
	    if (N.IsNodeGood(FirstNode.Node)) {FirstNode.GoodNode=true;MarkedGoodNodes++;}
	    Front.add(FirstNode);
	}
	if ((verbosity & 2)!=0) {N.Progress();}
	while (Front.size()!=0)
	    {
		AdvanceFront(Front,Node_D);
		if ((verbosity & 2)!=0) {N.Progress();}
	    }
    }

    private void AdvanceFront(LinkedList <NodeInfo<NetNode>> Front,NetNode Node_D)
    {
	NodeInfo<NetNode> First = Front.get(0);
	// Have we found our destination?
	if (First.Node == Node_D)
	    {
		Front.clear(); // no more front.
		return;
	    }
	// Have we done enough nodes?
	if (NodeLimit!=0)
	    {
		if (MarkedGoodNodes>=NodeLimit)
		    {
			Front.clear(); // no more front.
			return;
		    }
	    }
	Front.remove(0);
	N.DblMarkNode(First.Node);
	ProcessNode(First,Front);
	/*
	  Str.printn("Front has " + Front.size() + " : " );
	  {
	    ListIterator<NodeInfo<NetNode>> wif = Front.listIterator();
	    while (wif.hasNext())
		{
		    Str.printn(" " + N.IsNodeMarked(wif.next().Node));
		}
 	   }
	   Str.print(" : " );	
	*/

    }    
    
    private int ProcessNode(NodeInfo<NetNode> node,LinkedList <NodeInfo<NetNode>> NN)
    /*
      Given a node, find appropriate set of next nodes.
      Add them to NN maintaining NN distance-sorting.
     */
    {
	int a=0;
	NetworkNodeList<NetNode> C = N.ConnectedNodes(node.Node);
	int jlim = C.Nodes.size();
	for (int j=0;j<jlim;j++)
	    {
		NetNode nn = C.Nodes.get(j);
		//N.PrintNode(nn);
		double edgelen = C.Lengths.get(j);
		int m = N.IsNodeMarked(nn);
		NodeInfo<NetNode> ni;
		if (m==-1)
		    { // just adding Nodeinfo. m has nothing to do with Visited.
			ni = new NodeInfo<NetNode>(nn,node,0,KeepAll);
			m = NoodsAll.size();NoodsAll.add(ni);
			N.MarkNode(ni.Node,m);  // New NodeInfo is unvisited.
			MarkedNodes++;
			if (N.IsNodeGood(ni.Node)) {ni.GoodNode=true;MarkedGoodNodes++;}
		    }
		else
		    {
			ni = NoodsAll.get(m);
		    }
		if (0!=ni.UpdateParents(node.Dist+edgelen , node))
		    {
			AddNode(NN,ni);
			a++;
		    }
	    }
	return(a);
    }

    private void AddNode(LinkedList<NodeInfo<NetNode>> NN,NodeInfo<NetNode> ni)
    /*
      Adds ni to NN in sorted position, and removes any other instances of ni.
     */
    {
	ListIterator<NodeInfo<NetNode>> wif = NN.listIterator();
	boolean inserted=false;
	while (wif.hasNext())
	    {
		NodeInfo ne = wif.next();
		if ( (ne.Dist>ni.Dist) && (!inserted) )
		    {
			ne=wif.previous(); wif.add(ni); ne=wif.next();
			inserted=true;
		    }
		else
		    {
			if (ne==ni)
			    {
				wif.remove();
			    }
		    }
	    }
	if (!inserted)
	    {
		NN.add(ni);
		inserted=true;
	    }
    }



    /*
      ============================================================================================================
      Djikstra algorithm just used for propagation; find N closest nodes.
      ============================================================================================================
     */

    public ArrayList<NetNode> FindClosestNodes(NetNode _StartingNode,int Num)
    /*
      Returns a list of nodes closest to _StartingNode, which satisty IsNodeGood().

      The caller is encouraged to use the list returned, 
      and not build his own based on marking done;
      the order of marking and meaning is not guaranteed.
      (i.e. it is internal and may change!)
     */	
    {
	NodeLimit=Num;
	Node1=_StartingNode;Node2=null;
	KeepAll=true;         // Only best choices.
	Djikstra(Node1,null);
	ArrayList<NetNode> Soln = new ArrayList<NetNode>();
	// Just add all we touched, in order, provided they were good. Easy.
	ListIterator<NodeInfo<NetNode>> wif = NoodsAll.listIterator();
	while (wif.hasNext())
	    {
		NodeInfo<NetNode> ni = wif.next();
		if (ni.GoodNode)
		    {
			Soln.add(ni.Node);
		    }
		N.UnmarkNode(ni.Node);  // just like unmarkusednodes().
	    }
	return(Soln);	
    }


    
    // Basis set for Japanese names:     村 木 橋 石 高 川 中 林 徳 大 山

    /*
      ============================================================================================================
      Extended-Djikstra algorithm for N shortest paths on graph.
      ============================================================================================================
     */

    /*
      Within Djikstra, every time we arrive at a marked node we replace if the distance is less.
      Here we do the same, but do not discard the replaced parent choices, and sort them, best first.
      
      Now, tracing back from Node2 μ to Node1 ν, a given path of p+1 nodes (inclusive of endpoints) is 
      λ = { j_p , j_p-1 , . . . , j_1 }
      where j_k is the choice of parent at the (k+1)th node in the sequence. 
      The k=p node is ν, and the k=0 node is μ.
      The bext (Djikstra) path is λ_0 = { 0,0,0,0,0 } where at every node k the best parent j_k=0 is chosen.

      To degrade a path λ minimally, a single choice k is changed from j_k to j_k+1, and all the remaining j_k 
      are reset to zero.

      This is a minimal degradation, provided that λ is the result of similar degradations beginning at λ_0.

      Note that THIS problem is a new problem on a graph. The nodes are successive λ. 
      The edges are the degradations. The initial node is λ_0. There is no final node.
      We want to know successively the nodes with smallest distances to λ_0.
      We can use the Djikstra algorithm to propagate through N nodes to get our desired solution.
      This seems elegant. :-)

     */

    public double[] Path_Lengths;
    public ArrayList<ArrayList<NetNode>> FindShortestPaths(NetNode _Node1,NetNode _Node2,int Num)
    {
	return(FindShortestPaths(_Node1,_Node2,Num,0.0));  // 0 means ∞ :-D
    }

    public ArrayList<ArrayList<NetNode>> FindShortestPaths(NetNode _Node1,NetNode _Node2,int Num,double Fac)
    /*
      Find paths from Node1 to Node2, ranked in terms of shortness.

      Limit the number returned in two ways:
      
      1. Return the top 'Num' such paths (we might get more, since paths are added based on connectivity)

      2. (if Fac!=0) Return only the shortest Paths which are within a factor of Fac of the length of the best path.

      Outer index is path # 0...Num-1. Inner index is node index for each path.

      Algorithm is described in Notes (2018-04-22)
    */	
    {
	/*
	  First stage is to perform Djikstra.
	*/
	Node1 = _Node1;Node2 = _Node2;
	KeepAll=true;             // Keep all parent choices at every node.
	Djikstra(Node1,null);     // Propagate to all accessible nodes.
	SortParentsOfAllNodes();  // Put parents in order.

	PathDegradationNet<NetNode> PDN = new PathDegradationNet<NetNode>();
	NetworkCalculator<DegradablePath<NetNode>> PathNetCalc = new NetworkCalculator<DegradablePath<NetNode>>(PDN,0);
	
	// Identify the λ_0 path:
	DegradablePath<NetNode> λ_0 = new DegradablePath<NetNode>(N,Node1,Node2,NoodsAll);
	λ_0.SetBestPath();
	// Find Num closest paths:
	ArrayList<DegradablePath<NetNode>> Closest_Λ = PathNetCalc.FindClosestNodes(λ_0,Num);

	if (0==1)
	    {
		int p=0;
		ListIterator<DegradablePath<NetNode>> wil = Closest_Λ.listIterator();
		while (wil.hasNext())		    
		    {
			DegradablePath P = wil.next();
			Str.printE("ZPDCH: #050#Closest_Λ: #555#" + p + " : #500#" + P.toString());
			p++;
		    }
	    }

	// Now, convert these to solutions.
	ArrayList<ArrayList<NetNode>> Solns = new ArrayList<ArrayList<NetNode>>();
	ListIterator<DegradablePath<NetNode>> wit = Closest_Λ.listIterator();
	int pnum=0;
	Path_Lengths = new double[Closest_Λ.size()];
	int deq=0;
	while(wit.hasNext())
	    {
		DegradablePath<NetNode> λ_k = wit.next();
		//Str.print("ZPDCH: "+deq+"---------------------------------------------");
		ArrayList<NetNode> OneSoln =  λ_k.GetNodes();
		if (OneSoln==null)
		    {
			//Str.print("ZPDCH: "+deq+" NULL: loopy path");
		    }
		else
		    {
			if (0==1)
			    {
				int mark1 = N.IsNodeMarked(Node1);
				int mark2 = N.IsNodeMarked(Node2);
				NodeInfo ni1 = NoodsAll.get(mark1);
				NodeInfo ni2 = NoodsAll.get(mark2);			
				Str.print("ZPDCH: **> LABELS:" + mark1 + "       " + mark2);
				Str.print("ZPDCH: **> Nodes:" + ni1.Node + "       " + ni2.Node);
				Str.print("ZPDCH: **> Nodes:" + Node1 + "       " + Node2);			
				if (1==0) {return(null);}

				if (0==1)
				    {
					ListIterator<NetNode> wio = OneSoln.listIterator();
					int p=0;
					while (wio.hasNext())
					    {
						Str.printEn("ZPDCH: #555#node=" + p + " ");N.PrintNode(wio.next());p++;
					    }
				    }
			    }
			Solns.add( OneSoln );
			Path_Lengths[pnum] = λ_k.Length();
			pnum++;
		    }
		deq++;
	    }	
	/* 
	   Debug 
	   We no longer need our marks. Let us use them to display paths.
	*/
	if ((verbosity & 1)!=0)
	    {
		pnum=0;
		ListIterator<ArrayList<NetNode>> wif = Solns.listIterator();
		while (wif.hasNext())
		    {
			ArrayList<NetNode> OneSoln =  wif.next();
			ListIterator<NetNode> wig = OneSoln.listIterator();
			while (wig.hasNext())
			    {
				N.MarkNode(wig.next(),pnum);
			    }
			N.Progress();
			pnum++;
		    }
	    }
	UnmarkAllUsedNodes();
	return(Solns);
    }
    
    private void SortParentsOfAllNodes()
    {
	ListIterator<NodeInfo<NetNode>> wif = NoodsAll.listIterator();
	while (wif.hasNext())
	    {
		NodeInfo ni = wif.next();
		ni.SortParents();
	    }
    }










    







    
    

    



}
