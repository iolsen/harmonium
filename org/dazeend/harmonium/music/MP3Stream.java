package org.dazeend.harmonium.music;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import net.roarsoftware.lastfm.Track;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.LastFm;

public class MP3Stream extends HMusic implements PlayableTrack
{
	private String _uri;
	private UrlArtSource _artSource;
	private String _tagParsedStreamTitle;
	
	private String _tagParsedArtist;
	private String _tagParsedTrackName;
	private String _fetchedAlbumName;

	private static final Pattern _tagParseTitlePattern = Pattern.compile("(.+)\\b\\s*-\\s*(.+)\\b");
	private static final Pattern _tagParseTitleWithParensPattern = Pattern.compile("(.+)\\b\\s*-\\s*(.*)(?:\\s+\\(.*\\))");
	
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
	
	public String getTagParsedStreamTitle()
	{
		return _tagParsedStreamTitle;
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
		if (_artSource == null)
			return null;
		
		return _artSource.getAlbumArt(prefs);
	}

	public String getArtHashKey()
	{
		if (_artSource == null)
			return getURI();
		
		return _artSource.getArtHashKey();
	}

	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height)
	{
		if (_artSource == null)
			return null;
		
		return _artSource.getScaledAlbumArt(prefs, width, height);
	}

	public boolean hasAlbumArt(FactoryPreferences prefs)
	{
		if (_artSource == null)
			return false;
		
		return _artSource.hasAlbumArt(prefs);
	}

	public String getContentType()
	{
		return "audio/mpeg";
	}

	public String getDisplayArtistName()
	{
		return getArtistName();
	}

	public String getAlbumArtistName()
	{
		return getArtistName();
	}

	public String getAlbumName()
	{
		if (_fetchedAlbumName != null)
			return _fetchedAlbumName;
		return "";
	}

	public int getReleaseYear()
	{
		return 0;
	}
	
	public void setArtUrl(final String url)
	{
		_artSource = new UrlArtSource(url);
	}

	public void setTagParsedStreamTitle(String streamTitle)
	{
		_tagParsedStreamTitle = streamTitle;
	}

	private class UrlArtSource implements ArtSource
	{
		private final String _url;
		private Image _img;
		private boolean _fetchAttempted;
		private String _artHashKey;
		
		public UrlArtSource(String url)
		{
			_url = url;
			_artHashKey = url;
		}
		
		private Track parseArtistAndTrack(FactoryPreferences prefs, Matcher m)
		{
			if (_tagParsedStreamTitle == null || _tagParsedStreamTitle.isEmpty())
				return null;
			
			if (m.lookingAt())
			{
				if (prefs.inDebugMode())
				{
					System.out.println("Parsed Artist: [" + m.group(1) + "]");
					System.out.println(" Parsed Track: [" + m.group(2) + "]");
				}
				Track track = LastFm.fetchTrackInfo(m.group(1), m.group(2));
				
				if (track != null)
				{
					_tagParsedArtist = m.group(1);
					_tagParsedTrackName = m.group(2);

					_fetchedAlbumName = track.getAlbum();

					_img = LastFm.fetchArtForTrack(track);
					
					try
					{
						java.awt.MediaTracker mt = new java.awt.MediaTracker(new java.awt.Canvas());
				    	mt.addImage(_img, 0);
			    		mt.waitForAll(2000);
					}
					catch (Exception e)
					{
						_img = null;
					}
					return track;
				}
			}
			return null;
		}
		
		public synchronized Image getAlbumArt(FactoryPreferences prefs)
		{
			if (_fetchAttempted)
				return _img;
			
			_fetchAttempted = true;
			
		    try
			{
		    	String lowerUrl = _url.toLowerCase();
				if (lowerUrl.startsWith("http://"))
				{
			    	_img = Toolkit.getDefaultToolkit().getImage(new URL(_url));
			    	
			    	java.awt.MediaTracker mt = new java.awt.MediaTracker(new java.awt.Canvas());
			    	mt.addImage(_img, 0);
		    		mt.waitForAll(2000);

		    		if (_img.getWidth(null) < 1 || _img.getHeight(null) < 1)
		    		{
				    	if (prefs.inDebugMode())
				    		System.out.println("Failed to fetch art for mp3 stream from " + _url);
				    	_img = null;
		    		}
		    		else
		    		{
				    	if (prefs.inDebugMode())
				    		System.out.println("Successfully fetched art for mp3 stream from " + _url);
		    		}
				}
			} 
			catch (Exception e)
			{
				_img = null;
		    	if (prefs.inDebugMode())
		    	{
		    		System.out.println("Failed to fetch art for mp3 stream from " + _url);
					e.printStackTrace();
		    	}
			}
			
			// If the stream itself failed to provide art, try to get it ourselves from last.fm
			try
			{
				if (_img == null && _tagParsedStreamTitle != null) // TODO add http-art preference check here
				{
					_artHashKey = _tagParsedStreamTitle;

					Matcher m = _tagParseTitleWithParensPattern.matcher(_tagParsedStreamTitle);
					Track track = parseArtistAndTrack(prefs, m);
					if (track == null)
					{
						m = _tagParseTitlePattern.matcher(_tagParsedStreamTitle);
						track = parseArtistAndTrack(prefs, m);
					}

					if (prefs.inDebugMode())
					{
						if (track == null)
							System.out.println("Failed to retrieve last.fm info for stream: " + _tagParsedStreamTitle);
						else
						{
							System.out.println("Successfully retrieved last.fm info for stream: " + _tagParsedStreamTitle);
						}
					}
				}
			}
			catch (Exception e)
			{
				_img = null;
		    	if (prefs.inDebugMode())
		    	{
		    		System.out.println("Failed to retrieve last.fm album art for stream: " + _tagParsedStreamTitle);
					e.printStackTrace();
		    	}
			}
			
	    	return _img;
		}

		public String getArtHashKey()
		{
			return _artHashKey;
		}

		public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height)
		{
			Image img = this.getAlbumArt(prefs);
			
			if (img != null) 
			{
		        ImageIcon icon = new ImageIcon(img);
		        int imgW = icon.getIconWidth();
		        int imgH = icon.getIconHeight();
		        
		        // figure out the scale factor and the new size
		        float scale = Math.min((float) width / imgW, (float) height / imgH);
		        if (scale > 1.0f) {
		            scale = 1.0f;
		        }
		        int scaleW = (int)(imgW * scale);
		        int scaleH = (int)(imgH * scale);
		        
		        // Perform scaling if the image must be shrunk. We will send smaller
		        // images over and let the receiver scale them up.
		        
		        if (scale < 1.0f) {
		            img = img.getScaledInstance(scaleW, scaleH, Image.SCALE_FAST);
		        }
			}
	        return img;
		}

		public boolean hasAlbumArt(FactoryPreferences prefs)
		{
			if (getAlbumArt(prefs) != null)
				return true;
			
			return false;
		}
	}

	public String getArtistName()
	{
		if (_tagParsedArtist != null)
			return _tagParsedArtist;
		
		return "";
	}

	public int getDiscNumber()
	{
		return 0;
	}

	public String getTrackName()
	{
		if (_tagParsedTrackName != null)
			return _tagParsedTrackName;
		if (_tagParsedStreamTitle != null)
			return _tagParsedStreamTitle;
		return _uri;
	}

	public String getTrackNameTitleSortForm()
	{
		return _uri;
	}

	public int getTrackNumber()
	{
		return 0;
	}
}
