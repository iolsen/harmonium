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

/* This file is adapted from ViewUtils.java from the Bananas-Plus project.
 * It was received under the Apache 2.0 license, and this modified form is
 * distributed under the same terms and license as the rest of Harmonium.
 */

package org.dazeend.harmonium.screens;

import static com.tivo.hme.bananas.IBananas.*;

import com.tivo.hme.bananas.BApplication;
import com.tivo.hme.bananas.BSkin;
import com.tivo.hme.bananas.BSkin.Element;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;
import java.awt.Rectangle;

public class ViewUtils {

	public static final int S_STRETCH           = 0x01000000;
	public static final int S_STRETCH_PERCENT   = 0x03000000;
	public static final int S_STRETCH_MASK      = 0xff000000;
    public static final int S_NEGATIVE_MASK     = 0x00f00000;
    public static final int S_DELTA_MASK        = 0x000fffff;
    
	private ViewUtils() {}
	
    /**
     * Gets the resource from the skin used by the view.  It will return null if either the resource
     * doesn't exist or the skin doesn't define the resourceName
     * @param view the skinned view
     * @param resourceName the resource name to get from the skin
     * @return the resource to use based on the skin
     */
	public static Resource getResource(BView view, String resourceName) {
		BSkin skin = view.getBApp().getSkin();
		BSkin.Element e = skin.get(resourceName);
		if (e != null) {
			return e.getResource();
		} else {
			return null;
		}
	}
    
    public static int getWidth(BView view, String resourceName) {
        BSkin skin = view.getBApp().getSkin();
        BSkin.Element e = skin.get(resourceName);
        if (e != null) {
            return e.getWidth();
        } else {
            return 0;
        }
    }
    
    public static int getHeight(BView view, String resourceName) {
        BSkin skin = view.getBApp().getSkin();
        BSkin.Element e = skin.get(resourceName);
        if (e != null) {
            return e.getHeight();
        } else {
            return 0;
        }
    }

    /**
     * Gets the bounds for a view that is nested inside the parent.  This method accepts any of the
     * IBananas.A_ANCHOR_XXX modifiers for positional location.  The dimension will be retrieved
     * from a resource out of the skin of the parent view.  Note that the parent View must 
     * reference a BApplication in order for it to have a skin.  The View itself doesn't need to 
     * be a BView.
     * <p>
     * To anchor the x or y locations:<br>
     * <code>A_RIGHT - 25</code> (right justify and subtract 25 pixels from the location)<br>
     * </p>
     * @param parent the parent view
     * @param anchorX the (potentially anchored) x location for the view
     * @param anchorY the (potentially anchored) y location for the view
     * @param resource the resource name to retrieve from the skin to get the view dimensions
     * @return the bounds for the view
     */
	public static Rectangle getBounds(View parent, int anchorX, int anchorY, String resource) {
		int height = 0;
		int width = 0;

		Element e = ((BApplication)parent.getApp()).getSkin().get(resource);
		if (e != null) {
			width = e.getWidth();
			height = e.getHeight();
		}
		return getBounds(parent, anchorX, anchorY, width, height);
	}

    /**
     * Gets the bounds for a view that is nested inside the parent.  This method accepts any of the
     * IBananas.A_ANCHOR_XXX modifiers for positional location and IBananasPlus.S_STRETCH_XXX for 
     * the width.  The height will be retrieved from a resource out of the skin of the parent view.  
     * Note that the parent View must reference a BApplication in order for it to have a skin.  
     * The View itself doesn't need to be a BView.
     * <p>
     * To anchor the x or y locations:<br>
     * <code>A_RIGHT - 25</code> (right justify and subtract 25 pixels from the location)<br>
     * To stretch the width dimension:<br>
     * <code>S_STRETCH - 100</code> (100 pixels smaller than the parent)<br>
     * or <code>S_STRETCH_PERCENT + 50</code> (50% of the size of the parent)
     * </p>
     * @param parent the parent view
     * @param anchorX the (potentially anchored) x location for the view
     * @param anchorY the (potentially anchored) y location for the view
     * @param stretchWidth the (potentially stretched) width for the view
     * @param resource the resource name to retrieve from the skin to get the height dimension
     * @return the bounds for the view
     */
	public static Rectangle getBounds(View parent, int anchorX, int anchorY, int stretchWidth, String... resources) {
		int height = 0;
		
        for (String resource : resources) {
    		Element e = ((BApplication)parent.getApp()).getSkin().get(resource);
    		if (e != null) {
    			height += e.getHeight();
    		}
        }
		return getBounds(parent, anchorX, anchorY, stretchWidth, height);
	}
	
