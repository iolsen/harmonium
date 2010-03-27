package org.dazeend.harmonium;

import org.dazeend.harmonium.music.ArtSource;
import org.dazeend.harmonium.music.Playable;
import org.dazeend.harmonium.music.PlayableTrack;

public interface DiscJockeyListener
{
	public void nowPlayingChanged(final Playable nowPlaying);
	public void nextTrackChanged(final PlayableTrack nextTrack);
	
	public void artChanged(final ArtSource artSource);
	public void trackTitleChanged(final String title);
	
	public void shuffleChanged(final boolean shuffle);
	public void repeatChanged(final boolean repeat);

	public void timeElapsedChanged(final long msElapsed, long msDuration, double fractionComplete);
	
	/**
	 * DiscJockey calls this just before the play rate changes to newPlayRate.
	 * 
	 *  TODO maybe.  It's not yet clear if this has do be done before the rate change in order for the system sound to play.
	 */
	public void playRateChanging(final PlayRate newPlayRate);
}
