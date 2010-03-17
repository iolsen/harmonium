package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;

/**
 * Defines a generic screen containing a list that fills the body of the screen.
 */
public class HListScreen extends HScreen {
	
	protected HList list;		// the list in this screen

	
	/**
	 * @param app
	 */
	public HListScreen(Harmonium app, String title) {
		super(app, title);
		
		// Create list that fills the body of the screen
		list = new HList(	this.getNormal(), 							// Put list on "normal" level
							this.safeTitleH , 							// x coord. of list origin
							(int)(this.screenHeight * 0.25), 			// y coord. of list origin
							(this.screenWidth - (2 * this.safeTitleH)), // width of list (full screen)
							rowHeight * 8,								// height of list (full screen). Defined in terms of row height to ensure that height is an even multiple or rowheight.
							rowHeight									// row height for 8 rows
		);
		setFocusDefault(list);
	}

}
