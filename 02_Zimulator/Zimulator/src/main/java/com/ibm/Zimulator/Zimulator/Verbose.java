
package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

class Verbose
{
    zio IO;
    public Verbose(zio _IO,boolean _colourflag)
    {
	IO=_IO;
	linbuf="";
	cf = _colourflag;
    }

    private String linbuf;
    private boolean cf;
    
    public void print(String S)
    {
	IO.sendVerboseLine(linbuf + S);
	linbuf="";
    }
    public void printn(String S)
    {
	linbuf = linbuf + S;
    }
    public void printrgb(String S,int r,int g,int b)
    {
	if (cf)
	    {
		IO.sendVerboseLine(linbuf + Str.composergb(S,r,g,b));
	    }
	else
	    {
		IO.sendVerboseLine(linbuf + S);
	    }
	linbuf="";
    }
    public void printnrgb(String S,int r,int g,int b)
    {
	if (cf)
	    {
		linbuf = linbuf + Str.composergb(S,r,g,b);
	    }
	else
	    {
		linbuf = linbuf + S;
	    }
    }
    public void printE(String S)
    {
	if (cf)
	    {
		IO.sendVerboseLine(linbuf + Str.composeE(S));
	    }
	else
	    {
		IO.sendVerboseLine(linbuf + S);
	    }
	linbuf="";	
    }
    public void printEn(String S)
    {
	if (cf)
	    {
		linbuf = linbuf + Str.composeE(S);
	    }
	else
	    {
		linbuf = linbuf + S;
	    }	
    }    
}
