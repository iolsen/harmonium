package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BView;

public class ExitScreen extends HListScreen {
	
	private String exitString = "Exit";
	private String returnString = "Return to Harmonium";

	public ExitScreen(Harmonium app) {
		super(app, "Exit Harmonium?");
		this.app = app;
		
		list.add(this.returnString);
		list.add(this.exitString);
	}
	
	public boolean handleAction(BView view, Object action) {
        if(action.equals("select") || action.equals("right")) {
        	String menuOption = (String)list.get( list.getFocus() );
        	
        	if( menuOption.equals(this.returnString) ) {
        		this.app.pop();
        	}
        	else if( menuOption.equals(this.exitString) ) {
        		if(this.app.getDiscJockey().isPlaying()) {
    				this.app.getDiscJockey().stop();
    			}
        		this.app.play("select.snd");
        		this.app.setActive(false);
        	}
            return true;
        }  
        
        return super.handleAction(view, action);
    }

}
