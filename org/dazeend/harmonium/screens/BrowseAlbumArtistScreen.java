package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.Album;
import org.dazeend.harmonium.music.AlbumArtist;
import org.dazeend.harmonium.music.PlayableLocalTrack;
import org.dazeend.harmonium.music.PlayableCollection;

import com.tivo.hme.bananas.BView;

/**
 * The album artist screen to use when there are known albums.
 */
public class BrowseAlbumArtistScreen extends HAlbumInfoListScreen
{

	private List<PlayableLocalTrack> tracksWithNoAlbum;

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
		tracksWithNoAlbum = new ArrayList<PlayableLocalTrack>();
		tracksWithNoAlbum.addAll(thisAlbumArtist.getTrackList());
		Collections.sort(tracksWithNoAlbum, this.app.getPreferences().getAlbumArtistTrackComparator());
		this.list.add(tracksWithNoAlbum.toArray());
	}

	public boolean handleAction(BView view, Object action)
	{
		if (action.equals("right") || action.equals("select"))
		{
			PlayableCollection musicItem = (PlayableCollection) list.get(list.getFocus());

			if (musicItem instanceof Album)
			{
				this.app.push(new BrowseAlbumScreen(this.app, (Album) musicItem), TRANSITION_LEFT);
			} else
			{
				this.app.push(new TrackScreen(this.app, (PlayableLocalTrack) musicItem), TRANSITION_LEFT);
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

			List<PlayableCollection> playlist = new ArrayList<PlayableCollection>();
			playlist.add((PlayableCollection) this.list.get(this.list.getFocus()));
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
