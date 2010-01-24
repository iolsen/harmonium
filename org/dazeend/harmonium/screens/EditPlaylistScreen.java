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
import org.dazeend.harmonium.Harmonium.DiscJockey;
import org.dazeend.harmonium.Harmonium.DiscJockey.CurrentPlaylist;
import org.dazeend.harmonium.music.EditablePlaylist;
import org.dazeend.harmonium.music.Playable;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.View;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class EditPlaylistScreen extends HScreen {

	private EditablePlaylist editablePlaylist;
	private BButton moveButton;
	private ButtonList list;
	private int rows;
	private boolean shuffled;
	
	
	static class ButtonList extends BList 
    {
        private BButton moveButton;
        
		/**
         * Constructor
         */
        public ButtonList(HManagedResourceScreen parent, BButton moveButton, int x, int y, int width, int listHeight, int rowHeight)
        {
            super(	parent, x, y, width, listHeight, rowHeight);
            
            this.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", H_RIGHT);
            this.setFocusable(true);
            parent.setFocusDefault(this);

            this.moveButton = moveButton;
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
		        text.setValue( String.valueOf(index + 1) + ". " + ((Playable)this.get(index)).getTrackName() + " - " + ((Playable)this.get(index)).getArtistName());

		        View p = parent;
		        while (p instanceof HManagedResourceScreen == false && p != null)
		        	p = p.getParent();
		        if (p != null)
		        	((HManagedResourceScreen)p).setManagedView(text);
			}
			
		}
        
		
		/* (non-Javadoc)
		 * @see com.tivo.hme.bananas.BList#handleFocus(boolean, com.tivo.hme.bananas.BView, com.tivo.hme.bananas.BView)
		 */
		/*@Override
		public boolean handleFocus(boolean isGained, BView gained, BView lost) {
            
			boolean value = super.handleFocus(isGained, gained, lost);
			
            if (isGained && gained.getParent() == this) {
                
            	// move the button and its highlights (bar, arrows), and animate it.
            	// Note that the list and button have different parents, so that must be taken
            	// into account when setting the Y coordinate.
                this.moveButton.setLocation(this.moveButton.getX(), gained.getParent().getY() + gained.getY(), this.getResource(BList.ANIM));
                this.moveButton.getHighlights().refresh(this.getResource(BList.ANIM));
            }
            
			return value;
		}*/
		
		private void setMoveButtonLocation() {
			if(this.getFocus() < 0) {
				return;
			}
			
			int rowOffset = this.getFocus() - this.getTop();
			int yOffset = rowOffset * this.getRowHeight();
			
			// move the button and its highlights (bar, arrows), and animate it.
        	// Note that the list and button have different parents, so that must be taken
        	// into account when setting the Y coordinate.
            this.moveButton.setLocation(this.moveButton.getX(), this.getY() + yOffset, this.getResource(BList.ANIM));
            this.moveButton.getHighlights().refresh(this.getResource(BList.ANIM));
		}

		/* (non-Javadoc)
		 * @see com.tivo.hme.bananas.BList#refresh()
		 */
		@Override
		public void refresh() {
			super.refresh();
			this.setMoveButtonLocation();
		}
		
		
		
    }
	
	/**
	 * @param app
	 * @param title
	 */
	public EditPlaylistScreen(Harmonium app, EditablePlaylist playlist) {
		super(app, playlist.toString());
		
		this.app = app;
		this.editablePlaylist = playlist;
		this.rows = 8;
		
		if (this.editablePlaylist instanceof CurrentPlaylist)
			this.shuffled = app.getDiscJockey().isShuffling();
		
		// create list and button only if the playlist has members
		if(this.editablePlaylist.listMemberTracks(app).size() > 0) {
			int buttonWidth =  ( this.screenWidth - (2 * this.safeTitleH) ) / this.rows;
	        
			
	        // Create button
			this.moveButton = new BButton(	this, 													// parent
												this.screenWidth - this.safeTitleH - buttonWidth,		// x
												this.screenHeight / 4,									// y
												buttonWidth,		// width
												this.rowHeight											// height
			);
	
			this.moveButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, H_LEFT, null, null, null, false);
			this.moveButton.setVisible(true);
			this.moveButton.setFocusable(true);
			
			// Create image view for arrows
	        BView iconView = new BView(this.moveButton, 0, 0, this.moveButton.getWidth(), this.moveButton.getHeight());
	        
	        // choose between HiDef and StdDef arrow images
	        if(this.screenHeight > 480) {
	        	iconView.setResource("up_down_720.png");
	        }
	        else {
	        	iconView.setResource("up_down_sd.png");
	        }
			
			this.list = new ButtonList(	this, 													// parent
										this.moveButton,										// button
										this.safeTitleH, 										// x
					            		this.screenHeight / 4, 									// y
					            		( this.screenWidth - (2 * this.safeTitleH) ) - (3 * buttonWidth / 2), 	// width
					            		this.rowHeight * 8,										// list height
					            		this.rowHeight											// row height
			);
			
			buildList(false, 0);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.screens.HScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView view, Object action) {
		if(action.equals("moveUp")) {
			
			// play a sound
			this.app.play("updown.snd");
			
			// Make the change in the playlist
			try {
				this.editablePlaylist.move(this.list.getFocus(), this.list.getFocus() - 1);
			}
			catch(IllegalArgumentException e) {
				return false;
			}
			
			// swap rows in the list
			Object temp = this.list.get(this.list.getFocus());
			this.list.set(this.list.getFocus(), this.list.get(this.list.getFocus() - 1));
			this.list.set(this.list.getFocus() - 1, temp);
			
			// update the list row that has focus (this also moves the button)
			this.list.setFocus(this.list.getFocus() - 1, true);
			
			// return focus to the button
			this.setFocus(this.moveButton);
			
			return true;
		}
		else if(action.equals("moveDown")) {
				
			// play a sound
			this.app.play("updown.snd");
			
			
			// Make the change in the playlist
			try {
				this.editablePlaylist.move(this.list.getFocus(), this.list.getFocus() + 1);
			}
			catch(IllegalArgumentException e) {
				return false;
			}
			// swap rows
			Object temp = this.list.get(this.list.getFocus());
			this.list.set(this.list.getFocus(), this.list.get(this.list.getFocus() + 1));
			this.list.set(this.list.getFocus() + 1, temp);
			
			// update the list row that has focus (this also moves the button)
			this.list.setFocus(this.list.getFocus() + 1, true);
			
			// return focus to the button
			this.setFocus(this.moveButton);
			
			return true;
		}
		else if(action.equals("moveWayUp")) {
			// play a sound
			this.app.play("updown.snd");
			
			// Figure out how far up to move
			int to;
			if(this.list.getFocus() == this.list.getTop()) {
				// We're already on the top row, so move up one screenful
				if(this.list.getTop() - this.rows < 0) {
					to = 0;
				}
				else {
					to = this.list.getTop() - this.rows;
				}
			}
			else {
				// Jump to the top of the screen
				to = this.list.getTop();
			}
			
			// Make the change in the playlist
			try {
				this.editablePlaylist.move(this.list.getFocus(), to);
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
			
			// swap rows in the list
			Object temp = this.list.get(this.list.getFocus());
			int i;
			for(i = this.list.getFocus(); i > to; --i) {
				this.list.set(i, this.list.get(i - 1));
			}
			this.list.set(i, temp);
			
			// update the list row that has focus (this also moves the button)
			this.list.setFocus(to, true);
			
			// return focus to the button
			this.setFocus(this.moveButton);
			
			return true;
		}
		else if(action.equals("moveWayDown")) {
			// play a sound
			this.app.play("updown.snd");
			
			// Figure out how far up to move
			int to;
			if(this.list.getFocus() == this.list.getTop() + this.rows - 1) {
				// We're already on the bottom row, so move down one screenful
				if(this.list.getFocus() + this.rows >= this.list.size()) {
					to = this.list.size() - 1;
				}
				else {
					to = this.list.getFocus() + this.rows;
				}
			}
			else {
				// Jump to the bottom of the screen
				to = this.list.getTop() + this.rows - 1;
			}
			
			// Make the change in the playlist
			try {
				this.editablePlaylist.move(this.list.getFocus(), to);
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
			
			// swap rows in the list
			Object temp = this.list.get(this.list.getFocus());
			int i;
			for(i = this.list.getFocus(); i < to; ++i) {
				this.list.set(i, this.list.get(i + 1));
			}
			this.list.set(i, temp);
			
			// update the list row that has focus (this also moves the button)
			this.list.setFocus(to, true);
			
			// return focus to the button
			this.setFocus(this.moveButton);
			
			return true;
		}
//		else if(action.equals("pop")) {
//			// write the newly edited playlist to disc
//			try {
//				this.editablePlaylist.save();
//			}
//			catch(IOException e) {
//				this.app.play("bonk.snd");
//				this.app.push(new ErrorScreen(this.app, "IOException: Cannot save playlist."), TRANSITION_LEFT);
//			}
//			
//			this.app.pop();
//			return true;
//		}
		else if(action.equals("deleteItem")) {
			
			// the the index to remove
			int index = this.list.getFocus();
			// remove the item from the playlist
			try {
				Playable removed = this.editablePlaylist.remove(index);
				if (removed == null)
				{
					// Tried to remvoe the currently playing song from the Now Playing playlist.
					this.app.play("bonk.snd");
					return true;
				}
				else
					this.app.play("select.snd");
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
			
			// re-populate the BList
			buildList(true, index);
		
			if(this.editablePlaylist.listMemberTracks(app).isEmpty()) {
				// this was the last member of the playlist, so pop the screen
				postEvent(new BEvent.Action(this, "pop"));
			}
			return true;
		}
		
		return super.handleAction(view, action);
	}
	
	private void buildList(boolean rebuild, int focusIndex)
	{
		if (rebuild)
		{
			if (focusIndex < 0)
				focusIndex = this.list.getFocus();
			this.list.clear();
		}

		this.list.add(this.editablePlaylist.listMemberTracks(app).toArray());
		
		// set the focus
		if(focusIndex < this.list.size()) {
			this.list.setFocus(focusIndex, false);
		}
		else {
			this.list.setFocus(this.list.size() - 1, false);
		}

		if (rebuild)
			this.list.refresh();
	}

	@Override
	public boolean handleEnter(Object arg0, boolean arg1) {

		// If we're editing the now playling playlist and the shuffle mode has 
		// changed while the screen was still on the stack, we need to rebuild
		// to reflect the shuffled/unshuffled playlist.
		if (this.editablePlaylist instanceof CurrentPlaylist && this.shuffled != this.app.getDiscJockey().isShuffling() )
		{
			DiscJockey dj = this.app.getDiscJockey(); 
			this.shuffled = dj.isShuffling();
			this.editablePlaylist = dj.getCurrentPlaylist();
			buildList(true, -1);
		}
		
		return super.handleEnter(arg0, arg1);
	}
	
	@Override
	public boolean handleExit()
	{
		if (this.editablePlaylist instanceof CurrentPlaylist)
		{
			try
			{
				this.editablePlaylist.save();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return super.handleExit();
	}
	
	@Override
    public boolean handleKeyPress(int code, long rawcode) 
    {
		this.app.checkKeyPressToResetInactivityTimer(code);
		if(this.getFocus().equals(this.moveButton)) {
			// Move the button around when the move button is selected
	    	switch (code) {
	    	case KEY_UP:
	    		if(this.list.getFocus() > 0) {
	    			postEvent(new BEvent.Action(this, "moveUp"));
	    		}
	    		return true;
	    	case KEY_DOWN:
	    		if( this.list.getFocus() < (this.list.size() - 1) ) {
	    			postEvent(new BEvent.Action(this, "moveDown"));
	    		}
	    		return true;
	    	case KEY_CHANNELUP:
	    		if(this.list.getFocus() > 0) {
	    			postEvent(new BEvent.Action(this, "moveWayUp"));
	    		}
	    		return true;
	    	case KEY_CHANNELDOWN:
	    		if( this.list.getFocus() < (this.list.size() - 1) ) {
	    			postEvent(new BEvent.Action(this, "moveWayDown"));
	    		}
	    		return true;
			}
		}
		else {
			// only catch these keys if the list has focus
			switch(code) {
			case KEY_CLEAR:
				postEvent(new BEvent.Action(this, "deleteItem"));
				return true;
			case KEY_SELECT:
				this.app.push(new TrackInfoScreen( this.app, (Playable)this.list.get( this.list.getFocus() ) ), TRANSITION_LEFT);
				return true;
			case KEY_NUM1:
				// Jump to 10% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10, true);
				return true;
			case KEY_NUM2:
				// Jump to 20% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 5, true);
				return true;
			case KEY_NUM3:
				// Jump to 30% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10 * 3, true);
				return true;
			case KEY_NUM4:
				// Jump to 40% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10 * 4, true);
				return true;
			case KEY_NUM5:
				// Jump to 50% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 2, true);
				return true;
			case KEY_NUM6:
				// Jump to 60% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10 * 6, true);
				return true;
			case KEY_NUM7:
				// Jump to 70% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10 * 7, true);
				return true;
			case KEY_NUM8:
				// Jump to 80% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10 * 8, true);
				return true;
			case KEY_NUM9:
				// Jump to 90% through the list
				this.getApp().play("select.snd");
				this.list.setFocus(this.list.size() / 10 * 9, true);
				return true;
			case KEY_NUM0:
				// Go to the end of the list, unless we're already there, in which case we jump to the top.
				this.getApp().play("select.snd");
				if (this.list.getFocus() == this.list.size() - 1)
					this.list.setFocus(0, true);
				else
					this.list.setFocus(this.list.size() - 1, true);
				return true;
			}
		}
	
    	return super.handleKeyPress(code, rawcode);
    }		
}
