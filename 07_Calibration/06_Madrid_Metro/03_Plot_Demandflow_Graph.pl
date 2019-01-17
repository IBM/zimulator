#!/usr/bin/perl                                                                                                                      

#
# Reads in the demand file, and some description of the 
# system geometry to produce a graphical representation of the net demand.
# Dumps EPS to stdout.
#

use strict;
use warnings;

# 'global' :
my $debug = 0;

my $figwid = 1000;
my $fighgt = 1000;
my $figx = 0;
my $figy = 0;

my $DemandFile = "_demand_during_day.dat";
my $StationFile = "Madrid_Public_Data/_007_Station_List.csv";
my $LinesFile = "Madrid_Public_Data/_007_Itinerary_List.csv";

my %StationInfo;

my @DemandField;
my $DemandFieldSize = 20;   # square.

my $MinLon = 0;
my $MinLat = 0;
my $MaxLon = 0;
my $MaxLat = 0;

my $pi = 4*atan2(1,1);

my @MRT_Itins; #  [line num] -> array[stopnum] = STN

{
    ps_start();


    print "%" . ("-" x 50) . " READING IN STATIONS \n";
    {
	open(my $in,  "<",  $StationFile) or die "$StationFile $!";
	my $s = 0;
	while (<$in>)
	{  # $_ is input line; includes EoL.
	    my $lin = $_;  #  origTLC destTLC pp t
	    if (substr($lin,0,1) eq "#") {next;}
	    my @flds = split(/,/,$lin); # 0:Code,1:Name,2:Lat,3:Lon,4:SourceURL
	    my $TLC = $flds[0];
	    my $Lon = $flds[3];
	    my $Lat = $flds[2];
	    {
		$MinLon = ($MinLon == 0) ? $Lon : min($MinLon,$Lon);
		$MaxLon = ($MaxLon == 0) ? $Lon : max($MaxLon,$Lon);
		$MinLat = ($MinLat == 0) ? $Lat : min($MinLat,$Lat);
		$MaxLat = ($MaxLat == 0) ? $Lat : max($MaxLat,$Lat);
	    }
	    $StationInfo{$TLC} = { 'Name' => $flds[1] , 'Lat' => $Lat , 'Lon' => $Lon , };
	    $s++;
	}
	# Print to check:
	$s = 0;
	STNS: foreach my $stn (keys %StationInfo)
	{
	    my $TLC = $stn;
	    $s ++;
	    if ($s > 10) {last STNS;}
	    print("% " . $TLC . " [ " . $StationInfo{$TLC}->{'Name'} . " : " 
		  . $StationInfo{$TLC}->{'Lat'} . "," . $StationInfo{$TLC}->{'Lon'} . " ] \n");
	}
	print("%" . (" . " x 10) . "\n");	
	print("% MaxLat = $MaxLat  MinLat = $MinLat  MaxLon = $MaxLon  MinLon = $MinLon \n");
    }


    
    print "%" . ("-" x 50) . " READING IN METRO LINES \n";
    {
	open(my $in,  "<",  $LinesFile) or die "$LinesFile $!";
	my $lastline = 0;
	while (<$in>)
	{  # $_ is input line; includes EoL.
	    my $lin = $_;  #  line,stop,code,time:  1,3,CCH,822.981
	    if (substr($lin,0,1) eq "#") {next;}
	    my @flds = split(/,/,$lin); # 
	    if ($flds[0] != $lastline)  {$lastline = $flds[0];}
	    $MRT_Itins[$flds[0]]->[$flds[1]] = $flds[2];
	}
	print "%" . (" . " x 10) . "\n";	
    }
    

    print "%" . ("-" x 50 ). " READING IN DEMAND DATA \n";

    InitDemandField();
    
    {
	open(my $in,  "<",  $DemandFile) or die "$DemandFile $!";
	my $p = 0;
	DEMS: while (<$in>)
	{  # $_ is input line; includes EoL.
	    my $lin = $_;  #  origTLC destTLC pp t 
	    my @flds = split(" ",$lin);  # space emulates awk.
	    my $o_TLC = $flds[1];
	    my $d_TLC = $flds[0];
	    my $pax = $flds[2];
	    my $timsec = $flds[3];
	    my $oLat = $StationInfo{$o_TLC}->{'Lat'} ;
	    my $oLon = $StationInfo{$o_TLC}->{'Lon'} ;
	    my $dLat = $StationInfo{$d_TLC}->{'Lat'} ;
	    my $dLon = $StationInfo{$d_TLC}->{'Lon'} ;

	    my $wd = WorldDistance($dLat,$dLon,$oLat,$oLon);
	    if ($p % 500 == 0)
	    {
		print  ("% Trip #$p [ $o_TLC -> $d_TLC :  $oLat , $oLon  -  $dLat , $dLon\n");
	    }    
	    
	    my $dir = CartAngle($dLat - $oLat,$dLon - $oLon);
	    
	    my $dirdeg = $dir / atan2(1,0) * 90;
	    if ($p % 500 == 0)
	    {
		print  ( "% Trip #$p [ $pax pax $o_TLC -> $d_TLC : " . (sprintf("%.3f",$wd/1000)) . " ㎞  Direction " . sprintf("%.0f",$dirdeg)
			 . "° t=$timsec ] \n" );
	    }
	    # Now, make contribution to field:
	    my @ij =  WorldToGrid(($oLat + $dLat)/2,($oLon + $dLon)/2);    # Of course, not just o, since would always be inward.
	    #print "i,j = $ij[0] $ij[1] \n";
	    my $lay = int ($timsec / 3600);
	    #print ("LAYER:" . $lay . " from t= " . $timsec . "\n");
	    AddToField($lay,$ij[0],$ij[1],$dir,$pax);	    
	    $p++;
	}
    }
    print "%" . ("-" x 50) . "\n";


    { # Draw all the stations:
	ps_col(0,0,0);
	if (0==1)
	{
	    foreach my $TLC (keys %StationInfo)
	    {
		my $Nam = $StationInfo{$TLC}->{'Name'};
		my $Lat = $StationInfo{$TLC}->{'Lat'};
		my $Lon = $StationInfo{$TLC}->{'Lon'};
		my @xy = WorldTo1kxy($Lat,$Lon);	  
		my @psxy = xyl_to_3d($xy[0],$xy[1],0);
		ps_disc($psxy[0],$psxy[1],4);
		#ps_text($psxy[0],$psxy[1],$Nam);  # what a mess!
	    }
	}
	
	# Print to check:
	my $lnum = 1;
	foreach my $LN (@MRT_Itins)
	{ # LN is an array reference.
	    my $stns = $#{$LN};
	    for( my $sn=1;$sn<$stns;$sn++)
	    {
		my $TLC1 = $LN->[$sn];
		my $TLC2 = $LN->[$sn+1];
		#print ("  $lnum : " . $TLC . "\n");
		my $Lat1 = $StationInfo{$TLC1}->{'Lat'};
		my $Lon1 = $StationInfo{$TLC1}->{'Lon'};
		my @xy1 = WorldTo1kxy($Lat1,$Lon1);	  
		my @psxy1 = xyl_to_3d($xy1[0],$xy1[1],0);
		my $Lat2 = $StationInfo{$TLC2}->{'Lat'};
		my $Lon2 = $StationInfo{$TLC2}->{'Lon'};
		my @xy2 = WorldTo1kxy($Lat2,$Lon2);
		my @psxy2 = xyl_to_3d($xy2[0],$xy2[1],0);

		ps_disc($psxy1[0],$psxy1[1],4);

		ps_line($psxy1[0],$psxy1[1],$psxy2[0],$psxy2[1]);
	    }
	    $lnum++;
	}
    }
    
    { # Draw the field.	
	ps_font(14);
	ps_lw(1);
	for (my $lay=4;$lay<=24;$lay=$lay+4)	#my $lay=8;
	{
	    my $gs = $DemandFieldSize;
	    my @txy = xyl_to_3d(1000,1000,$lay);
	    ps_col(0.1,0.8*(1.0-$lay/23),0.8*($lay/23));
	    ps_text($txy[0],$txy[1],$lay . ":00");
	    for (my $i=0;$i<$gs;$i++)
	    {
		for (my $j=0;$j<$gs;$j++)
		{
		    my @xy = Grid_to_1kxy($i,$j);
		    my @df = GetDemandField($lay,$i,$j);
		    my $dx = 0.005 * $df[0];
		    my $dy = 0.005 * $df[1];
		    #print ( "%VEC " . $dx . " " . $dy );
		    tdl($xy[0],$xy[1],$xy[0]+$dx,$xy[1]+$dy,$lay,3);
		}
	    }
	}
    }
    ps_end();
}

