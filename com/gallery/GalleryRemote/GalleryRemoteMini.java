package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 14, 2004
 */
public class GalleryRemoteMini extends GalleryRemote {
	protected void run() {
		super.run();
		
		Log.setMaxLevel();
	}

	public Frame getMainFrame() {
		return DialogUtil.findParentWindow(applet);
	}

	public GalleryRemoteCore getCore() {
		return (GalleryRemoteCore) applet;
	}
}
