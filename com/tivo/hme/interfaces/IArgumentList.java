//////////////////////////////////////////////////////////////////////
//
// File: IArgumentList.java
//
// Copyright (c) 2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.interfaces;

/**
 * Interface for encapsulating and manipulating command line arguments.
 * 
 * @author kgidley
 */
public interface IArgumentList {

    /**
     * Look for a command line parameter and throw an exception if not
     * found. For example, if the ArgumentList is <code>"-something good"</code>
     * then <code>getValue("-something")</code> will return <code>"good"</code>.
     */
    public String getValue(String key);

    /**
     * Look for a command line parameter as an int and throw an exception if not
     * found.
     */
    public int getInt(String key);

    /**
     * Remove the first command line argument.
     */
    public String shift();

    /**
     * Look for a command line argument and return defaultValue if not found.
     */
    public String getValue(String key, String defaultValue);


    /**
     * Look for a command line argument as an int and return defaultValue if not
     * found.
     */
    public int getInt(String key, int defaultValue);

    
    /**
     * Return true if the flag is found. Note that a boolean flag doesn't have a
     * parameter.
     */
    public boolean getBoolean(String key);

    /**
     * Throws an exception if there are any flags remaining in the list of arguments.
     */
    public void checkForIllegalFlags();

    /**
     * Returns the arguments that remain.
     */
    public String[] getRemainingArgs();

    /**
     * Returns the number of arguments that remain.
     */
    public int getRemainingCount();


    /** 
     * Exception thrown when invalid or missing arguments are detected.
     */
    @SuppressWarnings("serial")
	public static class BadArgumentException extends RuntimeException
    {
        public BadArgumentException(String s)
        {
            super(s);
        }
    }
}
