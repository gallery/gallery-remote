/*
 * Gallery Remote - a File Upload Utility for Gallery 
 *
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2001 Bharat Mediratta
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.*;

/**
 *	This interface is a temporary mechanism to let us use version
 *	1 and 2 of the protocol by changing a little code -- a replacement for
 *	this is under development that will allow a GalleryRemote client
 *	to automatically determine what protocol it should use given
 *	a Gallery and to use the appropriate implementation.
 *	
 *  @author <a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
 */
public interface GalleryComm {

	/**
	 *	Causes the GalleryComm instance to upload the pictures in the
	 *	associated Gallery to the server.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void uploadFiles( StatusUpdate su );
	
	/**
	 *	Causes the GalleryComm instance to fetch the albums contained by
	 *	associated Gallery from the server.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void fetchAlbums( StatusUpdate su );
	
	/**
	 *	Causes the GalleryComm instance to fetch the album properties
	 *	for the given Album.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void albumInfo( StatusUpdate su, Album a );
	
	/**
	 *	Causes the GalleryComm instance to fetch the album properties
	 *	for the given Album.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void logOut();
}
