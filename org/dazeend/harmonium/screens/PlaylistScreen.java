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
import org.dazeend.harmonium.music.HPLFile;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class PlaylistScreen extends HListScreen {

	private PlaylistFile playlist;
	
	private static final String PLAY_LABEL = "Play";
	private static final String BROWSE_LABEL = "Browse";
	private static final String EDIT_PLAYLIST_LABEL = "Edit Playlist";
	private static final String EDIT_DESCRIPTION_LABEL = "Edit Description";
	private static final String EDIT_OPTIONS_LABEL = "Edit Playlist Options";
	private static final String DELETE_LABEL = "Delete";
	
	/**
	 * @param app
	 * @param title
	 */
	public PlaylistScreen(Harmonium app, PlaylistFile playlist) {
		super(app, playlist.toString());
		
		this.app = app;
		this.playlist = playlist;
		
		list.add(PLAY_LABEL);
		list.add(BROWSE_LABEL);
		
		if( playlist.getClass() == HPLFile.class ) {
			// These options apply only to HPL Playlists
			if(! this.playlist.getMembers().isEmpty() ) {
				// only allow playlist members to be edited if there are members to edit
				list.add(EDIT_PLAYLIST_LABEL);
			}
			list.add(EDIT_DESCRIPTION_LABEL);
			list.add(EDIT_OPTIONS_LABEL);
		}
		
		list.add(DELETE_LABEL);
	}
	
	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if(menuOption.equals(BROWSE_LABEL) ) {
        		this.app.push(new BrowsePlaylistScreen(this.app, this.playlist), TRANSITION_LEFT);
        	}
        	else if( menuOption.equals(PLAY_LABEL) ) {
        		this.app.getDiscJockey().play(this.playlist.getMembers(), this.playlist.getShuffleMode(this.app), this.playlist.getRepeatMode(this.app));
        	}
        	else if(menuOption.equals(EDIT_DESCRIPTION_LABEL) ) {
        		this.app.push(new EditPlaylistDescriptionScreen(this.app, (HPLFile)this.playlist), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(DELETE_LABEL)) {
        		this.app.push(new DeletePlaylistScreen(this.app, this.playlist), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(EDIT_PLAYLIST_LABEL)) {
        		this.app.push(new EditPlaylistScreen(this.app, (HPLFile)this.playlist), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(EDIT_OPTIONS_LABEL)) {
        		this.app.push(new EditPlaylistOptionsScreen(this.app, (HPLFile)this.playlist), TRANSITION_LEFT);
        	}
        	
        	return true;
        }
        return super.handleAction(view, action);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
	 */
	@Override
	public boolean handleEnter(Object screenArgument, boolean isReturning) {
		if(isReturning) {
			// See if this playlist still exists
			if(MusicCollection.getMusicCollection(this.app.getHFactory()).getPlaylists().contains(this.playlist)) {
				// The playlist does exist, so update the title of this screen to match the
				// description. (We may have just edited the description.)
				this.titleText.setValue(this.playlist.toString());
				
				// Check to see if the playlist still has members
				if(this.playlist.getMembers().isEmpty()) {
					// The playlist is empty, so delete the option to edit it's members
					this.list.remove(EDIT_PLAYLIST_LABEL);
					this.list.setFocus(0, false);
				}
			}
			else {
				// The playlist has been deleted, so pop this screen
				this.app.pop();
			}
		}
		return super.handleEnter(screenArgument, isReturning);
	}
	
	

}
