//////////////////////////////////////////////////////////////////////
//
// File: ILogger.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.interfaces;

/**
 * Interface for logging.
 */
public interface ILogger
{
    //
    // logging priorities 
    //
    int LOG_INFO    = 0;
    int LOG_DEBUG   = 1;
    int LOG_NOTICE  = 2;
    int LOG_WARNING = 3;

    void log(int priority, String s);
}
