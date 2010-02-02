package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Playable;

import com.tivo.hme.bananas.BView;

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
