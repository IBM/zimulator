package com.ibm.Zimulator.Zimulator;

import java.io.*;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

/*
  Dump zsystem's zboxen to Postscript. Some points:
  
  > PS output with only built-in fonts requires no libraries.
  
  > Unfortunately, we can only support a few basic characters.
    {α-ω}, {Α-Ω}, Unicode zero-page apart from ().

  > Good for debugging; can see whole system on one page.
*/

/*
  zboxen should want to have ratio ϕ, of course.
 */

/*
  %!PS-Adobe-2.0 EPSF-2.0
  %%Creator: Report_to_ps.awk
  %%BoundingBox: 00 00 1600 900
  %%EndComments
  2 setlinejoin
  /Times-Roman findfont 14 scalefont setfont
  
  /Times-Roman findfont 18 scalefont setfont
  1 setlinewidth 
  0.8 0.8 0.8 setrgbcolor 
  50 90 moveto 1550 90 lineto stroke 
  0.5 0.5 0.5 setrgbcolor 
  50 90 moveto (0) show 
  0.8 0.8 0.8 setrgbcolor 
  50 130 moveto 1550 130 lineto stroke 
  0.5 0.5 0.5 setrgbcolor 
  50 130 moveto (10) show 
  0.8 0.8 0.8 setrgbcolor 
  50 170 moveto 1550 170 lineto stroke 
  0.5 0.5 0.5 setrgbcolor 
  50 170 moveto (20) show 

 */
public class PS_output
{
    public static void Dump(zsystem Ψ,String psfn)
    {
	FileOutputStream out_fos;
	try {out_fos = new FileOutputStream(psfn);}
	catch (FileNotFoundException FNF) {out_fos=null;}
	if (out_fos==null) {return;}
	{
	    Version V = new Version();
	    W(out_fos,"%!PS:); //-Adobe-2.0 EPSF-2.0");	
	    W(out_fos,"%%Creator: Zimulator "+V.V+">");
	    W(out_fos,"%%BoundingBox: 00 00 596 842");	
	    W(out_fos,"%%EndComments");
	    W(out_fos,"2 setlinejoin");

	    ArrayList<zbox> Uncontained = Ψ.System_Z_Lists.AllUnContainedZboxen();

       	    psbox P = new psbox(20,20,556,757,null);
	    P.Inside = GeneratePsboxen(P,Uncontained);

	    RenderPSB(P,out_fos,psfn,0);
	    
	    W(out_fos,"showpage");
	}

    }

    private static void W(FileOutputStream out_fos,String S)
    {
	S = S + "\n";
	try { out_fos.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem. 
    }

    public static void RenderPSB(psbox P,FileOutputStream out_fos,String psfn,int reclev)
    {
	//	if (reclev>2) {return;}
	Iterator<String> wis = P.PS(reclev).iterator();
	while (wis.hasNext()) {W(out_fos,wis.next());}
	if (P.Inside!=null)
	    {
		Iterator<psbox> wif = P.Inside.iterator();
		while (wif.hasNext()) {RenderPSB(wif.next(),out_fos,psfn,reclev+1);}
	    }
    }
	
    public static ArrayList<psbox> GeneratePsboxen(psbox parent,ArrayList<zbox> zboxen)
    /*
     */
    {
	ArrayList<psbox> PSB = new ArrayList<psbox>();
	int num = zboxen.size();
	double rat = 6.0;   // desired ratio. [.....]

        double pw,ph;
        pw=parent.wid;  
        double tt = parent.textsize();
        ph = parent.hgt - tt;
		
	double w,h; // size of internal one.
	int m,n;
	n = MLR.ftoi(Math.sqrt(ph/pw*num*rat));
	if (n==0) {n=1;}
        if (num==1)
	    {m=1;}
	else
	    {m = ((num-1)/n)+1;}
	if (m==0) {m=1;}
	//Maybe adjust n again :-D
	if (num!=0)
	    {
		n = ((num-1)/m)+1;
	    }
	h = ph / n;
        w = pw / m;
	double spc = 0.12 * h;
	//	if (spc>20) {spc=20;}
	//Str.print("num="+num+"  m,n="+m+","+n+"  w,h=" + w + "," + h);
	
	{
	    int i=0;
	    int j=0;
	    for (int k=0;k<num;k++)
		{
		    zbox φ = zboxen.get(k);
		    double x,y;   // lower left.
		    x=parent.x;y=parent.y;
		    psbox P = new psbox(x + spc/2 + (pw - spc) /m*i,
					y + parent.hgt - spc/2 - (ph - spc/2)/n*(j+1),
					w - spc,h - spc/2,
					φ);
		    // Now, recurse:
		    if (φ.Z!=null)
			{
			    ArrayList<zbox> ins = new ArrayList<zbox>();
			    Iterator<zbox> wif = φ.Z.iterator();
			    while (wif.hasNext()) { ins.add(wif.next()); }
			    P.Inside = GeneratePsboxen(P,ins);
			}
		    PSB.add(P);
		    i++;if (i==m) {i=0;j++;}
		}
	}
	return(PSB);
    }
    
}


class psbox
{
    double x,y,wid,hgt; //coord's
    ArrayList<String> Label;
    ArrayList<psbox> Inside;

