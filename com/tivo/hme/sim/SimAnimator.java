//////////////////////////////////////////////////////////////////////
//
// File: SimAnimator.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.util.*;

import com.tivo.hme.sdk.util.*;

/**
 * SimAnimator is a singleton that contains all currently running animations in
 * the simulator.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin 
 */
@SuppressWarnings("unchecked")
class SimAnimator implements Ticker.Client
{
    // how often the animator should run
    final static int ANIMATION_INTERVAL = 1000 / 10;

    private static SimAnimator master = new SimAnimator();

    //
    // list of animations
    //
    
    Vector animations = new Vector();

    //
    // if true, the list of animations was "dirtied" while executing a specific
    // animation (ie - remove()). Discard the iterator and start over.
    //
    
    boolean dirty;

    /**
     * Accessor for the singleton.
     */
    static SimAnimator get()
    {
        return master;
    }

    /**
     * Add an animation to the list.
     */
    void add(Animation anim)
    {
        synchronized (Simulator.get().sim) {
            animations.addElement(anim);

            // kick off
            if (animations.size() == 1) {
                Ticker.master.add(this, System.currentTimeMillis(), null);
            }
        }
    }

    /**
     * Remove an animation from the list. Animations which are owned by the
     * SimObject will be removed. If clazz is not null then only animations
     * which are of the given clazz will be removed.
     */
    void remove(SimObject object, Class clazz)
    {
        synchronized (Simulator.get().sim) {
            for (int i = animations.size(); i-- > 0; ) {
                Animation a = (Animation)animations.elementAt(i);
                if (a.object == object) {
                    if (clazz == null || a.getClass() == clazz) {
                        animations.removeElementAt(i);
                        dirty = true;
                    }
                }
            }
        }
    }

    /**
     * Callback from the timer thread - run all animations once.
     */
    public long tick(long tm, Object arg)
    {
        synchronized (Simulator.get().sim) {
            do {
                dirty = false;
                for (Iterator i = animations.iterator() ; i.hasNext() ; ) {
                    Animation a = (Animation)i.next();

                    //
                    // run the animation
                    //
                    
                    boolean again = a.next(tm);

                    //
                    // is our list dirty? if so throw away the iterator and
                    // restart.
                    //
                    
                    if (dirty) {
                        break;
                    }

                    //
                    // if the animation doesn't want to run again, remove it
                    // from the list.
                    //
                    
                    if (!again) {
                        i.remove();
                    }
                }
            } while (dirty);

            //
            // keep going as long as there are animations
            //
            
            return (animations.size() > 0) ? System.currentTimeMillis() + ANIMATION_INTERVAL : -1;
        }
    }

    /**
     * Animation superclass. Each animation has an owning object, a start time,
     * and an animation resource. The Animation class asks the subclass to
     * handle the animation based on a percent (0..1).
     */
    static abstract class Animation
    {
        SimObject object;
        long start;
        SimResource.AnimResource animation;

        Animation(SimObject object, SimResource animation)
        {
            this.object = object;
            this.animation = (SimResource.AnimResource)animation;
            start = System.currentTimeMillis();
        }

        boolean next(long now)
        {
            //
            // figure out how far the animation has progressed
            //
            
            //float duration = animation.duration;
            float percent = (now - start) / (float)animation.duration;
            if (percent > 1.0f) {
                percent = 1.0f;
            } else if (percent < 0.0f) {
                percent = 0.0f;
            }

            //
            // ask the animation resource to interpolate the percentage
            //
            
            percent = animation.interpolate(percent);
            if (percent > 1.0f) {
                percent = 1.0f;
            } else if (percent < 0.0f) {
                percent = 0.0f;
            }

            //
            // animate!
            //
            
            next(percent);

            //
            // are we done yet?
            //
            
            if (percent < 1.0f) {
                return true;
            }

            animation = null;
            return false;
        }

