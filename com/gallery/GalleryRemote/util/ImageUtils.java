/*
 *  Gallery Remote - a File Upload Utility for Gallery
 *
 *  Gallery - a web based photo album viewer and editor
 *  Copyright (C) 2000-2001 Bharat Mediratta
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryFileFilter;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.PropertiesFile;
import com.gallery.GalleryRemote.GalleryRemote;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;

/**
 *  Interface to common image manipulation routines
 *
 *@author     paour
 *@created    September 1, 2002
 */

public class ImageUtils {
	public static final String MODULE = "ImageUtils";
	
	static Vector toDelete = new Vector();
	static long totalTime = 0;
	static int totalIter = 0;
	static boolean useIM = false;
	static String imPath = null;
	static File tmpDir = null;
	
	public static final int THUMB = 0;
	public static final int PREVIEW = 1;
	public static final int UPLOAD = 2;
	
	static String[] filterName = new String[3];
	static String[] format = new String[3];
	
	public final static String DEFAULT_IMAGE = "default.gif";
	public final static String UNRECOGNIZED_IMAGE = "default.gif";

	public static ImageIcon defaultThumbnail = null;
	public static ImageIcon unrecognizedThumbnail = null;

	/**
	 *  Perform the actual icon loading
	 *
	 *@param  filename  path to the file
	 *@return           Resized icon
	 */
	public static ImageIcon load( String filename, Dimension d, int usage ) {
		ImageIcon r = null;
		long start = System.currentTimeMillis();
		
		if ( ! GalleryFileFilter.canManipulate(filename) ) {
			return unrecognizedThumbnail;
		}
		
		if (useIM) {
			try {
				StringBuffer cmdline = new StringBuffer(imPath);
				cmdline.append(" -size ");
				
				cmdline.append(d.width);
				cmdline.append("x");
				cmdline.append(d.height);
				
				if (filterName[usage] != null && filterName[usage].length() > 0) {
					cmdline.append(" -filter ");
					cmdline.append(filterName[usage]);
				}
				
				cmdline.append(" \"");
				cmdline.append(filename);
				
				cmdline.append("\" -resize ");
				cmdline.append(d.width);
				cmdline.append("x");
				cmdline.append(d.height);
				cmdline.append(" +profile \"*\" ");
				
				File temp = File.createTempFile("thumb", "." + format[usage], tmpDir);
				toDelete.add(temp);
				
				cmdline.append(temp.getPath());
				
				Log.log(Log.TRACE, MODULE, "Executing " + cmdline.toString());
			
				Process p = Runtime.getRuntime().exec(cmdline.toString());
				p.waitFor();
				Log.log(Log.TRACE, MODULE, "Returned with value " + p.exitValue());
				
				if (p.exitValue() != 0) {
					Log.log(Log.CRITICAL, MODULE, "ImageMagick doesn't seem to be working. Disabling");
					useIM = false;
				} else {
					r = new ImageIcon(temp.getPath());
				}
			} catch (IOException e1) {
				Log.logException(Log.ERROR, MODULE, e1);
			} catch (InterruptedException e2) {
				Log.logException(Log.ERROR, MODULE, e2);
			}
		}

		if ( ! useIM && r == null ) {
			r = new ImageIcon( filename );
		
			Image scaled = null;
			Dimension newD = getSizeKeepRatio(
				new Dimension( r.getIconWidth(), r.getIconHeight() ),
				d );
			scaled = r.getImage().getScaledInstance( newD.width, newD.height, Image.SCALE_FAST );
	
			r.getImage().flush();
			r.setImage( scaled );
		}

		long time = System.currentTimeMillis() - start;
		totalTime += time;
		totalIter++;
		Log.log(Log.TRACE, MODULE, "Time: " + time + " - Avg: " + (totalTime/totalIter) );

		return r;
	}
	