    public double textsize()
    {
	double ts = hgt/4;
	if (ts>20) {ts=20;}
	return(ts);
    }
    
    public psbox(double _x,double _y,double _w,double _h,zbox φ)
    {
	x=_x;y=_y;wid=_w;hgt=_h;
	Label = new ArrayList<String>();
	if (φ!=null)
	    {
		String S = ZboxLabelToPSLabel(φ.Label);
		if (φ.Z!=null)
		    {
			S += " [" + φ.Z.size() + "]";
		    }
		Label.add(S);
		Label.add("[" + φ.e.A + "," + φ.e.n + "]");
		if (φ.Z!=null)
		    {
			Label.add("[" + φ.Z.size() + " zboxen]");
		    }
	    }
    }

    String ZboxLabelToPSLabel(String zbl)
    {
	// Poor-boy postscript. Just restrict to a subset of unicode page 0
	int[] u = Str.getUni(zbl);
	for (int j=0;j<u.length;j++)
	    {
		int ch = u[j];
		// !"#$&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~
		if ((ch=='(')||(ch==')')) {u[j]='_';continue;}
		if ((ch>=' ')&&(ch<'~')) {continue;}
		u[j]='_';
	    }
	return( Str.setUni(u));
    }
	
    public ArrayList<String> PS(int levelcol)
    { // levelcol >= 0
	ArrayList<String> ps = new ArrayList<String>();
	double lt = hgt/100;
	//if (lt<0.5) {lt=0.5;}
	if (lt>1) {lt=1;}
	{
	    double gg = 1.0 - 0.5/(levelcol+1);
	    ps.add(gg + " " +gg + " " +gg + " setrgbcolor 0 setlinewidth");	    
	    ps.add(x + " " + y + " moveto " + (x+wid) + " " + y + " lineto " +
		   (x+wid) + " " + (y+hgt) + " lineto " + x + " " + (y+hgt) + " lineto closepath fill ");

	}
	ps.add(lt + " setlinewidth");
	ps.add(" 0 0 0 setrgbcolor");	
	ps.add("/Times-Roman findfont " + textsize() + " scalefont setfont");
	//ps.add("0.8 0.8 0.8 setrgbcolor ");
	ps.add(x + " " + y + " moveto " + (x+wid) + " " + y + " lineto " +
	       (x+wid) + " " + (y+hgt) + " lineto " + x + " " + (y+hgt) + " lineto closepath stroke ");
	double sy = y+textsize()*0.1;

	if (0==0)
	    {
		if (Label!=null)
		    {
			if (Label.size()>0)
			    {
				//ps.add((x+lt*8) + " " + sy + " moveto (" + Str.substr(Label.get(0),0,10) + ") show");
				ps.add((x+lt*2) + " " + sy + " moveto (" + Label.get(0) + ") show");
			    }
		    }
	    }
	else
	    {
		Iterator<String> ll = Label.iterator();
		while (ll.hasNext())
		    {
			ps.add((x+lt*2) + " " + sy + " moveto (" + Str.substr(ll.next(),0,10) + ") show");
			sy += textsize();
		    }
	    }
	return(ps);
    }


	
}

