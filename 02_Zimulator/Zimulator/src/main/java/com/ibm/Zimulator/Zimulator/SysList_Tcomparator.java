
package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;


class SysList_Tcomparator implements Comparator<zobject>
{
    public int compare(zobject zobx,zobject zoby)
    {
	if (zobx == zoby) {return (0);}  // The same object.
	// Compare() must be anticommutative when the objects are non-identical.
	// ( in order for the TreeSet to be a Set )
	if (zobx.slt < zoby.slt ) {return(-1);}
	if (zobx.slt > zoby.slt ) {return(+1);}
	// Forget all that below. Just use the zobject's uid.
	if (zobx.uid != zoby.uid) { return ( zobx.uid < zoby.uid ? -1 : +1 ); }

	if ((zobx.Label!=null) && (zoby.Label!=null)) { return(zobx.Label.compareTo(zoby.Label)); }

	/*
	  int hx= zobx.hashCode();
	  int hy= zoby.hashCode();
	  if (hx!=hy) {return ( hx<hy ? -1 : +1 );}
	*/
	    
	return(0);  // :-(
	    
    }
}
