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

public class GalleryComm {
	private static final String MODULE = "GalleryCom";
	
	private static final String PROTOCAL_VERSION = "1";
	private static final String SCRIPT_NAME = "gallery_remote.php";

	private String mURLString = null;
	private String mUsername = null;
	private String mPassword = null;
	private String mAlbum = null;
	
	private ArrayList mFileList;
	private ArrayList mAlbumList;
	
	private String mStatus;
	private int mUploadedCount;
	
	private boolean mLoggedIn = false;
	private boolean mDone = false;
	
	private HTTPConnection mConnection; 
	
	public GalleryComm() {
		//-- our policy handler accepts all cookies ---
		CookieModule.setCookiePolicyHandler(new GalleryCookiePolicyHandler());
	}
	
	public void setURLString (String val) {
		mURLString = val;
		
		//-- needs to have a trailing slash ---
		if (!mURLString.endsWith("/")) {
			mURLString = mURLString + "/";
		}
		
		if (!mURLString.startsWith("http://"))
		{
			mURLString = "http://" + mURLString;
		}
		
		mLoggedIn = false;
	}
	
	public String getURLString () {
		return mURLString;
	}
	
	public void setUsername (String val) {
		mUsername = val;
		mLoggedIn = false;
	}
	
	public String getUsername () {
		return mUsername;
	}
	
	public void setPassword (String val) {
		mPassword = val;
		mLoggedIn = false;
	}
	
	public String getPassword () {
		return mPassword;
	}
	
	public void setAlbum (String val) {
		mAlbum = val;
	}
	
