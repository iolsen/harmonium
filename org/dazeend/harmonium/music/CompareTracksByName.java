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
 * Sorts tracks by name.
 * 
 * @author	Charles Perry (harmonium@DazeEnd.org)
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
			return track1.getTrackNameTitleSortForm().compareTo(track2.getTrackNameTitleSortForm());
		}
		
		// neither track has a title set, so compare by filename.
		return track1.getFilename().compareTo( track2.getFilename() );		
	}

}
