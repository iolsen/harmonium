//////////////////////////////////////////////////////////////////////
//
// File: SimStreamResource.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.tivo.hme.host.http.client.HttpClient;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.io.FastInputStream;
import com.tivo.hme.sdk.io.HmeInputStream;
import com.tivo.hme.sdk.util.Ticker;

/**
 * A class that streams media into a view. While running the stream will
 * periodically send resource info events indicating how much of the stream has
 * been processed. For example, for an mp3 the event contains the current
 * position and duration in milliseconds.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
abstract class SimStreamResource extends SimResource implements Runnable, Ticker.Client
{
    //
    // the url and http request for the stream
    //
    
    String url;
    HttpClient http;

    //
    // if true, the stream should start playing as soon as possible
    //
    
    boolean play;

    //
    // RSRC_STATUS_XXXX
    //
    
    int status;

    //
    // current play speed. 0 is pause, one is normal speed, etc.
    //
    
    float speed;

    //
    // if true, the stream has been closed.
    //
    
    boolean closed;

    SimStreamResource(SimApp app, int id, String url, boolean play)
    {
        super(app, id);
        resourceStreamCount++;
        resourceGCStreamCount++;
        this.url = url;
        this.play = play;
    }

    protected void finalize() throws Throwable
    {
        resourceGCStreamCount--;
        super.finalize();
    }


    //
    // stream lifecycle
    //
    
    /**
     * Start the stream in a new thread.
     */
    void start()
    {
        new Thread(this).start();
    }

    /**
     * Make the http connection.
     */
    public void run()
    {
        try {
            // check for fake URLs
            if (url.startsWith("livetv:") || url.startsWith("loopset:") || url.startsWith("recording:")) {
                handle(null);
                return;
            }

            //
            // connect
            //
            setStatus(RSRC_STATUS_CONNECTING);
            Simulator.get().setStatus("Connecting: " + url);
            http = new HttpClient(new URL(url));
            http.connect();

            //
            // write headers
            //
            setStatus(RSRC_STATUS_CONNECTED);
            appendHeaders(http);            
            http.addHeader("Connection", "close");

            //
            // check result
            //
            int status = http.getStatus();
            switch (status) {
              case 200:
                break;
              case 401:
                error(RSRC_ERROR_CONNECT_FAILED, "Access denied.");
                return;
              case 404:
                error(RSRC_ERROR_CONNECT_FAILED, "File not found.");
                return;
              default:
                error(RSRC_ERROR_CONNECT_FAILED, "HTTP Error " + status + ".");
                return;
            }

            //
            // handle set-cookie header
            //
            String str = http.get("set-cookie");
            if (str != null) {
                Simulator.get().cookies.parseSetCookie(http.getURL().getHost(), str);
            }

            //
            // start running
            //
            setStatus(RSRC_STATUS_LOADING);
            setTicking(true);
            Simulator.get().setStatus("RUNNING: " + url);
            
            //
            // go!
            //
            handle(http);
        } catch (IOException e) {
            error(RSRC_ERROR_CONNECTION_LOST, "Connection lost : " + e);
        } finally {
            close();
        }
    }

    /**
     * Subclasses can override to add more headers to the http request.
     */
    protected void appendHeaders(HttpClient http) throws IOException
    {
    }

    abstract void handle(HttpClient http) throws IOException;
    
    /**
     * Pause/unpause.
     */
    void setSpeed(float speed)
    {
        switch (status) {
          case RSRC_STATUS_READY:
          case RSRC_STATUS_PLAYING:         
          case RSRC_STATUS_PAUSED:
          case RSRC_STATUS_SEEKING: {
              this.speed = speed;
              if (speed == 0f) {
                  status = RSRC_STATUS_PAUSED;
              } else if (speed == 1f) {
                  status = RSRC_STATUS_PLAYING;
              } else {
                  status = RSRC_STATUS_SEEKING;
              }
              sendResourceInfo(null);
              break;
          }
          default:
            warning(RSRC_ERROR_BAD_STATE, "can't set speed while in state " + status);
            break;
        }
    }
