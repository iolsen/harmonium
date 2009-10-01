//////////////////////////////////////////////////////////////////////
//
// File: ILoader.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.interfaces;

/**
 * 
 * @author kgidley
 */
public interface ILoader {

    public void setFactory(IFactory factory);

    public void close();

}
