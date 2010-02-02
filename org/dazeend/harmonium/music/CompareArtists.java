package org.dazeend.harmonium.music;

import java.util.Comparator;

/**
 * @throws NullPointerException
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
