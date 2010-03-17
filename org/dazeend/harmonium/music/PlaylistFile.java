package org.dazeend.harmonium.music;

import java.io.File;
import java.util.List;

import org.dazeend.harmonium.Harmonium;

public interface PlaylistFile extends PlayableCollection, Comparable<PlaylistFile> {

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
	List<PlayableCollection> getMembers();
	
	/**
	 * Returns the description for this Playlist.
	 * @return
	 */
	String getDescription();
}
