package com.ibm.Zimulator.ZimCLI;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

class FileOrHttpLineIterator
{
    public static Iterator<String> get(String fileoraddr)
    {
	boolean http=false;
	if (Str.equals(Str.tolower(Str.substr(fileoraddr,0,7)),"http://"))
	    {
		http=true;
	    }
	if (Str.equals(Str.tolower(Str.substr(fileoraddr,0,8)),"https://"))
	    {
		http=true;
	    }
	
	if (http)
	    {
		HttpLineIterator HTTPLI;
		try {
		    HTTPLI = new HttpLineIterator(fileoraddr,null);
		} catch (Exception anything)
		    {
			HTTPLI = null;
		    }
		return(HTTPLI);
	    }

	// Just a file:
	return(new FilenameToIterator(fileoraddr));
    }
}

