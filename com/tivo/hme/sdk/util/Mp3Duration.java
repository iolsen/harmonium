//////////////////////////////////////////////////////////////////////
//
// File: Mp3Duration.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A helper class for calculating the duration of an mp3 file.
 * 
 * @deprecated Use Mp3Helper class instead. {@link Mp3Helper}
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class Mp3Duration
{
//    // if true, spew some debug output
//    final static boolean DEBUG = false;

    /**
     * Calculate the duration in ms for a file.
     */
    public static long getMp3Duration(File file) throws IOException
    {
        InputStream in = new FileInputStream(file);
        try {
            Mp3Helper mp3Helper = new Mp3Helper(in, file.length());
            return mp3Helper.getMp3Duration();
        } finally {
            in.close();
        }
    }
    
    /**
     * Calculate the duration in ms for a stream.
     */
    public static long getMp3Duration(InputStream in, long available) throws IOException
    {
        Mp3Helper mp3Helper = new Mp3Helper(in, available);
        return mp3Helper.getMp3Duration();
    }
    
    /**
     * For testing.
     */
    public static void main(String args[]) throws IOException
    {
        for (int i = 0; i < args.length; ++i) {
            File file = new File(args[i]);
            System.out.println(file + "\t" + getMp3Duration(file));
            System.out.flush();
        }
    }
}
