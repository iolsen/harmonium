////////////////////////////////////////////////////////////
// 
// Copyright (C) 2005 TiVo Inc.
// All rights reserved.
// 
////////////////////////////////////////////////////////////

package com.tivo.core.ds;


import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

@SuppressWarnings("unchecked")
public class TeDict
{
    /**
     * Version number for serializable object.
     */
	
    public static final long serialVersionUID = 1;
	
    /**
     * Creates an empty dictionary.
     */
    public TeDict()
    {
        mMap = new Hashtable();
    }

    /**
     * Constructs a dictionary that is empty, except that the type
     * field is set.
     */
    public TeDict(String type)
    {
        mMap = new Hashtable();
        add("type", type);
    }

    /**
     * Returns true iff other is a TeDict and
     *
     *   this.get( name, index ).equals( other.get( name, index ) )
     *
     * for each name/index pair in this dict and other contains no other
     * values.
     */
    public boolean equals( Object other )
    {
        if ( ! ( other instanceof TeDict ) )
        {
            return false;
        }
        
        TeDict otherDict = (TeDict) other;
        
        TeIterator myNames = getNamesSortedByAlpha();
        TeIterator otherNames = otherDict.getNamesSortedByAlpha();
        
        while ( myNames.hasNext() )
        {
            if ( ! otherNames.hasNext() )
            {
                return false;
            }
            
            String name = (String) myNames.next();
            String otherName = (String) otherNames.next();
            
            if ( ! name.equals( otherName ) )
            {
                return false;
            }
        
            if ( countValues(name) != otherDict.countValues(name) )
            {
                return false;
            }
            
            for ( int i = 0;  i < countValues(name);  i++ )
            {
                if ( ! get(name,i).equals( otherDict.get(name,i) ) )
                {
                    return false;
                }
            }
        }
        
        if ( otherNames.hasNext() )
        {
            return false;
        }
        
        return true;
    }

    public int hashCode()
    {
        return mMap.hashCode();
    }
    
    // not calling it clone() because clone can throw CloneNotSupportedException!
    public Object deepCopy() {
        TeDict twin = new TeDict();
        
        TeIterator keyIter = getNames();
        while ( keyIter.hasNext() ) {
            String name = (String) keyIter.next();
            
            int nValue = countValues( name );
            for( int i=0; i < nValue; i++ ) {
                int type = getType( name, i );
                if ( type == STRING ) {
                    twin.add( name, getString( name, i ) );
                }
                else {
                    twin.add( name, getDict(name,i).deepCopy() );
                }
            }
        }
        
        return twin;
    }

    /**
     * Returns the number of values associated with name.
     */
    public int countValues( String name )
    {
        Vector array = (Vector) mMap.get( name );
        return (array == null) ? 0 : array.size();
    }

    /**
     * Returns true iff no names are bound in this TeDict.
     */
    public boolean isEmpty()
    {
        return mMap.isEmpty();
    }

    /**
     * Returns name[index].  If there's no such value or it's not a string,
     * throws TeDictException.
     */
    public String getString( String name, int index )
    {
        Object value = getValue( name, index );
        try
        {
            return (String) value;
        }
        catch ( ClassCastException e )
        {
            throw new TeDictException(
                "tried to get string, but was really a '" +
                value.getClass().getName() );
        }
    }

    /**
     * Returns the name[index] or defValue if there's no such value.
     * Throws TeDictException if the value is there, but it's not a string.
     */
    public String getStringOrDefault( String name,
                                      int index,
                                      String defValue )
    {
        Object value = getValueOrNull( name, index );

        if ( value == null )
        {
            return defValue;
        }
        
        try
        {
            return (String) value;
        }
        catch ( ClassCastException e )
        {
            throw new TeDictException(
                "tried to get string, but was really a '" +
                value.getClass().getName() );
        }
    }

    /**
     * Returns name[index] or throws TeDictException if there's no such value or
     * if it's not a TeDict.
     */
    public TeDict getDict( String name, int index )
    {
        Object value = getValue( name, index );
        try
        {
            return (TeDict) value;
        }
        catch ( ClassCastException e )
        {
            throw new TeDictException(
                "tried to get dict, but was really a '" +
                value.getClass().getName() + "'" );
        }
    }

    /**
     * Returns name[index] parsed as an int.  If the index is bad, throws
     * TeDictException.  If the value can't be parsed as an int, returns 0.
     */
    public int getInt( String name, int index )
    {
        Object value = getValue( name, index );
        try
        {
            return Integer.parseInt( (String) value );
        }
        catch ( Throwable e )
        {
            return 0;
        }
    }

