package com.ibm.Zimulator.SmallAux;

    
import java.io.*;
import java.util.*;
import java.text.*;


/*
  
  A few handfuls of small supplementary routines.
  
 */


public class MLR
{    
    public static void Sleep(int ms)
    /*
      Sleeps for the a number of milliseconds.
     */
    {
	try {Thread.sleep(ms);}
	catch (InterruptedException ie) { }
    }    

    public static String ftos(double D,int decimaldigits)
    /*
      Produces a [decimal] floating-point string like "1.63e+19".

      ```
      double cost= 314.2543625362;
      ftos(cost,2);  // will return "314.25"
      ```
    */
    {
	return(String.format("%." + decimaldigits + "f", D));
    }

    public static String itosb(int x,int base)
    /*
      Converts an integer to a string representation. 
      This works just like the simpler itos() but can use any base 2...36.
      Digits are chosen from '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'.

      To show that Hallowe'en = Christmas:
      ```
      Str.print("Oct 31 = " + itosb(25,8));
      Str.print("Dec 25 = " + itosb(25,10));      
      ```
     */
    {
	return(itosb(x,base,-1));
    }

    public static String itosb(int x,int base,int digits)
    /*
      Exactly like itosb() except that a number of digits can be specified.
      Leading 0s are aded as necessary.
     */
    {
	int sg=1;if (x<0) {x=-x;sg=-1;}
	if (x==0) {return("0");}
	String num = "";
	while (x>0)
	    {
		int d;
		d=(x % base);
		if (d>9) {d=d-10+'A';} else {d=d+'0';}
		num = Str.setUni(d) + num;
		x = x/base;
	    }
	if (digits>0)
	    {
		num = Str.replace(Str.pad_on_left(num,digits)," ","0");
	    }
        if (sg==-1) {num="-" + num;}
	return(num);
    }

    
    public static int ftoi(double x)
    /*
      Equivalent to ftoi_floor().
    */
    {
	Double X;
	X=Math.floor(x);
	return(X.intValue());	
    }
    public static int ftoi_floor(double x)
    /*
      Converts double to an integer. 
      Always rounds downwards.
    */
    {
	Double X;
	X=Math.floor(x);
	return(X.intValue());	
    }
    public static int ftoi_ceil(double x)
    /*
      Converts double to an integer. 
      Always rounds upwardss.
      ftoi(2.34) = 3
      ftoi(-2.34) = -2
    */
    {
	Double X;
	X=-Math.floor(-x);
	return(X.intValue());	
    }
    public static int ftoi_near(double x)
    /*
      Converts double to an integer.
      Always rounds nearest (and upwards on 0.5)
      ftoi(2.74) = 3
      ftoi(-2.74) = -3
    */
    {
	Double X;
	X=Math.floor(x+0.5);
	return(X.intValue());
    }
    public static int ftoi_zero(double x)
    /*
      Converts double to an integer.
      Always rounds towards zero, like gawk int().
      ftoi(2.74) = 2
      ftoi(-2.74) = -2
    */
    {
	Double X;
	if (x>0)
	    {
		X=Math.floor(x);
	    }
	else
	    {
		X=-Math.floor(-x);
	    }	    
	return(X.intValue());
    }
    

    public static double mod(double x,int y)
    /*
      Returns x mod y.

      For positive x, this is the same as x % y.
     */
    {
	if (y==0) {return(0.0);}
	double Y=y;
	return(mod(x,Y));
    }
    public static double mod(double x,double y)
    /*
      Returns x mod y.
      For positive x, this is the same as x % y.
     */
    {
	if (y==0.0) {return(0.0);}
	return((( x % y ) + y) % y);
    }
    public static int mod(int x,int y)
    /*
      Returns x mod y.
      For positive x, this is the same as x % y.
     */
    {
	if (y==0) {return(0);}
	return((( x % y ) + y) % y);
    }

    public static double floor(double x,double y)
    /*
      Rounds x down to a multiple of y.
     */
    {
	return(x - mod(x,y));
    }
    public static int floor(int x,int y)
    /*
      Rounds x down to a multiple of y.
     */
    {
	return(x - mod(x,y));
    }
    

    public static int bound(int x,int a,int b)
    /*
      Bound x between a and b. (order of a and b does not matter)
      If x is less than the range a...b then return the lower bound.
      If x is greater than the range a...b then return the upper bound.
      Otherwise, just return x.
    */
    {
	if (b>a)
	    {
		if (x<a) {return(a);}
		if (x>b) {return(b);}
	    }
	else
	    {
		if (x<b) {return(b);}
		if (x>a) {return(a);}
	    }
	return(x);
    }

   public static int[] asort(ArrayList<Double> Values)
    /*
     * Returns an array of indices into the provided array.
     * The indices returned reference the elements therein lowest to highest.
     */
    {
        asort mys = new asort<Object>(Values);
        return mys.order();
    }

    public static int[] asort(double[] Values)
    /*
     * Returns an array of indices into the provided array.
     * The indices returned reference the elements therein lowest to highest.
     */
    {
	asort mys = new asort<Object>(Values);
	return mys.order();
    }

    public static int[] asort(int[] Values)
    /*
     * Returns an array of indices into the provided array.
     * The indices returned reference the elements therein lowest to highest.
     */
    {
	asort mys = new asort<Object>(Values);
	return mys.order();	
    }

}
