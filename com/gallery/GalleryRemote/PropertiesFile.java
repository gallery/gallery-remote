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

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 *  Properties file for Gallery Remote
 *
 *@author     paour
 *@created    11 août 2002
 */
public class PropertiesFile extends GalleryProperties
{
	public static final String MODULE = "PropsFile";
	
	protected boolean read = false;
	protected boolean written = false;
	protected String mFilename;

	/**
	 *  Constructor for the PropertiesFile object
	 *
	 *@param  p  Description of Parameter
	 */
	public PropertiesFile( PropertiesFile p ) {
		super( p );
	}


	/**
	 *  Constructor for the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 */
	public PropertiesFile( String name ) {
		super();

		setFilename( name );
	}


	/**
	 *  Constructor for the PropertiesFile object
	 *
	 *@param  p     Description of Parameter
	 *@param  name  Description of Parameter
	 */
	public PropertiesFile( PropertiesFile p, String name ) {
		super( p );

		setFilename( name );
	}


	/**
	 *  Overrides default method to track dirty state
	 */
	public Object setProperty( String name, String value ) {
		written = false;

		return super.setProperty( name, value );
	}


	/**
	 *  Change the filename of the file (why would you want to do that?)
	 *
	 *@param  name  The new filename value
	 */
	public synchronized void setFilename( String name ) {
		mFilename = name + ".properties";
	}


	/**
	 *  Read a property as a string, read the file in first
	 * so you don't have to explicitly read the file in beforehand.
	 *
	 *@param  name  Name of the property
	 *@return       The property value
	 */
	public String getProperty( String name ) {
		if ( !read ) {
			try {
				read();
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			}
		}

		return super.getProperty( name );
	}

	/**
	 *  Read the property file from disk
	 *
	 *@exception  FileNotFoundException  Description of Exception
	 */
	public synchronized void read()
		throws FileNotFoundException {
		if ( mFilename != null ) {
			FileInputStream fileIn = null;
			try {
				fileIn = new FileInputStream( mFilename );
				load( fileIn );
			} catch ( IOException e ) {
				//e.printStackTrace();
				//write();
			} finally {
				try {
					fileIn.close();
				} catch ( IOException e2 ) {
				} catch ( NullPointerException e3 ) {
				}
			}
		}

		read = true;
		written = true;
	}


	/**
	 *  Write the property file to disk
	 */
	public synchronized void write() {
		if ( !written ) {
			FileOutputStream fileOut = null;
			try {
				fileOut = new FileOutputStream( mFilename );
				store( fileOut, null );
			} catch ( IOException e ) {
				e.printStackTrace();
			} finally {
				try {
					fileOut.close();
				} catch ( IOException e2 ) {
				} catch ( NullPointerException e3 ) {
				}
			}
		}

		written = true;
	}
}

