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

import com.gallery.GalleryRemote.*;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Interface to common image manipulation routines
 * 
 * @author paour
 * @created September 1, 2002
 */

public class ImageUtils {
	public static final String MODULE = "ImageUtils";

	static ArrayList toDelete = new ArrayList();
	static long totalTime = 0;
	static int totalIter = 0;

	public static boolean useIM = false;
	static String imPath = null;
	static int jpegQuality = 75;
	static boolean imIgnoreErrorCode = false;

	public static boolean useJpegtran = false;
	static String jpegtranPath = null;
	static boolean jpegtranIgnoreErrorCode = false;

	static File tmpDir = null;

	public static final int THUMB = 0;
	public static final int PREVIEW = 1;
	public static final int UPLOAD = 2;

	static String[] filterName = new String[3];
	static String[] format = new String[3];

	//public final static String DEFAULT_IMAGE = "img/default.gif";
	//public final static String UNRECOGNIZED_IMAGE = "img/default.gif";
	public final static String DEFAULT_RESOURCE = "/default.gif";
	public final static String UNRECOGNIZED_RESOURCE = "/default.gif";

	public static ImageIcon defaultThumbnail = null;
	public static ImageIcon unrecognizedThumbnail = null;

	public static boolean deferredStopUsingIM = false;
	public static boolean deferredStopUsingJpegtran = false;

	public static ImageIcon load(String filename, Dimension d, int usage) {
		return load(filename, d, usage, false);
	}

	public static ImageIcon load(String filename, Dimension d, int usage, boolean ignoreFailure) {
		if (!new File(filename).exists()) {
			return null;
		}

		ImageIcon r = null;
		long start = System.currentTimeMillis();

		if (!GalleryFileFilter.canManipulate(filename)) {
			return unrecognizedThumbnail;
		}

		if (useIM) {
			//StringBuffer cmdline = new StringBuffer(imPath);
			ArrayList cmd = new ArrayList();
			cmd.add(imPath);

			//cmdline.append(" -size ").append(d.width).append("x").append(d.height);
			cmd.add("-size");
			cmd.add(d.width + "x" + d.height);

			if (filterName[usage] != null && filterName[usage].length() > 0) {
				//cmdline.append(" -filter ").append(filterName[usage]);
				cmd.add("-filter");
				cmd.add(filterName[usage]);
			}

			//cmdline.append(" \"").append(filename).append("\"");
			cmd.add(filename);

			//cmdline.append(" -resize \"").append(d.width).append("x").append(d.height).append("\"");
			cmd.add("-resize");
			cmd.add(d.width + "x" + d.height);

			//cmdline.append(" +profile \"*\" ");
			cmd.add("+profile");
			cmd.add("*");

			File temp = deterministicTempFile("thumb", "." + format[usage], tmpDir, filename + d);

			if (!temp.exists()) {
				toDelete.add(temp);

				//cmdline.append("\"" +temp.getPath() + "\"");
				cmd.add(temp.getPath());

				//int exitValue = exec(cmdline.toString());
				int exitValue = exec((String[]) cmd.toArray(new String[0]));

				if (exitValue != 0 && !imIgnoreErrorCode && !ignoreFailure) {
					if (exitValue != -1) {
						// don't kill IM if it's just an InterruptedException
						Log.log(Log.LEVEL_CRITICAL, MODULE, "ImageMagick doesn't seem to be working. Disabling");
						stopUsingIM();
					}
				} else {
					r = new ImageIcon(temp.getPath());
				}
			} else {
				r = new ImageIcon(temp.getPath());
			}
		}

		if (!useIM && r == null) {
			r = javaLoad(filename, d);
		}

		long time = System.currentTimeMillis() - start;
		totalTime += time;
		totalIter++;
		Log.log(Log.LEVEL_TRACE, MODULE, "Time: " + time + " - Avg: " + (totalTime / totalIter));

		return r;
	}

	public static ImageIcon javaLoad(URL url, Dimension d) {
		ImageIcon r = new ImageIcon(url);

		return javaLoadInternal(r, d);
	}

	public static ImageIcon javaLoad(String filename, Dimension d) {
		ImageIcon r = new ImageIcon(filename);

		return javaLoadInternal(r, d);
	}

	private static ImageIcon javaLoadInternal(ImageIcon r, Dimension d) {
		Image scaled = null;
		Dimension newD = getSizeKeepRatio(
				new Dimension(r.getIconWidth(), r.getIconHeight()),
				d);
		scaled = r.getImage().getScaledInstance(newD.width, newD.height, Image.SCALE_FAST);

		r.getImage().flush();
		r.setImage(scaled);
		return r;
	}

