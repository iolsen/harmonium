//////////////////////////////////////////////////////////////////////
//
// File: IContext.java
//
// Copyright (c) 2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

/**
 * Hosting interface that describes the context an application will run within.
 * The hosting environment will provide an implementation of this interface and
 * it will be passed to an Application when the application is initialized.  The
 * Application can then use the context for various services such as logging, 
 * persistent storage, etc.
 * 
 * @author kgidley
 */
@SuppressWarnings("unchecked")
public interface IContext 
{
    boolean DEBUG_FLUSHES = true;

    /**
     * Returns an output stream used to send commands to the receiver.
     */
    OutputStream getOutputStream();

    /**
     * Returns an input stream used to read messages sent by the receiver.
     */
    InputStream getInputStream();

    /**
     * Closes this context.  Application calls this to indicate 
     * that it is done with the context.
     */
    void close() throws IOException;


    /**
     * Persistently stores the key/value pair for the current user of the current application.
     * The data may be retrieved the next time the current user runs the current application.
     */
    void setPersistentData(String key, String value); 

    /**
     * <p>Persistently stores the key/value pair, using the following rules:
     * <p>
     * <ol>
     * <li>If the applicationGlobal flag is true, then ALL users of the application may set and/or get the key/value pair.</li>
     * <li>The applicationId string MUST be a substring of the application's fully qualified name. E.g. if the application 
     *    is 'com.tivo.hme.games.word.Wordsmith', then 'com.tivo' & 'com.tivo.hme.games' are valid, but 'com.yahoo' is not.
     *    Setting the applicationId to 'null' indicates the fully qualified classname should be used.</li>
     * </ol>
     * 
     * <p>Examples:
     * <TABLE class=body cellSpacing=0 cellPadding=2 width="100%" bgColor=white border=1>
     * <TBODY>
     * <TR>
     * <TH>Code</TH>
     * <TH>What it does</TH>
     * </TR>
     * <TR vAlign=center>
     * <TD width="45%"><code>setPersistentData(&quot;high-score&quot;, score, null, true);</code></TD>
     * <TD>Global high score - all users of this app can see these values</TD>
     * </TR>
     * <TR>
     * <TD width="45%"><code>setPersistentData(&quot;high-score&quot;, score, null, false);</code></TD>
     * <TD>User high score - only this user of this app can see these values</TD>
     * </TR>
     * <TR>
     * <TD width="45%"><code>setPersistentData(&quot;zipcode&quot;, zipcode, "com.tivo", false);</code></TD>
     * <TD>Zipcode for this user of all apps in this domain - all applications in this domain, when run by this user, can access this info</TD>
     * </TR>
     * <TR>
     * <TD width="45%"><code>setPersistentData(&quot;todays-special&quot;, special, "com.tivo", true);</code></TD>
     * <TD>Data for all users of all apps in this domain - all applications in this domain can access this info</TD>
     * </TR>
     * </TBODY>
     * </TABLE>     
     */
    void setPersistentData(String key, String value, String applicationId, boolean applicationGlobal); 

    /**
     * Retrieves from the persistent store the value for the specified key for the current user of the current application.
     */
    String getPersistentData(String key); 

    /**
     * <p>Retrieves from the persistent store the value for the specified key, using the following rules:
     * <p>
     * <ol>
     * <li>If the applicationGlobal flag is true, then ALL users of the application may set and/or get the key/value pair.</li>
     * <li>The applicationId string MUST be a substring of the application's fully qualified name. E.g. if the application 
     *    is 'com.tivo.hme.games.word.Wordsmith', then 'com.tivo' & 'com.tivo.hme.games' are valid, but 'com.yahoo' is not.
     *    Setting the applicationId to 'null' indicates the fully qualified classname should be used.</li>
     * </ol>
     * 
     * <p>Examples:
     * <TABLE class=body cellSpacing=0 cellPadding=2 width="100%" bgColor=white border=1>
     * <TBODY>
     * <TR>
     * <TH>Code</TH>
     * <TH>What it does</TH>
     * </TR>
     * <TR vAlign=center>
     * <TD width="45%"><code>String val = getPersistentData(&quot;high-score&quot;, null, true);</code></TD>
     * <TD>Gets the global high score - all users of this app can see these values</TD>
     * </TR>
     * <TR>
     * <TD width="45%"><code>String val = getPersistentData(&quot;high-score&quot;, null, false);</code></TD>
     * <TD>Gets the users high score - only this user of this app can see these values</TD>
     * </TR>
     * <TR>
     * <TD width="45%"><code>String val = getPersistentData(&quot;zipcode&quot;, "com.tivo", false);</code></TD>
     * <TD>Gets the zipcode for this user available to all apps in this domain - only this user can access this data</TD>
     * </TR>
     * <TR>
     * <TD width="45%"><code>String val = getPersistentData(&quot;todays-special&quot;, "com.tivo", true);</code></TD>
     * <TD>Gets some domain global data - all users of all apps in this domain can access this info</TD>
     * </TR>
     * </TBODY>
     * </TABLE>     
     */
    String getPersistentData(String key, String applicationId, boolean applicationGlobal); 

    /**
     * Returns a string that uniquely identifies the receiver associated with
     * this instance of the application.
     * 
     * @return The unique identifier for the receiver.
     */
    String getReceiverGUID();

    /**
     * Looks for the value associated the specified key in the connection
     * attributes.  The connection attributes include the HTTP headers and 
     * any URL parameters used during the opening of the connection to the 
     * application.
     * 
     * @param key The particular attribute to find.
     * @return The connection attribute for the given key.
     */
    String getConnectionAttribute(String key);

    /**
     * Returns a Map containing all the connection attributes.  The Map
     * may be used to iterate over the entire set of attributes associated
     * with the connection.
     * 
     * @return A map containing all the connection attributes.
     */
    Map getConnectionAttributes();
    
    /**
     * Returns an ILogger instance the application can use to
     * write messages to the centralized log.
     *
     * @return An implementation of ILogger.
     */    
    ILogger getLogger();
    
    /**
     * Get the base URL of the application.
     *
     * @return The base URL of the application.
     */
    URL getBaseURI();

    /**
     * Get the URL for the application's assets.
     *
     * @return The URL for the application's assets.
     */
    URL getAssetURI();
    
}
