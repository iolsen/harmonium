package org.dazeend.harmonium;

import org.dazeend.harmonium.music.ArtSource;
import org.dazeend.harmonium.music.PlayableTrack;

public interface DiscJockeyListener
{
	public void beginUpdate();
	public void endUpdate();
	
	public void nextTrackChanged(final PlayableTrack nextTrack);
	public void artChanged(final ArtSource artSource);
	public void trackNameChanged(final String trackName);
	public void albumChanged(final String albumName, final int DiscNumber);
	public void releaseYearChanged(final int releaseYear);
	public void trackArtistChanged(final String trackArtistName);
	public void albumArtistChanged(final String albumArtistName);
	
	public void timeElapsedChanged(final long msElapsed, long msDuration, double fractionComplete);

	public void shuffleChanged(final boolean shuffle);
	public void repeatChanged(final boolean repeat);

	/**
	 * DiscJockey calls this just before the play rate changes to newPlayRate.
	 */
	public void playRateChanging(final PlayRate newPlayRate);
}
