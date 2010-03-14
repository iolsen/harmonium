package org.dazeend.harmonium.music;

import java.io.File;

/**
 * The public interface for all tracks that can be played from local files.
 * It should be implemented by each file type to play (MP3, AAC, MP3, OggVorbis, etc.) 
 */
public interface PlayableLocalTrack extends PlayableTrack, PlayableRateChangeable
{
	/**
	 * Gets filename of track on disk.
	 */
	public String getFilename();
	
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
	 *  Gets the File object that represents this track on disk.
	 *  
	 *  @return a File that represents this track on disk
	 */
	public File getTrackFile();

}
