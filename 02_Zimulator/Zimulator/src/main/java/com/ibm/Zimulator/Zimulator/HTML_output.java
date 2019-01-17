package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

public class HTML_output
{
    public static void Dump(zsystem Ψ,String htmlfn)
    /*
      Dump zsystem's zboxen to HTML. Maybe this is a reasonable thing to do; maybe not.
     */
    {
	FileOutputStream out_fos;
	try {out_fos = new FileOutputStream(htmlfn);}
	catch (FileNotFoundException FNF) {out_fos=null;}
	if (out_fos==null) {return;}
	
	W(out_fos,"<html><head>");
	W(out_fos,"<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\">");
	W(out_fos,"</head><body>");

	ArrayList<zbox> Uncontained = Ψ.System_Z_Lists.AllUnContainedZboxen();
	int jlim = Uncontained.size();
	for (int j=0;j<jlim;j++)
            {
		DumpHTML(Ψ,Uncontained.get(j),0,out_fos,htmlfn);
	    }
	W(out_fos,"</body></html>");

	try {out_fos.close();} catch (IOException IOEc) {} //whatever problem.
	 
    }

    private static void W(FileOutputStream out_fos,String S)
    {
	S = S + "\n";
	try { out_fos.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem. 
    }

    private static void DumpHTML(zsystem Ψ,zbox φ,int i,FileOutputStream out_fos,String htmlfn)
    {
	String Plabel = "";
	if (φ.P!=null)
	    {
		Plabel = φ.P.Label;
		if ((Plabel==null)||Str.equals(Plabel,"")) {Plabel="[p]";}
		Plabel += "(" + φ.P.e.A + "," + φ.P.e.n +")";
	    }
	W(out_fos,"<table border=3 title=\"zbox:"+φ.Label+" - " + Plabel + "\">");
	W(out_fos,"<tr><td bgcolor=#aaaaaa>zbox:"+φ.Label+" - " + Plabel + "</td></tr>");
	String zlinks="";
	{
	    if (φ.zlinklist!=null)
		{
		    Iterator<zlink> wif = φ.zlinklist.iterator();
		    while (wif.hasNext())
			{
			    zlink χ = wif.next();
			    if (χ.μ == φ) { zlinks += " " + χ.ν.Label; }
			    else if (χ.ν == φ) { zlinks += " " + χ.μ.Label; }
			    else { zlinks += " !"; }
			    String I = " A:" + χ.concisetypelist();
			    zlinks += I +"<br>";
			}
		}
	    if (φ.e.χ!=null)
		{
		    zlinks += "χ_i:" + φ.e.χ.concisetypelist();
		}
	}
	W(out_fos,"<tr><td bgcolor=#bbbb55>zlinks:<i>"+zlinks+"</i></td></tr>");
	String zpaths="";
	{
	    HashSet<zpath> PathsThroughHere = Ψ.AllActivePathsThroughZbox(φ,0);
	    if (PathsThroughHere!=null)
		{
		    Iterator<zpath> pths = PathsThroughHere.iterator();
		    while (pths.hasNext())
			{				
			    zpath PathToConsider = pths.next();
			    String plabel = PathToConsider.Label;
			    if ((plabel==null)||Str.equals(plabel,"")) {plabel="[p]";}
			    zpaths += " " + plabel;
			    zpaths += "(" + PathToConsider.e.A + "," + PathToConsider.e.n +")";
			}
		}
	}
	W(out_fos,"<tr><td bgcolor=#5555ff>zpaths:<i>"+zpaths+"</i></td></tr>");	

	if (φ.Z!=null)
	    {
		int klim=φ.Z.size();
		if (klim>0)
		    {
			W(out_fos,"<tr>");
			if ((i&1)==0) {	W(out_fos,"<td valign=top>"); }
			Iterator<zbox> wif = φ.Z.iterator();
			while (wif.hasNext())
			    {
				zbox _zi = wif.next(); //φ.Z.Z_next;
				if ((i&1)==1) {	W(out_fos,"<td>"); }				
				DumpHTML(Ψ,_zi,i+1,out_fos,htmlfn);
				if ((i&1)==1) {	W(out_fos,"</td>"); }
			    }
			if ((i&1)==0) {	W(out_fos,"</td>"); }
			W(out_fos,"</tr>");			
		    }
	    }
	W(out_fos,"</table>");	
    }

    
}
