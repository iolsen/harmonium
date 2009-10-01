//////////////////////////////////////////////////////////////////////
//
// File: SoundResource.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

/**
 * A sound resource. See {@link HmeObject#createSound(String)}.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class SoundResource extends Resource
{
    String filename;

    SoundResource(Application app, String filename, int id)
    {
        super(app, id);
        this.filename = filename;
    }
            
    SoundResource(Application app, String filename)
    {
        super(app);
        this.filename = filename;
        if (filename == null) {
            throw new NullPointerException("SoundResource filename == null");
        }
        app.cmdRsrcAddSound(getID(), filename);
    }

    SoundResource(Application app)
    {
        super(app);
    }

    /**
     * Play the sound.
     */
    public void play()
    {
        getApp().cmdRsrcSetSpeed(getID(), 1.0f);
    }
        
    protected void toString(StringBuffer buf)
    {
        buf.append(",file=" + filename);
    }

    /**
     * Sound from byte array.
     */
    static class SoundResourceBytes extends SoundResource
    {
        byte buf[];
        int off;
        int len;
    
        SoundResourceBytes(Application app, byte buf[], int off, int len)
        {
            super(app);
            this.buf = buf;
            this.off = off;
            this.len = len;

            app.cmdRsrcAddSound(getID(), buf, off, len);
        }

    }
}
