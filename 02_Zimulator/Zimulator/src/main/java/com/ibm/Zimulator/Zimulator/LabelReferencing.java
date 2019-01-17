package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

class LabelReferencing
{
    // Used to convert all labels to integers:
    private SBHH AllLabels; 
    private ArrayList<zobject> AllLabeled;

    public LabelReferencing()
    {
	AllLabels = new SBHH();
	AllLabeled = new ArrayList<zobject>();
	AllLabels.getidx("!ZZZ_INVALID_0_ENTRY!");  // 0 is reserved. (punctuation is not supported in labels)
    }


    /*
      Returns a label index, in any case.
      Generate one if need be.

      Call this when any reference is made to an object.
     */
    public int ReferenceLabel(String Label)
    {
	if (Label==null) {return(0);}  // Sometimes Label is null. Not every object is labelled.
	// String[] lab = Str.split0(Label,"_"); // _ is separator for now.
	// This is how we refer to a label; we must always add it to the list.
	int idx = AllLabels.getidx(Label);
	return(idx);	
    }

    /*
      This is used to refer to an extant object.
      0 is returned if the label does not reference an object.
      otherwise, the index.
      **Not that this does not confirm only the existence of the Label; the OBJECT must exist.**
    */
    public int CheckLabelObj(String Label)
    {
	if (Label==null) {return(0);}  // Sometimes Label is null. Not every object is labelled.
	//String[] lab = Str.split0(Label,"_"); // _ is separator for now.

	int idx = AllLabels.chkidx(Label);
	if (idx==-1) {return(-1);}
	
	if (idx<AllLabeled.size())
	    {
		if (AllLabeled.get(idx)!=null)
		    {
			// This Label is used by a zobject.
			return(idx);
		    }
	    }  
	return(-1);
    }

    public void DumpAllLabels()
    { // for debugging.
	for (int pp=0;pp<AllLabels.size();pp++)
	    {
		String Used="-";
		int idx=pp;
		if (idx<AllLabeled.size())
		    {
			if (AllLabeled.get(idx)!=null)
			    {
				Used="*";
			    }
		    }
		Str.printE("## #500#" + pp + " #432#" + AllLabels.getkey(pp) + " #555*#" + Used);
	    }
    }

    public int DefineLabel(String Label,zobject zo)
    /*
      This is called every time a label is defined by a loaded zobject.
      Therefore, the Label should not be one which is already used by an object in the system.

      Labels get an index whenever they are used or referenced. 
      The way to check for redefinition is to wee if an actual object is associated.

      We might modify later to allow re-definition, 
      so that objects can be loaded with replacement.

      0 is returned if the Label is absent.
      -1 is returned if a zobject already uses this Label.
     */
    { 
	if (Label==null) {return(0);}  // Fine. zo cannot be referenced by Label then.

	/*
	  We split via _ right now. This could be modified later to be better.
	*/
	//String[] lab = Str.split0(Label,"_");
	int idx = AllLabels.getidx(Label);
	/*
	  Is this idx already associated with a zobject?
	 */
	if (idx<AllLabeled.size())
	    {
		if (AllLabeled.get(idx)!=null)
		    {
			// This Label is used by a zobject.
			return(-1);
		    }
	    }  
	while (AllLabeled.size()<=idx) {AllLabeled.add(null);}
	AllLabeled.set(idx,zo);
	return(idx);
	
	// Olde way:
	/*
	{
	    int checkidx = NoDupeLabels.getidx(lab);
	    if (checkidx != (NoDupeLabels.length-1))
		{
		    // Not a new label, since not last in list.
		    return(-1);
		}
	}
	*/

	/* debugging
	if (Str.equals(Label,"Peter"))
	    {
		Str.print("::::::::DEF::::::::::::"+ Label + " ---> " + idx);
	    }
	*/
    }

    public zobject ZobjByLabel(String Label)
    {
	return(ZobjByLabelIdx(CheckLabelObj(Label)));
    }

    public zobject ZobjByLabelIdx(int LabelIdx)
    {
	if (LabelIdx<1) {return(null);}
	if (LabelIdx>=AllLabeled.size()) {return(null);}
	return(AllLabeled.get(LabelIdx));
    }
    
}
