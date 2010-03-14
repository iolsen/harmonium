package org.dazeend.harmonium.music;


/**
 * Defines the interface used to read album information from items in the
 * music collection.
 */
public interface AlbumReadable extends ArtSource
{
	/**
	 * Gets name of album artist for the album that this track comes from.
	 * 
	 * @return	a string containing the album artist for this track
	 */
	public String getAlbumArtistName();
	
	/**
	 * Gets name of the album that this track comes from.
	 * 
	 * @return	a string containing the name of album for this track
	 */
	public String getAlbumName();
	
	/**
	 * Gets the year that this track's album was released.
	 * 
	 * @return	the release year for this track
	 */
	public int getReleaseYear();
}
