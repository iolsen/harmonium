//////////////////////////////////////////////////////////////////////
//
// File: SimView.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Vector;

import com.tivo.hme.sdk.HmeObject;
import com.tivo.hme.sdk.IHmeProtocol;

/**
 * A view represents a rectangle inside another view.  The top-level view is the
 * entire screen. Note that the view's resource is stored as the first child.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
class SimView extends SimObject implements IHmeProtocol
{
    //
    // view properties
    //
    
    int x, y, width, height;
    int tx, ty;
    double sx = 1.0, sy = 1.0;
    boolean visible;
    float transparency;
    int painting;
    short viewDepth;

    //
    // used to control awt graphics transparency
    //
    
    Composite imageComposite;   // determined by its transparency

    //
    // resource flags (text flags, fit, alignment, etc.)
    //
    int resourceFlags;

    //
    // the "rendered" version of the resource. This is entirely for use by the
    // resource contained within this view. The view doesn't touch directly
    // except to set it to null. resourceRendered gets set to null whenever the
    // resource changes, or the view is resized, scaled, etc.
    //
    
    Object resourceRendered;

    //
    // an exact replica of this view, for use during setPainting false
    //

    SimView shadow;
 
    SimView(SimApp app, int id, SimObject parent, int x, int y, int width, int height, boolean visible)
    {
        super(app, id);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
        this.transparency = 0;
        this.painting = 0;

        if (parent != null) {
            parent.addChild(this);
            viewDepth = (short) (parent.getViewDepth() + 1);
            repaint();
        }
    }

    /**
     * Special constructor for creating shadow views when setPainting(false).
     */
    SimView(SimView other)
    {
        super(other.app, -1);
        x = other.x;
        y = other.y;
        width = other.width;
        height = other.height;
        tx = other.tx;
        ty = other.ty;
        sx = other.sx;
        sy = other.sy;
        visible = other.visible;
        transparency = other.transparency;
        painting = 0;
        imageComposite = other.imageComposite;
        resourceFlags = other.resourceFlags;
        resourceRendered = other.resourceRendered;
        viewDepth = other.viewDepth;

        // now children...
        nchildren = other.nchildren;
        children = (SimObject[])other.children.clone();
    }

    void remove()
    {
        //
        // save some stuff for the repaint
        //
        
        SimObject save[] = new SimObject[nparents];
        System.arraycopy(parents, 0, save, 0, nparents);
        Rectangle bounds = getDrawingBounds();
        toParentSpace(bounds);

        //
        // remove
        //
        
        for (int i = nparents; i-- > 0;) {
            parents[i].removeChild(this);
        }

        //
        // repaint
        //
        
        for (int i = save.length; i-- > 0;) {
            save[i].repaint(bounds);
        }
            
        close();
    }

    void close()
    {
        //
        // remove our id from the app
        //
        
        app.set(id, (SimView)null);

        //
        // kill animations and resource
        //
        
        SimAnimator.get().remove(this, null);
        setResource(null);

        //
        // close all children
        //
        
        for (int i = nchildren ; i-- > 0 ; ) {
            ((SimView)children[i]).close();
        }
    }

    short getViewDepth()
    {
        return viewDepth;
    }

    boolean isPainting()
    {
        return painting == 0;
    }

    //
    // overridden to make setPainting work
    //
    
    void paint(SimObject parent, Graphics2D g)
    {
        if (isPainting()) {
            super.paint(parent, g);
        } else {
            shadow.paint(parent, g);
        }
    }


    
    //
    // coord overriddes from SimObject
    //
    
    Graphics2D getChildGraphics(Graphics2D g)
    {
        if (!visible || transparency == 1) {
            return null;
        }

        Graphics2D g2 = (Graphics2D)g.create(x, y, (int)(width * sx), (int)(height * sy));
        
        // translate
        g2.translate(tx, ty);

        // scale
        if (sx != 1.0f || sy != 1.0f) {
            Object hint = null;
            if (sx > 1.01 || sy > 1.01) {
                hint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            } else if (sx < 0.99 || sy < 0.99) {
                hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
            }
            if (hint != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            }
            g2.scale(sx, sy);
        }
            
        // transparency
        if (imageComposite != null) {
            g2.setComposite(imageComposite);
        }

        return g2;
    }

    Rectangle getDrawingBounds()
    {
        return new Rectangle(-tx, -ty, width, height);
    }
    
    List getAbsoluteBounds()
    {
        List list = new Vector();
        list.add(getDrawingBounds());
        toAbsoluteRectangles(list);
        return list;
    }

    void toChildSpace(Point p)
    {
        p.translate(-x, -y);
        if (sx != 1.0f) {
            p.x = (int)(p.x / sx); 
        }
        if (sy != 1.0f) {
            p.y = (int)(p.y / sy); 
        }
        p.translate(-tx, -ty);
    }

    void toParentSpace(Rectangle r)
    {
        r.translate(tx, ty);     
        if (sx != 1.0f) { 
            r.x     = (int) (r.x * sx); 
            r.width = (int) (r.width * sx); 
        } 
        if (sy != 1.0f) { 
            r.y = (int) (r.y * sy); 
            r.height = (int) (r.height * sy); 
        } 
        r.translate(x, y);
    }


    
    //
    // individual commands
    //

    void setVisible(boolean visible)
    {
        if (this.visible != visible) {
            this.visible = visible;
            repaint();
            touch();
        }
    }

    void setTransparency(float transparency)
    {
        if (this.transparency != transparency) {
            this.transparency = transparency;

            if (transparency == 0) {
                imageComposite = null;
            } else {
                imageComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - transparency);
            }
            repaint();
            touch();
        }
    }

    void setPainting(boolean painting)
    {
        boolean oldPainting = isPainting();
        if (painting) {
            if (this.painting < 0) {
                ++this.painting;
            }
        } else {
            --this.painting;
        }
        
        boolean newPainting = isPainting();
        if (oldPainting != newPainting) {
            if (newPainting) {
                shadow = null;
            } else {
                shadow = new SimView(this);
            }
            repaint();
            touch();
        }
    }

    void setTranslation(int tx, int ty)
    {
        if (this.tx != tx || this.ty != ty) {
            this.tx = tx;
            this.ty = ty;
            repaint();
            touch();
        }
    }

    void setScale(double sx, double sy)
    {
        if (this.sx != sx || this.sy != sy) {
            repaint(false);
            this.sx = sx;
            this.sy = sy;
            repaint(true);
            touch();
        }
    }

    void setBounds(int x, int y, int width, int height)
    {
        if (this.x != x || this.width != width || this.y != y || this.height != height) {
            resourceRendered = null;
            
            repaint(false);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            repaint(true);
            touch();
        }
    }

    
    //
    // resource management - the resource is stored as child 0
    //

    void setResource(SimResource rsrc)
    {
        setResource(rsrc, 0);
    }
    
    void setResource(SimResource rsrc, int flags)
    {
        resourceFlags = flags;
        resourceRendered = null;
        
        SimResource oldResource = getResource();
        if (oldResource != rsrc) {
            if (oldResource != null) {
                removeChild(oldResource);
            }
            if (rsrc != null) {
                addChild(rsrc, 0);
            }
        }

        repaint();
    }

    SimResource getResource()
    {
        if (nchildren > 0) {
            SimObject child = children[0];
            if (child instanceof SimResource) {
                return (SimResource)child;
            }
        }
        return null;
    }

    //
    // tree overriddes
    //

    String getIcon()
    {
        return "view.png";
    }
    
    void toString(StringBuffer buf)
    {
        buf.append("," + x + "," + y + "," + width + "x" + height);
        if ((tx != 0) || (ty != 0)) {
            buf.append(",tx=" + tx + ",ty=" + ty);
        }
        if ((sx != 1) || (sy != 1)) {
            buf.append(",sx=" + (int)(sx * 100f) + "%,sy=" + (int)(sy * 100f) + "%");
        }
        if (transparency != 0) {
            buf.append(",transparency=" + (int)(transparency * 100f) + "%");
        }
        if (resourceFlags != 0) {
            buf.append(",flags=" + HmeObject.rsrcFlagsToString(resourceFlags));
        }
        if (!visible) {
            buf.append(",!visible");
        }
        if (!isPainting()) {
            buf.append(",!painting");
        }
    }
}
