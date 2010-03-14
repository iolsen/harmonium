package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Disc;
import org.dazeend.harmonium.music.PlayableLocalTrack;
import org.dazeend.harmonium.music.PlayableCollection;

import com.tivo.hme.bananas.BView;


public class BrowseDiscScreen extends HAlbumInfoListScreen {
	
	private Disc disc;
	
	public BrowseDiscScreen(Harmonium app, final Disc thisDisc) {
		super(app, thisDisc, thisDisc.toString());

		this.disc = thisDisc;
		
		List<PlayableLocalTrack> tracks = new ArrayList<PlayableLocalTrack>();
		tracks.addAll( thisDisc.getTrackList() );
		Collections.sort(tracks, this.app.getPreferences().getDiscTrackComparator());
		for (PlayableLocalTrack track : tracks)
			addToList(track);
	}
	
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	PlayableLocalTrack track = (PlayableLocalTrack)getListSelection();

    		if (this.disc.getTrackList().size() > 1)
    			this.app.push(new TrackScreen(this.app, track, this.disc), TRANSITION_LEFT);
    		else
    			this.app.push(new TrackScreen(this.app, track), TRANSITION_LEFT);

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
			playlist.add( this.disc );
			boolean shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
			boolean repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode, (PlayableLocalTrack)getListSelection());
			return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}
}
