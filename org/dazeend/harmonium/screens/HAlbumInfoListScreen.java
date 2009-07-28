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
 
package org.dazeend.harmonium.screens;

import java.util.ArrayList;
import java.util.List;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.AlbumReadable;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resource;

/**
 * A screen that displays album info for a music collection item and lists its contents below.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public class HAlbumInfoListScreen extends HSkipListScreen {

	protected HSkipList list;				// the list in this screen
	protected int	rowHeight;				// the height of each row in the list
	protected BView albumArtView;			// the view that contains the album art
	protected BText albumNameText;			// the text that displays the album name
	protected BText albumArtistText;		// the text that displays the album artist
	protected BText yearText;				// the text that displays the release year for the album
	protected BView albumArtBGView;			// the view behind the album art. Used for crossfades.
	protected BText albumNameBGText;
	protected BText albumArtistBGText;
	protected BText yearBGText;
	
	public HAlbumInfoListScreen(Harmonium app, AlbumReadable musicItem, String title) {
		this(app, title);
		
		if(app.getHFactory().getPreferences().inDebugMode()) {
			System.out.println("DEBUG: Initializing album info");
			System.out.flush();
		}
		
		this.initAlbumInfo(musicItem);
	}

	
	/**
	 * @param app
	 */
	public HAlbumInfoListScreen(Harmonium app, String title) {
		super( app, title );
		
		this.app = app;
		
		// Define rowHeight to fit 8 rows in the body of the screen
		this.rowHeight = (int)(((0.75 * this.screenHeight) - this.safeTitleV)/8);
		
		// Define an indents that corresponds to 1% of the width/height of the title-safe area
		int hIndent = (int)( ( this.screenWidth - (2 * this.safeTitleH) ) * 0.01 );
		int vIndent = (int)( ( this.screenHeight - (2 * this.safeTitleV) ) * 0.01 );
		
		// Set up album info area that is the height of three list rows.
		BView	albumInfo = new BView(	this.getNormal(),							// Put album info on "normal" layer of screen
										this.safeTitleH + hIndent,					// x coord of album info view
										(int)(this.screenHeight * 0.25) + vIndent,	// y coord of album info view
										(this.screenWidth - (2 * this.safeTitleH)),	// width of album info view
										(this.rowHeight * 3) - vIndent				// Use 3/8 of remaining height for album info
		);

		// Create album art views
		this.albumArtBGView = new BView( albumInfo, 0, 0, albumInfo.getHeight(), albumInfo.getHeight() );
		this.albumArtView = new BView( albumInfo, 0, 0, albumInfo.getHeight(), albumInfo.getHeight() );
		
		// Define constants to make layout easier
		int paraFontSize = app.hSkin.paragraphFontSize;
		int paraLabelFontSize = app.hSkin.paragraphLabelFontSize;
		int paraHeight = ( paraFontSize * this.albumArtView.getHeight() ) / ( (3 * paraFontSize) + (3 * paraLabelFontSize) );
		int paraLabelHeight = ( paraLabelFontSize * this.albumArtView.getHeight() ) / ( (3 * paraFontSize) + (3 * paraLabelFontSize) );
		int leftEdgeCoord = this.albumArtView.getWidth() + hIndent;
		int textWidth = albumInfo.getWidth() - this.albumArtView.getWidth();
		
		// Create BG text for crossfade
		albumNameBGText = new BText(	albumInfo, 		// parent
										leftEdgeCoord,	// x coord relative to parent
										0, 				// y coord relative to parent
										textWidth,		// width
										paraHeight		// height
		);
		albumNameBGText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		albumNameBGText.setShadow(false);
		albumNameBGText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM);
		albumNameBGText.setFont(app.hSkin.paragraphFont);

		// Put album name info in album art view
		albumNameText = new BText(	albumInfo, 		// parent
									leftEdgeCoord,	// x coord relative to parent
									0, 				// y coord relative to parent
									textWidth,		// width
									paraHeight		// height
		);
		albumNameText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		albumNameText.setShadow(false);
		albumNameText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM);
		albumNameText.setFont(app.hSkin.paragraphFont);
	

		// Put in album name label
		BText albumNameLabelText = new BText(	albumInfo,
												leftEdgeCoord,
												paraHeight,
												textWidth,
												paraLabelHeight
		);
		albumNameLabelText.setColor(HSkin.PARAGRAPH_LABEL_TEXT_COLOR);
		albumNameLabelText.setShadow(false);
		albumNameLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
		albumNameLabelText.setFont(app.hSkin.paragraphLabelFont);
		albumNameLabelText.setValue("Album");
												
		// Create BG text for crossfade
		albumArtistBGText = new BText(	albumInfo,						// parent
										leftEdgeCoord,					// x coord relative to parent	
										paraHeight + paraLabelHeight,	// y coord relative to parent
										textWidth,						// width
										paraHeight						// height
		);
		albumArtistBGText.setFont(app.hSkin.paragraphFont);
		albumArtistBGText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		albumArtistBGText.setShadow(false);
		albumArtistBGText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM);
		
		// Put album artist name in album art view
		albumArtistText = new BText(	albumInfo,						// parent
										leftEdgeCoord,					// x coord relative to parent	
										paraHeight + paraLabelHeight,	// y coord relative to parent
										textWidth,						// width
										paraHeight						// height
		);
		albumArtistText.setFont(app.hSkin.paragraphFont);
		albumArtistText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		albumArtistText.setShadow(false);
		albumArtistText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM);
	
		
		// Put in album artist label
		BText albumArtistNameLabelText = new BText(	albumInfo,							// parent
													leftEdgeCoord,						// x coord relative to parent
													(2 * paraHeight) + paraLabelHeight,	// y coord relative to parent
													textWidth,							// width
													paraLabelHeight						// height
		);
		albumArtistNameLabelText.setColor(HSkin.PARAGRAPH_LABEL_TEXT_COLOR);
		albumArtistNameLabelText.setShadow(false);
		albumArtistNameLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
		albumArtistNameLabelText.setFont(app.hSkin.paragraphLabelFont);
		albumArtistNameLabelText.setValue("Album Artist");
		
		// Create BG text for crossfade
		yearBGText = new BText(	albumInfo,									// parent
								leftEdgeCoord,								// x coord relative to parent	
								(2 * paraHeight) + (2 * paraLabelHeight),	// y coord relative to parent
								textWidth,									// width
								paraHeight									// height
		);
		yearBGText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		yearBGText.setShadow(false);
		yearBGText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM);
		yearBGText.setFont(app.hSkin.paragraphFont);
		
		// Put year in album art view
		yearText = new BText(	albumInfo,									// parent
								leftEdgeCoord,								// x coord relative to parent	
								(2 * paraHeight) + (2 * paraLabelHeight),	// y coord relative to parent
								textWidth,									// width
								paraHeight									// height
		);
		yearText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
		yearText.setShadow(false);
		yearText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM);
		yearText.setFont(app.hSkin.paragraphFont);
	
		
		// Put in year label
		BText yearLabelText = new BText(	albumInfo,									// parent
											leftEdgeCoord,								// x coord relative to parent
											(3 * paraHeight) + (2 * paraLabelHeight),	// y coord relative to parent
											textWidth,									// width
											paraLabelHeight								// height
		);
		yearLabelText.setColor(HSkin.PARAGRAPH_LABEL_TEXT_COLOR);
		yearLabelText.setShadow(false);
		yearLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
		yearLabelText.setFont(app.hSkin.paragraphLabelFont);
		yearLabelText.setValue("Year");

		// Set up list for contents of musicItem
		list = new HSkipList(	this.getNormal(), 								// Put list on "normal" level
							safeTitleH , 									// x coord. of list
							screenHeight - safeTitleV - (rowHeight * 5), 	// y coord. of list
							(screenWidth - (2 * safeTitleH)), 				// width of list (full screen)
							rowHeight * 5,									// height of list (5/8  of body). Defined in terms of row height to ensure that height is an even multiple or rowheight.
							rowHeight										// row height
		);
		setFocusDefault(list);
	}
	

	protected void initAlbumInfo(final AlbumReadable musicItem) {
		
		this.albumNameText.setValue(musicItem.getAlbumName());
		this.albumArtistText.setValue(musicItem.getAlbumArtistName());
		
		if(musicItem.getReleaseYear() == 0) {
			yearText.setValue("");
		}
		else {
			yearText.setValue(musicItem.getReleaseYear());
		}
		
		// Load images via a separate thread
		new Thread() {
			public void run() {
				loadImage(musicItem);
				flush(); // Necessary to ensure the image shows up, because we're in another thread.
			}
		}.start();
	}
	
	/**
	 * loads album art into album info area at top of screen
	 *
	 */
	private void loadImage(AlbumReadable musicItem) {
		ImageResource albumArtImage;
		if(musicItem.hasAlbumArt()) {
			// Scale image to less of 640x480 or size of albumArtView. 
			// (640x480 is the maximum image size that TiVo can load.)
			int artHeight = Math.min(this.albumArtView.getHeight(), 480);
			int artWidth = Math.min(this.albumArtView.getWidth(), 640);
    		albumArtImage = createManagedImage( musicItem.getScaledAlbumArt(artHeight, artWidth) );
		}
		else {
			// default_album_art.gif has known dimensions of 300x300, so no scaling is needed
			albumArtImage = createManagedImage("default_album_art.gif");
		}
		
		this.setManagedResource(albumArtView, albumArtImage, RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
	}
	
	/**
	 * Creates a list that updates the album art in a view.
	 * 
	 * @author Charles Perry (harmonium@DazeEnd.org)
	 *
	 */
	protected class HAlbumArtList extends HSkipList {
		
		BView	albumArtView;			// the view that contains the album art
		BView 	albumArtBGView;		// the background view used for crossfading
		BText 	albumNameText;
		BText 	albumNameBGText;
		BText 	albumArtistText;
		BText 	albumArtistBGText;
		BText 	yearText;
		BText 	yearBGText;
		List<ImageResource> imageCache = new ArrayList<ImageResource>();
		Boolean imageCacheBuilt = false;
		
		private AlbumReadable oldMusicItem;
		
		HAlbumArtList(	BView parent, 
						int x, 
						int y, 
						int width, 
						int height, 
						int rowHeight, 
						BView foreground, 
						BView background,
						BText albumNameText,
						BText albumNameBGText,
						BText albumArtistText,
						BText albumArtistBGText,
						BText yearText,
						BText yearBGText
						) {
			
            super(parent, x, y, width, height, rowHeight);
            this.albumArtView = foreground;
            this.albumArtBGView = background;
            this.albumNameText = albumNameText;
			this.albumNameBGText = albumNameBGText;
			this.albumArtistText = albumArtistText;
			this.albumArtistBGText = albumArtistBGText;
			this.yearText = yearText;
			this.yearBGText = yearBGText;
			
        }
		
		 /**
	     * Whenever the focused row changes handleFocus gets called twice, 
	     * once for the row that loses focus and once for the row 
	     * that gains focus
	     */
	    public boolean handleFocus(boolean isGained, BView gained, BView lost) {    
	     try {
	    	 if (isGained) {

        		final AlbumReadable newMusicItem = (AlbumReadable)this.get( this.getFocus() );

		    	// When used in a playlist, the album art may or may not be different from the row that just lost focus.
	        	// Here we store the artist and album of the row losing focus, so we can compare when we regain focus
		    	// and do nothing if the album hasn't changed.  This prevents the "flicker" of crossfading to the same image.
		        if (oldMusicItem == null || 
	        		oldMusicItem.getAlbumName().compareToIgnoreCase(newMusicItem.getAlbumName()) != 0 || 
	        		oldMusicItem.getAlbumArtistName().compareToIgnoreCase(newMusicItem.getAlbumArtistName()) != 0) {
			    	
		            oldMusicItem = newMusicItem;
		            
					updateInfo(newMusicItem, getFocus());
	        	}
	        }
	     }
	     catch(Exception e) {
	    	 e.printStackTrace();
	     }

	        return super.handleFocus(isGained, gained, lost);
	    }
	    
	    private void updateInfo(final AlbumReadable newMusicItem, final int focusItem)
	    {
            // turn off painting while we update the images
        	app.getRoot().setPainting(false);
        	
        	try {
        		// define animation for fade
	        	Resource anim = getResource("*500");
	        	
        		// Move the current art to the background
	        	setManagedResource(this.albumArtBGView, this.albumArtView.getResource(), RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
       
        		// Put the new image in the foreground, preferably using cached image
        		if( imageCacheBuilt || imageCache.size() > focusItem ) {
        			// Cached image is available: use it.
        			setManagedResource(this.albumArtView, this.imageCache.get(focusItem), RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
        		}
        		else {
					new Thread()
					{
						public void run()
						{
							// The image isn't yet in the cache, so fetch it ourselves in another thread.  This looks like it has the potential 
							// to screw up the animation below, and it sometimes does, but it seems to give the best performance perception: you 
							// can move through this list with decent speed even if the cache is still loading and music is playing.
		        			setManagedResource(albumArtView, getAlbumImage((AlbumReadable)get(focusItem)), RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
		        			flush();
						}
					}.start();
        		}
        		
	    		// Perform the crossfade between images using an animation to match highlight movement
	    		this.albumArtView.setTransparency(1.0f);		// start transparent, then fade IN
	            this.albumArtView.setTransparency(0.0f, anim);
	            this.albumArtBGView.setTransparency(0.0f);		// start opaque, then fade OUT
	            this.albumArtBGView.setTransparency(1.0f, anim);

	            // Copy text to background text items
	            this.albumNameBGText.setValue(albumNameText.getValue());
	            this.albumArtistBGText.setValue(albumArtistText.getValue());
	            this.yearBGText.setValue(yearText.getValue());
	            
	            // Update text in foreground text elements
	            this.albumNameText.setValue(newMusicItem.getAlbumName());
	            this.albumArtistText.setValue(newMusicItem.getAlbumArtistName());
	            if(newMusicItem.getReleaseYear() == 0) {
	    			yearText.setValue("");
	    		}
	    		else {
	    			yearText.setValue(newMusicItem.getReleaseYear());
	    		}
	            
	            // Fade out background elements
	            this.albumNameBGText.setTransparency(0.0f);
	            this.albumNameBGText.setTransparency(1.0f, anim);
	            this.albumArtistBGText.setTransparency(0.0f);
	            this.albumArtistBGText.setTransparency(1.0f, anim);
	            this.yearBGText.setTransparency(0.0f);
	            this.yearBGText.setTransparency(1.0f, anim);
	            
	            // Fade in foreground elements
	            this.albumNameText.setTransparency(1.0f);
	            this.albumNameText.setTransparency(0.0f, anim);
	            this.albumArtistText.setTransparency(1.0f);
	            this.albumArtistText.setTransparency(0.0f, anim);
	            this.yearText.setTransparency(1.0f);
	            this.yearText.setTransparency(0.0f, anim);
        	}
        	finally {
                app.getRoot().setPainting(true);
            }
	    }
	    
	    protected synchronized void initImageCache(Boolean build)
	    {
	    	if (build)
	    		buildImageCache();
	    	else
	    		resetImageCache();	
	    }
	    
	    private synchronized void resetImageCache() {
	    	new Thread()
	    	{
	    		public void run()
	    		{
	    	    	lockManagedResources();
	    	    	try {
	    	    		
	    	    		if (!imageCacheBuilt)
	    	    			return;
	    	    		
	    				imageCacheBuilt = false;
	    				if (app.isInSimulator())
	    					System.out.println("imageCacheBuilt = false");
	    				imageCache.clear();
	    			} finally {
	    				unlockManagedResources();
	    			}
	    		}
	    	}.start();
	    }
	    
	    /**
	     * Caches the album art for list items.
	     * 
	     */
		private synchronized void buildImageCache() {	
			
			new Thread()
			{
				public void run()
				{
					lockManagedResources();
					try {
						
						if (imageCacheBuilt)
							return;

						assert(imageCache.size() == 0);
						for(int i = 0; i < size(); ++i) {
							AlbumReadable musicItem = (AlbumReadable)get(i);
							imageCache.add(getAlbumImage(musicItem));
						}
	    				if (app.isInSimulator())
	    					System.out.println("imageCacheBuilt = true");
						imageCacheBuilt = true;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						unlockManagedResources();
					}
				}
			}.start();
		}
		
		private ImageResource getAlbumImage(AlbumReadable musicItem) {
			
			if (app.isInSimulator())
				try {
					Thread.sleep(250); // better simulate performance of real Tivo.
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			if(musicItem.hasAlbumArt()) {
				// Scale image to less of 640x480 or size of albumArtView. 
				// (640x480 is the maximum image size that TiVo can load.)
				int artHeight = Math.min(this.albumArtView.getHeight(), 480);
				int artWidth = Math.min(this.albumArtView.getWidth(), 640);
				if ( artHeight > 0 && artWidth > 0 )
					return createManagedImage(musicItem.getScaledAlbumArt(artHeight, artWidth));
			}

			// default_album_art.gif has known dimensions of 300x300, so no scaling is needed
			return createManagedImage("default_album_art.gif");
		}
	}

}