/**
    
    REMIND: I don't think this is needed.... keeping until sure
    void close()
    {
        if (!closed) {
            SimResource.resourceStreamCount--;
        }
        super.close();
    }
**/
    /**
     * Close the stream and stop the thread.
     */
    synchronized void close(int status)
    {
        if (!closed) {
            SimResource.resourceStreamCount--;
            closed = true;
            setTicking(false);
            setStatus(status);
            setActive(false);
            if (http != null) {
                http.close();
                http = null;
            }
        }
    }


    
    //
    // resource info
    //

    /**
     * Update status and send resource info.
     */
    void setStatus(int status)
    {
        if (this.status != status) {
            this.status = status;
            sendResourceInfo(null);
        }
    }

    /**
     * Start/stop sending periodic resource info events.
     */
    void setTicking(boolean ticking)
    {
        if (ticking) {
            Ticker.master.add(this, System.currentTimeMillis(), null);
        } else {
            Ticker.master.remove(this, null);
        }
    }

    /**
     * Send a warning to the containing app.
     */
    void warning(int errorCode, String text)
    {
        Simulator.get().setStatus("WARNING: " + text + " [" + errorCode + "]");
        error0(errorCode, text);
    }

    /**
     * Send an error to the containing app and die.
     */
    void error(int errorCode, String text)
    {
        if (status < RSRC_STATUS_CLOSED) {
            if (errorCode == RSRC_ERROR_CONNECT_FAILED) {
                text += " " + url;
            }
            Simulator.get().setStatus("ERROR: " + text + " [" + errorCode + "]");
            error0(errorCode, text);
            close(RSRC_STATUS_ERROR);
        }
    }
    
    void error0(int errorCode, String text)
    {
        Map map = new HashMap();
        map.put("error.code", "" + errorCode);
        map.put("error.text", text);
        sendResourceInfo(map);
    }

    /**
     * Send resource info to the containing app.
     *
     * REMIND: This is left in the simulator because it works - The
     * sendResourceInfo method added to parent class will never have a null
     * application because it actually grabs the application from the Simulator.
     */
    void sendResourceInfo(Map map)
    {
        if (app != null) {
            if (map == null) {
                map = new HashMap();
            }
            appendResourceInfo(map);
            app.processEvent(new HmeEvent.ResourceInfo(id, null, status, map));
        }
    }

    /**
     * Append key/value pairs to the map in preparation for sending a resource
     * info event. Subclass can override this method to add more metadata.
     */
    void appendResourceInfo(Map map)
    {
        if (status > RSRC_STATUS_READY) {
            map.put("speed", "" + speed);
            long duration = getDuration();
            if (duration > 0) {
                map.put("pos", getPosition() + "/" + duration);
            }
        }
    }

    /**
     * Return the current position in milliseconds.
     */
    protected long getPosition()
    {
        return 0;
    }

    /**
     * Return the duration in milliseconds.
     */
    protected long getDuration()
    {
        return 0;
    }

    /**
     * Send an ack.  Returns the next time an ack should be sent.
     */
    public long tick(long tm, Object arg)
    {
        sendResourceInfo(null);
        return tm + 500;
    }

    /**
     * Activate or deactivate this resource. When a resource is deactivated,
     * it's parent becomes active.
     */
    void setActive(boolean active)
    {
        SimResource old = Simulator.get().active;
        boolean isActive = (old == this);
        if (active != isActive) {
            // deactivate the old resource
            if (old != null) {
                old.processEvent(new HmeEvent.ApplicationInfo(old.id, "active", "false"));
            }
            
            // set active
            Simulator.get().active = active ? this : app;
            
            // activate the new resource
            if (Simulator.get().active != null) {
                Simulator.get().active.processEvent(new HmeEvent.ApplicationInfo(Simulator.get().active.id, "active", "true"));
            }
        }
    }

    /**
     * Process an event. The stream resources understands pause and play.
     */
    void processEvent(HmeEvent event)
    {
        switch (event.getOpCode()) {
          case EVT_KEY: {
              HmeEvent.Key ir = (HmeEvent.Key)event;
              switch (ir.getAction()) {
                case KEY_PRESS:
                  switch (ir.getCode()) {
                    case KEY_PAUSE:
                      setSpeed(speed == 0 ? 1 : 0);
                      return;
                    case KEY_PLAY:
                      setSpeed(1);
                      return;
                  }
                  break;
              }
          }
        }
        super.processEvent(event);
    }

    /**
     * Send an event to the resource. By default stream resources only
     * understand how to interpret key events.
     */
    void sendEvent(byte buf[], int off, int len)
    {
        FastInputStream fin = new FastInputStream(buf, off, len);
    	HmeInputStream in = new HmeInputStream(fin);

        try {
            int opcode = (int)in.readVInt();
            if (opcode == EVT_KEY) {
                processEvent(new HmeEvent.Key(in));
            }
        } catch (IOException e) {
            System.out.println("SimStreamResource.sendEvent : " + e);
        }
    }

    void toString(StringBuffer buf)
    {
        buf.append("," + url);
    }
}
