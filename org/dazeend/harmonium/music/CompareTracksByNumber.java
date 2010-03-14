package org.dazeend.harmonium.music;

import java.util.Comparator;



/**
 * Compares two tracks and sorts them by their track number.
 * 
 * @throws NullPointerException
 */
public class CompareTracksByNumber implements Comparator<PlayableTrack> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(PlayableTrack track1, PlayableTrack track2) {
		
		// If one of the albums is null, then throw an exception
		if(track1 == null || track2 == null) {
			throw new NullPointerException();
		}
		
		// Sort first by track number if track numbers have been set.
		// If only one track has a number set, it should come before.
		if(track1.getTrackNumber() != 0 && track2.getTrackNumber() == 0) {
			return -1;
		}
		else if (track1.getTrackNumber() == 0 && track2.getTrackNumber() != 0) {
			return 1;
		}
		else if(track1.getTrackNumber() > track2.getTrackNumber()) {
			return 1;
		}
		else if(track1.getTrackNumber() < track2.getTrackNumber()) {
			return -1;
		}
		
		// If we made it here, then tracks have track numbers that are equal.
		// Sort secondly by track name. If only one track has a name set, it should come before.
		if( (! track1.getTrackNameTitleSortForm().equals("") ) && track2.getTrackNameTitleSortForm().equals("") ) {
			return -1;
		}
		else if(track1.getTrackNameTitleSortForm().equals("") && (! track2.getTrackNameTitleSortForm().equals("") ) ) {
			return 1;
		}
		else if( (! track1.getTrackNameTitleSortForm().equals("") ) && (! track2.getTrackNameTitleSortForm().equals("") ) ) {
			return track1.getTrackNameTitleSortForm().compareTo(track2.getTrackNameTitleSortForm());
		}
		
		return 0;
	}

}
