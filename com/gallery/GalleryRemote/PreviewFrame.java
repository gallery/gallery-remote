/*
 * Gallery Remote - a File Upload Utility for Gallery 
 *
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2001 Bharat Mediratta
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.gallery.GalleryRemote;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class PreviewFrame extends javax.swing.JFrame {
	public static final String MODULE = "PreviewFrame";
	
	SmartHashtable imageIcons = new SmartHashtable();
	ImageIcon currentImage = null;
	String currentImageFile = null;
	PreviewLoader previewLoader = new PreviewLoader();
	int previewCacheSize = 10;
	
	public void initComponents()
	{
		setTitle("Preview");
		
		setBounds(GalleryRemote.getInstance().properties.getPreviewBounds());
		
		addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					imageIcons.clear();
				}
			}
		);
		
		previewCacheSize = GalleryRemote.getInstance().properties.getIntProperty("previewCacheSize");
	}
	
	public void paint(Graphics g)
	{
		g.clearRect(0, 0, getSize().width, getSize().height);
		
		if (currentImage != null)
		{
			currentImage.paintIcon(getContentPane(), g, getRootPane().getLocation().x, getRootPane().getLocation().y);
		}
	}
	
	public void displayFile(String filename) {
		if (filename == null)
		{
			currentImage = null;
			currentImageFile = null;
			
			repaint();
		}
		else if (! filename.equals(currentImageFile))
		{
			currentImageFile = filename;
			
			ImageIcon r = (ImageIcon) imageIcons.get(filename);
			if (r != null) {
				Log.log(Log.TRACE, MODULE, "Cache hit: " + filename);
				currentImage = r;
				repaint();
			} else {
				Log.log(Log.TRACE, MODULE, "Cache miss: " + filename);
				previewLoader.loadPreview(filename);
			}
		}
	}
	
	public ImageIcon getSizedIconForce(String filename)	{
		ImageIcon r = (ImageIcon) imageIcons.get(filename);
		
		if (r == null)
		{
			/*r = safeNewImageIcon(filename);
			Dimension d = getSizeKeepRatio(new Dimension(r.getIconWidth(), r.getIconHeight()), getRootPane().getSize());
			r.setImage(safeGetScaledInstance(r.getImage(), d.width, d.height, Image.SCALE_FAST));*/
			r = ImageUtils.load( 
				filename, 
				getRootPane().getSize(), 
				ImageUtils.PREVIEW );
			
			Log.log(Log.TRACE, MODULE, "Adding to cache: " + filename);
			imageIcons.put(filename, r);
		}
		
		return r;
	}

	class PreviewLoader implements Runnable
	{
		String iFilename = null;
		boolean stillRunning = false;
		
		public void run()
		{
			Log.log(Log.TRACE, MODULE, "Starting " + iFilename);
			while (iFilename != null)
			{
				String tmpFilename;
				synchronized (iFilename)
				{
					tmpFilename = iFilename;
					iFilename = null;
				}
				
				currentImage = getSizedIconForce(tmpFilename);
			}
			stillRunning = false;

			repaint();
			Log.log(Log.TRACE, MODULE, "Ending");
		}
		
		public void loadPreview(String filename)
		{
			Log.log(Log.TRACE, MODULE, "loadPreview " + filename);
			
			iFilename = filename;
			
			if (! stillRunning)
			{
				stillRunning = true;
				Log.log(Log.TRACE, MODULE,"Calling Start");
				new Thread(this).start();
			}
		}
	}
	
	/*public ImageIcon safeNewImageIcon(String filename)
	{
		Log.log(Log.TRACE, MODULE, "safeNewImageIcon " + filename);
		Log.log(Log.TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		try
		{
			return new ImageIcon(filename);
		}
		catch (OutOfMemoryError e)
		{
			Log.log(Log.ERROR, MODULE, "Caught out of memory error in safeNewImageIcon");
			imageIcons.shrink();
			return safeNewImageIcon(filename);
		}
		finally
		{
			Log.log(Log.TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		}
	}
	
	public Image safeGetScaledInstance(Image image, int width, int height, int mode)
	{
		Log.log(Log.TRACE, MODULE, "safeGetScaledInstance");
		Log.log(Log.TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		try
		{
			Image result = image.getScaledInstance(width, height, mode);
			image.flush();
			return result;
		}
		catch (OutOfMemoryError e)
		{
			Log.log(Log.ERROR, MODULE, "Caught out of memory error in safeGetScaledInstance");
			imageIcons.shrink();
			return safeGetScaledInstance(image, width, height, mode);
		}
		finally
		{
			Log.log(Log.TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		}
	}*/
	

	public class SmartHashtable extends Hashtable
	{
		Vector touchOrder = new Vector();
		
		public Object put(Object key, Object value)
		{
			touch(key);
			super.put(key, value);
			
			Log.log(Log.TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
			/*if (Runtime.getRuntime().freeMemory() < 2000000)
			{
				Log.log(Log.TRACE, MODULE, "Not enough free ram, shrinking...");
				shrink();
				Runtime.getRuntime().gc();
			}
			else */if (previewCacheSize > 0 && touchOrder.size() > previewCacheSize)
			{
				shrink();
			}
			Log.log(Log.TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
			
			return value;
		}
		
		public Object get(Object key)
		{
			Object result = super.get(key);
			
			if (result != null)
			{
				touch(key);
			}
			
			return result;
		}
		
		public void clear()
		{
			super.clear();
			touchOrder.clear();
		}
		
		public void touch(Object key)
		{
			Log.log(Log.TRACE, MODULE, "touch " + key);
			int i = touchOrder.indexOf(key);
			
			if (i != -1)
			{
				touchOrder.remove(i);
			}

			touchOrder.add(key);
		}
		
		public void shrink()
		{
			if (touchOrder.size() == 0)
			{
				Log.log(Log.ERROR, MODULE, "Empty SmartHashtable");
				//throw new OutOfMemoryError();
				return;
			}
			
			Object key = touchOrder.elementAt(0);
			touchOrder.remove(0);
			
			ImageIcon i = (ImageIcon) get(key);
			if (i != null) {
				i.getImage().flush();
				i = null;
			}
			
			remove(key);

			Runtime.getRuntime().gc();
			
			Log.log(Log.TRACE, MODULE, "Shrunk " + key);
		}
	}
}
