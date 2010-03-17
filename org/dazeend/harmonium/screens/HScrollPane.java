/*************************************************************************
 * Copyright 2008 David Almilli
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 **************************************************************************/

/* This file is adapted from BScrollPanePlus.java from the Bananas-Plus project.
 * It was received under the Apache 2.0 license, and this modified form is
 * distributed under the same terms and license as the rest of Harmonium.
 */

package org.dazeend.harmonium.screens;

import com.tivo.hme.bananas.BHighlight;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;
import java.awt.Rectangle;

import org.dazeend.harmonium.Harmonium;

/**
 * The <code>BScrollPanePlus</code> class is a scroll pane that encapsulates it's child views in a
 * scrollable view if the bounds of this scroll pane is smaller than the total bounding box of all
 * it's child views.  After you have added all the child view, you must call refresh() for it to
 * calculate the bounds.  If you dynamically change the size of any children, you must either call 
 * invalidate() and then call refresh() or post a "resize" action to this view for it 
 * to update the bounds changes.
 * @author s2kdave
 */
public class HScrollPane extends BView {
	public static final String ACTION_RESIZE = "resize";
    public final static String ANIM = "*100";
    
	protected int lineHeight;
	protected int pageHeight;
	protected int offset;
	protected int totalHeight;
	protected boolean validated;
	protected boolean animate = true;

	public HScrollPane(BView parent, int x, int y, int width, int height) {
		this(parent, x, y, width, height, 25, true);
	}

	public HScrollPane(BView parent, int x, int y, int width, int height, int lineHeight) {
		this(parent, x, y, width, height, lineHeight, true);
	}
	
	public HScrollPane(BView parent, int x, int y, int width, int height, int lineHeight, boolean visible) {
		this(parent, ViewUtils.getBounds(parent, x, y, width, height), lineHeight, true);
	}

	protected HScrollPane(BView parent, Rectangle bounds, int lineHeight, boolean visible) {
		super(parent, bounds.x, bounds.y, bounds.width, bounds.height, visible);
		this.lineHeight = lineHeight;
		this.pageHeight = bounds.height - lineHeight;
	}
	
	protected void validate() {
		if (!validated) {
			validated = true;
			totalHeight = 0;
			int count = getChildCount();
			for (int i=0; i < count; i++) {
				View view = getChild(i);
                if (view.getVisible()) {
                    totalHeight = Math.max(view.getY() + view.getHeight(), totalHeight);
                }
			}
            getHighlights().setPageHint(H_PAGEUP,   A_RIGHT+13, A_TOP - 25);
            getHighlights().setPageHint(H_PAGEDOWN, A_RIGHT+13, A_BOTTOM + 30);
			refresh();
		}
	}
	
	public void invalidate() {
		validated = false;
	}
	
	public void reset() {
		offset = 0;
		setTranslation(0, offset);
	}
	
	public void pageUp() {
		validate();
		if (offset < 0) {
	        Resource anim = animate ? getAnimationResource() : null;
			offset = Math.min(0, offset+pageHeight);
			setTranslation(0, offset, anim);
			refresh();
			getBApp().play("pageup.snd");
		}
	}
	
	protected Resource getAnimationResource() {
		return getResource(ANIM);
	}
	
	public void pageDown() {
		validate();
		if (offset > pageHeight-totalHeight) {
	        Resource anim = animate ? getAnimationResource() : null;
			offset = Math.max(pageHeight-totalHeight, offset-pageHeight);
			offset = Math.min(0, offset);
			setTranslation(0, offset, anim);
			refresh();
			getBApp().play("pagedown.snd");
		}
	}
	
	public void lineUp() {
		validate();
		if (offset < 0) {
	        Resource anim = animate ? getAnimationResource() : null;
			offset = Math.min(0, offset+lineHeight);
			setTranslation(0, offset, anim);
			refresh();
			getBApp().play("updown.snd");
		}
	}
	
	public void lineDown() {
		validate();
		if (offset > pageHeight-totalHeight) {
	        Resource anim = animate ? getAnimationResource() : null;
			offset = Math.max(pageHeight-totalHeight, offset-lineHeight);
			offset = Math.min(0, offset);
			setTranslation(0, offset, anim);
			refresh();
			getBApp().play("updown.snd");
		}
	}

	public void refresh() {
		validate();
		refreshHighlights();
	}
	
	protected void refreshHighlights() {
		BHighlight up = getHighlights().get(H_PAGEUP);
		if (up != null) {
			up.setVisible((offset < 0) ? H_VIS_TRUE : H_VIS_FALSE);
		}
		
		BHighlight down = getHighlights().get(H_PAGEDOWN);
		if (down != null) {
			down.setVisible((offset > pageHeight-totalHeight) ? H_VIS_TRUE : H_VIS_FALSE);
		}
		getHighlights().refresh();
	}

	@Override
	public boolean handleAction(BView view, Object action) {
		if (action.equals(ACTION_RESIZE)) {
			invalidate();
			refresh();
		}
		return super.handleAction(view, action);
	}

	@Override
	public boolean handleKeyPress(int code, long rawcode) {

		((Harmonium)this.getApp()).checkKeyPressToResetInactivityTimer(code);
		
        switch (code) {
        case KEY_UP:
            lineUp();
            return true;
        case KEY_DOWN:
            lineDown();
            return true;
        case KEY_CHANNELUP:
            pageUp();
            return true;
        case KEY_CHANNELDOWN:
            pageDown();
            return true;
        }

        return super.handleKeyPress(code, rawcode);
	}

	@Override
	public boolean handleKeyRepeat(int code, long rawcode) {
		return handleKeyPress(code, rawcode);
	}

	public boolean isAnimate() {
		return animate;
	}

	public void setAnimate(boolean animate) {
		this.animate = animate;
	}

    public int getLineHeight() {
        return lineHeight;
    }
/*
    public void setLineHeight(int lineHeight) {
        synchronized (lock) {
            this.lineHeight = lineHeight;
            this.pageHeight = getHeight() - lineHeight;
            refresh();
        }
    }
*/
}
