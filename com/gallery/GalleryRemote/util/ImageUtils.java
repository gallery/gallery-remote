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
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifDirectory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.ImageIcon;

/**
 *  Interface to common image manipulation routines
 *
 *@author     paour
 *@created    September 1, 2002
 */

public class ImageUtils {
	public static final String MODULE = "ImageUtils";

	static ArrayList toDelete = new ArrayList();
	static long totalTime = 0;
	static int totalIter = 0;

	static boolean useIM = false;
	static String imPath = null;
	static int jpegQuality = 75;
	static boolean imIgnoreErrorCode = false;

	static boolean useJpegtran = false;
	static String jpegtranPath = null;
	static boolean jpegtranIgnoreErrorCode = false;

	static File tmpDir = null;

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
			StringBuffer cmdline = new StringBuffer(imPath);

			cmdline.append(" -size ").append(d.width).append("x").append(d.height);

			if (filterName[usage] != null && filterName[usage].length() > 0) {
				cmdline.append(" -filter ").append(filterName[usage]);
			}

			cmdline.append(" \"").append(filename).append("\"");

			cmdline.append(" -resize \"").append(d.width).append("x").append(d.height).append("\"");

			cmdline.append(" +profile \"*\" ");

			File temp = deterministicTempFile("thumb", "." + format[usage], tmpDir, filename + d);

