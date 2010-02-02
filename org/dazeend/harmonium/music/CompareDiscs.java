package org.dazeend.harmonium.music;

import java.util.Comparator;

/**
 * @throws NullPointerException
 */
public class CompareDiscs implements Comparator<Disc> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Disc disc1, Disc disc2) {
		
		// If one of the discs is null, then throw an exception
		if(disc1 == null || disc2 == null) {
			throw new NullPointerException();
		}
		
		// Sort by disc number
		if(disc1.getReleaseYear() < disc2.getDiscNumber()) {
			return -1;
		}
		else if(disc1.getDiscNumber() > disc2.getDiscNumber()) {
			return 1;
		}
		
		// If we made it this far, then they are equal.
		return 0;
	}
}
