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
 *@created    11 août 2002
 */

public class Gallery
{
	String url;
	String username;
	String password;

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
}
