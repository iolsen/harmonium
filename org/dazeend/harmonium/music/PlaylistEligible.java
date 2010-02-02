package org.dazeend.harmonium.music;

import java.util.List;

import org.dazeend.harmonium.Harmonium;

/**
 * Interface for objects that can be placed in a {@link Playlist}.
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