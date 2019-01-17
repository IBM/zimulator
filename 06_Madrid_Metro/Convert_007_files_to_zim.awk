
# All input is on stdin:
#
# input: _007_Station_List.csv , _007_Line_List.csv , _007_Itinerary_List.csv
#
# Optional: _size_mod_list.csv   ( e.g. output of calibration ) IF THIS IS MISSING, RANDOM L VALUES WILL BE GENERATED; see Gt_dist, Pf_dist
#
# # 1:Code,2:Name,3:Lat,4:Lon,5:SourceURL
# TDM,Tirso de Molina,40.4123454,-3.70466,https://en.wikipedia.org/wiki/Tirso_de_Molina_(Madrid_Metro)
#
# # 1:Index,2:Mode,3:LineCode,4:LineName,5:SourceURL
# 1,s,L1,Pinar de Chamartín - Valdecarros,https://en.wikipedia.org/wiki/Line_1_(Madrid_Metro)
#
# # 1:LineNum,2:StopNum,3:StationCode,4:DistanceTo[m]
# 1,1,PDC,0
#
# # 1:ZboxLabel,2:L
# RAR_GtCo_2,30
#
# Produces zsyntax output files.
# Random lengths will differ every time, due to srand() below.
#

BEGIN{

    srand();
    
    OF_Stations = "_01_Stations.zim";
    OF_Tracks = "_02_Tracks.zim";
    OF_Paths = "_03_Paths.zim";
    OF_Schedules = "_04_Schedules.zim";
    OF_Demand = "_05_Demand.zim";
#    OF_LiOfCo = "_06_List_Of_Corridors.csv";

    OF_Headway_for_Plotting = "_headway_vs_ToD.csv";

    OF_DemField = "_demand_during_day.dat";  # for demand-field plotting.
    
    peak_am = 7*3600 + 30;
    peak_pm = 18*3600 + 0;
 
    FS=",";
    PaxVel=2.0;
    max_ln=0;
}
 {     
     if (substr($1,1,1)=="#") {next;}
     if ((NF==5)&&(($1+0)!=$1))  # _007_Station_List.csv
     {
	 Process_STN_line($0);
	 next;
     }
     if ((NF==5))
     {
	 Process_LL_line($0);
	 next;
     }
     if (NF==4) # _007_Itinerary_List.csv
     {
	 Process_IL_line($0);	 
	 next;
     }
     if (NF==2)
     {
	 Process_ZboxL_line($0);
     }
     
 }

function Process_ZboxL_line(lin,  lina)
{
    split(lin,lina,",");
    SpecifiedZboxLengths[lina[1]] = int(lina[2]);
};
 
function Process_STN_line(lin,  lina)
{
    split(lin,lina,",");
    STN=lina[1];
    stnlat[STN]=lina[3];
    stnlon[STN]=lina[4];
    stnnam[STN]=lina[2];
    next;
};

function Process_LL_line(lin,     lina,ln)
{
    ## 1:Index,2:LineCode,3:LineName,4:SourceURL
    #1,s,L1,Pinar de Chamartín - Valdecarros,https://en.wikipedia.org/wiki/Line_1_(Madrid_Metro)
    split(lin,lina,",");
    ln = 0+lina[1];
    LINCode[ln] = lina[3];
    LINName[ln] = lina[4];
    LINType[ln] = lina[2];  # s or l
    if (ln>max_ln) {max_ln = ln;}
};

function Process_IL_line(lin    ,lina)
{
    ##1:LineNum,2:StopNum,3:StationCode,4:DistanceTo[m]
    #1,1,PDC,0
    #
    # Generate a zpath which visits all the stops on this line.
    #

    # Train source before beginning:
    split(lin,lina,",");
    ln = lina[1];
    sn = lina[2];
    LineStops[ln][sn] = lina[3];
    LineDists[ln][sn] = lina[4];
};    



