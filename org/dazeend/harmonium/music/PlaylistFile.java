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

import java.io.File;
import java.util.List;

import org.dazeend.harmonium.Harmonium;

public interface PlaylistFile extends PlaylistEligible, Comparable<PlaylistFile> {

	File getFile();
	
	/**
	 * returns the default shuffle mode for this playlist
	 * 
	 * @return
	 */
	boolean getShuffleMode(Harmonium app);
	
	/**
	 * returns the default repeat mode for this PlaylistFile
	 * 
	 * @return
	 */
	boolean getRepeatMode(Harmonium app);
	
	/**
	 * returns the list of PlaylistEligible objects that are contained
	 * by this PlaylistFile.
	 * 
	 * @return
	 */
	List<PlaylistEligible> getMembers();
	
	/**
	 * Returns the description for this Playlist.
	 * @return
	 */
	String getDescription();
}
