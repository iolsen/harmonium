package org.dazeend.harmonium.music;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.Harmonium.HarmoniumFactory;

import org.blinkenlights.jid3.ID3Exception;


/**
 * Creates the root of the data structure that represents a collection of music.
 */
public class MusicCollection implements PlaylistEligible {
	
	// Static variables
	private static MusicCollection INSTANCE;		// This is the only instance of MusicCollection that should exist.
	private static String HARMONIUM_CACHE_VERSION = "# Harmonium Cache File v1.0 #";
	private static String HARMONIUM_CACHE_FILE_NAME = ".HarmoniumCache";
	
	// Instance variables
	private List<AlbumArtist>	albumArtistList = new ArrayList<AlbumArtist>();
	private List<Playable>		albumlessTrackList = new ArrayList<Playable>();
	private List<TrackArtist>	trackArtistList = new ArrayList<TrackArtist>();
	private String				musicRoot = "";
	private String				playlistRoot = "";
	private Map<String, Playable>	trackMap = new HashMap<String, Playable>(512);	// used to map canonical file paths to Playable objects. Used for playlist lookup
	private List<PlaylistFile>	playlistFiles = new ArrayList<PlaylistFile>();
	private HarmoniumFactory	hFactory;
	private List<File>			m3uCache = new ArrayList<File>();
	private boolean				refreshing = false;
	private long				cacheDate = 0;
	
	/**
	 * Private constructor. Only one instance of MusicCollection should ever exist.
	 */
	private MusicCollection(HarmoniumFactory hFactory) {
		super();
		
		this.hFactory = hFactory;
		
		// Get music source from preferences
		this.musicRoot = hFactory.getPreferences().getMusicRoot();
		this.playlistRoot = hFactory.getPreferences().getPlaylistRoot();
	}

	/**
	 * Gets instance of MusicCollection.
	 * 
	 *  @returns	the instance of MusicCollection for this application
	 */
	public static MusicCollection getMusicCollection(HarmoniumFactory hFactory) {
		if(INSTANCE == null) {
			INSTANCE = new MusicCollection(hFactory);
			INSTANCE.build();			// This MUST be here and not in the constructor. Otherwise leads to nasty recursion.
		}
		return INSTANCE;
	}

	/**
	 * Adds an album artist as a member of the <code>MusicCollection</code>. Checks to ensure that the album artist
	 * is not already a member, and if it is not adds it to the music collection.
	 * 
	 * @param newAlbumArtist	the album artist to add to the music collection
	 * @return					<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	private synchronized boolean addAlbumArtist(AlbumArtist newAlbumArtist) {
		
		// Check to ensure that the newAlbumArtist is not already a member of the music collection.
		if(this.albumArtistList.contains(newAlbumArtist)) {
			return false;
		}
		
		// If we got this far, then the newAlbumArtist is not yet a member of the music collection, so add it.
		if(this.albumArtistList.add(newAlbumArtist)) {
			// The album artist was successfully added. Return TRUE.
			return true;
		}
		else {
			// The album artist was not added, so return FALSE.
			return false;
		}
	}
	
	private synchronized boolean addTrackArtist(TrackArtist newTrackArtist) {
		
		// Check to ensure that the newTrackArtist is not already a member of the music collection.
		if(this.trackArtistList.contains(newTrackArtist)) {
			return false;
		}
		
		// If we got this far, then the newTrackArtist is not yet a member of the music collection, so add it.
		if(this.trackArtistList.add(newTrackArtist)) {
			// The album artist was successfully added. Return TRUE.
			return true;
		}
		else {
			// The album artist was not added, so return FALSE.
			return false;
		}
	}
	

	/**
	 * Removes a track from the MusicCollection. Deletes track and any newly-empty objects it was a member of.
	 * 
	 * @param Track
	 */
	private synchronized void removeTrack(Playable track) {
		// See if track belongs to an album artist.
		String trackAlbumArtist = track.getAlbumArtistName();
		if(! trackAlbumArtist.equals("") ) {
			
			// track belongs to an album artist. See if the album artist is a member of this music collection.
			Iterator<AlbumArtist> albumArtistIterator = this.albumArtistList.iterator();
			while( albumArtistIterator.hasNext() ) {
				AlbumArtist albumArtist = albumArtistIterator.next();
				
				if( albumArtist.getArtistName().compareToIgnoreCase(trackAlbumArtist) == 0 ) {
					// The album artist is a member of this music collection, so delete track from the album artist.
					albumArtist.removeTrack(track);
					
					// Check if the album artist is empty
					if( albumArtist.getAlbumList().isEmpty() && albumArtist.getTrackList().isEmpty() ) {
						// It's empty, so delete the album artist via its iterator
						albumArtistIterator.remove();
					}
						
				}
			}
		}
		else {
			// The track is not part of an album artist, so just delete it from the list
			this.albumlessTrackList.remove(track);
		}

		// See if track belongs to a track artist.
		String trackArtistName = track.getArtistName();
		if(! trackArtistName.equals("") ) {
			
			// track belongs to a track artist. See if the track artist is a member of this music collection.
			Iterator<TrackArtist> trackArtistIterator = this.trackArtistList.iterator();
			while( trackArtistIterator.hasNext() ) {
				TrackArtist trackArtist = trackArtistIterator.next();
				
				if( trackArtist.getArtistName().compareToIgnoreCase(trackArtistName) == 0 ) {
					// The track artist is a member of this music collection, so delete track from the album artist.
					trackArtist.removeTrack(track);
					
					// Check if the track artist is empty
					if( trackArtist.getTrackList().isEmpty() ) {
						// It's empty, so delete the album artist via its iterator
						trackArtistIterator.remove();
					}
						
				}
			}
		}
	}
	
