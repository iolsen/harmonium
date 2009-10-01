//////////////////////////////////////////////////////////////////////
// 
// File: TeURLEncoder.java
// Description: Wrapper class for URLEncoder
// 
// Copyright (c) 2004 TiVo Inc.
// 
//////////////////////////////////////////////////////////////////////

package com.tivo.core.ds;

import java.net.URLEncoder;

public class TeURLEncoder
{
    public static String encode(String s)
    {
        String encoded;
        try {
            encoded = URLEncoder.encode(s,"utf-8");
        } catch (Exception e) {
            encoded = null;
        }
        return encoded;
    }
}
