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
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.gallery.GalleryRemote.*;

/**
 *  Gallery model
 *
 *@author     paour
 *@created    17 août 2002
 */

public class Gallery implements ComboBoxModel
{
	public static final String MODULE="Gallery";
	
	URL url = null;
	String username;
	String password;
	ArrayList albumList = null;
	Album selectedAlbum = null;
	
	GalleryComm comm = null;

	// ListModel
	Vector listeners = new Vector( 1 );

	/**
	 *  Constructor for the Gallery object
	 */
	public Gallery() { }

	
	/*TEMPORARY*/ StatusUpdate su;
	
	/**
	 *  Constructor for the Gallery object
	 *
	 *@param  url       Description of Parameter
	 *@param  username  Description of Parameter
	 *@param  password  Description of Parameter
	 */
	public Gallery( URL url, String username, String password, /*TEMPORARY*/ StatusUpdate su ) {
		setUrl( url );
		this.username = username;
		this.password = password;
		/*TEMPORARY*/ this.su = su;
	}
	
	
	public void uploadFiles( StatusUpdate su ) {
		getComm().uploadFiles( su, true );
	}
	
	public void fetchAlbums( StatusUpdate su ) {
		albumList = null;
		/* TEMPORARY */ // getComm().logOut();
		getComm().fetchAlbums( su, true );
	}
	
	public void newAlbum( Album a, StatusUpdate su) {
		Log.log(Log.INFO, MODULE, "Creating new album " + a.toString());

		// create album synchronously
		getComm().newAlbum( su, a.getParentAlbum(), a.getName(),
			a.getTitle(), a.getCaption(), false );
		
		// refresh album list asynchronously
		fetchAlbums( su );
	}
	
	/**
	 *  Sets the url attribute of the Gallery object
	 *
	 *@param  url  The new url value
	 */
	public void setUrlString( String urlString ) throws MalformedURLException {
		if ( urlString == null ) {
			throw new IllegalArgumentException( "urlString must not be null" );
		}
	
		if (!urlString.endsWith("/")) {
			urlString += "/";
		}
		
		if (!urlString.startsWith("http://"))
		{
			urlString = "http://" + urlString;
		}
		
		this.url = new URL( urlString );
	}
	
	/** 
	 *
	 */
	public void setUrl( URL url ) {
		this.url = url;
	}
	

	/**
	 *  Sets the username attribute of the Gallery object
	 *
	 *@param  username  The new username value
	 */
	public void setUsername( String username ) {
		//Log.log(Log.TRACE, MODULE, "setusername: " + username);
		if ( username != null && username.length() > 0
			&& ! username.equals( this.username ) ) {
			
			this.username = username;
			
			logOut();
		}
	}


	/**
	 *  Sets the password attribute of the Gallery object
	 *
	 *@param  password  The new password value
	 */
	public void setPassword( String password ) {
		//Log.log(Log.TRACE, MODULE, "setpassword: " + password);
		if ( password != null && password.length() > 0 
			&& ! password.equals( this.password ) ) {
			
			this.password = password;
			
			logOut();
		}
	}
	
	public void logOut() {
		albumList = null;
		selectedAlbum = null;
		
		notifyListeners();
	}


	/**
	 *  Sets the albumList attribute of the Gallery object
	 *
	 *@param  albumList  The new albumList value
	 *@deprecated
	 */
	public void setAlbumList( ArrayList albumList ) {
		if ( albumList == null ) {
			throw new IllegalArgumentException( "Must supply non-null album list." );
		}
		this.albumList = albumList;
		if ( albumList.size() > 0 ) {
			selectedAlbum = (Album) this.albumList.get(0);
		}
		
		notifyListeners();
	}
	
	/**
	 * Adds an album to the gallery and selects the first one added.
	 */
	public synchronized void addAlbum( Album a ) {
		if ( a == null ) {
			throw new IllegalArgumentException( "Must supply non-null album." );
		}
		
		// when the first album becomes available, make sure to select
		// it in the list
		boolean firstAlbum = false;
		
		// lazy allocation
		if ( this.albumList == null ) {
			this.albumList = new ArrayList();
			firstAlbum = true;
		}
		
		albumList.add( a );
		
		if ( firstAlbum ) {
			selectedAlbum = (Album) this.albumList.get(0);
		}
		
		notifyListeners();
	}


	/**
	 *  Gets the url attribute of the Gallery object
	 *
	 *@return    The url value
	 */
	public String getUrlString() {
		return url.toString();
	}


	/**
	 *  Gets the url attribute of the Gallery object
	 *
	 *@return    The url value
	 */
	public URL getUrl() {
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
	
	public ArrayList getAllPictures() {
		ArrayList pictures = new ArrayList();
		
		Iterator i = albumList.iterator();
		while (i.hasNext()) {
			Album a = (Album) i.next();
			
			pictures.addAll(a.getPicturesVector());
		}
		
		return pictures;
	}


	/**
	 *  For the list models to display the Gallery
	 *
	 *@return    Description of the Returned Value
	 */
	public String toString() {
		if (url == null) {
			return "http://";
		}
		
		return url.toString();
	}

	public Album getSelectedAlbum() {
		return selectedAlbum;
	}
	

	/*
	 *	ListModel Implementation
	 */
	public int getSize() {
		if (albumList != null) {
			return albumList.size();
		} else {
			return 0;
		}
	}


	public Object getElementAt( int index ) {
		return albumList.get( index );
	}

	public void setSelectedItem(Object anItem) {
		selectedAlbum = (Album) anItem;
	}
	
	public Object getSelectedItem() {
		return selectedAlbum;
	}
	
	public void addListDataListener( ListDataListener ldl ) {
		listeners.addElement( ldl );
	}


	public void removeListDataListener( ListDataListener ldl ) {
		listeners.removeElement( ldl );
	}


	/**
	 *	Lazy instantiation for the GalleryComm instance.
	 */
	public GalleryComm getComm() {
		if ( comm == null ) {
			/* TEMPORARY*/
			if (GalleryRemote.getInstance().properties.getIntProperty("protocolVersion") == 2) {
				comm = new GalleryComm2( this );
			} else {
				comm = new GalleryComm1( su, this );
			}
		}
		
		return comm;
	}
	
	void notifyListeners() {
		ListDataEvent lde;
		if (albumList != null) {
			lde = new ListDataEvent( com.gallery.GalleryRemote.GalleryRemote.getInstance().mainFrame, ListDataEvent.CONTENTS_CHANGED, 0, albumList.size() );
		} else {
			lde = new ListDataEvent( com.gallery.GalleryRemote.GalleryRemote.getInstance().mainFrame, ListDataEvent.CONTENTS_CHANGED, 0, 0 );
		}
		
		notifyListeners(lde);
	}
	
	void notifyListeners(ListDataEvent lde) {
		Log.log(Log.TRACE, MODULE, "Firing ListDataEvent=" + lde.toString());
		Enumeration e = listeners.elements();
		while ( e.hasMoreElements() ) {
			ListDataListener ldl = (ListDataListener) e.nextElement();
			ldl.contentsChanged( lde );
		}
	}
}

