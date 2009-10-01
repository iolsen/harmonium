//////////////////////////////////////////////////////////////////////
//
// File: Misc.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.util;

import java.security.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Misc helpers.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class Misc
{
    final static String ZEROES = "0000000000000000000000000000";
    final static char HEX[] = {'0','1','2','3','4','5','6','7','8','9',
                               'a','b','c','d','e','f'};
    
    private static SecureRandom random;
    
    /**
     * Hex dump to System.out.
     */
    public static void hexl(byte buf[], int off, int len)
    {
        int pos = 0;
        char string[] = new char[16];
        while (pos < len) {
            int i;
            for (i = 0; i < 16; ++i) {
                if (pos == len) {
                    if (i == 0) {
                        break;
                    }
                    System.out.print("  ");
                    string[i] = 0;
                } else {
                    if (i == 0) {
                        String hex = Integer.toHexString(pos);
                        pad(hex, 8);
                        System.out.print(": ");
                    }
                    int c = ((int)buf[off + pos]) & 0xFF;
                    String hex = Integer.toHexString(c);
                    pad(hex, 2);
                    string[i] = (char)((c < 0x20 || c >= 0x7F) ? '.' : c);
                    ++pos;
                }
                if (i % 2 == 1) {
                    System.out.print(" ");
                }
            }
            if (i != 0) {
                System.out.println(" " + new String(string));
            }
        }
    }

    /**
     * Get some random bytes.
     */
    public static String getRandomBytes()
    {
        MessageDigest digest;
        try {
            if (random == null) {
                synchronized (Misc.class) {
                    if (random == null) {
                        random = SecureRandom.getInstance("SHA1PRNG");
                    }
                }
            }
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("could not generate cookie");
        }

        return bytesToHex(digest.digest(("" + random.nextInt()).getBytes()));
    }

    /**
     * Get some random bytes.
     */
    public static String oneWayHashString(String hashStr)
    {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("could not generate cookie");
        }

        return bytesToHex(digest.digest(hashStr.getBytes()));
    }
    
    /**
     * Convert bytes to hex string.
     */
    public static String bytesToHex(byte bytes[])
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            buf.append(HEX[(b & 0xf0) >> 4]);
            buf.append(HEX[(b & 0x0f) >> 0]);
        }
        return buf.toString();
    }

    /**
     * Returns true if running on windows. See
     * http://lopica.sourceforge.net/os.html.
     */
    public static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("windows ");
    }

    /**
     * Pretty print size in bytes.
     */
    public static String getPrettyBytes(float nBytes)
    {
        if (nBytes < 1024) {
            return ((int)nBytes) + " bytes";
        }
        nBytes /= 1024;
        if (nBytes < 1024) {
            return (((int)nBytes) + "." + ((int)(nBytes * 10) % 10)) + " KB";
        }
        nBytes /= 1024;
        return     (((int)nBytes) + "." + ((int)(nBytes * 10) % 10)) + " MB";
    }

    /**
     * Find/replace a string within a string.
     */
    public static String replace(String s, String find, String replace)
    {
        if (find == null || find.length() == 0) {
            return s;
        }
        
        StringBuffer buf = new StringBuffer();
        int flen = find.length();
        int index = 0;
        while (true) {
            int next = s.indexOf(find, index);
            if (next == -1) {
                break;
            }
            buf.append(s.substring(index, next));
            buf.append(replace);
            index = next + flen;
        }
        buf.append(s.substring(index));
        return buf.toString();
    }

    static void pad(String s, int len)
    {
        len = len - s.length();
        if (len > 0) {
            System.out.print(ZEROES.substring(0, len));
        }
        System.out.print(s);
    }

    /**
     * Return a list of all interfaces
     **/
    public static InetAddress[] getInterfaces() throws IOException, UnknownHostException 
    {
        try {
            //
            // this only works in JDK 1.4
            //
            Vector addrs = new Vector();
            for (Enumeration e = NetworkInterface.getNetworkInterfaces() ; e.hasMoreElements() ;) {
                NetworkInterface ni = (NetworkInterface)e.nextElement();
                Enumeration a = ni.getInetAddresses();
                while (a.hasMoreElements()) {
                    InetAddress i = (InetAddress)a.nextElement();
                    if (i instanceof Inet4Address) {
                        addrs.addElement(i);
                    }
                }
            }

            InetAddress result[] = new InetAddress[addrs.size()];
            addrs.copyInto(result);
            return result;
        } catch (NoClassDefFoundError e) {
            // the old standby
            return InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
        }
    }
}
