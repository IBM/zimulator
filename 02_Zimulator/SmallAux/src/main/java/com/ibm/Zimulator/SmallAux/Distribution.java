package com.ibm.Zimulator.SmallAux;

import static java.lang.Math.*;


public class Distribution
/*
  Provides Random sampling from an arbitrary distribution.

  Very simple. 
 */
{
    double[] cumtable;
    double[] invcumtable;
    double xmin,xmax,dx;
    double tol;
    double f_μ,f_σ; // for provided f.
    
    public Distribution(DblFunc f, double _f_μ,double _f_σ, double _xmin,double _xmax,int samples,double tolerance)
    /*
      Constructor given a distribution f.      
      Specify mean and std.dev.
    */
    {
	f_μ = _f_μ ; f_σ = _f_σ;
	tol=tolerance;
	cumtable = new double[samples];
	invcumtable = new double[samples];
	xmax=_xmax;xmin=_xmin;
	dx = (_xmax - _xmin)  / (samples-1) ;
	double If = 0;
	for (int k=0;k<samples;k++)
	    {
		double x = dx * k + _xmin;
		double y = f.f(x);
		cumtable[k] = If;
		//Str.print(k + " >>> " + cumtable[k] + " ::: " + If);
		If += dx*y;
	    }
	// Now, we can fill in invcumtable:
	for (int k=0;k<samples;k++)
	    {
		double y = 1.0/(samples-1)*k;
		double x = inverse_cumulative(y);
		invcumtable[k] = x;
	    }	
    }

    public double sample(double μ,double σ,double r)
    /*
      Provide desired μ and σ, and a random number from [0...1] (flat distribution).
     */
    {
	return((invcumulative(r) - f_μ)/f_σ*σ + μ);
    }
    
    public double invcumulative(double y)
    /*
      Calculates Φ⁻¹(y).
      Φ(x) := ∫₋∞...x dx' f(x')
     */
    {
	return(interp(invcumtable,0,1,y));
    }
    
    public double cumulative(double x)
    /*
      Returns Φ(x) := ∫₋∞...x dx' f(x')
    */
    {
	double dmax = cumtable[cumtable.length-1];
	return(interp(cumtable,xmin,xmax,x)/dmax);
    }

    private double inverse_cumulative(double y)
    // We may assume monotonically increasing fn.
    {
	double x=0;
	while ( 0==0 )
	    {
		double gy = cumulative(x);
		double dif2 = gy - y; dif2 = dif2 * dif2;
		if (dif2<tol*tol) {break;}
		x = x + (xmax-xmin)/100.0*(y-gy);
	    }
	return(x);
    }
    
    private double interp(double[] table, double tmin,double tmax,double x)
    {
	if (x>=tmax) {return(table[table.length-1]);}
	if (x<=tmin) {return(table[0]);}
	double idx = (x - tmin) / (tmax-tmin) * (table.length-1);
	int i = MLR.ftoi_floor(idx);
	double f = idx - i;
	double y = table[i] * (1.0-f) + table[i+1]*f;
	//Str.print(x + " -> " + y);
	return(y);
    }
    

}
