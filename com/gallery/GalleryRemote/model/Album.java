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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;

import com.gallery.GalleryRemote.GalleryCommCapabilities;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.StatusUpdate;
import com.gallery.GalleryRemote.StatusUpdateAdapter;

/**
 *  Album model
 *
 *@author     paour
 *@created    11 août 2002
 */

public class Album extends Picture implements ListModel, Serializable
{
	/* -------------------------------------------------------------------------
	 * CONSTANTS
	 */
	public static final String MODULE="Album";
	

	/* -------------------------------------------------------------------------
	 * LOCAL STORAGE
	 */
	Vector pictures = new Vector();


	/* -------------------------------------------------------------------------
	 * SERVER INFO
	 */
	Gallery gallery = null;
	Album parent; // parent Album
	String title = "Not yet connected to Gallery";
	String name;

	transient int autoResize = 0;
	// permissions -- default to true for the sake of old protocols ...
	transient boolean canRead = true;
	transient boolean canAdd = true;
	transient boolean canWrite = true;
	transient boolean canDeleteFrom = true;
	transient boolean canDeleteThisAlbum = true;
	transient boolean canCreateSubAlbum = true;
	
	transient boolean hasFetchedInfo = false;
	
	
	/* -------------------------------------------------------------------------
	 * INSTANCE VARIABLES
	 */ 
	transient long pictureFileSize = -1;
	

	/* -------------------------------------------------------------------------
	 * PUBLIC CLASS METHODS
	 */
	 
	 	
	/**
	 *  Retrieves the album properties from the server.
	 *
	 *@param  gallery  The new gallery
	 */
	public void fetchAlbumProperties( StatusUpdate su ) {
		if ( ! hasFetchedInfo && getGallery().getComm( su ).hasCapability(GalleryCommCapabilities.CAPA_ALBUM_INFO))
		{
			if ( su == null ) {
				su = new StatusUpdateAdapter(){};
			}
			
			try {
				gallery.getComm( su ).albumInfo( su, this, false );
			} catch (RuntimeException e) {
				Log.log(Log.INFO, MODULE, "Server probably doesn't support album-info");
				Log.logException(Log.INFO, MODULE, e);
			}
		}
	}
	
	/**
	 *  Sets the server auto resize dimension.
	 *
	 *@param  autoResize  the server's resize dimension
	 */
	public void setServerAutoResize( int autoResize ) {
		this.autoResize = autoResize;
		hasFetchedInfo = true;
	}
	
	/**
	 *  Gets the server auto resize dimension.
	 *
	 *@return    the server's resize dimension for this album
	 */
	public int getServerAutoResize() {
		fetchAlbumProperties(null);
		
		return autoResize;
	}
	
	/**
	 *  Sets the gallery attribute of the Album object
	 *
	 *@param  gallery  The new gallery
	 */
	public void setGallery( Gallery gallery ) {
		this.gallery = gallery;
	}

	/**
	 *  Gets the gallery attribute of the Album object
	 *
	 *@return    The gallery
	 */
	public Gallery getGallery() {
		return gallery;
	}

	/**
	 *  Gets the pictures inside the album
	 *
	 *@return    The pictures value
	 */
	public Enumeration getPictures() {
		return pictures.elements();
	}
	
	/**
	 *  Adds a picture to the album
	 *
	 *@param  p  the picture to add. This will change its parent album
	 */
	public void addPicture( Picture p ) {
		p.setAlbum( this );
		pictures.addElement( p );

		notifyListeners();
	}

	/**
	 *  Adds a picture to the album
	 *
	 *@param  file  the file to create the picture from
	 */
	public void addPicture( File file ) {
		Picture p = new Picture( file );
		p.setAlbum( this );
		pictures.addElement( p );

		notifyListeners();
	}

	/**
	 *  Adds pictures to the album
	 *
	 *@param  files  the files to create the pictures from
	 */
	public void addPictures( File[] files ) {
		addPictures(files, 0);
	}
        
