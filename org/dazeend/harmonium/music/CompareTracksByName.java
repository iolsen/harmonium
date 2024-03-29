package org.dazeend.harmonium.music;

import java.util.Comparator;


/**
 * Sorts tracks by name.
 * 
 * @throws	NullPointerExecption
 */
public class CompareTracksByName implements Comparator<PlayableTrack> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(PlayableTrack track1, PlayableTrack track2) {
		
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
		
		return 0;
	}

}
