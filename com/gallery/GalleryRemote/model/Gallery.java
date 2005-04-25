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
 * @created 17 août 2002
 */

public class Gallery extends DefaultTreeModel implements Serializable, PreferenceNames {
	public static final String MODULE = "Gallery";

	String stUrlString = null;
	String pnLoginUrlString = null;
	String pnGalleryUrlString = null;
	String phpnLoginUrlString = null;
	String phpnGalleryUrlString = null;
	String glLoginUrlString = null;
	String glGalleryUrlString = null;
	String username;
	String password;
	String alias;
	String userAgent = null;
	int type = TYPE_STANDALONE;

	transient GalleryComm comm = null;

	transient StatusUpdate su;
	transient private int prefsIndex;
	transient private Boolean ambiguousUrl;
	transient private boolean blockWrites = false;
	transient public boolean cookieLogin = false;
	transient public int galleryVersion = 1;
	transient public int forceGalleryVersion = 0;

	public static String types[] = new String[]{STANDALONE, POSTNUKE, PHPNUKE, GEEKLOG};
	public static final int TYPE_STANDALONE = 0;
	public static final int TYPE_POSTNUKE = 1;
	public static final int TYPE_PHPNUKE = 2;
	public static final int TYPE_GEEKLOG = 3;

	public static final int TYPE_APPLET = 99;

	public static final int TOSTRING_MAXLEN = 40;

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

	public String doNewAlbum(Album a, StatusUpdate su) {
		Log.log(Log.LEVEL_INFO, MODULE, "Creating new album " + a.toString());

		// create album synchronously
		String newAlbumName = getComm(su).newAlbum(su, a.getParentAlbum(), a.getName(),
				a.getTitle(), a.getCaption(), false);

		// refresh album list asynchronously
        //fetchAlbums(su);

		if (!newAlbumName.equals(a.getName())) {
			//Log.log(Log.LEVEL_INFO, MODULE, "Album name probably conflicted on the server, need to reload album list");
			//getComm(su).fetchAlbums(su, false);
			a.setName(newAlbumName);
		}

		//addAlbum(a);

		return newAlbumName;
	}