sub Grid_to_1kxy # i,j -> x,y
{
    my $i = shift;my $j = shift;
    my @xy = ( $i * 1000/$DemandFieldSize ,  $j * 1000/$DemandFieldSize);
    return(@xy);    
}

sub WorldTo1kxy # Lat,Lon  returns i,j.
{
    my $Lat = shift;
    my $Lon = shift;
    my $j = ($DemandFieldSize * ($Lat - $MinLat) / ($MaxLat - $MinLat) );
    my $i = ($DemandFieldSize * ($Lon - $MinLon) / ($MaxLon - $MinLon) );
    return(Grid_to_1kxy($i,$j));
}

    
sub tdl # x1,y1,x2,y2,lay,m,
{
    my $x1=shift;my $y1=shift;my $x2=shift;my $y2=shift;
    my $lay=shift;my $m=shift;
    my @xy1 = xyl_to_3d($x1,$y1,$lay);
    my @xy2 = xyl_to_3d($x2,$y2,$lay);
    ps_line($xy1[0],$xy1[1],$xy2[0],$xy2[1]);
    ps_disc($xy1[0],$xy1[1],$m);
}

sub xyl_to_3d   #x,y,lay  -> px,py
{
    my $x=shift;my $y=shift;my $lay = 4 + shift;

    # $x = 1000 - $x;

    my @pxy;
    if ($debug == 1)
    {
	$pxy[0] = $x;
	$pxy[1] = $y;
	return @pxy;
    }
    my $pers=11;
    $pxy[0] = ($x + $y) / 2 + $lay*2;
    $pxy[1] = $lay * 40 + ($y - $x)/$pers;
    return @pxy;
}

