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
public class EditDefaultAlbumArtistOptions extends HScreen {

	private BButton repeatButton;
	private BButton shuffleButton;
	private BButton albumSortButton;
	private BButton sortButton;
	private BButton OKButton;
	
	private Resource trueButtonLabel;
	private Resource falseButtonLabel;
	private Resource sortByNameLabel;
	private Resource sortByNumberLabel;
	private Resource sortAlbumByNameLabel;
	private Resource sortAlbumByYearLabel;
	
	private static final String OK_LABEL = "Set Options";
	
	/**
	 * @param app
	 * @param title
	 */
	public EditDefaultAlbumArtistOptions(Harmonium app) {
		super(app, "Album Artist Options");	
		
		this.app = app;
									
		this.trueButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "On");
		this.falseButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Off");
		this.sortByNameLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Title");
		this.sortByNumberLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Track Number");
		this.sortAlbumByNameLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Title");
		this.sortAlbumByYearLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Release Year");
		
		
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
		int buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		
		this.repeatButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumArtistDefaultRepeatMode()) {
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
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumArtistDefaultShuffleMode()) {
			this.shuffleButton.setResource(this.trueButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "shuffleToFalse", null, H_UP, H_DOWN, true);
		}
		else {
			this.shuffleButton.setResource(this.falseButtonLabel);
			this.shuffleButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "shuffleToTrue", H_UP, H_DOWN, true);
		}
		
		this.shuffleButton.setFocusable(true);
		
		// Create album sort label
		BText sortAlbumText = new BText(	this, 
				this.safeTitleH, 
				this.screenHeight / 4 + (4 * this.rowHeight), 
				( this.screenWidth - (2 * this.safeTitleH) ) / 2,
				this.rowHeight
		);	
		sortAlbumText.setFont(this.app.hSkin.barFont);
		sortAlbumText.setColor(HSkin.NTSC_WHITE);
		sortAlbumText.setValue("Sort Albums by:");
		sortAlbumText.setFlags(RSRC_HALIGN_LEFT);
		sortAlbumText.setVisible(true);
		
		// Create album sort button
		this.albumSortButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4 + (4 * this.rowHeight),
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumSortMode().equals(ApplicationPreferences.SORT_ALBUMS_BY_NAME)) {
			this.albumSortButton.setResource(this.sortAlbumByNameLabel);
			this.albumSortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "albumSortToYear", H_UP, H_DOWN, true);
		}
		else if(this.app.getPreferences().getAlbumSortMode().equals(ApplicationPreferences.SORT_ALBUMS_BY_YEAR)) {
			this.albumSortButton.setResource(this.sortAlbumByYearLabel);
			this.albumSortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "albumSortToName", null, H_UP, H_DOWN, true);
		}
		else {
			this.app.push(new ErrorScreen(this.app, "Illegal value for album sort mode"), TRANSITION_LEFT);
		}
		
		this.albumSortButton.setFocusable(true);
		
		// Create sort label
		BText sortText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4 + (6 * this.rowHeight), 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		sortText.setFont(this.app.hSkin.barFont);
		sortText.setColor(HSkin.NTSC_WHITE);
		sortText.setValue("Sort Tracks by:");
		sortText.setFlags(RSRC_HALIGN_LEFT);
		sortText.setVisible(true);
		
		// Create sort button
		this.sortButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4 + (6 * this.rowHeight),
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getAlbumArtistTrackSortMode().equals(ApplicationPreferences.SORT_TRACKS_BY_NUMBER)) {
			this.sortButton.setResource(this.sortByNumberLabel);
			this.sortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "trackSortToName", null, H_UP, H_DOWN, true);
		}
		else if(this.app.getPreferences().getAlbumArtistTrackSortMode().equals(ApplicationPreferences.SORT_TRACKS_BY_NAME)) {
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
			String albumSortMode;
			
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
			
			// Find selected album sort mode
			if(this.albumSortButton.getResource().equals(this.sortAlbumByNameLabel)) {
				albumSortMode = ApplicationPreferences.SORT_ALBUMS_BY_NAME;
			}
			else {
				albumSortMode = ApplicationPreferences.SORT_ALBUMS_BY_YEAR;
			}
			
			// Find selected track sort mode
			if(this.sortButton.getResource().equals(this.sortByNameLabel)) {
				sortMode = ApplicationPreferences.SORT_TRACKS_BY_NAME;
			}
			else {
				sortMode = ApplicationPreferences.SORT_TRACKS_BY_NUMBER;
			}

			
			// set Preferences
			this.app.getPreferences().setAlbumArtistDefaultRepeatMode(repeatMode);
			this.app.getPreferences().setAlbumArtistDefaultShuffleMode(shuffleMode);
			this.app.getPreferences().setAlbumSort(albumSortMode);
			this.app.getPreferences().setAlbumArtistTrackSort(sortMode);
			
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
		else if(action.equals("albumSortToName")) {
			this.app.play("updown.snd");
			this.albumSortButton.setResource(this.sortAlbumByNameLabel);
			this.albumSortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "albumSortToYear", H_UP, H_DOWN, true);
			this.albumSortButton.getHighlights().refresh();
		}
		else if(action.equals("albumSortToYear")) {
			this.app.play("updown.snd");
			this.albumSortButton.setResource(this.sortAlbumByYearLabel);
			this.albumSortButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "albumSortToName", null, H_UP, H_DOWN, true);
			this.albumSortButton.getHighlights().refresh();
		}
			
		return super.handleAction(view, action);
	}
}
