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
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

import HTTPClient.Codecs;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import HTTPClient.NVPair;

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.HTMLEscaper;

public class GalleryComm1 extends GalleryComm implements GalleryCommCapabilities {
	private static final String MODULE = "GalComm1";



	public static final String PROTOCAL_VERSION = "1";
	public static final String SCRIPT_NAME = "gallery_remote.php";
	
	protected Gallery g = null;

	protected GalleryComm1(Gallery g) {
		this.g = g;
		
		capabilities = new int[] {CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS};
		Arrays.sort(capabilities);
	}
	
	void doTask(GalleryTask task, boolean async) {
		if (async) {
			Thread t = new Thread(task);
			t.start();
		} else {
			task.run();
		}
	}
	
	public void uploadFiles( StatusUpdate su, boolean async ) {
		doTask(new UploadTask( su ), async);
	}

	public void fetchAlbums( StatusUpdate su, boolean async ) {
		doTask(new AlbumListTask( su ), async);
	}	
	
	
	//-------------------------------------------------------------------------
	//-- GalleryTask
	//-------------------------------------------------------------------------	
	abstract class GalleryTask implements Runnable {
		HTTPConnection mConnection;
		StatusUpdate su;
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
				Log.log(Log.LEVEL_TRACE, MODULE, "Still logged in to " + g.toString());
			}
			
