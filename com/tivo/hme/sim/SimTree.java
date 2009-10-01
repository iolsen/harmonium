//////////////////////////////////////////////////////////////////////
//
// File: SimTree.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A pane which contains a tree view.
 *
 * The tree view is complicated. JTree allows you to define a completely
 * arbitrary "model" representing the data in your tree. In our case, the model
 * reflects the underlying SimObjects. Each SimObject appears in the tree,
 * possibly in multiple places.
 *
 * Updates to the tree are communicated via events. When a node is
 * inserted/removed/changed, an event is fired to the tree using the model. Each
 * event contains the complete path to the object that is being updated. The
 * "path" is a list of objects. There are helpers inside SimObject and SimTree
 * for building paths and firing events.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings({ "unchecked", "serial" })
public class SimTree extends JTree implements TreeSelectionListener, HierarchyListener
{
    final static int CHANGED  = 0;
    final static int INSERTED = 1;
    final static int REMOVED  = 2;

    public SimTree()
    {
        super(new Model(Simulator.get().sim.zero));

        setCellRenderer(new Renderer());
        setScrollsOnExpand(true);
        
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(this);
        addHierarchyListener(this);

    }

    //
    // overrides from JTree
    //
    
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        return value.toString();
    }

    public void valueChanged(TreeSelectionEvent e)
    {
        Simulator.get().sim.setSelected((SimObject)getLastSelectedPathComponent());
    }

    public void hierarchyChanged(HierarchyEvent e)
    {
    }

    public void scrollRectToVisible(Rectangle r)
    {
        r.x = 0;
        super.scrollRectToVisible(r);
    }

    //
    // fire an event to the tree
    //
    void fireEvent(int action, Object path[], int len, Object src, int index)
    {
        // reverse the array
        Object copy[] = new Object[len];
        for (int i = len; i-- > 0;) {
            copy[i] = path[len - i - 1];
        }

        // send the event
        int indices[] = new int[]{index};
        Object children[] = new Object[]{src};

        // create the event: it will be dispatched by Swing
        // in the swing event thread
        new SimTreeEvent(this, copy, indices, children, action);
    }

    static class SimTreeEvent extends TreeModelEvent implements Runnable
    {
        int action;
        Model model;
        
        SimTreeEvent(SimTree source, Object[] path, int[] indices,
                     Object[] children, int action)
        {
            super(source, path, indices, children);
            this.action = action;
            model = (Model)source.getModel();
            SwingUtilities.invokeLater(this);
        }
        
        public void run() {
            switch (action) {
              case CHANGED:  model.fireTreeNodesChanged(this);  break;
              case INSERTED: model.fireTreeNodesInserted(this); break;
              case REMOVED:  model.fireTreeNodesRemoved(this);  break;
            }
        }
    }
    
    //
    // a special TreeModel that exposes the hme tree
    //
    static class Model implements TreeModel
    {
        SimObject root;
        int nlisteners;
        TreeModelListener listeners[];

        Model(SimObject root)
        {
            this.root = root;
            this.listeners = new TreeModelListener[4];
        }

        //
        // overrides from TreeModel
        //
        public Object getRoot()
        {
            return root;
        }

        public Object getChild(Object node, int index)
        {
            SimObject obj = ((SimObject)node).getChild(index);
            if (obj == null) {
                return FakeSimObject.get();
            }
            return obj;
        }
    
        public int getChildCount(Object node)
        {
            return ((SimObject)node).nchildren;
        }

        public boolean isLeaf(Object node)
        {
            return ((SimObject)node).nchildren == 0;
        }

        public void valueForPathChanged(TreePath path, Object newValue)
        {
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            return ((SimObject)parent).getIndexOfChild(child);
        }


        /**
         * A FakeSimObject is required because the tree event dispatching is
         * delayed because the events must be fires from the AWT event thread. A
         * Fake object is returned by getChild when ever the index would be out
         * of bounds.
         */
        static class FakeSimObject extends SimObject
        {
            static SimObject obj;
            
            public static SimObject get() {
                if (obj != null) return obj;
                obj = new FakeSimObject();
                return obj;
            }
            
            protected FakeSimObject()
            {
                super(null,-1);
            }
            
            Graphics2D getChildGraphics(Graphics2D g)
            {
                return null;
            }
            
            Rectangle getDrawingBounds()
            {
                return null;
            }

            List getAbsoluteBounds()
            {
                return null;
            }
            
            void toChildSpace(Point p)
            {
            }
            
            void toParentSpace(Rectangle r)
            {
            }
        }


        //
        // manage listeners
        //
        
        public void addTreeModelListener(TreeModelListener listener)
        {
            if (nlisteners == listeners.length) {
                System.arraycopy(listeners, 0, listeners = new TreeModelListener[nlisteners * 2], 0, nlisteners);
            }
            listeners[nlisteners++] = listener;
        }

        public void removeTreeModelListener(TreeModelListener listener)
        {
            for (int i = nlisteners; i-- > 0;) {
                if (listeners[i] == listener) {
                    if (nlisteners > 1) {
                        listeners[i] = listeners[nlisteners - 1];
                    }
                    --nlisteners;
                    break;
                }
            }
        }

        //
        // eventing
        //
        
        public void fireTreeNodesChanged(TreeModelEvent e)
        {
            for (int i = nlisteners; i-- > 0;) {
                listeners[i].treeNodesChanged(e);
            }
        }

        public void fireTreeNodesInserted(TreeModelEvent e)
        {
            for (int i = nlisteners; i-- > 0;) {
                listeners[i].treeNodesInserted(e);
            }
        }

        public void fireTreeNodesRemoved(TreeModelEvent e)
        {
            for (int i = nlisteners; i-- > 0;) {        
                listeners[i].treeNodesRemoved(e);
            }
        }

        public void fireTreeStructureChanged(TreeModelEvent e)
        {
            for (int i = nlisteners; i-- > 0;) {        
                listeners[i].treeStructureChanged(e);
            }
        }
    }

    //
    // a special TreeCellRenderer that can draw hme objects
    //

    class Renderer extends DefaultTreeCellRenderer
    {
        Map icons;

        Renderer()
        {
            icons = new HashMap();
            setFont(new Font("Verdana", Font.PLAIN, 11));
        }

        Icon getIcon(String name)
        {
            Icon icon = (Icon)icons.get(name);
            if (icon == null) {
                synchronized (this) {
                    icon = (Icon)icons.get(name);
                    if (icon == null) {
                        icon = new ImageIcon(Simulator.get().getImage(name));
                        icons.put(name, icon);
                    }
                }
            }
            return icon;
        }
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            this.hasFocus = hasFocus;
            this.selected = selected;

            //
            // default color
            //
            
            if (selected) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }

            //
            // default icon
            //
            setEnabled(true);

            SimObject obj = (SimObject)value;
            String icon = obj.getIcon();
            if (icon != null) {
                setIcon(getIcon(icon));
            } else {
                if (leaf) {
                    setIcon(getLeafIcon());
                } else if (expanded) {
                    setIcon(getOpenIcon());
                } else {
                    setIcon(getClosedIcon());
                }
            }
            
            setComponentOrientation(tree.getComponentOrientation());
            setText(value.toString());      

            return this;
        }
    }
}
