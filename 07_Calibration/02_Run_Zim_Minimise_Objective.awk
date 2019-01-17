#!/bin/gawk -f

BEGIN{

    walkdist[0] = 2.0;   # v_μ
    walkdist[1] = 0.2;   # v_σ

    SourceDir = "ZimInput/";
    SetupFile = "00_StaticTypes.zim";
    NewSetupFile = "_00_StaticTypes.zim";

    StartTime="05:00";
    EndTime="09:00";
    TimeBins="16";
    ReferenceFile="_test_SYN_.0500-0900.csv";

    #
    # Use current walkdist and do a run:
    #
    #Obj = Run_Simulation(walkdist);
    #print "Φ = " Obj;

    wdlo[0]=1.0;wdlo[1]=0.05;
    wdhi[0]=10.0;wdhi[1]=1.0;
    
    N=2; #  Should be 2.
    maxiter=100;
    funcname = "Run_Simulation";
    slopefac = 1.0e-08;

    print "# ============== Calibration in progress.";
    print "# To see progress:";
    print "# $ tail -f _RZMO.log";

    Minimise(funcname,walkdist,0.01,slopefac,  1.075,0.7,  0.0,0.000,  0.01,  wdlo,wdhi,N,maxiter,1,"# min;","_RZMO.log");
}


#
# Use walkdist[] to run simulation, and retrieve objective value.
#
function Run_Simulation(walkdist,    com,lin,lina,Obj)
{
    Prepare_00_Setup_File(walkdist);

    # z=240 
    com="java -jar ZimCLI.jar -razo zl=1 z=60 R=out/madrid_calibration.zo t_1=" EndTime;
    com=com " I=_00_StaticTypes.zim I=" SourceDir "_02_Tracks.zim I=" SourceDir "_05_Demand.zim";
    com=com " I=" SourceDir "_01_Stations.zim I=" SourceDir "_04_Schedules.zim I=" SourceDir "_03_Paths.zim";
    system(com);

    com="java -jar TravelTimeTool.jar OBJ " StartTime "," EndTime "," TimeBins " " ReferenceFile " out/madrid_calibration.zo";
    print "#  " com;
    Obj = 0;
    while (0<(com | getline lin))
    {
	#print lin > "__test_ttt.u8";
	split(lin,lina);
	if (lina[1]=="Objective:")
	{
	    Obj = 0+lina[2];
	}
    }
    fflush();
    close(com);
    return(Obj);
};

function Prepare_00_Setup_File(walkdist,    lin,lina)
{
    while (0< ( getline lin < (SourceDir SetupFile) ) )
    {
	split(lin,lina);
	if (lina[1]=="v_μ")
	{
	    print "# " lin > (NewSetupFile);
	    lin = "v_μ = " (0+walkdist[0]);
	}
	if (lina[1]=="v_σ")
	{
	    print "# " lin > (NewSetupFile);
	    lin = "v_σ = " (0+walkdist[1]);
	}
	print lin > (NewSetupFile);
    }
    fflush();
    close(SourceDir SetupFile);
    close((NewSetupFile));    
};


