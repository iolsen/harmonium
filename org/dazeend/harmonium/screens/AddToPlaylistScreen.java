package org.dazeend.harmonium.screens;

import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.DiscJockey;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.HPLFile;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlayableCollection;

import com.tivo.hme.bananas.BView;

public class AddToPlaylistScreen extends HListScreen {

	private PlayableCollection musicItem;
	private static final String NEW_PLAYLIST_LABEL = "Add to New Playlist";
	private static final String NP_PLAYLIST_LABEL = "Add to \"Now Playing\" Playlist";
	
	public AddToPlaylistScreen(Harmonium app, PlayableCollection musicItem) {
		super(app, "Add " + musicItem.toString() + " to Playlist");
		
		this.app = app;
		this.musicItem = musicItem;
		
		// if there's music playing, and we're not saving the current playlist, put "Now Playing" at the top.
		if ( this.app.getDiscJockey().hasCurrentPlaylist() && !(musicItem instanceof DiscJockey.CurrentPlaylist)) {
			this.list.add(NP_PLAYLIST_LABEL);
		}
		
		// sort existing HPL playlists by date last modified, add them to the list
		List<HPLFile> sortedHPLPlaylists = MusicCollection.getMusicCollection(this.app.getHFactory()).getHPLPlaylists();
		HPLFile.DateDescendingComparator c = new HPLFile.DateDescendingComparator();
		Collections.sort(sortedHPLPlaylists, c);
		this.list.add( sortedHPLPlaylists.toArray() );

		// Put "Add to new playlist" at the bottom of the list. 
		this.list.add(NEW_PLAYLIST_LABEL);
}
	
	
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
	 */
	@Override
	public boolean handleEnter(Object screenArgument, boolean isReturning) {
		
		if(isReturning) {
			// pops screen if we have just finished creating a new playlist
			this.app.pop();
		}
		
		return super.handleEnter(screenArgument, isReturning);
	}



	@Override
	public boolean handleAction(BView view, Object action) {
        if(action.equals("right") || action.equals("select")) {
        	if( list.get( list.getFocus() ).equals(NEW_PLAYLIST_LABEL) ) {
        		// Create a new playlist and add this music item to it
        		this.app.push(new CreatePlaylistScreen(this.app, this.musicItem), TRANSITION_LEFT);
        	}
        	else if (  list.get( list.getFocus() ).equals(NP_PLAYLIST_LABEL)  ) {
        		// Add music item to now playing playlist
        		this.app.getDiscJockey().enqueueAtEnd(this.musicItem);
        	}
        	else {
        		HPLFile hplFile = (HPLFile)list.get( list.getFocus() );
        		
        		// Add this music item's tracks to the HPL playlist
        		try {
        			hplFile.add(this.app, this.musicItem);
        			this.app.pop();
        		}
        		catch(Exception e) {
        			this.app.play("bonk.snd");
        			this.app.push(new ErrorScreen(this.app, "Could not write to playlist."), TRANSITION_LEFT);
        		}
        	}
        
            
        }  
        return super.handleAction(view, action);
    }
}
