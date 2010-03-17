//////////////////////////////////////////////////////////////////////
//
// File: View.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * A view.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class View extends HmeObject
{
    static View EMPTY[] = new View[0];
    
    //
    // properties
    //

    /**
     * The view which contains this view.
     */
    private View parent;

    /**
     * The resource contained within the view.
     */
    private Resource resource;

    /**
     * The flags for the resource.
     */
    private int flags;

    /**
     * The view's bounds within the parent.
     * REMIND: package private
     */
    private int x, y, width, height;

    /**
     * The view's content translation.
     */
    private int tx, ty;

    /**
     * The view's scale, by default 1. Scaling affects all of the view's content.
     */
    private float sx = 1.0f, sy = 1.0f;

    /**
     * The view's transparency.
     * 
     * <p>0 is opaque, 1 is transparent. Transaparency affects all of the view's content.
     */
    private float transparency = 0.0f;

    /**
     * True if the view is currently painting onscreen.
     */
    private boolean painting = true;

    /**
     * True if the view is visible.
     * REMIND: package private
     */
    boolean visible;

    //
    // children
    //

    /**
     * The number of children contained within the view.
     */
    private int nchildren;

    /**
     * The view's children.
     */
    private View children[] = EMPTY;


    /**
     * This is the root view constructor.
     * Creates the root view as invisible.
     * 
     * @param app 
     */
    View(Application app)
    {
        super(app, ID_ROOT_VIEW);
        visible = false;
    }

    /**
     * Creates a new <code>View</code> instance. The view will be visible.
     */
    public View(View parent, int x, int y, int width, int height)
    {
        this(parent, x, y, width, height, true);
    }

    /**
     * Create a new <code>View</code> using <code>CMD_VIEW_ADD</code>.
     */
    public View(View parent, int x, int y, int width, int height, boolean visible)
    {
        super(parent.getApp(), parent.getApp().getNextID());
        
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
        parent.addChild(this);
        getApp().cmdViewAdd(getID(), parent, x, y, width, height, visible);
    }

    /**
     * This is used by Application to size the root view. The normal setSize or
     * setBounds cannot be used because there is no transaction with the
     * receiver.
     * 
     * NOTE: this is called from Application.setContext when the app has the
     * parsed command line Arguments arguments which may override the defaults.
     */
    void setRootSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }
    
    /**
     * @return The view which contains this view.
     */
    public View getParent()
    {
        return parent;
    }

    /**
     * @return The resource contained within the view.
     */
    public Resource getResource()
    {
        return resource;
    }

    /**
     * @return The flags for the resource.
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     * @return the X coordinate of this view in parent's coordinate space.
     */
    public int getX()
    {
        return x;
    }

    /**
     * @return the Y coordinate of this view in parent's coordinate space.
     */
    public int getY()
    {
        return y;
    }
    
    /**
     * @return the width of this view.
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * @return the height of this view.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @return the view's content translation X.
     */
    public int getTranslationX()
    {
        return tx;
    }

    /**
     * @return the view's content translation Y.
     */
    public int getTranslationY()
    {
        return ty;
    }

    /**
     * @return the view's scale X
     */
    public float getScaleX()
    {
        return sx;
    }

    /**
     * @return the view's scale Y
     */
    public float getScaleY()
    {
        return sy;
    }

    /**
     * 0 is opaque, 1 is transparent. Transaparency affects all of the view's content.
     * @return the view's transparency.
     */
    public float getTransparency()
    {
        return transparency;
    }

    /**
     * @return True if painting is enabled for the view.
     */
    public boolean getPainting()
    {
        return painting;
    }

    /**
     * @return True if the view is visible.
     */
    public boolean getVisible()
    {
        return visible;
    }

    /**
     * @return the number of child views contained by this parent.
     */
    public int getChildCount()
    {
        return nchildren;
    }

    /**
     * @return the child View at the given index position.
     */
    public View getChild(int index)
    {
        return children[index];
    }

    /**
     * Remove the view immediately.
     */
    public void remove()
    {
        remove(null);
    }

    /**
     * Remove the view using <code>CMD_VIEW_REMOVE</code>.
     */
    public void remove(Resource animation)
    {
        parent.removeChild(this);
        resource = null;
        if (getApp().getFocus() == this) {
            getApp().setFocus(null);
        }
        getApp().cmdViewRemove(getID(), animation);
        setID(-1);
    }
    
    //
    // getters
    //

    /**
     * Get the location.
     */
    public Point getLocation()
    {
        return new Point(x, y);
    }
    
    /**
     * Get the size.
     */
    public Dimension getSize()
    {
        return new Dimension(width, height);
    }

    /**
     * Get the bounds.
     */
    public Rectangle getBounds()
    {
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Get the translation.
     */
    public Point getTranslate()
    {
        return new Point(tx, ty);
    }

    //
    // setters
    //

    /**
     * Set the location immediately.
     */
    public void setLocation(int x, int y)
    {
        setBounds(x, y, width, height, null);
    }

    /**
     * Set the location using {@link #setBounds(int,int,int,int,Resource)}.
     */
    public void setLocation(int x, int y, Resource animation)
    {
        setBounds(x, y, width, height, animation);
    }

    /**
     * Set the size immediately.
     */
    public void setSize(int width, int height)
    {
        setBounds(x, y, width, height, null);
    }

    /**
     * Set the size using {@link #setBounds(int,int,int,int,Resource)}.
     */
    public void setSize(int width, int height, Resource animation)
    {
        setBounds(x, y, width, height, animation);      
    }

    /**
     * Set the bounds immediately.
     */
    public void setBounds(int x, int y, int width, int height)
    {
        setBounds(x, y, width, height, null);
    }

    /**
     * Set the bounds using <code>CMD_VIEW_SET_BOUNDS</code>.
     */
    public void setBounds(int x, int y, int width, int height, Resource animation)
    {
        if (animation == null && this.x == x && this.y == y && this.width == width && this.height == height) {
            return;
        }

        getApp().cmdViewSetBounds(getID(), x, y, width, height, animation);
        
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Set the scale for the view immediately.
     *
     * sx and sy must be greater than or equal to 0.  Scaling to numbers less
     * than 1 means that the size of the view and its contents are reduced by
     * that factor. Scaling to numbers greater than 1 means that the size of the
     * view and its contents are multiplied by that factor.  Scaling by 1 scales
     * the view to its x and y values.
     *
     * @param sx The 'x' scaling factor.
     * @param sy The 'y' scaling factor.
     */
    public void setScale(float sx, float sy)
    {
        setScale(sx, sy, null);
    }

    /**
     * Set the scale for the view using <code>CMD_VIEW_SET_SCALE</code>.
     *
     * @param sx The 'x' scaling factor.
     * @param sy The 'y' scaling factor.
     * @param animation An animation resource, e.g. to scale over time.
     * @see #setScale(float sx, float sy)
     */
    public void setScale(float sx, float sy, Resource animation)
    {
        if (animation == null && this.sx == sx && this.sy == sy) {
            return;
        }

        getApp().cmdViewSetScale(getID(), sx, sy, animation);

        this.sx = sx;
        this.sy = sy;
    }
    
    /**
     * Set the translation for the view immediately.
     */
    public void setTranslation(int tx, int ty)
    {
        setTranslation(tx, ty, null);
    }

    /**
     * Set the translation for the view using
     * <code>CMD_VIEW_SET_TRANSLATION</code>.
     */
    public void setTranslation(int tx, int ty, Resource animation)
    {
        if (animation == null && this.tx == tx && this.ty == ty) {
            return;
        }

        getApp().cmdViewSetTranslation(getID(), tx, ty, animation);
        
        this.tx = tx;
        this.ty = ty;
    }
    
    /**
     * Offset the translation for the view immediately.
     */
    public void translate(int dx, int dy)
    {
        translate(dx, dy, null);
    }

    /**
     * Offset the translation for the view using {@link
     * #setTranslation(int,int,Resource)}.
     */
    public void translate(int dx, int dy, Resource animation)
    {
        setTranslation(tx + dx, ty + dy, animation);
    }

    /**
     * Set the visibility immediately.
     */
    public void setVisible(boolean visible)
    {
        setVisible(visible, null);
    }

    /**
     * Set the visibility using <code>CMD_VIEW_SET_VISIBLE</code>.
     */
    public void setVisible(boolean visible, Resource animation)
    {
        if (animation == null && this.visible == visible) {
            return;
        }

        getApp().cmdViewSetVisible(getID(), visible, animation);
        
        this.visible = visible;
    }

    /**
     * Set the transparency immediately.
     */
    public void setTransparency(float transparency)
    {
        setTransparency(transparency, null);
    }

    /**
     * Set the visibility using <code>CMD_VIEW_SET_TRANSPARENCY</code>.
     */
    public void setTransparency(float transparency, Resource animation)
    {
        if (animation == null && this.transparency == transparency) {
            return;
        }

        getApp().cmdViewSetTransparency(getID(), transparency, animation);
        
        this.transparency = transparency;
    }

    /**
     * Not yet implemented.
     */
    public void setPainting(boolean painting)
    {
        if (this.painting == painting) {
            return;
        }

        getApp().cmdViewSetPainting(getID(), painting);
        
        this.painting = painting;
    }

    /**
     * Clear the view's resource.
     */
    public void clearResource()
    {
        setResource((Resource)null, 0);
    }

    /**
     * Set the view's resource by key.
     */
    public void setResource(Object key)
    {
        setResource(getApp().getResource(key), 0);
    }

    public void setResource(Object key, int flags)
    {
        setResource(getApp().getResource(key), flags);
    }

    public void setResource(Resource resource)
    {
        setResource(resource, 0);
    }

    /**
     * Set the view's resource using <code>CMD_VIEW_SET_RESOURCE</code>.
     */
    public void setResource(Resource resource, int flags)
    {
        if (resource != null && resource == this.resource && flags == this.flags) {
            return;
        } 

        getApp().cmdViewSetResource(getID(), resource, flags);

        if (resource != this.resource) {
            if (this.resource instanceof StreamResource) {
                ((StreamResource)this.resource).removeHandler(this);
            }
            this.resource = resource;
            if (this.resource instanceof StreamResource) {
                ((StreamResource)this.resource).addHandler(this);
            }
        }
    }

    /**
     * This method will word-break the text into lines based on the size of the
     * view and the given fontInfo. This inserts a single new-line character at
     * the line break point and will collapse \r\n into a single \n
     *
     * Note: The simulator also has a version of this method.
     * see com.tivo.hme.sim.SimResource#layout(SimView, FontMetrics, String)
     *
     * This code should be moved to a shared location and support
     * HmeEvent.FontInfo or Java Metrics. Or the simulator needs to be changed
     * to not use awt DrawString and perform character placement manually and
     * render the text with DrawChar.
     *
     * @param fontInfo the font metrics info for the intended font
     * @param text the text that will be layed out
     */
    public String layoutText(HmeEvent.FontInfo fontInfo, String text)
    {
        char chars[] = text.toCharArray();
        int start = 0;
        int eow = -1;

        int i;
        StringBuffer result = new StringBuffer();
        for (i = 0; i < chars.length;) {
            char ch = chars[i];
            boolean isNewline = false;
            if (Character.isWhitespace(ch)) {
                if (ch == '\r') {
                    isNewline = true;
                    if (i + 1 < chars.length && chars[i + 1] == '\n') {
                        ++i;
                    }
                } else if (ch == '\n') {
                    isNewline = true;
                }
                eow = i;
            }

            int txtWidth = fontInfo.measureTextWidth(new String(chars, start, (i - start) + 1));
            if (txtWidth >= width || isNewline) {
                if (i == start && !isNewline) {
                    return "";
                }

                int breakat;
                int newline;
                if (eow >= start) {
                    breakat = eow;
                    newline = breakat + 1;
                } else {
                    breakat = i;
                    newline = i;
                }

                String tt = new String(chars, start, breakat - start);
                result.append(tt);
                result.append('\n');
                i = start = newline;
            } else {
                i++;
            }
        }

        if (i > start) {
            String tt = new String(chars, start, i - start);
            result.append(tt);
        }
        return new String(result);
    }

    //
    // misc commands
    //

    /**
     * Set the focus to this view.
     */
    public void setFocus()
    {
        getApp().setFocus(this);
    }

    /**
     * Return true if this view has the focus.
     */
    public boolean hasFocus()
    {
        return (getApp().getFocus() == this);
    }

    /**
     * Handle change of focus. Return true when the event is consumed.
     */
    public boolean handleFocus(boolean focus)
    {
        return false;
    }

    /**
     * Deliver an event to the view and propagate it to its parents until it is
     * handled.
     */
    public void postEvent(HmeEvent event)
    {
        if (!handleEvent(event)) {
            if (parent != null) {
                parent.postEvent(event);
            } else if (getApp() != null) {
                getApp().postEvent(event);
            }
        }
    }

    //
    // children
    //
    
    void addChild(View child)
    {
        if (nchildren == children.length) {
            System.arraycopy(children, 0, children = new View[nchildren + 5], 0, nchildren);
        }
        children[nchildren++] = child;
        child.parent = this;
    }

    void removeChild(View child)
    {
        for (int i = 0; i < nchildren; i++) {
            if (children[i] == child) {
                if (i < nchildren - 1) {
                    System.arraycopy(children, i + 1, children, i, nchildren - i - 1);
                }
                children[--nchildren] = null;
                break;
            }
        }
    }

    /**
     * Dump the view and all children to System.out, with an indent.
     */
    public void dump(int indent)
    {
        for (int i = 0 ; i < indent ; i++) {
            System.out.print(" ");
        }
        System.out.println(this);
        if (resource != null) {
            resource.dump(indent + 2);
        }
        for (int i = 0 ; i < nchildren ; i++) {
            children[i].dump(indent + 2);
        }
    }

    protected void toString(StringBuffer buf)
    {
        buf.append(",bounds=" + x + "," + y + "," + width + "x" + height);
        if ((tx != 0) || (ty != 0)) {
            buf.append(",tx=" + tx + ",ty=" + ty);
        }
        if ((sx != 1) || (sy != 1)) {
            buf.append(",sx=" + sx + ",sy=" + sy);
        }
        if (transparency != 0) {
            buf.append(",transparency=" + (int)(100f * transparency) + "%");
        }
        if (!visible) {
            buf.append(",!visible");
        }
        if (!painting) {
            buf.append(",!painting");
        }
    }
}
