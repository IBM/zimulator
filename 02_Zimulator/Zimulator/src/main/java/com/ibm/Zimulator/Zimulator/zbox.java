package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

/*
  A 'zbox' is the main dynamical element of the system. Zboxen are described extensively in the Specification.
  The most important method here is ProcessState() which (as for all z*), overrides 'zobject' ProcessState().
*/

class zbox extends zobject implements CompiledFileRW
{
    zsystem Ψ;  // Convenient. :-/

    String info; // null or else information which is to be included in reporting. 'i' field in documentation.

    // null, or else all zboxen which are waiting for this to make a move. (i.e. this is in the way.)
    //ArrayList<zbox> DependingOnUs;
    HashSet<zbox> DependingOnUs;

    // Server communication flags and server number to contact.
    // int ξ,n_ξ;  Olde way; 2018-09-07.
    ServerFlags ξ;

    // What IS this zbox? ( In InputParser(), for example, (A,n) is specified, and mapped to a ztype).
    ztype e;

    // The following override elements of 'e' IFF non-negative (we use -1 for int ones). We might think of a better way later.
    private int L,W,N,S; // use get and set, below.
    public ReportFlags R;

    private double v;  // natural velocity. Can be re-written by facility in zdemand for each zbox entered.

    // This feature is deprecated; this is a sequence of velocities to acquire in subsequent zboxen on the current zpath.
    private FA_d NaturalVelocityList; 

    // Where this zbox is contained; possibly null.
    zbox z;
    

    // null, or A set of zboxen which is contained in this zbox. New zboxen are added to the end.
    ContainmentList Z; 
    int ContainmentRank;  // ordinal number in our container. Manipulated only in ContainmentList.
    
    /*
      Position of this zbox in its container z.
      Valid whenever we are contained (modes MOVE or MOVED), 
      and  z.m = a continuous type. (Currently Span, Pipe)
      When we have arrived at the end, but we are still inside,
      This will be at L_z - l. We will shift to state MOVED.
    */
    double x;

    
    /* 2018-05-10.
       Contained zboxen may have a 'presence' associated with them. 
       Each zbox in the system can be spread over several separate locations or trajectories
       by splitting the presence. The interpretation (which is poor) desired at the moment is 
       [independent] probability, so the idea is that whenever a zbox makes a choice, its 
       presence is split to reflect the degree to which each possibility is taken.

       zboxen with a presence act exactly like normal zboxen, including with respect to capacity 
       and size restrictions.
     */
    private double ρ;    // Presence: ρ ∊ [0,1], or -1 to disable presence.
    int ρ_clone_number;  // assigned to each clone, by means of the clone number kept in the ztype.
    zbox ρ_Prohibited;   // null or else a zbox in which we are prohibited to shift to. Cleared when we do shift.
    double Total_ρ;      // Presence acumulator. Kept up-to-date (but will accumulate error.)
                         // Initialised along with SpaceInside (which does not).
    
    /*
      Keep track of L - Σ_j l_j.       (if zbox is not a prototype)
      (instead of recomputation)
    */
    public int Space_Inside;  // Really only used by Accommodation class.

    /*
      How many copies have been made.  (if zbox is a prototype)
      **Note that we currently do not support prototypes which contain things.
    */
    private int Copy_Number;


    
    /*
      (δx,δt) is valid when z.m = a continuous type (Currently Span, Pipe), when the zbox 
      is in an 'M' or 'D' state. This is what the zbox has done to get to x,t. (i.e. it 
      describes recent history) It is always a move by ProcessState() from the SysTime 
      into the future. Therefore any other element in the system which is processed will 
      begin at a time which is within the δt range.
    */
    double δx,δt;

    public double x_at_t(double tst_t)
    /*
      It is assumed that container zbox supports x.
      tst_t should usually be system time, or later.
      Returns -1 if cannot be known.
    */
    {
	if ((state=='M')||(state=='D'))
	    {
		if (tst_t < (t - δt)) {return(x-δx);}
		if (tst_t > t) {return(x);}
		if (δt==0) {return(x);}
		return( x - δx/δt * (t - tst_t) );
	    }
	if (state=='S') {return(x);}  // no way to know.
	return(-1.0);
    }

    public double t_at_x(double tst_x)
    /*
      Give any position.
      Returned will be a time after which we expect to attain that position or greater.
      or -1 if not predictable.
    */
    {
	if (state=='S') {return(x>=tst_x ? 0 : -1);} // Should never use this. :-/
	if ((state=='M')||(state=='D'))
	    {
		if (tst_x < (x - δx)) {return(t-δt);}
		if (tst_x > x) {return(-1.0);}
		if (δx==0) {return(-1.0);}
		return( t + δt/δx * (tst_x - x) );  // will be in the range t-δt ... t.
	    }
	return(-1.);
    }


    public double σ_to_x(double σ,ztype mover)
    {
	if (σ<=0) {return(0);}
	if (σ>1) {σ=1;}
	double x_max = get_L() * get_W() - mover.l;
	return(x_max*σ);
    }


    public void vel_from_lognormal(double v_μ,double v_σ)
    {
	// Provided mean and stddev are for Lognormal distribution:
	double σ2 = Math.log(v_σ*v_σ/v_μ/v_μ + 1.0);
	double μ = Math.log(v_μ) - σ2/2;
	double logv = Ψ.ND.sample(μ,Math.sqrt(σ2),Math.random());
	v = Math.exp(logv);
	
	//Str.print("LogNormal: v_μ=" + v_μ +" v_σ="+ v_σ + " σ2=" + σ2 + " μ=" + μ + " log v=" + logv + " => v=" +v);	
	//v=1.5;
	//    v = MLR.ftoi(3.0*Math.random())*0.5+1;
	//	v = 1.0 + Math.random();
	//if (v>2.0) {v=2.0;}	
	//Str.print("vel_from_lognormal:" + v);

	//v = v_μ;  //DEBUG 2018-12-18.
    }
    
    
    // Zbox internal state machine:
    public int state;  // Only read by Accommodation class.
    /*
      Use a letter integer (i.e. a 0-page Unicode number): '-', 'M', 'D', 'S' 'P'.
      
      'M' MOVE = Moving within z; still have space to go. We are progressing through z.
             x,v,V are used in case of continuous containment.
      'D' MOVED = We have hit the end of the containing box z, and are ready to move to SHIFT mode to leave.
      'S' SHIFT = passing through discrete states. Following zlinks and whatnot.
      This is also used for containment types with discrete position. (Currently: Shelf,Fifo,Bag)

      'P' Prototype = not subject to state processing; just used to make copies.

      '-' Deletion : Subject to removal at the next removal stage. See zsystem.removeZbox().

      'Z' Sleeping : do this if no exit from 'D' state right away.
      REMOVED, 2018-08-28, since now a zbox in 'M' state can also sleep if there is another zbox blocking.
      That is, sleeping is no longer a state, it is just presence in the [Z] syslist.
    */
    boolean Sleeping;   // True when moved to [Z]. False when moved elsewhere.

    // Used solely in 'zpathresolution', by the 'BoxNetwork' implementation.
    public ZboxNodeWrapper NodeWrapper;  

    // Each use requires new value requested from zsystem.FreshMark2()
    int mark2;   // Used to find zlinked zboxen in Λ calculations. 

    /*
      The following are lists or caches:
      things which are more efficiently updated than repeatedly calculated!
    */

    // null, or all explicit zlinks in which this zbox is involved. (i.e. μ=this or ν=this)
    // ReferenceResolution.zpaths_zlinks_zboxen() will initialise this.
    HashSet<zlink> zlinklist;

    // null, or zpaths in system which have a zstop with φ=this.    
    // ReferenceResolution.zpaths_zlinks_zboxen() will initialise this.
    HashSet<zpath> PathsWhoStopHere;
    
    // null, or Caches all the Λ sets associated with this zbox for various ztypes.
    public Neighbourhoods zneighcache;        


   /*-----------------------------------------------------------------*/
    // Following zpaths:
    /*
      P - The zpath this zbox is currently following. Either 'Plan' or 'Intent'. 
      P will be 'null' if no path is assigned.      
      
      When P is other than a 'Plan' zpath, i_P has no meaning.
      
      When P is a 'Plan' zpath, i_P is always the index into P corresponding to the /current/ container.
      This is true in all of 'S' 'M' or 'D' states.

      If σ_i or σ_f therein are applicable, then are used below for entry and exit.

      If P is an intent path and i_P is -1, then the path has not yet started; 
      upon entering the first zpath zstop, i_P will be 0.
    */
    int i_P;
    zpath P;

    private zstop Get_Current_zstop()
    {
	//V("ZSX0. Getting current zstop");
	if (P==null) {return(null);}
	//V("ZSX1. Getting current zstop");
	if (P.IsIntent()) {return(null);}  // We only can get current zstop with a plan.
	//V("ZSX2. Getting current zstop");
	if (P.Λ_λ==null) {V("Null Λ_λ even though we have an intent path. Should not happen! Get_Current_zstop()");}
	//V("ZSX3. Getting current zstop");
	if (i_P<0) {return null;}
	//V("ZSX4. Getting current zstop");
	if (i_P>=P.Λ_λ.length) {V("i_P beyond Λ_λ. Should not happen! Get_Current_zstop()");}
	//V("ZSX5. Getting current zstop");
	zstop zs = P.Λ_λ[i_P];
	if (zs==null) {return(null);}
	//V("ZSX6. Getting current zstop");
	return(zs);
    }

    private void next_path_index(int verbose)
    {
	i_P++;
	if (verbose>0) {V("Following zpath. This index is " + i_P);}			
    }

    
    /*
      We are on a path. Let us shift our intent to the
      next path element, which we will then process in a
      SHIFT state.
    */
    private void CompletedLastPathElement(int verbose)
    {
	Rd();
	if (P.m==1)
	    {
		if (verbose>0) {V("Finished zpath.");}
		P.RemoveFollower(this);
		P = null;
	    }
	else
	    {
		i_P=-1;
		if (verbose>0) {V("Following zpath; restarting cyclic; next index will be " + (i_P+1));}
	    }
    }
    
    /*-----------------------------------------------------------------*/

    public zbox(zsystem _Ψ)
    {
	ContainmentRank=0;	
	Ψ = _Ψ;
	info=null;
	Sleeping=false;
	DependingOnUs = null;
	ρ_Prohibited = null;
	ρ = -1;  // by default there is no Presence.
	Label = null;
	state = 'M';
	x = 0;δx = 0;δt = 0; // t is initialised elsewhere.
	P = null;
	i_P = -1; // default index. Irrelevant when P is null.
	//	Space_Inside=L;   L is not available until ztype is initialised. Also, this includes W now; see below.
	Copy_Number = 0;
	// This is initialised below in InitialiseSpaceInside().
	//Over-rides: (<0 means inherit from ztype)
	L=-1;W=-1;N=-1;S=-1;v=-1.0;
	Z = null;  // empty.
	NodeWrapper = null;
	NaturalVelocityList = null; 
	ξ=null;
    }

