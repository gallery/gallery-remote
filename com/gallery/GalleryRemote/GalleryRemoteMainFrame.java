package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.prefs.PropertiesFile;

import javax.swing.*;
import java.io.File;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 14, 2004
 */
public class GalleryRemoteMainFrame extends GalleryRemote {
	private MainFrame mainFrame = null;

	protected GalleryRemoteMainFrame() {
		super();

		File f = new File(System.getProperty("user.home")
				+ File.separator + ".GalleryRemote"
				+ File.separator);

		f.mkdirs();

		properties = new PropertiesFile(defaults, f.getPath()
				+ File.separator + "GalleryRemote");
	}

	protected void initializeGR() {
		super.initializeGR();

		try {
			if (isAppletMode() || !Update.upgrade()) {
				mainFrame = new MainFrame();
				//mainFrame.initComponents();
			} else {
				Log.shutdown();
				System.exit(0);
			}
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, "Startup", e);
			Log.shutdown();
		}

		if (!isAppletMode()) {
			Update update = new Update();
			update.check(true);
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
