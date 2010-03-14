package org.dazeend.harmonium.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;

public abstract class BaseArtist extends HMusic implements PlayableCollection {

	protected List<PlayableLocalTrack>	_trackList = Collections.synchronizedList( new ArrayList<PlayableLocalTrack>() );
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
	public List<PlayableLocalTrack> getTrackList() {
		return _trackList;
	}

	public abstract void removeTrack(PlayableLocalTrack track);
	public abstract boolean addTrack(FactoryPreferences prefs, PlayableLocalTrack newTrack);
	public abstract List<PlayableLocalTrack> getMembers(Harmonium app);

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
