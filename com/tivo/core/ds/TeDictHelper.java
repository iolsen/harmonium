package com.tivo.core.ds;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.tivo.core.ds.TeIterator;

public class TeDictHelper {

    public static String dictToString(TeDict dict) {
        return dictToString(dict, 0);
    }

    public static String dictToString(TeDict dict, int level) {
        StringBuffer indent = new StringBuffer(level*2);
        for (int j=0; j < level*2; j++) {
            indent.append(' ');
        }
        StringBuffer outStrBuf = new StringBuffer();
        for (TeIterator iter = dict.getNamesSortedByAlpha(); iter.hasNext();) {
            String key = (String) iter.next();
            int count = dict.countValues(key);
            for(int i=0; i<count; i++) {
                int type = dict.getType(key, i);
                if (type == TeDict.STRING) {
                    outStrBuf.append(indent).append(key).append("[").append(i).append("]: ").append(dict.getString(key, i)).append('\n');
                } else {
                    outStrBuf.append(indent).append(key).append("[").append(i).append("]: (TeDict)\n");
                    outStrBuf.append(dictToString(dict.getDict(key, i), level+1));
                }
            }
        }
        return outStrBuf.toString();
    }

    public static long sqlStringToLong( String dateString )
    {
        return sqlStringToDate(dateString).getTime();
    }

