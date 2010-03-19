package org.dazeend.harmonium.music;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.screens.NowPlayingScreen;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.ImageSize;
import java.net.URL;

import com.tivo.hme.sdk.util.Mp3Helper;



/**
 * This class represents and manages playback of MP3 files.
 */
public class MP3File extends HMusic implements PlayableLocalTrack {

	private String 			albumArtistName = "";	
	private String			albumName = "";			
	private String			artistName = "";
	private String			trackName = "";
	private File			trackFile;
	private String			trackPath = "";
	private String			trackNameTitleSortForm = "";
	private int				trackNumber;
	private int				releaseYear;
	private int				discNumber;			
	private RatingLevel		rating = RatingLevel.UNRATED;
	private long			duration = -1;
	
	private Boolean			albumImageFetched = false;
	private Boolean			hasAlbumArt = false;


	/**
	 * Constructor scans MP3 file for ID3 tags to get track information.
	 * 
	 * @param path		a String that represents the path to the track relative to the music root.
	 * @param file		a <code>File</code> that represents the track on disk.
	 */
	public MP3File(String path, File file) throws ID3Exception {

		this.trackFile = file;
		this.trackPath = path;
		
		// Create an org.blinkenlights.jid3.MP3File to read tag data from
		org.blinkenlights.jid3.MP3File mp3File = new org.blinkenlights.jid3.MP3File(file);
		
		// Get any ID3v2.3 tag that exists in the mp3 file and load its data
		ID3V2_3_0Tag v23Tag = (ID3V2_3_0Tag) mp3File.getID3V2Tag();
		if(v23Tag != null) {

			// An ID3v2.3 tag exists, so pull text fields from tags
			if( (v23Tag.getTALBTextInformationFrame() != null) && (v23Tag.getTALBTextInformationFrame().getAlbum() != null ) ){
				this.albumName = removeDiscLabel(v23Tag.getTALBTextInformationFrame().getAlbum().trim());
			}
			
			if( (v23Tag.getTIT2TextInformationFrame() != null) && (v23Tag.getTIT2TextInformationFrame().getTitle() != null ) ) {
				this.trackName = v23Tag.getTIT2TextInformationFrame().getTitle().trim();
			}
			
			if( v23Tag.getTRCKTextInformationFrame() != null ) {
				this.trackNumber = v23Tag.getTRCKTextInformationFrame().getTrackNumber();
			}
			
			if( (v23Tag.getTPOSTextInformationFrame() != null) ) {
				
				// Do not set the tag if there is only 1 totalPart
				if(v23Tag.getTPOSTextInformationFrame().getTotalParts() != 1) {
					this.discNumber = v23Tag.getTPOSTextInformationFrame().getPartNumber();
				}
			}
			
			if(v23Tag.getTYERTextInformationFrame() != null) {
				this.releaseYear = v23Tag.getTYERTextInformationFrame().getYear();
			}
			
			// Create title formatted string for track. 
			if(! this.trackName.equals("")) {
				Matcher stringMatcher = titlePattern.matcher(this.trackName);
				if(stringMatcher.lookingAt()) {
					// Found a leading article. Move it to the end of the string.
					this.trackNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
				}
				else {
					this.trackNameTitleSortForm = this.trackName;
				}
			}
			
			// Artists are returned as an array of artists. Concatenate them into one string, if the frame exists.
			if(v23Tag.getTPE1TextInformationFrame() != null) {
				String[] tempArtistNameArray = v23Tag.getTPE1TextInformationFrame().getLeadPerformers();
				if(tempArtistNameArray.length > 0) {
					StringBuilder tempArtistNameBuilder = new StringBuilder(tempArtistNameArray[0].trim());
					for(int i = 1; i < tempArtistNameArray.length; ++i) {
						tempArtistNameBuilder.append('/').append(tempArtistNameArray[i].trim());
					}
					this.artistName = tempArtistNameBuilder.toString();
				}
			}
			
			// AlbumArtist is a non-standard ID3 tag, and TPE2 isn't really intended for it. So if AlbumArtist is
			// blank, copy the track artist into the album artist.
			if( (v23Tag.getTPE2TextInformationFrame() != null) && ( ! v23Tag.getTPE2TextInformationFrame().getBandOrchestraAccompaniment().trim().equals("") ) ) {
				this.albumArtistName = v23Tag.getTPE2TextInformationFrame().getBandOrchestraAccompaniment().trim();
			}
			if(this.albumArtistName.equals("")) {
				this.albumArtistName = this.artistName;
			}
			
/*
			// Get any popularimeter (POPM) frames in the tag
			POPMID3V2Frame[] POPMFrames = v23Tag.getPOPMFrames();
			
			// Iterate through the POPM frams to see if any of them belong to us.
			for(POPMID3V2Frame frame : POPMFrames) {
				
				// Get email address of user
				// TODO DEFERRED: implement for ratings: get email address from Preferences object
				String myEmail = "harmonium@DazeEnd.org";
				
				// Check if the frame belongs to us.
				if( myEmail.equals( frame.getEmailToUser() ) ) {
						
					// This is our tag, so get the rating and save it
					switch(frame.getPopularity()) {
					
					case 1:
						this.rating = RatingLevel.LEVEL_1;
						break;
						
					case 2:
						this.rating = RatingLevel.LEVEL_2;
						break;
						
					case 3:
						this.rating = RatingLevel.LEVEL_3;
						break;
						
					case 4:
						this.rating = RatingLevel.LEVEL_4;
						break;
						
					case 5:
						this.rating = RatingLevel.LEVEL_5;
						break;
						
					default:
						this.rating = RatingLevel.UNRATED;
					}
					
					// We've already found our rating, so stop looking by dropping out of the for-loop
					break;
				}
			}
			*/
		}
	
		// We've grabbed all the ID3v2.3 data. Now fill in any blanks with data from ID3v1.x tags.
		// Note that ID3v1.x tags are much more limited than v2.3, so we won't be able to get all the data.
		ID3V1Tag v1Tag = mp3File.getID3V1Tag();
		
		if( (this.artistName.equals("")) && (v1Tag != null) && (v1Tag.getArtist() != null ) ) {
			this.artistName = v1Tag.getArtist().trim();
		}
		
		if(this.albumArtistName.equals("")) {
			// There's no way to get album artist from v1.x, so fake it.
			this.albumArtistName = this.artistName;  
		}
		
		if( (this.albumName.equals("")) && (v1Tag != null) && (v1Tag.getAlbum() != null ) ) {
			this.albumName = removeDiscLabel(v1Tag.getAlbum().trim());
		}
		
		if( (this.trackName.equals("")) && (v1Tag != null) && (v1Tag.getTitle() != null ) ) {
			this.trackName = v1Tag.getTitle();
		}
		
		if(this.releaseYear == 0 && (v1Tag != null)) {
			try{
				this.releaseYear = Integer.parseInt(v1Tag.getYear());
			}
			catch(NumberFormatException e) {
				// If we get a bad number from the tag, just skip it.
			}
		}
		
		if((this.trackNumber == 0) && (v1Tag != null) && (v1Tag instanceof ID3V1_1Tag)) {
			try {
				this.trackNumber = ((ID3V1_1Tag) v1Tag).getAlbumTrack();
			}
			catch(ClassCastException e) {
				// If there's a problem with the cast, just skip this field.
			}
		}
		
		// Find the length of this track in milliseconds
		try {
			InputStream in = new FileInputStream(file);
	        try {
	            Mp3Helper mp3Helper = new Mp3Helper(in, file.length());
	            this.duration = mp3Helper.getMp3Duration();
	        } finally {
	        	try {
	        		in.close();
	        	}
	        	catch(IOException e) {
	        	}
	        }
		}
		catch(FileNotFoundException e) {
		}
	}
	