	/**
	 * Adds a track as a member of the <code>MusicCollection</code>. Checks to ensure that the track is not already
	 * a member, and if it is not adds it to the album.
	 * 
	 * @param newTrack		the track to add to the music collection
	 * @return				<code>true</code> if the file was successfully added, otherwise <code>false</code>
	 */
	private synchronized boolean addTrack(FactoryPreferences prefs, Playable newTrack) {
		
		// See if newTrack belongs to a track artist.
		Boolean addedToTrackArtist = false;
		String newTrackArtistName = newTrack.getArtistName();
		if(! newTrackArtistName.equals("") ) {

			// newTrack belongs to a track artist. See if the track artist is already a member of this music collection.
			for( BaseArtist artist : this.trackArtistList) {

				if( artist.getArtistName().compareToIgnoreCase(newTrackArtistName) == 0 ) {
					// The track artist is already a member of this music collection, so add newTrack to the album artist.
					artist.addTrack(prefs, newTrack);
					addedToTrackArtist = true;
					break;
				}
			}
			
			if (!addedToTrackArtist) {
				// The track artist is not yet a member of this music collection.
				
				// Create a new track artist to hold the newTrack
				TrackArtist newTrackArtist = new TrackArtist(newTrackArtistName);
						
				// Add the newTrack to the newTrackArtist
				if( newTrackArtist.addTrack(prefs, newTrack) ) {
					// the track was added to the artist, so add the artist to this music collection
					this.addTrackArtist(newTrackArtist);
				}
			}
		}
		
		// See if newTrack belongs to an album artist.
		String newTrackAlbumArtist = newTrack.getAlbumArtistName();
		if(! newTrackAlbumArtist.equals("") ) {

			// newTrack belongs to an album artist. See if the album artist is already a member of this music collection.
			for( BaseArtist albumArtist : this.albumArtistList) {

				if( albumArtist.getArtistName().compareToIgnoreCase(newTrackAlbumArtist) == 0 ) {
					// The album artist is already a member of this music collection, so add newTrack to the album artist.
					if(albumArtist.addTrack(prefs, newTrack)) {
						// The track was successfully added. Return TRUE.
						return true;
					}
					else {
						// There was an error in adding the track.
						System.out.printf("Failed to add track %1$s to library under album artist: %2$s\r\n", newTrack, albumArtist);
						return false;
					}
						
				}
			}
			
			// The album artist is not yet a member of this music collection.
					
			// Create a new album artist to hold the newTrack
			AlbumArtist newAlbumArtist = new AlbumArtist( newTrack.getAlbumArtistName() );
					
			// Add the newTrack to the newAlbumArtist
			if( newAlbumArtist.addTrack(prefs, newTrack) ) {
				// the track was added to the album, so add the album artist to this music collection
				if( this.addAlbumArtist(newAlbumArtist) ) {
					
					// the album artist was successfully added
					return true;
				}
				else {
					// There was an error in adding the new album artist to the music collection
					System.out.printf("Failed to add album artist to library: %1$s\r\n", newAlbumArtist);
					return false;
				}
			}
			else {
				// There was an error in adding the track to the new album artist
				System.out.printf("Failed to add track %1$s to album artist %2$s\r\n", newTrack, newAlbumArtist);
				return false;
			}
		}
		else {
			// newTrack does not belong to an album artist.
			// Check to ensure that the newTrack is not already a direct member of this music collection.
			if(this.albumlessTrackList.contains(newTrack)) {
				return false;
			}
	
			// If we got this far, then the track is not yet in this music collection as a direct member, so add it.
			if(this.albumlessTrackList.add(newTrack)) {
				// The track was successfully added. Return TRUE.
				return true;
			}
			else {
				// there was an error in adding the track as a direct member of this music collection
				System.out.printf("Failed to add track %1$s to library without album artist.\r\n", newTrack);
				return false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public List<Playable> listMemberTracks(Harmonium app) {
		
		List<Playable> outputList = new ArrayList<Playable>();
 		
 		// Get tracks from each member albumArtist and add them to the output list
		List<AlbumArtist> sortedAlbumArtistList = this.albumArtistList;
		Collections.sort( sortedAlbumArtistList, new CompareArtists() );
		for(BaseArtist albumArtist : sortedAlbumArtistList) {
 			outputList.addAll(albumArtist.listMemberTracks(app));
 		}
		
 		// Get tracks that are direct members of the music root
 		List<Playable> sortedTrackList = new ArrayList<Playable>();
		sortedTrackList.addAll(albumlessTrackList);
		
		if(app != null) {
			Collections.sort( sortedTrackList, app.getPreferences().getMusicCollectionTrackComparator() );
		}
		outputList.addAll(sortedTrackList);
		
		return outputList;
	}
	
	/**
	 * Creates a new cache file with the tracks in this music collection
	 */
	private synchronized void writeCache() {
		File cacheFile = new File(this.musicRoot + File.separator + HARMONIUM_CACHE_FILE_NAME);
		File tempFile;
		try {
			// Create a temp file
			tempFile = File.createTempFile("harmonium", ".txt");
			tempFile.deleteOnExit();
		}
		catch(IOException e) {
			// Couldn't create the temp file. Bail out.
			return;
		}
		
		// Get a list of all the tracks in the music collection
		List<Playable> musicCollection = this.listMemberTracks(null);
		
		try{
			// Open the temp file for writing. (We'll write the temp file, then move it at the end.)
			FileWriter fout = new FileWriter(cacheFile);
			BufferedWriter cacheWriter = new BufferedWriter(fout);
			
			// Write the cache file header
			cacheWriter.write(HARMONIUM_CACHE_VERSION);
			cacheWriter.newLine();
			
			// Write the time stamp
			long currentTime = System.currentTimeMillis();
			this.cacheDate = currentTime;
			String timestamp = String.valueOf( currentTime );
			cacheWriter.write(timestamp);
			cacheWriter.newLine();
			
			// Create CSV writer
			CSVWriter csvWriter = new CSVWriter(cacheWriter);
			
			// Write entries to cache in CSV format
			// album_artist, album_name, year_num, disc_num, track_num, track_name, artist_name, duration, path
			
			for(Playable musicItem : musicCollection) {
				
				// Create the text line for this entry
				String[] line = {	musicItem.getAlbumArtistName(),
									musicItem.getAlbumName(),
									String.valueOf(musicItem.getReleaseYear()),
									String.valueOf(musicItem.getDiscNumber()),
									String.valueOf(musicItem.getTrackNumber()),
									musicItem.getTrackName(),
									musicItem.getArtistName(),
									String.valueOf(musicItem.getDuration()),
									musicItem.getPath()
				};
				
				// write the line in CSV format
				csvWriter.writeNext(line);
			}
			
			// close output streams
			csvWriter.close();
			cacheWriter.close();
			fout.close();
			
			// move the temp file to it's final location
			tempFile.renameTo(cacheFile);
		}
		catch(IOException e) {
			// There was an error in writing the temp file.
			tempFile.delete();
		}
	}
	
	/**
	 * Reads files on disk and creates a data structure representing music collection.
	 * 
	 * @return
	 */
	private synchronized void build() {
		
		System.out.println("Looking for music collection cache...");
		System.out.flush();
		
		File cacheFile = new File(this.musicRoot + File.separator + HARMONIUM_CACHE_FILE_NAME);
		if(cacheFile.exists() && cacheFile.canRead()) {
			// A cache file exists, so use it to reconstitute the music collection
			try {
				FileReader fin = new FileReader(cacheFile);
				BufferedReader cacheReader = new BufferedReader(fin);
				
				// Verify that this is really our file by checking the fingerprint of the first line
				String fingerprint = cacheReader.readLine();
				if(fingerprint.startsWith(HARMONIUM_CACHE_VERSION)) {

					System.out.println("Cache found. Loading cache...");
					System.out.flush();
					// This is our file. Grab the timestamp from line 2.
					String timestamp = cacheReader.readLine();
					try {
						this.cacheDate = Long.parseLong(timestamp);
					}
					catch(NumberFormatException e) {
						// There was a problem with the timestamp. Give it a default.
						this.cacheDate = 0;
					}
					
					//Read it line by line as a CSV file
					CSVReader csvIn = new CSVReader(cacheReader);
					String[] line;
					while( ( line = csvIn.readNext() ) != null ) {
						
						if(line.length == 9) {
							// This record has the right number of fields, so read it in
							
							try {
								// album_artist, album_name, year_num, disc_num, track_num, track_name, artist_name, duration, path
								String albumArtist	= line[0];
								String albumName	= line[1];
								int yearNum			= Integer.parseInt(line[2]);
								int discNum			= Integer.parseInt(line[3]);
								int trackNum		= Integer.parseInt(line[4]);
								String trackName	= line[5];
								String artistName	= line[6];
								long duration		= Long.parseLong(line[7]);
								String path			= line[8];
								
								
								File trackFile		= new File(this.musicRoot, path);
								if(! trackFile.exists()) {
									throw new FileNotFoundException();
								}
								
								// Create the new MP3File
								MP3File mp3 = new MP3File(path, trackFile, albumArtist, albumName, yearNum, discNum, trackNum, trackName, artistName, duration);
								
								// Add the MP3File to the trackMap
								String mapKey = trackFile.getCanonicalPath();
								this.trackMap.put(mapKey, mp3);
								
								// Add the MP3File to this music collection
								this.addTrack(this.hFactory.getPreferences(), mp3);
								
							}
							catch(NumberFormatException e) {
							}
							catch(FileNotFoundException e) {
							}
						}
							
					}
					csvIn.close();
					cacheReader.close();
					fin.close();
					
					if(this.hFactory.getPreferences().inDebugMode()) {
		        		System.out.println("DEBUG: Loaded from cache:");
		        		System.out.flush();
		        		this.printMusic(System.out);
		        	}
					return;
				}
				cacheReader.close();
				fin.close();
			}
			catch(Exception e) {
			}
 			
		}
		
		// If we made it this far, there was no cache or we couldn't read it. Load from scratch.
		
		// Loading music and playlists can take a while, so print some status messages
		System.out.println("No cache found. Searching for music. This may take several minutes...");
		System.out.flush();
		this.loadMusic(this.musicRoot, "");
		this.processM3UCache();		// Must be run AFTER loadMusic. Adds all m3u files encountered during music search. 
		
		System.out.println("Searching for playlists. This may take several minutes...");
		System.out.flush();
		this.loadPlaylists(this.playlistRoot, "");
		
		System.out.println("Build of music database completed");
		System.out.flush();
		
		// Write a cache file for this music collection
		this.writeCache();
	
	}
	
	public void refresh(Harmonium app) {
		
		// Only do stuff if we're not already refreshing in some other application
		if(! this.refreshing) {
			
			// Set lock to prevent multiple concurrent refreshes
			this.refreshing = true;
			
			try {
				
			
			// Remove tracks from music collection that no longer exist
			String[] keyArray = this.trackMap.keySet().toArray(new String[0]);
			for(int i=0; i < keyArray.length; i++) {

				// See if file corresponding to key still exists
				File trackFile = new File(keyArray[i]);
				if(! trackFile.exists()) {
					// File no longer exists, so delete the track from the music collection
					Playable track = this.trackMap.get(keyArray[i]);
					if(track != null) {
						// Remove track from music collection
						this.removeTrack(track);
						
						// Remove track from track map
						this.trackMap.remove(keyArray[i]);
					}
				}
			}
			
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			// Remove playlists from music collection that no longer exist
			int i = 0;
			while( i < this.playlistFiles.size() ) {
				
				if( ! this.playlistFiles.get(i).getFile().exists() ) {
					// This playlist no longer exists. Remove it from the list.
					this.playlistFiles.remove(i);
					
					// NOTE: The remove() method shifts later elements down, so DO NOT increment the index i.
				}
				else {
					// the playlist still existed, so move on to the next one
					++i;
				}
			}
			
			// Check for new tracks that need to be added to the music collection.
			this.loadMusic(this.musicRoot, "");
			
			// Process any M3U files that were found under the music root
			this.processM3UCache();
			
			// Check for new playlists under the playlist root
			this.loadPlaylists(this.playlistRoot, "");
			
			// Write a cache file for this music collection
			this.writeCache();
			
			// Unset the lock
			this.refreshing = false;
		}
	}
	
	/**
	 * Creates M3UFile objects for m3u files encountered under musicRoot
	 *
	 */
	private void processM3UCache() {
		
		for(File file : this.m3uCache) {
			
        	try {
        		M3UFile newM3U = new M3UFile(this.hFactory, file);
        		
        		// If the playlist is already in the music collection remove it (It may have changed.)
        		this.playlistFiles.remove(newM3U);
        		
        		// Add file to the list of playlists only if 
        		// it has not already been found under the playlistRoot and it is not empty.
        		if( (! this.playlistFiles.contains(newM3U) ) && (! newM3U.getMembers().isEmpty() ) ) {
        			this.playlistFiles.add(newM3U);
        		}
        	}
        	catch(Exception e) {
        		
        	}
		}
		
	}

	
	/**
	 * Prints members of this object. Used for debugging.
	 * 
	 * @param outputStream
	 */
	protected void printMusic(PrintStream outputStream) {
		
		outputStream.println("Music Collection:");
		outputStream.flush();
		
		for(AlbumArtist albumArtist : this.albumArtistList) {
			albumArtist.printMusic(outputStream);
		}
		
		for(Playable track : this.albumlessTrackList) {
			outputStream.println("= Track: " + track.getPath());
		}
		outputStream.flush();
	}
	
	/**
	 * Recursively search for music and load it into the collection.
	 * 
	 * @param musicDir	the root directory to search
	 * @param path		the path under the root directory to process
	 */
	private void loadMusic(String root, String path) {
		
		// Clear the M3U cache
		this.m3uCache.clear();
		
        File file = new File(root, path);
        if (file.isDirectory()) {
            if (path.length() > 0 && !path.endsWith("/")) {
                path += "/";
            }
            String list[] = file.list();             
            for (int i = 0; i < list.length; i++) {
                this.loadMusic(root, path + list[i]);
            }
        } 
        else if( path.toLowerCase().endsWith(".mp3") ) {
        	// This file is an MP3 music file
        	
        	String mapKey;
        	try {
        		mapKey = file.getCanonicalPath();
        	}
        	catch(IOException e) {
        		mapKey = null;
        	}
        	
        	// If the file has been last modified AFTER the time when our cache was written,
        	// then remove the track from the music collection and map so that it can be 
        	// re-read and re-added.
        	if(mapKey != null && this.trackMap.containsKey(mapKey) && (file.lastModified() > this.cacheDate) ) {
        		
        		if(this.hFactory.getPreferences().inDebugMode()) {
        			System.out.println("DEBUG: File out of date. Removing: " + path);
	        		System.out.println("DEBUG: Cache written: " + this.cacheDate);
        			System.out.println("DEBUG: file modified: " + file.lastModified());
	        		System.out.flush();
	        	}
        		
        		this.removeTrack(trackMap.get(mapKey));
        		this.trackMap.remove(mapKey);
        	}
        	
        	// Only add the file if the file has not already been added
        	if((mapKey != null) && (! this.trackMap.containsKey(mapKey) ) ) {
        		
	        	if(this.hFactory.getPreferences().inDebugMode()) {
	        		System.out.println("DEBUG: reading file " + path);
	        		System.out.flush();
	        	}
	        	
	        	org.dazeend.harmonium.music.MP3File newTrack = null;
	        	boolean addTrack = true;
	        	try {
	        		// Create a new MP3File to represent file we just found
	        		newTrack = new org.dazeend.harmonium.music.MP3File(path, file);
	        		
	                // Add the music track to the track map
	                this.trackMap.put(mapKey, newTrack);
	        	}
	        	catch(ID3Exception e) {
	        		// There was an error in reading the tags in the file. Don't add it.
	        		addTrack = false;
	        	}
	        	
	        	if(addTrack) {
	        		// There were no problems in getting file info. 
	        		// Add the music track to the music collection
	        		this.addTrack(this.hFactory.getPreferences(), newTrack);
	        	}
        	}
        }
        else if( path.toLowerCase().endsWith(".m3u") ) {
        	// This file is an M3U playlist, which are the only playlists permitted under musicRoot.
        	// Since the m3u file may contain references to tracks that have not been found yet,
        	// save the m3u file for later processing.
        	
        	this.m3uCache.add(file);
        	
        }
	}
	
	/**
	 * Recursively search for playlists and load it into the collection.
	 * 
	 * @param playlistDir	the root directory to search
	 * @param path		
	 */
	private void loadPlaylists(String root, String path) {
		
        File file = new File(root, path);
        if (file.isDirectory()) {
        	
            if (path.length() > 0 && !path.endsWith("/")) {
                path += "/";
            }
            String list[] = file.list();  
            
            for (int i = 0; i < list.length; i++) {
            	
                this.loadPlaylists(root, path + list[i]);
            }
        } 
        else if( path.toLowerCase().endsWith(".hpl") ) {
        	// This file is an HPL playlist.
        	// Create an HPLFile object from the playlist and add it 
        	try {
        		HPLFile newHPL = new HPLFile(this, file);

        		// NOTE: HPL playlists are editable on-screen so we don't need to delete
        		// and re-add them to have changes take effect.
        		
        		// only add the HPL playlist if it is not already in the music collection.
        		// NOTE: empty HPL playlists are permitted.
        		if( ! this.playlistFiles.contains(newHPL) ) {
        			this.playlistFiles.add(newHPL);
        		}
        	}
        	catch(Exception e) {
        	}
        }
        else if( path.toLowerCase().endsWith(".m3u") ) {
        	// This file is an M3U playlist.
        	
        	try {
        		M3UFile newM3U = new M3UFile(this.hFactory, file);
        		
        		// If the M3UFile is already in the music collection, remove it: it might have changed
            	this.playlistFiles.remove(newM3U);
        		
            	// Add it to the list of playlists only if 
            	// it has not already been found under the musicRoot and it is not empty.
        		if( (! this.playlistFiles.contains(newM3U) ) && (! newM3U.getMembers().isEmpty() ) ) {
        			this.playlistFiles.add(newM3U);
        		}
        	}
        	catch(Exception e) {
        		
        	}
        }
	}

	/**
	 * Returns a list of album artists in the collection.
	 * 
	 * @return
	 */
	public List<AlbumArtist> getAlbumArtistList() {
		return albumArtistList;
	}
	
	public List<TrackArtist> getTrackArtistList() {
		return trackArtistList;
	}

	/**
	 * Returns a list of tracks that have no artist information.
	 * 
	 * @return
	 */
	public List<Playable> getAlbumlessTrackList() {
		return albumlessTrackList;
	}
	
	/**
	 * @return the playlists
	 */
	public List<PlaylistFile> getPlaylists() {
		return playlistFiles;
	}
	
	/**
	 * Gets only HPL playlists that have been found.
	 * 
	 * @return
	 */
	public List<HPLFile> getHPLPlaylists() {
		List<HPLFile> hplList = new ArrayList<HPLFile>();
		
		for(PlaylistFile playlist : this.playlistFiles) {
			if(playlist.getClass().equals(HPLFile.class)) {
				hplList.add((HPLFile)playlist);
			}
		}
		return hplList;
	}
	
	/**
	 *  Adds a new playlist
	 */
	public void addPlaylist(PlaylistFile playlist) {
		this.playlistFiles.add(playlist);
	}

	/**
	 * Returns the Playable object represented by the given File. If it doesn't exist, returns null.
	 * 
	 * 
	 * @param file
	 * @return
	 */
	public Playable lookupTrackByFile(File file) {
		
		String canonicalPath = null;
		try {
			canonicalPath = file.getCanonicalPath();
		}
		catch(IOException e) {
			// Cannot find the canonical path, so there can't be a match.
			return null;
		}
		
		if(this.trackMap.containsKey(canonicalPath)) {
			return this.trackMap.get(canonicalPath);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @return the musicRoot
	 */
	public String getMusicRoot() {
		return musicRoot;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	public String toStringTitleSortForm(){
		return "All Music";
	}

	@Override
	public String toString() {
		return this.toStringTitleSortForm();
	}
}
