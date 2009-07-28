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

public abstract class PlaylistFile implements PlaylistEligible, Comparable<PlaylistFile> {

	protected File file;
	
	/**
	 * Gets the File object that represents this PlaylistFile on disk.
	 * 
	 * @return
	 */
	public File getFile() {
		return this.file;
	}
	
	/**
	 * returns the default shuffle mode for this playlist
	 * 
	 * @return
	 */
	public abstract boolean getShuffleMode(Harmonium app);
	
	/**
	 * returns the default repeat mode for this PlaylistFile
	 * 
	 * @return
	 */
	public abstract boolean getRepeatMode(Harmonium app);
	
	/**
	 * returns the list of PlaylistEligible objects that are contained
	 * by this PlaylistFile.
	 * 
	 * @return
	 */
	public abstract List<PlaylistEligible> getMembers();
	
	/**
	 * Returns the description for this Playlist.
	 * @return
	 */
	public abstract String getDescription();
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PlaylistFile that) {
		
		// NullPointerException if we are trying to compare to a null object
		if(that == null){
			throw new NullPointerException();
		}
		
		return this.toString().compareToIgnoreCase(that.toString());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PlaylistFile other = (PlaylistFile) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}


	
	
}
