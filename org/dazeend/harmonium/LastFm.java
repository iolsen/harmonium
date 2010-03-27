package org.dazeend.harmonium;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.Track;

public class LastFm
{
	private final static String API_KEY = "7984437bf046cc74c368f02bf9de16de";

	public static Image fetchAlbumArt(String artistName, String albumName)
	{
		Image img = null;
		Album albumInfo = Album.getInfo(artistName, albumName, API_KEY);
		if (albumInfo != null) 
		{
			String ImageURL = albumInfo.getImageURL(ImageSize.valueOf("EXTRALARGE"));

			//lets prevent some MalformedURLExceptions by making sure we actually have something in our URL
			if(ImageURL != null && ImageURL.length() > 0) {
				try {
					URL url = new URL(ImageURL);
					img = Toolkit.getDefaultToolkit().createImage(url);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return img;
	}
	
	public static Image fetchAlbumArtForTrack(String artistName, String trackName)
	{
		Image img = null;

		Track trackInfo = Track.getInfo(artistName, trackName, API_KEY);
		if (trackInfo != null)
			img = fetchAlbumArt(artistName, trackInfo.getName());
		
		return img;
	}
}
