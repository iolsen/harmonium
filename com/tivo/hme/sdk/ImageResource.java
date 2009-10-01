//////////////////////////////////////////////////////////////////////
//
// File: ImageResource.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.tivo.hme.sdk.io.FastOutputStream;


/**
 * An image resource.
 * See {@link HmeObject#createImage(String)}, and
 * {@link HmeObject#createImage(byte[],int,int)}.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public abstract class ImageResource extends Resource
{
    int width;
    int height;

    ImageResource(Application app, int width, int height)
    {
        super(app);
        this.width = width;
        this.height = height;
    }

    /**
     * Get the width of the image. The image will be loaded from disk if necessary.
     */
    public int getWidth()
    {
        if (width == -1) {
            loadSize(getImage());
        }
        return width;
    }
            
    /**
     * Get the height of the image. The image will be loaded from disk if necessary.
     */
    public int getHeight()
    {
        if (height == -1) {
            loadSize(getImage());
        }
        return height;
    }

    abstract Image getImage();

    void loadSize(Image image)
    {
        if (image.getWidth(null) == -1) {
            new ImageLoader(image);
        }
        width = image.getWidth(null);
        height = image.getHeight(null);
    }

    //
    // a helper class for watching an image load and finding out the width and
    // the height
    //
    
    private static class ImageLoader implements ImageObserver
    {
        boolean done;
        
        ImageLoader(Image image)
        {
            if (image.getWidth(this) == -1 || image.getHeight(this) == -1) {
                while (!done) {
                    try {
                        synchronized (this) {
                            wait(100);
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public synchronized boolean imageUpdate(Image image, int flags, int x, int y, int w, int h)
        {
            // do we have the width and height?
            if ((flags & WIDTH) != 0 && (flags & HEIGHT) != 0) {
                done = true;
                notify();
                return false;
            }

            // error?
            if ((flags & ERROR) != 0 || (flags & ABORT) != 0) {
                System.out.println("could not load image");
                done = true;
                notify();
                return false;
            }
            
            return true;
        }
    }

    protected void toString(StringBuffer buf)
    {
        buf.append("," + width + "x" + height);
    }

    /**
     * Image from an actual image.
     */
    static class ImageResourceImage extends ImageResource
    {
        Image image;
    
        ImageResourceImage(Application app, Image image)
        {
            super(app, -1, -1);
            this.image = image;
            app.cmdRsrcAddImage(getID(), image);
        }

        public Image getImage()
        {
            return image;
        }
    }
    
    /**
     * Image from byte array.
     */
    static class ImageResourceBytes extends ImageResource
    {
        byte buf[];
        int off;
        int len;
    
        ImageResourceBytes(Application app, byte buf[], int off, int len)
        {
            super(app, -1, -1);
            this.buf = buf;
            this.off = off;
            this.len = len;

            app.cmdRsrcAddImage(getID(), buf, off, len);
        }

        public Image getImage()
        {
            return Toolkit.getDefaultToolkit().createImage(buf, off, len);
        }
    }

    /**
     * Image from file.
     */
    static class ImageResourceFile extends ImageResource
    {
        String filename;

        ImageResourceFile(Application app, String filename)
        {
            super(app, -1, -1);
            this.filename = filename;
            
            if (filename == null) {
                throw new NullPointerException("ImageFileResource filename == null");
            }

            app.cmdRsrcAddImage(getID(), filename);
        }
    
        public Image getImage()
        {
            if (new File(filename).exists()) {
                return Toolkit.getDefaultToolkit().createImage(filename);
            }

            try {
                InputStream in = getApp().getStream(filename);
                if (in == null) {
                    return null;
                }
            
                try {
                    FastOutputStream out = new FastOutputStream(4096);
                    byte buf[] = new byte[4096];
                    while (true) {
                        int count = in.read(buf, 0, buf.length);
                        if (count < 0) {
                            break;
                        }
                        out.write(buf, 0, count);
                    }
                    return Toolkit.getDefaultToolkit().createImage(out.getBuffer(), 0, (int)out.getCount());
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) { }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void toString(StringBuffer buf)
        {
            super.toString(buf);
            buf.append(",file=" + filename);
        }
    }
}
