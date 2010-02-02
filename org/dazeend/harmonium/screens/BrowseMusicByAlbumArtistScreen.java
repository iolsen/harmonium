package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.AlbumArtist;
import org.dazeend.harmonium.music.CompareArtists;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;

import com.tivo.hme.bananas.BView;

public class BrowseMusicByAlbumArtistScreen extends HPlaylistAddCapableListScreen {
	
	public BrowseMusicByAlbumArtistScreen(Harmonium app, MusicCollection thisMusicCollection) {
		super(app, "All Album Artists");
		
		this.app = app;
		
		// If this music collection is broken into album artists, add them to the screen
		List<AlbumArtist> albumArtists = new ArrayList<AlbumArtist>();
		albumArtists.addAll( thisMusicCollection.getAlbumArtistList() );
		Collections.sort(albumArtists, new CompareArtists() );
		this.list.add( albumArtists.toArray() );
		
		// If this album has any tracks that are not identified as members of an album,
		// add them to the screen.
		List<Playable> tracks = new ArrayList<Playable>();
		tracks.addAll( thisMusicCollection.getAlbumlessTrackList() );
		Collections.sort(tracks, this.app.getPreferences().getMusicCollectionTrackComparator());

		this.list.add(tracks.toArray());
	}
	
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	PlaylistEligible musicItem = (PlaylistEligible)this.list.get( this.list.getFocus() );
 
        	if(musicItem.getClass() == AlbumArtist.class) {
        		this.app.push(new BrowseAlbumArtistScreen(this.app, (AlbumArtist)musicItem), TRANSITION_LEFT);
        	}
        	else {
        		this.app.push(new TrackScreen(this.app, (Playable)musicItem), TRANSITION_LEFT);
        	}
            return true;
        }  
   
        return super.handleAction(view, action);
    }
	
	/* (non-Javadoc)
	 * Handles key presses from TiVo remote control.
	 */
	@Override
	public boolean handleKeyPress(int key, long rawcode) {
	
		this.app.checkKeyPressToResetInactivityTimer(key);
		
		switch(key) {
		case KEY_PLAY:
			
			List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
			playlist.add( ( PlaylistEligible)this.list.get( this.list.getFocus() ) );
			boolean shuffleMode;
			boolean repeatMode;
			
			if( this.list.get( this.list.getFocus() ) instanceof AlbumArtist ) {
				// We're playing an entire album artist
				shuffleMode = this.app.getPreferences().getAlbumArtistDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getAlbumArtistDefaultRepeatMode();
			}
			else {
				// We're playing a member track
				shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
			}
			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
			return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}
}
