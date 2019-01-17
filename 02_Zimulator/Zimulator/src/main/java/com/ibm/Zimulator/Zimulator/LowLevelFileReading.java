package com.ibm.Zimulator.Zimulator;
import java.util.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

/*
  This class contains routines specifically for reading in low-level plaintext 
  data, tailored for and used solely by InputParser().

  LowLevelFileReading() to set up reading.
  Read_Item() to get data.

  2018-08-13: Now handles String[] input as well as files. Constructor is now always expecting Iterator<String>.
*/

public class LowLevelFileReading
{
    Iterator<String> textsource;
    
    private int PositionInCurrentLine;
    private int[] CurrentLineFromFile;

    public LowLevelFileReading(Iterator<String> _textsource)
    {
	textsource = _textsource;
	PositionInCurrentLine=0;
	CurrentLineFromFile=null;
    }

    /*
      Main routine for reading in an 'item'. All reading done through this.
      Reads in the next piece of information; result is in 'Item' as a String.
      Resulting Item does not include any delimiter.
      Skips initial whitespace.

      We terminate on any delimiter in the set: whitespace } ] > < [ { =
      Delimiter will be an integer in ItemDel, and also returned: ' ' } ... or -1 for EoF.
      'Non-Right-side' delimiters will always be PASSED BY,
      but }]> will be passed by IFF item read is empty.

      In EoF case or if we see a delimiter before any non-WS, then Item=null.

      Suppose we read the last item in a line like: { a b c d e }
      When we read d, we will get ItemDel=' '.
      When we then read e, we will get ItemDel='}'.
    */
    public String Item;
    public int ItemDel;

    public String Last_Line_Read()
    {
	return(Str.setUni(CurrentLineFromFile));
    }    

    int Read_Item_Include_Equalsign()
    {
	return(Read_Item(false));
    }
    int Read_Item()
    {
	return(Read_Item(true));
    }
    int Read_Item(boolean StopOnEquals)
    {
	Read_WhiteSpace();
	FA_i UniLine = new FA_i();
	int ch,j=0;
	int WSF=0;
	while (0==0)
	    {
		ch=Next_UniPoint();
		if (ch==-1) {	ItemDel=-1;break; }
		if ( (ch=='<') ||(ch=='{') ||(ch=='[') ) {ItemDel=ch;break;}
		if (StopOnEquals &&  (ch=='=') ) {ItemDel=ch;break;}
		if ( (ch=='>') ||(ch=='}') ||(ch==']') )
		    {
			if (j>0) {Back_up_one_UniPoint();} // Do not pass by >}] unless it's all we've.
			ItemDel=ch;
			break;
		    }
		if (Is_WhiteSpace(ch)) {WSF=1;} // We saw whitespace after. We'll stop soon.
		else
		    {
			if (WSF==1)
			    {
				//We have already finished with terminating whitespace.
				if ( (ch=='<') ||(ch=='{') ||(ch=='[') ) {ItemDel=ch;break;}
				if (StopOnEquals && (ch=='=') ) {ItemDel=ch;break;}
				if ( (ch=='>') ||(ch=='}') ||(ch==']') )
				    {
					if (j>0) {Back_up_one_UniPoint();} // do not pass by >}]
					ItemDel=ch;
					break;
				    } // This is the real delimiter, not the WS.
				//We found something else: the WS was the delimiter.
				Back_up_one_UniPoint(); 
				ItemDel=' '; // The real delimiter was the WS.
				break;
			    }
			else
			    {
				UniLine.set(j,ch);
				j++;
			    }
		    }
	    }
	if (UniLine.length==0)
	    {
		Item=null;
	    }
	else
	    {
		Item = new String(UniLine.toArray(),0,UniLine.length);
	    }
	if (1==0)
	{
	    int[] ppp = new int[1];
	    ppp[0]=ItemDel;
	    Str.print("                ReadItem: '" + Item +"' , '" + Str.setUni(ppp) + "'");
	}
	return(ItemDel);
    }



    
    int Read_Next_NonWS_UniPoint()
    {
	Read_WhiteSpace();
	return(Next_UniPoint());
    }

    boolean Read_WhiteSpace()
    {
	boolean WasWS=false;
	int ch;
	ch=' ';
	while (Is_WhiteSpace(ch)) { WasWS=true; ch = Next_UniPoint(); }
	Back_up_one_UniPoint();
	return(WasWS);
    }
    
    boolean Is_WhiteSpace(int code)
    {
	if (code==' ') {return(true);}
	if (code=='\n') {return(true);}
	if (code=='\t') {return(true);}
	if (code=='\r') {return(true);}
	return(false);	
    }
    

    /* ===============================================================================
      Basic file reading; unicode points at a time, with the ability to back up one.
    */
    void Back_up_one_UniPoint()
    { // We can always back up one, except for the very first from the file.
	PositionInCurrentLine--;
    }
    int Next_UniPoint()
    /*
      Next unicode point; -1 for EoF.
     */
    {
	if (CurrentLineFromFile!=null)
	    {
		if (PositionInCurrentLine<CurrentLineFromFile.length)
		    {
			PositionInCurrentLine++;
			return(CurrentLineFromFile[PositionInCurrentLine-1]);
		    }
	    }

	if (!textsource.hasNext())
	    {
		CurrentLineFromFile = null;
		return(-1);
	    }		
	String lin = textsource.next();
	{
	    int n;
	    n=Str.index(lin,"#");
	    if (n!=-1)
		{
		    lin=Str.substr(lin,0,n);
		}
	}
	lin = lin + " "; // LF really was whitespace.
	CurrentLineFromFile = Str.getUni(lin);
	//Str.print("LINE: " + lin);
	PositionInCurrentLine=1;
	return(CurrentLineFromFile[PositionInCurrentLine-1]);
    }
}
