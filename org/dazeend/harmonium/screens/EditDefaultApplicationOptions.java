package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.ApplicationPreferences;
import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;

public class EditDefaultApplicationOptions extends HScreen {

	private BButton screenSaverDelayButton;
	private BButton screenSaverTypeButton;
	private BButton OKButton;
	private ScreenSaverDelaySetting delaySetting;
	private ScreenSaverTypeSetting typeSetting;

	private static final String OK_LABEL = "Set Options";
	private static final String SSD_DECREASE_ACTION = "screenSaverDelayDecrease";
	private static final String SSD_INCREASE_ACTION = "screenSaverDelayIncrease";
	private static final String SST_DECREASE_ACTION = "screenSaverTypeDecrease";
	private static final String SST_INCREASE_ACTION = "screenSaverTypeIncrease";
	
	
	/**
	 * @param app
	 * @param title
	 */
	public EditDefaultApplicationOptions(Harmonium app) {
		super(app, "Application Options");	
		
		this.app = app;
		
		// Create screensaver delay label
		BText screenSaverDelayText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4, 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		screenSaverDelayText.setFont(this.app.hSkin.barFont);
		screenSaverDelayText.setColor(HSkin.NTSC_WHITE);
		screenSaverDelayText.setValue("Screen Saver:");
		screenSaverDelayText.setFlags(RSRC_HALIGN_LEFT);
		screenSaverDelayText.setVisible(true);
		setManagedView(screenSaverDelayText);
		
		// Create screensaver delay button
		int buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		
		this.screenSaverDelayButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											buttonWidth,
											this.rowHeight
		);
		delaySetting = new ScreenSaverDelaySetting(app, screenSaverDelayButton, app.getPreferences().screenSaverDelay());
		this.screenSaverDelayButton.setFocusable(true);
		this.setFocusDefault(this.screenSaverDelayButton);

		// Create screensaver type label
		BText screenSaverTypeText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4 + (2 * this.rowHeight), 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		screenSaverTypeText.setFont(this.app.hSkin.barFont);
		screenSaverTypeText.setColor(HSkin.NTSC_WHITE);
		screenSaverTypeText.setValue("Screen Saver Type:");
		screenSaverTypeText.setFlags(RSRC_HALIGN_LEFT);
		screenSaverTypeText.setVisible(true);
		setManagedView(screenSaverTypeText);

		// Create screensaver type button
		//buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		this.screenSaverTypeButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4 + (2 * this.rowHeight),
											buttonWidth,
											this.rowHeight
		);
		typeSetting = new ScreenSaverTypeSetting(app, screenSaverTypeButton, app.getPreferences().getScreenSaverType());
		this.screenSaverTypeButton.setFocusable(true);

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
			this.app.getPreferences().setScreenSaverDelay(delaySetting.getCurrentValue());
			this.app.updateScreenSaverDelay();
			this.app.getPreferences().setScreenSaverType(typeSetting.getCurrentValue());
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
		if(action.equals(SSD_DECREASE_ACTION)) {
			this.app.play("updown.snd");
			delaySetting.decrease();
		}
		else if(action.equals(SSD_INCREASE_ACTION)) {
			this.app.play("updown.snd");
			delaySetting.increase();
		}
		else if (action.equals(SST_DECREASE_ACTION)) {
			this.app.play("updown.snd");
			typeSetting.decrease();
		}
		else if (action.equals(SST_INCREASE_ACTION)) {
			this.app.play("updown.snd");
			typeSetting.increase();
		}
		return super.handleAction(view, action);
	}

	private class ScreenSaverDelaySetting {
		
		private int _currentIndex;
		private BButton _button;		
		private Resource[] _labels = new Resource[6];
		private int[] _values = new int[6];
		
		public ScreenSaverDelaySetting(Harmonium app, BButton button, int value) {
			
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
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, SSD_INCREASE_ACTION, null, H_DOWN, true);
			else if (_currentIndex == 5)
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, SSD_DECREASE_ACTION, null, null, H_DOWN, true);
			else
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, SSD_DECREASE_ACTION, SSD_INCREASE_ACTION, null, H_DOWN, true);

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

	private class ScreenSaverTypeSetting {
		
		private int _currentIndex;
		private BButton _button;		
		private Resource[] _labels = new Resource[2];
		private String[] _values = new String[2];
		
		public ScreenSaverTypeSetting(Harmonium app, BButton button, String value) {
			
			_button = button;

			_labels[0] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "Blank");
			_values[0] = ApplicationPreferences.SCREENSAVER_TYPE_BLANK;
			_labels[1] = createText(app.hSkin.barFont, HSkin.NTSC_WHITE, "Album Art");
			_values[1] = ApplicationPreferences.SCREENSAVER_TYPE_ART_ONLY;
			
			_currentIndex = -1;
			for (int i = 0; i < _values.length; i++)
			{
				if (_values[i] == value) {
					_currentIndex = i;
					break;
				}
			}
			if (_currentIndex == -1)
				_currentIndex = 0;
			
			updateButton();
		}
		
		private void updateButton() {
			_button.setResource(_labels[_currentIndex]);
			
			if (_currentIndex == 0)
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, SST_INCREASE_ACTION, H_UP, H_DOWN, true);
			else if (_currentIndex == 1)
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, SST_DECREASE_ACTION, null, H_UP, H_DOWN, true);
			else
				_button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, SST_DECREASE_ACTION, SST_INCREASE_ACTION, H_UP, H_DOWN, true);

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
		
		public String getCurrentValue() {
			return _values[_currentIndex];
		}
	}

}
