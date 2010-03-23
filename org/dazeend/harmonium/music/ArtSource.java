package org.dazeend.harmonium.music;

import java.awt.Image;
import java.io.IOException;

import org.dazeend.harmonium.FactoryPreferences;

public interface ArtSource
{
	/**
	 * Gets whether music item has album art
	 * 
	 * @return
	 */
	public boolean hasAlbumArt(FactoryPreferences prefs);
	
	/**
	 * Gets an <code>Image</code> containing album art for this track.
	 * 
	 * @return	album art for this track
	 */
	public Image getAlbumArt(FactoryPreferences prefs);
	
	/**
	 * Gets an Image containing scaled album art for this track.
	 * 
	 * @param width		The maximum width of the scaled image
	 * @param height	The maximum height of the scaled image
	 * @return
	 * @throws IOException 
	 */
	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height);
	
	public String getArtHashKey();
}
