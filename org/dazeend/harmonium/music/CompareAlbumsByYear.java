package org.dazeend.harmonium.music;

import java.util.Comparator;

/**
 * @throws NullPointerException
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
