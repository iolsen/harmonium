package org.dazeend.harmonium.screens;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.dazeend.harmonium.DiscJockeyListener;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.PlayRate;
import org.dazeend.harmonium.music.ArtSource;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlayableTrack;

import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;


public class ScreenSaverScreen extends HManagedResourceScreen implements DiscJockeyListener 
{

	Harmonium app;
	Resource oldBackground = null;
	
	private BView albumArtView;
	private Timer moveTimer;

	public ScreenSaverScreen(Harmonium app) {
		super(app);
		doNotFreeResourcesOnExit();
		this.app = app;

		int screenWidth 	= app.getWidth();
//		int screenHeight 	= app.getHeight();
		int safeTitleH	= app.getSafeTitleHorizontal();
//		int safeTitleV	= app.getSafeTitleVertical();
//		int hIndent = (int)( ( screenWidth - (2 * safeTitleH) ) * 0.01 );
//		int vIndent = (int)( ( screenHeight - (2 * safeTitleV) ) * 0.01 );

		int albumInfoRowHeight = app.hSkin.paragraphFontSize + (app.hSkin.paragraphFontSize / 4);
				
		// Create views for album art. Size is square, sides less of 480px or half title-safe width
		// (640x480 is the maximum image size that TiVo can load.)
		final int artSide = Math.min(480,(screenWidth - (2 * safeTitleH) ) / 2 );
		
		// Define the y-coordinate for album art so that the info is verticaly centered in the screen
		int albumArtViewY = ( this.getHeight() - ( artSide + (3 * albumInfoRowHeight) ) ) / 2;
		this.albumArtView = new BView( this.getNormal(), safeTitleH, albumArtViewY, artSide, artSide);
	}
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
	 */
	@Override
	public boolean handleEnter(Object arg0, boolean arg1) {
		boolean status = super.handleEnter(arg0, arg1);
		
		View rootView = this.app.getRoot();
		
		// Store old background
		if (oldBackground != null)
		{
			if (oldBackground != rootView.getResource())
			{
				oldBackground.flush();
				oldBackground.remove();
				oldBackground = null;
			}
		}
		
		if (oldBackground == null)
			this.oldBackground = rootView.getResource();
		
		// Set the background to black when entering the screen
		rootView.setResource("screensaver.mpg");

		moveTimer = new Timer();
		moveTimer.schedule(new MoveArtCheckTimerTask(this), 7000, 7000);
		
		if (app.getDiscJockey().isPlaying())
		{
			Playable nowPlaying = app.getDiscJockey().getNowPlaying();
			if (nowPlaying != null)
				artChanged(nowPlaying);
		}

		return status;
	}

	public void moveAlbumArt()
	{
		int screenWidth 	= app.getWidth();
		//int screenHeight 	= app.getHeight();
		int safeTitleH	= app.getSafeTitleHorizontal();
		int safeTitleV	= app.getSafeTitleVertical();
		int artSide = Math.min(480,(screenWidth - (2 * safeTitleH) ) / 2 );
		
		//max X value for album art is total width, minus unusable space on right, minus width of the album art
		//We dont know the width of the album art so we have to assume worst case scenario.
		int maxX = app.getWidth()-safeTitleH-artSide;
		int minX = safeTitleH;
		//max X value for album art should be total height, minus unusable space on the bottom, minus the height of the album art
		//We dont know the height of the album art so we have to assume worst case scenario.
		int maxY = app.getHeight()-safeTitleV-artSide;
		int minY = safeTitleV;
		
		Random genCoord = new Random();

		int xPosition = genCoord.nextInt(maxX-minX) + minX;
		int yPosition = genCoord.nextInt(maxY-minY) + minY;

		this.albumArtView.setLocation(xPosition,yPosition);
	}
	

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleExit()
	 */
	@Override
	public boolean handleExit() {
		// restore background image
		this.app.getRoot().setResource(this.oldBackground);
		moveTimer.cancel();
		return super.handleExit();
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int key, long rawcode) {

		if(key == KEY_VOLUMEUP || key == KEY_VOLUMEDOWN || key == KEY_MUTE || key == KEY_CLEAR)
			return true;

		this.app.checkKeyPressToResetInactivityTimer(key);
		
		// pop the screensaver on any keypress that doesn't control volume
		this.app.pop();
		
		// Forward this keypress event to the screen under the screensaver.
		return this.app.getCurrentScreen().handleKeyPress(key, rawcode);
	}

	private class MoveArtCheckTimerTask extends TimerTask {

		private ScreenSaverScreen handler;
		
		public MoveArtCheckTimerTask(ScreenSaverScreen handler) {
			this.handler = handler;
		}
		
		@Override
		public void run() {
			handler.moveAlbumArt();
		}
	}

	public void artChanged(final ArtSource artSource)
	{
		app.getRoot().setPainting(false);

		try 
    		{
	       		// Update views with new info
	    		new Thread() 
	    		{
	    			public void run() 
	    			{
	    				if (artSource.hasAlbumArt(app.getFactoryPreferences()))
	    				{
				    		ImageResource albumArtImage = createManagedImage(artSource, albumArtView.getWidth(), albumArtView.getHeight());
				    		setManagedResource(albumArtView, albumArtImage, RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
	    				}
	    				else
	    				{
	    					// Don't display the cheesy art placeholder on the screen saver.
	    					albumArtView.setResource(null);
	    				}
			    		flush(); // Necessay to ensure UI updates, because we're in another thread.
	    			}
	    		}.start();

	    	}
	    	finally 
	    	{
	    		app.getRoot().setPainting(true);
	    	}
	}

	public void nextTrackChanged(PlayableTrack nextTrack)
	{
		// Do nothing
	}

	public void nowPlayingChanged(final Playable nowPlaying)
	{
		artChanged(nowPlaying);
	}

	public void playRateChanging(PlayRate newPlayRate)
	{
		// Do nothing
	}

	public void repeatChanged(boolean repeat)
	{
		// Do nothing
	}

	public void shuffleChanged(boolean shuffle)
	{
		// Do nothing
	}

	public void timeElapsedChanged(long msElapsed, long msDuration, double fractionComplete)
	{
		// Do nothing
	}

	public void trackTitleChanged(String title)
	{
		// Do nothing
	}
}
