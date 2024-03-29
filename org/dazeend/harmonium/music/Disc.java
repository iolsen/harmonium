package org.dazeend.harmonium.music;

import java.awt.Image;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;


/**
 * Represents a sub-unit of an album.
 */
public class Disc implements PlayableCollection, AlbumArtListItem {

	private List<PlayableLocalTrack>	trackList = Collections.synchronizedList( new ArrayList<PlayableLocalTrack>() );
	private int 			discNumber;					// Set only through constructor. Setting later could break data structure.
	private String			albumArtistName = "";		// Set only through constructor. Setting later could break data structure.
	private String 			albumName = "";				// Set only through constructor. Setting later could break data structure.
	private int				releaseYear;
	private	AlbumReadable	artSource;					// the object to pull art from
	
	
	
	/**
	 * Creates Disc object and sets key values.
	 * 
	 * @param albumArtistName	the album artist of this disc
	 * @param albumName			the album that this disc belongs to
	 * @param discNumber		the number of this disc in the list
	 */
	public Disc(String albumArtistName, String albumName, int discNumber) {
		super();
		this.albumArtistName = albumArtistName;
		this.albumName = albumName;
		this.discNumber = discNumber;
	}

	/**
	 * Removes a track from a disc.
	 * 
	 * @param track
	 */
	public synchronized void removeTrack(PlayableLocalTrack track) {
		// Remove the track from this disc.
		this.trackList.remove(track);
	}
	
	/**
	 * Adds a track as a member of the <code>Disc</code>. Checks to ensure that the track is not already
	 * a member, and if it is not adds it to the Disc.
	 * 
	 * @param newTrack		the track to add to the disc
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	public synchronized boolean addTrack(FactoryPreferences prefs, PlayableLocalTrack newTrack) {
		
		// Check to ensure that the newTrack is eligible to be a member of this disc.
		if(discNumber != newTrack.getDiscNumber() || (albumName.compareToIgnoreCase(newTrack.getAlbumName()) != 0) || (albumArtistName.compareToIgnoreCase(newTrack.getAlbumArtistName()) != 0) ) {
			return false;
		}
		
		// Check that the track is not already in the disc
		if(trackList.contains(newTrack)) {
			return false;
		}
		
		// If we got this far, then the track is not yet in the disc and it is eligible, so add it.
		if(trackList.add(newTrack)) {
			// The track was successfully added. Copy metadata (if needed) and return TRUE.
			if(this.artSource == null && newTrack.hasAlbumArt(prefs)) {
				artSource = newTrack;
			}

			if(this.releaseYear == 0) {
				releaseYear = newTrack.getReleaseYear();
			}
			return true;
		}
		else {
			// The track was not added, so return FALSE.
			return false;
		}
	}


	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Disc other = (Disc) obj;
		if (albumArtistName == null) {
			if (other.albumArtistName != null)
				return false;
		} else if (albumArtistName.compareToIgnoreCase(other.albumArtistName) != 0)
			return false;
		if (albumName == null) {
			if (other.albumName != null)
				return false;
		} else if (albumName.compareToIgnoreCase(other.albumName) != 0)
			return false;
		if (discNumber != other.discNumber)
			return false;
		return true;
	}
	
	/**
	 * Checks existance of album art for this object.
	 */
	public boolean hasAlbumArt(FactoryPreferences prefs) {
		if(this.artSource != null && this.artSource.hasAlbumArt(prefs)) {
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Gets the album art for the disc.
	 * 
	 * @return	an <code>Image</code> object that contains album art for the disc
	 */
	public Image getAlbumArt(FactoryPreferences prefs) {
		if(this.hasAlbumArt(prefs)) {
			return this.artSource.getAlbumArt(prefs);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets an Image containing scaled album art for this disc.
	 * 
	 * @param width		The maximum width of the scaled image
	 * @param height	The maximum height of the scaled image
	 * @return
	 */
	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height) {
		if(this.hasAlbumArt(prefs)) {
			return this.artSource.getScaledAlbumArt(prefs, width, height);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets album artist of the <code>Disc</code>.
	 *
	 * @return		the album artist of the disc
	 */
	public String getAlbumArtistName() {
		return albumArtistName;
	}
	
	/**
	 * Gets the album that this <code>Disc</code> belongs to.
	 * 
	 * @return		the name of the album of this disc
	 */
	public String getAlbumName() {
		return albumName;
	}

	/**
	 * Returns disc number of the disc.
	 * 
	 * @return	the disc number of the disc
	 */
	public int getDiscNumber() {
		return discNumber;
	}
	
	/**
	 * Gets the year the album containing the disc was released.
	 * 
	 * @return	an <code>int</code> representing the year the album containing the disc was released
	 */
	public int getReleaseYear() {
		return releaseYear;
	}

	/**
	 * Gets the raw list of member tracks.
	 * 
	 * @return the trackList
	 */
	public List<PlayableLocalTrack> getTrackList() {
		return trackList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((albumArtistName == null) ? 0 : albumArtistName.hashCode());
		result = PRIME * result + ((albumName == null) ? 0 : albumName.hashCode());
		result = PRIME * result + discNumber;
		return result;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public List<PlayableLocalTrack> getMembers(Harmonium app) 
	{
		List<PlayableLocalTrack> sortedTrackList = new ArrayList<PlayableLocalTrack>(trackList.size());
		sortedTrackList.addAll(trackList);
		
		if (app != null)
			Collections.sort(sortedTrackList, app.getPreferences().getDiscTrackComparator());
		
		return sortedTrackList;
	}
	
	/**
	 * Prints members of this object. Used for debugging.
	 * 
	 * @param outputStream
	 */
	protected void printMusic(PrintStream outputStream) {
		outputStream.println("=== Disc: " + this.toString());
		for(PlayableLocalTrack track : this.trackList) {
			outputStream.println("==== Track: " + track.getURI());
		}
		outputStream.flush();
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public String toStringTitleSortForm() {
		String title = "Disc " + discNumber;
		return title;
	}

	@Override
	public String toString() {
		return this.toStringTitleSortForm();
	}

	public String getDisplayArtistName()
	{
		return getAlbumArtistName();
	}

	public String getArtHashKey()
	{
		return artSource.getArtHashKey();
	}
}
