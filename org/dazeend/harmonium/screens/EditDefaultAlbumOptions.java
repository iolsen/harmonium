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

import org.dazeend.harmonium.ApplicationPreferences;
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
public class EditDefaultAlbumOptions extends HScreen {

	private BButton repeatButton;
	private BButton shuffleButton;
	private BButton sortButton;
	private BButton OKButton;
	
	private Resource trueButtonLabel;
	private Resource falseButtonLabel;
	private Resource sortByNameLabel;
	private Resource sortByNumberLabel;
	
	private static final String OK_LABEL = "Set Options";
	
	/**
	 * @param app
	 * @param title
	 */
	public EditDefaultAlbumOptions(Harmonium app) {	
		super(app, "Album Options");
		
		this.app = app;
									
		this.trueButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "On");
		this.falseButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Off");
		this.sortByNameLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Title");
		this.sortByNumberLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Track Number");
		
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
		setManagedView(repeatText);
		
		// Create repeat button
		int buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		
		this.repeatButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumDefaultRepeatMode()) {
			this.repeatButton.setResource(this.trueButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "repeatToFalse", null, null, H_DOWN, true);
		}
		else {
			this.repeatButton.setResource(this.falseButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "repeatToTrue", null, H_DOWN, true);
		}
		this.repeatButton.setFocusable(true);
		this.setFocusDefault(this.repeatButton);
		
		// Create shuffle label
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
		setManagedView(shuffleText);
		
		// Create shuffle button
		this.shuffleButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4 + (2 * this.rowHeight),
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumDefaultShuffleMode()) {
			this.shuffleButton.setResource(this.trueButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "shuffleToFalse", null, H_UP, H_DOWN, true);
		}
		else {
			this.shuffleButton.setResource(this.falseButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "shuffleToTrue", H_UP, H_DOWN, true);
		}
		
		this.shuffleButton.setFocusable(true);
		
		// Create sort label
		BText sortText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4 + (4 * this.rowHeight), 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		sortText.setFont(this.app.hSkin.barFont);
		sortText.setColor(HSkin.NTSC_WHITE);
		sortText.setValue("Sort Tracks by:");
		sortText.setFlags(RSRC_HALIGN_LEFT);
		sortText.setVisible(true);
		setManagedView(sortText);
		
		this.sortButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4 + (4 * this.rowHeight),
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumTrackSortMode().equals(ApplicationPreferences.SORT_TRACKS_BY_NUMBER)) {
			this.sortButton.setResource(this.sortByNumberLabel);
			this.sortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "trackSortToName", null, H_UP, H_DOWN, true);
		}
		else if(this.app.getPreferences().getAlbumTrackSortMode().equals(ApplicationPreferences.SORT_TRACKS_BY_NAME)) {
			this.sortButton.setResource(this.sortByNameLabel);
			this.sortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "trackSortToNumber", H_UP, H_DOWN, true);
		}
		else {
			this.app.push(new ErrorScreen(this.app, "Illegal value for track sort mode"), TRANSITION_LEFT);
		}
		
		this.sortButton.setFocusable(true);
		
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
			String sortMode;
			
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

			// Find selected track sort mode
			if(this.sortButton.getResource().equals(this.sortByNameLabel)) {
				sortMode = ApplicationPreferences.SORT_TRACKS_BY_NAME;
			}
			else {
				sortMode = ApplicationPreferences.SORT_TRACKS_BY_NUMBER;
			}
			
			// set Preferences
			this.app.getPreferences().setAlbumDefaultRepeatMode(repeatMode);
			this.app.getPreferences().setAlbumDefaultShuffleMode(shuffleMode);
			this.app.getPreferences().setAlbumTrackSort(sortMode);
			
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
		else if(action.equals("trackSortToName")) {
			this.app.play("updown.snd");
			this.sortButton.setResource(this.sortByNameLabel);
			this.sortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "trackSortToNumber", H_UP, H_DOWN, true);
			this.sortButton.getHighlights().refresh();
		}
		else if(action.equals("trackSortToNumber")) {
			this.app.play("updown.snd");
			this.sortButton.setResource(this.sortByNumberLabel);
			this.sortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "trackSortToName", null, H_UP, H_DOWN, true);
			this.sortButton.getHighlights().refresh();
		}
			
		return super.handleAction(view, action);
	}
}

