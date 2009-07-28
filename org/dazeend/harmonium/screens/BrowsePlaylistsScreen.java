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

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class BrowsePlaylistsScreen extends HSkipListScreen {

	/**
	 * @param app
	 * @param title
	 */
	public BrowsePlaylistsScreen(Harmonium app) {
		super(app, "Browse Playlists");
		
		this.app = app;
		List<PlaylistFile> sortedPlaylists = new ArrayList<PlaylistFile>();
		sortedPlaylists.addAll( MusicCollection.getMusicCollection( this.app.getHFactory() ).getPlaylists() );
		Collections.sort(sortedPlaylists);
		this.list.add( sortedPlaylists.toArray() );
	}

	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	PlaylistFile playlist = (PlaylistFile)list.get( list.getFocus() );
        	this.app.push(new PlaylistScreen(this.app, playlist), TRANSITION_LEFT);
        }
        return super.handleAction(view, action);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int code, long rawcode) {
		
		this.app.checkKeyPressToResetInactivityTimer(code);
		
		if(code == KEY_PLAY) {
			PlaylistFile playlist = (PlaylistFile)list.get( list.getFocus() );
			this.app.getDiscJockey().play(playlist.getMembers(), playlist.getShuffleMode(this.app), playlist.getRepeatMode(this.app));
			return true;
		}
		else if(code == KEY_CLEAR) {
			this.app.play("select.snd");
			this.app.push(new DeletePlaylistScreen(this.app, (PlaylistFile)list.get( list.getFocus() ) ), TRANSITION_LEFT);
			return true;
		}
		
		return super.handleKeyPress(code, rawcode);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
	 */
	@Override
	public boolean handleEnter(Object screenArgument, boolean isReturning) {
		
		if(isReturning) {
			// this is needed so that the list will remain sorted and consistant after returning from child screens
			PlaylistFile focusedPlaylist = (PlaylistFile)list.get( list.getFocus() );
			this.list.clear();
			List<PlaylistFile> sortedPlaylists = new ArrayList<PlaylistFile>();
			sortedPlaylists.addAll( MusicCollection.getMusicCollection( this.app.getHFactory() ).getPlaylists() );
			Collections.sort(sortedPlaylists);
			this.list.add( sortedPlaylists.toArray() );
			
			if(this.list.contains(focusedPlaylist)) {
				this.list.setFocus(this.list.indexOf(focusedPlaylist), false);
			}
			else {
				this.list.setFocus(0, false);
			}
		}
		return super.handleEnter(screenArgument, isReturning);
	}
	
	
	
}
