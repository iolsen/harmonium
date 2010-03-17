package org.dazeend.harmonium.music;

public interface PlayableTrack extends Playable, AlbumArtListItem
{
	/**
	 * Gets name of the artist of this track.
	 * 
	 * @return	a string containing the artist of this track
	 */
	public String getArtistName();
	
	/**
	 * Gets the name of this track.
	 * 
	 * @return	a string containing the name of this track
	 */
	public String getTrackName();
	
	/**
	 * Gets the name of this track in title sort form. For example, "A Hard Day's Night" is becomes "Hard Day's Night, A".
	 * 
	 * @return	a string containing the name of this track in title sort format
	 */
	public String getTrackNameTitleSortForm();

	/**
	 * Gets the track number of this track.
	 * 
	 * @return	the track number for this track
	 */
	public int getTrackNumber();
	
	/**
	 * Gets the disc number for this track.
	 * 
	 * @return	the disc number for this track
	 */
	public int getDiscNumber();
}
