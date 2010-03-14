package org.dazeend.harmonium.music;

import org.dazeend.harmonium.screens.NowPlayingScreen;

public interface PlayableRateChangeable
{
	/**
	 * Sets the play speed for an already playing track.
	 */
	public boolean setPlayRate(NowPlayingScreen nowPlayingScreen, float speed);
}
