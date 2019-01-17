package com.ibm.Zimulator.ZimCLI;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

import java.io.*;
/*
import java.io.DataOutputStream;
import java.io.InputStreamReader;
*/
import java.net.HttpURLConnection;
import java.net.URL;
//import javax.net.ssl.HttpsURLConnection;


class HttpLineIterator implements Iterator<String>
{
    private String HttpAdd;
    private String lin;
    private URL obj;
    private int responseCode;
    private BufferedReader inp_br;

    private final String USER_AGENT = "Zimulator";

    public HttpLineIterator(String _HttpAdd,Iterator<String> PostData) throws Exception
	/*
	  PostData can be null if there are no data to send.
	*/
	{
	    HttpAdd = _HttpAdd;	    
	    obj = new URL(HttpAdd);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();  // Could be https.	    
	    con.setRequestMethod("POST");
	    con.setRequestProperty("User-Agent", USER_AGENT);
	    //con.setRequestProperty("Accept-Language", "en-CA,en;q=0.5");
	    con.setDoOutput(true);
	    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	    if (PostData!=null)
		{
		    while (PostData.hasNext())
			{
			    wr.writeBytes(PostData.next() + "\n");  // stupidly, we need a CR in addition to LF for HTTP post body.
			}
		}
	    wr.flush();
	    wr.close();

	    responseCode = con.getResponseCode();
	    inp_br = new BufferedReader(new InputStreamReader(con.getInputStream()));

	    next(); // Start us off.
	}

    public String next()
    {
	String alin = lin;
	try {
	    lin = inp_br.readLine();
	} catch (IOException ioe)
	    {
		lin=null;
	    }
	if (lin==null)
	    {
		try {
		    inp_br.close();
		} catch (IOException ioe)
		    {
		    }
	    }
	return(alin);
    }
    
    public boolean hasNext()
    {
	return(lin!=null);
    }

}