    /**
	 *  Adds pictures to the album at a specified index
	 *
	 *@param  files  the files to create the pictures from
         *@param  index  the index in the list at which to begin adding
	 */
	public void addPictures( File[] files, int index ) {
		for ( int i = 0; i < files.length; i++ ) {
			Picture p = new Picture( files[i] );
			p.setAlbum( this );
			pictures.add( index++, p );
		}

		notifyListeners();
	}

	/**
	 *  Number of pictures in the album
	 *
	 *@return    Number of pictures in the album
	 */
	public int sizePictures() {
		return pictures.size();
	}

	/**
	 *  Remove all the pictures
	 */
	public void clearPictures() {
		pictures.clear();

		notifyListeners();
	}

	/**
	 *  Remove a picture
	 *
	 *@param  n  item number of the picture to remove
	 */
	public void removePicture( int n ) {
		pictures.remove( n );

		fireIntervalRemoved(this, n, n);
	}

	public void removePicture( Picture p ) {
		removePicture(pictures.indexOf(p));
	}

	/**
	 *  Remove pictures
	 *
	 *@param  indices  list of indices of pictures to remove
	 */
	public void removePictures( int[] indices ) {
		int min, max;
		min = max = indices[0];
		
		for ( int i = indices.length - 1; i >= 0; i-- ) {
			pictures.remove( indices[i] );
			if (indices[i] > max) max = indices[i];
			if (indices[i] < min) min = indices[i];
		}

		fireIntervalRemoved(this, min, max);
	}

	/**
	 *  Get a picture from the album
	 *
	 *@param  n  index of the picture to retrieve
	 *@return    The Picture
	 */
	public Picture getPicture( int n ) {
		return (Picture) pictures.get( n );
	}


	/**
	 *  Set a picture in the album
	 *
	 *@param  n  index of the picture
	 *@param  p  The new picture
	 */
	public void setPicture( int n, Picture p ) {
		pictures.set( n, p );

		notifyListeners();
	}

	/**
	 *  Get the list of files that contain the pictures
	 *
	 *@return    The fileList value
	 */
	public ArrayList getFileList() {
		ArrayList l = new ArrayList( pictures.size() );

		Enumeration e = pictures.elements();
		while ( e.hasMoreElements() ) {
			l.add( ( (Picture) e.nextElement() ).getSource() );
		}

		return l;
	}

	/**
	 *  Sets the name attribute of the Album object
	 *
	 *@param  name  The new name value
	 */
	public void setName( String name ) {
		this.name = removeOffendingChars(name);
	}
	
	static final String offendingChars = "\\/*?\"\'&<>|.+# ";
	static String removeOffendingChars(String in) {
		StringBuffer out = new StringBuffer();
		
		int l = in.length();
		for (int i = 0; i < l; i++) {
			char c = in.charAt(i);
			if (offendingChars.indexOf(c) == -1) {
				out.append(c);
			}
		}
		
		return out.toString();
	}

	/**
	 *  Gets the name attribute of the Album object
	 *
	 *@return    The name value
	 */
	public String getName() {
		return name;	
	}

	/**
	 *  Sets the title attribute of the Album object
	 *
	 *@param  title  The new title
	 */
	public void setTitle( String title ) {
		this.title = title;
	}

	/**
	 *  Gets the title attribute of the Album object
	 *
	 *@return    The title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 *  Gets the aggregated file size of all the pictures in the album
	 *
	 *@return    The file size (bytes)
	 */
	public long getPictureFileSize() {
		if ( pictureFileSize == -1 ) {
			pictureFileSize = getPictureFileSize( (Picture[]) pictures.toArray( new Picture[0] ) );
		}

		return pictureFileSize;
	}

	/**
	 *  Gets the aggregated file size of a list of pictures
	 *
	 *@param  pictures  the list of Pictures
	 *@return           The file size (bytes)
	 */
	public static long getPictureFileSize( Picture[] pictures ) {
		return getObjectFileSize( pictures );
	}

