//////////////////////////////////////////////////////////////////////
// 
// File: TeIterator.java
// Description: TeIterator adaptor to make Enumerations look like Iterators.
// 
// Copyright (c) 2004 TiVo Inc.
// 
//////////////////////////////////////////////////////////////////////

package com.tivo.core.ds;

import java.util.Enumeration;

@SuppressWarnings("unchecked")
public class TeIterator
{
    Enumeration e;
    public TeIterator(Enumeration e)
    {
        this.e = e;
    }

    public boolean hasNext()
    {
        return e.hasMoreElements();
    }

    public Object next()
    {
        return e.nextElement();
    }
}
