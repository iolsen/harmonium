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
		
		if (artistName != null && albumName != null)
		{
			Album albumInfo = Album.getInfo(artistName, albumName, API_KEY);
			if (albumInfo != null) 
			{
				String imageURL = albumInfo.getImageURL(ImageSize.valueOf("EXTRALARGE"));

				//lets prevent some MalformedURLExceptions by making sure we actually have something in our URL
				if(imageURL != null && imageURL.length() > 0) {
					try {
						URL url = new URL(imageURL);
						img = Toolkit.getDefaultToolkit().createImage(url);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return img;
	}
	
	public static Track fetchTrackInfo(String artistName, String trackName)
	{
		return Track.getInfo(artistName, trackName, API_KEY);
	}
	
	public static Image fetchArtForTrack(Track track)
	{
		Image img = null;
		
		String imageURL = track.getImageURL(ImageSize.valueOf("EXTRALARGE"));

		//lets prevent some MalformedURLExceptions by making sure we actually have something in our URL
		if(imageURL != null && imageURL.length() > 0) {
			try {
				URL url = new URL(imageURL);
				img = Toolkit.getDefaultToolkit().createImage(url);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return img;
	}
}
