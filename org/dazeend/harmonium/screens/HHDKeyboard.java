package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.Harmonium;

import com.almilli.tivo.bananas.hd.HDKeyboard;
import com.tivo.hme.bananas.BView;

//Harmonium high definition keyboard.
//Does nothing but reset the inactivity timer on keypresses.
public class HHDKeyboard extends HDKeyboard
{
	private Harmonium app;

	public HHDKeyboard(Harmonium app, BView parent, int x, int y, int width, int height)
	{
		super(parent, x, y, width, height);
		this.app = app;
	}

	public HHDKeyboard(Harmonium app, BView parent, int x, int y, int width, int height, int keyboardType, boolean tips)
	{
		super(parent, x, y, width, height, keyboardType, tips);
		this.app = app;
	}

	public HHDKeyboard(Harmonium app, BView parent, int x, int y, int width, int height, Keyboard keyboard, boolean tips,
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
