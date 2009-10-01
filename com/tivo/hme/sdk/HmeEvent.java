//////////////////////////////////////////////////////////////////////
//
// File: HmeEvent.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.tivo.core.ds.TeDict;
import com.tivo.core.ds.TeIterator;
import com.tivo.hme.sdk.io.HmeInputStream;
import com.tivo.hme.sdk.io.HmeOutputStream;

/**
 * A superclass for HME events.
 * 
 * @author Adam Doppelt
 * @author Arthur van Hoff
 * @author Brigham Stevens
 * @author Jonathan Payne
 * @author Steven Samorodin
 */
@SuppressWarnings("unchecked")
public abstract class HmeEvent implements IHmeProtocol
{
    /** The protocol opcode for the event. */
    private int opcode;

    /** The id of the destination resource. */
    private int id;


    /**
     * Construct an HME event, must be called by subclasses.
     *
     * @param opcode the type of the event, e.g. EVT_RSRC_INFO, EVT_APP_INFO,
     * or Resource.EVT_RSRC_STATUS (if the rsrc status chanaged)
     * @param id the identifier of the target resource
     */
    protected HmeEvent(int opcode, int id)
    {
        this.opcode = opcode;
        this.id = id;
    }
    
    /**
     * @return the opcode of this event
     */
    public int getOpCode()
    {
        return opcode;
    }
    
    /**
     * @return the ID of this event
     */
    public int getID()
    {
        return id;
    }

    /**
     * Write the event to a stream.
     */
    public void write(HmeOutputStream out) throws IOException
    {
        throw new IOException("not a writable event");
    }

    //
    // helpers for reading/writing maps
    //

    /**
     * Helper for reading a map from a stream.
     */
    protected void readMap(HmeInputStream in, Map map) throws IOException
    {
        int size = (int)in.readVInt();
        for (int i = 0 ; i < size ; i++) {
            map.put(in.readUTF(), in.readUTF());
        }
    }

