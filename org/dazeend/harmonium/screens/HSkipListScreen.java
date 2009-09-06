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

/**
 * Defines a generic screen containing a list that fills the body of the screen.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class HSkipListScreen extends HScreen {
	
	protected HSkipList list;		// the list in this screen

	
	/**
	 * @param app
	 */
	public HSkipListScreen(Harmonium app, String title) {
		super(app, title);
		
		// Create list that fills the body of the screen
		list = new HSkipList(	this.getNormal(), 							// Put list on "normal" level
							this.safeTitleH , 							// x coord. of list origin
							(int)(this.screenHeight * 0.25), 			// y coord. of list origin
							(this.screenWidth - (2 * this.safeTitleH)), // width of list (full screen)
							rowHeight * 8,								// height of list (full screen). Defined in terms of row height to ensure that height is an even multiple or rowheight.
							rowHeight									// row height for 8 rows
		);
		setFocusDefault(list);
	}

}
