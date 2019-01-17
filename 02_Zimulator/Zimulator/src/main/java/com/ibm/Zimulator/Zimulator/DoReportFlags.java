package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

public class DoReportFlags
{

    public static String toString(ReportFlags R)
    {
	if (R==null) {return("-");}
	return(R.toString());
    }
    
    public static boolean Anything(ReportFlags R) { return (R==null ? false : R.Anything() ); }
    public static boolean Self(ReportFlags R) { return (R==null ? false : R.Self() ); }
    public static boolean Container(ReportFlags R) { return (R==null ? false : R.Container() ); }
    public static boolean Path(ReportFlags R) { return (R==null ? false : R.Path() ); }
    public static int Channel(ReportFlags R) { return (R==null ? 0 : R.Channel() ); }
    public static boolean Details(ReportFlags R) { return (R==null ? false : R.Details() ); }

    // Allow overriding. 1st one overrides; 2nd is fallback:

    public static boolean Details(ReportFlags R1,ReportFlags R2)
    {
	if (R1!=null) {return R1.Details(); }
	if (R2!=null) {return R2.Details(); }
	return(false);
    }
    
    public static boolean Anything(ReportFlags R1,ReportFlags R2)
    {
	if (R1!=null) {return R1.Anything(); }
	if (R2!=null) {return R2.Anything(); }
	return(false);
    }
    public static boolean Self(ReportFlags R1,ReportFlags R2)
    {
	if (R1!=null) {return R1.Self(); }
	if (R2!=null) {return R2.Self(); }
	return(false);
    }
    public static boolean Container(ReportFlags R1,ReportFlags R2)
    {
	if (R1!=null) {return R1.Container(); }
	if (R2!=null) {return R2.Container(); }
	return(false);
    }
    public static boolean Path(ReportFlags R1,ReportFlags R2)
    {
	if (R1!=null) {return R1.Path(); }
	if (R2!=null) {return R2.Path(); }
	return(false);
    }
    public static int Channel(ReportFlags R1,ReportFlags R2)
    {
	if (R1!=null) {return R1.Channel(); }
	if (R2!=null) {return R2.Channel(); }
	return(0);
    }


    public static ReportFlags ReadFromObjFile(ArrayList<ObjRef> ORL_Dummy,PrimitiveDataIn D) throws IOException
    {
	int R = D.ri();
	if (R==-1)
	    {
		return(null);
	    }
	int R_Channel = D.ri();
	return(new ReportFlags(R,R_Channel));
    }

    public static void WriteToObjFile(PrimitiveDataOut D,ReportFlags R) throws IOException
    {
	if (R==null)
	    {
		D.wi(-1);
		return;
	    }
	R.Write_ObjFile(D);
    }
}
