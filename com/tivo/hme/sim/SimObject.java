//////////////////////////////////////////////////////////////////////
//
// File: SimObject.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.tivo.hme.sdk.IHmeProtocol;

/**
 * A SimObject is either a view or a resource. Each object has a containing app,
 * a list of parents, and a list of children. Each SimObject knows how to do
 * several things:
 *
 * 1) paint itself and children
 * 2) ask parents to repaint
 * 3) display inside the tree widget (getIcon/toString)
 * 4) fire events to the tree when things change
 *
 * The comments below refer to three different kinds of coordinates:
 * 
 * 1) absolute coordinates are on screen (0..640)
 * 2) child coordinates might be translated and scaled
 * 3) parent coordinates are untranslated and unscaled
 *
 * SimObject is a bit complicated because it supports multiple parents. If there
 * are multiple parents each point in child space is drawn in multiple locations
 * in absolute space.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings("unchecked")
abstract class SimObject implements IHmeProtocol
{
    //
    // empty array that we reuse everywhere
    //
    
    static SimObject EMPTY[] = new SimObject[0];

    //
    // map classes to names, for use by toString()
    //
    
    static Map names = new HashMap();

    //
    // the app that contains this object, and our id within that app.
    //
    
    SimApp app;
    int id;

    //
    // list of parents
    //
    
    int nparents;
    SimObject parents[] = EMPTY;

    //
    // list of children
    //
    
    int nchildren;
    SimObject children[] = EMPTY;

    //
    // the portion of this object that is in need of repainting.
    //
    
    Rectangle dirty;

    SimObject(SimApp app, int id)
    {
        this.app = app;
        this.id = id;
    }


    
    //
    // manage list of parents/children. These methods fire events to the tree
    // widget when things change.
    //

    /**
     * Add a parent to the list.
     */
    void addParent(SimObject parent)
    {
        if (nparents == parents.length) {
            System.arraycopy(parents, 0, parents = new SimObject[nparents + 5], 0, nparents);
        }
        parents[nparents++] = parent;
    }

    /**
     * Remove a parent from the list.
     */
    void removeParent(SimObject parent)
    {
        for (int i = 0; i < nparents; i++) {
            if (parents[i] == parent) {
                if (i < nparents - 1) {
                    System.arraycopy(parents, i + 1, parents, i, nparents - i - 1);
                }
                parents[--nparents] = null;
                break;
            }
        }
    }

    /**
     * Returns the view depth for this object.
     */
    short getViewDepth()
    {
        return 0;
    }

    /**
     * Add a child to the end of the list.
     */
    void addChild(SimObject child)
    {
        addChild(child, nchildren);
    }

    /**
     * Add a child to the list. Fires an event to the tree indicating that
     * something changed.
     */
    synchronized void addChild(SimObject child, int pos)
    {
        if (nchildren == children.length) {
            System.arraycopy(children, 0, children = new SimObject[nchildren + 5], 0, nchildren);
        }
        if (pos < nchildren) {
            System.arraycopy(children, pos, children, pos + 1, nchildren - pos);
        }
        children[pos] = child;
        ++nchildren;

        child.addParent(this);
        fireEvent(SimTree.INSERTED, child, pos);
    }

    /**
     * Remove a child from the list. Fires an event to the tree indicating that
     * something changed.
     */
    synchronized void removeChild(SimObject child)
    {
        for (int i = 0; i < nchildren; i++) {
            if (children[i] == child) {
                fireEvent(SimTree.REMOVED, child, i);
                child.removeParent(this);
                if (i < nchildren - 1) {
                    System.arraycopy(children, i + 1, children, i, nchildren - i - 1);
                }
                children[--nchildren] = null;
                break;
            }
        }
    }

    synchronized SimObject getChild(int index)
    {
        return (index < nchildren) ? children[index] : null;
    }

    /**
     * Find a child in the list.
     */
    int getIndexOfChild(Object child)
    {
        SimObject obj = (SimObject)child;
        for (int i = nchildren; i-- > 0;) {
            if (children[i] == obj) {
                return i;
            }
        }
        return -1;
    }


    
    //
    // painting
    //

    /**
     * Repaint the whole object immediately.
     */
    void repaint()
    {
        repaint(null, true);
    }

    /**
     * Repaint the whole object. If flush is true, repaint immediately.
     */
    void repaint(boolean flush)
    {
        repaint(null, flush);
    }
    
    /**
     * Repaint all or part of the object immediately.
     */
    void repaint(Rectangle bounds)
    {
        repaint(bounds, true);
    }

    /**
     * Repaint all or part of the object. Basically, just ask the parents to
     * repaint. If flush is true, repaint immediately.
     */
    void repaint(Rectangle bounds, boolean flush)
    {
        // calculate bounds
        if (bounds == null) {
            bounds = getDrawingBounds();
        }
        toParentSpace(bounds);

        // update dirty
        if (dirty == null) {
            dirty = bounds;
        } else if (bounds != null) {
            dirty = dirty.union(bounds);
        }

        // and flush dirty rect if necessary
        if (flush) {
            for (int i = nparents; i-- > 0;) {
                parents[i].repaint(dirty, true);
            }
            dirty = null;
        }
    }

    /**
     * Paint this object and all children.
     */
    void paint(SimObject parent, Graphics2D g)
    {
        Graphics2D g2 = getChildGraphics(g);
        if (g2 != null) {
            try {
                paintHME(parent, g2);
                for (int i = 0; i < nchildren; ++i) {
                    children[i].paint(this, g2);
                }
            } finally {
                if (g != g2) {
                    g2.dispose();
                }
            }
        }
    }

    /**
     * Paint this object.
     */
    protected void paintHME(SimObject parent, Graphics2D g)
    {
    }


    
    //
    // coord operations
    //

    /**
     * Convert each of the rectangles in the list into absolute coordinates on
     * screen. Note that since we might have multiple parents each rectangle
     * might turn into multiple rectangles.
     */
    List toAbsoluteRectangles(List list)
    {
        // move each rect into parent space
        for (Iterator i = list.iterator(); i.hasNext();) {
            toParentSpace((Rectangle)i.next());
        }
        // and now ask parents to make each rectangle absolute
        List result = new Vector();
        for (int i = nparents; i-- > 0;) {
            result.add(parents[i].toAbsoluteRectangles(list));
        }
        return result;
    }

    /**
     * Returns true if the point is inside this object. Point is in child
     * coordinates.
     */
    boolean contains(Point p)
    {
        Rectangle r = getDrawingBounds();
        return (r == null) ? true : r.contains(p);
    }

    /**
     * Find all objects under point. That includes this object and any children
     * that happen to be under point. Point is in child coordinates. The result
     * is a list of paths. Note that "path" is built up recursively and added to
     * list whenever a match is found.
     */
    void findObjectsAt(Point p, List path, List list)
    {
        p = new Point(p);
        toChildSpace(p);
        if (contains(p)) {
            path.add(this);
            for (int i = nchildren; i-- > 0;) {
                children[i].findObjectsAt(p, path, list);
            }
            list.add(new Vector(path));
            path.remove(path.size() - 1);
        }
    }


    
    //
    // coord overrides
    //

    /**
     * Convert graphics to child space.
     */
    abstract Graphics2D getChildGraphics(Graphics2D g);

    /**
     * Get the rectangle that we draw into for this object, in child space.
     */
    abstract Rectangle getDrawingBounds();

    /**
     * Get the list of absolute rectangles for this object.
     */
    abstract List getAbsoluteBounds();

    /**
     * Convert a point from parent into child space.
     */
    abstract void toChildSpace(Point p);

    /**
     * Convert a point from child into parent space.
     */
    abstract void toParentSpace(Rectangle r);


    
    //
    // event helpers
    //

    /**
     * Fire an event to all parents. Use app.path to build the tree path if
     * possible.
     */
    void fireEvent(int action, Object src, int index)
    {
        Object path[] = (app != null) ? app.path : new Object[16];
        fireEvent(action, path, 0, src, index);
    }

    /**
     * Fire an event to all parents. The tree path will be accumulated inside
     * path.
     */
    void fireEvent(int action, Object path[], int len, Object src, int index)
    {
        if (path.length == len) {
            System.arraycopy(path, 0, path = new Object[len * 2], 0, len);
        }
        path[len++] = this;
        if (nparents == 0) {
            Simulator.get().tree.fireEvent(action, path, len, src, index);
        } else {
            for (int i = nparents; i-- > 0;) {
                parents[i].fireEvent(action, path, len, src, index);
            }
        }
    }

    /**
     * Something changed - fire an event.
     */
    void touch()
    {
        for (int i = nparents; i-- > 0;) {
            parents[i].fireEvent(SimTree.CHANGED, this, parents[i].getIndexOfChild(this));
        }
    }


    
    //
    // getIcon + toString
    //

    String getIcon()
    {
        return null;
    }

    String getClassName()
    {
        Class clazz = getClass();
        String nm = (String)names.get(clazz);
        if (nm == null) {
            nm = clazz.getName();
            nm = nm.substring(nm.lastIndexOf('.') + 1);
            int i = nm.lastIndexOf('$');
            if (i != -1) {
                nm = nm.substring(i + 1);
            }
            if (nm.endsWith("Resource")) {
                nm = nm.substring(0, nm.length() - "Resource".length());
            }
            if (nm.endsWith("Stream")) {
                nm = nm.substring(0, nm.length() - "Stream".length());
            }
            names.put(clazz, nm);
        }
        return nm;
    }

    void toString(StringBuffer buf)
    {
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(getClassName());
        buf.append('[');
        if (id != -1) {
            buf.append('#');
            buf.append(id);
        }
        toString(buf);
        buf.append("]");
        return buf.toString();
    }
}
