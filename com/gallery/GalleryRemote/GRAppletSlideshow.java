package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;

import javax.swing.*;
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
public class GRAppletSlideshow extends GRAppletMini implements GalleryRemoteCore, ActionListener {
	public static final String MODULE = "AppletSlideshow";
	JButton jStart;

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

		album.fetchAlbumImages(jStatusBar);
	}

	protected void jbInit() {
		jStart = new JButton(GRI18n.getString(MODULE, "Start"));
		jStart.addActionListener(this);
		getContentPane().add("Center", jStart);

		jStatusBar = new StatusBar(75);
		getContentPane().add("South", jStatusBar);

		jPicturesList = new DroppableList();
	}

	public void setInProgress(boolean inProgress) {
		jStart.setEnabled(!inProgress);

		this.inProgress = inProgress;
	}

	public void actionPerformed(ActionEvent e) {
		new SlideshowFrame().start(getCurrentAlbum().getPicturesList());
	}
}
