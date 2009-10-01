//////////////////////////////////////////////////////////////////////
//
// File: IHmeConstants.java
//
// Copyright (c) 2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.interfaces;

/**
 * Various constants used by HME. 
 * 
 * @author kgidley
 */
public interface IHmeConstants {

    String MDNS_TYPE                    = "_tivo-hme._tcp.local.";
    String MDNS_DNSSD_TYPE              = "_tivo-hme._tcp";
    String MIME_TYPE                    = "application/x-hme";
    String TIVO_DURATION                = "X-TiVo-Accurate-Duration";

    //
    // TCP constants
    //
    int TCP_PACKET_SIZE = 1460;
    int TCP_BUFFER_SIZE = (3 * TCP_PACKET_SIZE);

}
