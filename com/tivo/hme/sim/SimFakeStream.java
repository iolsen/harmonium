//////////////////////////////////////////////////////////////////////
//
// File: SimFakeStream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.tivo.hme.host.http.client.HttpClient;

/**
 * A fake stream resource. This is displayed on screen in place of video.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
class SimFakeStream extends SimStreamResource
{
    //
    // duration of the fake video, in seconds
    //
    
    final static int DURATION = 60;

    Font font;
    Font smfont;
    long pos;
    boolean TiVoStream;
    
    public SimFakeStream(SimApp app, int id, String url, boolean play)
    {
        super(app, id, url, play);
        FontResource rsrc = (FontResource)app.getResource(ID_SYSTEM_TTF, null);
        font = rsrc.font.deriveFont(Font.PLAIN, 32);
        smfont = rsrc.font.deriveFont(Font.PLAIN, 12);
    }

    void handle(HttpClient http) throws IOException
    {
        if (http == null) {
            TiVoStream = true;
        }
        setStatus(RSRC_STATUS_READY);
        if (play) {
            speed = 1;
            status = RSRC_STATUS_PLAYING;
        }

        //
        // fake video size
        //
        
        Map m = new HashMap();
        m.put("width", "320");
        m.put("height", "240");
        sendResourceInfo(m);

        //
        // loop for DURATION seconds, repainting occasionally.
        //
        
        if (TiVoStream) {
            return;
        }

        long tm0 = System.currentTimeMillis();
        try {
            while (status < RSRC_STATUS_CLOSED && (pos < DURATION * 1000)) {
                // are we paused?
                if (speed == 0) {
                    synchronized (this) {
                        wait();
                    }
                    tm0 = System.currentTimeMillis() - pos;
                } else {
                    // sleep a few...
                    Thread.sleep(100);
                    pos = Math.min(DURATION * 1000, System.currentTimeMillis() - tm0);
                    repaint();
                }
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * Pretty print a time in ms.
     */
    static String timeString(long ms)
    {
        StringBuffer buf = new StringBuffer();
        int duration = (int)(ms / 1000);
        int sec = duration % 60;
        int min = (duration / 60) % 60;
        int hrs = duration / 3600;
        if (hrs > 0) {
            if (hrs > 9) {
                buf.append(hrs / 10);
            }
            buf.append(hrs % 10);
            buf.append(':');
        }
        buf.append(min / 10);
        buf.append(min % 10);
        buf.append(':');
        buf.append(sec / 10);
        buf.append(sec % 10);
        buf.append('.');
        buf.append((ms % 1000) / 100);
        return buf.toString();
    }

    /**
     * Paint the fake stream.
     */
    protected void paintHME(SimObject parent, Graphics2D g)
    {
        
        SimView view = (SimView)parent;

        // bg
        g.setColor(Color.black);
        g.fillRect(0, 0, view.width, view.height);

        if (TiVoStream) {
            FontMetrics fm = g.getFontMetrics();
            g.setFont(smfont);
            g.setColor(Color.white);
            g.drawString(url, 10, 5 + fm.getHeight());
        } else {
            // fg
            String text = timeString(pos);

            g.setFont(font);
            g.setColor(Color.white);
            FontMetrics fm = g.getFontMetrics();
            int x = (view.width - fm.stringWidth(text)) / 2;
            int y = (view.height + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(text, x, y);
        }
    }

    //
    // overrides from SimStreamResource
    //
    
    synchronized void setSpeed(float speed)
    {
        super.setSpeed(speed);
        notify();
    }

    protected long getPosition()
    {
        return pos;
    }

    protected long getDuration()
    {
        return DURATION * 1000;
    }
}

