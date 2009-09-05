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
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.AlbumArtist;
import org.dazeend.harmonium.music.CompareAlbumArtists;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;

public class BrowseMusicByAlbumArtistScreen extends HSkipListScreen {
	
	public BrowseMusicByAlbumArtistScreen(Harmonium app, MusicCollection thisMusicCollection) {
		super(app, "All Album Artists");
		
		this.app = app;
		
		// If this music collection is broken into album artists, add them to the screen
		List<AlbumArtist> albumArtists = new ArrayList<AlbumArtist>();
		albumArtists.addAll( thisMusicCollection.getAlbumArtistList() );
		Collections.sort(albumArtists, new CompareAlbumArtists() );
		this.list.add( albumArtists.toArray() );
		
		// If this album has any tracks that are not identified as members of an album,
		// add them to the screen.
		List<Playable> tracks = new ArrayList<Playable>();
		tracks.addAll( thisMusicCollection.getAlbumlessTrackList() );
		Collections.sort(tracks, this.app.getPreferences().getMusicCollectionTrackComparator());

		this.list.add(tracks.toArray());
		
		// Add a note to the bottom of the screen
		BText enterNote = new BText(	this.getNormal(),
										this.safeTitleH,
										this.list.getY() + (8 * this.rowHeight) + (this.screenHeight / 100),
										this.screenWidth - (2 * this.safeTitleH),
										this.app.hSkin.paragraphFontSize
		);
		enterNote.setFont(app.hSkin.paragraphFont);
		enterNote.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		enterNote.setFlags(RSRC_HALIGN_CENTER + RSRC_VALIGN_BOTTOM);
		enterNote.setValue("press ENTER to add the entire music collection to a playlist");
		setManagedView(enterNote);
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
		case KEY_ENTER:
			this.app.play("select.snd");
			this.app.push(new AddToPlaylistScreen(this.app, MusicCollection.getMusicCollection(this.app.getHFactory())), TRANSITION_LEFT);
    		return true;
		}
		
		return super.handleKeyPress(key, rawcode);

	}
}
