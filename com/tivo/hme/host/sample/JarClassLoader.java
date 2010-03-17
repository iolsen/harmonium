//////////////////////////////////////////////////////////////////////
// 
// File: JarClassLoader.java
// 
// Copyright (c) 2004 TiVo Inc.
// 
//////////////////////////////////////////////////////////////////////
package com.tivo.hme.host.sample;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.security.cert.*;

import com.tivo.hme.interfaces.IFactory;
import com.tivo.hme.interfaces.ILoader;
import com.tivo.hme.interfaces.ILogger;

/**
 * A simple class loader which loads files from a jar file.  It favors classes
 * from the system class loader over ones in the actual jar file, just to keep
 * things simple.  E.g., it's OK to accidentally put SDK files into the jar file
 * because they will be ignored.
 *
 * The jar file verifies the signed classes automatically with the JarFile
 * class. This class, however, is responsible for checking the certificates for
 * a jar entry to make sure they were issued by the set of trusted CA's that are
 * passed into the constructor.  No checking is done if trustedCerts is null.
 *
 * Jar files may contain embedded jar files in the classpath.  When that's the
 * case those files are extracted into the a tmp file in the filesystem and a
 * JarClassLoader is used to load the classes from it.  The embedded jar files
 * need not be signed and in fact they are not verified even if they are.
 *
 * @author	Jonathan Payne
 */
@SuppressWarnings("unchecked")
public class JarClassLoader extends ClassLoader implements ILoader
{
    final static Object[] DEFAULT_CLASSPATH = new Object[] { "" };
    IFactory factory;
    JarFile jf;
    File file;
    boolean mustBeSigned;
    Hashtable trustedCerts;
    ILogger logger;
    Object classpath[] = DEFAULT_CLASSPATH;
    
    /**
     * Constructs a class loader from jar file and a hashtable of trusted certs.
     * Errors are logged using the ILogger interface.
     */
    public JarClassLoader(JarFile jf, ILogger logger, Hashtable trustedCerts)
    throws IOException, java.security.GeneralSecurityException
    {
        super(null);
        this.jf = jf;
        this.logger = logger;
        this.trustedCerts = trustedCerts;
        
        file = new File(jf.getName());
        mustBeSigned = trustedCerts != null;
        
        // check for a classpath in the manifest
        Manifest manifest = jf.getManifest();
        if (manifest != null) {
            Attributes attrs = manifest.getMainAttributes();
            String mcp = attrs.getValue("Class-Path");
            if (mcp != null) {
                StringTokenizer tok = new StringTokenizer(mcp, ":");
                classpath = new Object[tok.countTokens()];
                for (int i = 0; i < classpath.length; i++) {
                    String path = tok.nextToken();
                    if (path.toLowerCase().endsWith(".jar")) {
                        classpath[i] = extractJar(path);
                    } else {
                        if (path.equals(".")) {
                            path = "";
                        } else if (path.length() > 0 && !path.endsWith("/")) {
                            path += "/";
                        }
                        classpath[i] = path;
                    }
                }
            }
        }
    }
    
    public void setFactory(IFactory factory)
    {
        this.factory = factory;
    }
    
    /**
     * Handles a security exception by just completely bailing on this factory.
     * If the exception is a SecurityException it rethrows it, otherwise it
     * creates a new SecurityException and passes in the message of the original
     * exception.
     */
    private void securityException(Exception e, String name)
    {
        logger.log(ILogger.LOG_WARNING, "Security exception " + e + " while loading class: " + name);
        if (factory != null) {
            logger.log(ILogger.LOG_WARNING, "Disabling factory: security exception: " + factory.getAppName());
            factory.destroyFactory();
        }
        if (e instanceof SecurityException) {
            throw (SecurityException) e;
        } else {
            throw new SecurityException(e.toString());
        }
    }
    
