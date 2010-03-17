//////////////////////////////////////////////////////////////////////
//
// File: HttpServer.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.tivo.hme.host.util.*;
import com.tivo.hme.host.http.share.*;

/**
 * Simple http server.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public abstract class HttpServer implements IHttpConstants
{
    protected Config config;

    // these are read from the config file
    protected String httpName;
    protected String acceptorName;    
    protected int backlog;
    protected String intfs[];
    protected int ports[];
    protected List pis;
    protected boolean draining;

    /**
     * Constructor.
     */
    public HttpServer(Config config)
    {
        this.config = config;
    }

    /**
     * Start the server - read the config object for setup info.
     */
    public void start() throws IOException
    {
        httpName = config.getValue("http.name", "HttpServer");
        acceptorName = config.getValue("http.acceptor.name", "Acceptor");
        backlog = config.getInt("http.backlog", 50);

        // get interface names and resolve to ip addresses
        intfs = config.getValueList("http.interfaces", InetAddress.getLocalHost().getHostAddress());
        for (int i = 0; i < intfs.length; ++i) {
            intfs[i] = InetAddress.getByName(intfs[i]).getHostAddress();
        }
        Arrays.sort(intfs);

        // just to be nice, put 127.0.01 at the bottom
        for (int i = 0; i < intfs.length; ++i) {
            if (intfs[i].equals("127.0.0.1")) {
                intfs[i] = intfs[intfs.length - 1];
                intfs[intfs.length - 1] = "127.0.0.1";
                break;
            }
        }

        pis = new ArrayList();
        
        // create ports
        ports = config.getIntList("http.ports");
        Arrays.sort(ports);
        for (int i = 0; i < ports.length; ++i) {
            Port p = new Port(this, ports[i]);
            ports[i] = p.port;
        }
    }

    public void drain()
    {
        // stop listening
        draining = true;
        for (Iterator i = pis.iterator(); i.hasNext();) {
            PortInterface pi = (PortInterface)i.next();
            try {
                pi.ss.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Get the list of intfs on which the server is listening.
     */
    public String[] getInterfaces()
    {
        return intfs;
    }

    /**
     * Get the list of ports on which the server is listening.
     */
    public int[] getPorts()
    {
        return ports;
    }

    /**
     * Returns true if the server is draining.
     */
    public boolean isDraining()
    {
        return draining;
    }

    //
    // subclasses can override these
    //

    /**
     * Handle a request.
     */
    abstract public void handle(HttpRequest request) throws IOException;

    /**
     * An exception occurred. The default behavior dumps the trace.
     */
    protected void handleException(Object context, Throwable t)
    {
        t.printStackTrace();
    }
    
    /**
     * Handle a continue request.  This is usually called by a client that is
     * going to POST or PUT some data and wants to know NOW if everything is OK.
     * If this returns true, then everything is OK, and the caller will take
     * care of sending the continue response. If this returns false, it is
     * assumed that this method replied with an error condition.
     */
    public boolean handleContinue(HttpRequest request) throws IOException
    {
        return true;
    }

    /**
     * Port - one per port. This Port will create one PortInterface for each
     * interface.
     */
    class Port
    {
        HttpServer server;  // the server
        int port;           // port on which we are listening
        int n;              // number of connectionx
        int max;            // max number of connections (or -1)

        Port(HttpServer server, int port) throws IOException
        {
            this.server = server;
            this.port = port;
            
            max = config.getInt("http.maxconnections." + port, -1);
            
            for (int i = 0; i < intfs.length; ++i) {
                new PortInterface(this, InetAddress.getByName(intfs[i]));
            }
        }

        synchronized boolean addConnection()
        {
            if (max >= 0 && n >= max) {
                return false;
            }
            ++n;
            return true;
        }

        synchronized void removeConnection()
        {
            --n;
        }
    }
        
    //
    // PortInterface - one per intf per port. Each listener accepts connections
    // in a seperate thread and creates a new thread for each incoming request.
    //

    class PortInterface implements Runnable
    {
        Port port;              // port on which we are listening
        InetAddress intf;       // intf on which we are listening
        ServerSocket ss;        // server socket accepting connections

        PortInterface(Port port, InetAddress intf) throws IOException
        {
            this.port = port;
            this.intf = intf;
            
            this.ss = new ServerSocket(port.port, backlog, intf);
            if (port.port == 0) {
                port.port = ss.getLocalPort();
            }
            pis.add(this);
            
            new Thread(this).start();
        }
        
        public void run()
        {
            Thread.currentThread().setName(httpName + "(" + intf.getHostAddress() + ":" + port.port + ")");
            
            try {
                while (true) {
                    new HttpConnection(this, ss.accept());
                }
            } catch (IOException e) {
                if (!draining) {
                    handleException(ss, e);
                }
            }
        }
    }
}
