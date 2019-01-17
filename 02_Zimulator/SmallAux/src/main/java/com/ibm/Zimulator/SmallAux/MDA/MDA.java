package com.ibm.Zimulator.SmallAux.MDA;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

/*
	All MDA_*.java source files are generated from this, for specific types.

	See MakeJavaSources.sh for the 'preprocessor'
*/

/*
  # Multi-dimensional [sparse] array class

  * MDA where Я is one of b,i,l,f,d for fundamental byte, int, long, float, double.
    MDA<Я> where Я is any class.
    
  * Use of a basic multi-dimensional array might look like this:
  ```
    double[][][] A = new double[3][3][3];
        for (int i=0;i<3;i++)
            {
                for (int j=0;j<3;j++)
                    {
                        for (int k=0;k<3;k++)
                            {
                                A[i][j][k] = 0.2*i + j + k;
                            }
                    }
            }
     // oops! I wanted element A[3][4][5]
    A[3][4][5] = 23.9;  // error.
  ```
  * You need to know ahead of time that it will be 3×3×3, and cannot change it.
    (For the 1-D case you could use FA_d to avoid this problem)
    ( DblArray is deprecated. )
  * You need to allocate all the elements even if you might not use most of them.

  # MDA solves both these problems. 

  * The above code would be written:
  ```
  MDA_d A = new MDA_d(3);   // make a three-dimensional array. (of doubles)
  // instead of i,j,k we use i[0],i[1],i[2]:
  int[] i = new int[3];   // Three indices.
  for (i[0]=0;i[0]<3;i[0]++)
   for (i[1]=0;i[1]<3;i[1]++)
    for (i[2]=0;i[2]<3;i[2]++)
    {
      A.set(i,0.2*i[0] + i[1] + i[2]);
    }
  // oops! I wanted element A[3][4][5]. No problem:
  i[0]=3;i[1]=4;i[2]=5; A.set(i,23.9);
  ```

  # Indices

  * To access an n-dimensional MDA, an int[n] index is needed. the above example shows the n=3 case.

  * For the cases of 1,2,3 and 4-dimensions, the indices can be optionally written explicitly instead:
  ```
  i[0]=3;i[1]=4;i[2]=5; A.set(i,23.9);  
  // equivalent:
  A.set(3,4,5,23.9);
  ```
  So then the above code snippet would look like this:
  ```
    MDA_d A = new MDA_d(3);
        for (int i=0;i<3;i++)
            {
                for (int j=0;j<3;j++)
                    {
                        for (int k=0;k<3;k++)
                            {
                                A.set(i,j,k, 0.2*i + j + k );
                            }
                    }
            }
     // oops! I forgot element A(3,4,5) :
    A.set(3,4,5, 23.9);
  ```

  * If the provided index array is too big (e.g. a 3-D MDA but int[5] idx),
  then only the first index components are used:
  ```
  MDA A = new MDA(3);
  int[] i= new int[5];
  i[0]=3;i[1]=4;i[2]=5;i[3]=10;i[4]=2; A.set(i,23.9);  // still sets element 3,4,5 since A has dimension 3.
  ```

  ! If the provided array is too small, get() returns 0 and set() does nothing. THIS BEHAVIOUR MAY CHANGE.

  # MDA variable type

  * MDA_b, MDA_i, MDA_l, MDA_f, MDA_d, MDA_bool, MDA_S for basic types byte, int, long, float, double, boolean, String.
  * MDA<yyy> for any object type yyy (use just like ArrayList<yyy>)
  * For example:
  ```
      // Define a bunch of 7-dimensional arrays:
	MDA_i someintegers = new MDA_i(7);
	MDA_d somenumbers = new MDA_d(7);
	MDA_f somefloats = new MDA_f(7);
	MDA_l somebigintegers = new MDA_l(7);
	MDA_S somestrings = new MDA_S(7);
	MDA_b somebytes = new MDA_byte(7);
	MDA_bool some_t_or_f = new MDA_bool(7);
	MDA<potato> some_of_my_potatoes = new MDA<potato>(7);
  ```

  # Iterators

  * Since an MDA might be mostly empty, it is inefficient to do a number of for loops to access all elements.
    (In the above example, there are three 'for' loops)

  * Instead, the indices of all non-empty elements can be iterated over automatically.
    ```
    MDA_d somenumbers = new MDA_d(7);
    // ... fill up 'somenumbers' with a bunch of data ...
      {
	int[] idx = new int[7];
	idx[0]=1;idx[1]=1;idx[2]=0;idx[3]=7;idx[4]=2;idx[5]=3;idx[6]=3;
	somenumbers.set(idx,471.2);   // somenumbers[1,1,0,7,2,3,3] = 471.2;
        // ... and many more ...  
      }
    Iterator<int[]> nonempties = somenumbers.idxiterator();
    while (nonempties.hasNext())
    {
       int[] idx = nonempties.next();  // the index of the next non-empty slot, e.g. (1,1,0,7,2,3,3).
       double num = somenumbers.get(idx);
       // Here we could do some computation making use of num and idx.
    }
    ```

  # Performance
  
  * Since an MDA is slower than a basic array, it is /sometimes/ useful to be able to convert back and forth.
    That is, you might fill an MDA, and then convert it to a basic array, for two reasons:
    -> Basic array is much smaller
    -> Basic array is much quicker
    This conversion is not supported in any automatic way.
    It only makes sense for the non-sparse case.
    You can do it yourself, of course.
*/

