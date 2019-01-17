package com.ibm.Zimulator.SmallAux;

import com.ibm.Zimulator.SmallAux.FA.*;

//pjsec Strings
/*
  static class str

#  Functions for manipulation of Strings.

## Characters and fields

  Characters here are _not_ java 'char' objects; they are distinct unicode poins.

  Character position in strings starts at 0.  
   This is **different** from gawk, where character position starts at 1.
  N = Str.length(S) gives number, so the characters are at positions 0...(N-1)

  If something is split using split() or read using getField(), then FIELD NUMBERS are used.
  FIELD NUMBERS START AT 1, if something is split() or splitCSV(). 
  (This is the SAME as in gawk.)
  If numFields(S) returns N, then split(S) produces an array with the fields at
  indices 1...N. Index 0 will be null. The length of the array will be N+1.

  If you need the array of FIELDS to start at zero (say, for making a pointlist) 
  functions like split0() are provided.

  Strings should only be manipulated:
  * With the Str.***() functions. Also Str.print*() for printing to terminal.
  * WITH + to join them together.
  * Strings can ONLY be compared with Str.equals(S1,S2) and fuzzyEquals(S1,S2). 
  ! Do not try to compare String values with the '==' operator.
  
## Why String routines? Why not use Java built-in things?

 The String datumtype is not totally broken in Java, but it is stupidly UTF-16 internally, and uses surrogate pairs.
 Methods within the String 'class' do NOT deal with these pairs; unicode points like 𩗩 = $295E9 and 🤔 cannot be used.
  Thus:
  * String type can still be used, but methods like substr() are screwy and broken.
  * 'char' type CANNOT be used; it is totally erroneous. There is NO SUCH THING as a "16-bit unicode character"
  * Thus, we provide a class of methods to deal with Strings, working with unicode points.

  Operations which can be trusted for String type:
  * Assignment. String hello="Hello!";
  * System.out.printXXX seem to work fine for Strings.
  * Concatenation using + operator

  Operations which cannot be trusted:
  * Anything using char type.
  * Internal string methods. substr etc.

! COMPARISON CAN ONLY BE DONE WITH 'Str.equals()' or 'Str.compare()' below. The '==' operator will NOT work.

## Anyway, Java might switch to UTF-8 internally.

The way Strings are stored internally is not relevant.
In the below, it is totally masked from the user, and we can convert if need be to: 
*  utf8    byte[]  of utf-8 encoding of single unicode points.
*  uni     int[]   of single unicode points.
*  String   Happens to be utf-16 with Surrogate Pairs, but this is private.

*/


//pjpref Str
public class Str
{
    public static String encspace(String S)
    /*
       Encode away whitespace, using URL notation (for storage or whatever).
       %-encodes tab, + and % to %xx where xx is hex.
       Replaces spaces by +.

       encspace("Wally was here.") will return "Wally+was+here."
    */
    {
	//SLOW:
	S=gsub("%","%25",S);
	S=gsub("\\+","%2b",S);
	S=gsub("\t","%09",S);
	S=gsub("\n","%0a",S);
	S=gsub(" ","+",S);
	return(S);
    };
    

    public static String copyXXX(String S)
    { // Never need this, since strings are immutable.
	if (S==null) {return(null);}
	return(uni_to_string(string_to_uni(S)));
    }
    

    public static String intdig(int number,int digits)
    /*
      Returns a string for the given integer, like itos(), but with some zeroes in front.
      
      intdig("53",4) will return "0053"
    */
    { // digits up to 20 or so.
	String numus = "000000000000000000000000000000" + number;
	numus = Str.substr(numus,Str.length(numus) - digits,digits);
	return(numus);
    }

    public static String intdigk(int number,int digits)
    /*
      Returns a string for the given integer, like itos(), but with some spaces in front.
      
      intdig("53",5) will return "   53"
    */
    { // digits up to 20 or so.
	String numus = "                                   " + number;
	numus = Str.substr(numus,Str.length(numus) - digits,digits);
	return(numus);
    }

    public static boolean IsCommentLine(String lin)
    {
	if (Str.equals(Str.substr(lin,0,1),"#")) {return(true);}
	return(false);
    }
    
    public static int length(String S)
    /*
      Returns the number of "letters" or "characters" in a string (actually, the number of unicode points).
      
      This is the ONLY notion of String length which should be utilised.
      Str.length("Wally") will return 5
    */
    {
	if (S==null) {return(0);}
	return(string_to_uni(S).length);
    }

    private static int getOneUni(String S)
    {
	int[] v = getUni(S);
	if (v.length==0) {return(0);}
	return(v[0]);
    }

    public static int[] getUni(String S)
    /*
      Given a string, return an array of real unicode points.
      (No garbage java surrogate pairs.)
     */
    {
	if (S==null) {return(new int[0]);}
	return(string_to_uni(S));
    }

    public static String setUni(int [] Uni)
    /*
      Given an array of unicode points, build a string.
      Careful using this with combining characters; you an make nonsensical things.
     */
    {	
	if (Uni==null) {return("");}
	if (Uni.length==0) {return("");}
	return(uni_to_string(Uni));
    }

    public static int uni1(String s)
    /*
      Returns a single unicode point; the first found in s.
      if s is null, returns 0.
     */
    {
	if (s==null) {return(0);}
	return(string_to_uni(s)[0]);
    }
    
    public static String setUni(int Uni)
    {	
	if (Uni==0) {return("");}
	int[] uni = new int[1];
	uni[0]=Uni;
	return(uni_to_string(uni));
    }

    public static String setUtf8(byte [] U8)
    {	
	if (U8==null) {return("");}
	if (U8.length==0) {return("");}
	return(uni_to_string(utf8_to_uni(U8)));
    }

    public static byte[] getUtf8(String S) // returns utf-8.
    {
	if (S==null) {return(new byte[0]);}
	return(uni_to_utf8(string_to_uni(S),0,-1));
    }
    
    /*
      Manipulation functions.
     */

    public static String between(String S,String left,String right)
    /*
      Returns a string found between left and right delimiters.

      Str.between("Here is the best one.","the","one")  will return " best ".
     */
    {
	if (S==null) {return("");}
	int l = index(S,left);
	if (l==-1) {return("");}
	String bet = substr(S,l+length(left),-1);
	l = index(bet,right);
	if (l==-1) {return("");}
	bet = substr(bet,0,l);
	return(bet);
    }
    
    public static int[] indices(String S,String srch)
    /*
      Returns indices of all occurences of String srch in string S.
      I = Str.indices("Wally","e")
      will return: I[0]=1 I[1]=3
      Indices start at 0, like index().
     */
    {
	int sl=Str.length(srch);
	int i=0;
	int n=0;
	while (0==0)
	    {
		i=index(S,srch,i);
		if (i==-1) {break;}
		n++;
		i=i+sl;
	    }
	int[] I = new int[n];
	i=0;n=0;
	while (0==0)
	    {
		i=index(S,srch,i);
		if (i==-1) {break;}
		I[n++]=i;
		i=i+sl;
	    }
	return(I);
    }

    public static int index(String S,String srch)
    /*
      Returns index of string srch in string S, or -1 if it does not appear.
      Index starts at 0.
      
      Str.index("House","s") will return 3
     */
    {
	return(index(S,srch,0));
    }
    
    public static int index(String S,String srch,int ss)
    /*
      Returns index of string srch in string S, or -1 if it does not appear.
      Index starts at 0.
      Search only begins at index ss in string S.

      index("Generator","e",3) will return -1.
      index("Generator","e",2) will return 3.
     */
    {
	if ((S==null)||(srch==null)) {return(-1);}
	int i,j,s,l;
	j=0;s=ss;
	int uni[] = string_to_uni(S);
	int srh[] = string_to_uni(srch);
	for (i=ss;i<uni.length;i++)
	    {
		if (uni[i]==srh[j])
		    {
			j++;
			if (j==srh.length)
			    {
				return(s);
			    }
		    }
		else
		    {
			j=0;s=i+1;
		    }
	    }
	return(-1);
    };

    
    public static String removeLTWS(String S)
    /*
      Removes leading and trailing whitespace; tabs and spaces.

      Str.removeLTWS(" 35 h Wally    ") will return "35 h Wally".
    */
    {
	if (S==null) {return("");}
	int uni[] = string_to_uni(S);
	int l=uni.length;
	if (l==0) {return("");}
	int i=0;
	while ((i<l) && uni_is_WS(uni[i])) {i++;}
	int j=l-1;
	while ((j>=0) && uni_is_WS(uni[j])) {j--;}
	int nl;
	if (i<=j) {nl = j-i+1;} else {nl=0;}
	return(uni_to_string(uni,i,nl));
    }
    public static String removeQuotes(String S)
    /*
      Removes '' or "" around something.
      Str.removeQuotes("\"Hello there\"") will return "Hello there".
      Str.removeQuotes("'Can't you see?'") will return "Can't you see?".
      Str.removeQuotes("Try again.") will return "Try again."
     */
    {
	if (S==null) {return("");}
	int uni[] = string_to_uni(S);
	int l=uni.length;
	if (l<2) {return(S);}
	if ((uni[0]=='\'')&&(uni[l-1]=='\'')) {return(uni_to_string(uni,1,l-2));}
	if ((uni[0]=='"')&&(uni[l-1]=='"')) {return(uni_to_string(uni,1,l-2));}
	return(S);
    }

