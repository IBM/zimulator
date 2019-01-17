package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

/*
  A Route just "unfolds" a zpath, so that zpaths specified within are converted to likely lists of zboxen.
  It does not follow zlinks, since they might not exist (i.e. might be implied)
  Instead, it recursively follows zpaths specified through zstops to get a list of
  zboxen visited, (and  potentially connected by the right type of links.)
  
  A list of zboxen could be generated, which includes:
  (i) zboxen visited and entered during zpath
  (ii) zboxen which contain stops on a path for a zbox in which we will ride.
  (They will be implicitly zlinked to the vehicle zbox via the ztype's χ)
  This could be generated recursively, provided that zstops include not μ,ν but i,j (indices in zpaths). Use i,j.
  
  This is what we have called a Route, and there is exactly one which lives inside 'zpath'.
  'Route' is not fully developed, and should be cleaned up somewhat and made more precise.

*/


class Route  implements CompiledFileRW
{
    ArrayList<zbox> Zboxen;
    FA_i AfA;  // 0,1,2...  0 for an explicit zbox. 1 for zbox on path. 2 for zbox on path on path...
    FA_i Idx;  // For AfA==0, this is an index into the zpath used to create the route.

    zpath P;  // The path which generated this Route.
    ztype ZT;  // Will /usually/ match that of zpath.
    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {
	D.wb('R');  //Route
	D.wi(Zboxen.size());
	for (int j=0;j<Zboxen.size();j++)
	    {
		D.wi(Zboxen.get(j).GetFileRefNumber());
		D.wb(AfA.get(j));
		D.wi(Idx.get(j));
	    }
	D.wi(P.GetFileRefNumber());
	D.wi(ZT.GetFileRefNumber());
    }

    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
 	ObjRef OR = new ObjRef(this);  // A 'Route' cannot be referenced (is not a zobject).
	OR.Rafs = new FA_i();
	int ZB_l = D.ri();
	for (int j=0;j<ZB_l;j++)
	    {
		OR.Rafs.add(D.ri()); // zbox.
		AfA.add(D.rb());
		Idx.add(D.ri());
	    }
	OR.Rifs = new FA_i();	
	OR.Rifs.add(D.ri()); // P
	OR.Rifs.add(D.ri()); // ZT
	ORL.add(OR);
    }

    public void SetFileRefNumber(int n) {}
    public int GetFileRefNumber() {return(0);}    
    
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	for (int j=0;j<OR.Rafs.length;j++)
	    {
		Zboxen.add((zbox) RR.getref(OR.Rafs.get(j)));
	    }
	P = (zpath) RR.getref(OR.Rifs.get(0));
	ZT = (ztype) RR.getref(OR.Rifs.get(1));
	return(true);
    }
    
    public String toString()
    {
	String I;
	I="[Route {";
	for (int j=0;j<Zboxen.size();j++)
	    {
		I += " " + Zboxen.get(j).Label + "[Lev."+AfA.get(j)+"]<Idx"+Idx.get(j)+"> ";
	    }
	I += "} for ztype (" + ZT.A + "," + ZT.n + ")";
	return(I);
    }

    public Route()
    {	// this will only be used when loading from file.
	Zboxen = new ArrayList<zbox>();
	AfA = new FA_i();
	Idx = new FA_i();
    }
    
    public Route(zpath _P,ztype mover)
    /*
      Construct a Route for the provided zpath.
      Since the zpath, through implicit zstops may reference other zpaths, 
      this process is recursive.

      Fill in the top-level indices. Some will be -1.
      
      ( If the zpath is either active (having follower zboxen) or listed in a schedule,
      then it will have a ztype associated, and that can be used for mover )
     */
    {
	P = _P;
	ZT = mover;
	Zboxen = new ArrayList<zbox>();
	AfA = new FA_i();
	Idx = new FA_i();
	int i_start = 0;
	int j_end = P.Λ_λ.length-1;
	if (P.m == 2 ) { j_end++; }

	zpathtoRoute(0,P,ZT,i_start,j_end); // It's just that easy to recurse.
    }
    
    private void zpathtoRoute(int Lev,zpath P,ztype mover,int i,int j)
    /*
      Recursively traverses the zpath P, and adds all the boxen to the list Zboxen;
      j can be 'past end' to wrap around in the case of a closed path.
     */
    {
	for (int ki=i;ki<=j;ki++)
	    {
		int k = ki % P.Λ_λ.length;
		zstop zs = P.Λ_λ[k];		
		if (zs.φ!=null)
		    { // Explicit stop.
			Zboxen.add(zs.φ);			
			AfA.add(Lev);			
			Idx.add(k);
		    }
		else
		    { // We should have K.
			if (zs.K!=null)
			    { // Agents-for-agents here:
				// Would our connection using this path be viable for our mover type?
				// (Which is the zbox travelling on P)
				// Otherwise, we cannot use it.
				if (zs.K.e.χ.CanAllowZboxType(P.e,0))  // Boarding & alighting.
				    {
					// Recurse through K,i,j.
					zpathtoRoute(Lev+1,zs.K,P.e,zs.i,zs.j);
				    }
			    }
		    }
	    }
    }
	


    public ArrayList<zbox> NextVisited(zbox zb)
    {
	ArrayList<zbox> Boxen = new ArrayList<zbox>();
	int jlim=Zboxen.size()-1;
	for (int j=0;j<jlim;j++)
	    {
		if (Zboxen.get(j)==zb)
		    {
			Boxen.add(Zboxen.get(j+1));
		    }
	    }
	return(Boxen);
    }


    
    public ArrayList<Integer> NextVisited_byIdx(zbox zb)
    {
	ArrayList<Integer> Boxen = new ArrayList<Integer>();
	int jlim=Zboxen.size()-1;
	for (int j=0;j<jlim;j++)
	    {
		if (Zboxen.get(j)==zb)
		    {
			Boxen.add(j+1);
		    }
	    }
	return(Boxen);
    }

    public ArrayList<Integer> ThisVisited_byIdx(zbox zb)
    {
	ArrayList<Integer> Boxen = new ArrayList<Integer>();
	int jlim=Zboxen.size()-1; // do not include last one.
	for (int j=0;j<jlim;j++)
	    {
		if (Zboxen.get(j)==zb)
		    {
			Boxen.add(j);
		    }
	    }
	return(Boxen);
    }

    public ArrayList<Integer> AllVisitedAfter_byIdx(zbox zb)
    {
	ArrayList<Integer> Boxen = new ArrayList<Integer>();
	int jlim=Zboxen.size();
	int flg=0;
	for (int j=0;j<jlim;j++)
	    {
		if (Zboxen.get(j)==zb) {flg=1;continue;}
		if (flg==1)
		    {
			Boxen.add(j);
		    }
	    }
	return(Boxen);
    }



    
}
