//////////////////////////////////////////////////////////////////////
// 
// File: TeURLDecoder.java
// Description: Wrapper class for URLDecoder.
// 
// Copyright (c) 2004-2005 TiVo Inc.
// 
//////////////////////////////////////////////////////////////////////

package com.tivo.core.ds;

import java.net.URLDecoder;

public class TeURLDecoder
{
    public static String decode(String s)
    {
        String decoded;
        try {
            decoded = URLDecoder.decode(s,"utf-8");
        } catch (Exception e) {
            decoded = null;
        }
        return decoded;
    }
}
