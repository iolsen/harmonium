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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dazeend.harmonium.Harmonium;


/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class AlbumArtist implements PlaylistEligible {

	private List<Album>		albumList = Collections.synchronizedList( new ArrayList<Album>() );
	private List<Playable>	trackList = Collections.synchronizedList( new ArrayList<Playable>() );
	private String			albumArtistName = "";	// Set only through constructor. Setting later could break data structure.
	private String			albumArtistNameTitleSortForm = "";
	
	/**
	 * Creates album artist and initialized key metadata.
	 * 
	 * @param albumArtistName	the name of the artist that recorded this album
	 */
	public AlbumArtist(String albumArtistName) {
		super();
		this.albumArtistName = albumArtistName;
		
		// Put artist name in title sort form
		// Compile pattern for matching leading articles
		Pattern titlePattern = Pattern.compile("(?i)^(the|a|an)\\s");
		Matcher stringMatcher = titlePattern.matcher(this.albumArtistName);
		if(stringMatcher.lookingAt()) {
			// Found a leading article. Move it to the end of the string.
			this.albumArtistNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
		}
		else {
			this.albumArtistNameTitleSortForm = this.albumArtistName;
		}
	}

	/**
	 * Adds an album as a member of the <code>AlbumArtist</code>. Checks to ensure that the album is not already
	 * a member, and if it is not adds it to the album artist.
	 * 
	 * @param newAlbum		the album to add to the album artist
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	public synchronized boolean addAlbum(Album newAlbum) {
		
		// Check to ensure that the newAlbum is eligible to be a member of this album artist.
		if( this.albumArtistName.compareToIgnoreCase(newAlbum.getAlbumArtistName()) != 0 ) {
			return false;
		}
		// Check to ensure that the newAlbum is not already a member of the album artist.
		if(this.albumList.contains(newAlbum)) {
			return false;
		}
		
		// If we got this far, then the newAlbum is not yet a member of the album artist, so add it.
		if(this.albumList.add(newAlbum)) {
			// The album was successfully added. Return TRUE.
			return true;
		}
		else {
			// The album was not added, so return FALSE.
			return false;
		}
	}

	/**
	 * Removes a track from an album artist. Deletes track and any newly-empty objects it was a member of.
	 * 
	 * @param track
	 */
	public synchronized void removeTrack(Playable track) {
		// See if track belongs to an album.
		String trackAlbum = track.getAlbumName();
		if(! trackAlbum.equals("") ) {
			
			// track belongs to an album. See if the album is a member of this album artist.
			Iterator<Album> albumIterator = this.albumList.iterator();
			while( albumIterator.hasNext() ) {
				Album album = albumIterator.next();
				
				if( album.getAlbumName().compareToIgnoreCase(trackAlbum) == 0 ) {
					// The album is a member of this album artist, so delete the track from the album .
					album.removeTrack(track);
					
					// Check if the album is empty
					if( album.getDiscList().isEmpty() && album.getTrackList().isEmpty() ) {
						// It's empty, so delete the album via its iterator.
						albumIterator.remove();
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
	 * Adds a track as a member of the <code>AlbumArtist</code>. Checks to ensure that the track is not already
	 * a member, and if it is not adds it to the album.
	 * 
	 * @param newTrack		the track to add to the album artist
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	public synchronized boolean addTrack(Playable newTrack) {
		// Check to ensure that the newTrack is eligible to be a member of this album artist.
		if( albumArtistName.compareToIgnoreCase(newTrack.getAlbumArtistName()) != 0 ) {
			return false;
		}
		
		// See if newTrack belongs to an album.
		String newTrackAlbumName = newTrack.getAlbumName();
		if(! newTrackAlbumName.equals("") ) {
			
			// newTrack belongs to an album. See if the album is already a member of this album artist.
			for( Album album : this.albumList) {

				if( album.getAlbumName().compareToIgnoreCase(newTrackAlbumName) == 0 ) {
					// The album is already a member of this album artist, so add newTrack to the album.
					if(album.addTrack(newTrack)) {
						// The track was successfully added. Return TRUE.
						return true;
					}
					else {
						// There was an error in adding the track.
						return false;
					}	
				}
			}
			
			// The album is not yet a member of this music collection.
					
			// Create a new album to hold the newTrack
			Album newAlbum = new Album( newTrack.getAlbumArtistName(), newTrack.getAlbumName() );
					
					
			// Add the newTrack to the newAlbum
			if( newAlbum.addTrack(newTrack) ) {
				// the track was added to the album, so add the album to this music collection
				if( this.addAlbum(newAlbum) ) {

					// the album was successfully added
					return true;
				}
				else {
					// There was an error in adding the new album to the album artist
					return false;
				}
			}
			else {
				// There was an error in adding the track to the new album
				return false;
			}
		}
		else {
			// newTrack does not belong to an album.
			// Check to ensure that the newTrack is not already a direct member of this album artist.
			if(this.trackList.contains(newTrack)) {
				return false;
			}
	
			// If we got this far, then the track is not yet in this album artist as a direct member, so add it.
			if(this.trackList.add(newTrack)) {
				// The track was successfully added. Return TRUE.
				return true;
			}
			else {
				// there was an error in adding the track as a direct member of this music collection
				return false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public List<Playable> listMemberTracks(Harmonium app) {
		
		List<Playable> outputList = new ArrayList<Playable>();
 		
 		// Get tracks from each member album and add them to the output list
		List<Album> sortedAlbumList = this.albumList;
		
		if(app != null) {
			Collections.sort(sortedAlbumList, app.getPreferences().getAlbumComparator());
		}
		
		for(Album album : sortedAlbumList) {
 			outputList.addAll(album.listMemberTracks(app));
 		}
		
 		// Get tracks that are direct members of this album artist
 		List<Playable> sortedTrackList = new ArrayList<Playable>();
		sortedTrackList.addAll(trackList);
		
		if(app != null) {
			Collections.sort(sortedTrackList, app.getPreferences().getAlbumArtistTrackComparator());
		}

		outputList.addAll(sortedTrackList);
		
		return outputList;
 		
	}

	/**
	 * Gets the name of this artist.
	 * 
	 * @return the albumArtistName
	 */
	public String getAlbumArtistName() {
		return albumArtistName;
	}

	/**
	 * Gets the list of albums by this artist.
	 * 
	 * @return the albumList
	 */
	public List<Album> getAlbumList() {
		return albumList;
	}

	/**
	 * Gets the list of direct member tracks associated with this artist.
	 * 
	 * @return the trackList
	 */
	public List<Playable> getTrackList() {
		return trackList;
	}

	/**
	 * Prints members of this object. Used for debugging.
	 * 
	 * @param outputStream
	 */
	protected void printMusic(PrintStream outputStream) {
		outputStream.println("= Album Artist: " + this.toString());
		outputStream.flush();
		
		for(Album album : this.albumList) {
			album.printMusic(outputStream);
		}
		
		for(Playable track : this.trackList) {
			outputStream.println("== Track: " + track.getPath());
		}
		outputStream.flush();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((albumArtistName == null) ? 0 : albumArtistName.hashCode());
		return result;
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
		final AlbumArtist other = (AlbumArtist) obj;
		if (albumArtistName == null) {
			if (other.albumArtistName != null)
				return false;
		} else if (albumArtistName.compareToIgnoreCase(other.albumArtistName) != 0)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public String toStringTitleSortForm() {
		return this.albumArtistNameTitleSortForm;
	}

	@Override
	public String toString() {
		if(this.albumArtistName == null) {
			return "";
		}
		return this.albumArtistName;
	}

	/**
	 * @return the albumArtistNameTitleSortForm
	 */
	public String getAlbumArtistNameTitleSortForm() {
		if(this.albumArtistNameTitleSortForm == null) {
			return "";
		}
		return this.albumArtistNameTitleSortForm;
	}
}
