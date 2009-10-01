//////////////////////////////////////////////////////////////////////
//
// File: SimMDNS.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.host.util.Misc;
import com.tivo.hme.interfaces.IHmeConstants;

/**
 * This class manages the Network menu and the Application menu in the
 * simulator.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class SimMDNS implements
                     IHmeProtocol, ServiceListener, ServiceTypeListener,
                     ActionListener, Comparator, ISimPrefs
{
    JmDNS mdns;

    JMenu appMenu, networkMenu;
    JMenuItem noAppsItem;

    Hashtable list = new Hashtable();

    ImageIcon defaultIcon;
    IconFetcher iconFetcher;
    
    public SimMDNS(JMenuBar menubar)
    {
        try {
//             if (Simulator.DEBUG) {
//                 System.getProperties().put("jmdns.debug", "1");
//             }

            //
            // Initialize the menus
            //
            networkMenu = new JMenu("Network");
            appMenu     = new JMenu("Applications");
            noAppsItem  = new JMenuItem("No Applications");

            noAppsItem.setEnabled(false);
            appMenu.add(noAppsItem);
            
            defaultIcon = new ImageIcon(Simulator.getResourceAsURL("default_app.png"));
            //
            // Build Network menu
            //
            InetAddress intf[] = Misc.getInterfaces();
            Arrays.sort(intf, this);
            for (int i = 0; i < intf.length; i++) {
                JMenuItem item = new JCheckBoxMenuItem(intf[i].getHostAddress());
                item.addActionListener(this);
                networkMenu.add(item);
            }

            iconFetcher = new IconFetcher();
            
            menubar.add(networkMenu);
            menubar.add(appMenu);
            loadPrefs();
        } catch(IOException e) {
            e.printStackTrace();
            Simulator.get().setStatus("ERROR: Failed to get interfaces : " + e.toString());
        }
    }
    
    /**
     * Set the network interface used for mDNS, and resets the application menu.
     */
    void setInterface(String host)
    {
        try {
            InetAddress addr = InetAddress.getByName(host);

            //
            // Create the new service listener, disposing of old one if needed
            //
            if (mdns != null) {
                mdns.close();
            }
            mdns = new JmDNS(addr);
            mdns.addServiceListener(IHmeConstants.MDNS_TYPE, this);

            //
            // fix up the apps and network menus
            //
            appMenu.removeAll();
            appMenu.add(noAppsItem);
            for (int i = 0; i < networkMenu.getItemCount(); i++) {
                JMenuItem item = networkMenu.getItem(i);
                item.setSelected(item.getText().equals(host));
            }
        } catch (IOException e) {
            Simulator.get().setStatus("WARNING: Could not set interface: " + host);
            mdns = null;
        }
    }

    public void storePrefs(Preferences prefs) throws IOException
    {
        if (mdns != null) {
            prefs.put("addr", mdns.getInterface().getHostAddress());
        }
    }

    void loadPrefs()
    {
        String address = Simulator.get().prefs.get("addr", null);
        if (address != null) {
            setInterface(address);
        } 
    }


    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.startsWith("http")) {
            Simulator.get().setURL(cmd);
        } else {
            setInterface(cmd);
        }
    }

        
    public void addService(JmDNS jmdns, String type, String name)
    {
        ServiceInfo info = jmdns.getServiceInfo(type, name, 3*1000);

        //
        // create the menu item for this application
        //
        String label = info.getURL("http");
        JMenuItem item = new JMenuItem(label, defaultIcon);
        item.addActionListener(this);

        // save this in the application hashtable
        list.put(name, item);

        // get the icon for this item
        iconFetcher.addItem(item);
        
        //
        // insert into menu alphabetically
        //
        appMenu.remove(noAppsItem);
        for (int i = 0; i < appMenu.getItemCount(); i++) {
            if (label.compareTo(appMenu.getItem(i).getText()) < 0) {
                appMenu.insert(item, i);
                return;
            }
        }
        appMenu.add(item);
    }
        
    public void removeService(JmDNS jmdns, String type, String name)
    {
        appMenu.remove((JMenuItem)list.get(name));
        list.remove(name);
    }

    public void resolveService(JmDNS jmdns, String type, String name, ServiceInfo info) {}
    public void addServiceType(JmDNS jmdns, String type) {}


    public boolean equals(Object o1)
    {
        return o1 == this;
    }
    
    public int compare(Object o1, Object o2)
    {
        String h1 = ((InetAddress)o1).getHostAddress();
        String h2 = ((InetAddress)o2).getHostAddress();
        return h1.compareTo(h2);
    }


    public class IconFetcher implements Runnable
    {
        Vector list = new Vector();
        
        public IconFetcher()
        {
            new Thread(this, "IconFetcher").start();
        }

        synchronized int size()
        {
            return list.size();
        }

        synchronized void addItem(JMenuItem item)
        {
            list.add(item);
            notify();
        }

        synchronized JMenuItem getItem()
        {
            while (list.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            return (JMenuItem)list.remove(0);
        }
        
        public void run()
        {
            while (true) {
                JMenuItem item = getItem();
                try {
                    URL url = new URL(item.getText()+"icon.png");
                    ImageIcon icon = new ImageIcon(url);
                    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        item.setIcon(new ImageIcon(icon.getImage().getScaledInstance(20,16, Image.SCALE_DEFAULT)));
                    } else {
                        item.setIcon(defaultIcon);
                    }
                } catch(IOException  e) {
                    e.printStackTrace();
                    item.setIcon(defaultIcon);
                }
            }
        }
        
    }
}
