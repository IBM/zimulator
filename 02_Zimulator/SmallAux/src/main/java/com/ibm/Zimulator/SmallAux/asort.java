package com.ibm.Zimulator.SmallAux;

import java.util.*;

public class asort<Я>
/*
  Construct this to sort objects of type Я which can be ordered using a double.
*/
{
    class sorty
    {
	double val;
	int index;
	public sorty(double v,int idx) {val=v;index=idx;}
    }

    int[] sorted;

    public asort(double[] Values)
    {
	int klim = Values.length;
	ArrayList<sorty> S = new ArrayList<sorty>(klim);
	for (int k=0;k<klim;k++) { S.add(new sorty(Values[k],k));}
	S.sort( (s1,s2) -> ( s1.val <= s2.val ? -1 : 1 ));
	sorted = new int[klim];
	for (int k=0;k<klim;k++) { sorted[k] = S.get(k).index; }
    }
    public asort(int[] Values)
    {
	int klim = Values.length;
	ArrayList<sorty> S = new ArrayList<sorty>(klim);
	for (int k=0;k<klim;k++) { S.add(new sorty(Values[k],k));}
	S.sort( (s1,s2) -> ( s1.val <= s2.val ? -1 : 1 ));
	sorted = new int[klim];
	for (int k=0;k<klim;k++) { sorted[k] = S.get(k).index; }
    }

    public asort(ArrayList<Double> Values)
    {
        int klim = Values.size();
        ArrayList<sorty> S = new ArrayList<sorty>(klim);
        for (int k=0;k<klim;k++) { S.add(new sorty(Values.get(k),k));}
        S.sort( (s1,s2) -> ( s1.val <= s2.val ? -1 : 1 ));
        sorted = new int[klim];
        for (int k=0;k<klim;k++) { sorted[k] = S.get(k).index; }        
    }

    
    public int[] order()
    {
	return(sorted);
    }
    public int size()
    {
	return(sorted.length);
    }
    
}
