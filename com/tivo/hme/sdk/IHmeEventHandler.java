//////////////////////////////////////////////////////////////////////
//
// File: IHmeEventHandler.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk;

/**
 * An event handler.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public interface IHmeEventHandler
{
    /**
     * Handle an event.
     */
    void postEvent(HmeEvent event);
}
