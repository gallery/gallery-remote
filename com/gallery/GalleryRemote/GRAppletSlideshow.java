package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.prefs.SlideshowPanel;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.border.TitledBorder;
import java.applet.Applet;
import java.io.FilePermission;
import java.io.File;
import java.util.Iterator;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import HTTPClient.CookieModule;
import HTTPClient.Cookie;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Oct 30, 2003
 */
public class GRAppletSlideshow extends GRAppletMini implements GalleryRemoteCore, ActionListener, ListDataListener,
		PreferenceNames {
	public static final String MODULE = "AppletSlideshow";
	JButton jStart;
	SlideshowPanel jSlidePanel;
	SlideshowFrame slideshowFrame = null;

	public GRAppletSlideshow() {
		coreClass = "com.gallery.GalleryRemote.GalleryRemoteMini";
	}

	public void startup() {
		galleries = new DefaultComboBoxModel();
		AppletInfo info = getGRAppletInfo();

		gallery = info.gallery;

		galleries.addElement(gallery);
		ImageUtils.deferredTasks();

		album = new Album(gallery);
		album.setName(info.albumName);
		album.addListDataListener(this);

		album.fetchAlbumImages(jStatusBar,
				GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_RECURSIVE, true), 
				GalleryRemote._().properties.getIntProperty(SLIDESHOW_MAX_PICTURES, 0));
	}

	protected void jbInit() {
		getContentPane().setLayout(new GridBagLayout());

		jStart = new JButton(GRI18n.getString(MODULE, "Start"));

		jStatusBar = new StatusBar(75);

		jSlidePanel = new SlideshowPanel();

		getContentPane().add(jSlidePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(new JLabel(GRI18n.getString(MODULE, "Disabled")), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 3, 0, 3), 0, 0));
		getContentPane().add(jStart, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		getContentPane().add(jStatusBar, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		jSlidePanel.buildUI();
		jSlidePanel.remove(jSlidePanel.spacerPanel);
		jSlidePanel.readProperties(GalleryRemote._().properties);
		jStart.addActionListener(this);
		jStart.setEnabled(false);

		jPicturesList = new DroppableList();
	}

	public void setInProgress(boolean inProgress) {
		jStart.setEnabled(!inProgress);

		this.inProgress = inProgress;
	}

	public void actionPerformed(ActionEvent e) {
		jSlidePanel.writeProperties(GalleryRemote._().properties);

		if (slideshowFrame == null) {
			slideshowFrame = new SlideshowFrame();
		}

		slideshowFrame.showSlideshow();
		slideshowFrame.start(getCurrentAlbum().getPicturesList());

		// null slideshowFrame so that next time the user clicks the button
		// they get a black one, in case they changed positioning
		slideshowFrame = null;
	}

	public void shutdown() {
		if (hasStarted) {
			jSlidePanel.writeProperties(GalleryRemote._().properties);
			GalleryRemote._().properties.write();
		}

		super.shutdown();
	}

	public void contentsChanged(ListDataEvent e) {
		// most likely, pictures were just added to the album. Preload the first one.
		new Thread() {
			public void run() {
				slideshowFrame = new SlideshowFrame();
				if (album.getPicturesList().size() > 0) {
					ImageUtils.download((Picture) album.getPicturesList().get(0), getGraphicsConfiguration().getBounds().getSize(), GalleryRemote._().getCore().getMainStatusUpdate(), null);
				}
			}
		}.start();
	}

	public void intervalAdded(ListDataEvent e) {}

	public void intervalRemoved(ListDataEvent e) {}

}
