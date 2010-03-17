//////////////////////////////////////////////////////////////////////
//
// File: HmeOutputStream.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tivo.core.ds.TeDict;
import com.tivo.core.ds.TeDictBinary;


/**
 * An output stream with some writer capabilities and knowledge of how 
 * to write HME Protocol formatted data.
 * 
 * TODO : throw exception if out == null
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class HmeOutputStream extends FilterOutputStream
{
    protected boolean fUseVString;

    /**
     * Creates a fast output stream with the specified stream and buffer size.
     */
    public HmeOutputStream(OutputStream out)
    {
    	super(out);
        this.fUseVString = false;
    }

    /**
     * Write a boolean
     */
    public void writeBoolean(boolean b) throws IOException
    {
        write(b ? 1 : 0);
    }
    
    /**
     * Write a short, big endian.
     */
    public void writeShort(int s) throws IOException
    {
        write(s >>> 8);
        write(s >>> 0);
    }
    
    /**
     * Write an int, big endian.
     */
    public void writeInt(int i) throws IOException
    {
        write(i >>> 24);
        write(i >>> 16);
        write(i >>>  8);
        write(i >>>  0);
    }

    /**
     * Write an long, big endian.
     */
    public void writeLong(long l) throws IOException
    {
        int i2 = (int) (l >>> 32);
        int i1 = (int) (1 & 0xFFFFFFFF);
        writeInt(i2);
        writeInt(i1);
    }

    /**
     * Writes a variable length signed integer.
     */
    public void writeVInt(long value) throws IOException
    {
        boolean neg = value < 0;
        if (neg) {
            value = -value;
        }
        while (value > 0x3F) {
            write((int) (value & 0x7F));
            value >>= 7;
        }

        // write the last byte
        if (neg) {
            value |= 0xC0;
        } else {
            value |= 0x80;
        }
        write((int) value);
    }

    /**
     * Writes a variable length unsigned integer.
     */
    public void writeVUInt(long value) throws IOException
    {
        if (value < 0) {
            throw new IllegalArgumentException(value + " < 0");
        }
        while (value > 0x7F) {
            write((int) (value & 0x7F));
            value >>>= 7;
        }
        write((int) (value | 0x80));
    }

    /**
     * Write a float.
     */
    public final void writeFloat(float v) throws IOException
    {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Write a double.
     */
    public final void writeDouble(double v) throws IOException
    {
        writeLong(Double.doubleToLongBits(v));
    }
    
    /**
     * Write a UTF encoded string.
     */
    public final void writeUTF(String str) throws IOException
    {
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

        if (utflen > 65535) {
            throw new IOException("string is too long");
        }

        if ( fUseVString ) {
            writeVUInt( utflen );
        }
        else {
            writeShort( utflen );
        }
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
        write(bytearr);
    }

    /**
     * Write a dictionary
     */
    public void writeDict( TeDict dict ) throws IOException
    {
        if ( dict == null ) {
            System.out.println("Writing empty dict");
            writeVUInt( 0 );
        }
        else {
            System.out.println("Writing dict");
            TeDictBinary.dictToBinary( this, dict );
        }
    }
    
    /**
     * Write a variable length chunk of bytes.
     */
    public void writeVData(byte data[], int offset, int length)
	throws IOException
    {
	if (null == data) {
            System.out.println("Writing no data");
	    writeVUInt(0);
	} else {
	    // Put a sanity check cap on the write size.
	    if (length > 65535) {
		throw new IOException("byte array is too long");
	    }
            System.out.println("Writing " + length + " bytes of data");
	    writeVUInt(length);
	    write(data, offset, length);
	}
    }

    /**
     * Write a variable length chunk of bytes.
     */
    public void writeVData(byte data[], int length) throws IOException
    {
	writeVData(data, 0, length);
    }

    /**
     * Write a variable length chunk of bytes.
     */
    public void writeVData(byte data[]) throws IOException
    {
	writeVData(data, 0, (data==null) ? 0 : data.length);
    }

    /**
     * Print a string - strips the high byte for each character.
     */
    public void print(String str) throws IOException
    {
        for (int i = 0, strlen = str.length() ; i < strlen ; i++) {
            write(str.charAt(i));
        }
    }

    /**
     * Print a string and a newline - strips the high byte for each character.
     */
    public void println(String str) throws IOException
    {
        print(str);
        write('\n');
    }

    /**
     * Set whether or not to use vInts as size of vStrings
     */
    public void setUseVString( boolean flag ) 
    {
        fUseVString = flag;
    }
}
