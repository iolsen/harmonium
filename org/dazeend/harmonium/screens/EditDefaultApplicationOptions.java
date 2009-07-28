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

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class EditDefaultApplicationOptions extends HScreen {

	private BButton screenSaverButton;
	private BButton OKButton;

	private Resource trueButtonLabel;
	private Resource falseButtonLabel;
	
	private static final String OK_LABEL = "Set Options";
	
	/**
	 * @param app
	 * @param title
	 */
	public EditDefaultApplicationOptions(Harmonium app) {
		super(app, "Application Options");	
		
		this.app = app;
									
		this.trueButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "On");
		this.falseButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Off");
		
		// Create screensaver label
		BText screenSaverText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4, 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		screenSaverText.setFont(this.app.hSkin.barFont);
		screenSaverText.setColor(HSkin.NTSC_WHITE);
		screenSaverText.setValue("Screen Blanking:");
		screenSaverText.setFlags(RSRC_HALIGN_LEFT);
		screenSaverText.setVisible(true);
		
		// Create screensaver button
		int buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		
		this.screenSaverButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().useScreenSaver()) {
			this.screenSaverButton.setResource(this.trueButtonLabel);
			this.screenSaverButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "screenSaverToFalse", null, null, H_DOWN, true);
		}
		else {
			this.screenSaverButton.setResource(this.falseButtonLabel);
			this.screenSaverButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "screenSaverToTrue", null, H_DOWN, true);
		}
		this.screenSaverButton.setFocusable(true);
		this.setFocusDefault(this.screenSaverButton);
		
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
			boolean screenSaverMode;
			
			// Find the selected repeat mode
			if(this.screenSaverButton.getResource().equals(this.trueButtonLabel)) {
				screenSaverMode = true;
			}
			else {
				screenSaverMode = false;
			}
			
			// set Preferences
			this.app.getPreferences().setUseScreenSaver(screenSaverMode);
			
			this.app.pop();
			
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
		if(action.equals("screenSaverToFalse")) {
			this.app.play("updown.snd");
			this.screenSaverButton.setResource(this.falseButtonLabel);
			this.screenSaverButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "screenSaverToTrue", null, H_DOWN, true);
			this.screenSaverButton.getHighlights().refresh();
		}
		else if(action.equals("screenSaverToTrue")) {
			this.app.play("updown.snd");
			this.screenSaverButton.setResource(this.trueButtonLabel);
			this.screenSaverButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "screenSaverToFalse", null, null, H_DOWN, true);
			this.screenSaverButton.getHighlights().refresh();
		}
		return super.handleAction(view, action);
	}
}
