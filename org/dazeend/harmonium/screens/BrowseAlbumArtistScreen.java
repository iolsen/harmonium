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

package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Album;
import org.dazeend.harmonium.music.AlbumArtist;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlaylistEligible;

import com.tivo.hme.bananas.BView;

/**
 * The album artist screen to use when there are known albums.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 * 
 */
public class BrowseAlbumArtistScreen extends HAlbumInfoListScreen
{

	private List<Playable> tracksWithNoAlbum;

	public BrowseAlbumArtistScreen(Harmonium app, AlbumArtist thisAlbumArtist)
	{
		// The parent constructor needs an album to initialize album info. Sent
		// the first one.
		super(app, thisAlbumArtist.toString());

		// Set up list for contents of musicItem
		this.list = new HAlbumArtList(
				this.getNormal(), // Put list on "normal" level
				this.safeTitleH, // x coord. of list
				this.screenHeight - this.safeTitleV - (this.rowHeight * 5), // y
				// coord.
				// of
				// list
				(this.screenWidth - (2 * this.safeTitleH)), // width of list
				// (full screen)
				this.rowHeight * 5, // height of list (5/8 of body). Defined in
				// terms of row height to ensure that height
				// is an even multiple or rowheight.
				this.rowHeight, // row height
				this.albumArtView, this.albumArtBGView, this.albumNameText, this.albumNameBGText, this.artistNameText,
				this.albumArtistBGText, this.yearText, this.yearBGText);
		setFocusDefault(this.list);

		// Add albums to the screen
		List<Album> albums = new ArrayList<Album>();
		albums.addAll(thisAlbumArtist.getAlbumList());
		Collections.sort(albums, this.app.getPreferences().getAlbumComparator());

		this.list.add(albums.toArray());

		// If this album has any tracks that are direct members, add them to the
		// screen.
		tracksWithNoAlbum = new ArrayList<Playable>();
		tracksWithNoAlbum.addAll(thisAlbumArtist.getTrackList());
		Collections.sort(tracksWithNoAlbum, this.app.getPreferences().getAlbumArtistTrackComparator());
		this.list.add(tracksWithNoAlbum.toArray());
	}

	public boolean handleAction(BView view, Object action)
	{
		if (action.equals("right") || action.equals("select"))
		{
			PlaylistEligible musicItem = (PlaylistEligible) list.get(list.getFocus());

			if (musicItem instanceof Album)
			{
				this.app.push(new BrowseAlbumScreen(this.app, (Album) musicItem), TRANSITION_LEFT);
			} else
			{
				this.app.push(new TrackScreen(this.app, (Playable) musicItem), TRANSITION_LEFT);
			}

			return true;
		}

		return super.handleAction(view, action);
	}

	/*
	 * (non-Javadoc) Handles key presses from TiVo remote control.
	 */
	@Override
	public boolean handleKeyPress(int key, long rawcode)
	{

		this.app.checkKeyPressToResetInactivityTimer(key);

		switch (key)
		{
		case KEY_PLAY:

			List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
			playlist.add((PlaylistEligible) this.list.get(this.list.getFocus()));
			boolean shuffleMode;
			boolean repeatMode;

			if (this.list.get(this.list.getFocus()) instanceof Album)
			{
				// Playing an entire album
				shuffleMode = this.app.getPreferences().getAlbumDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getAlbumDefaultRepeatMode();
			} else
			{
				// Playing an individual track
				shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
				repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
			}
			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode);
			return true;
		}

		return super.handleKeyPress(key, rawcode);

	}
}
