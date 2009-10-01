//////////////////////////////////////////////////////////////////////
//
// File: Simulator.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MutableComboBoxModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

import com.tivo.hme.host.sample.Main;
import com.tivo.hme.host.util.ArgumentList;
import com.tivo.hme.host.util.Cookies;
import com.tivo.hme.sdk.Factory;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.io.FastInputStream;
import com.tivo.hme.sdk.io.FastOutputStream;
import com.tivo.hme.sdk.util.HmeVersion;

/**
 * The main simulator.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class Simulator implements IHmeProtocol, ActionListener, KeyListener, ISimPrefs
{
    final static int WIDTH = 640;
    final static int HEIGHT = 480;

    final static int ADDRESS_HEIGHT = 30;
    
    static Simulator master;

    // settings
    static boolean DEBUG = false;
    static boolean SOUND = true;
    static boolean SHOW_SAFE = false;
    static boolean SHOW_HILITE = false;
    static boolean RECORD = false;
    static boolean USEMDNS = true;
    
    // widgets
    SimPane sim;
    SimTree tree;
    JScrollPane treeScroll;
    SimInfoPane info;
    AddressPane address;
    SimMDNS simMDNS;
    SimResView simResView;
    
    String prevStatusText = "";
    
    JFrame simFrame;
    JFrame treeFrame;
    JFrame kbdShortcutFrame;
    JFrame aboutFrame;
    Map accelerators;

    // active resource
    SimResource active;

    // http cookies
    Cookies cookies;
    
    // image cache
    Map images;

    // system resources
    static Map paths;
    Map resources;

    // user preferences
    Preferences prefs;
    Vector prefList;
    
    Simulator()
    {
        master = this;

        resources = new HashMap();
        
        //
        // this works around an annoying awt deriveFont bug
        //
        try {
            Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("default.ttf"));
        } catch (Exception e) {
        }

        cookies = new Cookies();

        //
        // initialize preferences
        //
        prefs = Preferences.userRoot().node("com/tivo/hme/sdk/simulator");        
        prefList = new Vector();
        addPrefs(this);

        DEBUG = prefs.getBoolean("DEBUG", DEBUG);
        SOUND = prefs.getBoolean("SOUND", SOUND);
        SHOW_SAFE = prefs.getBoolean("SHOW_SAFE", SHOW_SAFE);
        SHOW_HILITE = prefs.getBoolean("SHOW_HILITE", SHOW_HILITE);

        //
        // menubar
        //
        JMenuBar menus = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic('F');
        addMenuCheckbox(file, "Show Views", 'V');
        addMenuCheckbox(file, "Show Resource Usage", 'R');
        addMenuCheckbox(file, "Show Highlight", 'H');
        addMenuCheckbox(file, "Show Safe Action", 'S');
        file.addSeparator();
        addMenuCheckbox(file, "Debug Output", 'D');
        file.addSeparator();
        addMenuCommand(file, "Take Snapshot...", 'T');
        file.addSeparator();
        addMenuCommand(file, "Quit", 'Q');
        menus.add(file);

        //
        // create the sim
        //
        sim = new SimPane(WIDTH, HEIGHT);
        sim.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        new Simulator.MouseCursorHandler(sim, Cursor.CROSSHAIR_CURSOR);

        //
        // create the info and address bars
        //
        info = new SimInfoPane(sim);

        address = new AddressPane();
        addPrefs(address);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(address);
        
        //
        // create the Frame and set it to quit when closed
        //
        simFrame = new JFrame();
        simFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                savePrefs();
                System.exit(0);
            }
        });

        simFrame.setTitle("Simulator");
        simFrame.setResizable(false);
        simFrame.getContentPane().setLayout(new BorderLayout());
        simFrame.getContentPane().add(toolbar, BorderLayout.NORTH);
        simFrame.getContentPane().add(info, BorderLayout.SOUTH);
        simFrame.getContentPane().add(sim, BorderLayout.CENTER);
        simFrame.setJMenuBar(menus);
        simFrame.pack();
        positionFrame(simFrame);
        setIcon(simFrame);
        
        //
        // create the tree
        //
        tree = new SimTree();
        treeFrame = new JFrame();
        treeFrame.setTitle("Views");
        treeFrame.getContentPane().setLayout(new BorderLayout());

        treeScroll = new JScrollPane();
        treeScroll.getViewport().add(tree);
        
        treeFrame.getContentPane().add(treeScroll);
        treeFrame.pack();
        treeFrame.setBounds(simFrame.getX() + simFrame.getSize().width + 8,
                            simFrame.getY(),
                            400, simFrame.getHeight());

        treeFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    setMenuItemSelected("Show Views", false);
                }
        });
        
        setIcon(treeFrame);
        
        //
        // events
        //
        sim.addKeyListener(this);
        tree.addKeyListener(this);

        //
        // obey settings
        //
        setMenuItemSelected("Debug Output", DEBUG);
        setMenuItemSelected("Show Highlight", SHOW_HILITE);
        setMenuItemSelected("Show Safe Action", SHOW_SAFE);


        //
        // create the application and network menus
        //
        if (USEMDNS) {
            simMDNS = new SimMDNS(menus);
            addPrefs(simMDNS);
        }

        //
        // create the help menu
        //
        JMenu kbdShortcut = new JMenu("Help");
        kbdShortcut.setMnemonic('H');
        addMenuCommand(kbdShortcut, "Keyboard Shortcuts", 'K');
        kbdShortcut.addSeparator();
        addMenuCommand(kbdShortcut, "About", 0);
        menus.add(kbdShortcut);
        

        //
        // get ready...
        //
        sim.requestFocus();
        simFrame.setVisible(true);

        // start loading safe.png. this can take a while.
        getImage("safe.png");
    }

    void resourceUpdate()
    {
        if (simResView != null) {
            simResView.update();
        }
    }
    
    void takeSnapshot()
    {
        // take snapshot first
        Image img = sim.createImage(sim.getWidth(), sim.getHeight());
        sim.paintComponent(img.getGraphics());

        // build file chooser, set up supported file types
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Snapshot As...");
        ExtensionFileFilter ff = new ExtensionFileFilter(".png");
        fc.setFileFilter(ff);

        // display save dialog and write the image
        if (fc.showSaveDialog(simFrame) == JFileChooser.APPROVE_OPTION) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(ff.getFile(fc.getSelectedFile()));
                ImageIO.write((BufferedImage)img, "png", out);
            } catch (IOException e) {
                e.printStackTrace();
                setStatus("ERROR: Could not save file: " +
                          fc.getSelectedFile() + " " + e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


	void showAboutBox()
    {
        if (aboutFrame != null) {
            aboutFrame.show();
        } else {
            JPanel content = new JPanel();
            content.setLayout(new GridLayout(0,1));
            ImageIcon icon = new ImageIcon(getClass().getResource("app.png"));
            content.add(new JLabel("The Simulator", icon, JLabel.LEFT));
            HmeVersion v = new HmeVersion();
            content.add(new JLabel(v.getTitle()));
            content.add(new JLabel(v.getVersion()));
            aboutFrame = new JFrame("About The Simulator");

            ImageIcon aboutIcon = new ImageIcon(getClass().getResource("about.gif")); 
            JPanel outer = new JPanel();
            outer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            outer.setLayout(new BorderLayout());
            outer.add(content, BorderLayout.NORTH);
            JPanel center = new JPanel();
            center.setLayout(new GridLayout(0,1));
            center.add(new JLabel(aboutIcon, JLabel.CENTER));
            center.add(new JLabel("http://tivohme.sourceforge.net", JLabel.CENTER));
            outer.add( center, BorderLayout.CENTER);
            outer.add(new JLabel("(c) 2004, 2005 TiVo, Inc.", JLabel.CENTER), BorderLayout.SOUTH);
            aboutFrame.getContentPane().add(outer);
            aboutFrame.setIconImage(getImage("tivo.png"));
            aboutFrame.pack();
            aboutFrame.setResizable(false);
            aboutFrame.setSize(400, 180);
            aboutFrame.setLocation(simFrame.getX() + (simFrame.getSize().width/2) - (aboutFrame.getSize().width/2),
                                   simFrame.getY() + (simFrame.getSize().height/2) - (aboutFrame.getSize().height/2));
            aboutFrame.show();
        }
    }

    void addKbdShortcutText(JPanel p, String key, String action)
    {

        // Note the embedded tab so columns line up.
        String text = "   " + key + "	" + action + "  ";
        // yes this could be implemented with a single text area.
        JTextArea jta = new JTextArea(text, 1, 50);
        jta.setEditable(false);
        p.add(jta);
    }

    void showKeyboardShortcutsBox()
    {
        if (kbdShortcutFrame != null) {
            kbdShortcutFrame.show();
        } else {
            JPanel content = new JPanel();
            content.setLayout(new GridLayout(0,1));
            kbdShortcutFrame = new JFrame("Keyboard Shortcuts");

            addKbdShortcutText(content, "arrows", "up, down, left, right");
            addKbdShortcutText(content, "page up", "channel up");
            addKbdShortcutText(content, "page down", "channel down");
            addKbdShortcutText(content, "0-9", "num0 - num9");
            addKbdShortcutText(content, "enter", "select");
            addKbdShortcutText(content, "p", "play");
            addKbdShortcutText(content, "space", "pause");
            addKbdShortcutText(content, "s", "slow");
            addKbdShortcutText(content, "]", "forward");
            addKbdShortcutText(content, "[", "reverse");
            addKbdShortcutText(content, "-", "replay");
            addKbdShortcutText(content, "=", "advance");
            addKbdShortcutText(content, "u", "thumbs up");
            addKbdShortcutText(content, "d", "thumbs down");
            addKbdShortcutText(content, "m", "mute");
            addKbdShortcutText(content, "r", "record");
            addKbdShortcutText(content, "e", "enter");
            addKbdShortcutText(content, "c", "clear");
            addKbdShortcutText(content, "i", "info/display");
            addKbdShortcutText(content, "w", "window/PIP/aspect (optional)");
            addKbdShortcutText(content, "x", "exit (optional)");
            addKbdShortcutText(content, "`", "stop (optional)");
            addKbdShortcutText(content, ",", "menu (optional)");
            addKbdShortcutText(content, ".", "top menu (optional)");
            addKbdShortcutText(content, "a", "angle (optional)");
            
            JPanel outer = new JPanel();
            outer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            outer.setLayout(new BorderLayout());
            outer.add(content, BorderLayout.NORTH);
            kbdShortcutFrame.getContentPane().add(outer);
            kbdShortcutFrame.setIconImage(getImage("tivo.png"));
            kbdShortcutFrame.pack();
            kbdShortcutFrame.setResizable(true);
            kbdShortcutFrame.setSize(278, 411);
            Rectangle r = simFrame.getBounds();
            kbdShortcutFrame.setLocation(r.x + 250, r.y + r.height);
            kbdShortcutFrame.show();
        }
    }

    static class ExtensionFileFilter extends FileFilter
    {
        String ext;

        public ExtensionFileFilter(String ext)
        {
            this.ext = ext;
        }

        public File getFile(File file)
        {
            if (!file.getName().endsWith(ext)) {
                file = new File(file.getAbsolutePath() + ext);
            }
            return file;
        }
        
        public boolean accept(File f)
        {
            if (f.isDirectory()) {
                return true;
            }
            return f.getName().endsWith(ext);
        }

        public String getDescription()
        {
            return ext;
        }
    }

    void savePrefs()
    {
        try {
            prefs.clear();        
            Enumeration e = prefList.elements();
            while (e.hasMoreElements()) {
                ISimPrefs p = (ISimPrefs)e.nextElement();
                p.storePrefs(prefs);
            }
        } catch (IOException e) {
            setStatus("ERROR: Failed to save preferences: "+e.toString());
            e.printStackTrace();
        } catch (BackingStoreException e) {            
            setStatus("ERROR: Failed to save preferences: "+e.toString());
            e.printStackTrace();
        }
    }

    public void storePrefs(Preferences prefs)
    {
        prefs.putBoolean("DEBUG", DEBUG);
        prefs.putBoolean("SOUND", SOUND);
        prefs.putBoolean("SHOW_SAFE", SHOW_SAFE);
        prefs.putBoolean("SHOW_HILITE", SHOW_HILITE);
        prefs.putInt(simFrame.getTitle() + ".X", simFrame.getX());
        prefs.putInt(simFrame.getTitle() + ".Y", simFrame.getY());
        prefs.put("address", address.getText());
    }

    void addPrefs(ISimPrefs prefs)
    {
        prefList.addElement(prefs);
    }


    void setBusy(boolean busy)
    {
        address.setBusy(busy);
    }
    
    void setStatus(String message)
    {
        if (!prevStatusText.equals(message) && !message.startsWith("Connecting:")) {
            System.out.println(message);
            prevStatusText = message;
            if (message.length()==0) {
                setBusy(false);
            }
        }
        info.setText(message);
    }
    
    void setURL(String url)
    {
        sim.setURL(url);
        address.setText((url == null) ? "" : url);

        // close and remove the old tree
        treeScroll.getViewport().remove(tree);        

        // create a new empty tree and install it
        tree = new SimTree();
        treeScroll.getViewport().add(tree);
    }

    void run(String url)
    {
        if (url != null) {
            setURL(url);
        }
        sim.run();
    }

    
    void setTreePath(TreePath path)
    {
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    //
    // UI helpers
    //

    void addMenuCommand(JMenu menu, String label, int accel)
    {
        addMenuItem(menu, new JMenuItem(label), accel);
    }

    void addMenuCheckbox(JMenu menu, String label, int accel)
    {
        addMenuItem(menu, new JCheckBoxMenuItem(label), accel);
    }

    void addMenuItem(JMenu menu, JMenuItem item, int accel)
    {
        item.addActionListener(this);
        if (accel != 0) {
            if (accelerators == null) {
                accelerators = new HashMap();
            }
            item.setMnemonic(accel);
            item.setAccelerator(KeyStroke.getKeyStroke((char)accel, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
            accelerators.put(new Integer(accel), item);
        }
        menu.add(item);
    }

    AbstractButton getMenuItem(String label)
    {
        JMenuBar menus = simFrame.getJMenuBar();
        for (int i = menus.getMenuCount(); i-- > 0;) {
            JMenu menu = menus.getMenu(i);
            for (int j = menu.getMenuComponentCount(); j-- > 0;) {
                Component c = menu.getMenuComponent(j);
                if (c instanceof AbstractButton) {
                    AbstractButton item = (AbstractButton)c;
                    if (label.equals(item.getText())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    void setMenuItemEnabled(String label, boolean enabled)
    {
        getMenuItem(label).setEnabled(enabled);
    }

    void setMenuItemSelected(String label, boolean selected)
    {
        getMenuItem(label).setSelected(selected);        
    }

    void positionFrame(JFrame frame)
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = prefs.getInt(frame.getTitle() + ".X",
                             (screen.height - frame.getSize().height) / 8);
        int y = prefs.getInt(frame.getTitle() + ".Y",
                             (screen.width  - frame.getSize().width)  / 8);
        frame.setLocation(x, y);
    }

    void centerFrame(JFrame frame)
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screen.width  - frame.getSize().width)  / 2,
                          (screen.height - frame.getSize().height) / 3);
    }

    void setIcon(JFrame frame)
    {
        frame.setIconImage(getImage("tivo.png"));
    }

    //
    // event callbacks
    //

    public void actionPerformed(ActionEvent e)
    {
        String label = e.getActionCommand();
        if (label.startsWith("About")) {
            showAboutBox();
        } else if (label.startsWith("Keyboard Shortcuts")) {
            showKeyboardShortcutsBox();
        } else if (label.equals("Show Highlight")) {
            SHOW_HILITE = !SHOW_HILITE;
            storePrefs(prefs);
            sim.touch();
        } else if (label.equals("Show Safe Action")) {
            SHOW_SAFE = !SHOW_SAFE;
            storePrefs(prefs);
            sim.touch();
        } else if (label.equals("Show Views")) {
            if(!treeFrame.isVisible()) {
                treeFrame.setBounds(simFrame.getX() +
                                    simFrame.getSize().width + 8,
                                    simFrame.getY(),
                                    400, simFrame.getHeight());
            }
            treeFrame.setVisible(!treeFrame.isVisible());
        } else if (label.equals("Show Resource Usage")) {
            if (simResView == null) {
                simResView = new SimResView();
                simResView.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent event) {
                            setMenuItemSelected("Show Resource Usage", false);
                        }
                    });
            } else {
                simResView.show();
            }
        } else if (label.equals("Debug Output")) {
            DEBUG = !DEBUG;
            storePrefs(prefs);
        } else if (label.equals("Take Snapshot...")) {
            takeSnapshot();
        } else if (label.equals("Quit")) {
            savePrefs();
            System.exit(0);
        }
    }

    public void keyTyped(KeyEvent e) { }
    public void keyReleased(KeyEvent e) { }
    
    public void keyPressed(KeyEvent e)
    {
        if (e.getModifiers() != InputEvent.CTRL_MASK) {
            return;
        }

        AbstractButton button = (AbstractButton)accelerators.get(new Integer(e.getKeyCode()));
        if (button != null) {
            e.consume();
            button.doClick();
        }
    }

    //
    // resource helpers
    //

    synchronized SimResource getSystemResource(int id)
    {
        Integer key = new Integer(id);
        SimResource rsrc = (SimResource)resources.get(key);
        if (rsrc == null) {
            String path = (String)paths.get(key);
            if (path != null) {
                if (path.endsWith(".ttf")) {
                    rsrc = new SimResource.FontResource(null, id, path);
                } else if (path.endsWith(".pcm")) {
                    rsrc = new SimResource.SoundResource(null, id, path);
                }
            }
            if (rsrc != null) {
                resources.put(key, rsrc);
            }
        }
        return rsrc;
    }

    synchronized Image getImage(String name)
    {
        if (images == null) {
            images = new HashMap();
        }
        Image image = (Image)images.get(name);
        if (image == null) {
            URL url = getResourceAsURL(name);
            image = Toolkit.getDefaultToolkit().getImage(url);
            images.put(name, image);
        }
        return image;
    }

    private static String getResourceName(String name)
    {
        return Simulator.class.getPackage().getName().replace('.', '/') + "/" + name;
    }

    static URL getResourceAsURL(String name)
    {
        name = getResourceName(name);
        return Simulator.class.getClassLoader().getResource(name);
    }

    static InputStream getResourceAsStream(String name)
    {
        name = getResourceName(name);        
        return Simulator.class.getClassLoader().getResourceAsStream(name);
    }

    static FastInputStream getResourceAsFastStream(String name)
    {
        return new FastInputStream(getResourceAsStream(name), 1024);
    }
    
    static FastOutputStream getResourceBytes(String name) throws IOException
    {
        InputStream in = getResourceAsStream(name);
        try {
            return drainStream(in);
        } finally {
            in.close();
        }
    }
    
    static FastOutputStream drainStream(InputStream in) throws IOException
    {
        byte buf[] = new byte[1024];
        FastOutputStream out = new FastOutputStream(1024);
        while (true) {
            int n = in.read(buf, 0, buf.length);
            if (n < 0) {
                break;
            }
            out.write(buf, 0, n);
        }
        return out;
    }

    //
    // accessors
    //

    static Simulator get()
    {
        return master;
    }

    //
    // main
    //
    
    static void usage()
    {
        new HmeVersion().printVersion(System.out);
        System.out.println("Usage : Simulator [options]... [url] or [class + args]");
        System.out.println();
        System.out.println("Options:");
        System.out.println(" -d       turn on debugging");
        System.out.println(" -mute    disable sound");
        System.out.println(" -s       disable application discovery using mdns");
        
        System.out.println();
        System.out.println("When started with a url, the Simulator will connect to that url at");
        System.out.println("startup. If the first argument is not a url the Simulator will pass");
        System.out.println("all arguments to a Factory and connect to it.");
        System.exit(1);
    }

    public static void main(String argv[]) throws Exception
    {
        ArgumentList args = new ArgumentList(argv);
        Simulator.DEBUG = args.getBoolean("-d");

        
        if (args.getBoolean("-h") || args.getBoolean("--help")) {
            usage();
            return;
        } else if (Simulator.DEBUG) {
            new HmeVersion().printVersion(System.out);
        }

        if (args.getBoolean("-mute")) {
            SOUND = false;
        }

        if (args.getBoolean("-record")) {
            RECORD = true;
        }

        if (args.getBoolean("-s")) {
            USEMDNS = false;
        }
        
        String url = null;
        String classname = args.getValue("-class", null);
        if (args.getRemainingCount() > 0) {
            String arg = args.shift();
            if (arg.startsWith("http://")) {
                // it's a URL
                url = arg;
                if (args.getRemainingCount() > 0) {
                    usage();
                    return;
                }
            } else {
                // assume remaining arg is the class to run
                classname = arg;
            }
        }

        // we have an applicaion class, start a hosting environment
        // using that class
        if (classname != null) {
        	if (args.getRemainingCount() > 0) {
        		classname = classname + " " + args.toString();
        	}
            ArgumentList mainArgs = new ArgumentList(classname);
            Main main = new Main(mainArgs);
            if (main.getFactories() != null && main.getListener() != null) {
                Factory appFactory = (Factory)(main.getFactories().get(0));
                String[] intfs = main.getListener().getInterfaces();
                url = "http://" + intfs[0] + appFactory.getAppName();
            } else {
            	System.out.println("Unable to start specified application - running Simulator stand alone.");
            }
            //System.out.println("*** url = ->" + url +"<-");
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        new Simulator().run(url);
    }

    /**
     * Display the url of the application
     * When enter is pressed change the application the simulator is running.
     **/
    @SuppressWarnings("serial")
	class AddressPane extends JPanel implements ActionListener, ISimPrefs
    {
        final static int MAX_HISTORY = 10;
        
        JComboBox text;
        ListModel list;

        JLabel statusIcon;
        ImageIcon idleImg, busyImg;
        
        public AddressPane()
        {
            setSize(WIDTH, ADDRESS_HEIGHT);
            setPreferredSize(getSize());
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            setLayout(new BorderLayout());

            list = new ListModel();
            text = new JComboBox(list);
            text.setEditable(true);
            text.addActionListener(this);

            idleImg = new ImageIcon(getResourceAsURL("idle.gif"));
            busyImg = new ImageIcon(getResourceAsURL("busy.gif"));
                
            statusIcon = new JLabel(idleImg);
            
            add(new JLabel(" Address:  "), BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(statusIcon, BorderLayout.EAST);
            loadPrefs();
        }

        public void storePrefs(Preferences prefs)
        {
            for(int i =  0; i < address.list.history.size(); i++) {
                String item = (String)address.list.history.elementAt(i);
                if (!"".equals(item)) {
                    prefs.put("address.history." + i, item);
                }
            }
        }

        void loadPrefs()
        {
            for (int i = MAX_HISTORY-1; i >-1; i--) {
                String s = prefs.get("address.history." + i, null);
                if (s == null) {
                    break;
                }
                text.addItem(s);
            }
        }
        

        String getText()
        {
            String s = (String)list.getSelectedItem();
            return (s != null) ? s : "";
        }
        
        void setText(String s)
        {
            if (s != null) {
                text.addItem(s);
                text.setSelectedItem(s);
            }
        }

        void setBusy(boolean busy)
        {
            statusIcon.setIcon(busy ? busyImg : idleImg);
        }

        /**
         * This event handler will try to fix up the URL and change the
         * simulator's view to the entered URL.
         */
        public void actionPerformed(ActionEvent e)
        {
            String url = getText().trim();
            if ("".equals(url)) {
                return;
            }

            if (!url.startsWith("http://")) {
                url = "http://" + url;
            }

            try {
                URL u = new URL(url);
                if (u.getPort() == -1) {
                    url = u.getProtocol() + "://" + u.getHost() + ":7288" + u.getPath();
                }
            } catch(MalformedURLException me) {
                // let the simulator handle this exception
                // it will cause another exception downstream
            }

            setText(url);
            setURL(url);
        }


        class ListModel implements MutableComboBoxModel
        {
            Vector listDataListener = new Vector();
            Vector history = new Vector();

            int selectedIndex;
            Object selected;

            //
            // list data model
            //
            
            public void addListDataListener(ListDataListener l)
            {
                listDataListener.add(l);
            }

            public Object getElementAt(int index)
            {
                return history.elementAt(index);
            }

            public int getSize()
            {
                return history.size();
            }

            public void removeListDataListener(ListDataListener l)
            {
                listDataListener.remove(l);
            }

            //
            // combo box data model
            //
            
            public Object getSelectedItem()
            {
                return selected;
            }

            public void setSelectedItem(Object item)
            {
                selected = item;
            }

            //
            // mutable combo box data model
            //
            
            public void addElement(Object obj)
            {
                insertElementAt(obj, 0);
            }

            public void insertElementAt(Object obj, int index)
            {
                if (history.contains(obj)) {
                    removeElement(obj);
                }
                history.insertElementAt(obj, index);
                Enumeration e = listDataListener.elements();
                while (e.hasMoreElements()) {
                    ListDataListener ld = (ListDataListener)e.nextElement();
                    ld.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index));
                }
            }

            public void removeElement(Object obj)
            {
                int index = history.indexOf(obj);
                if (index != -1) {
                    removeElementAt(index);
                }
            }

            public void removeElementAt(int index)
            {
                history.removeElementAt(index);
                Enumeration e = listDataListener.elements();
                while (e.hasMoreElements()) {
                    ListDataListener ld = (ListDataListener)e.nextElement();
                    ld.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index + 1));
                }
            }
        }
    }

    /**
     * This class will handle changing the cursor when the mouse enters a
     * component. This will also request focus when the mouse is pressed in a
     * component.
     */
    static class MouseCursorHandler implements MouseListener
    {
        int cursor;
        JComponent comp;

        public MouseCursorHandler(JComponent comp, int cursor)
        {
            this.comp = comp;
            this.cursor = cursor;
            comp.addMouseListener(this);
        }
        
        public void mousePressed(MouseEvent e)
        {
            comp.requestFocus();
        }
        
        public void mouseReleased(MouseEvent e) { }

        public void mouseEntered(MouseEvent e)
        {
            comp.setCursor(Cursor.getPredefinedCursor(cursor));
        }

        public void mouseExited(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
    }

    static {
        paths = new HashMap();
        paths.put(new Integer(ID_DEFAULT_TTF),      "default.ttf");
        paths.put(new Integer(ID_SYSTEM_TTF),       "system.ttf");
        
        paths.put(new Integer(ID_BONK_SOUND),       "sounds/bonk_2.pcm");
        paths.put(new Integer(ID_UPDOWN_SOUND),     "sounds/updown_2.pcm");
        paths.put(new Integer(ID_THUMBSUP_SOUND),   "sounds/thumbsup_2.pcm");
        paths.put(new Integer(ID_THUMBSDOWN_SOUND), "sounds/thumbsdown_2.pcm");
        paths.put(new Integer(ID_SELECT_SOUND),     "sounds/select_2.pcm");
        paths.put(new Integer(ID_TIVO_SOUND),       "sounds/tivo_2.pcm");
        paths.put(new Integer(ID_LEFT_SOUND),       "sounds/pageup_2.pcm");
        paths.put(new Integer(ID_RIGHT_SOUND),      "sounds/pagedown_2.pcm");
        paths.put(new Integer(ID_PAGEUP_SOUND),     "sounds/pagedown_2.pcm");
        paths.put(new Integer(ID_PAGEDOWN_SOUND),   "sounds/pageup_2.pcm");
        paths.put(new Integer(ID_ALERT_SOUND),      "sounds/alert_2.pcm");
        paths.put(new Integer(ID_DESELECT_SOUND),   "sounds/deselect_2.pcm");
        paths.put(new Integer(ID_ERROR_SOUND),      "sounds/error_2.pcm");
        paths.put(new Integer(ID_SLOWDOWN1_SOUND),  "sounds/slowdown1_2.pcm");
        paths.put(new Integer(ID_SPEEDUP1_SOUND),   "sounds/speedup1_2.pcm");
        paths.put(new Integer(ID_SPEEDUP2_SOUND),   "sounds/speedup2_2.pcm");
        paths.put(new Integer(ID_SPEEDUP3_SOUND),   "sounds/speedup3_2.pcm");
    }
}
