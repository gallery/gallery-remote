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

import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;

import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.HTMLEscaper;
import com.gallery.GalleryRemote.Log;

/**
 *  Picture model
 *
 *@author     paour
 *@created    11 août 2002
 */
public class Picture implements Serializable {
	public static final String MODULE="Picture";

    File source = null;
    String caption = null;
    Album album = null;
    private int listIndex;

	transient double fileSize = 0;
	transient String escapedCaption = null;

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
        
		if (GalleryRemote.getInstance().properties.getBooleanProperty("setCaptionsWithFilenames")) {
			setCaption(source.getName());
		}
		
        fileSize = 0;
    }
    
    
    /**
     *  Sets the caption attribute of the Picture object
     *
     *@param  caption  The new caption value
     */
    public void setCaption( String caption ) {
        this.caption = caption;
		this.escapedCaption = null;
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
	 *  Gets the fource file of the picture, prepared for upload.
	 *  Called by GalleryComm to upload the picture.
	 *
	 *@return    The source value
	 */
    public File getUploadSource() {
		if ( GalleryRemote.getInstance().properties.getBooleanProperty("resizeBeforeUpload") ) {
			Dimension d = GalleryRemote.getInstance().properties.getDimensionProperty("resizeTo");
			
			if ( d == null || d.equals( new Dimension( 0, 0 ) ) ) {
				d = null;
				int l = album.getServerAutoResize();
				
				if ( l != 0 ) {
					d = new Dimension( l, l );
				} else {
					// server can't tell us how to resize, try default
					d = GalleryRemote.getInstance().properties.getDimensionProperty("resizeToDefault");
					
					if ( d.equals( new Dimension( 0, 0 ) ) ) {
						d = null;
					}
				}
			}

			File picture = null;

			if ( d != null ) {
				try {
					picture = ImageUtils.resize( getSource().getPath(), d );
				} catch (UnsupportedOperationException e) {
					Log.log(Log.ERROR, MODULE, "Couldn't use ImageUtils to resize the image, it will be uploaded at the original size");
					Log.logException(Log.ERROR, MODULE, e);
				}
			}

			if (picture == null) {
				picture = getSource();
			}

			return picture;
		} else {
			return getSource();
		}
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
	 * Cache the escapedCaption because the escaping is lengthy and this is called by a frequent UI method
	 * @return the HTML escaped version of the caption
	 */
	public String getEscapedCaption() {
		if (escapedCaption == null) {
			if (caption != null) {
				escapedCaption = HTMLEscaper.escape(caption);
			}
		}

		return escapedCaption;
	}

    /**
     *  Gets the size of the file
     *
     *@return    The size value
     */
    public double getFileSize() {
        if ( fileSize == 0 && source != null && source.exists() ) {
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

    /** Getter for property listIndex.
     * @return Value of property listIndex.
     *
     */
    public int getListIndex() {
		return this.listIndex;
    }
    
    /** Setter for property listIndex.
     * @param listIndex New value of property listIndex.
     *
     */
    public void setListIndex(int listIndex) {
    }
	
	public String toString() {
		return source.getName();
	}
}

