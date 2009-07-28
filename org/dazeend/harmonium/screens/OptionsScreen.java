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

import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class OptionsScreen extends HListScreen {

	private static final String APPLICATION = "Application Preferences";
	private static final String MUSIC_COLLECTION = "Music Collection Preferences";
	private static final String ALBUM_ARTIST = "Album Artist Preferences";
	private static final String ALBUM = "Album Preferences";
	private static final String DISC = "Disc Preferences";
	private static final String TRACK = "Track Preferences";
	private static final String PLAYLIST = "Playlist Preferences";
	
	/**
	 * Constructor.
	 * 
	 * @param app	This instance of the application.
	 */
	public OptionsScreen(Harmonium app) {
		super(app, "Harmonium Preferences");
		
		this.app = app;
		
		this.list.add(APPLICATION);
		this.list.add(MUSIC_COLLECTION);
		this.list.add(ALBUM_ARTIST);
		this.list.add(ALBUM);
		this.list.add(DISC);
		this.list.add(TRACK);
		this.list.add(PLAYLIST);
		
	}
	
	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if( menuOption.equals(ALBUM_ARTIST) ) {
        		this.app.push(new EditDefaultAlbumArtistOptions(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if( menuOption.equals(MUSIC_COLLECTION) ) {
        		try {
        		this.app.push(new EditDefaultMusicCollectionOptions(this.app), TRANSITION_LEFT);
        		}
        		catch(Exception e) {
        			e.printStackTrace();
        		}
        		return true;
        	}
        	else if( menuOption.equals(ALBUM) ) {
        		this.app.push(new EditDefaultAlbumOptions(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if( menuOption.equals(DISC) ) {
        		this.app.push(new EditDefaultDiscOptions(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if( menuOption.equals(TRACK) ) {
        		this.app.push(new EditDefaultTrackOptions(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if( menuOption.equals(PLAYLIST) ) {
        		this.app.push(new EditDefaultPlaylistFileOptions(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if( menuOption.equals(APPLICATION) ) {
        		this.app.push(new EditDefaultApplicationOptions(this.app), TRANSITION_LEFT);
        		return true;
        	}
        }  
        
        return super.handleAction(view, action);
    }
}