    /**
     * Returns name[index] parsed as an int.  If there's no such value or if it
     * can't be parsed as an int, this returns defValue.
     */
    public int getIntOrDefault( String name, int index, int defValue )
    {
        Object value = getValueOrNull( name, index );

        if ( value == null )
        {
            return defValue;
        }
        
        try
        {
            return Integer.parseInt( (String) value );
        }
        catch ( Throwable e )
        {
            return defValue;
        }
    }

//    /**
//     * Returns name[index] parsed as a double.  If there's no such value or if
//     * it can't be parsed as an int, this returns 0.
//     */
//    public double getDouble( String name, int index )
//    {
//        Object value = getValue( name, index );
//        try
//        {
//            return Double.parseDouble( (String) value );
//        }
//        catch ( Throwable e )
//        {
//            return 0;
//        }
//    }
//
//    /**
//     * Returns name[index] parsed as a double.  If there's no such value or if
//     * it can't be parsed as a double, this returns defValue.
//     */
//    public double getDoubleOrDefault( String name,
//                                      int index,
//                                      double defValue )
//    {
//        Object value = getValueOrNull( name, index );
//
//        if ( value == null )
//        {
//            return defValue;
//        }
//        
//        try
//        {
//            return Double.parseDouble( (String) value );
//        }
//        catch ( Throwable e )
//        {
//            return defValue;
//        }
//    }

    /**
     * Returns name[index] parsed as a time.  If there's no such value or if it
     * can't be parsed as a time, this returns the unix epoch.
     */
    public long getTime( String name, int index )
    {
        String strTime = this.getString( name, index );

	try {
	    return TeDictHelper.sqlStringToLong(strTime);
	} catch ( Throwable e ) {
	    return 0;
	}
    }

    /**
     * Returns name[index] parsed as a time.  If there's no such value or if it
     * can't be parsed as a time, this returns defValue.
     */
    public long getTimeOrDefault( String name, int index, long defValue )
    {
        Object value = getValueOrNull( name, index );

        if ( value == null )
        {
            return defValue;
        }

        try {
	    return TeDictHelper.sqlStringToLong( (String) value );
        } catch ( Throwable e ) {
            return defValue;
        }
    }

    /**
     * Returns name[index] parsed as a duration.  If the index is bad, throws
     * TeDictException.  If the value can't be parsed as a duration, returns 0
     * seconds.
     * XXX: use real duration type?
     */
    public int getDuration( String name, int index )
    {
        return getInt( name, index );
    }

    /**
     * Returns name[index] parsed as a duration.  If there's no such value or
     * if it can't be parsed as a duration, this returns defValue.
     * XXX: use real duration type?
     */
    public int getDurationOrDefault( String name, int index, int defValue )
    {
        return getIntOrDefault( name, index, defValue );
    }

    /**
     * Returns name[index] parsed as a boolean.  If the index is bad, throws
     * TeDictException.  If the value is anything other than "true", returns
     * false.
     */
    public boolean getBoolean( String name, int index )
    {
        String str = getString( name, index );
        return str.equals( "true" );
    }

    /**
     * Returns name[index] parsed as a boolean.  If there's no such value or if
     * it can't be parsed as a duration, this returns defValue.
     */
    public boolean getBooleanOrDefault( String name, int index,
                                        boolean defValue )
    {
        Object value = getValueOrNull( name, index );

        if ( value == null )
        {
            return defValue;
        }

        String str = (String) value;
        if ( str.equals( "true" ) )
            return true;
        else if ( str.equals( "false" ) )
            return false;
        else
            return defValue;
    }

    /**
     * Add the given value to the given name.
     */
    public void add( String name, String value )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            array = new Vector();
            mMap.put( name, array );
        }

        array.addElement( value );
    }

    /**
     * Add the given value to the given name.
     */

    public void add( String name, long value )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            array = new Vector();
            mMap.put( name, array );
        }

        array.addElement( Long.toString( value ) );
    }

    /**
     * Add the given value to the given name.
     */
