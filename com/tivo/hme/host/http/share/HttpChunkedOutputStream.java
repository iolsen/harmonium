//////////////////////////////////////////////////////////////////////
//
// File: HttpChunkedOutputStream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.share;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A chunked output stream.  This is a buffered stream, which can
 * switch into HTTP 1.1 chunked encoding mode.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class HttpChunkedOutputStream extends BufferedOutputStream
{
    static final int CHUNK_TRAILER_SIZE = 2;  // "\r\n" after data

    boolean closed;                           // remember if we're closed
    boolean chunksEnabled;                    // true if we're doing chunked encoding
    int chunkHeaderSize;                      // header size based on our buffer size
    int pos;
    int posLimit;

    public HttpChunkedOutputStream(OutputStream out, int size)
    {
        super(out, size);
    }

    public void reset() throws IOException
    {
        closed = false;
        chunksEnabled = false;
    }

    public void enableChunks() throws IOException
    {
        if (!chunksEnabled) {
            flush();
            chunksEnabled = true;
            chunkHeaderSize = Integer.toHexString(buf.length).length() + 2;
            pos = chunkHeaderSize;
            posLimit = buf.length - CHUNK_TRAILER_SIZE;
        }
    }

    public void disableChunks() throws IOException
    {
        if (chunksEnabled) {
            flush();
            chunksEnabled = false;
            pos = 0;
            posLimit = buf.length;
        }
    }

    public boolean chunksEnabled()
    {
        return chunksEnabled;
    }

    public void flush() throws IOException
    {
        flushBuffer();
        if (out != null) {
            out.flush();
        }
    }
    
    /**
     * Flush the buffer in HTTP chunked encoding format.
     */
    public void flushBuffer() throws IOException
    {
        if (!chunksEnabled) {
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
                pos = 0;
            }
            return;
        }

        // check to see if there's something to flush
        int size = pos - chunkHeaderSize;
        if (size == 0) {
            return;
        }

        // encode the chunk size
        String hex = Long.toHexString(size);
        int off = chunkHeaderSize;
        buf[--off] = '\n';
        buf[--off] = '\r';
        for (int i = hex.length(); --i >= 0; ) {
            buf[--off] = (byte) hex.charAt(i);
        }

        buf[pos] = '\r';
        buf[pos + 1] = '\n';

        // now write the data
        out.write(buf, off, (pos + 2) - off);
        pos = chunkHeaderSize;
    }

    public void close() throws IOException
    {
        if (!closed) {
            if (chunksEnabled) {
                finish();
            } else {
                flush();
            }
            closed = true;
        }
    }

    /**
     * Finished this stream, but flushing the current chunk, and
     * then writing a 0 chunk.
     */
    public void finish() throws IOException
    {
        // flush the last chunk
        flush();

        // write the EOF chunk
        buf[0] = '0';
        buf[1] = '\r';
        buf[2] = '\n';

        // write the chunked footer
        buf[3] = '\r';
        buf[4] = '\n';

        // write the data to the subordinate stream
        out.write(buf, 0, 5);
    }

    public void write(int c) throws IOException
    {
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
}
