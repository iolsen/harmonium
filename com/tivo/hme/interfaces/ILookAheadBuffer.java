//////////////////////////////////////////////////////////////////////
//
// File: ILookAheadBuffer.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.interfaces;

/**
 * Interface that defines a buffer that can be used by the application
 * to determine if enough data is available to process without blocking.
 * 
 * @author kgidley
 */
public interface ILookAheadBuffer {

    int bytesAvailable();
    void peekBytes(int size, byte[] bytes);
    void skipBytes(int size);

}