//    public void add( String name, double value )
//    {
//        Vector array = (Vector) mMap.get( name );
//
//        if ( array == null )
//        {
//            array = new Vector();
//            mMap.put( name, array );
//        }
//
//        array.add( Double.toString( value ) );
//    }

    /**
     * Add the given value to the given name.
     */
    public void add( String name, int value )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            array = new Vector();
            mMap.put( name, array );
        }

        array.addElement( Integer.toString( value ) );
    }

    /**
     * Add the given value to the given name.
     */
    public void add( String name, boolean value )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            array = new Vector();
            mMap.put( name, array );
        }

        array.addElement( value ? "true" : "false" );
    }

    /**
     * Add the given value to the given name.
     */
    public void add( String name, TeDict value )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            array = new Vector();
            mMap.put( name, array );
        }

        array.addElement( value );
    }

    /**
     * Add the given value to the given name.
     */
    public void addTime( String name, long time )
    {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(tz);
	
        cal.setTime( new Date(time) );

        String str = ( "" +
                       cal.get(Calendar.YEAR) +
                       "-" +
                       (cal.get(Calendar.MONTH) + 1) + 
                       "-" +
                       cal.get(Calendar.DATE) + 
                       "-" +
		       cal.get(Calendar.HOUR_OF_DAY) + 
                       "-" +
                       cal.get(Calendar.MINUTE) +
                       "-" +
                       cal.get(Calendar.SECOND) );
        this.add( name, str );
    }

    /**
     * Add the given value to the given name.
     * XXX: use real duration type?
     */
    public void addDuration( String name, int nSec )
    {
        this.add( name, nSec );
    }

    /**
     * Add the given value to the given name.
     */
    public void add( String name, Object value )
    {
        if ( (! (value instanceof String) ) &&
             (! (value instanceof TeDict) ) )
        {
            throw new TeDictException( "unexpected type " +
                                        value.getClass().getName() );
        }
        
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            array = new Vector();
            mMap.put( name, array );
        }
        // XXX: if array already exists, check array[0] is a string?

        array.addElement( value );
    }

    /**
     * Returns an iterator over the set of names with values.  Note that you
     * should *not* modify the TeDict while using the iterator.
     */
    public TeIterator getNames()
    {
        return new TeIterator(mMap.keys());
    }

    /**
     * Returns an iterator over the set of names with values, sorted
     * alphabetically.  Note that you should *not* modify the TeDict while using
     * the iterator.
     */
    public TeIterator getNamesSortedByAlpha()
    {
        String[] keys = new String[mMap.size()];
        int i=0;
        for (TeIterator iterator = getNames(); iterator.hasNext();) {
            keys[i++] = (String) iterator.next();
        }

        TeArrays.sort(keys);
        Vector sortedKeys = new Vector();
        for (int j = 0; j < keys.length; j++) {
            sortedKeys.addElement(keys[j]);
        }

        return new TeIterator(sortedKeys.elements());
    }

    /**
     * Empties the entire dictionary.
     */
    public void removeAll()
    {
        mMap.clear();
    }
    
    /**
     * Removes all values for the given name.
     */
    public void remove( String name )
    {
        mMap.remove( name );
    }

    /**
     * Returns a human-readable form of this TeDict.
     */
    public String toString()
    {
        return TeDictHelper.dictToString( this );
    }
    
    /**
     * Returns the index-th value of name or throws a TeDictException if there
     * isn't such a value.  The result will either be a String or a TeDict.
     */
    public Object getValue( String name, int index )
    {
        Object value = getValueOrNull( name, index );

        if ( value == null )
        {
            throw new TeDictException( name + "[" + index +
                                        "] doesn't exist!" );
        }

        return value;
    }

    /**
     * Returns the value of name[index] or throws a TeDictException if there's no
     * such value.
     */
    Object get( String name, int index )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            throw new TeDictException( "no values for name '" + name + "'" );
        }

        return array.elementAt( index );
    }

    static public final int STRING = 1;
    static public final int DICT   = 2;

    /**
     * Returns the class for name[index] or throws a TeDictException.
     * Will be either STRING or DICT.
     */
    public int getType( String name, int index )
    {
        try
        {
            Vector array = (Vector) mMap.get( name );
            Object value = array.elementAt( index );
            if ( value == null ) {
                throw new TeDictException( "there is no value " + name +
                                         "[" + index + "]" );
            }
            return value.getClass() == String.class ? STRING : DICT;
        }
        catch ( Throwable t )
        {
            throw new TeDictException( t.toString() );
        }
    }

    /**
     * Returns the number of names with values.
     */
    int countNames()
    {
        return mMap.size();
    }
    
    /**
     * Returns the type of the index-th value of name.
     */
    Object getValueOrNull( String name, int index )
    {
        Vector array = (Vector) mMap.get( name );

        if ( array == null )
        {
            return null;
        }

        if ( index < 0 || index >= array.size() )
        {
            return null;
        }

        Object value = array.elementAt( index );
        return value;
    }

    // mMap maps from a String to an Vector
    private Hashtable mMap;
};
