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
	SmartHashtable imageIcons = new SmartHashtable();
	ImageIcon currentImage = null;
	String currentImageFile = null;
	PreviewLoader previewLoader = new PreviewLoader();
	int previewCacheSize = 10;

	public void initComponents(Properties mPropertiesFile)
	{
		setTitle("Preview");
		
		if (mPropertiesFile.getProperty("previewx") == null || mPropertiesFile.getProperty("previewy") == null)
		{
			setLocation(new java.awt.Point(578, 0));
		}
		else
		{
			setLocation(new java.awt.Point(Integer.parseInt(mPropertiesFile.getProperty("previewx")), Integer.parseInt(mPropertiesFile.getProperty("previewy"))));
		}
		
		if (mPropertiesFile.getProperty("previewwidth") == null || mPropertiesFile.getProperty("previewheight") == null)
		{
			setSize(new java.awt.Dimension(502, 521));
		}
		else
		{
			setSize(new java.awt.Dimension(Integer.parseInt(mPropertiesFile.getProperty("previewwidth")), Integer.parseInt(mPropertiesFile.getProperty("previewheight"))));
		}
		
		addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					imageIcons.clear();
				}
			}
		);
		
		if (mPropertiesFile.getProperty("previewcachesize") != null)
		{
			previewCacheSize = Integer.parseInt(mPropertiesFile.getProperty("previewcachesize"));
		}
	}
	
	public void paint(Graphics g)
	{
		g.clearRect(0, 0, getSize().width, getSize().height);
		
		if (currentImage == null)
		{
			getSizedIcon(MainFrame.DEFAULT_IMAGE).paintIcon(getContentPane(), g, getRootPane().getLocation().x, getRootPane().getLocation().y);
		}
		else
		{
			currentImage.paintIcon(getContentPane(), g, getRootPane().getLocation().x, getRootPane().getLocation().y);
		}
	}
	
	public void displayFile(String filename)
	{
		if (filename == null)
		{
			currentImage = null;
			currentImageFile = null;
			
			repaint();
		}
		else if (! filename.equals(currentImageFile))
		{
			currentImageFile = filename;
			previewLoader.loadPreview(filename);
		}
	}
	
	public ImageIcon getSizedIcon(String filename)
	{
		if (filename == null)
		{
			return null;
		}
		
		ImageIcon r = (ImageIcon) imageIcons.get(filename);
		
		if (r == null)
		{
			r = safeNewImageIcon(filename);
			Dimension d = getSizeKeepRatio(new Dimension(r.getIconWidth(), r.getIconHeight()), getRootPane().getSize());
			r.setImage(safeGetScaledInstance(r.getImage(), d.width, d.height, Image.SCALE_FAST));
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
			//System.out.println("Starting " + iFilename);
			while (iFilename != null)
			{
				String tmpFilename;
				synchronized (iFilename)
				{
					tmpFilename = iFilename;
					iFilename = null;
				}
				
				currentImage = getSizedIcon(tmpFilename);
			}
			stillRunning = false;

			repaint();
			//System.out.println("Ending");
		}
		
		public void loadPreview(String filename)
		{
			//System.out.println("loadPreview " + filename);
			
			iFilename = filename;
			
			if (! stillRunning)
			{
				stillRunning = true;
				//System.out.println("Calling Start");
				new Thread(this).start();
			}
		}
	}
	
	public ImageIcon safeNewImageIcon(String filename)
	{
		//System.out.println("safeNewImageIcon " + filename);
		//System.out.println(Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		try
		{
			return new ImageIcon(filename);
		}
		catch (OutOfMemoryError e)
		{
			//System.out.println("Caught error");
			imageIcons.shrink();
			return safeNewImageIcon(filename);
		}
		finally
		{
			//System.out.println(Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		}
	}
	
	public Image safeGetScaledInstance(Image image, int width, int height, int mode)
	{
		System.out.println("safeGetScaledInstance");
		System.out.println(Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		try
		{
			Image result = image.getScaledInstance(width, height, mode);
			image.flush();
			return result;
		}
		catch (OutOfMemoryError e)
		{
			//System.out.println("Caught error");
			imageIcons.shrink();
			return safeGetScaledInstance(image, width, height, mode);
		}
		finally
		{
			System.out.println(Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		}
	}
	
	public static Dimension getSizeKeepRatio(Dimension source, Dimension target)
	{
		Dimension result = new Dimension();
		
		float sourceRatio = (float) source.width / source.height;
		float targetRatio = (float) target.width / target.height;
		
		if (targetRatio > sourceRatio)
		{
			result.height = target.height;
			result.width = (int) source.width * target.height / source.height;
		}
		else
		{
			result.width = target.width;
			result.height = (int) source.height * target.width / source.width;
		}

		return result;
	}
	
	public class SmartHashtable extends Hashtable
	{
		Vector touchOrder = new Vector();
		
		public Object put(Object key, Object value)
		{
			touch(key);
			super.put(key, value);
			
			System.out.println(Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
			if (Runtime.getRuntime().freeMemory() < 2000000)
			{
				shrink();
				Runtime.getRuntime().gc();
			}
			else if (previewCacheSize > 0 && touchOrder.size() > previewCacheSize)
			{
				shrink();
			}
			System.out.println(Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
			
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
			//System.out.println("touch " + key);
			int i = touchOrder.indexOf(key);
			
			if (i != -1)
			{
				touchOrder.remove(i);
			}

			touchOrder.add(key);
		}
		
		public void shrink()
		{
			System.out.println("shrink");
			if (touchOrder.size() == 0)
			{
				System.out.println("Empty SmartHashtable");
				//throw new OutOfMemoryError();
				return;
			}
			
			Object key = touchOrder.elementAt(0);
			touchOrder.remove(0);
			remove(key);
		}
	}
}
