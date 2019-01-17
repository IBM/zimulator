package com.ibm.Zimulator.SmallAux.Network;

import java.util.*;

public class NetworkNodeList<NetNode>
{
    public ArrayList<NetNode> Nodes;
    public ArrayList<Double> Lengths;
    public NetworkNodeList()
    {
	Nodes = new ArrayList<NetNode>();
	Lengths = new ArrayList<Double>();
    }
}