sub InitDemandField
{
    for (my $lay=4;$lay<=24;$lay=$lay+4)	#my $lay=8;
    {
	for (my $fi=0;$fi<$DemandFieldSize;$fi++)
	{
	    for (my $fj=0;$fj<$DemandFieldSize;$fj++)
	    {
		my $idx = $DemandFieldSize * $DemandFieldSize * $lay + $DemandFieldSize * $fj + $fi;
		$DemandField[2*$idx ] += 0;
		$DemandField[2*$idx + 1] += 0;
	    }
	}
    }
}

sub AddDemandField  #  lay,i,j,valx,valy
{
    my $lay = shift;
    my $fi = shift;
    my $fj = shift;
    my $fx = shift;
    my $fy = shift;
    my $idx = $DemandFieldSize * $DemandFieldSize * $lay + $DemandFieldSize * $fj + $fi;
    $DemandField[2*$idx ] += $fx;
    $DemandField[2*$idx + 1] += $fy;
}
sub GetDemandField  #  i,j  returns x,y
{
    my $lay = shift;
    my $fi = shift;
    my $fj = shift;
    my $idx = $DemandFieldSize*$DemandFieldSize*$lay + $DemandFieldSize * $fj + $fi;
    #my @fxy = @DemandField[2*$idx , 2*$idx + 1];

    my @fxy = ( 0+ $DemandField[2*$idx] , 0+ $DemandField[2*$idx+1] ); 
    return(@fxy);
}


