package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

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