	public String getAlbum () {
		return mAlbum;
	}
	
	
	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	public void uploadFiles(ArrayList fileList) {
		mFileList = fileList;
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				return new ActualTask("upload");
			}
		};
		worker.start();
	}

	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	public void fetchAlbums() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				return new ActualTask("fetch-albums");
			}
		};
		worker.start();
	}
	
	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	class ActualTask {
		ActualTask (String task) {
			mDone = false;

			//-- login ---
			if (!mLoggedIn) {
				if (!login()) {
					mDone = true;
					return;
				}
			}
			
			if (task.equals("upload")) {
				//-- upload each file, one at a time ---
				boolean allGood = true;
				mUploadedCount = 0;
				Iterator iter = mFileList.iterator();
				while (iter.hasNext() && allGood) {
					File file = (File) iter.next();
					allGood = uploadFile(file);
					mUploadedCount++;
				}
				
				if (allGood) {
					status("[Upload] Complete.");
				} else {
					status("[Upload] Aborted.");
				}
			} else if (task.equals("fetch-albums")) {
				requestAlbumList();
				status("[Album Fetch] Complete.");
			}
	
			mDone = true;
		}
	}
		
	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	private boolean login() {
	
		mLoggedIn = false;
		String loginMessage;
		status("[Login] Logging in...");
		
		try
		{
			URL url = new URL(mURLString);
			String loginPage = url.getFile() + SCRIPT_NAME;
			
			NVPair form_data[] = {
				new NVPair("cmd", "login"),
				new NVPair("protocal_version", PROTOCAL_VERSION),
				new NVPair("uname", mUsername),
				new NVPair("password", mPassword)
			};
			Log.log(Log.TRACE, MODULE, "login parameters: uname:" + mUsername 
				+ " password: " + mPassword + " url: " + mURLString + SCRIPT_NAME);
			
			mConnection = new HTTPConnection(url);
			HTTPResponse rsp = mConnection.Post(loginPage, form_data);
			
			if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
				// retry, the library will have fixed the URL
				rsp = mConnection.Post(loginPage, form_data);
			}
			
			if (rsp.getStatusCode() >= 400)	{
				loginMessage = "HTTP Error: "+ rsp.getStatusCode()+" "+rsp.getReasonLine();
			} else {
				String response = new String(rsp.getData()).trim();
				Log.log(Log.TRACE, MODULE, response);
				
				if (response.indexOf("SUCCESS") >= 0) {
					mLoggedIn = true;
					loginMessage = "Success";
				} else {
					loginMessage = "Login Error: " + response;
				}
			}
		} catch (IOException ioe) {
			Log.logException(Log.ERROR, MODULE, ioe);
			loginMessage = "Error: " + ioe.toString();
		} catch (ModuleException me) {
			Log.logException(Log.ERROR, MODULE, me);
			loginMessage = "Error handling request: " + me.getMessage();
		}

		status("[Login] " + loginMessage);
		return mLoggedIn;
	}

	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	private boolean requestAlbumList() {
		mLoggedIn = false;
		String albumMessage;
		status("[Fetch Albums] Fetching...");
		
		try {
			URL url = new URL(mURLString);
			String loginPage = url.getFile() + SCRIPT_NAME;
			
			NVPair form_data[] = {
				new NVPair("cmd", "fetch-albums"),
				new NVPair("protocal_version", PROTOCAL_VERSION),
				new NVPair("uname", mUsername),
				new NVPair("password", mPassword)
			};
			Log.log(Log.TRACE, MODULE, "fetchAlbums parameters: uname:" + mUsername 
				+ " password: " + mPassword + " url: " + mURLString + SCRIPT_NAME);
			
			mConnection = new HTTPConnection(url);
			HTTPResponse rsp = mConnection.Post(loginPage, form_data);
			
			if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
				// retry, the library will have fixed the URL
				rsp = mConnection.Post(loginPage, form_data);
			}
			
			if (rsp.getStatusCode() >= 400)
			{
				albumMessage = "HTTP Error: "+ rsp.getStatusCode()+" "+rsp.getReasonLine();
			} else {
				String response = new String(rsp.getData()).trim();
				Log.log(Log.TRACE, MODULE, response);

				if (response.indexOf("SUCCESS") >= 0) {
					albumMessage = "Success";
					
					mAlbumList = new ArrayList();
					
					// build the list of hashtables here...
					StringTokenizer lineT = new StringTokenizer(response, "\n");
					while (lineT.hasMoreTokens()) {
						StringTokenizer colT = new StringTokenizer(lineT.nextToken(), "\t");
						Hashtable h = new Hashtable();
						if (colT.countTokens() == 2) {
							h.put("name", URLDecoder.decode(colT.nextToken()));
							h.put("title", URLDecoder.decode(colT.nextToken()));
							mAlbumList.add(h);
						}
					}
				} else {
					albumMessage = "Error: [" + response + "]";
				}
			}
		} catch (IOException ioe) {
			Log.logException(Log.ERROR, MODULE, ioe);
			albumMessage = "Error: " + ioe.toString();
		} catch (ModuleException me) {
			Log.logException(Log.ERROR, MODULE, me);
			albumMessage = "Error handling request: " + me.getMessage();
		} catch (Exception ee) {
			Log.logException(Log.ERROR, MODULE, ee);
			albumMessage = "Error: " + ee.getMessage();
		}

		status("[Album Fetch] " + albumMessage);
		return mLoggedIn;
	}

	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	public boolean uploadFile(File file) {
		String filename = file.getName();
		status("[Upload " + filename + "] Uploading...");
		
		String uploadMessage;
		
		try	{
			URL url = new URL(mURLString);
			String savePage = url.getFile() + SCRIPT_NAME;
		
			NVPair[] opts = {
				new NVPair("set_albumName", mAlbum),
				new NVPair("cmd", "add-item"), 
				new NVPair("protocal_version", PROTOCAL_VERSION)
			};
			Log.log(Log.TRACE, MODULE, "fetchAlbums parameters: album name:" + mAlbum 
				+ " url: " + mURLString + SCRIPT_NAME);

			NVPair[] afile = { new NVPair("userfile", file.getAbsolutePath()) };
			NVPair[] hdrs = new NVPair[1];
			byte[]   data = Codecs.mpFormDataEncode(opts, afile, hdrs);
			HTTPResponse rsp = mConnection.Post(savePage, data, hdrs);
			
			if (rsp.getStatusCode() >= 300 && rsp.getStatusCode() < 400) {
				// retry, the library will have fixed the URL
				rsp = mConnection.Post(savePage, data, hdrs);
			}
			
			if (rsp.getStatusCode() >= 400)	{
				uploadMessage = "HTTP Error: "+ rsp.getStatusCode()+" "+rsp.getReasonLine();
			} else {
				String response = new String(rsp.getData()).trim();
				Log.log(Log.TRACE, MODULE, response);

				if (response.indexOf("SUCCESS") >= 0) {
					uploadMessage = "Success";
				} else {
					uploadMessage = "Upload Error: " + response;
				}
			}
		}
		catch (IOException ioe)	{
			Log.logException(Log.ERROR, MODULE, ioe);
			uploadMessage = "Error: " + ioe.toString();
		} catch (ModuleException me) {
			Log.logException(Log.ERROR, MODULE, me);
			uploadMessage = "Error handling request: " + me.getMessage();
		}		
		
		status("[Upload " + filename + "] " + uploadMessage);
		
		return (uploadMessage.equals("Success"));
	}
	
	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	private void status(String message) {
		mStatus = message;
		Log.log(Log.INFO, MODULE, message);
	}
	
	public String getStatus() {
		return mStatus;
	}
	
	public int getUploadedCount() {
		return mUploadedCount;
	}		

	public ArrayList getAlbumList() {
		return mAlbumList;
	}
	
	public boolean done() {
		return mDone;
	}
}
