    public void TripsIntoBuckets()
    {
	BucketedTrips = new HHH<Bucket>();
	int tripcount=0;
	/*
	  Iterate through all passengers:
	  Every trajectory goes into exactly one bucket.
	*/
	Iterator<Trajectory> wip = AllPax.objIterator();
	while (wip.hasNext())
	    {
		Trajectory T = wip.next();
		if (!T.complete()) {continue;}
		int toi = TB.GetTimeBinIndex(T.t_origin());
		if (toi<0) {continue;}
		String BL = T.O().label + " " + T.D().label + " " + toi;  //bucket index label.
		Bucket B = BucketedTrips.get(BL);
		if (B==null) { B = new Bucket(T.O().label,T.D().label,toi); BucketedTrips.set(BL,B);}
		B.addTrip(T);
		if (0==1)
		    {
			if (V!=null)
			    {
				if ((tripcount % 2000) ==1 )
				    {
					V.hash();
				    }
				if ((tripcount % 80000) ==1 )
				    {
					V.v("Trip #" + tripcount); // + ": ["+BL+"] "+B.n+" "+ T.travel_time() +" trip:" + T.toString());
				    }
			    }
		    }
		tripcount++;
	    }
    }

