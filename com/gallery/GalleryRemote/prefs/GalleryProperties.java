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
package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Base64;
import com.gallery.GalleryRemote.Log;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GalleryProperties: access property data with a higher level of abstraction
 * 
 * @author paour
 */
public class GalleryProperties extends Properties implements PreferenceNames {
	public static final String MODULE = "GalProps";

	SimpleDateFormat dateFormat
			= new SimpleDateFormat("yyyy/MM/dd");

	// caches
	protected Dimension thumbnailSize = null;
	protected Rectangle mainBounds = null;
	protected Rectangle previewBounds = null;


	public GalleryProperties(Properties p) {
		super(p);
	}

	public GalleryProperties() {
	}

	public String getProperty(String key) {
		return getProperty(key, false);
	}

	public String getProperty(String key, boolean emptySignificant) {
		Object oval = super.get(key);
		String sval = (oval instanceof String) ? (String)oval : null;
		String value = ((sval == null || (sval.length() == 0 && ! emptySignificant)) && (defaults != null)) ? defaults.getProperty(key) : sval;

		if (value == null || (value.length() == 0 && ! emptySignificant)) {
			return null;
		} else {
			return value;
		}
	}

	public void copyProperties(Properties source) {
		Enumeration names = source.propertyNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = source.getProperty(name);
			String currentValue = getProperty(name);

			// don't override existing property with empty one
			if ((currentValue == null || currentValue.length() == 0)
					|| (value != null && value.length() != 0)) {
				super.setProperty(name, value);
			}
		}
	}

	public File getCurrentDirectory() {
		String currentDirectory = getProperty("filedialogPath");
		if (currentDirectory != null) {
			return new File(currentDirectory);
		} else {
			return null;
		}
	}

	public void setCurrentDirectory(File currentDirectory) {
		setProperty("filedialogPath", currentDirectory.getPath());
	}


	public boolean getShowPreview() {
		return getBooleanProperty("showPreview");
	}


	public void setShowPreview(boolean showPreview) {
		setProperty("showPreview", String.valueOf(showPreview));
	}


	public boolean getShowPath() {
		return getBooleanProperty("showPath");
	}


	public void setShowPath(boolean showPath) {
		setProperty("showPath", String.valueOf(showPath));
	}


	public boolean getShowThumbnails() {
		return getBooleanProperty(SHOW_THUMBNAILS);
	}


	public void setShowThumbnails(boolean showThumbnails) {
		setProperty(SHOW_THUMBNAILS, String.valueOf(showThumbnails));
	}


	public Dimension getThumbnailSize() {
		if (thumbnailSize == null) {
			thumbnailSize = getDimensionProperty(THUMBNAIL_SIZE);
		}

		return thumbnailSize;
	}


	public Rectangle getMainBounds() {
		if (mainBounds == null) {
			mainBounds = getRectangleProperty("mainBounds");
		}

		return mainBounds;
	}


	public Rectangle getPreviewBounds() {
		if (previewBounds == null) {
			previewBounds = getRectangleProperty("previewBounds");
		}

		return previewBounds;
	}


	public void setMainBounds(Rectangle r) {
		setRectangleProperty("mainBounds", r);
	}


	public void setPreviewBounds(Rectangle r) {
		setRectangleProperty("previewBounds", r);
	}


	public void setThumbnailSize(Dimension size) {
		thumbnailSize = size;
		setDimensionProperty(THUMBNAIL_SIZE, size);
	}


	public Dimension getDimensionProperty(String key) {
		String value = getProperty(key);
		if (value == null) return null;

		StringTokenizer st;
		if (value != null && (st = new StringTokenizer(value, ",")).countTokens() == 2) {
			return new Dimension(Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()));
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Parameter " + key + " = " + value + " is missing or malformed (should be width,height)");
			return null;
		}
	}

	public void setDimensionProperty(String key, Dimension d) {
		setProperty(key, ((int) d.getWidth()) + "," + ((int) d.getHeight()));
	}

	public int getIntDimensionProperty(String key) {
		int i = getIntProperty(key + "1", -1);

		if (i == -1) {
			Dimension d = getDimensionProperty(key);
			if (d != null)	{
				i = d.width;
			} else {
				i = 0;
			}

			setIntDimensionProperty(key, i);
		}
		
		return i;
	}

	public void setIntDimensionProperty(String key, int i) {
		setProperty(key + "1", String.valueOf(i));
	}

	public Color getColorProperty(String key) {
		String value = getProperty(key);
		if (value == null) return null;

		StringTokenizer st;
		if (value != null && (st = new StringTokenizer(value, ",")).countTokens() == 3) {
			return new Color(Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()));
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Parameter " + key + " = " + value + " is missing or malformed (should be red,green,blue)");
			return null;
		}
	}

	public void setColorProperty(String key, Color c) {
		setProperty(key, ((int) c.getRed()) + "," + ((int) c.getGreen()) + "," + ((int) c.getBlue()));
	}

	/**
	 * getLoadLastMRU returns whether we should automatically load the
	 * last MRU file when GR starts. If this is true then we should try
	 * to load getMRUItem(0).
	 * 
	 * @return true if we should load the last opened file.
	 */
	public boolean getLoadLastMRU() {
		return (getBooleanProperty(LOAD_LAST_FILE, false));
	}

	/**
	 * setLoadLastMRU sets the current value of LOAD_LAST_FILE to the passed
	 * value.
	 * 
	 * @param loadLastMRU the new value of LOAD_LAST_FILE.
	 */
	public void setLoadLastMRU(boolean loadLastMRU) {
		setBooleanProperty(LOAD_LAST_FILE, loadLastMRU);
	}

	/**
	 * setMRUCountProperty sets the number of MRU items that we will show
	 * in the File menu.  This change will not be seen until the file menu
	 * is rebuilt (this happens on file save, file open, etc.).
	 * 
	 * @param MRUCountProperty The new MRU count property.
	 */
	public void setMRUCountProperty(int MRUCountProperty) {
		setIntProperty(MRU_COUNT, MRUCountProperty);
	}

	/**
	 * getMRUCountProperty returns the Most Recently Used count (the number
	 * of MRU entries we should show in the menu.
	 * 
	 * @return the mruCount value from the properties file.
	 */
	public int getMRUCountProperty() {
		return (getIntProperty(MRU_COUNT, 4));
	}

	/**
	 * getMRUItem returns the MRU item at the passed number. These are
	 * stored in property items that end in the number with a prefix of
	 * MRU_BASE. If the item does not exist in the properties file we
	 * will return null.
	 * 
	 * @param mruItemNumber the number of the MRU item to return
	 * @return the string stored at item mruItemNumber.  This is a file (as a
	 *         string) and was probably set by addMRUItem().
	 */
	public String getMRUItem(int mruItemNumber) {
		return (getProperty(MRU_BASE + mruItemNumber, null));
	}

	/**
	 * removeMRUItem removes the designated MRU item. Note that this
	 * does not get this new value into the file, if you want this MRU
	 * value to be permanent you will have to also force the properties
	 * file to save.
	 * 
	 * @param mruItemNumber the number of the MRU item to return
	 */
	public void removeMRUItem(int mruItemNumber) {
		remove(MRU_BASE + mruItemNumber);
	}

	/**
	 * setMRUItem set the designated MRU item to the passed string. Note that this
	 * does not get this new value into the file, if you want this MRU
	 * value to be permanent you will have to also force the properties
	 * file to save.
	 * 
	 * @param mruItemNumber the number of the MRU item to set
	 * @param mruItem       the MRU item to set
	 */
	public void setMRUItem(int mruItemNumber, String mruItem) {
		setProperty(MRU_BASE + mruItemNumber, mruItem);
	}

	/**
	 * addMRUItem saves the passed mruItem onto the list of MRU items
	 * stored in the properties file and causes the properties file to
	 * be written out. If the item was already in the list, then it
	 * simply moves the passed item to the top of the list.
	 * <p/>
	 * Note that this does not get this new value into the file, if you
	 * want this MRU value to be permanent you will have to also force
	 * the properties file to save.
	 * 
	 * @param mruItem The mruItem File to add.
	 */
	public void addMRUItem(File mruItem) {
		// First get all of the MRU items from the current file
		try {
			String currentItem = mruItem.getCanonicalPath();
			addMRUItem(currentItem);
		} catch (IOException ioe) {
			// Just log it as there isn't really anything else we can do just now.
			Log.log(Log.LEVEL_ERROR, MODULE, "mruItem add attempt with an invalid File object");
			Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
		}
	}

	/**
	 * addMRUItem saves the passed mruItem onto the list of MRU items
	 * stored in the properties file and causes the properties file to
	 * be written out. If the item was already in the list, then it
	 * simply moves the passed item to the top of the list.
	 * <p/>
	 * Note that this does not get this new value into the file, if you
	 * want this MRU value to be permanent you will have to also force
	 * the properties file to save.
	 * 
	 * @param mruItem The mruItem path String to add.
	 */
	public void addMRUItem(String newMRUItem) {
		// First get all of the MRU items from the current file
		Vector mruItems = new Vector();
		mruItems.addElement(newMRUItem);
		for (int i = 0; i < 20; i++) {
			String nextItem = getMRUItem(i);

			// Skip any missing items and if the current item is already
			// in the list skip it too.
			if (null != nextItem && !nextItem.equals(newMRUItem)) {
				mruItems.addElement(nextItem);
			}
		}

		// First clean up the list.
		for (int i = 1; i <= 20; i++) {
			removeMRUItem(i);
		}

		// OK, we now have the new list, add the properties back.
		for (int i = 1; i <= mruItems.size(); i++) {
			setMRUItem(i, (String) mruItems.elementAt(i - 1));
		}
	}

	public Rectangle getRectangleProperty(String key) {
		String value = getProperty(key);
		if (value == null) return null;

		StringTokenizer st;
		if (value != null && (st = new StringTokenizer(value, ",")).countTokens() == 4) {
			return new Rectangle(Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()));
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Parameter " + key + " is missing or malformed (should be x,y,width,height)");
			return null;
		}
	}

	public void setRectangleProperty(String key, Rectangle rect) {
		setProperty(key, ((int) rect.getX()) + "," + ((int) rect.getY()) + ","
				+ ((int) rect.getWidth()) + "," + ((int) rect.getHeight()));
	}


	public boolean getBooleanProperty(String key) {
		String booleanS = getProperty(key);

		if (booleanS != null) {
			if (booleanS.equalsIgnoreCase("yes") || booleanS.equalsIgnoreCase("true")) {
				return true;
			} else if (booleanS.equalsIgnoreCase("no") || booleanS.equalsIgnoreCase("false")) {
				return false;
			}
		}

		throw new NumberFormatException("Parameter " + key + " = " + booleanS + " is missing or malformed (should be true/yes or false/no)");
	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		try {
			return getBooleanProperty(key);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public void setBooleanProperty(String key, boolean value) {
		setProperty(key, value ? "true" : "false");
	}


	public int getIntProperty(String key) {
		String intS = getProperty(key);
		try {
			return Integer.valueOf(intS).intValue();
		} catch (Exception e) {
			throw new NumberFormatException("Parameter " + key + " = " + intS + " is missing or malformed (should be an integer value)");
		}
	}

	public int getIntProperty(String key, int defaultValue) {
		try {
			return getIntProperty(key);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public void setIntProperty(String key, int value) {
		setProperty(key, String.valueOf(value));
	}


	public String getBase64Property(String key) {
		String base64S = getProperty(key);
		if (base64S == null) return null;

		try {
			return Base64.decode(base64S);
		} catch (Error e) {
			throw new NumberFormatException("Parameter " + key + " = " + base64S + " is missing or malformed (should be a Base64 value)");
		}
	}

	public void setBase64Property(String key, String value) {
		setProperty(key, Base64.encode(value));
	}


	public Date getDateProperty(String key) {
		String dateS = getProperty(key);
		if (dateS == null) return null;

		try {
			return dateFormat.parse(dateS);
		} catch (ParseException e) {
			throw new NumberFormatException("Parameter " + key + " = " + dateS + " is missing or malformed (should be a Date value (yyyy/mm/dd))");
		}
	}

	public void setDateProperty(String key, Date date) {
		setProperty(key, dateFormat.format(date));
	}

	public String getProperty(String key, String defaultValue) {
		String tmp = getProperty(key);

		if (tmp == null) {
			return defaultValue;
		} else {
			return tmp;
		}
	}

	public void logProperties(int level, String module) {
		if (module == null) {
			module = MODULE;
		}

		ArrayList names = new ArrayList(100);
		Enumeration e = propertyNames();
		while (e.hasMoreElements()) {
			names.add(e.nextElement());
		}

		Object[] namesArray = names.toArray();
		Arrays.sort(namesArray);

		for (int i = 0; i < namesArray.length; i++) {
			String name = (String) namesArray[i];
			Log.log(level, module, logPropertiesHelper(name));
		}
	}

	public String logPropertiesHelper(String name) {
		return name + "= |" + getProperty(name) + "|";
	}

	public void uncache() {
		thumbnailSize = null;
		mainBounds = null;
		previewBounds = null;
	}
}