	public static File resize(String filename, Dimension d) {
		File r = null;
		long start = System.currentTimeMillis();

		if (!GalleryFileFilter.canManipulateJpeg(filename)) {
			return new File(filename);
		}

		if (useIM) {
			try {
				//StringBuffer cmdline = new StringBuffer(imPath);
				ArrayList cmd = new ArrayList();
				cmd.add(imPath);

				//cmdline.append(" -size ").append(d.width).append("x").append(d.height);
				cmd.add("-size");
				cmd.add(d.width + "x" + d.height);

				if (filterName[UPLOAD] != null && filterName[UPLOAD].length() > 0) {
					//cmdline.append(" -filter ").append(filterName[UPLOAD]);
					cmd.add("-filter");
					cmd.add(filterName[UPLOAD]);
				}

				//cmdline.append(" \"").append(filename).append("\"");
				cmd.add(filename);

				//cmdline.append(" -resize \"").append(d.width).append("x").append(d.height).append(">\"");
				cmd.add("-resize");
				cmd.add(d.width + "x" + d.height);

				//cmdline.append("-gravity SouthEast -draw \"image Over 200,200 0,0 G:\\Projects\\Dev\\gallery_remote10\\2ni.png\" ");

				//cmdline.append(" -quality ").append(jpegQuality);
				cmd.add("-quality");
				cmd.add("" + jpegQuality);

				r = File.createTempFile("res"
						, "." + GalleryFileFilter.getExtension(filename), tmpDir);
				toDelete.add(r);

				//cmdline.append(" \"").append(r.getPath()).append("\"");
				cmd.add(r.getPath());

				int exitValue = exec((String[]) cmd.toArray(new String[0]));

				if (exitValue != 0 && !imIgnoreErrorCode) {
					if (exitValue != -1) {
						// don't kill IM if it's just an InterruptedException
						Log.log(Log.LEVEL_CRITICAL, MODULE, "ImageMagick doesn't seem to be working. Disabling");
						stopUsingIM();
					}
					r = null;
				}
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		if (!useIM && r == null) {
			throw new UnsupportedOperationException("IM must be installed for this operation");
		}

		long time = System.currentTimeMillis() - start;
		totalTime += time;
		totalIter++;
		Log.log(Log.LEVEL_TRACE, MODULE, "Time: " + time + " - Avg: " + (totalTime / totalIter));

		return r;
	}

	public static File rotate(String filename, int angle, boolean flip, boolean resetExifOrientation) {
		File r = null;

		if (!GalleryFileFilter.canManipulateJpeg(filename)) {
			Log.log(Log.LEVEL_TRACE, MODULE, "jpegtran doesn't support rotating anything but jpeg");
			return new File(filename);
		}

		if (useJpegtran) {
			File orig = null;
			File dest = null;
			try {
				if (MainFrame.IS_MAC_OS_X) {
					orig = new File(filename);
					dest = File.createTempFile("tmp"
							, "." + GalleryFileFilter.getExtension(filename), tmpDir);

					orig.renameTo(dest);
					filename = dest.getPath();
				}

				if (flip) {
					r = jpegtranExec(filename, "-flip", "horizontal");
					filename = r.getPath();
				}

				if (angle != 0) {
					r = jpegtranExec(filename, "-rotate", "" + (angle * 90));
				}

				/*if (resetExifOrientation) {
				resetExifOrientation(filename);
				}*/

			} catch (IOException e1) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e1);
			} finally {
				if (orig != null && dest != null) {
					dest.renameTo(orig);
				}
			}
		}

		if (!useJpegtran && r == null) {
			throw new UnsupportedOperationException("jpegtran must be installed for this operation");
		}

		return r;
	}

