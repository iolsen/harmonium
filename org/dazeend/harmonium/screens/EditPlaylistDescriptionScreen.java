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

import java.io.IOException;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.HPLFile;

import com.almilli.tivo.bananas.hd.HDKeyboard;
import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BKeyboard;
import com.tivo.hme.bananas.BView;


/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class EditPlaylistDescriptionScreen extends HScreen {
	
	private HPLFile playlist;
	private BKeyboard keyboard;
	private BButton OKButton;
	
	private static final String OK_LABEL = "Save Description";
	
	public EditPlaylistDescriptionScreen(Harmonium app, HPLFile playlist) {
		super(app, "Edit Playlist Description");
		
		this.app = app;
		this.playlist = playlist;
		
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
		this.keyboard.setValue(this.playlist.getDescription());
		this.keyboard.setFocusable(true);
		this.setFocusDefault(this.keyboard);

		// Create OK button
		this.OKButton = new BButton(this.getNormal(),										// Put list on "normal" level
									this.safeTitleH,										// x coord. of button
									this.screenHeight - this.safeTitleV - this.rowHeight, 	// y coord - Align with bottom of screen
									(this.screenWidth - (2 * this.safeTitleH)) / 2, 		// width of button (half screen)
									this.rowHeight											// height of list
		);
		this.OKButton.setResource(createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, OK_LABEL));
		this.OKButton.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", "editDescription", null, null, true);
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
			postEvent(new BEvent.Action(this.getFocus(), "editDescription"));
		}
		return super.handleKeyPress(code, rawcode);
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.screens.HScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView arg0, Object arg1) {
		if( arg1.equals("editDescription") ) {
			// Update the playlist description
			try {
				this.playlist.setDescription( this.keyboard.getValue() );
				
				// Return to the previous screen
				this.app.pop();
			}
			catch(IOException e) {
				this.app.play("bonk.snd");
				this.app.push(new ErrorScreen(this.app, "IOException: Cannot write playlist to disk."), TRANSITION_LEFT);
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