    public static String substr(String S,int pos,int len)
    /*
      Return a portion of this String.
      Can specify len=-1 to go to until end.
      Position begins at 0.

      Str.substr("Wally",1,3) will return "ete".
    */
    {  //   Note that this is different from the way java substr has it (start,end).
	if (S==null) {return("");}
	int uni[] = string_to_uni(S);
	return(uni_to_string(uni,pos,len));
    }

    private static boolean uni_is_WS(int code)
    { // all whitespace codes are one byte; use for UNI or a single UTF8 byte.
	//code=code & 0xff;  // THIS WAS A SERIOUS MISTAKE. SHOULD NOT BE HERE.
	if (code==' ') {return(true);}
	if (code=='\n') {return(true);}
	if (code=='\t') {return(true);}	
	if (code=='\r') {return(true);}
	return(false);
    }

    private static boolean uni_is_digit(int code)
    { // all whitespace codes are one byte; use for UNI or UTF8.
	//code=code & 0xff;  // THIS WAS A SERIOUS MISTAKE. SHOULD NOT BE HERE.
	if (code<'0') {return(false);}
	if (code>'9') {return(false);}
	return(true);
    }

    public static int indexFirstDigit(String S)
    /*
      Returns the index of the first numerical digit in the string.
      
      Str.indexFirstDigit("Hello65 is good") will return 5.
     */
    {
	if (S==null) {return(-1);}
	int uni[] = string_to_uni(S);	
	if (uni.length==0) {return(-1);}
	int k;
	for (k=0;k<uni.length;k++)
	    {
		if (uni_is_digit(uni[k])) {return(k);}
	    }
	return(-1);
    }
    
    public static String untilDigit(String S)
    /*
      Returns a string form the start until just before the first numerical digit.
      
      Str.untilDigit("Hello65") will return "Hello".
      Str.untilDigit("Hello") will return "Hello".
     */
    {
	if (S==null) {return("");}
	int k = indexFirstDigit(S);
	if (k==-1) {return(S);}  // Strings are immutable. substr(S,0,-1));}
	return(substr(S,0,k));
    }
    
    public static String fromFirstDigit(String S)
    /*
      Returns a string which goes from the first numerical digit onwards to the end.
      
      Str.fromFirstDigit("Hello67 There") will return "67 There".
     */
    {
	if (S==null) {return("");}
	int k =indexFirstDigit(S);
	if (k==-1) {return("");}
	return(substr(S,k,-1));
    }

    public static String field(String S,int f)
    /*
      Fields are numbered from 1 and are separated by whitespace.
      This returns field number f.
      
      Str.field("This is a set of 7 fields.",4) will return "set".
     */
    {
	if (S==null) {return("");}
	return(getField(S,f));
    }

    public static String removeWS(String S)
    /*
      Returns a new String with all whitespace removed.

      Str.removeWS("These are some fields.") will return "Thesearesomefields.".
    */
    {
	if (S==null) {return("");}
	return(removeWhiteSpace(S));
    }

    public static String nonThingsToRepl(String S,String things,String repl)
    /*
      Returns a new String with all single characters NOT appearing in 'things' converted to 'repl' each.

      Str.nonThingsToRepl("Wally.","et","w") will return "weteww".
     */
    {
	if (S==null) {return("");}
	int j;
	String ch;
	String out="";
	int ls=length(S);
	for (j=0;j<ls;j++)
	    {
		ch=substr(S,j,1);
		if (index(things,ch)==-1)
		    {
			ch=repl;
		    }
		out=out + ch;
	    }	
	return(out);
    }

    public static String thingsToRepl(String S,String things,String repl)
    /*
      Returns a new String with all single characters appearing in 'things' converted to 'repl' each.
      
      Str.thingsToRepl("Wally.","et","wa") will return "Pwawawar."
     */
    {
	if (S==null) {return("");}
	int j;
	String ch;
	String out="";
	int ls=length(S);
	for (j=0;j<ls;j++)
	    {
		ch=substr(S,j,1);
		if (index(things,ch)!=-1)
		    {
			ch=repl;
		    }
		out=out + ch;
	    }	
	return(out);
    }

    public static String nonDigitsToWS(String S)
    /*
      Returns a new String with all non-numerical characters converted to space.

      This could be done [slightly slower] with Str.nonThingsToRepl(S,"0123456789"," ");
     */
    {
	if (S==null) {return("");}
	int uni[] = string_to_uni(S);
	int newuni[] = new int[uni.length];
	int k;
	for (k=0;k<uni.length;k++)
	    {
		if (uni_is_digit(uni[k])) 
		    {
			newuni[k]=uni[k];
		    }
		else
		    {
			newuni[k]=' ';
		    }
	    }
	return(uni_to_string(newuni));
    }
    
    public static String removeWhiteSpace(String S)
    /*
      Removes all whitespace from the string.
    */
    {
	if (S==null) {return("");}
	return(replace(replace(S," ",""),"\t",""));
    }

    public static String multiReplace(String S,String find_fields,String repl_fields)
    /*
      Provide things to find, and things to replace them with respectively.
      
      Str.multiReplace("Hello to the chicken and the cow.","chicken cow","dog wiener") 
      will return "Hello to the dog and the wiener.".

      The function simply makes arrays and calls the array version of multiReplace(), like this:
      
      multiReplace(S,split0(find_fields),split0(repl_fields))
    */
    {
	return(multiReplace(S,split0(find_fields),split0(repl_fields)));
    }

    public static String multiReplace(String S,String[] finds,String[] repls)
    /*
      Provide an array of things to find, and another of things to replace them with respectively.
     */
    {
	int n=Math.min(finds.length,repls.length);
	int j;
	for (j=0;j<n;j++)
	    {
		S = replace(S,finds[j],repls[j]);
	    }
	return(S);
    }

    public static String circleDigits(String S)
    /*
      Str.circleDigits("Try 1234-5678.") will return "Try ①②③④-⑤⑥⑦⑧.".
    */
    {
	return(multiReplace(S,"0 1 2 3 4 5 6 7 8 9","⓪ ① ② ③ ④ ⑤ ⑥ ⑦ ⑧ ⑨ "));
    }
    public static String kanjiDigits(String S)
    /*
      Str.kanjiDigits("Try 1234-5678.") will return "Try 一二三四-五六七八.".
    */
    {
	return(multiReplace(S,"0 1 2 3 4 5 6 7 8 9","零 一 二 三 四 五 六 七 八 九"));
    }
    public static String kanjiCircleDigits(String S)
    /* 
       Str.kanjiCircleDigits("Try 1234-5678.") will return "Try ㊀㊁㊂㊃-㊄㊅㊆㊇.".
    */
    {
	return(multiReplace(S,"0 1 2 3 4 5 6 7 8 9","㋺ ㊀ ㊁ ㊂ ㊃ ㊄ ㊅ ㊆ ㊇ ㊈"));
    }
    public static String circleLetters(String S)
    /*
      Str.circleLetters("Try 1234-5678.") will return "Ⓣⓡⓨ 1234-5678.".
    */
    {
	return(multiReplace(S,
			    "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z a b c d e f g h i j k l m n o p q r s t u v w x y z",
			    "Ⓐ Ⓑ Ⓒ Ⓓ Ⓔ Ⓕ Ⓖ Ⓗ Ⓘ Ⓙ Ⓚ Ⓛ Ⓜ Ⓝ Ⓞ Ⓟ Ⓠ Ⓡ Ⓢ Ⓣ Ⓤ Ⓥ Ⓦ Ⓧ Ⓨ Ⓩ ⓐ ⓑ ⓒ ⓓ ⓔ ⓕ ⓖ ⓗ ⓘ ⓙ ⓚ ⓛ ⓜ ⓝ ⓞ ⓟ ⓠ ⓡ ⓢ ⓣ ⓤ ⓥ ⓦ ⓧ ⓨ ⓩ"));
    }
    
    public static String gsub(String find,String repl,String S)
    /*
      Returns a new String with the replacement done. 
      Works like gsub() in gawk. Take care with the order of the arguments! 

      In Gawk:  gsub(find,repl,S);
      In Pinjo: S = Str.gsub(find,repl,S);
      
      Compare with Str.replace().

    */
    {
	if (S==null) {return("");}
	return(replace(find,repl,S));
    }

    public static String replace(String S,String find,String repl)
    /*
      Returns a new String with the replacement done. 
      Like gsub() in gawk, but notice the order of the arguments!

      In Gawk:  gsub(find,repl,S);
      In Pinjo: S = Str.replace(S,find,repl);

      Compare with Str.gsub().
    */
    { // SLOW. Should switch to Uni, do it, and switch back. Optimise later.
	if (S==null) {return("");}
	if (find==null) {return(S);}
	if (repl==null) {repl="";}
	int fl;
	fl = length(find);
	while (0==0)
	    {
		int j;
		j=index(S,find);
		if (j<0) {break;}
		S = substr(S,0,j) + repl + substr(S,j+fl,-1);
	    }
	return(S);
    }

