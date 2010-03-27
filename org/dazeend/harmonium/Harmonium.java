package org.dazeend.harmonium;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.LinkedList;

import javazoom.spi.mpeg.sampled.file.tag.IcyInputStream;

import org.blinkenlights.jid3.ID3Exception;
import org.dazeend.harmonium.music.ArtSource;
import org.dazeend.harmonium.music.MP3File;
import org.dazeend.harmonium.music.MusicCollection;
import org.dazeend.harmonium.screens.ExitScreen;
import org.dazeend.harmonium.screens.HManagedResourceScreen;
import org.dazeend.harmonium.screens.MainMenuScreen;
import org.dazeend.harmonium.screens.NowPlayingScreen;

import com.almilli.tivo.bananas.hd.HDApplication;
import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.interfaces.IArgumentList;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.sdk.Factory;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resource;

public class Harmonium extends HDApplication {
	
	// public hSkin used to hold common text attributes
	public HSkin hSkin;
	
	// The context for this application
	private IContext context;
	
	// Application preferences
	private ApplicationPreferences preferences;
	
	// This application's DiscJockey
	private DiscJockey discJockey = DiscJockey.getDiscJockey(this);
	
	// Tracks user inactivity for displaying the Now Playing screen and the screensaver.
	private InactivityHandler inactivityHandler;
	
	private AlbumArtCache albumArtCache = AlbumArtCache.getAlbumArtCache(this);
	
	private NowPlayingScreen nowPlayingScreen;	
	
	// Are we in the simulator?
	private boolean inSimulator = false;
	
	private Harmonium app;

	private String _requestedStream;
	
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BApplicationPlus#init(com.tivo.hme.interfaces.IContext)
	 */
	@Override
	public void init(IContext context) throws Exception {
		this.context = context;
		this.app = this;
		super.init(context);
	}
	
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BApplicationPlus#init()
	 */
	@Override
	protected void initService() {
		
		// At this point the resolution has been set
		super.initService();
		
		// get application preferences
		this.preferences = new ApplicationPreferences(this.context);
		
		// Initialize skin
		this.hSkin = new HSkin(this);
		
		// Refresh the music list
		new Thread() {
			public void run() {
				MusicCollection.getMusicCollection(getHFactory()).refresh(app);
			}
		}.start();
		
		// Load the main menu and background
		MainMenuScreen mainMenuScreen = new MainMenuScreen( this, MusicCollection.getMusicCollection(this.getHFactory()) );
		this.setBackgroundImage();
		this.push(mainMenuScreen, TRANSITION_NONE);	

		// Instantiate the inactivity handler.
		inactivityHandler = new InactivityHandler(this);
}

	/**
	 * @return the discJockey
	 */
	public DiscJockey getDiscJockey() {
		return this.discJockey;
	}

	/**
	 * Gets the HarmoniumFactory for this Harmonium
	 * 
	 * @return
	 */
	public HarmoniumFactory getHFactory() {
		return (HarmoniumFactory)this.getFactory(); 
	}
	
	
	/**
	 * @return the Application preferences
	 */
	public ApplicationPreferences getPreferences() {
		return this.preferences;
	}

	public FactoryPreferences getFactoryPreferences() {
		return ((HarmoniumFactory)(this.getFactory())).getPreferences();
	}
	
	/**
	 * @return the inSimulator
	 */
	public boolean isInSimulator() {
		return inSimulator;
	}
	
	public boolean isInDebugMode() {
		return getHFactory().getPreferences().inDebugMode();
	}

	public final boolean ignoreEmbeddedArt()
	{
		return getHFactory().getPreferences().ignoreEmbeddedArt();
	}

	public final boolean ignoreJpgFileArt()
	{
		return getHFactory().getPreferences().ignoreJpgFileArt();
	}

	public final boolean preferJpgFileArt()
	{
		return getHFactory().getPreferences().preferJpgFileArt();
	}
	
