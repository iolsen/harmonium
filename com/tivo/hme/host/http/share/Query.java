//////////////////////////////////////////////////////////////////////
//
// File: Query.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.share;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A class that reads and writes URL query strings. Query assumes that the keys
 * and values are URL encoded.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class Query
{
    Map map;                    // map of the query converted to lower case
    List list;                  // ordered list of the same query
    char data[];                // for building strings

    /**
     * Create an empty query.
     */
    public Query()
    {
        map = new Hashtable();
        list = new ArrayList();
    }
    
    /**
     * Create a query from the given String.
     */
    public Query(String query)
    {
        this();
        parse(query);
    }
    
    /**
     * Create a query with the given URL.
     */
    public Query(URL url)
    {
        this(url.getQuery());
    }

    /**
     * Parse the query.
     */
    public void parse(String query)
    {
        if (query == null || query.indexOf('=') == -1) {
            return;
        }

        int at = 0;
        int len = query.length();

        if (query.startsWith("?")) {
            ++at;
        }
        do {
            // find = and &
            int equal = query.indexOf('=', at);
            if (equal < 0) {
                throw new IllegalArgumentException("invalid query (trailing key, = not found)");
            }
            int amp = query.indexOf('&', equal);
            if (amp == -1) {
                amp = query.length();
            }

            // add the key/value
            try {
                String key = URLDecoder.decode(query.substring(at, equal), "UTF-8");
                String value = URLDecoder.decode(query.substring(equal + 1, amp), "UTF-8");
                map.put(key.toLowerCase(), value);
                list.add(key);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // advance
            at = amp + 1;
        } while (at < len);
    }

    /**
     * Returns a query value. This will be more efficient if key is lowercase.
     */
    public String get(String key)
    {
        String value = (String)map.get(key);
        if (value == null) {
            // try lowercase
            value = (String)map.get(key.toLowerCase());
        }
        return value;
    }

    /**
     * Returns a query value as an integer. This will be more efficient if key is
     * lowercase.
     */
    public int getInt(String key, int defaultValue)
    {
        String value = get(key);
        return (value == null) ? defaultValue : Integer.parseInt(value);
    }

    public Map getMap()
    {
        return map;
    }
    
    /**
     * Returns an iterator for the keys in the order they were received.
     */
    public Iterator getKeys()
    {
        return list.iterator();
    }

    /**
     * Convert the query back into a string. The string will have URL encoded keys and
     * values.
     */
    public String toString()
    {
        boolean amp = false;
        StringBuffer buf = new StringBuffer();
        Iterator i = getKeys();
        while (i.hasNext()) {
            if (amp) {
                buf.append('&');
            }
            String key = (String)i.next();
            String value = get(key);
            try {
                buf.append(URLEncoder.encode(key, "UTF-8"));
                buf.append('=');
                buf.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            amp = true;
        }
        return new String(buf);
    }

    public static void main(String args[])
    {
        Query query = new Query(args[0]);
        System.out.println(query);
        for (int i = 1; i < args.length; ++i) {
            System.out.println(args[i] + " = " + query.get(args[i]));
        }
    }
}
