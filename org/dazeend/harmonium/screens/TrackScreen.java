package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Album;
import org.dazeend.harmonium.music.Disc;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

public class TrackScreen extends HAlbumInfoListScreen {

	private String screenTitle;
	private Playable playableTrack;
	private PlaylistEligible trackParent;
	private final String playTrackString = "Play This Track Now";
	private final String playOnlyTrackString = "Play Only This Track Now";
	private final String addTrackToPlaylistString = "Add Track to Playlist";
	private final String playParentAlbumString = "Play Album Starting With This Track";
	private final String playParentPlaylistString = "Play Playlist Starting With This Track";
	private final String playParentDiscString = "Play Disc Starting With This Track";
	
	private final String enqueueTrackNext = "Play This Track Next";
	private final String enqueueTrackEnd = "Add Track to \"Now Playing\" Playlist";
	
	private HList list;
	
	public TrackScreen(Harmonium app, final Playable thisTrack) {
		this(app, thisTrack, null);
	}
	
	public TrackScreen(Harmonium app, final Playable thisTrack, final PlaylistEligible trackParent) {
		super( app, thisTrack, thisTrack.toString(), false );
		
		artistNameLabelText.setValue("Artist");
		artistNameText.setValue(thisTrack.getArtistName());
		
		Vector<String> listCommands = new Vector<String>(5);
		
		this.screenTitle = thisTrack.toString();
		this.playableTrack = thisTrack;
		this.trackParent = trackParent;
		
		// Set up modified list
		if (this.app.getDiscJockey().isPlaying()) {
			// Something's playing now, so add the enqueue options.
			listCommands.add(this.enqueueTrackNext);
			listCommands.add(this.enqueueTrackEnd);
		}
		if (trackParent != null) {
			if (trackParent instanceof Album)
				listCommands.add(this.playParentAlbumString);
			else if (trackParent instanceof Disc)
				listCommands.add(this.playParentDiscString);
			else
				listCommands.add(this.playParentPlaylistString);
			listCommands.add(this.playOnlyTrackString);
		}
		else {
			listCommands.add(this.playTrackString);
		}
		listCommands.add(this.addTrackToPlaylistString);

		int listHeight = Math.min(5, listCommands.size()) * rowHeight;

		this.list = new HList(	this.getNormal(), 									// Put list on "normal" level
								this.safeTitleH, 									// x coord. of button
								this.screenHeight - this.safeTitleV - listHeight, 	// y coord - Align with bottom of screen
								(screenWidth - (2 * safeTitleH)), 					// width of list (full screen)
								listHeight,											// height of list
								this.rowHeight										// height of each row
		);
		
		for (String listCommand : listCommands)
			this.list.add(listCommand);

		this.setFocusDefault(this.list);
		
	}
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView view, Object action) {
		if(action.equals("right") || action.equals("select")) {
        	String menuOption = (String)this.list.get( this.list.getFocus() );
        	
        	if(menuOption.equals(this.addTrackToPlaylistString)) {
        		this.app.push(new AddToPlaylistScreen(this.app, this.playableTrack), TRANSITION_LEFT);
        		return true;
        	}
        	else {
        		play();
        		return true;
        	}
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
			play();
			return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}
	
	private void play() {
    	String menuOption = (String)this.list.get( this.list.getFocus() );
    	if(menuOption.equals(this.playTrackString) || menuOption.equals(this.playOnlyTrackString)) {
    		List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
			playlist.add( (PlaylistEligible)this.playableTrack);
			boolean shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
			boolean repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
    	}        
    	else if (menuOption.equals(playParentAlbumString) || menuOption.equals(playParentPlaylistString) || menuOption.equals(this.playParentDiscString)) {
    		
			List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
			playlist.add( this.trackParent );

			boolean shuffleMode;
			boolean repeatMode;
			if (menuOption.equals(playParentAlbumString)) {
				shuffleMode = this.app.getPreferences().getAlbumDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getAlbumDefaultRepeatMode();
			}
			else if (menuOption.equals(playParentPlaylistString)) {
				if (this.trackParent instanceof PlaylistFile) {
					shuffleMode = ((PlaylistFile) this.trackParent).getShuffleMode(this.app);
					repeatMode = ((PlaylistFile) this.trackParent).getRepeatMode(this.app);
				}
				else {
					shuffleMode = this.app.getPreferences().getPlaylistFileDefaultShuffleMode();
					repeatMode = this.app.getPreferences().getPlaylistFileDefaultRepeatMode();
				}
			}
			else if (menuOption.equals(playParentDiscString)) {
				shuffleMode = this.app.getPreferences().getDiscDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getDiscDefaultRepeatMode();
			}
			else {
				shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
			}

			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode, playableTrack);
    	}
    	else if (menuOption.equals(enqueueTrackNext)) {
    		this.app.getDiscJockey().enqueueNext(this.playableTrack);
    	}
    	else if (menuOption.equals(enqueueTrackEnd)) {
    		this.app.getDiscJockey().enqueueAtEnd(this.playableTrack);
    	}
    	else
    		this.app.play("bonk.snd");
	}
	

	
	/* (non-Javadoc)
	 * @see com.tivo.hme.sdk.HmeObject#toString()
	 */
	@Override
	public String toString() {
		return this.screenTitle;
	}
}
