package org.dazeend.harmonium.screens;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.dazeend.harmonium.DiscJockeyListener;
import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.PlayRate;
import org.dazeend.harmonium.music.ArtSource;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlayableTrack;

import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resource;

public class NowPlayingScreen extends HManagedResourceScreen implements DiscJockeyListener
{
	// These are the fields that might be updated
	private BView albumArtView;
	private BText albumNameText;
	private BText albumArtistText;
	private BText yearText;
	private BText trackNameText;
	private BText artistNameText;
	private BText shuffleModeText;
	private BText repeatModeText;
	private BText nextTrackText;
	private ProgressBar progressBar;
	private BText artistNameLabelText;

	/**
	 * @param app
	 */
	public NowPlayingScreen(Harmonium app) 
	{
		super(app);
		doNotFreeResourcesOnExit(); // We'll free of our own resources, using the tools HManagedResourceScreen gives us. 
		
		// Define all dimensions in terms of percentage of the screen height and width. This make it
		// resolution-safe.
		
		//	constants used for screen layout
		int screenWidth 	= app.getWidth();
		int screenHeight 	= app.getHeight();
		int safeTitleH	= app.getSafeTitleHorizontal();
		int safeTitleV	= app.getSafeTitleVertical();
		int hIndent = (int)( ( screenWidth - (2 * safeTitleH) ) * 0.01 );
		int vIndent = (int)( ( screenHeight - (2 * safeTitleV) ) * 0.01 );
		
		// Define height of each row of album info text
		int albumInfoRowHeight = app.hSkin.paragraphFontSize + (app.hSkin.paragraphFontSize / 4);
		
		// Create views for album art. Size is square, sides less of 480px or half title-safe width
		// (640x480 is the maximum image size that TiVo can load.)
		final int artSide = Math.min(480,(screenWidth - (2 * safeTitleH) ) / 2 );
		
		// Define the y-coordinate for album art so that the info is verticaly centered in the screen
		int albumArtViewY = ( this.getHeight() - ( artSide + (3 * albumInfoRowHeight) ) ) / 2;
		this.albumArtView = new BView( this.getNormal(), safeTitleH, albumArtViewY, artSide, artSide);
		
		// Add album info text
		this.albumNameText = new BText(	this.getNormal(), 													// parent
										this.albumArtView.getX() - safeTitleH,								// x
										this.albumArtView.getY() + this.albumArtView.getHeight() + vIndent,	// y
										this.albumArtView.getWidth() + (2 * safeTitleH),					// width
										albumInfoRowHeight													// height
		);
		
		// Set album info text properties
		this.albumNameText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		this.albumNameText.setShadow(false);
		this.albumNameText.setFlags(RSRC_HALIGN_CENTER);
		this.albumNameText.setFont(app.hSkin.paragraphFont);
		
		// Add album artist info text
		this.albumArtistText = new BText(	this.getNormal(), 										// parent
											this.albumArtView.getX() - safeTitleH,					// x
											this.albumNameText.getY() + albumNameText.getHeight(),	// y
											this.albumArtView.getWidth() + (2 * safeTitleH),		// width
											albumInfoRowHeight										// height
		);
		
		// Set album info text properties
		this.albumArtistText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		this.albumArtistText.setShadow(false);
		this.albumArtistText.setFlags(RSRC_HALIGN_CENTER);
		this.albumArtistText.setFont(app.hSkin.paragraphFont);
		
		// Add album year text
		this.yearText = new BText(	this.getNormal(), 											// parent
									this.albumArtView.getX(),									// x
									this.albumArtistText.getY() + albumArtistText.getHeight(),	// y
									this.albumArtView.getWidth(),								// width
									albumInfoRowHeight											// height
		);
		
		// Set album info text properties
		this.yearText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		this.yearText.setShadow(false);
		this.yearText.setFlags(RSRC_HALIGN_CENTER);
		this.yearText.setFont(app.hSkin.paragraphFont);
		
		// Define constants to make track info layout easier
		// NOTE: Can't be defined with other constants, because they rely on albumArtView
		int leftEdgeCoord = this.albumArtView.getX() + this.albumArtView.getWidth() + hIndent;
		int textWidth = this.getWidth() - leftEdgeCoord - safeTitleH;
		int rowHeight = this.albumArtView.getHeight() / 5;
		
		// Add track title
		this.trackNameText = new BText(	this.getNormal(), 			// parent
										leftEdgeCoord,				// x coord relative to parent
										this.albumArtView.getY(), 	// y coord relative to parent
										textWidth,					// width
										rowHeight					// height
		);
		this.trackNameText.setColor(HSkin.BAR_TEXT_COLOR);
		this.trackNameText.setShadow(false);
		this.trackNameText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM + RSRC_TEXT_WRAP);
		this.trackNameText.setFont(app.hSkin.barFont);
		
