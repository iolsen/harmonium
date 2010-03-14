package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Album;
import org.dazeend.harmonium.music.CompareDiscs;
import org.dazeend.harmonium.music.Disc;
import org.dazeend.harmonium.music.PlayableLocalTrack;
import org.dazeend.harmonium.music.PlayableCollection;

import com.tivo.hme.bananas.BView;



public class BrowseAlbumScreen extends HAlbumInfoListScreen {
	
	private Album album;
	
	public BrowseAlbumScreen(Harmonium app, final Album thisAlbum) {
		super(app, thisAlbum, thisAlbum.toString());
		
		this.album = thisAlbum;
		
		// If this album is broken into discs, add them to the screen
		List<Disc> discs = new ArrayList<Disc>();
		discs.addAll( thisAlbum.getDiscList() );
		Collections.sort( discs, new CompareDiscs() );
		for (Disc disc : discs)
			addToList(disc);
		
		// If this album has any tracks that are not identified as members of a disc,
		// add them to the screen.
		List<PlayableLocalTrack> tracks = new ArrayList<PlayableLocalTrack>();
		tracks.addAll( thisAlbum.getTrackList() );
		Collections.sort(tracks, this.app.getPreferences().getAlbumTrackComparator());
		for (PlayableLocalTrack track : tracks)
			addToList(track);
	}
	
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	PlayableCollection musicItem = getListSelection();
       
        	if(musicItem instanceof Disc) {
        		this.app.push(new BrowseDiscScreen(this.app, (Disc)musicItem), TRANSITION_LEFT);
        	}
        	else {
        		if (this.album.getTrackList().size() > 1)
        			this.app.push(new TrackScreen(this.app, (PlayableLocalTrack)musicItem, this.album), TRANSITION_LEFT);
        		else
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
			boolean shuffleMode;
			boolean repeatMode;
			PlayableCollection selected = getListSelection();
			PlayableLocalTrack startPlaying = null;
			
			if(  selected instanceof Disc ) {
				// Playing an entire disc
				playlist.add( selected );
				shuffleMode = this.app.getPreferences().getDiscDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getDiscDefaultRepeatMode();
			}
			else {
				// Playing an individual track
				playlist.add( this.album );
				shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
				startPlaying = (PlayableLocalTrack)selected;
			}
			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode, startPlaying);
			return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}
}
