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
 * @author Charles Perry (harmonium@DazeEnd.org)
 * @throws NullPointerException
 *
 */
public class CompareAlbumsByYear implements Comparator<Album> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Album album1, Album album2) {
		
		// If one of the albums is null, then throw an exception
		if(album1 == null || album2 == null) {
			throw new NullPointerException();
		}
		
		// Sort by album release year.
		Integer year1 = album1.getReleaseYear();
		Integer year2 = album2.getReleaseYear();
		
		if(year1 != year2) {
			// If one of the albums doesn't have a release year set (releaseYear == 0), then it should come after
			if(year1 == 0) {
				// Album1 doesn't have a year set. It should come after.
				return 1;
			}
			else if(year2 == 0) {
				// Album2 doesn't have a year set. It should come after.
				return -1;
			}
			else {
				// Both albums have years set (which are different), so compare them.
				return year1.compareTo(year2);
			}
		}
		
		// Sort finally by album name. If only one album has a name set, it should come before.
		if( (! album1.getAlbumNameTitleSortForm().equals("") ) && album2.getAlbumNameTitleSortForm().equals("")) {
			return -1;
		}
		else if(album1.getAlbumNameTitleSortForm().equals("") && (! album2.getAlbumNameTitleSortForm().equals("") ) ) {
			return 1;
		}
		
		// compare strings in title order
		return album1.getAlbumNameTitleSortForm().compareTo(album2.getAlbumNameTitleSortForm());
	}

}
