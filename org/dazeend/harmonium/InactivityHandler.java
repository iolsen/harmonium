package org.dazeend.harmonium;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.dazeend.harmonium.screens.NowPlayingScreen;
import org.dazeend.harmonium.screens.ScreenSaverScreen;

import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.IBananas;

public final class InactivityHandler {
	
	// If no notable key is pressed for this many milliseconds, we go into the inactive state.
	private static int inactivityMilliseconds;
	
	private Harmonium app;
	private Timer idleTimer = new Timer();
	private Date lastActivityDate;
	private boolean inactive = false;
	private ScreenSaverScreen screenSaverScreen;
	
	public InactivityHandler(Harmonium app) 
	{
		this.app = app;

		screenSaverScreen = new ScreenSaverScreen(this.app);
		app.getDiscJockey().addListener(screenSaverScreen);

		updateScreenSaverDelay();
		lastActivityDate = new Date();
		idleTimer.schedule(new InactiveCheckTimerTask(this), 10000, 10000);
	}
	
	/**
	 * Call this any time a notable key (e.g. not volume) is pressed to reset the inactivity timer. 
	 */
	public synchronized void resetInactivityTimer(){
		
		lastActivityDate = new Date();
		
		if (this.app.isInDebugMode())
			System.out.println("INACTIVITY DEBUG: Resetting inactivity timer: " + lastActivityDate);

		if (inactive) {
			if (this.app.isInDebugMode()) {
				System.out.println("INACTIVITY DEBUG: Setting inactivity state: ACTIVE.");
				System.out.flush();
			}
			inactive = false;
		}
	}

	public synchronized void checkIfInactive() {
		if (inactive)
			return;
		
		if (this.app.isInDebugMode())
			System.out.println("INACTIVITY DEBUG: Checking if inactive.");

		Date rightNow = new Date();
		long timeInactive = rightNow.getTime() - lastActivityDate.getTime(); 
		if (this.app.isInDebugMode())
			System.out.println("INACTIVITY DEBUG: No user activity for " + timeInactive + " ms.");

		if ( timeInactive >= 20000) {
			
			// If we've been inactive, but there's music playing and we're not on the Now Playing screen,
			// go to the Now Playing screen.  Next time we go inactive we'll enable the screen saver.
			if (this.app.getDiscJockey().isPlaying() && (this.app.getCurrentScreen().getClass() != NowPlayingScreen.class) 
					&& this.app.getNowPlayingScreen() != null) {
				
				if (this.app.isInDebugMode())
					System.out.println("INACTIVITY DEBUG: Resetting and pushing now playing screen.");

				resetInactivityTimer();
				this.app.push(this.app.getNowPlayingScreen(), IBananas.TRANSITION_LEFT);
				return;
			}
			
			if (inactivityMilliseconds != 0 && timeInactive >= inactivityMilliseconds) {

				if (this.app.isInDebugMode())
					System.out.println("INACTIVITY DEBUG: Inactive for long enough.");

				setInactive();
			}
			else
			{
				if (this.app.isInDebugMode())
					System.out.println("INACTIVITY DEBUG: Not inactive for long enough.");
			}
		}
		else
		{
			if (this.app.isInDebugMode())
				System.out.println("INACTIVITY DEBUG: Inactive for less than 20 seconds.  Nothing to do.");
		}
	}

	/**
	 * Called when the inactivity timer elapses and via Harmonium class for HME's idle event.
	 */
	public synchronized void setInactive()	{
		if (inactive)
			return;
		inactive = true;

		if (this.app.isInDebugMode()){
			System.out.println("INACTIVITY DEBUG: Setting inactivity state: INACTIVE.");
			System.out.flush();
		}
		
		// Start the screen saver.
		BScreen currentScreen = app.getCurrentScreen();
		if (currentScreen != screenSaverScreen) {
			app.push(screenSaverScreen, IBananas.TRANSITION_FADE);
			app.flush();
		}
	}
	
	private class InactiveCheckTimerTask extends TimerTask {

		private InactivityHandler handler;
		
		public InactiveCheckTimerTask(InactivityHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public void run() {
			handler.checkIfInactive();
		}
	}

	public void updateScreenSaverDelay()
	{
		inactivityMilliseconds = app.getPreferences().screenSaverDelay();
		if (this.app.isInDebugMode()){
			System.out.println("INACTIVITY DEBUG: Updating screen saver delay: " + inactivityMilliseconds);
			System.out.flush();
		}
	}
}
