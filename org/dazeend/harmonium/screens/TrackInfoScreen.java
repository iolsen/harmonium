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

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Playable;

import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class TrackInfoScreen extends HAlbumInfoListScreen {

	private String screenTitle;
	private final String goBackString = "Go Back";
	private HList list;
	
	public TrackInfoScreen(Harmonium app, final Playable thisTrack) {
		super( app, thisTrack, thisTrack.toString(), false );
		
		this.screenTitle = thisTrack.toString();
		
		// Set up modified list
		int listHeight = rowHeight;
		this.list = new HList(	this.getNormal(), 									// Put list on "normal" level
								this.safeTitleH , 									// x coord. of button
								this.screenHeight - this.safeTitleV - listHeight, 	// y coord - Align with bottom of screen
								(this.screenWidth - (2 * this.safeTitleH)) / 2, 	// width of button (half screen)
								listHeight,											// height of list
								this.rowHeight										// height of each row
		);
		this.list.add(this.goBackString);
		this.setFocusDefault(this.list);
	}
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView view, Object action) {
		if(action.equals("right") || action.equals("select")) {
        	String menuOption = (String)this.list.get( this.list.getFocus() );
        	
        	if(menuOption.equals(this.goBackString)) {
        		this.app.pop();
        	}        
        }
        return super.handleAction(view, action);
    }
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.sdk.HmeObject#toString()
	 */
	@Override
	public String toString() {
		return this.screenTitle;
	}
}
