package org.dazeend.harmonium.music;

import java.awt.Image;

import javax.swing.ImageIcon;

import org.dazeend.harmonium.FactoryPreferences;

public class BasicArtSource implements ArtSource
{
	private final Image _img;
	private final String _hashKey;
	
	public BasicArtSource(Image img, String hashKey)
	{
		_img = img;
		_hashKey = hashKey;
	}
	public Image getAlbumArt(FactoryPreferences prefs)
	{
		return _img;
	}

	public String getArtHashKey()
	{
		return _hashKey;
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
		return (getAlbumArt(prefs) != null);
	}

}