	/**
	 *  Gets the aggregated file size of a list of pictures Unsafe, the Objects
	 *  will be cast to Pictures.
	 *
	 *@param  pictures  the list of Pictures
	 *@return           The file size (bytes)
	 */
	public static long getObjectFileSize( Object[] pictures ) {
		long total = 0;

		for ( int i = 0; i < pictures.length; i++ ) {
			total += ( (Picture) pictures[i] ).getFileSize();
		}

		return total;
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append( indentHelper("") );
		ret.append( title );
		
		if (pictures.size() != 0) {
			ret.append( " (" + pictures.size() + ")" );
		}
		
		// using canAdd here, since that's the only operation we perform 
		// currently.  eventually, when we start changing things
		// on the server, permission support will get more ... interesting.
		if ( ! canAdd ) {
			ret.append( " (read-only)" );	
		}
		
		return ret.toString();
	}
	
	public boolean equals(Object o) {
		return (o != null
			&& o instanceof Album
			&& ((Album) o).getGallery() == getGallery()
			&& getName() != null
			&& ((Album) o).getName() != null
			&& ((Album) o).getName().equals(getName()));
	}
		
	
	//public void setListSelectionModel(ListSelectionModel listSelectionModel) {
	//	this.listSelectionModel = listSelectionModel;
	//}
	
	
	/* -------------------------------------------------------------------------
	 *ListModel Implementation
	 */
	 
	/**
	 *  Gets the size attribute of the Album object
	 *
	 *@return    The size value
	 */
	public int getSize() {
		return pictures.size();
	}

	/**
	 *  Gets the elementAt attribute of the Album object
	 *
	 *@param  index  Description of Parameter
	 *@return        The elementAt value
	 */
	public Object getElementAt( int index ) {
		return pictures.elementAt( index );
	}

	/**
	 *  Description of the Method
	 *
	 *@param  ldl  Description of Parameter
	 */
	public Album getParentAlbum() {
		return parent;	
	}
	
	/**
	 *  Description of the Method
	 *
	 *@param  ldl  Description of Parameter
	 */
	public void setParentAlbum( Album a ) {
		parent = a;
	}
	
	public void setCanRead( boolean b ){
		canRead = b;
	}
	public boolean getCanRead(){
		return canRead;
	}
	
	public void setCanAdd( boolean b ){
		canAdd = b;
	}
	public boolean getCanAdd(){
		return canAdd;
	}
	
	
	public void setCanWrite( boolean b ){
		canWrite = b;
	}
	public boolean getCanWrite(){
		return canWrite;
	}
	
	
	public void setCanDeleteFrom( boolean b ){
		canDeleteFrom = b;
	}
	public boolean getCanDeleteFrom(){
		return canDeleteFrom;
	}
	
	
	public void setCanDeleteThisAlbum( boolean b ){
		canDeleteThisAlbum = b;
	}
	public boolean getCanDeleteThisAlbum(){
		return canDeleteThisAlbum;
	}
	
	
	public void setCanCreateSubAlbum( boolean b ){
		canCreateSubAlbum = b;
	}
	public boolean getCanCreateSubAlbum(){
		return canCreateSubAlbum;
	}

	
	/* -------------------------------------------------------------------------
	 *NON-PUBLIC INSTANCE METHODS
	 */
	 
	/**
	 *	Package access to get the whole picture vector at once.
	 */
	Vector getPicturesVector() {
		return pictures;
	}
	
	void setPicturesVector(Vector pictures) {
		this.pictures = pictures;
		
		for (Enumeration e = pictures.elements(); e.hasMoreElements(); ) {
			((Picture) e.nextElement()).setAlbum(this);
		}
		
		notifyListeners();
	}
	
	public static final String INDENT_QUANTUM = "     ";
	String indentHelper( String indent ) {
		if ( getParentAlbum() != null ) {
			return getParentAlbum().indentHelper( indent + INDENT_QUANTUM );
		} else {
			return indent;
		}
	}
	
	void notifyListeners() {
		fireContentsChanged( this, 0, pictures.size() );
	}
}

