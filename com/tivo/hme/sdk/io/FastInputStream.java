//////////////////////////////////////////////////////////////////////
//
// File: FastInputStream.java
//
//  NOTE: this file has been copied to: com.tivo.hme.host.io.FastInputStream
//  If you make changes to this file, you may need to update that file as well.
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.io;

import java.io.*;

/**
 * A buffered input stream with some reader capabilities coming
 * and the ability to specify a limit.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 * @author      Richard Lee
 */
public class FastInputStream extends FilterInputStream
{
    int pos;
    int count;
    byte buf[];
    char str[];
    long limit;
    long total;
    boolean marked;

    /**
     * A buffered stream of the specified buffer size.
     */
    public FastInputStream(InputStream in, int bufsize)
    {
        super(in);
        if (in == null) {
            throw new NullPointerException("Null stream");
        }
        this.buf = new byte[bufsize];
        this.limit = -1;
    }

    /**
     * A buffered stream in a byte array.
     */
    public FastInputStream(byte buf[], int offset, int length)
    {
        super(null);
        this.buf = buf;
        pos = offset;
        count = pos + length;
    }

    /**
     * Return the number of bytes read.
     */
    public long getCount()
    {
        return total - count + pos;
    }

    /**
     * Limit the stream to the specified limit.  If limit < 0, then
     * there is no limit.
     */
    public void setLimit(long limit) throws IOException
    {
        this.total = count - pos;
        this.limit = limit;
        if (limit >= 0) {
            if (total > limit) {
                throw new IOException("limit already exceeded: " + total + " > " + limit);
            }
        }
    }

    /**
     * Returns true if this stream was not limited and there is no data left in the
     * buffer, OR, if the stream was limited and all the data was read.
     */
    public boolean isDrained() throws IOException
    {
        if (limit == -1) {
            return (pos == count);
        } else {
            return limit == total;
        }
    }

    /**
     * Get data to fill the buffer.  This can be overridden.
     */
    protected int fillBuffer(byte buf[], int offset, int space) throws IOException
    {
        return in.read(buf, offset, space);
    }

    /**
     * Attmpts to fill the buffer with more data.  Returns false if it fails to
     * get any data at all.
     */
    public boolean fill() throws IOException
    {
        // check if this is a byte array input stream
        if (in == null) {
            return false;
        }

        int left = count - pos;
        if (left == buf.length) {
            return false;
        }

        if (marked && count == buf.length) {
            marked = false;
        }
        
        // move existing data (if any) to the front of the buffer
        if (!marked) {
            if (left > 0) {
                System.arraycopy(buf, pos, buf, 0, left);
            }
            count = left;
            pos = 0;
        }

        // read as much as we can into the buffer
        int space = buf.length - count;
        if ((limit >= 0) && (space > (limit - total))) {
            space = (int)(limit - total);
        }
        int n = fillBuffer(buf, count, space);
        if (n > 0) {
            count += n;
            total += n;
            return true;
        }

        return false;
    }

    /**
     * Read one character from the stream.  Returns -1 on EOF.
     */
    public int read() throws IOException
    {
        return ((pos < count) || fill()) ? (buf[pos++] & 0xFF) : -1;
    }

    /**
     * Read at most len characters into a byte array.  Returns -1 upon EOF,
     * or if the input limit is reached.
     */
    public int read(byte data[], int off, int len) throws IOException
    {
        if ((pos == count) && !fill()) {
            return -1;
        }
        if (len > (count - pos)) {
            len = count - pos;
        }
        System.arraycopy(buf, pos, data, off, len);
        pos += len;
        return len;
    }

    /**
     * Skip some bytes. Throws an EOF if the end of the file is reached.  Private implementation.
     */
    private void skip(int len) throws IOException
    {
        while (len > 0) {
            if ((pos == count) && !fill()) {
                throw new EOFException();
            }

            int n = len;
            if (n > (count - pos)) {
                n = count - pos;
            }
            pos += n;
            len -= n;
        }
    }

    /**
     * Skip some bytes. Throws an EOF if the end of the file is reached.  Overrides skip from InputStream
     */
    public long skip(long len) throws IOException
	{
    	skip((int)len);
    	return len;
	}

    public int available() throws IOException {
        int total = count - pos;
        if (in != null) {
            total += in.available();
        }
        return total;
    }

    /**
     * Mark the stream. Call reset() to return to this spot.
     */
    public synchronized void mark(int readlimit)
    {
        if (readlimit > buf.length) {
            throw new IllegalArgumentException("readlimit is too long");
        }

        // move existing data (if any) to the front of the buffer       
        int left = count - pos; 
        if (left > 0) {
            System.arraycopy(buf, pos, buf, 0, left);
        }
        count = left;
        pos = 0;
        marked = true;
    }

    /**
     * Reset back to the mark.
     */
    public synchronized void reset() throws IOException
    {
        if (!marked) {
            throw new IOException("mark invalidated - cannont reset");
        }
        pos = 0;
        marked = false;
    }

    /**
     * Returns true if marking is supported.
     */
    public boolean markSupported()
    {
        return true;
    }
    
    public void close() throws IOException
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
            in = null;
        }
        if (limit > 0 && total < limit) {
            throw new IOException("limit not reached, " + (limit - total) + " bytes remain");
        }
    }
}