    public static boolean equals(String S1,String S2)
    /*
      Used to compare strings exactly. For fuzzy comparison, see fuzzyEquals().

      ( Str.equals(name,"dan") )  evaluates to 'true' or 'false'
      
      == cannot be used. Careful! (name=="dan") evaluates to 'true' or false', 
      but does NOT compare the Strings.

     */
    {
	if ((S1==null)&&(S2==null)) {return(true);}
	if ((S1==null)||(S2==null)) {return(false);}
	return(S1.equals(S2));
	/*
	int uni1[] = string_to_uni(S1);
	int uni2[] = string_to_uni(S2);
	if (uni1.length != uni2.length) {return(false);}
	int j;
	for (j=0;j<uni1.length;j++)
	    {
		if (uni1[j]!=uni2[j]) {return(false);}
	    }
	return(true);
	*/
    }

    /*
      Silly generalish toupper() and tolower() ingredients.
     */

    private static int rangeonechar(int ch,String lo,String hi,String sh,int d)
    {
	return(rangeonechar(ch,getOneUni(lo),getOneUni(hi),getOneUni(sh),d));
    }
    private static int rangeEvenchar(int ch,String lo,String hi,String sh,int d)
    {
	return(rangeEvenchar(ch,getOneUni(lo),getOneUni(hi),getOneUni(sh),d));
    }
    private static int rangeonechar(int ch,int lo,int hi,int sh,int d)
    {
	if (d==1)
	    {
		if ((ch>=lo)&&(ch<=hi))
		    {
			ch=ch-lo+sh;
		    }
	    }
	else
	    {
		if ((ch>=sh)&&(ch<=(hi-lo+sh)))
		    {
			ch=ch-sh+lo;
		    }
	    }
	return(ch);
    };
    private static int rangeEvenchar(int ch,int lo,int hi,int sh,int d)
    { // for, say, ĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥ
	if (d==1)
	    {
		if ((ch>=lo)&&(ch<=hi))
		    {
			if (((ch-lo) % 2) == 0)
			    {
				ch=ch-lo+sh;
			    }
		    }
	    }
	else
	    {
		if ((ch>=sh)&&(ch<=(hi-lo+sh)))
		    {
			if (((ch-sh) % 2) == 0)
			    {
				ch=ch-sh+lo;
			    }
		    }
	    }
	return(ch);
    }
    
    public static String tolower(String S)
    /*
      Similar to gawk tolower().

      Returns a lowercase version of the string.
      Works with Roman letters, accented or not, 
      greek, cyrillic, and a few other cased alphabets.
      Not really debugged; should work with common letters though.

      Str.tolower("Wally says ΑΒΓΔ is a valid variable NAME.") will return "peter says αβγδ is a valid variable name.".
     */
    {
	return(changecase(S,1));
    }
    public static String toupper(String S)
    /*
      Similar to gawk toupper().

      Returns an uppercase version of the string.
      Works with Roman letters, accented or not, 
      greek, cyrillic, and a few other cased alphabets.
      Not really debugged; should work with common letters though.

      Str.toupper("Wally says ΑΒΓΔ is a valid variable NAME.") will return "PETER SAYS ΑΒΓΔ IS A VALID VARIABLE NAME.".
     */
    {
	return(changecase(S,0));
    }

    private static String changecase(String S,int x)
    {
	if ((S==null)&&(S==null)) {return("");}
	int uni[] = string_to_uni(S);
	int j;
	for (j=0;j<uni.length;j++)
	    {
		uni[j]= rangeonechar(uni[j],'A','Z','a',x);
		uni[j]= rangeonechar(uni[j],"À","Ö","à",x);
		uni[j]= rangeonechar(uni[j],"Ø","Þ","ø",x);
		uni[j]= rangeEvenchar(uni[j],"Ā","Ž","ā",x);
		uni[j]= rangeEvenchar(uni[j],"Ǎ","Ǿ","ǎ",x);
		uni[j]= rangeEvenchar(uni[j],"Ȁ","Ȳ","ȁ",x);
		uni[j]= rangeonechar(uni[j],"Ά","Ω","ά",x);
		uni[j]= rangeEvenchar(uni[j],"Ϙ","Ϯ","ϙ",x);
		uni[j]= rangeonechar(uni[j],"А","Я","а",x);
		uni[j]= rangeonechar(uni[j],"Ѐ","Џ","џ",x);
		uni[j]= rangeEvenchar(uni[j],"Ѡ","Ӿ","ѡ",x);
		uni[j]= rangeEvenchar(uni[j],"Ԁ","Ԧ","ԁ",x);
	    }
	return(uni_to_string(uni));
    }


    public static int getFieldIndex(String S, int m) 
    /*
      Returns position of field m in the string S, or -1 if no field m.
      Fields are whitespace-separated.
      As usual, field numbers start at m=1, and indices into the string start at 0.

      Str.getFieldIndex("Chickens cross this 66 road.",2) will return 9.
     */
    {
	if (S==null) {return(-1);}
	int uni[] = string_to_uni(S);
	int j;
	int fnum=0;
	boolean lw=true;
	for (j=0;j<uni.length;j++)
	    {
		if ((!uni_is_WS(uni[j])) && lw )
		    { // rising edge.
			fnum++;
			if (fnum==m)
			    {
				return(j);
			    }
		    }
		lw=uni_is_WS(uni[j]);
	    }
	return(-1);
    }
 
    public static String getField(String S, int m) 
    /*
      Just like field().
      Returns field m from the string S.
      Fields are whitespace-separated.
      The first field is m=1.

      Str.getField("Chickens cross this 66 road.",2) will return "cross".

      Note that to access many fields, it is much faster to use some form of split().
     */
    {
	if (S==null) {return("");}
	int uni[] = string_to_uni(S);
	int j;
	int fnum=0;
	int fsj=0;
	boolean lw=true;
	for (j=0;j<uni.length;j++)
	    {
		if ((!uni_is_WS(uni[j])) && lw )
		    { // rising edge.
			fnum++;
			if (fnum==m)
			    {
				fsj=j;
			    }
		    }
		if ((uni_is_WS(uni[j]))&& (!lw) )
		    { // falling edge.
			if (fnum==m)
			    {
				return(uni_to_string(uni,fsj,j-fsj));
			    }
		    }
		lw=uni_is_WS(uni[j]);
	    }
	if (fnum==m)
	    {
		return(uni_to_string(uni,fsj,j-fsj));
	    }
	return("");
    }

    public static int numFieldsXXX(String S,int Ch)
    {
    /*
      Returns the number of fields in the string S,
      separated by a SINGLE CHARACTER Ch.
      Fields are numbered 1...N, so this would return N.

      Str.numFields("Chickens cross this 66 road.","s") will return 5, since there are four 's' in the string.

      Note that Ch is not a String. For example:
      numFields(Inputline,'%');
     */
	if (S==null) {return(0);}
	int uni[] = string_to_uni(S);
	int j;
	int fnum=0;
	for (j=0;j<uni.length;j++)
	    {
		if (uni[j]==Ch) {fnum++;}
	    }
	fnum++;
	return(fnum);
    }

    public static int numFields(String S,String Separator)
    /*
      Returns the number of fields in the string S, separated by the given Separator.
      Fields are numbered 1...N, so this would return N.
      
      Str.numFields("Chickens cross this 66 road.","s") will return 5, since there are four 's' in the string.
      
      Str.numFields("Chickens cross%%this 66%%road.","%%") will return 3.

      Note that:
      Str.numFields("Chickens     cross    this    road."," ") will return 14, since there are 13 spaces separating fields (many fields are "").
      
      Whereas:
      Str.numFields("Chickens     cross    this    road.") will return 4, since there are 3 regions of whitespace separating the fields.

    */
    {
	if (S==null) {return(0);}
	int[] indy = indices(S,Separator);
	return(indy.length+1);
    }
    
    public static int numFields(String S)
    /*
      Returns the number of fields in the string S, where fields are separated by whitespace.
      Fields are numbered 1...N, so this would return N.	
    */
    {
	if (S==null) {return(0);}
	int uni[] = string_to_uni(S);
	int j;
	int fnum=0;
	boolean lw=true;
	for (j=0;j<uni.length;j++)
	    {
		if ((!uni_is_WS(uni[j])) && lw )
		    { // rising edge.
			fnum++;
		    }
		lw=uni_is_WS(uni[j]);
	    }
	return(fnum);
    }


    public static String[] split0(String S,String Separator)
    {
	String[] StartsAtOne = split(S,Separator);
	int e;
	String[] fields = new String[StartsAtOne.length-1];
	for (e=1;e<StartsAtOne.length;e++)
	    {
		fields[e-1]=StartsAtOne[e];
	    }
	return(fields);
    }

