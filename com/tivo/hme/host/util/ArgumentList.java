//////////////////////////////////////////////////////////////////////
//
// File: ArgumentList.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.util;

import java.util.*;

import com.tivo.hme.interfaces.IArgumentList;

/**
 * ArgumentList is a helper class for parsing command line arguments. It is also
 * useful for converting command line arguments from arrays to strings and back
 * again.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class ArgumentList implements IArgumentList
{
    Vector args;

    /**
     * Create a new ArgumentList from an existing list.
     */
    public ArgumentList(ArgumentList list)
    {
        this.args = new Vector(list.args);
    }
    
    /**
     * Create a new ArgumentList from an array of Strings.
     */
    public ArgumentList(String args[])
    {
        this.args = new Vector(args.length);
        for (int i = 0; i < args.length; ++i) {
            this.args.addElement(args[i]);
        }
    }

    /**
     * Create a new ArgumentList from a command line String.
     */
    public ArgumentList(String cmdLine)
    {
        args = new Vector();

        boolean inQuote = false;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < cmdLine.length(); ++i) {
            char c = cmdLine.charAt(i);
            switch (c) {
              case '"':
                if (inQuote) {
                    args.addElement(new String(buf));
                    buf.setLength(0);
                }
                inQuote = !inQuote;
                break;

              case ' ':
                if (!inQuote) {
                    if (buf.length() > 0) {
                        args.addElement(new String(buf));
                        buf.setLength(0);
                    }
                } else {
                    buf.append(c);
                }
                break;
                
              default:
                buf.append(c);
                break;
            }
        }

        if (buf.length() > 0) {
            args.addElement(new String(buf));
            buf.setLength(0);
        }
    }

    //
    // Accessors for flags that MUST be present in the arguments. These two
    // methods will throw exceptions if the key isn't found.
    //

    /**
     * Look for a command line parameter and throw an exception if not
     * found. For example, if the ArgumentList is <code>"-something good"</code>
     * then <code>getValue("-something")</code> will return <code>"good"</code>.
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
     * Look for a command line parameter as an int and throw an exception if not
     * found.
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
     * Remove the first command line argument.
     */
    public String shift()
    {
        if (args.size() == 0) {
            throw new BadArgumentException("missing argument");
        }
        String value = (String)args.elementAt(0);
        args.removeElementAt(0);
        return value;
    }


    //
    // Accessors for flags that don't have to be present. The default is
    // returned if the flag is not found.
    //

    /**
     * Look for a command line argument and return defaultValue if not found.
     */
    public String getValue(String key, String defaultValue)
    {
        int index = args.indexOf(key);
        if (index == -1) {
            return defaultValue;
        }
        args.removeElementAt(index);

        if (index == args.size()) {
            throw new BadArgumentException(key + " needs a parameter");
        }

        String value = (String)args.elementAt(index);
        if (value.startsWith("-")) {
            throw new BadArgumentException(key + " needs a parameter");
        }
        args.removeElementAt(index);
        
        return value;
    }

    /**
     * Look for a command line argument as an int and return defaultValue if not
     * found.
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
     * Return true if the flag is found. Note that a boolean flag doesn't have a
     * parameter.
     */
    public boolean getBoolean(String key)
    {
        int index = args.indexOf(key);
        if (index == -1) {
            return false;
        }
        args.removeElementAt(index);
        return true;
    }

    /**
     * Throws an exception if there are any flags remaining in the list of arguments.
     */
    public void checkForIllegalFlags()
    {
        Enumeration e = args.elements();
        while (e.hasMoreElements()) {
            String arg = (String)e.nextElement();
            if (arg.startsWith("-")) {
                throw new BadArgumentException("don't understand " + arg);
            }
        }
    }

    /**
     * Returns the arguments that remain.
     */
    public String[] getRemainingArgs()
    {
        String remaining[] = new String[args.size()];
        args.copyInto(remaining);
        return remaining;
    }

    /**
     * Returns the number of arguments that remain.
     */
    public int getRemainingCount()
    {
        return args.size();
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < args.size(); ++i) {
            if (i != 0) {
                buf.append(' ');
            }
            String arg = (String)args.elementAt(i);
            if (arg.indexOf(' ') != -1 && !arg.startsWith("\"")) {
                buf.append('"');
                buf.append(arg);
                buf.append('"');                
            } else {
                buf.append(arg);
            }
        }
        return new String(buf);
    }

    // for testing purposes
    public static void main(String args[]) {
        ArgumentList list = new ArgumentList(args);

        String must = list.getValue("-must");
        int mustInt = list.getInt("-mustint");

        String param = list.getValue("-param", "default");
        int paramInt = list.getInt("-paramint", 1775);
        
        boolean flag = list.getBoolean("-f");

        list.checkForIllegalFlags();

        System.out.println("must = " + must);
        System.out.println("mustInt = " + mustInt);
        System.out.println("param = " + param);
        System.out.println("paramInt = " + paramInt);
        System.out.println("flag = " + flag);

        String remaining[] = list.getRemainingArgs();
        for (int i = 0; i < remaining.length; ++i) {
            System.out.println(i + " = " + remaining[i]);
        }
    }
}
