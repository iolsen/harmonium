package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.CompareArtists;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlayableLocalTrack;
import org.dazeend.harmonium.music.PlayableCollection;
import org.dazeend.harmonium.music.TrackArtist;

import com.tivo.hme.bananas.BView;

public class BrowseMusicByTrackArtistScreen extends HPlaylistAddCapableListScreen
{
	public BrowseMusicByTrackArtistScreen(Harmonium app, MusicCollection thisMusicCollection) {
		super(app, "All Track Artists");
		
		this.app = app;
		
		// Add track artists to list
		List<TrackArtist> trackArtists = new ArrayList<TrackArtist>();
		trackArtists.addAll( thisMusicCollection.getTrackArtistList() );
		Collections.sort(trackArtists, new CompareArtists() );
		this.list.add( trackArtists.toArray() );
		
		// Ian TODO: add tracks with no track artist
	}

	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	PlayableCollection musicItem = (PlayableCollection)this.list.get( this.list.getFocus() );
 
        	if(musicItem.getClass() == TrackArtist.class) {
        		this.app.push(new BrowseTrackArtistScreen(this.app, (TrackArtist)musicItem), TRANSITION_LEFT);
        	}
        	else {
        		this.app.push(new TrackScreen(this.app, (PlayableLocalTrack)musicItem), TRANSITION_LEFT);
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
			
			List<PlayableCollection> playlist = new ArrayList<PlayableCollection>();
			playlist.add( ( PlayableCollection)this.list.get( this.list.getFocus() ) );
			boolean shuffleMode;
			boolean repeatMode;
			
			if( this.list.get( this.list.getFocus() ) instanceof TrackArtist ) {
				// We're playing an entire track artist
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
