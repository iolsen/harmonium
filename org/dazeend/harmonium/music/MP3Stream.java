package org.dazeend.harmonium.music;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;

public class MP3Stream extends HMusic implements Playable
{
	private String _uri;
	private UrlArtSource _artSource;
	
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
	
	public void setArtUrl(final String url)
	{
		_artSource = new UrlArtSource(url);
	}

	private class UrlArtSource implements ArtSource
	{
		private final String _url;
		private Image _img;
		private boolean _fetchAttempted;
		
		public UrlArtSource(String url)
		{
			_url = url;
		}
		
		public synchronized Image getAlbumArt(FactoryPreferences prefs)
		{
			if (_fetchAttempted)
				return _img;
			
			_fetchAttempted = true;
			
		    try
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
			catch (Exception e)
			{
				_img = null;
		    	if (prefs.inDebugMode())
		    	{
		    		System.out.println("Failed to fetch art for mp3 stream from " + _url);
					e.printStackTrace();
		    	}
			}
	    	return _img;
		}

		public String getArtHashKey()
		{
			return _url;
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
}
