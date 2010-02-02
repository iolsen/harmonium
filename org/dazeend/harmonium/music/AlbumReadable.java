package org.dazeend.harmonium.music;

import java.awt.Image;

import org.dazeend.harmonium.FactoryPreferences;

/**
 * Defines the interface used to read album information from items in the
 * music collection.
 */
public interface AlbumReadable {
	
	public boolean hasAlbumArt(FactoryPreferences prefs);
	
	public Image getAlbumArt(FactoryPreferences prefs);
	
	public Image getScaledAlbumArt(FactoryPreferences prefs, int width, int height);
	
	public String getAlbumArtistName();
	
	public String getAlbumName();
	
	public int getReleaseYear();
	
	public String toString();
}
