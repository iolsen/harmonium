//////////////////////////////////////////////////////////////////////
//
// File: HostingVersion.java
//
// Copyright (c) 2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.sample;


import java.io.*;

/**
 * This class contains the version information for HME.  The version is stored
 * using the "Java Product Versioning" standard.  A separate class is used
 * because the versioning calls do not work from a static method, so any Hme
 * tool that requires version information can simply create one of these
 * objects.
 *
 * This class can also be used in the future if further use of the versioning
 * standard becomes necessary, such as to check for compatibility at runtime.
 * 
 * 
 * (see <a href=http://java.sun.com/j2se/1.4.2/docs/guids/versioning/spec/versioning2.html>Java Product Versioning Spec</a>)
 */

public class HostingVersion
{

    public String getTitle()
    {
        Package p = getClass().getPackage();
        return p.getSpecificationTitle();
    }

    public String getVersion()
    {
        Package p = getClass().getPackage();
        return p.getSpecificationVersion();
    }

    public String getVendor()
    {
        Package p = getClass().getPackage();
        return p.getSpecificationVendor();
    }
    
    /**
     * returns a descriptive string identifying the version of the SDK.
     */
    public String getVersionString()
    {
        Package p = getClass().getPackage();
        return p.getSpecificationTitle() + 
            " " + p.getSpecificationVersion() +
            " (" + p.getSpecificationVendor()+")";
    }

    /**
     * @param out the printstream to print the version string to.
     */
    public void printVersion(PrintStream out)
    {
        out.println(getVersionString());
    }
}