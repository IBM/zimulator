
#LineNum,StopNum,SST,StationName,SourceURL,Lat,Lon
#1,1,PDC,Pinar de Chamartín,https://en.wikipedia.org/wiki/Pinar_de_Chamart%C3%ADn_(Madrid_Metro),40.4801375,-3.6667999

@include "/usr/local/bin/pwm/GawkLibrary.awk"

BEGIN{
    FS=",";
    OFS=",";

}
 {
     ln=0+$1;
     sn=0+$2;
     stop[ln][sn]=$3;
     lat[$3]=$6;
     lon[$3]=$7;
 }
END{
    for (ln=1;ln<=16;ln++)
    {
	sn=1;
	while (stop[ln][sn]!="")
	{
	    if (sn==1)
	    {
		L = 0;
	    }
	    else
	    {
		L = WorldDist(lat[stop[ln][sn-1]],lon[stop[ln][sn-1]],lat[stop[ln][sn]],lon[stop[ln][sn]]);
	    }
	    # Now, we don't always go in a straight line!
	    # Let us simply use a factor:
	    L *= 1.1;
	    print ln,sn,stop[ln][sn],L;
	    sn++;
	}
    }
}


function WorldDist(lat1,lon1,lat2,lon2,   R,x1,y1,z1,x2,y2,z2,mag1,mag2,dot,cang,ang,dist)
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

    ang = atan2(sqrt(1/(cang*cang) -1),1);

    dist = R*ang;
    return(dist);
};

