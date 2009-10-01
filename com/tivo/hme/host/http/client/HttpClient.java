//////////////////////////////////////////////////////////////////////
//
// File: HttpClient.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Iterator;

import com.tivo.hme.host.http.share.Headers;
import com.tivo.hme.host.http.share.HttpChunkedInputStream;
import com.tivo.hme.host.http.share.HttpException;
import com.tivo.hme.host.http.share.IHttpConstants;
import com.tivo.hme.host.io.FastInputStream;
import com.tivo.hme.host.io.FastOutputStream;

// TODO : keep-alive
// TODO : proxy support

/**
 * An HttpClient for making requests to a server. Usage generally follows this
 * pattern:
 * 
 * <pre>
 *  1. connect()
 *  2.   Use addHeader() to add additional headers. (optional)
 *  3. getOutputStream() to upload data
 *  4. getInputStream() to get the payload from the server.
 *  5.   Use getStatus() to check the HTTP status code. (optional)
 *  6.   Use get() to check headers from the server. (optional)
 *  7. close()
 * </pre>
 * 
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class HttpClient implements IHttpConstants
{
    String method;
    URL url;
    String userAgent;
    int httpVersion;
    
    Socket socket;
    
    InputStream in;
//    FastOutputStream out;
    OutputStream out;

    boolean requestComplete;

    int status;
    Headers headers;

    static char str[];

    // for debugging
    boolean chunksDisabled = false;
    
    public HttpClient(URL url)
    {
        this("GET", url);
    }

    public HttpClient(String method, URL url)
    {
        this(method, url, HTTP_VERSION_11);
    }
    
    public HttpClient(String method, URL url, int httpVersion)
    {
        this.method = method;
        this.url = url;
        this.httpVersion = httpVersion;

        userAgent = "tivo.http/03/30/2003";
        requestComplete = false;
    }

    //
    // accessors for the request header
    //

    /**
     * Get the request user agent.
     */
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * Set the request user agent.
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * Get the request HTTP version.
     */
    public int getVersion()
    {
        return httpVersion;
    }

    /**
     * Return the request HTTP method.
     */
    public String getMethod()
    {
        return method;
    }

    /**
     * Return the request URL.
     */
    public URL getURL()
    {
        return url;
    }

    //
    // the mechanics of the HTTP request
    //
    
    /**
     * Establish a connection to the host.
     *
     * <p><code>connect()</code> writes the HTTP request line and a few
     * predefined MIME headers.
     */
    public void connect() throws IOException
    {
        if (socket != null) {
            throw new HttpException("invalid HttpClient state (already connected)");
        }

        int port = url.getPort();
        if (port == -1) {
            port = 80;
        }
        socket = new Socket(url.getHost(), port);
        out = new FastOutputStream(socket.getOutputStream(), TCP_BUFFER_SIZE);
        
        // GET /theurl HTTP/1.1
        String file = url.getFile();
        if (file.equals("")) {
            file = "/";
        }

        String version = "HTTP/" + ((httpVersion == HTTP_VERSION_10) ? "1.0" : "1.1");
        out.write( (method + ' ' + file + ' ' + version + CRLF).getBytes());
        
        addHeader("Host", url.getHost());
        addHeader("User-Agent", userAgent);
        if (httpVersion >= HTTP_VERSION_11) {
            // we don't support keep-alive and/or content-lengths yet
            addHeader("Connection", "close");
        }
    }

    /**
     * Write a mime header as part of the request. The header gets written
     * immediately. addHeader() should be called immediately after connect().
     */
    public void addHeader(String key, String value) throws IOException
    {
        if (out == null) {
            connect();
        }
        out.write( (key + ": " + value + CRLF).getBytes());
    }

    /**
     * Get the http socket.
     */
    public Socket getSocket()
    {
        return socket;
    }

    /**
     * Get an output stream of unknown length. The caller should close the
     * output stream when the request is complete.
     */
    public OutputStream getOutputStream() throws IOException
    {
        return getOutputStream(-1);
    }

    /**
     * Get an output stream to upload a payload.
     */
    public OutputStream getOutputStream(int len) throws IOException
    {
        if (out == null) {
            // connect if necessary
            connect();
        }

        if (!requestComplete) {
            // finish the request if necessary
            if (len >= 0) {
//                out.setLimit(len);
                addHeader("Content-Length", Integer.toString(len));
            }
            out.write(CRLF.getBytes());
            out.flush();
            requestComplete = true;
        }
        return out;
    }
    
    /**
     * Get an input stream to suck down the payload from the server. This parses
     * the HTTP response.
     */
    public InputStream getInputStream() throws IOException
    {
        // you can call this more than once
        if (in != null) {
            return in;
        }
        
        if (!requestComplete) {
            // finish the request if necessary
            getOutputStream();
        }

        // now read the response
        in = new FastInputStream(socket.getInputStream(), TCP_BUFFER_SIZE);
        do {
            // parse HTTP/1.1 200 OK
            String ln = readLine(in);
            if (ln == null) {
                throw new HttpException("unexpected EOF in HTTP response header");
            }
            int i = ln.indexOf(' ');
            if (i < 0) {
                throw new HttpException("malformed HTTP response: " + ln);
            }
            int j = ln.indexOf(' ', i+1);
            if (j < 0) {
                throw new HttpException("malformed HTTP response: " + ln);
            }
            
            String version = ln.substring(0, i);
            
            try {
                status = Integer.parseInt(ln.substring(i+1, j));
            } catch (NumberFormatException e) {
                throw new HttpException("malformed HTTP status code '" + ln + "'");
            }
            
            String statusText = ln.substring(j + 1);
            
            // read the mime header
            if (headers == null) {
                headers = new Headers(in);
            } else {
                headers.parse(in);
            }
            headers.addInternal("http-version", version);
            headers.addInternal("http-status", Integer.toString(status));
            headers.addInternal("http-statustext", statusText);
        } while (status == HTTP_STATUS_CONTINUE);

        // support for chunked encoding
        if (httpVersion >= HTTP_VERSION_11 && isChunkedResponse()) {
            in = new HttpChunkedInputStream(in);
        }
        
        return in;
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
    
    boolean isChunkedResponse() throws IOException
    {
//        System.out.println("*** isChunkedResponse = " + (!chunksDisabled && "chunked".equals(get("transfer-encoding"))));
//        System.out.println("*** transfer-encoding = " + get("transfer-encoding"));
        return !chunksDisabled && "chunked".equals(get("transfer-encoding"));
    }

    //
    // accessors for the response header
    //

    /**
     * Get the status code from the response. This should be called after
     * getInputStream().
     */
    public int getStatus() throws IOException
    {
        if (headers == null) {
            getInputStream();
        }
        return status;
    }

    /**
     * Get a specific mime header from the response. This should be called after
     * getInputStream().
     */
    public String get(String key) throws IOException
    {
        return getHeaders().get(key);
    }

    /**
     * Accessor for the response headers. The iterator will return the values in
     * the order they were received.  This should be called after
     * getInputStream().
     */
    public Headers getHeaders() throws IOException
    {
        if (headers == null) {
            getInputStream();
        }
        return headers;
    }

    /**
     * Drain the response and throw away the bytes.
     */
    public void drain() throws IOException
    {
        InputStream in = getInputStream();
        try {
            byte buf[] = new byte[1024];
            while (in.read(buf, 0, buf.length) > 0) {
                ;
            }
        } finally {
            in.close();
        }
    }

    /**
     * Close the request.
     */
    public void close()
    {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) { }            

            if (in == null) {
                try {
                    drain();
                } catch (IOException e) { }
            }
            if (out == null) {
                try {
                    getOutputStream(-1).close();
                } catch (IOException e) { }            
            }
            socket = null;
        }
    }

    /**
     * Disable chunks, for debugging.
     */
    public void setChunksDisabled(boolean chunksDisabled)
    {
        this.chunksDisabled = chunksDisabled;
    }

    public static void main(String args[]) throws IOException
    {
        boolean chunksDisabled = false;

        if (args.length == 0) {
            System.out.println("Usage: HttpClient [URL]");
            System.exit(1);
        } else if (args.length > 1) {
            if (args[1].equals("-debug")) {
                chunksDisabled = true;
            }
        }
        
        HttpClient client = new HttpClient(new URL(args[0]));
        client.setChunksDisabled(chunksDisabled);
        client.connect();
        client.addHeader("Connection", "close");
        client.getOutputStream();
        InputStream in = client.getInputStream();

        System.out.println(client.getMethod() + " " + client.getURL());
        
        Iterator i = client.getHeaders().getKeys();
        while (i.hasNext()) {
            String key = (String)i.next();
            String value = client.get(key);
            System.out.println(key + ": " + value);
        }
        System.out.println();

        byte buf[] = new byte[1024];
        do {
            int n = in.read(buf, 0, buf.length);
            if (n <= 0) {
                break;
            }
            System.out.write(buf, 0, n);
        } while (true);

        client.close();
    }
}
