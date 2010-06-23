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

import com.gallery.GalleryRemote.*;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.prefs.GalleryProperties;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.prefs.PropertiesFile;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.awt.*;

/**
 * Gallery model
 * 
 * @author paour
 */

public class Gallery extends DefaultTreeModel implements Serializable, PreferenceNames {
	public static final String MODULE = "Gallery";

	String urlString = null;
	String username;
	String password;
	String alias;
	String key;
	String userAgent = "Gallery Remote " + GalleryRemote._().properties.getProperty("version");
	boolean autoLoadOnStartup = false;

	transient GalleryComm comm = null;

	transient StatusUpdate su;
	transient private int prefsIndex;
	transient private Boolean ambiguousUrl;
	transient private boolean blockWrites = false;
	transient public boolean cookieLogin = false;
	transient public int galleryVersion = 1;
	transient public int forceGalleryVersion = 0;
	transient public String forceProtocolEncoding = null;
	transient public int resizeJpegQuality = -1;
	transient public boolean dirty = false;
	transient public int type = TYPE_STANDALONE;

	public static final int TYPE_STANDALONE = 99;
	public static final int TYPE_APPLET = 99;

	public static final int TOSTRING_MAXLEN = 40;
	public String authToken;

	public Gallery(StatusUpdate su) {
		super(null);
		this.su = su;

		// make sure to update flat album list when tree is changed
		addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				flatAlbumList = null;
			}

			public void treeNodesInserted(TreeModelEvent e) {
				treeNodesChanged(e);
			}

			public void treeNodesRemoved(TreeModelEvent e) {
				treeNodesChanged(e);
			}