	public void logOut() {
		if (comm != null) {
			comm.logOut();
		}
		comm = null;

		setRoot(null);
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

	public ArrayList getAllPictures() {
		return getAllPictures(false);
	}

	public ArrayList getAllUploadablePictures() {
		return getAllPictures(true);
	}

	public ArrayList getAllPictures(boolean onlyUploadable) {
		ArrayList pictures = new ArrayList();
		ArrayList albumList = getFlatAlbumList();

		if (albumList != null) {
			Iterator i = albumList.iterator();
			while (i.hasNext()) {
				Album a = (Album) i.next();

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

	public void setStUrlString(String urlString) {
		if (urlString == null) {
			stUrlString = null;
			return;
		}

		stUrlString = reformatUrlString(urlString, true);

		if (!blockWrites && stUrlString != null) {
			GalleryRemote._().properties.setProperty(URL + prefsIndex, stUrlString);
		}
	}

	public String getStUrlString() {
		if (stUrlString != null) {
			return stUrlString;
		} else {
			return "http://example.com/gallery";
		}
	}

	/* PostNuke Gallery URL */

	public void setPnGalleryUrlString(String urlString) {
		if (urlString == null) {
			pnGalleryUrlString = null;
			return;
		}

		pnGalleryUrlString = reformatUrlString(urlString, false);

		if (!blockWrites && pnGalleryUrlString != null) {
			GalleryRemote._().properties.setProperty(PN_GALLERY_URL + prefsIndex, pnGalleryUrlString);
		}
	}

	public String getPnGalleryUrlString() {
		if (pnGalleryUrlString != null) {
			return pnGalleryUrlString.toString();
		} else {
			return "http://example.com/modules.php?op=modload&name=gallery&file=index&include=$GALLERYFILE$";
		}
	}

	/* PostNuke Login URL */

	public void setPnLoginUrlString(String urlString) {
		if (urlString == null) {
			pnLoginUrlString = null;
			return;
		}

		pnLoginUrlString = reformatUrlString(urlString, false);

		if (!blockWrites && pnLoginUrlString != null) {
			GalleryRemote._().properties.setProperty(PN_LOGIN_URL + prefsIndex, pnLoginUrlString);
		}
	}

	public String getPnLoginUrlString() {
		if (pnLoginUrlString != null) {
			return pnLoginUrlString.toString();
		} else {
			return "http://example.com/user.php?uname=$USERNAME$&pass=$PASSWORD$&module=NS-User&op=login";
		}
	}

	/* PHPNuke Gallery URL */

	public void setPhpnGalleryUrlString(String urlString) {
		if (urlString == null) {
			phpnGalleryUrlString = null;
			return;
		}

		phpnGalleryUrlString = reformatUrlString(urlString, false);

		if (!blockWrites && phpnGalleryUrlString != null) {
			GalleryRemote._().properties.setProperty(PHPN_GALLERY_URL + prefsIndex, phpnGalleryUrlString);
		}
	}

	public String getPhpnGalleryUrlString() {
		if (phpnGalleryUrlString != null) {
			return phpnGalleryUrlString.toString();
		} else {
			return "http://example.com/modules.php?name=gallery&include=$GALLERYFILE$";
		}
	}

	/* PHPNuke Login URL */

	public void setPhpnLoginUrlString(String urlString) {
		if (urlString == null) {
			phpnLoginUrlString = null;
			return;
		}

		phpnLoginUrlString = reformatUrlString(urlString, false);

		if (!blockWrites && phpnLoginUrlString != null) {
			GalleryRemote._().properties.setProperty(PHPN_LOGIN_URL + prefsIndex, phpnLoginUrlString);
		}
	}

	public String getPhpnLoginUrlString() {
		if (phpnLoginUrlString != null) {
			return phpnLoginUrlString.toString();
		} else {
			return "http://example.com/modules.php?name=Your_Account&op=login&username=$USERNAME$&user_password=$PASSWORD$";
		}
	}

	/* GeekLog Gallery URL */

	public void setGlGalleryUrlString(String urlString) {
		if (urlString == null) {
			glGalleryUrlString = null;
			return;
		}

		glGalleryUrlString = reformatUrlString(urlString, false);

		if (!blockWrites && glGalleryUrlString != null) {
			GalleryRemote._().properties.setProperty(GL_GALLERY_URL + prefsIndex, glGalleryUrlString);
		}
	}

	public String getGlGalleryUrlString() {
		if (glGalleryUrlString != null) {
			return glGalleryUrlString.toString();
		} else {
			return "http://example.com/path/to/gallery/$GALLERYFILE$";
		}
	}

	/* PostNuke Login URL */

	public void setGlLoginUrlString(String urlString) {
		if (urlString == null) {
			glLoginUrlString = null;
			return;
		}

		glLoginUrlString = reformatUrlString(urlString, false);

		if (!blockWrites && glLoginUrlString != null) {
			GalleryRemote._().properties.setProperty(GL_LOGIN_URL + prefsIndex, glLoginUrlString);
		}
	}

	public String getGlLoginUrlString() {
		if (glLoginUrlString != null) {
			return glLoginUrlString.toString();
		} else {
			return "http://example.com/path/to/geeklog/public_html/users.php?loginname=$USERNAME$&passwd=$PASSWORD$";
		}
	}

	/* Applet URL */

	public void setApUrlString(String urlString) {
		stUrlString = urlString;
	}

	/* Desired User-Agent */

	public void setUserAgent(String userAgent) {
	        this.userAgent = userAgent;
	}

	public String getUserAgent() {
	        return userAgent;
	}

	/* Generic */

	public URL getLoginUrl(String galleryFile) {
		try {
			switch (type) {
				case TYPE_STANDALONE:
					return new URL(stUrlString + galleryFile);

				case TYPE_POSTNUKE:
					return new URL(replace(pnLoginUrlString, galleryFile));

				case TYPE_PHPNUKE:
					return new URL(replace(phpnLoginUrlString, galleryFile));

				case TYPE_GEEKLOG:
					return new URL(replace(glLoginUrlString, galleryFile));

				case TYPE_APPLET:
					return new URL(stUrlString);

				default:
					throw new RuntimeException("Unknown type: " + type);
			}
		} catch (MalformedURLException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Malformed URL.");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			JOptionPane.showMessageDialog((JFrame) su, "Malformed URL (" + e.getMessage() + ")",
					"Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public URL getGalleryUrl(String galleryFile) {
		try {
			switch (type) {
				case TYPE_STANDALONE:
					return new URL(stUrlString + galleryFile);

				case TYPE_POSTNUKE:
					return new URL(replace(pnGalleryUrlString, galleryFile));

				case TYPE_PHPNUKE:
					return new URL(replace(phpnGalleryUrlString, galleryFile));

				case TYPE_GEEKLOG:
					return new URL(replace(glGalleryUrlString, galleryFile));

				case TYPE_APPLET:
					return new URL(stUrlString);

				default:
					throw new RuntimeException("Unknown type: " + type);
			}
		} catch (MalformedURLException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Malformed URL.");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			JOptionPane.showMessageDialog((Component) su, "Malformed URL (" + e.getMessage() + ")",
					"Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	String replace(String urlString, String galleryFile) {
		StringBuffer sb = new StringBuffer(urlString);

		replace(sb, "$USERNAME$", username);
		replace(sb, "$PASSWORD$", password);
		replace(sb, "$GALLERYFILE$", galleryFile);

		return sb.toString();
	}

	boolean replace(StringBuffer sb, String token, String value) {
		int n = sb.indexOf(token);

		if (n != -1) {
			sb.replace(n, n + token.length(), value);
			return true;
		}

		return false;
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
					GalleryRemote._().properties.remove(PASSWORD + prefsIndex);
				}
			}
		}
	}

	public void setType(int type) {
		this.type = type;

		if (!blockWrites) {
			GalleryRemote._().properties.setProperty(TYPE + prefsIndex, types[type]);
		}
	}

	public void setAlias(String alias) {
		this.alias = alias;

		if (!blockWrites) {
			GalleryRemote._().properties.setProperty(ALIAS + prefsIndex, alias);
		}
	}

	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}


	public int getType() {
		return type;
	}

	public String getAlias() {
		return alias;
	}

	public static Gallery readFromProperties(GalleryProperties p, int prefsIndex, StatusUpdate su) {
		return readFromProperties(p, prefsIndex, su, true);
	}

	public static Gallery readFromProperties(GalleryProperties p, int prefsIndex, StatusUpdate su, boolean mustHaveUsername) {
		String url = p.getProperty(URL + prefsIndex);
		String username = p.getProperty(USERNAME + prefsIndex);

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
		g.setStUrlString(url);

		g.setPnLoginUrlString(p.getProperty(PN_LOGIN_URL + prefsIndex));
		g.setPnGalleryUrlString(p.getProperty(PN_GALLERY_URL + prefsIndex));

		g.setPhpnLoginUrlString(p.getProperty(PHPN_LOGIN_URL + prefsIndex));
		g.setPhpnGalleryUrlString(p.getProperty(PHPN_GALLERY_URL + prefsIndex));

		g.setGlLoginUrlString(p.getProperty(GL_LOGIN_URL + prefsIndex));
		g.setGlGalleryUrlString(p.getProperty(GL_GALLERY_URL + prefsIndex));

		String typeS = p.getProperty(TYPE + prefsIndex);
		if (typeS != null) {
			int type = Arrays.asList(types).indexOf(typeS);
			if (type != -1) {
				g.setType(type);
			}
		}
		g.setAlias(p.getProperty(ALIAS + prefsIndex));

		g.setPrefsIndex(prefsIndex);

		g.setBlockWrites(false);
		return g;
	}

	public void writeToProperties(PropertiesFile p) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Wrote to properties: " + toString());

		p.setProperty(URL + prefsIndex, stUrlString);
		p.setProperty(USERNAME + prefsIndex, username);
		if (getPassword() != null && p.getBooleanProperty(SAVE_PASSWORDS)) {
			p.setBase64Property(PASSWORD + prefsIndex, password);
		} else {
			p.remove(PASSWORD + prefsIndex);
		}
		p.setProperty(TYPE + prefsIndex, types[type]);
		if (getAlias() != null && getAlias().length() > 0) {
			p.setProperty(ALIAS + prefsIndex, getAlias());
		} else {
			p.setProperty(ALIAS + prefsIndex, null);
		}

		if (pnLoginUrlString != null) {
			p.setProperty(PN_LOGIN_URL + prefsIndex, pnLoginUrlString);
		}
		if (pnGalleryUrlString != null) {
			p.setProperty(PN_GALLERY_URL + prefsIndex, pnGalleryUrlString);
		}

		if (phpnLoginUrlString != null) {
			p.setProperty(PHPN_LOGIN_URL + prefsIndex, phpnLoginUrlString);
		}
		if (phpnGalleryUrlString != null) {
			p.setProperty(PHPN_GALLERY_URL + prefsIndex, phpnGalleryUrlString);
		}

		if (glLoginUrlString != null) {
			p.setProperty(GL_LOGIN_URL + prefsIndex, glLoginUrlString);
		}
		if (glGalleryUrlString != null) {
			p.setProperty(GL_GALLERY_URL + prefsIndex, glGalleryUrlString);
		}
	}

	public static void removeFromProperties(PropertiesFile p, int n) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Removed from properties: " + n);

		p.remove(URL + n);
		p.remove(USERNAME + n);
		p.remove(PASSWORD + n);
		p.remove(TYPE + n);
		p.remove(PN_LOGIN_URL + n);
		p.remove(PN_GALLERY_URL + n);
		p.remove(PHPN_LOGIN_URL + n);
		p.remove(PHPN_GALLERY_URL + n);
		p.remove(ALIAS + n);
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

		switch (type) {
			case TYPE_STANDALONE:
				tmp = stUrlString;
				break;

			case TYPE_POSTNUKE:
				tmp = pnGalleryUrlString;
				break;

			case TYPE_PHPNUKE:
				tmp = phpnGalleryUrlString;
				break;

			case TYPE_GEEKLOG:
				tmp = glGalleryUrlString;
				break;

			case TYPE_APPLET:
				tmp = stUrlString;
				break;

			default:
				throw new RuntimeException("Unknown type: " + type);
		}

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
		if (comm == null && stUrlString != null) {
			URL url = getGalleryUrl("");
			if (url != null) {
				comm = GalleryComm.getCommInstance(su, url, this);

				if (comm == null) {
					Log.log(Log.LEVEL_ERROR, MODULE, "No protocol implementation found");
					su.error(GRI18n.getString(MODULE, "galleryNotFound", new Object[] {stUrlString}));
				}
			}
		}

		return comm;
	}

	public boolean hasComm() {
		return comm != null;
	}

	public Album getAlbumByName(String name) {
		ArrayList albumList = getFlatAlbumList();
		if (albumList == null || name == null) {
			return null;
		}

		Iterator it = albumList.iterator();
		while (it.hasNext()) {
			Album a = (Album) it.next();

			if (name.equals(a.getName())) {
				return a;
			}
		}

		return null;
	}

	public void setBlockWrites(boolean blockWrites) {
		this.blockWrites = blockWrites;
	}

	ArrayList flatAlbumList = null;
	public ArrayList getFlatAlbumList() {
		if (flatAlbumList == null) {
			if (getRoot() != null) {
				flatAlbumList = Collections.list(new TreeEnumeration((TreeNode) getRoot()));
			}

			// G2 root is a normal album, don't add a fake root...
			if (galleryVersion == 2 && flatAlbumList != null) {
				flatAlbumList.remove(getRoot());
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

	public int getGalleryVersion() {
		return galleryVersion;
	}

	public void setGalleryVersion(int galleryVersion) {
		this.galleryVersion = galleryVersion;
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
}