sub AddToField  #  lay,i,j,dir,pax
{
    my $lay = shift; my $i = shift; my $j = shift; my $dir = shift; my $mag = shift;

    my $ds = $DemandFieldSize / 8.0;
    for (my $fi=0;$fi<$DemandFieldSize;$fi++)
    {
	for (my $fj=0;$fj<$DemandFieldSize;$fj++)
	{
	    my $d2 = ( ($fi-$i)*($fi-$i) + ($fj-$j)*($fj-$j) ) / ($ds*$ds);
	    my $c = $mag * exp( - $d2 ) * 3;
	    #print ("C: $i $j $fi $fj $d2 \n");
	    AddDemandField($lay,$fi,$fj, $c * cos($dir) ,  $c * sin($dir) );
	}
    }
}




sub WorldDistance # lat,lon,lat,lon   Returns geodesic dist' in metres.
{
    my $lat1 = shift;  my $lon1 = shift;    my $lat2 = shift;    my $lon2 = shift;
    my $R = 6371000;  # m
    my $deg = atan2(1,0)/90;

    my $x1 = $R * cos($lon1*$deg) * cos($lat1*$deg);
    my $y1 = $R * sin($lon1*$deg) * cos($lat1*$deg);
    my $z1 = $R * sin($lat1*$deg);
    my $x2 = $R * cos($lon2*$deg) * cos($lat2*$deg);
    my $y2 = $R * sin($lon2*$deg) * cos($lat2*$deg);
    my $z2 = $R * sin($lat2*$deg);
    my $mag1 = sqrt( $x1*$x1 + $y1*$y1 + $z1*$z1);
    my $mag2 = sqrt( $x2*$x2 + $y2*$y2 + $z2*$z2);
    my $dot = $x1*$x2 + $y1*$y2 + $z1*$z2;
    my $cang = $dot/($mag1*$mag2);
    if ($cang>1.0) {$cang=1.0;}
    my $ang = atan2(sqrt(1/($cang*$cang) -1),1);
    my $dist = $R*$ang;
    return($dist);
}


sub CartAngle # y,x    Returns 0...2*pi.
{
    my $y = shift;
    my $x = shift;
    $a = atan2($y,$x);
    if ($a<0) {$a = 4*atan2(1,0) + $a;}
    return($a);
}


    
sub WorldToGrid # Lat,Lon  returns i,j.
{
    my $Lat = shift;
    my $Lon = shift;
    my $j = int ($DemandFieldSize * ($Lat - $MinLat) / ($MaxLat - $MinLat) );
    my $i = int ($DemandFieldSize * ($Lon - $MinLon) / ($MaxLon - $MinLon) );
    #print "%MMMMLL  $MinLat $MaxLat $MinLon $MaxLon $Lat $Lon \n";	
    #print "%LLIJ: $Lat $Lon  $i  $j \n";
    my @gridcoords = ( $i , $j );
    return(@gridcoords);
}

#
# Basic PS functions.
#

sub ps_start
{
    print "%!PS-Adobe-2.0 EPSF-2.0\n";
    print "%%Creator: 03_Plot_Demandflow_Graph.pl\n";
    print "%%BoundingBox: 00 00 1100 1220\n";
    print "%%EndComments\n";
    #print "%PS\n";
    print "2 setlinejoin\n";
    ps_font(14);
}

sub ps_font  # (s)
{
    my $s = shift;
    print "/Times-Roman findfont " . int(0+$s) . " scalefont setfont\n";        
}

sub ps_end
{
    # print " showpage\n";  # No showpage for EPS.
}

sub ps_disc# (x,y,r)
{
    my $x = shift;
    my $y = shift;
    my $r = shift;
    my $g = 0.9; # g=(0.7+0.2*rand()) 
    $x = pp($x/1000*$figwid+$figx);
    $y = pp($y/1000*$fighgt+$figy);
    $r = pp($r/1000*$figwid);
    print $x . " " . $y . " " . $r . " " . 0 . " " . 360 . " arc closepath fill \n";
}


