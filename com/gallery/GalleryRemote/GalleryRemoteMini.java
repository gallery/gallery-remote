package com.gallery.GalleryRemote;

import javax.swing.*;

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

	public JFrame getMainFrame() {
		//throw new IllegalStateException("This method should not be called in mini mode");
		return null;
	}

	public GalleryRemoteCore getCore() {
		return (GRAppletMini) applet;
	}
}
