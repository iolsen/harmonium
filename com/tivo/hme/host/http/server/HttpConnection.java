//////////////////////////////////////////////////////////////////////
//
// File: HttpConnection.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.tivo.hme.host.http.share.IHttpConstants;
import com.tivo.hme.host.io.FastInputStream;
import com.tivo.hme.host.io.FastOutputStream;

/**
 * A class that represents an HTTP connection that spans multiple requests.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class HttpConnection implements IHttpConstants, Runnable
{
    HttpServer.PortInterface pi;        // http server PortInterface
    Socket s;                           // socket
    InputStream in;                 // "raw" input stream
    OutputStream out;        // "raw" output stream
    HttpRequest request;                // current request we're working on

    /**
     * Creates a new connection from the specified server and socket.
     */
    public HttpConnection(HttpServer.PortInterface pi, Socket s) throws IOException
    {
        this.pi = pi;
        this.s = s;
        new Thread(this).start();
    }
    
    /**
     * The input stream with a close method that closes the current request
     * but not the underlying input stream.
     */
//    class InStream extends FastInputStream {
//        InStream(InputStream in, int size) {
//            super(in, size);
//        }
//        public void close() throws IOException {
//            if (request != null) {
//                request.close();
//            }
//        }
//    }
    class InStream extends FastInputStream {
        InStream(InputStream in, int size) {
            super(in, size);
        }
        public void close() throws IOException {
            if (request != null) {
                request.close();
            }
        }
    }

    /**
     * Runs the connection.
     */
    public void run()
    {
        Thread.currentThread().setName(pi.port.server.acceptorName);

        //
        // are we allowed to add more connections?
        //
        
        if (!pi.port.addConnection()) {
            // no, reply with 503 Server Busy
            try {
                getOutputStream();
                out.write("HTTP/1.1 503 Server Busy\r\n\r\n".getBytes());
                out.flush();
            } catch (IOException e) {
                // ignore - we're busy
            } finally {
                close();
            }
            return;
        }
        
        try {
//            in = new InStream(s.getInputStream(), TCP_BUFFER_SIZE);
            in = new InStream(s.getInputStream(), TCP_BUFFER_SIZE);
            int cnt = 0;
            while ((request = HttpRequest.read(this, cnt > 0)) != null) {
                // check for Expect: 100-continue header
                if ("100-continue".equalsIgnoreCase(request.get("expect"))) {
                    if (pi.port.server.handleContinue(request)) {
                        getOutputStream();
                        out.write((request.get("http-version") + " 100 Continue\r\n\r\n").getBytes());
                        out.flush();
                    } else {
                        if (!request.replied) {
                            request.reply(500, "Continue not OK, and not handled");
                        }
                        getOutputStream().flush();
                        break;
                    }
                }

                //
                // give the request back to the server for processing
                //
                    
                try {
                    pi.port.server.handle(request);
                } catch (IOException e) {
                    pi.port.server.handleException(this, e);
                } catch (RuntimeException e) {
                    pi.port.server.handleException(this, e);
                } finally {
                    try {
                        request.close();
                    } catch (IOException e) {
                    }
                    if (s == null) {
                        // this was a keep alive connection, and we closed.
                        return;
                    }
                }
                cnt += 1;

                // clear the limits on the input/output streams
//                in.setLimit(-1);
//                out.setLimit(-1);
//                out.reset();
            }
        } catch (IOException e) {
            pi.port.server.handleException(request, e);
        } finally {
            pi.port.removeConnection();
            close();
        }
    }

    /**
     * Get the local interface.
     */
    public InetAddress getInterface()
    {
        return pi.intf;
    }
    
    /**
     * Get the local port.
     */
    public int getPort()
    {
        return pi.port.port;
    }
    
    /**
     * Grab the socket.
     */
    public Socket getSocket()
    {
        return s;
    }

    /**
     * Return the input stream.
     */
    public InputStream getInputStream()
    {
        return in;
    }

    /**
     * Gets a buffered output stream for the reply.  Re-uses the
     * input stream's buffer if the HTTP method implies no sharing.
     */
    public OutputStream getOutputStream() throws IOException
    {
        if (out == null) {
            //out = new HttpChunkedOutputStream(s.getOutputStream(), TCP_BUFFER_SIZE);
            out = new FastOutputStream(s.getOutputStream(), TCP_BUFFER_SIZE);
        }
        return out;
    }

    /**
     * Close this connection.  No more keep-alive.
     */
    void close()
    {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                // ignore this - what good can we do with it?
            } finally {
                s = null;
            }
        }
    }
}
