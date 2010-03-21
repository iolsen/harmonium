package org.dazeend.harmonium.music;

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
	 * Gets the duration of the playable object in milliseconds.
	 * If the duration is unknown, it returns -1;
	 * 
	 * @return	the duration of the playable object in milliseconds
	 */
	public long getDuration();

	public String toString();
	
	public String getContentType();
}
