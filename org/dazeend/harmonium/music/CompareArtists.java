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
public class CompareArtists implements Comparator<BaseArtist> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(BaseArtist artist1, BaseArtist artist2) {
		
		// If one of the albums is null, then throw an exception
		if(artist1 == null || artist2 == null) {
			throw new NullPointerException();
		}
		
		// Sort by name. If only one album has a name set, it should come before.
		if( (! artist1.getAlbumArtistNameTitleSortForm().equals("") ) && artist2.getAlbumArtistNameTitleSortForm().equals("")) {
			return -1;
		}
		else if(artist1.getAlbumArtistNameTitleSortForm().equals("") && (! artist2.getAlbumArtistNameTitleSortForm().equals("") ) ) {
			return 1;
		}
		else {
			return artist1.getAlbumArtistNameTitleSortForm().compareToIgnoreCase(artist2.getAlbumArtistNameTitleSortForm());
		}
		
		
	}
}
