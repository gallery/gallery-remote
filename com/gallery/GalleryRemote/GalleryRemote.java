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
import java.io.*;
import javax.swing.*;

/**
 *  Main class and entry point of Gallery Remote
 *
 *@author     paour
 *@created    11 août 2002
 */
public class GalleryRemote
{
	private static GalleryRemote singleton = null;
	
	public MainFrame mainFrame = null;
	public PropertiesFile properties = null;
	public PropertiesFile defaults = null;
	
	private GalleryRemote() {}
	
	private void run()
	{
		try
		{
			// For native Look and Feel, uncomment the following code.
			/// *
			try
			{
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			catch ( Exception e )
			{
			}
			//* /
			
			defaults = new PropertiesFile("defaults");
			properties = new PropertiesFile(defaults, "remote");
			
			mainFrame = new MainFrame();
			mainFrame.initComponents();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public static GalleryRemote getInstance()
	{
		return singleton;
	}

	// Main entry point
	public static void main( String[] args )
	{
		singleton = new GalleryRemote();
		
		singleton.run();

		//-- check for updated version of the app, using InstallAnywhere's --
		//-- PowerUpdate service ---
		try
		{
			Runtime.getRuntime().exec( "update -whisper" );
		}
		catch ( IOException e )
		{
		}
	}
}