	private static final Pattern discPattern = Pattern.compile("(?i)(.*)(\\s+[\\(\\[\\{]?Disc\\s[0-9]+[\\)\\]\\}]?)\\z");
	private static final Pattern cdPattern = Pattern.compile("(?i)(.*)(\\s+[\\(\\[\\{]?CD\\s[0-9]+[\\)\\]\\}]?)\\z");
	private static String removeDiscLabel(String originalName)
	{
		Matcher m = discPattern.matcher(originalName);
		if (m.lookingAt())
			return m.group(1);
		else {
			m = cdPattern.matcher(originalName);
			if (m.lookingAt())
				return m.group(1);
			else
				return originalName;
		}
	}
	
	/**
	 * Constructor takes track information as parameters and does not scan file for ID3 tags.
	 * 
	 * @param path		a String that represents the path to the track relative to the music root.
	 * @param file		a <code>File</code> that represents the track on disk.
	 */
	public MP3File(	String	path, 			// path to file relative to music root
					File	file, 			// file object representing track on disk
					String	albumArtist,
					String	albumName, 
					int		yearNum, 		// Release year
					int		discNum, 		// disc number for multi-part albums. (Use zero for no disc number.)
					int		trackNum, 
					String	trackName, 
					String	artistName,
					long	duration
	) {
		this.trackFile = file;
		this.trackPath = path;
		
		this.albumArtistName = albumArtist;
		this.albumName = albumName;
		this.releaseYear = yearNum;
		this.discNumber = discNum;
		this.trackNumber = trackNum;
		this.trackName = trackName;
		this.artistName = artistName;
		this.duration = duration;
		
		// Create title formatted string for track. 
		if(! this.trackName.equals("")) {
			// The track has a name, so re-format it
			// Compile pattern for matching leading articles
			Matcher stringMatcher = titlePattern.matcher(this.trackName);
			if(stringMatcher.lookingAt()) {
				// Found a leading article. Move it to the end of the string.
				this.trackNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
			}
			else {
				this.trackNameTitleSortForm = this.trackName;
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.Playable#getDuration()
	 */
	public long getDuration() {
		return this.duration;
	}

	/**
	 * Checks existence of album art for this object.
	 */
	//@Override
	public boolean hasAlbumArt(FactoryPreferences prefs) {
		
		if ( albumImageFetched )
			return hasAlbumArt;
		
		return (this.getAlbumArt(prefs) != null);
	}
	
	private Image getAlbumArtFromID3Tag(FactoryPreferences prefs)
	{
		if (prefs.inDebugMode())
			System.out.println("Retrieving embedded cover art from " + this.trackFile.getAbsolutePath());

		Image img = null;

		// Create an org.blinkenlights.jid3.MP3File to read tag data from
		org.blinkenlights.jid3.MP3File mp3File = new org.blinkenlights.jid3.MP3File(this.trackFile);

		// Get any ID3v2.3 tag that exists in the mp3 file and load its data
		ID3V2_3_0Tag v23Tag;
		try {
			v23Tag = (ID3V2_3_0Tag) mp3File.getID3V2Tag();
		}
		catch(ID3Exception e) {
			// There was an error reading the ID3 info, so just return null
			return null;
		}
		
		if(v23Tag != null) {
			
			// Get any attached picture (APIC) frames in this tag.
			APICID3V2Frame[] APICFrames = v23Tag.getAPICFrames();

			if(APICFrames.length > 0) {
				// APIC frames categorize each attached picture with a "picture type". If a picture with the type
				// of "front cover" exists, use that picture -- it should be the album art. If there's no picture
				// with a type of "front cover" just use the first picture in the array.
				for(APICID3V2Frame frame : APICFrames) {
					if( frame.getPictureType().equals(APICID3V2Frame.PictureType.FrontCover) ) {
						// Make sure that the MIME type for this cover art can be understood by TiVo
						String tempMimeType = frame.getMimeType();
						for(TivoImageFormat format : TivoImageFormat.values()) {
							if(format.getMimeType().equals(tempMimeType)) {
								img = new ImageIcon(frame.getPictureData()).getImage();
								break;
							}
						}
						// We found the cover art, so stop looking for it.
						break;
					}
				}
				
				// If we didn't find an attached picture that claims to be of the front cover, just grab
				// the first valid image.
				if(img == null) {
					for(APICID3V2Frame frame : APICFrames) {
						// Make sure that the MIME type for this album art can be understood by TiVo
						String tempMimeType = frame.getMimeType();
						for(TivoImageFormat format : TivoImageFormat.values()) {
							if(format.getMimeType().equals(tempMimeType)) {
								img = new ImageIcon(frame.getPictureData()).getImage();
								if (img.getWidth(null) < 1 || img.getHeight(null) < 1)
									img = null;
								else
									break;
							}
						}
						if (img != null)
							break;
					}
				}
			}
		}
		return img;
	}
	
	private Image getAlbumArtFromFile(FactoryPreferences prefs) throws IOException
	{

		if (prefs.inDebugMode())
			System.out.println("Retrieving file-based cover art for " + this.trackFile.getAbsolutePath());
		
		Image img = null;
		File parentFolder = this.trackFile.getParentFile();
		String possibleImageFileName = null;
		String imageFileName = null;

		String[] files = parentFolder.list();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].equalsIgnoreCase("folder.jpg") || files[i].equalsIgnoreCase("cover.jpg"))
				{
					imageFileName = files[i];
					break;
				}
				else if (possibleImageFileName == null)
				{
					String extension = files[i].substring(files[i].length() - 4);
					if (extension.equalsIgnoreCase(".jpg"))
						possibleImageFileName = files[i];
				}
			}
		}
		