    /**
     * Helper for writing a map to a steam.
     */
    protected void writeMap(HmeOutputStream out, Map map) throws IOException
    {
        out.writeVInt(map.size());
        Iterator i = map.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            String value = (String)map.get(key);
            out.writeUTF(key);
            out.writeUTF(value);
        }
    }

    /**
     * A key event.
     */
    public static class Key extends HmeEvent
    {
        static Map codeToString;
        static Map stringToCode;        

        /**
         * The action is one of <code>KEY_PRESS, KEY_REPEAT or
         * KEY_REPEAT</code>.
         */
        private int action;

        /**
         * One of the IHmeProtocol.KEY_XXX values
         */
        private int code;

        /**
         * The raw IR code for the keypress.
         */
        private long rawcode;


        public int getAction()
        {
            return action;
        }

        public int getCode()
        {
            return code;
        }

        public long getRawCode()
        {
            return rawcode;
        }

        public Key(int id, int action, int code, long rawcode)
        {
            super(EVT_KEY, id);
            this.action = action;
            this.code = code;
            this.rawcode = rawcode;
        }

        public Key(HmeInputStream in) throws IOException
        {
            this((int)in.readVInt(), (int)in.readVInt(), (int)in.readVInt(), in.readVInt());
        }
        public void write(HmeOutputStream out) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());
            out.writeVInt(getAction());       
            out.writeVInt(getCode());
            out.writeVInt(getRawCode());
        }
        
        public static String actionToString(int action)
        {
            switch (action) {
              case KEY_PRESS:   return "press";
              case KEY_REPEAT:  return "repeat"; 
              case KEY_RELEASE: return "release";
              default:          return "unknown action " + action;
            }
        }
        
        public static String codeToString(int code)
        {
            String str = (String)codeToString.get(new Integer(code));
            return (str != null) ? str : ("unknown code " + code);
        }

        public static int stringToCode(String str)
        {
            Integer i = (Integer)stringToCode.get(str);
            return (i != null) ? i.intValue() : KEY_UNKNOWN;
        }
        
        public String toString()
        {
            return "key(" + actionToString(action) + "," + codeToString(code) + "," + rawcode + ")";
        }

        // Note that display/info as well as window/pip/aspect map to the same
        // key code.  Consequently retrieving the string value for a given code
        // will only give the last code added to he hash below.
        static
        {
            codeToString = new HashMap();
            stringToCode = new HashMap();
            
            addCodeString(KEY_TIVO, "tivo");
            addCodeString(KEY_UP, "up");
            addCodeString(KEY_DOWN, "down");
            addCodeString(KEY_LEFT, "left");
            addCodeString(KEY_RIGHT, "right");
            addCodeString(KEY_SELECT, "select");
            addCodeString(KEY_PLAY, "play");
            addCodeString(KEY_PAUSE, "pause");
            addCodeString(KEY_SLOW, "slow");
            addCodeString(KEY_REVERSE, "reverse");
            addCodeString(KEY_FORWARD, "forward");
            addCodeString(KEY_REPLAY, "replay");
            addCodeString(KEY_ADVANCE, "advance");
            addCodeString(KEY_THUMBSUP, "thumbsup");
            addCodeString(KEY_THUMBSDOWN, "thumbsdown");
            addCodeString(KEY_VOLUMEUP, "volumeup");
            addCodeString(KEY_VOLUMEDOWN, "volumedown");
            addCodeString(KEY_CHANNELUP, "channelup");
            addCodeString(KEY_CHANNELDOWN, "channeldown");
            addCodeString(KEY_MUTE, "mute");
            addCodeString(KEY_RECORD, "record");
            addCodeString(KEY_LIVETV, "livetv");
            addCodeString(KEY_DISPLAY, "display");
            addCodeString(KEY_INFO, "info");
            addCodeString(KEY_CLEAR, "clear");
            addCodeString(KEY_ENTER, "enter");
            addCodeString(KEY_NUM0, "num0");
            addCodeString(KEY_NUM1, "num1");
            addCodeString(KEY_NUM2, "num2");
            addCodeString(KEY_NUM3, "num3");
            addCodeString(KEY_NUM4, "num4");
            addCodeString(KEY_NUM5, "num5");
            addCodeString(KEY_NUM6, "num6");
            addCodeString(KEY_NUM7, "num7");
            addCodeString(KEY_NUM8, "num8");
            addCodeString(KEY_NUM9, "num9");
            addCodeString(KEY_OPT_ASPECT, "aspect");
            addCodeString(KEY_OPT_PIP, "pip");
            addCodeString(KEY_OPT_WINDOW, "window");
            addCodeString(KEY_OPT_EXIT, "exit");
            addCodeString(KEY_OPT_LIST, "list");
            addCodeString(KEY_OPT_GUIDE, "guide");
            addCodeString(KEY_OPT_STOP, "stop");
            addCodeString(KEY_OPT_MENU, "menu");
            addCodeString(KEY_OPT_TOP_MENU, "topmenu");
            addCodeString(KEY_OPT_ANGLE, "angle");
            addCodeString(KEY_OPT_DVD, "dvd");
        }

        static void addCodeString(int code, String str)
        {
            Integer i = new Integer(code);
            codeToString.put(i, str);
            stringToCode.put(str, i);
        }
    }

    /**
     * An event containing information about an device.
     */
    public static class DeviceInfo extends HmeEvent
    {
        /**
         * A map of properties about the device. The map can contain the
         * following properties: <code>active, warning.code/warning.text</code>.
         */
        private Map map;

        public Map getMap()
        {
            return map;
        }
        
        public DeviceInfo(Map map)
        {
            this(ID_ROOT_STREAM, map);
        }
        
        public DeviceInfo(int id, Map map)
        {
            super(EVT_DEVICE_INFO, id);
            this.map = map;
        }
        
        public DeviceInfo(HmeInputStream in) throws IOException
        {
            this((int)in.readVInt(), new HashMap());
            readMap(in, map);
        }
        
        public void write(HmeOutputStream out) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());
            writeMap(out, getMap());
        }
        
        public String toString()
        {
            return getID() + ".DEVICE_INFO(" + map + ")";
        }
    }

    /**
     * An event containing information about an application.
     */
    public static class ApplicationInfo extends HmeEvent
    {
        /**
         * A map of properties about the application. The map can contain the
         * following properties: <code>active, warning.code/warning.text</code>.
         */
        private Map map;

        public Map getMap()
        {
            return map;
        }

        public ApplicationInfo(Map map)
        {
            this(ID_ROOT_STREAM, map);
        }
        
        public ApplicationInfo(int id, Map map)
        {
            super(EVT_APP_INFO, id);
            this.map = map;
        }

        public ApplicationInfo(int id, String key, String value)
        {
            super(EVT_APP_INFO, id);
            map = new HashMap();
            map.put(key, value);
        }
        
        public ApplicationInfo(HmeInputStream in) throws IOException
        {
            this((int)in.readVInt(), new HashMap());
            readMap(in, map);
        }
        
        public void write(HmeOutputStream out) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());
            writeMap(out, getMap());
        }
        
        public String toString()
        {
            return getID() + ".APP_INFO(" + map + ")";
        }
    }
    
    /**
     * An event containing information about initialization.
     */
    public static class InitInfo extends HmeEvent
    {
        private byte[] data;
        private TeDict   args;

        /**
	 * If this application was started as the result of a
	 * transition back from another application, then this data is
	 * the memento that the application left for itself.
	 * Otherwise, if this application was *not* started due to a
	 * transition back, there will be no memento data.  This data
	 * is the same data that was sent by the application when it
	 * initiated the forward transition.
         */
        public byte[] getMemento()
        {
            return data;
        }

	/**
	 * If this application was started as the result of a
	 * transition, then this data is the parameters that were
	 * passed (forward or back) to this application from the one
	 * that called it.
	 */
        public TeDict getParams()
        {
            return args;
        }
        
        public InitInfo(byte[] data)
        {
            super(EVT_INIT_INFO, ID_ROOT_STREAM);
	    this.data = data;
        }
        
        public InitInfo(HmeInputStream in) throws IOException
        {
        	super(EVT_INIT_INFO, (int) in.readVInt());
        	args = in.readDict();
        	data = in.readVData();
        }
        
        public void write(HmeOutputStream out) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());
            out.writeDict(args);
            out.writeVData(data, 0, data.length);
        }
        
        public String toString()
        {
        	int cnt = 0;
        	if (args != null)
        	{
        		for (TeIterator iter = args.getNames();
        		iter.hasNext(); iter.next())
        		{
        			cnt++;
        		}
        	}
        	return getID() + ".INIT_INFO(data_bytes=" + data.length + 
        	", n_args=" + cnt + ")";
        }
    }
    
    /**
     * An event containing information about a resource.
     */
    public static class ResourceInfo extends HmeEvent
    {
        /**
         * The resource to which the event refers.
         */
        private Resource rsrc;

        /**
         * One of the IHmeProtocol.RSRC_STATUS_XXX values
         */
        private int status;

        /**
         * A map of properties about the resources. The map can contain the
         * following properties: <code>speed, pos, bitrate,
         * warning.code/warning.text, error.code/error.text</code>.
         */
        private Map map;

        public Resource getResource()
        {
            return rsrc;
        }

        public int getStatus()
        {
            return status;
        }

        public Map getMap()
        {
            return map;
        }

        protected ResourceInfo( int opcode, int id, Resource rsrc, int status, Map map )
        {
            super(opcode, id);
            this.rsrc = rsrc;
            this.status = status;
            this.map = map;
        }

        public ResourceInfo(int id, int status, Map map)
        {
            this(id, null, status, map);
        }

        public ResourceInfo(int id, Resource rsrc, int status, Map map)
        {
            this( EVT_RSRC_INFO, id, rsrc, status, map );
        }

        public ResourceInfo(HmeInputStream in, Application app) throws IOException
        {
            this((int)in.readVInt(), null, (int)in.readVInt(), new HashMap());
            readMap(in, map);
            rsrc = (app.getResource(getID()));
        }
        
        public void write(HmeOutputStream out) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());
            out.writeVInt(getStatus());
            writeMap(out, getMap());
        }
        
        public static String statusToString(int status)
        {
            switch (status) {
              case RSRC_STATUS_UNKNOWN:    return "unknown";
              case RSRC_STATUS_CONNECTED:  return "connected";
              case RSRC_STATUS_CONNECTING: return "connecting";         
              case RSRC_STATUS_LOADING:    return "loading";
              case RSRC_STATUS_READY:      return "ready";
              case RSRC_STATUS_PLAYING:    return "playing";            
              case RSRC_STATUS_PAUSED:     return "paused";             
              case RSRC_STATUS_SEEKING:    return "seeking";            
              case RSRC_STATUS_CLOSED:     return "closed";
              case RSRC_STATUS_COMPLETE:   return "complete";
              case RSRC_STATUS_ERROR:      return "error";
            }
            return "unknown status " + status;
        }
        
        public String toString()
        {
            return getID() + ".RESOURCE_INFO(" + statusToString(status) + ", "
                + map + ")";
        }
    }

    /**
     * An event containing idle notification
     */
    public static class Idle extends HmeEvent
    {
        private boolean isIdle;
        
        /**
         * @return true if the receiver is idle
         */
        public boolean isIdle()
        {
            return isIdle;
        }
        
        public Idle(int id, boolean isIdle)
        {
            super( EVT_IDLE, id );
            this.isIdle = isIdle;
        }

        public Idle(HmeInputStream in) throws IOException
        {
            this((int)in.readVInt(), in.readBoolean() );
        }
        
        public void write(HmeOutputStream out) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());
            out.writeBoolean( isIdle );
        }
        public String toString()
        {
            return getID() + ".IDLE(" + isIdle + ")";
        }
    }

    public static class FontInfo extends HmeEvent
    {
        private float ascent;
        
        private float descent;
            
        private float height;
        
        private float lineGap;
        
        private Map glyphInfoMap;
        
        public float getAscent()
        {
            return ascent;
        }
        
        public float getDescent()
        {
            return descent;
        }
        
        public float getHeight()
        {
            return height;
        }
        
        public float getLineGap()
        {
            return lineGap;
        }
        
        public GlyphInfo getGlyphInfo( char glyph )
        {
            if ( glyphInfoMap != null )
            {
                return (GlyphInfo) glyphInfoMap.get( new Character( glyph ) );
            }
            else
            {
                return null;
            }
        }
        
        public FontInfo( int id, float ascent, float descent, float height, float lineGap, Map glyphInfoMap )
        {
            super( EVT_FONT_INFO, id );
            this.ascent = ascent;
            this.descent = descent;
            this.height = height;
            this.lineGap = lineGap;
            this.glyphInfoMap = glyphInfoMap;
        }
        
        public FontInfo( HmeInputStream in ) throws IOException
        {
            this( (int) in.readVInt(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(), null );

            int nFieldsPerGlyph = (int) in.readVInt();
            int nGlyphMetrics = (int) in.readVInt();
            
            if ( nGlyphMetrics != 0 )
            {
                glyphInfoMap = new HashMap( nGlyphMetrics );

                for ( int i = 0; i < nGlyphMetrics; i++ )
                {
                    char character = (char) in.readVInt();
                    GlyphInfo glyphInfo = new GlyphInfo( character, in.readFloat(), in.readFloat() );
                    
                    glyphInfoMap.put( new Character( character ), glyphInfo    );
                    
                    // consume unknown metrics, which must all be the size of a
                    // float
                    for ( int j = 3; j < nFieldsPerGlyph; j++ )
                    {
                        in.readFloat();
                    }
                }
            }
        }        
        
        public void write( HmeOutputStream out ) throws IOException
        {
            out.writeVInt(getOpCode());
            out.writeVInt(getID());

            // write out font wide info
            out.writeFloat( ascent );
            out.writeFloat( descent );
            out.writeFloat( height );
            out.writeFloat( lineGap );

            // write the number of metrics per glyph
            out.writeVInt( 3 );

            if ( glyphInfoMap != null )
            {
                Collection glyphInfoCollection = glyphInfoMap.values(); 
                // write the number of values
                out.writeVInt( glyphInfoCollection.size() );
                
                // iterate over glyph info
                Iterator glyphInfoIter = glyphInfoCollection.iterator();
                while ( glyphInfoIter.hasNext() )
                {
                    GlyphInfo glyphInfo = (GlyphInfo) glyphInfoIter.next();
                    out.writeVInt( glyphInfo.getCharacter() );
                    out.writeFloat( glyphInfo.getAdvance() );
                    out.writeFloat( glyphInfo.getBoundingWidth() );
                }
            }
            else
            {
                // no glyph info
                out.writeVInt( 0 );
            }
        }
        
        /**
         * Returns the pixel width of the text string using the font metrics
         * supplied by the receiver.
         * @param string the string to measure
         * @return the pixel width of the string
         */
        public int measureTextWidth( String string )
        {
            float width = 0;
            
            for ( int i = 0; i < string.length(); i++ )
            {
                GlyphInfo info = getGlyphInfo( string.charAt(i) );
                
                if ( info != null )
                {
                    if ( i < string.length() - 1 )
                    {
                        // use pen advance for leading characters 
                        width += info.getAdvance();
                    }
                    else
                    {
                        // use bounding width for last character
                        // as some glyphs (e.g. italics) extend past
                        // the advance width
                        width += info.getBoundingWidth();
                    }
                }
            }
            
            return (int) width;
        }
        
        public static class GlyphInfo
        {
            private char character;
            
            private float advance;
            
            private float boundingWidth;
            
            public char getCharacter()
            {
                return character;
            }
            
            public float getAdvance()
            {
                return advance;
            }
            
            public float getBoundingWidth()
            {
                return boundingWidth;
            }
            
            public GlyphInfo( char character, float advance, float boundingWidth )
            {
                this.character = character;
                this.advance = advance;
                this.boundingWidth = boundingWidth;
            }
        }
    }
}
