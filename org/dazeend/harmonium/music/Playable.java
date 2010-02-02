package org.dazeend.harmonium.music;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.screens.NowPlayingScreen;


/**
 * The public interface for all tracks that can be played. This interface must be implemented for each type
 * of file that is to be played (MP3, AAC, MP3, OggVorbis, etc.) 
 */


public interface Playable extends PlaylistEligible, AlbumArtListItem {

	/**
	 * Enumerates image types that TiVo can natively display.
	 */
	public enum TivoImageFormat {
		GIF		("image/gif"),
		JPEG	("image/jpeg"),
		JPG		("image/jpg"),
		PNG		("image/png");
		
		private final String mimeType;
		
		TivoImageFormat(String mimeType) {
			this.mimeType = mimeType;
		}
		
		public String getMimeType() {
			return this.mimeType;
		}
	}
	
	/**
	 * Gets name of album artist for the album that this track comes from.
	 * 
	 * @return	a string containing the album artist for this track
	 */
	public String getAlbumArtistName();
	
	/**
	 * Gets name of the artist of this track.
	 * 
	 * @return	a string containing the artist of this track
	 */
	public String getArtistName();
	
	/**
	 * Gets name of the album that this track comes from.
	 * 
	 * @return	a string containing the name of album for this track
	 */
	public String getAlbumName();
	
	/**
	 * Gets the name of this track.
	 * 
	 * @return	a string containing the name of this track
	 */
	public String getTrackName();
	
	/**
	 * Gets the duration of the track in milliseconds. If the duration of
	 * the track is unknown, it returns -1;
	 * 
	 * @return	the duration of the track in milliseconds
	 */
	public long getDuration();
	
	/**
	 * Gets the name of this track in title sort form. For example, "A Hard Day's Night" is becomes "Hard Day's Night, A".
	 * 
	 * @return	a string containing the name of this track in title sort format
	 */
	public String getTrackNameTitleSortForm();
	/**
	 * Gets the path to the track on disk relative to the music root.
	 * 
	 * @return	the path to the track on disk
	 */
	public String getPath();
	
	/**
	 * Gets filename of track on disk.
	 */
	public String getFilename();
	
	/**
	 * Gets whether music item has album art
	 * 
	 * @return
	 */
	public boolean hasAlbumArt(FactoryPreferences prefs);
	
	/**
	 * Gets an <code>Image</code> containing album art for this track.
	 * 
	 * @return	album art for this track
	 */
	public Image getAlbumArt(FactoryPreferences prefs);
	
	/**
	 * Gets an Image containing scaled album art for this track.
	 * 
	 * @param width		The maximum width of the scaled image
	 * @param height	The maximum height of the scaled image
	 * @return
	 * @throws IOException 
	 */
	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height);
	
	/**
	 * Gets the track number of this track.
	 * 
	 * @return	the track number for this track
	 */
	public int getTrackNumber();
	
	/**
	 * Gets the year that this track's album was released.
	 * 
	 * @return	the release year for this track
	 */
	public int getReleaseYear();
	
	/**
	 * Gets the disc number for this track.
	 * 
	 * @return	the disc number for this track
	 */
	public int getDiscNumber();
	
	/**
	 * Gets the rating for this track.
	 * 
	 * @return the rating for this track.
	 */
	public RatingLevel getRating();
	
	/**
	 * Increases the rating of this track by one increment.
	 * 
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public void increaseRating();
	
	/**
	 * Decreases the rating of this track by one increment.
	 * 
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public void decreaseRating();

	/**
	 * Compares equality of this track with another object. The other object is equal to this track if and only
	 * if the other object has the same class and refers to the same object on disk.
	 * 
	 * @param obj	the object to which this track should be compared
	 * @return		<code>true</code> if equal, otherwise <code>false</code>
	 */
	public boolean equals(Object obj);
	
	/**
	 * Returns an indentifying hash code for this object.
	 * 
	 * @return	a hash code
	 */
	public int hashCode();
	
	/**
	 * Pauses playback of this track.
	 * 
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public boolean pause(NowPlayingScreen nowPlayingScreen);
	
	/**
	 * Resumes playback of this track.
	 * 
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public boolean unpause(NowPlayingScreen nowPlayingScreen);
	
	/**
	 * Initiates play of this track.
	 * 
	 * @param nowPlayingScreen
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public boolean play(NowPlayingScreen nowPlayingScreen);
	
	/**
	 * Sets the play speed for an already playing track.
	 */
	public boolean setPlayRate(NowPlayingScreen nowPlayingScreen, float speed);
	
	/**
	 * Permanently stops play of this track.
	 */
	public boolean stop(NowPlayingScreen nowPlayingScreen);
	
	/**
	 *  Gets the File object that represents this track on disk.
	 *  
	 *  @return a File that represents this track on disk
	 */
	public File getTrackFile();

}