        /**
         * Subclasses must override in order to actually do something.
         */
        abstract void next(double percent);

        public String toString()
        {
            String name = getClass().getName();
            return name.substring(name.lastIndexOf('.') + 1) + "[" + animation + "]";
        }
    }

    /**
     * An animation owned by a view.
     */
    static abstract class ViewAnimation extends Animation
    {
        ViewAnimation(SimView view, SimResource animation)
        {
            super(view, animation);
        }

        SimView getView()
        {
            return (SimView)object;
        }
    }

    /**
     * An animation owned by a resource.
     */
    static abstract class ResourceAnimation extends Animation
    {
        ResourceAnimation(SimResource rsrc, SimResource animation)
        {
            super(rsrc, animation);
        }

        SimResource getResource()
        {
            return (SimResource)object;
        }
    }

    //
    // Animation subclasses.
    //
    
    static class SetBounds extends ViewAnimation
    {
        int ox, oy, ow, oh;
        int dx, dy, dw, dh;

        SetBounds(SimView view, SimResource animation, int x, int y, int w, int h)
        {
            super(view, animation);
            ox = view.x;
            oy = view.y;
            ow = view.width;
            oh = view.height;
            dx = x - view.x;
            dy = y - view.y;
            dw = w - view.width;
            dh = h - view.height;
            master.add(this);
        }
        void next(double percent)
        {
            getView().setBounds((int) (ox + dx * percent), (int) (oy + dy * percent),
                                (int) (ow + dw * percent), (int) (oh + dh * percent));
        }
    }

    static class SetTransparency extends ViewAnimation
    {
        float ot, nt;

        SetTransparency(SimView view, SimResource animation, float transparency)
        {
            super(view, animation);
            ot = view.transparency;
            nt = transparency;
            master.add(this);
        }
        void next(double percent)
        {
            getView().setTransparency(ot + (float)((nt - ot) * percent));
        }
    }

    static class SetTranslation extends ViewAnimation
    {
        int ox, oy;
        int dx, dy;

        SetTranslation(SimView view, SimResource animation, int tx, int ty)
        {
            super(view, animation);
            ox = view.tx;
            oy = view.ty;
            dx = tx - view.tx;
            dy = ty - view.ty;
            master.add(this);
        }
        void next(double percent)
        {
            getView().setTranslation((int) (ox + dx * percent), (int) (oy + dy * percent));
        }
    }

    static class SetScale extends ViewAnimation
    {
        double ox, oy;
        double dx, dy;

        SetScale(SimView view, SimResource animation, double sx, double sy)
        {
            super(view, animation);
            ox = view.sx;
            oy = view.sy;
            dx = sx - view.sx;
            dy = sy - view.sy;
            master.add(this);
        }
        void next(double percent)
        {
            getView().setScale(ox + dx * percent, oy + dy * percent);
        }
    }

    static class SetVisible extends ViewAnimation
    {
        boolean visible;

        SetVisible(SimView view, SimResource animation, boolean visible)
        {
            super(view, animation);
            this.visible = visible;
            master.add(this);
        }
        void next(double percent)
        {
            if (percent == 1.0) {
                getView().setVisible(visible);
            }
        }
    }

    static class Remove extends ViewAnimation
    {
        Remove(SimView view, SimResource animation)
        {
            super(view, animation);
            master.add(this);
        }
        void next(double percent)
        {
            if (percent == 1.0) {
                getView().remove();
            }
        }
    }

    static class SendEvent extends ResourceAnimation
    {
        byte buf[];
        int off;
        int len;
        
        SendEvent(SimResource rsrc, SimResource animation, byte buf[], int off, int len)
        {
            super(rsrc, animation);
            this.buf = buf;
            this.off = off;
            this.len = len;
            master.add(this);
        }
        void next(double percent)
        {
            if (percent == 1.0) {
                getResource().sendEvent(buf, off, len);
            }
        }
    }
}
