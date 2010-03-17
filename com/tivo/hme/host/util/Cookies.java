//////////////////////////////////////////////////////////////////////
//
// File: Cookies.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import com.tivo.hme.host.util.ArgumentList;

/**
 * Cookie list and helpers.
 * 
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
public class Cookies
{
    final static String RESERVED[] = { "Comment", "Domain", "Max-Age", "Path",
                                      "Version" };
    
    File file;
    List cookies;

    /**
     * Create the list of cookies.
     */
    public Cookies()
    {
        this(getDefaultCookieFile());
    }

    /**
     * Create the list of cookies from the given file.
     */
    Cookies(File file)
    {
        this.file = file;
        cookies = new Vector();
        
        if (file != null && file.exists()) {
            try {
                FileInputStream fin = new FileInputStream(file);
                try {
//                    FastInputStream in = new FastInputStream(fin, 4096);
                    LineNumberReader in = new LineNumberReader(new InputStreamReader(fin));
                    while (true) {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.startsWith("#")) {
                            continue;
                        }

                        // for compatability with the old props format
                        int equal = line.indexOf("\\=");
                        if (equal != -1) {
                            line = line.substring(0, equal) + "=" + line.substring(equal + 2);
                        }
                        
                        cookies.add(line);
                    }
                } finally {
                    fin.close();
                }
            } catch (IOException e) {
                System.out.println("Could not load cookies file " + file);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Parse Set-Cookie: XXXX.
     *
     * Note that we only support a subset of 2109:
     * <ul>
     *  <li>only one cookie per set-cookie
     *  <li>reserved words are ignored
     *  <li>no support for quoted values
     * </ul>
     */
    public synchronized void parseSetCookie(String host, String mimeValue)
    {
        StringTokenizer tokens = new StringTokenizer(mimeValue, ";");

        String resultKey = null;
        String resultValue = null;
        
        int cnt = tokens.countTokens();
        for (int i = 0; i < cnt; ++i) {
            String token = tokens.nextToken();

            //
            // break token into key/value
            //
            
            String key, value;
            int equal = token.indexOf('=');
            if (equal == -1) {
                key = token.trim();
                value = null;
            } else {
                key = token.substring(0, equal).trim();
                value = token.substring(equal + 1).trim();
            }

            // ignore invalid keys
            if (!isValidKey(key) || key.startsWith("$")) {
                continue;
            }

            //
            // is it a reserved key?
            //
            
            boolean isReserved = false;
            for (int j = RESERVED.length; j-- > 0;) {
                if (RESERVED[j].equalsIgnoreCase(key)) {
                    isReserved = true;
                    break;
                }
            }

            //
            // if this is the first pair, it must be NAME=VALUE
            //
            
            if (i == 0) {
                if (isReserved || value == null) {
                    // no NAME=VALUE, bail
                    return;
                }

                resultKey = key;
                resultValue = value;
            }

            //
            // note that the reserved keys are ignored
            //
        }

        if (resultKey == null) {
            return;
        }

        //
        // add the cookie to our list. replace existing cookies if necessary.
        //
        
        boolean found = false;
        String toFind = host + "=" + resultKey + "=";
        String toAdd  = host + "=" + resultKey + "=" + resultValue;
        for (int i = cookies.size(); i-- > 0;) {
            String line = (String)cookies.get(i);
            if (line.startsWith(toFind)) {
                // replace the existing cookie
                cookies.set(i, toAdd);
                found = true;
                break;
            }
        }
        if (!found) {
            cookies.add(toAdd);
        }
        
        //
        // now save
        //

        if (file != null) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    OutputStreamWriter out = new OutputStreamWriter(fout);
                    out.write("#\n# TiVo cookies file\n#\n");
                    cnt = cookies.size();
                    for (int i = 0; i < cnt; ++i) {
                        out.write((String)cookies.get(i));
                    }
                    out.flush();
                } finally {
                    fout.close();
                }
            } catch (IOException e) {
                System.out.println("Could not write cookies file " + file);
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the cookies for the given host.
     */
    public synchronized List getCookies(String host)
    {
        List list = new Vector();
        String toFind = host + "=";
        //int cnt = cookies.size();
        for (int i = 0; i < cookies.size(); ++i) {
            String line = (String)cookies.get(i);
            if (line.startsWith(toFind)) {
                // found one!
                list.add(line.substring(toFind.length()));
            }
        }
        return list;
    }

    /**
     * Parse 'Cookie: XXXX' and return map of key/value pairs.
     */
    public static Map parseCookie(String mimeValue)
    {
        StringTokenizer tokens = new StringTokenizer(mimeValue, ";");

        Map map = new HashMap();
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();

            //
            // break token into key/value
            //
            
            String key, value;
            int equal = token.indexOf('=');
            if (equal == -1) {
                key = token.trim();
                value = "<null>";
            } else {
                key = token.substring(0, equal).trim();
                value = token.substring(equal + 1).trim();
            }

            // ignore invalid keys
            if (!isValidKey(key) || key.startsWith("$")) {
                continue;
            }

            map.put(key.toLowerCase(), value);
        }
        return map;
    }
    
    /**
     * Create a new, random cookie.
     */
    public static String createRandomCookie()
    {
        return Misc.getRandomBytes();
    }

    static File getDefaultCookieFile()
    {
        String home = System.getProperty("user.home");
        String tivo = Misc.isWindows() ? "Application Data/TiVo" : ".tivo";
        return new File(home + "/" + tivo + "/sim-cookies.txt");
    }
    
    /**
     * Returns true if the key is a valid cookie key (no whitespace
     */
    static boolean isValidKey(String key)
    {
        if (key.length() == 0) {
            return false;
        }
        for (int i = key.length(); i-- > 0;) {
            char ch = key.charAt(i);
            
            // see rfc 2068 "CTL"            
            if (ch <= (char)31 || ch >= (char)127) {
                return false;
            }

            // see rfc 2068 "tspecials"
            switch (key.charAt(i)) {
                case '(': case ')': case '<': case '>': case '@':
                case ',': case ';': case ':': case '\\': case '"':
                case '/': case '[': case ']': case '?': case '=':
                case '{': case '}':
                case ' ': case '\n': case '\r': case '\t':
                return false;
            }
        }
        return true;
    }
    
    static void usage()
    {
        System.err.println("Usage : Cookies [--load] [--clear] [--set host value] [--get host]");
    }
    
    /**
     * Main, for testing.
     */
    public static void main(String argv[])
    {
        Cookies c = null;

        ArgumentList args = new ArgumentList(argv);
        if (args.getRemainingCount() == 0) {
            usage();
            return;
        }

        while (args.getRemainingCount() > 0) {
            boolean dump = true;
            String arg = args.shift();
            if (arg.equals("--load")) {
                c = new Cookies();
            } else if (arg.equals("--clear")) {
                new File(System.getProperty("user.home") + "/Application Data/TiVo/sim-cookies.txt").delete();
                c = null;
            } else if (arg.equals("--set")) {
                String host = args.shift();
                String value = args.shift();
                if (c == null) {
                    c = new Cookies(null);
                }
                System.out.println("(" + host + ") Set-Cookie: " + value);
                c.parseSetCookie(host, value);
            } else if (arg.equals("--get")) {
                String host = args.shift();
                if (c == null) {
                    c = new Cookies(null);
                }
                List list = c.getCookies(host);
                for (int i = 0; i < list.size(); ++i) {
                    System.out.println("(" + host + ") Cookie: " + list.get(i));
                }
                dump = false;
            } else {
                usage();
            }

            if (dump && c != null) {
                for (int i = 0; i < c.cookies.size(); ++i) {
                    System.out.println("[" + i + "] " + c.cookies.get(i));
                }
            }
        }
    }
}
