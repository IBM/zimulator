package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

class zschedule extends zobject implements CompiledFileRW
{
    zsystem Ψ;  // Convenient. :-/
    
    double T_0;
    double[] T;

    zsource S;

    int A,n;
    zpath P;
    
    int DeploymentIdx;  // next deployment to make.
    int LastDeployed;  // last one we deployed, or -1.

    ReportFlags R;  // reporting.
    
    // Economics Eco;  2018-08-25: Moved to ztype instead of zdemand and zschedule.

    boolean SourceExpires;
    
    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    {     // T_0,T,S,A,n,P,Didx,LD,R,R_n,ZPDM
	D.wb('h');  //zschedule
	D.wi(GetFileRefNumber());
	D.wd(T_0);
	D.wi(T.length);for (int j=0;j<T.length;j++) {D.wd(T[j]);}
	D.wi(S.GetFileRefNumber());
	D.wi(A);D.wi(n);
	D.wi(P.GetFileRefNumber());
	D.wi(DeploymentIdx);
	D.wi(LastDeployed);
	DoReportFlags.WriteToObjFile(D,R);
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	OR.Rafs=new FA_i();
	T_0 = D.rd();
	T = new double[D.ri()];
	for (int j=0;j<T.length;j++) {T[j] = D.rd();}
	OR.Rafs.add(D.ri()); // S
	A=D.ri();n=D.ri();
	OR.Rafs.add(D.ri()); // P
	DeploymentIdx=D.ri();
	LastDeployed=D.ri();
	R = DoReportFlags.ReadFromObjFile(ORL,D);
	Label=D.rs();
	ORL.add(OR);
    }
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	S = (zsource) RR.getref(OR.Rafs.get(0));	
	P = (zpath) RR.getref(OR.Rafs.get(1));	
	return(true);
    }
    
    public String toString()
    {
	String I;
	I="[zschedule";
	if (Label!=null) { I += " '" + Label +"'";}
	I += " T_0=" + T_0 +"="  +Str.SecInDayToTime(T_0);
	if (T==null) {I+=" T:{} ";}
	else
	    {
		I += " Times:" + T.length + " T:{";
		for (int j=0;j<T.length;j++)
		    {
			I += "" + T[j]+ "s ";
		    }
		I+="}";
	    }
	I += "]";
	return(I);
    }

    public zschedule(zsystem _Ψ)
    {
	Ψ = _Ψ;	
	DeploymentIdx=0;
	LastDeployed=-1;
	SourceExpires=false;	
    }
    
    public int ProcessState(zsystem Ψ,double maxΔt,int verbose)
    /*
      ProcessState implemented for zschedule.
    */
    {
	//V("Processing zschedule......................");
	/*
	  Remember that we are not a zbox. We just contain information needed to deploy one.
	*/

	if (P==null) {return(1);} // no zpath.
	if (S==null) {return(1);} // no zsource.
	if (S.φ==null) {return(1);} // no zbox.
	if (DeploymentIdx == T.length) 
	    { // Finished.
	      // This zschedule should be deleted from the system.
		return(0);
	    } 
	double Tdep = T[DeploymentIdx] + T_0; // in order.
	if (Tdep==Ψ.t)
	    {
		AttemptDeployment(Ψ,verbose);
		if (SourceExpires)
		    {
			//Str.print("Deleting : " + Label);
			// We have used up this zsource.
			Ψ.System_Z_Lists.add_to_BufferD(S);
			S=null;
			// We have exhausted this zschedule also!
			return(0); // delete.
		    }
		DeploymentIdx++;
		if (DeploymentIdx == T.length) {return(0);} // finished.
	    }
	ToNextViableTime(Ψ,verbose);
	if (DeploymentIdx == T.length) {return(0);} // finished.	
	Tdep = T[DeploymentIdx] + T_0; // in order.
	if (verbose>0) {V("Identified deployment time:  " + Tdep);}
	t = Tdep;
	
	return(3); // Re-insertion into [T]
    }
	


    private void ToNextViableTime(zsystem Ψ,int verbose)
    /*
      Find the next deployment time, at or after system time.
    */
    {
	int j = DeploymentIdx;
	double Tdep = T[j] + T_0; // in order.
	if (Tdep < Ψ.t)
	    { // somehow we missed it.
		if (verbose>0) { V(" Missed deployment." + j + " t=" + (T[j] + T_0) + "=" + Str.SecInDayToTime(T[j] + T_0) + " " + Label);}
	        Tdep =-1;
		for(j=DeploymentIdx;j<T.length;j++)
		    {
			Tdep = T[j] + T_0; // in order.
			//V(". . . Ψ.t=" + Ψ.t + " . . testing Tdep = " + Tdep);
			if (Tdep >= Ψ.t) {break;}
		    }
		DeploymentIdx=j;
	    }
    }

    private void AttemptDeployment(zsystem Ψ,int verbose)
    /*
      true = still more to do.
      false = we're finished. Zschedule can be removed from lists [].
    */
    {
	int j = DeploymentIdx;
	double Tdep = T[j] + T_0; // in order.

	/*
	  Tdep is our next deployment time.
	*/
	// Tdep == Ψ.t
	    
	/*
	  Find a zbox and deploy him on P
	  Tdep
	*/
	    	
	zbox γ = S.φ; // m=1 case.
	if (S.m==2)
	    {
		if (S.φ.Z == null) {return;} // [0]
		// Who is ready to leave. Pick first one, since new are added at end.
		if (S.φ.Z.size()<1) {return;}  // [0] no first one.
		γ = S.φ.Z.getFirst();  // First in.
	    }
	// Is γ a prototype?
	if (γ.isPrototype())
	    {
		if (verbose>0) {V(γ.Label + " is a prototype. Send on zpath at t=" + Tdep);}
		γ = Ψ.AddCopyOfPrototype(γ);  // adds to end.		
		γ.Label = γ.Label; // + "--Dep." + DeploymentIdx;
	    }
	else
	    {
		if (verbose>0) {V(γ.Label + " is a specific zbox. Send on zpath at t=" + Tdep);}
		if (S.m==1)
		    {
			if (verbose>0) {V(" zsource " + Label + " expires.");}
			SourceExpires=true;
		    }
	    }

	// Maybe it needs to choose a random velocity:
	if (S.v_μ>0)
	    {
		γ.vel_from_lognormal(S.v_μ,S.v_σ);
	    }
	
	
	Ψ.System_Z_Lists.add_to_Buffer0(γ);
	
	zpath ResolvedPath = P;                   // may or may not have dwf. Probably 0.
	if (verbose>0) {V("  . . . Zpath:" + P);}

	/*
	  It used to be the case that the zpath was resolved here, and the zbox sent on it.
	  Now, the zbox is set up and simply given an intent path;
	  the zbox is responsible for zpath resolution.
	 */
	if (0==1)
	    {
		if (P.IsIntent())
		    {
			if (verbose>0) {V("  . . . Resolving Zpath:" + P + " . . . ");}
			ResolvedPath = P.Resolve();
			ResolvedPath.dwf=1;
			if (verbose>0) {V("  . . . Resolved Zpath:" + ResolvedPath);}
		    }
	    }

	if (ResolvedPath!=null)
	    { // Is there a zpath?
		StateChange=true;
		// Assign zpath
		if (verbose>0) {V(" . . Deploying '" + γ.Label + "' on path at t=" + Tdep);}
		if (DoReportFlags.Self(R))
		    {
			Ψ.Report(DoReportFlags.Channel(R),"zschedule deploy zbox=" + γ.Label + " t=" + Tdep);
			//Str.print(Ψ.ReportPrefix + "zschedule deploy zbox=" + γ.Label + " t=" + Tdep);
		    }
		ResolvedPath.AddToLists();
		
		if (S.o==1)  // container
		    {
			ResolvedPath.AddFollower(γ);
			γ.i_P = -1;
			γ.InvalidateCaches(0); // invalidate from old Λ῀ before moving.
			γ.Enter_S_state();  // Used to be 'M' here...
		    }
		else
		    {  // teleport
			ResolvedPath.AddFollower(γ);
			γ.i_P = 0;  // we go directly to first stop.
			// This was dangerous; there might not be room:
			// γ.MoveToNewContainer(Ψ,ResolvedPath.Λ_λ[0].φ);  // just teleport there.

			if (0==γ.Attempt_Shift(ResolvedPath.Λ_λ[0].φ,0,Ψ.t,true))
			    {
				γ.Enter_M_state(Ψ.t);
			    }
			else
			    {
				// We cannot enter the system.
				γ.setRemovalState();
			    }
		    }
		// resolution:
		γ.t = Ψ.t;
		//t=Ψ.t;
		// Ψ.System_Z_Lists.addToBuffer0(ResolvedPath); // We only add because of marking. . . . 

		// γ should also report:
		γ.Rentered();
	    }
	else
	    {
		// !!!  We should provide an Intent path, and resolve it later when we can. !!!		
	    }	
	return;
    }

    
    
    private void V(String S)
    {
	Ψ.Verb.printnrgb("zschedule: " + Label,3,5,3);
	Ψ.Verb.printrgb(" " + S ,4,5,2);
    }
    
    
    
}