    /*
      Access to state machine:
     */
    public boolean isPrototype()  { return(state=='P'); }
    public void setPrototype()    { state='P'; }
    public void setRemovalState() { state='-'; }
    public void Enter_M_state(double SysTime) { state='M';δx=0;δt=0;t=SysTime; }  // x has been supplied.
    public void Enter_S_state() { Enter_S_state(Ψ.t); }
    public void Enter_S_state(double SysTime) {	state='S'; t=SysTime; δx=0;δt=0; }  // x is indeed potentially utilised in 'S' state.

    /*
      The local ones could just be set from 'e', but this is better;
      we can always tell where they came from; we might /change/ ztype dynamically later, etc.
    */
    public ServerFlags get_ξ()  {   if (ξ!=null) {return(ξ);} else {return(e.ξ);}   }
    public int get_W()    {     if (W>=0) {return(W);} else {return(e.W);}         }
    public int get_L()    {	if (L>=0) {return(L);} else {return(e.L);}        }
    public int get_S()    {	if (S>=0) {return(S);} else {return(e.S);}     }
    public int get_N()    {	if (N>=0) {return(N);} else {return(e.N);}    }
    public double get_v() {	if (v>=0) {return(v);} else {return(e.v);}   }
    public double get_v_eff() {
	int _W = z.get_W();
	int _L = z.get_L();
	int _S = z.get_S();
	double vel = get_v();
	if (!(z.e.V<0)) { if (z.e.V < vel) {vel = z.e.V;} }
	double v_eff = vel * ( _W*_L - e.l)/(_L - e.l);

	//Str.print("v_eff=" + v_eff + " vel_0=" + vel + " W=" + _W + " L=" + _L + " _S=" + _S);
	return(v_eff);
    }
    public void set_L(int _L)    {	L=_L;    }
    public void set_W(int _W)    {	W=_W;    }
    public void set_S(int _S)    {	S=_S;    }
    public void set_N(int _N)    {	N=_N;    }
    public void set_v(double _v)    {	v=_v;    }
    public double get_ρ() {  return(ρ);} // no longer exists in ztype.
    public void set_ρ(double _ρ)    {	ρ=_ρ;    }



    
    public zbox CopyOf(boolean vflag)
    /*
      zschedule & zdemand use this to copy prototypes, through zsystem.AddCopyOfPrototype().
      boolean vflag;
      vflag can be false: in this case, the verbosity list will not be checked and the result will be uncontained!
     */
    {
	zbox φ = new zbox(Ψ);
	φ.e = e; //ztype.
	φ.L=L; φ.W = W; φ.v = v;
	φ.R=R;  
	φ.S=S;
	φ.N=N;	
	φ.InitialiseSpaceInside(); // empty.
	if (state=='P')
	    {
		Copy_Number++; // copy number.
		if (Label!=null)
		    {
			φ.Label=Label + "/" + Copy_Number;
			if (vflag)
			    {
				if (Ψ.IsInVerboseList(φ.Label))
				    {
					φ.Follow=true;
				    }
			    }
		    }
		// Contained in the same place, forcibly.
		if (vflag)
		    {
			if (z!=null)
			    {
				φ.Shift(z,0);   // At x=0.
			    }
		    }
	    }
	return(φ);
    }
    

    
    private void Modify_ρ(double ρ_factor)
    /*
      Change the presence ρ by a factor.
     */
    {
	ρ = ρ * ρ_factor;
	if (Z!=null)
	    {
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			zbox γ = wif.next(); // Z.Z_next;
			γ.Modify_ρ(ρ_factor);
		    }
	    }
    }

    private void Compute_total_ρ()
    /*
      Add up the presence of all zboxen within.
      This is called from InitialiseSpaceInside() and then 
      Total_ρ functions as an [imperfect] accumulator.
    */
    {
	Total_ρ = 0.0;
	if (Z!=null)
	    {
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			zbox γ = wif.next(); // Z.Z_next;
			Total_ρ += γ.ρ;
		    }
	    }
    }


    private zbox SingleCloneOf(zbox destzbox,double ρ_factor)
    /*
      When using Presence ρ, we utilise this routine to obtain a zbox for each outcome.
      This works recursively, since the clones should contain everything the original did.

      Should call ClonesOf() below instead of using this directly? 

      If used directly, modify ρ.
     */
    {
	zbox φ = new zbox(Ψ);
	// Variables to clone:
	{
	    φ.e=e; // ztype.
	    φ.L=L; φ.R=R; φ.S=S; φ.N=N; φ.W = W;  // size, etc.
	    φ.ρ = ρ * ρ_factor;  // Presence.
	    //state:
	    φ.x=x; φ.δx=δx;φ.δt=δt;φ.t=t;
	    φ.state = state;
	    // zpath needs to know:
	    P.AddFollower(φ); // sets i_P
	    // zpath is not at beginning:
	    φ.i_P = i_P;
	    // Identical label.
	    φ.Label = Label;	    
	}
	if (Label!=null)
	    {
		e.clone_num++;
		φ.ρ_clone_number = e.clone_num;
		if (Ψ.IsInVerboseList(φ.Label))
		    {
			φ.Follow=true;
		    }
	    }
	// Contained in destbox, forcibly.
	if (destzbox!=null)
	    {
		φ.Shift(destzbox,0);  // at x=0.
	    }
	φ.InitialiseSpaceInside(); // empty.
	// Now, clone all we contain into the new one.
	{
	    if (Z!=null)
		{
		    Iterator<zbox> wif = Z.iterator();
		    while (wif.hasNext())
			{
			    zbox γ = wif.next();// Z.Z_next;
			    γ.SingleCloneOf(φ,ρ_factor);
			}
		}
	}
	return(φ);
    }

    public ArrayList<zbox> ClonesOf(ArrayList<zbox> destzboxen, ArrayList<Double> f)
    /*
      2018-05-10:
      Generates n clones of this zbox. Places them in the provided destination zboxen,
      and /gives/ each one a fraction f of the presence of this one. Σ f < 1 obviously.

      That is, fractions of the presence ρ of the current zbox are distributed to the clones.

      Result: There will be n clones + the original.
    */
    {
	ArrayList<zbox> clones = new ArrayList<zbox>();
	Iterator<zbox> wif = destzboxen.iterator();
	double totf=0;
	int j=0;
	while (wif.hasNext())
	    {
		zbox η = wif.next();  // container.
		double ff = f.get(j);
		zbox γ = SingleCloneOf(η,ff);
		totf += ff;
		clones.add(γ);
		j++;
	    }
	if (totf>1.0) {totf=1.0;}
        Modify_ρ(1.0 - totf);
	return(clones);
    }


    
    
    public void InitialiseSpaceInside()
    /*
      Set this once and then accumulate it.
      SpaceInside already includes W-scaling.

      2018-05-12: Also deal with Total_ρ here.

    */
    {
	if (state=='P')
	    {
		Copy_Number=0; // Only for a prototype.
	    }
	else
	    {
		Space_Inside = get_L() * get_W();
		if (Z==null) {return;}
		if (Space_Inside<0) {return;}
		int jcnt=0;
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			zbox _zi = wif.next();
			Space_Inside -= _zi.e.l;
			jcnt++;
		    }
		if (jcnt>0)
		    {
			Space_Inside -= (jcnt-1)*get_S();
		    }
	    }
	Compute_total_ρ(); // Sets Total_ρ.
    }

    

    public void set_natural_velocity_list(FA_d lis)
    {
	NaturalVelocityList=lis;
    }

    public String toString()
    {
	String I;
	I="[zbox";
	if (Label!=null)
	    {
		I += " '" + Label +"'";
		if (get_ρ()>=0) {I += " /c" + ρ_clone_number +"/ ρ=" + get_ρ() + " ";}		
	    }
	I += " A:" +e.A+ " n:" +e.n;
	I += " | t:" +t;
	if (z==null) { I+= " Uncontained";} else { I+= " Contained in z:" +z.Label+" at x="+x+ " v:" + get_v();}
	I += " R=" + DoReportFlags.toString(R) +" ";
	I += " Z:{";
	if (Z!=null)
	    {
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			zbox _zi=wif.next();
			if (_zi.Label!=null) {I += _zi.Label;}
			if (_zi.e!=null)
			    {
				I += "(" + _zi.e.A + ") ";
			    }
		    }
	    }
	I+="}";
	if (P!=null)  {
	    I += " P:{" ;
	    if (P.Label!=null) { if (Str.length(P.Label)>0) { I += "'" + P.Label + "' ";}}
	    if (P.Λ_λ!=null) { I += P.Λ_λ.length + " ";}
	    I += "}";
	}
	if (zlinklist!=null)  {I += " Linked:{" + zlinklist.size() + "} "; }
	if (state=='M')
	    {
		I += " x=" + x;
	    }
	I += " |"+Str.setUni(state) +"| ";
	I += "]";
	return(I);
    }

    
    public void InvalidateCaches(int BeforeOrAfter)
    /*
      Invalidates all Λ caches associated with the implicit zlink χ.

      The process is slightly different in the two cases:
  
      * BeforeOrAfter = 0 : use extant (or maybe fresh) Λ῀ and discard it.
      Just use Λ῀ to identify zboxen whose caches should all be invalidated, for the types specified in χ.
      Useful before modification of the situation of this zbox.

      * BeforeOrAfter = 1 : force fresh Λ῀ and keep it.
      Force generation of a new Λ῀, and then perform invalidation as in other case.
      Useful after modification of the situation of this zbox.
     */
    {
	if (e.χ==null) {return;}                     // No ztypes.
	if (Ψ.cacheNeighbourhoodsFlag==0) {return;}  // No caching.	
	if (zneighcache==null) {zneighcache = new Neighbourhoods(Ψ,this);}
	for (int j=0;j<e.χ.A_A.length;j++)  // (A,n) allowed by χ
	    {
		InvalidateCaches(e.χ.A_A[j],e.χ.A_n[j],BeforeOrAfter);
	    }
    }
    
    public void InvalidateCaches(int A,int n, int BeforeOrAfter)
    /*
      Invalidates caches for specified ztype.
    */
    {
	if (Ψ.cacheNeighbourhoodsFlag==0) {return;}  // No caching.
        if (zneighcache==null) {zneighcache = new Neighbourhoods(Ψ,this);}
	if (BeforeOrAfter==1)
	    {
		zneighcache.invalidate_with_new_Λt(A,n);   // New Λ῀; rest can be generated subsequently on demand.
	    }
	else
	    {
		zneighcache.invalidate_from_old_Λt(A,n);   // Λ῀ for everyone will be absent. Ready for change.
	    }
    }

    
    public void Shift(zbox γ,double ins_x)
    /*
      Shift this from wherever it is, to being contained inside γ.

      * 'ins_x' is utilised iff γ has a notion of position.
      * If ins is exactly zero or negative, then insertion will be at x=0 and last in γ's containment.
      * When ins>0, the insertion will not be 'last in the queue' but instead 
      exactly at position ins_x, at the corresponding position in the containment of γ.

      Nothing is checked; this is to be called after verification 
      that the rules and capacity requirements have been satisfied.
      ( To perform such verification first, use Attempt_Shift() )

      Invalidates appropriate NeighHood caches.
      Updates 'Space_Inside'.
      Does not deal with 'Total_ρ'.
    */
    {
	// --------- .z is olde container. FROM -----------
	InvalidateCaches(0); // Invalidate for olde container.
	// Remove from olde containment:
	if (z!=null)
	    {
		if (z.Z!=null)  // Must be.
		    {
			z.Space_Inside += e.l;
			if (z.Z.size()!=1) { z.Space_Inside += z.get_S(); }
			z.Z.isolate(this);
		    }
	    }
	// Add to new containment:
	if (γ.Z==null)
	    {
		γ.Z = new ContainmentList(Ψ.ConCon);
	    }
	if (ins_x<=0)
	    {
		ins_x=0;
		γ.Z.add(this);
		x=0;    // Just starting a new zbox containment.
	    }
	else
	    {		
		double insertion_pos = ins_x;
		double insertion_time = Ψ.t;
		x = ins_x; // insert at requested location.
		γ.Z.add(this,insertion_time,_φ_ -> (insertion_pos > _φ_.x_at_t(insertion_time) )); // λ expression for passing criterion.
		/*
		if (ins_x>0)  //DEBUG
		    {
			V("SHIFTING into " + γ.Label + " with ins_x=" + ins_x);
			V("RESULT: " + this);
			γ.DumpContainedWithX();
		    }
		*/
	    }

	// Go ahead and change this now:
	z = γ;  
	// --------- .z is new container. TO -----------
	if (z.Z.size()!=1) {z.Space_Inside -= z.get_S();}
	z.Space_Inside -= e.l;

	δx=0; //  We've done nothing yet.
	InvalidateCaches(1); // for our new location.
	ρ_Prohibited=null; // clear this after we do make a move.
    }


    public void CutMoveTime(double cutoff)
    {
	if ((state=='D')||(state=='M'))
	    {
		if (t < cutoff)
		    {
			double adjustt = (t - cutoff);
			double adjustx = (δx/δt)*adjustt;
			t = cutoff;
			x = x - adjustx;
			if (x<0) {x=0;} // prec.
			δx = δx - adjustx;
			δt = δt - adjustt;
			if ((δt<0)||(δx<0)) // only precision (might be -ε)
			    {
				δt=0;δx=0;
			    }
			state='M';   // might have been 'D' but now it will not make it to the end on its own.
		    }
	    }
    }

    
    public void DumpContainedWithX()
    { // DEBUG
	Str.print("-------------------------------- Contained in " + Label);
	if (Z!=null)
	    {
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			zbox β = wif.next();
			Str.printE("#522#x=" + β.x_at_t(Ψ.t) + "#444#  " + β);
		    }
	    }	
    }
    
    
    public void AwakenContained()
    /*
      Causes all zboxen contained inside this one to awaken if they are asleep.
      They get moved to the dynamical [0] list from (presumably) the [Z] list.
    */
    {
	//Str.print("  WAKING up inside " + Label +":");			
	if (Z!=null)
	    {
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			wif.next().Awaken();
		    }
	    }
    }

    private void AwakenAllDependingOnUs()
    /*
      Awakens all zboxen which are dependent on this one getting out of the way.
     */
    {
	if (DependingOnUs==null) {return;}
	/*
	if (DependingOnUs.size()>0)
	    {
		Str.print("!!! Awakening "+DependingOnUs.size()+"zboxen dependent on " + Label + ": ");
	    }
	*/
	Iterator<zbox> wif = DependingOnUs.iterator();
	while (wif.hasNext())
	    {
		zbox _zi = wif.next();
		// Str.printEn("#432# " + _zi.Label + ": ");
		// _zi.AwakenContained(Ψ);   // Was erroneously this until 2018-08-27.
		_zi.Awaken();
	    }
	//Str.print("");
	DependingOnUs.clear();
    }
    

    private void Awaken()
    {
	if (Sleeping)     // 2018-08-28 we used to have (state=='Z')
	    {
		// state = 'S';  // 2018-08-28.
		t = Ψ.t;         // 2018-06-11 NewWaking.
		δt = 0;
		δx = 0;
		Ψ.System_Z_Lists.add_to_Buffer0(this);
		Sleeping = false;
	    }
    }

    
    public void AwakenLinked()
    /*
      This used to have nothing to do with ztype, and used to be done recursively.

      Now, the type of the implicit zlink is used to determine whose occupants to awaken.

      This used to be called 'AwakenContained()' but containment is the wrong concept.

      Policy is:   2018-02-19[月]

      We awaken all zboxen whose traversable zlinks include the
      implicit zlink; So, we begin at the present zbox, consider
      accessible zboxen to the types allowed by the implicit zlink,
      and wake up all occupants of those.  It is /not/ recursive by
      containment, since zlink connectivity is /independent/ from
      hierarchal containment, except for implicit zlinks.

      Similarly, we do not wake up m=8 static zboxen, ever.
     */
    {
	if (e.χ!=null)
	    {
		//Str.print("Wake up linked to " + Label +" :");
		// Do this zbox first:
		AwakenContained();
		if (e.χ.A_A!=null)
		    {
			for (int j=0;j<e.χ.A_A.length;j++)
			    {
				int A,n;
				A = e.χ.A_A[j];
				n = e.χ.A_n[j];
				HashSet<zbox> Φ = Get_Λ(A,n); //   ReachableViaZlinks(this,A,n,true,0,Ψ.FreshMark2()); // Λ(φ)
				Iterator<zbox> wirvz = Φ.iterator();
				while (wirvz.hasNext())
				    {
					zbox φ = wirvz.next();
					if (φ==this) {continue;} // already did 'this'.
					φ.AwakenContained();
				    }
			    }
		    }
	    }
    }
    
    private boolean GoToSleepIfWeCan()
    /*
      Called when the zbox shifts to a new container (naturally, not teleported or forced)
      Called when the zbox cannot make the next shift because of insufficient zlinks
      (not because of lack of room in destination zbox).
      Also called when the zbox is waiting for another zbox (i.e. put on its dependence list).
     */
    {
	if (Ψ.allowZboxSleepingFlag==0)
	    {
		return(false);
	    }
	
	if (e.Z_A==null) {return(Sleeping);} // Never sleep.
	for (int k=0;k<e.Z_A.length;k++)  // list of types in which we can sleep.
	    {
		if (e.Z_A[k]==z.e.A)
		    {
			if ((e.Z_n[k]==z.e.n) || (e.Z_n[k]==0))
			    {
				Sleeping=true;
				//Str.print(" Sleep: " + Label + " trying to sleep: t=" +t + " ("+k+") Z_A,n=" + e.Z_A[k] +"," + e.Z_n[k] + " z_A,n=" + z.e.A +"," + z.e.n);
				//V(" Sleep: Going to sleep.");
				return(Sleeping);
			    }
		    }
	    }
	return(Sleeping);
    }
    

    
    public void AddDependingOnUs(zbox θ)
    /*
      This zbox is in the way of zbox θ. 
      Since θ is likely to go to sleep, this zbox will later awaken it.
     */
    {
	if (DependingOnUs==null) {DependingOnUs=new HashSet<zbox>();}
	if (Ψ.allowZboxSleepingFlag==0) {return;}
	DependingOnUs.add(θ);
    }
    
    
    zbox GetZboxAheadOf()
    /*
      Finds the zbox ahead of this, in its container's Z queue.
      Returns null if there's no one ahead of φ, or if φ is not even in the list.
    */
    {
	int j;
	if (z==null) {return(null);}   // Uncontained.
	if (z.Z==null) {return(null);} // No list.
	return(z.Z.getPrev(this)); //null if no-one. "Previous" = "Previous in line" is the convention.
    }



    /*
      The following are used for zbox SHIFT states.
     */

    boolean IsZboxLinked(zbox friend,ztype mover)
    /*
      See if there is a sequence of appropriate zlinks connecting this with friend, for the mover type to follow.
      Considers both explicit and implicit (χ_φ type) zlinks.

      Originally this involved recursive searching and such. 
      Now, it is only necessary to check the Λ' set (which still must be searched via contains()).
    */
    {
	HashSet<zbox> RR = Get_Λp(mover.A,mover.n); // ReachableViaZlinks(this,mover.A,mover.n,true,1,Ψ.FreshMark2());  // Λ'_(A,n)(φ).
	if (0==1) // Just for debugging.
	    {
		Str.print("qqq DEBUGSTART");
		Str.print("qqq  // (ztype " + mover.A +"," + mover.n + ") : " + friend.Label + " zlinked to:" );
		Iterator<zbox> wit = RR.iterator();
		int k=0;
		while (wit.hasNext())
		    {
			zbox φ = wit.next();
			k++;
			Str.print("     " + k + ": " + φ.Label);
		    }
		Str.print("qqq DEBUGEND");
	    }

	if (0==0)
	{ //debug
	    Iterator<zbox> wif = RR.iterator();
	    while (wif.hasNext())
		{
		    if (wif.next() == friend) {return(true);}
		}
	    return(false);	    
	}

	return(RR.contains(friend));   // this takes serious time. :-/
    }
    

    private String seeZboxRoute(ArrayList<zbox> L)
    /*
      Just for log files & debugging.
     */
    {
	String S="";
	S="  zlinks: ";
	for (int j=0;j<L.size();j++)
	    {
		S += L.get(j).Label + " ";
	    }
	return(S);
    }




    
    public int ProcessState(zsystem Ψ,double maxΔt,int verbose)
    /*
      =======================================================================================================
      This is the zbox state machine, in which is encoded the dynamics of most of the system.
      Call this to process the zbox state.

      M and D states are only processed if they need to be at the current system time Ψ.t

      Ψ.SysTime is the next known transition in the system.  
      If we find something which will happen earlier, we modify it accordingly.
      If the present zbox.t and the SysTime are exactly equal, then the state
      will be processed. Otherwise, it is a matter for future consideration.

      Given maxΔt is the maximum δt to advance (in this case, an 'M' state),
      even if a (δx,δt) could be predicted past this time.

      After D->S, further processing on the 'S' state is allowed; see 'fall through' below.

      Some checks here (e.g. for current state and containment) might be redundant, 
      but these are very fast.

      In an 'M' or 'D' state, δx,δt,x are valid only if containment is continuous; 
      containment type is Pipe or Span.
      =======================================================================================================
     */
    {
	if (Follow) {verbose++;}
	if (Sleeping) // 2018-08-28 (state=='Z')
	    { // Sleep
		/*
		  ProcessState() should really not have been called here. Switch us to the [Z] list.
		*/
		Str.print("Should not have been called.");
		return(4); //[Z]
	    }
	if (state=='-')
	    {   // Deleted; this no longer actually does anything but inhibit processing until disappearance from [].
		// Deletion therefrom is affected in zobject.DeletionFlag.
		// ProcessState() should [maybe] not have been called here. Delete from any list.
		return(0); //del.
	    }
	if (state=='P')
	    { // Prototype; switch to the [S] list.
		return(5); // [S]
	    }
	if (z==null)
	    { // Uncontained; switch to the [S] list.
		return(5); // [S]
	    }

	/*
	  If our container is a sink, delete ourselves!
	*/
	if ( z.e.m == 7 )
	    {
		// We should report our deletion.
		R("vanished");
		if (verbose>0) {V("Container " + z.Label + " is a sink; " +Label+" disappears.");}

		//zpath PP = P;  //This is just for debugging, to make sure thie zbox is removed from the list.
		//if (verbose>0) { Str.printn("BEFORE:");debug_zpath_print_followers(PP); }
		Delete();
		//if (verbose>0) { Str.printn("AFTER :");debug_zpath_print_followers(PP); }
		return(0);
	    }
	
	/*
	  If we are contained in a Static type, we do nothing.
	*/
	if (z.e.m==8)
	    {
		//	V("No movement; static containment in:" + z.Label );
		return(5); // [S]
	    }
	
	/*
	  We might be something with meaningful state. Check it:
	 */
	if (verbose>0)
	    {
		String δδ="";
		if ((state=='D')||(state=='M'))  {δδ = δδ();}
		V(" State:" + Str.setUni(state) + " with  Ψ.t=" + Ψ.t + "="+Str.SecInDayToTime(Ψ.t)+" " + δδ());
	    }

	
	if (state=='M') // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` 
	    {
		Debug_x_δx("M");
		return( ProcessState_M(Ψ,maxΔt,verbose) );
	    }

	if (state=='D')  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` 
	    {
		if (t > Ψ.t)
		    {  // We will need attention in the future.
			return(3); //[T]
		    }
		Debug_x_δx("D");
		ProcessState_D(Ψ,maxΔt,verbose);   // We will shift to 'S'; just fall through vvv
	    }
	
	
	if (state=='S')  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` 
	    {
		if (t > Ψ.t)
		    {  // We will need attention in the future.
			return(3); //[T]
		    }
		 return( ProcessState_S(Ψ,maxΔt,verbose) );
	    }
	
	return(1); // No processing.
    }
    

    private int ProcessState_S(zsystem Ψ,double maxΔt,int verbose)
    {
	// We need attention right now.
	if (P==null)
	    {
		if (verbose>0) {V("S mode, but no path.");}
		/*
		  If we are not on a zpath, then we have no intent. We're just idle.
		  Possibly treat this better later. Maybe we do need a 'STATIC' state or so.
		  Stay in SHIFT state. Maybe we will get a path later. 
		  Waiting.
		*/
		return(5); // List [S]. We might acquire a path later; we would be moved to [0] or [T] then.
	    }
	/*
	  Either there is an intent or plan zpath.
	*/
	if (P.IsIntent())
	    { // We will try to plan. If impossible, just give up for now.
		zpath ResolvedPath = P;
		if (verbose>0) {V("  . . . Resolving Zpath:" + P + " . . . ");}
		ResolvedPath = P.Resolve();
		if (verbose>0) {V("  . . . Resolved Zpath:" + ResolvedPath);}
		if (ResolvedPath!=null)
		    {
			/*
			  The zbox should be reomved from the intent zpath,
			  which might disappear.
			*/
			P.RemoveFollower(this);
				
			StateChange=true;
			ResolvedPath.dwf = 1; // Discard when finished. No other zbox will use it.
			// Assign zpath
			if (verbose>0) {V(" . . Deploying '" + Label + "' on path");}
			ResolvedPath.AddFollower(this);
			ResolvedPath.AddToLists();
			/*
			  A path may have been resolved from the present container to somewhere else.
			  In this special case, there is no need to shift to the present container.
				  
			  This is not ideal.
			*/
			if (z == ResolvedPath.Λ_λ[0].φ) {i_P++;}  // Will be 0.
		    }
		else
		    {// If not, just forget about this deployment.
			if (verbose>0) {V("S mode, but no path can be resolved. Forget intent.");}
			P.RemoveFollower(this);
		    }
	    }
	/*
	  We are on a path, and can identify where we would like to go next.
	  We either will have an explicit zbox, or a zpath match.
	*/
	zstop Next_Stop = null;
	if (P.Λ_λ==null)
	    {
		V("SHOULD NOT HAPPEN. P.Λ_λ==null FOR NON-NULL P. zbox.java");
	    }
	else
	    {
		int nexti_P = i_P + 1;
		if (P.Λ_λ.length > nexti_P)
		    {
			Next_Stop = P.Λ_λ[nexti_P];
		    }
		else
		    {
			// END of path here.
			CompletedLastPathElement(verbose);
			return(2); //[0]
		    }
	    }
	if (Next_Stop!=null)
	    {
		zbox Des_Nxt_φ = Next_Stop.φ;
		zpath Des_nxt_K = Next_Stop.K;
		if ( (Des_Nxt_φ==null) && (Des_nxt_K==null) )
		    {
			E("Both φ and K in zstop are null. Should not happen.");
		    }
			
		if (Des_Nxt_φ!=null)
		    { // Explicit next zbox.
			if (verbose>0) {V("S mode in " + z.Label + "; desired next is zbox " + Des_Nxt_φ.Label);}
				
			zbox Prev_Container = z;
			double ins_x = Des_Nxt_φ.σ_to_x(Next_Stop.σ_i,e);

			int M = Attempt_Shift(Des_Nxt_φ,verbose,Ψ.t,false,true,ins_x);
			
			if (3==M)
			    { // space issue. We should have a new time.
				return(3); // -> [T]
			    }
			if (0==M)
			    {
				// Do we have a special natural velocity in mind?
				if (NaturalVelocityList!=null)
				    {
					if (NaturalVelocityList.size()>i_P)
					    {
						v = NaturalVelocityList.get(i_P);
						if (verbose>0)
						    {
							V("When contained in " + z.Label + " which is #" +i_P+" on path velocity adjusted: " + Label + "_v=" + v);
						    }
					    }
				    }
				AwakenLinked();
				if (Ψ.SleepWhenFull)
				    {
					if (Prev_Container!=null)
					    {Prev_Container.AwakenAllDependingOnUs();}  // Since there may be room there now.
				    }
				Rentered();
				StateChange=true;
				
				//DEBUG
				//V("SHIFTED into " + Des_Nxt_φ.Label + " with ins_x=" + ins_x);
				
				return(2);// [0]
			    }
			if (1==M) // zlink issue only.
			    {
				GoToSleepIfWeCan();
				if (Sleeping) {return(4);} // [Z]     2018-08-28 (state=='Z')
				return(2);
			    }
			// if (2==M)  only case left.
			{
			    /*
			      Trouble is basically that this zbox cannot fit into desired container.
			      Add this to dependency list for that container, and sleep.
			    */
			    if (Ψ.SleepWhenFull)
				{
				    GoToSleepIfWeCan();
				    if (Sleeping)
					{
					    Des_Nxt_φ.AddDependingOnUs(this);
					    return(4);
					}
				}
			    return(2); //[0]
			}
		    }
			
		/*
		  Specific zbox is 'null'; indicates that we probably
		  have specified: "Anyone on the specified zpath"
		*/
		if (Des_nxt_K!=null)
		    {
			/*
			  Check the zboxen on this zpath, until we find a suitable one.   !Note1.
			  2018-04-10: We used to iterate through all zboxen on this path, and try moving to each one of them.
				  
			  We should instead make a list of zboxen which are (utilising this path) and (inside our Λ´).
			  noting that the list of (inside our Λ´) is almost always the shorter.		  
			*/
			if (verbose>0) {V("Checking zboxen on zpath '" + Des_nxt_K.Label 
					  + "' for a suitable agent; there are "
					  + Des_nxt_K.Followers.size() + " candidates.");}
			ArrayList<zbox> PossibleDestinations = new ArrayList<zbox>();
			{ // Iterate through Λ and keep ones which are on the right path.
			    HashSet<zbox> Λ_p = z.Get_Λp(e.A,e.n);
			    Iterator<zbox> Λ_zbi = Λ_p.iterator();
			    while (Λ_zbi.hasNext())
				{
				    zbox γ = Λ_zbi.next();
				    if (γ.P == Des_nxt_K) { PossibleDestinations.add(γ); }
				}					    
			}
			int MM = 1;
			{
			    Iterator<zbox> poxj = PossibleDestinations.iterator();
			    //		    Str.print("POSSDEST? ("+Label+")("+poxj.hasNext()+")");
		    
			    while (poxj.hasNext())
				{
				    Des_Nxt_φ = poxj.next();
				    //Str.print("POSSDEST: ("+Label+")" + Des_Nxt_φ.Label);
				    zbox Prev_Container = z;
				    int M = Attempt_Shift(Des_Nxt_φ,verbose,Ψ.t,true,// We have already checked, since we've selected from z.Λ'
							  true,0);
				    if (M==0)
					{
					    AwakenLinked();
					    if (Ψ.SleepWhenFull)
						{
						    if (Prev_Container!=null)
							{Prev_Container.AwakenAllDependingOnUs();}  // Since there may be room there now.
						}
					    R("(i) ⤷ " + z.Label);
					    StateChange=true;
					    return(2); //[0]
					}
				    else if ( (M==2) || (M==3) ) 
					{ // Impediment is more than only zlink.
					  // (We cannot predict t if there is more than one possibility.)
					    MM = 2;
					}
				}
			}
			if (verbose>0) {V("No zbox on the zpath was available. MM=" + MM);}
			if (1 == MM)
			    { // All issues were zlink issues.
				GoToSleepIfWeCan();
				if (Sleeping) {return(4);} // [Z]   2018-08-28 (state=='Z')
				return(2);
			    }
		    }
	    }
	/*
	  We did not find anywhere to go.
	  Stay in 'S' state.
	  Waiting.
	*/
	return(2); //[0]
    } // state 'S'



    private int ProcessState_D(zsystem Ψ,double maxΔt,int verbose)
    {
		
	if (verbose>0) {V("MOVED. currently inside " + z.Label + " Checking what to do next");}
	/*
	  It is time to move to our next intended zbox.
	*/
	if (P==null)
	    {
		if (verbose>0) {V("No zpath. we just go to SHIFT state.");}
		/*
		  We are not on a trajectory; this means we have no intent 
		  and we will simply shift to SHIFT mode. 
		  Eventually a zschedule or some other thing might grab us, and give intent.
		*/
		state = 'S';
		Rd();
		//StateChange=true;  // need to resolve if we've shifted.
		return(2); // Waiting in 'S'.  // [0]
	    }
	// If on a zpath, the 'S' state will take care of it.
	state='S'; // Switch to 'S' now.  In case we get stuck here, we keep 'x' valid.
	//v("New state: " + Str.setUni(state));
	StateChange=true;
	return(2); // -> [0] We used to fall through, but we need to be in [0] list.
    } // ! State 'S' can fall through v v v v  (if we did not, we should return(2);)

    
    private int ProcessState_M(zsystem Ψ,double maxΔt,int verbose)
    {
	if (t > Ψ.t)
	    { // We will need attention in the future.
		return(3);  // [T]
	    }
		
	double Δt=0;
	/*
	  We are to progress through our current container z.
	  How we do this and what we watch out for will depend on z's
	  containment mode.

	  "1:Span 2:Pipe 3:Shelf 4:Fifo 5:Bag 6:Source 7:Sink 8:Static"

	  These are /defined/ in a string in InputParser().
	*/
	if (z.e.m==1) //.....................................................Span
	    {
		if (verbose>0) {V("MOVE state within an m=1 Span zbox " + z.Label);}
		int _W = z.get_W();
		int _L = z.get_L();
		double x_max = _W * _L - e.l;
		// Current path might have a more stringent limit.
		{
		    zstop zs = Get_Current_zstop();
		    if (zs!=null)
			{
			    double σ_f = zs.σ_f;
			    if ((σ_f>0.0) && (σ_f<1.0))
				{
				    x_max *= σ_f;
				}
			}
		}
		boolean EoC=true; // end of container.
		double v_eff = get_v_eff();

		//if (v_eff>10.0) {v_eff = 10.0;}
		//if (v_eff<19.0) {v_eff = 19.0;}
		//v_eff = 50.0;  // DEBUG. 2018-12-14

		Δt = (x_max - x) / v_eff;
		if (Δt > maxΔt) { Δt = maxΔt; EoC = false; }
		// Store our movement:
		δx = Δt * v_eff; δt = Δt;
		// New x and 'expiry' time:
		x += δx;
		t = Ψ.t + δt;
		if (EoC)
		    {
			/*
			  We have moved to end of our containing box.
			  Thus, we will be in 'D' rather than 'M'; this is our last move within our container.
			*/
			if (verbose>0) {V("Shifting to D state. t=" + t);}
			state = 'D';
			// if (debug) {v("New state: " + Str.setUni(state));}
		    }
		Rd();
		// We are in 'D' or 'M' state, with new t. 
		StateChange=true;
		//AwakenAllDependingOnUs();
		return(3); // [T]
	    } // End of 'Span'.

		
	if (z.e.m==2) //.....................................................Pipe
	    {
		if (verbose>0) {V("MOVE state within an m=2 Pipe zbox " + z.Label);}
		int _W = z.get_W();
		int _L = z.get_L();
		int _S = z.get_S();
		double x_max = _W * _L - e.l;
		if (x_max<0)
		    {  // This could happen given bad inputs; say, L=0 mox.
			E("ERROR: '" + Label + "' x_max=" + x_max + " from: _W=" + _W + " _L=" + _L + " e.l=" + e.l );
		    }

		// Current path might have a more stringent limit.
		zstop zs = Get_Current_zstop();
		if (zs!=null)
		    {
			double σ_f = zs.σ_f;
			if ((σ_f>0.0) && (σ_f<1.0))
			    {
				x_max *= σ_f;
			    }
		    }
		double v_eff = get_v_eff();

		//v_eff = 21.0;  // DEBUG. 2018-12-14
		//Str.print(z.Label + " is a PIPE!  v_eff=" + v_eff );
			
		zbox γ = GetZboxAheadOf();
		if (γ==null)
		    {
			if (verbose>0) {V("No Box ahead.");}
			boolean EoC = true; // end of container.
			// No-one is ahead.
			Δt = (x_max - x) / v_eff;
			if (Δt > maxΔt)
			    {
				EoC = false;
				Δt = maxΔt; 
				δx = Δt * v_eff;
				δt = Δt;
				x += δx;
				// t += δt; 2018-09-27
				t = Ψ.t + δt;
				//Debug_x_δx("NBA -> M");
			    }
			else
			    {
				// Store our movement:
				δx = x_max - x;
				δt = Δt;
				x = x_max;
				// t += δt; 2018-09-27
				t = Ψ.t + δt;
				//Debug_x_δx("NBA -> D");
			    }
			if ((verbose>0)) {V("Movement: x=" +x + " t="+t+" δx=" + δx + " δt=" + δt);}
			if (EoC)
			    {
				/*
				  We have moved to end of our containing box.
				  Thus, we will be in 'D' rather than 'M'; this is our last move within our container.
				*/
				if (verbose>0) {V("Shifting to D state. t="+t);}
				state = 'D';
				//v("New state: " + Str.setUni(state));
			    }
			Rd();
			// We are in 'D' or 'M' state, with new t. 
			AwakenAllDependingOnUs();
			StateChange=true;
			return(3); // [T]
		    } // end of γ=null.
		// (γ!=null)  Zbox is ahead.
		{
		    zbox φ=this;
		    if (verbose>0) {V("Box ahead. " + γ.Label);}
		    /*
		      2018-06-10[風水日]
		      We try to be a bit clever here.
		      The problem is: what if we have already hit the box ahead, 
		      but this is still the lowest 'expiration' time in the system?
		      We should look at its trajectory and anticipate ours,
		      and our 'expiration' time should then be the same as that trajectory.
				  
		      This box: φ
		      Box ahead: γ
		    */
		    double gap = e.l + _S;

		    if (x == γ.x - gap)
			{
			    if (verbose>0) {V("We have already bumped into next zbox! x=" + x);}
			    GoToSleepIfWeCan();
			    StateChange=false;
			    t = γ.t;       // 2018-06-11 NewWaking; see above comment.
			    if (Sleeping)
				{
				    //Str.print("!!! Going to sleep: " + Label + " ...on the dependence list for abox " + γ.Label);
				    γ.AddDependingOnUs(φ);
				    return(4);
				} // [Z]    // 2018-08-28  (state=='Z')
			    return(2); //[0]
			}

		    AwakenAllDependingOnUs();
	
		    if ( ((γ.state!='M')&&(γ.state!='D')))
			{
			    // γ state must be 'S'.
			    // γ.t is not really valid. use system time.
			    // γ.x assumed valid.
			    double _t_ = Ψ.t;
			    if (verbose>0) {V("Box ahead is immobile: x=" + γ.Label + ".x=" + γ.x);}
			    // The next box is not moving.
			    Δt = (γ.x - gap - φ.x) / v_eff;
			    if (Δt > maxΔt)
				{
				    Δt = maxΔt;
				    // Store our movement:
				    δx = Δt * v_eff; δt = Δt;
				    x += δx;            // ! t += δt;
				    φ.t = _t_ + δt;     // 2018-09-27. As below.
				    //Debug_x_δx("NBA S");
				}
			    else
				{
				    // Store our movement:
				    δx = γ.x - gap - φ.x;   φ.x = γ.x - gap;
				    // δt = _t_ - φ.t ;        φ.t = _t_;      // 2018-09-20  was strange.
				    δt = Δt;
				    φ.t = _t_ + δt;   // 2018-09-27.
				    //Debug_x_δx("NBA S");
				}
			    // We are in 'M' state, with new t.
			    // 
			    if (δt<0) {V("δt Happened here:" + δx + " " + δt + " " + x + " " + t );}
			    Rd();
			    StateChange = false;
			    return(3); //[T].
			}
		    /*
		      γ is in 'D' or 'M' state.
		      The question: When and where will φ catch up to γ?
		    */
		    {
			if (verbose>0) {V("Box ahead '"+γ.Label+"' is mobile:  x=" +γ.x + " t="
					  +γ.t+" δx=" + γ.δx + " γ.δt=" + δt);}
			// for t < γ.t-γ.δt we are not worried.
			// for γ.t-γ.δt <= t <= γ.t we consider a collision with γ's trajectory:
			//       γ traj:  For γ.t - γ.δt < t < γ.t,  γ_x(t)  = γ.x - (γ.δx/γ.δt)(γ.t - t)
			double vel_diff;
			vel_diff = γ.δx/γ.δt - v_eff;
			double t_catch_γ=-1.0;
			if (vel_diff!=0)
			    {
				t_catch_γ = (1.0 / vel_diff)*(φ.x - γ.x + gap + γ.δx/γ.δt*γ.t - v_eff*φ.t);
			    }
			if ((vel_diff>=0) || (t_catch_γ > γ.t))
			    { /*
				φ will never catch up since it is slower.
				So, our Δt is until we get to γ.x (minus gap), which is where γ will be at γ.t,
				or else smaller if we're not allowed to forecast that long.
			      */
				if (verbose>0) {V("We will never catch up; we are either slow or late.");}
				Δt = (γ.x - gap - φ.x) / v_eff;
				if (Δt > maxΔt)
				    {
					Δt=maxΔt;
					δx = Δt * v_eff;
					δt = Δt;
					//φ.t += δt;        // 2018-09-27
					φ.t = Ψ.t + δt;
					φ.x += δx;
					//Debug_x_δx("BAM");
				    }
				else
				    {
					δx = γ.x - gap - φ.x;
					δt = Δt;
					//Debug_x_δx("BAM2  γ.x="+γ.x+" φ.x="+φ.x+" gap=" + gap);
					φ.x = γ.x - gap;
					//φ.t += Δt;        // 2018-09-27
					φ.t = Ψ.t + Δt;
				    }
				// We are in 'M' state, with new t.
				if (δt<0) {V("δt Happened HERE.");}
				Rd();
				StateChange = false;   //2018-09-21. was true.
				return(3); //[T].
			    }
			/*
			  2018-06-11 Collision Problem

			  We will catch up before γ.t.
			  We used to simply adjust so we catch up AT γ.t,
			  but that slows us down and possibly causes trouble behind us.

			  We want to proceed to the collision, and then follow γ with the same speed.
			  How to implement this?

			*/
			{
			    // This is not good, since we end up doing it asymptotically!
			    if (0==1)
				{
				    φ.δt = t_catch_γ - φ.t;  φ.t = t_catch_γ;
				    φ.δx = v_eff * φ.δt ; φ.x += φ.δx;
				}
			    else
				{
				    if (verbose>0) {V("We would (gap=" +gap+") catch up, so we share the limit t=" + γ.t);}
				    φ.δx = γ.x - gap - φ.x;  φ.x = γ.x - gap;
				    φ.δt = γ.t - φ.t;        φ.t = γ.t;
				}
			    Rd();
			    StateChange=true;
			    return(3); //[T].					
			}
		    }
		} // someone ahead: γ
	    } // End of 'Pipe'.


	if (z.e.m==3) //.....................................................Shelf
	    {
		if (verbose>0) {V("MOVE state within a m=3 Shelf zbox." + z.Label);}
		// We do not use x,v,V in this case; there is only ordinal position.
		zbox zb_ahead = GetZboxAheadOf();
		if (zb_ahead!=null) {return(1);}
		// We stand a chance of exiting since no-one is ahead of us.
		if ( 
		    (z.Space_Inside < (e.l + z.get_S()) )  // z unable to hold another of us.
		    ||
		    ( (z.Z.size() >= z.get_N()) && (z.get_N()>=0) )
		     )  // z cannot hold any more of anything.
		    {
			// We're free to go.
			state = 'D';
			Rd();				
			StateChange=true;
			return(2); // [0] since we'll need to shift.
		    }
		return(2); // [0] since we could leave anytime.
	    } // End of 'Shelf'.

	if (z.e.m==4) //.....................................................Fifo
	    {
		if (verbose>0) {V("MOVE state within a m=4 Fifo zbox." + z.Label);}
		// We do not use x,v,V in this case; there is only ordinal position.
		zbox zb_ahead = GetZboxAheadOf();
		if (zb_ahead!=null) {return(1);}
		// We're free to go. 
		state = 'D';
		Rd();
		StateChange=true;
		return(2); //[0]
	    } // End of 'Fifo'.
		
	if (z.e.m==5) //.....................................................Bag
	    {
		if (verbose>0) {V("MOVE state within a m=5 Bag zbox." + z.Label);}
		// We're Always free to go. (Not into MOVED state since we don't have δx,δt; we go straight to SHIFT.)
		state = 'D';
		Rd();
		StateChange=true;
		return(2); //[0]
	    } // End of 'Bag'.
	return(2); // [0] since we have expired in 'M' anyway!
    } // state 'M'



    

    int Attempt_Shift(zbox NewContainer,int verbose,double SysTime,boolean AlreadyCheckedZlinks)
    /*
      Attempt shifting to the starting position of a new container.
      x=0 is assumed; to support σ_i use instead Attempt_Shift(...,ins_x).
    */
    {
	int res=Attempt_Shift(NewContainer,verbose,SysTime,AlreadyCheckedZlinks,false,0);
	return(res);
    }


    int Attempt_Shift(zbox NewContainer,int verbose,double SysTime,boolean BypassZlinkCheck,boolean Next_i_P,double ins_x)
    /*
      Returns whether a move occurred or not:
      0 = Move occurred.
      1 = Move failed due to lack of zlinks.
      2 = Move failed for another reason. (Containment issues, etc.)
      3 = Move failed, but we have a projected time to check again.

      In order to allow
      (
      Competition: • More than one container may be anticipated to satisfy an entry condition.
      Choice: • More than one zbox may be competing for entry into a given container.
      )      
      we do NOT actually make the move unless the system time has arrived.

      2018-05-11:
      In the case of the move occurring, (i.e. we return a 0) we now support bifurcation of Presence.
      ztype: ρ  - bifurcation constant
      zbox: ρ - presence
      So, we will report that the move occurred, and indeed it did, but we leave some of 
      our Presence ρ behind in a cloned zbox, with the instruction not to follow us too closely!      
    */
    {
	/*
	  Maybe there is no container?
	  
	  e.g. Maybe the first zstop on a zpath (in teleport mode) is of K not φ type...
	*/
	if (NewContainer==null)
	    {
		if (verbose>1) {V("New container absent.");}
		return(2);
	    }
	/*
	  Maybe we are prohibited from trying?
	 */
	if (NewContainer==ρ_Prohibited)
	    {
		if (verbose>1) {V("Presence prohibited in new container.");}		
		return(2);
	    }
	/*
	  Are there suitable zlinks in place to get to
	  the NewContainer zbox from our current container?
	*/
	if (!BypassZlinkCheck)
	    {
		if (!z.IsZboxLinked(NewContainer,e))
		    {
			if (verbose>1) {V("No zlinks to get to " + NewContainer.Label);}
			return(1);
		    }
	    }
	/*
	  Are we allowed (by type) inside the NewContainer zbox?
	*/
	if (!NewContainer.e.CanContain(e))
	    {
		if (verbose>0) {V("Containment not allowed by ztype in " + NewContainer.Label);}
		return(2);
	    }
	/*
	  Is there space enough for us to enter, and when?
	*/
	Accommodation A = new Accommodation(Ψ,NewContainer,this,verbose);
	/*
	  Purely by capacity:
	*/
	if (!A.CouldBy_Capacity())
	    {
		if (verbose>1) {V("No space inside "+ NewContainer.Label);}
		return(2);
	    }
	/*
	  What about geometry?
	*/
	double τ = A.CouldBy_Geometry(ins_x);
	/*
	  τ < 0 : 不可能
	  τ = 0 : 都可以
	  τ > 0 : 會時間 (可能是Ψ.SysTime，還會更早)
	*/
	if ((τ>SysTime))
	    {
		// 2018-04-02. Seems here we should not move. We should be in [T] list.
		t = τ; // We had this on 2018-06-10...! SysTime;
		if (verbose>0) {V(Label + "("+z.Label+") cannot yet fit into " + NewContainer.Label + " ... check at t=" + t);}
		return(3);  // We have a new time. :-) 
	    }	
	if ((τ>=0) && (τ<= SysTime))
	    {
		boolean bifurcate=false;
		double f_to_move = 1.0;
		if (NewContainer.e.ρ>=0)
		    { // This container supports bifurcation. (ρ non-negative)
			if (ρ>=0)
			    {  // We have Presence. (ρ non-negative)
				int N_0=0;  if (z.Z!=null) { N_0 = z.Z.size();}
				int N_1=0;  if (NewContainer.Z!=null) { N_0 = NewContainer.Z.size();}
				double ρ_0=0.0; if (z!=null) {ρ_0 = z.Total_ρ;} // Not currently possible given we've qualified to move, but we might be uncontained?
				double ρ_1 = NewContainer.Total_ρ;
				ztype C_0 = z.e;
				ztype C_1 = NewContainer.e;
				f_to_move = NewContainer.e.ρ * Presence.TransferFraction(N_0,ρ_0,C_0,N_1,ρ_1,C_1,ρ);

				if ( (ρ * (1.0 - f_to_move)) > e.ρ_0 )
				    { // We are not just leaving an insignificant trace.
					bifurcate=true;

					if (verbose>0)
					    {
						V("Bifurcation: N_0=" + N_0 + " ρ_0=" + ρ_0 + "  N_1=" + N_1 + " ρ_1=" + ρ_1
						  + " ztype.ρ=" + NewContainer.e.ρ + " -> f'=" + f_to_move);
					    }
					
				    }
			    }
		    }

		{
		    AboutToExitContainer_PossiblyContactServer(verbose>0);
		}

		if (bifurcate)      // BIFURCATION IS DEPRECATED.
		    { 
			zbox φ_cl = SingleCloneOf(z,1.0 - f_to_move);  // it is the clone who stays behind.	
			φ_cl.state = 'S'; //ready to go. M would be inappropriate, since it would navigate this zbox again.
			φ_cl.ρ_Prohibited = NewContainer;  // ... and is NOT allowed to follow!
			if (verbose>0)
			    {
				V("  --> Bifurcation: " + toString());
				V("  --> Bifurcation: " + φ_cl.toString());
			    }
			ρ *= f_to_move;                                  // and we move.
    			Shift(NewContainer,ins_x); // ...to the new container.
			// Shift() deals with Space_Inside but not Total_ρ:
			z.Total_ρ -= ρ;            // ρ is the amount we moved.
			NewContainer.Total_ρ += ρ; // from z to the new container.
			// Finally, the new clone needs to be added to [0].
			Ψ.System_Z_Lists.add_to_Buffer0(φ_cl);
			// Finally finally, the clone should sleep, since nothing will
			// happen until the zlink configuration changes.
			φ_cl.GoToSleepIfWeCan();
		    }
		else
		    {
			Shift(NewContainer,ins_x);
			// even if we do not bifurcate, we keep track of ρ.
			double ρ_eff = 1.0; if (ρ>=0) {ρ_eff = ρ;}
			z.Total_ρ -= ρ_eff;
			NewContainer.Total_ρ += ρ_eff;
		    }
		Enter_M_state(SysTime);

		/*
		  We did indeed Shift(), so
		  i_P must be updated; it should reflect current container.
		 */
		{
		    if (Next_i_P) {next_path_index(verbose);}
		}

		// Traditionaly, we have used grep ______ to search for this obvious line in the verbose output:
		if (verbose>0) {V("New container: __________________ -> " + z.Label);}
		
		/*
		  This is the only place in which a zbox moves to a new container 'naturally',
		  that is, not teleported or forced. Possibly a server should be advised.

		  2018-10-01:
		  i_P++ must be performed first, above.
		  Reply from server might give zbox a new zpath, and performing i_P++ after that would be incorrect.
		*/
		{		    
		    EnteredNewContainer_PossiblyContactServer(verbose>0);
		}
		

		/*
		  Just for debugging.
		*/
		if (Ψ.pauseoneveryshift)
		    {
			MLR.Sleep(1000);
		    }
		return(0);
	    }	
	if (τ>=0)
	    { // Future.
   	        return(2);
	    }
	// τ < 0 : nothing we can do.
	if (verbose>1) {V("No space inside "+ NewContainer.Label);}
	return(2);
    }



    /*

      Some routines for making server contact.

     */
    


    private int EnteredNewContainer_PossiblyContactServer(boolean v)
    /*
      The zbox HAS just finished entering a new container.
      Returns how many servers are consulted.
    */
    {
	int scc=0;	
	// This is C and should check f,v,p bits:
	//Str.print("zbox '" + Label + " has entered " + z.Label + " and we might have ξ.");
	ServerFlags l_ξ = get_ξ();
	if (l_ξ==null) {return(0);}
	int C_ξ = z.e.get_ξ_for_ztype(e.A,e.n); // ξ spec' in the new container.
	if (C_ξ==0) {return(0);} // Everything is masked away.
	if (!l_ξ.mask_contains_bit(C_ξ,'C')) {return(0);}

	int jlim = l_ξ.size();
	for (int j=0;j<jlim;j++)
	    {
		if (l_ξ.contains_bit(j,'C'))
		    { // f,v,p are restrictions:
			if (l_ξ.contains_bit(j,'f'))
			    {
				if (z.Z.size()>1)  {continue;}  // Do not satisfy f since we are not alone.
			    }
			if (l_ξ.contains_bit(j,'v'))			
			    {
				Accommodation A = new Accommodation(Ψ,z,this,0);
				if (!A.CouldBy_Capacity())  // ! Really should be by type only ?; listed in Diary.u8.
				    { continue; } // Does not satisfy v since would not fit right now.
			    }
			if (l_ξ.contains_bit(j,'p'))
			    {
				if (P!=null)
				    {
					//V("zbox '" + Label + " has finished path '"+P.Label+"' i_P=" + i_P + " and utilise ξ.");
					if (i_P != (P.Λ_λ.length-1)) {continue;} // Do not satisfy p since we're not on the last step.
				    }
			    }
			if (PossiblyContactServer(l_ξ.get_n_ξ(j),v))
			    {
				//V("zbox '" + Label + " has utilised server. Now P= '"+P.Label+"' i_P=" + i_P );
				scc++;
			    }
		    }
	    } // for j
	return(scc);
    }

    private int AboutToExitContainer_PossiblyContactServer(boolean v)
    /*
      The zbox is still contained in old container, but is about to shift to a new one.
      Returns how many servers are consulted.
    */
    {
	int scc=0;	
	// This is E and should check f,v,p bits:
	ServerFlags l_ξ = get_ξ();
	if (l_ξ==null) {return(0);}
	if (z==null) {return(0);}
	if (z.e==null) {Str.print("!! Container has no ztype. Should not happen.");return(0);}

	int C_ξ = z.e.get_ξ_for_ztype(e.A,e.n); // ξ specification.
	if (C_ξ==0) {return(0);}
	if (!l_ξ.mask_contains_bit(C_ξ,'E')) {return(0);}

	int jlim = l_ξ.size();
	for (int j=0;j<jlim;j++)
	    {
		if (l_ξ.contains_bit(j,'E'))
		    { // l,p are restrictions:
			if (l_ξ.contains_bit(j,'l'))
			    {
				if (z.Z.size()!=1) {continue;}  // l not satisfied since it is not just this zbox.				
			    }
			if (l_ξ.contains_bit(j,'p'))
			    if (P!=null)
				{
				    // i_P used, not i_P+1. i_P reflects current container.
				    if (i_P != (P.Λ_λ.length-1)) {continue;} // Do not satisfy p since we're not on the last step.
				}
			if (PossiblyContactServer(l_ξ.get_n_ξ(j),v)) { scc++; }
		    }
	    } // for j
	return(scc);
    }
    
    private boolean PossiblyContactServer(int snum,boolean verbose)
    /*
      This is called when the criterion is determined to have been satisfied.
      Just need to check bits and then make contact.
     */
    {
	// That is enough; determine which server.
	ExternalServer S;
	if ((snum>=Ψ.Ξ.size())||(snum<0)) {return(false);}
	S = Ψ.Ξ.get(snum);

	if (verbose)
	    {
		//Str.printE("Event #411#" + event + "#0#;#131# l_ξ=" + l_ξ +"#115# C_ξ=" + C_ξ + " #132#Contact Server ("+snum+"): " + S.Desc() );
		Str.printE("Event #132#Contact Server ("+snum+"): " + S.Desc() );
	    }

	ServerRequest Req = new ServerRequest(Ψ,this);
	// Actually complete the request.

	Ψ.InsertZsyntax_IntoSystem(S.request(Req),verbose);
	
	// The system might now be altered in ways that cannot here be understood.
	// Acknowledge the same by returning truth.
	return(true);
    }

    
    
    /*
      Verbosity and Reporting.
     */

    private void Rd() //detailed reporting. i.e. every move. Many many kB.
    { 
	if (!DoReportFlags.Details(R,e.R)) {return;}
	R();
    }
    

    public void Rentered()
    {
	if (z!=null)
	    {
		R("⤷ " + z.Label);
	    }
    }
    public void R()
    {
	R("");
    }
    private String δδ()
    {
	return(" t="+t+"="+Str.SecInDayToTime(t)+" x=" +x + " δx=" + δx + " δt=" + δt +" ");
    }
    private void R(String S)
    /*
      Reporting function.
      String is optional; not sure how to use it yet.
    */
    {
	if (!DoReportFlags.Anything(R,e.R)) {return;}
	Ψ.Report(DoReportFlags.Channel(R,e.R),ReportInfo() + " D=\"" + S + "\"");
    }

    public String ReportInfo()
    {
	return(ReportInfo(false));
    }
    public String ReportInfo(boolean force)
    /*
      We would like to use this both for output reporting and server consultation.
      Server consultation probably always gets full detail; r=-1.
     */
    {
	// Required information:
	String lin = "ztype=" + Ψ.ZtypeRefs.getName(e.A) + "," + e.A + "," + e.n;
	lin += " t=" + Ψ.t;
	lin += " R=" + DoReportFlags.toString(R);
	//R bits: 1,2,4 = S C P
	if (force||DoReportFlags.Self(R,e.R))
	    {
		int ww;
		if (z!=null)
		    {
			ww = z.get_W();
		    }
		else
		    {
			ww = -1;
		    }
		lin += " state="+Str.setUni(state);

		if (Z!=null)
		    {
			lin += " Z.n=" + Z.size();
		    }
		else
		    {
			lin += " Z.n=0";
		    }

		if (Label!=null)
		    {
			lin += " label=" + Label;
			if (get_ρ()>=0) {lin += "/c" + ρ_clone_number + "/ ρ=" + get_ρ();}
		    }
		if (z!=null)
		    {
			if (z.Label!=null) {lin +=" z.label=" + z.Label;}
			lin += " z.L=" + z.get_L() + " z.W=" + ww;
		    }
		if ((state=='M')||(state=='D'))
		    {
			lin += " t0=" + (t-δt) + " x0=" + ((x-δx)/ww) + " t1=" + t + " x1=" + (x/ww) + " δt=" + δt;
			// lin += " t0H=" + Str.SecInDayToTime(t-δt); // + " t1H=" + Str.SecInDayToTime(t);
		    }
		if (state=='S')
		    {
			lin += " t1=" + Ψ.t + " x1=" + (x/ww);
			// lin += " t0H=" + Str.SecInDayToTime(t-δt); // + " t1H=" + Str.SecInDayToTime(t);
		    }
		lin += " l=" + e.l + " L=" + get_L();
		if (Z!=null) {lin += " Z.n=" + Z.size() + " SpaceInside=" + Space_Inside;}

		if (z!=null)
                    {
			lin += " z.n=" + z.Z.size();
		    }
		
		// can add others here.
            }

	if (force||DoReportFlags.Container(R,e.R))
	    { // C mode.  Containment both ways; z and Z.
		if (z!=null)
		    {
			//lin += " o=" + l;
			if (1==1) // Was removed. Too much detail!
			    {
				lin += " z.Z={";
				Iterator<zbox> wif = z.Z.iterator();
				while (wif.hasNext())
				    {
					zbox _zi=wif.next();
					if (_zi.Label!=null) {lin += " " + _zi.Label;}
					lin += "(" + _zi.e.A + "," + _zi.e.n + ")";
				    }
				lin+="}";
			    }
		    }
		if (Z!=null)
		    {
			lin += " Z={";
			Iterator<zbox> wif = Z.iterator();
			while (wif.hasNext())
			    {
				zbox _zi=wif.next(); //Z_next;
				if (_zi.Label!=null) {lin += " " + _zi.Label;}
				lin += "(" + _zi.e.A + "," + _zi.e.n + ")";
			    }
			lin+="}";
		    }
	    }
	if (force||DoReportFlags.Path(R,e.R))
	    { // P mode
		if (P!=null)
		    {
			if (P.Label!=null)
			    {
				lin += " K=" + P.Label + " i_P=" + i_P ;
			    }
			zstop zs = Get_Current_zstop();
			if (zs!=null)
			    {
				if (zs.Label!=null) {lin = lin + " zs=" + zs.Label; }
				if (zs.σ_i>=0) {lin = lin + " zs.σ_i=" + zs.σ_i;}
				if (zs.σ_f>=0) {lin = lin + " zs.σ_f=" + zs.σ_f;}
				if (zs.info!=null)  {lin = lin + " zs.i=" + zs.info; }
			    }
		    }			
	    }

	if (info !=  null)
	    {
		lin = " i=" + info + " " + lin;
	    }

	return("zbox " + lin);
	//Ψ.Report("zbox" + lin + " D=\"" + S + "\"");
	//Str.print(Ψ.ReportPrefix + "zbox" + lin + " D=\"" + S + "\"");
    }



    boolean ccontained_xx(zbox zb2)   //   _xx unused.
    {
	//Are we ccontained in zb2 at any order?
	zbox zb = this;
	while (zb.z!=null)
            {
		if (zb.z==zb2) {return(true);}
                zb=zb.z;
            }
        return(false);
    }
    
    ArrayList<zbox> Ccontained_XX()   // _XX unused.
    /*
      Returns list of zboxen in which we are contained to any order.
     */
    {
	ArrayList<zbox> CC = new ArrayList<zbox>();	
	zbox zb = this;
	while (zb.z!=null)
	    {
		CC.add(zb.z);
		zb=zb.z;
	    }
	return(CC);
    }


    
    ArrayList<zbox> HierDiff_xx(zbox from)   // _xx unused.
    /*
      Returns a set of zboxen in which 'from' is ccontained, but 'this' zbox is not.
      ccontained = ( contained ) ^ n where n>0  i.e. any order.

      Can return null. Deals with null input.

      If φ moves from zb1 to zb2, this is a list of container zboxen he has exited.
     */
    {
	if (from==null) {return(null);}
	if (from.z==null) {return(null);}
	ArrayList<zbox> HD = new ArrayList<zbox>();
	zbox zb;
	zb = from;
	while (zb.z!=null) {HD.add(zb.z);zb=zb.z;}
	zb = this;
	while (zb.z!=null) {HD.remove(zb.z);zb=zb.z;}
	return(HD);
    }
    

    /*
      Used in BoxNetwork, so used in PathResolution.
     */
    double CostToTraverse(ztype mover)
    {
	return(e.BaseCost * mover.CostFactor);
    }

    
    double TimeToTraverse(ztype mover)
    /*
      Right now, we give a best-case estimate based on type only.
      We might do this more intelligently later,
      which is why this is here rather than in ztype.

      In principle, this is an opportunity to acquire learning from experience.
    */
    {
	/*
	  Best-case estimate for mover to traverse this. 
	  If containment is impossible, return 0.

	  Keep in mind we might have L=0 (Bag case).
	*/
	if (!e.CanContain(mover)) {return(0);}
	// Now, traversal depends on containment, of course:
	if ( (e.m==1) || (e.m==2) ) // 'Span' or 'Pipe'
	    {
		double vel = mover.v;
		if (e.V>=0) {if (vel>e.V) {vel=e.V;}}
		double t = 1.0 * (get_L() - mover.l) / vel;
		//Str.printn(" [Traverse: " + mover.A + "∊" + A + ":" + t + "] ");
		return(t);
	    }
	// Otherwise, we should be getting an estimate from what happened in the past. Implement later.
	return(0.0); // Serious optimism.
    }


    /*
      Get_Λ* functions to look at various Λ neighbourhoods.
      These cache if we are allowed.

      Class NeighbourhoodCalculation contains a detailed description of the meaning of the various Λ sets.

      The repetition of code here is intentional; these may be treated less symetrically later.
      (They used to be much less symmetrical.)
     */    
    public HashSet<zbox> Get_Λ1(int A,int n)
    {	
	if (Ψ.cacheNeighbourhoodsFlag==0)
	    {
		return(NeighbourhoodCalculation.Reachable_ViaZlink(this,A,n,1,Ψ.FreshMark2()));
	    }
	if (zneighcache==null) {zneighcache=new Neighbourhoods(Ψ,this);}
	return(zneighcache.get_NeighHood(A,n).get_Λ1());
    }
    public HashSet<zbox> Get_Λt(int A,int n)
    {
	if (Ψ.cacheNeighbourhoodsFlag==0)
	    {
		return(NeighbourhoodCalculation.Reachable_ViaZlinks(this,A,n,false,0,Ψ.FreshMark2()));
	    }
	if (zneighcache==null) {zneighcache=new Neighbourhoods(Ψ,this);}
	return(zneighcache.get_NeighHood(A,n).get_Λt());
    }
    public HashSet<zbox> Get_Λ(int A,int n)
    {
	if (Ψ.cacheNeighbourhoodsFlag==0)
	    {
		return(NeighbourhoodCalculation.Reachable_ViaZlinks(this,A,n,true,0,Ψ.FreshMark2()));
	    }
	if (zneighcache==null) {zneighcache=new Neighbourhoods(Ψ,this);}
	return(zneighcache.get_NeighHood(A,n).get_Λ());
    }

    
    public HashSet<zbox> Get_Λp(int A,int n)
    {
	if (Ψ.cacheNeighbourhoodsFlag==0)
	    {
		return(NeighbourhoodCalculation.Reachable_ViaZlinks(this,A,n,true,1,Ψ.FreshMark2()));
	    }
	if (zneighcache==null) {zneighcache=new Neighbourhoods(Ψ,this);}
	return(zneighcache.get_NeighHood(A,n).get_Λp());
    }
    public HashSet<zbox> Get_Λd(int A,int n)
    {
	if (Ψ.cacheNeighbourhoodsFlag==0)
	    {
		return(NeighbourhoodCalculation.Reachable_ViaZlinks(this,A,n,false,1,Ψ.FreshMark2()));
	    }
	if (zneighcache==null) {zneighcache = new Neighbourhoods(Ψ,this);}
	return(zneighcache.get_NeighHood(A,n).get_Λd());
    }
    


	
    public void Delete()
    {
	Delete(true);
    }
    
    public void Delete(boolean RFC)
    /*
      Remove a zbox from the system: Deletion.

      Our zbox is important to whom?

      1. zlinks  μ,ν              - linked to ours.
      2. zpaths  PathsWhoStophere - stopping at ours.
      3. zboxen  z                - Containing us.
      4. zboxen  Z                - contained in us - these get removed recursively.
      5. zboxen  DependingOnUs    - not references, but wake them up so they do not wait!
      6. zpath which we are following.
      7. Λ caches                 - need to be invalidated.
      8. Syslist [].              - set zobject.DeletionFlag so we get removed from system.
    */
    {
	if (DeletionFlag) {return;}  // we are already deleted.
	
	// 1. zlinks.
	if (zlinklist!=null)
	{
	    ArrayList<zlink> zllc  = new ArrayList<zlink>();
	    zllc.addAll(zlinklist);
	    
	    Iterator<zlink> wif = zllc.iterator();
	    while (wif.hasNext())
		{
		    wif.next().Delete();
		}
	    zlinklist=null;
	}	
	// 2. PathsWhoStopHere.

	/*
	  We should convert them back to intent paths? Not clear yet, but probably.
	  Probably remove all but first and last stops from zpath and switch to intent mode.
	  If this zbox is the first or last, probably the zpath should be deleted.

	  NOT IMPLEMENTED YET.
	*/
	
	// 3. Remove from containment.
	if (RFC)
	    {
		if (z!=null)
		    {
			if (z.Z!=null)
			    {
				z.Space_Inside += e.l;
				if (z.Z.size()!=1) {z.Space_Inside += z.get_S();}
				z.Z.isolate(this);
			    }
			z=null;
		    }
	    }
	//4. We should recursively remove all that this zbox contains:
	if (Z!=null)
	    {
		Iterator<zbox> wif = Z.iterator();
		while (wif.hasNext())
		    {
			wif.next().Delete(false);  // No need for that zbox to remove itself from this.
		    }
		Z=null;
	    }
	// 5. Path we are following.
	if (P!=null)
	    {
		P.RemoveFollower(this);
		P = null;
		i_P=-1;   // makes no difference whan P is null.
	    }
	// 6.
	AwakenAllDependingOnUs();
	// 7.
	InvalidateCaches(0); // for where we are now.
	
	// 8.  Purge this from the zsystem lists.
	state = '-';       // This does not do anything now, but inhibit further processing.
	DeletionFlag=true;  // inhibits endless recursion.

	Ψ.System_Z_Lists.add_to_BufferD(this);  // Causes [] removal.
    }


    



    
    // ******************************************************************
    // Implementation of 'CompiledFileRW'
    // ******************************************************************

    public void WriteToObjFile(PrimitiveDataOut D) throws IOException
    { // DepOnUs,e,L,W,N,S,R,v,z,Z,x,Space_Inside,Copy_Number,δx,δt,state,i_P,linklist,paths,zneigh,Label.
	//   Should never happen. if (state=='-') {return;} // we're actually not here.
	D.wb('b');  //zbox.
	D.wi(GetFileRefNumber());
	if (DependingOnUs!=null)
	    {
		D.wi(DependingOnUs.size());
		Iterator<zbox> wif = DependingOnUs.iterator();
		while (wif.hasNext()) {D.wi(wif.next().GetFileRefNumber());}
	    }
	else
	    {
		D.wi(0);
	    }
	D.wi(e.GetFileRefNumber());
	D.wi(L);
	D.wi(W);
	D.wi(N);
	D.wi(S);
	if (ξ==null)
	    {
		D.wb(0);
	    }
	else
	    {
		D.wb(1);
		ξ.WriteToObjFile(D);
	    }
	D.wi(Space_Inside);
	D.wi(Copy_Number);
	D.wi(i_P);
	if (P==null) {D.wi(-2);}
	else
	    {
		D.wi(P.GetFileRefNumber());
		//if (P.GetFileRefNumber()<10) // just debugging.
		//{Str.print(Label + " PATH: " + P.GetFileRefNumber());}
	    }
	DoReportFlags.WriteToObjFile(D,R);
	D.wb(state);
	D.wboo(Sleeping);
	D.wd(v);
	D.wd(x);
	D.wd(t);
	D.wd(δx);
	D.wd(δt);
	if (z==null) {D.wi(-2);}
	else
	    {
		int frn = z.GetFileRefNumber();
		//Str.print(Label + ":::" + z.Label + "(((" + Str.setUni(state));
		D.wi(frn);
	    }
	if (Z!=null)
	    {
		Iterator<zbox> Zwif = Z.iterator();
		while (Zwif.hasNext()) {D.wi(Zwif.next().GetFileRefNumber());}
	    }
	D.wi(-1);
	/*
	  zlinklist  -- not loaded. Updated by zlink upon loading.
	  PathsWhoStopHere  -- not loaded. Updated by zpath upon loading.
	 */
	D.ws(Label);
    }
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D) throws IOException
    {
	ObjRef OR = new ObjRef(D.ri(),this);
	int DoU_l = D.ri();
	if (DoU_l!=0)
	    { //DependingOnUs
		OR.Rafs = new FA_i();
		for (int j=0;j<DoU_l;j++)
		    {
			OR.Rafs.add(D.ri());         // DoU
		    }
	    }
	OR.Rifs = new FA_i();
	OR.Rifs.set(0,D.ri());                       // e;
	L = D.ri();
	W = D.ri();
	N = D.ri();
	S = D.ri();
	if (D.rb()==1)
	    {
		ξ = new ServerFlags();
		ξ.ReadFromObjFile(null,D);
	    }
	Space_Inside = D.ri();
	Copy_Number = D.ri();
	i_P=D.ri();
	OR.Rifs.set(1,D.ri());                       //P
	R = DoReportFlags.ReadFromObjFile(ORL,D);
	state=D.rb();
	Sleeping=D.rboo();
	v=D.rd();
	x=D.rd();
	t=D.rd();
	δx=D.rd();
	δt=D.rd();
	OR.Rifs.set(2,D.ri());                       // z
	while (0==0)
	    {                                        // Z
		int r = D.ri();
		if (r==-1) {break;}
		if (OR.Rufs==null) {OR.Rufs = new FA_i();}
		OR.Rufs.add(r);
	    }
	Label = D.rs();
	ORL.add(OR);
	//Str.print("zbox: " + Label + " z=i(" + OR.Rifs.get(1) + ")");
    }

    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ)
    {
	if (OR.Rafs!=null)
	    {
		DependingOnUs = new HashSet<zbox>();
		for (int j=0;j<OR.Rafs.length;j++)
		    {
			DependingOnUs.add((zbox) RR.getref(OR.Rafs.get(j)));
		    }
	    }
	//Str.print(" Reference is " + OR.Rifs.get(0) + " which produces: " + RR.getref(OR.Rifs.get(0)));
	//Str.print("Label: " + Label + " OR#:" + OR.C + " Refs:" + OR.Rifs.get(0) + "," + OR.Rifs.get(1) + "," + OR.Rifs.get(2) ); //+ "->" + RR.getref(OR.Rifs.get(1)));
	
	e = (ztype) RR.getref(OR.Rifs.get(0));
	P = (zpath) RR.getref(OR.Rifs.get(1));
	z = (zbox) RR.getref(OR.Rifs.get(2));

	if (OR.Rufs!=null)
	    {
		Z = new ContainmentList(Ψ.ConCon);
		for (int j=0;j<OR.Rufs.length;j++)
		    {		
			Z.add((zbox) RR.getref(OR.Rufs.get(j)));
		    }
	    }
	//Str.printE("#014# zbox!! ResORs #123#(should be resolved) #343#" + toString() + " #550#" + Str.setUni(state));
	return(true);
    }




    /*
      2018-09-14: Verbosity.
     */

    /*
      All verbose output for testing goes through here.
    */
    public void V(String S)
    {
	Ψ.Verb.printnrgb("zbox: " + uid + " [" + Str.setUni(state) + "] ",0,3,5);
	if (Label!=null)
	    {
		Ψ.Verb.printn(" '" + Label +"' ");
		if (get_ρ()>=0)
		    {
			if (e.clone_num!=0)
			    {
				Ψ.Verb.printnrgb("/c" + ρ_clone_number +"/ ρ=" + get_ρ(),5,1,5);
			    }
		    }
	    }
	Ψ.Verb.printnrgb(" (" + e.A + "," + e.n + ") ",0,5,0);
	if (z!=null) {Ψ.Verb.printnrgb(" ∊ '" + z.Label + "' ",0,4,0);}
	if ((state=='M') || (state=='D'))
	    {
		Ψ.Verb.printnrgb(" t=" + t + "=" + Str.SecInDayToTime(t) +" ",1,3,5);
	    }
	Ψ.Verb.printrgb(" " + S,0,1,5);
    }
    /*
      All verbose error output goes through here.
     */
    private void E(String S)
    {
	Ψ.Verb.printnrgb("zbox: ",5,3,5);
	if ((state=='M') || (state=='D'))
	    {
		Ψ.Verb.printnrgb("t=" +t+" ",2,3,5);
	    }
	Ψ.Verb.printrgb(Label + ": ERROR :" + S,5,1,0);
    }

    

    
    
    private void debug_zpath_print_followers(zpath K)
    {
	HashSet<zbox> K_zb = K.Followers;
	String DEE = "% Zpath: " + K.Label + " Followers: #135#" + K_zb.size() + "#0#: ";
	Iterator<zbox> wiz = K_zb.iterator();
	while (wiz.hasNext())
	    {
		DEE += "[#531#" + wiz.next().Label + "#0#]";
	    }
	Str.printE(DEE);
    }




    private void Debug_x_δx(String S)
    {
	// Some debugging: These should never happen, of course.
	if (x<0) {  V("!!!!!!!!!["+S+"]: x = "+x+" < 0 !!!!!!!!"); System.exit(0); }
	if (δx<0) {  V("!!!!!!!!!["+S+"]: δx = "+δx+" < 0 !!!!!!!!"); System.exit(0); }
    }
    
}