//pjpref [MDA]

public class MDA<Я>
/*
  # MDA - multidimensional array - for holding type Я
*/
{
    private int dim;
    private SBHH indexer;
    ArrayList<Я> data;    // All at once for generic case.         Яgen
    private int pagesize;
    
    public MDA(int dim)
    /*
      Constructs a new MDA (of element type Я)
    */
    {
	this.dim=dim;
	data = new ArrayList<Я>();    // Яgen
	indexer = new SBHH();
	pagesize = 256;
    }

    public int size()
    /*
      Returns how many elements of this MDA have been set to something.
      This is how many would be iterated over using the idxiterator().
     */
    {
	return(indexer.size());
    }
    
    private byte[] idx_to_sidx(int[] idx)
    {
	byte[] b = new byte[4*dim];
	for (int j=0;j<dim;j++)
	    {
		int v=idx[j];
		b[4*j] = (byte)   ((v>>24) & 0xff);
		b[4*j+1] = (byte) ((v>>16) & 0xff);
		b[4*j+2] = (byte) ((v>>8) & 0xff);
		b[4*j+3] = (byte) (v & 0xff);
	    }
	return(b);
    }

    public Iterator<int[]> idxiterator()
    /*
      # Returns an iterator giving the index of every non-empty element.
     */
    {
	return(new iter(indexer.keyiterator()));
    }
    
    public void set(int[] idx, Я value)
    /*
     * Sets the element of the MDA
     ! Be sure that idx.length is at least the dimension of the MDA.
    */
    {
	if (idx.length<dim) {return;}
	int i = indexer.getidx(idx_to_sidx(idx));
	while (i >= data.size()) {data.add(null);}     // Яgen
	data.set(i,value);                             // Яgen
    }

    public Я get(int[] idx)
    /*
     * Gets the element of the MDA. 
     * If element idx has never been set, returns 0.
     ! Be sure that idx.length matches the dim of the MDA.
    */
    {
	if (idx.length!=dim) {return(null);}
	int i = indexer.chkidx(idx_to_sidx(idx));
	if (i<0) {return(null);}
	return(data.get(i));                          // Яgen
    }

    public boolean chk(int[] idx)
    /*
     * Returns whether or not the element idx has ever been set.
     ! Be sure that idx.length matches the dim of the MDA.
    */
    {
	if (idx.length!=dim) {return(false);}
	int i = indexer.chkidx(idx_to_sidx(idx));
	return(i>=0);
    }
    

    /*
     * Accumulates 'value' in the element of the MDA; i.e. adds 'value' to it.
     ! Be sure that idx.length matches the dim of the MDA.
    */


    /*
      Abbreviations.
     */
	    
    public void set(int i, Я value)
    /*
      # Abbreviation for specific case of 1-dimensional MDA.
     */
    {
	if (dim>1) {return;}
	int[] idx = new int[1];
	idx[0]=i;
	set(idx,value);
    }
    public void set(int i,int j, Я value)
    /*
      # Abbreviation for specific case of 2-dimensional MDA.
     */
    {
	if (dim>2) {return;}
	int[] idx = new int[2];
	idx[0]=i;idx[1]=j;
	set(idx,value);
    }
    public void set(int i,int j,int k, Я value)
    /*
      # Abbreviation for specific case of 3-dimensional MDA.
     */
    {
	if (dim>3) {return;}
	int[] idx = new int[3];
	idx[0]=i;idx[1]=j;idx[2]=k;
	set(idx,value);
    }
    public void set(int i,int j,int k,int l, Я value)
    /*
      # Abbreviation for specific case of 4-dimensional MDA.
     */
    {
	if (dim>4) {return;}
	int[] idx = new int[4];
	idx[0]=i;idx[1]=j;idx[2]=k;idx[3]=l;
	set(idx,value);
    }

    public Я get(int i)
    /*
      # Abbreviation for specific case of 1-dimensional MDA.
     */
    {
	if (dim>1) {return(null);}
	int[] idx = new int[1];
	idx[0]=i;
	return(get(idx));
    }
    public Я get(int i,int j)
    /*
      # Abbreviation for specific case of 2-dimensional MDA.
     */
    {
	if (dim>2) {return(null);}
	int[] idx = new int[2];
	idx[0]=i;idx[1]=j;
	return(get(idx));
    }
    public Я get(int i,int j,int k)
    /*
      # Abbreviation for specific case of 3-dimensional MDA.
     */
    {
	if (dim>3) {return(null);}
	int[] idx = new int[3];
	idx[0]=i;idx[1]=j;idx[2]=k;
	return(get(idx));
    }
    public Я get(int i,int j,int k,int l)
    /*
      # Abbreviation for specific case of 4-dimensional MDA.
     */
    {
	if (dim>4) {return(null);}
	int[] idx = new int[4];
	idx[0]=i;idx[1]=j;idx[2]=k;idx[3]=l;
	return(get(idx));
    }
	
    public boolean chk(int i)
    /*
      # Abbreviation for specific case of 1-dimensional MDA.
     */
    {
	if (dim>1) {return(false);}
	int[] idx = new int[1];
	idx[0]=i;
	return(chk(idx));
    }
    public boolean chk(int i,int j)
    /*
      # Abbreviation for specific case of 2-dimensional MDA.
     */
    {
	if (dim>2) {return(false);}
	int[] idx = new int[2];
	idx[0]=i;idx[1]=j;
	return(chk(idx));
    }
    public boolean chk(int i,int j,int k)
    /*
      # Abbreviation for specific case of 3-dimensional MDA.
     */
    {
	if (dim>3) {return(false);}
	int[] idx = new int[3];
	idx[0]=i;idx[1]=j;idx[2]=k;
	return(chk(idx));
    }
    public boolean chk(int i,int j,int k,int l)
    /*
      # Abbreviation for specific case of 4-dimensional MDA.
     */
    {
	if (dim>4) {return(false);}
	int[] idx = new int[4];
	idx[0]=i;idx[1]=j;idx[2]=k;idx[3]=l;
	return(chk(idx));
    }

    /*
      # Abbreviation for specific case of 1-dimensional MDA.
     */
    /*
      # Abbreviation for specific case of 2-dimensional MDA.
     */
    /*
      # Abbreviation for specific case of 3-dimensional MDA.
     */
    /*
      # Abbreviation for specific case of 4-dimensional MDA.
     */

}
