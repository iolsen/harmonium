package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BKeyboard;
import com.tivo.hme.bananas.BView;

// Harmonium standard definition keyboard.
// Does nothing but reset the inactivity timer on keypresses.
public class HSDKeyboard extends BKeyboard
{
	private Harmonium app;
	
	public HSDKeyboard(Harmonium app, BView parent, int x, int y, int width, int height)
	{
		super(parent, x, y, width, height);
		this.app = app;
	}

	public HSDKeyboard(Harmonium app, BView parent, int x, int y, int width, int height, int keyboardType, boolean tips)
	{
		super(parent, x, y, width, height, keyboardType, tips);
		this.app = app;
	}

	public HSDKeyboard(Harmonium app, BView parent, int x, int y, int width, int height, Keyboard keyboard, boolean tips,
			int textEntryWidth, boolean visible)
	{
		super(parent, x, y, width, height, keyboard, tips, textEntryWidth, visible);
		this.app = app;
	}

	@Override
	public boolean handleKeyPress(int code, long rawcode)
	{
		if (app != null)
			app.checkKeyPressToResetInactivityTimer(code);
		return super.handleKeyPress(code, rawcode);
	}

}
