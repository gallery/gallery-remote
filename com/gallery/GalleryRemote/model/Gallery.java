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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  Album model
 *
 *@author     paour
 *@created    17 août 2002
 */

public class Gallery
{
	String url = "http://";
	String username;
	String password;
	ArrayList albumList = null;


	/**
	 *  Constructor for the Gallery object
	 */
	public Gallery() { }


	/**
	 *  Constructor for the Gallery object
	 *
	 *@param  url       Description of Parameter
	 *@param  username  Description of Parameter
	 *@param  password  Description of Parameter
	 */
	public Gallery( String url, String username, String password ) {
		this.url = url;
		this.username = username;
		this.password = password;
	}


	/**
	 *  Sets the url attribute of the Gallery object
	 *
	 *@param  url  The new url value
	 */
	public void setUrl( String url ) {
		this.url = url;
	}


	/**
	 *  Sets the username attribute of the Gallery object
	 *
	 *@param  username  The new username value
	 */
	public void setUsername( String username ) {
		this.username = username;
	}


	/**
	 *  Sets the password attribute of the Gallery object
	 *
	 *@param  password  The new password value
	 */
	public void setPassword( String password ) {
		this.password = password;
	}


	/**
	 *  Sets the albumList attribute of the Gallery object
	 *
	 *@param  albumList  The new albumList value
	 */
	public void setAlbumList( ArrayList albumList ) {
		this.albumList = albumList;
	}


	/**
	 *  Gets the url attribute of the Gallery object
	 *
	 *@return    The url value
	 */
	public String getUrl() {
		return url;
	}


	/**
	 *  Gets the username attribute of the Gallery object
	 *
	 *@return    The username value
	 */
	public String getUsername() {
		return username;
	}


	/**
	 *  Gets the password attribute of the Gallery object
	 *
	 *@return    The password value
	 */
	public String getPassword() {
		return password;
	}


	/**
	 *  Gets the albumList attribute of the Gallery object
	 *
	 *@return    The albumList value
	 */
	public ArrayList getAlbumList() {
		return albumList;
	}


	/**
	 *  For the list models to display the Gallery
	 *
	 *@return    Description of the Returned Value
	 */
	public String toString() {
		return url;
	}
}

