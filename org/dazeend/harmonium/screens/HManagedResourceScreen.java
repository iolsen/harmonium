package org.dazeend.harmonium.screens;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.dazeend.harmonium.Harmonium;

import com.tivo.hme.bananas.BApplication;
import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public abstract class HManagedResourceScreen extends BScreen {

	private List<Resource> _managedResources;
	private ReentrantLock _lock = new ReentrantLock();
	private Boolean _freeOnExit = true;
	
	protected Harmonium app;

	public HManagedResourceScreen(BApplication app) {
		super(app);
		this.app = (Harmonium) app;
		_managedResources = new ArrayList<Resource>();
	}
	
	protected void doNotFreeResourcesOnExit()
	{
		_freeOnExit = false;
	}

	protected void lockManagedResources()
	{
		_lock.lock();
	}
	
	protected void unlockManagedResources()
	{
		_lock.unlock();
	}
	
	protected void setManagedResource(View view, Resource resource, int flags)
	{
		if (resource != null)
		{
			_managedResources.add(resource);
			if (this.app.isInSimulator())
				System.out.println("setManagedResource:" + resource.toString());
			view.setResource(resource, flags);
		}
	}
	
	protected void setManagedResource(View view, Resource resource)
	{
		if (resource != null)
		{
			_managedResources.add(resource);
			if (this.app.isInSimulator())
				System.out.println("setManagedResource:" + resource.toString());
			view.setResource(resource);
		}
	}
	
	protected void removeManagedResource(Resource resource)
	{
		_lock.lock();
		try {
			if (resource != null)
			{
				resource.remove();
				if (this.app.isInSimulator())
					System.out.println("removeManagedResource:" + resource.toString());
				_managedResources.remove(resource);
			}
		} finally {
			_lock.unlock();
		}
	}
	
	protected void cleanupAllManagedResources()
	{
		_lock.lock();
		try {
			for ( Resource r : _managedResources )
			{
				if (app.isInSimulator())
					System.out.println("removeManagedResource:" + r.toString());
				r.remove();
			}
			_managedResources.clear();
		} finally {
			_lock.unlock();
		}
	}
	
	protected ImageResource createManagedImage(Image arg0) {
		ImageResource ir = createImage(arg0);
		if (this.app.isInSimulator())
			System.out.println("createManagedImage:" + ir.toString());
		_managedResources.add(ir);
		return ir;
	}

	protected ImageResource createManagedImage(String arg0) {
		ImageResource ir = createImage(arg0);
		if (this.app.isInSimulator())
			System.out.println("createManagedImage:" + ir.toString());
		_managedResources.add(ir);
		return ir;
	}

	@Override
	public boolean handleExit() {
		if (_freeOnExit)
		{
			new Thread() {
				public void run() {
					cleanupAllManagedResources();
				}
			}.start();
		}
		return super.handleExit();
	}
}
