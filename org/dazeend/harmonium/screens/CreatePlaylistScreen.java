package org.dazeend.harmonium.screens;

import java.io.File;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.HPLFile;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.music.PlaylistEligible;

import com.almilli.tivo.bananas.hd.HDKeyboard;
import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BKeyboard;
import com.tivo.hme.bananas.BView;

public class CreatePlaylistScreen extends HScreen {

	private PlaylistEligible musicItem;
	private BKeyboard keyboard;
	private BButton OKButton;

	
	
	private static final String OK_LABEL = "Create New Playlist";
	
	public CreatePlaylistScreen(Harmonium app, PlaylistEligible musicItem) {
		super(app, "Create Playlist with Filename...");
		
		this.app = app;
		this.musicItem = musicItem;
		
		
		// Set up keyboard
		if(this.app.getHeight() >= 720) {
			this.keyboard = new HHDKeyboard(this.app,
											this.getNormal(),							// Parent view
											this.safeTitleH,							// x
											this.screenHeight / 4,						// y
											this.screenWidth - (2 * this.safeTitleH),	// width
											this.rowHeight * 7,							// height
											HDKeyboard.PLAIN_KEYBOARD,					// keyboard type
											true										// show tips
			);
		}
		else {
			this.keyboard = new HSDKeyboard(this.app,
											this.getNormal(),							// Parent view
											this.safeTitleH,							// x
											this.screenHeight / 4,						// y
											this.screenWidth - (2 * this.safeTitleH),	// width
											this.rowHeight * 7,							// height
											BKeyboard.PLAIN_KEYBOARD,					// keyboard type
											true										// show tips
			);
		}
		this.keyboard.setFocusable(true);
		this.setFocusDefault(this.keyboard);

		// Create OK button
		this.OKButton = new BButton(this.getNormal(),										// Put list on "normal" level
									this.safeTitleH,										// x coord. of button
									this.screenHeight - this.safeTitleV - this.rowHeight, 	// y coord - Align with bottom of screen
									(this.screenWidth - (2 * this.safeTitleH)) / 2, 		// width of button (half screen)
									this.rowHeight											// height of list
		);
		setManagedResource(OKButton, createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, OK_LABEL));
		this.OKButton.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", "createPlaylist", null, null, true);
		this.OKButton.setFocusable(true);
		
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int code, long rawcode) {
		this.app.checkKeyPressToResetInactivityTimer(code);
		if(code == KEY_UP) {
			this.setFocus(this.keyboard);
			return true;
		}
		else if(this.getFocus() != null && this.getFocus().equals(this.OKButton) && code == KEY_SELECT) {
			postEvent(new BEvent.Action(this.getFocus(), "createPlaylist"));
		}
		return super.handleKeyPress(code, rawcode);
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.screens.HScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView arg0, Object arg1) {
		
		if( arg1.equals("createPlaylist") ) {
			
			// Create the playlist
			String filename = this.keyboard.getValue();
			
			if( filename != null && (! filename.equals("") ) ) {
				if(! filename.endsWith(".hpl") ) {
					filename = filename + ".hpl";
				}
				
				File playlistFile = new File(this.app.getHFactory().getPreferences().getPlaylistRoot(), filename);
				
				if( ! playlistFile.exists() ) {
					
					try {
			    		// File does not yet exist on disk. Create data structure.
						HPLFile playlist = new HPLFile(	this.app,														// app
														playlistFile,													// File
														this.app.getPreferences().getPlaylistFileDefaultShuffleMode(),	// shuffle mode
														this.app.getPreferences().getPlaylistFileDefaultRepeatMode(),	// repeat mode
														"",																// description
														this.musicItem													// item to add to playlist
						);
						
						// Add the new playlist to the datastructure
						MusicCollection.getMusicCollection(this.app.getHFactory()).addPlaylist(playlist);
						
						// Pop this screen
						this.app.pop();
					}
					catch(Exception e) {
						this.app.play("bonk.snd");
						this.app.push(new ErrorScreen(this.app, "Cannot create playlist: " + e), TRANSITION_LEFT);
					}
					
				}
				else {
					this.app.play("bonk.snd");
					this.app.push(new ErrorScreen(this.app, "Did not create playlist: File already exists"), TRANSITION_LEFT);
				}
			}
			return true;
        } 
		else if (arg1.equals("left")) {
            this.app.pop();
            return true;                
        } 
		return super.handleAction(arg0, arg1);
	}
	
	
}