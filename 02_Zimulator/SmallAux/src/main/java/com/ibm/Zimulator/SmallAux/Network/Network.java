package com.ibm.Zimulator.SmallAux.Network;

public interface Network<NetNode>
/*
  This inteface allows us to perform computations on an abstract network.
  The whole network need not be known; only local properties.
  (i.e. we suppose no global list of all nodes or edges, etc.)

  There is a facility for 'marking' nodes. The concept of marking is
  boolean, and when marked the concept of 'label' is non-negative integer.

  A node can be 'double-marked'. This is exactly the same as 'marked'
  as far as IsNodeMarked() is concerned. The only reason for
  double-marking is for debugging or Progress() usage.

  Routines which use this interface whould be well-behaved in that
  they assume all nodes are unmarked initially, and after calculation
  revert all nodes to this status.
 */
{
    /*
      Given one node, return all adjacent nodes and edge lengths.
    */
    NetworkNodeList<NetNode> ConnectedNodes(NetNode Node);
    
    /*
      'Mark' a given node in the network as marked, and assign it an
      integer non-negative label.
    */
    void MarkNode(NetNode Node,int Label);    
    /*
      Find out if a given node is marked.
      Returns -1 if unmarked, otherwise the integer Label.
    */
    int IsNodeMarked(NetNode Node); 

    /*
      'Unmark' a given node in the network.
    */
    void UnmarkNode(NetNode Node);


    /*
      Never need these. Wrappers are built with the single-mark structure above.
    */
    //void Mark2Node(NetNode Node,boolean val);    
    //boolean IsNodeMarked2(NetNode Node); 


    /*
      Returns whether a node is 'good' or not, for the purpose of 
      returning a number of nodes of some kind.
      If all nodes are 'good' just always return 'true'. :-D
    */
    boolean IsNodeGood(NetNode Node);


    /* ---------------------------------------------------------
      The following are just progress- and debugging-related.
      Implemening any of them as 'return' will not affect any
      computation results.
    */
    
    /*
      Progress report; just for debugging. Progress() is called
      intermittantly, depending on the calculation being performed.
     */
    void Progress();
    /*
      For debugging. It is useful to have a way to dump to stdout
      a description of a given node. 
     */
    void PrintNode(NetNode Node);
    /*
      A Double-marked node should behave just like a marked node.
      Progress() might decide to display it differently is all.
     */
    void DblMarkNode(NetNode Node);

}
