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
	private JLabel jLabel;

	public void init() {
		jLabel = new JLabel("<HTML>The Gallery Remote applet is running. Please don't close this window or navigate away!</HTML>");
		getContentPane().add(jLabel);
	}

	public void start() {
		GalleryRemote.setProperties();

		if (GalleryRemote.createInstance(true, this) == null) {
			JOptionPane.showMessageDialog(DialogUtil.findParentWindow(this),
					"Only one instance of the Gallery Remote can run at the same time...",
					"Error", JOptionPane.ERROR_MESSAGE);

			return;
		}

		// update the look and feel
		SwingUtilities.updateComponentTreeUI(this);
	}

	public void stop() {
		GalleryRemote._().getCore().shutdown();
	}

	public void hasShutdown() {
		jLabel.setText("<HTML>The Gallery Remote applet has stopped, you can navigate away or close the window</HTML>");
	}
}
