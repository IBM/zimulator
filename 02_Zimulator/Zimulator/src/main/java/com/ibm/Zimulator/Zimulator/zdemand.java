package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.MDA.*;
import com.ibm.Zimulator.SmallAux.FA.*;

class zdemand extends zobject implements CompiledFileRW
{
    zsystem Ψ;  // Convenient. :-/

    double T_0;
    zsource S;
    zbox[] L;
    int[] o;      // indices in L
    int[] d;      // indices in L
    double[] T;
    int[] N;

    private MDA<zpath> ResolvedZpathCache; 

    int[] order; // indices in  o,d,T,n   If null, they are already ordered :-D
    int DeploymentIdx;  // next deployment to make. in order[] order.length for finished.
    String Reference;  // only for inputparser.
    ReportFlags R;
    
    ArrayList<FA_d> list_v;
    boolean CacheResolvedPaths;
    //    Economics Eco;  2018-08-25 - Moved to {ztype}.

    private int Order(int j)
    {
	if (order!=null) {return(order[j]);}
	return(j);
    }
    public void CalculateOrder()
    {
	// V("Sorting " + T.length + " times.----begin");
	order = MLR.asort(T);
	// V("Sorting times.----end");
    }


    private int Length()
    {
	return(T.length);
    }
    
    public String toString()
    {
	String I;
	I="[zdemand ";
	if (Label!=null) { I += " '" + Label +"'";}
	int nn=0;
	for (int k=0;k<N.length;k++)
	    {
		nn += N[k];
	    }
	I += " " +o.length+ " o-d trips with a total of " + nn + " entities.";
	I += "]";
	return(I);
    }

    public zdemand(zsystem _Ψ)
    { // Just like zschedule:
	Ψ = _Ψ;
	DeploymentIdx = 0;
	CacheResolvedPaths = true;
	ResolvedZpathCache = null;
	// Eco = new Economics();  // contains defaults.
	list_v=null;
	order=null;
	Reference="%s";
    }