	/**
	 * Sets the background image based on the current resolution.
	 *
	 */
	public void setBackgroundImage() {
		
		// If we are in the simulator, set a PNG background.
		double screenHeight = this.getHeight();
		double screenWidth = this.getWidth();
		double aspectRatio = screenWidth / screenHeight;
			
		if(this.inSimulator) {
			// We are in the simulator, so set a PNG background.
			
			// Change the background based on the new aspect ratio.
			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) {
				// The current aspect ratio is approximately 16:9. 
				// Use the high definition background meant for 720p.
				String url = this.getContext().getBaseURI().toString();
				try {
					url += URLEncoder.encode("background_720.png", "UTF-8");
				}
				catch(UnsupportedEncodingException e) {
				}
				getRoot().setResource(this.createStream(url, "image/png", true));
			}
			else {
				// Default background is standard definition 640 x 480 (4:3)
				getRoot().setResource("background_sd.png");
			}
		}
		else {
			// We are running on a real TiVo, so use an MPEG background to conserve memory.
			
			// Change the background based on the new aspect ratio.
			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) {
				// The current aspect ratio is approximately 16:9. 
				// Use the high definition background meant for 720p.		
				getRoot().setResource("background_720.mpg");
			}
			else {
				// Default background is standard definition 640 x 480 (4:3)
				getRoot().setResource("background_sd.mpg");
			}
		}
	}

	public void checkKeyPressToResetInactivityTimer(int key) {
		// Reset inactivity on non-volume keys.
		if (key != KEY_MUTE && key != KEY_VOLUMEDOWN && key != KEY_VOLUMEUP)
			inactivityHandler.resetInactivityTimer();
	}
	
	public void setInactive()
	{
		inactivityHandler.setInactive();
	}
	
	public void resetInactivityTimer()
	{
		if (inactivityHandler != null)
			inactivityHandler.resetInactivityTimer();
	}
	
	public void updateScreenSaverDelay()
	{
		inactivityHandler.updateScreenSaverDelay();
	}

	public AlbumArtCache getAlbumArtCache() {
		return albumArtCache;
	}
	
	public void pushNowPlayingScreen()
	{
		// push Now Playing Screen
		if(this.nowPlayingScreen == null) 
		{
			this.nowPlayingScreen = new NowPlayingScreen(this);
			discJockey.addListener(this.nowPlayingScreen);
		}

		this.app.push(this.nowPlayingScreen, TRANSITION_NONE);
	}
	
	public NowPlayingScreen getNowPlayingScreen()
	{
		return nowPlayingScreen;
	}

	/* (non-Javadoc)
	 * Handles key presses from TiVo remote control.
	 */
	@Override
	public boolean handleKeyPress(int key, long rawcode) {
		
		checkKeyPressToResetInactivityTimer(key);
		
		switch(key) {
		case KEY_LEFT:
			if(this.getStackDepth() <= 1) {
				
				if( this.getDiscJockey().isPlaying() ) {
					this.push(new ExitScreen(this), TRANSITION_LEFT);
				}
				else {
					// Exit
					this.setActive(false);
				}
			}
			else {
				this.pop();
			}
			return true;
		case KEY_PAUSE:
			this.getDiscJockey().togglePause();
			return true;
		case KEY_INFO:
			// Jump to the Now Playing Screen if there is music playing
			if((nowPlayingScreen != null) && this.getDiscJockey().isPlaying() && (this.getCurrentScreen().getClass() != NowPlayingScreen.class))
			{
				this.push(nowPlayingScreen, TRANSITION_LEFT);
				return true;
			}
			else{
				this.play("bonk.snd");
			}
		}
		
		return super.handleKeyPress(key, rawcode);

	}

	/**
	 * Handles TiVo events
	 */
	@Override
	public boolean handleEvent(HmeEvent event) 
	{
		boolean result = super.handleEvent(event);
		
		if (event.getClass() == HmeEvent.DeviceInfo.class) 
		{
			// This event tells us what kind of TiVo is running our app.
			HmeEvent.DeviceInfo deviceInfo = (HmeEvent.DeviceInfo) event;
			
			// If we are running on the simulator, we need to change the background
			String platform = (String)deviceInfo.getMap().get("platform");
			if(platform != null && platform.startsWith("sim-")) {
				
				// notify the app that we are in the simulator
				this.inSimulator = true;
				
				// Set background image
				this.setBackgroundImage();
			}
		}

		return result;
	}

	public void setLastRequestedStream(String uri)
	{
		String lowerUri = uri.toLowerCase();
		if (lowerUri.startsWith("http://"))
			_requestedStream = uri;
	}

	public String getLastRequestedStream()
	{
		return _requestedStream;
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.sdk.Application#handleIdle(boolean)
	 */
	@Override
	public boolean handleIdle(boolean isIdle) {
		
		if(isIdle) {

			if (this.app.isInDebugMode()){
				System.out.println("INACTIVITY DEBUG: Tivo sent idle event.");
				System.out.flush();
			}
			
			inactivityHandler.checkIfInactive();

			// tell the receiver that we handled the idle event
			this.acknowledgeIdle(true);
		}
		
		return true;
	}
	
	@Override
	public void pop()
	{
		BScreen currentScreen = getCurrentScreen();
		super.pop();
		
		// Now that the screen has been popped, it's safe to clean up its resources.
		// If it's a screen that knows how to clean up after itself, ask it to do so.
		if (currentScreen instanceof HManagedResourceScreen)
			((HManagedResourceScreen)currentScreen).cleanup();
	}

	/**
	 * Server side factory.
	 */
	public static class HarmoniumFactory extends Factory {
		
		private final static String VERSION = "0.8 ({REV})";

		private FactoryPreferences preferences;
		private final Hashtable<String, Long> _durationTable = new Hashtable<String, Long>();

		/**
		 *  Create the factory. Reads preferences and initialized data structures.
		 *  Run when server-side application begins execution.
		 */
		@Override
		protected void init(IArgumentList args) {
			
			// print out stack traces on error and exceptions
			try {
				// See if all we want is version information
				if(args.getBoolean("-version")) {
					System.out.println(HarmoniumFactory.VERSION);
					System.out.flush();
					System.exit(0);
				}
				
				// Read factory preferences from disk.
				this.preferences = new FactoryPreferences(args);
				
				// Create the music collection
				MusicCollection.getMusicCollection(this);
			}
			catch(Error e) {
				e.printStackTrace();
				throw e;
			}
			catch(RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

	     /**
		 * @return the preferences
		 */
		public FactoryPreferences getPreferences() {
			return this.preferences;
		}
		
		/**
		 * @return the VERSION
		 */
		public static String getVersion() {
			return VERSION;
		}

    	private void addTrackDuration(String uri, long duration)
    	{
    		_durationTable.put(uri, duration);
    	}
    	
        @Override
		protected long getMP3Duration(String uri)
		{
        	return _durationTable.remove(uri);
		}

        private Harmonium getAppThatRequestedStream(String requestedUri)
        {
			for (int i = 0; i < active.size(); i++) 
			{
				Harmonium app = (Harmonium)active.elementAt(i);
				String lastStreamUri = app.getLastRequestedStream();
				if (lastStreamUri != null && lastStreamUri.equals(requestedUri))
					return app;
			}
			return null;
        }
        
	    /* (non-Javadoc)
         * @see com.tivo.hme.sdk.MP3Factory#getMP3StreamFromURI(java.lang.String)
         */
		public InputStream getStream(String uri) throws IOException 
        {
			String lowerUri = uri.toLowerCase();
			if (lowerUri.startsWith("http://"))
			{
				System.out.println("Fetching MP3 stream for playback: " + uri);
				
				Harmonium app = getAppThatRequestedStream(uri);
				
                try
                {
    	            URL url = new URL(uri);
    	            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    	            conn.setInstanceFollowRedirects(true);

                    conn.setRequestProperty("Icy-Metadata", "1");
                    conn.setRequestProperty("User-Agent", "WinampMPEG/5.0");
                    conn.setRequestProperty("Accept", "audio/mpeg");

                    InputStream inputStream = conn.getInputStream();

                    IcyInputStream icyInputStream = new IcyInputStream(inputStream);
                    if (app != null)
                    	icyInputStream.addTagParseListener(app.getDiscJockey());

    	            return new BufferedInputStream(icyInputStream, 102400);
                }
                catch (Throwable t)
                {
                	if (preferences.inDebugMode())
                		t.printStackTrace();
                	System.out.println("Not an Icy stream.  Re-opening without icy listener...");
                }

	            URL url = new URL(uri);
	            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	            conn.setInstanceFollowRedirects(true);

                conn.setRequestProperty("Accept", "audio/mpeg");

                InputStream inputStream = conn.getInputStream();

	            return new BufferedInputStream(inputStream, 102400);
}
			else if (lowerUri.endsWith(".mp3"))
			{
	            File file = new File(MusicCollection.getMusicCollection(this).getMusicRoot(), URLDecoder.decode(uri, "UTF-8"));
	            if (file.exists()) 
	            {
					System.out.println("Fetching MP3 file for playback: " + uri);

					try
					{
		            	MP3File mp3file = new MP3File(file.getPath(), file);
		            	addTrackDuration(uri, mp3file.getDuration());
					} 
					catch (ID3Exception e)
					{
						e.printStackTrace();
					}

		            InputStream in = new FileInputStream(file);
	                return in;
	            }
			}

			return super.getStream(uri);
        }
	}

	public static class AlbumArtCache {
		
		private static final int CACHE_SIZE = 15;
		
		private class ArtCacheItem {
			private int _hash;
			private ImageResource _resource;
			
			public ArtCacheItem(int hash, ImageResource resource) {
				_hash = hash;
				_resource = resource;
			}
			
			public int getHash() { return _hash; }
			public ImageResource getResource() { return _resource; }
		}
		
		private Harmonium _app;
		private LinkedList<ArtCacheItem> _managedImageList;
		private Hashtable<Integer, ArtCacheItem> _managedImageHashtable;
		private Hashtable<Resource, Boolean> _managedResourceHashtable;
				
		// Disallow instantation by another class.
		private AlbumArtCache(Harmonium app) {
			_app = app;
			_managedImageList = new LinkedList<ArtCacheItem>();
			_managedImageHashtable = new Hashtable<Integer, ArtCacheItem>(CACHE_SIZE);
			_managedResourceHashtable = new Hashtable<Resource, Boolean>(CACHE_SIZE);
		}
		
		public synchronized static AlbumArtCache getAlbumArtCache(Harmonium app) {
			AlbumArtCache cache = app.getAlbumArtCache(); 
			if (app.getAlbumArtCache() == null)
				cache = new AlbumArtCache(app);
			return cache;
		}
		
		public synchronized ImageResource Add(BScreen screen, ArtSource artSource, int width, int height) {
			
			// Hash a bunch of album attributes together to uniquely identify the album.
			//
			// The first version of this actually hashed the bytes that make up the image, but that was really
			// slow, and it didn't save the image scaling step because images of different sizes all need to be 
			// separately cached.  In hindsight, this was a major waste of time I should have predicted.  :)  
			// Anyway this hash is imperfect, but dead simple and therefore pretty doggone fast.  If you
			// have different cover art images embedded in files of the same album, we'll always display
			// the first one we cache.  But I think that's probably unusual.  And I like how fast this is.
			int hash = 0;
			if (artSource.hasAlbumArt(_app.getFactoryPreferences()))
				hash = (artSource.getArtHashKey() + width + height).hashCode();
					
			ArtCacheItem aci = _managedImageHashtable.get(hash);
			if (aci == null) {
				if (_app.isInSimulator()) {
					System.out.println("album art cache miss: " + hash);

					try {
						Thread.sleep(500); // better simulate performance of real Tivo.
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (hash != 0)
				{
					// When the receiver chokes on album art for some reason, this is where we die.  But no exception is thrown,
					// Application closes itself.  And changing this doesn't seem to be an improvement.  The failure leaves
					// things in a weird state such that subsequent songs fail to play, etc.  I think it's better to let it 
					// fail in a deterministic way, so at least it's relatively easy to identify the offending music.
					aci = new ArtCacheItem(hash, screen.createImage(artSource.getScaledAlbumArt(_app.getFactoryPreferences(), width, height)));
				}
				else
					aci = new ArtCacheItem(hash, screen.createImage("default_album_art2.png"));
				
				if (_managedImageList.size() == CACHE_SIZE) {

					// We're going to add this image resource to the cache, but the cache is at its maximum size.
					// So we remove the item at the end of the list, which is the "stalest" item: the one accessed  
					// the longest ago.

					ArtCacheItem removeItem = _managedImageList.removeLast();
					Resource removeResource = removeItem.getResource();
					_managedImageHashtable.remove(removeItem.getHash());
					_managedResourceHashtable.remove(removeResource);
					removeResource.remove();
				}
				
				// Add this image resource to the front of the list, signifying that it's the most recently accessed.
				_managedImageList.addFirst(aci);
				_managedImageHashtable.put(hash, aci);
				_managedResourceHashtable.put(aci.getResource(), false);
			}
			else {
				if (_app.isInSimulator())
					System.out.println("album art cache hit: " + hash);
				
				// Found the item in the cache.  Move it to the front of the list to indicate it's the most recently accessed.
				_managedImageList.remove(aci);
				_managedImageList.addFirst(aci);
			}
			return aci.getResource();
		}
		
		public Boolean Contains(Resource r) {
			return _managedResourceHashtable.containsKey(r);
		}
	}

}
