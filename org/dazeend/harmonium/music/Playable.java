package org.dazeend.harmonium.music;

import org.dazeend.harmonium.screens.NowPlayingScreen;

public interface Playable extends PlayableCollection, AlbumArtListItem
{
	/**
	 * Enumerates image types that TiVo can natively display.
	 */
	public enum TivoImageFormat {
		GIF		("image/gif"),
		JPEG	("image/jpeg"),
		JPG		("image/jpg"),
		PNG		("image/png");
		
		private final String mimeType;
		
		TivoImageFormat(String mimeType) {
			this.mimeType = mimeType;
		}
		
		public String getMimeType() {
			return this.mimeType;
		}
	}

	/**
	 * Gets the playable object's URI
	 */
	public String getURI();

	/**
	 * Initiates play of this playable object.
	 * 
	 * @param nowPlayingScreen
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public boolean play(NowPlayingScreen nowPlayingScreen);

	/**
	 * Permanently stops play of this playable object.
	 */
	public boolean stop(NowPlayingScreen nowPlayingScreen);

	/**
	 * Pauses playback of this track.
	 * 
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public boolean pause(NowPlayingScreen nowPlayingScreen);
	
	/**
	 * Resumes playback of this track.
	 * 
	 * @return	<code>true</code> if successful, otherwise </code>false</code>
	 */
	public boolean unpause(NowPlayingScreen nowPlayingScreen);
	
	/**
	 * Gets the duration of the playable object in milliseconds.
	 * If the duration is unknown, it returns -1;
	 * 
	 * @return	the duration of the playable object in milliseconds
	 */
	public long getDuration();

	public String toString();
	
	public String getContentType();
}
