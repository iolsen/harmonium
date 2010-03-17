//////////////////////////////////////////////////////////////////////
//
// File: IHttpRequest.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class that represents an http request on an http server.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public interface IHttpRequest //extends IHttpConstants
{
    /**
     * Returns a mime header from the request.
     */
    String get(String header);

    /**
     * Returns the request method type.
     */
    String getMethod();

    /**
     * Returns the request URI.
     */
    String getURI();

    /**
     * Returns an input stream, if the method is POST or PUT.
     * Otherwise returns null.
     */
    InputStream getInputStream() throws IOException;


    /**
     * Indicates whether the request has been replied to yet.
     */
    public boolean getReplied();

    /**
     * Replies to the HTTP request with the specified return code and message.
     */
    void reply(int code, String message) throws IOException;

    /**
     * Adds a mime header to the reply message.
     */
    void addHeader(String header, String value) throws IOException;

    /**
     * Specifies that the response may use chunked encoding, if the underlying
     * protocol supports it.  Returns true if the underlying protocol supports
     * it.
     */
    //boolean enableChunkedEncoding();

    /**
     * Gets the output stream for replying to the request.  Content length is
     * unknown, which means the code will try to use chunked encoding.  If
     * that's the case, the connection may be kept alive.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Gets the output stream with the specified content length.  Chunked
     * encoding will not be used.
     */
    OutputStream getOutputStream(long length) throws IOException;

    /**
     * Closes the request.  Keep-alive will be used if possible.  If data is not
     * fully read and/or written, the socket is not re-used.
     */
    void close() throws IOException;
}
