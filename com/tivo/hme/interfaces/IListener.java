//////////////////////////////////////////////////////////////////////
//
// File: IListener.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.interfaces; 

/**
 * Interface for removing a factory from the listener.
 */
public interface IListener
{
    public final static String ACCEPTOR_NAME = "Acceptor";

    void remove(IFactory factory);

    ILogger getLogger();
}
