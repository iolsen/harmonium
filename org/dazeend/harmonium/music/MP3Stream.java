package org.dazeend.harmonium.music;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.screens.NowPlayingScreen;

public class MP3Stream extends HMusic implements Playable
{
	private String _uri;
	
	public MP3Stream(String uri)
	{
		_uri = uri;
	}

	public long getDuration()
	{
		return -1;
	}

	public String getURI()
	{
		return _uri;
	}

	public boolean pause(NowPlayingScreen nowPlayingScreen)
	{
		return false;
	}

	public boolean play(NowPlayingScreen nowPlayingScreen)
	{
		return nowPlayingScreen.play(this); 
	}

	public boolean stop(NowPlayingScreen nowPlayingScreen)
	{
		nowPlayingScreen.stopPlayback();
		return true;
	}

	public boolean unpause(NowPlayingScreen nowPlayingScreen)
	{
		return false;
	}

	public List<Playable> getMembers(Harmonium app)
	{
		List<Playable> members = new ArrayList<Playable>(1);
		members.add(this);
		return members;
	}

	public String toStringTitleSortForm()
	{
		return _uri;
	}

	public Image getAlbumArt(FactoryPreferences prefs)
	{
		return null;
	}

	public String getArtHashKey()
	{
		return getURI();
	}

	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height)
	{
		return null;
	}

	public boolean hasAlbumArt(FactoryPreferences prefs)
	{
		return false;
	}

	public String getContentType()
	{
		return "audio/mpeg";
	}

	public String getDisplayArtistName()
	{
		return "";
	}

	public String getAlbumArtistName()
	{
		return "";
	}

	public String getAlbumName()
	{
		return "";
	}

	public int getReleaseYear()
	{
		return 0;
	}
}
