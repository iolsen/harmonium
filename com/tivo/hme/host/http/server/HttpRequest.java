//////////////////////////////////////////////////////////////////////
//
// File: HttpRequest.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import com.tivo.hme.interfaces.IHttpRequest;
import com.tivo.hme.host.http.share.Headers;
import com.tivo.hme.host.http.share.HttpException;
import com.tivo.hme.host.http.share.IHttpConstants;

// REMIND: request timeouts

/**
 * HTTP request.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class HttpRequest implements IHttpRequest, IHttpConstants
{
    InetAddress source;           // source IP address
    HttpConnection conn;          // our connection
    InputStream in;               // input stream handed to client (if asked for) 
    OutputStream out;  // output stream handed to client (if asked for)
    String method;                // request method
    String uri;                   // request uri
    Headers headers;              // mime headers
    int httpVersion;              // HTTP_10 or HTTP_11
//    boolean chunkedEnabled;       // whether or not chunked encoding is enabled
    boolean keep;                 // whether or not to keep this connection alive
    boolean replied;              // true if somebody replied to this request
    static char str[];

    /**
     * A static method to load a request from a connection, or return null if
     * there are no more requests for this connection.  If blankOK is true, then
     * it's OK for there to be one blank line before the real request.  This is
     * related to problems with browsers that send an extra \r\n when POSTing
     * requests to servers.  It's wrong to do that but we have to support it.
     */
    static HttpRequest read(HttpConnection conn, boolean blankOK) throws IOException
    {
        InputStream in = conn.getInputStream();
        String ln = readLine(in);
//        System.out.println("*** read line = ->"+ln +"<-");
        if (ln == null) {
            // eof occurred
            return null;
        }
        if (ln.length() == 0) {
            // blank line, which is OK after a POST on a previous keep-alive
            if (blankOK) {
                ln = readLine(in);
//                System.out.println("*** read line = ->"+ln +"<-");
                if (ln == null) {
                    return null;
                }
            }
        }
        return new HttpRequest(conn, ln);
    }

    /**
     * Reads a line from the input stream.  A line is terminated by EOF, CR, LF
     * or CRLF.  If EOF is reached and there is no data in the line, the null is
     * returned.
     */
    public static String readLine(InputStream in) throws IOException
    {
        
        int i = 0;
        
        if (str == null) {
            str = new char[256];
        }
        loop:
            while (true) {
                int ch = in.read();
                switch (ch) {
                case -1:
                    if (i > 0) {
                        break loop;
                    }
                    // eof with no data
                return null;
                
                case '\n':
                    break loop;
                    
                case '\r':
                    if (in.markSupported())
                    {
                        in.mark(1);
                        if ((ch = in.read()) != '\n') {
                            //in.unread(ch);
                            in.reset();
                        }
                    } else {
                        
                    }
                    break loop;
                }
                if (i == str.length) {
                    char newstr[] = new char[str.length * 2];
                    System.arraycopy(str, 0, newstr, 0, i);
                    str = newstr;
                }
                str[i++] = (char)ch;
            }
        String s = new String(str, 0, i);
        return s;
    }
    
    /**
     * A new HttpRequest for the specified server/socket, and the first
     * line read from the request.
     */
    private HttpRequest(HttpConnection conn, String ln) throws IOException
    {
        this.conn = conn;
        this.in = conn.getInputStream();

        // parse method, uri and http version
        //FastInputStream in = conn.in;
        int i = ln.indexOf(' ');
        if (i < 0) {
            throw new HttpException("malformed HTTP request: " + ln);
        }
        int j = ln.indexOf(' ', i+1);
        if (j < 0) {
            throw new HttpException("malformed HTTP request: " + ln);
        }
        method = ln.substring(0, i);
        uri = ln.substring(i+1, j);

        String version = ln.substring(j+1);
        if (version.endsWith("/1.0")) {
            httpVersion = HTTP_VERSION_10;
        } else {
            httpVersion = HTTP_VERSION_11;
        }
        
        // read the mime header
        headers = new Headers(in);
        headers.addInternal("http-method", method);
        headers.addInternal("http-uri", uri);
        headers.addInternal("http-version", version);
//        System.out.println("*** headers: " + headers.toString());

        // check for keep-alive
        String connection = headers.get("connection");
        switch (httpVersion) {
          case HTTP_VERSION_10:
            if ("Keep-Alive".equalsIgnoreCase(connection)) {
                // keep is true if content-length is also specified
                keep = !requestHasPayload() || getContentLength() >= 0;
            }
            break;

          case HTTP_VERSION_11:
          default:
            // HTTP 1.1 assumes keep-alive unless the "Connection: close" is specified.
            keep = !"close".equals(connection);
            break;
        }
//        System.out.println("*** keep = " + keep);
    }

    /**
     * Get the http server.
     */
    public HttpServer getServer()
    {
        return conn.pi.port.server;
    }

    /**
     * Get the connection.
     */
    public HttpConnection getConnection()
    {
        return conn;
    }

    /**
     * Get the source IP address.
     */
    public synchronized InetAddress getInetAddress()
    {
        if (source == null) {
            source = conn.s.getInetAddress();
        }
        return source;
    }

    /**
     * Get the local interface.
     */
    public InetAddress getInterface()
    {
        return conn.getInterface();
    }
    
    /**
     * Get the local port.
     */
    public int getPort()
    {
        return conn.getPort();
    }
    
    /**
     * Returns true if the upstream has a payload.
     */
    private boolean requestHasPayload()
    {
        return method.equals("POST") || method.equals("PUT");
    }

    /**
     * Get a MIME header.
     */
    public String get(String header)
    {
        return headers.get(header);
    }

    /**
     * Get all MIME headers.
     */
    public Headers getHeaders()
    {
        return headers;
    }
    
    /**
     * Returns the HTTP request method.
     */
    public String getMethod()
    {
        return method;
    }

    /**
     * Returns the HTTP URI.
     */
    public String getURI()
    {
        return uri;
    }

    /**
     * Return the HTTP version.
     */
    public int getVersion()
    {
        return httpVersion;
    }

    /**
     * Returns the content length for this request, or -1 if not known.
     */
    public long getContentLength()
    {
        String cl = get("content-length");
        return (cl != null) ? Long.parseLong(cl) : -1;
    }

    /**
     * Get the content type (in lower case, without its parameters)
     */
    public String getContentType()
    {
        String ct = get("content-type");
        if (ct == null) {
            return "unknown";
        }
        int i = ct.indexOf(';');
        if (i >= 0) {
            ct = ct.substring(0, i);
        }
        return ct.trim().toLowerCase();
    }

    /**
     * Return true when replied.
     */
    public boolean getReplied()
    {
        return replied;
    }

    /**
     * Get the content encoding (ie charset) of this document.
     */
    public String getContentEncoding()
    {
        String ct = get("content-type");
        if (ct != null) {
            int i = ct.indexOf("charset=");
            if (i >= 0) {
                ct = ct.substring(i + 8);
                if ((i = ct.indexOf(';')) >= 0) {
                    ct = ct.substring(0, i);
                }
                if (ct.startsWith("\"") && ct.endsWith("\"")) {
                    ct = ct.substring(1, ct.length() - 1);
                }
                return ct.trim().toLowerCase();
            }
        }
        return "ascii";
    }

    /**
     * Gets an input stream on the request.
     */
    public InputStream getInputStream() throws IOException
    {
        // you can call this more than once
        if (in != null) {
            return in;
        }

        // no input stream unless we're posting
        if (!requestHasPayload()) {
            return null;
        }

        // figure out what kind of input stream to use
        String str = get("content-length");
        if (str != null) {
            in = conn.in;
//            conn.in.setLimit(Long.parseLong(str));
//        } else if ("chunked".equals("transfer-encoding")) {
//            in = new HttpChunkedInputStream(conn.in);
        }
        return in;
    }

    /**
     * Reply with the specified code and message.
     */
    public void reply(int code, String message) throws IOException
    {
        if (replied) {
            throw new HttpException("invalid request state");
        }
        replied = true;

        // make sure keep-alive is still OK if we weren't using chunked encoding
        if (keep && in == conn.in) {
            keep = conn.in.available() <= 0;
            if (!keep) {
                System.out.println("DISABLED KEEP: input not drained");
            }
        }

        // get the output stream
        conn.getOutputStream();
        conn.out.write( (get("http-version") + " " + code + " " + message + "\r\n").getBytes());
        conn.out.write( ("Server: " + conn.pi.port.server.httpName + "\r\n").getBytes());
    }

    /**
     * Set a reply header.
     */
    public void addHeader(String header, String value) throws IOException
    {
        if (conn.out == null) {
            throw new IOException("Must reply before adding headers");
        }
        conn.out.write( (header + ": " + value + "\r\n").getBytes() );
    }

