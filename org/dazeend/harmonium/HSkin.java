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
 
package org.dazeend.harmonium;

import java.awt.Color;

import com.tivo.hme.bananas.BSkin;

/**
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class HSkin extends BSkin {
	
	// Define constant colors
	public static final Color NTSC_WHITE = new Color(235, 235, 235);
	public static final Color NTSC_YELLOW = new Color(235, 192, 0);
	public static final Color NTSC_GREEN = new Color(0, 235, 0);
	
	// Define screen element colors
	public static Color TITLE_SHADOW_COLOR = Color.BLACK;
	public static Color TITLE_TEXT_COLOR = HSkin.NTSC_YELLOW;
	public static Color BAR_TEXT_COLOR = HSkin.NTSC_WHITE;
	public static Color PARAGRAPH_TEXT_COLOR = HSkin.NTSC_WHITE;
	public static Color PARAGRAPH_LABEL_TEXT_COLOR = HSkin.NTSC_WHITE;
	
	// Define static layout values
	public static int TITLE_SHADOW_OFFSET = 3;
	
	// Public Fonts
	public String titleFont;
	public String barFont;
	public String paragraphFont;
	public String paragraphLabelFont;
	
	// Public Font Sizes
	public int titleFontSize;
	public int barFontSize;
	public int paragraphFontSize;
	public int paragraphLabelFontSize;

	/**
	 * @param app	HDApplication
	 */
	public HSkin(Harmonium app) {
		super(app);
		
		if(app.getHFactory().getPreferences().inDebugMode()) {
			System.out.println("DEBUG: HSkin: Screen Height: " + app.getHeight() );
			System.out.println("DEBUG: HSkin: Screen Width: " + app.getWidth() );
			System.out.flush();
		}
		
		// font sizes are defined for 720p resolution then scaled appropriately
		double scaleFactor = ( (double) app.getHeight() ) / 720;
		
		// Define Title text
		int titleRawFontSize = 68;		// originally 68
		this.titleFontSize = (int)(titleRawFontSize * scaleFactor);
		this.titleFont = "default-" + this.titleFontSize + ".font";
		
		// Define Bar text
		int titleRawBarSize = 36;		// originally 32
		this.barFontSize = (int)(titleRawBarSize * scaleFactor);
		this.barFont = "default-" + this.barFontSize + ".font";
		
		// Define Paragraph text
		int paragraphRawFontSize = 28;	// originally 24
		this.paragraphFontSize = (int)(paragraphRawFontSize * scaleFactor);
		this.paragraphFont = "default-" + this.paragraphFontSize + ".font";
		
		// Define ParagraphLabel text
		int paragraphLabelRawFontSize = 22;	// originally 18
		this.paragraphLabelFontSize = (int)(paragraphLabelRawFontSize * scaleFactor);
		this.paragraphLabelFont = "default-" + this.paragraphLabelFontSize + ".font";
	}

}
