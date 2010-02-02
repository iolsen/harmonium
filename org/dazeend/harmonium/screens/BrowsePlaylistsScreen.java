package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BView;

public class BrowsePlaylistsScreen extends HSkipListScreen
{

	private static final String NOW_PLAYING_PLAYLIST = "\"Now Playing\" Playlist";

	/**
	 * @param app
	 * @param title
	 */
	public BrowsePlaylistsScreen(Harmonium app)
	{
		super(app, "Browse Playlists");

		this.app = app;

		List<PlaylistFile> sortedPlaylists = new ArrayList<PlaylistFile>();
		sortedPlaylists.addAll(MusicCollection.getMusicCollection(this.app.getHFactory()).getPlaylists());
		Collections.sort(sortedPlaylists);
		this.list.add(sortedPlaylists.toArray());

	}

	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	Object selected = list.get(list.getFocus()); 
        	       	
        	if ( selected instanceof String && selected.equals(NOW_PLAYING_PLAYLIST)) {
        		this.app.push(new PlaylistScreen(this.app, this.app.getDiscJockey().getCurrentPlaylist()), TRANSITION_LEFT);
        	}
        	else {
            	PlaylistFile playlist = (PlaylistFile)list.get( list.getFocus() );
            	this.app.push(new PlaylistScreen(this.app, playlist), TRANSITION_LEFT);
        	}
        }
        return super.handleAction(view, action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int code, long rawcode)
	{

		this.app.checkKeyPressToResetInactivityTimer(code);
		
		if (list.size() > 0) {
	    	Object selected = list.get(list.getFocus()); 

	    	if (code == KEY_PLAY)
			{
	        	if ( selected instanceof String && selected.equals(NOW_PLAYING_PLAYLIST)) {
	        		this.app.push(this.app.getDiscJockey().getNowPlayingScreen(), TRANSITION_LEFT);
	        	}
	        	else {
					PlaylistFile playlist = (PlaylistFile)selected;
					this.app.getDiscJockey().play(playlist.getMembers(), playlist.getShuffleMode(this.app),
							playlist.getRepeatMode(this.app));
	        	}
				return true;
			} else if (code == KEY_CLEAR)
			{
	        	if ( selected instanceof String && selected.equals(NOW_PLAYING_PLAYLIST)) {
	        		this.app.play("bonk.snd");
	        	}
	        	else {
					this.app.play("select.snd");
					this.app.push(new DeletePlaylistScreen(this.app, (PlaylistFile)selected), TRANSITION_LEFT);
	        	}
				return true;
			}
		}

		return super.handleKeyPress(code, rawcode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
	 */
	@Override
	public boolean handleEnter(Object screenArgument, boolean isReturning)
	{
		if (isReturning)
		{
			// this is needed so that the list will remain sorted and consistant
			// after returning from child screens
			Object focusedItem = list.get(list.getFocus());
			this.list.clear();

			if (app.getDiscJockey().hasCurrentPlaylist())
				this.list.add(0, NOW_PLAYING_PLAYLIST);

			List<PlaylistFile> sortedPlaylists = new ArrayList<PlaylistFile>();
			sortedPlaylists.addAll(MusicCollection.getMusicCollection(this.app.getHFactory()).getPlaylists());
			Collections.sort(sortedPlaylists);
			this.list.add(sortedPlaylists.toArray());

			if (this.list.contains(focusedItem))
			{
				this.list.setFocus(this.list.indexOf(focusedItem), false);
			} else
			{
				this.list.setFocus(0, false);
			}
		}
		else
		{
			if (app.getDiscJockey().hasCurrentPlaylist())
				this.list.add(0, NOW_PLAYING_PLAYLIST);
		}

		return super.handleEnter(screenArgument, isReturning);
	}

}
