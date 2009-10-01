////////////////////////////////////////////////////////////
// 
// Copyright (C) 2005 TiVo Inc.
// All rights reserved.
// 
////////////////////////////////////////////////////////////

package com.tivo.core.ds;

public class TeDictException extends RuntimeException
{
    /**
     * Serializable objects need a version number.
     */
    
    public static final long serialVersionUID = 1;
   
    public TeDictException( String message )
    {
        super( message );
    }
};

