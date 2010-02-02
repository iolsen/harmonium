package org.dazeend.harmonium.music;

import java.util.Comparator;


/**
 * Sorts tracks by name.
 * 
 * @throws	NullPointerExecption
 */
public class CompareTracksByName implements Comparator<Playable> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Playable track1, Playable track2) {
		
		// If one of the tracks is null, then throw an exception
		if(track1 == null || track2 == null) {
			throw new NullPointerException();
		}
		
		// Sort first by track name. If only one track has a name set, it should come before.
		if( (! track1.getTrackNameTitleSortForm().equals("") ) && track2.getTrackNameTitleSortForm().equals("") ) {
			return -1;
		}
		else if(track1.getTrackNameTitleSortForm().equals("") && (! track2.getTrackNameTitleSortForm().equals("") ) ) {
			return 1;
		}
		else if( (! track1.getTrackNameTitleSortForm().equals("") ) && (! track2.getTrackNameTitleSortForm().equals("") ) ) {
			// both tracks have names/titles set, so compare them
			return track1.getTrackNameTitleSortForm().compareToIgnoreCase(track2.getTrackNameTitleSortForm());
		}
		
		// neither track has a title set, so compare by filename.
		return track1.getFilename().compareToIgnoreCase( track2.getFilename() );		
	}

}
