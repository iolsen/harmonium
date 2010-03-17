package org.dazeend.harmonium.screens;

import java.awt.Color;

import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public class ScreenSaverScreen extends BScreen {

	Harmonium app;
	Resource oldBackground = null;

	public ScreenSaverScreen(Harmonium app) {
		super(app);
		this.app = app;
		this.getAbove().setResource(Color.black);
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
		
		return status;
	}
	

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BScreen#handleExit()
	 */
	@Override
	public boolean handleExit() {
		// restore background image
		this.app.getRoot().setResource(this.oldBackground);
		
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
}