    public static Date sqlStringToDate( String dateString )
    {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(new Date(0));
        cal.set( Calendar.MILLISECOND, 0 );

        // assuming HH is 24 hour time...
        //final String sqlDateTimeFormatG = "yyyy-MM-dd HH:mm:ss";
        
        try {
//            int year  = getIntPart( dateString.substring(0, 4), 1970 );
//            int month = getIntPart( dateString.substring(6, 2), 1 );
//            int date  = getIntPart( dateString.substring(9, 2), 1 );
//            int hours = getIntPart( dateString.substring(12, 2), 0 );
//            int mins  = getIntPart( dateString.substring(15, 2), 0 );
//            int secs  = getIntPart( dateString.substring(18, 2), 0 );
//            
//            //cal.set(year, month-1, date, hours, mins, secs);
//            cal.set(Calendar.YEAR, year);
//            cal.set(Calendar.MONTH, month-1);
//            cal.set(Calendar.DATE, date);
//            cal.set(Calendar.HOUR_OF_DAY, hours);
//            cal.set(Calendar.MINUTE, mins);
//            cal.set(Calendar.SECOND, secs);
            
            return parseDate(dateString, cal);
        } catch ( Throwable e ) { 
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Date parseDate( String str, Calendar cal )
    {
        // assuming HH is 24 hour time...
        //final String sqlDateTimeFormatG = "yyyy-MM-dd HH:mm:ss";
        int strLen = str.length();
        if ( str != null && strLen > 0 )
        {
            // find next integer part - the year
            int endIdx = 0;
            while (endIdx < strLen && Character.isDigit(str.charAt(endIdx))) {
                endIdx++;
            }
            int year = -1;
            if (endIdx > 0) {
                try {
                    year = getIntPart(str.substring(0, endIdx), 1970);
                    cal.set(Calendar.YEAR, year);
                } catch (NumberFormatException ex) {
                    year = -1;
                }
            } 
            
            if (year == -1) {
                // bad input - return default date
                throw new RuntimeException("Invalid format for Date string:" + str);
//                cal.set(Calendar.YEAR, 1970);
//                return cal.getTime();
            }
            if (endIdx == strLen) {
                return cal.getTime();
            }
            
            // skip to next digit
            int startIdx=endIdx;
            while (startIdx < strLen && !Character.isDigit(str.charAt(startIdx))) {
                startIdx++;
            }

            if (startIdx > endIdx && startIdx == strLen) {
                // bad data - trailing chars
                throw new RuntimeException("Invalid format for Date string:" + str);
                //return new Date(0);
            }

            // find next integer part - the month
            endIdx = startIdx;
            while (endIdx < strLen && Character.isDigit(str.charAt(endIdx))) {
                endIdx++;
            }
            int month = -1;
            if (endIdx-startIdx > 0) {
                try {
                    month = getIntPart(str.substring(startIdx, endIdx), 1);
                    cal.set(Calendar.MONTH, month-1);
                } catch (NumberFormatException ex) {
                    month = -1;
                }
            } else {
                // at end of string
            }
            if (endIdx == strLen) {
                // end of string, try to use what we have
                return cal.getTime();
            }
            
            // skip to next digit
            startIdx=endIdx;
            while (startIdx < strLen && !Character.isDigit(str.charAt(startIdx))) {
                startIdx++;
            }

            if (startIdx > endIdx && startIdx == strLen) {
                // bad data - trailing chars
                throw new RuntimeException("Invalid format for Date string:" + str);
                //return new Date(0);
            }

            // find next integer part - the day
            endIdx = startIdx;
            while (endIdx < strLen && Character.isDigit(str.charAt(endIdx))) {
                endIdx++;
            }
            int day = -1;
            if (endIdx-startIdx > 0) {
                try {
                    day = getIntPart(str.substring(startIdx, endIdx), 1);
                    cal.set(Calendar.DATE, day);
                } catch (NumberFormatException ex) {
                    day = -1;
                }
            }
            if (endIdx == strLen) {
                // end of string, try to use what we have
                return cal.getTime();
            }

            // skip to next digit
            startIdx=endIdx;
            while (startIdx < strLen && !Character.isDigit(str.charAt(startIdx))) {
                startIdx++;
            }

            if (startIdx > endIdx && startIdx == strLen) {
                // bad data - trailing chars
                throw new RuntimeException("Invalid format for Date string:" + str);
                //return new Date(0);
            }

            // find next integer part - the hour
            endIdx = startIdx;
            while (endIdx < strLen && Character.isDigit(str.charAt(endIdx))) {
                endIdx++;
            }
            int hour = -1;
            if (endIdx-startIdx > 0) {
                try {
                    hour = getIntPart(str.substring(startIdx, endIdx), 0);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                } catch (NumberFormatException ex) {
                    hour = -1;
                }
            }
            if (endIdx == strLen) {
                // end of string, try to use what we have
                return cal.getTime();
            }
        
            // skip to next digit
            startIdx=endIdx;
            while (startIdx < strLen && !Character.isDigit(str.charAt(startIdx))) {
                startIdx++;
            }

            if (startIdx > endIdx && startIdx == strLen) {
                // bad data - trailing chars
                throw new RuntimeException("Invalid format for Date string:" + str);
                //return new Date(0);
            }

            // find next integer part - the minute
            endIdx = startIdx;
            while (endIdx < strLen && Character.isDigit(str.charAt(endIdx))) {
                endIdx++;
            }
            int minute = -1;
            if (endIdx-startIdx > 0) {
                try {
                    minute = getIntPart(str.substring(startIdx, endIdx), 0);
                    cal.set(Calendar.MINUTE, minute);
                } catch (NumberFormatException ex) {
                    minute = -1;
                }
            }
            if (endIdx == strLen) {
                // end of string, try to use what we have
                return cal.getTime();
            }

            // skip to next digit
            startIdx=endIdx;
            while (startIdx < strLen && !Character.isDigit(str.charAt(startIdx))) {
                startIdx++;
            }

            if (startIdx > endIdx && startIdx == strLen) {
                // bad data - trailing chars
                throw new RuntimeException("Invalid format for Date string:" + str);
                //return new Date(0);
            }

            // find next integer part - the minute
            endIdx = startIdx;
            while (endIdx < strLen && Character.isDigit(str.charAt(endIdx))) {
                endIdx++;
            }
            int second = -1;
            if (endIdx-startIdx > 0) {
                try {
                    second = getIntPart(str.substring(startIdx, endIdx), 0);
                    cal.set(Calendar.SECOND, second);
                } catch (NumberFormatException ex) {
                    second = -1;
                }
            }
            
            // anthing left over?  its bad!
            if (endIdx != strLen) {
                throw new RuntimeException("Invalid format for Date string:" + str);
                //return new Date(0);
            }
        }

        return cal.getTime();
    }

    private static int getIntPart( String str, int defValue )
        throws NumberFormatException
    {
        
        if ( str != null && str.length() > 0 )
        {
            return Integer.parseInt( str );
        }
        else
        {
            return defValue;
        }
    }
}
