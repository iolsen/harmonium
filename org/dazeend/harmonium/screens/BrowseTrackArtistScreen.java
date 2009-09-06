/*
 * Copyright 2009 Ian Olsen
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
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.music.CompareTracksByName;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;
import org.dazeend.harmonium.music.TrackArtist;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;

public class BrowseTrackArtistScreen extends HAlbumInfoListScreen
{
	private HAlbumArtList list;
	private TrackArtist trackArtist;
	private List<Playable> trackList;
	
	public BrowseTrackArtistScreen(Harmonium app, TrackArtist thisTrackArtist) {
		// The parent constructor needs an album to initialize album info. Sent the first one.
		super(app, thisTrackArtist.toString() );
		
		this.artistNameLabelText.setValue("Artist");
		this.artistNameText.setValue(thisTrackArtist.getArtistName());
		
		this.trackArtist = thisTrackArtist;
		
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
		trackList = new ArrayList<Playable>();
		trackList.addAll( thisTrackArtist.getTrackList() );
		Collections.sort(trackList, new CompareTracksByName());
		this.list.add( trackList.toArray() );
		
		// Add a note to the bottom of the screen
		BText enterNote = new BText(	this.getNormal(),
										this.safeTitleH,
										this.list.getY() + (5 * this.rowHeight) + (this.screenHeight / 100),
										this.screenWidth - (2 * this.safeTitleH),
										this.app.hSkin.paragraphFontSize
		);
		enterNote.setFont(app.hSkin.paragraphFont);
		enterNote.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		enterNote.setFlags(RSRC_HALIGN_CENTER + RSRC_VALIGN_BOTTOM);
		enterNote.setValue("press ENTER to add this artist to a playlist");
		setManagedView(enterNote);
	}
		
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	Playable musicItem = (Playable)list.get( list.getFocus() );
    		this.app.push(new TrackScreen(this.app, (Playable)musicItem), TRANSITION_LEFT);
        	
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

			// Playing an individual track
			boolean shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
			boolean repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();

			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
			return true;

		case KEY_ENTER:
			this.app.play("select.snd");
			this.app.push(new AddToPlaylistScreen(this.app, this.trackArtist), TRANSITION_LEFT);
    		return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}

}
