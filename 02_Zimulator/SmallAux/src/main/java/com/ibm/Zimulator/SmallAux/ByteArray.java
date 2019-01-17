package com.ibm.Zimulator.SmallAux;
    
public class ByteArray
{
    public byte[] A;
    public int length;
    private int step;
    public ByteArray()
    {
        step=80;
        length=0;
        A = new byte[step];
    }
    public ByteArray(int s)
    {
        step=s;
        length=0;
        A = new byte[step];
    }
    public ByteArray(int st,int sz)   // supply step & initial size.
    {
        step=st;
        length=0;
        A = new byte[sz];
    }

    public void clear()
    /*
     */
    {
	length=0;
    }

    public int add(byte val)
    /*
      Adds an entry to the end of the array, and assigns it the specified value.
      Returns the index.
     */
    {
        set(length,val);
        return(length-1);
    }

    public void set(int i,byte val)
    /*
      Sets element 'j' of the ByteArray to the value 'val'.
     */
    {
        if (i>=A.length)
            {
                int newSize = ((i/step)+1)*step;
                byte[] B = new byte[newSize];
                int j;
                for (j=0;j<length;j++) {B[j]=A[j];}
                A=B;
            }
        A[i]=val;
        if (i>=length) {length=i+1;}
    }
    public byte get(int i)
    /*
      Gets the value of element 'j' of the ByteArray.
     */
    {
        if (i<0) {return(0);}
        if (i>=length) {return(0);}
        return(A[i]);
    }

    public byte[] toArray()
    /*
      Converts this to a normal array.
     */
    {
        byte[] Ar = new byte[length];
        for (int i=0;i<length;i++) {Ar[i]=A[i];}
        return(Ar);
    }
    public void fromArray(byte[] Ar)
    /*
      Copies from a normal array.
     */
    {
	A = new byte[Ar.length];
	length=Ar.length;	
	for (int j=0;j<length;j++) { A[j]=Ar[j]; }
    }

}
