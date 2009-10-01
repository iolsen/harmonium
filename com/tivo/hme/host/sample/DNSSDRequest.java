//////////////////////////////////////////////////////////////////////
//
// File: DNSSDRequest.java
//
// Copyright (c) 2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.sample;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class attempts to contact the local Rendezvous daemon
 * to register a service.
 *
 * @author Arthur van Hoff
 */
@SuppressWarnings("unchecked")
public class DNSSDRequest
{
    final static int PORT = 5354;
    final static int VERSION = 1;
    final static int REUSE_SOCKET = 2;
    final static int OPCODE_REGISTER = 5;
    final static int NO_REPLY = 1;
    Socket s;
    
    static Thread shutdown;                // used for shutdown hook
    static Vector list = new Vector();     // keeps track of all requests.
    
    /**
     * Create a DNSSDRequest. If the daemon is not running
     * it will throw an IOException.
     */
    public DNSSDRequest() throws IOException
    {
        s = new Socket("127.0.0.1", PORT);
        list.add(this);
        if (shutdown == null) {
            shutdown = new Thread(new Shutdown(), "DNSSD.Shutdown");
            Runtime.getRuntime().addShutdownHook(shutdown);
        }
    }
    
    /**
     * Register a service given a name, type, and url.
     * The hostname in the URL is ignored (it is always localhost).
     */
    public void registerService(String name, String type, String url) throws MalformedURLException, IOException
    {
        registerService(name, type, new URL(url));
    }
    
    /**
     * Register a service given a name, type, and URL.
     * The hostname in the URL is ignored (it is always localhost).
     */
    public void registerService(String name, String type, URL url) throws IOException
    {
        Vector v = new Vector();
        v.addElement("path=" + url.getFile());
        registerService(name, type, url.getPort(), v);
    }
    
    /**
     * Register a service given a name, type, port, and vector of text properties.
     */
    public void registerService(String name, String type, int port, Vector txt) throws IOException
    {
        // data
        byte data[] = new byte[1024];
        int datalen = 0;
        for (Enumeration e = txt.elements() ; e.hasMoreElements() ; ) {
            String val = (String)e.nextElement();
            data[datalen++] = (byte)val.length();
            for (int i = 0, len = val.length() ; i < len ; i++) {
                data[datalen++] = (byte)val.charAt(i);
            }
        }
        
        // create message
        ByteArrayOutputStream msgout = new ByteArrayOutputStream();
        writeInt(msgout, NO_REPLY); // flags
        writeInt(msgout, 0); // interface
        writeUTF80(msgout, name);
        writeUTF80(msgout, type);
        writeUTF80(msgout, ""); // domain
        writeUTF80(msgout, ""); // host
        writeShort(msgout, ((port >> 8) & 0xFF) + ((port & 0xFF) << 8));
        writeShort(msgout, datalen);
        msgout.write(data, 0, datalen);
        
        byte msg[] = msgout.toByteArray();
        
        // write the request
        BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());
        writeInt(out, VERSION); 	// version
        writeInt(out, msg.length);      // datalen
        writeInt(out, REUSE_SOCKET);    // flags
        writeInt(out, OPCODE_REGISTER); // opcode
        writeInt(out, 0);               // client context
        writeInt(out, 0);               // client context
        writeInt(out, 0);               // reg index
        out.write(msg, 0, msg.length);
        out.flush();
        
        int reply = readReply();
        if (reply != 0) {
            throw new IOException("Registration failed: " + reply);
        }
    }
    
    /**
     * Write an integer.
     */
    void writeInt(OutputStream out, int val) throws IOException
    {
        // REMIND: platform specific, on big endian systems this
        // needs to be changed
        out.write((val >> 0) & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }
    
    /**
     * Write a short.
     */
    void writeShort(OutputStream out, int val) throws IOException
    {
        // REMIND: platform specific, on big endian systems this
        // needs to be changed
        out.write((val >> 8) & 0xFF);
        out.write((val >> 0) & 0xFF);
    }
    
    /**
     * Write a UTF8 null terminated string.
     */
    void writeUTF80(OutputStream out, String str) throws IOException
    {
        for (int i = 0, len = str.length() ; i < len ; i++) {
            int c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out.write(c);
            } else if (c > 0x07FF) {
                out.write(0xE0 | ((c >> 12) & 0x0F));
                out.write(0x80 | ((c >>  6) & 0x3F));
                out.write(0x80 | ((c >>  0) & 0x3F));
            } else {
                out.write(0xC0 | ((c >>  6) & 0x1F));
                out.write(0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(0);
    }
    
    /**
     * Write some bytes from a string.
     */
    void writeBytes(OutputStream out, String str) throws IOException
    {
        for (int i = 0, len = str.length() ; i < len ; i++) {
            out.write(str.charAt(i));
        }
    }
    
    /**
     * Read an integer reply.
     */
    int readReply() throws IOException
    {
        InputStream in = s.getInputStream();
        int c0 = in.read();
        int c1 = in.read();
        int c2 = in.read();
        int c3 = in.read();
        
        return ((c0 & 0xFF) << 0) +
        ((c1 & 0xFF) << 8) +
        ((c2 & 0xFF) << 16) +
        ((c3 & 0xFF) << 24);
    }
    
    /**
     * Close the request.
     */
    public void close() 
    {
        if (s != null) {
            list.remove(this);
            try {
                s.close();
            } catch (IOException e) {
                // ignore
            }
            s = null;
        }
    }
    
    /**
     * This shutdown hook thread will Unregister all applications when the VM is
     * shut down.
     */
    static class Shutdown implements Runnable
    {
        public void run()
        {
            DNSSDRequest.shutdown = null;
            while (list.size() > 0) {
                ((DNSSDRequest)list.elementAt(0)).close();
            }
        }
    }
}
