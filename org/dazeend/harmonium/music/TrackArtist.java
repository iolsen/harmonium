package org.dazeend.harmonium.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dazeend.harmonium.FactoryPreferences;
import org.dazeend.harmonium.Harmonium;

public class TrackArtist extends BaseArtist
{
	public TrackArtist(String trackArtistName) {
		super(trackArtistName);
	}

	@Override
	public boolean addTrack(FactoryPreferences prefs, PlayableLocalTrack newTrack)
	{
		// Check to ensure that the newTrack is eligible to be a member of this track artist.
		if( _artistName.compareToIgnoreCase(newTrack.getArtistName()) != 0 ) {
			return false;
		}
		
		// Check to ensure that the newTrack is not already a member of this track artist.
		if(this._trackList.contains(newTrack)) {
			return false;
		}

		// Add track to artist.
		if(this._trackList.add(newTrack)) {
			// The track was successfully added. Return TRUE.
			return true;
		}
		else {
			// there was an error in adding the track
			return false;
		}
	}

	@Override
	public List<PlayableLocalTrack> getMembers(Harmonium app)
	{
		List<PlayableLocalTrack> outputList = new ArrayList<PlayableLocalTrack>();
 		
 		// Get tracks that are direct members of this album artist
 		List<PlayableLocalTrack> sortedTrackList = new ArrayList<PlayableLocalTrack>();
		sortedTrackList.addAll(_trackList);
		
		if(app != null) {
			Collections.sort(sortedTrackList, app.getPreferences().getAlbumTrackComparator());
		}

		outputList.addAll(sortedTrackList);
		
		return outputList;
	}

	@Override
	public void removeTrack(PlayableLocalTrack track)
	{
		_trackList.remove(track);
	}
}