    public int ProcessState(zsystem Ψ,double maxΔt,int verbose)
    /*
      ProcessState implemented for zdemand.
     */
    {
	if (verbose>0) {V("Processing zdemand.......... Among "+L.length+" zboxen.");}

	if (Ψ.enableZdemandFlag == 0)
	    {
		V(" [suppressed] ");
		return(2);
	    }  // Zdemand is suppressed. Keep this in [0] though, so it doesn't disappear!

	if (DeploymentIdx == -1) {return(1);} // no zpath.
	if (S==null) {return(1);} // no zsource.
	if (S.φ==null) {return(1);} // no zbox.
	
	/*
	  Find the next deployment time, at or after system time.
	 */

	while (0==0)
	    {
		if (DeploymentIdx >= T.length) {return(0);} // finished.
		
		double Tdep = T[Order(DeploymentIdx)] + T_0; // in order.
	
		if (Tdep < Ψ.t)
		    { // Somehow we missed it.
		      // (This could happen if the zdemand list starts before the simulation start time, for example)
			Tdep =-1;
			int k=0;
			for(k = DeploymentIdx;k<T.length;k++)
			    {
				Tdep = T[Order(k)] + T_0; // in order.
				if (Tdep >= Ψ.t) {break;}
			    }
			DeploymentIdx=k;
			if (verbose>0) {V(". . . Ψ.t=" + Ψ.t + " . . deployment idx: " + DeploymentIdx + " Tdep = " + Tdep);}
			
			if (k==T.length) {return(0);}  // Nothing to do. All in the past.
		    }
		/*
		  Tdep is our next deployment time.
		*/
		if (Tdep > Ψ.t)
		    {
			if (verbose>0) {V("Will deploy at " + Tdep);}
			t = Tdep;
			return(3);  // [T]
		    }

		
		// Tdep == Ψ.t

		
		/*
		  Find a zbox and deploy him on P
		  Tdep
		*/

		//verbose=1;   // DEBUG 2018-12-26
		
		if (verbose>0)
		    {
			V(" ::: DeployMentIdx = #" + DeploymentIdx);
			V(" ::: Will deploy at " + Tdep);
			V(" ::: Ordered idx = #" + Order(DeploymentIdx));
			V(" ::: (o,d) = (" + o[Order(DeploymentIdx)] + "," + d[Order(DeploymentIdx)] + ")");
			zbox Origin = L[o[Order(DeploymentIdx)]];
			zbox Destination = L[d[Order(DeploymentIdx)]];
			//V(" ::: " + Eco.toString());
			V(" ::: zbox_o = " + Origin.Label);
			V(" ::: zbox_d = " + Destination.Label);			
			V(" (o,d) = (" + o[Order(DeploymentIdx)] + "," + d[Order(DeploymentIdx)] + ")");
		    }
		
		
		for (int pax=0;pax<N[Order(DeploymentIdx)];pax++)
		    {		
			zpath ResolvedPath = ResolveAndCacheZPath(o[Order(DeploymentIdx)],d[Order(DeploymentIdx)],Ψ,verbose,Order(DeploymentIdx));

			//ResolvedPath=null;
			
			zbox Origin = L[o[Order(DeploymentIdx)]];
		
			if (ResolvedPath!=null)
			    {
				StateChange=true;
			
				zbox γ = S.φ;
				// Is γ a prototype?
				if (γ.isPrototype())
				    {
					if (verbose>0) {V(γ.Label + " is a prototype; copying." + Tdep);}
					γ = Ψ.AddCopyOfPrototype(γ);  // adds to end.                To do N, do this over and over.
				    }

				/*
				  Velocity. γ will have the velocity from the prototype, 
				  either the ztype of the prototype or else an explicit zbox v.
				  Here, we might re-set it if we have v[] provided.
				*/
				boolean set_vel = false;				
				if (list_v!=null)
				    {
					γ.set_v(list_v.get(Order(DeploymentIdx)).get(0));
					if ( list_v.get(Order(DeploymentIdx)).size()>1)
					    {
						γ.set_natural_velocity_list(list_v.get(Order(DeploymentIdx)));
						set_vel=true;
					    }
				    }
				/*
				  If that was not present, then check our distribution specification:
				*/
				if ((!set_vel) && (S.v_μ>0))
				    {
					γ.vel_from_lognormal(S.v_μ,S.v_σ);
				    }
				
			
				Ψ.System_Z_Lists.add_to_Buffer0(γ);  // Add to [0] circle.

				// Add path to system. Why? So others can find it... but they search by zbox.
				//			Ψ.System_Z_Lists.add(ResolvedPath);
				// We no longer do this.

				// 'Add' this path's zstops to system.
				ResolvedPath.AddToLists();
				
				/*
				  dwf = 1 means that the zpath will not be kept in system lists after use.
				  This does NOT mean that it is not kept in the zdemand cache.
				 */
				ResolvedPath.dwf = 1;  
				if (S.o==2) //teleport.
				    {
					//Dangerous; there might not be room.
					//γ.MoveToNewContainer(Ψ,Origin);  // just teleport there.
					if (0==γ.Attempt_Shift(Origin,0,Ψ.t,true))
					    {
						ResolvedPath.AddFollower(γ);  // i_P will be -1.
						γ.i_P = 0; // reflects current container.
						γ.Enter_M_state(Ψ.t);
					    }
					else
					    {
						// We cannot enter the system.
						γ.setRemovalState();
					    }
				    }
				else
				    { //container.
					γ.InvalidateCaches(0); // Invalidate from old Λ῀ before moving.
					γ.i_P = -1; // reflects current container; not yet started path.
					//γ.state='S';			// S state, so we start path.  ---- !! maybe do not change state.
				    }
				// resolution:
				γ.t = Ψ.t;
				// Add resolved path to zsystem:
				//Ψ.System_Z_Lists.addToBuffer0(ResolvedPath); // No need. see Diary 2018-03-02

				Ψ.System_Z_Lists.add_to_BufferS(ResolvedPath); // We do this so it can be saved in the file; see 'CompiledFileIO'
				
				// When a zpath has no followers, remove it from the system?
				if (verbose>0) {V("  . . . " + γ.Label +" deployed.");}

				if (DoReportFlags.Self(R) || DoReportFlags.Path(R))
				    {
					String lin="zdemand " + Label + " t=" + γ.t + " zbox=" + γ.Label;
					if (DoReportFlags.Path(R))
					    {
						lin = lin + " " + γ.P.ReportStops();
					    }
					Ψ.Report(DoReportFlags.Channel(R),lin);
				    }
				// γ itself should also report.
				γ.Rentered();				
			    }
			else
			    {
				// !!!  We should provide an Intent path, and resolve it later when we can. !!!
			    }
		    }
		DeploymentIdx++; // we're finished with this one, whether or not we actually deployed.
	    }
    }

