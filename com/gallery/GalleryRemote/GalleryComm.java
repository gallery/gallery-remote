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

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.RequestUserAgent;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

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

	DefaultHttpClient httpclient;
	Gallery g;
	StatusUpdate su;

	/* -------------------------------------------------------------------------
	 * STATIC INITIALIZATON
	 */

	static {
		/* Enable customized AuthorizePopup */
		AuthorizePopup.enable();


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

	protected GalleryComm(Gallery g, StatusUpdate su) {
		if (g == null) {
			throw new IllegalArgumentException("Must supply a non-null gallery.");
		}

		this.g = g;
		this.su = su;

		SingleClientConnManager cm = null;

		if (g.getUrl().getProtocol().equals("https")) {
			try {
				SSLSocketFactory sf = new SSLSocketFactory(SSLContext.getInstance("TLS"), SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("https", sf, 443));
				cm = new SingleClientConnManager(schemeRegistry);
			} catch (NoSuchAlgorithmException e) {
				Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			}
		}

		httpclient = new DefaultHttpClient(cm);

		ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
				httpclient.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault());
		httpclient.setRoutePlanner(routePlanner);

		httpclient.removeRequestInterceptorByClass(RequestUserAgent.class);
		final String ua = g.getUserAgent();
		httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				request.setHeader(HTTP.USER_AGENT, ua);
			}
		});

	}

	/**
	 * Causes the GalleryComm instance to upload the pictures in the
	 * associated Gallery to the server.
	 * @param su an instance of StatusUpdate
	 * @param async whether to run the action asynchronously
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
	public void albumProperties(StatusUpdate su, Album a, boolean async) {
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

    public void fetchAlbumImages(StatusUpdate su, Album a, boolean recusive, boolean async, int maxPictures, boolean random) {
        throw new RuntimeException("This method is not available on this protocol");
    }

	public boolean moveAlbum(StatusUpdate su, Album a, Album newParent, boolean async) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	public void login(StatusUpdate su) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	public void incrementViewCount(StatusUpdate su, Picture p) {
		throw new RuntimeException("This method is not available on this protocol");
	}

	public void logOut() {
		Log.log(Log.LEVEL_INFO, MODULE, "Logging out and clearing cookies");
		isLoggedIn = false;
		httpclient.getCookieStore().clear();
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
		return lastRespCode == 401;
	}

	public static GalleryComm getCommInstance(StatusUpdate su, Gallery g) {
		return new GalleryComm3(g, su);
	}

	public static void addUserInfo(URL url) {
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

	/*private static boolean tryComm(StatusUpdate su, HttpClient mConnection, String urlPath, StringBuffer content) {
		try {
			HTTPResponse rsp;

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
	}*/
}