function Minimise(funcname,x0A,dx,s,enth,reluc,mindy,toly,mindx,xlowA,xhighA,N,lim,verb,pref,logfile,      tries,done,x,y,i,j,xp,yp,G,dxmag,xnew,ynew,xsame)
# Performs controlled descent to find x which minimises y = funcname(x).
# This routine only requires the routine array_to_string() below.
#
# Typically enth=1.075 and reluc=0.7
# > When a step produces a lower objective y, the step size s -> enth * s
# > When a step produces a higher objective y, the step size s -> reluc * s and the step is re-tried.
# funcname is the name of the function, as a string.
# It should accept x0A as a single argument (of course it could have further optional arguments)
# toly is the amount y is required to be less than. (Useful when trying to solve for y=0)
# mindy is the minimum amount y can change in a step. If y changes less than this, the result is returned.
# mindx is the minimum amount x can change in a step. If x changes less than this, the result is returned.
# x0A is an array which is the starting point for x.
# dx is the x difference to use in taking a derivative f' := dy/dx ~= [y(x+dx) - y(x)]/dx.
#    It is applied to all components of x.
# s is the initial step size - the factor by which the (-) calculated difference is multiplied to get a new x.
#    s>0. It is all right to start small, provided that enth is large enough. 0.3 is a good start usually.
# xlowA is an array of lower bounds for x. xhighA is an array of upper bounds for x.
# Currently these low and high bounds cannot be ignored.
# lim is the maximum number of iterations to perform. (includes evaluation points and reluctance points)
# verb is 0 or 1 to control verbosity. When 1, the prefix 'pref' is used.
# All the arrays go from 0 . . . N-1 where N is the dimension of the x space.
#
# The result is returned in x0A. The number of iterations is returned.
#
{
    if (verb==1) {print pref "---------------- Minimise()--------------- 2018-09-12 ->" logfile;}
    # Set up variable x:
    for (j=0;j<N;j++)
    {
	x[j] = x0A[j];
	if (x[j] < xlowA[j]) {x[j] = xlowA[j];}
	if (x[j] > xhighA[j]) {x[j] = xhighA[j];}
    }
    tries=0;
    done=0;
    # Evaluate objective at x:
    if (logfile!="") { print pref " starting at x=" array_to_string(x,N,7) "..." > logfile; ;}
    y = @funcname(x);
    if (logfile!="") { print pref "" tries " f" array_to_string(x,N,7) " = " y > logfile; ;}
    while (done==0)
    {
	if (y < toly)
	{
	    if (logfile!="") {print pref "y = " y " < " toly " = toly" > logfile; }
	    break;
	}
	tries++;
	if (tries>lim)
	{
	    if (logfile!="") {print pref "Max iteration count reached." > logfile;}
	    break;
	}
	# Evaluate at x+dx nearby points:
	for (i=0;i<N;i++)
	{
	    for (j=0;j<N;j++) {  xp[j]=x[j];  if (j==i) {xp[j] += dx; }  }
	    yp[i] = @funcname(xp);
	    if (logfile!="")
	    { print pref "  f_" i " := f" array_to_string(xp,N,7) " = " yp[i] > logfile; }
	}
	# Evaluate numerical gradient:
	for (i=0;i<N;i++) {  G[i] = (yp[i] - y) / dx; }
	if (logfile!="") { print pref "    G[f] = " array_to_string(G,N,7) > logfile; }
	while (0==0)
	{
	    # Get new point by descending:
	    dxmag=0;
	    for (i=0;i<N;i++)
	    {
		xnew[i] = x[i] - G[i]*s;
		if (xnew[i] < xlowA[i]) {xnew[i] = xlowA[i];}
		if (xnew[i] > xhighA[i]) {xnew[i] = xhighA[i];}
		dxmag += (xnew[i] - x[i])*(xnew[i] - x[i]);
	    }
	    dxmag = sqrt(dxmag);
	    if (dxmag < mindx)
	    {
		if (logfile!="") {print pref "|dx| = " dxmag " < " mindx " = mindx"> logfile; }
		done=1;
	    } # flag that we have finished.
	    # Is new point really new?
	    xsame=0; for (i=0;i<N;i++) { if (x[i] == xnew[i]) {xsame++;}}
	    if (xsame==N) {done=1;break;}  # For whatever reason, we cannot move.
	    # Evaluate at new point.
	    ynew = @funcname(xnew);
	    if (ynew<y)
	    { # We have done better. Exhibit enthusiasm.
		if ((y - ynew) < mindy)
		{
		    if (logfile!="") {print pref "y - newy = " (y-ynew) " < " mindy " = mindy" > logfile;}
		    done=1;
		}    # flag that we are done.
		for (i=0;i<N;i++) { x[i] = xnew[i]; }
		s *= enth;
		y = ynew;
		if (logfile!="") { print pref "" tries " s +=" s " | f" array_to_string(xnew,N,7) " = " ynew > logfile;}
		break;
	    }	    
	    # We performed poorly. Exhibit reluctance.
	    s *= reluc;
	    if (logfile!="") { print pref "" tries " s -=" s " | f" array_to_string(xnew,N,7) " = " ynew > logfile;}
	    tries++;
	    if (tries>lim)
	    {
		if (logfile!="") {print pref "Max iteration count reached." > logfile;}
		break;
	    }
	}
	# We have already calculated y(x) and this is at new point.
    } # while
    # We now likely have a solution.
    for (j=0;j<N;j++) {x0A[j]=x[j];}
    return(tries);
};

function array_to_string(aa,n,l,    ss,i,ll)
# takes the elements 0....n-1 of aa[] and makes a string (....) of components.
# l is a limit, or 0.
{
    ll="";if (l!=0) {if (n>l) {n=l;ll=",...";}}
    ss="(" aa[0];
    for (i=1;i<n;i++)  {ss=ss "," aa[i];}
    ss=ss ll ")";
    return(ss);
};
