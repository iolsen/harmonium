package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.EditablePlaylist;
import org.dazeend.harmonium.music.HPLFile;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

public class PlaylistScreen extends HListScreen {

	private PlaylistFile playlistFile;
	
	private static final String PLAY_LABEL = "Play";
	private static final String BROWSE_LABEL = "Browse";
	private static final String EDIT_PLAYLIST_LABEL = "Edit Playlist";
	private static final String EDIT_DESCRIPTION_LABEL = "Edit Description";
	private static final String EDIT_OPTIONS_LABEL = "Edit Playlist Options";
	private static final String DELETE_LABEL = "Delete";
	private static final String SAVE_LABEL = "Save to Playlist";
	
	/**
	 * @param app
	 * @param title
	 */
	public PlaylistScreen(Harmonium app, PlaylistFile playlistFile) {
		this(app, playlistFile.toString());
		
		this.playlistFile = playlistFile;
		
		list.add(PLAY_LABEL);
		list.add(BROWSE_LABEL);

		if( playlistFile.getClass() == HPLFile.class ) {
			// These options apply only to HPL Playlists
			if(! this.playlistFile.getMembers().isEmpty() ) {
				// only allow playlist members to be edited if there are members to edit
				list.add(EDIT_PLAYLIST_LABEL);
			}
			list.add(EDIT_DESCRIPTION_LABEL);
			list.add(EDIT_OPTIONS_LABEL);
		}
		
		list.add(DELETE_LABEL);
	}
	
	public PlaylistScreen(Harmonium app, EditablePlaylist playlist) {
		this (app, "\"Now Playing\" Playlist");
		
		list.add(BROWSE_LABEL);
		list.add(EDIT_PLAYLIST_LABEL);
		list.add(SAVE_LABEL);
	}
	
	private PlaylistScreen(Harmonium app, String title) {
		super(app, title);

		this.app = app;
	}
	
	
	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if(menuOption.equals(BROWSE_LABEL) ) {
        		if (this.playlistFile != null )
        			this.app.push(new BrowsePlaylistScreen(this.app, this.playlistFile), TRANSITION_LEFT);
        		else
        			this.app.push(new BrowsePlaylistScreen(this.app), TRANSITION_LEFT);
        	}
        	else if( menuOption.equals(PLAY_LABEL) ) {
        		this.app.getDiscJockey().play(this.playlistFile.getMembers(), this.playlistFile.getShuffleMode(this.app), this.playlistFile.getRepeatMode(this.app));
        	}
        	else if(menuOption.equals(EDIT_DESCRIPTION_LABEL) ) {
        		this.app.push(new EditPlaylistDescriptionScreen(this.app, (HPLFile)this.playlistFile), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(DELETE_LABEL)) {
        		this.app.push(new DeletePlaylistScreen(this.app, this.playlistFile), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(EDIT_PLAYLIST_LABEL)) {
        		if (this.playlistFile != null )
        			this.app.push(new EditPlaylistScreen(this.app, (HPLFile)this.playlistFile), TRANSITION_LEFT);
        		else
        			this.app.push(new EditPlaylistScreen(this.app, this.app.getDiscJockey().getCurrentPlaylist()), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(EDIT_OPTIONS_LABEL)) {
        		this.app.push(new EditPlaylistOptionsScreen(this.app, (HPLFile)this.playlistFile), TRANSITION_LEFT);
        	}
        	else if(menuOption.equals(SAVE_LABEL)) {
        		this.app.push(new AddToPlaylistScreen(this.app, this.app.getDiscJockey().getCurrentPlaylist()), TRANSITION_LEFT);
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
			
			if (this.playlistFile != null) {
				// See if this playlist still exists
				if(MusicCollection.getMusicCollection(this.app.getHFactory()).getPlaylists().contains(this.playlistFile)) {
					// The playlist does exist, so update the title of this screen to match the
					// description. (We may have just edited the description.)
					this.titleText.setValue(this.playlistFile.toString());
					
					// Check to see if the playlist still has members
					if(this.playlistFile.getMembers().isEmpty()) {
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
		}
		return super.handleEnter(screenArgument, isReturning);
	}
	
	

}
