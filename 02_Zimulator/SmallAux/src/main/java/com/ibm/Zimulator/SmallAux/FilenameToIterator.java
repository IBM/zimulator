package com.ibm.Zimulator.SmallAux;

import java.io.*;
import java.util.*;

/*
  Converts a filename to a String iterator for reading in the whole file.
*/

public class FilenameToIterator implements Iterator<String>
{
    private BufferedReader f;
    private String L;    
    public FilenameToIterator(String Filename)
    {
	BufferedReader infile = null;	
	try {
	    infile = new BufferedReader(new FileReader(Filename));	    
	} catch (FileNotFoundException FNF)
	    {
		infile=null;
	    }
	f = infile;
	next();
    }
    public boolean hasNext()
    {
	return(L!=null);
    }
    public String next()
    {
	String s = L;
	L = readline();
	return(s);
    }
    public String readline()
    {
	if (f==null) {return(null);}
	String s;
	try {
	    s = f.readLine();
	    if (s==null)
		{
		    try {f.close();} catch (IOException IOEc) {} //whatever problem.
		    return(null);
		} //EOF
	} catch (IOException IOE3)
	    {
		try {f.close();} catch (IOException IOE) {} //whatever problem.
		return(null);
	    } //some problem.		
	return(s);
    }
}

