//////////////////////////////////////////////////////////////////////
//
// File: FastOutputStream.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.io;

import java.io.*;

/**
 * A buffered output stream with some writer capabilities and the ability to
 * specify a limit.
 * 
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class FastOutputStream extends OutputStream
{
    protected OutputStream out;         // output stream
    protected byte buf[];                       // out buffer
    protected int pos;                          // how far we've written
    protected int posLimit;                     // usually buf.length
    protected long limit;                       // the limit
    protected boolean limited;                  // true if this stream is limited
    protected long total;                       // number of bytes written

    /**
     * Creates a fast output stream with the specified stream and buffer size.
     */
    public FastOutputStream(OutputStream out, int size)
    {
        this.out = out;
        setBuffer(new byte[size]);
    }

    /**
     * Creates a fast output stream with the specified stream and buffer.
     */
    public FastOutputStream(OutputStream out, byte buf[])
    {
        this.out = out;
        setBuffer(buf);
    }

    /**
     * Creates a memory buffer with the specified initial size.
     */
    public FastOutputStream(int size)
    {
        setBuffer(new byte[size]);
    }

    /**
     * Creates a memory buffer with the specified buffer.
     */
    public FastOutputStream(byte buf[])
    {
        setBuffer(buf);
    }

    private void setBuffer(byte buf[]) {
        this.buf = buf;
        posLimit = buf.length;
    }

    public void setLimit(long limit) throws IOException
    {
        this.limit = limit;
        limited = (limit >= 0);
    }

    public byte[] getBuffer()
    {
        return buf;
    }
    
    public byte[] toByteArray()
    {
        byte b[] = new byte[pos];
        System.arraycopy(buf, 0, b, 0, pos);
        return b;
    }

    public long getCount()
    {
        return total + pos;
    }

    public void write(int c) throws IOException
    {
        if (limited) {
            if (limit <= 0) {
                throw new IOException("output limit exceeded");
            }
            limit -= 1;
        }
        if (pos == posLimit) {
            flush();
        }
        buf[pos++] = (byte) c;
    }

    public void write(byte data[]) throws IOException
    {
        write(data, 0, data.length);
    }

    public void write(byte data[], int off, int length) throws IOException
    {
        // deal with limit, if any
        if (limited) {
            if (length > limit) {
                throw new IOException("output limit exceeded");
            }
            limit -= length;
        }

        // write the data
        while (length > 0) {
            int room = posLimit - pos;
            if (room == 0) {
                flush();
                room = posLimit - pos;          
            }
            int n = (room > length) ? length : room;
            System.arraycopy(data, off, buf, pos, n);
            pos += n;
            off += n;
            length -= n;
        }
    }

    protected void flushBuffer() throws IOException {
        if (pos > 0) {
            if (out == null) {
                if (pos == posLimit) {
                    // grow the buffer
                    int len = buf.length;
                    System.arraycopy(buf, 0, buf = new byte[len * 2], 0, len);
                    posLimit = buf.length;
                }
                return;
            }
            out.write(buf, 0, pos);
            total += pos;
            pos = 0;
        }
    }

    public int getUnflushedCount()
    {
        return pos;
    }

    public void flush() throws IOException
    {
        flushBuffer();
        if (out != null) {
            out.flush();
        }
    }

    public void close() throws IOException
    {
        flush();
        if (out != null) {
            out.close();
        }
    }

    /**
     * Returns true if the setLimit was honored, that is, all the data we said
     * we'd write was actually written.
     */
    public boolean complete() throws IOException
    {
        return !limited || (limit == 0);
    }
}
