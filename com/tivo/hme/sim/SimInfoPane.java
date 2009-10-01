//////////////////////////////////////////////////////////////////////
//
// File: SimInfoPane.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import com.tivo.hme.host.util.Misc;

/**
 * A pane which contains information and other panes of interest.
 *
 * Left side - Status Text
 * Right Side - Mouse Coordinates
 * Click and Drag to measure an area
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings({ "unchecked", "serial" })
public class SimInfoPane extends JComponent
{
    final static int INFOBAR_HEIGHT = 38;
    final static int MOUSEINFO_WIDTH = 60;
    
    SimPane sim;
    JPanel infoPanel;
    JTextArea text;
    
    public SimInfoPane(SimPane sim)
    {
        this.sim = sim;
        setSize(SimPane.WIDTH, INFOBAR_HEIGHT);
        setPreferredSize(getSize());
        setLayout(new BorderLayout());

        // create info text area: 2 rows by 40 columns
        text = new JTextArea(2, 40);
        text.setFont(new Font("Verdana", Font.PLAIN, 11));
        text.setBackground(getBackground());
        text.setEditable(false);

        // infoPanel contains the status message on left side
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1, 1));
        infoPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        infoPanel.add(text);

        // add panel to left and create Mouse tracking panel on right
        add(infoPanel, BorderLayout.CENTER);
        add(new MouseInfo(), BorderLayout.EAST);

        // make sure the cursor turns to text selector when over message text
        new Simulator.MouseCursorHandler(text, Cursor.TEXT_CURSOR);
    }
    
    public String getText()
    {
        return text.getText();
    }
    
    /**
     * Set the text in the left side status area if the message
     * is different.
     */
    void setText(String message)
    {
        Color c;
        if (message.startsWith("ERROR:")) {
            c = Color.red;
        } else if (message.startsWith("WARNING:")) {
            c = Color.red;
        } else {
            c = Color.black;
        }
        text.setForeground(c);
        text.setText(message);
    }

    /**
     * Display information about the currently selected item.
     */
    void setSelected(SimObject selected)
    {
        if (selected == null) {
            setText("");
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(" ");
            sb.append(Misc.replace(selected.toString(), "\n", "\\n"));
            List l = selected.getAbsoluteBounds();
            if(l.size() == 1) {
                Rectangle r = (Rectangle)l.get(0);
                sb.append("\n [" +
                          "x1=" + r.x + " " +
                          "y1=" + r.y + " " +
                          "x2=" + (r.x + r.width) + " " +
                          "y2=" + (r.y + r.height) + "]");
            } else {
                sb.append("\n [instances=" + l.size() + "]");
            }
            setText(sb.toString());
        }
    }


    /**
     * This component will track the mouse motion and display the
     * coordinates. This also takes care of setting the cross cursor for the
     * simulator.
     */
    class MouseInfo extends JComponent implements
                                       MouseMotionListener,
                                       MouseListener
    {
        JTextArea text;
        int dragStartX, dragStartY;
        
        public MouseInfo()
        {
            setSize(MOUSEINFO_WIDTH, INFOBAR_HEIGHT);
            setPreferredSize(getSize());
            setLayout(new BorderLayout());

            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

            text = new JTextArea(2, 16);
            text.setBackground(getBackground());
            text.setEditable(false);
            text.setFont(new Font("Courier", Font.PLAIN, 11));
            add(text);

            sim.addMouseMotionListener(this);
            sim.addMouseListener(this);

            new Simulator.MouseCursorHandler(text, Cursor.TEXT_CURSOR);
        }

        String format(int n)
        {
            String s = "" + n;
            if (s.length() < 4) {
                int len = 4 - s.length();
                StringBuffer sb = new StringBuffer();
                while (len-- > 0) {
                    sb.append(" ");
                }
                sb.append(s);
                return sb.toString();
            }
            return s;
        }
        
        // mouse Motion                                         
        public void mouseDragged(MouseEvent e)
        {
            setText("[drag " +
                    "x1=" + dragStartX + " " +
                    "y1=" + dragStartY + " " +
                    "x2=" + e.getX() + " " +
                    "y2=" + e.getY() + "]");
        }
        
        public void mouseMoved(MouseEvent e)
        {
            text.setText(" X: " + format(e.getX())+
                         "\n Y: " + format(e.getY()));
        }

        // mouse Listener
        public void mousePressed(MouseEvent e)
        {
            dragStartX = e.getX();
            dragStartY = e.getY();
        }

        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
    }
}