//    /**
//     * Enables chunked encoding while responding.
//     */
//    public boolean enableChunkedEncoding()
//    {
//        if (httpVersion >= HTTP_VERSION_11) {
//            chunkedEnabled = true;
//        }
//        return chunkedEnabled;
//    }

    /**
     * Get the output stream of unknown length.  If chunked-encoding is enabled
     * then we'll use that, otherwise, we disable keep-alive on this connection.
     */
    public OutputStream getOutputStream() throws IOException
    {
        return getOutputStream(-1);
    }

    /**
     * Get an output stream to handle the reply.  If length < 0 then chunked
     * encoding is used if possible.  If that's not possible then no
     * content-length is specified and this connection cannot be re-used.
     */
    public OutputStream getOutputStream(long length) throws IOException
    {
        if (out != null) {
            return out;
        }

        out = conn.out;
        if (length >= 0) {
            addHeader("Content-length", Long.toString(length));
            if (httpVersion == HTTP_VERSION_10) {
                addHeader("Connection", "Keep-Alive");
            }
            conn.out.write("\r\n".getBytes());
//            conn.out.setLimit(length);
        } else if (httpVersion >= HTTP_VERSION_11) {
//            if (chunkedEnabled) {
//                addHeader("Transfer-Encoding", "chunked");
//                conn.out.print("\r\n");
//                conn.out.enableChunks();
//            } else {
                keep = false;
                addHeader("Connection", "close");
                conn.out.write("\r\n".getBytes());
//            }
        } else {
            keep = false;
            conn.out.write("\r\n".getBytes());
        }
        return out;
    }

    /**
     * Close the request, and maybe keep the connection alive.
     */
    public void close() throws IOException
    {
        if (conn != null) {
            try {
                if (!replied) {
                    reply(500, "Internal Server Error: no handler for request");
                }

                if (out == null) {
                    // They never called getOutputStream, so do it now for them.
                    // Disable chunks to ensure that the connection is closed
                    // (i.e., not kept).
//                    chunkedEnabled = false;
                    getOutputStream(-1);
                }
                out.close();
//                if (!out.chunksEnabled()) {
//                    if (keep) {
//                        // make sure we wrote everything we said we would
//                        keep = conn.out.complete();
//                    }
//                }
                if (!keep) {
                    conn.close();
                }
            } finally {
                conn = null;
            }
        }
    }

    public String toString()
    {
        return "HttpRequest " + uri;
    }
}