END{
    # Make all generic stations:
    for (STN in stnnam)
    {
	Stn_Label = STN;
	Cc_Label = STN "_Cc";
	infostring = "latlon:" stnlat[STN] "," stnlon[STN];
	print "# ----------- Station: " STN " -- " stnnam[STN] " -----------" > OF_Stations;
	print "[zbox " Stn_Label " i=" infostring " A=Station n=1 ]" > OF_Stations;
	print "[zbox " Cc_Label " A=Concourse n=1 z=<" Stn_Label "> ]" > OF_Stations;
	# Give every station four gates, about 100 m to the concourse.
	print "# ----------- Gates for " STN > OF_Stations;
	for (g=1;g<5;g++)
	{
	    Gt_Label = Stn_Label "_Gt_" g;
	    GtOut_Label = Stn_Label "_Gt_" g "_out";
	    GtCo_Label = STN "_GtCo_" g;

	    Gt_dist = SpecifiedZboxLengths[GtCo_Label];
	    if (Gt_dist=="")  # None was specified.
	    {
		Gt_dist = int(70 + g*10*rand());   # random example length.
	    }
	    print "[zlink μ=<" Cc_Label "> ν=<" GtCo_Label "> A={ Pax . 0 } ]" > OF_Stations;
	    print "[zbox " GtCo_Label " A=Corridor n=1 L=" Gt_dist " z=<" Stn_Label "> ]" > OF_Stations;	 
#	    print GtCo_Label "," Gt_dist > OF_LiOfCo;
	    print "[zlink μ=<" GtCo_Label "> ν=<" Gt_Label "> A={ Pax . 0 } ]" > OF_Stations;	 
	    print "[zbox " Gt_Label " A=Gate n=1 z=<" Stn_Label "> ]" > OF_Stations;
	    print "[zlink μ=<" Gt_Label "> ν=<" GtOut_Label "> A={ Pax . 0 } ]" > OF_Stations;	 
	    print "[zbox " GtOut_Label " A=OutGate n=1 z=<" Stn_Label "> ]" > OF_Stations;	 
	}
    }

    # Now, Follow the lines and make the tracks and bounds and platforms.

    for (ln = 1 ; ln <= max_ln ; ln++)
    {
	# Make a train source and sink for use of this line.
	print "# ----------- Line " LINCode[ln] "(" LINName[ln] ") :" > OF_Tracks;
	print "[zsource Train_Source_" LINCode[ln] " φ=<Train_proto/" LINCode[ln] "> m=One o=Container ]" > OF_Tracks;
	print "[zbox Train_proto/" LINCode[ln] " A=Train n=1 π=1 z=<Source_" LINCode[ln] ">]" > OF_Tracks;
	print "[zbox Source_" LINCode[ln] " A=TrainSource n=1 ]" > OF_Tracks;
	print "[zbox Sink_" LINCode[ln] " A=TrainSink n=1 ]" > OF_Tracks;

	stops = length(LineStops[ln]);

	for (dir=0;dir<2;dir++)
	{
	    Ln_Label = LINCode[ln] "_" dir;
	    # Construct a zpath describing travel along this line:
	    for (esn = 1 ; esn <= stops ; esn++)
	    {
		sn = esn; nxsn = sn + 1;
		if (dir==1) {sn = stops + 1 - esn; nxsn = sn-1;}
		sn = mod1(sn,stops);
		nxsn = mod1(nxsn,stops);
		
		STN =  LineStops[ln][sn];
		Stn_Label = STN;
		Cc_Label = STN "_Cc";
		Bn_Label = STN "_Bn_" Ln_Label;	    
		Pf_Label = STN "_Pf_" Ln_Label;
		PfCo_Label = STN "_PfCo_" Ln_Label;

		Pf_dist = SpecifiedZboxLengths[PfCo_Label];
		if (Pf_dist=="")  # None was specified.
		{
		    Pf_dist = int(70 + g*10*rand());   # random example length.
		}
		infostring = "latlon:" stnlat[STN] "," stnlon[STN];
		# Each station stop: Bound, Pf, Corridor. zlinks:
		print "# ----------- Line " Ln_Label "(" LINName[ln] ") visiting " Stn_Label > OF_Stations;
		print "[zbox " Bn_Label " i=" infostring " A=Bound n=1 z=<" Stn_Label "> ]" > OF_Stations;
		print "[zlink μ=<" Bn_Label "> ν=<" Pf_Label "> A={ Pax . 0 } ]" > OF_Stations;
		print "[zbox " Pf_Label " A=Platform n=1 z=<" Stn_Label "> ]" > OF_Stations;
		print "[zlink μ=<" Pf_Label "> ν=<" PfCo_Label "> A={ Pax . 0 } ]" > OF_Stations;
		print "[zbox " PfCo_Label " A=Corridor n=1 L=" Pf_dist " z=<" Stn_Label "> ]" > OF_Stations;
#		print PfCo_Label "," Pf_dist > OF_LiOfCo;
		print "[zlink μ=<" PfCo_Label "> ν=<" Cc_Label "> A={ Pax . 0 } ]" > OF_Stations;
		# Track between stations, zlinks to bounds:
		if ((esn < stops) || (LINType[ln] == "l"))
		{
		    ti = "latlon2:" stnlat[LineStops[ln][sn]] "," stnlon[LineStops[ln][sn]] "," stnlat[LineStops[ln][nxsn]] "," stnlon[LineStops[ln][nxsn]];
		    tn = "Trk_" Ln_Label "_" LineStops[ln][sn] "_" LineStops[ln][nxsn];
		    if (dir==0) 
		    {
			Len = LineDists[ln][nxsn];
			if (Len==0)
			{
			    Len = LineDists[ln][nxsn+1];   # As an approximation.
			}
		    }
		    else
		    {
			Len = LineDists[ln][sn];
			if (Len==0)
			{
			    Len = LineDists[ln][sn+1];   # As an approximation.
			}
		    }
		    print "[zbox " tn " i=" ti " L=" Len " A=Track n=1 ]"  > OF_Tracks;
		    print "[zlink μ=<" LineStops[ln][sn] "_Bn_" Ln_Label "> ν=<" tn "> A={ Train . 1 } ]" > OF_Tracks;
		    print "[zlink μ=<" tn "> ν=<" LineStops[ln][nxsn] "_Bn_" Ln_Label "> A={ Train . 1 } ]" > OF_Tracks;
		}
	    }

	    # Make the zpath:
	    # For end-to-end lines, just go once.
	    # For loop lines go, say, two times.
	    times = 1 ; if ( LINType[ln] == "l" ) {times=2;}
	    ZPath = "";  #   "[zs φ=<Source_" LINCode[ln] ">]";    do not include the zsource in the list.
	    for (esn = 1 ; esn <= (stops * times) ; esn++)
	    {
		sn = esn; nxsn = sn + 1;
		if (dir==1) {sn = stops + 1 - esn; nxsn = sn-1;}
		sn = mod1(sn,stops);
		nxsn = mod1(nxsn,stops);
		STN =  LineStops[ln][sn];
		Bn_Label = STN "_Bn_" Ln_Label;
		ZPath = ZPath " [zs φ=<" Bn_Label ">] ";
		if (esn < (stops * times))
		{
		    tn = "Trk_" Ln_Label "_" LineStops[ln][sn] "_" LineStops[ln][nxsn];
		    ZPath = ZPath " [zs φ=<" tn ">] ";
		}
	    }
	    print "# ----------- Line " Ln_Label "(" LINName[ln] ") complete path:" > OF_Paths;
	    ZPath = ZPath " [zs φ=<Sink_" LINCode[ln] ">]";
	    # Print out accumulated zpath:
	    print "[zpath Train_" Ln_Label "_Line A=Train n=1 m=Open Λ={" ZPath "}]" > OF_Paths;

	    # Connect both ends to source:
	    firstBn = LineStops[ln][1] "_Bn_" Ln_Label;
	    lastBn = LineStops[ln][length(LineStops[ln])] "_Bn_" Ln_Label;
	    print "[zlink ν=<" firstBn "> μ=<Source_" LINCode[ln] "> A={ Train . 1 } ]" > OF_Tracks;
	    print "[zlink ν=<" lastBn "> μ=<Source_" LINCode[ln] "> A={ Train . 1 } ]" > OF_Tracks;
	    # Conenct both ends to sink:
	    print "[zlink ν=<" firstBn "> μ=<Sink_" LINCode[ln] "> A={ Train . -1 } ]" > OF_Tracks;
	    print "[zlink ν=<" lastBn "> μ=<Sink_" LINCode[ln] "> A={ Train . -1 } ]" > OF_Tracks;
	}
    }

    # Now, actually deploy some trains.
    {
	print "# SecInDay , Headway " > OF_Headway_for_Plotting;
	period_max = 500;  # off-peak
	period_min = 150;  # on-peak.
	pi = 4*atan2(1,1);
	for (dir=0;dir<2;dir++)
	{
	    for (ln = 1 ; ln <= max_ln ; ln++)
	    {
		Ln_Label = LINCode[ln] "_" dir;	    
		# [zschedule Sch_EWL_EB_1 T_0=05:14:00 T={ 0 255 510 765 1020 1275 } S=<Train_Source_EWL_EB> P=<Train_EWL_EB_Line> ]
		T=" ";
		t = 4*3600 + int(rand()*1800);   # first one.
		while (t<90000)
		{
		    T = T t " ";
		    x1 = (t - peak_am)/3600/5;
		    x2 = (t - peak_pm)/3600/5;
		    peaks = exp (- x1*x1) + exp (- x2*x2);
		    if (peaks>1.0) {peaks=1.0;}
		    dt = period_max - (period_max - period_min) * peaks;
		    #dt = (period_max + period_min)/2 - (period_max - period_min) / 2 * cos( (t-peak_am)/(peak_pm-peak_am)*2*pi);
		    dt = 45*int(dt/45);
		    if (ln==1)
		    {
			print t "," dt > OF_Headway_for_Plotting;
		    }
		    #T = T " <" dt ">";
		    t+=dt;
		}
		print "[zschedule Sch_" Ln_Label " T_0 = 0 T={" T "} S=<Train_Source_" LINCode[ln] "> P=<Train_" Ln_Label "_Line> ]" > OF_Schedules;
	    }
	}
    }
    
    # Finally; we need passengers to ride them.
    # Generate a day of demand, with a million passengers.
    #
    # Simulate flows within the city in the following way.
    # Centre of the metro area is, say, TTR station (Tribunal) which is at 40.426187,-3.7011027
    #
    # AM peak: O-D weighted so that most trips lead toward the city centre.
    # PM peak: O-D weighted oppositely.
    #

    #Produce a distance from each station to city centre.

    min_lat=0;
    min_lon=0;
    max_lat=0;
    max_lon=0;

    city_centre_lat = stnlat["TTR"];
    city_centre_lon = stnlon["TTR"];
    print "# City centre is at station TTR: " city_centre_lat "," city_centre_lon  > OF_Demand;
    maxdist = 0;
    zdem = "[zdemand Madrid_Day_pax S=<PassengerSource> T_0=0 Reference=%s L={";    # Reference the zsource in 00 file.
    for (STN in stnnam)
    {
	if ((min_lat==0) || (stnlat[STN] < min_lat)) {min_lat = stnlat[STN];}
	if ((max_lat==0) || (stnlat[STN] > max_lat)) {max_lat = stnlat[STN];}
	if ((min_lon==0) || (stnlon[STN] < min_lon)) {min_lon = stnlon[STN];}
	if ((max_lon==0) || (stnlon[STN] > max_lon)) {max_lon = stnlon[STN];}
	
	for (g=1;g<5;g++)
	{
	    zdem=zdem " <" STN "_Gt_" g "> <" STN "_Gt_" g "_out>";
	}
	stndist[STN] = WorldDist(city_centre_lat,city_centre_lon,stnlat[STN],stnlon[STN]);
	if (stndist[STN] > maxdist) {maxdist = stndist[STN];}
	print "# Dist from c.c. to " STN " @ " stnlat[STN] "," stnlon[STN] "  :  " stndist[STN] " m" > OF_Demand;
    }
    zdem=zdem " }";
    print zdem " D={ " > OF_Demand;
    stns = asorti(stndist,stnlist) * 4;   # count every gate.
    np=0;
    t = 4*3600;    # 04:00
    tmax=25*3600;  # 01:00 + day
    trips=0;
    maxtrips=100000;
    for (trips=0;trips<maxtrips;trips++)
    {
	if (t>tmax) {break;}
	orign = int(rand()*stns);
	origTLC = stnlist[int(orign/4)+1];
	destn = int(rand()*stns);
	destTLC = stnlist[int(destn/4)+1];
	# How popular is this route?
	{
	    x1 = (t - peak_am)/3600/4;
	    x2 = (t - peak_pm)/3600/4;
	    peaks = exp (- x1*x1) - exp (- x2*x2);
	    # peaks = (1 + cos( (t - peak_am)/(peak_pm - peak_am) * 2 * pi));  # weighting for peaks. 0...2
	    fp1 = 5;          # Basic level.
	    dr = stndist[destTLC] - stndist[origTLC];
	    fp2 = 15 * peaks * dr / 1000;  # level due to peaks.
	    if (fp2<0) {fp2=0;}
	    #print destTLC,stndist[destTLC],origTLC,stndist[origTLC];
	    fp = fp2 + fp1;
	    # fp3 = 10 + (dr / 1000) * cos( (t - peak_am)/(peak_pm - peak_am) *  pi)
	    p = int(fp);
	}
	#if ((t>peak_pm)&&(t<peak_pm+3600))
	#{
	# print "#  PEAK: ",int(t/3600),fp1,fp2,fp3, ":",dr;
	#}
	for (pp=0;pp<p;pp++)
	{
	    print orign*2 ,destn*2 + 1,t,1  > OF_Demand;
	    t += 1/p * 2.3;  # Normalise to get 1.7M in the day.	    
	    np++;
	}
	# Output for plotting:
	{
	    print origTLC,destTLC,pp,t > OF_DemField;
	}
#	print "## " dr " " fp3;
    }
    print " } ] # total of " np " passengers for the day."  > OF_Demand;    
	
};
    