    public static String[] split(String S,String Separator)
    /*
      In Pinjo:  String[] Fields = Str.split(line,sep);
                 int N = Fields.length-1;   // Since Fields[0] is not used.

      In Gawk:   N = split(line,Fields,sep);

      Splits a string up into an array of strings, using the given Separator.
      If Separator is null or "", splits every character out. 1...N.

      After separation, the string S is decomposed into N fields (supposing N-1 separators are found).
      The array returned will actually have N+1 elements, with the 0 element 'null'. 
      Elements 1 to N will contain the fields.
     */
    { //TESTED.
	if (Separator==null) {Separator="";}
	// Every-letter case:
	if (equals(Separator,""))
	    {
		int uni[] = string_to_uni(S);
		String[] ret = new String[uni.length+1];
		int j;
		for (j=0;j<uni.length;j++)
		    {
			ret[j+1] = new String(uni,j,1);
		    }
		return(ret);
	    }
	// Usual case:
	int seplen = length(Separator);
	int[] indys = indices(S,Separator);
	String[] fields = new String[indys.length+2];
	int l=0;
	int j;
	for (j=0;j<indys.length;j++)
	    {
		fields[j+1]=substr(S,l,indys[j]-l);
		l=indys[j]+seplen;
	    }
	fields[j+1]=substr(S,l,length(S)-l);
	return(fields);
    }


    
    public static String[] split(String S) 
    /*
      In Pinjo:  String[] Fields = Str.split(line);
                 int N = Fields.length-1;   // Since Fields[0] is not used.

      In Gawk:   N = split(line,Fields);

      Splits the string S into an array of strings, using WHITESPACE
      Note that the array length will be N+1, where N is the number of fields.
      A = split(SSS);
      A[0] will be 'null'. A[1]...A[N] will contain the fields.      

      If a different separator than whitespace is needed, use the other form of split().
     */
    {
	return(splito(S,0));
    }
    public static String[] split0(String S)
    /*
      Exactly like split(), except the returned array begins at 0, not 1,
      Therefore, the N fields are in array positions 0...N-1 instead of 1...N,
      and the length of the resulting array will be N instead of N+1.
     */
    {
	return(splito(S,-1));
    }

    public static String[] splito(String S,int offset)   //no quoting or anything. 1...N. + offset.
    {
	int n;
	n=numFields(S);
	if (n==0) {return(new String[0]);}
	String[] fields = new String[n+1+offset];  // since we begin at 1.
	int uni[] = string_to_uni(S);
	int j;
	int fnum=0;
	int fsj=0;
	boolean lw=true;
	for (j=0;j<uni.length;j++)
	    {
		if ((!uni_is_WS(uni[j])) && lw )
		    { // rising edge.
			fnum++;
			fsj=j;
		    }
		if ((uni_is_WS(uni[j]))&& (!lw) )
		    { // falling edge.
			fields[fnum+offset] = uni_to_string(uni,fsj,j-fsj);
		    }
		lw=uni_is_WS(uni[j]);
	    }
	if (!lw)
	    {
		fields[fnum+offset] = uni_to_string(uni,fsj,j-fsj);
	    }
	return(fields);
    }

    public static double[] split_d(String S)   //no quoting or anything. 1...N.
    /*
      Exactly like split(), but returns an array of doubles. (Array length N+1: using indices 1...N)
     */
    {
	return(split_d(S,0));
    }
    public static double[] split0_d(String S)   //no quoting or anything. 0...N-1.
    /*
      Exactly like split0(), but returns an array of doubles. (Array length N: using indices 0...N-1)
     */
    {
	return(split_d(S,-1));
    }

    public static double[] split_d(String S,int offset)   //no quoting or anything. 1...N. + offset.
    {
	int n;
	n=numFields(S);
	if (n==0) {return(new double[0]);}
	double[] fields = new double[n+1+offset];  // since we begin at 1.
	int uni[] = string_to_uni(S);
	int j;
	int fnum=0;
	int fsj=0;
	boolean lw=true;
	for (j=0;j<uni.length;j++)
	    {
		if ((!uni_is_WS(uni[j])) && lw )
		    { // rising edge.
			fnum++;
			fsj=j;
		    }
		if ((uni_is_WS(uni[j]))&& (!lw) )
		    { // falling edge.
			fields[fnum+offset] = atof(uni_to_string(uni,fsj,j-fsj));
		    }
		lw=uni_is_WS(uni[j]);
	    }
	if (!lw)
	    {
		fields[fnum+offset] = atof(uni_to_string(uni,fsj,j-fsj));
	    }
	return(fields);
    }

   
    public static int numCSVfields(String S)
    /*
      Returns the number of fields in the CSV line. Quoting is allowed with '' or "".
      'Dave Barry',1, 3.5,"Here's,,,one",
      has five fields (the last is empty).
     */
    {
	if (S==null) {return(0);}
	int j;
	int qm=0;
	int oqm;
	int fnum=1;
	int fsj=0;
	int LastNonWhiteSpace=',';
	int uni[] = string_to_uni(S);
	for (j=0;j<uni.length;j++)
	    {
		oqm=qm;
		if (LastNonWhiteSpace==',')  //only then can we enter quote mode.
		    {
			if (oqm==0)
			    {
				if (uni[j]=='"') {qm=1;}
				if (uni[j]=='\'') {qm=2;}
			    }
		    }
		if (oqm==1)
		    {
			if (uni[j]=='"') {qm=0;}
		    }
		else if (oqm==2)
		    {
			if (uni[j]=='\'') {qm=0;}
		    }
		if (!uni_is_WS(uni[j])) {LastNonWhiteSpace=uni[j];}

		if ((qm==0)&&(uni[j]==','))
		    {
			fnum++;fsj=j+1;continue;
		    }
	    }
	return(fnum);
    }

    
    public static String[] splitCSV(String S)
    /*
      Exactly like split() but for CSV fields. Quoting is allowed.
      Note that for N fields, the array will have length N+1;
      the fields 1..N will be in array elements 1..N;
      element 0 will be empty.
     */
    {
	int n;
	n=numCSVfields(S);
	if (n==0) {return(new String[0]);}
	String[] fields = new String[numCSVfields(S)+1];   // since we begin at 1.
	int uni[] = string_to_uni(S);
	int j;
	int qm=0,oqm;
	int fnum=1;
	int fsj=0;
	int LastNonWhiteSpace=',';   //SoL.
	for (j=0;j<uni.length;j++)
	    {
		oqm=qm;
		if (LastNonWhiteSpace==',')  //only then can we enter quote mode.
		    {
			if (oqm==0)
			    {
				if (uni[j]=='"') {qm=1;}
				if (uni[j]=='\'') {qm=2;}
			    }
		    }
		if (oqm==1)
		    {
			if (uni[j]=='"') {qm=0;}
		    }
		else if (oqm==2)
		    {
			if (uni[j]=='\'') {qm=0;}
		    }
		if (!uni_is_WS(uni[j])) {LastNonWhiteSpace=uni[j];}
		
		if ((qm==0)&&(uni[j]==','))
		    {
			// We have just finished a field; we want them all.
			fields[fnum]=removeQuotes(removeLTWS(uni_to_string(uni,fsj,j-fsj)));
			//fields[fnum]=removeQuotes((uni_to_string(uni,fsj,j-fsj)));
			//print(fields[fnum] + "   [" + fsj + ":" + (j-fsj) + "]");
			fnum++;fsj=j+1;continue;
		    }
	    }
	fields[fnum] = removeQuotes(removeLTWS(uni_to_string(uni,fsj,j-fsj)));
	return(fields);
    }

    
    public static String getCSVfield(String S,int m)
    /*
      Returns a single CSV field from a string. m starts at 1. Quoting is allowed.
     */
    {
	if (S==null) {return("");}
	int uni[] = string_to_uni(S);
	int j;
	int qm=0;
	int oqm;
	int fnum=1;
	int fsj=0;
	int LastNonWhiteSpace=',';
	for (j=0;j<uni.length;j++)
	    {
		oqm=qm;
		if (LastNonWhiteSpace==',')  //only then can we enter quote mode.
		    {
			if (oqm==0)
			    {
				if (uni[j]=='"') {qm=1;}
				if (uni[j]=='\'') {qm=2;}
			    }
		    }
		if (oqm==1)
		    {
			if (uni[j]=='"') {qm=0;}
		    }
		else if (oqm==2)
		    {
			if (uni[j]=='\'') {qm=0;}
		    }
		if (!uni_is_WS(uni[j])) {LastNonWhiteSpace=uni[j];}

		if ((qm==0)&&(uni[j]==','))
		    {
			// Have we just finished the field we want?
			if (fnum==m)
			    {
				return(removeQuotes(removeLTWS(uni_to_string(uni,fsj,j-fsj))));
			    }
			fnum++;fsj=j+1;continue;
		    }
	    }
	//EoL.
	if (fnum==m)
	    {
		return(removeQuotes(removeLTWS(uni_to_string(uni,fsj,j-fsj))));
	    }
	return("");
    }

    public static int getInt(String S)
    {
	return(atoi(S));
    }
    public static double getFlt(String S)
    {
	return(atof(S));
    }
    public static int atoi(String S)
    {
	if (S==null) {return(0);}
	//stupid.
	Double val;
	int vall;
	val=atof(S);
	vall=val.intValue();
	return(vall);
    }
    public static double atof(String S)
    {
	if (S==null) {return(0);}
	double val;
	try {
	    // "throws" an exception if given text. :-/
	    val=Double.parseDouble(S);
	}
	catch (NumberFormatException piff)
	    {
		val=0;
	    }
	return(val);
    }




    
    /*
      Private converters.
     */
    