			public void treeStructureChanged(TreeModelEvent e) {
				treeNodesChanged(e);
			}
		});

		// when loading from prefs, galleries not yet created. No matter: in that case, the
		// prefsIndex is forced.
		if (GalleryRemote._().getMainFrame() != null) {
			prefsIndex = GalleryRemote._().getCore().getGalleries().getSize();
		}
	}

	/*
	* **** Gallery online management ****
	*/

	public void doUploadFiles(StatusUpdate su) {
		GalleryComm comm = getComm(su);

		if (comm != null) {
			comm.uploadFiles(su, true);
		} else {
			// don't worry about it, an error message is displayed somewhere else.
		}
	}

	public void doFetchAlbums(StatusUpdate su) {
		doFetchAlbums(su, true);
	}

	public void doFetchAlbums(StatusUpdate su, boolean async) {
		GalleryComm comm = getComm(su);

		if (comm != null) {
			comm.fetchAlbums(su, async);
		} else {
			// don't worry about it, an error message is displayed somewhere else.
		}
	}

	public void doNewAlbum(Album a, StatusUpdate su) {
		Log.log(Log.LEVEL_INFO, MODULE, "Creating new album " + a.toString());

		// create album synchronously
		getComm(su).newAlbum(su, a, false);

		// refresh album list asynchronously
        //fetchAlbums(su);

//		if (!newAlbumName.equals(a.getName())) {
//			//Log.log(Log.LEVEL_INFO, MODULE, "Album name probably conflicted on the server, need to reload album list");
//			//getComm(su).fetchAlbums(su, false);
//			a.setName(newAlbumName);
//		}

		//addAlbum(a);

		//return newAlbumName;
	}

	public void incrementViewCount(Picture p, StatusUpdate su) {
		if (getComm().hasCapability(GalleryCommCapabilities.CAPA_INCREMENT_VIEW_COUNT)) {
			Log.log(Log.LEVEL_INFO, MODULE, "Incrementing viewCount on " + p.toString());

			getComm(su).incrementViewCount(su, p);
		}
	}

	public void logOut() {
		//if (comm != null) {
		//	comm.logOut();
		//}
		comm = null;

		setRoot(null);

		dirty = false;
	}

	/*
	* **** Gallery contents handling ****
	*/

	public File getGalleryDefaultFile() {
		StringBuffer defaultFilePath = new StringBuffer();

		defaultFilePath.append(System.getProperty("user.home"));
		defaultFilePath.append(File.separator);
		defaultFilePath.append(".GalleryRemote");
		defaultFilePath.append(File.separator);
		defaultFilePath.append("backup.");
		defaultFilePath.append(getPrefsIndex());
		defaultFilePath.append(".grg");

		// Define which file is used to store the current state if
		// the user does not provide a specific file.
		return (new File(defaultFilePath.toString()));
	}

	public ArrayList<Picture> getAllPictures() {
		return getAllPictures(false);
	}

	public ArrayList<Picture> getAllUploadablePictures() {
		return getAllPictures(true);
	}

	public ArrayList<Picture> getAllPictures(boolean onlyUploadable) {
		ArrayList<Picture> pictures = new ArrayList<Picture>();
		ArrayList<Album> albumList = getFlatAlbumList();

		if (albumList != null) {
			for (Album a : albumList) {
				if (onlyUploadable) {
					pictures.addAll(a.getUploadablePicturesList());
				} else {
					pictures.addAll(a.getPicturesList());
				}
			}
		}

		return pictures;
	}

	/**
	 * Delete all of the pictures from the current gallery without
	 * affecting the list of albums that are loaded.  This is used
	 * by the "New" function in the UI.
	 */
	public void deleteAllPictures() {
		ArrayList albumList = getFlatAlbumList();

		if (albumList != null) {
			Iterator i = albumList.iterator();
			while (i.hasNext()) {
				Album a = (Album) i.next();
				a.clearPictures();
			}
		}
	}

	public int countAllPictures() {
		int c = 0;
		ArrayList albumList = getFlatAlbumList();

		if (albumList != null) {
			Iterator i = albumList.iterator();
			while (i.hasNext()) {
				Album a = (Album) i.next();

				c += a.getSize();
			}
		}

		return c;
	}

	public boolean hasPictures() {
		ArrayList albumList = getFlatAlbumList();

		if (albumList != null) {
			Iterator i = albumList.iterator();
			while (i.hasNext()) {
				Album a = (Album) i.next();
				if (a.getSize() > 0) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	* **** Gallery URL management ****
	*/

	public static String reformatUrlString(String urlString, boolean trailingSlash) {
		if (urlString == null) {
			throw new IllegalArgumentException("urlString must not be null");
		}

		if (trailingSlash && !urlString.endsWith("/")) {
			urlString += "/";
		}

		if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
			urlString = "http://" + urlString;
		}

		return urlString;
	}

	/* Standalone URL */

	public void setUrlString(String urlString) {
		if (urlString == null) {
			this.urlString = null;
			return;
		}

		this.urlString = reformatUrlString(urlString, true);

		if (!blockWrites && this.urlString != null) {
			GalleryRemote._().properties.setProperty(GURL + prefsIndex, this.urlString);
		}
	}

	public String getUrlString() {
		if (urlString != null) {
			return urlString;
		} else {
			return "http://example.com/gallery";
		}
	}

	/* Applet URL */

	public void setApUrlString(String urlString) {
		this.urlString = urlString;
	}

	/* Desired User-Agent */

	public void setUserAgent(String userAgent) {
	        this.userAgent = userAgent;
	}

	public String getUserAgent() {
	        return userAgent;
	}

	/* Generic */

	public URL getUrl() {
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Malformed URL.");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			JOptionPane.showMessageDialog((Component) su, "Malformed URL (" + e.getMessage() + ")",
					"Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/*
	* **** Gallery properties management ****
	*/

	public void setUsername(String username) {
		if (/*username != null && username.length() > 0
		&&*/ !username.equals(this.username)) {

			this.username = username;

			logOut();

			if (!blockWrites) {
				GalleryRemote._().properties.setProperty(USERNAME + prefsIndex, username);
			}
		}
	}

	public void setPassword(String password) {
		//Log.log(Log.TRACE, MODULE, "setpassword: " + password);
		if (/*password != null && password.length() > 0
		&&*/ !password.equals(this.password)) {

			this.password = password;

			logOut();

			if (!blockWrites) {
				if (GalleryRemote._().properties.getBooleanProperty(SAVE_PASSWORDS)) {
					GalleryRemote._().properties.setBase64Property(PASSWORD + prefsIndex, password);
				} else {
					GalleryRemote._().properties.setProperty(PASSWORD + prefsIndex, null);
				}
			}
		}
	}

	public void setAlias(String alias) {
		this.alias = alias;

		if (!blockWrites) {
			GalleryRemote._().properties.setProperty(ALIAS + prefsIndex, alias);
		}
	}

	public void setKey(String key) {
		if (key == null) {
			this.key = null;
		} else if (!key.equals(this.key)) {
			this.key = key;

			logOut();

			if (!blockWrites) {
				GalleryRemote._().properties.setProperty(KEY + prefsIndex, key);
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getAlias() {
		return alias;
	}

	public String getKey() {
		return key;
	}

	public static Gallery readFromProperties(GalleryProperties p, int prefsIndex, StatusUpdate su) {
		return readFromProperties(p, prefsIndex, su, true);
	}

	public static Gallery readFromProperties(GalleryProperties p, int prefsIndex, StatusUpdate su, boolean mustHaveUsername) {
		String url = p.getProperty(GURL + prefsIndex);
		String username = p.getProperty(USERNAME + prefsIndex, true);

		if (mustHaveUsername && username == null) {
			return null;
		}

		String password = null;
		try {
			password = p.getBase64Property(PASSWORD + prefsIndex);
		} catch (NumberFormatException e) {
		}

		Log.log(Log.LEVEL_INFO, MODULE, "Loaded saved URL " + prefsIndex + ": " + url + " (" + username + "/******)");

		Gallery g = new Gallery(su);
		if (GalleryRemote._().getCore() instanceof TreeModelListener) {
			g.addTreeModelListener((TreeModelListener) GalleryRemote._().getCore());
		}

		g.setBlockWrites(true);
		g.username = username;
		g.password = password;
		g.setUrlString(url);

		g.setAlias(p.getProperty(ALIAS + prefsIndex));
		g.setKey(p.getProperty(KEY + prefsIndex));

		g.forceGalleryVersion = p.getIntProperty(FORCE_GALLERY_VERSION + prefsIndex, 0);
		g.forceProtocolEncoding = p.getProperty(FORCE_PROTOCOL_ENCODING + prefsIndex);
		g.resizeJpegQuality = p.getIntProperty(RESIZE_JPEG_QUALITY + prefsIndex, -1);
		g.autoLoadOnStartup = p.getBooleanProperty(AUTO_LOAD_ON_STARTUP + prefsIndex, false);

		g.setPrefsIndex(prefsIndex);

		g.setBlockWrites(false);
		return g;
	}

	public void writeToProperties(PropertiesFile p) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Writing to properties: " + toString());

		p.setProperty(GURL + prefsIndex, urlString);
		p.setProperty(USERNAME + prefsIndex, username);
		if (getPassword() != null && p.getBooleanProperty(SAVE_PASSWORDS)) {
			p.setBase64Property(PASSWORD + prefsIndex, password);
		} else {
			p.setProperty(PASSWORD + prefsIndex, null);
		}
		if (getAlias() != null && getAlias().length() > 0) {
			p.setProperty(ALIAS + prefsIndex, getAlias());
		} else {
			p.setProperty(ALIAS + prefsIndex, null);
		}
		if (getKey() != null && getKey().length() > 0) {
			p.setProperty(KEY + prefsIndex, getKey());
		} else {
			p.setProperty(KEY + prefsIndex, null);
		}

		p.setBooleanProperty(AUTO_LOAD_ON_STARTUP + prefsIndex, autoLoadOnStartup);
	}

	public static void removeFromProperties(PropertiesFile p, int n) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Removed from properties: " + n);

		p.setProperty(GURL + n, null);
		p.setProperty(USERNAME + n, null);
		p.setProperty(PASSWORD + n, null);
		p.setProperty(ALIAS + n, null);
		p.setProperty(KEY + n, null);
		p.setProperty(FORCE_GALLERY_VERSION + n, null);
		p.setProperty(FORCE_PROTOCOL_ENCODING + n, null);
		p.setProperty(RESIZE_JPEG_QUALITY + n, null);
		p.setProperty(AUTO_LOAD_ON_STARTUP + n, null);
	}

	public void setPrefsIndex(int prefsIndex) {
		this.prefsIndex = prefsIndex;
	}

	protected int getPrefsIndex() {
		return (this.prefsIndex);
	}

	public String toString() {
		return toString(true);
	}

	public String toString(boolean disambiguate) {
		if (alias != null) {
			return alias;
		}

		String tmp = null;

		tmp = urlString;

		if (tmp == null) {
			tmp = "http://";
		}

		if (disambiguate && isAmbiguousUrl()) {
			if (username == null || username.length() == 0) {
				tmp += "[username not set]";
			} else {
				tmp += " [" + username + "]";
			}
		}

		if (tmp.length() > TOSTRING_MAXLEN) {
			tmp = tmp.substring(0, TOSTRING_MAXLEN) + "...";
		}

		return tmp;
	}

	public boolean isAmbiguousUrl() {
		if (ambiguousUrl == null) {
			ListModel galleries = GalleryRemote._().getCore().getGalleries();
			String myUrl = toString(false);

			for (int i = 0; i < galleries.getSize(); i++) {
				Gallery g = (Gallery) galleries.getElementAt(i);

				if (g != this && myUrl.equals(g.toString(false))) {
					ambiguousUrl = new Boolean(true);
					break;
				}
			}

			if (ambiguousUrl == null) {
				ambiguousUrl = new Boolean(false);
			}
		}

		return ambiguousUrl.booleanValue();
	}

	public static void uncacheAmbiguousUrl() {
		ListModel galleries = GalleryRemote._().getCore().getGalleries();

		for (int i = 0; i < galleries.getSize(); i++) {
			Gallery g = (Gallery) galleries.getElementAt(i);

			g.ambiguousUrl = null;
		}
	}

	/*
	* Miscellaneous
	*/

	/**
	 * Lazy instantiation for the GalleryComm instance.
	 */
	public GalleryComm getComm(StatusUpdate su) {
		if (comm == null && urlString != null) {
			URL url = getUrl();
			if (url != null) {
				comm = GalleryComm.getCommInstance(su, this);

				if (comm == null) {
					Log.log(Log.LEVEL_ERROR, MODULE, "No protocol implementation found");
					su.error(GRI18n.getString(MODULE, "galleryNotFound", new Object[] {urlString}));
				}
			}
		}

		return comm;
	}

	public GalleryComm getComm() {
		return comm;
	}

	public boolean hasComm() {
		return comm != null;
	}

	public Album getAlbumByName(String name) {
		ArrayList<Album> albumList = getFlatAlbumList();
		if (albumList == null || name == null) {
			return null;
		}

		for (Album a : albumList) {
			if (name.equals(a.getName())) {
				return a;
			}
		}

		return null;
	}

	public void setBlockWrites(boolean blockWrites) {
		this.blockWrites = blockWrites;
	}

	ArrayList<Album> flatAlbumList = null;
	public ArrayList<Album> getFlatAlbumList() {
		if (flatAlbumList == null) {
			if (getRoot() != null) {
				flatAlbumList = Collections.list(new TreeEnumeration((TreeNode) getRoot()));
			}
		}

		return flatAlbumList;
	}

	public Album createRootAlbum() {
		if (getRoot() != null) {
			throw new IllegalStateException("Root album already exists");
		}

		Album album = new Album(this);
		album.setTitle(GRI18n.getString("Common", "rootAlbmTitle"));
		album.setName("root.album");
		setRoot(album);

		return (Album) getRoot();
	}

	public Album getRootAlbum() {
		return (Album) getRoot();
	}

	public int getGalleryVersion() {
		if (forceGalleryVersion != 0) {
			// override
			return forceGalleryVersion;
		}

		return galleryVersion;
	}

	public void setGalleryVersion(int galleryVersion) {
		this.galleryVersion = galleryVersion;
	}

	public int getResizeJpegQuality() {
		return resizeJpegQuality;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isAutoLoadOnStartup() {
		return autoLoadOnStartup;
	}

	public void setAutoLoadOnStartup(boolean autoLoadOnStartup) {
		this.autoLoadOnStartup = autoLoadOnStartup;
		
		if (!blockWrites) {
			GalleryRemote._().properties.setBooleanProperty(AUTO_LOAD_ON_STARTUP + prefsIndex, autoLoadOnStartup);
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	class TreeEnumeration implements Enumeration {
		protected TreeNode root;
		protected Enumeration children;
		protected Enumeration subtree;
		boolean rootSent = false;

		public TreeEnumeration(TreeNode rootNode) {
			super();
			root = rootNode;
			children = root.children();
			subtree = DefaultMutableTreeNode.EMPTY_ENUMERATION;
		}

		public boolean hasMoreElements() {
			return !rootSent || subtree.hasMoreElements() || children.hasMoreElements();
		}

		public Object nextElement() {
			Object retval = null;

			if (!rootSent) {
				retval = root;
				rootSent = true;
			} else if (subtree.hasMoreElements()) {
				retval = subtree.nextElement();
			} else if (children.hasMoreElements()) {
				subtree = new TreeEnumeration(
						(TreeNode)children.nextElement());
				retval = subtree.nextElement();
			}

			return retval;
		}
	}

	public Album newAlbum() {
		return new Album(this);
	}
}
