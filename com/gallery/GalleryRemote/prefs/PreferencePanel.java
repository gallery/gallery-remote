package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.MainFrame;

import javax.swing.*;

/**
 * Interface for Preference Panels
 * User: paour
 * Date: May 8, 2003
 */
public abstract class PreferencePanel extends JPanel {
	protected JPanel panel = new JPanel();
	protected boolean hasBeenRead = false;
	MainFrame mainFrame = null;
	JDialog dialog = null;

	public abstract JLabel getIcon();

	public boolean isReversible() {
		return true;
	}

	public abstract void buildUI();

	public void readPropertiesFirst(GalleryProperties props) {
		if (!hasBeenRead) {
			hasBeenRead = true;
			readProperties(props);
		}
	}

	public abstract void readProperties(GalleryProperties props);

	public abstract void writeProperties(GalleryProperties props);

	public boolean hasBeenRead() {
		return hasBeenRead;
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}
}
