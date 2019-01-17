package com.ibm.Zimulator.SmallAux.Network;

import com.ibm.Zimulator.SmallAux.*;
import java.util.*;

/*
  Depends on having parent lists in NodeInfo array.
*/
class DegradablePath<NetNode>
{
    /*
      Path through system; parent choce at each NodeInfo.
      For a path of length p+1 nodes, including endpoints, this will contain p elements,
      representing that a choice can be made at every node p-0,p-1,p-2...1 but not at node 0.
      The choices are in order from the end of the path.
    */
    
    // To treat this as a node in PathDegradationNet class.
    //	int PDN_Mark;
    //	int PDN_Label;
    
    private Network<NetNode> N;
    private NetNode Node1,Node2;
    private ArrayList<NodeInfo<NetNode>> NoodsAll;
	    
    private ArrayList<Integer> P; 
    private double L;

    public String toString()
    {
	String S = P.size() + " {";
	for (int k=0;k<P.size();k++)
	    {
		// S=S + " " + k + ":" + P.get(k);
		S=S + " " + P.get(k);
	    }
	S=S+" }";
	return(S);
    }
	
    public DegradablePath(Network<NetNode> _N,NetNode _Node1,NetNode _Node2,ArrayList<NodeInfo<NetNode>> _NoodsAll)
    {
	N=_N;
	Node1=_Node1;
	Node2=_Node2;
	NoodsAll=_NoodsAll;
	    
	P = null;
	L = -1.0;
    }

    /*
      Return a set of paths which result from minimal degradation (one 'next choice') of this one.
      The degradation should be such that there are only 0 choices after.
    */
    public ArrayList<DegradablePath<NetNode>> Neighbours()
    {
	int FirstPossibility = P.size()-1;
	while(P.get(FirstPossibility)==0)
	    {if (FirstPossibility==0) {break;} FirstPossibility--; }
	    
	ArrayList<DegradablePath<NetNode>> Degs = new ArrayList<DegradablePath<NetNode>>();
	NodeInfo<NetNode> ni = NoodsAll.get(N.IsNodeMarked(Node2)); // will be marked.
	int k = 0;
	while (ni.Node!=Node1)
	    {
		if (k>=FirstPossibility) //into the 0 region.
		    {
			int j_k = P.get(k);  // original j_k next in list.
			if (j_k < ( ni.Parents.size()-1 ))
			    { // can try next one.
				Degs.add(DegradablePath(k));
			    }
		    }
		ni = ni.Parents.get(P.get(k));  // we look at single-choice deviations from provided DP.
		k++;
	    }
	//Str.print("*** Neighbours: " + Degs.size());

	if (0==1)
	    {
		int p=0;
		ListIterator<DegradablePath<NetNode>> wil = Degs.listIterator();
		while (wil.hasNext())
		    {
			Str.printE("ZPDCH: #555#" + p + " : #500#" + wil.next().toString());
			p++;
		    }
	    }

	return(Degs);
    }

    /*
      Returning new Degradable path degraded by making the NEXT choice at index kk,
      and of course choice 0 thereafter.
    */
    public DegradablePath<NetNode> DegradablePath(int kk)
    {
	DegradablePath<NetNode> NewPath = new DegradablePath<NetNode>(N,Node1,Node2,NoodsAll);
	NewPath.P = new ArrayList<Integer>();
	NewPath.L=0;
	NodeInfo<NetNode> ni = NoodsAll.get(N.IsNodeMarked(Node2)); // will be marked.
	int k = 0;
	while (ni.Node!=Node1)
	    {
		int j_k=0;
		if (k < kk)
		    {
			j_k = P.get(k);
		    }
		if (k == kk)
		    {
			j_k = P.get(k) + 1;
		    }
		NewPath.P.add(j_k);
		NodeInfo<NetNode> nip = ni.Parents.get(j_k);
		double EdgeLen = ni.Dists.get(j_k) - nip.Dist;
		NewPath.L += EdgeLen;
		ni = nip;k++;
	    }
	//Str.printE("ZPDCH: *** OldPath::: #353#" + toString() + "#0# Degraded path: #335#" + NewPath.toString());
	return(NewPath);
    }
	
    public double Length()
    {
	if (L<0) {calculateLength();}
	return(L);
    }
	
    public void calculateLength()
    /*
      Evaluate length. Nontrivial for any but (0,0,0,0...) path.
    */
    {
	L=0;
	NodeInfo<NetNode> ni = NoodsAll.get(N.IsNodeMarked(Node2)); // will be marked.
	int k = 0;
	while (ni.Node!=Node1)
	    {
		int j_k = P.get(k);
		NodeInfo<NetNode> nip = ni.Parents.get(j_k);
		double EdgeLen = ni.Dists.get(j_k) - nip.Dist;
		L += EdgeLen;
		ni = nip;k++;
	    }
    }

    public boolean IsGood()
    {
	return(!(null==GetNodes()));
    }
	
    public ArrayList<NetNode> GetNodes()
    {
	// Get the nodes in this path, in reverse order.
	ArrayList<NodeInfo<NetNode>> nis = new ArrayList<NodeInfo<NetNode>>();
	int n2label = N.IsNodeMarked(Node2);
	NodeInfo<NetNode> ni = NoodsAll.get(n2label); // will be marked.
	int k=0;
	//	    Str.print("*******> " + P.size() + " " + n2label + ":" + ni.Node + " " + Node2 + "   Node1: " + Node1 );
	while (ni.Node!=Node1)
	    {
		if (nis.contains(ni)) {return(null);}  //SLOW. O(n^2)
		nis.add(ni);
		int j_k = P.get(k); // j_k is the k'th choice.
		if (0==1)
		    {
			int choix = ni.Parents.size();
			Str.printEn("ZPDCH: Choice of " + choix + ": ");
			for (int q=0;q < choix;q++)
			    {
				Str.printEn("#050#" + q +":"); N.PrintNode(ni.Parents.get(q).Node);
			    }
			Str.printn(" -> "); N.PrintNode(ni.Parents.get(j_k).Node); Str.print("");
		    }
		ni = ni.Parents.get(j_k);
		k++;
	    }
	if (nis.contains(ni)) {return(null);}   //SLOW. O(n^2)
	nis.add(ni); //Node1.


	// Now, buld soln in other order:
	ArrayList<NetNode> soln = new ArrayList<NetNode>();
	int jlim=nis.size();
	for (int j=0;j<jlim;j++)
	    {
		soln.add(nis.get(jlim-j-1).Node);
	    }
	return(soln);
    }

    public void SetBestPath()
    {
	P = new ArrayList<Integer>();
	NodeInfo<NetNode> ni = NoodsAll.get(N.IsNodeMarked(Node2)); // will be marked.
	L = ni.Dist;
	while (ni.Node!=Node1)
	    {
		ni = ni.Parents.get(0);  // same as Parant.
		P.add(0);
	    }
    }

}

