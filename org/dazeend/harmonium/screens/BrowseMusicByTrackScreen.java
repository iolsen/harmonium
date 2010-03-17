package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.AlbumArtist;
import org.dazeend.harmonium.music.CompareTracksByName;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlayableLocalTrack;
import org.dazeend.harmonium.music.PlayableCollection;
import org.dazeend.harmonium.music.PlayableTrack;

import com.tivo.hme.bananas.BView;

public class BrowseMusicByTrackScreen extends HPlaylistAddCapableListScreen
{
	public BrowseMusicByTrackScreen(Harmonium app, MusicCollection thisMusicCollection) {
		super(app, "All Tracks");
		
		this.app = app;
		
		// Add all tracks from music collection to screen
		List<PlayableLocalTrack> tracks = new ArrayList<PlayableLocalTrack>();
		tracks.addAll( thisMusicCollection.getMembers(app) );
		
		List<PlayableTrack> sortedTracks = new ArrayList<PlayableTrack>(tracks.size());
		sortedTracks.addAll(tracks);
		Collections.sort(sortedTracks, new CompareTracksByName());

		this.list.add(sortedTracks.toArray());
	}
	
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	PlayableLocalTrack musicItem = (PlayableLocalTrack)this.list.get( this.list.getFocus() );
    		this.app.push(new TrackScreen(this.app, musicItem), TRANSITION_LEFT);
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
