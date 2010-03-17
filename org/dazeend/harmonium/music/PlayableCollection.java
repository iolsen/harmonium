package org.dazeend.harmonium.music;

import java.util.List;

import org.dazeend.harmonium.Harmonium;

/**
 * Interface for objects that represent a collection of one or more Playable objects.
 */
public interface PlayableCollection 
{
	/**
	 * Gets a sorted <code>List</code> of {@link Playable} objects.
	 * 
	 * @param app
	 * @return
	 */
	public List<? extends Playable> getMembers(Harmonium app);
	//public Playable[] getMemberArray(Harmonium app);
	
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