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
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.GalleryProperties;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
*  Main class and entry point of Gallery Remote
*
*@author     paour
*@created    11 août 2002
*/
public class GalleryRemote {
	private static GalleryRemote singleton = null;
	
	public MainFrame mainFrame = null;
	public PropertiesFile properties = null;
	public PropertiesFile defaults = null;
		
	private GalleryRemote() {
		defaults = new PropertiesFile("defaults");
		
		File f = new File(System.getProperty("user.home")
		+ File.separator + ".GalleryRemote"
		+ File.separator);
		
		f.mkdirs();
		
		properties = new PropertiesFile(defaults, f.getPath()
		+ File.separator + "GalleryRemote");
	}
	
	private void run() {
		try {
			// For native Look and Feel, uncomment the following code.
			/// *
			try {
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			catch ( Exception e ) {
			}
			//* /
			
			// log system properties
			new GalleryProperties(System.getProperties()).logProperties(Log.LEVEL_INFO, "SysProps");

			// log properties
			properties.logProperties(Log.LEVEL_TRACE, "UsrProps");

			if (!Update.upgrade()) {
				mainFrame = new MainFrame();
				mainFrame.initComponents();
			} else {
				Log.shutdown();
				System.exit(0);
			}
		}
		catch ( Exception e ) {
			Log.logException(Log.LEVEL_CRITICAL, "Startup", e);
			Log.shutdown();
		}

		Update update = new Update();
		update.check( true );
	}
	
	public static GalleryRemote getInstance() {
		if (singleton == null) {
			singleton = new GalleryRemote();
		}
		
		return singleton;
	}
	
	// Main entry point
	public static void main( String[] args ) {
		getInstance().run();
	}
}

