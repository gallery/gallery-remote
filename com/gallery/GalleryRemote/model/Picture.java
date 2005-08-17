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

import com.gallery.GalleryRemote.GalleryAbstractListModel;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.util.HTMLEscaper;
import com.gallery.GalleryRemote.util.ImageUtils;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Picture model
 * 
 * @author paour
 */
public class Picture extends GalleryItem implements Serializable, PreferenceNames, Cloneable {
	public static final String MODULE = "Picture";

	File source = null;

	HashMap extraFields;

	boolean hidden;

	int angle = 0;
	boolean flipped = false;
	boolean suppressServerAutoRotate = false;

	boolean online = false;
	URL urlFull = null;
	Dimension sizeFull = null;
	URL urlResized = null;
	Dimension sizeResized = null;
	URL urlThumbnail = null;
	Dimension sizeThumbnail = null;
	Rectangle cropTo = null;

	Album albumOnServer = null;
	int indexOnServer = -1;

	transient double fileSize = 0;
	transient int indexCache = -1;
	transient Dimension dimension = null;
	transient ExifData exif = null;
	transient String forceExtension = null;
	transient String uniqueId = null;

	/**
	 * Constructor for the Picture object
	 */
	public Picture(Gallery gallery) {
		super(gallery);

		setAllowsChildren(false);
	}


	/**
	 * Constructor for the Picture object
	 * 
	 * @param source File the Picture is based on
	 */
	public Picture(Gallery gallery, File source) {
		this(gallery);

		setSource(source);
	}

	public Object clone() {
		Picture newPicture = (Picture) super.clone();

		newPicture.source = source;

		newPicture.extraFields = extraFields;

		newPicture.angle = angle;
		newPicture.flipped = flipped;
		newPicture.suppressServerAutoRotate = suppressServerAutoRotate;

		newPicture.online = online;
		newPicture.urlFull = urlFull;
		newPicture.sizeFull = sizeFull;
		newPicture.urlResized = urlResized;
		newPicture.sizeResized = sizeResized;
		newPicture.urlThumbnail = urlThumbnail;
		newPicture.sizeThumbnail = sizeThumbnail;

		newPicture.fileSize = fileSize;
		newPicture.escapedCaption = escapedCaption;
		newPicture.indexCache = indexCache;

		newPicture.albumOnServer = albumOnServer;
		newPicture.indexOnServer = indexOnServer;

		return newPicture;
	}


	/**
	 * Sets the source file the Picture is based on
	 * 
	 * @param source The new file
	 */
	public void setSource(File source) {
		this.source = source;

		if (GalleryRemote._().properties.getBooleanProperty(SET_CAPTIONS_WITH_FILENAMES)) {
			String filename = source.getName();

			if (GalleryRemote._().properties.getBooleanProperty(CAPTION_STRIP_EXTENSION)) {
				int i = filename.lastIndexOf(".");

				if (i != -1) {
					filename = filename.substring(0, i);
				}
			}

			setCaption(filename);
		} else if (GalleryRemote._().properties.getBooleanProperty(SET_CAPTIONS_WITH_METADATA_COMMENT, false)
				&& getExifData() != null && getExifData().getCaption() != null) {
			setCaption(getExifData().getCaption());
		}

		fileSize = 0;
	}


	/**
	 * Gets the source file the Picture is based on
	 * 
	 * @return The source value
	 */
	public File getSource() {
		if (online) {
			throw new RuntimeException("Can't get source for an online file!");
		}

		return source;
	}

	/**
	 * Gets the fource file of the picture, prepared for upload.
	 * Called by GalleryComm to upload the picture.
	 * 
	 * @return The source value
	 */
	public File getUploadSource() {
		boolean useLossyCrop = false;
		File picture = getSource();
		Album album = getParentAlbum();

		// crop
		if (cropTo != null) {
			try {
				picture = ImageUtils.losslessCrop(picture.getPath(), cropTo);
			} catch (UnsupportedOperationException e) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't use ImageUtils to losslessly crop the image, will try lossy");
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
				useLossyCrop = true;
			}
		}

