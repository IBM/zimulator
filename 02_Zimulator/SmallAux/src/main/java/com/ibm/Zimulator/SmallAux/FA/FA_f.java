package com.ibm.Zimulator.SmallAux.FA;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

/*
	All MDA_*.java source files are generated from this.

	Specific versions for basic types, and a <> version with generics.

	_f -> _d, _f, ...       <Obj>
	_f -> _d, _f, ...      ""
	Float -> Double, Float, ...  Obj
	float -> double, float, ...  Obj
	0 -> 0, 0, ...    	 null

	floatg   to select lines. (specific)
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
  * FA<float> for any object type float (use just like ArrayList<float>)
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

//pjpref [FA_f]

public class FA_f
{
    private ArrayList<Float> A;
    public int length;
    public FA_f()
    {
	A = new ArrayList<Float>();
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
    public int add(float val)
    /*
      Adds an entry to the end of the array, and assigns it the specified value.
      Returns the index.
     */
    {
	A.add(val);
	length++;
	return(length-1);
    }
    public float get(int i)
    /*
      Gets the value of element 'j' of the FlexArray.
    */
    {
	if (i<0) {return(0);}
	if (i>=length) {return(0);}
	return(A.get(i));
    }
    public void set(int i,float val)
    /*
      Sets element 'i' of the FlexArray to the value 'val'.
     */
    {
	if (i<0) {return;}
	while (i>=A.size())
	    {
		A.add((float) 0);    // floatg
		length++;
	    }
	A.set(i,val);
    }


    public float[] toArray()                              //floatg
    {                                                 //floatg              
	float[] Ar = new float[length];                       //floatg              
	for (int i=0;i<length;i++) {Ar[i]=A.get(i);}  //floatg              
	return(Ar);                                   //floatg              
    }                                                 //floatg              

    public void fromArray(float[] Ar)                          //floatg              
    {                                                      //floatg 
	A = new ArrayList<Float>();                           //floatg 
	length=Ar.length;	                           //floatg 
	for (int j=0;j<length;j++) { A.add(Ar[j]); }       //floatg 
    }                                                      //floatg 
    public float[] toArray(int len)                            //floatg 
    {                                                      //floatg 
	float[] Ar = new float[len];                               //floatg 
	int l = Math.min(len,length);                      //floatg 
	for (int i=0;i<l;i++) {Ar[i]=A.get(i);}            //floatg 
	return(Ar);                                        //floatg 
    }                                                      //floatg 

}
