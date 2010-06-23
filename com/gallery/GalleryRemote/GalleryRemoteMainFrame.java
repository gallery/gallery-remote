package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.util.OsShutdown;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 14, 2004
 */
public class GalleryRemoteMainFrame extends GalleryRemote {
	private MainFrame mainFrame = null;

	public void createProperties() {
		super.createProperties();

		if (isAppletMode()) {
			properties = getAppletOverrides(properties, "GRDefault_");
		}

		File f = new File(System.getProperty("user.home")
				+ File.separator + ".GalleryRemote"
				+ File.separator);

		boolean created = f.mkdirs();

		File pf = new File(f, "GalleryRemote.properties");

		if (!pf.exists()) {
			try {
				pf.createNewFile();
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		// On unix os's try to chmod the properties file because it contains passwords
		// [ 1738472 ] [GR] Insecure permissions - user/password world readable
		if (created && OsShutdown.isUnix()) {
			try {
				Class c = GalleryRemote.secureClassForName("com.gallery.GalleryRemote.PrivateShutdown");
				Method m = c.getMethod("exec");
				m.invoke(null, "chmod -R go-rwx " + f.getPath().replaceAll(" ", "\\ "));
			} catch (Throwable e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		properties = new PropertiesFile(properties, pf.getPath(), "user");

		if (isAppletMode()) {
			properties = getAppletOverrides(properties, "GROverride_");
		}
	}

	protected void initializeGR() {
		super.initializeGR();

		Log.startLog(properties.getIntProperty(PreferenceNames.LOG_LEVEL),
				properties.getBooleanProperty("toSysOut") ||
				(System.getProperty("lax.stdout.redirect") != null && System.getProperty("lax.stdout.redirect").length() > 0));

		try {
			if (isAppletMode() || !Update.upgrade()) {
				mainFrame = new MainFrame();
				CoreUtils.initCore();
				mainFrame.initMainFrame();
			} else {
				Log.shutdown();
				System.exit(0);
			}
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, "Startup", e);
			Log.shutdown();
		}

		if (!isAppletMode()) {
			new Thread() {
				public void run() {
					Update update = new Update();
					update.check(true);
				}
			}.start();
		}
	}

	public Frame getMainFrame() {
		return mainFrame;
	}

	public GalleryRemoteCore getCore() {
		return mainFrame;
	}

	protected void loadIcons() {
		super.loadIcons();

		try {
			iAbout = new ImageIcon(GalleryRemote.class.getResource("/Information16.gif"));
			iSave = new ImageIcon(GalleryRemote.class.getResource("/Save16.gif"));
			iOpen = new ImageIcon(GalleryRemote.class.getResource("/Open16.gif"));
			iPreferences = new ImageIcon(GalleryRemote.class.getResource("/Preferences16.gif"));
			iQuit = new ImageIcon(GalleryRemote.class.getResource("/Stop16.gif"));
			iCut = new ImageIcon(GalleryRemote.class.getResource("/Cut16.gif"));
			iCopy = new ImageIcon(GalleryRemote.class.getResource("/Copy16.gif"));
			iPaste = new ImageIcon(GalleryRemote.class.getResource("/Paste16.gif"));

			iNewGallery = new ImageIcon(GalleryRemote.class.getResource("/WebComponentAdd16.gif"));
			iLogin = new ImageIcon(GalleryRemote.class.getResource("/WebComponent16.gif"));
			iNewAlbum = new ImageIcon(GalleryRemote.class.getResource("/New16.gif"));
			iNew = iNewAlbum;

			iUp = new ImageIcon(GalleryRemote.class.getResource("/Up16.gif"));
			iDown = new ImageIcon(GalleryRemote.class.getResource("/Down16.gif"));
			iDelete = new ImageIcon(GalleryRemote.class.getResource("/Delete16.gif"));
			iRight = new ImageIcon(GalleryRemote.class.getResource("/RotateRight24.gif"));
			iLeft = new ImageIcon(GalleryRemote.class.getResource("/RotateLeft24.gif"));
			iFlip = new ImageIcon(GalleryRemote.class.getResource("/FlipHoriz24.gif"));
		} catch (Exception e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}
}
