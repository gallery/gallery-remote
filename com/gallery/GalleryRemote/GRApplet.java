package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.model.Gallery;

import javax.swing.*;
import java.applet.Applet;
import java.io.FilePermission;
import java.net.URL;
import java.net.MalformedURLException;

import HTTPClient.CookieModule;
import HTTPClient.Cookie;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Oct 30, 2003
 */
public class GRApplet extends JApplet {
	public static final String MODULE = "GRApplet";

	protected JLabel jLabel;
	boolean hasStarted = false;
	String coreClass = "com.gallery.GalleryRemote.GalleryRemoteMainFrame";

	public void init() {
		System.out.println("Applet init");
	}

	public void start() {
		System.out.println("Applet start");
		initGalleryRemote();

		if (hasStarted) {
			GalleryRemote._().initializeGR();
			initUI();

			new Thread() {
				public void run() {
					GalleryRemote._().runGR();
				}
			}.start();
		} else {
			initDummyUI();
		}
	}

	protected void initUI() {
		jLabel = new JLabel("<HTML><CENTER>The Gallery Remote applet is running. Please don't close this window or navigate away!</CENTER></HTML>");
		getContentPane().add(jLabel);
	}

	protected void initDummyUI() {
		jLabel = new JLabel("<HTML><CENTER>The Gallery Remote applet is not running because another is running in the same browser</CENTER></HTML>");
		getContentPane().add(jLabel);
	}

	protected void initGalleryRemote() {
		if (! GalleryRemote.createInstance(coreClass, this)) {
			JOptionPane.showMessageDialog(DialogUtil.findParentWindow(this),
					"Only one instance of the Gallery Remote can run at the same time...",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			hasStarted = true;
		}
	}

	public void stop() {
		System.out.println("Applet stop");
		// don't shutdown the applet if it didn't start...
		if (hasStarted && GalleryRemote._() != null) {
			GalleryRemote._().getCore().shutdown();
		}

		ImageUtils.purgeTemp();

		Log.log(Log.LEVEL_INFO, MODULE, "Shutting down log");
		Log.shutdown();
	}

	public void hasShutdown() {
		jLabel.setText("<HTML><CENTER>The Gallery Remote applet has stopped, you can navigate away or close the window</CENTER></HTML>");
	}

	protected AppletInfo getGRAppletInfo() {
		AppletInfo info = new AppletInfo();

		info.gallery = new Gallery(GalleryRemote._().getCore().getMainStatusUpdate());

		String url = getParameter("gr_url");
		String cookieName = getParameter("gr_cookie_name");
		String cookieValue = getParameter("gr_cookie_value");
		String cookieDomain = getParameter("gr_cookie_domain");
		String cookiePath = getParameter("gr_cookie_path");

		info.albumName = getParameter("gr_album");

		Log.log(Log.LEVEL_TRACE, MODULE, "Applet parameters: ");
		Log.log(Log.LEVEL_TRACE, MODULE, "gr_url: " + url);
		Log.log(Log.LEVEL_TRACE, MODULE, "gr_cookie_name: " + cookieName);
		Log.log(Log.LEVEL_TRACE, MODULE, "gr_cookie_domain: " + cookieDomain);
		Log.log(Log.LEVEL_TRACE, MODULE, "gr_cookie_path: " + cookiePath);
		Log.log(Log.LEVEL_TRACE, MODULE, "gr_album: " + info.albumName);

		if (cookieDomain == null || cookieDomain.length() < 1) {
			try {
				cookieDomain = new URL(url).getHost();
			} catch (MalformedURLException e) {
				//Log.logException(Log.LEVEL_ERROR, MODULE, e);
				URL documentBase = getDocumentBase();
				cookieDomain = documentBase.getHost();

				Log.log(Log.LEVEL_INFO, MODULE, "URL probably doesn't have a host part because the Gallery " +
						"is in (unsupported) relative mode. Using the Applet documentBase: " + cookieDomain);

				try {
					url = new URL(documentBase.getProtocol(), documentBase.getHost(), documentBase.getPort(),
							url).toString();
				} catch (MalformedURLException e1) {
					Log.logException(Log.LEVEL_ERROR, MODULE, e1);
				}
			}
		}

		info.gallery.setType(Gallery.TYPE_STANDALONE);
		info.gallery.setStUrlString(url);
		info.gallery.cookieLogin = true;

		CookieModule.addCookie(new Cookie(cookieName, cookieValue, cookieDomain, cookiePath, null, false));

		return info;
	}

	class AppletInfo {
		String albumName;
		Gallery gallery;
	}
}
