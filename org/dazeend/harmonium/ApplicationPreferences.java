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

import java.util.Comparator;

import org.dazeend.harmonium.music.Album;
import org.dazeend.harmonium.music.CompareAlbumsByName;
import org.dazeend.harmonium.music.CompareAlbumsByYear;
import org.dazeend.harmonium.music.CompareTracksByName;
import org.dazeend.harmonium.music.CompareTracksByNumber;
import org.dazeend.harmonium.music.Playable;

import com.tivo.hme.interfaces.IContext;

public class ApplicationPreferences {
	// application context
	IContext context;

	// persistant comparators
	private Comparator<Album> compareAlbumsByName;
	private Comparator<Album> compareAlbumsByYear;
	private Comparator<Playable> compareTracksByName;
	private Comparator<Playable> compareTracksByNumber;

	// static keys used for storing preferences
	private static final String MC_SHUFFLE = "musicCollectionShuffleMode";
	private static final String MC_REPEAT = "musicCollectionRepeatMode";
	private static final String MC_TRACK_SORT = "musicCollectionTrackSort";
	private static final String AA_SHUFFLE = "albumArtistShuffleMode";
	private static final String AA_REPEAT = "albumArtistRepeatMode";
	private static final String AA_TRACK_SORT = "albumArtistTrackSort";
	private static final String A_SHUFFLE = "albumShuffleMode";
	private static final String A_REPEAT = "albumRepeatMode";
	private static final String A_SORT = "albumSort";
	private static final String A_TRACK_SORT = "albumTrackSort";
	private static final String D_SHUFFLE = "discShuffleMode";
	private static final String D_REPEAT = "discRepeatMode";
	private static final String D_TRACK_SORT = "discTrackSort";
	private static final String T_REPEAT = "trackRepeatMode";
	private static final String PL_SHUFFLE = "playlistShuffleMode";
	private static final String PL_REPEAT = "playlistRepeatMode";
	private static final String SCREENSAVER = "screensaver";
	
	// public static values used stored in keys as preferences
	public static final String BOOLEAN_TRUE = "on";
	public static final String BOOLEAN_FALSE = "off";
	public static final String SORT_ALBUMS_BY_NAME = "byName";
	public static final String SORT_ALBUMS_BY_YEAR = "byYear";
	public static final String SORT_TRACKS_BY_NAME = "byName";
	public static final String SORT_TRACKS_BY_NUMBER = "byNumber";
	
	private static final int DEFAULT_SCREENSAVER_DELAY_MS = 300000; // 5 minutes
	
	// Music collection options
	private boolean 		musicCollectionDefaultShuffleMode;
	private boolean 		musicCollectionDefaultRepeatMode;	
	private Comparator<Playable> musicCollectionTrackSort;
	private String			musicCollectionTrackSortMode;

	
	// album artist options
	private boolean 		albumArtistDefaultShuffleMode;
	private boolean 		albumArtistDefaultRepeatMode;
	private Comparator<Playable> albumArtistTrackSort;
	private String			albumArtistTrackSortMode;
	
	// album options
	private boolean 		albumDefaultShuffleMode;
	private boolean 		albumDefaultRepeatMode;
	private Comparator<Album> albumSort;
	private String			albumSortMode;
	private Comparator<Playable> albumTrackSort;
	private String			albumTrackSortMode;
	
	// disc options
	private boolean 		discDefaultShuffleMode;
	private boolean 		discDefaultRepeatMode;
	private Comparator<Playable> discTrackSort;
	private String			discTrackSortMode;
	
	// track options
	// Note: it makes no sense to shuffle a single track
	private boolean			trackDefaultShuffleMode = false;
	private boolean			trackDefaultRepeatMode;
	
	// playlist options
	private boolean			playlistFileDefaultShuffleMode;
	private boolean			playlistFileDefaultRepeatMode;
	
	// application options
	private int			screenSaverDelay;
	
	
	public ApplicationPreferences(IContext context) {
		
		this.context = context;
		this.compareAlbumsByName = new CompareAlbumsByName();
		this.compareAlbumsByYear = new CompareAlbumsByYear();
		this.compareTracksByName = new CompareTracksByName();
		this.compareTracksByNumber = new CompareTracksByNumber();
		
		// temp variable used to store preferences. This
		//prevents double reading of file for each call to getPersistantData() 
		String value;
		
		// initialize preferences. All boolean options default to OFF.
		// TODO Check for NULL preferences before checking values
		value = context.getPersistentData(MC_SHUFFLE);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.musicCollectionDefaultShuffleMode = true;
		}
		else{
			this.musicCollectionDefaultShuffleMode = false;
		}
		