	private static File jpegtranExec(String filename, String arg1, String arg2) throws IOException {
		File r;
		//StringBuffer cmdline = new StringBuffer(jpegtranPath);
		ArrayList cmd = new ArrayList();
		cmd.add(jpegtranPath);

		//cmdline.append(" -copy all");
		cmd.add("-copy");
		cmd.add("all");
		//cmd.add("-debug");

		//cmdline.append(command);
		//cmd.add(command);
		cmd.add(arg1);
		cmd.add(arg2);

		r = File.createTempFile("rot"
				, "." + GalleryFileFilter.getExtension(filename), tmpDir);
		toDelete.add(r);

		//cmdline.append(" -outfile \"").append(r.getPath()).append("\"");
		//cmdline.append(" -outfile ").append(r.getPath());
		cmd.add("-outfile");
		cmd.add(r.getPath());

		//cmdline.append(" \"").append(filename).append("\"");
		cmd.add(filename);

		//int exitValue = exec(cmdline.toString());
		int exitValue = exec((String[]) cmd.toArray(new String[0]));

		if (exitValue != 0 && !jpegtranIgnoreErrorCode) {
			if (exitValue != -1) {
				// don't kill jpegtran if it's just an InterruptedException
				Log.log(Log.LEVEL_CRITICAL, MODULE, "jpegtran doesn't seem to be working. Disabling");
				stopUsingJpegtran();
			}
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
			transform.translate(-width1 / 2 - (angle == 3 ? width - width1 : 0) + (flipped ? width - width1 : 0) * (angle == 1 ? -1 : 1),
					-height1 / 2 - (angle == 1 ? height - height1 : 0));

			g.drawImage(thumb.getImage(), transform, c);

			thumb = new ImageIcon(vImg);
		}

		return thumb;
	}

	public static LocalInfo getLocalFilenameForPicture(Picture p, boolean full) {
		URL u = null;
		Dimension d = null;

		if (full == false && p.getSizeResized() == null) {
			// no resized version
			return null;
		}

		if (full) {
			u = p.getUrlFull();
			d = p.getSizeFull();
		} else {
			u = p.getUrlResized();
			d = p.getSizeResized();
		}

		String name = u.getPath();
		String ext;

		int i = name.lastIndexOf('/');
		name = name.substring(i + 1);

		i = name.lastIndexOf('.');
		ext = name.substring(i + 1);
		name = name.substring(0, i);
		String filename = name + "." + ext;

		return new LocalInfo(name, ext, filename,
				deterministicTempFile("server", "." + ext, tmpDir, p.getAlbumOnServer().getName() + name + d));
	}

	static class LocalInfo {
		String name;
		String ext;
		String filename;
		File file;

		public LocalInfo(String name, String ext, String filename, File file) {
			this.name = name;
			this.ext = ext;
			this.filename = filename;
			this.file = file;
		}
	}

	public static File download(Picture p, Dimension d, StatusUpdate su) {
		URL pictureUrl = null;
		//Dimension pictureDimension = null;
		File f;
		String filename;
		LocalInfo fullInfo = getLocalFilenameForPicture(p, true);

		if (p.getSizeResized() != null) {
			LocalInfo resizedInfo = getLocalFilenameForPicture(p, false);

			if (d.width > p.getSizeResized().width || d.height > p.getSizeResized().height
					|| fullInfo.file.exists()) {
				pictureUrl = p.getUrlFull();
				//pictureDimension = p.getSizeFull();
				f = fullInfo.file;
				filename = fullInfo.filename;
			} else {
				pictureUrl = p.getUrlResized();
				//pictureDimension = p.getSizeResized();
				f = resizedInfo.file;
				filename = resizedInfo.filename;
			}
		} else {
			pictureUrl = p.getUrlFull();
			//pictureDimension = p.getSizeFull();
			f = fullInfo.file;
			filename = fullInfo.filename;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Going to download " + pictureUrl);

		try {
			URLConnection conn = pictureUrl.openConnection();
			int size = conn.getContentLength();

			if (f.exists()) {
				Log.log(Log.LEVEL_TRACE, MODULE, filename + " already existed: no need to download it again");
				return f;
			}

			su.startProgress(StatusUpdate.LEVEL_BACKGROUND, 0, size,
					GRI18n.getString(MODULE, "down.start", new Object[]{filename}), false);

			Log.log(Log.LEVEL_TRACE, MODULE, "Saving to " + f.getPath());

			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));

			byte[] buffer = new byte[2000];
			int l;
			int dl = 0;
			long t = -1;
			long start = System.currentTimeMillis();
			while ((l = in.read(buffer)) != -1) {
				out.write(buffer, 0, l);
				dl += l;

				long now = System.currentTimeMillis();
				if (t != -1 && now - t > 1000) {
					su.updateProgressValue(StatusUpdate.LEVEL_BACKGROUND, dl);
					su.updateProgressStatus(StatusUpdate.LEVEL_BACKGROUND,
							GRI18n.getString(MODULE, "down.progress",
									new Object[]{filename, new Integer(dl / 1024), new Integer(size / 1024), new Integer((int) (dl / (now - start) * 1000 / 1024))}));

					t = now;
				}

				if (t == -1) {
					t = now;
				}
			}

			in.close();
			out.flush();
			out.close();

			su.stopProgress(StatusUpdate.LEVEL_BACKGROUND,
					GRI18n.getString(MODULE, "down.end", new Object[]{filename}));
		} catch (IOException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			f = null;

			su.stopProgress(StatusUpdate.LEVEL_BACKGROUND, "Downloading failed");
		}

