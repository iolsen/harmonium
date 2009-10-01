//////////////////////////////////////////////////////////////////////
//
// File: HmeException.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

/**
 * HME exception.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("serial")
public class HmeException extends RuntimeException
{
    public HmeException(Throwable t)
    {
        super(t.getMessage());
    }

    public HmeException(String msg)
    {
        super(msg);
    }
}
