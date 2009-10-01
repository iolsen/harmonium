//////////////////////////////////////////////////////////////////////
//
// File: SimResource.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.io.FastOutputStream;

/**
 * A resource which can be embedded in multiple views.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
abstract class SimResource extends SimObject
{
    static int resourceCount = 0;
    static int resourceTextCount = 0;
    static int resourceFontCount = 0;    
    static int resourceColorCount = 0;
    static int resourceImageCount = 0;
    static int resourceStreamCount = 0;
    static int resourceSoundCount = 0;
    static int resourceAnimCount = 0;

    static int resourceGCCount = 0;
    static int resourceGCTextCount = 0;
    static int resourceGCFontCount = 0;    
    static int resourceGCColorCount = 0;
    static int resourceGCImageCount = 0;
    static int resourceGCStreamCount = 0;
    static int resourceGCSoundCount = 0;
    static int resourceGCAnimCount = 0;

    protected static Map glyphCache = new HashMap();

    // for measuring text
    protected static Graphics2D textG =
        new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR).createGraphics();
    
    SimResource(SimApp app, int id)
    {
        super(app, id);
        resourceCount++;
        resourceGCCount++;
    }

    protected void finalize() throws Throwable
    {
        resourceGCCount--;
        super.finalize();
    }

    
    //
    // SimObject overrides
    //
    // Note that a resource doesn't have any child space of its own. Drawing is
    // entirely determined by where the parents want to draw.
    //

    Graphics2D getChildGraphics(Graphics2D g)
    {
        return g;
    }

    Rectangle getDrawingBounds()
    {
        return null;
    }
    
    List getAbsoluteBounds()
    {
        List list = new Vector();
        for (int i = nparents; i-- > 0;) {
            list.addAll(parents[i].getAbsoluteBounds());
        }
        return list;
    }

    void toChildSpace(Point p)
    {
    }

    void toParentSpace(Rectangle bounds)
    {
    }


    
    //
    // resource stuff - the defaults don't do much
    //
    
    void start()
    {
    }

    void close()
    {
        close(RSRC_STATUS_CLOSED);
    }

    void close(int status)
    {
        resourceCount--;
        Simulator.get().resourceUpdate();
    }
    
    void setSpeed(float speed)
    {
    }

    void setActive(boolean active)
    {
        throw new RuntimeException("setActive not supported on " + this);
    }

    void processEvent(HmeEvent event)
    {
        if (app != null) {
            app.processEvent(event);
        }
    }
    
    void sendEvent(byte buf[], int off, int len)
    {
    }

    /**
     * Send resource info to the containing app.
     */
    void sendResourceInfo(int status, Map map)
    {
        if (Simulator.get().sim.getApp() != null) {
            if (map == null) {
                map = new HashMap();
            }
            appendResourceInfo(map);
            Simulator.get().sim.getApp().processEvent(new HmeEvent.ResourceInfo(id, status, map));
        }
    }


    /**
     * For resources that send back data in the info event implement this method.
     */
    void appendResourceInfo(Map map)
    {
        
    }
    
    //
    // some static helpers for resource subclasses
    //
    
    /**
     * A helper for positioning a view based on the view's resourceFlags.
     */
    static int getX(SimView view, int rsrcWidth)
    {
        int x;
        switch (view.resourceFlags & RSRC_HALIGN_MASK) {
          case RSRC_HALIGN_LEFT:
            x = 0;
            break;
          default:
          case RSRC_HALIGN_CENTER:
            x = (view.width - rsrcWidth) / 2;
            break;
          case RSRC_HALIGN_RIGHT:
            x = view.width - rsrcWidth;
            break;
        }
        return x;
    }

    /**
     * A helper for positioning a view based on the view's resourceFlags.
     */
    static int getY(SimView view, int rsrcHeight)
    {
        int y;
        switch (view.resourceFlags & RSRC_VALIGN_MASK) {
          case RSRC_VALIGN_TOP:
            y = 0;
            break;
          default:
          case RSRC_VALIGN_CENTER:
            y = (view.height - rsrcHeight) / 2;
            break;
          case RSRC_VALIGN_BOTTOM:
            y = view.height - rsrcHeight;
            break;
        }
        return y;
    }

    /**
     * Pad a string to a certain size.
     */
    static void pad(StringBuffer buf, String s, int width, char c)
    {
        width -= s.length();
        if (width > 0) {
            while (width-- > 0) {
                buf.append(c);
            }
        }
        buf.append(s);
    }

    /**
     * Append a color string to a StringBuffer.
     */
    static void appendColor(StringBuffer buf, Color c)
    {
        buf.append(",0x");
        int argb = c.getRGB();
        pad(buf, Integer.toHexString(argb), 8, '0');
        if ((argb >> 24 & 0xff) == 0) {
            buf.append(" !alpha");
        }
    }

    /**
     * Color resource.
     */
    static class ColorResource extends SimResource
    {
        Color color;

        ColorResource(SimApp app, int id, Color color)
        {
            super(app, id);
            resourceColorCount++;
            resourceGCColorCount++;
            this.color = color;
            
        }

        void close()
        {
            resourceColorCount--;
            super.close();
        }

        protected void finalize() throws Throwable
        {
            resourceGCColorCount--;
            super.finalize();
        }
        
        protected void paintHME(SimObject parent, Graphics2D g)
        {
            SimView view = (SimView)parent;
            g.setColor(color);
            g.fillRect(0, 0, view.width, view.height);
        }
        String getIcon()
        {
            return "color.png";
        }
        void toString(StringBuffer buf)
        {
            appendColor(buf, color);
        }

    }

    /**
     * TrueType resource.
     */
    static class FontResource extends SimResource
    {
        Font font;
        String name;

        FontResource(SimApp app, int id, Font font, int flags)
        {
            super(app, id);
            resourceFontCount++;
            resourceGCFontCount++;
            this.font = font;
            this.name = font.getFontName();
            sendResourceInfo(RSRC_STATUS_COMPLETE, null);
            sendFontInfo( flags );
        }

        protected void finalize() throws Throwable
        {
            resourceGCFontCount--;
            super.finalize();
        }
        
        void close() 
        {
            resourceFontCount--;
            super.close();
        }

        FontResource(SimApp app, int id, String name)
        {
            super(app, id);
            this.name = name;
            InputStream in = Simulator.getResourceAsFastStream(name);
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, in);
                sendResourceInfo(RSRC_STATUS_COMPLETE, null);
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        /**
         * Retreive the glyphMap from the glyph cache if available
         * Otherwise create a new one.
         * 
         * Glyphs are cached in the simulator because later on, at render time
         * The simulator may need the table. This is not totally implemented yet
         * but getting the cache in place is a baby step in that direction.
         */
        Map createGlyphMapCached()
        {
            Map glyphMap = (Map)glyphCache.get(getFontDescriptor());
            if(glyphMap != null) {
                return glyphMap;
            }
                
            // get the glyph metrics for first 256 glyphs
            glyphMap = new HashMap(256);
            char chars[] = new char[256];
            for( int i = 0; i < 256; i++) {
                chars[i] = (char) i;
            }
            GlyphVector glyphVector = font.createGlyphVector( textG.getFontRenderContext(), chars );
            for( char c = 0; c < glyphVector.getNumGlyphs(); c++ ) {
                GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics( c );
                HmeEvent.FontInfo.GlyphInfo glyphInfo = new HmeEvent.FontInfo.GlyphInfo( 
                    c, 
                    glyphMetrics.getAdvance(), 
                    glyphMetrics.getLSB() + (float) glyphMetrics.getBounds2D().getWidth()
                    );
                glyphMap.put( new Character( c ), glyphInfo );
            }

            glyphCache.put(getFontDescriptor(), glyphMap);

            return glyphMap;
        }
        
        void sendFontInfo(int flags)
        {
            if (0 != (flags & (FONT_METRICS_BASIC | FONT_METRICS_GLYPH))) {
                Map glyphMap = null;

                if (0 != (flags & FONT_METRICS_GLYPH)) {
                    glyphMap = createGlyphMapCached();
                }
                
                // get the general font metrics
                FontMetrics fm = textG.getFontMetrics(font);

                // send the metrics
                Simulator.get().sim.getApp().processEvent(
                        new HmeEvent.FontInfo( 
                                id,
                                fm.getAscent(), fm.getDescent(),
                                fm.getHeight(), fm.getLeading(),
                                glyphMap
                        )
                );
            }
        }

        /**
         * Used as key for the glyph cache
         * @return a string that describes this font 
         */
        String getFontDescriptor()
        {
            return name + "-" + font.getSize() + "-" +
                (font.isBold() ? "bold" : (font.isItalic() ? "italic" : "plain"));
        }
        
        void toString(StringBuffer buf)
        {
            buf.append("," + font);
        }

    }

    /**
     * Text resource.
     */
    static class TextResource extends SimResource
    {
        Font font;
        Color color;
        String text;

        TextResource(SimApp app, int id, Font font, Color color, String text)
        {
            super(app, id);
            resourceTextCount++;
            resourceGCTextCount++;
            this.font = font;
            this.color = color;
            this.text = text;
        }

        protected void finalize() throws Throwable
        {
            resourceGCTextCount--;
            super.finalize();
        }

        void close() 
        {
            resourceTextCount--;
            super.close();
        }

        protected void paintHME(SimObject parent, Graphics2D g)
        {
            SimView view = (SimView)parent;

            g.setFont(font);
            g.setColor(color);
            
            FontMetrics fm = g.getFontMetrics();

            //
            // cache the wrapped text inside the view, so we don't do this every
            // time.
            //
            
            boolean containsNewLine = (text.indexOf("\n") != -1) || (text.indexOf("\r") != -1);
            String cached = (String)view.resourceRendered;
            if (cached == null) {
            	view.resourceRendered = cached = layout(view, fm, text);
            }

            //
            // if the text is wrapped, draw each line. Otherwise just draw the
            // single line.
            //

            int lineHeight = fm.getHeight();
                
            if (   ((view.resourceFlags & RSRC_TEXT_WRAP) != 0)
                || containsNewLine) {
                StringTokenizer tokens = new StringTokenizer(cached, "\n");
                int nlines = tokens.countTokens();

                
                int y = getY(view, nlines * lineHeight) + fm.getAscent();
                tokens = new StringTokenizer(cached, "\n", true);
                while (tokens.hasMoreTokens()) {
                    if (y > view.height) {
                        break;
                    }
                    String line = tokens.nextToken();
                    if (line.equals("\n")) {
                        y += lineHeight;
                    } else {
                        drawText(view, g, fm, line, y);
                    }
                }
            } else {
                int y = getY(view, lineHeight) + fm.getAscent();
                drawText(view, g, fm, cached, y);
            }
        }

        
        //
        // wrap text - this inserts \n at the end of each word-wrapped line
        // NOTE: this layout method may get moved to a common place since the algorithm
        //       is part of the view class. Better to put in shared place than duplicate.
        String layout(SimView view, FontMetrics metrics, String text)
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

                int txtWidth = metrics.charsWidth(chars, start, (i - start) + 1);
                if (txtWidth > view.width || isNewline) {
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
        // draw a line while obeying resourceFlags
        //

        void drawText(SimView view, Graphics g, FontMetrics fm, String text, int y)
        {
            g.drawString(text, getX(view, fm.stringWidth(text)), y);
        }

        String getIcon()
        {
            return "text.png";
        }
        
        void toString(StringBuffer buf)
        {
            buf.append(",\"" + text + "\"");
            appendColor(buf, color);        
        }
    }

    /**
     * Image resource.
     */
    static class ImageResource extends SimResource implements ImageObserver
    {
        SimImageState image;

        ImageResource(SimApp app, int id, Image img)
        {
            super(app, id);
            resourceImageCount++;
            resourceGCImageCount++;
            image = new SimImageState(this, img);
            image.start(this);
        }

        protected void finalize() throws Throwable
        {
            resourceGCImageCount--;
            super.finalize();
        }
        
        
        void close() 
        {
            resourceImageCount--;
            super.close();
        }

        protected void paintHME(SimObject parent, Graphics2D g)
        {
            if (image != null) {
                image.paintHME(parent, g);
            }
        }


        public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h)
        {
            return image.imageUpdate(img, flags, x, y, w, h);
        }
        
        String getIcon()
        {
            return "image.png";
        }
        
        void toString(StringBuffer buf)
        {
            if (image == null) {
                buf.append(",null");
            } else {
                buf.append("," + image.w + "x" + image.h);
            }
        }
    }

    /**
     * Sound resource.
     */
    static class SoundResource extends SimResource
    {
        Clip clip;
        
        SoundResource(SimApp app, int id, byte buf[], int off, int len)
        {
            super(app, id);
            resourceSoundCount++;
            resourceGCSoundCount++;
            load(buf, off, len);
        }

        SoundResource(SimApp app, int id, String name)
        {
            super(app, id);
            resourceSoundCount++;
            resourceGCSoundCount++;
            try {
                FastOutputStream out = Simulator.getResourceBytes(name);
                load(out.getBuffer(), 0, (int)out.getCount());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void finalize() throws Throwable
        {
            resourceGCSoundCount--;
            super.finalize();
        }

        void close() 
        {
            resourceSoundCount--;
            super.close();
        }

        void load(byte buf[], int off, int len)
        {
            if (!Simulator.SOUND) {
                return;
            }

            //
            // play the sound
            //
            
            if (len > 0) {
                AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(Clip.class, format);
            
                try {
                    clip = (Clip)AudioSystem.getLine(info);
                    clip.open(format, buf, off, len);
                } catch (IllegalArgumentException e) {
                    Simulator.SOUND = false;
                    System.out.println("WARNING: No Sound Device Available. Sound Disabled.");
                } catch (LineUnavailableException e) {
                    Simulator.SOUND = false;
                    System.out.println("WARNING: No Sound Device Available. Sound Disabled.");
                }
            }
        }
        
        void setSpeed(float speed)
        {
            if (!Simulator.SOUND) {
                return;
            }
            if ((speed == 1f) && (clip != null)) {
                clip.setFramePosition(0);
                clip.start();
            }
        }
    }

    /**
     * Animation resource.
     */
    static class AnimResource extends SimResource
    {
        int duration;
        float ease;
        
        AnimResource(SimApp app, int id, int duration, float ease)
        {
            super(app, id);
            resourceAnimCount++;
            resourceGCAnimCount++;            
            this.duration = duration;
            this.ease = ease;
        }

        protected void finalize() throws Throwable
        {
            resourceGCAnimCount--;
            super.finalize();
        }
        
        
        void close() 
        {
            resourceAnimCount--;
            super.close();
        }


        float interpolate(float percent)
        {
            //
            // linear
            //
            
            if (ease == 0) {
                return percent;
            }

            //
            // ease in
            //
            
            if (ease < 0) {
                float a = -ease / 2.0f;
                float v0 = 2.0f / (2.0f - a);
                if (percent < a) {
                    return (v0 * percent * percent) / (2.0f * a);
                }
                return (v0 * (2.0f * percent - a)) / 2.0f;
            }
            
            //
            // ease out
            //
            
            float b = 1.0f - (ease / 2.0f);
            float v0 = 2.0f / (1.0f + b);
            if (percent < b) {
                return v0 * percent;
            }
            return (b * v0) + ((percent - b + ((b * b - percent * percent) / 2.0f)) * v0 / (1.0f - b));
        }
    }
}
