package org.dazeend.harmonium.music;

import java.io.IOException;
import java.util.List;

public abstract class EditablePlaylist implements PlaylistEligible
{
	protected List<Playable> _tracks;

	public abstract void save() throws IOException;

	/**
	 * Moves a playlist member to another location in the list. Does NOT save the change.
	 * 
	 * @param from	the index of the item that will move
	 * @param to	the index the item will move to
	 */
	public void move(int from, int to) throws IllegalArgumentException {
		
		// validate item to move
		if( from < 0 || from > (this._tracks.size() - 1) ) {
			throw new IllegalArgumentException();
		}
		
		// validate location moving to
		if( to < 0 || to > (this._tracks.size() - 1) ) {
			throw new IllegalArgumentException();
		}
		
		if(to < from) {
			// Moving item closer to beginning of list
			Playable temp = this._tracks.get(from);
			
			int i;
			for(i = from; i > to; --i) {
				this._tracks.set(i, this._tracks.get(i-1));
			}
			
			this._tracks.set(i, temp);
		}
		else if(to > from) {
			// Moving item closer to end of list
			Playable temp = this._tracks.get(from);
			
			int i;
			for(i = from; i < to; ++i) {
				this._tracks.set(i, this._tracks.get(i+1));
			}
			this._tracks.set(i, temp);
		}

	}

	/**
	 * Removes a given member from this playlist. Does NOT save the change.
	 * 
	 * @param i the index of the member to remove
	 * @throws IllegalArgumentException
	 */
	public Playable remove(int i) throws IllegalArgumentException {
		if( i < 0 || i >= this._tracks.size() ) {
			throw new IllegalArgumentException();
		}
		return this._tracks.remove(i);
	}
}
