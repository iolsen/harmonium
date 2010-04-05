package org.dazeend.harmonium;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

public class ImageHelper
{
	public static Image getImageFromUrl(Harmonium app, String url)
	{
		Image img = null;
		try
		{
	    	String lowerUrl = url.toLowerCase();
			if (lowerUrl.startsWith("http://"))
			{
		    	img = Toolkit.getDefaultToolkit().getImage(new URL(url));
		    	if (!imageIsValid(img))
		    		img = null;
			}
		} 
		catch (Exception e)
		{
			img = null;
	    	if (app.isInDebugMode())
				e.printStackTrace();
		}

		if (app.isInDebugMode())
		{
			if (img == null)
				System.out.println("Failed to fetch image from " + url);
			else
				System.out.println("Successfully fetched image from " + url);
		}
		
		return img;
	}
	
	public static boolean imageIsValid(Image img)
	{
		if (img == null)
			return false;
		
		try
		{
			java.awt.MediaTracker mt = new java.awt.MediaTracker(new java.awt.Canvas());
	    	mt.addImage(img, 0);
    		mt.waitForAll(2000);

    		if (img.getWidth(null) < 1 || img.getHeight(null) < 1)
		    	return false;
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
}
