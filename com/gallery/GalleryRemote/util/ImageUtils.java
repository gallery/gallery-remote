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
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.GalleryRemote;

import java.awt.*;
import java.awt.geom.AffineTransform;
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
	static boolean useJpegtran = false;
	static String jpegtranPath = null;
	static File tmpDir = null;
	static int jpegQuality = 75;

	public static final int THUMB = 0;
	public static final int PREVIEW = 1;
	public static final int UPLOAD = 2;
	
	static String[] filterName = new String[3];
	static String[] format = new String[3];
	
	public final static String DEFAULT_IMAGE = "img/default.gif";
	public final static String UNRECOGNIZED_IMAGE = "img/default.gif";

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

				cmdline.append(" -size ").append(d.width).append("x").append(d.height);

				if (filterName[usage] != null && filterName[usage].length() > 0) {
					cmdline.append(" -filter ").append(filterName[usage]);
				}
				
				cmdline.append(" \"").append(filename).append("\"");

				cmdline.append(" -resize \"").append(d.width).append("x").append(d.height).append("\" ");

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

		if ( ! GalleryFileFilter.canManipulateJpeg(filename) ) {
			return new File(filename);
		}

		if (useIM) {
			try {
				StringBuffer cmdline = new StringBuffer(imPath);

				cmdline.append(" -size ").append(d.width).append("x").append(d.height);

				if (filterName[UPLOAD] != null && filterName[UPLOAD].length() > 0) {
					cmdline.append(" -filter ").append(filterName[UPLOAD]);
				}

				cmdline.append(" \"").append(filename).append("\"");

				cmdline.append(" -resize \"").append(d.width).append("x").append(d.height).append(">\" ");

				//cmdline.append("-gravity SouthEast -draw \"image Over 200,200 0,0 G:\\Projects\\Dev\\gallery_remote10\\2ni.png\" ");

				cmdline.append(" -quality ").append(jpegQuality);

				r = File.createTempFile("res"
					, "." + GalleryFileFilter.getExtension(filename), tmpDir);
				toDelete.add(r);

				cmdline.append(" \"").append(r.getPath()).append("\"");

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

	public static File rotate( String filename, int angle, boolean flip ) {
		File r = null;

		if ( ! GalleryFileFilter.canManipulate(filename) ) {
			return new File(filename);
		}

		if (useJpegtran) {
			try {
				if (flip) {
					r = jpegtranExec(filename, " -flip horizontal");
					filename = r.getPath();
				}

				if (angle != 0) {
					r = jpegtranExec(filename, " -rotate " + angle * 90);
				}
			} catch (IOException e1) {
				Log.logException(Log.ERROR, MODULE, e1);
			} catch (InterruptedException e2) {
				Log.logException(Log.ERROR, MODULE, e2);
			}
		}

		if ( ! useJpegtran && r == null ) {
			throw new UnsupportedOperationException("jpegtran must be installed for this operation");
		}

		return r;
	}

	private static File jpegtranExec(String filename, String command) throws IOException, InterruptedException {
		File r;
		StringBuffer cmdline = new StringBuffer(jpegtranPath);

		cmdline.append(" -copy all");

		cmdline.append(command);

		cmdline.append(" \"").append(filename).append("\"");

		r = File.createTempFile("res"
			, "." + GalleryFileFilter.getExtension(filename), tmpDir);
		toDelete.add(r);

		cmdline.append(" \"").append(r.getPath()).append("\"");

		Log.log(Log.TRACE, MODULE, "Executing " + cmdline.toString());

		Process p = Runtime.getRuntime().exec(cmdline.toString());
		p.waitFor();
		Log.log(Log.TRACE, MODULE, "Returned with value " + p.exitValue());

		if (p.exitValue() != 0) {
			Log.log(Log.CRITICAL, MODULE, "jpegtran doesn't seem to be working. Disabling");
			useJpegtran = false;
			r = null;
		}

		return r;
	}

	public static ImageIcon rotateImageIcon(ImageIcon thumb, int angle, boolean flipped, Component c) {
		if (angle != 0 || flipped) {
			int width;
			int height;
			int width1;
			int height1;

			width = thumb.getImage().getWidth(c);
			height = thumb.getImage().getHeight(c);

			if (angle % 2 == 0) {
				width1 = width;
				height1 = height;
			} else {
				width1 = height;
				height1 = width;
			}

			Image vImg = c.createImage(width1, height1);

			Graphics2D g = (Graphics2D) vImg.getGraphics();

			AffineTransform transform = AffineTransform.getTranslateInstance(width / 2, height / 2);
			if (angle != 0) {
				transform.rotate(angle * Math.PI / 2);
			}
			if (flipped) {
				transform.scale(-1, 1);
			}
			transform.translate(-width1 / 2 - (angle == 3?width - width1:0) + (flipped?width - width1:0) * (angle == 1?-1:1),
					-height1 / 2 - (angle == 1?height - height1:0));

			g.drawImage(thumb.getImage(), transform, c);

			thumb = new ImageIcon(vImg);
		}

		return thumb;
	}

	static {
		tmpDir = new File(System.getProperty("java.io.tmpdir"), "thumbs");
		
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		
		Log.log(Log.INFO, MODULE, "tmpDir: " + tmpDir.getPath());

		// Making sure ImageMagick works
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

				jpegQuality = p.getIntProperty("jpegQuality", jpegQuality);
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

		// Making sure jpegtran works
		try {
			PropertiesFile p = new PropertiesFile("jpegtran/jpegtran");

			useJpegtran = p.getBooleanProperty("enabled");
			Log.log(Log.INFO, MODULE, "useJpegtran: " + useJpegtran);
			if (useJpegtran) {
				jpegtranPath = p.getProperty("jpegtranPath");
				Log.log(Log.INFO, MODULE, "jpegtranPath: " + jpegtranPath);

				if (! new File(jpegtranPath).exists()) {
					Log.log(Log.CRITICAL, MODULE, "Can't find jpegtran at the above path");
					useJpegtran = false;
				}
			}
		} catch (Exception e) {
			Log.logException(Log.CRITICAL, MODULE, e);
			useJpegtran = false;
		}
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
			result.width = source.width * target.height / source.height;
		}
		else
		{
			result.width = target.width;
			result.height = source.height * target.width / source.width;
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
