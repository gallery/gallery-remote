package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 14, 2004
 */
public class GalleryRemoteMini extends GalleryRemote {
	protected void initializeGR() {
		super.initializeGR();

		CoreUtils.initCore();

		Log.startLog(_().properties.getIntProperty(PreferenceNames.LOG_LEVEL), _().properties.getBooleanProperty("toSysOut"));
	}

	public void createProperties() {
		super.createProperties();

		properties = getAppletOverrides(properties, "GRDefault_");

		File f = new File(System.getProperty("user.home")
				+ File.separator + ".GalleryRemote"
				+ File.separator);

		f.mkdirs();

		File pf = new File(f, "GalleryRemoteApplet.properties");

		if (!pf.exists()) {
			try {
				pf.createNewFile();
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		properties = new PropertiesFile(properties, pf.getPath(), "user");

		properties = getAppletOverrides(properties, "GROverride_");
	}

	public Frame getMainFrame() {
		return DialogUtil.findParentWindow(applet);
	}

	public GalleryRemoteCore getCore() {
		return (GalleryRemoteCore) applet;
	}
}
