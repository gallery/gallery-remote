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
package com.gallery.GalleryRemote.model;

import java.io.*;

/**
 *  Picture model
 *
 *@author     paour
 *@created    11 août 2002
 */
public class Picture
{
	File source = null;
	String caption = null;
	double fileSize = -1;
	Album album = null;


	/**
	 *  Constructor for the Picture object
	 */
	public Picture() { }


	/**
	 *  Constructor for the Picture object
	 *
	 *@param  source  File the Picture is based on
	 */
	public Picture( File source ) {
		setSource( source );
	}


	/**
	 *  Sets the source file the Picture is based on
	 *
	 *@param  source  The new file
	 */
	public void setSource( File source ) {
		this.source = source;

		fileSize = -1;
	}


	/**
	 *  Sets the caption attribute of the Picture object
	 *
	 *@param  caption  The new caption value
	 */
	public void setCaption( String caption ) {
		this.caption = caption;
	}


	/**
	 *  Sets the album this Picture is inside of
	 *
	 *@param  album  The new album value
	 */
	public void setAlbum( Album album ) {
		this.album = album;
	}


	/**
	 *  Gets the source file the Picture is based on
	 *
	 *@return    The source value
	 */
	public File getSource() {
		return source;
	}


	/**
	 *  Gets the caption attribute of the Picture object
	 *
	 *@return    The caption value
	 */
	public String getCaption() {
		return caption;
	}


	/**
	 *  Gets the size of the file
	 *
	 *@return    The size value
	 */
	public double getFileSize() {
		if ( fileSize == -1 && source != null && source.exists() ) {
			fileSize = source.length();
		}

		return fileSize;
	}


	/**
	 *  Gets the album this Picture is inside of
	 *
	 *@return    The album
	 */
	public Album getAlbum() {
		return album;
	}
}

