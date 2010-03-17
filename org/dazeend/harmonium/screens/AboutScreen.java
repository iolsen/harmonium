package org.dazeend.harmonium.screens;

import org.dazeend.harmonium.screens.HScrollPane;
import org.dazeend.harmonium.HSkin;
import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.Harmonium.HarmoniumFactory;
import com.tivo.hme.bananas.BText;

import com.tivo.hme.bananas.BButton;


public class AboutScreen extends HScreen {

	private BButton OKButton;
	private static final String OK_LABEL = "Return to Main Menu";
	private String message;
	private HScrollPane scrollPane;

	/**
	 * @param app
	 * @param title
	 */
	public AboutScreen(Harmonium app) {
		super(app, "About Harmonium");

		this.app = app;
		this.message = "Harmonium Music Player\nVersion: " + HarmoniumFactory.getVersion() + "\nCopyright (C) 2008-2010  Charles Perry, Ian Olsen\n\nHarmonium is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.\n\nYou should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.\n\nIf you would like the source code for Harmonium, you can download it from <http://code.google.com/p/harmonium/>.";

		// Create text message view
		this.scrollPane = new HScrollPane(	this.getNormal(),
											this.safeTitleH,
											this.screenHeight / 4,
											this.screenWidth - (2 * this.safeTitleH),
											this.rowHeight * 6
		);


		BText messageText = new BText(	this.scrollPane,
										0,
										0,
										this.scrollPane.getWidth(),
										(int)(this.rowHeight * 8.5)
		);
		messageText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
		messageText.setFont(this.app.hSkin.paragraphFont);
		messageText.setColor(HSkin.NTSC_WHITE);
		messageText.setValue(this.message);
		messageText.setVisible(true);
		setManagedView(messageText);

		// Refresh the scroll pane
		this.scrollPane.refresh();

		// Create OK button
		this.OKButton = new BButton(this.getNormal(),										// Put list on "normal" level
									this.safeTitleH,										// x coord. of button
									this.screenHeight - this.safeTitleV - this.rowHeight, 	// y coord - Align with bottom of screen
									(this.screenWidth - (2 * this.safeTitleH)) / 2, 		// width of button (half screen)
									this.rowHeight											// height of list
		);
		setManagedResource(OKButton, createText(this.app.hSkin.barFont, HSkin.NTSC_WHITE, OK_LABEL));
		this.OKButton.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null, null, true);
		this.OKButton.setFocusable(true);

		// Set default focus on scrollPane
		this.setFocusDefault(this.OKButton);
	}

	/* (non-Javadoc)
	 * @see com.tivo.hme.bananas.BView#handleKeyPress(int, long)
	 */
	@Override
	public boolean handleKeyPress(int code, long rawcode) {

		this.app.checkKeyPressToResetInactivityTimer(code);

		if(this.getFocus() != null && this.getFocus().equals(this.OKButton) && code == KEY_SELECT) {

			this.app.pop();
			return true;
		}

		  switch (code) {
	        case KEY_UP:
	            this.scrollPane.lineUp();
	            return true;
	        case KEY_DOWN:
	            this.scrollPane.lineDown();
	            return true;
	        case KEY_CHANNELUP:
	            this.scrollPane.pageUp();
	            return true;
	        case KEY_CHANNELDOWN:
	            this.scrollPane.pageDown();
	            return true;
	        }
		return super.handleKeyPress(code, rawcode);
	}
}
