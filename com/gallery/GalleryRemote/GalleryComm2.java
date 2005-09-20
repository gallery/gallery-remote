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

import HTTPClient.*;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.prefs.GalleryProperties;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.HTMLEscaper;
import com.gallery.GalleryRemote.util.UrlMessageDialog;
import com.gallery.GalleryRemote.util.NaturalOrderComparator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.SocketException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

/**
 * The GalleryComm2 class implements the client side of the Gallery remote
 * protocol <a href="http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/gallery/gallery/gallery_remote.php?rev=HEAD&content-type=text/vnd.viewcvs-text">
 * version 2</a>.
 * 
 * @author jackodog
 * @author paour
 * @author <a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
 */
public class GalleryComm2 extends GalleryComm implements GalleryComm2Consts,
		GalleryCommCapabilities, PreferenceNames {
	/* Implementation notes:  One GalleryComm2 instance is needed per Gallery
	* server (since the protocol only logs into each server once).  So the
	* constructor requires a Gallery instance and is immutable with respect
	* to it.
	*/


	/* -------------------------------------------------------------------------
	* CLASS CONSTANTS
	*/

	/** Module name for logging. */
	private static final String MODULE = "GalComm2";

	/** Remote scriptname that provides version 2 of the protocol on the server. */
	public static final String SCRIPT_NAME = "gallery_remote2.php";

	protected String scriptName = SCRIPT_NAME;


	/* -------------------------------------------------------------------------
	*	INSTANCE VARIABLES
	*/

	/** The gallery this GalleryComm2 instance is attached to. */
	protected final Gallery g;

	/**
	 * The minor revision of the server (2.x)
	 * Use this to decide whether some functionality
	 * should be disabled (because the server would not understand.
	 */
	protected int serverMinorVersion = 0;

	/** The capabilities for 2.1 */
	private static int[] capabilities1;
	private static int[] capabilities2;
	private static int[] capabilities5;
	private static int[] capabilities7;
	private static int[] capabilities9;
	private static int[] capabilities13;
	private static int[] capabilities14;
	private static int[] capabilities15;


	/* -------------------------------------------------------------------------
	* CONSTRUCTION
	*/

	/**
	 * Create an instance of GalleryComm2 by supplying an instance of Gallery.
	 */
	protected GalleryComm2(Gallery g) {
		if (g == null) {
			throw new IllegalArgumentException("Must supply a non-null gallery.");
		}

		this.g = g;

		/*	Initialize the capabilities array with what protocol 2.0 supports.
		*	Once we're logged in and we know what the minor revision of the
		*	protocol is, we'll be able to add more capabilities, such as
		*	CAPA_NEW_ALBUM (since 2.1) */
		capabilities = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								 CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO};
		capabilities1 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM};
		capabilities2 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE};
		capabilities5 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FORCE_FILENAME};
		capabilities7 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FORCE_FILENAME, CAPA_MOVE_ALBUM};
		capabilities9 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FORCE_FILENAME, CAPA_MOVE_ALBUM, CAPA_FETCH_ALBUM_IMAGES};
		capabilities13 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FORCE_FILENAME, CAPA_MOVE_ALBUM, CAPA_FETCH_ALBUM_IMAGES,
								  CAPA_FETCH_ALBUMS_TOO, CAPA_FETCH_NON_WRITEABLE_ALBUMS};
		capabilities14 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FORCE_FILENAME, CAPA_MOVE_ALBUM, CAPA_FETCH_ALBUM_IMAGES,
								  CAPA_FETCH_ALBUMS_TOO, CAPA_FETCH_NON_WRITEABLE_ALBUMS, CAPA_FETCH_HONORS_HIDDEN};
		capabilities15 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FORCE_FILENAME, CAPA_MOVE_ALBUM, CAPA_FETCH_ALBUM_IMAGES,
								  CAPA_FETCH_ALBUMS_TOO, CAPA_FETCH_NON_WRITEABLE_ALBUMS, CAPA_FETCH_HONORS_HIDDEN,
								  CAPA_IMAGE_MAX_SIZE};

		// the algorithm for search needs the ints to be sorted.
		Arrays.sort(capabilities);
		Arrays.sort(capabilities1);
		Arrays.sort(capabilities2);
		Arrays.sort(capabilities5);
		Arrays.sort(capabilities7);
		Arrays.sort(capabilities9);
		Arrays.sort(capabilities13);
		Arrays.sort(capabilities14);
		Arrays.sort(capabilities15);
	}


	/* -------------------------------------------------------------------------
	* PUBLIC INSTANCE METHODS
	*/

	/**
	 * Causes the GalleryComm2 instance to upload the pictures in the
	 * associated Gallery to the server.
	 *
	 * @param su an instance that implements the StatusUpdate interface.
	 */
	public void uploadFiles(StatusUpdate su, boolean async) {
		UploadTask uploadTask = new UploadTask(su);
		doTask(uploadTask, async);
	}

	/**
	 * Causes the GalleryComm2 instance to fetch the albums contained by
	 * associated Gallery from the server.
	 *
	 * @param su an instance that implements the StatusUpdate interface.
	 */
	public void fetchAlbums(StatusUpdate su, boolean async) {
		doTask(new AlbumListTask(su), async);
	}

	/**
	 * Causes the GalleryComm2 instance to fetch the album properties
	 * for the given Album.
	 *
	 * @param su an instance that implements the StatusUpdate interface.
	 */
	public void albumInfo(StatusUpdate su, Album a, boolean async) {
		doTask(new AlbumPropertiesTask(su, a), async);
	}

	/**
	 * Causes the GalleryComm instance to create a new album as a child of
	 * the specified album (or at the root if album is null)
	 */
	public String newAlbum(StatusUpdate su, Album parentAlbum,
						   String newAlbumName, String newAlbumTitle,
						   String newAlbumDesc, boolean async) {
		NewAlbumTask newAlbumTask = new NewAlbumTask(su, parentAlbum, newAlbumName,
				newAlbumTitle, newAlbumDesc);
		doTask(newAlbumTask, async);

		return newAlbumTask.getNewAlbumName();
	}

	public void fetchAlbumImages(StatusUpdate su, Album a, boolean recusive, boolean async, int maxPictures) {
		FetchAlbumImagesTask fetchAlbumImagesTask = new FetchAlbumImagesTask(su,
				a, recusive, maxPictures);
		doTask(fetchAlbumImagesTask, async);
	}

	public boolean moveAlbum(StatusUpdate su, Album a, Album newParent, boolean async) {
		MoveAlbumTask moveAlbumTask = new MoveAlbumTask(su, a, newParent);
		doTask(moveAlbumTask, async);

		return moveAlbumTask.getSuccess();
	}

	public void login(StatusUpdate su) {
		LoginTask loginTask = new LoginTask(su);
		doTask(loginTask, false);
	}

	/* -------------------------------------------------------------------------
	* UTILITY METHODS
	*/
	void doTask(GalleryTask task, boolean async) {
		if (async) {
			Thread t = new Thread(task);
			t.start();
		} else {
			task.run();
		}
	}

	void status(StatusUpdate su, int level, String message) {
		Log.log(Log.LEVEL_INFO, MODULE, message);
		su.updateProgressStatus(level, message);
	}

	void error(StatusUpdate su, String message) {
		status(su, StatusUpdate.LEVEL_GENERIC, message);
		su.error(message);
	}

	void trace(String message) {
		Log.log(Log.LEVEL_TRACE, MODULE, message);
	}


	/* -------------------------------------------------------------------------
	* INNER CLASSES
	*/

	/**
	 * This class serves as the base class for each GalleryComm2 task.
	 */
	abstract class GalleryTask implements Runnable {
		StatusUpdate su;
		HTTPConnection mConnection;
		boolean interrupt = false;
		boolean terminated = false;
		Thread thread = null;

		public GalleryTask(StatusUpdate su) {
			if (su == null) {
				this.su = new StatusUpdateAdapter() {
				};
			} else {
				this.su = su;
			}
		}

		public void run() {
			thread = Thread.currentThread();
			su.setInProgress(true);
			if (!isLoggedIn) {
				if (!login()) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Failed to log in to " + g.toString());
					su.setInProgress(false);
					return;
				}

				isLoggedIn = true;
			} else {
				Log.log(Log.LEVEL_TRACE, MODULE, "Still logged in to " + g.toString());
			}

			runTask();

			cleanUp();
		}

		public void interrupt() {
			thread.interrupt();
			interrupt = true;
		}

		public void cleanUp() {
			su.setInProgress(false);
			terminated = true;
		}

		abstract void runTask();

		private boolean login() {
			Object[] params = {g.toString()};
			status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "logIn", params));

			if (g.getType() != Gallery.TYPE_STANDALONE && g.getType() != Gallery.TYPE_APPLET) {
				try {
					requestResponse(null, null, g.getLoginUrl(scriptName), false, su, this);
				} catch (IOException ioe) {
					Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
					Object[] params2 = {ioe.toString()};
					error(su, GRI18n.getString(MODULE, "error", params2));
				} catch (ModuleException me) {
					Log.logException(Log.LEVEL_ERROR, MODULE, me);
					Object[] params2 = {me.getMessage()};
					error(su, GRI18n.getString(MODULE, "errReq", params2));
				}
			}

			// setup protocol parameters
			String username = g.getUsername();
			String password = g.getPassword();

			if ((username == null || username.length() == 0)
					&& ! g.cookieLogin) {
				username = (String) JOptionPane.showInputDialog(
						GalleryRemote._().getMainFrame(),
						GRI18n.getString(MODULE, "usernameLbl"),
						GRI18n.getString(MODULE, "username"),
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						null);

				if (username != null) {
					g.setUsername(username);
				}
			}

			if (username != null && (password == null || password.length() == 0)) {
				password = (String) JOptionPane.showInputDialog(
						GalleryRemote._().getMainFrame(),
						GRI18n.getString(MODULE, "passwdLbl"),
						GRI18n.getString(MODULE, "passwd"),
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						null);

				g.setPassword(password);
			}

			NVPair form_data[] = {
				new NVPair("cmd", "login"),
				new NVPair("protocol_version", PROTOCOL_VERSION),
				null,
				null
			};

			if (username != null) {
				form_data[2] = new NVPair("uname", username);
				Log.log(Log.LEVEL_TRACE, MODULE, "login parameters: " + Arrays.asList(form_data));
				form_data[3] = new NVPair("password", password);
			} else {
				Log.log(Log.LEVEL_TRACE, MODULE, "login parameters: " + Arrays.asList(form_data));
			}

			form_data = fudgeFormParameters(form_data);

			// make the request
			try {
				triedLogin = true;

				// load and validate the response
				Properties p = requestResponse(form_data, null, g.getGalleryUrl(scriptName), true, su, this, true);
				if (GR_STAT_SUCCESS.equals(p.getProperty("status"))
						|| GR_STAT_LOGIN_MISSING.equals(p.getProperty("status"))) {
					status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "loggedIn"));
					try {
						String serverVersion = p.getProperty("server_version");
						int i = serverVersion.indexOf(".");
						serverMinorVersion = Integer.parseInt(serverVersion.substring(i + 1));

						Log.log(Log.LEVEL_TRACE, MODULE, "Server minor version: " + serverMinorVersion);

						handleCapabilities();
					} catch (Exception e) {
						Log.log(Log.LEVEL_ERROR, MODULE, "Malformed server_version: " + p.getProperty("server_version"));
						Log.logException(Log.LEVEL_ERROR, MODULE, e);
					}
					return true;
				} else if (GR_STAT_PASSWD_WRONG.equals(p.getProperty("status"))) {
					error(su, GRI18n.getString(MODULE, "usrpwdErr"));
					return false;
				} else {
					Object[] params2 = {p.getProperty("status_text")};
					error(su, GRI18n.getString(MODULE, "loginErr", params2));
					return false;
				}
			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params2 = {gr2e.getMessage()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params2 = {me.getMessage()};
				error(su, GRI18n.getString(MODULE, "errReq", params2));
			}

			return false;
		}
	}

	class LoginTask extends GalleryTask {
		LoginTask(StatusUpdate su) {
			super(su);
		}

		void runTask() {}
	}

	/**
	 * An extension of GalleryTask to handle uploading photos.
	 */
	class UploadTask extends GalleryTask {
		UploadTask(StatusUpdate su) {
			super(su);
		}

		void runTask() {
			ArrayList pictures = g.getAllUploadablePictures();

			su.startProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, 0, pictures.size(), GRI18n.getString(MODULE, "upPic"), false);

			if (su instanceof UploadProgress) {
				((UploadProgress) su).setCancelListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						((UploadProgress) su).updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "upStop"));
						((UploadProgress) su).setUndetermined(StatusUpdate.LEVEL_UPLOAD_PROGRESS, true);
						interrupt();
						long startTime = System.currentTimeMillis();

						while (!terminated && System.currentTimeMillis() < startTime + 10000) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						}

						if (!terminated) {
							Log.log(Log.LEVEL_ERROR, "Thread would not terminate properly: killing it");
							thread.stop();

							// since we killed the thread, it's not going to clean up after itself
							cleanUp();
						}

						((UploadProgress) su).setVisible(false);
					}
				});
			}

			// upload each file, one at a time
			boolean allGood = true;
			int uploadedCount = 0;
			Iterator iter = pictures.iterator();
			while (iter.hasNext() /*&& allGood*/ && !interrupt) {
				Picture p = (Picture) iter.next();

				Object[] params = {p.toString(), new Integer((uploadedCount + 1)), new Integer(pictures.size())};
				su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "upStatus", params));

				allGood = uploadPicture(p, p);

				su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_PROGRESS, ++uploadedCount);

				if (allGood) {
					p.getParentAlbum().removePicture(p);
				}
			}

			if (allGood) {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "upComplete"));

				if (su instanceof UploadProgress) {
					if (((UploadProgress) su).isShutdown()) {
						GalleryRemote._().getCore().shutdown(true);
					}
				}

				GalleryRemote._().getCore().flushMemory();
			} else {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "upFailed"));
			}
		}

		boolean uploadPicture(Picture p, Picture picture) {
			try {
				boolean escapeCaptions = GalleryRemote._().properties.getBooleanProperty(HTML_ESCAPE_CAPTIONS);
				boolean utf8 = !escapeCaptions && p.getParentAlbum().getGallery().galleryVersion == 2;

				if (utf8) {
					Log.log(Log.LEVEL_INFO, MODULE, "Will upload using UTF-8 for text data");
				}

				status(su, StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upPrep"));

				// can't set null as an NVPair value
				String caption = p.getCaption();
				caption = (caption == null) ? "" : caption;

				if (escapeCaptions) {
					caption = HTMLEscaper.escape(caption);
				}

				// setup the protocol parameters
				NVPair[] opts = {
					new NVPair("cmd", "add-item"),
					new NVPair("protocol_version", PROTOCOL_VERSION),
					new NVPair("set_albumName", p.getParentAlbum().getName()),
					new NVPair("caption", caption, utf8?"UTF-8":null),
					new NVPair("force_filename", p.getSource().getName()),
					null
				};

				// set auto-rotate only if we do the rotation in GR, otherwise we'd be overriding the server setting
				if (p.getAngle() != 0) {
					opts[5] = new NVPair("auto_rotate", "no");
				}

				// set up extra fields
				if (p.getExtraFieldsMap() != null && p.getExtraFieldsMap().size() > 0) {
					ArrayList optsList = new ArrayList(Arrays.asList(opts));

					Iterator it = p.getExtraFieldsMap().keySet().iterator();
					while (it.hasNext()) {
						String name = (String) it.next();
						String value = p.getExtraField(name);

						if (value != null) {
							if (escapeCaptions) {
								value = HTMLEscaper.escape(value);
							}

							optsList.add(new NVPair("extrafield." + name, value, utf8?"UTF-8":null));
						}
					}

					opts = (NVPair[]) optsList.toArray(opts);
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "add-item parameters: " + Arrays.asList(opts));

				// setup the multipart form data
				NVPair[] afile = {new NVPair("userfile", p.getUploadSource().getAbsolutePath())};
				NVPair[] hdrs = new NVPair[1];
				byte[] data = Codecs.mpFormDataEncode(fudgeFormParameters(opts), fudgeParameters(afile), hdrs);

				// load and validate the response
				Properties props = requestResponse(hdrs, data, g.getGalleryUrl(scriptName), true, su, this);
				if (props.getProperty("status").equals(GR_STAT_SUCCESS)) {
					status(su, StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upSucc"));
					String newItemName = props.getProperty("item_name");
					if (newItemName != null) {
						su.doneUploading(newItemName, picture);
					}
					return true;
				} else {
					Object[] params = {props.getProperty("status_text")};
					error(su, GRI18n.getString(MODULE, "upErr", params));
					return false;
				}
			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params = {gr2e.getMessage()};
				error(su, p.toString() + ": " + GRI18n.getString(MODULE, "error", params));
			} catch (SocketException swe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, swe);
				Object[] params = {swe.toString()};
				error(su, p.toString() + ": " + GRI18n.getString(MODULE, "confErr", params));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params = {ioe.toString()};
				error(su, p.toString() + ": " + GRI18n.getString(MODULE, "error", params));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params = {me.getMessage()};
				error(su, p.toString() + ": " + GRI18n.getString(MODULE, "errReq", params));
			}

			return false;
		}
	}

	/**
	 * An extension of GalleryTask to handle fetching albums.
	 */
	class AlbumListTask extends GalleryTask {

		AlbumListTask(StatusUpdate su) {
			super(su);
		}

		void runTask() {
			su.startProgress(StatusUpdate.LEVEL_BACKGROUND, 0, 10, GRI18n.getString(MODULE, "albmFtchng", new Object[] {g.toString()}), true);

			try {
				long startTime = System.currentTimeMillis();

				if (serverMinorVersion < 2) {
					list20();
				} else {
					list22();
				}

				// tell the tree to reload
				g.reload();

				Log.log(Log.LEVEL_INFO, MODULE, "execution time for AlbumList: " + (System.currentTimeMillis() - startTime));
			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params2 = {gr2e.getMessage()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params2 = {me.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			}

			su.stopProgress(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "fetchComplete"));
		}

		private void list20() throws IOException, ModuleException {
			// setup the protocol parameters
			NVPair form_data[] = {
				new NVPair("cmd", "fetch-albums"),
				new NVPair("protocol_version", PROTOCOL_VERSION)
			};
			Log.log(Log.LEVEL_TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));

			form_data = fudgeFormParameters(form_data);

			// load and validate the response
			Properties p = requestResponse(form_data, su, this);
			if (p.getProperty("status").equals(GR_STAT_SUCCESS)) {
				ArrayList mAlbumList = new ArrayList();

				// parse and store the data
				int albumCount = Integer.parseInt(p.getProperty("album_count"));

				HashMap ref2parKey = new HashMap();
				HashMap ref2album = new HashMap();

				Album rootAlbum = g.createRootAlbum();

				for (int i = 1; i < albumCount + 1; i++) {
					Album a = new Album(g);
					a.setSuppressEvents(true);

					String nameKey = "album.name." + i;
					String titleKey = "album.title." + i;
					String parentKey = "album.parent." + i;
					String permsAddKey = "album.perms.add." + i;
					String permsWriteKey = "album.perms.write." + i;
					String permsDelItemKey = "album.perms.del_item." + i;
					String permsDelAlbKey = "album.perms.del_alb." + i;
					String permsCreateSubKey = "album.perms.create_sub." + i;
					String infoExtraFieldsKey = "album.info.extrafields." + i;

					a.setCanAdd(isTrue(p.getProperty(permsAddKey)));
					a.setCanWrite(isTrue(p.getProperty(permsWriteKey)));
					a.setCanDeleteFrom(isTrue(p.getProperty(permsDelItemKey)));
					a.setCanDeleteThisAlbum(isTrue(p.getProperty(permsDelAlbKey)));
					a.setCanCreateSubAlbum(isTrue(p.getProperty(permsCreateSubKey)));
					a.setExtraFieldsString(HTMLEscaper.unescape(p.getProperty(infoExtraFieldsKey)));

					a.setName(p.getProperty(nameKey));
					a.setTitle(HTMLEscaper.unescape(p.getProperty(titleKey)));

					a.setSuppressEvents(false);
					mAlbumList.add(a);

					// map album ref nums to albums
					ref2album.put("" + i, a);

					// map album refs to parent refs
					String parentRefS = p.getProperty(parentKey);
					int parentRef = Integer.parseInt(parentRefS);
					if (parentRef != 0) {
						ref2parKey.put("" + i, parentRefS);
					} else {
						rootAlbum.add(a);
					}
				}

				// link albums to parents
				for (int i = 1; i < albumCount + 1; i++) {
					String parentKey = (String) ref2parKey.get("" + i);
					if (parentKey != null) {
						Album a = (Album) ref2album.get("" + i);
						if (a != null) {
							Album pa = (Album) ref2album.get(parentKey);
							pa.add(a);
						}
					}
				}

				status(su, StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "ftchdAlbms"));

				//g.setAlbumList(mAlbumList);
			} else {
				Object[] params = {p.getProperty("status_text")};
				error(su, GRI18n.getString(MODULE, "error", params));
			}
		}

		private void list22() throws IOException, ModuleException {
			// setup the protocol parameters
			NVPair form_data[] = {
				new NVPair("cmd", "fetch-albums-prune"),
				new NVPair("protocol_version", PROTOCOL_VERSION)
			};
			Log.log(Log.LEVEL_TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));

			form_data = fudgeFormParameters(form_data);

			// load and validate the response
			GalleryProperties p = requestResponse(form_data, su, this);
			if (p.getProperty("status").equals(GR_STAT_SUCCESS)) {
				ArrayList albums = new ArrayList();

				// parse and store the data
				int albumCount = Integer.parseInt(p.getProperty("album_count"));
//	System.err.println( "### albumCount = " + albumCount );
				HashMap name2parentName = new HashMap();
				HashMap name2album = new HashMap();

				Album rootAlbum = g.createRootAlbum();

				for (int i = 1; i < albumCount + 1; i++) {
					Album a = new Album(g);
					a.setSuppressEvents(true);

					String nameKey = "album.name." + i;
					String titleKey = "album.title." + i;
					String parentKey = "album.parent." + i;
					String permsAddKey = "album.perms.add." + i;
					String permsWriteKey = "album.perms.write." + i;
					String permsDelItemKey = "album.perms.del_item." + i;
					String permsDelAlbKey = "album.perms.del_alb." + i;
					String permsCreateSubKey = "album.perms.create_sub." + i;
					String infoExtraFieldKey = "album.info.extrafields." + i;

					a.setCanAdd(isTrue(p.getProperty(permsAddKey)));
					a.setCanWrite(isTrue(p.getProperty(permsWriteKey)));
					a.setCanDeleteFrom(isTrue(p.getProperty(permsDelItemKey)));
					a.setCanDeleteThisAlbum(isTrue(p.getProperty(permsDelAlbKey)));
					a.setCanCreateSubAlbum(isTrue(p.getProperty(permsCreateSubKey)));

					String name = p.getProperty(nameKey);
					a.setName(name);
					a.setTitle(HTMLEscaper.unescape(p.getProperty(titleKey)));
					a.setExtraFieldsString(HTMLEscaper.unescape(p.getProperty(infoExtraFieldKey)));

					a.setSuppressEvents(false);

					albums.add(a);

					// map album names to albums
					name2album.put(name, a);

					// map album refs to parent refs
					String parentName = p.getProperty(parentKey);

					if (parentName.equals(name)) {
						Log.log(Log.LEVEL_ERROR, MODULE, "Gallery error: the album " + name +
								" is its own parent. You should delete it, the album database " +
								"is corrupted because of it.");

						parentName = null;
					}

					if (parentName != null && parentName.length() > 0 && !parentName.equals("0")) {
						name2parentName.put(name, parentName);
					} else {
						rootAlbum.add(a);
					}
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Created " + albums.size() + " albums");

				// link albums to parents
				Iterator it = name2parentName.keySet().iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					String parentName = (String) name2parentName.get(name);
					Album child = (Album) name2album.get(name);
					Album parent = (Album) name2album.get(parentName);

					if (child != null && parent != null) {
						parent.add(child);
					}
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Linked " + name2parentName.size() + " albums to their parents");

				// reorder
				Collections.sort(albums, new NaturalOrderComparator());
				Collections.reverse(albums);
				ArrayList orderedAlbums = new ArrayList();
				int depth = 0;
				while (!albums.isEmpty()) {
					it = albums.iterator();
					while (it.hasNext()) {
						Album a = (Album) it.next();

						try {
							if (a.getAlbumDepth() == depth) {
								it.remove();
								a.sortSubAlbums();

								Album parentAlbum = a.getParentAlbum();
								if (parentAlbum == null) {
									orderedAlbums.add(0, a);
								} else {
									int i = orderedAlbums.indexOf(parentAlbum);
									orderedAlbums.add(i + 1, a);
								}
							}
						} catch (IllegalArgumentException e) {
							it.remove();
							Log.log(Log.LEVEL_TRACE, MODULE, "Gallery server album list is corrupted: " +
									"album " + a.getName() + " has a bad containment hierarchy.");

							/*JOptionPane.showMessageDialog(GalleryRemote.getInstance().mainFrame,
									GRI18n.getString(MODULE, "fixCorrupted", new String[] {
										a.getTitle(),
										a.getGallery().getGalleryUrl(a.getName()).toString()}),
									GRI18n.getString(MODULE, "fixCorruptedTitle"),
									JOptionPane.ERROR_MESSAGE);*/

							if (! GalleryRemote._().properties.getBooleanProperty(SUPPRESS_WARNING_CORRUPTED)) {
								UrlMessageDialog md = new UrlMessageDialog(
										GRI18n.getString(MODULE, "fixCorrupted", new String[] {
											a.getTitle(),
											a.getGallery().getGalleryUrl(a.getName()).toString()}),
										a.getGallery().getGalleryUrl(GRI18n.getString(MODULE, "fixCorruptedUrl", new String[] {
											a.getName()} )).toString(),
										null);

								if (md.dontShow()) {
									GalleryRemote._().properties.setBooleanProperty(SUPPRESS_WARNING_CORRUPTED, true);
								}
							}

							// This doesn't work: there's a problem with the connection (maybe not re-entrant...)
							//if (answer == JOptionPane.YES_OPTION) {
								//a.moveAlbumTo(su, null);
								//moveAlbum(su, a, null, true);
							//}
						}
					}

					depth++;
				}

				rootAlbum.setCanCreateSubAlbum(p.getBooleanProperty("can_create_root", false));

				Log.log(Log.LEVEL_TRACE, MODULE, "Ordered " + orderedAlbums.size() + " albums");

				status(su, StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "ftchdAlbms"));

				//g.setAlbumList(orderedAlbums);
			} else {
				Object[] params = {p.getProperty("status_text")};
				error(su, GRI18n.getString(MODULE, "error", params));
			}
		}
	}

	/**
	 * An extension of GalleryTask to handle getting album information.
	 */
	class AlbumPropertiesTask extends GalleryTask {
		Album a;

		AlbumPropertiesTask(StatusUpdate su, Album a) {
			super(su);
			this.a = a;
		}

		void runTask() {
			status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "getAlbmInfo", new String[] { a.toString() }));

			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "album-properties"),
					new NVPair("protocol_version", PROTOCOL_VERSION),
					new NVPair("set_albumName", a.getName())
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "album-info parameters: " + Arrays.asList(form_data));

				form_data =  fudgeFormParameters(form_data);

				// load and validate the response
				GalleryProperties p = requestResponse(form_data, su, this);
				if (p.getProperty("status").equals(GR_STAT_SUCCESS)) {
					// parse and store the data
					int autoResize = p.getIntProperty("auto_resize");
					int maxSize = p.getIntProperty("max_size", 0);

					// use larger of intermediate and max size
					a.setServerAutoResize(autoResize>maxSize?autoResize:maxSize);

					status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "ftchdAlbmProp"));

				} else {
					error(su, "Error: " + p.getProperty("status_text"));
				}

			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params2 = {gr2e.getMessage()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params2 = {me.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			}
		}
	}

	/**
	 * An extension of GalleryTask to handle creating a new album.
	 */
	class NewAlbumTask extends GalleryTask {
		Album parentAlbum;
		String albumName;
		String albumTitle;
		String albumDesc;
		private String newAlbumName;

		NewAlbumTask(StatusUpdate su, Album parentAlbum, String albumName,
					 String albumTitle, String albumDesc) {
			super(su);
			this.parentAlbum = parentAlbum;
			this.albumName = albumName;
			this.albumTitle = albumTitle;
			this.albumDesc = albumDesc;
		}

		void runTask() {
			status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "newAlbm", new Object[] { albumName, g.toString() }));

			boolean escapeCaptions = GalleryRemote._().properties.getBooleanProperty(HTML_ESCAPE_CAPTIONS);
			boolean utf8 = !escapeCaptions && parentAlbum.getGallery().galleryVersion == 2;

			// if the parent is null (top-level album), set the album name to an illegal name so it's set to null
			// by Gallery. Using an empty string doesn't work, because then the HTTP parameter is not
			// parsed, and the session album is kept the same as before (from the cookie).
			String parentAlbumName = (parentAlbum == null) ? "hack_null_albumName" : parentAlbum.getName();

			if (escapeCaptions) {
				albumTitle = HTMLEscaper.escape(albumTitle);
				albumDesc = HTMLEscaper.escape(albumDesc);
			}

			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "new-album"),
					new NVPair("protocol_version", PROTOCOL_VERSION),
					new NVPair("set_albumName", parentAlbumName),
					new NVPair("newAlbumName", albumName),
					new NVPair("newAlbumTitle", albumTitle, utf8?"UTF-8":null),
					new NVPair("newAlbumDesc", albumDesc, utf8?"UTF-8":null)
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "new-album parameters: " + Arrays.asList(form_data));

				form_data = fudgeFormParameters(form_data);

				// load and validate the response
				Properties p = requestResponse(form_data, su, this);
				if (p.getProperty("status").equals(GR_STAT_SUCCESS)) {
					status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "crateAlbmOk"));
					newAlbumName = p.getProperty("album_name");
				} else {
					Object[] params2 = {p.getProperty("status_text")};
					error(su, GRI18n.getString(MODULE, "error", params2));
				}

			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params2 = {gr2e.getMessage()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params2 = {me.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			}
		}

		public String getNewAlbumName() {
			return newAlbumName;
		}
	}

	/**
	 * An extension of GalleryTask to handle getting album information.
	 */
	class FetchAlbumImagesTask extends GalleryTask {
		Album a;
		boolean recursive = false;
		int maxPictures = 0;

		FetchAlbumImagesTask(StatusUpdate su, Album a, boolean recursive, int maxPictures) {
			super(su);

			this.a = a;
			this.recursive = recursive;
			this.maxPictures = maxPictures;
		}

		void runTask() {
			su.startProgress(StatusUpdate.LEVEL_GENERIC, 0, 10, GRI18n.getString(MODULE, "fetchAlbImages",
							new String[]{a.getName()}), true);

			try {
				ArrayList newPictures = new ArrayList();
				fetch(a, a.getName(), newPictures);

				a.setHasFetchedImages(true);
				a.addPictures(newPictures);
				GalleryRemote._().getCore().preloadThumbnails(newPictures.iterator());

				su.stopProgress(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "fetchAlbImagesDone",
								new String[]{"" + newPictures.size()}));
			} catch (GR2Exception e) {
				error(su, GRI18n.getString(MODULE, "error", new String[] {e.getMessage()}));
				su.stopProgress(StatusUpdate.LEVEL_GENERIC, e.getMessage());
			}

		}

		private void fetch(Album a, String albumName, ArrayList newPictures)
				throws GR2Exception {
			su.updateProgressStatus(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "fetchAlbImages",
							new String[]{albumName}));

			try {
				// setup the protocol parameters
				NVPair[] form_data = new NVPair[] {
					new NVPair("cmd", "fetch-album-images"),
					new NVPair("protocol_version", PROTOCOL_VERSION),
					new NVPair("set_albumName", albumName),
					new NVPair("albums_too", recursive?"yes":"no")
				};

				Log.log(Log.LEVEL_TRACE, MODULE, "fetch-album-images parameters: " +
						Arrays.asList(form_data));

				form_data = fudgeFormParameters(form_data);

				// load and validate the response
				GalleryProperties p = requestResponse(form_data, su, this);
				if (p.getProperty("status").equals(GR_STAT_SUCCESS)) {
					// parse and store the data
					int numImages = p.getIntProperty("image_count");
					String baseUrl = p.getProperty("baseurl");

					try {
						if (baseUrl == null) {
							Log.log(Log.LEVEL_TRACE, MODULE, "Gallery root, baseurl is null");
						} else {
							// verify that baseUrl is a valid URL (don't remove)
							URL tmpUrl = new URL(baseUrl);
						}
					} catch (MalformedURLException e) {
						Log.log(Log.LEVEL_TRACE, MODULE, "baseurl is relative, tacking on Gallery URL (only works for standalone)");
						URL tmpUrl = new URL(g.getStUrlString());
						baseUrl = new URL(tmpUrl.getProtocol(), tmpUrl.getHost(), tmpUrl.getPort(), baseUrl).toString();
					}

					int width;
					int height;
					ArrayList extraFields = a.getExtraFields();
					for (int i = 1; i <= numImages; i++) {
						if (maxPictures> 0 && newPictures.size() >= maxPictures) {
							Log.log(Log.LEVEL_TRACE, MODULE, "Fetched maximum of " + maxPictures +
									" pictures: stopping.");
							break;
						}

						String subAlbumName = p.getProperty("album.name." + i);
						boolean subAlbumHidden = p.getBooleanProperty("album.hidden." + i, false);

						if (subAlbumName != null) {
							if (!subAlbumHidden) {
								fetch(a, subAlbumName, newPictures);
							}
						} else {
							Picture picture = new Picture(g);
							picture.setOnline(true);

							String rawName = p.getProperty("image.name." + i);
							if (rawName != null) {
								picture.setUrlFull(new URL(baseUrl + rawName));
								width = p.getIntProperty("image.raw_width." + i, 0);
								height = p.getIntProperty("image.raw_height." + i, 0);
								picture.setSizeFull(new Dimension(width, height));
								picture.setFileSize(p.getIntProperty("image.raw_filesize." + i));

								picture.setUniqueId(a.getName() + '_' + rawName);
								picture.setItemId(rawName);
							}

							String forceExtension = p.getProperty("image.forceExtension." + i);
							if (forceExtension != null) {
								picture.setForceExtension(forceExtension);
							}

							String resizedName = p.getProperty("image.resizedName." + i);
							if (resizedName != null) {
								picture.setUrlResized(new URL(baseUrl + resizedName));
								width = p.getIntProperty("image.resized_width." + i);
								height = p.getIntProperty("image.resized_height." + i);
								picture.setSizeResized(new Dimension(width, height));
							}

							picture.setUrlThumbnail(new URL(baseUrl + p.getProperty("image.thumbName." + i)));
							width = p.getIntProperty("image.thumb_width." + i);
							height = p.getIntProperty("image.thumb_height." + i);
							picture.setSizeThumbnail(new Dimension(width, height));

							picture.setCaption(p.getProperty("image.caption." + i));

							String title = p.getProperty("image.title." + i);
							if (title != null) {
								picture.setName(title);
							}

							if (extraFields != null) {
								for (Iterator it = extraFields.iterator(); it.hasNext();) {
									String name = (String) it.next();
									String value = p.getProperty("image.extrafield." + name + "." + i);

									if (value != null) {
										picture.setExtraField(name, value);
									}
								}
							}

							picture.setHidden(p.getBooleanProperty("image.hidden." + i, false));

							picture.setAlbumOnServer(a);
							picture.setIndexOnServer(i - 1);

							if (!picture.isHidden() && !(rawName == null && resizedName == null)) {
								// don't add the picture if the current user can't get access to it
								newPictures.add(picture);
							}
						}
					}
				} else {
					throw new GR2Exception(p.getProperty("error"));
				}

			/*} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params2 = {gr2e.getMessage()};
				error(su, GRI18n.getString(MODULE, "error", params2));*/
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params2 = {me.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			}
		}
	}

	/**
	 * An extension of GalleryTask to handle moving an album.
	 */
	class MoveAlbumTask extends GalleryTask {
		Album a;
		Album newParent;
		boolean success = false;

		MoveAlbumTask(StatusUpdate su, Album a, Album newParent) {
			super(su);
			this.a = a;
			this.newParent = newParent;
		}

		void runTask() {
			String newParentName;
			String destAlbumName;

			if (newParent != null) {
				newParentName = destAlbumName = newParent.getName();
			} else {
				newParentName = GRI18n.getString(MODULE, "rootAlbum");
				destAlbumName = "0";
			}

			status(su, StatusUpdate.LEVEL_GENERIC,
					GRI18n.getString(MODULE, "moveAlbum",
							new String[]{a.getName(), newParentName}));

			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "move-album"),
					new NVPair("protocol_version", PROTOCOL_VERSION),
					new NVPair("set_albumName", a.getName()),
					new NVPair("set_destalbumName", destAlbumName)
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "move-album parameters: " +
						Arrays.asList(form_data));

				form_data = fudgeFormParameters(form_data);

				// load and validate the response
				GalleryProperties p = requestResponse(form_data, su, this);
				if (p.getProperty("status").equals(GR_STAT_SUCCESS)) {
					status(su, StatusUpdate.LEVEL_GENERIC,
							GRI18n.getString(MODULE, "moveAlbumDone"));

					success = true;
				} else {
					error(su, "Error: " + p.getProperty("status_text"));
				}

			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params2 = {gr2e.getMessage()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
				Object[] params2 = {me.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
			}
		}

		public boolean getSuccess() {
			return success;
		}
	}

	boolean isTrue(String s) {
		return s != null && s.equals("true");
	}

	/**
	 * POSTSs a request to the Gallery server with the given form data.
	 */
	GalleryProperties requestResponse(NVPair form_data[], StatusUpdate su, GalleryTask task) throws GR2Exception, ModuleException, IOException {
		return requestResponse(form_data, null, g.getGalleryUrl(scriptName), true, su, task);
	}

	GalleryProperties requestResponse(NVPair form_data[], URL galUrl, StatusUpdate su, GalleryTask task) throws GR2Exception, ModuleException, IOException {
		return requestResponse(form_data, null, galUrl, true, su, task);
	}

	GalleryProperties requestResponse(NVPair form_data[], byte[] data, URL galUrl, boolean checkResult, StatusUpdate su, GalleryTask task) throws GR2Exception, ModuleException, IOException {
		return requestResponse(form_data, data, galUrl, checkResult, su, task, false);
	}

	/**
	 * POSTSs a request to the Gallery server with the given form data.  If data is
	 * not null, a multipart MIME post is performed.
	 */
	GalleryProperties requestResponse(NVPair form_data[], byte[] data, URL galUrl, boolean checkResult, StatusUpdate su, GalleryComm2.GalleryTask task, boolean alreadyRetried) throws GR2Exception, ModuleException, IOException {
		// assemble the URL
		String urlPath = galUrl.getFile();
		Log.log(Log.LEVEL_TRACE, MODULE, "Connecting to: " + galUrl);
		Log.log(Log.LEVEL_TRACE, MODULE, "Path: " + urlPath);

		if (data != null) {
			su.startProgress(StatusUpdate.LEVEL_UPLOAD_ONE, 0, 0, GRI18n.getString(MODULE, "upStart"), false);
		}

		// create a connection
		HTTPConnection mConnection = new HTTPConnection(galUrl);
		String userAgent = g.getUserAgent();
		if (userAgent != null) {
		    mConnection.setDefaultHeaders(new NVPair[] { new NVPair("User-Agent", userAgent) });
		}

		// Markus Cozowicz (mc@austrian-mint.at) 2003/08/24
		HTTPResponse rsp = null;

		// post multipart if there is data
		if (data == null) {
			if (form_data == null) {
				rsp = mConnection.Get(urlPath);
			} else {
				rsp = mConnection.Post(urlPath, form_data);
			}
		} else {
			rsp = mConnection.Post(urlPath, data, form_data, new MyTransferListener(su));
		}

		// handle 30x redirects
		if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
			// retry, the library will have fixed the URL
			status(su, StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "redirect"));
			if (data == null) {
				if (form_data == null) {
					rsp = mConnection.Get(urlPath);
				} else {
					rsp = mConnection.Post(urlPath, form_data);
				}
			} else {
				rsp = mConnection.Post(urlPath, data, form_data, new MyTransferListener(su));
			}
		}

		// handle response
		if (rsp.getStatusCode() >= 300) {
			Object[] params = {new Integer(rsp.getStatusCode()), rsp.getReasonLine()};
			throw new GR2Exception(GRI18n.getString(MODULE, "httpPostErr", params));
		} else {
			// load response
			String response = new String(rsp.getData()).trim();
			Log.log(Log.LEVEL_TRACE, MODULE, response);

			if (checkResult) {
				// validate response
				int i = response.indexOf(PROTOCOL_MAGIC);

				if (i == -1) {
					if (alreadyRetried) {
						// failed one time too many
						Object[] params = {galUrl.toString()};
						throw new GR2Exception(GRI18n.getString(MODULE, "gllryNotFound", params));
					} else {
						// try again
						Log.log(Log.LEVEL_INFO, MODULE, "Request failed the first time: trying again...");
						return requestResponse(form_data, data, galUrl, checkResult, su, task, true);
					}
				} else if (i > 0) {
					response = response.substring(i);
					Log.log(Log.LEVEL_TRACE, MODULE, "Short response: " + response);
				}

				GalleryProperties p = new GalleryProperties();
				p.load(new StringBufferInputStream(response));

				// catch session expiration problems
				if (!alreadyRetried && !g.cookieLogin && g.getUsername() != null && g.getUsername().length() != 0
						&& p.getProperty("debug_user_already_logged_in") != null
						&& ! "1".equals(p.getProperty("debug_user_already_logged_in"))) {
					Log.log(Log.LEVEL_INFO, MODULE, "The session seems to have expired: trying to login and retry...");

					if (task.login()) {
						return requestResponse(form_data, data, galUrl, checkResult, su, task, true);
					} else {
						Log.log(Log.LEVEL_INFO, MODULE, "Login attempt unsuccessful");
					}
				}

				//mConnection.stop();

				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "addImgOk"));

				return p;
			} else {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "addImgErr"));

				return null;
			}
		}
	}

	public NVPair[] fudgeFormParameters(NVPair[] form_data) {
		return form_data;
	}

    public NVPair[] fudgeParameters(NVPair[] data) {
        return data;
    }

	void handleCapabilities() {
		if (serverMinorVersion >= 15) {
			capabilities = capabilities15;
		} else if (serverMinorVersion >= 14) {
			capabilities = capabilities14;
		} else if (serverMinorVersion >= 13) {
			capabilities = capabilities13;
		} else if (serverMinorVersion >= 9) {
			capabilities = capabilities9;
		} else if (serverMinorVersion >= 7) {
			capabilities = capabilities7;
		} else if (serverMinorVersion >= 5) {
			capabilities = capabilities5;
		} else if (serverMinorVersion >= 2) {
			capabilities = capabilities2;
		} else if (serverMinorVersion == 1) {
			capabilities = capabilities1;
		}
	}

	class MyTransferListener implements TransferListener {
		StatusUpdate su;
		java.text.DecimalFormat df = new java.text.DecimalFormat("##,##0");
		java.text.DecimalFormat ff = new java.text.DecimalFormat("##,##0.0");

		MyTransferListener(StatusUpdate su) {
			this.su = su;
		}

		public void dataTransferred(int transferred, int overall, double kbPerSecond) {
			Object[] params = {
				df.format(transferred / 1024),
				df.format(overall / 1024),
				ff.format(kbPerSecond / 1024.0)};
			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "trnsfrStat", params));
			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ONE, transferred);
		}

		public void transferStart(int overall) {
			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ONE, 0, overall);
		}

		public void transferEnd() {
			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upCompSrvrProc"));
			su.setUndetermined(StatusUpdate.LEVEL_UPLOAD_ONE, true);
		}
	};

	/* -------------------------------------------------------------------------
	* MAIN METHOD (test only)
	*/
	/*public static void main( String [] args ) {

	try {
	StatusUpdate su = new StatusUpdateAdapter(){};
	Gallery g = new Gallery();
	g.setStUrlString( "http://www.deathcult.com/gallery/" );
	g.setUsername( "ted" );
	g.setPassword( "1qwe2asd" );
	g.setStatusUpdate( su );
	GalleryComm2 gc = new GalleryComm2( g );
	gc.fetchAlbums( su, false );

	//try { Thread.sleep( 10000 ); } catch ( InterruptedException ie ) {}

	ArrayList albumList = g.getAlbumList();
	System.err.println( "albumList size = " + albumList.size() );
	for ( int i = 0; i < albumList.size(); i++ ) {
	Album a = (Album)albumList.get( i );
	a.fetchAlbumProperties( su );
	//try { Thread.sleep( 500 ); } catch ( InterruptedException ie ) {}
	}

	//try { Thread.sleep( 10000 ); } catch ( InterruptedException ie ) {}

	ArrayList albumList2 = g.getAlbumList();
	System.err.println( "albumList2 size = " + albumList2.size() );
	for ( int i = 0; i < albumList2.size(); i++ ) {
	Album a = (Album)albumList2.get( i );
	System.err.println( a.getName() + "   srv_rsz = " + a.getServerAutoResize() );
	}

	} catch ( MalformedURLException mue ) {

	}
	}*/
}
