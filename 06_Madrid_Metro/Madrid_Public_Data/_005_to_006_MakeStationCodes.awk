
#1:LineNum,2:StopNum,3:StationName,4:SourceURL,5:Lat,6:Lon

BEGIN{
    FS=",";
    OFS=",";  
}
 {
     if ($2=="") {next;}
     print $1,$2,MakeTLC($3),$3,$4,$5,$6;
 }

function MakeTLC(name,   nama,noo,onn,tlc,orig)
{
    orig=name;
    if (CODE[orig]!="")
    {
	return(CODE[orig]);
    }
    name=toupper(name);
    gsub("AIUEO","",name);
    onn = name;gsub(" ","",onn);
    gsub("[ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞß]","",name);
    noo = split(name,nama," ");
    #print "# ",noo, name;
    gsub(" ","",name);
    if (noo<2)
    {
#	nama[2]=substr(name,2,3);	
    }
    if (noo<3)
    {
#	nama[3]=substr(name,3,3);	
    }
    tlc = substr(nama[1],1,1) substr(nama[2],1,1) substr(nama[3],1,1);
    if (length(tlc)!=3)
    {
	tlc = substr( tlc substr(onn,length(TLC)+1,3),1,3);
    }
    while ((length(tlc)!=3)||(TLCS[tlc]!=""))
    {
	c1 = int(1+rand()*length(onn));
	c2 = c1+int(1+rand()*length(onn));
	c3 = c2+int(1+rand()*length(onn));
	tlc = substr(onn,c1,1) substr(onn,c2,1) substr(onn,c3,1);
    }
    TLCS[tlc]=orig;
    CODE[orig]=tlc;
    return(tlc);
}
