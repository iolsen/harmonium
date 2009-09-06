/*
 * Copyright 2009 Ian Olsen
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import org.dazeend.harmonium.Harmonium;

public abstract class BaseArtist extends HMusic implements PlaylistEligible {

	protected List<Playable>	_trackList = Collections.synchronizedList( new ArrayList<Playable>() );
	protected String			_artistName = "";	// Set only through constructor. Setting later could break data structure.
	protected String			_albumArtistNameTitleSortForm = "";

	protected BaseArtist(String artistName) {

		_artistName = artistName;

		// Put artist name in title sort form
		// Compile pattern for matching leading articles
		Matcher stringMatcher = titlePattern.matcher(this._artistName);
		if(stringMatcher.lookingAt()) {
			// Found a leading article. Move it to the end of the string.
			this._albumArtistNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
		}
		else {
			this._albumArtistNameTitleSortForm = this._artistName;
		}
	}

	/**
	 * Gets the name of this artist.
	 * 
	 * @return the albumArtistName
	 */
	public String getArtistName() {
		return _artistName;
	}

	/**
	 * Gets the list of direct member tracks associated with this artist.
	 * 
	 * @return the trackList
	 */
	public List<Playable> getTrackList() {
		return _trackList;
	}

	public abstract void removeTrack(Playable track);
	public abstract boolean addTrack(Playable newTrack);
	public abstract List<Playable> listMemberTracks(Harmonium app);

	public String toStringTitleSortForm()
	{
		return this._albumArtistNameTitleSortForm;
	}

	@Override
	public String toString()
	{
		if(this._artistName == null) {
			return "";
		}
		return this._artistName;
	}

	/**
	 * @return the albumArtistNameTitleSortForm
	 */
	public String getAlbumArtistNameTitleSortForm()
	{
		if(this._albumArtistNameTitleSortForm == null) {
			return "";
		}
		return this._albumArtistNameTitleSortForm;
	}
}
