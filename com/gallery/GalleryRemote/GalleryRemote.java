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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.applet.Applet;
import java.util.Enumeration;

/**
 * Main class and entry point of Gallery Remote
 * 
 * @author paour
 * @created 11 août 2002
 */
public abstract class GalleryRemote {
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

			// log properties
			properties.logProperties(Log.LEVEL_TRACE, "UsrProps");

			loadIcons();
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, "Startup", e);
			Log.shutdown();
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
		_().runGR();
	}

	public static void setStaticProperties() {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Gallery Remote");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("apple.awt.showGrowBox", "false");
		System.setProperty("apple.awt.brushMetalLook", "true");

		// fix buggy Swing Windows XP lookup code
		System.setProperty("swing.noxp", "true");

		// todo: this should not remain this way
		//System.setProperty("apple.awt.fakefullscreen", "true");
	}

	public void createProperties() {
		defaults = new PropertiesFile("defaults");
		defaults.setReadOnly();
	}

	public PropertiesFile createAppletOverride(PropertiesFile p) {
		PropertiesFile override;

		if (p == null) {
			override = new PropertiesFile(defaults);
		} else {
			override = new PropertiesFile(p);
		}

		override.setReadOnly();

		return override;
	}

	public PropertiesFile getAppletOverrides(PropertiesFile p, String prefix) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Getting applet parameters for prefix " + prefix);

		for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			String value = applet.getParameter(prefix + name);

			if (value != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Got: " + name + "= |" + value + "|");
				p.setProperty(name, value);
			}
		}

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