
#
# This is not a generic visualisation script; it is a very specific
# program designed to illustrate the results of the
# five-rooms-with-balls test.
#
 
#R: zbox ztype=Ball,2,1 t=27100.0 state=M label=BallProto/5 z.label=RoomEnd z.L=100 z.W=1 t0=27100.0 x0=0.0 t1=27100.0 x1=0.0 l=10 z.n=1 z.Z={ BallProto/5(21)} Z.n=0 K=RoomTour D="⤷ RoomEnd"

BEGIN{

    yscale=1000;
    
    BoxTransitions[0][1]=0;  # [b][s] List of transitions of the box 'b'; sequence s=1,2,3... room,x0,t0,x1,t1.

    #Containers[];   # number for each container name. 0,1,2.
    
    csize=150;  # size to plot each container.

    StartTime=000;
    EndTime=200;
    
    ps_start();

    # So we get order right.
    nc++;Containers["Room1"] = nc;
    nc++;Containers["Room2"] = nc;
    nc++;Containers["Room3"] = nc;
    nc++;Containers["Room4"] = nc;
    nc++;Containers["Room5"] = nc;
    
}

 {
     if ($1!="R:") {next;}
     lin=$0;
     label=GetParamValue(lin,"label",0);
     state=GetParamValue(lin,"state",0);
     cont=GetParamValue(lin,"z.label",0); #=Room1, Room2, ... RoomEnd
     contn=0+substr(cont,5,length(cont));

     print "% LABEL:" label,Ballnum;
     
     ztype=GetParamValue(lin,"ztype",0);
     if (substr(ztype,1,4)=="Ball")
     {
	 Ball_l=0+GetParamValue(lin,"l",0);
	 # A movement.
	 if ((state=="M")||(state=="D"))
	 {	     
	     t0=GetParamValue(lin,"t0",0);
	     t1=GetParamValue(lin,"t1",0);
	     x0=GetParamValue(lin,"x0",0);
	     x1=GetParamValue(lin,"x1",0);
	     p[label]++;
	     if (Containers[cont]=="") {nc++;Containers[cont]=nc;}
	     if (Boxen[label]=="")
	     {
		 nb++;
		 Boxen[label]=nb;
	     }
	     
	     BoxTransitions[label][p[label]] = cont "," x0 "," t0 "," x1 "," t1;
	     print "% " BoxTransitions[label][p[label]] " ContainerNum: " Containers[cont];
	 }
     }
 }

END{
    print "% Ball_l=" Ball_l;

    ps_font(18);
		    
    ps_lw(1);
    for (t=StartTime;t<EndTime;t=t+10)
    {
	ps_col(0.8,0.8,0.8);
	ps_line(0,ps_y(t),1000,ps_y(t));
	ps_col(0.5,0.5,0.5);
	ps_text(0,ps_y(t),t);
    }

    for (b in Containers)
    {
	cn=Containers[b];
	ps_col(1,0,0);
	ps_text(ps_x(0,cn),ps_y(StartTime-10),b);
	ps_line(ps_x(0,cn),ps_y(StartTime-10),ps_x(70,cn),ps_y(StartTime-10));	
	ps_col(1,0.5,0.5);
	ps_line(ps_x(0,cn),ps_y(StartTime-10),ps_x(0,cn),ps_y(EndTime));	
	ps_line(ps_x(70,cn),ps_y(StartTime-10),ps_x(70,cn),ps_y(EndTime));	
    }

    ps_font(10);    
    for (pass=0;pass<2;pass++)
    {
	for (b in Boxen)
	{
	    gr_boxinroom(b,pass);
	}
    }   
    ps_end();
	
}



#
# graphing functions.
#
function gr_boxinroom(box,pass,  e,lastt,noo,trana,cn,x0,t0,x1,t1,cc ,nx0,nx1,nt0,nt1,ncn )  # give box number.
{
    split(Rainbow(Boxen[box]),cc,",");
    print "%BOX: " box,Boxen[box];
    ps_lw(3);
    lastt="";
    for (e in BoxTransitions[box])
    {
	noo=split(BoxTransitions[box][e],trana,",");
	# container,x,t,dx,dt.
	if (noo==5)
	{
	    cn=Containers[trana[1]];  # container number. 0,1,2,3...
	    x0=0+trana[2];
	    t0=0+trana[3];
	    x1=0+trana[4];
	    t1=0+trana[5];	    

	    #ps_line(ps_x(x0,cn),ps_y(t0),ps_x(x1,cn),ps_y(t1));
	    #ps_line(ps_x(x0+Ball_l,cn),ps_y(t0),ps_x(x1+Ball_l,cn),ps_y(t1));
	    #ps_line(ps_x(x0,cn),ps_y(t0),ps_x(x0+Ball_l,cn),ps_y(t0));
	    #ps_line(ps_x(x1,cn),ps_y(t1),ps_x(x1+Ball_l,cn),ps_y(t1));

	    if (pass==0)
	    {		
		ps_col(sqrt(cc[1]),sqrt(cc[2]),sqrt(cc[3]));
		ps_quadrauf(ps_x(x0,cn),ps_y(t0), ps_x(x1,cn),ps_y(t1), ps_x(x1+Ball_l,cn),ps_y(t1), ps_x(x0+Ball_l,cn),ps_y(t0));
		print " gsave fill grestore ";
		ps_col(cc[1],cc[2],cc[3]);
		print " stroke ";
	    }
	    else
	    {
		if (t1!=t0)
		{
		    ps_col(0,0,0);
		    vel=(x1-x0)/(t1-t0);
		    ps_text(ps_x(x0,cn),ps_y(t0),x0 "," t0);
		    if (x0==0) {ps_text(ps_x(x0,cn),ps_y(t0)-10,box);}
		    #"[" vel "]");
		}
	    }
	    # When is the next time for our box?
	    noo=split(BoxTransitions[box][e+1],trana,",");
	    # container,x,t,dx,dt.
	    if (noo==5)
	    {
		ncn=Containers[trana[1]];  # container number. 0,1,2,3...
		nx0=0+trana[2];
		nt0=0+trana[3];
		nx1=0+trana[4];
		nt1=0+trana[5];	    

		{
		    if (pass==0)
		    {
			ps_col(sqrt(cc[1]),sqrt(cc[2]),sqrt(cc[3]));
			ps_quadrauf(ps_x(x1,cn),ps_y(t1), ps_x(x1+Ball_l,cn),ps_y(t1), ps_x(x1+Ball_l,cn),ps_y(nt0), ps_x(x1,cn),ps_y(nt0));
			print " gsave fill grestore ";
			ps_col(cc[1],cc[2],cc[3]);
			print " stroke ";
		    }
		    else
		    {
			ps_col(0,0,0);
			ps_text(ps_x(x1,cn),ps_y(t1),x1 "," t1);
		    }
		}
	    }

	}
    }
}


