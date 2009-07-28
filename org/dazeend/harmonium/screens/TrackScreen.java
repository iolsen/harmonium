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
 
package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Album;
import org.dazeend.harmonium.music.AlbumReadable;
import org.dazeend.harmonium.music.Disc;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;
import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class TrackScreen extends HAlbumInfoListScreen {

	private String screenTitle;
	private Playable playableTrack;
	private PlaylistEligible trackParent;
	private final String playTrackString = "Play This Track";
	private final String playOnlyTrackString = "Play Only This Track";
	private final String addTrackToPlaylistString = "Add Track to Playlist";
	private final String playParentAlbumString = "Play Album Starting With This Track";
	private final String playParentPlaylistString = "Play Playlist Starting With This Track";
	private final String playParentDiscString = "Play Disc Starting With This Track";
	private HList list;
	
	public TrackScreen(Harmonium app, final Playable thisTrack) {
		this(app, thisTrack, null);
	}
	
	public TrackScreen(Harmonium app, final Playable thisTrack, final PlaylistEligible trackParent) {
		super( app, (AlbumReadable) thisTrack, thisTrack.toString() );
		
		this.screenTitle = thisTrack.toString();
		this.playableTrack = thisTrack;
		this.trackParent = trackParent;
		
		// Set up modified list
		int itemsInList = trackParent == null ? 2 : 3;
		int listHeight = itemsInList * rowHeight;
		this.list = new HList(	this.getNormal(), 									// Put list on "normal" level
								this.safeTitleH , 									// x coord. of button
								this.screenHeight - this.safeTitleV - listHeight, 	// y coord - Align with bottom of screen
								(this.screenWidth * 3/4), 							// width of button (3/4s of screen)
								listHeight,											// height of list
								this.rowHeight										// height of each row
		);
		if (trackParent != null) {
			if (trackParent instanceof Album)
				this.list.add(this.playParentAlbumString);
			else if (trackParent instanceof Disc)
				this.list.add(this.playParentDiscString);
			else
				this.list.add(this.playParentPlaylistString);
			this.list.add(this.playOnlyTrackString);
		}
		else
			this.list.add(this.playTrackString);
		this.list.add(this.addTrackToPlaylistString);
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
		case KEY_ENTER:
			this.app.play("select.snd");
			this.app.push(new AddToPlaylistScreen(this.app, this.playableTrack), TRANSITION_LEFT);
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
    		
    		// Ian TODO: Consider moving this logic down into DiscJockey.  It's also
    		//			 used in BrowseAlbumScreen.
			List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
			playlist.add( this.trackParent );
			boolean shuffleMode;
			boolean repeatMode;
			
			// Ian TODO: Should this be disc/album shuffle mode?  See also comment
			//			 in BrowseAlbumScreen.
			shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
			repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();

			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode, playableTrack);
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
