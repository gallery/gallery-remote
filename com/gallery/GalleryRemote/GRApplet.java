package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;

import javax.swing.*;
import java.applet.Applet;
import java.io.FilePermission;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Oct 30, 2003
 */
public class GRApplet extends JApplet {
	protected JLabel jLabel;
	boolean hasStarted = false;
	String coreClass = "com.gallery.GalleryRemote.GalleryRemoteMainFrame";

	public void init() {}

	public void start() {
		initGalleryRemote();

		if (hasStarted) {
			//SwingUtilities.invokeLater(new Runnable() {
			new Thread() {
				public void run() {
					initUI();
				}
			//});
			}.start();
		} else {
			initDummyUI();
		}
	}

	protected void initUI() {
		jLabel = new JLabel("<HTML>The Gallery Remote applet is running. Please don't close this window or navigate away!</HTML>");
		getContentPane().add(jLabel);
	}

	protected void initDummyUI() {
		jLabel = new JLabel("<HTML>The Gallery Remote applet is not running because another is running in the same browser</HTML>");
		getContentPane().add(jLabel);
	}

	protected void initGalleryRemote() {
		if (GalleryRemote.createInstance(coreClass, this) == null) {
			JOptionPane.showMessageDialog(DialogUtil.findParentWindow(this),
					"Only one instance of the Gallery Remote can run at the same time...",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			hasStarted = true;
		}
	}

	public void stop() {
		// don't shutdown the applet if it didn't start...
		if (hasStarted) {
			GalleryRemote._().getCore().shutdown();
		}
	}

	public void hasShutdown() {
		jLabel.setText("<HTML>The Gallery Remote applet has stopped, you can navigate away or close the window</HTML>");
	}
}
