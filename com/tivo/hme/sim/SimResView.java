//////////////////////////////////////////////////////////////////////
//
// File: SimResView.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.sim;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

/**
 * Resource Usage Summary
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 * @version     $Revision: #2 $, $DateTime: 2005/02/21 12:26:56 $
 */

@SuppressWarnings({ "unchecked", "serial" })
public class SimResView extends JFrame implements Runnable, ActionListener
{
    JTextPane outText;
    Simulator sim;
    Thread gcThread;
    Thread updateThread;
    boolean done = false;

    
    
    @SuppressWarnings("deprecation")
	public SimResView()
    {
        super("Resource Usage");
        sim = Simulator.get();
        getContentPane().setLayout(new BorderLayout());
        setIconImage(Simulator.get().getImage("tivo.png"));

        // main resource counters
        outText = new JTextPane();
        outText.setContentType("text/html");
        outText.setEditable(false);
        JPanel center = new JPanel();
        center.add(outText);
        getContentPane().add(center, BorderLayout.CENTER);
        
        // the bottom display
        JPanel buttonPanel = new JPanel();
        JButton button = new JButton("Run GC");
        button.setActionCommand("GC");
        button.addActionListener(this);
        buttonPanel.add(button);

        button = new JButton("Dump");
        button.setActionCommand("Dump");
        button.addActionListener(this);
        buttonPanel.add(button);

/**        button = new JButton("Graph");
        button.setActionCommand("GRAPH");
        button.addActionListener(this);
        buttonPanel.add(button);
**/
        
        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        south.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(south, BorderLayout.SOUTH);

        // initialize counter panel and size window
        refreshDisplay();
        pack();

        // position the window and show it
        Rectangle r = sim.simFrame.getBounds();
        setLocation(r.x, r.y + r.height);
        setResizable(false);
        show();

        updateThread = new UpdateThread();
        updateThread.start();
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("GC")) {
            if (gcThread == null) {
                gcThread = new GCThread();
                gcThread.start();
            }
        } else if (cmd.equals("Dump")) {
            getStats(true);
        }
    }

    class UpdateThread extends Thread
    {
        public void run() {
            try {
                while (!done) {
                    update();
                    Thread.sleep(1000);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class GCThread extends Thread {
        public void run()
        {
            for (int i = 0 ; i < 3 ; i++) {
                System.out.println("*** RUNNING GC ***");
                System.runFinalization();
                System.gc();
                try {
                    Thread.sleep(1000 * i);
                } catch (InterruptedException e) {
                    return;
                }
            }
            gcThread = null;
            update();
        }
    }
    
    public void update()
    {
        SwingUtilities.invokeLater(this);
    }
    
    public void run()
    {
        refreshDisplay();
    }

    String pad(long x, int len)
    {
        String s = ""+x;
        StringBuffer sb = new StringBuffer();
        String SPACES = "        ";
        len = len - s.length();
        if (len > 0) {
            sb.append(SPACES.substring(0, len));
        }
        sb.append(s);
        return sb.toString();
    }
    

    public String getStats(boolean dump)
    {
        int viewCount = 0;
        int appCount = 0;
        if (sim.sim.getApp() != null) {
            if (sim.sim.getApp().resources != null) {
                appCount = sim.sim.getApp().resources.size();
                viewCount = sim.sim.getApp().views.size();
            }
        }
        long totalMem = (Runtime.getRuntime().maxMemory() / 1024)/1024;
        long freeMem = Runtime.getRuntime().freeMemory() / 1024;


        if (dump) {
            System.out.print("\nResource Usage: ");
            System.out.print(new Date().toString());
            System.out.print("\n");

            System.out.print("Resource   Sim    GC\n");
            System.out.print("Anim   : ");
            System.out.print(pad(SimResource.resourceAnimCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCAnimCount, 5));
            System.out.print("\n");

            System.out.print("Color  : ");
            System.out.print(pad(SimResource.resourceColorCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCColorCount, 5));
            System.out.print("\n");

            System.out.print("Font   : ");
            System.out.print(pad(SimResource.resourceFontCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCFontCount, 5));
            System.out.print("\n");

            System.out.print("Image  : ");
            System.out.print(pad(SimResource.resourceImageCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCImageCount, 5));
            System.out.print("\n");

            System.out.print("Sound  : ");
            System.out.print(pad(SimResource.resourceSoundCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCSoundCount, 5));
            System.out.print("\n");

            System.out.print("Stream : ");
            System.out.print(pad(SimResource.resourceStreamCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCStreamCount, 5));
            System.out.print("\n");

            System.out.print("Text   : ");
            System.out.print(pad(SimResource.resourceTextCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCTextCount, 5));
            System.out.print("\n");
        
            System.out.print("Total  : ");
            System.out.print(pad(SimResource.resourceCount, 5));
            System.out.print(" ");
            System.out.print(pad(SimResource.resourceGCCount, 5));
            System.out.print("\n");

            System.out.print("App    : ");
            System.out.print(pad(appCount, 5));
            System.out.print(" ");
            System.out.print("\n");

            System.out.print("System : ");
            System.out.print(pad(sim.resources.size(), 5));
            System.out.print(" ");
            System.out.print("\n");

            System.out.print("Views  : ");
            System.out.print(pad(viewCount, 5));
            System.out.print("\n");

            System.out.print("Memory : ");
            System.out.print(pad(freeMem, 4));
            System.out.print("k ");
            System.out.print(pad(totalMem, 4));
            System.out.print("M\n");

            System.out.println("\nResource Dump\n");
            Collection values = sim.resources.values();
            Iterator iter = values.iterator();
            int i = 0;
            while (iter.hasNext()) {
                i++;
                SimResource rsrc = (SimResource)iter.next();
                System.out.println(i +  " " + rsrc.toString());
            }
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><body><font face=\"Courier\" Size=3>");
            sb.append("<table style=\"border-style: solid; border-width: 1px\" width=200>");
            sb.append("<tr bgcolor=#CCCCCC><td><b>Resource</td>");
            sb.append("<td align=RIGHT><b>Sim</td><td ALIGN=RIGHT><b>GC</td></tr>");

            sb.append("<tr><td>Animation :</td><td align=RIGHT>");
            sb.append(SimResource.resourceAnimCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCAnimCount);
            sb.append("</td></tr>");
            

            sb.append("<tr><td>Color :</td><td align=RIGHT>");
            sb.append(SimResource.resourceColorCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCColorCount);
            sb.append("</td></tr>");

            sb.append("<tr><td>Font :</td><td align=RIGHT>");
            sb.append(SimResource.resourceFontCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCFontCount);
            sb.append("</td></tr>");

            sb.append("<tr><td>Image :</td><td align=RIGHT>");
            sb.append(SimResource.resourceImageCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCImageCount);
            sb.append("</td></tr>");

            sb.append("<tr><td>Sound :</td><td align=RIGHT>");
            sb.append(SimResource.resourceSoundCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCSoundCount);
            sb.append("</td></tr>");

            sb.append("<tr><td>Stream :</td><td align=RIGHT>");
            sb.append(SimResource.resourceStreamCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCStreamCount);
            sb.append("</td></tr>");

            sb.append("<tr><td>Text :</td><td align=RIGHT>");
            sb.append(SimResource.resourceTextCount);
            sb.append("</td></td><td align=RIGHT>");
            sb.append(SimResource.resourceGCTextCount);
            sb.append("</td></tr>");
        
            sb.append("<tr><td><b>Total:</b></td><td align=RIGHT><b>");
            sb.append(SimResource.resourceCount);
            sb.append("</b></td></td><td align=RIGHT><b>");
            sb.append(SimResource.resourceGCCount);
            sb.append("</b></td></tr>");

            sb.append("<tr><td><b>App:</b></td><td align=RIGHT><b>");
            sb.append(appCount);
            sb.append("</b></td></td><td align=RIGHT><b>");
            sb.append("</b></td></tr>");

            sb.append("<tr><td><b>System :</b></td><td align=RIGHT><b>");
            sb.append(sim.resources.size());
            sb.append("</b></td></td><td align=RIGHT><b>");
            sb.append("</b></td></tr>");

            sb.append("<tr><td><b>Views:</b></td><td align=RIGHT><b>");
            sb.append(viewCount);
            sb.append("</b></td></td><td align=RIGHT><b>");
            sb.append("</b></td></tr>");

            sb.append("<tr><td><b>Memory:</b></td><td align=RIGHT><b>");
            sb.append(freeMem);
            sb.append("k</b></td></td><td align=RIGHT><b>");
            sb.append(totalMem);
            sb.append("M</b></td></tr>");

            sb.append("</font></body></html>");
            return sb.toString();
        }

    }
    
    public void refreshDisplay()
    {
        outText.setText(getStats(false));
    }
}
