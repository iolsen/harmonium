//////////////////////////////////////////////////////////////////////
//
// File: SimApp.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tivo.hme.host.http.client.HttpClient;
import com.tivo.hme.interfaces.IHmeConstants;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.HmeException;
import com.tivo.hme.sdk.HmeObject;
import com.tivo.hme.sdk.io.*;


/**
 * An application. The application class processes commands and writes
 * events. It contains maps of views and resources for dispatching the commands.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
class SimApp extends SimStreamResource
{
    //
    // static map of content types / extensions to sim types
    //

    static Map contentToClass;
    static Map extensionToClass;

    //
    // maps of views+resources
    //
    
    Map views;
    Map resources;

    //
    // I/O
    //
    
    ChunkedInputStream in;
    ChunkedOutputStream out;
    byte buf[];

    //
    // root view
    //
    
    SimView root;

    //
    // a tree path
    //
    // This is stored in the app and used by all objects contained within the
    // app to avoid creating arrays of objects everywhere. It can be shared
    // because we know that within an app only one object at a time can fire
    // events, since commands are processed synchronously.
    //
    
    Object path[];
    
    //
    // version of the application connected to the receiver
    //
    
    int version;

    /**
     * Creates a new app.
     */
    public SimApp(SimApp app, int id, String url, boolean play)
    {
        super(app, id, url, play);
        
        this.views = new HashMap();
        this.resources = new HashMap();

        buf = new byte[4096];
        path = new Object[128];
        
        root = new AppView(this);
        set(ID_ROOT_VIEW, root);

        // make the new app active by default
        Simulator.get().active = this;
    }

    /**
     * Overridden from SimStreamResource to append a cookie to the outgoing http
     * request..
     */
    protected void appendHeaders(HttpClient http) throws IOException
    {
        List cookies = Simulator.get().cookies.getCookies(http.getURL().getHost());
        for (int i = 0; i < cookies.size(); ++i) {
            http.addHeader("Cookie", (String)cookies.get(i));
        }
        http.addHeader("tsn", "00000000000000000000");
    }

    //
    // accessors for view/resource maps
    //

    /**
     * Get a resource by id. If clazz is not null, make sure that the resource
     * is of that type. If this method fails it will call warning() and return
     * null.
     */
    SimResource getResource(int id, Class clazz)
    {
        SimResource rsrc = null;

        //
        // is it a system resource?
        //
        
        if (id < ( version < VERSION_0_38 ?
                   ID_CLIENT_PRE_0_38 :
                   ID_CLIENT ) ) 
        {
            switch (id) {
              case ID_NULL:
                return null;
              case ID_ROOT_STREAM:
                rsrc = this;
                break;
            }
            if (rsrc == null) {
                rsrc = Simulator.get().getSystemResource(id);
                if (rsrc == null) {
                    warning(APP_ERROR_BAD_ARGUMENT, "unknown system resource id " + id);
                    return null;
                }
            }
        } else {
            rsrc = (SimResource)resources.get(new Integer(id));
        }

        //
        // make sure we found a rsrc and it's the right type
        //
        
        if (rsrc == null) {
            warning(APP_ERROR_RSRC_NOT_FOUND, "resource " + id + " not found (type " + clazz + ")");
            return null;
        }
        if (clazz != null && !clazz.isInstance(rsrc)) {
            warning(APP_ERROR_RSRC_NOT_FOUND, "rsrc " + rsrc + " is not of type " + clazz);
            return null;
        }
        
        return rsrc;
    }

    /**
     * Get a view by id. If this method fails it will call warning() and return
     * null.
     */
    SimView getView(int id)
    {
        SimView view = (SimView)views.get(new Integer(id));
        if (view == null) {
            warning(APP_ERROR_VIEW_NOT_FOUND, "view " + id + " not found");
            return null;
        }
        return view;
    }

    /**
     * Convenience function for getting an animation by id. Really just a
     * wrapper around getResource().
     */
    SimResource.AnimResource getAnimation(int id)
    {
        return (SimResource.AnimResource)getResource(id, SimResource.AnimResource.class);
    }

    /**
     * Add a resource to the resource map. Note that addResource performs some
     * last minute checks before adding the resource, and might fail if the add
     * is invalid. Callers should throw away the rsrc if addResource returns
     * false.
     */
    boolean addResource(SimResource rsrc)
    {
        if (rsrc.id >= ID_NULL && 
            rsrc.id < ( version < VERSION_0_38 ?
                        ID_CLIENT_PRE_0_38 :
                        ID_CLIENT ) ) 
        {
            warning(APP_ERROR_BAD_ARGUMENT, "invalid resource id (" + rsrc.id + ")");
            return false;
        }
        
        set(rsrc.id, rsrc);
        return true;
    }

    /**
     * Add/remove a view from the view map.
     */
    void set(int id, SimView view)
    {
        if (view == null) {
            views.remove(new Integer(id));
        } else {
            views.put(new Integer(id), view);
        }
    }

    /**
     * Add/remove a resource from the view map.
     */
    void set(int id, SimResource rsrc)
    {
        if (rsrc == null) {
            resources.remove(new Integer(id));
        } else {
            resources.put(new Integer(id), rsrc);
        }
    }
    
    /**
     * Handle a request. Doesn't return until the app is dead.
     */
    void handle(HttpClient http) throws IOException
    {
        Thread.currentThread().setName("App[" + url + "]");
        setTicking(false);

        //
        // kill nagle
        //
        
        Socket socket = http.getSocket();
        socket.setTcpNoDelay(true);

        FastOutputStream fout = new FastOutputStream(http.getOutputStream(), 1024);
        FastInputStream fin = new FastInputStream(http.getInputStream(), 1024);
        HmeOutputStream hout = new HmeOutputStream(fout);
        HmeInputStream hin = new HmeInputStream(fin);
        
        //
        // handshake
        //
        
        hout.writeInt(MAGIC);
//        hout.writeInt(VERSION);
        hout.writeInt(VERSION_0_40);
        hout.flush();

        int magic = hin.readInt();
        if (magic != MAGIC) {
            Simulator.get().setStatus("ERROR: bad MAGIC");
            return;
        }
        version = hin.readInt();

        // XXX bad version check!  should look at major/minor parts separately!
//        if (version >> 8 > VERSION >> 8) {
        if (version >> 8 > VERSION_0_40 >> 8) {
            Simulator.get().setStatus("ERROR: VERSION mismatch");
            return;
        }

        //
        // start chunking
        //
        
        in = new ChunkedInputStream(hin);//, 1024);
        out = new ChunkedOutputStream(hout, 1024);

        //
        // Set the right string reading flavor
        //
        if ( version < VERSION_0_40 )
        {
            if (Simulator.DEBUG) {
                System.out.println("NOT using vstrings");
            }
            in.setUseVString( false );
            out.setUseVString( false );
        }
        else {
            if (Simulator.DEBUG) {
                System.out.println("Using vstrings");
            }
            in.setUseVString( true );
            out.setUseVString( true );
        }
        
        //
        // get ready...
        //

        speed = 1;
        setStatus(RSRC_STATUS_READY);

        //
        // send device info
        //
        
        Map map = new HashMap();
        map.put("brand", "TiVo");
        map.put("platform", "sim-java");
        map.put("version", "0.9");
        map.put("host", InetAddress.getLocalHost().getCanonicalHostName());
        
        processEvent(new HmeEvent.DeviceInfo(map));

        //
        // send app info event
        //
        Map mapAppInfo = new HashMap();
        mapAppInfo.put("active", "true");
        processEvent(new HmeEvent.ApplicationInfo(mapAppInfo));

        // clear the status and request focus
        Simulator.get().setStatus("");
        Simulator.get().sim.requestFocus();

        //
        // process commands
        //
        while (status <= RSRC_STATUS_CLOSED) {
            try {
                int opcode = (int)in.readVInt();
                if (opcode == -1) {
                    break;
                }
                int id = (int)in.readVInt();
                processCommand(opcode, id);
            } catch (IOException e) {
                if (Simulator.DEBUG) {
                    e.printStackTrace();
                }
                System.out.println("CLOSING: " + this);
                throw e;
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                in.readTerminator();
            }
        }
    }

    //
    // helpers for making sure that a value lies within a certain min/max. If
    // the value is invalid a warning is fired.
    //
    // Note that when a value is found to be invalid by checkLimit() the command
    // should not be processed. If the value is found to be invalid by
    // checkBound() the value is corrected and processing should continue.
    //

    boolean checkLimit(String name, long value, long max)
    {
        if (value <= max) {
            return false;
        }
    
        warning(APP_ERROR_BAD_COMMAND, name + " exceeded (" + value + " > " + max + ")");
        return true;
    }
    
    int checkBound(String name, int value, int min)
    {
        if (value < min) {
            warning(APP_ERROR_BAD_ARGUMENT, "invalid " + name + " (" + value + ")");
            value = min;
        }
        return value;
    }

    float checkBound(String name, float value, float min)
    {
        if (value < min) {
            warning(APP_ERROR_BAD_ARGUMENT, "invalid " + name + " (" + value + ")");
            value = min;
        }
        return value;
    }

    float checkBound(String name, float value, float min, float max)
    {
        if (value < min || value > max) {
            warning(APP_ERROR_BAD_ARGUMENT, "invalid " + name + " (" + value + ")");
            value = Math.max(min, Math.min(value, max));
        }
        return value;
    }

    //
    // enourmous function for processing commands
    //
    
    void processCommand(int opcode, int id) throws IOException
    {
        // process the command
        switch (opcode) {
          case CMD_VIEW_ADD: {
              int parentID = (int)in.readVInt();
              int x = (int)in.readVInt();
              int y = (int)in.readVInt();
              int w = (int)in.readVInt();
              int h = (int)in.readVInt();
              boolean visible = in.read() != 0;
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + x + "," + y + "," + w + "," + h + "," + visible + ")");
              }

              w = checkBound("width", w, 0);
              h = checkBound("height", h, 0);

              SimView parent = getView(parentID);
              if (parent != null) {
                  if (checkLimit("max view depth", parent.getViewDepth() + 1, LIMIT_VIEW_DEPTH)) {
                      return;
                  }
                  SimView view = new SimView(this, id, parent, x, y, w, h, visible);
                  set(view.id, view);
              }
              break;
          }
          case CMD_VIEW_SET_BOUNDS: {
              int x = (int)in.readVInt();
              int y = (int)in.readVInt();
              int w = (int)in.readVInt();
              int h = (int)in.readVInt();
              int animID = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + x + "," + y + "," + w + "," + h + "," + animID + ")");
              }

              w = checkBound("width", w, 0);
              h = checkBound("height", h, 0);

              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              SimAnimator.get().remove(view, SimAnimator.SetBounds.class);
              
              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.SetBounds(view, anim, x, y, w, h);
              } else {
                  view.setBounds(x, y, w, h);
              }
              break;
          }
          case CMD_VIEW_SET_SCALE: {
              float sx = in.readFloat();
              float sy = in.readFloat();
              int animID = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + sx + "," + sy + "," + animID + ")");
              }

              sx = checkBound("scale x", sx, 0);
              sy = checkBound("scale y", sy, 0);
              
              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              SimAnimator.get().remove(view, SimAnimator.SetScale.class);
              
              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.SetScale(view, anim, sx, sy);
              } else {
                  view.setScale(sx, sy);
              }
              break;
          }
          case CMD_VIEW_SET_TRANSLATION: {
              int tx = (int)in.readVInt();
              int ty = (int)in.readVInt();
              int animID = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + tx + "," + ty + "," + animID + ")");
              }

              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              SimAnimator.get().remove(view, SimAnimator.SetTranslation.class);
              
              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.SetTranslation(view, anim, tx, ty);
              } else {
                  view.setTranslation(tx, ty);
              }
              break;
          }
          case CMD_VIEW_SET_TRANSPARENCY: {
              float transparency = in.readFloat();
              int animID = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + transparency + "," + animID + ")");
              }

              transparency = checkBound("transparency", transparency, 0, 1);

              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              SimAnimator.get().remove(view, SimAnimator.SetTransparency.class);
              
              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.SetTransparency(view, anim, transparency);
              } else {
                  view.setTransparency(transparency);
              }
              break;
          }
          case CMD_VIEW_SET_VISIBLE: {
              boolean visible = in.read() != 0;
              int animID = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + visible + "," + animID + ")");
              }

              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              SimAnimator.get().remove(view, SimAnimator.SetVisible.class);
              
              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.SetVisible(view, anim, visible);
              } else {
                  view.setVisible(visible);
              }
              break;
          }
          case CMD_VIEW_SET_PAINTING: {
              boolean painting = in.read() != 0;
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + painting + ")");
              }

              SimView view = getView(id);
              if (view == null) {
                  return;
              }
              
              view.setPainting(painting);
              break;
          }
          case CMD_VIEW_SET_RESOURCE: {
              int rsrcID = (int)in.readVInt();
              int flags = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + rsrcID + ", " + HmeObject.rsrcFlagsToString(flags) + ")");
              }

              SimResource rsrc = getResource(rsrcID, null);
              
              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              if (rsrc != null) {
                  if (rsrc instanceof SimResource.ColorResource) {
                      // disallow flags entirely                  
                      flags = 0;
                  } else if (rsrc instanceof SimResource.TextResource) {
                      // strip image flags                  
                      flags &= ~RSRC_IMAGE_MASK;
                  } else if ((rsrc instanceof SimResource.ImageResource) ||
                             (rsrc instanceof SimStreamResource)) {
                      // strip text flags
                      flags &= ~RSRC_TEXT_MASK;
                  } else {
                      warning(APP_ERROR_RSRC_NOT_FOUND,
                              "rsrc" + rsrc + " is not a color/test/image/stream.");
                      return;
                  }
              }
              
              view.setResource(rsrc, flags);
              break;
          }
          case CMD_VIEW_REMOVE: {
              int animID = (int)in.readVInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + animID + ")");
              }

              SimView view = getView(id);
              if (view == null) {
                  return;
              }

              SimAnimator.get().remove(view, SimAnimator.Remove.class);
              
              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.Remove(view, anim);
              } else {
                  view.remove();
              }
              break;
          }
          case CMD_RSRC_ADD_COLOR: {
              int rgb = in.readInt();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(0x" + Integer.toHexString(rgb) + ")");
              }

              addResource(new SimResource.ColorResource(this, id, new Color(rgb, true)));
              break;
          }
          case CMD_RSRC_ADD_TTF: {
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(...)");
              }

              try {
                  addResource(new SimResource.FontResource(this, id, Font.createFont(Font.TRUETYPE_FONT, new NoCloseStream(in)), 0));
              } catch (FontFormatException e) {
                  e.printStackTrace();
                  throw new HmeException(e.toString());
              } catch (IOException e) {
                  e.printStackTrace();
                  throw new HmeException(e.toString());
              }
              break;
          }
          case CMD_RSRC_ADD_FONT: {
              int ttfId = (int)in.readVInt();
              int style = (int)in.readVInt();
              float size = in.readFloat();
              int flags = 0;
              try {
              	  flags = (int) in.readVInt();
              } catch ( IOException e )
              {
                  // older applications might not know to send flags, so assume no flags
              }
              
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + ttfId + "," + style + "," + size + "," + flags + ")");
              }

              if (checkLimit("font point size", (int) size, LIMIT_FONT_POINT_SIZE)) {
                  return;
              }

              SimResource.FontResource ttf = (SimResource.FontResource)getResource(ttfId, SimResource.FontResource.class);
              if (ttf == null) {
                  return;
              }
              
              addResource(new SimResource.FontResource(this, id, ttf.font.deriveFont(style, size), flags));
              break;
          }
          case CMD_RSRC_ADD_TEXT: {
              int fontId = (int)in.readVInt();
              int colorId = (int)in.readVInt();
              String text = in.readUTF();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + fontId + "," + colorId + "," + text + ")");
              }

              if (checkLimit("max text bytes", text.length(), LIMIT_TEXT_NBYTES)) {
                  return;
              }

              SimResource.FontResource font = (SimResource.FontResource)getResource(fontId, SimResource.FontResource.class);
              if (font == null) {
                  return;
              }

              SimResource.ColorResource color = (SimResource.ColorResource)getResource(colorId, SimResource.ColorResource.class);
              if (color == null) {
                  return;
              }
              
              addResource(new SimResource.TextResource(this, id, font.font, color.color, text));
              break;
          }
          case CMD_RSRC_ADD_IMAGE: {
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(...)");
              }
              
              FastOutputStream out = drainStream(in);
              if (out == null) {
                  return;
              }

              if (checkLimit("max image bytes", out.getCount(), LIMIT_IMAGE_NBYTES)) {
                  return;
              }
              
              
              Image image = Toolkit.getDefaultToolkit().createImage(out.getBuffer());
              addResource(new SimResource.ImageResource(this, id, image));
              break;
          }
          case CMD_RSRC_ADD_SOUND: {
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(...)");
              }

              FastOutputStream out = drainStream(in);
              if (out == null) {
                  return;
              }

              if (checkLimit("max sound bytes", out.getCount(), LIMIT_SOUND_NBYTES)) {
                  return;
              }
              
              
              addResource(new SimResource.SoundResource(this, id, out.getBuffer(), 0, (int)out.getCount()));
              break;
          }
          case CMD_RSRC_ADD_STREAM: {
              String url = in.readUTF();
              String ctype = in.readUTF();
              boolean play = in.read() != 0;
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + url + "," + ctype + "," + play + ")");
              }

              if (checkLimit("max url bytes", url.length(), LIMIT_URL_NBYTES)) {
                  return;
              }
              
              //
              // what sort of resource is it?
              //

              Class clazz = (Class)contentToClass.get(ctype);
              if (clazz == null) {
                  int dot = url.lastIndexOf('.');
                  if (dot != -1) {
                      String extension = url.substring(dot).toLowerCase();
                      clazz = (Class)extensionToClass.get(extension);
                  }
              }
              if (clazz == null) {
                  clazz = SimFakeStream.class;
              }

              //
              // now create the resource from clazz
              //
              
              Class signature[] = { SimApp.class, Integer.TYPE,
                                    String.class, Boolean.TYPE };

              Constructor ctor = null;
              try {
                  ctor = clazz.getConstructor(signature);
              } catch (NoSuchMethodException e) {
                  e.printStackTrace();
              }

              Object args[] = { this, new Integer(id), url, new Boolean(play) };
              
              SimResource rsrc = null;
              try {
                  rsrc = (SimResource)ctor.newInstance(args);
              } catch (InstantiationException e) {
                  e.printStackTrace();                  
              } catch (IllegalAccessException e) {
                  e.printStackTrace();                  
              } catch (IllegalArgumentException e) {                  
                  e.printStackTrace();
              } catch (InvocationTargetException e) {                  
                  e.printStackTrace();
              }
              
              //
              // try to add it
              //
              
              if (addResource(rsrc)) {
                  rsrc.start();
              }
              break;
          }
          case CMD_RSRC_ADD_ANIM: {
              int duration = (int)in.readVInt();
              float ease = in.readFloat();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + duration + "," + ease + ")");
              }

              duration = checkBound("duration", duration, 0);
              ease = checkBound("ease", ease, -1, 1);              
              
              addResource(new SimResource.AnimResource(this, id, duration, ease));
              break;
          }
          case CMD_RSRC_SET_ACTIVE: {
              boolean active = in.read() != 0;
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + active + ")");
              }

              SimResource rsrc = getResource(id, SimStreamResource.class);
              if (rsrc == null) {
                  return;
              }

              rsrc.setActive(active);
              // see if this is the application closing
              if (id == this.id && !active) {
                  Simulator.get().setURL(null);
              }
              break;
          }
          case CMD_RSRC_SET_SPEED: {
              float speed = in.readFloat();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + speed + ")");
              }

              SimResource rsrc = getResource(id, null);
              if (rsrc == null) {
                  return;
              }
              
              if (!(rsrc instanceof SimResource.SoundResource) &&
                  !(rsrc instanceof SimStreamResource)) {
                  warning(APP_ERROR_RSRC_NOT_FOUND, "rsrc " + rsrc + " is not a stream/sound");
                  return;
              }
              
              rsrc.setSpeed(speed);
              break;
          }
          case CMD_RSRC_SEND_EVENT: {
              // REMIND: send to parent
              // REMIND : animID
              int animID = (int)in.readVInt();

              FastOutputStream out = drainStream(in);
              if (out == null) {
                  return;
              }

              if (checkLimit("max event bytes", out.getCount(), LIMIT_EVENT_NBYTES)) {
                  return;
              }

              SimResource rsrc = getResource(id, SimStreamResource.class);
              if (rsrc == null) {
                  return;
              }

              SimResource.AnimResource anim = getAnimation(animID);
              if (anim != null) {
                  new SimAnimator.SendEvent(rsrc, anim, out.getBuffer(), 0, (int)out.getCount());
              } else {
                  rsrc.sendEvent(out.getBuffer(), 0, (int)out.getCount());
              }
              break;
          }
          case CMD_RSRC_CLOSE: {
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "()");
              }

              SimResource rsrc = getResource(id, SimStreamResource.class);
              if (rsrc == null) {
                  return;
              }
              
              rsrc.close();
              break;
          }
          case CMD_RSRC_REMOVE: {
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "()");
              }

              if (id >= ID_NULL && 
                  id < ( version < VERSION_0_38 ?
                         ID_CLIENT_PRE_0_38 :
                         ID_CLIENT ) )
              {
                  warning(APP_ERROR_BAD_ARGUMENT,
                          "can't remove rsrc. invalid id " + id);
                  return;
              }
              
              SimResource rsrc = getResource(id, null);
              if (rsrc == null) {
                  return;
              }
              rsrc.close();
              set(id, (SimResource)null);
              break;
          }

          case CMD_RECEIVER_ACKNOWLEDGE_IDLE: {
              boolean isHandled = in.readBoolean();
              if (Simulator.DEBUG) {
                  System.out.println(id + "." + opcodeToString(opcode) + "(" + isHandled + ")");
              }
              break;
          }
            
          default:
            throw new HmeException("opcode " + opcode + " not implemented");
        }
    }


    /**
     * I/O helper for draining all bytes from a stream and returning the
     * resulting bytes. drainStream obeys LIMIT_CMD_NBYTES and will return null
     * if the stream is too long.
     */
    FastOutputStream drainStream(InputStream in) throws IOException
    {
        FastOutputStream out = new FastOutputStream(1024);
        while (true) {
            int n = in.read(buf, 0, buf.length);
            if (n < 0) {
                break;
            }
            out.write(buf, 0, n);
            if (checkLimit("max bytes per command", out.getCount(), LIMIT_CMD_NBYTES)) {
                return null;
            }
        }
        return out;
    }
    
    //
    // Upstream events
    //

    /**
     * Send a warning to the app.
     */
    void warning(int errorCode, String text)
    {
        if (status < RSRC_STATUS_CLOSED) {
            Simulator.get().setStatus("WARNING: " + text + " [" + errorCode + "]");
            
            Map map = new HashMap();
            map.put("error.code", "" + errorCode);
            map.put("error.text", text);
            processEvent(new HmeEvent.ApplicationInfo(map));
        }
    }

    /**
     * Overridden from SimStreamResource. SimApp sends all events upstream.
     */
    void processEvent(HmeEvent event)
    {
        if (out == null) {
            return;
        }
        
        synchronized (out) {
            if (Simulator.DEBUG) {
                System.out.println("event " + event);
            }
            try {
                event.write(out);
                out.writeTerminator();
                out.flush();
                Simulator.get().sim.record(event);
            } catch (IOException e) {
                out = null;
            }
        }
    }

    /**
     * Overridden from SimStreamResource. SimApp sends all events upstream.
     */
    void sendEvent(byte buf[], int off, int len)
    {
        if (out == null) {
            return;
        }
        
        synchronized (out) {
            try {
                out.write(buf, off, len);
                out.writeTerminator();
                out.flush();
                Simulator.get().sim.record(null, buf, off, len);
            } catch (IOException e) {
                out = null;
            }
        }
    }

    /**
     * Close the app.
     */
    void close(int status)
    {
        if (!closed) {
            super.close(status);
            root.setVisible(false);
            root.close();

            // close all resources
            for (Iterator i = resources.values().iterator(); i.hasNext();) {
                SimResource r = (SimResource)i.next();
                r.close();
            }

            // run GC a few times ...
            new Thread(new GCME()).start();
        }
    }

    //
    // used by the tree view
    //

    String getIcon()
    {
        return "app.png";
    }

    void toString(StringBuffer buf)
    {
        buf.append("," + url);
    }

    /**
     * Helper for converting an opcode to a string.
     */
    static String opcodeToString(int opcode)
    {
        switch (opcode) {
          case CMD_VIEW_ADD:                    return "VIEW_ADD";
          case CMD_VIEW_SET_BOUNDS:             return "VIEW_SET_BOUNDS";
          case CMD_VIEW_SET_SCALE:              return "VIEW_SET_SCALE";
          case CMD_VIEW_SET_TRANSLATION:        return "VIEW_SET_TRANSLATION";
          case CMD_VIEW_SET_TRANSPARENCY:       return "VIEW_SET_TRANSPARENCY";
          case CMD_VIEW_SET_VISIBLE:            return "VIEW_SET_VISIBLE";
          case CMD_VIEW_SET_PAINTING:           return "VIEW_SET_PAINTING";
          case CMD_VIEW_SET_RESOURCE:           return "VIEW_SET_RESOURCE";
          case CMD_VIEW_REMOVE:                 return "VIEW_REMOVE";
          case CMD_RSRC_ADD_COLOR:              return "RSRC_ADD_COLOR";
          case CMD_RSRC_ADD_TTF:                return "RSRC_ADD_TTF";
          case CMD_RSRC_ADD_FONT:               return "RSRC_ADD_FONT";
          case CMD_RSRC_ADD_TEXT:               return "RSRC_ADD_TEXT";
          case CMD_RSRC_ADD_IMAGE:              return "RSRC_ADD_IMAGE";
          case CMD_RSRC_ADD_SOUND:              return "RSRC_ADD_SOUND";
          case CMD_RSRC_ADD_STREAM:             return "RSRC_ADD_STREAM";
          case CMD_RSRC_ADD_ANIM:               return "RSRC_ADD_ANIM";
          case CMD_RSRC_SET_SPEED:              return "RSRC_SET_SPEED";
          case CMD_RSRC_SET_ACTIVE:             return "RSRC_SET_ACTIVE";
          case CMD_RSRC_SEND_EVENT:             return "RSRC_SEND_EVENT";
          case CMD_RSRC_CLOSE:                  return "RSRC_CLOSE";
          case CMD_RSRC_REMOVE:                 return "RSRC_REMOVE";
          case CMD_RECEIVER_ACKNOWLEDGE_IDLE:   return "RECEIVER_ACKNOWLEDGE_IDLE";
          case CMD_RESERVED:                    return "RESERVED";
        }
        return "unknown opcode " + opcode;
    }

    /**
     * The root view for this app. It will call back into the app when it
     * detects that the app is starting or stopping.
     */
    static class AppView extends SimView
    {
        AppView(SimApp app)
        {
            super(app, ID_ROOT_VIEW, app, 0, 0, 640, 480, false);
        }

        void setVisible(boolean visible)
        {
            if (this.visible != visible) {
                super.setVisible(visible);
                if (visible && (app.status == RSRC_STATUS_READY)) {
                    app.setStatus(RSRC_STATUS_PLAYING); 
                }
            }
        }

        void removeChild(SimObject child)
        {
            super.removeChild(child);
            if (nchildren == 0 && !(child instanceof SimResource)) {
                app.close(RSRC_STATUS_CLOSED);
            }
        }

        String getClassName()
        {
            return "root";
        }
    }

    /**
     * A special filter stream that doesn't close the parent. This is used when
     * asking awt to create a new font, since awt likes to close the stream. We
     * don't want our connection to be closed...
     */
    static class NoCloseStream extends FilterInputStream
    {
        NoCloseStream(InputStream in)
        {
            super(in);
        }
        public void close()
        {
        }
    }

    /**
     * Helper thread for garbage collection. Do your part to keep the VM running
     * smoothly!
     */
    static class GCME implements Runnable
    {
        public void run()
        {
            for (int i = 0 ; i < 3 ; i++) {
                System.runFinalization();
                System.gc();
                try {
                    Thread.sleep(1000 * i);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * Static initializer for the content/extension maps.
     */
    static {
        contentToClass = new HashMap();
        contentToClass.put(IHmeConstants.MIME_TYPE, SimApp.class);
        contentToClass.put("image/jpeg", SimImageStream.class);
        contentToClass.put("image/jpg", SimImageStream.class);
        contentToClass.put("image/png", SimImageStream.class);
        contentToClass.put("image/gif", SimImageStream.class);
        contentToClass.put("audio/mpeg3", SimMp3Stream.class);

        extensionToClass = new HashMap();
        extensionToClass.put(".mp3", SimMp3Stream.class);
        extensionToClass.put(".png", SimImageStream.class);
        extensionToClass.put(".gif", SimImageStream.class);
        extensionToClass.put(".jpg", SimImageStream.class);
        extensionToClass.put(".jpeg", SimImageStream.class);        
    }
}
