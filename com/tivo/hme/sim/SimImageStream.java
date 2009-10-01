//////////////////////////////////////////////////////////////////////
//
// File: SimImageStream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.Map;

import com.tivo.hme.host.http.client.HttpClient;
import com.tivo.hme.sdk.io.FastInputStream;
import com.tivo.hme.sdk.io.FastOutputStream;

/**
 * An image read from a stream.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
class SimImageStream extends SimStreamResource implements ImageObserver
{
    SimImageState image;
    
    public SimImageStream(SimApp parent, int id, String url, boolean play)
    {
        super(parent, id, url, play);
    }

    /**
     * Handle the response by draining the stream and reading the image.
     */
    void handle(HttpClient http) throws IOException
    {
        //
        // drain the stream
        //
        
        byte buf[] = new byte[1024];
        FastOutputStream out = new FastOutputStream(4096);
        FastInputStream in = new FastInputStream(http.getInputStream(), 1024);
        while (true) {
            int n = in.read(buf, 0, buf.length);
            if (n < 0) {
                break;
            }
            out.write(buf, 0, n);
            if (app.checkLimit("max image bytes", out.getCount(), LIMIT_IMAGE_NBYTES)) {
                error(RSRC_ERROR_BAD_DATA, "too many image bytes");
                return;
            }
            
        }

        //
        // did we get anything?
        //
        
        if (out.getCount() == 0) {
            error(RSRC_ERROR_CONNECTION_LOST, "Connection lost.");
            return;
        }

        //
        // turn the bytes into an image
        //

        image = new SimImageState(this, Toolkit.getDefaultToolkit().createImage(out.getBuffer(), 0, (int)out.getCount()));
        image.start(this);
    }

    /**
     * Paint the image.
     */
    protected void paintHME(SimObject parent, Graphics2D g)
    {
        if (image != null) {
            image.paintHME(parent, g);
        }
    }

    /**
     * AWT calls this when some more of the image has been decoded.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h)
    {
        //
        // update state
        //
        
        int nframes = image.nframes;
        boolean keepDecoding = image.imageUpdate(img, flags, x, y, w, h);

        //
        // update status
        //

        if (nframes == 0 && image.nframes > 0) {
            setStatus(RSRC_STATUS_COMPLETE);
        } else if (image.errorText != null) {
            error(RSRC_ERROR_BAD_DATA, image.errorText);
        }

        //
        // if we don't have any frames, keep decoding if necessary
        //

        return keepDecoding;
    }
    

    /**
     * Add image width/height to the rsrc info.
     */
    void appendResourceInfo(Map map)
    {
        if (image != null && image.img != null && image.w != -1 && image.h != -1) {
            map.put("width", String.valueOf(image.w));
            map.put("height", String.valueOf(image.h));
        }
    }
    
    //
    // overrides from SimStreamResource
    //
    
    String getIcon()
    {
        return "image.png";
    }
}