function ps_y(t)
{
    return(  (t-StartTime)/(EndTime-StartTime)*yscale + 50 );
}
function ps_x(x,cn)
{
    return(  (cn-1)*csize + x/90*csize + 50 );
}

#
# Basic PS functions.
#

function ps_start()
{
    print "%!PS-Adobe-2.0 EPSF-2.0";
    print "%%Creator: Report_to_ps.awk";
    print "%%BoundingBox: 00 00 1600 1000";
    print "%%EndComments";
    #print "%PS";
    print "2 setlinejoin";
    ps_font(14);
};

function ps_font(s)
{
    print "/Times-Roman findfont " int(0+s) " scalefont setfont";        
}

function ps_end()
{
    # print " showpage";  # No showpage for EPS.
};

function ps_line(x1,y1,x2,y2)  # range 0...1000
{
    x1=pp(x1/1000*1500+50);
    y1=pp(y1/1000*800+50);
    x2=pp(x2/1000*1500+50);
    y2=pp(y2/1000*800+50);
    print " " x1 " " y1 " moveto " x2 " " y2 " lineto stroke ";
    # 72 720 moveto (X) show  72 733 moveto  (0) show 
};

function ps_coo(x1,y1)
{
    x1=pp(x1/1000*1500+50);
    y1=pp(y1/1000*800+50);
    return(" " x1 " " y1 " ");
};

function ps_quadra(x1,y1,x2,y2,x3,y3,x4,y4)
{
    print " newpath " ps_coo(x1,y1) " moveto " ps_coo(x2,y2) " lineto " ps_coo(x3,y3) " lineto " ps_coo(x4,y4) " lineto closepath fill "; 
}

function ps_quadrauf(x1,y1,x2,y2,x3,y3,x4,y4)
{
    print " newpath " ps_coo(x1,y1) " moveto " ps_coo(x2,y2) " lineto " ps_coo(x3,y3) " lineto " ps_coo(x4,y4) " lineto closepath"; 
}


function ps_text(x,y,s)
{
    x=pp(x/1000*1500+50);
    y=pp(y/1000*800+50);
    print " " x " " y " moveto (" s ") show ";
    # 72 720 moveto (X) show  72 733 moveto  (0) show 
};
function ps_lw(w)  # 1,2,3...
{
    w=int(w);
    if (w<1) {w=1;}
    if (w>5) {w=5;}
    print " " w " setlinewidth ";
};
function ps_col(r,g,b)  # 0...1
{
    r=pp(r);g=pp(g);b=pp(b);
    if (r<0) {r=0;}    if (r>1) {r=1;}
    if (g<0) {g=0;}    if (g>1) {g=1;}
    if (b<0) {b=0;}    if (b>1) {b=1;}
    print " " r " " g " " b " setrgbcolor ";
};


function pp(x)
{
    return(int(x * 1000)/1000);
};

function Rainbow(n,    r,g,b)
# returns r,g,b 
{
    r=n*0.16 % 1;
    #g=n*0.05+0.5;
    g=0.5+0.3*sin(0.5*n);
    b=0.5+0.5*sin(n);
    return( r "," g "," b);

    
    if (n==1) {return("1,0,0");}
    if (n==2) {return("0.9,0.3,0");}
    if (n==3) {return("0.9,0.8,0");}
    if (n==4) {return("0,0.8,0");}
    if (n==5) {return("0.2,0.2,1");}
    if (n==6) {return("0,0.5,1");}
    if (n==7) {return("0,1,1");}
    if (n>7)
    {
	return("1" Rainbow(n -6));
    }
    return(".5,.,5.,5");
}

function GetParamValue(allparams,param,qf,    a,val)
# Extracts a parameter value from a space-delimited string of the form:
# param=value param=value ... param=value ...
# Does not do any unencoding. 
# Allows values to be quoted in "" if qf=1 (and does not return the "")
{
    allparams=" " allparams;
    a=index(allparams," " param "=");
    if (a==0) {return("");}
    val=substr(allparams,a+2+length(param),length(allparams));

    if ((qf==1)&&(substr(val,1,1)=="\""))
    {
	val=substr(val,2,length(val));
	a=index(val,"\"");
    }
    else
    {
	a=index(val," ");
    }
    if (a!=0) {val=substr(val,1,a-1);}
    return(val);
};
