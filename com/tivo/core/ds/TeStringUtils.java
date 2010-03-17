////////////////////////////////////////////////////////////
// 
// Copyright (C) 2005 TiVo Inc.
// All rights reserved.
// 
////////////////////////////////////////////////////////////

package com.tivo.core.ds;

import java.util.StringTokenizer;

public class TeStringUtils {
    
    public static String[] split(String source, String delims) {
        StringTokenizer strTok = new StringTokenizer(source, delims);
        int count = strTok.countTokens();
        String[] retVal = new String[count];
        int i = 0;
        while (strTok.hasMoreTokens()) {
            retVal[i++] = strTok.nextToken();
        }
        return retVal;
    }

    /**
     * Determines if the specified character is ISO-LATIN-1 white space.
     * This method returns <code>true</code> for the following five
     * characters only:
     * <table>
     * <tr><td><code>'\t'</code></td>            <td><code>'&#92;u0009'</code></td>
     *     <td><code>HORIZONTAL TABULATION</code></td></tr>
     * <tr><td><code>'\n'</code></td>            <td><code>'&#92;u000A'</code></td>
     *     <td><code>NEW LINE</code></td></tr>
     * <tr><td><code>'\f'</code></td>            <td><code>'&#92;u000C'</code></td>
     *     <td><code>FORM FEED</code></td></tr>
     * <tr><td><code>'\r'</code></td>            <td><code>'&#92;u000D'</code></td>
     *     <td><code>CARRIAGE RETURN</code></td></tr>
     * <tr><td><code>'&nbsp;'</code></td>  <td><code>'&#92;u0020'</code></td>
     *     <td><code>SPACE</code></td></tr>
     * </table>
     *
     * @param      ch   the character to be tested.
     * @return     <code>true</code> if the character is ISO-LATIN-1 white
     *             space; <code>false</code> otherwise.
     */
    public static boolean isWhitespace(char ch) {
        if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f') {
            return true;
        }
        return false;
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
    
    public static float parseFloat( String in ) throws NumberFormatException {
        return Float.parseFloat(in);
    }

    public static void main( String args[] ) 
    {
        try {
        String number = "-123456789.123456789";
        float value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "12389";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);

        number = "12345.6789";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "1.23456789";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "-122.25";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "+10";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "-.001234";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "-.000001234";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "0.0000000001234f";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        number = "-1.000000001234";
        value = parseFloat(number);
        System.out.println("Input: " + number + ", value: " + value);
        
        try {
            number = "-xw3fv234";
            value = parseFloat(number);
            System.out.println("Input: " + number + ", value: " + value);
        }
        catch (Throwable ex) {
            System.out.println("Input: " + number + ", value: exception");
//            ex.printStackTrace(System.out);
        }
        
        try {
            number = "-223xw3fv234";
            value = parseFloat(number);
            System.out.println("Input: " + number + ", value: " + value);
        }
        catch (Throwable ex) {
            System.out.println("Input: " + number + ", value: exception");
//            ex.printStackTrace(System.out);
        }
        try {
            number = "235565436345234124";
            value = parseFloat(number);
            System.out.println("Input: " + number + ", value: " + value);
        }
        catch (Throwable ex) {
            System.out.println("Input: " + number + ", value: exception");
//            ex.printStackTrace(System.out);
        }
        try {
            number = "++3423432&#q2";
            value = parseFloat(number);
            System.out.println("Input: " + number + ", value: " + value);
        }
        catch (Throwable ex) {
            System.out.println("Input: " + number + ", value: exception");
//            ex.printStackTrace(System.out);
        }
        
        }
        catch (Throwable ex) {
//            ex.printStackTrace(System.out);
        }
    }
}
