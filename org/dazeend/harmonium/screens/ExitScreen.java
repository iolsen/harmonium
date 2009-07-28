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

import com.tivo.hme.bananas.BView;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
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
