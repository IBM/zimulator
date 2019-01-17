package com.ibm.Zimulator.Zimulator;

import java.util.*;
import java.io.*;
import com.ibm.Zimulator.SmallAux.*;
import com.ibm.Zimulator.SmallAux.FA.*;

interface CompiledFileRW 
/*
  All of these are meaningful for zobjects.
  First three are meaningful for all filed objects.
  
  For full explanation of Object files, see 'CompiledFileIO'
*/
{

    public void WriteToObjFile(PrimitiveDataOut D)  throws IOException;
    public void ReadFromObjFile(ArrayList<ObjRef> ORL,PrimitiveDataIn D)  throws IOException;
    public boolean ResolveObjRefs(ResolveRef RR,ObjRef OR,zsystem Ψ);
    public void SetFileRefNumber(int n);  // If can be referenced, set the ref. integer.
    public int GetFileRefNumber();   // Just return 0 if cannot be referenced.

}

interface ResolveRef
/*
  Useful in λ-expressions for ResolveObjRefs().
*/
{
    public Object getref(int idx);
}

class ObjRef
/*
  Maybe would also be used in input parser, but not likely.
  Gets used in object-file loading so we need not keep references around forever.

  For full explanation of Object files, see 'CompiledFileIO'
 */
{
    Object O;   // zbox,zstop,Route,etc. which contains references to resolve.
    int C;      // Ref. Idx for O.
    FA_i Rafs; // some references to others. | Very ad-hoc. Differs for each object; 
    FA_i Rifs; // more references to others. | consistency only required between 
    FA_i Rufs; // more references to others. | WriteToObjFile() and ReadFromObjFile().
    public ObjRef(int RefNum,Object _O)
    {
	O = _O;
	C = RefNum;
	Rafs=null;
	Rifs=null;
	Rufs=null;
	//Str.printE("------------------------------| #543#" + C);
    }
    public ObjRef(Object _O)
    {
	O = _O;
	C = 0;       // for an object which cannot be referenced, e.g. Route.
	Rafs=null;
	Rifs=null;
	Rufs=null;
    }
}