		if (imageFileName == null && possibleImageFileName != null)
			imageFileName = possibleImageFileName;
		
		if (imageFileName != null)
		{
			String imageFilePath = parentFolder.getCanonicalPath() + File.separatorChar + imageFileName;
			img = new ImageIcon(imageFilePath).getImage();
		}
		
		return img;
	}

	/*
	private BufferedReader read(String url) throws Exception{
		return new BufferedReader(
			new InputStreamReader(
				new URL(url).openStream()));}
	*/

	//This function connects to last.fm, retrieves a URL to album art and downloads the album art
	private Image getAlbumArtFromHTTP(FactoryPreferences prefs) throws Exception
	{
		if (prefs.inDebugMode())
			System.out.println("Retrieving http-based cover art for " + this.trackFile.getAbsolutePath());
		
		Image img = null;
		String apiKey = "7984437bf046cc74c368f02bf9de16de";
		Album albumInfo = Album.getInfo(artistName,albumName,apiKey);
		if (albumInfo != null)
		{
			String ImageURL = albumInfo.getImageURL(ImageSize.valueOf("LARGE"));

			//lets prevent some MalformedURLExceptions by making sure we actually have something in our URL
			if(ImageURL.length() > 0)
			{
				try {
					URL url = new URL(ImageURL);
					img = Toolkit.getDefaultToolkit().createImage(url);
					if (prefs.inDebugMode())
						System.out.println("Found album cover by http for " + artistName + "-" + albumName);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (img == null && prefs.inDebugMode()) {
			System.out.println("No Album Art Found by http For Artist: " + artistName + " Album: " + albumName);
		}
		
		return img;
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getAlbumArt()
	 */
	//@Override
	public synchronized Image getAlbumArt(FactoryPreferences prefs) {
		
		Image img = null;
		
		try 
		{
			// If set to prefer file-based art, and not ignoring file-based art, look for that first.
			if (prefs.preferJpgFileArt() &&!prefs.ignoreJpgFileArt())
				img = getAlbumArtFromFile(prefs);
			
			// If we don't yet have an image and we're not ignoring embedded art, look there.
			if (img == null && !prefs.ignoreEmbeddedArt())
				img = getAlbumArtFromID3Tag(prefs);
			
			// If we still don't have an image, and we didn't already look at file-based art, 
			// and we're not ignoring file-based art, look for it now.
			if (img == null && !prefs.preferJpgFileArt() && !prefs.ignoreJpgFileArt())
				img = getAlbumArtFromFile(prefs);

			//If we still dont have it, give the online service a shot.
			if (img == null){
				img = getAlbumArtFromHTTP(prefs);
			}
			
			hasAlbumArt = (img != null);
		} 
		catch (Throwable t) 
		{
			hasAlbumArt = false;
			t.printStackTrace();
		} 
		finally 
		{
			albumImageFetched = true;
		}
		
		return img;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getAlbumArtistName()
	 */
	//@Override
	public String getAlbumArtistName() {
		return this.albumArtistName;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getAlbumName()
	 */
	//@Override
	public String getAlbumName() {
		return this.albumName;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getArtistName()
	 */
	//@Override
	public String getArtistName() {
		return this.artistName;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getDiscNumber()
	 */
	//@Override
	public int getDiscNumber() {
		return this.discNumber;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getRating()
	 */
	//@Override
	public RatingLevel getRating() {
		return this.rating;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getReleaseYear()
	 */
	//@Override
	public int getReleaseYear() {
		return this.releaseYear;
	}
	
	/* (non-Javadoc
	 * 
	 * Gets an Image containing scaled album art for this track.
	 *
	 */
	//@Override
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
//	            try {
//	            	// the media tracker reqires a component, so we use this button
//	                Button mediaTrackerComp = new Button();
//	                
//	                // use MediaTracker to wait for image to be completely scaled. (I think? TiVo code.)
//	                MediaTracker mt = new MediaTracker(mediaTrackerComp);
//	                mt.addImage(img, 0);
//	                mt.waitForAll();
//	            } catch(InterruptedException e) {
//	            }
	        }
		}
        return img;
	}

	/**
	 * Gets the path to the track on disk relative to the music root.
	 * 
	 * @return	the path to the track on disk
	 */
	//@Override
	public String getURI() {
		return this.trackPath;
	}
	
	/**
	 * @return the filename
	 */
	//@Override
	public String getFilename() {
		return this.trackFile.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getTrackName()
	 */
	//@Override
	public String getTrackName() {
		return this.trackName;
	}

	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getTrackNameTitleSortForm()
	 */
	//@Override
	public String getTrackNameTitleSortForm() {
		return this.trackNameTitleSortForm;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#getTrackNumber()
	 */
	//@Override
	public int getTrackNumber() {
		return this.trackNumber;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#play()
	 */
	//@Override
	public boolean play(NowPlayingScreen nowPlayingScreen) 
	{
		return nowPlayingScreen.play(this); 
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.Playable#stop()
	 */
	//@Override
	public boolean stop(NowPlayingScreen nowPlayingScreen) {
		nowPlayingScreen.stopPlayback();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#pause()
	 */
	//@Override
	public boolean pause(NowPlayingScreen nowPlayingScreen) {
		if( nowPlayingScreen.getMusicStream() != null ) {
			if(! nowPlayingScreen.getMusicStream().isPaused() ) {
				nowPlayingScreen.getMusicStream().pause();
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#unpause()
	 */
	//@Override
	public boolean unpause(NowPlayingScreen nowPlayingScreen) {
		if( nowPlayingScreen.getMusicStream() != null ) {
			if(nowPlayingScreen.getMusicStream().isPaused() ) {
				nowPlayingScreen.getMusicStream().play();
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#setPlayRate()
	 */
	//@Override
	public boolean setPlayRate(NowPlayingScreen nowPlayingScreen, float speed) {
		
		if(nowPlayingScreen.getMusicStream() == null) {
			
		}
		if(nowPlayingScreen.getMusicStream() != null) {
			nowPlayingScreen.getMusicStream().setSpeed(speed);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#increaseRating()
	 */
	//@Override
	public synchronized void increaseRating() {
		// TODO DEFERRED: implement for ratings
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#decreaseRating()
	 */
	//@Override
	public synchronized void decreaseRating() {
		// TODO DEFERRED: implement for ratings
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
	 */
	//@Override
	public List<Playable> getMembers(Harmonium app) {
		// Playable objects are the leaf of the tree and there can be no other	
		// tracks below it. So return a list with only this object in it.
		List<Playable> list = new ArrayList<Playable>();
		list.add(this);
		return list;
	}

	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.Playable#toTitle()
	 */
	//@Override
	public String toStringTitleSortForm() {
		if((! this.trackNameTitleSortForm.equals("")) && (this.trackNameTitleSortForm != null)) {
			return this.trackNameTitleSortForm;
		}
		else {
			// The track doesn't have a name, so just return the track's filename
			return this.trackFile.getName();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * @see org.dazeend.harmonium.Playable#toString()
	 */
	@Override
	public String toString() {
		// Prints track name, or failing that the filename.
		
		if((! trackName.equals("")) && (this.trackName != null)) {
			return this.trackName;
		}
		else {
			// The track doesn't have a name, so just return the track's filename
			return this.trackFile.getName();
		}
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((trackFile == null) ? 0 : trackFile.hashCode());
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MP3File other = (MP3File) obj;
		if (trackFile == null) {
			if (other.trackFile != null)
				return false;
		} else if (!trackFile.equals(other.trackFile))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.music.Playable#getTrackFile()
	 */
	public File getTrackFile() {
		return this.trackFile;
	}

	public String getDisplayArtistName()
	{
		return getArtistName();
	}

	public String getArtHashKey()
	{
		return getAlbumArtistName() + getAlbumName() + getReleaseYear();
	}

	public String getContentType()
	{
		return "audio/mpeg";
	}
}
