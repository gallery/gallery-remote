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

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.net.SocketException;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import HTTPClient.*;

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.HTMLEscaper;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;

/**
 *	The GalleryComm2 class implements the client side of the Gallery remote
 *	protocol <a href="http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/gallery/gallery/gallery_remote.php?rev=HEAD&content-type=text/vnd.viewcvs-text">
 *  version 2</a>.
 *	
 *  @author jackodog
 *  @author paour
 *  @author <a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
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
	 
	/**
	 * Module name for logging.
	 */
	private static final String MODULE = "GalComm2";
    private static GRI18n grRes = GRI18n.getInstance();


	/* -------------------------------------------------------------------------
	 *	INSTANCE VARIABLES
	 */
	
	/**
	 *	The gallery this GalleryComm2 instance is attached to.
	 */
	protected final Gallery g;

	/**
	 *  The minor revision of the server (2.x)
	 *	Use this to decide whether some functionality
	 *	should be disabled (because the server would not understand.
	 */
	protected int serverMinorVersion = 0;
	
	/**
	 *	The capabilities for 2.1
	 */
	private static int[] capabilities1;
	private static int[] capabilities2;
	private static int[] capabilities5;

	
	/* -------------------------------------------------------------------------
	 * CONSTRUCTION
	 */ 
	
	/**
	 *	Create an instance of GalleryComm2 by supplying an instance of Gallery.
	 */
	protected GalleryComm2( Gallery g ) {
		if ( g == null ) {
			throw new IllegalArgumentException( "Must supply a non-null gallery." );
		}
		
		this.g = g;
		
		/*	Initialize the capabilities array with what protocol 2.0 supports.
		 *	Once we're logged in and we know what the minor revision of the
		 *	protocol is, we'll be able to add more capabilities, such as
		 *	CAPA_NEW_ALBUM (since 2.1) */
		capabilities = new int[] { CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
			CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO };
		capabilities1 = new int[] { CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
			CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM };
		capabilities2 = new int[] { CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
			CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE };
		capabilities5 = new int[] { CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
			CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
			CAPA_FORCE_FILENAME};
		Arrays.sort(capabilities);
	}
	
	
	/* -------------------------------------------------------------------------
	 * PUBLIC INSTANCE METHODS
	 */ 
	
	/**
	 *	Causes the GalleryComm2 instance to upload the pictures in the
	 *	associated Gallery to the server.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void uploadFiles( StatusUpdate su, boolean async ) {
		doTask( new UploadTask( su ), async );
	}
	
	/**
	 *	Causes the GalleryComm2 instance to fetch the albums contained by
	 *	associated Gallery from the server.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void fetchAlbums( StatusUpdate su, boolean async ) {
		doTask( new AlbumListTask( su ), async );
	}
	
	/**
	 *	Causes the GalleryComm2 instance to fetch the album properties
	 *	for the given Album.
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 */
	public void albumInfo( StatusUpdate su, Album a, boolean async ) {
		doTask( new AlbumPropertiesTask( su, a ), async );
	}
	
	/**
	 *	Causes the GalleryComm instance to create a new album as a child of
	 *	the specified album (or at the root if album is null)
	 *	
	 *	@param su an instance that implements the StatusUpdate interface.
	 *	@param a if null, create the album in the root of the gallery; otherwise
	 *				create as a child of the given album
	 */
	public String newAlbum( StatusUpdate su, Album parentAlbum,
			String newAlbumName, String newAlbumTitle,
			String newAlbumDesc, boolean async ) {
		NewAlbumTask newAlbumTask = new NewAlbumTask( su, parentAlbum, newAlbumName,
				newAlbumTitle, newAlbumDesc );
		doTask( newAlbumTask, async );

		return newAlbumTask.getNewAlbumName();
	}
		
	/* -------------------------------------------------------------------------
	 * UTILITY METHODS
	 */ 
	void doTask( GalleryTask task, boolean async ) {
		if ( async ) {
			Thread t = new Thread( task );
			t.start();
		} else {
			task.run();
		}
	}
	
	void status( StatusUpdate su, int level, String message) {
		Log.log(Log.LEVEL_INFO, MODULE, message);
		su.updateProgressStatus(level, message);
	}
	
	void error( StatusUpdate su, String message) {
		status(su, StatusUpdate.LEVEL_GENERIC, message);
		su.error( message );
	}
	
	void trace(String message) {
		Log.log(Log.LEVEL_TRACE, MODULE, message);
	}


	/* -------------------------------------------------------------------------
	 * INNER CLASSES
	 */
	
	/**
	 *	This class serves as the base class for each GalleryComm2 task.
	 */
	abstract class GalleryTask implements Runnable {

		StatusUpdate su;
		HTTPConnection mConnection;
		boolean interrupt = false;
		boolean terminated = false;
		Thread thread = null;

		public GalleryTask( StatusUpdate su ) {
			if ( su == null ) {
				this.su = new StatusUpdateAdapter(){};
			} else {
				this.su = su;	
			}
		}
		
		public void run() {
			thread = Thread.currentThread();
			su.setInProgress(true);
			if ( ! isLoggedIn ) {
				if ( !login() ) {
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
		
		/**
		 *	POSTSs a request to the Gallery server with the given form data.
		 */	
		Properties requestResponse( NVPair form_data[] ) throws GR2Exception, ModuleException, IOException {
			return requestResponse( form_data, null, g.getGalleryUrl(SCRIPT_NAME), true);
		}

		Properties requestResponse( NVPair form_data[], URL galUrl ) throws GR2Exception, ModuleException, IOException {
			return requestResponse( form_data, null, galUrl, true);
		}

		/**
		 *	POSTSs a request to the Gallery server with the given form data.  If data is
		 *	not null, a multipart MIME post is performed.
		 */	
		Properties requestResponse( NVPair form_data[], byte[] data, URL galUrl, boolean checkResult) throws GR2Exception, ModuleException, IOException {
			// assemble the URL
			String urlPath = galUrl.getFile();
			Log.log(Log.LEVEL_TRACE, MODULE, "Url: " + urlPath );

			if (data != null) {
				su.startProgress(StatusUpdate.LEVEL_UPLOAD_ONE, 0, 0, grRes.getString(MODULE, "upStart"), false);
			}

			// create a connection
			HTTPConnection mConnection = new HTTPConnection( galUrl );

			// Markus Cozowicz (mc@austrian-mint.at) 2003/08/24
			HTTPResponse rsp = null;

			// post multipart if there is data
			if ( data == null ) {
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
				status(su, StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "redirect"));
				if ( data == null ) {
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
			if (rsp.getStatusCode() >= 300)	{
                Object [] params = {new Integer(rsp.getStatusCode()), rsp.getReasonLine() };
				throw new GR2Exception( grRes.getString(MODULE, "httpPostErr", params));
			} else {
				// load response 
				String response = new String(rsp.getData()).trim();
				Log.log(Log.LEVEL_TRACE, MODULE, response);

				if (checkResult) {
					// validate response
					int i = response.indexOf(PROTOCOL_MAGIC);

					if ( i == -1 ) {
                        Object [] params = {galUrl.toString() };
						throw new GR2Exception(grRes.getString(MODULE, "gllryNotFound", params));
					} else if ( i > 0 ) {
						response = response.substring(i);
						Log.log(Log.LEVEL_TRACE, MODULE, "Short response: " + response);
					}

					Properties p = new Properties();
					p.load( new StringBufferInputStream( response ) );

					su.stopProgress(StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "addImgOk"));

					return p;
				} else {
					su.stopProgress(StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "addImgErr"));

					return null;
				}
			}
		}
		
		private boolean login() {
            Object [] params = {g.toString()};
			status(su, StatusUpdate.LEVEL_GENERIC, grRes.getString(MODULE, "logIn", params));

			if (g.getType() != Gallery.TYPE_STANDALONE) {
				try {
					requestResponse( null, null, g.getLoginUrl(SCRIPT_NAME), false );
				} catch (IOException ioe) {
					Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                    Object [] params2 = {ioe.toString() };
					error(su, grRes.getString(MODULE, "error", params2));
				} catch (ModuleException me) {
					Log.logException(Log.LEVEL_ERROR, MODULE, me);
                    Object [] params2 = {me.getMessage()};
					error(su, grRes.getString(MODULE, "errReq" , params2));
				}
			}

			// setup protocol parameters
			String username = g.getUsername();
			String password = g.getPassword();

			if (username == null || username.length() == 0) {
				username = (String)JOptionPane.showInputDialog(
                    (JFrame) su,
                    grRes.getString(MODULE, "usernameLbl"),
                    grRes.getString(MODULE, "username"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);

				g.setUsername(username);
			}

			if (password == null || password.length() == 0) {
				password = (String)JOptionPane.showInputDialog(
                    (JFrame) su,
                    grRes.getString(MODULE, "passwdLbl"),
                    grRes.getString(MODULE, "passwd"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);

				g.setPassword(password);
			}

			NVPair form_data[] = {
				new NVPair("cmd", "login"),
				new NVPair("protocol_version", PROTOCOL_VERSION),
				new NVPair("uname", username),
				new NVPair("password", password)
			};
			Log.log(Log.LEVEL_TRACE, MODULE, "login parameters: " + Arrays.asList(form_data));

			// make the request
			try	{
				// load and validate the response
				Properties p = requestResponse( form_data, g.getGalleryUrl(SCRIPT_NAME) );
				if ( GR_STAT_SUCCESS.equals( p.getProperty( "status" ) ) ) {
					status(su, StatusUpdate.LEVEL_GENERIC, grRes.getString(MODULE, "loggedIn"));
					try {
						String serverVersion = p.getProperty( "server_version" );
						int i = serverVersion.indexOf( "." );
						serverMinorVersion = Integer.parseInt( serverVersion.substring( i+1 ) );

						Log.log(Log.LEVEL_TRACE, MODULE, "Server minor version: " + serverMinorVersion);

						handleCapabilities();
					} catch (Exception e) {
						Log.log( Log.LEVEL_ERROR, MODULE, "Malformed server_version: " + p.getProperty( "server_version" ) );
						Log.logException(Log.LEVEL_ERROR, MODULE, e);
					}
					return true;
				} else if (GR_STAT_PASSWD_WRONG.equals(p.getProperty("status"))) {
					error(su, grRes.getString(MODULE, "usrpwdErr"));
					return false;
				} else {
                    Object [] params2 = {p.getProperty( "status_text" ) };
					error(su, grRes.getString(MODULE, "loginErr", params2));
					return false;
				}
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e );
                Object [] params2 = {gr2e.getMessage() };
				error(su, grRes.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params2 = {ioe.toString() };
				error(su, grRes.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params2 = {me.getMessage() };
				error(su, grRes.getString(MODULE, "errReq", params2));
			}

			return false;
		}

		private void handleCapabilities() {
			if (serverMinorVersion >= 5) {
				// we have more than the 2.2 capabilities, we have 2.5 capabilities.
				capabilities = capabilities5;
			} else if (serverMinorVersion >= 2) {
				// we have more than the 2.1 capabilities, we have 2.2 capabilities.
				capabilities = capabilities2;
			} else if (serverMinorVersion == 1) {
				// we have more than the 2.0 capabilities, we have 2.1 capabilities.
				capabilities = capabilities1;
			}
		}
	}
	
	/**
	 *	An extension of GalleryTask to handle uploading photos.
	 */
	class UploadTask extends GalleryTask {		
		UploadTask( StatusUpdate su ) {
			super(su);	
		}
		
		void runTask() {
			ArrayList pictures = g.getAllPictures();
			
			su.startProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, 0, pictures.size(), grRes.getString(MODULE, "upPic"), false);

			if (su instanceof UploadProgress) {
				((UploadProgress) su).setCancelListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						((UploadProgress) su).updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_PROGRESS, grRes.getString(MODULE, "upStop"));
						((UploadProgress) su).setUndetermined(StatusUpdate.LEVEL_UPLOAD_PROGRESS, true);
						interrupt();
						long startTime = System.currentTimeMillis();

						while (!terminated && System.currentTimeMillis() < startTime + 10000) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {	}
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
			while (iter.hasNext() && allGood && !interrupt) {
				Picture p = (Picture) iter.next();

                Object [] params = {p.toString(), new Integer((uploadedCount + 1)), new Integer(pictures.size()) };
				su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_PROGRESS, grRes.getString(MODULE, "upStatus", params));
				
				allGood = uploadPicture(p);
				
				su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_PROGRESS, ++uploadedCount);
				
				if (allGood) {
					p.getAlbum().removePicture(p);
				}
			}
			
			if (allGood) {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, grRes.getString(MODULE, "upComplete"));

				if (su instanceof UploadProgress) {
					if (((UploadProgress) su).isShutdown()) {
						GalleryRemote.getInstance().mainFrame.shutdown(true);
					}
				}
			} else {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, grRes.getString(MODULE, "upFailed"));
			}
		}
		
		boolean uploadPicture(Picture p) {
			try	{
				boolean escapeCaptions = GalleryRemote.getInstance().properties.getBooleanProperty(HTML_ESCAPE_CAPTIONS);

				status(su, StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "upPrep"));

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
					new NVPair("set_albumName", p.getAlbum().getName()),
					new NVPair("caption", caption ),
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

							optsList.add(new NVPair("extrafield_" + name, value));
						}
					}

					opts = (NVPair[]) optsList.toArray(opts);
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "add-item parameters: " + Arrays.asList(opts));
				
				// setup the multipart form data
				NVPair[] afile = { new NVPair("userfile", p.getUploadSource().getAbsolutePath()) };
				NVPair[] hdrs = new NVPair[1];
				byte[]   data = Codecs.mpFormDataEncode(opts, afile, hdrs);
				
				// load and validate the response
				Properties props = requestResponse( hdrs, data, g.getGalleryUrl(SCRIPT_NAME), true );
				if ( props.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
					status(su, StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "upSucc"));
					return true;
				} else {
                    Object [] params = {props.getProperty( "status_text" )};
					error(su, grRes.getString(MODULE, "upErr", params));
					return false;
				}
				
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e );
                Object [] params = {gr2e.getMessage()};
				error(su, grRes.getString(MODULE, "error", params));
			} catch (SocketException swe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, swe);
                Object [] params = {swe.toString()};
				error(su, grRes.getString(MODULE, "confErr" , params));
			} catch (IOException ioe)	{
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params = {ioe.toString()};
				error(su, grRes.getString(MODULE, "error", params));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params = {me.getMessage()};
				error(su, grRes.getString(MODULE, "errReq", params));
			}		
			
			return false;
		}
	}
	
	/**
	 *	An extension of GalleryTask to handle fetching albums.
	 */
	class AlbumListTask extends GalleryTask {
		
		AlbumListTask( StatusUpdate su ) {
			super(su);	
		}
		
		void runTask() {
            Object [] params = {g.toString()};
			su.startProgress(StatusUpdate.LEVEL_BACKGROUND, 0, 10, grRes.getString(MODULE, "albmFtchng", params), true);
			
			try {
				long startTime = System.currentTimeMillis();

				if (serverMinorVersion < 2) {
					list20();
				} else {
					list22();
				}

				Log.log(Log.LEVEL_INFO, MODULE, "execution time for AlbumList: " + (System.currentTimeMillis() - startTime));
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e );
                Object [] params2 = {gr2e.getMessage()};
				error(su, grRes.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params2 = {ioe.toString()};
				error(su, grRes.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params2 = {me.toString()};
				error(su, grRes.getString(MODULE, "error", params2));
			}
			
			su.stopProgress(StatusUpdate.LEVEL_BACKGROUND, grRes.getString(MODULE, "fetchComplete"));
		}

		private void list20() throws IOException, ModuleException {
			// setup the protocol parameters
			NVPair form_data[] = {
				new NVPair("cmd", "fetch-albums"),
				new NVPair("protocol_version", PROTOCOL_VERSION )
			};
			Log.log(Log.LEVEL_TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));

			// load and validate the response
			Properties p = requestResponse( form_data );
			if ( p.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
				ArrayList mAlbumList = new ArrayList();

				// parse and store the data
				int albumCount = Integer.parseInt( p.getProperty( "album_count" ) );
//	System.err.println( "### albumCount = " + albumCount );
				HashMap ref2parKey = new HashMap();
				HashMap ref2album = new HashMap();
				for ( int i = 1; i < albumCount + 1; i++ ) {
					Album a = new Album();

					String nameKey = "album.name." + i;
					String titleKey = "album.title." + i;
					String parentKey = "album.parent." + i;
					String permsAddKey = "album.perms.add." + i;
					String permsWriteKey = "album.perms.write." + i;
					String permsDelItemKey = "album.perms.del_item." + i;
					String permsDelAlbKey = "album.perms.del_alb." + i;
					String permsCreateSubKey = "album.perms.create_sub." + i;
					String infoExtraFieldsKey = "album.info.extrafields." + i;

					a.setCanAdd( isTrue( p.getProperty( permsAddKey ) ) );
					a.setCanWrite( isTrue( p.getProperty( permsWriteKey ) ) );
					a.setCanDeleteFrom( isTrue( p.getProperty( permsDelItemKey ) ) );
					a.setCanDeleteThisAlbum( isTrue( p.getProperty( permsDelAlbKey ) ) );
					a.setCanCreateSubAlbum( isTrue( p.getProperty( permsCreateSubKey ) ) );
					a.setExtraFieldsString( p.getProperty( infoExtraFieldsKey ) );

					a.setName( p.getProperty( nameKey ) );
					a.setTitle( p.getProperty( titleKey ) );

					a.setGallery( g );
					mAlbumList.add( a );

					// map album ref nums to albums
					ref2album.put( "" + i, a );

					// map album refs to parent refs
					String parentRefS = p.getProperty( parentKey );
					int parentRef = Integer.parseInt( parentRefS );
					if ( parentRef != 0 ) {
						ref2parKey.put( "" + i, parentRefS );
					}
				}

				// link albums to parents
				for ( int i = 1; i < albumCount + 1; i++ ) {
					String parentKey = (String)ref2parKey.get( "" + i );
					if ( parentKey != null ) {
						Album a = (Album)ref2album.get( "" + i );
						if ( a == null ) {
						}
						Album pa = (Album)ref2album.get( parentKey );
						a.setParentAlbum( pa );
					}
				}

				status(su, StatusUpdate.LEVEL_BACKGROUND, grRes.getString(MODULE, "ftchdAlbms"));

				g.setAlbumList(mAlbumList);
			} else {
                Object [] params = {p.getProperty( "status_text" )};
				error(su, grRes.getString(MODULE, "error", params));
			}
		}

		private void list22() throws IOException, ModuleException {
			// setup the protocol parameters
			NVPair form_data[] = {
				new NVPair("cmd", "fetch-albums-prune"),
				new NVPair("protocol_version", PROTOCOL_VERSION )
			};
			Log.log(Log.LEVEL_TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));

			// load and validate the response
			Properties p = requestResponse( form_data );
			if ( p.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
				ArrayList mAlbumList = new ArrayList();

				// parse and store the data
				int albumCount = Integer.parseInt( p.getProperty( "album_count" ) );
//	System.err.println( "### albumCount = " + albumCount );
				HashMap name2parentName = new HashMap();
				HashMap name2album = new HashMap();
				for ( int i = 1; i < albumCount + 1; i++ ) {
					Album a = new Album();

					String nameKey = "album.name." + i;
					String titleKey = "album.title." + i;
					String parentKey = "album.parent." + i;
					String permsAddKey = "album.perms.add." + i;
					String permsWriteKey = "album.perms.write." + i;
					String permsDelItemKey = "album.perms.del_item." + i;
					String permsDelAlbKey = "album.perms.del_alb." + i;
					String permsCreateSubKey = "album.perms.create_sub." + i;
					String infoExtraFieldKey = "album.info.extrafields." + i;

					a.setCanAdd( isTrue( p.getProperty( permsAddKey ) ) );
					a.setCanWrite( isTrue( p.getProperty( permsWriteKey ) ) );
					a.setCanDeleteFrom( isTrue( p.getProperty( permsDelItemKey ) ) );
					a.setCanDeleteThisAlbum( isTrue( p.getProperty( permsDelAlbKey ) ) );
					a.setCanCreateSubAlbum( isTrue( p.getProperty( permsCreateSubKey ) ) );

					String name = p.getProperty( nameKey );
					String title = p.getProperty( titleKey );
					a.setName( name );
					a.setTitle( title );
					a.setExtraFieldsString( p.getProperty(infoExtraFieldKey));

					a.setGallery( g );
					mAlbumList.add( a );

					// map album names to parent albums
					name2album.put( name, a );

					// map album refs to parent refs
					String parentName = p.getProperty( parentKey );
					if ( parentName != null && parentName.length() > 0 && !parentName.equals("0")) {
						name2parentName.put( name, parentName );
					}
				}

				// link albums to parents
				Iterator it = name2parentName.keySet().iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					String parentName = (String) name2parentName.get(name);
					Album child = (Album) name2album.get(name);
					Album parent = (Album) name2album.get(parentName);

					if (child != null && parent != null) {
						child.setParentAlbum(parent);
					}
				}

				// reorder
				//Collections.reverse(mAlbumList);
				ArrayList orderedAlbums = new ArrayList();
				int depth = 0;
				while (!mAlbumList.isEmpty()) {
					it = mAlbumList.iterator();
					while (it.hasNext()) {
						Album a = (Album) it.next();

						if (a.getAlbumDepth() == depth) {
							it.remove();

							Album parentAlbum = a.getParentAlbum();
							if (parentAlbum == null) {
								orderedAlbums.add(a);
							} else {
								int i = orderedAlbums.indexOf(parentAlbum);
								orderedAlbums.add(i + 1, a);
							}
						}
					}

					depth++;
				}

				status(su, StatusUpdate.LEVEL_BACKGROUND, grRes.getString(MODULE, "ftchdAlbms"));

				g.setAlbumList(orderedAlbums);
			} else {
                Object [] params = {p.getProperty( "status_text" )};
				error(su, grRes.getString(MODULE, "error", params));
			}
		}
	}
	
	/**
	 *	An extension of GalleryTask to handle getting album information.
	 */
	class AlbumPropertiesTask extends GalleryTask {
		Album a;
		
		AlbumPropertiesTask( StatusUpdate su, Album a ) {
			super(su);
			this.a = a;
		}
		
		void runTask() {
            Object [] params = {g.toString()};
			status(su, StatusUpdate.LEVEL_GENERIC, grRes.getString(MODULE, "getAlbmInfo"));
			
			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "album-properties"),
					new NVPair("protocol_version", PROTOCOL_VERSION ),
					new NVPair("set_albumName", a.getName() )
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "album-info parameters: " + Arrays.asList(form_data));
				
				// load and validate the response
				Properties p = requestResponse( form_data );
				if ( p.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
					// parse and store the data
					int autoResize = Integer.parseInt( p.getProperty( "auto_resize" ) );
					a.setServerAutoResize( autoResize );
					
					status(su, StatusUpdate.LEVEL_GENERIC, grRes.getString(MODULE, "ftchdAlbmProp"));
					
				} else {
					error(su, "Error: " + p.getProperty( "status_text" ));
				}
				
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e );
                Object [] params2 = {gr2e.getMessage()};
				error(su, grRes.getString(MODULE, "error", params2));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params2 = {ioe.toString()};
				error(su, grRes.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params2 = {me.toString()};
				error(su, grRes.getString(MODULE, "error", params2));
			}
		}
	}
	
	/**
	 *	An extension of GalleryTask to handle creating a new album.
	 */
	class NewAlbumTask extends GalleryTask {
		Album parentAlbum;
		String albumName;
		String albumTitle;
		String albumDesc;
		private String newAlbumName;

		NewAlbumTask( StatusUpdate su, Album parentAlbum, String albumName,
				String albumTitle, String albumDesc ) {
			super(su);
			this.parentAlbum = parentAlbum;
			this.albumName = albumName;
			this.albumTitle = albumTitle;
			this.albumDesc = albumDesc;
		}
		
		void runTask() {
            Object [] params = {g.toString()};
			status(su, StatusUpdate.LEVEL_GENERIC, grRes.getString(MODULE, "getAlbmInfo", params));

			// if the parent is null (top-level album), set the album name to an illegal name so it's set to null
			// by Gallery. Using an empty string doesn't work, because then the HTTP parameter is not
			// parsed, and the session album is kept the same as before (from the cookie).
			String parentAlbumName = (parentAlbum == null) ? "hack_null_albumName" : parentAlbum.getName();

			if (GalleryRemote.getInstance().properties.getBooleanProperty(HTML_ESCAPE_CAPTIONS)) {
				albumTitle = HTMLEscaper.escape(albumTitle);
				albumDesc = HTMLEscaper.escape(albumDesc);
			}

			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "new-album"),
					new NVPair("protocol_version", PROTOCOL_VERSION ),
					new NVPair("set_albumName", parentAlbumName ),
					new NVPair("newAlbumName", albumName ),
					new NVPair("newAlbumTitle", albumTitle ),
					new NVPair("newAlbumDesc", albumDesc )
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "new-album parameters: " + Arrays.asList(form_data));
				
				// load and validate the response
				Properties p = requestResponse( form_data );
				if ( p.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
					status(su, StatusUpdate.LEVEL_GENERIC, grRes.getString(MODULE, "crateAlbmOk"));
					newAlbumName = p.getProperty("album_name");
				} else {
                    Object [] params2 = {p.getProperty( "status_text" )};
					error(su, grRes.getString(MODULE, "error", params2));
				}

            } catch ( GR2Exception gr2e ) {
                Log.logException(Log.LEVEL_ERROR, MODULE, gr2e );
                Object [] params2 = {gr2e.getMessage()};
                error(su, grRes.getString(MODULE, "error", params2));
            } catch (IOException ioe) {
                Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params2 = {ioe.toString()};
                error(su, grRes.getString(MODULE, "error", params2));
            } catch (ModuleException me) {
                Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params2 = {me.toString()};
                error(su, grRes.getString(MODULE, "error", params2));
            }
		}

		public String getNewAlbumName() {
			return newAlbumName;
		}
	}
	
	boolean isTrue( String s ){
		return s.equals( "true" );	
	}

	class MyTransferListener implements TransferListener {
		StatusUpdate su;
		java.text.DecimalFormat df = new java.text.DecimalFormat("##,##0");
		java.text.DecimalFormat ff = new java.text.DecimalFormat("##,##0.0");

		MyTransferListener(StatusUpdate su) {
			this.su = su;
		}

		public void dataTransferred(int transferred, int overall, double kbPerSecond)
		{
            Object [] params = {
                df.format(transferred / 1024),
                df.format(overall / 1024),
                ff.format(kbPerSecond / 1024.0) };
			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "trnsfrStat", params));
			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ONE, transferred);
		}

		public void transferStart(int overall) {
			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ONE, 0, overall);
		}

		public void transferEnd() {
			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ONE, grRes.getString(MODULE, "upCompSrvrProc"));
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
