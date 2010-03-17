////////////////////////////////////////////////////////////
// 
// Copyright (C) 2005 TiVo Inc.
// All rights reserved.
// 
////////////////////////////////////////////////////////////

package com.tivo.core.ds;


/**
 * TeDictUrl provides utilities for encoding a TeDict as a URL and for decoding a
 * TeDict from a URL.
 * 
 * XXX: So far, it only handles dictionaries that only contain string
 *      values for each name.
 *
 * XXX: you could imagine a lot more error checking!
 */
 
public class TeDictUrl
{
    public static String dictToUrlQuery( TeDict dict )
    {
        return dictToUrlQueryAux( dict, "" );
    }

    /**
     * Returns dictionary encoded as for the query part of a url.
     */
    private static String dictToUrlQueryAux( TeDict dict, String prefix )
    {
        StringBuffer resultBuf = new StringBuffer();

        TeIterator iter = dict.getNamesSortedByAlpha();
        while( iter.hasNext() )
        {
            String name   = (String) iter.next();
            int    nValue = dict.countValues( name );

            for ( int i=0; i < nValue; i++ )
            {
                if ( resultBuf.length() > 0 )
                {
                    resultBuf.append( "&" );
                }

                int type = dict.getType( name, i );

                if ( type == TeDict.STRING )
                {
                    if ( prefix.length() > 0 )
                    {
                        resultBuf.append( prefix );
                        resultBuf.append( "." );
                    }
                    resultBuf.append( name );

                    String strValue = dict.getString( name, i );
                    strValue = TeURLEncoder.encode( strValue );

                    // The TeURLEncoder class does not conform to
                    // RFC2396, which does not say that you can use
                    // "+" to mean space, so at this point we change
                    // every "+" to "%20".
                    strValue = escapePlusCharacters( strValue );

                    if ( strValue.length() > 0 )
                    {
                        resultBuf.append( "=" );
                        resultBuf.append( strValue );
                    }
                }
                else if ( type == TeDict.DICT )
                {
                    String newPrefix = "";
                    if ( prefix.length() > 0 )
                    {
                        newPrefix = prefix + ".";
                    }
                    newPrefix = newPrefix + name + "." + i;
                    TeDict subDict = dict.getDict( name, i );
                    if ( subDict.isEmpty() )
                    {
                        resultBuf.append( newPrefix );
                    }
                    else
                    {
                        resultBuf.append( dictToUrlQueryAux( subDict,
                                                             newPrefix ) );
                    }
                }
                else
                {
                    throw new RuntimeException( "unexpected type " + type );
                }
            }
        }

        return resultBuf.toString();
    }

    /**
     * Replaces all of the "+" characters in a string with "%20".
     */
    private static String escapePlusCharacters(String str) {
        //return str.replaceAll("\\+", "%20");
        StringBuffer strBuf = new StringBuffer();
        int rangeStart = 0;
        int rangeEnd = -1;
        while ( (rangeEnd = str.indexOf('+', rangeStart)) > -1) {
            strBuf.append(str.substring(rangeStart, rangeEnd));
            strBuf.append("%20");
            rangeStart = rangeEnd + 1;
        }
        strBuf.append(str.substring(rangeStart, str.length()));
        return strBuf.toString();
    }

    /**
     * Returns a new TeDict with contents decoded from the query part of a url.
     */
    public static TeDict urlQueryToDict( String query )
    {
        if ( query.length() == 0 )
        {
            return new TeDict();
        }
        
        String vParams[] = TeStringUtils.split(query, "&" );

        TeDict dict = new TeDict();
        for( int i=0; i < vParams.length; i++ )
        {
            String pair = vParams[ i ];
            String fullName;
            String value;

            int iEqual = pair.indexOf( "=" );
            if ( iEqual >= 0 )
            {
                fullName = pair.substring( 0, iEqual );
                value = pair.substring( iEqual+1 );
                value = TeURLDecoder.decode( value );
            }
            else
            {
                fullName = pair;
                value = "";
            }
            if ( fullName.length() == 0 )
            {
                throw new RuntimeException( "empty name? '" + pair + "'" );
            }

            String vNameParts[] = TeStringUtils.split(fullName, "." );
            int nParts = vNameParts.length;
            String baseName = vNameParts[ nParts - 1 ];
            if ( nParts == 1 )
            {
                dict.add( baseName, value );
            }
            else if ( nParts % 2 != 1 )
            {
                if ( value.length() == 0 )
                {
                    // if well-formed, this is an empty dict.  just make it!
                    findOrCreateDict( dict, vNameParts, 0, nParts );
                }
                else
                {
                    throw new RuntimeException( "name '" + fullName +
                                                "' -- with even # of parts " +
                                                "looks like it should be an "+
                                                "empty dict, but it has a " +
                                                "non-empty value! (" + value +
                                                ")");
                }
            }
            else
            {
                TeDict subDict = findOrCreateDict( dict, vNameParts,
                                                 0, nParts-1 );
                subDict.add( baseName, value );
            }
        }

        return dict;
        }


    private static TeDict findOrCreateDict( TeDict parentDict,
                                          String vNameParts[],
                                          int offset,
                                          int iMaxPartToUse )
    {
        if ( offset + 2 > iMaxPartToUse )
        {
            throw new RuntimeException( "internal error (offset=" + offset +
                                        " iMaxPartToUse=" +iMaxPartToUse+ ")");
        }

        String name = vNameParts[offset];
        offset++;
        int    index = Integer.parseInt( vNameParts[offset] );
        offset++;

        int nCurValues = parentDict.countValues( name );
        if ( nCurValues == index )
        {
            // need to make a dictionary.
            parentDict.add( name, new TeDict() );
            nCurValues++;
        }

        if ( nCurValues != (index+1) )
        {
            throw new RuntimeException( "out of order? '" + name + "." +
                                        index + "'" );
        }

        TeDict dict = parentDict.getDict( name, index );
        if ( offset < iMaxPartToUse )
        {
            dict = findOrCreateDict( dict, vNameParts, offset, iMaxPartToUse );
        }
        return dict;
    }
 
    public static void main(String[] args) {
        String foo = "This+is+a+String+with+plus+signs.";
        System.out.println("Before: " + foo);
        System.out.println("After : " + escapePlusCharacters(foo));

        foo = "This+is+a+String+with+plus+signs,+and+one+on+end+";
        System.out.println("Before: " + foo);
        System.out.println("After : " + escapePlusCharacters(foo));
        foo = "+Starts+with+a+plus+sign.";
        System.out.println("Before: " + foo);
        System.out.println("After : " + escapePlusCharacters(foo));
        
        foo = "Has+several+plus+signs+together:+++:here.";
        System.out.println("Before: " + foo);
        System.out.println("After : " + escapePlusCharacters(foo));
        
    }
}

