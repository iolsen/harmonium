//////////////////////////////////////////////////////////////////////
//
// File: Headers.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.share;

import java.io.*;
import java.util.*;

// REMIND: If the next line starts with a space, it's a continuation of the
// previous line's value.

/**
 * A class that reads and writes MIME headers.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class Headers
{
    Hashtable hash;                     // lowercase hash of the mime headers
    List list;                          // ordered list of the same mime headers
    char data[];                        // for building strings

    /**
     * Creates headers from the specified input stream.
     */
    public Headers(InputStream in) throws IOException
    {
        hash = new Hashtable();
        list = new ArrayList();
        parse(in);
    }

    /**
     * Parse the mime header.
     */
    public void parse(InputStream in) throws IOException
    {
        String key;
        data = new char[128];
        while ((key = readKey(in)) != null) {
            String value = readValue(in);
            hash.put(key.toLowerCase(), value);
            list.add(key);
        }
        data = null;
    }

    /**
     * Add a key to the hash, but not the list.  This is for
     * http-method, http-uri, http-version.
     */
    public void addInternal(String key, String value) {
        hash.put(key, value);
    }

    /**
     * Read the mime key.  Returns null if we've reached the end of
     * the mime header.
     */
    private String readKey(InputStream in) throws IOException
    {
        int i = 0;

        while (true) {
            int ch = in.read();
            switch (ch) {
              case -1:
                throw new EOFException("bad mime header reading key: " + new String(data, 0, i));

              case ':':
                return new String(data, 0, i);
                
              case '\r':
                if ((ch = in.read()) == '\n') {
                    // end of headers
                    return null;
                }
                throw new IOException("malformed mime header: CRLF");
              case '\n':
                return null;
            }

            // check for room
            if (i == data.length) {
                System.arraycopy(data, 0, data = new char[i * 2], 0, i);
            }

            data[i++] = (char) ch;
        }
    }

    /**
     * Read the mime value.
     */
    private String readValue(InputStream in) throws IOException
    {
        int i = 0;

        while (true) {
            int ch = in.read();
            switch (ch) {
              case -1:
                throw new EOFException("malformed mime header reading value");

              case '\r':
                if ((ch = in.read()) != '\n') {
                    throw new IOException("Malformed mime header: CRLF");
                }
              case '\n':
                return new String(data, 0, i).trim();

              case ' ': case '\t':
                if (i == 0) {
                    // skip leading white space
                    continue;
                }
                break;
            }

            // check for room
            if (i == data.length) {
                System.arraycopy(data, 0, data = new char[i * 2], 0, i);
            }

            // add the character
            data[i++] = (char) ch;
        }
    }

    /**
     * Returns a mime header. This will be more efficient if key is lowercase.
     */
    public String get(String key) {
        String value = (String)hash.get(key);
        if (value == null) {
            // try lowercase
            value = (String)hash.get(key.toLowerCase());
        }
        return value;
    }

    /**
     * Returns a mime header as an integer. This will be more efficient if key is
     * lowercase.
     */
    public int getInt(String key, int defaultValue)
    {
        String value = get(key);
        return (value == null) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Returns a mime header as an integer. This will be more efficient if key is
     * lowercase.
     */
    public long getLong(String key, long defaultValue)
    {
        String value = get(key);
        return (value == null) ? defaultValue : Long.parseLong(value);
    }

    /**
     * Returns an iterator for the mime header keys in the order they were received.
     */
    public Iterator getKeys() {
        return list.iterator();
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        Iterator i = getKeys();
        while (i.hasNext()) {
            String key = (String)i.next();
            String value = get(key);
            buf.append(key);
            buf.append(": ");
            buf.append(value);
            buf.append("\r\n");
        }
        return new String(buf);
    }
}