			if (! temp.exists()) {
				toDelete.add(temp);

				cmdline.append("\"" +temp.getPath() + "\"");

				int exitValue = exec(cmdline.toString());

				if (exitValue != 0 && ! imIgnoreErrorCode) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "ImageMagick doesn't seem to be working. Disabling");
					useIM = false;
				} else {
					r = new ImageIcon(temp.getPath());
				}
			} else {
				r = new ImageIcon(temp.getPath());
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
		Log.log(Log.LEVEL_TRACE, MODULE, "Time: " + time + " - Avg: " + (totalTime/totalIter) );

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

				cmdline.append(" -resize \"").append(d.width).append("x").append(d.height).append(">\"");

				//cmdline.append("-gravity SouthEast -draw \"image Over 200,200 0,0 G:\\Projects\\Dev\\gallery_remote10\\2ni.png\" ");

				cmdline.append(" -quality ").append(jpegQuality);

				r = File.createTempFile("res"
						, "." + GalleryFileFilter.getExtension(filename), tmpDir);
				toDelete.add(r);

				cmdline.append(" \"").append(r.getPath()).append("\"");

				int exitValue = exec(cmdline.toString());

				if (exitValue != 0 && ! imIgnoreErrorCode) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "ImageMagick doesn't seem to be working. Disabling");
					useIM = false;
					r = null;
				}
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		if ( ! useIM && r == null ) {
			throw new UnsupportedOperationException("IM must be installed for this operation");
		}

		long time = System.currentTimeMillis() - start;
		totalTime += time;
		totalIter++;
		Log.log(Log.LEVEL_TRACE, MODULE, "Time: " + time + " - Avg: " + (totalTime/totalIter) );

		return r;
	}

	public static File rotate( String filename, int angle, boolean flip, boolean resetExifOrientation) {
		File r = null;

		if ( ! GalleryFileFilter.canManipulateJpeg(filename) ) {
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

				/*if (resetExifOrientation) {
				resetExifOrientation(filename);
				}*/
			} catch (IOException e1) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e1);
			}
		}

		if ( ! useJpegtran && r == null ) {
			throw new UnsupportedOperationException("jpegtran must be installed for this operation");
		}

		return r;
	}

	private static File jpegtranExec(String filename, String command) throws IOException {
		File r;
		StringBuffer cmdline = new StringBuffer(jpegtranPath);

		cmdline.append(" -copy all");

		cmdline.append(command);

		r = File.createTempFile("rot"
				, "." + GalleryFileFilter.getExtension(filename), tmpDir);
		toDelete.add(r);

		cmdline.append(" -outfile \"").append(r.getPath()).append("\"");

		cmdline.append(" \"").append(filename).append("\"");

		int exitValue = exec(cmdline.toString());

		if (exitValue != 0 && ! jpegtranIgnoreErrorCode) {
			Log.log(Log.LEVEL_CRITICAL, MODULE, "jpegtran doesn't seem to be working. Disabling");
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

	public static AngleFlip getExifTargetOrientation(String filename) {
		if (GalleryFileFilter.canManipulateJpeg(filename)) {
			File jpegFile = new File(filename);
			try {
				Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);

				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				String orientation = exifDirectory.getString(ExifDirectory.TAG_ORIENTATION);

				if (orientation == null) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " has no EXIF ORIENTATION tag");
					return null;
				} else {
					Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " EXIF ORIENTATION: " + orientation);

					int or = 0;
					AngleFlip af = null;
					try {
						or = Integer.parseInt(orientation);
					} catch (NumberFormatException e) {
						Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't parse orientation " + orientation + " for " + filename);
						return null;
					}

					switch (or) {
						case 1:
							af = new AngleFlip(0, false);
							break;

						case 2:
							af = new AngleFlip(0, true);
							break;

						case 3:
							af = new AngleFlip(2, false);
							break;

						case 4:
							af = new AngleFlip(2, true);
							break;

						case 5:
							af = new AngleFlip(1, true);
							break;

						case 6:
							af = new AngleFlip(1, false);
							break;

						case 7:
							af = new AngleFlip(3, true);
							break;

						case 8:
							af = new AngleFlip(3, false);
							break;

						default:
							Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't parse orientation " + orientation + " for " + filename);
							return null;
					}

					Log.log(Log.LEVEL_TRACE, MODULE, "Angle: " + af.angle + " Flipped: " + af.flip);
					return af;
				}
			} catch (FileNotFoundException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
				return null;
			} catch (JpegProcessingException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
				return null;
			}
		} else {
			return null;
		}
	}

	public static void resetExifOrientation(String filename) {
		if (GalleryFileFilter.canManipulateJpeg(filename)) {
			File jpegFile = new File(filename);
			try {
				Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);

				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				exifDirectory.setString(ExifDirectory.TAG_ORIENTATION, "1");

				// todo: this doesn't do anything at present: the library can only READ
				// EXIF, not write to it...
			} catch (FileNotFoundException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} catch (JpegProcessingException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}
	}

	static {
		tmpDir = new File(System.getProperty("java.io.tmpdir"), "thumbs");

		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}

		Log.log(Log.LEVEL_INFO, MODULE, "tmpDir: " + tmpDir.getPath());

		// Making sure ImageMagick works
		try {
			PropertiesFile p = null;
			if (new File("imagemagick/im.properties").exists()) {
				p = new PropertiesFile("imagemagick/im");
			} else {
				p = new PropertiesFile("im");
			}

			useIM = p.getBooleanProperty("enabled");
			Log.log(Log.LEVEL_INFO, MODULE, "useIM: " + useIM);
			if (useIM) {
				imPath = p.getProperty("imConvertPath");
				Log.log(Log.LEVEL_INFO, MODULE, "imPath: " + imPath);

				imIgnoreErrorCode = p.getBooleanProperty("ignoreErrorCode", imIgnoreErrorCode);
				Log.log(Log.LEVEL_INFO, MODULE, "imIgnoreErrorCode: " + imIgnoreErrorCode);

				if (imPath.indexOf("/") == -1 && imPath.indexOf("\\") == -1) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "ImageMagick path is not fully qualified, " +
							"presence won't be tested until later");
				} else 	if (! new File(imPath).exists()) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "Can't find ImageMagick Convert at the above path");
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
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
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
			PropertiesFile p = null;
			if (new File("jpegtran/jpegtran.properties").exists()) {
				p = new PropertiesFile("jpegtran/jpegtran");
			} else {
				p = new PropertiesFile("jpegtran");
			}

			useJpegtran = p.getBooleanProperty("enabled");
			Log.log(Log.LEVEL_INFO, MODULE, "useJpegtran: " + useJpegtran);
			if (useJpegtran) {
				jpegtranPath = p.getProperty("jpegtranPath");
				Log.log(Log.LEVEL_INFO, MODULE, "jpegtranPath: " + jpegtranPath);

				jpegtranIgnoreErrorCode = p.getBooleanProperty("ignoreErrorCode", jpegtranIgnoreErrorCode);
				Log.log(Log.LEVEL_INFO, MODULE, "jpegtranIgnoreErrorCode: " + jpegtranIgnoreErrorCode);

				if (jpegtranPath.indexOf("/") == -1 && jpegtranPath.indexOf("\\") == -1) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "jpegtran path is not fully qualified, " +
							"presence won't be tested until later");
				} if (! new File(jpegtranPath).exists()) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "Can't find jpegtran at the above path");
					useJpegtran = false;
				}
			}
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			useJpegtran = false;
		}
	}


	public static void purgeTemp()
	{
		for (Iterator it = toDelete.iterator(); it.hasNext();) {
			File file = (File) it.next();
			file.delete();
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

	public static class AngleFlip {
		public int angle = 0;
		public boolean flip = false;

		public AngleFlip(int angle, boolean flip) {
			this.angle = angle;
			this.flip = flip;
		}
	}

	public static File deterministicTempFile(String prefix, String suffix, File directory, String hash) {
		if (directory == null) {
			directory = new File(System.getProperty("java.io.tmpdir"));
		}

		return new File(directory, prefix + hash.hashCode() + suffix);
	}

	public static int exec(String cmdline) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Executing " + cmdline.toString());

		try {
			Process p = Runtime.getRuntime().exec(cmdline.toString());

			DataInputStream out = new DataInputStream(new BufferedInputStream(p.getInputStream()));
			DataInputStream err = new DataInputStream(new BufferedInputStream(p.getErrorStream()));

			int exitValue = p.waitFor();

			String line = null;
			while ((line = out.readLine()) != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Out: " + line);
			}

			while ((line = err.readLine()) != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Err: " + line);
			}

			Log.log(Log.LEVEL_TRACE, MODULE, "Returned with value " + exitValue);

			return exitValue;
		} catch (InterruptedException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (IOException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}

		return 1;
	}

	/* ********* Utilities ********** */
	public static List expandDirectories( List filesAndFolders )
		throws IOException {
		ArrayList allFilesList = new ArrayList();

		Iterator iter = filesAndFolders.iterator();
		while ( iter.hasNext() ) {
			File f = (File) iter.next();
			if ( f.isDirectory() ) {
				allFilesList.addAll( listFilesRecursive( f ) );
			} else {
				allFilesList.add( f );
			}
		}

		return allFilesList;
	}

	public static java.util.List listFilesRecursive( File dir )
		throws IOException {
		ArrayList ret = new ArrayList();

		/* File.listFiles: stupid call returns null if there's an
				   i/o exception *or* if the file is not a directory, making a mess.
				   http://java.sun.com/j2se/1.4/docs/api/java/io/File.html#listFiles() */
		File[] fileArray = dir.listFiles();
		if ( fileArray == null ) {
			if ( dir.isDirectory() ) {
				/* convert to exception */
				throw new IOException( "i/o exception listing directory: " + dir.getPath() );
			} else {
				/* this method should only be called on a directory */
				Log.log( Log.LEVEL_CRITICAL, MODULE, "assertion failed: listFilesRecursive called on a non-dir file" );
				return ret;
			}
		}

		java.util.List files = Arrays.asList( fileArray );

		Iterator iter = files.iterator();
		while ( iter.hasNext() ) {
			File f = (File) iter.next();
			if ( f.isDirectory() ) {
				ret.addAll( listFilesRecursive( f ) );
			} else {
				ret.add( f );
			}
		}

		return ret;
	}
}
