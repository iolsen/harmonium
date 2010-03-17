package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.CompareTracksByName;
import org.dazeend.harmonium.music.PlayableLocalTrack;
import org.dazeend.harmonium.music.PlayableCollection;
import org.dazeend.harmonium.music.TrackArtist;
import com.tivo.hme.bananas.BView;

public class BrowseTrackArtistScreen extends HAlbumInfoListScreen
{
	private List<PlayableLocalTrack> trackList;
	
	public BrowseTrackArtistScreen(Harmonium app, TrackArtist thisTrackArtist) {
		// The parent constructor needs an album to initialize album info. Sent the first one.
		super(app, thisTrackArtist.toString() );
		
		this.artistNameLabelText.setValue("Artist");
		this.artistNameText.setValue(thisTrackArtist.getArtistName());
		
		// Set up list for contents of musicItem
		this.list = new HAlbumArtList(	this.getNormal(), 								// Put list on "normal" level
										this.safeTitleH , 									// x coord. of list
										this.screenHeight - this.safeTitleV - (this.rowHeight * 5), 	// y coord. of list
										(this.screenWidth - (2 * this.safeTitleH)), 				// width of list (full screen)
										this.rowHeight * 5,									// height of list (5/8  of body). Defined in terms of row height to ensure that height is an even multiple or rowheight.
										this.rowHeight,										// row height
										this.albumArtView,
										this.albumArtBGView,
										this.albumNameText,
										this.albumNameBGText,
										this.artistNameText,
										this.albumArtistBGText,
										this.yearText,
										this.yearBGText
										);
		setFocusDefault(this.list);
		
		// Add tracks to the screen.
		trackList = new ArrayList<PlayableLocalTrack>();
		trackList.addAll( thisTrackArtist.getTrackList() );
		Collections.sort(trackList, new CompareTracksByName());
		this.list.add( trackList.toArray() );
	}
		
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	PlayableLocalTrack musicItem = (PlayableLocalTrack)list.get( list.getFocus() );
    		this.app.push(new TrackScreen(this.app, (PlayableLocalTrack)musicItem), TRANSITION_LEFT);
        	
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

			// Playing an individual track
			boolean shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
			boolean repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();

			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
			return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}

}