	public static File resize( String filename, Dimension d ) {
		File r = null;
		long start = System.currentTimeMillis();
		
		if ( ! GalleryFileFilter.canManipulate(filename) ) {
			return new File(filename);
		}
		
		if (useIM) {
			try {
				StringBuffer cmdline = new StringBuffer(imPath);
				cmdline.append(" -size ");
				
				cmdline.append(d.width);
				cmdline.append("x");
				cmdline.append(d.height);
				
				if (filterName[UPLOAD] != null && filterName[UPLOAD].length() > 0) {
					cmdline.append(" -filter ");
					cmdline.append(filterName[UPLOAD]);
				}
				
				cmdline.append(" \"");
				cmdline.append(filename);
				
				cmdline.append("\" -resize \"");
				cmdline.append(d.width);
				cmdline.append("x");
				cmdline.append(d.height);
				cmdline.append(">\" ");
				
				//cmdline.append("-gravity SouthEast -draw \"image Over 200,200 0,0 G:\\Projects\\Dev\\gallery_remote10\\2ni.png\" ");
				
				r = File.createTempFile("res"
					, "." + GalleryFileFilter.getExtension(filename), tmpDir);
				toDelete.add(r);
				
				cmdline.append(r.getPath());
				
				Log.log(Log.TRACE, MODULE, "Executing " + cmdline.toString());
			
				Process p = Runtime.getRuntime().exec(cmdline.toString());
				p.waitFor();
				Log.log(Log.TRACE, MODULE, "Returned with value " + p.exitValue());
				
				if (p.exitValue() != 0) {
					Log.log(Log.CRITICAL, MODULE, "ImageMagick doesn't seem to be working. Disabling");
					useIM = false;
					r = null;
				}
			} catch (IOException e1) {
				Log.logException(Log.ERROR, MODULE, e1);
			} catch (InterruptedException e2) {
				Log.logException(Log.ERROR, MODULE, e2);
			}
		}

		if ( ! useIM && r == null ) {
			throw new UnsupportedOperationException("IM must be installed for this operation");
		}

		long time = System.currentTimeMillis() - start;
		totalTime += time;
		totalIter++;
		Log.log(Log.TRACE, MODULE, "Time: " + time + " - Avg: " + (totalTime/totalIter) );

		return r;
	}
	
	static {
		tmpDir = new File(System.getProperty("java.io.tmpdir"), "thumbs");
		
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		
		Log.log(Log.INFO, MODULE, "tmpDir: " + tmpDir.getPath());
		
		try {
			PropertiesFile p = new PropertiesFile("imagemagick/im");
			
			useIM = p.getBooleanProperty("enabled");
			Log.log(Log.INFO, MODULE, "useIM: " + useIM);
			if (useIM) {
				imPath = p.getProperty("imConvertPath");
				Log.log(Log.INFO, MODULE, "imPath: " + imPath);
				
				if (! new File(imPath).exists()) {
					Log.log(Log.CRITICAL, MODULE, "Can't find ImageMagick Convert at the above path");
					useIM = false;
				}
			}
			
			if (useIM) {
				filterName[THUMB] = p.getProperty("imThumbnailResizeFilter");
				filterName[PREVIEW] = p.getProperty("imPreviewResizeFilter");
				filterName[UPLOAD] = p.getProperty("imUploadResizeFilter");
				if ( filterName[UPLOAD] == null ) {
					filterName[UPLOAD] = filterName[PREVIEW];
				}

				format[THUMB] = p.getProperty("imThumbnailResizeFormat", "gif");
				format[PREVIEW] = p.getProperty("imPreviewResizeFormat", "jpg");
				format[UPLOAD] = null;
			}
		} catch (Exception e) {
			Log.logException(Log.CRITICAL, MODULE, e);
			useIM = false;
		}

		defaultThumbnail = load(
			DEFAULT_IMAGE,
			GalleryRemote.getInstance().properties.getThumbnailSize(),
			THUMB );
			
		unrecognizedThumbnail = load(
			UNRECOGNIZED_IMAGE,
			GalleryRemote.getInstance().properties.getThumbnailSize(),
			THUMB );
	}

	
	public static void purgeTemp()
	{
		Enumeration e = toDelete.elements();
		while (e.hasMoreElements()) {
			((File) e.nextElement()).delete();
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
	
	public static float getRatio(Dimension source, Dimension target)
	{
		float widthRatio = (float) target.width / source.width;
		float heightRatio = (float) target.height / source.height;
		
		if (heightRatio > widthRatio)
		{
			return widthRatio;
		}
		else
		{
			return heightRatio;
		}
	}
}
