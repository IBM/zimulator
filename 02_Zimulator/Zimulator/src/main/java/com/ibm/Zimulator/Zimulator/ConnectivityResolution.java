package com.ibm.Zimulator.Zimulator;

import java.util.*;
import com.ibm.Zimulator.SmallAux.*;

class ConnectivityResolution
{

    public static void zpaths_zlinks_zboxen(ArrayList<zobject> ListInclZpaths)
    /*
      zbox lists paths which stop there.   zbox.PathsWhoStopHere
      zpath lists followers.               zpath.Followers
      zbox lists zlinks.                   zbox.zlinklist

      This is called once after loading; otherwise such lists are maintained, not generated.
    */
    {
	Iterator<zobject> wit = ListInclZpaths.listIterator();
	while (wit.hasNext())
	    {
		zobject zo = wit.next();
		if (zo instanceof zpath)
		    {
			zpath P = (zpath) zo;
			P.AddPathRefsToZboxen();
		    }
		if (zo instanceof zlink)
		    {
			zlink χ = (zlink) zo;
			χ.AddLinkRefsToZboxen();
		    }
		if (zo instanceof zbox)
		    {
			zbox φ = (zbox) zo;
			if (φ.P!=null)
			    {
				if (φ.P.Followers==null) {φ.P.Followers = new HashSet<zbox>();}
				if (!φ.P.Followers.contains(φ))
				    {
					φ.P.Followers.add(φ);					
				    }
			    }
		    }
	    }
    }


    
    public static void zboxen_containment(ArrayList<zobject> LOB,boolean Verbose, Containment_Controller ZS_ConCon)
    /*
      zbox φ has field z (in whom φ is contained) and field Z (list of who is contained in φ).
      They might be only partially specified in input files (which is fine).
      Update all Z and z to reflect the other.
    */
    {
	if (Verbose) {Str.printn("# zboxen: ");}
	Iterator<zobject> wit = LOB.listIterator();
	while(wit.hasNext())
	    {
		zobject zo = wit.next();
		if (zo instanceof zbox)
		    {
			zbox φ = (zbox) zo; // No better than a C *void anyway... :-/
			// z,Z
			if (φ.z!=null)
			    {
				if (φ.z.Z==null) {φ.z.Z=new ContainmentList(ZS_ConCon);}
				if (!φ.z.Z.contains(φ))
				    {
					if (Verbose)
					    {
						Str.printnrgb(" " + φ.z.Label + ".Z ∍ " + φ.Label +" ; ",0,3,0);
					    }
					φ.z.Z.add(φ);
				    }
			    }
			if (φ.Z!=null)
			    {
				Iterator<zbox> wif = φ.Z.iterator();
				while (wif.hasNext())
				    {
					zbox _zi = wif.next();
					_zi.z = φ;
					if (Verbose)
					    {
						Str.printnrgb(" " + φ.Label +" ∊ " + _zi.Label + ".Z ; ",0,2,1);
					    }
				    }
			    }
		    }
	    }
	if (Verbose)
	    {
		Str.print("");
	    }
    }


    public static void DumpNonSavedZboxListFields(String Pref, ArrayList<zobject> zobs)
    {
	Iterator<zobject> wif = zobs.iterator();
	while (wif.hasNext())
	    {
		zobject zo = wif.next();
		//if (zo instanceof zbox)
		    {
			//zbox φ = (zbox) zo;
			Str.printE(Pref + "#543#zbox: #321#" + zo.toString());
		    }		
	    }
    }    


    
}

