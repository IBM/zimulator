package com.ibm.Zimulator.SmallAux.FA;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

/*
	All MDA_*.java source files are generated from this.

	Specific versions for basic types, and a <> version with generics.

	_S -> _d, _f, ...       <Obj>
	_S -> _d, _f, ...      ""
	String -> Double, Float, ...  Obj
	String -> double, float, ...  Obj
	null -> 0, 0, ...    	 null

	Stringg   to select lines. (specific)
*/

/*
  # FA - Flexible array class.
  
  * Elastic array object to hold either specific or generic type.

  Use of a basic array might look like this:

  ```
  double[] A = new double[3];
        for (int i=0;i<3;i++)
            {
	       A[i] = 0.2*i;
            }
     // oops! I wanted element A[7] too.
    A[7] = 23.9;  // error.
  ```
  * You need to know ahead of time the size, and you cannot simply add elements anywhere.
  * You need to allocate all the elements even if you might not use most of them.

  * FA solves the first problem. The above would be written:

  ```
  FA_d A = new FA_d();
  for (int i=0;i<3;i++)                                                                                                                                
    {
      A.set(i,0.2*i);
    }
  // oops! I wanted element A[7]. No problem:
  A.set(7,23.9);
  ```

  # FA variable type

  * FA_i, FA_d, FA_f, FA_S, FA_l, FA_byte, FA_bool for basic types.
  * FA<String> for any object type String (use just like ArrayList<String>)
  * For example:
  ```
	FA_i someintegers = new FA_i();
	FA_d somenumbers = new FA_d();
	FA_f somefloats = new FA_f();
	FA_l somebigintegers = new FA_l();
	FA_S somestrings = new FA_S();
	FA_byte somebytes = new FA_byte();
	FA_bool some_t_or_f = new FA_bool();
	FA<potato> some_of_my_potatoes = new FA<potato>();
  ```

*/

//pjpref [FA_S]

public class FA_S
{
    private ArrayList<String> A;
    public int length;
    public FA_S()
    {
	A = new ArrayList<String>();
	length=0;
    }
    public int size()
    /*
      Returns the size, which is n if the largest element written was n-1.
    */
    {
	return(length);
    }
    public void clear()
    /*
      Clears all elements, returning the size to zero.
    */
    {
	A.clear();
	length=0;
    }
    public int add(String val)
    /*
      Adds an entry to the end of the array, and assigns it the specified value.
      Returns the index.
     */
    {
	A.add(val);
	length++;
	return(length-1);
    }
    public String get(int i)
    /*
      Gets the value of element 'j' of the FlexArray.
    */
    {
	if (i<0) {return(null);}
	if (i>=length) {return(null);}
	return(A.get(i));
    }
    public void set(int i,String val)
    /*
      Sets element 'i' of the FlexArray to the value 'val'.
     */
    {
	if (i<0) {return;}
	while (i>=A.size())
	    {
		A.add((String) null);    // Stringg
		length++;
	    }
	A.set(i,val);
    }


    public String[] toArray()                              //Stringg
    {                                                 //Stringg              
	String[] Ar = new String[length];                       //Stringg              
	for (int i=0;i<length;i++) {Ar[i]=A.get(i);}  //Stringg              
	return(Ar);                                   //Stringg              
    }                                                 //Stringg              

    public void fromArray(String[] Ar)                          //Stringg              
    {                                                      //Stringg 
	A = new ArrayList<String>();                           //Stringg 
	length=Ar.length;	                           //Stringg 
	for (int j=0;j<length;j++) { A.add(Ar[j]); }       //Stringg 
    }                                                      //Stringg 
    public String[] toArray(int len)                            //Stringg 
    {                                                      //Stringg 
	String[] Ar = new String[len];                               //Stringg 
	int l = Math.min(len,length);                      //Stringg 
	for (int i=0;i<l;i++) {Ar[i]=A.get(i);}            //Stringg 
	return(Ar);                                        //Stringg 
    }                                                      //Stringg 

}
