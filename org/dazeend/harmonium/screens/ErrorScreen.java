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
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BText;

public class ErrorScreen extends HScreen {

	BButton returnButton;
	
	/**
	 * @param app
	 * @param title
	 */
	public ErrorScreen(Harmonium app, String message) {
		super(app, "ERROR");
		
		BText messageText = new BText(	getNormal(), 
										this.safeTitleH, 
										this.screenHeight / 4, 
										this.screenWidth - (2 * this.safeTitleH), 
										this.screenHeight - this.rowHeight - (this.screenHeight / 4)
		);
		messageText.setFlags(RSRC_VALIGN_TOP | RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP);
        messageText.setFont(app.hSkin.barFont);
        messageText.setValue(message);
		
		returnButton = new BButton(	this.getNormal(), 
									this.safeTitleH, 
									this.screenHeight - this.rowHeight, 
									(this.screenWidth - (2 * this.safeTitleH)) / 2,
									this.rowHeight
		);
		
		returnButton.setBarAndArrows(BAR_HANG, BAR_DEFAULT, null, null, "pop", null, true);
		returnButton.setResource(createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "Back"));
		setFocusDefault(returnButton);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int arg0, long arg1) {
		((Harmonium)this.getApp()).checkKeyPressToResetInactivityTimer(arg0);
		if(this.getFocus() != null && this.getFocus().equals(returnButton)) {
			if(arg0 == KEY_SELECT) {
				postEvent(new BEvent.Action(this.getFocus(), "pop"));
				return true;
			}
		}
		return super.handleKeyPress(arg0, arg1);
	}
	
	

	
}
