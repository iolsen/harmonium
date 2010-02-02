package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistEligible;

import com.tivo.hme.bananas.BView;

public class MainMenuScreen extends HListScreen {
	
	private MusicCollection musicCollection;
	
	private static final String NOW_PLAYING = "Now Playing";
	private static final String PLAY_ALL_MUSIC = "Play Entire Music Collection";
	private static final String BROWSE_MUSIC = "Browse Music";
	private static final String BROWSE_PLAYLISTS = "Browse Playlists";
	private static final String PREFERENCES = "Set Preferences";
	private static final String ABOUT = "About Harmonium";
	
	/**
	 * Constructor.
	 * 
	 * @param app	This instance of the application.
	 */
	public MainMenuScreen(Harmonium app, MusicCollection thisMusicCollection) {
		super(app, "Harmonium");
		
		this.app = app;
		this.musicCollection = thisMusicCollection;
		
		list.add(BROWSE_MUSIC);
		list.add(BROWSE_PLAYLISTS);
		list.add(PLAY_ALL_MUSIC);
		list.add(PREFERENCES);
		list.add(ABOUT);
	}
	
	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if( menuOption.equals(BROWSE_MUSIC) ) {
        		this.app.push(new BrowseMusicByWhatScreen(this.app, this.musicCollection), TRANSITION_LEFT);
        		return true;
        	}
        	else if(menuOption.equals(BROWSE_PLAYLISTS)) {
        		this.app.push(new BrowsePlaylistsScreen(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if( menuOption.equals(PLAY_ALL_MUSIC)) {
        		List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
				playlist.add( (PlaylistEligible)this.musicCollection);
				boolean shuffleMode = this.app.getPreferences().getMusicCollectionDefaultShuffleMode();
				boolean repeatMode = this.app.getPreferences().getMusicCollectionDefaultRepeatMode();
				this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
				return true;
        	}
        	else if(menuOption.equals(PREFERENCES)) {
        		this.app.push(new OptionsScreen(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if(menuOption.equals(ABOUT)) {
        		this.app.push(new AboutScreen(this.app), TRANSITION_LEFT);
        		return true;
        	}
        	else if(menuOption.equals(NOW_PLAYING)) {
        		if(this.app.getDiscJockey().isPlaying() && (this.app.getDiscJockey().getNowPlayingScreen() != null) ) {
        			this.app.push(this.app.getDiscJockey().getNowPlayingScreen(), TRANSITION_LEFT);
        		}
        		return true;
        	}
        }  
        
        return super.handleAction(view, action);
    }
	
	@Override
	public boolean handleEnter(Object arg0, boolean arg1) {
		
		if ( list.size() == 5 && this.app.getDiscJockey().isPlaying() )
			list.add(NOW_PLAYING);
		
		return super.handleEnter(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int key, long rawcode) {
		
		this.app.checkKeyPressToResetInactivityTimer(key);
		
		switch(key) {
		case KEY_PLAY:
			String menuOption = (String)list.get( list.getFocus() );
			
			// If we just pressed play while over PLAY_ALL_MUSIC, play the music.
			if( menuOption.equals(PLAY_ALL_MUSIC)) {
        		List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
				playlist.add( (PlaylistEligible)this.musicCollection);
				boolean shuffleMode = this.app.getPreferences().getMusicCollectionDefaultShuffleMode();
				boolean repeatMode = this.app.getPreferences().getMusicCollectionDefaultRepeatMode();
				this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
				return true;
        	}
		}
		return super.handleKeyPress(key, rawcode);
	}
}
