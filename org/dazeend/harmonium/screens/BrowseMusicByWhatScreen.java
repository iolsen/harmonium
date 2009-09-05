package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.MusicCollection;

import com.tivo.hme.bananas.BView;

public class BrowseMusicByWhatScreen extends HListScreen 
{
	private MusicCollection musicCollection;
	
	private static final String BY_ALBUM_ARTIST = "By Album Artist";
	private static final String BY_TRACK = "By Track";
	
	/**
	 * Constructor.
	 * 
	 * @param app	This instance of the application.
	 */
	public BrowseMusicByWhatScreen(Harmonium app, MusicCollection thisMusicCollection) {
		super(app, "Browse Music");
		
		this.app = app;
		this.musicCollection = thisMusicCollection;
		
		list.add(BY_ALBUM_ARTIST);
		list.add(BY_TRACK);
	}
	
	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if( menuOption.equals(BY_ALBUM_ARTIST) ) {
        		this.app.push(new BrowseMusicByAlbumArtistScreen(this.app, this.musicCollection), TRANSITION_LEFT);
        		return true;
        	}
        	if( menuOption.equals(BY_TRACK) ) {
        		this.app.push(new BrowseMusicByTrackScreen(this.app, this.musicCollection), TRANSITION_LEFT);
        		return true;
        	}
        }  
        
        return super.handleAction(view, action);
    }
}
