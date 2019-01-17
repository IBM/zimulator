package com.ibm.Zimulator.SmallAux;

import java.io.*;

//pjsec Histo
/*

 * class Histo

 Keeps track of a set of data, and can output statistical properties thereof.

 An example:
  
  ```
  Histo Analysis = new Histo(30,-100.0,+250.0);
  {
    // ... populate the Histo with values using Analysis.AddSample().
  }
  Analysis.Print(80,2," m ");
  ``` 
*/

//pjpref [Histo]

public class Histo
{
    public Histo(int NumBuckets,double Range_low,double Range_high)
    /*
      Constructor.
      
      Sets the number of bins, along with the range of those bins.

      The 'Histo' will then be ready to be populated with data.
     
     */
    {
	Counts=new int[NumBuckets];
	this.Range_low=Range_low;
	this.Range_high=Range_high;
	CountsN=0;
	LargestCountIndex=0;
	Sum=0;
	Sum2=0;
	BucketSize = (Range_high - Range_low)/NumBuckets;
    }

    private double Sum;
    private double Sum2;
    public int[] Counts;
    public int CountsN;
    public double BucketSize;    
    public double Range_low;
    public double Range_high;
    private int LargestCountIndex;   // Mode bucket.

    private int getBucketIdx(double x) // or -1.
    {
	int b=MLR.ftoi_floor(getFloatBucketIdx(x));
	if (b<0) {b=-1;}
	if (b>=Counts.length) {b=-1;}
	return(b);	
    }
    private double getFloatBucketIdx(double x)
    {
	double idx = (x - Range_low) / BucketSize;
	return( idx ); // Buckets NOt centred.
    }



    
    public void AddSample(double x)
    /*
      Adds one sample datum to the Histo.

      e.g.
      ```
      Histo Analysis = new Histo(30,-100.0,+250.0);
      Analysis.AddSample(x);
      ```
     */
    {
	int b = getBucketIdx(x);
	if ((b>=0)&&(b<Counts.length))
	    {
		Counts[b]++;
		CountsN++;
		if (Counts[b] > Counts[LargestCountIndex]) {LargestCountIndex=b;}
		Sum += x;
		Sum2 += x*x;
	    }
    }


    public void AddSamples(double x,int cnt)
    /*
      Adds a datum repeatedly.
     */
    {
	int b = getBucketIdx(x);
	if ((b>=0)&&(b<Counts.length))
	    {
		Counts[b] += cnt;
		CountsN += cnt;		
		if (Counts[b] > Counts[LargestCountIndex]) {LargestCountIndex=b;}
		Sum += x*cnt;
		Sum2 += x*x*cnt;
	    }
    }

    
    public double Mean()
    /*
      Returns the sample mean.
    */
    {
	if (CountsN==0) {return(0);}
	return(Sum / CountsN);
    }

    public double StdDev()
    /*
      Returns the sample standard deviation.
    */
    {
	if (CountsN==0) {return(0);}
	double mean =  Sum / CountsN;
	double MeanOFSquare =  Sum2 / CountsN ;
	return( Math.sqrt(MeanOFSquare - mean*mean));
    }

    private int MeanBucket()
    {
	/*
	  Returns which bucket contains the mean.
	*/
	return(getBucketIdx(Mean()));
    }



    
    public void Dump(int digs,String Unit)
    /*
      Prints out the Histogram data. 'Unit' is used for labelling.
     */
    {
	if (CountsN==0) {return;}
	if (Unit==null) {Unit="";}
	int b;
	double cumfrac = 0;
	Str.print("# Fraction of Total,Cumulative Fraction,RangeLow,RangeHigh");
	for (b=0;b<Counts.length;b++)
	    {
		cumfrac = cumfrac + (1.0*Counts[b]/CountsN);
		//10%-intervals from median:
		String TP="   ";
		{
		    int tenp = MLR.ftoi(Math.abs(100.0*(cumfrac)));
		    TP=Str.intdig(tenp,2) + "%";
		}
		double x;
		x = 1.0 * Counts[b] / Counts[LargestCountIndex];

		int perc;
		perc = 100 * Counts[b] / CountsN;
		double low,high;
		low = (1.0*b/Counts.length) * (Range_high - Range_low) + Range_low;
		high = (1.0*(b+1)/Counts.length) * (Range_high - Range_low) + Range_low;

		Str.print("" + x + "," + cumfrac + "," + low + "," + high);
	    }
    }


    public void DumpPDF(double interval)
    /*
      Prints out the data for the PDF, with any x-interval.
    */
    {
	if (CountsN==0) {return;}
	double x;
	for (x=Range_low;x<Range_high;x=x+interval)
	    {
		Str.print("" + x + "," + PDF(x));
	    }
    }
    
    public void DumpCPDF(double interval)
    /*
      Prints out the data for cumulative PDF, with any x-interval.
     */
    {
	if (CountsN==0) {return;}
	double x;
	for (x=Range_low;x<Range_high;x=x+interval)
	    {
		Str.print("" + x + "," + CPDF(x));
		//Str.print("" + x + " " + FractionBetween(x,Range_high));
	    }
    }