    /**
     * Gets the bounds for a view that is nested inside the parent.  This method accepts any of the
     * IBananas.A_ANCHOR_XXX modifiers for positional location and IBananasPlus.S_STRETCH_XXX for 
     * the dimensions.  Note that the parent View must reference a BApplication in order for it 
     * to have a skin.  The View itself doesn't need to be a BView.
     * <p>
     * To anchor the x or y locations:<br>
     * <code>A_RIGHT - 25</code> (right justify and subtract 25 pixels from the location)<br>
     * To stretch the width or height dimensions:<br>
     * <code>S_STRETCH - 100</code> (100 pixels smaller than the parent)<br>
     * or <code>S_STRETCH_PERCENT + 50</code> (50% of the size of the parent)
     * </p>
     * @param parent the parent view
     * @param anchorX the (potentially anchored) x location for the view
     * @param anchorY the (potentially anchored) y location for the view
     * @param stretchWidth the (potentially stretched) width for the view
     * @param stretchHeight the (potentially stretched) height for the view
     * @return the bounds for the view
     */
	public static Rectangle getBounds(View parent, int anchorX, int anchorY, 
            int stretchWidth, int stretchHeight) {
		Rectangle rect = parent.getBounds();
        Rectangle bounds = new Rectangle(anchorX, anchorY, stretchWidth, stretchHeight);
        calculateStretch(rect, bounds, stretchWidth, stretchHeight);
        calculateAnchor(parent, rect, bounds, anchorX, anchorY);
		return bounds;
    }

    private static void calculateAnchor(View parentView, Rectangle parent, Rectangle view, int anchorX, int anchorY) {
        //
		// use bit twiddling to extract a delta from the anchor value
		//

        int deltaX;
        if (anchorX > A_DELTA_MASK) {
            if ((anchorX & A_NEGATIVE_MASK) != 0) {
                deltaX = anchorX | ~A_DELTA_MASK;
                anchorX = (anchorX | A_CENTER) & A_ANCHOR_MASK;
            } else {
                deltaX = anchorX & A_DELTA_MASK;
                anchorX = anchorX & A_ANCHOR_MASK;
            }
        } else {
            deltaX = anchorX;
            anchorX = 0;
        }

        int deltaY;
        if (anchorY > A_DELTA_MASK) {
            if ((anchorY & A_NEGATIVE_MASK) != 0) {
                deltaY = anchorY | ~A_DELTA_MASK;
                anchorY = (anchorY | A_CENTER) & A_ANCHOR_MASK;
            } else {
                deltaY = anchorY & A_DELTA_MASK;
                anchorY = anchorY & A_ANCHOR_MASK;
            }
        } else {
            deltaY = anchorY;
            anchorY = 0;
        }

		int x = 0;
		switch (anchorX) {
		case A_LEFT:
        default:
			x = 0;
			break;
		case A_CENTER:
			x = (parent.width - view.width) / 2;
			break;
		case A_RIGHT:
			x = parent.width - view.width;
			break;
		}

		int y = 0;
		switch (anchorY) {
		case A_TOP:
        default:
			y = 0;
			break;
		case A_CENTER:
			y = (parent.height - view.height) / 2;
			break;
		case A_BOTTOM:
			y = parent.height - view.height;
			break;
		}
        
        view.x = x + deltaX;
        view.y = y + deltaY;
	}

    private static void calculateStretch(Rectangle parent, Rectangle view, int stretchW, int stretchH) {
        //
        // use bit twiddling to extract a delta from the stretch value
        //

        int deltaW;
        if (stretchW > S_DELTA_MASK) {
            if ((stretchW & S_NEGATIVE_MASK) != 0) {
                deltaW = stretchW | ~S_DELTA_MASK;
                stretchW = (stretchW | S_STRETCH) & S_STRETCH_MASK;
            } else {
                deltaW = stretchW & S_DELTA_MASK;
                stretchW = stretchW & S_STRETCH_MASK;
            }
        } else {
            deltaW = stretchW;
            stretchW = 0;
        }

        int deltaH;
        if (stretchH > S_DELTA_MASK) {
            if ((stretchH & S_NEGATIVE_MASK) != 0) {
                deltaH = stretchH | ~S_DELTA_MASK;
                stretchH = (stretchH | S_STRETCH) & S_STRETCH_MASK;
            } else {
                deltaH = stretchH & S_DELTA_MASK;
                stretchH = stretchH & S_STRETCH_MASK;
            }
        } else {
            deltaH = stretchH;
            stretchH = 0;
        }

        int width = deltaW;
        switch (stretchW) {
        case S_STRETCH:
            width = parent.width + deltaW;
            break;
        case S_STRETCH_PERCENT:
            width =  parent.width * deltaW / 100;
            break;
        }

        int height = deltaH;
        switch (stretchH) {
        case S_STRETCH:
            height = parent.height + deltaH;
            break;
        case S_STRETCH_PERCENT:
            height =  parent.height * deltaH / 100;
            break;
        }

        view.width = width;
        view.height = height;
    }
}