    /**
     * Look up a jar file with the specified path.  If it can be found, extract
     * the jar file into a temporary location.  This causes the signature to be
     * verified.  Then create a JarClassLoader for it and return that.  If the
     * jar file cannot be found a warning is displayed but it's not a fatal
     * error.  Returns null in that case.
     */
    private JarClassLoader extractJar(String path)
    throws IOException, java.security.GeneralSecurityException
    {
        JarEntry entry = jf.getJarEntry(path);
        if (entry == null) {
            throw new FileNotFoundException(path + " not in " + file);
        }
        
        // create tmp file and extract the jar file into it
        File tmpjar = File.createTempFile(file.getName(), null, null);
        try {
            // copy the jar file from this jar file into the tmp file
            byte data[] = new byte[4096];
            int n;
            InputStream in = jf.getInputStream(entry);
            try {
                OutputStream out = new FileOutputStream(tmpjar);
                try {
                    while ((n = in.read(data, 0, data.length)) > 0) {
                        out.write(data, 0, n);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
            
            // If we got this far the entry was not tampered with.  Now verify
            // the certs.
            verifyCerts(entry);
            
            // now create the child class loader
            return new JarClassLoader(new JarFile(tmpjar, false), logger, null);
        } finally {
            // Delete the file now (on UNIX) to really make sure it goes
            // away but if that fails ask the VM to do it when we exit.
            if (!tmpjar.delete()) {
                tmpjar.deleteOnExit();
            }
        }
    }
    
    /**
     * Finds and loads the class with the specified name in the classpath.  This
     * asks the system class loader first to make sure apps don't redefine some
     * of the HME classes accidentally or on purpose.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
    protected Class findClass(final String name)
    throws ClassNotFoundException
    {
        try {
            Class c = findSystemClass(name);
            if (c != null) {
                return c;
            }
        } catch (ClassNotFoundException e) {
        }
        String path = name.replace('.', '/').concat(".class");
        for (int i = 0; i < classpath.length && classpath[i] != null; i++) {
            if (classpath[i] instanceof String) {
                JarEntry entry = jf.getJarEntry(classpath[i].toString() + path);
                if (entry != null) {
                    try {
                        byte cdata[] = new byte[(int) entry.getSize()];
                        DataInputStream in = new DataInputStream(jf.getInputStream(entry));
                        try {
                            in.readFully(cdata);
                        } finally {
                            in.close();
                        }
                        
                        // If we read the class ok then the checksums matched (or
                        // the class was not signed).  Now we can ask for the certs
                        // and if there aren't any but we expect them then we'll get
                        // an exception.
                        verifyCerts(entry);
                        return defineClass(name, cdata, 0, cdata.length);
                    } catch (java.lang.SecurityException e) {
                        securityException(e, name);
                    } catch (java.security.GeneralSecurityException e) {
                        securityException(e, name);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
            } else {
                JarClassLoader jcl = (JarClassLoader) classpath[i];
                try {
                    Class c = jcl.findClass(name);
                    if (c != null) {
                        return c;
                    }
                } catch (ClassNotFoundException e) {
                    // ignore - not in this class loader
                }
            }
        }
        return super.findClass(name);
    }
    
    private void verifyCerts(JarEntry je) throws java.security.GeneralSecurityException
    {
        Certificate certs[] = je.getCertificates();
        if (certs == null) {
            if (mustBeSigned) {
                throw new SecurityException(je + ": not signed");
            }
            return;
        } else if (!mustBeSigned) {
            return;
        }
        
        // check each cert (and possibly its chain)
        for (int i = 0; i < certs.length; i++) {
            verifyCert((X509Certificate) certs[i]);
        }
    }
    
    private void verifyCert(X509Certificate x509) throws java.security.GeneralSecurityException
    {
        String certname = x509.getSubjectDN().getName();
        X509Certificate tcert = (X509Certificate) trustedCerts.get(certname);
        if (tcert != null) {
            // found something with the same dn but let's make sure the rest of
            // the cert is also the same
            if (tcert.equals(x509)) {
                return;
            } else {
                logger.log(ILogger.LOG_WARNING, "Certificate name conflict.");
                logger.log(ILogger.LOG_WARNING, "Trusted: " + tcert);
                logger.log(ILogger.LOG_WARNING, "New: " + x509);
                logger.log(ILogger.LOG_NOTICE, "-------------------");
                throw new SecurityException("certificate name conflict: " + x509.getSubjectDN());
            }
        }
        
        // look up the issuer - it should already be trusted
        String issuername = x509.getIssuerDN().getName();
        X509Certificate issuer = (X509Certificate) trustedCerts.get(issuername);
        if (issuer == null) {
            throw new SecurityException("cannot find issuer for cert: " + x509.getSubjectDN());
        }
        
        // check the cert - this throws exceptions if something goes wrong
        x509.verify(issuer.getPublicKey());
        trustedCerts.put(certname, x509);
    }        
    
    /**
     * Returns the specified path as an input stream.  It searches the jar file
     * in the order specified by the classpath.
     *
     * @param path the resource path
     * @return null if the file is not found, otherwise the InputStream.
     */
    public InputStream getResourceAsStream(String path)
    {
        for (int i = 0; i < classpath.length; i++) {
            if (classpath[i] instanceof String) {
                JarEntry entry = jf.getJarEntry(classpath[i].toString() + path);
                if (entry != null) {
                    try {
                        return jf.getInputStream(entry);
                    } catch (IOException e) {
                        System.out.println("WARNING: " + e);
                    }
                }
            } else {
                InputStream in = ((JarClassLoader) classpath[i]).getResourceAsStream(path);
                if (in != null) {
                    return in;
                }
            }
        }
        return null;
    }
    
    public Enumeration findResources(final String name) throws IOException
    {
        return new Vector(0).elements();
    }
    
    public String toString()
    {
        return "JarClassLoader[" + jf.getName() + "]";
    }
    
    protected void finalize()
    {
        close();
    }
    
    public void close()
    {
        try {
            jf.close();
        } catch (IOException e) {}
        for (int i = 0; i < classpath.length; i++) {
            if (classpath[i] instanceof JarClassLoader) {
                ((JarClassLoader) classpath[i]).close();
            }
        }
    }
}
