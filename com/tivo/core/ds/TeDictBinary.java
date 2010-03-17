//////////////////////////////////////////////////////////////////////
// 
// File: TeDictBinary.java
// 
// Description: 
// 
// Copyright (C) 2005 by TiVo Inc.
// 
//////////////////////////////////////////////////////////////////////

package com.tivo.core.ds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class TeDictBinary
{
    static final byte LAST_VALUE_TYPE = 0;
    static final byte STRING_TYPE     = 1;
    static final byte DICT_TYPE       = 2;

    /**
     * Return dict read in from InputStream
     */
    public static TeDict binaryToDict( InputStream in ) throws IOException
    {
        TeDictOrderChecker dict = new TeDictOrderChecker();
        while ( true )
        {
            String key = TeDictBinaryHelper.readUTF8String(in);

            if ( key.length() == 0 )
            {
                break;
            }
            
            while ( true )
            {
                byte type = TeDictBinaryHelper.readByte(in);
                if ( type == STRING_TYPE )
                {
                    // string value
                    String strValue = TeDictBinaryHelper.readUTF8String(in);
                    dict.add(key, strValue);
                }
                else if ( type == DICT_TYPE )
                {
                    // dict value (recursive)
                    TeDict newDict = binaryToDict( in );
                    dict.add(key, newDict);
                }
                else if ( type == LAST_VALUE_TYPE )
                {
                    break;
                }
                else
                {
                    throw new IOException( "unexpected type " + type );
                }
            }
        }
        return dict.getDict();
    }

    /**
     * Helper for writing a map to an OutputStream
     */
    public static void dictToBinary( OutputStream out, TeDict dict )
        throws IOException
    {
        TeIterator iter = dict.getNamesSortedByAlpha();
        while ( iter.hasNext() )
        {
            String key = (String) iter.next();
            TeDictBinaryHelper.writeUTF8String( out, key );
            int nValue = dict.countValues(key);
            for (int j = 0; j < nValue ; j++)
            {
                if ( dict.getType( key, j ) == TeDict.STRING )
                {
                    TeDictBinaryHelper.writeByte( out, STRING_TYPE );
                    TeDictBinaryHelper.writeUTF8String( out, dict.getString( key, j ) );
                }
                else {
                    TeDictBinaryHelper.writeByte( out, DICT_TYPE );
                    dictToBinary( out, dict.getDict( key, j ) );
                }
            }
            TeDictBinaryHelper.writeByte( out, LAST_VALUE_TYPE );
        }

        TeDictBinaryHelper.writeUTF8String( out, "" );  // empty key is last entry
    }
}
