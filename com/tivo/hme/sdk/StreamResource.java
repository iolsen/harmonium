//////////////////////////////////////////////////////////////////////
//
// File: StreamResource.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;


/**
 * A stream resource.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class StreamResource extends Resource
{
    /**
     * The stream's uri.
     */
    private String uri;

    /**
     * The stream's content type, which may be null.
     */
    private String contentType;

    /**
     * The stream's current speed - 0 is paused, 1 is playing at normal speed.
     */
    private float speed;

    boolean play;
    
    // for apps
    StreamResource()
    {
    }

    // for normal streams
    StreamResource(Application app, String uri, String contentType, boolean play)
    {
        super(app);
        this.uri = uri;
        this.contentType = contentType;
        this.play = play;
        this.status = RSRC_STATUS_UNKNOWN;
        this.speed = Integer.MIN_VALUE;
        app.cmdRsrcAddStream(getID(), uri, contentType, play);
    }


    /**
     * @return the URI for this resource.
     */
    public String getURI()
    {
        return uri;
    }
    
    /**
     * @return the contentType for this resource
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * The stream's status - one of the
     * <code>IHmeProtocol.MEDIA_STATUS_XXXX</code> flags.
     * @return the status of this stream resource
     */
    public int getStatus()
    {
        return status;
    }
    
    /**
     * @return the speed of this stream resource
     */
    public float getSpeed()
    {
        return speed;
    }
    
    public boolean handleEvent(HmeEvent event)
    {
        switch (event.getOpCode()) {
          case EVT_RSRC_INFO: {
              HmeEvent.ResourceInfo info = (HmeEvent.ResourceInfo)event;
              if (info.getID() == getID()) {
                  String str = (String)info.getMap().get("speed");
                  if (str != null) {
                      speed = (int)Float.parseFloat(str);
                  }
              }
              // let this event propagate to our parent
              break;
          }
        }
        return super.handleEvent(event);
    }
    
    //
    // media methods
    //

    /**
     * Play the stream using {@link #setSpeed}.
     */
    public void play()
    {
        setSpeed(1);
    }

    /**
     * Pause or unpause the stream using {@link #setSpeed}.
     */
    public void pause()
    {
        setSpeed(isPaused() ? 1 : 0);
    }

    /**
     * Close the stream using <code>CMD_RSRC_CLOSE</code>.
     */
    public void close()
    {
        getApp().cmdRsrcClose(getID());
    }

    //
    // getters
    //

    /**
     * Returns true if the stream is paused, false otherwise.
     */
    public boolean isPaused()
    {
        return speed == 0;
    }

    //
    // setters
    //

    /**
     * Set the position using <code>CMD_RSRC_SET_POSITION</code>.
     */
    public void setPosition(long position)
    {
        getApp().cmdRsrcSetPosition(getID(), position);
    }

    /**
     * Set the speed using <code>CMD_RSRC_SET_SPEED</code>.
     */
    public void setSpeed(float speed)
    {
        getApp().cmdRsrcSetSpeed(getID(), speed);
    }
    
    protected void toString(StringBuffer buf)
    {
        buf.append(",uri=" + uri);
    }
}
