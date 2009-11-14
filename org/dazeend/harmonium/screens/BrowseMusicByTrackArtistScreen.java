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
import org.dazeend.harmonium.music.CompareArtists;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;
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
        	PlaylistEligible musicItem = (PlaylistEligible)this.list.get( this.list.getFocus() );
 
        	if(musicItem.getClass() == TrackArtist.class) {
        		this.app.push(new BrowseTrackArtistScreen(this.app, (TrackArtist)musicItem), TRANSITION_LEFT);
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