		// resize
		if (album.getResize()) {
			int i = album.getResizeDimension();

			if (i <= 0) {
				int l = album.getServerAutoResize();

				if (l != 0) {
					i = l;
				} else {
					// server can't tell us how to resize, try default
					i = GalleryRemote._().properties.getIntDimensionProperty(RESIZE_TO_DEFAULT);
				}
			}

			if (i != -1 || useLossyCrop) {
				try {
					picture = ImageUtils.resize(picture.getPath(), new Dimension(i, i), useLossyCrop?cropTo:null);
				} catch (UnsupportedOperationException e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't use ImageUtils to resize the image, it will be uploaded at the original size");
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
				}
			}
		} else if (useLossyCrop) {
			picture = ImageUtils.resize(picture.getPath(), null, useLossyCrop?cropTo:null);
		}

		// rotate
		if (angle != 0 || flipped) {
			try {
				picture = ImageUtils.rotate(picture.getPath(), angle, flipped, true);
			} catch (UnsupportedOperationException e) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't use jpegtran to resize the image, it will be uploaded unrotated");
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		return picture;
	}

	/**
	 * Gets the size of the file
	 * 
	 * @return The size value
	 */
	public double getFileSize() {
		if (fileSize == 0 && source != null && source.exists()) {
			fileSize = source.length();
		}

		return fileSize;
	}

	public void setFileSize(double fileSize) {
		if (!online) {
			throw new RuntimeException("Can't set the size of a local image");
		}

		this.fileSize = fileSize;
	}


	/**
	 * Gets the album this Picture is inside of
	 * 
	 * @return The album
	 */
	/*public Album getAlbum() {
		return album;
	}*/

	public String toString() {
		if (online) {
			return getName();
		} else {
			return source.getName();
		}
	}

	public int hashCode() {
		String path = null;

		if (online) {
			path = safeGetUrlFull().toString();
		} else {
			path = source.getName();
		}

		return path.hashCode();
	}

	// Hacks to allow Album to inherit from Picture and AbstractListModel
	public int getSize() {
		return 0;
	}

	public Object getElementAt(int index) {
		return null;
	}

	public void rotateRight() {
		angle = (angle + 1) % 4;
	}

	public void rotateLeft() {
		angle = (angle + 3) % 4;
	}