    private static int[] utf8_to_uni(byte[] u8)
    {
	// DIY utf-8 since Java is untrustworthy with stupid "modified utf8" since
	// utf-8 standard somehow was not good enough for Sun.
	// Terminates on a zero.
	// Really should replace bad sequences with '_' or something...
	int l=u8.length;
	int j=0;
	int m=0;
	int ch,ch2;
	while (j<l)
	    {
		ch=u8[j];ch=ch&0xff;
		if (ch==0) {l=j;break;}
		if ((ch & 0x80) == 0) {m++;j++;}
		else {while ( (ch & 0x80) !=0) { ch=ch<<1;j++;} m++;}
	    }
	int codes[] = new int[m];
	j=0;int k=0;int p;
	while (j<l)
	    {
		ch=u8[j];ch=ch&0xff;
		if ((ch & 0x80) ==0) {codes[k]=ch;k++;j++;}
		else
		    {
			int badflag=0;
			int mask=0x7f;
			m=0;while ( (ch & 0x80) !=0) { ch=ch<<1; mask=mask>>1;m++;}
			//m-byte sequence:
			ch = u8[j] ; ch=ch & mask;
			for (p=1;p<m;p++)
			    {
				ch2=u8[j+p];
				if ((ch2 & 0xc0)!=0x80)
				    {
					// bad sequence!
					badflag=1;
				    }
				ch2=ch2 & 0x3f; // 6 bits.
				ch = (ch<<6) | ch2;
			    }
			j=j+m;
			if (badflag==0)
			    {
				codes[k]=ch;k++;
			    }
			else
			    {
				codes[k]='_';k++;
			    }
		    }
	    }
	return(codes);
    }
    private static int[] string_to_uni(String inStr)
    /*
      String -> Unicode points.
      Produces NO surrogate pairs. Only full unicode points.
      length can be -1.
    */
    {
	return(string_to_uni(inStr,0,-1));
    }
    private static int[] string_to_uni(String inStr,int start,int length)
    {
	int l,m,i,j,cp;
	l=inStr.length();
	i=start;j=0;
	while (i<l)
	    {
		if (length>=0)
		    {
			if (i>=start+length) {break;}
		    }
		cp = inStr.codePointAt(i);
		if (cp>0xffff) {i++;} // codePointAt resolves surrogate pair.
		i++;j++;
	    }
	m=j;  //length in code points.
	int[] codes = new int[m];
	i=start;j=0;
	while (i<l)
	    {
		if (length>=0)
		    {
			if (i>=start+length) {break;}
		    }
		codes[j] = inStr.codePointAt(i);
		if (codes[j]>0xffff) {i++;} // codePointAt resolves surrogate pair.
		i++;j++;
	    }
	return(codes);
    }

    private static byte[] uni_to_utf8(int[] unipoints,int start,int len)
    /*
      Unicode Points -> utf8.
     */
    {
	// DIY utf-8 since Java is untrustworthy.
	int i;
	int l=0;
	for (i=start;i<unipoints.length;i++)
	    {
		if (len>=0)
		    {
			if (i>=start+len) {break;}
		    }
		int uni=unipoints[i];
		// Convert this code point to utf-8:
		// Single byte: 7 bits.
		// n-byte sequence: 7 - n + 6(n-1) = 1 + 5n bits.  n = 2 .. 7 (really capped at 6, but fine.)
		int n;
		if (uni<0x80) {n=1;}  //single byte.   7 bits 0xxxxxxx  
		else for (n=2;n<8;n++) { if (uni < (1 << (1 + 5*n))) {break;} }
		l=l+n;
	    }
	// we now have length of output:
	byte[] u8 = new byte[l];
	int e,f=0,k=0;
	Integer uni;
	for (i=start;i<unipoints.length;i++)
	    {
		if (len>=0)
		    {
			if (i>=start+len) {break;}
		    }
		uni = unipoints[i];
		int n;
		//System.out.println(":::" + uni);	
		if (uni<0x80) {u8[k]=uni.byteValue();k++;continue;} // 7 bits 0xxxxxxx    
		for (e=2;e<8;e++)  //e-byte seq. contains 1+5e bits.
		    {
			if (uni<(1<<(1+5*e)))
			    {
				Integer ub;
				//e bytes: 5e+1 bits
				ub=((0xfe<<(7-e))&0xff) | (uni>>(6*(e-1)));  //1...10x...x
				u8[k]=ub.byteValue();k++;
				for(f=1;f<e;f++)
				    {
					ub=0x80|((uni>>( 6*(e-1-f) ))&0x3f);
					u8[k]=ub.byteValue();k++;
				    }
				break;
			    }
		    }
	    }
	return(u8);
    }

   
    private static byte[] string_to_utf8(String sss,int startcp,int lencp)
    {
	byte[] u8 =  uni_to_utf8(string_to_uni(sss,startcp,lencp),0,-1);
	return(u8);
    }

    private static String uni_to_string(int [] codes)
    {
	return (uni_to_string(codes,0,codes.length));
    }

    private static String damage(String S)
    {
	S=substr(S,0,1);
	return (S);
    }

    public static String spaces(int len)
    /*
      Provides a String with a number of spaces.
     */
    {
	String S="";
	String spc="        ";
	while (len>8)
	    {
		S=S + spc;
		len=len-8;
	    }
	while (len>0)
	    {
		S=S + " ";
		len--;
	    }
	return(S);	
    }
    
    public static String pad_on_left(String S,int newlength)
    /*
      If S is shorter than newlength, pads it on the left with spaces.
     */
    {
	int ln=Str.length(S);
	if (ln>=newlength) {return(S);}
	return(spaces(newlength-ln) + S);
    }
    
    private static String uni_to_string(int [] codes,int i,int l)
    {
	if (codes==null) {return("");}
	if (codes.length==0) {return("");}
	if (l==-1) {l=codes.length;}
	if (l>(codes.length-i)) {l=codes.length-i;}
	return(new String(codes,i,l));

	/*
	  int[] newcodes = new int[l];
	  for (int j=0;j<l;j++) {newcodes[j]=codes[i+j];}
	  return(new String(newcodes,0,l));
	*/
    }


    static String ConcatArray(String[] strings,String Connector)
    {
	int j;
	String rec="";
	for (j=0;j<strings.length;j++) 
	    {
		if (j>0) {rec=rec + Connector;}
		rec=rec + strings[j];
	    }
	return(rec);
    }
    static String ConcatArray(FA_S strings,String Connector)
    {
	int j;
	String rec="";
	for (j=0;j<strings.length;j++) 
	    {
		if (j>0) {rec=rec + Connector;}
		rec=rec + strings.get(j);
	    }
	return(rec);
    }


    

    public static String PrepareStringForDollarEscape(String sss)
    /*
      Prepares sss to fit in $'sss' for a bash command line.
      sss can contain any bytes except zero.
    */
    {
	int[] uni = string_to_uni(sss);
	String Out="";
	int[] oneuni = new int[1];
	int j;
	for (j=0;j<uni.length;j++)
	    {
		if (uni[j]=='\\') {Out=Out + "\\x5c";}
		if (uni[j]=='\'') {Out=Out + "\\'";}
		if (uni[j]<0x20)
		    {
			String hex=MLR.itosb(uni[j],16);
			if (Str.length(hex)<2) {hex="0" + hex;}
			Out=Out + "\\x" + hex;
		    }
		oneuni[0]=uni[j];
		Out=Out + uni_to_string(oneuni);
	    }
	return(Out);
    }

    public static String Repeat(String S,int len)
    /*
      Repeats string S over and over until specified length is achieved.
      (The last one might be cut off)
     */
    {
	String OS = S;
	while (Str.length(OS)<len)
	    {
		OS = OS + OS;
	    }
	return(Str.substr(OS,0,len));	
    }

    public static int Find(String[] S,String key)
    /*
      Returns index of key in the string array, or -1.
     */
    {
	int i;
	for (i=0;i<S.length;i++) {if (Str.equals(S[i],key)) {return(i);}}
	return(-1);	
    }
    
    
    
    /*
      These are no longer used.
     */        
    public static String[] XXXsplitch(String S,int Ch)
    {
	return(XXXsplitch(S,Ch,0));
    }
    public static String[] XXXsplitch0(String S,int Ch)
    {
	return(XXXsplitch(S,Ch,-1));
    }
    public static String[] XXXsplitch(String S,int Ch,int offset)
    {    //no quoting or anything. 1...N. + offset
	if (S==null) {return(new String[0]);}
	int uni[] = string_to_uni(S);
	if (uni.length==0) {return(new String[1]);}
	String[] fields = new String[numFieldsXXX(S,Ch)+1+offset];  // since we begin at 1.
	int j,fsj=0;
	int fnum=0;
	for (j=0;j<uni.length;j++)
	    {
		if (uni[j]==Ch)
		    {
			fnum++;
			fields[fnum+offset] = uni_to_string(uni,fsj,j-fsj);
			fsj=j+1;
		    }
	    }
	fnum++;
	fields[fnum+offset] = uni_to_string(uni,fsj,j-fsj);
	return(fields);
    }

    public static String SecInDayToTime(double SecInDay)
    /*
      Changes seconds since midnight to a time hh:mm:ss.xxx
      xxx is some number of ms.

      Note that times may be larger than 23:59. i.e. 86400 -> 24:00:00

      SecInDayToTime(3665.6) = "01:01:05.600"
     */
    {
	int ms = MLR.ftoi_floor(MLR.mod(SecInDay,1.0)*1000);
	String MS = "" + ms;
	if (ms<100) {MS="0" + MS;}
	if (ms<10) {MS="0" + MS;}
	return(SecInDayToTime(MLR.ftoi_floor(SecInDay)) + "." + MS);
    }
    public static String SecInDayToTime(int SecInDay)
    /*
      Changes seconds since midnight to a time hh:mm:ss
      
      SecInDayToTime(3665) = "01:01:05"
    */
    {
	//SecInDay = MLR.mod(SecInDay,86400);
	int h = SecInDay/3600;
	int m = (SecInDay - h*3600)/60;
	int s = SecInDay % 60;
	String H = "" + h; if (h<10) {H = "0" + H;}
	String M = "" + m; if (m<10) {M = "0" + M;}
	String S = "" + s; if (s<10) {S = "0" + S;}
	return(H + ":" + M + ":" + S);
    }

    

//pjsec Printing Strings
/*
  Str.print()  Str.printn()
  Str.printc()  Str.printnc()
  Str.printrgb()  Str.printnrgb()
  Str.printRGB()  Str.printnRGB()
  
  print = normal printing, ending with end-of-line.
  Add:
  n   : no EoL printed.
  c   : 3-bit colour; 2^3 = 8 colours.
  rgb : 6^3 = 216 colours.
  RGB : 24-bit colout; 256^3 = 2^24 = 16777216 colours.

  Usually the most convenient for adding colour is printrgb() or printnrgb().
  
  * Printing might not appear on the terminal right away; use Str.flush()

*/    

