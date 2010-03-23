package org.dazeend.harmonium;

import java.io.File;

import com.tivo.hme.interfaces.IArgumentList;

public class FactoryPreferences {

	private String 			musicRoot;
	private String			playlistRoot;
	private boolean			debugMode = false;
	private boolean			ignoreEmbeddedArt = false;
	private boolean			ignoreJpgFileArt = false;
	private boolean			preferJpgFileArt = false;
	private boolean			includeHTTP = false;

	public FactoryPreferences(IArgumentList args) {
		
		// Check and set debug mode
		if(args.getBoolean("-debug")) {
			this.debugMode = true;
		}
		
		// Set music root
		this.musicRoot = args.getValue("-musicRoot");

		// validate musicRoot
		if(this.musicRoot == null || this.musicRoot.equals("")) {
			// no value given for music root
			throw new RuntimeException("musicRoot: No value provided");
		}
		else {
			// check that music root is a readable directory
			File musicDir = new File(musicRoot);
			if( ! musicDir.exists() ) {
				throw new RuntimeException("musicRoot: " + musicRoot + " does not exist");
			}
			else if(! musicDir.isDirectory()) {
				throw new RuntimeException("musicRoot: " + musicRoot + "is not a directory");
			}
			else if(! musicDir.canRead()) {
				throw new RuntimeException("musicRoot: " + musicRoot + "cannot read directory");
			}
		}

		// Set playlist root
		this.playlistRoot = args.getValue("-playlistRoot");

		// validate playlistRoot
		if(this.playlistRoot == null || this.playlistRoot.equals("")) {
			// no value given for music root
			throw new RuntimeException("playlistRoot: No value provided");
		}
		else {
			// check that playlist root is a readable/writable directory
			File playlistDir = new File(playlistRoot);
			if( ! playlistDir.exists() ) {
				throw new RuntimeException("playlistRoot: " + playlistRoot + " does not exist");
			}
			else if(! playlistDir.isDirectory()) {
				throw new RuntimeException("playlistRoot: " + playlistRoot + "is not a directory");
			}
			else if(! playlistDir.canRead()) {
				throw new RuntimeException("playlistRoot: " + playlistRoot + "cannot read directory");
			}
			else if(! playlistDir.canWrite()) {
				throw new RuntimeException("playlistRoot: " + playlistRoot + "not writable");
			}
		}

		this.ignoreEmbeddedArt = args.getBoolean("-ignoreEmbeddedArt");
		this.ignoreJpgFileArt = args.getBoolean("-ignoreJpgFileArt");
		this.preferJpgFileArt = args.getBoolean("-preferJpgFileArt");
		this.includeHTTP = args.getBoolean("-includeHTTPAlbumArt");
	}
		
	/**
	 * @return the musicRoot
	 */
	public String getMusicRoot() {
		return musicRoot;
	}
	
	/**
	 * @return the playlistRoot
	 */
	public String getPlaylistRoot() {
		return this.playlistRoot;
	}
	
	/**
	 * @return the debugMode
	 */
	public boolean inDebugMode() {
		return this.debugMode;
	}

	public final boolean ignoreEmbeddedArt()
	{
		return ignoreEmbeddedArt;
	}

	public final boolean ignoreJpgFileArt()
	{
		return ignoreJpgFileArt;
	}

	public final boolean preferJpgFileArt()
	{
		return preferJpgFileArt;
	}

	public final boolean includeHTTP()
	{
		return includeHTTP;
	}

	public final void setIncludeHTTP(boolean newValue)
	{
		includeHTTP = newValue;
	}
	
	
	
}
