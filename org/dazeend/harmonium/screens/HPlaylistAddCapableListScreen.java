package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.music.PlayableCollection;
import com.tivo.hme.bananas.BText;

public abstract class HPlaylistAddCapableListScreen extends HSkipListScreen
{
	private boolean _enableAddToPlaylist;
	
	protected HPlaylistAddCapableListScreen(Harmonium app, String title)
	{
		this(app, title, true);
	}
	
	protected HPlaylistAddCapableListScreen(Harmonium app, String title, boolean enableAddToPlaylist)
	{
		super(app, title);
		_enableAddToPlaylist = enableAddToPlaylist;
		
		int noteY;
		if (this.screenHeight == 480) //SD
			noteY = 452;
		else // HD
			noteY = this.screenHeight - 38;
		
		if (enableAddToPlaylist) {
			// Add a note to the bottom of the screen
			BText enterNote = new BText(this.getNormal(), this.safeTitleH, noteY, 
				this.screenWidth - (2 * this.safeTitleH), this.app.hSkin.paragraphFontSize);
			enterNote.setFont(app.hSkin.paragraphFont);
			enterNote.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
			enterNote.setFlags(RSRC_HALIGN_CENTER + RSRC_VALIGN_BOTTOM);
			enterNote.setValue("press ENTER to add selected item to a playlist");
			setManagedView(enterNote);
		}
	}
	
	@Override
	public boolean handleKeyPress(int key, long rawcode)
	{
		if (_enableAddToPlaylist && key == KEY_ENTER) {
			this.app.play("select.snd");
			int selectedIndex = this.list.getFocus();
			PlayableCollection toPlay = (PlayableCollection) this.list.get(selectedIndex);
			this.app.push(new AddToPlaylistScreen(this.app, toPlay), TRANSITION_LEFT);
			return true;
		}
		return super.handleKeyPress(key, rawcode);
	}

}
