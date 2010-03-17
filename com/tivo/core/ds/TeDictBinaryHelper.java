//////////////////////////////////////////////////////////////////////
// 
// File: TeDictBinaryHelper.java
// 
// Description: 
// 
// Copyright (C) 2005 by TiVo Inc.
// 
//////////////////////////////////////////////////////////////////////

// TODO: remove '.trio'
package com.tivo.core.ds;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * A static helper class for reading from InputStreams and writing to
 * OutputStreams 
 */
public class TeDictBinaryHelper 
{
    /**
     * Reads one byte just like read(), if EOF is reached this throws EOFException.
     */
    static public byte readByte(InputStream in) throws IOException
    {
        int ch = in.read();
        if (ch == -1) {
            throw new EOFException();
        }
        return (byte)ch;
    }

    /**
     * Read a boolean
     */
    static public boolean readBoolean(InputStream in) throws IOException
    {
        return ( readByte(in) != 0 );
    }

    /**
     * Reads a variable length signed integer.
     */
    static public long readVInt(InputStream in) throws IOException
    {
        long value = 0;
        int c;
        int shift = 0;
        while (((c = in.read()) & 0x80) == 0) {
            value += (c << shift);
            shift += 7;
            if (shift > 70) {
                throw new IOException("vint is too long");
            }
        }
        if (c == -1) {
            throw new EOFException();
        }
        value += ((c & 0x3F) << shift);
        if ((c & 0x40) != 0) {
            value = -value;
        }
        return value;
    }

    /**
     * Reads a variable length unsigned integer.
     */
    static public long readVUInt(InputStream in) throws IOException
    {
        long value = 0;
        int c;
        int shift = 0;
        while (((c = in.read()) & 0x80) == 0) {
            value += (c << shift);
            shift += 7;
            if (shift > 70) {
                throw new IOException("vuint is too long");
            }
        }
        if (c == -1) {
            throw new EOFException();
        }
        return value + ((c & 0x7F) << shift);
    }


    /**
     * Read s UTF-8 encoded string.
     */
    static public String readUTF8String(InputStream in) throws IOException
    {
        int len = (int)readVUInt(in);
        if ( len == 0 ) return new String();
        byte vBytes[] = new byte[len];
        int readlen = in.read(vBytes, 0, len);
        if ( readlen != len ) {
            throw new EOFException();
        }
        return new String( vBytes, 0, len, "UTF-8" );
    }

    
    /**
     * Write a boolean
     */
    static public void writeBoolean(OutputStream out, boolean b) throws IOException
    {
        out.write(b ? 1 : 0);
    }
    
    /**
     * Write a byte
     */
    static public void writeByte(OutputStream out, byte b) throws IOException
    {
        out.write( b );
    }
    
    /**
     * Writes a variable length signed integer.
     */
    static public void writeVInt(OutputStream out, long value) throws IOException
    {
        boolean neg = value < 0;
        if (neg) {
            value = -value;
        }
        while (value > 0x3F) {
            out.write((int)(value & 0x7F));
            value >>= 7;
        }

        // write the last byte
        if (neg) {
            value |= 0xC0;
        } else {
            value |= 0x80;
        }
        out.write((int) value);
    }

    /**
     * Writes a variable length unsigned integer.
     */
    static public void writeVUInt(OutputStream out, long value) throws IOException
    {
        if (value < 0) {
            throw new IllegalArgumentException(value + " < 0");
        }
        while (value > 0x7F) {
            out.write((int) (value & 0x7F));
            value >>>= 7;
        }
        out.write((int) (value | 0x80));
    }

    /**
     * Write a UTF-8 encoded string.
     */
    
    static public void writeUTF8String(OutputStream out, String str) throws IOException
    {
//        byte [] vBytes;
//        int nBytes = 0;
//        try {
//            Charset utf8 = Charset.forName("UTF-8");
//            ByteBuffer bb = utf8.encode(str);
//            vBytes = bb.array();
//            nBytes = vBytes.length;
//            // This fixes an odd behavior in Charset encoder:  it puts spurious
//            // '\0' characters at the end of the string!  This strips them off:
//            while ( nBytes > 0 && vBytes[nBytes-1] == '\0' ) {
//                nBytes--;
//            }
//        }
//        catch ( IllegalArgumentException e )
//        {
//            throw new IOException("Internal error: can't encode UTF-8");
//        }
//        
//        // Write byte length as a vuint
//        writeVUInt( out, nBytes );
//            
//        if ( nBytes > 0 ) {
//            // ...and write the bytes
//            out.write( vBytes, 0, nBytes );
//        }
        
         int strlen = str.length();
         int utflen = 0;
         char[] charr = new char[strlen];
         int c, count = 0;

         str.getChars(0, strlen, charr, 0);
 
         for (int i = 0; i < strlen; i++) {
             c = charr[i];
             if ((c >= 0x0001) && (c <= 0x007F)) {
                 utflen++;
             } else if (c > 0x07FF) {
                 utflen += 3;
             } else {
                 utflen += 2;
             }
         }

         // Length as vuint
         writeVUInt( out, utflen );

         byte[] bytearr = new byte[utflen];
         for (int i = 0; i < strlen; i++) {
             c = charr[i];
             if ((c >= 0x0001) && (c <= 0x007F)) {
                 bytearr[count++] = (byte) c;
             } else if (c > 0x07FF) {
                 bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                 bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                 bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
             } else {
                 bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                 bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
             }
         }
         out.write(bytearr);
    }

};
