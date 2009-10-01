//////////////////////////////////////////////////////////////////////
//
// File: Config.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A set of configuration options, indexed by key. Several formats are
 * supported:
 *
 * VALUE:       key=value
 * INT:         key=10
 * BOOLEAN:     key=true|false
 * VALUE LIST:  key=a,b,c
 * INT LIST:    key=10,20,30
 */
@SuppressWarnings("unchecked")
public class Config
{
	List keys;
    Map map;

	public Config()
    {
        this.keys = new ArrayList();
        this.map = new HashMap();
    }

	public void load(String path) throws IOException
    {
        clear();
        LineNumberReader in = new LineNumberReader(new FileReader(path));
        try {
            Map sections = new HashMap();
            String section = "";
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();

                // comments
                if (line.startsWith(";") || line.length() == 0) {
                    continue;
                }
                
                // sections
                if (line.startsWith("[")) {
                    if (!line.endsWith("]")) {
                        throw new IOException("invalid section " + line);
                    }
                    String s = line.substring(1, line.length() - 1).trim();
                    Integer cnt = (Integer)sections.get(s);
                    if (cnt == null) {
                        cnt = new Integer(0);
                    }
                    section = s + cnt + ".";
                    sections.put(s, new Integer(cnt.intValue() + 1));
                    continue;
                }

                // values
                int equals = line.indexOf('=');
                if (equals == -1) {
                    throw new IOException("could not find '=' in " + line);
                }
                String key = line.substring(0, equals).trim();
                String value = line.substring(equals + 1).trim();
                put(section + key, value);
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    public void clear()
    {
        keys.clear();
        map.clear();
    }
    
    /**
     * Update a key in the config.
     */
    public void put(String key, String value)
    {
        if (map.put(key, value) == null) {
            keys.add(key);
        }
    }
    
    //
    // Accessors for flags that MUST be present. These two methods will throw
    // exceptions if the key isn't found.
    //

    /**
     * Look for a key and throw an exception if not found.
     */
    public String getValue(String key)
    {
        String value = getValue(key, null);
        if (value == null) {
            throw new BadArgumentException(key + " is required");
        }
        return value;
    }

    /**
     * Look for a key as an int and throw an exception if not found.
     */
    public int getInt(String key)
    {
        try {
            return Integer.parseInt(getValue(key));
        } catch (NumberFormatException e) {
            throw new BadArgumentException(key + " should be a number");
        }
    }

    /**
     * Look for a key as a boolean and throw an exception if not found.
     */
    public boolean getBool(String key)
    {
        return getValue(key).equals("true");
    }
    
    /**
     * Look for a key as a list of strings and throw an exception if not found.
     */
    public String[] getValueList(String key)
    {
        String value[] = getValueList(key, null);
        if (value.length == 0) {
            throw new BadArgumentException(key + " is required");
        }
        return value;
    }

    /**
     * Look for a key as a list of ints and throw an exception if not found.
     */
    public int[] getIntList(String key)
    {
        int values[] = getIntList(key, null);
        if (values.length == 0) {
            throw new BadArgumentException(key + " is required");
        }
        return values;
    }


    
    //
    // Accessors for flags that don't have to be present. The default is
    // returned if the flag is not found.
    //

    /**
     * Look for a key and return defaultValue if not found.
     */
    public String getValue(String key, String defaultValue)
    {
        String value = (String)map.get(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Look for a key as an int and return defaultValue if not found.
     */
    public int getInt(String key, int defaultValue)
    {
        String value = getValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadArgumentException(key + " should be a number");
        }
    }

    /**
     * Look for a key as a boolean and return defaultValue if not found.
     */
    public boolean getBool(String key, boolean defaultValue)
    {
        String value = getValue(key, null);
        return (value != null) ? value.equals("true") : defaultValue;
    }
    
    
    /**
     * Look for a key as a list of strings and throw an exception if not found.
     */
    public String[] getValueList(String key, String defaultValue)
    {
        if (defaultValue == null) {
            defaultValue = "";
        }
        String str = getValue(key, defaultValue);
        StringTokenizer tokens = new StringTokenizer(str, ",");
        String value[] = new String[tokens.countTokens()];
        for (int i = 0; i < value.length; ++i) {
            value[i] = tokens.nextToken();
        }
        return value;
    }

    /**
     * Look for a key as a list of ints and throw an exception if not found.
     */
    public int[] getIntList(String key, String defaultValue)
    {
        String strs[] = getValueList(key, defaultValue);
        int value[] = new int[strs.length];
        for (int i = value.length; i-- > 0;) {
            try {
                value[i] = Integer.parseInt(strs[i]);
            } catch (NumberFormatException e) {
                throw new BadArgumentException(key + " should be a list of numbers");
            }
        }
        return value;
    }

    public void dump()
    {
        for (Iterator i = keys.iterator(); i.hasNext();) {
            String key = (String)i.next();
            System.out.println(key+ "=" + map.get(key));
        }
    }

    @SuppressWarnings("serial")
	public static class BadArgumentException extends RuntimeException
    {
        public BadArgumentException(String s)
        {
            super(s);
        }
    }

    public static void main(String args[]) throws IOException
    {
        Config c = new Config();
        c.load(args[0]);
        c.dump();
    }
}
