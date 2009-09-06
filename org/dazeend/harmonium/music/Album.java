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

import java.awt.Image;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import org.dazeend.harmonium.Harmonium;


/**
 * Represents an album.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 */
/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class Album extends HMusic implements PlaylistEligible, AlbumArtListItem {

	private List<Disc>		discList = Collections.synchronizedList( new ArrayList<Disc>() );
	private List<Playable>	trackList = Collections.synchronizedList( new ArrayList<Playable>() );
	private String			albumArtistName = "";	// Set only through constructor. Setting later could break data structure.
	private String			albumArtistNameTitleSortForm;
	private String 			albumName = "";			// Set only through constructor. Setting later could break data structure.
	private String			albumNameTitleSortForm = ""; 
	private int				releaseYear;
	private	AlbumReadable	artSource;				// the object to pull album art from
	
	/**
	 * Creates album and sets key fields.
	 * 
	 * @param albumArtistName	the album's artist
	 * @param albumName			the name of the album
	 */
	public Album(String albumArtistName, String albumName) {
		super();
		this.albumArtistName = albumArtistName;
		this.albumName = albumName;
		
		// Reformat the album artist name into sort form
		Matcher stringMatcher = titlePattern.matcher(albumArtistName);
		if(stringMatcher.lookingAt()) {
			// Found a leading article. Move it to the end of the string.
			this.albumArtistNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
		}
		else {
			this.albumArtistNameTitleSortForm = albumName;
		}
		
		// Reformat the album name into sort form
		stringMatcher = titlePattern.matcher(albumName);
		if(stringMatcher.lookingAt()) {
			// Found a leading article. Move it to the end of the string.
			this.albumNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
		}
		else {
			this.albumNameTitleSortForm = albumName;
		}
		
		
	}

	/**
	 * Adds a disc as a member of the <code>Album</code>. Checks to ensure that the disc is not already
	 * a member, and if it is not adds it to the album.
	 * 
	 * @param newDisc		the disc to add to the disc
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	public synchronized boolean addDisc(Disc newDisc) {
		
		// Check to ensure that the newDisc is eligible to be a member of this album.
		if((this.albumName.compareToIgnoreCase(newDisc.getAlbumName()) != 0) || (this.albumArtistName.compareToIgnoreCase(newDisc.getAlbumArtistName()) != 0 ) ) {
			return false;
		}
		// Check to ensure that the newDisc is not already a member of the disc.
		if(discList.contains(newDisc)) {
			return false;
		}
		
		// If we got this far, then the disc is not yet in the disc, so add it.
		if(discList.add(newDisc)) {
			// The disc was successfully added. Copy metadata (if needed) and return TRUE.
			if(this.releaseYear == 0) {
				this.releaseYear = newDisc.getReleaseYear();
			}
			
			if(this.artSource == null && newDisc.hasAlbumArt()) {
				artSource = newDisc;
			}
			
			return true;
		}
		else {
			// The disc was not added, so return FALSE.
			return false;
		}
	}
	
	/**
	 * Removes a track from an album. Deletes track and any newly-empty objects it was a member of.
	 * 
	 * @param track
	 */
	public synchronized void removeTrack(Playable track) {
		// See if track belongs to a disc.
		int trackDisc = track.getDiscNumber();
		if(trackDisc != 0) {
			
			// track belongs to a disc. See if the disc is a member of this album.
			Iterator<Disc> discIterator = this.discList.iterator();
			while( discIterator.hasNext() ) {
				Disc disc = discIterator.next();
			
				if( disc.getDiscNumber() == trackDisc ) {
					// The disc is a member of this album, so delete the track from the disc.
					disc.removeTrack(track);
					
					// Check if the album is empty
					if( disc.getTrackList().isEmpty() ) {
						// It's empty, so delete the disc via its iterator.
						discIterator.remove();
					}
						
				}
			}
		}
		else {
			// The track is not part of an album, so just delete it from the list
			this.trackList.remove(track);
		}
	}
	/**
	 * Adds a track as a member of the <code>Album</code>. Checks to ensure that the track is not already
	 * a member, and if it is not adds it to the album.
	 * 
	 * @param newTrack		the track to add to the disc
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	public synchronized boolean addTrack(Playable newTrack) {
		// Check to ensure that the newTrack is eligible to be a member of this album.
		if( albumName.compareToIgnoreCase(newTrack.getAlbumName()) != 0 || albumArtistName.compareToIgnoreCase(newTrack.getAlbumArtistName()) != 0 ) {
			return false;
		}
		
		// See if newTrack belongs to a disc.
		int newTrackDiscNumber = newTrack.getDiscNumber();
		if(newTrackDiscNumber != 0) {
			
			// newTrack belongs to a disc. See if the disc is already a member of this album.
			for( Disc disc : this.discList) {
				
				if( disc.getDiscNumber() == newTrackDiscNumber ) {
					// The disc is already a member of this album, so add newTrack to the disc.
					if(disc.addTrack(newTrack)) {
						// The track was successfully added. Return TRUE.
						return true;
					}
					else {
						// There was an error in adding the track.
						return false;
					}	
				}
			}
			
			// The disc is not yet a member of this album.
					
			// Create a new disc to hold the newTrack
			Disc newDisc = new Disc( newTrack.getAlbumArtistName(), newTrack.getAlbumName(), newTrack.getDiscNumber() );
					
			
					
			// Add the newTrack to the newDisc
			if( newDisc.addTrack(newTrack) ) {
				// the track was added to the disc, so add the disc to this album
				if( this.addDisc(newDisc) ) {
					
					// the disc was successfully added
					return true;
				}
				else {
					// There was an error in adding the new disc to this album
					return false;
				}
			}
			else {
				// There was an error in adding the track to the disc
				return false;
			}
		}
		else {
			// newTrack does not belong to a disc.
			// Check to ensure that the newTrack is not already a direct member of this album.
			if(this.trackList.contains(newTrack)) {
				return false;
			}
	
			// If we got this far, then the track is not yet in this album as a direct member, so add it.
			if(this.trackList.add(newTrack)) {
				// The disc was successfully added. Copy metadata (if needed) and return TRUE.
				if(this.releaseYear == 0) {
					this.releaseYear = newTrack.getReleaseYear();
				}
				
				if(this.artSource == null && newTrack.hasAlbumArt()) {
					artSource = newTrack;
				}
				
				return true;
			}
			else {
				// there was an error in adding the track as a direct member of this album
				return false;
			}
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
		final Album other = (Album) obj;
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
		return true;
	}

	/**
	 * Gets album art.
	 * 
	 * @return the albumArt
	 */
	public Image getAlbumArt() {
		if(this.hasAlbumArt()) {
			return this.artSource.getAlbumArt();
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
	public Image getScaledAlbumArt(int width, int height) {
		if(this.hasAlbumArt()){
			return this.artSource.getScaledAlbumArt(width, height);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Checks existance of album art for this object.
	 */
	public boolean hasAlbumArt() {
		if(this.artSource != null && this.artSource.hasAlbumArt()) {
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Gets album artist.
	 * 
	 * @return the albumArtistName
	 */
	public String getAlbumArtistName() {
		return albumArtistName;
	}

	/**
	 * Gets name of album.
	 * 
	 * @return the albumName
	 */
	public String getAlbumName() {
		return albumName;
	}

	/**
	 * Gets a list of the discs that are members of this album.
	 * 
	 * @return the discList
	 */
	public List<Disc> getDiscList() {
		return discList;
	}

	/**
	 * Gets year album was released.
	 * 
	 * @return the releaseYear
	 */
	public int getReleaseYear() {
		return releaseYear;
	}

	/**
	 * Gets a list of tracks that are direct members of this album.
	 * 
	 * @return the trackList
	 */
	public List<Playable> getTrackList() {
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
		return result;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public List<Playable> listMemberTracks(Harmonium app) {
		
		List<Playable> outputList = new ArrayList<Playable>();
 		
 		// Get tracks from each member disc and add them to the output list
		List<Disc> sortedDiscList = discList;
		Collections.sort(sortedDiscList, new CompareDiscs());
		for(Disc disc : sortedDiscList) {
 			outputList.addAll(disc.listMemberTracks(app));
 		}
		
 		// Get tracks that are direct members of this album
 		List<Playable> sortedTrackList = new ArrayList<Playable>();
		sortedTrackList.addAll(trackList);
		
		if(app != null) {
			Collections.sort(sortedTrackList, app.getPreferences().getAlbumTrackComparator());
		}
		outputList.addAll(sortedTrackList);
		
		return outputList;
	}
	
	/**
	 * Prints members of this object. Used for debugging.
	 * 
	 * @param outputStream
	 */
	protected void printMusic(PrintStream outputStream) {
		outputStream.println("== Album: " + this.toString());
		outputStream.flush();
		for(Disc disc : this.discList) {
			disc.printMusic(outputStream);
		}
		
		for(Playable track : this.trackList) {
			outputStream.println("=== Track: " + track.getPath());
		}
		outputStream.flush();
	}

	@Override
	public String toString() {
		if(this.albumName == null) {
			return "";
		}
		return this.albumName;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#toStringTitleSortForm()
	 */
	public String toStringTitleSortForm() {
		if(this.albumNameTitleSortForm == null) {
			return "";
		}
		return this.albumNameTitleSortForm;
	}

	/**
	 * @return the albumNameTitleSortForm
	 */
	public String getAlbumNameTitleSortForm() {
		return albumNameTitleSortForm;
	}

	/**
	 * @return the albumArtistNameTitleSortForm
	 */
	public String getAlbumArtistNameTitleSortForm() {
		return albumArtistNameTitleSortForm;
	}

	public String getDisplayArtistName()
	{
		return getAlbumArtistName();
	}
	
	
	
	
}
