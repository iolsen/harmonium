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
 
package org.dazeend.harmonium.music;

import java.awt.Image;

import org.dazeend.harmonium.FactoryPreferences;

/**
 * Defines the interface used to read album information from items in the
 * music collection.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public interface AlbumReadable {
	
	public boolean hasAlbumArt(FactoryPreferences prefs);
	
	public Image getAlbumArt(FactoryPreferences prefs);
	
	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height);
	
	public String getAlbumArtistName();
	
	public String getAlbumName();
	
	public int getReleaseYear();
	
	public String toString();
}
