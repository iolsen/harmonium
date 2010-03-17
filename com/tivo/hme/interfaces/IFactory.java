//////////////////////////////////////////////////////////////////////
//
// File: IFactory.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Interface for application factories.
 */
@SuppressWarnings("unchecked")
public interface IFactory
{
    void initFactory(String appClassName, ClassLoader loader, IArgumentList args);
    void destroyFactory();
    boolean isActive();
    void setActive(boolean active);
    int getActiveCount();    
    
    String getAppName();
    void setAppName(String appName);
    String getAppTitle();
    void setAppTitle(String title);

    URL getAssetURI();
    void setAssetURI(URL assetURL);
    
    void setListener(IListener listener);
    
    IApplication createApplication(IContext ctx) throws IOException;
    void addApplication(IApplication app);
    void removeApplication(IApplication app);

    InputStream fetchAsset(IHttpRequest req);
//    InputStream getStream(String uri) throws IOException;
    

	public final String HME_VERSION_TAG = "version";
	public final String HME_APPLICATION_CLASSNAME = "applicationClassName";
	public final Object HME_DEBUG_KEY = "debug";

	Map getFactoryData();

}
