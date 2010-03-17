package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;

public class EditDefaultTrackOptions extends HScreen {

	private BButton repeatButton;
	private BButton OKButton;
	
	private Resource trueButtonLabel;
	private Resource falseButtonLabel;
	
	private static final String OK_LABEL = "Set Options";
	
	/**
	 * @param app
	 * @param title
	 */
	public EditDefaultTrackOptions(Harmonium app) {
		super(app, "Track Options");
		
		this.app = app;
									
		this.trueButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "On");
		this.falseButtonLabel = createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, "Off");
		
		// Create repeat label
		BText repeatText = new BText(	this, 
										this.safeTitleH, 
										this.screenHeight / 4, 
										( this.screenWidth - (2 * this.safeTitleH) ) / 2,
										this.rowHeight
		);	
		repeatText.setFont(this.app.hSkin.barFont);
		repeatText.setColor(HSkin.NTSC_WHITE);
		repeatText.setValue("Repeat Mode:");
		repeatText.setFlags(RSRC_HALIGN_LEFT);
		repeatText.setVisible(true);
		setManagedView(repeatText);
		
		// Create repeat button
		int buttonWidth = ( this.screenWidth - (2 * this.safeTitleH) ) / 3;
		
		this.repeatButton = new BButton(	this,
											3 * ( this.screenWidth - (2 * this.safeTitleH) ) / 4,
											this.screenHeight / 4,
											buttonWidth,
											this.rowHeight
		);
		
		if(this.app.getPreferences().getTrackDefaultRepeatMode()) {
			this.repeatButton.setResource(this.trueButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "repeatToFalse", null, null, H_DOWN, true);
		}
		else {
			this.repeatButton.setResource(this.falseButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "repeatToTrue", null, H_DOWN, true);
		}
		this.repeatButton.setFocusable(true);
		this.setFocusDefault(this.repeatButton);
		
		
		
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
			// The OKButton has been selected. save the selected options.
			boolean repeatMode;
			
			// Find the selected repeat mode
			if(this.repeatButton.getResource().equals(this.trueButtonLabel)) {
				repeatMode = true;
			}
			else {
				repeatMode = false;
			}
			
			// set Preferences
			this.app.getPreferences().setTrackDefaultRepeatMode(repeatMode);
			
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
		if(action.equals("repeatToFalse")) {
			this.app.play("updown.snd");
			this.repeatButton.setResource(this.falseButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, "repeatToTrue", null, H_DOWN, true);
			this.repeatButton.getHighlights().refresh();
		}
		else if(action.equals("repeatToTrue")) {
			this.app.play("updown.snd");
			this.repeatButton.setResource(this.trueButtonLabel);
			this.repeatButton.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, "repeatToFalse", null, null, H_DOWN, true);
			this.repeatButton.getHighlights().refresh();
		}
			
		return super.handleAction(view, action);
	}
}