    public static void printE(String S)
    /*
      Prints a string on stdout, and allows convenient escape sequences for colours.
      The escape sequence is between two # characters.
      To print a real '#' character, just put nothing between: ##

      6-level colour (6×6×6 = 216 colours):
      #rgb# with r,g,b from 0 to 5.

      256-level colour (256^3 = 16777216 colours):
      #rrrgggbbb# with r,g,b from 000 to 255 decimal.

      Bold, italic and underline can be combined with colour, by adding any of * / _
      
      To switch back to 'no colour' and plain style:
      #0#
      (Both style and colour attributes last until #0# or end of the string S.)

      Example:
      Str.printE("## #505#Hello#0# there, #500#Mr. #*#Red#0#. #/550#Please#0# #_303#meet#0# #005#Ms. #*#Blue#0#. I am #000200000#Green!");

     */
    {
	Str.print(composeE(S));
    }
    public static void printEn(String S)
    /*
      Exactly like printE() but does not add end-of-line character.
    */
    {
	Str.printn(composeE(S));
    }
    public static String composeE(String S)
    /*
      # ANSI colour and style generator
      * This composes the terminal ANSI-coloured string which printE() would print out.
      * See printE() for documentation of the escape sequences used (between #...#)
      ```
      Str.printE(something);
      ```
      is exactly equivalent to:
      ```
      Str.print(Str.composeE(something));
      ```
     */
    {
	int rgb=0;
	int sty=0;
	int r=0,g=0,b=0;
	String[] bits = split0(S,"#");
	String O = "";
	for (int j=0;j<bits.length;j++)
	    {
		String ss = bits[j];
		int l = length(ss);
		// EVEN are printing; ODD are control (or '#')
		if ((j&1)==0)
		    { // EVEN: Printing.
			O += ss;
		    }
		else
		    { // ODD: Control.
			if (l==0) {O += "#";}
			if (Str.index(ss,"*")>=0) {sty = sty | 1;}
			if (Str.index(ss,"/")>=0) {sty = sty | 2;}
			if (Str.index(ss,"_")>=0) {sty = sty | 4;}
			ss = Str.replace(ss,"_","");
			ss = Str.replace(ss,"*","");
			ss = Str.replace(ss,"/","");
			l = Str.length(ss);
			//Str.print("<<<<<" + ss + " " + l);
			if (l==3)
			    {
				rgb=6;
				int c=atoi(ss);
				r=MLR.bound(c/100,0,5);
				g=MLR.bound((c/10)%10,0,5);
				b=MLR.bound((c%10),0,5);
			    }
			else if (l==9)
			    {
				rgb=256;
				int c=atoi(ss);
				r=MLR.bound(c/1000000,0,255);
				g=MLR.bound((c/1000)%1000,0,255);
				b=MLR.bound((c%1000),0,255);
			    }
			else if (Str.equals(ss,"0"))
			    {
				rgb=0;
				sty=0;
			    }
			// Reset style and print controls:
			O += "\033[0m";
			if (sty!=0)
			    {
				if ((sty&1)!=0) {O += "\033[1m";}
				if ((sty&2)!=0) {O += "\033[3m";}
				if ((sty&4)!=0) {O += "\033[4m";}
			    }
			if (rgb==6)
			    {
				O += "\033[38;5;" + ANSIcol(r,g,b) + "m";
			    }
			if (rgb==256)
			    {
				O += "\033[38;2;" + ANSI8col(r,g,b) + "m";
			    }
			
		    }
	    }
	O += "\033[0m";
	return(O);
    }

    public static void flush()
    /*
      Flush the stdout buffer.
    */
    {
	System.out.flush();
    }
    
    public static void print(String S)
    /*
      Prints a string on stdout (the terminal), with an end-of-line.
      (Like Gawk print, except that only one argument can be given, not a comma-separated list.)
    */
    {
	Str.printn(S + "\n");
    }
    public static void printn(String S)
    /*
      Prints a string on stdout (the terminal), with no final end-of-line.

      Though, one can always insert a \n inside S to make an EoL:
      Str.printn("Hello\nWorld.");  // Prints Hello and World. on two lines.
    */
    { // This is the print routine which all other print*() functions call.
	System.out.print(S);
    }
    private static int onebitrgb_to_consolecol(int rgb)
    {
	return(((rgb&1)<<2) | (rgb&2) | ((rgb&4)>>2));
    }
    public static void printc(String S,int onebitFGcolour)
    /*
      Supposing STDOUT is connected to the terminal, this prints a string in the given colour:
      Red : 4
      Green : 2
      Blue : 1
      These can be added in any combination. (Yellow would be Green + Red = 6)

      If you need more than 8 colours, use printrgb() (216 colours) or printRGB() (16777216 colours).
    */
    {
	int col=30 + onebitrgb_to_consolecol(onebitFGcolour);
	Str.printn("\033[" + col + "m" + S + "\033[0m\n");
    }
    public static void printnc(String S,int onebitFGcolour)	
    /*
      Exactly like printc() but with no EoL.
    */
    {
	int col=30 + onebitrgb_to_consolecol(onebitFGcolour);
	Str.printn("\033[" + col + "m" + S + "\033[0m");
    }
    public static void printc(String S,int onebitFGcolour,int onebitBGcolour)
    /*
      Supposing STDOUT is connected to the terminal, this prints a string in the given colour:
      Red : 4
      Green : 2
      Blue : 1
      These can be added in any combination. (Yellow would be Green + Red = 6)

      If you need more than 8 colours, use printrgb() or printRGB().
    */
    {
	int col1=30 + onebitrgb_to_consolecol(onebitFGcolour);
	int col2=40 + onebitrgb_to_consolecol(onebitBGcolour);
	Str.printn("\033[" + col1 + ";" + col2 + "m" + S + "\033[0m\n");
    }
    public static void printnc(String S,int onebitFGcolour,int onebitBGcolour)
    /*
      Exactly like printc() but with no EoL.
    */
    {
	int col1=30 + onebitrgb_to_consolecol(onebitFGcolour);
	int col2=40 + onebitrgb_to_consolecol(onebitBGcolour);
	Str.printn("\033[" + col1 + ";" + col2 + "m" + S + "\033[0m");
    }

    public static void printrgb(String S,int r,int g,int b)
    /*
      r,g,b each go from 0 to 5, giving a possible 216 colours in total.

      This might not work on every type of terminal; it uses "8-bit ANSI codes".

      If it fails to produce colour, try lower-resolution colour with printc().

      printrgb("Hello. Green.",0,4,0);
    */
    {
	Str.print(composergb(S,r,g,b));
    }
    public static String composergb(String S,int r,int g,int b)
    /*
      composes terminal colours for a string.
      ```
      Str.printrgb(S,r,g,b);
      ```
      is equivalent to:
      ```
      Str.print(composergb(S,r,g,b));
      ```
    */
    {    
	return("\033[38;5;" + ANSIcol(r,g,b) + "m" + S +"\033[0m");
    }
    public static void printnrgb(String S,int r,int g,int b)
    /*
      Exactly like printrgb() but does not add EoL.
    */
    {
	Str.printn(composergb(S,r,g,b));
    }
    
    public static void printrgb(String S,int[] rgb)
    /*
      Another form of printrgb(). rgb is int[3], each 0...5.
    */    
    {
	printrgb(S,rgb[0],rgb[1],rgb[2]);
    }
    public static void printnrgb(String S,int[] rgb)
    /*
      Another form of printnrgb(). rgb is int[3], each 0...5.
    */    
    {
	printnrgb(S,rgb[0],rgb[1],rgb[2]);
    }

    public static void printrgb(String S,int rgb6)
    {
	/*
	  rgb6 = r * 36 + g * 6 + b, or -1.

	  UNUSED so far...
	*/
	if (rgb6==-1)
	    {
		printn(S);
	    }
	else
	    {
		printrgb(S,rgb6 / 36,(rgb6 / 6) % 6, rgb6 % 6);
	    }
    }

    
    public static void printRGB(String S,int R,int G,int B)
    /*
      This is like printrgb() except the values go from 0...255.

      This might not work on every type of terminal; it uses "truecolour ANSI codes".

      If it fails to produce colour, try lower-resolution colour with printrgb().

      printRGB("Hello. Bright Yellow.",240,255,0);

    */
    {
	// truecolour: :-D
	Str.printn("\033[38;2;" + ANSI8col(R,G,B) + "m" + S + "\033[0m\n");
    }
    public static void printnRGB(String S,int R,int G,int B)
    /*
      Exactly like printRGB() but does not add EoL.
    */
    {
	Str.printn("\033[38;2;" + ANSI8col(R,G,B) + "m" + S + "\033[0m");
    }
    public static void printRGB(String S,int[] RGB)
    /*
      Another form of printRGB(). RGB is int[3], each 0...255.
    */
    {
	printRGB(S,RGB[0],RGB[1],RGB[2]);
    }
    public static void printnRGB(String S,int[] RGB)
    /*
      Another form of printnRGB(). RGB is int[3], each 0...255.
    */
    {
	printRGB(S,RGB[0],RGB[1],RGB[2]);
    }
    
