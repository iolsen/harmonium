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
import org.dazeend.harmonium.PlayRate;
import org.dazeend.harmonium.music.PlaylistEligible;

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;

/**
 * Defines a generic screen for this app. Displays screen title and catches
 * select key press.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class HScreen extends HManagedResourceScreen {
	
	protected int screenWidth;
	protected int screenHeight;
	protected int safeTitleH;
	protected int safeTitleV;
	protected int rowHeight;
	private String screenTitle;
	protected BText titleText;

	/**
	 * 
	 * @param app	this application
	 * @param title	Title of this screen
	 */
	public HScreen(Harmonium app, String title) {
		super(app);
		
		// Set up screen dimensions
		this.screenWidth = app.getWidth();
		this.screenHeight = app.getHeight();
		this.safeTitleH = app.getSafeTitleHorizontal();
		this.safeTitleV = app.getSafeTitleVertical();
		this.rowHeight = (int)(((0.75 * this.screenHeight) - this.safeTitleV)/8);
		this.screenTitle = title;
		
		// Define all dimensions in terms of percentage of the screen height and width. This make it
		// resolution-safe.
		this.titleText = new BText(	this.getNormal(),
				this.safeTitleH,
				Harmonium.SAFE_ACTION_V,
				this.screenWidth - (2 * this.safeTitleH),
				(int)((0.24 * this.screenHeight) - Harmonium.SAFE_ACTION_V)
		
		);
		
		// Set title attributes
		this.titleText.setFont(app.hSkin.titleFont);
		this.titleText.setColor(HSkin.TITLE_TEXT_COLOR);
		this.titleText.setShadow(true);
		this.titleText.setShadow(HSkin.TITLE_SHADOW_COLOR, HSkin.TITLE_SHADOW_OFFSET);
		this.titleText.setFlags(RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_TEXT_WRAP);
		this.titleText.setValue(this.screenTitle);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView arg0, Object arg1) {
		// All "pop" actions are handled the same way, so handle them here.
		if(arg1.equals("pop")) {
			this.app.resetInactivityTimer();
			if(getBApp().getStackDepth() <= 1) {
				if( (this.app.getDiscJockey().getNowPlaying() != null) && (! this.app.getDiscJockey().getPlayRate().equals(PlayRate.STOP) ) ) {
					this.app.push(new ExitScreen(this.app), TRANSITION_LEFT);
				}
				else {
					getBApp().setActive(false);
				}
			}
			else {
				getBApp().pop();
			}
		}
		return super.handleAction(arg0, arg1);
	}
	
	@Override
	public boolean handleExit() {
		this.app.resetInactivityTimer();
		return super.handleExit();
	}
	
	/**
	 * Creates a list that catches the KEY_SELECT button press.
	 * 
	 * @author Charles Perry (harmonium@DazeEnd.org)
	 *
	 */
	protected class HList extends BList {
		
		public HList(BView parent, int x, int y, int width, int height, int rowHeight)
        {
            super(parent, x, y, width, height, rowHeight);
            
            // Configure list.
            this.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", "right");
        }
        
		
        /* (non-Javadoc)
		 * @see com.tivo.hme.bananas.BList#createRow(com.tivo.hme.bananas.BView, int)
		 */
		@Override
		protected void createRow(BView parent, int index) {
			if( get(index) != null) {
				int x = 10;
		        int y = 0;
		        int width = parent.getWidth() - (2 * x);
		        int height = parent.getHeight() - (2 * y);
		        
		        BText text = new BText(parent, x, y, width, height);
		        text.setShadow(true);
		        text.setFlags(RSRC_HALIGN_LEFT);
		        text.setFont( ( (Harmonium)this.getBApp() ).hSkin.barFont );
		        text.setColor(HSkin.BAR_TEXT_COLOR);
		        text.setValue( this.get(index).toString() );
			}
		}

		
		/**
         * Transform SELECT key into a "select" action.
         */
		@Override
        public boolean handleKeyPress(int code, long rawcode) 
        {
			((Harmonium)this.getApp()).checkKeyPressToResetInactivityTimer(code);
			switch (code) {
        	case KEY_SELECT:
        		postEvent(new BEvent.Action(this, "select"));
        		return true;
        	}
        	return super.handleKeyPress(code, rawcode);
        }
	}
	
	/**
	 * Creates a list that has additional navigation options:
	 *  - Skips through the alphabet using the ADVANCE/REPLAY buttons
	 *  - Skips to 1/10th intervals with the number buttons
	 * 
	 * @author Charles Perry (harmonium@DazeEnd.org)
	 *         Ian Olsen (ian.olsen@gmail.com)	
	 *
	 */
	protected class HSkipList extends HList {
		
		public HSkipList(BView parent, int x, int y, int width, int height, int rowHeight)
        {
            super(parent, x, y, width, height, rowHeight);
        }

		/* (non-Javadoc)
		 * @see org.dazeend.harmonium.screens.HScreen.HList#handleKeyPress(int, long)
		 */
		@Override
		public boolean handleKeyPress(int code, long rawcode) {
			
			((Harmonium)this.getApp()).checkKeyPressToResetInactivityTimer(code);
			
			PlaylistEligible focusObject;
			String currentLetter;
			int i;
			switch(code) {
			case KEY_ADVANCE:
				i = getFocus();
				if (i == -1) {
					return true;
				}
				focusObject = (PlaylistEligible)this.get(i);

				// Get the leading letter of the focused object
				currentLetter = focusObject.toStringTitleSortForm().toLowerCase().substring(0, 1);
	
				// Search forward until you reach an object that starts with a later letter
				i = this.getFocus() + 1;
				while( ( i < this.size() ) && ( ((PlaylistEligible)this.get(i)).toStringTitleSortForm().toLowerCase().substring(0, 1).compareTo(currentLetter) <= 0 ) ) {
					++i;
				}
				
				// Jump to the next letter (or end of list)
				this.getApp().play("pagedown.snd");
				this.setFocus(i, true);
				return true;
			case KEY_REPLAY:
				
				i = getFocus();
				if (i == -1) {
					return true;
				}
				focusObject = (PlaylistEligible)this.get(i);

				
				// Get the leading letter of the focused object
				currentLetter = focusObject.toStringTitleSortForm().toLowerCase().substring(0, 1);
				
				// Search backward until you reach an object that starts with an earlier letter
				i = this.getFocus() - 1;
				while( ( i >= 0 ) && ( ((PlaylistEligible)this.get(i)).toStringTitleSortForm().toLowerCase().substring(0, 1).compareTo(currentLetter) >= 0 ) ) {
					--i;
				}
				
				// Jump to the next letter (or end of list)
				this.getApp().play("pageup.snd");
				this.setFocus(i, true);
				return true;

			case KEY_NUM1:
				// Jump to 10% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10, true);
				return true;

			case KEY_NUM2:
				// Jump to 20% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 5, true);
				return true;

			case KEY_NUM3:
				// Jump to 30% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10 * 3, true);
				return true;

			case KEY_NUM4:
				// Jump to 40% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10 *4, true);
				return true;

			case KEY_NUM5:
				// Jump to 50% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 2, true);
				return true;

			case KEY_NUM6:
				// Jump to 60% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10 * 6, true);
				return true;

			case KEY_NUM7:
				// Jump to 70% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10 * 7, true);
				return true;

			case KEY_NUM8:
				// Jump to 80% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10 * 8, true);
				return true;

			case KEY_NUM9:
				// Jump to 90% through the list
				this.getApp().play("select.snd");
				this.setFocus(this.size() / 10 * 9, true);
				return true;

			case KEY_NUM0:
				// Go to the end of the list, unless we're already there, in which case we jump to the top.
				this.getApp().play("select.snd");
				if (this.getFocus() == this.size() - 1)
					this.setFocus(0, true);
				else
					this.setFocus(this.size() - 1, true);
				return true;
			}
			
			
			return super.handleKeyPress(code, rawcode);
		}
		
		
	}
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.sdk.HmeObject#toString()
	 */
	@Override
	public String toString() {
		return this.screenTitle;
	}
}
