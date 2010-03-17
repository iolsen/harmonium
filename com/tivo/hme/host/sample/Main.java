//////////////////////////////////////////////////////////////////////
//
// File: Main.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.host.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import com.tivo.hme.host.io.FastInputStream;
import com.tivo.hme.host.util.ArgumentList;
import com.tivo.hme.host.util.Config;
import com.tivo.hme.host.util.Misc;
import com.tivo.hme.interfaces.IArgumentList;
import com.tivo.hme.interfaces.IFactory;
import com.tivo.hme.interfaces.IHmeConstants;
import com.tivo.hme.interfaces.ILogger;

/**
 * Main SDK class to launch one or more factories.
 *
 * @author      Jonathan Payne
 */
@SuppressWarnings("unchecked")
public class Main implements ILogger
{
    final static int DEFAULT_PORT = 7288;
    public final static String DNSSD_KEY = "dnssd";
    
    protected Config config;
    protected Listener listener;
    protected List factories = new ArrayList();
    protected JmDNS rv[];

    public Main(ArgumentList args) throws IOException
    {
        this(args, true);
    }
    
    public Main(ArgumentList args, boolean start) throws IOException
    {
        //
        // build config
        //

        config = new Config();
        config.put("listener.debug", "" + args.getBoolean("-d"));

        int port = args.getInt("--port", DEFAULT_PORT);
        config.put("http.ports", "" + port);

        //
        // determine list of interfaces
        //

        String interfaceList = "";

        String nomdns = args.getValue("--nomdns", null);
        if (nomdns != null) {
            // turn off mdns, bind to whatever the user specified
            interfaceList += "," + nomdns;
        } else {
            String intf = args.getValue("--intf", null);
            if (intf != null) {
                do {
                    if (isIPAddress(intf)) {
                        interfaceList += "," + intf;
                    } else {
                        // network interface name?
                        NetworkInterface ni = NetworkInterface.getByName(intf);
                        if (ni == null) {
                            System.out.println("\"" + intf + "\" is not a valid ipv4, ipv6, or network interface name. The\nnetwork interfaces on this machine are:");
                            printNetworkInterfaces();
                            throw new IOException("network interface not found: " + intf);
                        }
                        for (Enumeration e = ni.getInetAddresses() ; e.hasMoreElements() ; ) {
                            InetAddress addr = (InetAddress)e.nextElement();
                            interfaceList += "," + addr.getHostAddress();
                        }
                    }
                    intf = args.getValue("--intf", null);
                } while (intf != null);
            } else {
                // add at most one regular, and one linklocal interface
                boolean regularIntf = false;
                boolean linklocalIntf = false;
                
                InetAddress addrs[] = Misc.getInterfaces();
                for (int i = 0 ; i < addrs.length ; i++) {
                    InetAddress addr = addrs[i];
                    String str = addr.getHostAddress();
                    if (str.equals("127.0.0.1")) {
                        continue;
                    }
                    if (str.startsWith("169.254.")) {
                        if (!linklocalIntf) {
                            linklocalIntf = true;
                            interfaceList += "," + str;
                        }
                    } else if (!regularIntf) {
                        regularIntf = true;
                        interfaceList += "," + str;
                    }
                }
            }
        }
        if ("true".equals(System.getProperty("hme.loopback"))) {
            interfaceList += "," + "127.0.0.1";
        }
        config.put("http.interfaces", interfaceList);

        //
        // determine list of factories
        //

        String launcher = args.getValue("--launcher", null);
        String jardir = args.getValue("--jars", null);
        String jarfile = args.getValue("--jar", null);
        try {
            // load the factories
            if (launcher != null) {
                loadLaunchFile(launcher);
            } else if (jardir != null) {
                loadJarFiles(jardir);
            } else if (jarfile != null) {
                loadJarFile(new File(jarfile));
            } else if (start) {
                createFactory(args, ClassLoader.getSystemClassLoader());
            }

            // bail if we didn't get any
            if (start && factories.size() == 0) {
                System.out.println("Failed to instantiate any HME apps");
                return;
            }

            //
            // start the listener
            //
            
            try {
                listener = new Listener(config, this);
                Listener.DEBUG_FLUSHES = true;
            } catch (BindException e) {
                if (port == DEFAULT_PORT) {
                    // hm - default port failed, try a random one
                    config.put("http.ports", "0");
                    listener = new Listener(config, this);                
                } else {
                    throw e;
                }
            }
        
            //
            // get ready for JmDNS
            //
            
            if (nomdns == null) {
                String interfaces[] = listener.getInterfaces();
                rv = new JmDNS[interfaces.length];
                for (int i = 0; i < interfaces.length; ++i) {
                    rv[i] = new JmDNS(InetAddress.getByName(interfaces[i]));
                }
            }
            
            //
            // now start the factories
            //
            
            listener.setFactories(factories);
            for (Iterator i = factories.iterator(); i.hasNext(); ) {
                IFactory factory = (IFactory)i.next();
                register(factory);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error: " + e.getMessage());
            usage();
        }
    }

    private void usage()
    {
        System.out.println("usage: Main [options] class");
        System.out.println();
        System.out.println("Options:");
        System.out.println(" --port <port>         listen on a specific port");
        System.out.println(" --intf <interface>    listen on a specific interface");
        System.out.println(" --nomdns <interface>  listen on a specific interface, without mdns");
        System.out.println(" --launcher <file>     start factories listed in file");
        System.out.println(" --jars <dir>          scan directory for HME app jar files");
        System.out.println(" --jar <jarfile>       start factory for the given jar");
        System.exit(1);
    }

    //
    // helpers for building list of interfaces
    //

    static boolean isIPAddress(String s)
    {
        return isIPv4Address(s) || isIPv6Address(s);
    }

    static boolean isIPv4Address(String s)
    {
        int count = 0;
        for (StringTokenizer tokens = new StringTokenizer(s, ".") ; tokens.hasMoreTokens() ; ) {
            try {
                Integer.parseInt(tokens.nextToken());
            } catch (NumberFormatException e) {
                return false;
            }
            ++count;
        }
        return (count == 4);
    }

    // REMIND : this isn't 100% accurate
    static boolean isIPv6Address(String s)
    {
        if (s.indexOf(':') == -1) {
            return false;
        }
        for (StringTokenizer tokens = new StringTokenizer(s, ":") ; tokens.hasMoreTokens() ; ) {
            try {
                Integer.parseInt(tokens.nextToken(), 16);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    void printNetworkInterfaces() throws IOException
    {
        //
        // this only works in JDK 1.4
        //
        for (Enumeration e = NetworkInterface.getNetworkInterfaces() ; e.hasMoreElements() ; ) {
            NetworkInterface ni = (NetworkInterface)e.nextElement();
            System.out.print("  " + ni.getName());
            for (Enumeration e2 = ni.getInetAddresses() ; e2.hasMoreElements() ; ) {
                InetAddress addr = (InetAddress)e2.nextElement();
                System.out.print(" " + addr.getHostAddress());
            }
            System.out.println();
        }
    }

    //
    // jar file loading
    //

    private StringBuffer addArg(StringBuffer buf, String name, String value)
    {
        if (value != null) {
            if (name != null) {
                buf.append(' ').append(name);
            }
            buf.append(' ').append(value);
        }
        return buf;
    }

    /**
     * Scans a directory for jar files and tries to instantiate each as a HME
     * app.  Jar files must contain the following attributes:
     *
     * HME-Class:       the name of the HME app class
     * HME-Arguments:   arguments to be passed to the app factory's init method
     *
     * @see loadJarFile(File)
     * @param jardir full path to the jars directory.
     */
    private void loadJarFiles(String jardir) throws IOException
    {
        File dir = new File(jardir);
        String list[] = dir.list();
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.length; i++) {
            if (!list[i].endsWith(".jar")) {
                continue;
            }
            File jarfile = new File(dir, list[i]);
            loadJarFile(jarfile);
        }
    }

    /**
     * Tries to instantiate an HME app for the given jar file.  The jar file
     * must contain the following attributes:
     *
     * HME-Class:       the name of the HME app class
     * HME-Arguments:   arguments to be passed to the app factory's init method
     *
     * Bad jar files are ignored, and warnings are logged.
     *
     * @see loadJarFiles(String)
     * @param jarFile a jar file
     */
    private void loadJarFile(File jarFile) throws IOException
    {
        try {
            JarFile jf = new JarFile(jarFile, true);
            Manifest manifest = jf.getManifest();
            Attributes attrs = manifest.getMainAttributes();
            StringBuffer args = new StringBuffer(64);

            // check for factory, class and arguments
            addArg(args, "--class", attrs.getValue("HME-Class"));
            addArg(args, null, attrs.getValue("HME-Arguments"));
            createFactory(new ArgumentList(args.toString()),
                          new JarClassLoader(jf, this, null));
        } catch (Exception e) {
            log(ILogger.LOG_WARNING, "Ignoring jar file: " + jarFile);
            log(ILogger.LOG_WARNING, "Exception occurred: " + e);
            if (Listener.DEBUG) {
                e.printStackTrace();
            }
        }
    }        
        
    /**
     * Scans a text file which lists HME application class names and arguments,
     * and creates factories for them.
     *
     * @param file full path to the launch file
     */
    public void loadLaunchFile(String file) throws IOException
    {
        FastInputStream fin = new FastInputStream(new FileInputStream(file), 1024);
        LineNumberReader in = new LineNumberReader(new InputStreamReader(fin));
        try {
            String ln = in.readLine();
            while (ln != null) {
                ln = ln.trim();
                if (!ln.startsWith("#") && ln.length() > 0) {
                    createFactory(new ArgumentList(ln),
                                  ClassLoader.getSystemClassLoader());
                }
                ln = in.readLine();
            }
        } finally {
            in.close();
        }
    }

    /**
     * Create a factory with the specified arguments and class loader.
     */
    private void createFactory(ArgumentList args, ClassLoader loader)
    {
        try {
            String classname = args.getValue("--class", null);
            if (classname == null) {
                classname = args.shift();
            }
            Class appClass = Class.forName(classname, true, loader);
            Class[] paramTypes = {String.class, ClassLoader.class, IArgumentList.class};
            Method getFactoryMethod = appClass.getMethod("getAppFactory", paramTypes);
                        
            Object[] params = {classname, loader, args };
    
            IFactory factory = (IFactory)getFactoryMethod.invoke(null, params);
            
            args.checkForIllegalFlags();
            factories.add(factory);
        } catch (ClassNotFoundException e) {
            System.out.println("error: class not found: " + e.getMessage());
            System.out.println("error: check the classpath and access permissions");
        } catch (IllegalAccessException e) {
            System.out.println("error: illegal access: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public default constructor");
        } catch (NoSuchMethodException e) {
            System.out.println("error: no constructor: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public default constructor");
        } catch (IllegalArgumentException e) {
            System.out.println("error: illegal argument: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public static getAppFactory method with correct parameters");
        } catch (InvocationTargetException e) {
            System.out.println("error: unable to invoke method: " + e.getMessage());
            System.out.println("error: make sure the class is public and has a public static getAppFactory method");
        }
    }

    /**
     * Register a factory if MDNS is turned on.
     */
    protected void register(IFactory factory) throws IOException
    {
        String interfaces[] = listener.getInterfaces();
        int ports[] = listener.getPorts();
        for (int i = 0; i < interfaces.length; ++i) {
            for (int j = 0; j < ports.length; ++j) {
                String url = ("http://" + interfaces[i] + ":" +
                              ports[j] + factory.getAppName());

                if (rv == null) {
                    System.out.println(url + " [no mdns]");
                    continue;
                }

                System.out.println("MDNS: " + url);

                //
                // attempt to register using native mDNS daemon
                //
                DNSSDRequest dnssd = null;
                
                try {
                    dnssd = new DNSSDRequest();
                } catch (IOException e) {
                    // dnssd is not present, but it is OK
                }
                
                if (dnssd != null) {
                    try {
                        dnssd.registerService(factory.getAppTitle(), IHmeConstants.MDNS_DNSSD_TYPE, url);
                        factory.getFactoryData().put(DNSSD_KEY, dnssd);
                        continue;
                        
                    } catch (IOException e) {
                        // DNSSd Failed so make sure it is cleaned up
                        dnssd.close();
                        dnssd = null;
                    }
                }
                //
                // register using jmdns
                //
                if (dnssd == null) {
                	rv[j].registerService(getServiceInfo(IHmeConstants.MDNS_TYPE, factory, ports[j]));
                }
            }
        }
    }

    protected void unregister(IFactory factory) throws IOException
    {
        String interfaces[] = listener.getInterfaces();
        int ports[] = listener.getPorts();
        for (int i = 0; i < interfaces.length; ++i) {
            for (int j = 0; j < ports.length; ++j) {
                String url = ("http://" + interfaces[i] + ":" +
                              ports[j] + factory.getAppName());

                if (rv == null) {
                    System.out.println(url + " [no mdns]");
                    continue;
                }

                System.out.println("MDNS REMOVE: " + url);

                //
                // attempt to unregister using native mDNS daemon
                //
                DNSSDRequest dnssd = (DNSSDRequest) factory.getFactoryData().get(DNSSD_KEY);
                if ( dnssd != null) {
                    //
                    // attempt to unregister using native mDNS daemon
                    //
                    dnssd.close();
                    dnssd = null;
                } else {
                    //
                    // unregister using jmdns
                    //
                    rv[j].unregisterService(getServiceInfo(IHmeConstants.MDNS_TYPE, factory, ports[j]));
                }
            }
        }
    }

    protected ServiceInfo getServiceInfo(String mdns_type, IFactory factory, int port)
    {
        Hashtable atts = new Hashtable();
        atts.put("path", factory.getAppName());
        atts.put("version", (String)factory.getFactoryData().get(IFactory.HME_VERSION_TAG));
        return new ServiceInfo(mdns_type, factory.getAppTitle() + "." + mdns_type, port, 0, 0, atts);
    }

    /**
     * @return Returns the factories.
     */
    public List getFactories() {
        return factories;
    }
    /**
     * @return Returns the listener.
     */
    public Listener getListener() {
        return listener;
    }

    //
    // from IMain
    //

    public void log(int priority, String s)
    {
        System.out.println("LOG: " + s);        
    }

    public static void main(String argv[]) throws IOException
    {
        // print the version of the SDK every time at startup
        new HostingVersion().printVersion(System.out);
        new Main(new ArgumentList(argv));
    }
}
