# 16,1,CJC,Colonia Jardín,https://en.wikipedia.org/wiki/Colonia_Jard%C3%ADn_(Madrid_Metro),40.3969795,-3.7746114

BEGIN{
    FS=",";
    OFS=",";
}
 {
     if (substr($1,1,1)=="#") {next;}
     STAT[$3]=$3 "," $4 "," $6 "," $7 "," $5;
 }
END{
    for (s in STAT)
    {
	print STAT[s];
    }
}