    public static void printRGB(String S,int fgR,int fgG,int fgB,int bgR,int bgG,int bgB)
    /*
      A form of printRGB which can specify Foreground and Background colours.
      printRGB("Hello. Green on dark red.",0,255,0,80,0,0);
    */
    {
	Str.printn("\033[38;2;" + ANSI8col(fgR,fgG,fgB) + ";48;2;" + ANSI8col(bgR,bgG,bgB) + "m" + S + "\033[0m\n");
    }
    public static void printnRGB(String S,int fgR,int fgG,int fgB,int bgR,int bgG,int bgB)
    /*
      Exactly as printnRGB(), but does not add EoL.
    */
    {
	Str.printn("\033[38;2;" + ANSI8col(fgR,fgG,fgB) + ";48;2;" + ANSI8col(bgR,bgG,bgB) + "m" + S + "\033[0m");
    }
    public static void printRGB(String S,int[] fgRGB,int[] bgRGB)
    /*
      A form of printRGB which can specify Foreground and Background colours.
    */
    {
	printRGB(S,fgRGB[0],fgRGB[1],fgRGB[2],bgRGB[0],bgRGB[1],bgRGB[2]);
    }
    public static void printnRGB(String S,int[] fgRGB,int[] bgRGB)
    /*
      Exactly as printnRGB(), but does not add EoL.
    */
    {
	printnRGB(S,fgRGB[0],fgRGB[1],fgRGB[2],bgRGB[0],bgRGB[1],bgRGB[2]);
    }

    private static String ANSI8col(int r,int g,int b)
    {
    	if (r<0) {r=0;}	if (r>255) {r=255;}
	if (g<0) {g=0;}	if (g>255) {g=255;}
	if (b<0) {b=0;}	if (b>255) {b=255;}
	return("" + r + ";" + g + ";" + b);
    }
    private static int ANSIcol(int r,int g,int b)
    {
	
	if (r<0) {r=0;}	if (r>5) {r=5;}
	if (g<0) {g=0;}	if (g>5) {g=5;}
	if (b<0) {b=0;}	if (b>5) {b=5;}
	return(16 + b + g*6 + r*36);
    }

    public static void printrgb(String S,int fg_r,int fg_g,int fg_b,int bg_r,int bg_g,int bg_b)
    /*
      A form of printrgb which can specify Foreground and Background colours. each 0...5
      printrgb("Hello. Green on dark red.",0,5,0,1,0,0);
    */
    {
	// \033[38;5;196m
	Str.printn("\033[38;5;" + ANSIcol(fg_r,fg_g,fg_b) + ";48;5;" + ANSIcol(bg_r,bg_g,bg_b) + "m" + S + "\033[0m\n");
    }
    public static void printnrgb(String S,int fg_r,int fg_g,int fg_b,int bg_r,int bg_g,int bg_b)
    /*
      Exactly as printrgb() but adds no EoL.
    */
    {
	// \033[38;5;196m
	Str.printn("\033[38;5;" + ANSIcol(fg_r,fg_g,fg_b) + ";48;5;" + ANSIcol(bg_r,bg_g,bg_b) + "m" + S + "\033[0m");
    }
    public static void printrgb(String S,int[] fgrgb,int[] bgrgb)
    /*
      Another form of printrgb(). rgb = int[3].
    */    
    {
	printrgb(S,fgrgb[0],fgrgb[1],fgrgb[2],bgrgb[0],bgrgb[1],bgrgb[2]);
    }
    public static void printnrgb(String S,int[] fgrgb,int[] bgrgb)
    /*
      Exactly as printrgb() but adds no EoL.
    */    
    {
	printnrgb(S,fgrgb[0],fgrgb[1],fgrgb[2],bgrgb[0],bgrgb[1],bgrgb[2]);
    }

    private static String ExtractLF(String S)
    {
	int uni[] = string_to_uni(S);
	if (uni.length==0) {return(S);}
	if ((uni[uni.length-1])==10) // \n
	    {
		return("\n");
	    }
	return("");
    }
    private static String RemoveLF(String S)
    {
	int uni[] = string_to_uni(S);
	if (uni.length==0) {return(S);}
	if ((uni[uni.length-1])==10) // \n
	    {
		return(uni_to_string(uni,0,uni.length-1));
	    }
	return(S);
    }


    public static void printUtf8(String S)
    {
	if (S==null) {return;}
	byte wee[] = getUtf8(S);
	int j;for (j=0;j<wee.length;j++) {System.out.write(wee[j]);}
	System.out.write(10);
	return;
    }
    public static void dumpCodePoints(String S)
    {
	if (S==null) {return;}
	int uni[] = string_to_uni(S);
	System.out.print("{ ");
	int j;for (j=0;j<uni.length;j++) {System.out.print("[" + uni[j] + "]");}
	System.out.print(" }\n");
	return;
    }


    
//pjsec Arguments and Options
    /*
      String[] argv comes from the command line. 
      These routines help with parsing options.
      
      Using these routines, it is easy to support a command line of the form:

      $ Pinjo RunThisProgram -big -verbose x=100 y=120 InputFile="Heart.pl" OutputFile="Test.png"

      Options must come first.

      Here, 'big' and 'verbose' are called options.

      InputFile and OutputFile are called keys, and these are key assignments.

     */
    
    public static boolean IsOptionPresent(String[] argv,String option) //pjcodedump
    /*
      Give argv. This tells whether -option is present.

      if (IsOptionPresent(argv,"help"))
       {
          // $ MyProgram -help
       }

     */
    {
	int e;
	for (e=0;e<argv.length;e++)
	    {
		if (!Str.equals(substr(argv[e],0,1),"-")) {return(false);} // no more options.
		if (Str.equals(argv[e],"-" + option)) {return(true);}
	    }
	return(false);
    }
    //pjendcodedump

    public static String[] GetArgumentList(int first,String[] argv)  //pjcodedump
    /*
      If user runs
      $ MyProgram -opt -opt .... this that other
      we will skip the options and return a String[] with only this,that,other.
      It will never be null, but might have length 0.
      We start at argv[first] in any case.
     */
    {
	int e;
	for (e=first;e<argv.length;e++)
	    {
		if (substr(argv[e],0,1)!="-") {break;}
	    }
	if (e==argv.length) {return(new String[0]);}
	String[] args = new String[argv.length-e];
	int f;
	for (f=e;f<argv.length;f++)
	    {
		args[f-e]=argv[f];
	    }
	return(args);
    }
    //pjendcodedump


    /*
       Assignment code.................................................
     */


    public static String GetURLAssignment(String record,String key)
    /*  
	Return the value assigned to the key, where the record is in %-encoded URL notation.
	
	Note that the key, as provided, should not require url-encoding!

	Performs unencoding.

	GetURLAssignment("?animal=cat&vegetable=beet%20roots&","vegetable") would return "beet roots".

	returns null if assignment not present.
    */
    {
	String urlrec = "&" + replace(record,"?","&") + "&";
	return(GetAssignment(urlrec,key,"&","="));
    }

    public static String GetURLAss(String record,String key,String Default)
    /*
      Return a key value or else default if missing.
     */
    {
	String S= GetURLAssignment(record,key);
	if (S==null) {return(Default);}
	return(S);
    }
    public static int GetURLAss(String record,String key,int Default)
    /*
      Return a key value or else default if missing.
     */
    {
	String S= GetURLAssignment(record,key);
	if (S==null) {return(Default);}
	return(atoi(S));
    }
    public static double GetURLAss(String record,String key,double Default)
    /*
      Return a key value or else default if missing.
     */
    {
	String S= GetURLAssignment(record,key);
	if (S==null) {return(Default);}
	return(atof(S));
    }
    
    
    /*
      Main routine for assignment from an array.
     */
    public static String GetArgAssignment(String[] argv,String key)
    {
	return(GetArgAssignment(0,argv,key));
    }
    public static String GetArgAssignment(int first,String[] argv,String key)
    /*
      The user might set values on the command line:
      $ MyProgram -fullscreen -other -wide X=500 Y=20 ex="Hello there" key=value key=value...

      They can be retrieved with e.g.:

      int x = stoi(GetArgAssignment(argv,"X"));
      
      String mess = GetArgAssignment(argv,"ex")     

      This routine automatically SKIPS options on command line, and always
      starts at argv[first], skipping the 'first' ones 0...first-1.

      Also, the quotes "" in the above example are NOT passed by bash. We deal with that.

      RETURNS null if assignment is not present!
    */
    {
	return(GetArgAssignment(first,argv,key,1));
    }
    public static String GetArgAssignment(String[] argv,String key,int nth)
    {
	return(GetArgAssignment(0,argv,key,nth));
    }
    public static String GetArgAssignment(int first,String[] argv,String key,int nth)
    /*
      Works like GetArgAssignment(argv,key) but returns value of nth assignment of the key,
      after skipping 'first' entries of argv[]. (i.e. starts at argv[first])
      GetArgAssignment(argv,key,1) is thus equivalent to GetArgAssignment(argv,key).

      Suppose argv[] contains:

      N=something M=wee N=dog P=woo N=cat N=bird 

      GetArgAssignment(argv,"N",3) would return "cat".
      GetArgAssignment(1,argv,"M",1) would return null, since it starts at N=dog.
    */
    {
	if (nth<1) {return(null);}
	String[] args = GetArgumentList(first,argv);
	if (args.length==0) {return(null);}
	int mat=0;
	for (int e=0;e<args.length;e++)
	    {
		int i=index(args[e],"=");
		if (i>=0)
		    {
			//Str.print(args[e] + "?");
			if (equals(substr(args[e],0,i),key))
			    {
				mat++;
				if (mat==nth)
				    {
					return(substr(args[e],i+1,length(args[e])));
				    }
			    }
		    }
	    }
	return(null);
    }

