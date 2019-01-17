package com.ibm.Zimulator.Zimulator;

/*
  'PrimitiveDataOut' and 'PrimitiveDataIn' are used to write and read
  "compiled" ("object") files representing the state of the system.

  These are low-level routines which read and write integers, Strings, etc.

  Implementing the 'CompiledFileRW' interface requires using the routines here.

  For a full discussion of compiled files, see 'CompiledFileIO'.

 */
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;

class PrimitiveDataOut
{    
    private DataOutputStream DOS;

    public  PrimitiveDataOut(String filename)
    {
	FileOutputStream FOS;
	try {FOS = new FileOutputStream(filename);}
	catch (FileNotFoundException FNF) { FOS=null;}
	if (FOS != null)
	    {
		DOS = new DataOutputStream(FOS);
	    }
    }
    public  PrimitiveDataOut(DataOutputStream _DOS)
    {
	DOS = _DOS;
    }
    
    public boolean valid()
    {
	return(DOS!=null);
    }
    
    public void wi(int i) throws IOException
    {
	DOS.writeInt(i);
    }
    public void wb(int i) throws IOException
    {
	DOS.write(i);
    }
    public void wboo(boolean x) throws IOException
    {
	if (x) {wb(1);} else {wb(0);}
    }
    public void wd(double x) throws IOException
    {
	DOS.writeDouble(x);
    }
    public void ws(String s) throws IOException
    { // 0-terminated PROPER utf-8; null -> "".
	if (s!=null)
	    {
		byte[] label = Str.getUtf8(s);  // Real UTF-8, not Java 'modified' garbage.
		DOS.write(label, 0, label.length);
	    }
	DOS.write(0);
    }

    public void close() throws IOException
    {
	DOS.close();
	DOS=null;
    }
}

class PrimitiveDataIn
{    
    private DataInputStream DIS;

    public PrimitiveDataIn(String filename)
    {
	FileInputStream FOS;
	try {FOS = new FileInputStream(filename);}
	catch (FileNotFoundException FNF) { FOS=null;}
	if (FOS != null)
	    {
		DIS = new DataInputStream(FOS);
	    }
    }
    public PrimitiveDataIn(DataInputStream _DIS)
    {
	DIS = _DIS;
    }

    public boolean available() throws IOException
    {
	return(DIS.available()>0);
    }
    
    public boolean valid()
    {
	return(DIS!=null);
    }

    public int ri() throws IOException
    {
	int n=0;
	n = DIS.readInt();
	return(n);
    }
    public byte rb() throws IOException
    {
	byte n=0;
	n = DIS.readByte();
	return(n);
    }
    public boolean rboo() throws IOException
    {
	byte n=0;
	n = DIS.readByte();
	return(n==1);
    }
    
    public double rd() throws IOException
    {
	double x=0;
	x = DIS.readDouble();
	return(x);
    }
    public String rs() throws IOException
    { // 0-terminated PROPER utf-8.
	ByteArray A = new ByteArray(80);
	byte n=1;
	while (n!=0)
	    {
		n = DIS.readByte();
		A.add(n);
	    }
	return(Str.setUtf8(A.toArray()));  // Real UTF-8, not Java 'modified' garbage. Sun makes me angry every time this crosses my mind.
    }

    public void close() throws IOException
    {
	DIS.close();
	DIS=null;
    }
}

