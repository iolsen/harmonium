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

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class EditPlaylistOptionsScreen extends HScreen {

	private HPLFile playlist;
	
	private BButton repeatButton;
	private BButton shuffleButton;
	private BButton OKButton;
	
	private Resource trueButtonLabel;
	private Resource falseButtonLabel;
	
	private static final String OK_LABEL = "Set Options";
	
	/**
	 * @param app
	 * @param title
	 */
	public EditPlaylistOptionsScreen(Harmonium app, HPLFile playlist) {
		super(app, "Edit Playlist Options");
		
		this.app = app;
		this.playlist = playlist;
		this.trueButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "On");
		this.falseButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Off");
		
		// Create repeat label
		BText repeatText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4, 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		repeatText.setFont(this.app.hSkin.barFont);
		repeatText.setColor(HSkin.NTSC_WHITE);
		repeatText.setValue("Repeat Mode:");
		repeatText.setFlags(RSRC_HALIGN_LEFT);
		repeatText.setVisible(true);
		
		// Create repeat button
		this.repeatButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.rowHeight
		);
		
		if(this.playlist.getRepeatMode(this.app)) {
			this.repeatButton.setResource(this.trueButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "repeatToFalse", null, null, H_DOWN, true);
		}
		else {
			this.repeatButton.setResource(this.falseButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "repeatToTrue", null, H_DOWN, true);
		}
		this.repeatButton.setFocusable(true);
		this.setFocusDefault(this.repeatButton);
		
		// Create repeat label
		BText shuffleText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4 + (2 * this.rowHeight), 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		shuffleText.setFont(this.app.hSkin.barFont);
		shuffleText.setColor(HSkin.NTSC_WHITE);
		shuffleText.setValue("Shuffle Mode:");
		shuffleText.setFlags(RSRC_HALIGN_LEFT);
		shuffleText.setVisible(true);
		
		// Create repeat button
		this.shuffleButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4 + (2 * this.rowHeight),
											( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.rowHeight
		);
		
		if(this.playlist.getShuffleMode(this.app)) {
			this.shuffleButton.setResource(this.trueButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "shuffleToFalse", null, H_UP, H_DOWN, true);
		}
		else {
			this.shuffleButton.setResource(this.falseButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "shuffleToTrue", H_UP, H_DOWN, true);
		}
		
		this.shuffleButton.setFocusable(true);
		
		// Create OK button
		this.OKButton = new BButton(this.getNormal(),										// Put list on "normal" level
									this.safeTitleH,										// x coord. of button
									this.screenHeight - this.safeTitleV - this.rowHeight, 	// y coord - Align with bottom of screen
									(this.screenWidth - (2 * this.safeTitleH)) / 2, 		// width of button (half screen)
									this.rowHeight											// height of list
		);
		this.OKButton.setResource(createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, OK_LABEL));
		this.OKButton.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, H_UP, null, true);
		this.OKButton.setFocusable(true);
		
	}
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int code, long rawcode) {
		
		this.app.checkKeyPressToResetInactivityTimer(code);
		
		if(this.getFocus() != null && this.getFocus().equals(this.OKButton) && code == KEY_SELECT) {
			// The OKButton has been selected. save the selected options.
			boolean repeatMode;
			boolean shuffleMode;
			
			// Find the selected repeat mode
			if(this.repeatButton.getResource().equals(this.trueButtonLabel)) {
				repeatMode = true;
			}
			else {
				repeatMode = false;
			}
			
			// Find the selected shuffle mode
			if(this.shuffleButton.getResource().equals(this.trueButtonLabel)) {
				shuffleMode = true;
			}
			else {
				shuffleMode = false;
			}
			
			try {
				this.playlist.setOptions(repeatMode, shuffleMode);
				this.app.pop();
			}
			catch(IOException e) {
        		this.app.play("bonk.snd");
				this.app.push(new ErrorScreen(this.app, "IOException: Cannot write playlist to disk."), TRANSITION_LEFT);
	    	}
			return true;
		}
		else if(code == KEY_LEFT) {
			// Catch the left key press to prevent the screen from popping when on an option button
			return true;
		}
		return super.handleKeyPress(code, rawcode);
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.screens.HScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView view, Object action) {
		if(action.equals("repeatToFalse")) {
			this.app.play("updown.snd");
			this.repeatButton.setResource(this.falseButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "repeatToTrue", null, H_DOWN, true);
			this.repeatButton.getHighlights().refresh();
		}
		else if(action.equals("repeatToTrue")) {
			this.app.play("updown.snd");
			this.repeatButton.setResource(this.trueButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "repeatToFalse", null, null, H_DOWN, true);
			this.repeatButton.getHighlights().refresh();
		}
		else if(action.equals("shuffleToFalse")) {
			this.app.play("updown.snd");
			this.shuffleButton.setResource(this.falseButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "shuffleToTrue", H_UP, H_DOWN, true);
			this.shuffleButton.getHighlights().refresh();
		}
		else if(action.equals("shuffleToTrue")) {
			this.app.play("updown.snd");
			this.shuffleButton.setResource(this.trueButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "shuffleToFalse", null, H_UP, H_DOWN, true);
			this.shuffleButton.getHighlights().refresh();
		}
			
		return super.handleAction(view, action);
	}

	
}