    private zpath ResolveAndCacheZPath(int oidx,int didx,zsystem Ψ,int verbose,int idx)
    /*
      oidx and didx are indices in L.
     */
    {

	double systime = Ψ.t;
	zbox Origin = L[oidx];
	zbox Destination = L[didx];	
	ztype mover = S.φ.e;

	zpath ZP = null;
	if (CacheResolvedPaths && (Ψ.cacheResolvedZpathsFlag==1))
	    {
		if (ResolvedZpathCache==null)
		    {
			ResolvedZpathCache = new MDA<zpath>(2);  // Two-dim'
		    }
		zpath PossPath = ResolvedZpathCache.get(oidx,didx);
		//Str.print("Found Cached path:" + PossPath);  //DEBUG 2018-12-26.
		if (PossPath!=null)
		    {
			if ( (PossPath.ViabilityTime<0)||(PossPath.ViabilityTime>systime))
			    {
				if ( (PossPath.ExpiryTime<0)||(PossPath.ExpiryTime>systime))
				    {
					ZP = PossPath;
				    }
			    }
			ResolvedZpathCache.set(oidx,didx,ZP);
		    }		
	    }

	if (ZP==null)
	    {
		PathResolution zpr;
		//Str.printE("#543# O = " + Origin + "#345# D = " + Destination + "#354# e = " + mover + "#333# Eco = " + Eco);
		zpr = new PathResolution(Ψ,Origin,Destination,mover,mover.Eco);
		ZP = zpr.ZP;
	    }
	
	if (ZP==null) //null /still/. This means O-D disconnected. The 'null' propagates back all the way from Network class.
	    {
		return(null);
	    }
	
	if (ResolvedZpathCache!=null)
	    {
		//Str.print("Cached paths:" + ResolvedZpathCache.size()); //DEBUG 2018-12-26.
		//Str.print("  Cache path: " + ZP);  //DEBUG 2018-12-26.
		ResolvedZpathCache.set(oidx,didx,ZP);
	    }
	/*
	  Now we have ZP, from our cache if applicable, and in our cache if applicable.
	 */
	if (verbose>0) {V("  = (" + Origin.Label + "," + Destination.Label +")");}

	// Used to be a sample from distn. No longer the case, as this is inappropriate at this level of detail.
	// New way is simply to have shortest path feature as part of the "Z-core".
	zpath ResolvedPath = ZP;  
	if (verbose>0) {V("  . . . Resolved Zpath:" + ResolvedPath);}

	//R bits: 1,2,4 = S C P
	return(ResolvedPath);
    }
    
    private void V(String S)
    {
	Ψ.Verb.printnrgb("zdemand: ",3,0,0);
	Ψ.Verb.printrgb( S,4,0,1);
    }



    // ******************************************************************
    // Implementation of 'CompiledFileRW'
    // ******************************************************************


    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {     // T_0,S,L,o,d,T,N,PD,ZpDM,order,DepIdx,R,CRP 
	D.wb('d');  //zdemand
	D.wi(GetFileRefNumber());
	D.wd(T_0);
	D.wi(S.GetFileRefNumber());
	D.wi(L.length);
	for (int j=0;j<L.length;j++) {D.wi(L[j].GetFileRefNumber());}
	if (order==null)
	    {
		D.wi(o.length);		
		for (int j=0;j<o.length;j++)
		    {
			D.wi(o[j]);
			D.wi(d[j]);
			D.wd(T[j]);
			D.wi(N[j]);
		    }
	    }
	else
	    {
		D.wi(T.length);		
		for (int j=0;j<T.length;j++)
		    {
			D.wi(o[order[j]]);
			D.wi(d[order[j]]);
			D.wd(T[order[j]]);
			D.wi(N[order[j]]);
		    }
	    }
	D.wi(DeploymentIdx);
	DoReportFlags.WriteToObjFile(D,R);
	if (CacheResolvedPaths) {D.wb(1);} else {D.wb(0);}
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	T_0 = D.rd();
	OR.Rafs=new FA_i();
	OR.Rafs.add(D.ri()); // S
	int L_l = D.ri();
	OR.Rifs=new FA_i();
	for (int j=0;j<L_l;j++) {OR.Rifs.add(D.ri());} // L[j]
	order=null;
	int o_l = D.ri();
	o = new int[o_l];  d = new int[o_l]; T = new double[o_l];  N = new int[o_l];
	for (int j=0;j<o_l;j++)
	    {
		o[j] = D.ri();
		d[j] = D.ri();
		T[j] = D.rd();
		N[j] = D.ri();
	    }
	DeploymentIdx = D.ri();
	R = DoReportFlags.ReadFromObjFile(ORL,D);
	if (D.rb()==0) {CacheResolvedPaths=false;} else {CacheResolvedPaths=true;}
	Label = D.rs();
	ORL.add(OR);
    }    
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	S = (zsource) RR.getref(OR.Rafs.get(0));
	L = new zbox[OR.Rifs.length];
	for (int j=0;j<OR.Rifs.length;j++)
	    {
		L[j] = (zbox) RR.getref(OR.Rifs.get(j));
	    }
	return(true);
    }



    
}
