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
package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.prefs.GalleryProperties;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.model.Gallery;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.applet.Applet;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.lang.reflect.Method;

/**
 * Main class and entry point of Gallery Remote
 * 
 * @author paour
 */
public abstract class GalleryRemote implements PreferenceNames {
	public static final String MODULE = "GalRem";

	private static GalleryRemote singleton = null;

	/** Default properties, loaded from distribution file
	 * and generally not modified by the user.
	 */
	public PropertiesFile defaults = null;

	/** User properties, saved in a local file */
	public PropertiesFile properties = null;

	protected Applet applet = null;

	public static ImageIcon iLogin;
	public static ImageIcon iNewGallery;
	public static ImageIcon iAbout;
	public static ImageIcon iSave;
	public static ImageIcon iOpen;
	public static ImageIcon iPreferences;
	public static ImageIcon iNewAlbum;
	public static ImageIcon iNew;
	public static ImageIcon iQuit;
	public static ImageIcon iCut;
	public static ImageIcon iCopy;
	public static ImageIcon iPaste;
	public static ImageIcon iUp;
	public static ImageIcon iDown;
	public static ImageIcon iDelete;
	public static ImageIcon iRight;
	public static ImageIcon iLeft;
	public static ImageIcon iFlip;
	public static ImageIcon iComputer;
	public static ImageIcon iUploading;

	public static boolean IS_MAC_OS_X = (System.getProperty("mrj.version") != null);
	public static int ACCELERATOR_MASK = 0;

	protected GalleryRemote() {}