			runTask();
			su.setInProgress(false);
		}
		
		public void interrupt() {
			interrupt = true;
		}
		
		abstract void runTask();

		private boolean login() {
            Object [] params = {g.toString() };

			status(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "logging", params));
			
			try	{
				URL url = g.getGalleryUrl(SCRIPT_NAME);
				String urlPath = url.getFile();
				Log.log(Log.LEVEL_TRACE, MODULE, "Url: " + url);
				
				NVPair form_data[] = {
					new NVPair("cmd", "login"),
					new NVPair("protocal_version", PROTOCAL_VERSION),
					new NVPair("uname", g.getUsername()),
					new NVPair("password", g.getPassword())
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "login parameters: " + Arrays.asList(form_data));
				
				HTTPConnection mConnection = new HTTPConnection(url);
				HTTPResponse rsp = mConnection.Post(urlPath, form_data);
				
				if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
					// retry, the library will have fixed the URL
					status(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "redirect"));
					
					rsp = mConnection.Post(urlPath, form_data);
				}
				
				if (rsp.getStatusCode() >= 300)	{
                    Object [] params2 = {new Integer(rsp.getStatusCode()), rsp.getReasonLine()};
					error(GRI18n.getString(MODULE, "httpErr", params2));
					return false;
				} else {
					String response = new String(rsp.getData()).trim();
					Log.log(Log.LEVEL_TRACE, MODULE, response);
					
					if (response.indexOf("SUCCESS") >= 0) {
						status(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "loggedIn"));
						return true;
					} else {
                        Object [] params2 = { response };
						error(GRI18n.getString(MODULE, "logErr", params2));
						return false;
					}
				}
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params3 = {ioe.toString() };
				status(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "error", params3));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params3 = {me.getMessage() };
				status(StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "errReq", params3));
			}
	
			return false;
		}
		
		void status(int level, String message) {
			Log.log(Log.LEVEL_INFO, MODULE, message);
			su.updateProgressStatus(level, message);
		}
		
		void error(String message) {
			su.error( message );
			status(StatusUpdate.LEVEL_GENERIC, message);
		}
		
		void trace(String message) {
			Log.log(Log.LEVEL_TRACE, MODULE, message);
		}
	}
	
	class UploadTask extends GalleryTask {		
		UploadTask( StatusUpdate su ) {
			super(su);	
		}
		
		void runTask() {
			ArrayList pictures = g.getAllPictures();
			
			su.startProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, 0, pictures.size(), GRI18n.getString(MODULE, "uploadingPic"), false);
			
			// upload each file, one at a time
			boolean allGood = true;
			int uploadedCount = 0;
			Iterator iter = pictures.iterator();
			while (iter.hasNext() && allGood && !interrupt) {
				Picture p = (Picture) iter.next();

                Object [] params = {p.toString(), new Integer((uploadedCount + 1)), new Integer(pictures.size())};
				su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "uploadingStat", params));
				
				allGood = uploadPicture(p);
				
				su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_PROGRESS, uploadedCount++);
				
				p.getAlbum().removePicture(p);
			}
			
			if (allGood) {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "upldComplete"));
			} else {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_PROGRESS, GRI18n.getString(MODULE, "upldFailed"));
			}
		}

		boolean uploadPicture(Picture p) {
			try	{
				URL url = g.getGalleryUrl(SCRIPT_NAME);
				String urlPath = url.getFile();
				Log.log(Log.LEVEL_TRACE, MODULE, "Url: " + url);
			
				NVPair[] opts = {
					new NVPair("set_albumName", p.getAlbum().getName()),
					new NVPair("cmd", "add-item"), 
					new NVPair("protocal_version", PROTOCAL_VERSION)
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "add-item parameters: " + Arrays.asList(opts));
	
				NVPair[] afile = { new NVPair("userfile", p.getUploadSource().getAbsolutePath()) };
				NVPair[] hdrs = new NVPair[1];
				byte[]   data = Codecs.mpFormDataEncode(opts, afile, hdrs);
				HTTPConnection mConnection = new HTTPConnection(url);
				HTTPResponse rsp = mConnection.Post(urlPath, data, hdrs);
				
				if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
					// retry, the library will have fixed the URL
					status(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "redirect"));
					
					rsp = mConnection.Post(urlPath, data, hdrs);
				}
								
				if (rsp.getStatusCode() >= 300)	{
                    Object [] params2 = {new Integer(rsp.getStatusCode()), rsp.getReasonLine()};
					error(GRI18n.getString(MODULE, "httpErr", params2));
					return false;
				} else {
					String response = new String(rsp.getData()).trim();
					Log.log(Log.LEVEL_TRACE, MODULE, response);
	
					if (response.indexOf("SUCCESS") >= 0) {
						trace(GRI18n.getString(MODULE, "upldSucc"));
						return true;
					} else {
                        Object [] params = {response };
						error(GRI18n.getString(MODULE, "upldErr", params));
						return false;
					}
				}
			} catch (IOException ioe)	{
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params3 = {ioe.toString() };
				error(GRI18n.getString(MODULE, "error", params3));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params3 = {me.getMessage() };
				error(GRI18n.getString(MODULE, "errReq", params3));
			}		
			
			return false;
		}
	}
	
	class AlbumListTask extends GalleryTask {
		AlbumListTask( StatusUpdate su ) {
			super(su);	
		}
		
		void runTask() {
            Object [] params = {g.toString() };
			su.startProgress(StatusUpdate.LEVEL_BACKGROUND, 0, 10, GRI18n.getString(MODULE, "ftchngAlbm", params), true);
			
			try {
				URL url =g.getGalleryUrl(SCRIPT_NAME);
				String urlPath = url.getFile();
				Log.log(Log.LEVEL_TRACE, MODULE, "Url: " + url);
				
				NVPair form_data[] = {
					new NVPair("cmd", "fetch-albums"),
					new NVPair("protocal_version", PROTOCAL_VERSION),
					new NVPair("uname", g.getUsername()),
					new NVPair("password", g.getPassword())
				};
				Log.log(Log.LEVEL_TRACE, MODULE, "fetchAlbums parameters: " + Arrays.asList(form_data));
				
				mConnection = new HTTPConnection(url);
				HTTPResponse rsp = mConnection.Post(urlPath, form_data);
				
				if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
					// retry, the library will have fixed the URL
					status(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "redirect"));
					
					rsp = mConnection.Post(urlPath, form_data);
				}
				
				if (rsp.getStatusCode() >= 300)	{
                    Object [] params2 = {new Integer(rsp.getStatusCode()), rsp.getReasonLine()};
					error(GRI18n.getString(MODULE, "httpErr", params2));
					return;
				} else {
					String response = new String(rsp.getData()).trim();
					Log.log(Log.LEVEL_TRACE, MODULE, response);
	
					if (response.indexOf("SUCCESS") >= 0) {
						ArrayList mAlbumList = new ArrayList();
						
						// build the list of hashtables here...
						StringTokenizer lineT = new StringTokenizer(response, "\n");
						while (lineT.hasMoreTokens()) {
							StringTokenizer colT = new StringTokenizer(lineT.nextToken(), "\t");
							
							if (colT.countTokens() == 2) {
								Album a = new Album(g);
								
								a.setName( colT.nextToken() );
								a.setTitle( HTMLEscaper.unescape(colT.nextToken()) );
																
								mAlbumList.add(a);

								a.setParentAlbum(null);
							}
						}
						
						status(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "ftchdAlbm"));
						
						g.setAlbumList(mAlbumList);
					} else {
                        Object [] params2 = { response };
						error(GRI18n.getString(MODULE, "error", params2));
					}
				}
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
                Object [] params2 = {ioe.toString()};
				status(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "error", params2));
			} catch (ModuleException me) {
				Log.logException(Log.LEVEL_ERROR, MODULE, me);
                Object [] params2 = {me.toString()};
				status(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "error", params2));
			} catch (Exception ee) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ee);
                Object [] params2 = {ee.toString()};
				status(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "error", params2));
			}
		
			su.stopProgress(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "ftchComplt"));
		}
	}
}