		// Put in track title label
		BText trackNameLabelText = new BText(	this.getNormal(),
												leftEdgeCoord,
												this.albumArtView.getY() + rowHeight,
												textWidth,
												rowHeight
		);
		trackNameLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		trackNameLabelText.setShadow(false);
		trackNameLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
		trackNameLabelText.setFont(app.hSkin.paragraphFont);
		trackNameLabelText.setValue("Title");
		
		// Add track artist
		this.artistNameText = new BText(	this.getNormal(), 								// parent
											leftEdgeCoord,									// x coord relative to parent
											this.albumArtView.getY() + (2 * rowHeight), 	// y coord relative to parent
											textWidth,										// width
											rowHeight										// height
		);
		this.artistNameText.setColor(HSkin.BAR_TEXT_COLOR);
		this.artistNameText.setShadow(false);
		this.artistNameText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM + RSRC_TEXT_WRAP);
		this.artistNameText.setFont(app.hSkin.barFont);
		
		artistNameLabelText = new BText(this.getNormal(),
										leftEdgeCoord,
										this.albumArtView.getY() + (3 * rowHeight),
										textWidth,
										rowHeight);
		artistNameLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		artistNameLabelText.setShadow(false);
		artistNameLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
		artistNameLabelText.setFont(app.hSkin.paragraphFont);
		
		// Create Progress Bar
		this.progressBar = new ProgressBar(	this, 
											leftEdgeCoord, 
											this.albumArtView.getY() + (4 * rowHeight),
											textWidth,
											this.app.hSkin.paragraphFontSize
		);
		
		// Create footer layout variables
		int footerHeight = this.app.hSkin.paragraphFontSize;
		int footerTop = screenHeight - this.app.getSafeActionVertical() - footerHeight;
		
		// Create all footer objects before initializing them. 
		// Some methods we call depend on their existence.
		BText repeatLabelText = new BText(	this.getNormal(),
				safeTitleH,
				footerTop,
				3 * this.app.hSkin.paragraphFontSize,
				footerHeight
		);
		this.repeatModeText = new BText(	this.getNormal(),
				safeTitleH + repeatLabelText.getWidth(),
				footerTop,
				2 * this.app.hSkin.paragraphFontSize,
				footerHeight
		);
		BText shuffleLabelText = new BText(	this.getNormal(),
				this.repeatModeText.getX() + this.repeatModeText.getWidth(),
				footerTop,
				3 * this.app.hSkin.paragraphFontSize,
				footerHeight
		);
		this.shuffleModeText = new BText(	this.getNormal(),
				shuffleLabelText.getX() + shuffleLabelText.getWidth(),
				footerTop,
				2 * this.app.hSkin.paragraphFontSize,
				footerHeight
		);
		BText nextLabelText = new BText(	this.getNormal(),
				this.shuffleModeText.getX() + this.shuffleModeText.getWidth(),
				footerTop,
				2 * this.app.hSkin.paragraphFontSize,
				footerHeight
		);
		this.nextTrackText = new BText(	this.getNormal(),
				nextLabelText.getX() + nextLabelText.getWidth(),
				footerTop,
				screenWidth - safeTitleH - ( nextLabelText.getX() + nextLabelText.getWidth() ),
				footerHeight
		);
		
		// init Shuffle label
		shuffleLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		shuffleLabelText.setShadow(false);
		shuffleLabelText.setFlags(RSRC_HALIGN_LEFT);
		shuffleLabelText.setFont(app.hSkin.paragraphFont);
		shuffleLabelText.setValue("Shuffle:");
		
		// init Shuffle Mode Text
		this.shuffleModeText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		this.shuffleModeText.setShadow(false);
		this.shuffleModeText.setFlags(RSRC_HALIGN_LEFT);
		this.shuffleModeText.setFont(app.hSkin.paragraphFont);
		
		// init Repeat label
		repeatLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		repeatLabelText.setShadow(false);
		repeatLabelText.setFlags(RSRC_HALIGN_LEFT);
		repeatLabelText.setFont(app.hSkin.paragraphFont);
		repeatLabelText.setValue("Repeat:");
		
		// init Repeat Mode Text
		this.repeatModeText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		this.repeatModeText.setShadow(false);
		this.repeatModeText.setFlags(RSRC_HALIGN_LEFT);
		this.repeatModeText.setFont(app.hSkin.paragraphFont);
		
		// init next playing label
		nextLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		nextLabelText.setShadow(false);
		nextLabelText.setFlags(RSRC_HALIGN_LEFT);
		nextLabelText.setFont(app.hSkin.paragraphFont);
		nextLabelText.setValue("Next:");

		// init next track Text
		this.nextTrackText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		this.nextTrackText.setShadow(false);
		this.nextTrackText.setFlags(RSRC_HALIGN_LEFT);
		this.nextTrackText.setFont(app.hSkin.paragraphFont);
	}
	
	public void beginUpdate()
	{
    	app.getRoot().setPainting(false);
	}
	
	public void endUpdate()
	{
		app.getRoot().setPainting(true);
	}
	
	/**
	 * Update the screen to a new music item
	 */
	public void nowPlayingChanged(final Playable nowPlaying) 
	{
		// turn off painting while we update the images
    	
    	try 
    	{
       		// Update views with new info
	
    		
            // update the shuffle and repeat mode indicators
            shuffleChanged(this.app.getDiscJockey().isShuffling());
            repeatChanged(this.app.getDiscJockey().isRepeating());
    	}
    	finally 
    	{
    	}
	}

	public void nextTrackChanged(PlayableTrack nextTrack)
	{
		if (nextTrack != null)
			this.nextTrackText.setValue( nextTrack.getDisplayArtistName() + " - " + nextTrack.getTrackName() );
		else
			this.nextTrackText.setValue("");
	}

	public void playRateChanging(PlayRate newPlayRate)
	{
		this.app.play(newPlayRate.getSound());
	}

	public void artChanged(final ArtSource artSource)
	{
		new Thread() 
		{
			public void run() 
			{
				ImageResource albumArtImage = createManagedImage(artSource, albumArtView.getWidth(), albumArtView.getHeight());
				setManagedResource(albumArtView, albumArtImage, RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
	    		flush(); // Necessary to ensure UI updates, because we're in another thread.
			}
		}.start();
	}
	
	public void trackNameChanged(final String title)
	{
        this.trackNameText.setValue(title);
	}

	public void albumArtistChanged(String albumArtistName)
	{
		if (albumArtistName != null && !albumArtistName.isEmpty())
			albumArtistText.setValue(albumArtistName);
		else
            albumArtistText.setValue("");
	}

	public void albumChanged(String albumName, int discNumber)
	{
		if (albumName != null && !albumName.isEmpty())
		{
			if (discNumber > 0)
				albumNameText.setValue(albumName + " - Disc " + discNumber);
			else
				albumNameText.setValue(albumName);
		}
		else
			albumNameText.setValue("");
	}

	public void releaseYearChanged(int releaseYear)
	{
        if(releaseYear == 0)
			yearText.setValue("");
		else
			yearText.setValue(releaseYear);
	}

	public void trackArtistChanged(String trackArtistName)
	{
		if (trackArtistName != null && !trackArtistName.isEmpty())
		{
			artistNameLabelText.setValue("Artist");
			artistNameText.setValue(trackArtistName);
		}
		else
		{
			artistNameLabelText.setValue("");
			artistNameText.setValue("");
		}
	}

	public void timeElapsedChanged(long msElapsed, long msDuration, double fractionComplete)
	{
		// the current track is playing. Update the progress bar, if we know the length of the track.
		if(this.progressBar != null) 
		{
			// TODO: move this?
			this.progressBar.setDuration(msDuration);

			// Set elapsed, which updates the elapsed label and the progress bar position.
			if (msDuration > 0)
				this.progressBar.setElapsed(msElapsed, fractionComplete);
		}
	}

	public void repeatChanged(boolean repeat)
	{
		if(repeat)
			this.repeatModeText.setValue("On");
		else
			this.repeatModeText.setValue("Off");
		
		nextTrackChanged(app.getDiscJockey().getNextTrack());
	}

	public void shuffleChanged(boolean shuffle)
	{
		if(shuffle)
			this.shuffleModeText.setValue("On");
		else
			this.shuffleModeText.setValue("Off");
		
		nextTrackChanged(app.getDiscJockey().getNextTrack());
	}

	/**
	 * Restores screen and background image. This cannot be implemented in a handleExit() method
	 * because, we may exit this screen to the screensaver (which doesn't use the standard background).
	 *
	 */
	private void pop() 
	{
		this.app.setBackgroundImage();
		this.app.pop();
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
	 */
	@Override
	public boolean handleEnter(Object arg, boolean isReturn) 
	{
		boolean status = super.handleEnter(arg, isReturn);
		
		// Set the background when entering the screen
		
		//	constants used for screen layout
		double screenHeight	= this.app.getHeight();
		double screenWidth	= this.app.getWidth();
		double aspectRatio	= screenWidth / screenHeight;
			
		// Display the background image
		if( this.app.isInSimulator() ) {
			// We are in the simulator, so set a PNG background.
			
			// Change the background based on the new aspect ratio.
			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) 
			{
				// The current aspect ratio is approximately 16:9. 
				// Use the high definition background meant for 720p.
				String url = this.getContext().getBaseURI().toString();
				
				try {url += URLEncoder.encode("now_playing_720.png", "UTF-8");}
				catch(UnsupportedEncodingException e) {}
				
				this.app.getRoot().setResource(this.createStream(url, "image/png", true));
			}
			else 
			{
				// Default background is standard definition 640 x 480 (4:3)
				this.app.getRoot().setResource("now_playing_sd.png");
			}
		}
		else 
		{
			// We are running on a real TiVo, so use an MPEG background to conserve memory.
			
			// Change the background based on the new aspect ratio.
			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) 
			{
				// The current aspect ratio is approximately 16:9. 
				// Use the high definition background meant for 720p.		
				this.app.getRoot().setResource("now_playing_720.mpg");
			}
			else 
			{
				// Default background is standard definition 640 x 480 (4:3)
				this.app.getRoot().setResource("now_playing_sd.mpg");
			}
		}
		
		return status;
	}

	/* (non-Javadoc)
	 * Handles key presses from TiVo remote control.
	 */
	@Override
	public boolean handleKeyPress(int key, long rawcode)
	{
		if (key == KEY_CLEAR) 
		{
			this.app.setInactive();
			return true;
		}
			
		this.app.checkKeyPressToResetInactivityTimer(key);
		
		switch(key) 
		{
			case KEY_INFO:
				pop();
				BrowsePlaylistScreen bps;
				BScreen s = app.getCurrentScreen();
				if (s instanceof BrowsePlaylistScreen) 
				{
					bps = (BrowsePlaylistScreen)s;
					if(bps.isNowPlayingPlaylist()) 
					{
						bps.focusNowPlaying();
						return true;
					}
				}
				bps = new BrowsePlaylistScreen(app);
				app.push(bps, TRANSITION_LEFT);
				bps.focusNowPlaying();
				return true;
			case KEY_LEFT:
				if (this.app.getDiscJockey().isSeeking())
					this.app.play("bonk.snd");
				else
					this.pop();
				return true;
	
			case KEY_FORWARD:
				this.app.getDiscJockey().fastForward();
				return true;
				
			case KEY_REVERSE:
				this.app.getDiscJockey().rewind();
				return true;
				
			case KEY_PLAY:
				this.app.getDiscJockey().playNormalSpeed();
				return true;
				
			case KEY_PAUSE:
				this.app.getDiscJockey().togglePause();
				return true;
				
			case KEY_CHANNELUP:
	
				if (this.app.getDiscJockey().playNext())
					this.app.play("pageup.snd");
				else
					this.app.play("bonk.snd");
				
				return true;
				
			case KEY_CHANNELDOWN:
				
				if (this.app.getDiscJockey().playPrevious())
					this.app.play("pagedown.snd");
				else
					this.app.play("bonk.snd");
	
				return true;
				
			case KEY_REPLAY:
				this.app.play("select.snd");
				this.app.getDiscJockey().toggleRepeatMode();
				return true;
				
			case KEY_ADVANCE:
				this.app.play("select.snd");
				this.app.getDiscJockey().toggleShuffleMode();
				return true;
		}
		
		return super.handleKeyPress(key, rawcode);
	}


	/* (non-Javadoc)
	 * Handles TiVo events
	 */

	private class ProgressBar extends BView 
	{
		private BView trackingBar;
		private String elapsedLabel = "0:00";
		private BText elapsedText = new BText( this, 0, 0, 0, this.getHeight() );
		private BText durationText = new BText( this, this.getWidth(), 0, 0, this.getHeight() );
		private Resource.FontResource font;
		private HmeEvent.FontInfo fontInfo;
		private long durationMS;
		
		/**
		 * A bar that tracks the elapsed time of a stream. The height of the bar is dependent on the font sizej chosen
		 * for text.
		 * 
		 * @param parent		container for this ProgressBar
		 * @param x				X coordinate of top-left corner of this ProgressBar
		 * @param y				Y coordinate of top-left corner of this ProgressBar
		 * @param width			width of this ProgressBar
		 * @param timeLength	length of time this ProgressBar tracks in milliseconds
		 * @param fontSize		the font to use for text in the ProgressBar
		 */
		public ProgressBar(NowPlayingScreen parent, int x, int y, int width, int fontSize) 
		{
			super(parent.getNormal(), x, y, width, 0, false);
			
			// Create font. Use FONT_METRIC_BASIC flag so that we get font metrics.
			this.font = (Resource.FontResource) createFont( "default.ttf", FONT_PLAIN, fontSize, FONT_METRICS_BASIC | FONT_METRICS_GLYPH);
			this.elapsedText.setValue(elapsedLabel);
			this.elapsedText.setFont(this.font);
			this.durationText.setFont(this.font);
			this.font.addHandler(this);
			parent.progressBar = this;
			
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.tivo.hme.bananas.BView#handleEvent(com.tivo.hme.sdk.HmeEvent)
		 */
		@Override
		public boolean handleEvent(HmeEvent event) {

			switch (event.getOpCode()) {
            	
            case EVT_FONT_INFO:
                this.fontInfo = (HmeEvent.FontInfo) event;
   
                // Resize the height of this ProgressBar to fit the font
                this.setBounds( this.getX(), this.getY(), this.getWidth(), (int)this.fontInfo.getAscent() );
    			
    			// Format the zero label
    			int zeroWidth = fontInfo.measureTextWidth(this.elapsedLabel);
    			this.elapsedText.setBounds(0, 0, zeroWidth, this.getHeight() );
    			
    			// Create the tracking bar
    			int trackingBarHeight = (int) this.getHeight() / 2;
                this.trackingBar = new BView(	this, 
                								this.elapsedText.getWidth() + (int)fontInfo.getGlyphInfo('l').getAdvance(), 
                								(int)( (this.getHeight() - trackingBarHeight) / 2 ), 
                								0, 
                								trackingBarHeight
                );
    			this.trackingBar.setResource(HSkin.NTSC_WHITE);
                
    			// Make the progress bar visible
    			this.setVisible(true);
    			
                return true;
            }
            return super.handleEvent(event);
        }
		
		/**
		 * Sets the position of the tracking bar
		 * 
		 * @param position	a double value between 0 and 1 representing the fraction of the stream that has played
		 */
		private boolean setPosition(double position) 
		{
			if( (position < 0) || (position > 1) )
				return false;
			
			// reset width of tracking bar based on position
			this.trackingBar.setBounds(	this.trackingBar.getX(), 
										this.trackingBar.getY(), 
										(int) ( ( this.getWidth() - this.elapsedText.getWidth() - this.durationText.getWidth() - ( 2 * (int)fontInfo.getGlyphInfo('l').getAdvance() ) ) * position ),
										this.trackingBar.getHeight()
			);
			
			return true;
		}
		
		/**
		 * Sets the duration label of this ProgressBar and resizes views to fit.
		 * 
		 * @param label
		 * @return
		 */
		public synchronized void setDuration(long milliseconds) 
		{
			// If the duration is already set, or if we don'tyet know our font looks like, do nothing.  We'll get another update.
			if (durationMS == milliseconds || fontInfo == null)
				return;
			
			String label = millisecondsToTimeString(milliseconds);
			int labelWidth = fontInfo.measureTextWidth(label);
			
			durationMS = milliseconds;
			
			// Resize the width of this ProgressBar to fit the labels
			if(this.getWidth() < labelWidth) {
				this.setBounds( this.getX(), this.getY(), labelWidth * 2, this.getHeight() );
			}
			this.trackingBar.setLocation(labelWidth + (int)fontInfo.getGlyphInfo('l').getAdvance(), this.trackingBar.getY());
			
			// Re-size the time labels and set their text
            this.durationText.setBounds(this.getWidth() - labelWidth, 0, labelWidth, this.getHeight());
            
            if (milliseconds > 0)
                this.durationText.setValue(label);
            else
            {
                this.elapsedText.setValue("");
                this.durationText.setValue("");
                setPosition(0);
            }

            this.elapsedText.setBounds(0, 0, labelWidth, this.getHeight() );
		}
		
		public double setElapsed(long elapsedMS, double fractionComplete)
		{
			this.elapsedText.setValue(millisecondsToTimeString(elapsedMS));
			setPosition(fractionComplete);
			return fractionComplete;
		}
		
		private String millisecondsToTimeString(long milliseconds)
		{
			int minutes = (int)(milliseconds / 60000);
			int seconds = (int)((milliseconds % 60000) / 1000);
			String secondsLabel = String.format("%02d", seconds);
			return minutes + ":" + secondsLabel;
		}
	}
}
