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
import com.gallery.GalleryRemote.prefs.GalleryProperties;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Properties;

/**
 * This interface is a temporary mechanism to let us use version
 * 1 and 2 of the protocol by changing a little code -- a replacement for
 * this is under development that will allow a GalleryRemote client
 * to automatically determine what protocol it should use given
 * a Gallery and to use the appropriate implementation.
 * 
 * @author <a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
 */
public abstract class GalleryComm implements PreferenceNames {
	private static final String MODULE = "GalComm";

	int[] capabilities = null;
	private static int lastRespCode = 0;

	/** Flag to hold logged in status.  Only need to log in once. */
	protected boolean isLoggedIn = false;
	protected boolean triedLogin = false;

	/* -------------------------------------------------------------------------
	 * STATIC INITIALIZATON
	 */ 
	
	static {
		/* Enable customized AuthorizePopup */
		AuthorizePopup.enable();

		/* Configures HTTPClient to accept all cookies
		 * this should be done at least once per GalleryRemote
		 * invokation */
		CookieModule.setCookiePolicyHandler(new CookiePolicyHandler() {
			public boolean acceptCookie(Cookie cookie, RoRequest req, RoResponse resp) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Accepting cookie: " + cookie);
				return true;
			}

			public boolean sendCookie(Cookie cookie, RoRequest req) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Sending cookie: " + cookie);
				return true;
			}
		});

		// http://cvs.sourceforge.net/viewcvs.py/jameleon/jameleon/src/java/net/sf/jameleon/util/JsseSettings.java?rev=1.4&view=markup
		// http://tp.its.yale.edu/pipermail/cas/2004-March/000348.html
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					Log.log(Log.LEVEL_INFO, MODULE, "TrustManager.getAcceptedIssuers");
					return new java.security.cert.X509Certificate[0];
				}
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {
					Log.log(Log.LEVEL_INFO, MODULE, "TrustManager.checkClientTrusted");
				}
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {
					Log.log(Log.LEVEL_INFO, MODULE, "TrustManager.checkServerTrusted");
				}
			}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}

	/**
	 * Causes the GalleryComm instance to upload the pictures in the
	 * associated Gallery to the server.
	 * 
	 * @param su an instance that implements the StatusUpdate interface.
	 */
	public void uploadFiles(StatusUpdate su, boolean async) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	/**
	 * Causes the GalleryComm instance to fetch the albums contained by
	 * associated Gallery from the server.
	 * 
	 * @param su an instance that implements the StatusUpdate interface.
	 */
	public void fetchAlbums(StatusUpdate su, boolean async) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	/**
	 * Causes the GalleryComm instance to fetch the album properties
	 * for the given Album.
	 * 
	 * @param su an instance that implements the StatusUpdate interface.
	 */
	public void albumInfo(StatusUpdate su, Album a, boolean async) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	/**
	 * Causes the GalleryComm instance to create a new album as a child of
	 * the specified album (or at the root if album is null)
	 * 
	 * @param su          an instance that implements the StatusUpdate interface.
	 * @param parentAlbum if null, create the album in the root of the gallery; otherwise
	 *                    create as a child of the given album
	 */
	public String newAlbum(StatusUpdate su, Album parentAlbum,
						   String newAlbumName, String newAlbumTitle,
						   String newAlbumDesc, boolean async) {
		throw new RuntimeException("This method is not available on this protocol");
	}

    public void fetchAlbumImages(StatusUpdate su, Album a, boolean recusive, boolean async, int maxPictures) {
        throw new RuntimeException("This method is not available on this protocol");
    }

	public boolean moveAlbum(StatusUpdate su, Album a, Album newParent, boolean async) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	public void login(StatusUpdate su) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	public void logOut() {
		Log.log(Log.LEVEL_INFO, MODULE, "Logging out and clearing cookies");
		isLoggedIn = false;
		CookieModule.discardAllCookies();
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public boolean hasCapability(StatusUpdate su, int capability) {
		if (! isLoggedIn() && !triedLogin) {
			login(su);
		}

		return java.util.Arrays.binarySearch(capabilities, capability) >= 0;
	}

	/**
	 * Return true if the last communication attempt failed with authorization error
	 */
	public static boolean wasAuthFailure() {
		boolean result = (lastRespCode == 401);
		return result;
	}

	public static GalleryComm getCommInstance(StatusUpdate su, URL url, Gallery g) {
		try {
			GalleryProperties p = GalleryRemote._().properties;

			// set proxy info
			String proxyList = System.getProperty("javaplugin.proxy.config.list");

			String proxyHost = null;
			int proxyPort = 80;
			String proxyUsername = null;
			String proxyPassword = null;

			if (proxyList != null && proxyList.length() != 0) {
				try {
					proxyList = proxyList.toUpperCase();
					Log.log(Log.LEVEL_TRACE, MODULE, "Plugin Proxy Config List Property: " + proxyList);
					// 6.0.0 1/14/03 1.3.1_06 appears to omit HTTP portion of reported proxy list... Mod to accomodate this...
					// Expecting proxyList of "HTTP=XXX.XXX.XXX.XXX:Port" OR "XXX.XXX.XXX.XXX:Port" & assuming HTTP...

					if (proxyList.indexOf("HTTP=") != -1) {
						proxyHost = proxyList.substring(proxyList.indexOf("HTTP=")+5, proxyList.indexOf(":"));
					} else {
						proxyHost = proxyList.substring(0, proxyList.indexOf(":"));
					}
					int endOfPort = proxyList.indexOf(",");
					if (endOfPort < 1) endOfPort = proxyList.length();
					proxyPort = Integer.parseInt(proxyList.substring(proxyList.indexOf(":")+1,endOfPort));
					Log.log(Log.LEVEL_TRACE, MODULE, "proxy " + proxyHost+" port " + proxyPort);
				}
				catch (Exception e) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Exception during failover auto proxy detection" );
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
					proxyHost = null;
				}
			} else if (p.getBooleanProperty(USE_PROXY)) {
				proxyHost = p.getProperty(PROXY_HOST);
				try {
					proxyPort = p.getIntProperty(PROXY_PORT);
				} catch (NumberFormatException e) {
				}

				proxyUsername = p.getProperty(PROXY_USERNAME);

				if (proxyUsername != null && proxyUsername.length() > 0) {
					proxyPassword = p.getBase64Property(PROXY_PASSWORD);
				}
			}

			if (proxyHost != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Setting proxy to " + proxyHost + ":" + proxyPort);

				HTTPConnection.setProxyServer(proxyHost, proxyPort);

				if (proxyUsername != null && proxyUsername.length() > 0) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Setting proxy auth to " + proxyUsername + ":" + proxyPassword);
					AuthorizationInfo.addBasicAuthorization(proxyHost, proxyPort, "",
							proxyUsername, proxyPassword);
				}

				// also set Java URLConnection proxy
				Properties sprops = System.getProperties();
				sprops.setProperty("http.proxySet", "true");
				sprops.setProperty("http.proxyHost", proxyHost);
				sprops.setProperty("http.proxyPort", ""+proxyPort);
			} else {
				HTTPConnection.setProxyServer(null, 0);
			}

			// create a connection
			HTTPConnection mConnection = new HTTPConnection(url);
			addUserInfo(mConnection, url);

			if (g.getType() == Gallery.TYPE_STANDALONE) {
				// assemble the URL
				String urlPath = url.getFile();

				Log.log(Log.LEVEL_TRACE, MODULE, "Trying protocol 2 for " + url);
				// Test GalleryComm2
				String urlPath2 = urlPath + ((urlPath.endsWith("/")) ? GalleryComm2.SCRIPT_NAME : "/" + GalleryComm2.SCRIPT_NAME);
				if (tryComm(su, mConnection, urlPath2, null)) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Server has protocol 2");
					return new GalleryComm2(g);	
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Trying protocol 2.5 for " + url);
				// Test GalleryComm2
				String urlPath2_5 = urlPath + ((urlPath.endsWith("/")) ? GalleryComm2_5.SCRIPT_NAME : "/" + GalleryComm2_5.SCRIPT_NAME);
				StringBuffer sb = new StringBuffer();
				if (tryComm(su, mConnection, urlPath2_5, sb)) {
					if (sb != null && sb.indexOf("ERROR_PERMISSION_DENIED") == -1) {
						Log.log(Log.LEVEL_TRACE, MODULE, "Server has protocol 2.5");
						return new GalleryComm2_5(g);
					} else {
						// G2 remote module is deactivated
						su.error(GRI18n.getString(MODULE, "g2.moduleDisabled"));
					}
				}

				/*Log.log(Log.LEVEL_TRACE, MODULE, "Trying protocol 1 for " + url);
				// Test GalleryComm1
				// BUT: only if first try was not status code 401 = authorization failure
				String scriptName = "gallery_remote.php";
				String urlPath1 = urlPath + ((urlPath.endsWith("/")) ? scriptName : "/" + scriptName);
				if (lastRespCode != 401 && tryComm(su, mConnection, urlPath1)) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Server has protocol 1");

					// todo: Alert, we don't support protocol 1 any more.
				}*/
			} else {
				// if Gallery is embedded, only support protocol 2
				return new GalleryComm2(g);
			}
		} catch (HTTPClient.ProtocolNotSuppException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}

		return null;
	}

	public static void addUserInfo(HTTPConnection conn, URL url) {
		String userInfo = url.getUserInfo();
		if (userInfo != null) {
			StringTokenizer st = new StringTokenizer(userInfo, ":");
			if (st.countTokens() == 2) {
				String username = st.nextToken();
				String password = st.nextToken();

				Log.log(Log.LEVEL_TRACE, MODULE, "Added basic auth params: " + username + " - " + password);

				AuthorizePopup.hackUsername = username;
				AuthorizePopup.hackPassword = password;

				return;
			}
		}
		
		AuthorizePopup.hackUsername = null;
		AuthorizePopup.hackPassword = null;
	}

	private static boolean tryComm(StatusUpdate su, HTTPConnection mConnection, String urlPath, StringBuffer content) {
		try {
			HTTPResponse rsp = null;

			if (content == null) {
				rsp = mConnection.Head(urlPath);
			} else {
				rsp = mConnection.Get(urlPath);
			}

			// handle 30x redirects
			// (and authorization failure)
			int rspCode = rsp.getStatusCode();   // try actual communcation
			lastRespCode = rspCode;
			if (rspCode >= 300 && rspCode < 400) {
				// retry, the library will have fixed the URL
				if (content == null) {
					rsp = mConnection.Head(urlPath);
				} else {
					rsp = mConnection.Get(urlPath);
				}
				rspCode = rsp.getStatusCode();
			}

			if (content != null) {
				content.append(rsp.getText());
			}

			Log.log(Log.LEVEL_TRACE, MODULE, "tryComm " + urlPath + ": " + rspCode);

			return rspCode == 200;
		} catch (UnknownHostException uhe) {
			su.error("Unknown host: " + mConnection.getHost());
		} catch (IOException ioe) {
			// we can't directly catch the SSLPeerUnverifiedException, because Java 1.3 barfs and prevents
			// loading this class at all. Instead, cast it inside another try-catch...
			try {
				if (ioe instanceof javax.net.ssl.SSLPeerUnverifiedException) {
					Log.logException(Log.LEVEL_ERROR, MODULE, ioe);

					JOptionPane.showMessageDialog((Component) su, GRI18n.getString(MODULE, "noAuth"), GRI18n.getString(MODULE, "error"), JOptionPane.ERROR_MESSAGE);
				} else {
					Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				}
			} catch (NoClassDefFoundError ncdfe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
			}
		} catch (ModuleException me) {
			Log.logException(Log.LEVEL_ERROR, MODULE, me);
		} catch (ParseException pe) {
			Log.logException(Log.LEVEL_ERROR, MODULE, pe);
		} catch (Exception e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}

		return false;
	}
}
