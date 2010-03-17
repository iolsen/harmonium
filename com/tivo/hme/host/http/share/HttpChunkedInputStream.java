//////////////////////////////////////////////////////////////////////
//
// File: HttpChunkedInputStream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.share;

import java.io.*;

/**
 * A class that can read a chunked encoded stream.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class HttpChunkedInputStream extends FilterInputStream
{
    boolean eof;                // when true, no more reading allowed
    long clen;                  // current chunk size or -1 if not known
    Headers footer;             // footer mime header

    public HttpChunkedInputStream(InputStream in)
    {
        super(in);
    }
        
    public int read() throws IOException
    {
        throw new IOException("not supported");
    }

    public int read(byte data[]) throws IOException
    {
        return read(data, 0, data.length);
    }

    public int read(byte data[], int off, int len) throws IOException
    {
        // ensure we have a chunk size
        if (clen == 0) {
            if (eof) {
                // REMIND: return -1 or throw exception?
                return -1;
            }
            readChunkSize();
            if (clen == 0) {
                readFooter();
                // eof occurred
                eof = true;
                return -1;
            }
        }

        // read the data
        if (len > clen) {
            len = (int) clen;
        }
        int n = in.read(data, off, len);
        if (n > 0) {
            clen -= n;
            if (clen == 0) {
                // skip the trailing \r\n at the end of the chunk
                expect('\r');
                expect('\n');
            }
            return n;
        }
        return n;
    }

    private void expect(int c) throws IOException
    {
        int ch = in.read();
        if (ch != c) {
            throw new IOException("invalid chunk: " + ((char) c) + " unexpected");
        }
    }

    /**
     * Read the chunk size from the input stream.  Throws an exception
     * if it's malformed in any way.
     */
    private void readChunkSize() throws IOException
    {
        long len = -1;
        int c; 

        // read the hex number until \r
      loop:
        while (true) {
            int v;
            switch (c = in.read()) {
              case -1:
                throw new EOFException("bad chunk");
              case '0': case '1': case '2': case '3': case '4':
              case '5': case '6': case '7': case '8': case '9':
                v = (c - '0');
                break;
              case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                v = 10 + (c - 'a');
                break;
              case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                v = 10 + (c - 'A');
                break;
                
              case ' ':
              case '\t':
                // some peeps include whitespace after the hex length. skip to eol.
              case ';':
                // there's a chunk-ext ; ignore it and scan until end of line
                while ((c = in.read()) != -1) {
                    if (c == '\r') {
                        break;
                    }
                }
                // falls through ...

              case '\r':
                break loop;

              default:
                throw new IOException("malformed chunk: " + ((char) c) + " unexpected");
            }
            if (len == -1) {
                len = v;
            } else {
                len = (len << 4) + v;
            }
        }
        if (c == -1) {
            throw new EOFException("malformed chunk");
        }
        if (c == '\r') {
            expect('\n');
        }
        clen = len;
    }

    /**
     * Read the footer, which is just another MIME header.
     */
    private void readFooter() throws IOException
    {
        footer = new Headers(in);
    }

    public int available() throws IOException
    {
        return (int) Math.min(Integer.MIN_VALUE, clen);
    }

    public Headers getFooter() {
        return footer;
    }

    public static void main(String args[]) throws Exception
    {
        FileInputStream in = new FileInputStream(args[0]);
        try {
            HttpChunkedInputStream cin = new HttpChunkedInputStream(in);
            byte data[] = new byte[1024];
            int n;
            while ((n = cin.read(data, 0, data.length)) > 0) {
                System.out.write(data, 0, n);
            }
        } finally {
            in.close();
        }
    }
}
