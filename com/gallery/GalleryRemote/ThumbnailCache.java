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

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import javax.swing.ImageIcon;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.ImageUtils;

/**
 *  Thumbnail cache loads and resizes images in the background for display in
 *  the list of Pictures
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class ThumbnailCache implements Runnable
{
	public static final String MODULE = "ThumbCache";
	
	boolean stillRunning = false;
	Stack toLoad = new Stack();
	Hashtable thumbnails = new Hashtable();
	MainFrame mf;


	/**
	 *  Constructor for the ThumbnailCache object
	 *
	 *@param  mf  Description of Parameter
	 */
	public ThumbnailCache( MainFrame mf ) {
		this.mf = mf;
	}


	/**
	 *  Main processing method for the ThumbnailLoader object
	 */
	public void run() {
		Thread.yield();
		int loaded = 0;
		int pId = mf.startProgress(0, toLoad.size(), "Loading thumbnails", false);
		//Log.log(Log.TRACE, MODULE, "Starting " + iFilename);
		while ( !toLoad.isEmpty() ) {
			String filename = (String) toLoad.pop();

			if ( ! thumbnails.containsKey( filename ) ) {
				ImageIcon i = ImageUtils.load(
					filename, 
					GalleryRemote.getInstance().properties.getThumbnailSize(), 
					ImageUtils.THUMB );
				thumbnails.put( filename, i );
				
				loaded++;
				
				Log.log(Log.TRACE, MODULE, "update progress " + loaded + "/" + (loaded + toLoad.size()));
				mf.updateProgressValue(pId, loaded, loaded + toLoad.size());
				mf.thumbnailLoadedNotify();
			}
		}
		stillRunning = false;
		
		mf.stopProgress(pId, "Thumbnails loaded");

		//Log.log(Log.TRACE, MODULE, "Ending");
	}


	/**
	 *  Ask for the thumbnail to be loaded
	 *
	 *@param  filename  path to the file
	 */
	public void preloadThumbnail( String filename ) {
		Log.log(Log.TRACE, MODULE, "preloadThumbnail " + filename);
		
		if (!thumbnails.containsKey(filename)) {
			toLoad.add( 0, filename );

			rerun();
		}
	}


	/**
	 *  Ask for the thumbnail to be loaded as soon as possible
	 *
	 *@param  filename  path to the file
	 */
	public void preloadThumbnailFirst( String filename ) {
		Log.log(Log.TRACE, MODULE, "preloadThumbnailFirst " + filename);

		if (!thumbnails.containsKey(filename)) {
			toLoad.push( filename );

			rerun();
		}
	}


	/**
	 *  Ask for several thumnails to be loaded
	 *
	 *@param  files  enumeration of Picture objects that should be loaded
	 */
	public void preloadThumbnailFiles( Enumeration files ) {
		Log.log(Log.TRACE, MODULE, "preloadThumbnail " + files);

		while ( files.hasMoreElements() ) {
			String filename = ( (Picture) files.nextElement() ).getSource().getPath();
			
			if (!thumbnails.containsKey(filename)) {
				toLoad.add( 0, filename );
			}
		}

		rerun();
	}


	/**
	 *  Ask for several thumnails to be loaded
	 *
	 *@param  filenames  an array of File objects
	 */
	public void preloadThumbnailFiles( File[] files ) {
		Log.log(Log.TRACE, MODULE, "preloadThumbnailFiles " + files);

		for ( int i = 0; i < files.length; i++ ) {
			String filename = files[i].getPath();

			if (!thumbnails.containsKey(filename)) {
				toLoad.add( 0, filename );
			}
		}

		rerun();
	}

	/**
	 * Ask for an enumeration fo file names to be loaded
	 *
	 * @param filenames an enumeration of String file names
	 */
	public void preloadThumbnails( Enumeration filenames ) {
		Log.log(Log.TRACE, MODULE, "preloadThumbnails " + filenames);

		while ( filenames.hasMoreElements() ) {
			String filename = (String) filenames.nextElement();
			if (!thumbnails.containsKey(filename)) {
				toLoad.add( 0, filename );
			}
		}

		rerun();
	}

	public void reload() {
		Enumeration e = ((Hashtable) thumbnails.clone()).keys();
		thumbnails.clear();
		preloadThumbnails(e);
	}


	void rerun() {
		if ( !stillRunning && GalleryRemote.getInstance().properties.getShowThumbnails() ) {
			stillRunning = true;
			Log.log(Log.TRACE, MODULE, "Calling Start");
			new Thread( this ).start();
		}
	}


	void cancelLoad() {
		toLoad.clear();
	}


	/**
	 *  Retrieves a thumbnail from the thumbnail cache
	 *
	 *@param  filename  path to the file
	 *@return           The thumbnail object
	 */
	public ImageIcon getThumbnail( String filename ) {
		return (ImageIcon) thumbnails.get( filename );
	}
}