    public static String GetArgAss(String[] argv,String key,String Default)
    /*
      Return a key value or else default if missing.
     */
    {
	String S= GetArgAssignment(argv,key);
	if (S==null) {return(Default);}
	return(S);
    }
    public static int GetArgAss(String[] argv,String key,int Default)
    /*
      Return a key value or else default if missing.
     */
    {
	String S= GetArgAssignment(argv,key);
	if (S==null) {return(Default);}
	return(atoi(S));
    }
    public static double GetArgAss(String[] argv,String key,double Default)
    /*
      Return a key value or else default if missing.
     */
    {
	String S= GetArgAssignment(argv,key);
	if (S==null) {return(Default);}
	return(atof(S));
    }

    // is key at pos j in array?
    private static boolean ArrAtArr(int[] Array,int[] key,int pos)
    {
	for (int k=0;k<key.length;k++)
	    {
		if (Array[k+pos]!=key[k]) {return(false);}
	    }
	return(true);
    }

    
    /*
      Main routine for assignment from a string.
    */
    public static String GetAssignment(String record,String key,String Separator,String AssignmentOp)
    /*
      Finds an assignment of a value to a key in a string:
      (sep) (key) (op) (val) (sep)

      Val can be quoted with either '' or "".
     */
    {
	//printc("RECORD: [" + record + "]\n",6);
	int[] Rec = getUni(Separator + record);
	int[] Key = getUni(Separator + key + AssignmentOp);
	int qm=0;   // 0; 1=", 2='
	int j=0;
	int k=0;
	for (j=0;j<Rec.length;j++)
	    {	       
		if ((qm==0)&&(Rec[j]==Key[k]))
		    {
			k++;
			if (k==Key.length) {break;}
		    }
		else
		    {
			k=0;
		    }

		if (qm==0)
		    {
			if (Rec[j]=='"') {qm=1;k=0;continue;}
			if (Rec[j]=='\'') {qm=2;k=0;continue;}
		    }
		if (qm==1)
		    {
			if (Rec[j]=='"') {qm=0;k=0;continue;}
		    }
		if (qm==2)
		    {
			if (Rec[j]=='\'') {qm=0;k=0;continue;}
		    }
	    }
	if (k!=Key.length) {return(null);}
	j++;  // we are now just after '='.
	if (j==Rec.length) {return("");}
	String term=Separator;
	if (Rec[j]=='"') {term="\"";j++;}
	else if (Rec[j]=='\'') {term="'";j++;}
	int[] Term = getUni(term);
	int[] Val = new int[Rec.length];
	int l=0;
	int t=0;
	while (j<Rec.length)
	    {
		Val[l]=Rec[j];
		l++;
		if (Rec[j]==Term[t])
		    {
			t++;
			if (t==Term.length) {l -= Term.length;break;}
		    }
		else
		    {
			t=0;
		    }
		j++;
		if (j==Rec.length) {break;}
	    }
	int Vall[] = new int[l];
	for (j=0;j<l;j++) {Vall[j]=Val[j];}
	return(uni_to_string(Vall));
    }
    public static String GetKeyValue(String rec,String key,String Op)
    {
	return(GetAssignment(rec,key," ",Op));
    }

    public static int GetIntAss(String record,String key,int defval)
    /*
      Gets an assignment from the record. If it is not present, uses defval.

      Example: record is " time=10 dist=3 "

      GetIntAss(record,"time",7)  will return 10
      GetIntAss(record,"pace",7)  will return 7
    */
    {
	String v = GetAssignment(record,key);
	if (v==null) {return(defval);}
	return(atoi(v));
    }
    public static int GetIntAss(String[] records,String key,int defval)
    /*
      Different form of GetIntAss. Looks through whole array.
     */
    {
	return(GetIntAss(ConcatArray(records," "),key,defval));
    }
    public static int GetIntAss(FA_S records,String key,int defval)
    /*
      Different form of GetIntAss. Looks through whole array.
     */
    {
	return(GetIntAss(ConcatArray(records," "),key,defval));
    }
    
    public static double GetDblAss(String record,String key,double defval)
    /*
      Gets an assignment from the record. If it is not present, uses defval.
      
      Example: record is " time=10 dist=3.2 "
      
      GetDblAss(record,"time",5.0)  will return 10.0
      GetDblAss(record,"pace",5.0)  will return 5.0
    */
    {
	String v = GetAssignment(record,key);
	if (v==null) {return(defval);}
	return(atof(v));
    }
    public static double GetDblAss(String[] records,String key,double defval)
    /*
      Different form of GetDblAss. Looks through whole array.
     */
    {
	return(GetDblAss(ConcatArray(records," "),key,defval));
    }
    public static double GetDblAss(FA_S records,String key,double defval)
    /*
      Different form of GetDblAss. Looks through whole array.
     */
    {
	return(GetDblAss(ConcatArray(records," "),key,defval));
    }
    
    public static String GetStrAss(String record,String key,String defval)
    /*
      Gets an assignment from the record. If it is not present, uses defval.
      
      Example: record is " name="Joe Sandwich" dist=3 level="h w" "
      
      GetStrAss(record,"name","Wally")  will return "Joe Sandwich"
      GetStrAss(record,"pace","Nut")  will return "Nut"
    */
    {
	String v = GetAssignment(record,key);
	if (v==null) {return(defval);}
	return(v);
    }
    public static String GetStrAss(String[] records,String key,String defval)
    /*
      Different form of GetStrAss. Looks through whole array.
     */
    {
	return(GetStrAss(ConcatArray(records," "),key,defval));
    }
    public static String GetStrAss(FA_S records,String key,String defval)
    /*
      Different form of GetStrAss. Looks through whole array.
      See GetArgAssignment though.
     */
    {
	return(GetStrAss(ConcatArray(records," "),key,defval));
    }

    public static String GetAssignment(String record,String key)
    /*
      Return the value assigned to the key. 
      If it does not exist within record, return null.
      The record might look like:
      
      value=param value=param value=param...
      
      A record might contain several kinds of assignments.
      Here is a worst case:
      
      Piffy="We have Car=Dodge there." Car="White Boat" Distance="20'" Phrase=It's_a_Dream_bi'
      
      A record might be completely in %-encoded URL notation: ?animal=cat&vegetable=beet%20roots& 
      
      In this case, use GetURLAssignment().
      
      Keys are never quoted. As of now, they can contain spaces, though!
      Dog name="Barry"  would be found, and is apparently unambiguous.
    */
    {
	return(GetAssignment(record,key," ","="));	
    }
		
    
    

    
    /*
      Find index of Key in Record, /outside quotes "" or ''/.
      Key should not contain any quotes, obviously.
      or else -1.

      Untested.
    */
    private static int findoq(String Record,String Key)
    {
	int[] record =  getUni(Record);	
	int[] key =  getUni(Key);	
	int QM=0;
	int keyj=0;
	for (int j=0;j<record.length;j++)
	    {
		if (QM==0)
		    {
			if (record[j]=='"') {QM=1;keyj=0;continue;}
			if (record[j]=='\'') {QM=2;keyj=0;continue;}
		    }
		else if (QM==1)
		    {
			if (record[j]=='"') {QM=0;keyj=0;continue;}
			
		    }
		else if (QM==2)
		    {
			if (record[j]=='\'') {QM=0;keyj=0;continue;}
		    }
		if ((QM==0)&&(record[j]==key[keyj]))
		    {
			keyj++;
			if (keyj==key.length) {return(j-keyj);}
		    }
		else
		    {
			keyj=0;
		    }
	    }
	return(-1);
    }
    
    

    public static String TtoS(String S)
    /*
      Changes each TAB character to eight spaces.
     */
    {
	return(Str.replace(S,"\t","        "));
    }
    
    public static String WhitePad(String S,int l)
    /*
      Pads the string with spaces on the right if necessary to be at least length l. 
     */
    {
	return(S + spaces(l-Str.length(S)));	
    }

    public static String substr(String S,int BorEx,int x,int BorEy,int y,int sl,int sr)
    /*
      Returns a substring starting from x (inclusive) and ending at y (exclusive).
      BorEx: x is measured either from Beginning (+1) or end (-1)
      BorEy: y is measured either from Beginning (+1) or end (-1)
     */
    {
	// NIY.
	return("");
    }



}    
