//////////////////////////////////////////////////////////////////////
//
// File: ChunkedInputStream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.io;

import java.io.*;

/**
 * ChunkedInputStream reads streams that were divided into chunks using
 * ChunkedOutputStream. Terminator are read using readTerminator().
 * 
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class ChunkedInputStream extends HmeInputStream
{
    boolean terminator;         // true if the stream is at a terminator
    int clen;                   // current chunk length

    /**
     * Create a new chunked input stream with the specified stream and buffer size.
     */
    public ChunkedInputStream(InputStream in)
    {
        super(in);
    }

    /**
     * Read a single byte. Returns -1 on EOF.
     */
    public int read() throws IOException
    {
        if (!startChunk()) {
            return -1;
        }

        int ch = super.read();
        if (ch >= 0) {
            --clen;
        }
        return ch;
    }

    /**
     * Read at most len characters into a byte array.  Returns -1 upon EOF,
     * or if the input limit is reached.
     */
    public int read(byte data[], int off, int len) throws IOException
    {
        if (!startChunk()) {
            return -1;
        }

        // read the data
        int n = super.read(data, off, Math.min(len, clen));
        if (n > 0) {
            clen -= n;
        }
        return n;
    }

    /**
     * Read past the next terminator. This can be called at any time.
     */
    public void readTerminator() throws IOException
    {
        while (startChunk()) {
            skip(clen);
        }
        terminator = false;
    }

    /**
     * Skip some bytes. Throws an EOF if the end of the file is reached.
     */
    public void skip(int len) throws IOException
    {
        while (len > 0) {
            if (!startChunk()) {
                throw new EOFException("can't skip terminator");
            }

            int n = Math.min(len, clen);
            super.skip(n);
            len -= n;
            clen -= n;
        }
    }

    /**
     * Read the chunk size if necessary. Returns true if there is data available.
     */
    private boolean startChunk() throws IOException
    {
        if (clen == 0 && !terminator) {
            int ch1 = super.read();
            int ch2 = super.read();
            if ((ch1 | ch2) < 0) {
                throw new EOFException("eof while reading chunk length");
            }
            clen = ((ch1 & 0xff) << 8) + (ch2 & 0xff);
            
            if (clen == 0) {
                terminator = true;
            }
        }
        return !terminator;
    }
}
