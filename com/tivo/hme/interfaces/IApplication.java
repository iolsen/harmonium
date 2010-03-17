//////////////////////////////////////////////////////////////////////
//
// File: IApplication.java
//
// Copyright (c) 2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.interfaces;

import java.io.InputStream;


/**
 * Hosting interface for an HME Application.  The hosting environment will
 * use this interface to interact with the application during the applications
 * lifecycle.
 * 
 * 
 * @author kgidley
 *
 */
public interface IApplication 
{
    /**
     * The hosting environment will call this method during application 
     * creation, passing the applications context.
     * 
     * @param ctx The context.
     * @throws Exception
     */
    void open(IContext ctx) throws Exception;

    /**
     * The hosting environment will call this method as part of
     * shutting down an application. 
     *
     */
    void close();

    /** 
     * The hosting environment MAY call this method so the application
     * can examine the buffer to determine if enough data is available
     * for the application to process.  The application will return true
     * if there is enough data available to process.  The buffer will be
     * advanced to point to just past the end of the valid chunk of data. 
     * 
     * @param buf The look ahead buffer.
     */
    boolean isAChunk(ILookAheadBuffer buf);

    /**
     * The hosting environment will call this method passing in a stream 
     * that the application can read to receive information from the HME
     * receiver.   
     * 
     */
    boolean handleChunk(InputStream in);
    
}
