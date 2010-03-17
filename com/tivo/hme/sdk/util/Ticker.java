//////////////////////////////////////////////////////////////////////
//
// File: Ticker.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sdk.util;

// REMIND: This needs to use a Heap/priority queue or at least a binary tree.

/**
 * An interval timer class.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
public class Ticker extends Thread
{
    public static Ticker master = new Ticker("master", Thread.NORM_PRIORITY + 1);

    Entry entries;                      // sorted list of entries
    Entry current;                      // one being executed right now

    static public interface Client {
        long tick(long tm, Object arg);
    }

    static class Entry
    {
        Entry next;
        Client client;
        long tm;
        Object arg;
        public String toString() {
            return "Entry[" + client + ", " + tm + " " + (System.currentTimeMillis() - tm) + "]";
        }
    }

    public Ticker(String name, int priority)
    {
        super(name);
        setPriority(priority);
        start();
    }

    /**
     * Adds the specified client/arg combination to the task queue.
     */
    public synchronized void add(Client client, long tm, Object arg)
    {
        Entry e = unlinkEntry(client, arg);
        if (e == null) {
            e = new Entry();
            e.client = client;
            e.tm = tm;
            e.arg = arg;
        } else {
            e.tm = tm;
        }
        insert(e);
    }

    public synchronized boolean remove(Client client, Object arg)
    {
        boolean OK = unlinkEntry(client, arg) != null;
        notify();
        return OK;
    }

    private void insert(Entry entry)
    {
        Entry e, prev = null;
        for (e = entries; e != null; prev = e, e = e.next) {
            if (e.tm > entry.tm) {
                break;
            }
        }
        if (prev == null) {
            entry.next = entries;
            entries = entry;
        } else {
            prev.next = entry;
            entry.next = e;
        }
        notify();
    }

    private Entry unlinkEntry(Client client, Object arg)
    {
        if (current != null && current.client == client && current.arg == arg) {
            current = null;
            return null;
        }

        Entry prev = null;
        for (Entry e = entries; e != null; prev = e, e = e.next) {
            if (e.client == client && e.arg == arg) {
                if (prev == null) {
                    entries = e.next;
                } else {
                    prev.next = e.next;
                }
                return e;
            }
        }
        return null;
    }

    public void run()
    {
        while (true) {
            long now;
            Entry entry = null;
            // wait for an entry
            synchronized (this) {
                try {
                    while (true) {
                        now = System.currentTimeMillis();
                        if (entries == null) {
                            wait();
                            continue;
                        } else if (entries.tm > now) {
                            wait(entries.tm - now);
                            continue;
                        } else {
                            current = entry = entries;
                            entries = entries.next;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }

            try {
                long tm = entry.client.tick(now, entry.arg);
                synchronized (this) {
                    if (current == entry && tm > 0) {
                        entry.tm = tm;
                        insert(entry);
                    }
                    current = null;
                }
            } catch (ThreadDeath d) {
                return;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