    public void DumpInvCPDF(double interval)
    /*
      Prints out the data for the inverse cumulative PDF, with any interval.
     */
    {
	if (CountsN==0) {return;}
	double f;
	for (f=0.0;f<1.0;f=f+interval)
	    {
		double x=InvCPDF(f);
		Str.print("" + f + "," + x);
	    }
    }



    public double PDF(double x)
    /*
      This function is a linear apprimation of the histogram, a normalised PDF.

      Normalised with respect to original variable: ∫ dx PDF(x) = 1.
      
      * Equal to bucket fill value in CENTRE of bucket.

     */
    {
	x -= BucketSize/2.0;
	double y = getFloatBucketIdx(x);
	int n = MLR.ftoi_floor(y);
	double z = y - n;
	if (n<0) {return(0.0);}
	if (n>=Counts.length-1) {return(0.0);}
	double p = z * (Counts[n+1] - Counts[n]) + Counts[n];
	//Str.print("x=" + x + " y=" + y + " z=" + z +" p=" +p);
	p = p / CountsN / BucketSize;
	return(p);
    }

    public double InvCPDF(double Fraction)
    /*
      Inverse cumulative distribution.
    */
    {
	double A=0;
	int n=0;
	if (Fraction==0.0) {return(Range_low);}
	while (A<Fraction)
	    {
		A += 1.0*Counts[n]/CountsN;
		n++;
		if (n==Counts.length) {return(Range_high);}
	    }
	n--;
	A -= 1.0*Counts[n]/CountsN;
	// How much of bucket n?
	double x = n-1;
	x += (Fraction - A) / (1.0*Counts[n]/CountsN);
	x=x*BucketSize + Range_low;
	return(x);
    }
    
    public double CPDF(double x)
    /*
      Integrates from Range_low until x.
    */
    {
	if (CountsN==0) {return(0);}
	double A=0;
	int n;
	if (x<Range_low) {return(0);}
	{ // Partial bucket left of x.
	    double y = getFloatBucketIdx(x);
	    n = MLR.ftoi_floor(y);
	    double z = MLR.mod(y,1.0);
	    if ((n>=0)&&(n<Counts.length))
		{
		    A += 1.0 * Counts[n] * z;
		}
	}
	// Buckets until x's bucket.
	int mmax=Counts.length-1;
	if (n<Counts.length) {mmax=n-1;}
	if (mmax>=0)
	    {
		int m;
		for (m=0;m<=mmax;m++)
		    {
			A += 1.0 * Counts[m];
		    }
	    }
	return(A/CountsN);
    }    
    
    public double FractionBetween(double x0,double x1)
    /*
      This returns the fraction of the PDF between x0 and x1:

      ∫_(x0...x1) dx PDF(x)

     */
    {
	return(CPDF(x1)-CPDF(x0));
    }


    //pjpref

    public static void DataDump(FileOutputStream FIO,Histo H,String FirstLine)
    {
	Histo[] Hs = new Histo[1];
	Hs[0]=H;
	DataDump(FIO,Hs,FirstLine);
    }
    public static void DataDumpCum(FileOutputStream FIO,Histo H,String FirstLine,int points)
    {
	Histo[] Hs = new Histo[1];
	Hs[0]=H;
	DataDumpCum(FIO,Hs,FirstLine,points);
    }

    public static void DataDump(FileOutputStream FIO,Histo[] H,String FirstLine)
    /*
      Dumps several histograms' data to file. Suitable for gnuplot.
      Histograms MUST have same range, etc.
     */
    {
	if (FirstLine!=null)
	    {
		String S = FirstLine + "\n";try { FIO.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem.
	    }
	int b;
	for (b=0;b<H[0].Counts.length;b++)
	    {
		double x0,x1;
		x0 = H[0].Range_low + H[0].BucketSize*b;
		x1 = H[0].Range_low + H[0].BucketSize*(b+1);
		int h;
		String lin="" + x0 + " ";
		for (h=0;h<H.length;h++)
		    {
			lin = lin + " " + H[h].Counts[b];
		    }
		//Str.print(Filename + "[[[[[::::: " + b + "--" + H[0].Counts.length + "  : " + lin);
		String S = lin + "\n";try { FIO.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem.
	    }
    }


    public static void DataDumpCum(FileOutputStream FIO,Histo[] H,String FirstLine,int points)
    /*
      Just like Datadump.
      Prints out the data for cumulative PDF, with any arbitrary x-interval.
     */
    {
	if (FirstLine!=null)
	    {
		String S = FirstLine + "\n";try { FIO.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem.
	    }
	int b;double x;
	double interval = (H[0].Range_high - H[0].Range_low)/points;
	for (x=H[0].Range_low;x<H[0].Range_high;x=x+interval)
	    {
		int h;
		String lin="" + x + " ";
		for (h=0;h<H.length;h++)
		    {
			lin = lin + " " + H[h].CPDF(x);
		    }
		String S = lin + "\n";try { FIO.write(S.getBytes()); } catch (IOException IOEc) {} //whatever problem.
	    }
    }

}
