/*
 * Copyright 2008 Charles Perry
 *
 * This file is part of Harmonium, the TiVo music player.
 *
 * Harmonium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Harmonium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
package org.dazeend.harmonium.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.Harmonium.HarmoniumFactory;

public class M3UFile implements PlaylistFile {
	
	// Instance variables
	private HarmoniumFactory	hFactory;
	private List<Playable> members = new ArrayList<Playable>();
	protected File file;
	
	/**
	 * 
	 * @param hFactory the HFactory that this object is associated with.
	 * @param file	A File object that represents this M3UFile on disk.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public M3UFile(HarmoniumFactory hFactory, File file) throws IOException, FileNotFoundException {
		this.file = file;
		this.hFactory = hFactory;
		
		if(this.hFactory.getPreferences().inDebugMode()) {
			System.out.println("DEBUG: Processing M3U file: " + file.getAbsolutePath());
		}
		
		// open M3UFile for reading
		FileReader fin = new FileReader(this.file);
		BufferedReader bin = new BufferedReader(fin);
		try {
			// Read the file line by line
			while(bin.ready()) {
				String line = bin.readLine();
				// ignore any line that begins with a pound ('#')
				if( ! line.startsWith("#") ) {
					
					// Construct absolute path for the line and create the cooresponding File object
					File lineFile = new File(line);
					
					if( (! lineFile.exists() ) || (! lineFile.isAbsolute() ) ) {
						// The file wasn't absolute or didn't exist, so check relative to the parent of this M3UFile
						line = file.getParentFile().getAbsolutePath() + File.separator + line;
					
						lineFile = new File(line);
					}
					
					if(this.hFactory.getPreferences().inDebugMode()) {
						if(! lineFile.exists()) {
							System.out.println("DEBUG: Does not exist: " + lineFile.getPath());
						}
						else if(! lineFile.isFile()) {
							System.out.println("DEBUG: Not a file: " + lineFile.getPath());
						}
						else if(! lineFile.canRead()) {
							System.out.println("DEBUG: File not readable: " + lineFile.getPath());
						}
						
						System.out.flush();
					}
					
					// NOTE: The isFile test below MIGHT be fooled by symlinks 
					// that point to directories. Not sure.
					if(lineFile.exists() && lineFile.isFile() && lineFile.canRead()) {
						// lineFile is valid
						Playable track = MusicCollection.getMusicCollection( this.hFactory ).lookupTrackByFile(lineFile);
						
						if( track != null ) {
							// The track has been found in the music database. Add it to the list of members.
							this.members.add(track);
						}
					}
				}
			}
		}
		finally {
			// Close streams
			bin.close();
			fin.close();
		}
		
		if(this.hFactory.getPreferences().inDebugMode() ) {
			System.out.println("DEBUG: Printing M3U members:");
			for(Playable track : this.members) {
				System.out.println("DEBUG: " + track.getPath());
			}
			System.out.flush();
		}
	}

	

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.PlaylistFile#getMembers()
	 */
	public List<PlaylistEligible> getMembers() {
		List<PlaylistEligible> list = new ArrayList<PlaylistEligible>();
		list.addAll(this.members);
		return list;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.PlaylistFile#getDescription()
	 */
	public String getDescription() {
		
		return this.file.getName();
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.PlaylistFile#getRepeatMode()
	 */
	public boolean getRepeatMode(Harmonium app) {
		return app.getPreferences().getPlaylistFileDefaultRepeatMode();
	}


	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.PlaylistFile#getShuffleMode()
	 */
	public boolean getShuffleMode(Harmonium app) {
		
		return  app.getPreferences().getPlaylistFileDefaultShuffleMode();
	}



	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.PlaylistFile#toString()
	 */
	@Override
	public String toString() {
		return this.file.getName();
	}



	public List<Playable> listMemberTracks(Harmonium app) {
		List<Playable> list = new ArrayList<Playable>();
		list.addAll(this.members);
		return list;
	}



	public String toStringTitleSortForm() {
		return this.file.getName();
	}



	/**
	 * Gets the File object that represents this PlaylistFile on disk.
	 * 
	 * @return
	 */
	public File getFile()
	{
		return this.file;
	}

	public int compareTo(PlaylistFile that)
	{
		// NullPointerException if we are trying to compare to a null object
		if(that == null){
			throw new NullPointerException();
		}
		
		return this.toString().compareToIgnoreCase(that.toString());
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PlaylistFile other = (PlaylistFile) obj;
		if (file == null) {
			if (other.getFile() != null)
				return false;
		} else if (!file.equals(other.getFile()))
			return false;
		return true;
	}
	
	
}
