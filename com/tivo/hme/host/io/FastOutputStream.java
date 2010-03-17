//////////////////////////////////////////////////////////////////////
//
// File: FastOutputStream.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.io;

import java.io.*;

/**
 * A buffered output stream with some writer capabilities and the ability to
 * specify a limit.
 * 
 * TODO : throw exception if out == null
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

    /**
     * Write a boolean
     */
//    public void writeBoolean(boolean b) throws IOException
//    {
//        write(b ? 1 : 0);
//    }
    
    /**
     * Write a short, big endian.
     */
//    public void writeShort(int s) throws IOException
//    {
//        write(s >>> 8);
//        write(s >>> 0);
//    }
    
    /**
     * Write an int, big endian.
     */
//    public void writeInt(int i) throws IOException
//    {
//        write(i >>> 24);
//        write(i >>> 16);
//        write(i >>>  8);
//        write(i >>>  0);
//    }

    /**
     * Write an long, big endian.
     */
//    public void writeLong(long l) throws IOException
//    {
//        int i2 = (int) (l >>> 32);
//        int i1 = (int) (1 & 0xFFFFFFFF);
//        writeInt(i2);
//        writeInt(i1);
//    }

    /**
     * Writes a variable length signed integer.
     */
//    public void writeVInt(long value) throws IOException
//    {
//        boolean neg = value < 0;
//        if (neg) {
//            value = -value;
//        }
//        while (value > 0x3F) {
//            write((int) (value & 0x7F));
//            value >>= 7;
//        }
//
//        // write the last byte
//        if (neg) {
//            value |= 0xC0;
//        } else {
//            value |= 0x80;
//        }
//        write((int) value);
//    }

    /**
     * Writes a variable length unsigned integer.
     */
//    public void writeVUInt(long value) throws IOException
//    {
//        if (value < 0) {
//            throw new IllegalArgumentException(value + " < 0");
//        }
//        while (value > 0x7F) {
//            write((int) (value & 0x7F));
//            value >>>= 7;
//        }
//        write((int) (value | 0x80));
//    }

    /**
     * Write a float.
     */
//    public final void writeFloat(float v) throws IOException
//    {
//        writeInt(Float.floatToIntBits(v));
//    }

    /**
     * Write a double.
     */
//    public final void writeDouble(double v) throws IOException
//    {
//        writeLong(Double.doubleToLongBits(v));
//    }
    
    /**
     * Write a UTF encoded string.
     */
//    public final void writeUTF(String str) throws IOException
//    {
//        int strlen = str.length();
//        int utflen = 0;
//        char[] charr = new char[strlen];
//        int c, count = 0;
//
//        str.getChars(0, strlen, charr, 0);
// 
//        for (int i = 0; i < strlen; i++) {
//            c = charr[i];
//            if ((c >= 0x0001) && (c <= 0x007F)) {
//                utflen++;
//            } else if (c > 0x07FF) {
//                utflen += 3;
//            } else {
//                utflen += 2;
//            }
//        }
//
//        if (utflen > 65535) {
//            throw new IOException("string is too long");
//        }
//
//        if ( fUseVString ) {
//            writeVUInt( utflen );
//        }
//        else {
//            writeShort( utflen );
//        }
//        byte[] bytearr = new byte[utflen];
//        for (int i = 0; i < strlen; i++) {
//            c = charr[i];
//            if ((c >= 0x0001) && (c <= 0x007F)) {
//                bytearr[count++] = (byte) c;
//            } else if (c > 0x07FF) {
//                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
//                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
//                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
//            } else {
//                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
//                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
//            }
//        }
//        write(bytearr);
//    }

    /**
     * Print a string - strips the high byte for each character.
     */
//    public void print(String str) throws IOException
//    {
//        for (int i = 0, strlen = str.length() ; i < strlen ; i++) {
//            write(str.charAt(i));
//        }
//    }

    /**
     * Print a string and a newline - strips the high byte for each character.
     */
//    public void println(String str) throws IOException
//    {
//        print(str);
//        write('\n');
//    }

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

    /**
     * Set whether or not to use vInts as size of vStrings
     */
//    public void setUseVString( boolean flag ) 
//    {
//        fUseVString = flag;
//    }
}