	protected void initializeGR() {
		try {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {	}

			if (Float.parseFloat(System.getProperty("java.specification.version")) < 1.39) {
				JOptionPane.showMessageDialog(null, "Gallery Remote is not supported on Java " +
						"Virtual Machines older than 1.4. Please install a recent VM " +
						"and try running Gallery Remote again.", "VM too old", JOptionPane.ERROR_MESSAGE);

				System.exit(1);
			}

			createProperties();

			// log system properties
			new GalleryProperties(System.getProperties()).logProperties(Log.LEVEL_INFO, "SysProps");

			// log system environment
			new GalleryProperties(System.getenv()).logProperties(Log.LEVEL_INFO, "SysEnv");

			// log properties
			properties.logProperties(Log.LEVEL_TRACE, "UsrProps");

			loadIcons();

			setFontOverrides();
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, "Startup", e);
			Log.shutdown();
			System.err.println("Exception during startup: " + e);
			e.printStackTrace();
		}
	}

	private void setFontOverrides() {
		String name = properties.getProperty(PreferenceNames.FONT_OVERRIDE_NAME);
		if (name != null) {
			int style = properties.getIntProperty(PreferenceNames.FONT_OVERRIDE_STYLE);
			int size = properties.getIntProperty(PreferenceNames.FONT_OVERRIDE_SIZE);
			FontUIResource fur = new FontUIResource(name, style, size);
			UIManager.put("Label.font", fur);
			UIManager.put("List.font", fur);
			UIManager.put("Tree.font", fur);
			UIManager.put("TextArea.font", fur);
			UIManager.put("TextField.font", fur);
			UIManager.put("TextPane.font", fur);
		}
	}

	protected void runGR() {
		getCore().startup();
	}

	public boolean isAppletMode() {
		return applet != null;
	}

	public Applet getApplet() {
		return applet;
	}

	public abstract Frame getMainFrame();

	public abstract GalleryRemoteCore getCore();

	public static GalleryRemote _() {
		return singleton;
	}

	public static boolean createInstance(String className, Applet applet) {
		if (singleton == null) {
			System.out.println("Instanciating Gallery Remote...");

			setStaticProperties();

			try {
				singleton = (GalleryRemote) Class.forName(className).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			singleton.applet = applet;

			//singleton.run();

			return true;
		} else {
			System.err.println("Trying to instanciate Gallery Remote more than once...");
			Thread.dumpStack();

			return false;
		}
	}

	public static void shutdownInstance() {
		System.out.println("Shutting down Gallery Remote");

		singleton = null;
	}

	// Main entry point
	public static void main(String[] args) {
		createInstance("com.gallery.GalleryRemote.GalleryRemoteMainFrame", null);

		_().initializeGR();

		// Analyze command-line
		String url = null;
		String username = null;

		Iterator i = Arrays.asList(args).iterator();
		while (i.hasNext()) {
			String sw = (String) i.next();

			if (sw.equals("-url") && i.hasNext()) {
				url = (String) i.next();
				Log.log(Log.LEVEL_TRACE, MODULE, "Command-line switch: url=" + url);
			} else if (sw.equals("-username") && i.hasNext()) {
				username = (String) i.next();
				Log.log(Log.LEVEL_TRACE, MODULE, "Command-line switch: username=" + username);
			}
		}

		if (url != null) {
			// we got a URL on the command-line
			int j = 0;
			boolean found = false;
			while (_().properties.containsKey(GURL + j)) {
				if (_().properties.getProperty(GURL + j).equals(url)
						&& _().properties.getProperty(USERNAME + j).equals(username)) {
					// we have probably already loaded and saved thus URL, nothing to do
					found = true;
					break;
				}

				j++;
			}

			if (!found) {
				// add it
				_().properties.setProperty(GURL + j, url);

				if (username != null) {
					_().properties.setProperty(USERNAME + j, username);
				}

				_().properties.setBooleanProperty(AUTO_LOAD_ON_STARTUP + j, true);

				try {
					Gallery g = Gallery.readFromProperties(_().properties, j, _().getCore().getMainStatusUpdate());
					if (g != null) {
						_().getCore().getGalleries().addElement(g);
					}
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "Error trying to load Gallery profile " + i);
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
				}
			}
		}

		_().runGR();
	}

	public static void setStaticProperties() {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Gallery Remote");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("apple.awt.showGrowBox", "false");
		System.setProperty("apple.awt.brushMetalLook", "true");
		//System.setProperty("http.agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

		// fix buggy Swing Windows XP lookup code
		System.setProperty("swing.noxp", "true");

		// todo: this should not remain this way
		//System.setProperty("apple.awt.fakefullscreen", "true");

		// this isn't such a good idea, it crashes with some NVidia drivers
		/*try {
			if (Float.parseFloat(System.getProperty("java.specification.version")) >= 1.6) {
				System.setProperty("sun.java2d.opengl", "true");
			}
		} catch (RuntimeException e) {
			Log.log(Log.LEVEL_ERROR, "Couldn't get property java.specification.version: " +
					System.getProperty("java.specification.version"));
		}*/
		
		/*try {
			// purposely not using secure class loading...
			Class unsignedTest = Class.forName("com.gallery.GalleryRemote.insecureutil.UnsignedTest");
			Log.log(Log.LEVEL_TRACE, MODULE, "isSignedByGallery: " + isSignedByGallery(unsignedTest));
			
			if (isSignedByGallery(unsignedTest)) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Not signed by us, we should not execute method... but do it to test...");
			}
			Method createFile = unsignedTest.getMethod("createFile", null);
			createFile.invoke(null, null);
			
			Log.log(Log.LEVEL_TRACE, MODULE, "isSignedByGallery: " + isSignedByGallery(com.gallery.GalleryRemote.Base64.class));
		} catch (Throwable e) {
			System.err.println(e);
		}*/
	}
	
	public static boolean isSignedByGallery(Class c) {
		if (GalleryRemote.class.getSigners() == null) {
			// the main class is unsigned so we can't expect others to be signed
			Log.log(Log.LEVEL_INFO, MODULE, "GalleryRemote is not signed: none of the other classes need to be signed");
			return true;
		}
		
		Object[] signers = c.getSigners();
		if (signers != null && signers instanceof java.security.cert.Certificate[]) {
			java.security.cert.Certificate[] certs = (java.security.cert.Certificate[]) signers;
			
			for (int i = 0; i < certs.length; i++) {
				 if (signers[i].equals(GalleryRemote.class.getSigners()[0])) {
					 return true;
				 }
			}
		}
		
		Log.log(Log.LEVEL_CRITICAL, MODULE, "Could not find matching signature");
		return false;
	}
	
	public static Class secureClassForName(String name) throws ClassNotFoundException {
		Log.log(Log.LEVEL_INFO, MODULE, "Trying to securely load " + name);
		Class c = Class.forName(name);
		if (isSignedByGallery(c)) {
			return c;
		} else {
			throw new ClassNotFoundException("The class is not signed by Gallery, so we're not going to load it");
		}
	}

	public void createProperties() {
		properties = defaults = new PropertiesFile("defaults", "defaults");
		properties.setReadOnly();
	}

	/*public PropertiesFile createAppletOverride(PropertiesFile p) {
		PropertiesFile override;

		if (p == null) {
			override = new PropertiesFile(defaults);
		} else {
			override = new PropertiesFile(p);
		}

		override.setReadOnly();

		return override;
	}*/

	public PropertiesFile getAppletOverrides(PropertiesFile defaults, String prefix) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Getting applet parameters for prefix " + prefix);

		PropertiesFile p = new PropertiesFile(defaults, null, prefix);

		for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			String value = applet.getParameter(prefix + name);

			if (value != null && value.length() != 0) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Got: " + name + "= |" + value + "|");
				p.setProperty(name, value);
			}
		}

		p.setReadOnly();

		return p;
	}

	protected void loadIcons() {
		if (iAbout != null) {
			return;
		}

		try {
			if (!IS_MAC_OS_X) {
				ACCELERATOR_MASK = ActionEvent.CTRL_MASK;
			} else {
				ACCELERATOR_MASK = ActionEvent.META_MASK;
			}

			iComputer = new ImageIcon(GalleryRemote.class.getResource("/computer.gif"));
			iUploading = new ImageIcon(GalleryRemote.class.getResource("/uploading.gif"));
		} catch (Exception e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}
}