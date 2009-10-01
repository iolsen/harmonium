package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;
import org.dazeend.harmonium.music.PlaylistFile;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;

public class BrowsePlaylistScreen extends HAlbumInfoListScreen {

	private HAlbumArtList list;
	private PlaylistFile playlistFile;

	private BrowsePlaylistScreen(Harmonium app, String title) {
		super(app, title);

		this.artistNameLabelText.setValue("Artist");
		
		// Set up list for contents of musicItem
		this.list = new HPlaylistBrowseList(this.getNormal(),				 							// Put list on "normal" level
											this.safeTitleH , 											// x coord. of list
											this.screenHeight - this.safeTitleV - (this.rowHeight * 5), // y coord. of list
											(this.screenWidth - (2 * this.safeTitleH)), 				// width of list (full screen)
											this.rowHeight * 5,											// height of list (5/8  of body). Defined in terms of row height to ensure that height is an even multiple or rowheight.
											this.rowHeight,												// row height
											this.albumArtView,
											this.albumArtBGView,
											this.albumNameText,
											this.albumNameBGText,
											this.artistNameText,
											this.albumArtistBGText,
											this.yearText,
											this.yearBGText);
		setFocusDefault(this.list);
	}
		
	public BrowsePlaylistScreen(Harmonium app, PlaylistFile playlistFile) {
		this(app, playlistFile.toString());

		this.playlistFile = playlistFile;
		
		// Add playlist tracks to the list.
		List<Playable> tracks = new ArrayList<Playable>();
		tracks.addAll( playlistFile.listMemberTracks(this.app) );
		this.list.add( tracks.toArray() );
	}

	public BrowsePlaylistScreen(Harmonium app, List<Playable> playlist)
	{
		this(app, "\"Now Playing\" Playlist");
		
		// Add playlist tracks to the list.
		List<Playable> tracks = new ArrayList<Playable>();
		tracks.addAll( playlist );
		this.list.add( tracks.toArray() );
	}
	
	public boolean isNowPlayingPlaylist() {
		return playlistFile == null;
	}

	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {

        	PlaylistEligible musicItem = (PlaylistEligible)list.get( list.getFocus() );
            
        	if (!isNowPlayingPlaylist())
        		this.app.push(new TrackScreen(this.app, (Playable)musicItem, this.playlistFile), TRANSITION_LEFT);
        	else {
        		try
				{
					this.app.getDiscJockey().playItemInQueue((Playable)musicItem);
				} catch (Exception e)
				{
					return true;
				}
        		this.app.push(this.app.getDiscJockey().getNowPlayingScreen(), TRANSITION_LEFT);
        	}
        	
            return true;
        }  
        
        return super.handleAction(view, action);
    }

	@Override
	public boolean handleKeyPress(int key, long rawcode) {
	
		this.app.checkKeyPressToResetInactivityTimer(key);
		
		switch(key) {
		case KEY_PLAY:
			
			Playable startPlaying = (Playable)this.list.get(this.list.getFocus());

			if (this.playlistFile != null) {
				List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
				playlist.addAll( this.playlistFile.listMemberTracks(this.app) );

				boolean shuffleMode = this.app.getPreferences().getPlaylistFileDefaultShuffleMode();
				boolean repeatMode = this.app.getPreferences().getPlaylistFileDefaultRepeatMode();

				this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode, startPlaying);
			}
			else {
        		try
				{
					this.app.getDiscJockey().playItemInQueue(startPlaying);
				} catch (Exception e)
				{
					return true;
				}
        		this.app.push(this.app.getDiscJockey().getNowPlayingScreen(), TRANSITION_LEFT);
			}
				
			return true;
		}
		
		return super.handleKeyPress(key, rawcode);
	}

	public void focusNowPlaying() {
    	if (isNowPlayingPlaylist()) {
    		int index = this.app.getDiscJockey().getNowPlayingIndex();
    		this.list.setFocus(index, false);
    	}
	}
	
	@Override
	public boolean handleEnter(Object arg0, boolean arg1) {
		return super.handleEnter(arg0, arg1);
	}
	
	@Override
	public boolean handleExit() {
		return super.handleExit();
	}
	
	protected class HPlaylistBrowseList extends HAlbumArtList {

		HPlaylistBrowseList(BView parent, int x, int y, int width, int height, int rowHeight, BView foreground,
				BView background, BText albumNameText, BText albumNameBGText, BText albumArtistText,
				BText albumArtistBGText, BText yearText, BText yearBGText)
		{
			super(parent, x, y, width, height, rowHeight, foreground, background, albumNameText, albumNameBGText, albumArtistText,
					albumArtistBGText, yearText, yearBGText);
		}

		protected String getRowText(int index) {
			Playable p = (Playable)this.get(index);
			return p.getTrackName() + " - " + p.getArtistName();
		}
	}
}