		return f;
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

			// force exception handling if file is missing
			p.read();

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
				} else if (!new File(imPath).exists()) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "Can't find ImageMagick Convert at the above path");
					stopUsingIM();
				}
			}

			if (useIM) {
				filterName[THUMB] = p.getProperty("imThumbnailResizeFilter");
				filterName[PREVIEW] = p.getProperty("imPreviewResizeFilter");
				filterName[UPLOAD] = p.getProperty("imUploadResizeFilter");
				if (filterName[UPLOAD] == null) {
					filterName[UPLOAD] = filterName[PREVIEW];
				}

				format[THUMB] = p.getProperty("imThumbnailResizeFormat", "gif");
				format[PREVIEW] = p.getProperty("imPreviewResizeFormat", "jpg");
				format[UPLOAD] = null;

				jpegQuality = p.getIntProperty("jpegQuality", jpegQuality);
			}
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			stopUsingIM();
		}

		defaultThumbnail = javaLoad(ImageUtils.class.getResource(DEFAULT_RESOURCE),
				GalleryRemote.getInstance().properties.getThumbnailSize());

		unrecognizedThumbnail = javaLoad(ImageUtils.class.getResource(UNRECOGNIZED_RESOURCE),
				GalleryRemote.getInstance().properties.getThumbnailSize());

		// Making sure jpegtran works
		try {
			PropertiesFile p = null;
			if (new File("jpegtran/jpegtran.properties").exists()) {
				p = new PropertiesFile("jpegtran/jpegtran");
			} else {
				p = new PropertiesFile("jpegtran");
			}

			// force exception handling if file is missing
			p.read();

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
				}
				if (!new File(jpegtranPath).exists()) {
					Log.log(Log.LEVEL_CRITICAL, MODULE, "Can't find jpegtran at the above path");
					stopUsingJpegtran();
				}
			}
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			stopUsingJpegtran();
		}
	}


	public static void purgeTemp() {
		for (Iterator it = toDelete.iterator(); it.hasNext();) {
			File file = (File) it.next();
			file.delete();
		}
	}

	public static Dimension getSizeKeepRatio(Dimension source, Dimension target) {
		Dimension result = new Dimension();

		float sourceRatio = (float) source.width / source.height;
		float targetRatio = (float) target.width / target.height;

		if (targetRatio > sourceRatio) {
			result.height = target.height;
			result.width = source.width * target.height / source.height;
		} else {
			result.width = target.width;
			result.height = source.height * target.width / source.width;
		}

		return result;
	}

	public static float getRatio(Dimension source, Dimension target) {
		float widthRatio = (float) target.width / source.width;
		float heightRatio = (float) target.height / source.height;

		if (heightRatio > widthRatio) {
			return widthRatio;
		} else {
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


	public static ImageUtils.AngleFlip getExifTargetOrientation(String filename) {
		try {
			Class c = Class.forName("com.gallery.GalleryRemote.util.ExifImageUtils");
			Method m = c.getMethod("getExifTargetOrientation", new Class[]{String.class});
			return (AngleFlip) m.invoke(null, new Object[]{filename});
		} catch (Throwable e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			return null;
		}
	}

	/* ********* Utilities ********** */
	public static List expandDirectories(List filesAndFolders)
			throws IOException {
		ArrayList allFilesList = new ArrayList();

		Iterator iter = filesAndFolders.iterator();
		while (iter.hasNext()) {
			File f = (File) iter.next();
			if (f.isDirectory()) {
				allFilesList.addAll(listFilesRecursive(f));
			} else {
				allFilesList.add(f);
			}
		}

		return allFilesList;
	}

	public static java.util.List listFilesRecursive(File dir)
			throws IOException {
		ArrayList ret = new ArrayList();

		/* File.listFiles: stupid call returns null if there's an
				   i/o exception *or* if the file is not a directory, making a mess.
				   http://java.sun.com/j2se/1.4/docs/api/java/io/File.html#listFiles() */
		File[] fileArray = dir.listFiles();
		if (fileArray == null) {
			if (dir.isDirectory()) {
				/* convert to exception */
				throw new IOException("i/o exception listing directory: " + dir.getPath());
			} else {
				/* this method should only be called on a directory */
				Log.log(Log.LEVEL_CRITICAL, MODULE, "assertion failed: listFilesRecursive called on a non-dir file");
				return ret;
			}
		}

		java.util.List files = Arrays.asList(fileArray);

		Iterator iter = files.iterator();
		while (iter.hasNext()) {
			File f = (File) iter.next();
			if (f.isDirectory()) {
				ret.addAll(listFilesRecursive(f));
			} else {
				ret.add(f);
			}
		}

		return ret;
	}

	public static int exec(String cmdline) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Executing " + cmdline);

		try {
			Process p = Runtime.getRuntime().exec(cmdline);

			return pumpExec(p);
		} catch (InterruptedException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			return -1;
		} catch (IOException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}

		return 1;
	}

	public static int exec(String[] cmd) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Executing " + Arrays.asList(cmd));

		try {
			Process p = Runtime.getRuntime().exec(cmd);

			return pumpExec(p);
		} catch (InterruptedException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			return -1;
		} catch (IOException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}

		return 1;
	}

	private static int pumpExec(Process p) throws InterruptedException, IOException {
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
	}

	public static void deferredTasks() {
		if (deferredStopUsingIM) {
			deferredStopUsingIM = false;

			stopUsingIM();
		}

		if (deferredStopUsingJpegtran) {
			deferredStopUsingJpegtran = false;

			stopUsingJpegtran();
		}
	}

	static void stopUsingIM() {
		useIM = false;

		if (!GalleryRemote.getInstance().properties.getBooleanProperty(PreferenceNames.SUPPRESS_WARNING_IM)) {
			if (GalleryRemote.getInstance().mainFrame != null
					&& GalleryRemote.getInstance().mainFrame.isVisible()) {
				MessageDialog md = new MessageDialog(
						GRI18n.getString(MODULE, "warningTextIM"),
						GRI18n.getString(MODULE, "warningUrlIM"),
						GRI18n.getString(MODULE, "warningUrlTextIM")
				);

				if (md.dontShow()) {
					GalleryRemote.getInstance().properties.setBooleanProperty(PreferenceNames.SUPPRESS_WARNING_IM, true);
				}
			} else {
				deferredStopUsingIM = true;
			}
		}
	}

	static void stopUsingJpegtran() {
		useJpegtran = false;

		if (!GalleryRemote.getInstance().properties.getBooleanProperty(PreferenceNames.SUPPRESS_WARNING_JPEGTRAN)) {
			if (GalleryRemote.getInstance().mainFrame != null
					&& GalleryRemote.getInstance().mainFrame.isVisible()) {
				MessageDialog md = new MessageDialog(
						GRI18n.getString(MODULE, "warningTextJpegtran"),
						GRI18n.getString(MODULE, "warningUrlJpegtran"),
						GRI18n.getString(MODULE, "warningUrlTextJpegtran")
				);

				if (md.dontShow()) {
					GalleryRemote.getInstance().properties.setBooleanProperty(PreferenceNames.SUPPRESS_WARNING_JPEGTRAN, true);
				}
			} else {
				deferredStopUsingJpegtran = true;
			}
		}
	}

	static class MessageDialog extends JDialog {
		JLabel jIcon = new JLabel();
		JLabel jMessage = new JLabel();
		BrowserLink jURL = new BrowserLink();
		JCheckBox jDontShow = new JCheckBox();
		JButton jOk = new JButton();

		public MessageDialog(String message, String url, String urlText) {
			super(GalleryRemote.getInstance().mainFrame,
					GRI18n.getString(MODULE, "warningTitle"),
					true);

			jIcon.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			getContentPane().setLayout(new GridBagLayout());
			jMessage.setText(message);
			jURL.setText(urlText);
			jURL.setUrl(url);
			jDontShow.setText(GRI18n.getString(MODULE, "warningDontShow"));
			jOk.setText(GRI18n.getString(MODULE, "warningOK"));
			jOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});

			getContentPane().add(jIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
					, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 10), 0, 0));
			getContentPane().add(jMessage, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0
					, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
			getContentPane().add(jURL, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
			getContentPane().add(jDontShow, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
					, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
			getContentPane().add(jOk, new GridBagConstraints(2, 2, 1, 2, 0.0, 0.0
					, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));

			getRootPane().setDefaultButton(jOk);

			pack();

			DialogUtil.center(this, getOwner());

			setVisible(true);
		}

		public boolean dontShow() {
			return jDontShow.isSelected();
		}
	}
}
