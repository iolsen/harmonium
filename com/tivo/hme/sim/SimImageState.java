//////////////////////////////////////////////////////////////////////
//
// File: SimImageState.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.*;
import java.awt.image.*;

import com.tivo.hme.sdk.*;

/**
 * This is a helper class for loading image asynchronously using AWT. It keeps
 * track of the image dimensions, whether or not an error has occurred, etc.
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
class SimImageState implements IHmeProtocol, ImageObserver
{
    SimResource rsrc;
    Image img;
    int w, h;
    String errorText;
    int nframes;

    /**
     * Create a new SimImageState from an unloaded AWT image.
     */
    SimImageState(SimResource rsrc, Image img)
    {
        this.rsrc = rsrc;
        this.img = img;

        w = -1;
        h = -1;
    }

    /**
     * Create a new SimImageState based on another SimImageState.
     */
    SimImageState(SimImageState other)
    {
        this.rsrc = other.rsrc;
        this.img = other.img;
        this.w = other.w;
        this.h = other.h;
        this.errorText = other.errorText;
        this.nframes = other.nframes;
    }

    /**
     * Start loading an AWT image.
     */
    void start(ImageObserver observer)
    {
        Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, observer);
    }

    /**
     * Resources call this when some more of the image has been decoded.
     */
    synchronized public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h)
    {
        //
        // store size if it's available
        //
        
        if ((flags & WIDTH) != 0) {
            this.w = w;
        }
        if ((flags & HEIGHT) != 0) {
            this.h = h;
        }
        
        //
        // check limits
        //

        if (w != -1 && rsrc.app.checkLimit("max image width", w, LIMIT_IMAGE_WIDTH)) {
            errorText = "max image width exceeded (" + w + " > " + LIMIT_IMAGE_WIDTH + ")";
        }
        if (h != -1 && rsrc.app.checkLimit("max image height", h, LIMIT_IMAGE_HEIGHT)) {
            errorText = "max image height exceeded (" + h + " > " + LIMIT_IMAGE_HEIGHT + ")";
        }

        //
        // check for errors
        //
        
        if ((flags & ERROR) != 0) {
            errorText = "could not decode image";
        }

        //
        // did we get another frame? if so, repaint.
        //
        
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
            ++nframes;
            rsrc.repaint();
        }

        //
        // handle errors
        //
        
        if (errorText != null) {
            this.img = null;
            return false;
        }

        //
        // keep updating until we're done
        //

        return (flags & (ALLBITS | ABORT)) == 0;
    }

    /**
     * A helper for scaling an image according to a view's resourceFlags.
     */
    void setScaledImage(SimView view, Image image)
    {
        int fit = view.resourceFlags & RSRC_IMAGE_MASK;
        if (fit == 0) {
            this.img = image;
            return;
        }

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width == -1 || height == -1) {
            this.img = null;
            return;
        }

        float scalew = (float)view.width  / (float)width;
        float scaleh = (float)view.height / (float)height;
        float scale = 1.0f;
        switch (fit) {
          case RSRC_IMAGE_HFIT:
            scale = scalew;
            break;
          case RSRC_IMAGE_VFIT:
            scale = scaleh;
            break;
            
          case RSRC_IMAGE_BESTFIT: {
              if ((float)width * scaleh > view.width) {
                  scale = scalew;
              } else {
                  scale = scaleh;
              }
              break;
          }
        }

        this.w = (int)((float)width  * scale);
        this.h = (int)((float)height * scale);
        this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = (Graphics2D)img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Object hint;
        if (scale > 1) {
            hint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        } else {
            hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        g.scale(scale, scale);
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Paint the image. The "resourceRendered" object inside the view is the
     * cached image state. If the cached state doesn't exist or has a different
     * number of frames, recreate it.
     */
    synchronized void paintHME(SimObject parent, Graphics2D g)
    {
        if (img == null || w == -1 || h == -1) {
            return;
        }
            
        SimView view = (SimView)parent;

        //
        // is the cached image state out of date?
        //

        SimImageState cached = (SimImageState)view.resourceRendered;
        boolean render = false;
        if (cached == null) {
            view.resourceRendered = cached = new SimImageState(this);
            render = true;
        } else {
            render = cached.nframes != nframes;
        }
        if (render) {
            cached.setScaledImage(view, img);
            if (cached.img == null) {
                return;
            }
        }

        //
        // draw the image, obey resourceFlags
        //

        int x = SimResource.getX(view, cached.w);
        int y = SimResource.getY(view, cached.h);

        g.drawImage(cached.img, x, y, this);
    }
    
    static String flagsToString(int flags)
    {
        StringBuffer buf = new StringBuffer();
        if ((flags & WIDTH)      != 0) { buf.append(", WIDTH");      }
        if ((flags & HEIGHT)     != 0) { buf.append(", HEIGHT");     }
        if ((flags & PROPERTIES) != 0) { buf.append(", PROPERTIES"); }
        if ((flags & SOMEBITS)   != 0) { buf.append(", SOMEBITS");   }
        if ((flags & FRAMEBITS)  != 0) { buf.append(", FRAMEBITS");  }
        if ((flags & ALLBITS)    != 0) { buf.append(", ALLBITS");    }
        if ((flags & ERROR)      != 0) { buf.append(", ERROR");      }
        if ((flags & ABORT)      != 0) { buf.append(", ABORT");      }
        return (buf.length() > 0) ? buf.substring(2) : buf.toString();
    }

    public String toString()
    {
        return "ImageState [id=" + rsrc.id + ((img != null) ? " " : "!img ") + w + "x" + h + " [nframes=" + nframes + "]";
    }
}
