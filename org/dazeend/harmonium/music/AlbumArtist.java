package org.dazeend.harmonium.music;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;

public class AlbumArtist extends BaseArtist {

	private List<Album>		albumList = Collections.synchronizedList( new ArrayList<Album>() );
	
	/**
	 * Creates album artist and initialized key metadata.
	 * 
	 * @param albumArtistName	the name of the artist that recorded this album
	 */
	public AlbumArtist(String albumArtistName) {
		super(albumArtistName);
		
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
		if( this._artistName.compareToIgnoreCase(newAlbum.getAlbumArtistName()) != 0 ) {
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
			_trackList.remove(track);
		}
	}

	/**
	 * Adds a track as a member of the <code>AlbumArtist</code>. Checks to ensure that the track is not already
	 * a member, and if it is not adds it to the album.
	 * 
	 * @param newTrack		the track to add to the album artist
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	public synchronized boolean addTrack(FactoryPreferences prefs, Playable newTrack) {
		// Check to ensure that the newTrack is eligible to be a member of this album artist.
		if( _artistName.compareToIgnoreCase(newTrack.getAlbumArtistName()) != 0 ) {
			return false;
		}
		
		// See if newTrack belongs to an album.
		String newTrackAlbumName = newTrack.getAlbumName();
		if(! newTrackAlbumName.equals("") ) {
			
			// newTrack belongs to an album. See if the album is already a member of this album artist.
			for( Album album : this.albumList) {

				if( album.getAlbumName().compareToIgnoreCase(newTrackAlbumName) == 0 ) {
					// The album is already a member of this album artist, so add newTrack to the album.
					if(album.addTrack(prefs, newTrack)) {
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
			if( newAlbum.addTrack(prefs, newTrack) ) {
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
			if(this._trackList.contains(newTrack)) {
				return false;
			}
	
			// If we got this far, then the track is not yet in this album artist as a direct member, so add it.
			if(this._trackList.add(newTrack)) {
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
		sortedTrackList.addAll(_trackList);
		
		if(app != null) {
			Collections.sort(sortedTrackList, app.getPreferences().getAlbumArtistTrackComparator());
		}

		outputList.addAll(sortedTrackList);
		
		return outputList;
 		
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
		
		for(Playable track : this._trackList) {
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
		result = PRIME * result + ((_artistName == null) ? 0 : _artistName.hashCode());
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
		final BaseArtist other = (BaseArtist) obj;
		if (_artistName == null) {
			if (other._artistName != null)
				return false;
		} else if (_artistName.compareToIgnoreCase(other._artistName) != 0)
			return false;
		return true;
	}
}
