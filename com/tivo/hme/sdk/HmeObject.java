//////////////////////////////////////////////////////////////////////
//
// File: HmeObject.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

import java.awt.Color;
import java.awt.Image;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.interfaces.IHmeConstants;

/**
 * The superclass for HME views and resources.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public abstract class HmeObject implements IHmeEventHandler, IHmeProtocol
{
    private Application app;
    private int id;
    
    /**
     * Constructor. Use the given id.
     */
    HmeObject(Application app, int id)
    {
        this.app = app;
        this.id = id;
    }

    /**
     * The application which contains the object.
     */
    public Application getApp()
    {
        return app;
    }

    /**
     * The HME protocol id for the object.
     */
    public int getID()
    {
        return id;
    }

    /**
     * HME allowed to set the application but not HME apps.
     * REMIND: should this be package-private or public?
     * 
     * @param app The application being set for this object
     */
    void setApp(Application app)
    {
        this.app = app;
    }

    /**
     * HME allowed to change the ID but not HME apps.
     * REMIND: should this be package-private or public?
     * 
     * @param id the ID of the HmeObject you are setting.
     */
    void setID(int id)
    {
        this.id = id;
    }

    /**
     * Get the context for the application which contains the object.
     */
    public IContext getContext()
    {
        return app.getContext();
    }

    //
    // Events
    //
    
    /**
     * Post an event to the object.
     */
    public void postEvent(HmeEvent event)
    {
        handleEvent(event);
    }

    /**
     * Handle an event. Return true when the event is consumed.
     */
    public boolean handleEvent(HmeEvent event)
    {
        switch (event.getOpCode()) {
          case EVT_KEY: {
              HmeEvent.Key ir = (HmeEvent.Key) event;
              switch (ir.getAction()) {
                case KEY_PRESS:
                  return handleKeyPress(ir.getCode(), ir.getRawCode());
                case KEY_REPEAT:
                  return handleKeyRepeat(ir.getCode(), ir.getRawCode());
                case KEY_RELEASE:
                  return handleKeyRelease(ir.getCode(), ir.getRawCode());
              }
              break;
          }
        }
        return false;
    }

    /**
     * Handle a key press event. Return true when the event is consumed.
     */
    public boolean handleKeyPress(int code, long rawcode)
    {
        return false;
    }

    /**
     * Handle a key repeat event. Return true when the event is consumed.
     */
    public boolean handleKeyRepeat(int code, long rawcode)
    {
        return false;
    }

    /**
     * Handle a key release event. Return true when the event is consumed.
     */
    public boolean handleKeyRelease(int code, long rawcode)
    {
        return false;
    }

    //
    // resources
    //

    /**
     * Get a resource by key using {@link Application#getResource(Object)}.
     * @link Application#getResource(Object)
     */
    public Resource getResource(Object key)
    {
        return app.getResource(key);
    }

    /**
     * Create a color resource.
     */
    public Resource createColor(Color color)
    {
        return new Resource.ColorResource(app, color);
    }

    /**
     * Create a true type resource. The family should be either a filename or
     * <code>default.ttf</code> to use the receiver's default font.
     */
    public Resource createTrueType(String family)
    {
        return new Resource.TrueTypeResource(app, family);
    }

    /**
     * Create a font resource.
     * @param family the true type family name as given to createTrueType
     * @param style the glyph style (e.g. bold/italics)
     * @param size the point size
     * @return the font resource
     */
    public Resource createFont(String family, int style, int size)
    {
        return createFont( family, style, size, 0 );
    }

    /**
     * Create a font resource with metric measurement flags.
     * @param family the true type family name as given to createTrueType
     * @param style the glyph style (e.g. bold/italics)
     * @param size the point size
     * @param flags the font metric flags
     * @return the font resource
     */
    public Resource createFont(String family, int style, int size, int flags)
    {
        return new Resource.FontResource(app, getResource(family), style, size, flags); 
    }
    
    /**
     * Create a text resource.
     * @param font the font resource used to paint the text
     * @param color the foreground color for the text
     * @param text the text string
     * @return the text resource
     */
    public Resource createText(Object font, Object color, String text)
    {
        return new Resource.TextResource(app, getResource(font), getResource(color), text );
    }

    /**
     * Create a sound resource.
     *
     * The sound format is 8,000 Hz signed 16-bit little endian mono PCM.
     * 
     * @param name a sound file.
     */
    public SoundResource createSound(String name)
    {
        return new SoundResource(app, name);
    }

    /**
     * Create a sound from a byte buffer.
     * 
     * @param buf The buffer containing the sound bytes
     */
    public SoundResource createSound(byte buf[])
    {
        return createSound(buf, 0, buf.length);
    }

    /**
     * Create a sound from a byte buffer containing PCM Sound.
     * 
     * @param buf The buffer containing the sound bytes
     * @param off The offset into the byte buffer to start reading
     * @param len The number of bytes to read
     */
    public SoundResource createSound(byte buf[], int off, int len)
    {
        return new SoundResource.SoundResourceBytes(app, buf, off, len);
    }

    /**
     * Create an image resource. Name should be a PNG or JPG image file.
     */
    public ImageResource createImage(String name)
    {
        return new ImageResource.ImageResourceFile(app, name);
    }

    /**
     * Create an image resource from an AWT image.
     */
    public ImageResource createImage(Image image)
    {
        return new ImageResource.ImageResourceImage(app, image);
    }
    
    /**
     * Create an image from an entire byte buffer.
     */
    public ImageResource createImage(byte buf[])
    {
        return createImage(buf, 0, buf.length);
    }

    /**
     * Create an image from a byte buffer containing a PNG or JPG image file.
     */
    public ImageResource createImage(byte buf[], int off, int len)
    {
        return new ImageResource.ImageResourceBytes(app, buf, off, len);
    }

    /**
     * Create a new stream resource with the given uri using {@link
     * #createStream(String,String,boolean)}.
     */
    public StreamResource createStream(String uri)
    {
        return createStream(uri, null, true);
    }

    /**
     * Create a new stream resource.
     * @param uri the uri for the stream
     * @param contentType the content type for the stream. It is not necessary
     * to specify the content type in most cases.
     * @param play by default, audio and video streams are paused when
     * created. if play is true the stream will be played immediately.
     */
    public StreamResource createStream(String uri, String contentType, boolean play)
    {
        return new StreamResource(app, uri, contentType, play);
    }

    /**
     * Create a new HME application stream resource. This method is used to
     * embed one application within another. The arguments will be appended to
     * the uri.
     */
    public StreamResource createStream(String uristr, Map args)
    {
        URL uri;
        try {
            uri = new URL(uristr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
            
        //Query query = new Query(uri);
        Map queryMap = parseQuery(uri.getQuery());

        StringBuffer buf = new StringBuffer();
        buf.append(uri.getProtocol());
        buf.append("://");
        buf.append(uri.getHost());
        buf.append(':');
        buf.append(uri.getPort());
        buf.append(uri.getPath());

        // this is a bit convoluted because it preserves order

        // add the keys from the Query (but use values from args if available)
        boolean first = true;
        Iterator i = queryMap.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            String value = (args == null) ? null : (String)args.get(key);
            if (value == null) {
                value = (String)queryMap.get(key);
            }
            buf.append(first ? '?' : '&');
            try {
                buf.append(URLEncoder.encode(key, "UTF-8"));
                buf.append('=');
                buf.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            first = false;
        }
            
        // add the keys from args (but don't add any keys that we're already in query)
        if (args != null) {
            i = args.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                String value = (String)queryMap.get(key);
                if (value == null) {
                    value = (String)args.get(key);
                    buf.append(first ? '?' : '&');
                    try {
                        buf.append(URLEncoder.encode(key, "UTF-8"));
                        buf.append('=');
                        buf.append(URLEncoder.encode(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    first = false;
                }
            }
        }

        return createStream(buf.toString(), IHmeConstants.MIME_TYPE, true);
    }

    /**
     * Parse the query.
     */
    public Map parseQuery(String query)
    {
        Map map = LinkedHashMap();
        if (query == null || query.indexOf('=') == -1) {
            return null;
        }

        int at = 0;
        int len = query.length();

        if (query.startsWith("?")) {
            ++at;
        }
        do {
            // find = and &
            int equal = query.indexOf('=', at);
            if (equal < 0) {
                throw new IllegalArgumentException("invalid query (trailing key, = not found)");
            }
            int amp = query.indexOf('&', equal);
            if (amp == -1) {
                amp = query.length();
            }

            // add the key/value
            try {
                String key = URLDecoder.decode(query.substring(at, equal), "UTF-8");
                String value = URLDecoder.decode(query.substring(equal + 1, amp), "UTF-8");
                map.put(key.toLowerCase(), value);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // advance
            at = amp + 1;
        } while (at < len);
        return map;
    }
    
    /**
     * @return
     */
    private Map LinkedHashMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Create a linear animation over the given duration.
     */
    public Resource createAnimation(int duration)
    {
        return createAnimation(duration, 0);
    }

    /**
     * Create an animation over the given duration. An ease value of zero will
     * create a linear animation. An ease value between -1..0 will accelerate
     * gradually into the animation. An ease value between 0..1 will decelerate
     * gradually out of the animation.
     */
    public Resource createAnimation(int duration, float ease)
    {
        return new Resource.AnimResource(app, duration, ease);
    }

    //
    // Misc
    //
    
    /**
     * Flush the application using {@link Application#flush}.
     * @link Application#flush
     */
    public void flush()
    {
        app.flush();
    }

    /**
     * Play a sound
     */
    public void play(String name)
    {
        SoundResource snd = (SoundResource)getResource(name);
        if (snd != null) {
            snd.play();
        }
    }

    /**
     * Append attributes to the string buffer. This method is called by
     * {@link #toString}.
     */
    protected void toString(StringBuffer buf)
    {
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        String nm = getClass().getName();
        nm = nm.substring(nm.lastIndexOf('.') + 1);
        buf.append(nm);
        buf.append("[#");
        buf.append(id);
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * Turn resource flags into a string.
     */
    public static String rsrcFlagsToString(int flags)
    {
        StringBuffer buf = new StringBuffer();
        switch (flags & RSRC_HALIGN_MASK) {
          case RSRC_HALIGN_LEFT:   buf.append("left ");    break;
          case RSRC_HALIGN_CENTER: buf.append("hcenter "); break;
          case RSRC_HALIGN_RIGHT:  buf.append("right ");   break;
        }
        switch (flags & RSRC_VALIGN_MASK) {
          case RSRC_VALIGN_TOP:    buf.append("top ");     break;
          case RSRC_VALIGN_CENTER: buf.append("vcenter "); break;
          case RSRC_VALIGN_BOTTOM: buf.append("bottom ");  break;
        }
        if ((flags & RSRC_TEXT_WRAP) != 0) {
            buf.append("wrap ");
        }
        switch (flags & RSRC_IMAGE_MASK) {
          case RSRC_IMAGE_HFIT:    buf.append("hfit");    break;
          case RSRC_IMAGE_VFIT:    buf.append("vfit");    break;
          case RSRC_IMAGE_BESTFIT: buf.append("bestfit"); break;            
        }
        return buf.toString();
    }
    
}
