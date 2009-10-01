//////////////////////////////////////////////////////////////////////
//
// File: SimPane.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.tree.TreePath;

import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.io.FastOutputStream;
import com.tivo.hme.sdk.io.HmeOutputStream;

/**
 * A pane which contains the application running in the Simulator. There is only
 * one SimPane.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings({ "unchecked", "serial" })
public class SimPane extends JComponent implements IHmeProtocol, MouseListener
{
    // current app
    String url;
    ZeroView zero;

    // hilited object
    SimObject selected;
    int pos;
    List planes;

    // overlay (for selected view/rsrc hilite and safe.png)
    OverlayPane overlay;

    long tm0;
    HmeOutputStream record;
    SimKeyManager keyManager;

    public SimPane(int width, int height)
    {
        setSize(width, height);
        setPreferredSize(getSize());
        setLayout(null);
        setOpaque(true);

        keyManager = new SimKeyManager( this );
        
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        addMouseListener(this);
        
        overlay = new OverlayPane();
        overlay.setBounds(0, 0, width, height);
        add(overlay);

        zero = new ZeroView(width, height);

        synchronized (SimPane.class) {
            if (Simulator.RECORD) {
                try {
                    tm0 = System.currentTimeMillis();
                    record = new HmeOutputStream(new FastOutputStream(new FileOutputStream("record.txt"), 1024));
                    System.out.println("Created record.txt");
                } catch (IOException e) {
                    System.err.println("Could not create record.txt : " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    synchronized void record(HmeEvent evt)
    {
        if (record != null) {
            // maybe write to record

            try {
                //long tm = System.currentTimeMillis() - tm0;
                FastOutputStream buf = new FastOutputStream(128);
                //ByteArrayOutputStream buf = new ByteArrayOutputStream(128);
                HmeOutputStream buf2 = new HmeOutputStream(buf);
                if (getApp().version >= VERSION_0_40 )
                {
                    buf2.setUseVString( true );
                }
                evt.write(buf2);
                byte data[] = buf.toByteArray();
                record(evt, data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Recording disabled");
                record = null;
            }
        }
    }

    synchronized void record(HmeEvent evt, byte data[], int off, int len)
    {
        if (record != null) {
            try {
                StringBuffer sb = new StringBuffer(len + 2);
                final char hex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                    'A', 'B', 'C', 'D', 'E', 'F'};
                for (int i = 0; i < len; i++) {
                    int b = data[off + i] & 0xFF;
                    sb.append(hex[b >> 4]).append(hex[b & 0xF]);
                }
                long tm = System.currentTimeMillis() - tm0;
                // record a raw event
                record.print(tm + ": " + sb + ": ");
                record.println((evt == null) ? "RAW" : evt.toString());
                record.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Recording disabled");
                record = null;
            }
        }
    }


    //
    // app lifecycle management
    //
    
    public void run()
    {
        while (true) {
            //
            // run the app
            //

            if (url != null) {
                zero.run(url);
            }

            //
            // the app is dead. sleep and try again.
            //

            try {
                Thread.sleep(2000);
                if (url != null) {
                    Simulator.get().setStatus("Connecting:  " + url);
                    Simulator.get().setBusy(true);
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    void setURL(String url)
    {
        this.url = url;
        if (zero.app != null) {
            zero.app.close();
        }
    }

    SimApp getApp()
    {
        return zero.app;
    }

    //
    // painting
    //

    void setSelected(SimObject selected)
    {
        this.selected = selected;
        Simulator.get().info.setSelected(selected);
        overlay.repaint();
    }

    void touch()
    {
        repaint();
    }

    synchronized public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.black);
        g2.fillRect(0, 0, getWidth(), getHeight());
        zero.paint(null, g2);
    }

    //
    // events
    //
    
    protected void processComponentKeyEvent(KeyEvent e)
    {
        keyManager.processKeyEvent( e );
    }

    SimResource getTargetForKey( int keycode )
    {
        if ( keycode == KEY_TIVO )
        {
            return zero.getResource();
        }

        return Simulator.get().active;
    }

    public void mousePressed(MouseEvent e)
    {
        List newPlanes = new Vector();
        zero.findObjectsAt(e.getPoint(), new Vector(), newPlanes);
        if (!newPlanes.equals(planes)) {
            planes = newPlanes;
            pos = 0;
        } else {
            pos = (pos + 1) % planes.size();
        }
        List selected = (List)planes.get(pos);
        Simulator.get().setTreePath(new TreePath(selected.toArray()));
    }
    
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseClicked(MouseEvent e) { }

    
    /**
     * A helper for drawing the hilite.
     */
    class OverlayPane extends JComponent
    {
        Image safeImage;
        
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (Simulator.SHOW_SAFE) {
                if (safeImage == null) {
                    safeImage = Simulator.get().getImage("safe.png");
                }
                g2.drawImage(safeImage, 0, 0, this);
            }

            if (Simulator.SHOW_HILITE && selected != null) {
                List list = selected.getAbsoluteBounds();
                Color c = new Color((selected instanceof SimView) ? 0x60245EDC : 0x60E4FD3B, true);
                for (Iterator i = list.iterator(); i.hasNext();) {
                    Rectangle r = (Rectangle)i.next();
                    g2.setColor(c);
                    g2.fill(r);
                    g2.setColor(new Color(0x60000000, true));
                    g2.setStroke(new BasicStroke(3f));
                    g2.draw(r);
                }
            }
        }

        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
        {
            if ((infoflags & ALLBITS) != 0) {
                repaint();
                return false;
            }
            return true;
        }
    }

    
    /**
     * The zero view for the simulator. Acts as the bridge between SimPane and
     * the app.
     */
    class ZeroView extends SimView
    {
        ZeroView(int width, int height)
        {
            super(null, -1, null, 0, 0, width, height, true);
        }

        void run(String url)
        {
            app = new SimApp(null, ID_ROOT_STREAM, url, true);
            
            setResource(app);
            app.run();
            setResource(null);
        }
        
        synchronized void repaint(Rectangle r, boolean flush)
        {
            if (r != null) {
                // expand the rect a bit to account for the hilite
                r.grow(2, 2);
                SimPane.this.repaint(r.x, r.y, r.width, r.height);
            } else {
                SimPane.this.repaint();       
            }
        }

        public String toString()
        {
            return "Simulator";
        }
    }
}
