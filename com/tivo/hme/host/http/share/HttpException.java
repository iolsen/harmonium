//////////////////////////////////////////////////////////////////////
//
// File: HttpException.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.share;

import java.io.*;

/**
 * An http related exception.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("serial")
public class HttpException extends IOException
{
    public HttpException(String msg)
    {
        super(msg);
    }
}