		value = context.getPersistentData(MC_REPEAT);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.musicCollectionDefaultRepeatMode = true;
		}
		else {
			this.musicCollectionDefaultRepeatMode = false;
		}
		
		// By default, sort the music collection's tracks by name
		value = context.getPersistentData(MC_TRACK_SORT);
		if(value != null && value.equals(SORT_TRACKS_BY_NUMBER)) {
			this.musicCollectionTrackSort = this.compareTracksByNumber;
			this.musicCollectionTrackSortMode = SORT_TRACKS_BY_NUMBER;
		}
		else {
			this.musicCollectionTrackSort = this.compareTracksByName;
			this.musicCollectionTrackSortMode = SORT_TRACKS_BY_NAME;
		}
		
		value = context.getPersistentData(AA_SHUFFLE);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.albumArtistDefaultShuffleMode = true;
		}
		else {
			this.albumArtistDefaultShuffleMode = false;
		}
		
		value = context.getPersistentData(AA_REPEAT);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.albumArtistDefaultRepeatMode = true;
		}
		else {
			this.albumArtistDefaultRepeatMode = false;
		}
		
		// By default, sort album artist's tracks by name
		value = context.getPersistentData(AA_TRACK_SORT);
		if(value != null && value.equals(SORT_TRACKS_BY_NUMBER)) {
			this.albumArtistTrackSort = this.compareTracksByNumber;
			this.albumArtistTrackSortMode = SORT_TRACKS_BY_NUMBER;
		}
		else {
			this.albumArtistTrackSort = this.compareTracksByName;
			this.albumArtistTrackSortMode = SORT_TRACKS_BY_NAME;
		}
		
		value = context.getPersistentData(A_SHUFFLE);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.albumDefaultShuffleMode = true;
		}
		else {
			this.albumDefaultRepeatMode = false;
		}
		
		value = context.getPersistentData(A_REPEAT);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.albumDefaultRepeatMode = true;
		}
		else {
			this.albumDefaultRepeatMode = false;
		}
		
		// By default, sort albums by name
		value = context.getPersistentData(A_SORT);
		if(value != null && value.equals(SORT_ALBUMS_BY_YEAR)) {
			this.albumSort = this.compareAlbumsByYear;
			this.albumSortMode = SORT_ALBUMS_BY_YEAR;
		}
		else {
			this.albumSort = this.compareAlbumsByName;
			this.albumSortMode = SORT_ALBUMS_BY_NAME;
		}
		
		// By default, sort album tracks by number
		value = context.getPersistentData(A_TRACK_SORT);
		if(value != null && value.equals(SORT_TRACKS_BY_NAME)) {
			this.albumTrackSort = this.compareTracksByName;
			this.albumTrackSortMode = SORT_TRACKS_BY_NAME;
		}
		else {
			this.albumTrackSort = this.compareTracksByNumber;
			this.albumTrackSortMode = SORT_TRACKS_BY_NUMBER;
		}
		
		value = context.getPersistentData(D_SHUFFLE);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.discDefaultShuffleMode = true;
		}
		else {
			this.discDefaultShuffleMode = false;
		}
		
		value = context.getPersistentData(D_REPEAT);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.discDefaultRepeatMode = true;
		}
		else {
			this.discDefaultRepeatMode = false;
		}
		
		// By default, sort disc's tracks by number
		value = context.getPersistentData(D_TRACK_SORT);
		if(value != null && value.equals(SORT_TRACKS_BY_NAME)) {
			this.discTrackSort = this.compareTracksByName;
			this.discTrackSortMode = SORT_TRACKS_BY_NAME;
		}
		else {
			this.discTrackSort = this.compareTracksByNumber;
			this.discTrackSortMode = SORT_TRACKS_BY_NUMBER;
		}
		
		value = context.getPersistentData(T_REPEAT);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.trackDefaultRepeatMode = true;
		}
		else {
			this.trackDefaultRepeatMode = false;
		}
		
		value = context.getPersistentData(PL_SHUFFLE);
		if(value != null && value.equals(BOOLEAN_TRUE) ) {
			this.playlistFileDefaultShuffleMode = true;
		}
		else {
			this.playlistFileDefaultShuffleMode = false;
		}
		
		value = context.getPersistentData(PL_REPEAT);
		if(value != null && value.equals(BOOLEAN_TRUE)) {
			this.playlistFileDefaultRepeatMode = true;
		}
		else {
			this.playlistFileDefaultRepeatMode = false;
		}
		
		// The default screensaver mode is ON (unlike other boolean values)
		value = context.getPersistentData(SCREENSAVER);
		if (value == null)
			this.screenSaverDelay = DEFAULT_SCREENSAVER_DELAY_MS;
		else if (value.equals(BOOLEAN_FALSE))
			this.screenSaverDelay = 0;
		else if (value.equals(BOOLEAN_TRUE))
			this.screenSaverDelay = DEFAULT_SCREENSAVER_DELAY_MS;
		else {
			try
			{
				this.screenSaverDelay = Integer.parseInt(value);
			} catch (NumberFormatException e)
			{
				this.screenSaverDelay = DEFAULT_SCREENSAVER_DELAY_MS;
			}
		}
	}

	/**
	 * @return the musicCollectionDefaultShuffleMode
	 */
	public boolean getMusicCollectionDefaultShuffleMode() {
		return musicCollectionDefaultShuffleMode;
	}

	/**
	 * @param musicCollectionDefaultShuffleMode the musicCollectionDefaultShuffleMode to set
	 */
	public void setMusicCollectionDefaultShuffleMode(boolean value) {
		this.musicCollectionDefaultShuffleMode = value;
		if(value) {
			this.context.setPersistentData(MC_SHUFFLE, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(MC_SHUFFLE, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the musicCollectionDefaultRepeatMode
	 */
	public boolean getMusicCollectionDefaultRepeatMode() {
		return musicCollectionDefaultRepeatMode;
	}

	/**
	 * @param musicCollectionDefaultRepeatMode the musicCollectionDefaultRepeatMode to set
	 */
	public void setMusicCollectionDefaultRepeatMode(boolean value) {
		this.musicCollectionDefaultRepeatMode = value;
		
		if(value) {
			this.context.setPersistentData(MC_REPEAT, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(MC_REPEAT, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the albumArtistDefaultShuffleMode
	 */
	public boolean getAlbumArtistDefaultShuffleMode() {
		return albumArtistDefaultShuffleMode;
	}

	/**
	 * @param albumArtistDefaultShuffleMode the albumArtistDefaultShuffleMode to set
	 */
	public void setAlbumArtistDefaultShuffleMode(boolean value) {
		this.albumArtistDefaultShuffleMode = value;
		if(value) {
			this.context.setPersistentData(AA_SHUFFLE, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(AA_SHUFFLE, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the albumArtistDefaultRepeatMode
	 */
	public boolean getAlbumArtistDefaultRepeatMode() {
		return albumArtistDefaultRepeatMode;
	}

	/**
	 * @param albumArtistDefaultRepeatMode the albumArtistDefaultRepeatMode to set
	 */
	public void setAlbumArtistDefaultRepeatMode(boolean value) {
		this.albumArtistDefaultRepeatMode = value;
		
		if(value) {
			this.context.setPersistentData(AA_REPEAT, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(AA_REPEAT, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the albumDefaultShuffleMode
	 */
	public boolean getAlbumDefaultShuffleMode() {
		return albumDefaultShuffleMode;
	}

	/**
	 * @param albumDefaultShuffleMode the albumDefaultShuffleMode to set
	 */
	public void setAlbumDefaultShuffleMode(boolean value) {
		this.albumDefaultShuffleMode = value;
		if(value) {
			this.context.setPersistentData(A_SHUFFLE, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(A_SHUFFLE, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the albumDefaultRepeatMode
	 */
	public boolean getAlbumDefaultRepeatMode() {
		return albumDefaultRepeatMode;
	}

	/**
	 * @param albumDefaultRepeatMode the albumDefaultRepeatMode to set
	 */
	public void setAlbumDefaultRepeatMode(boolean value) {
		this.albumDefaultRepeatMode = value;
		if(value) {
			this.context.setPersistentData(A_REPEAT, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(A_REPEAT, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the discDefaultShuffleMode
	 */
	public boolean getDiscDefaultShuffleMode() {
		return discDefaultShuffleMode;
	}

	/**
	 * @param discDefaultShuffleMode the discDefaultShuffleMode to set
	 */
	public void setDiscDefaultShuffleMode(boolean value) {
		this.discDefaultShuffleMode = value;
		if(value) {
			this.context.setPersistentData(D_SHUFFLE, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(D_SHUFFLE, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the discDefaultRepeatMode
	 */
	public boolean getDiscDefaultRepeatMode() {
		return discDefaultRepeatMode;
	}

	/**
	 * @param discDefaultRepeatMode the discDefaultRepeatMode to set
	 */
	public void setDiscDefaultRepeatMode(boolean value) {
		this.discDefaultRepeatMode = value;
		if(value) {
			this.context.setPersistentData(D_REPEAT, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(D_REPEAT, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the trackDefaultRepeatMode
	 */
	public boolean getTrackDefaultRepeatMode() {
		return trackDefaultRepeatMode;
	}

	/**
	 * @param trackDefaultRepeatMode the trackDefaultRepeatMode to set
	 */
	public void setTrackDefaultRepeatMode(boolean value) {
		this.trackDefaultRepeatMode = value;
		if(value) {
			this.context.setPersistentData(T_REPEAT, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(T_REPEAT, BOOLEAN_FALSE);
		}
	}
	
	/**
	 * @return the trackDefaultShuffleMode
	 */
	public boolean getTrackDefaultShuffleMode() {
		return this.trackDefaultShuffleMode;
	}

	/**
	 * @return the playlistFileDefaultShuffleMode
	 */
	public boolean getPlaylistFileDefaultShuffleMode() {
		return playlistFileDefaultShuffleMode;
	}

	/**
	 * @param playlistFileDefaultShuffleMode the playlistFileDefaultShuffleMode to set
	 */
	public void setPlaylistFileDefaultShuffleMode(boolean value) {
		this.playlistFileDefaultShuffleMode = value;
		if(value) {
			this.context.setPersistentData(PL_SHUFFLE, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(PL_SHUFFLE, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the playlistFileDefaultRepeatMode
	 */
	public boolean getPlaylistFileDefaultRepeatMode() {
		return playlistFileDefaultRepeatMode;
	}

	/**
	 * @param playlistFileDefaultRepeatMode the playlistFileDefaultRepeatMode to set
	 */
	public void setPlaylistFileDefaultRepeatMode(boolean value) {
		this.playlistFileDefaultRepeatMode = value;
		if(value) {
			this.context.setPersistentData(PL_REPEAT, BOOLEAN_TRUE);
		}
		else {
			this.context.setPersistentData(PL_REPEAT, BOOLEAN_FALSE);
		}
	}

	/**
	 * @return the albumArtistTrackSort
	 */
	public Comparator<Playable> getAlbumArtistTrackComparator() {
		return albumArtistTrackSort;
	}

	/**
	 * @param albumArtistTrackSort the albumArtistTrackSort to set
	 */
	public void setAlbumArtistTrackSort(String value) {
		if(value.equals(SORT_TRACKS_BY_NAME)) {
			this.albumArtistTrackSortMode = SORT_TRACKS_BY_NAME;
			this.albumArtistTrackSort = this.compareTracksByName;
			this.context.setPersistentData(AA_TRACK_SORT, SORT_TRACKS_BY_NAME);
		}
		else if(value.equals(SORT_TRACKS_BY_NUMBER)) {
			this.albumArtistTrackSortMode = SORT_TRACKS_BY_NUMBER;
			this.albumArtistTrackSort = this.compareTracksByNumber;
			this.context.setPersistentData(AA_TRACK_SORT, SORT_TRACKS_BY_NUMBER);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the albumSort
	 */
	public Comparator<Album> getAlbumComparator() {
		return albumSort;
	}

	/**
	 * @param albumSort the albumSort to set
	 */
	public void setAlbumSort(String value) {
		if(value.equals(SORT_ALBUMS_BY_NAME)) {
			this.albumSortMode = SORT_ALBUMS_BY_NAME;
			this.albumSort = this.compareAlbumsByName;
			this.context.setPersistentData(A_SORT, SORT_ALBUMS_BY_NAME);
		}
		else if(value.equals(SORT_ALBUMS_BY_YEAR)) {
			this.albumSortMode = SORT_ALBUMS_BY_YEAR;
			this.albumSort = this.compareAlbumsByYear;
			this.context.setPersistentData(A_SORT, SORT_ALBUMS_BY_YEAR);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the albumTrackSort
	 */
	public Comparator<Playable> getAlbumTrackComparator() {
		return albumTrackSort;
	}

	/**
	 * @param albumTrackSort the albumTrackSort to set
	 */
	public void setAlbumTrackSort(String value) {
		if(value.equals(SORT_TRACKS_BY_NAME)) {
			this.albumTrackSortMode = SORT_TRACKS_BY_NAME;
			this.albumTrackSort = this.compareTracksByName;
			this.context.setPersistentData(A_TRACK_SORT, SORT_TRACKS_BY_NAME);
		}
		else if(value.equals(SORT_TRACKS_BY_NUMBER)) {
			this.albumTrackSortMode = SORT_TRACKS_BY_NUMBER;
			this.albumTrackSort = this.compareTracksByNumber;
			this.context.setPersistentData(A_TRACK_SORT, SORT_TRACKS_BY_NUMBER);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the discTrackSort
	 */
	public Comparator<Playable> getDiscTrackComparator() {
		return discTrackSort;
	}

	/**
	 * @param discTrackSort the discTrackSort to set
	 */
	public void setDiscTrackSort(String value) {
		if(value.equals(SORT_TRACKS_BY_NAME)) {
			this.discTrackSortMode = SORT_TRACKS_BY_NAME;
			this.discTrackSort = this.compareTracksByName;
			this.context.setPersistentData(D_TRACK_SORT, SORT_TRACKS_BY_NAME);
		}
		else if(value.equals(SORT_TRACKS_BY_NUMBER)) {
			this.discTrackSortMode = SORT_TRACKS_BY_NUMBER;
			this.discTrackSort = this.compareTracksByNumber;
			this.context.setPersistentData(D_TRACK_SORT, SORT_TRACKS_BY_NUMBER);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the musicCollectionTrackSort
	 */
	public Comparator<Playable> getMusicCollectionTrackComparator() {
		return musicCollectionTrackSort;
	}

	/**
	 * @param musicCollectionTrackSort the musicCollectionTrackSort to set
	 */
	public void setMusicCollectionTrackSort(String value) {
		if(value.equals(SORT_TRACKS_BY_NAME)) {
			this.musicCollectionTrackSortMode = SORT_TRACKS_BY_NAME;
			this.musicCollectionTrackSort = this.compareTracksByName;
			this.context.setPersistentData(MC_TRACK_SORT, SORT_TRACKS_BY_NAME);
		}
		else if(value.equals(SORT_TRACKS_BY_NUMBER)) {
			this.musicCollectionTrackSortMode = SORT_TRACKS_BY_NUMBER;
			this.musicCollectionTrackSort = this.compareTracksByNumber;
			this.context.setPersistentData(MC_TRACK_SORT, SORT_TRACKS_BY_NUMBER);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the albumArtistTrackSortMode
	 */
	public String getAlbumArtistTrackSortMode() {
		return albumArtistTrackSortMode;
	}

	/**
	 * @return the albumSortMode
	 */
	public String getAlbumSortMode() {
		return albumSortMode;
	}

	/**
	 * @return the albumTrackSortMode
	 */
	public String getAlbumTrackSortMode() {
		return albumTrackSortMode;
	}

	/**
	 * @return the discTrackSortMode
	 */
	public String getDiscTrackSortMode() {
		return discTrackSortMode;
	}

	/**
	 * @return the musicCollectionTrackSortMode
	 */
	public String getMusicCollectionTrackSortMode() {
		return musicCollectionTrackSortMode;
	}

	/**
	 * @return the useScreenSaver
	 */
	public int screenSaverDelay() {
		return screenSaverDelay;
	}

	/**
	 * @param useScreenSaver the useScreenSaver to set
	 */
	public void setScreenSaverDelay(int value) {
		this.screenSaverDelay = value;
		this.context.setPersistentData(SCREENSAVER, String.valueOf(value));
	}	
}