function CartAngle(y,x)
# Returns angle corresponding to the point x,y. 
# Note that y,x are given in reverse order.
# Always returns in the range 0...2π, unlike gawk's own atan2().
{
    return(mod(atan2(y,x),2*3.1415926536));
};
function mod(x,y,   s)
# Returns x modulo y.
# The result is always such that 0 ≤ x < y.
# (in the special case y=0, 0 is returned)
{
    if (y==0) {return(0);}
    s = x % y;
    if (s<0) {s = s + y;}
    return(s);
};


function mod1(x,y)
{
    x = x % y;
    while (x<=0) {x=x+y;}
    return(x);
};

function WorldDist(lat1,lon1,lat2,lon2,   R,x1,y1,z1,x2,y2,z2,mag1,mag2,dot,cang,ang,dist,deg)
#
# Finds geodesic world distance in metres.
#
{
    R=6371000;  # m
    deg=3.14159/180;

    x1 = R * cos(lon1*deg) * cos(lat1*deg);
    y1 = R * sin(lon1*deg) * cos(lat1*deg);
    z1 = R * sin(lat1*deg);
    x2 = R * cos(lon2*deg) * cos(lat2*deg);
    y2 = R * sin(lon2*deg) * cos(lat2*deg);
    z2 = R * sin(lat2*deg);
    mag1 = sqrt( x1*x1 + y1*y1 + z1*z1);
    mag2 = sqrt( x2*x2 + y2*y2 + z2*z2);
    dot = x1*x2 + y1*y2 + z1*z2;

    cang = dot/(mag1*mag2);
    if (cang>1) {cang=1;}

    ang = atan2(sqrt(1/(cang*cang) -1),1);

    dist = R*ang;
    return(dist);
};

