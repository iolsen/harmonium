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

import java.util.List;

import org.dazeend.harmonium.Harmonium;

/**
 * Interface for objects that can be placed in a {@link Playlist}.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public interface PlaylistEligible {

	/**
	 * Gets a sorted <code>List</code> of {@link Playable} objects in this object.
	 * 
	 * @param app
	 * @return
	 */
	public List<Playable> listMemberTracks(Harmonium app);
	
	/**
	 * Returns a string representing the object.
	 * 
	 * @return	the name of the object
	 */
	public String toString();
	
	/**
	 * Returns a title formatted string representing the object.
	 * 
	 * @return	the title formated name of the object
	 */
	public String toStringTitleSortForm();
	
}