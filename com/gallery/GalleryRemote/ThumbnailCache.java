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
import java.io.IOException;
import java.util.*;
import java.awt.*;

import javax.swing.ImageIcon;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.GRI18n;

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
    public static GRI18n grRes = GRI18n.getInstance();

	boolean stillRunning = false;
	Stack toLoad = new Stack();
	HashMap thumbnails = new HashMap();
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
		mf.jStatusBar.startProgress(StatusUpdate.LEVEL_CACHE, 0, toLoad.size(), grRes.getString(MODULE, "loadThmb"), false);
		//Log.log(Log.TRACE, MODULE, "Starting " + iFilename);
		while ( !toLoad.isEmpty() ) {
			Picture p = (Picture) toLoad.pop();
			ImageIcon i = null;

			if ( ! thumbnails.containsKey( p ) ) {
				if (p.isOnline()) {
					i = new ImageIcon(p.getUrlThumbnail());

					Image scaled = null;
					Dimension newD = ImageUtils.getSizeKeepRatio(
							new Dimension( i.getIconWidth(), i.getIconHeight() ),
							GalleryRemote.getInstance().properties.getThumbnailSize() );
					scaled = i.getImage().getScaledInstance( newD.width, newD.height, Image.SCALE_FAST );

					i.getImage().flush();
					i.setImage( scaled );
				} else {
					i = ImageUtils.load(
						p.getSource().getPath(),
						GalleryRemote.getInstance().properties.getThumbnailSize(),
						ImageUtils.THUMB );
				}

				thumbnails.put( p, i );
				
				loaded++;
				
				Log.log(Log.LEVEL_TRACE, MODULE, "update progress " + loaded + "/" + (loaded + toLoad.size()));
				mf.jStatusBar.updateProgressValue(StatusUpdate.LEVEL_CACHE, loaded, loaded + toLoad.size());
				mf.thumbnailLoadedNotify();
			}
		}
		stillRunning = false;
		
		mf.jStatusBar.stopProgress(StatusUpdate.LEVEL_CACHE, grRes.getString(MODULE, "thmbLoaded"));

		//Log.log(Log.TRACE, MODULE, "Ending");
	}


	/**
	 *  Ask for the thumbnail to be loaded
	 *
	 *@param  filename  path to the file
	 */
	/*public void preloadThumbnailFilename( String filename ) {
		Log.log(Log.LEVEL_TRACE, MODULE, "preloadThumbnailFilename " + filename);
		
		if (!thumbnails.containsKey(filename)) {
			toLoad.add( 0, filename );

			rerun();
		}
	}*/


	/**
	 *  Ask for the thumbnail to be loaded as soon as possible
	 *
	 *@param  filename  path to the file
	 */
	public void preloadThumbnailFirst( Picture p ) {
		Log.log(Log.LEVEL_TRACE, MODULE, "preloadThumbnailFirst " + p);

		if (!thumbnails.containsKey(p)) {
			toLoad.push( p );

			rerun();
		}
	}


	/**
	 *  Ask for several thumnails to be loaded
	 *
	 *@param  pictures  enumeration of Picture objects that should be loaded
	 */
	public void preloadThumbnails( Iterator pictures ) {
		Log.log(Log.LEVEL_TRACE, MODULE, "preloadThumbnails " + pictures);

		while ( pictures.hasNext() ) {
			Picture p = (Picture) pictures.next();

			if (!thumbnails.containsKey(p)) {
				toLoad.add( 0, p );
			}
		}

		rerun();
	}


	/**
	 *  Ask for several thumnails to be loaded
	 *
	 *@param  filenames  an array of File objects
	 */
	/*public void preloadThumbnailFiles( File[] files ) {
		Log.log(Log.LEVEL_TRACE, MODULE, "preloadThumbnailPictures " + files);

		for ( int i = 0; i < files.length; i++ ) {
			String filename = files[i].getPath();

			if (!thumbnails.containsKey(filename)) {
				toLoad.add( 0, filename );
			}
		}

		rerun();
	}*/

	/**
	 * Ask for an enumeration fo file names to be loaded
	 *
	 * @param filenames an enumeration of String file names
	 */
	/*public void preloadThumbnailFilenames( Enumeration filenames ) {
		Log.log(Log.LEVEL_TRACE, MODULE, "preloadThumbnailFilenames " + filenames);

		while ( filenames.hasMoreElements() ) {
			String filename = (String) filenames.nextElement();
			if (!thumbnails.containsKey(filename)) {
				toLoad.add( 0, filename );
			}
		}

		rerun();
	}*/

	public void reload() {
		Iterator it = ((HashMap) thumbnails.clone()).keySet().iterator();
		thumbnails.clear();
		preloadThumbnails(it);
	}

	public void flushMemory() {
		Iterator it = thumbnails.values().iterator();
		while (it.hasNext()) {
			ImageIcon i = (ImageIcon) it.next();
			i.getImage().flush();
		}

		thumbnails.clear();
	}


	void rerun() {
		if ( !stillRunning && GalleryRemote.getInstance().properties.getShowThumbnails() ) {
			stillRunning = true;
			Log.log(Log.LEVEL_TRACE, MODULE, "Calling Start");
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
	public ImageIcon getThumbnail( Picture p ) {
		return (ImageIcon) thumbnails.get( p );
	}
}

