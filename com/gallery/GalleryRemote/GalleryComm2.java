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
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import com.gallery.GalleryRemote.model.*;

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
	GalleryCommCapabilities {
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
	
	
	/* -------------------------------------------------------------------------
	 *	INSTANCE VARIABLES
	 */
	
	/**
	 *	The gallery this GalleryComm2 instance is attached to.
	 */
	protected final Gallery g;
	
	/**
	 *	The process ID for StatusUpdate.  XXX: What's -1? -- tim
	 *	-1 is an initialization value. means no id.
	 */
	int pId = -1;
	
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
	public void newAlbum( StatusUpdate su, Album parentAlbum,
			String newAlbumName, String newAlbumTitle,
			String newAlbumDesc, boolean async ) {
		doTask( new NewAlbumTask( su, parentAlbum, newAlbumName,
			newAlbumTitle, newAlbumDesc ), async );
	}
	
	public void logOut() {
		isLoggedIn = false;
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
	
	void status( StatusUpdate su, String message) {
		Log.log(Log.INFO, MODULE, message);
		if (pId != -1) {
			su.setStatus(message);
		} else {
			su.updateProgressStatus(pId, message);
		}
	}
	
	void error( StatusUpdate su, String message) {
		status(su, message);
		su.error( message );
	}
	
	void trace(String message) {
		Log.log(Log.TRACE, MODULE, message);
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
		
		public GalleryTask( StatusUpdate su ) {
			if ( su == null ) {
				this.su = new StatusUpdateAdapter(){};
			} else {
				this.su = su;	
			}
		}
		
		public void run() {
			su.setInProgress(true);
			if ( ! isLoggedIn ) {
				if ( !login() ) {
					su.setInProgress(false);
					return;
				}
				
				isLoggedIn = true;
			} else {
				Log.log(Log.TRACE, MODULE, "Still logged in to " + g.toString());
			}
			
			runTask();
			su.setInProgress(false);
		}
		
		public void interrupt() {
			interrupt = true;
		}
		
		abstract void runTask();
		
		/**
		 *	POSTSs a request to the Gallery server with the given form data.
		 */	
		Properties requestResponse( NVPair form_data[] ) throws GR2Exception, ModuleException, IOException {
			return requestResponse( form_data, null );
		}
		
		/**
		 *	POSTSs a request to the Gallery server with the given form data.  If data is
		 *	not null, a multipart MIME post is performed.
		 */	
		Properties requestResponse( NVPair form_data[], byte[] data) throws GR2Exception, ModuleException, IOException {

			// assemble the URL
			URL galUrl =  g.getUrl();
			String urlPath = galUrl.getFile();
			urlPath = urlPath + ( (urlPath.endsWith( "/" )) ? SCRIPT_NAME : "/" + SCRIPT_NAME );	
			Log.log(Log.TRACE, MODULE, "Url: " + urlPath );
			
			// create a connection	
			HTTPConnection mConnection = new HTTPConnection( galUrl );
			HTTPResponse rsp = null;
			
			// post multipart if there is data
			if ( data == null ) {
				rsp = mConnection.Post(urlPath, form_data);
			} else { 
				rsp = mConnection.Post(urlPath, data, form_data);
			}
			
			// handle 30x redirects
			if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
				// retry, the library will have fixed the URL
				status(su, "Received redirect, following...");
				if ( data == null ) {
					rsp = mConnection.Post(urlPath, form_data);
				} else { 
					rsp = mConnection.Post(urlPath, data, form_data);
				}
			}
			
			// handle response
			if (rsp.getStatusCode() >= 300)	{
				throw new GR2Exception( "HTTP POST failed (HTTP " + rsp.getStatusCode() + " " + rsp.getReasonLine() + ")");
			} else {
				// load response 
				String response = new String(rsp.getData()).trim();
				Log.log(Log.TRACE, MODULE, response);
				
				// validate response
				if ( response.startsWith( PROTOCOL_MAGIC ) ) {
					Properties p = new Properties();
					p.load( new StringBufferInputStream( response ) );
					return p;
				} else {
					throw new GR2Exception("Server contacted, but Gallery not found at this URL (" + galUrl.toString() + ")");
				}
			}
		}
		
		private boolean login() {
			status(su, "Logging in to " + g.toString());
			
			// setup protocol parameters
			NVPair form_data[] = {
				new NVPair("cmd", "login"),
				new NVPair("protocol_version", PROTOCOL_VERSION),
				new NVPair("uname", g.getUsername()),
				new NVPair("password", g.getPassword())
			};
			Log.log(Log.TRACE, MODULE, "login parameters: " + Arrays.asList(form_data));
			
			// make the request
			try	{
				// load and validate the response
				Properties p = requestResponse( form_data );
				if ( p.getProperty( "status" ).equals( GR_STAT_SUCCESS ) ) {
					status(su, "Logged in");
					try {
						String serverVersion = p.getProperty( "server_version" );
						int i = serverVersion.indexOf( "." );
						serverMinorVersion = Integer.parseInt( serverVersion.substring( i+1 ) );
						
						Log.log(Log.TRACE, MODULE, "Server minor version: " + serverMinorVersion);
						
						if (serverMinorVersion >= 1) {
							// we have more than the 2.0 capabilities, we have 2.1 capabilities.
							capabilities = capabilities1;
						}
					} catch (Exception e) {
						Log.log( Log.ERROR, MODULE, "Malformed server_version: " + p.getProperty( "server_version" ) );
						Log.logException(Log.ERROR, MODULE, e);
					}
					return true;
				} else {
					error(su, "Login Error: " + p.getProperty( "status_text" ));
					return false;
				}
			
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.ERROR, MODULE, gr2e );
				error(su, "Error: " + gr2e.getMessage());
			} catch (IOException ioe) {
				Log.logException(Log.ERROR, MODULE, ioe);
				error(su, "Error: " + ioe.toString());
			} catch (ModuleException me) {
				Log.logException(Log.ERROR, MODULE, me);
				error(su, "Error handling request: " + me.getMessage());
			}
			
			return false;
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
			
			pId = su.startProgress(0, pictures.size(), "Uploading pictures", false);
			
			// upload each file, one at a time
			boolean allGood = true;
			int uploadedCount = 0;
			Iterator iter = pictures.iterator();
			while (iter.hasNext() && allGood && !interrupt) {
				Picture p = (Picture) iter.next();
				
				su.updateProgressStatus(pId, "Uploading " + p.toString()
					+ " (" + (uploadedCount + 1) + "/" + pictures.size() + ")");
				
				allGood = uploadPicture(p);
				
				su.updateProgressValue(pId, uploadedCount++);
				
				p.getAlbum().removePicture(p);
			}
			
			if (allGood) {
				su.stopProgress(pId, "Upload complete");
			} else {
				su.stopProgress(pId, "Upload failed");
			}
			
			pId = -1;
		}
		
		boolean uploadPicture(Picture p) {
			try	{
				// can't set null as an NVPair value
				String caption = p.getCaption();
				caption = (caption == null) ? "" : caption;
				
				// setup the protocol parameters
				NVPair[] opts = {
					new NVPair("cmd", "add-item"),
					new NVPair("protocol_version", PROTOCOL_VERSION),
					new NVPair("set_albumName", p.getAlbum().getName()),
					new NVPair("caption", caption )
				};
				Log.log(Log.TRACE, MODULE, "add-item parameters: " + Arrays.asList(opts));
				
				// setup the multipart form data
				NVPair[] afile = { new NVPair("userfile", p.getUploadSource().getAbsolutePath()) };
				NVPair[] hdrs = new NVPair[1];
				byte[]   data = Codecs.mpFormDataEncode(opts, afile, hdrs);
				
				// load and validate the response
				Properties props = requestResponse( hdrs, data );
				if ( props.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
					status(su, "Upload successful.");
					return true;
				} else {
					error(su, "Upload error: " + props.getProperty( "status_text" ));
					return false;
				}
				
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.ERROR, MODULE, gr2e );
				error(su, "Error: " + gr2e.getMessage());
			} catch (IOException ioe)	{
				Log.logException(Log.ERROR, MODULE, ioe);
				error(su, "Error: " + ioe.toString());
			} catch (ModuleException me) {
				Log.logException(Log.ERROR, MODULE, me);
				error(su, "Error handling request: " + me.getMessage());
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
			pId = su.startProgress(0, 10, "Fetching albums from " + g.toString(), true);
			
			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "fetch-albums"),
					new NVPair("protocol_version", PROTOCOL_VERSION )
				};
				Log.log(Log.TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));
				
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
						
						a.setCanAdd( isTrue( p.getProperty( permsAddKey ) ) );
						a.setCanWrite( isTrue( p.getProperty( permsWriteKey ) ) );
						a.setCanDeleteFrom( isTrue( p.getProperty( permsDelItemKey ) ) );
						a.setCanDeleteThisAlbum( isTrue( p.getProperty( permsDelAlbKey ) ) );
						a.setCanCreateSubAlbum( isTrue( p.getProperty( permsCreateSubKey ) ) );
						
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
					
					status(su, "Fetched albums");
					
					g.setAlbumList(mAlbumList);
				} else {
					error(su, "Error: " + p.getProperty( "status_text" ));
				}
				
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.ERROR, MODULE, gr2e );
				error(su, "Error: " + gr2e.getMessage());
			} catch (IOException ioe) {
				Log.logException(Log.ERROR, MODULE, ioe);
				error(su, "Error: " + ioe.toString());
			} catch (ModuleException me) {
				Log.logException(Log.ERROR, MODULE, me);
				error(su, "Error: " + me.toString());
			}
			
			su.stopProgress(pId, "Fetch complete");
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
			status(su, "Getting album information from " + g.toString());
			
			try {
				// setup the protocol parameters
				NVPair form_data[] = {
					new NVPair("cmd", "album-properties"),
					new NVPair("protocol_version", PROTOCOL_VERSION ),
					new NVPair("set_albumName", a.getName() )
				};
				Log.log(Log.TRACE, MODULE, "album-info parameters: " + Arrays.asList(form_data));
				
				// load and validate the response
				Properties p = requestResponse( form_data );
				if ( p.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
					// parse and store the data
					int autoResize = Integer.parseInt( p.getProperty( "auto_resize" ) );
					a.setServerAutoResize( autoResize );
					
					status(su, "Fetched album properties.");
					
				} else {
					error(su, "Error: " + p.getProperty( "status_text" ));
				}
				
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.ERROR, MODULE, gr2e );
				error(su, "Error: " + gr2e.getMessage());
			} catch (IOException ioe) {
				Log.logException(Log.ERROR, MODULE, ioe);
				error(su, "Error: " + ioe.toString());
			} catch (ModuleException me) {
				Log.logException(Log.ERROR, MODULE, me);
				error(su, "Error: " + me.toString());
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
		
		NewAlbumTask( StatusUpdate su, Album parentAlbum, String albumName,
				String albumTitle, String albumDesc ) {
			super(su);
			this.parentAlbum = parentAlbum;
			this.albumName = albumName;
			this.albumTitle = albumTitle;
			this.albumDesc = albumDesc;
		}
		
		void runTask() {
			status(su, "Getting album information from " + g.toString());
			
			String parentAlbumName = (parentAlbum == null) ? "" : parentAlbum.getName();
			
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
				Log.log(Log.TRACE, MODULE, "new-album parameters: " + Arrays.asList(form_data));
				
				// load and validate the response
				Properties p = requestResponse( form_data );
				if ( p.getProperty( "status" ).equals(GR_STAT_SUCCESS) ) {
					status(su, "Create album successful.");
				} else {
					error(su, "Error: " + p.getProperty( "status_text" ));
				}
				
			} catch ( GR2Exception gr2e ) {
				Log.logException(Log.ERROR, MODULE, gr2e );
				error(su, "Error: " + gr2e.getMessage());
			} catch (IOException ioe) {
				Log.logException(Log.ERROR, MODULE, ioe);
				error(su, "Error: " + ioe.toString());
			} catch (ModuleException me) {
				Log.logException(Log.ERROR, MODULE, me);
				error(su, "Error: " + me.toString());
			}
		}
	}
	
	boolean isTrue( String s ){
		return s.equals( "true" );	
	}
	
	/* -------------------------------------------------------------------------
	 * MAIN METHOD (test only)
	 */ 
	public static void main( String [] args ) {
		
		try {
			StatusUpdate su = new StatusUpdateAdapter(){};
			Gallery g = new Gallery( new URL( "http://www.deathcult.com/gallery/" ), "ted", "1qwe2asd", /*TEMPORARY*/ su );
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
	}
}
