//////////////////////////////////////////////////////////////////////
//
// File: HmeInputStream.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.io;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

import com.tivo.core.ds.TeDict;
import com.tivo.core.ds.TeDictBinary;


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
public class HmeInputStream extends FilterInputStream
{
    char str[];
    boolean fUseVString;

    /**
     * A buffered stream of the specified buffer size.
     */
    public HmeInputStream(InputStream in)
    {
        super(in);
        if (in == null) {
            throw new NullPointerException("Null stream");
        }
        this.fUseVString = false;
    }

    /**
     * Reads one byte just like read(), if EOF is reached this throws
     * EOFException.
     */
    public int readOne() throws IOException
    {
        int ch = read();
        if (ch == -1) {
            throw new EOFException();
        }
        return ch;
    }

    /**
     * Read exactly len bytes into an array. Throws an EOF if the end
     * of the file is reached.
     */
    public void readFully(byte buf[], int off, int len) throws IOException
    {
        while (len > 0) {
            int n = read(buf, off, len);
            if (n < 0) {
                throw new EOFException("could not read " + len + " bytes");
            }
            off += n;
            len -= n;
        }
    }

    /**
     * Reads a line from the input stream.  A line is terminated by
     * EOF, CR, LF or CRLF.  If EOF is reached and there is no data in
     * the line, the null is returned.
     */
    public String readLine() throws IOException
    {
        int i = 0;

        if (str == null) {
            str = new char[256];
        }
      loop:
        while (true) {
            int ch = read();
            switch (ch) {
              case -1:
                if (i > 0) {
                    break loop;
                }
                // eof with no data
                return null;

              case '\n':
                break loop;

              case '\r':
              	if (in.markSupported()) {
              		in.mark(1);
	                if ((ch = read()) != '\n') {
	                    in.reset();
	                }
              	}
                break loop;
            }
            if (i == str.length) {
                char newstr[] = new char[str.length * 2];
                System.arraycopy(str, 0, newstr, 0, i);
                str = newstr;
            }
            str[i++] = (char)ch;
        }
        String s = new String(str, 0, i);
        return s;
    }

    /**
     * Read a boolean
     */
    public boolean readBoolean() throws IOException
    {
        return (boolean) ( readOne() != 0 );
    }

    /**
     * Read a short, big endian.
     */
    public short readShort() throws IOException
    {
        return (short) ((readOne() << 8) + (readOne() << 0));
    }
    
    /**
     * Read an int, big endian.
     */
    public int readInt() throws IOException
    {
        return ((readOne() << 24) + (readOne() << 16) +
		(readOne() << 8) + (readOne() << 0));
    }

    /**
     * Reads a variable length signed integer.
     */
    public long readVInt() throws IOException
    {
        long value = 0;
        long c;
        int shift = 0;
        while (((c = read()) & 0x80) == 0) {
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
    public long readVUInt() throws IOException
    {
        long value = 0;
        int c;
        int shift = 0;

        while (((c = read()) & 0x80) == 0) {
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
     * Read an int, big endian.
     */
    public long readLong() throws IOException
    {
        return ((long) readInt() << 32) + (long) readInt();
    }

    /**
     * Read a short, little endian.
     */
    public short readLittleShort() throws IOException
    {
        return (short)((readOne() << 0) + (readOne() << 8));
    }

    /**
     * Read an int, big endian.
     */
    public int readLittleInt() throws IOException
    {
        return ((readOne() << 0) + (readOne() << 8) +
		(readOne() << 16) + (readOne() << 24));
    }

    /**
     * Reads a float.
     */
    public final float readFloat() throws IOException
    {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads a double.
     */
    public final double readDouble() throws IOException
    {
        return Double.longBitsToDouble(readLong());
    }
    
    /**
     * Read s UTF encoded string.
     */
    public final String readUTF() throws IOException
    {
        int utflen;
        if ( fUseVString ) {
            utflen = (int) readVUInt();
        }
        else {
            utflen = readShort();
        }
        StringBuffer str = new StringBuffer(utflen);
        byte bytearr [] = new byte[utflen];
        int c, char2, char3;
        int count = 0;

        readFully(bytearr, 0, utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3:
	        case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    count++;
                    str.append((char)c);
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException();
                    char2 = (int) bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(); 
                    str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException();
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException();       
                    str.append((char)(((c     & 0x0F) << 12) |
                                      ((char2 & 0x3F) << 6)  |
                                      ((char3 & 0x3F) << 0)));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();           
                }
        }
        // The number of chars produced may be less than utflen
        return new String(str);
    }

    /**
     * Reads a dictionary from the stream.
     */
    public TeDict readDict() throws IOException {
    	return TeDictBinary.binaryToDict( this );
    }

    /**
     * Reads a variable size chunk of bytes.
     */
    public byte[] readVData() throws IOException {
    	int size = (int) readVUInt();
    	// Put a sanity check cap on the read size.
    	if (size > 65535) {
    		throw new IOException("byte array is too long");
    	}
    	byte data[] = new byte[size];
    	readFully(data, 0, size);
    	return data;
    }

    /**
     * Set whether or not to use vInts as size of vStrings
     */
    public void setUseVString( boolean flag ) 
    {
        fUseVString = flag;
    }
}
