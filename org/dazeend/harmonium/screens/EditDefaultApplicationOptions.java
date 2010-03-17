package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;

public class EditDefaultApplicationOptions extends HScreen {

	private BButton screenSaverButton;
	private BButton OKButton;
	private ScreenSaverSetting sss;

	private static final String OK_LABEL = "Set Options";
	private static final String SS_DECREASE_ACTION = "screenSaverDecrease";
	private static final String SS_INCREASE_ACTION = "screenSaverIncrease";
	
	
	/**
	 * @param app
	 * @param title
	 */
	public EditDefaultApplicationOptions(Harmonium app) {
		super(app, "Application Options");	
		
		this.app = app;
		
		// Create screensaver label
		BText screenSaverText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4, 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		screenSaverText.setFont(this.app.hSkin.barFont);
		screenSaverText.setColor(HSkin.NTSC_WHITE);
		screenSaverText.setValue("Screen Blanking:");
		screenSaverText.setFlags(RSRC_HALIGN_LEFT);
		screenSaverText.setVisible(true);
		setManagedView(screenSaverText);
		
		// Create screensaver button
		int buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		
		this.screenSaverButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											buttonWidth,
											this.rowHeight
		);
		
		sss = new ScreenSaverSetting(app, screenSaverButton, app.getPreferences().screenSaverDelay());
		
		this.screenSaverButton.setFocusable(true);
		this.setFocusDefault(this.screenSaverButton);
		
		// Create OK button
		this.OKButton = new BButton(this.getNormal(),										// Put list on "normal" level
									this.safeTitleH,										// x coord. of button
									this.screenHeight - this.safeTitleV - this.rowHeight, 	// y coord - Align with bottom of screen
									(this.screenWidth - (2 * this.safeTitleH)) / 2, 		// width of button (half screen)
									this.rowHeight											// height of list
		);
		this.OKButton.setResource(createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, OK_LABEL));
		this.OKButton.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, H_UP, null, true);
		this.OKButton.setFocusable(true);
	}
	
	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int code, long rawcode) {
		
		this.app.checkKeyPressToResetInactivityTimer(code);
		
		if(this.getFocus() != null && this.getFocus().equals(this.OKButton) && code == KEY_SELECT) {
			// set screen saver delay preference
			this.app.getPreferences().setScreenSaverDelay(sss.getCurrentValue());
			this.app.updateScreenSaverDelay();
			this.app.pop();
			
			return true;
		}
		else if(code == KEY_LEFT) {
			// Catch the left key press to prevent the screen from popping when on an option button
			return true;
		}
		return super.handleKeyPress(code, rawcode);
	}
	
	/* (non-Javadoc)
	 * @see org.dazeend.harmonium.screens.HScreen#handleAction(com.tivo.hme.bananas.BView, java.lang.Object)
	 */
	@Override
	public boolean handleAction(BView view, Object action) {
		if(action.equals(SS_DECREASE_ACTION)) {
			this.app.play("updown.snd");
			sss.decrease();
		}
		else if(action.equals(SS_INCREASE_ACTION)) {
			this.app.play("updown.snd");
			sss.increase();
		}
		return super.handleAction(view, action);
	}

	private class ScreenSaverSetting {
		
		private int _currentIndex;
		private BButton _button;		
		private Resource[] _labels = new Resource[6];
		private int[] _values = new int[6];
		
		public ScreenSaverSetting(Harmonium app, BButton button, int value) {
			
			_button = button;

			_labels[0] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "Off");
			_values[0] = 0;
			_labels[1] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "20 seconds");
			_values[1] = 20000;
			_labels[2] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "1 minute");
			_values[2] = 60000;
			_labels[3] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "5 minutes");
			_values[3] = 300000;
			_labels[4] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "10 minutes");
			_values[4] = 600000;
			_labels[5] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "20 minutes");
			_values[5] = 1200000;
			
			_currentIndex = -1;
			for (int i = 0; i < _values.length; i++)
			{
				if (_values[i] == value) {
					_currentIndex = i;
					break;
				}
			}
			if (_currentIndex == -1)
				_currentIndex = 3;
			
			updateButton();
		}
		
		private void updateButton() {
			_button.setResource(_labels[_currentIndex]);
			
			if (_currentIndex == 0)
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, SS_INCREASE_ACTION, null, H_DOWN, true);
			else if (_currentIndex == 5)
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, SS_DECREASE_ACTION, null, null, H_DOWN, true);
			else
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, SS_DECREASE_ACTION, SS_INCREASE_ACTION, null, H_DOWN, true);

			_button.getHighlights().refresh();
		}
		
		public synchronized void increase() {
			_currentIndex++;
			updateButton();
		}
		
		public synchronized void decrease() {
			_currentIndex--;
			updateButton();
		}
		
		public int getCurrentValue() {
			return _values[_currentIndex];
		}
	}
}
