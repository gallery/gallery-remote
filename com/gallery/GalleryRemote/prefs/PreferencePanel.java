package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.GalleryProperties;

import javax.swing.*;

/**
 * Interface for Preference Panels
 * User: paour
 * Date: May 8, 2003
 */
public abstract class PreferencePanel extends JPanel {
	public JPanel panel = new JPanel();

	public abstract JLabel getIcon();

	public abstract void buildUI();

	public abstract void readProperties(GalleryProperties props);

	public abstract void writeProperties(GalleryProperties props);
}
