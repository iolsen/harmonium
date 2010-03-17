//////////////////////////////////////////////////////////////////////
//
// File: ChunkedOutputStream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.io;

import java.io.*;

/**
 * A stream that writes out a length marker before each chunk of
 * data. Terminators can be used to divide a stream into distinct messages.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class ChunkedOutputStream extends HmeOutputStream
{
    // the number of bytes in the marker
    final static int MARKER_SIZE = 2;

    protected byte buf[];                       // out buffer
    protected int pos;                          // how far we've written
    protected int posLimit;                     // usually buf.length
    
    /**
     * Creates a chunked output stream with the specified stream and buffer
     * size. Note that the "size" is also the maximum chunk size including the
     * marker bytes.
     */
    public ChunkedOutputStream(OutputStream out, int size)
    {
        super(out); //, size);
        buf = new byte[size];
        pos = MARKER_SIZE;
        posLimit = size-pos;
    }

    /**
     * Creates a chunked output stream with the specified stream and
     * buffer. Note that the buffer length is the maximum chunk size including
     * the marker bytes.
     */
    public ChunkedOutputStream(OutputStream out, byte buf[])
    {
        super(out);
        this.buf = buf;
        pos = MARKER_SIZE;
        posLimit = buf.length-pos;
    }

    /**
     * Write a terminator. A terminator can be used to separate a stream into
     * distinct messages. See ChunkedInputStream.
     */
    public void writeTerminator() throws IOException
    {
        // REMIND : we could save a flush here in some cases (if there is room for the
        // terminator at the end of the buffer)
        flushBuffer();
        chunk(0);
    }

    protected void flushBuffer() throws IOException
    {
        int len = pos - MARKER_SIZE;
        if (len != 0) {
            chunk(len);
        }
    }
    
    protected void chunk(int len) throws IOException
    {
        buf[0] = (byte)(len >>> 8);
        buf[1] = (byte)(len >>> 0);
        out.write(buf, 0, pos);
        
//        total += pos;
        pos = MARKER_SIZE;
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
    
    public static void main(String args[]) throws IOException
    {
        while (true) {
            int clen = (int)(Math.random() * 7.0) + 3;
            
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        	HmeOutputStream hout = new HmeOutputStream(bout);
            ChunkedOutputStream cout = new ChunkedOutputStream(hout, clen);

            int sizes[] = new int[(int)(Math.random() * 3.0) + 1];

            //
            // write chunks
            //
            
            for (int i = 0; i < sizes.length; ++i) {
                sizes[i] = (int)(Math.random() * 10.0) + 3;
                byte in[] = new byte[sizes[i]];
                for (int j = 0; j < in.length; ++j) {
                    in[j] = (byte)(j + 1);
                }
                cout.write(in, 0, in.length);
                cout.writeTerminator();
            }
            cout.close();

            //
            // read chunks
            //

        	HmeInputStream in = new HmeInputStream(new ByteArrayInputStream(bout.toByteArray()));
            ChunkedInputStream cin = new ChunkedInputStream(in); //fin, 1024);

            for (int i = 0; i < sizes.length; ++i) {
                byte out[] = new byte[sizes[i]];
                int count = out.length;
                while (count > 0) {
                    int n = cin.read(out, out.length - count, count);
                    if (n == -1) {
                        System.out.println("EOF");
                        break;
                    }
                    count -= n;
                }
                cin.readTerminator();

                // check
                for (int j = 0; j < out.length; ++j) {
                    if (out[j] != (byte)(j + 1)) {
                        System.out.println("mismatch");
                        return;
                    }
                }
            }

            System.out.println("good");
        }       
    }
}
