//////////////////////////////////////////////////////////////////////
//
// File: Resource.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/**
 * An HME resource.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class Resource extends HmeObject
{
    /**
     * The resource ID of an event indicating that the resource status has
     * changed.
     */
    public final static int EVT_RSRC_STATUS = EVT_RESERVED + 1;
    
    static IHmeEventHandler EMPTY[] = new IHmeEventHandler[0];    
  
    int nhandlers;
    IHmeEventHandler handlers[] = EMPTY;

    /**
     * The resource's status - one of the
     * <code>IHmeProtocol.RSRC_STATUS_XXXX</code> flags.
     */
    public int status;
    
    // for apps
    protected Resource()
    {
        super(null, -1);
    }

    // for norma resources
    protected Resource(Application app)
    {
        this(app, app.getNextID());
    }

    // for system resources
    protected Resource(Application app, int id)
    {
        super(app, id);
        app.addResource(new Integer(id), this);
    }

    /**
     * Activate or deactivate hte resource with <code>CMD_RSRC_SET_ACTIVE</code>.
     */
    public void setActive(boolean active)
    {
        getApp().cmdRsrcSetActive(getID(), active);
    }

    /**
     * Add an event handler to the stream. The event handler will be asked to
     * handle any events that are sent to the resource.
     */
    public void addHandler(IHmeEventHandler handler)
    {
        if (nhandlers == handlers.length) {
            System.arraycopy(handlers, 0, handlers = new IHmeEventHandler[Math.max(1, nhandlers * 2)], 0, nhandlers);
        }
        handlers[nhandlers++] = handler;
    }

    /**
     * Remove an event handler.
     */
    public void removeHandler(IHmeEventHandler handler)
    {
        for (int i = 0; i < nhandlers; i++) {
            if (handlers[i] == handler) {
                if (i < nhandlers - 1) {
                    System.arraycopy(handlers, i + 1, handlers, i, nhandlers - i - 1);
                }
                handlers[--nhandlers] = null;
                break;
            }
        }
    }

    public void postEvent(HmeEvent event)
    {
        super.postEvent(event);

        for (int i = 0; i < nhandlers; i++) {
            handlers[i].postEvent(event);
        }
    }

    public boolean handleEvent(HmeEvent event)
    {
        switch (event.getOpCode()) {
          case EVT_RSRC_INFO: {
              HmeEvent.ResourceInfo info = (HmeEvent.ResourceInfo)event;
              if (info.getID() == getID()) {
                  if (this.status != info.getStatus()) {
                      this.status = info.getStatus();
                      postEvent(new ResourceStatus(getID(), this, status, info.getMap()));
                  }
              }
              // let this event propagate to our parent
              break;
          }
        }
        return super.handleEvent(event);
    }
    
    //
    // sending events
    //

    /**
     * Send an event to the resource immediately.
     */
    public void sendEvent(HmeEvent evt)
    {
        sendEvent(evt, null);
    }

    /**
     * Send an event to the resource using <code>CMD_RSRC_SEND_EVENT</code>.
     */
    public void sendEvent(HmeEvent evt, Resource animation)
    {
        getApp().cmdRsrcSendEvent(getID(), evt, animation);
    }

    /**
     * Remove the resource from the receiver using
     * <code>CMD_RSRC_REMOVE</code>. Normally this is done automatically by the
     * SDK when the resource is no longer in use. Remember that a resource can
     * still be in use on the receiver after has been removed by the SDK.
     */
    public void remove()
    {
        if (getID() >= ID_CLIENT) {
            getApp().cmdRsrcRemove(getID());
            setID(-1);
        }
    }

    /**
     * Upon finalization the resource will be removed with {@link #remove}.
     */
    protected void finalize()
    {
        // Check the context - if null the app is closed and we can avoid a lot
        // of exceptions which are not free.
        if (getApp().getContext() != null) {
            remove();
            flush();
        }
    }

    /**
     * Dump the resource to System.out with an indent.
     */
    public void dump(int indent)
    {
        for (int i = 0 ; i < indent ; i++) {
            System.out.print(" ");
        }
        System.out.println(this);
    }

    /**
     * Event posted to resource instances when its status changes.
     *
     * Used to handle status changes w/o having to process resource info
     * events.
     */
    static class ResourceStatus extends HmeEvent.ResourceInfo
    {
        ResourceStatus(int id, Resource rsrc, int status, Map map)
        {
            super(EVT_RSRC_STATUS, id, rsrc, status, map);
        }
        public String toString()
        {
            return getID() + ".RSRC_STATUS(" + statusToString(getStatus()) + ")";
        }
    }
    
    /**
     * Color.
     */
    static class ColorResource extends Resource
    {
        Color color;

        ColorResource(Application app, Color color)
        {
            super(app);
            this.color = color;
            getApp().cmdRsrcAddColor(getID(), color);
        }
        protected void toString(StringBuffer buf)
        {
            buf.append(",rgb=0x" + Integer.toHexString(color.getRGB()));
        }
    }

    /**
     * True type.
     */
    static class TrueTypeResource extends Resource
    {
        String name;

        TrueTypeResource(Application app, String name, int id)
        {
            super(app, id);
            this.name = name;
        }
        
        TrueTypeResource(Application app, String name)
        {
            super(app);
            this.name = name;

            if (name == null) {
                throw new NullPointerException("TrueTypeResource name == null");
            }

            getApp().cmdRsrcAddTtf(getID(), name);
        }
        protected void toString(StringBuffer buf)
        {
            buf.append(",ttf=" + name);
        }
    }

    /**
     * Font.
     */
    static public class FontResource extends Resource
    {
        Resource ttf;
        int style;
        float size;

        HmeEvent.FontInfo fontInfo;

        FontResource(Application app, Resource ttf, int style, float size, int flags)
        {
            super(app);
            this.ttf = ttf;
            this.style = style;
            this.size = size;

            getApp().cmdRsrcAddFont(getID(), ttf, style, size, flags);
        }

        public HmeEvent.FontInfo getFontInfo()
        {
            return fontInfo;
        }

        public boolean handleEvent(HmeEvent event)
        {
            switch (event.getOpCode()) {
              case EVT_FONT_INFO:
              {
                  fontInfo = (HmeEvent.FontInfo) event;
                  break;
              }
            }
        	
            return super.handleEvent(event);
        }

        protected void toString(StringBuffer buf)
        {
            boolean isBold   = (style & Font.BOLD)   != 0;
            boolean isItalic = (style & Font.ITALIC) != 0;

            String strStyle;
            if (isBold) {
                strStyle = isItalic ? "bolditalic" : "bold";
            } else {
                strStyle = isItalic ? "italic" : "plain";
            }
            buf.append(",font=" + ttf + ",style=" + strStyle + ",size=" + size);
        }

    }
    
    /**
     * Text.
     */
    static class TextResource extends Resource
    {
        Resource font;
        Resource color;
        String text;

        TextResource(Application app, Resource font, Resource color, String text)
        {
            super(app);
            this.font = font;
            this.color = color;
            this.text = text;

            if (font == null) {
                throw new NullPointerException("TextResource font == null");
            }
            if (text == null) {
                throw new NullPointerException("TextResource text == null");
            }

            app.cmdRsrcAddText(getID(), font, color, text);
        }
        protected void toString(StringBuffer buf)
        {
            buf.append(",txt=" + text);
        }
    }

    /**
     * An animation.
     */
    static class AnimResource extends Resource
    {
        int duration;
        float ease;
        
        AnimResource(Application app, int duration, float ease)
        {
            super(app);
            this.duration = duration;
            this.ease = ease;
            app.cmdRsrcAddAnim(getID(), duration, ease);
        }
        protected void toString(StringBuffer buf)
        {
            buf.append(",anim=[" + duration + "," + ease + "]");
        }
    }
}
