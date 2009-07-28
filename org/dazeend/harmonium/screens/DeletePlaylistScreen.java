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
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class DeletePlaylistScreen extends HListScreen {

	private PlaylistFile playlist;
	
	private final static String DELETE = "Delete Playlist";
	
	/**
	 * @param app
	 * @param title
	 */
	public DeletePlaylistScreen(Harmonium app, PlaylistFile playlist) {
		super(app, "Delete Playlist");
		
		this.app = app;
		this.playlist = playlist;
		
		this.list.add(DELETE);
		
	}
	
	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if( menuOption.equals(DELETE) ) {
        		
        		// Confirm that this is a file we can delete
        		if(this.playlist.getFile().exists() && this.playlist.getFile().isFile() && this.playlist.getFile().canWrite()) {
        			if(this.playlist.getFile().delete()) {
        				// Playlist File was deleted. Remove from data structure.
        				MusicCollection.getMusicCollection(this.app.getHFactory()).getPlaylists().remove(this.playlist);
        			}
        			else {
        				this.app.play("bonk.snd");
        				this.app.push(new ErrorScreen(this.app, "File could not be deleted."), TRANSITION_LEFT);
        			}
        		}
        		else {
        			this.app.play("bonk.snd");
        			this.app.push(new ErrorScreen(this.app, "Cannot delete file."), TRANSITION_LEFT);
        		}
        		
        		this.app.pop();
        		return true;
        	}
            
        }  
        
        return super.handleAction(view, action);
    }

}
