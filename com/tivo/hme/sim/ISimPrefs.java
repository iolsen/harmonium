//////////////////////////////////////////////////////////////////////
//
// File: ISimPrefs.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.io.*;
import java.util.prefs.*;

/**
 * Any simulator object that has preferences to save must implement this method
 * to ensure they are saved. The objects must also be added to the Simulator's
 * prefList by calling addPref(SimPrefs).
 */
public interface ISimPrefs 
{
    public void storePrefs(Preferences prefs) throws IOException;
}
