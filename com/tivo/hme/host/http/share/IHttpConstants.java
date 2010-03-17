//////////////////////////////////////////////////////////////////////
//
// File: IHttpConstants.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.http.share;

/**
 * HTTP constants that are shared between client and server.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public interface IHttpConstants
{
    //
    // TCP constants
    //

    int TCP_PACKET_SIZE = 1460;
    int TCP_BUFFER_SIZE = (3 * TCP_PACKET_SIZE);

    int HTTP_PORT = 80;

    String CRLF = "\r\n";
    
    //
    // HTTP versions
    //
    
    int HTTP_VERSION_10 = 10;
    int HTTP_VERSION_11 = 11;

    //
    // HTTP methods
    //
    
    String HTTP_METHOD_GET     = "GET";
    String HTTP_METHOD_POST    = "POST";
    String HTTP_METHOD_HEAD    = "HEAD";
    String HTTP_METHOD_OPTIONS = "OPTIONS";
    String HTTP_METHOD_PUT     = "PUT";
    String HTTP_METHOD_DELETE  = "DELETE";
    String HTTP_METHOD_TRACE   = "TRACE";

    //
    // HTTP status codes
    //

    int HTTP_STATUS_CONTINUE                            = 100;
    int HTTP_STATUS_SWITCHING_PROTOCOLS                 = 101;
    int HTTP_STATUS_OK                                  = 200;
    int HTTP_STATUS_CREATED                             = 201;
    int HTTP_STATUS_ACCEPTED                            = 202;
    int HTTP_STATUS_NOT_AUTHORITATIVE                   = 203; 
    int HTTP_STATUS_NO_CONTENT                          = 204;
    int HTTP_STATUS_RESET                               = 205;
    int HTTP_STATUS_PARTIAL                             = 206;
    int HTTP_STATUS_MULT_CHOICE                         = 300;
    int HTTP_STATUS_MOVED_PERM                          = 301;
    int HTTP_STATUS_MOVED_TEMP                          = 302;
    int HTTP_STATUS_SEE_OTHER                           = 303;
    int HTTP_STATUS_NOT_MODIFIED                        = 304;
    int HTTP_STATUS_USE_PROXY                           = 305;
    int HTTP_STATUS_SWITCH_PROXY                        = 306;
    int HTTP_STATUS_TEMPORARY_REDIRECT                  = 307;
    int HTTP_STATUS_BAD_REQUEST                         = 400;
    int HTTP_STATUS_UNAUTHORIZED                        = 401;
    int HTTP_STATUS_PAYMENT_REQUIRED                    = 402;
    int HTTP_STATUS_FORBIDDEN                           = 403;
    int HTTP_STATUS_NOT_FOUND                           = 404;
    int HTTP_STATUS_BAD_METHOD                          = 405;
    int HTTP_STATUS_NOT_ACCEPTABLE                      = 406;
    int HTTP_STATUS_PROXY_AUTH                          = 407;
    int HTTP_STATUS_CLIENT_TIMEOUT                      = 408;
    int HTTP_STATUS_CONFLICT                            = 409;
    int HTTP_STATUS_GONE                                = 410;
    int HTTP_STATUS_LENGTH_REQUIRED                     = 411;
    int HTTP_STATUS_PRECON_FAILED                       = 412;
    int HTTP_STATUS_ENTITY_TOO_LARGE                    = 413;
    int HTTP_STATUS_REQ_TOO_LONG                        = 414;
    int HTTP_STATUS_UNSUPPORTED_TYPE                    = 415;
    int HTTP_STATUS_REQUESTED_RANGE_NOT_SATISFIABLE     = 416;
    int HTTP_STATUS_EXPECTATION_FAILED                  = 417;
    int HTTP_STATUS_INTERNAL_ERROR                      = 500;
    int HTTP_STATUS_NOT_IMPLEMENTED                     = 501;
    int HTTP_STATUS_BAD_GATEWAY                         = 502;
    int HTTP_STATUS_UNAVAILABLE                         = 503;
    int HTTP_STATUS_GATEWAY_TIMEOUT                     = 504;
    int HTTP_STATUS_VERSION                             = 505;
}