	public void flip() {
		flipped = !flipped;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public boolean isFlipped() {
		return flipped;
	}

	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	public String getExtraField(String name) {
		if (extraFields == null) {
			return null;
		}

		return (String) extraFields.get(name);
	}

	public String getExtraFieldsString() {
		if (extraFields == null) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		String sep = System.getProperty("line.separator");

		for (Iterator it = getParentAlbum().getExtraFields().iterator(); it.hasNext();) {
			String name = (String) it.next();
			String value = (String) extraFields.get(name);

			if (value != null) {
				sb.append(name).append(": ").append(value).append(sep);
			}
		}

		return sb.toString();
	}

	public void setExtraField(String name, String value) {
		if (extraFields == null) {
			extraFields = new HashMap();
		}

		extraFields.put(name, value);
	}

	public void removeExtraField(String name) {
		if (extraFields == null) {
			extraFields = new HashMap();
		}

		extraFields.remove(name);
	}

	public HashMap getExtraFieldsMap() {
		return extraFields;
	}

	public void setSuppressServerAutoRotate(boolean suppressServerAutoRotate) {
		this.suppressServerAutoRotate = suppressServerAutoRotate;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public URL getUrlFull() {
		if (!online) {
			throw new RuntimeException("Can't get URL for a local file!");
		}

		return urlFull;
	}

	public URL safeGetUrlFull() {
		if (!online) {
			throw new RuntimeException("Can't get URL for a local file!");
		}

		if (urlFull != null) {
			return urlFull;
		} else if (urlResized != null) {
			return urlResized;
		} else {
			throw new RuntimeException("Neither full nor resized URL!");
		}
	}

	public void setUrlFull(URL urlFull) {
		this.urlFull = urlFull;
	}

	public Dimension getSizeFull() {
		if (!online) {
			throw new RuntimeException("Can't get dimension for a local file!");
		}

		return sizeFull;
	}

	public Dimension safeGetSizeFull() {
		if (!online) {
			throw new RuntimeException("Can't get dimension for a local file!");
		}

		if (sizeFull != null) {
			return sizeFull;
		} else if (sizeResized != null) {
			return sizeResized;
		} else {
			throw new RuntimeException("Neither full nor resized size!");
		}
	}

	public void setSizeFull(Dimension sizeFull) {
		this.sizeFull = sizeFull;
	}

	public URL getUrlResized() {
		if (!online) {
			throw new RuntimeException("Can't get URL for a local file!");
		}

		return urlResized;
	}

	public void setUrlResized(URL urlResized) {
		this.urlResized = urlResized;
	}

	public Dimension getSizeResized() {
		if (!online) {
			throw new RuntimeException("Can't get dimension for a local file!");
		}

		return sizeResized;
	}

	public void setSizeResized(Dimension sizeResized) {
		this.sizeResized = sizeResized;
	}

	public URL getUrlThumbnail() {
		if (!online) {
			throw new RuntimeException("Can't get URL for a local file!");
		}

		return urlThumbnail;
	}

	public void setUrlThumbnail(URL urlThumbnail) {
		this.urlThumbnail = urlThumbnail;
	}

	public Dimension getSizeThumbnail() {
		if (!online) {
			throw new RuntimeException("Can't get dimension for a local file!");
		}

		return sizeThumbnail;
	}

	public void setSizeThumbnail(Dimension sizeThumbnail) {
		this.sizeThumbnail = sizeThumbnail;
	}

	public String getName() {
		String path = safeGetUrlFull().getPath();

		int i = path.lastIndexOf('/');

		if (i != -1) {
			path = path.substring(i + 1);
		}

		i = path.lastIndexOf('.');
		if (i != -1) {
			path = path.substring(0, i);
		}

		return path;
	}

	public Album getAlbumOnServer() {
		if (!online) {
			throw new RuntimeException("Can't get Album on server for a local file!");
		}

		return albumOnServer;
	}

	public void setAlbumOnServer(Album albumOnServer) {
		this.albumOnServer = albumOnServer;
	}

	public int getIndexOnServer() {
		if (!online) {
			throw new RuntimeException("Can't get Index on server for a local file!");
		}

		return indexOnServer;
	}

	public int getIndex() {
		Album album = getParentAlbum();
		if (indexCache == -1
				|| indexCache >= album.pictures.size()
				|| album.pictures.get(indexCache) != this) {
			return album.pictures.indexOf(this);
		} else {
			return indexCache;
		}
	}

	public void setIndexOnServer(int indexOnServer) {
		this.indexOnServer = indexOnServer;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public Rectangle getCropTo() {
		return cropTo;
	}

	public void setCropTo(Rectangle cropTo) {
		Log.log(Log.LEVEL_TRACE, MODULE, "setCropTo " + cropTo);

		if (cropTo != null) {
			// make very sure the crop dimensions make sense
			Dimension d = getDimension();
			if (cropTo.x < 0) {
				cropTo.x = 0;
			}
			if (cropTo.y < 0) {
				cropTo.y = 0;
			}
			if (cropTo.width + cropTo.x > d.width) {
				cropTo.width = d.width - cropTo.x;
			}
			if (cropTo.height + cropTo.y > d.height) {
				cropTo.height = d.height - cropTo.y;
			}
		}

		this.cropTo = cropTo;
	}

	public Dimension getDimension() {
		if (dimension == null) {
			dimension = ImageUtils.getPictureDimension(this);
		}

		return dimension;
	}

	public ExifData getExifData() {
		if (exif == null && ImageUtils.isExifAvailable()) {
			exif = ImageUtils.getExifData(source.getPath());
		}

		return exif;
	}

	public String getForceExtension() {
		return forceExtension;
	}

	public void setForceExtension(String forceExtension) {
		this.forceExtension = forceExtension;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
}