sub ps_line   # (x1,y1,x2,y2)   range 0...1000
{
    my $x1 = shift;
    my $y1 = shift;
    my $x2 = shift;
    my $y2 = shift;
    $x1 = pp($x1/1000*$figwid+$figx);
    $y1 = pp($y1/1000*$fighgt+$figy);
    $x2 = pp($x2/1000*$figwid+$figx);
    $y2 = pp($y2/1000*$fighgt+$figy);
    print " " . $x1 . " " . $y1 . " moveto " . $x2 . " " . $y2 . " lineto stroke \n";
    # 72 720 moveto (X) show  72 733 moveto  (0) show 
}

sub ps_coo # (x1,y1)
{
    my $x1 = shift;
    my $y1 = shift;
    $x1 = pp($x1/1000*$figwid+$figx);
    $y1 = pp($y1/1000*$fighgt+$figy);
    return(" " . $x1 . " " . $y1 . " ");
}

sub ps_quadra #(x1,y1,x2,y2,x3,y3,x4,y4)
{
    my $x1 = shift; my $y1 = shift; my $x2 = shift; my $y2 = shift; 
    my $x3 = shift; my $y3 = shift; my $x4 = shift; my $y4 = shift;
    print " newpath " . ps_coo($x1,$y1) . " moveto " . ps_coo($x2,$y2) . " lineto " . ps_coo($x3,$y3)
	. " lineto " . ps_coo($x4,$y4) . " lineto closepath fill \n"; 
}

sub ps_quadrauf #(x1,y1,x2,y2,x3,y3,x4,y4)
{
    my $x1 = shift; my $y1 = shift; my $x2 = shift; my $y2 = shift; 
    my $x3 = shift; my $y3 = shift; my $x4 = shift; my $y4 = shift;
    print " newpath " . ps_coo($x1,$y1) . " moveto " . ps_coo($x2,$y2) . " lineto " . ps_coo($x3,$y3)
	. " lineto " . ps_coo($x4,$y4) . " lineto closepath\n"; 
}

sub ps_text #(x,y,s)
{
    my $x = shift;
    my $y = shift;
    my $s = shift;
    $x = pp($x/1000*$figwid+$figx);
    $y = pp($y/1000*$fighgt+$figy);
    print " " . $x . " " . $y . " moveto (" . $s . ") show \n";
    # 72 720 moveto (X) show  72 733 moveto  (0) show 
}

sub ps_lw #(w)  # 1,2,3...
{
    my $w = int(shift);
    if ($w<1) {$w=1;}
    if ($w>5) {$w=5;}
    print " " . $w . " setlinewidth \n";
}

sub ps_col #(r,g,b)  # 0...1
{
    my $r = shift;
    my $g = shift;
    my $b = shift;
    $r=pp($r);$g=pp($g);$b=pp($b);
    if ($r<0) {$r=0;}    if ($r>1) {$r=1;}
    if ($g<0) {$g=0;}    if ($g>1) {$g=1;}
    if ($b<0) {$b=0;}    if ($b>1) {$b=1;}
    print " " . $r . " " . $g . " " . $b . " setrgbcolor \n";
}

sub pp #(x)
{
    my $x = shift;
    return(int($x * 1000)/1000);
}

sub Rainbow # (n)
# returns r,g,b 
{
    my $n = shift;
    my $r=$n*0.16 % 1;
    #g=n*0.05+0.5;
    my $g = 0.5+0.3*sin(0.5*$n);
    my $b = 0.5+0.5*sin($n);
    return( $r . "," . $g . "," . $b);
}


#
# Little functions
#
sub max
{
    my $a = shift;
    my $b = shift;
    return ( $a > $b ? $a : $b);
}
sub min
{
    my $a = shift;
    my $b = shift;
    return ( $a < $b ? $a : $b);
}
