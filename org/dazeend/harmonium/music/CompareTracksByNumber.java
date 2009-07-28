/*
 * Copyright 2008 Charles Perry
 *
 * This file is part of Harmonium, the TiVo music player.
 *
 * Harmonium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Harmonium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
package org.dazeend.harmonium.music;

import java.util.Comparator;



/**
 * Compares two tracks and sorts them by their track number.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 * @throws NullPointerException
 */
public class CompareTracksByNumber implements Comparator<Playable> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Playable track1, Playable track2) {
		
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
		
		// Sort thirdly by track filename
		return track1.getFilename().compareTo( track2.getFilename() );		
	}

}
