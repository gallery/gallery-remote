package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.DialogUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.applet.Applet;
import java.io.FilePermission;
import java.io.File;
import java.util.Iterator;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import HTTPClient.CookieModule;
import HTTPClient.Cookie;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Oct 30, 2003
 */
public class GRAppletMini extends GRApplet implements GalleryRemoteCore, ActionListener {
	public static final String MODULE = "AppletMini";

	JButton jUpload;
	JButton jAdd;
	//StatusUpdate status;
	StatusBar jStatusBar;
	JScrollPane jScrollPane;
	DroppableList jPicturesList;

	DefaultComboBoxModel galleries = null;
	Album album = null;
	Gallery gallery = null;
	boolean inProgress = false;

	public GRAppletMini() {
		coreClass = "com.gallery.GalleryRemote.GalleryRemoteMini";
	}

	public void initUI() {
		// update the look and feel
		SwingUtilities.updateComponentTreeUI(this);

		jbInit();

		initAppletParamsAndLogin();

		jPicturesList.setModel(album);
		jPicturesList.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		jPicturesList.setInputMap(JComponent.WHEN_FOCUSED, null);
		jPicturesList.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		jPicturesList.setCellRenderer(new CoreUtils.FileCellRenderer());

		//jStatusBar.setStatus(GRI18n.getString(MODULE, "DefMessage"));
		jStatusBar.setStatus(GRI18n.getString("MainFrame", "selPicToAdd"));
	}

	protected void initAppletParamsAndLogin() {
		galleries = new DefaultComboBoxModel();
		gallery = new Gallery(getMainStatusUpdate());
		String url = getParameter("gr_url");
		String cookieName = getParameter("gr_cookie_name");
		String cookieValue = getParameter("gr_cookie_value");
		String cookieDomain = getParameter("gr_cookie_domain");
		String cookiePath = getParameter("gr_cookie_path");
		String albumName = getParameter("gr_album");

		if (cookieDomain == null || cookieDomain.length() < 1) {
			try {
				cookieDomain = new URL(url).getHost();
			} catch (Exception e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		gallery.setType(Gallery.TYPE_STANDALONE);
		gallery.setStUrlString(url);
		gallery.cookieLogin = true;
		galleries.addElement(gallery);

		CookieModule.addCookie(new Cookie(cookieName, cookieValue, cookieDomain, cookiePath, null, false));

		gallery.fetchAlbums(getMainStatusUpdate(), false);

		album = gallery.getAlbumByName(albumName);

		ImageUtils.deferredTasks();
	}

	public void shutdown() {
		if (hasStarted) {
			GalleryRemote.shutdownInstance();
		}
	}

	public void shutdown(boolean shutdownOs) {
		shutdown();
	}

	public void flushMemory() {}

	public void preloadThumbnails(Iterator pictures) {}

	public ImageIcon getThumbnail(Picture p) {
		return null;
	}

	public StatusUpdate getMainStatusUpdate() {
		return jStatusBar;
	}

	public ListModel getGalleries() {
		return galleries;
	}

	public void thumbnailLoadedNotify() {}

	public void setInProgress(boolean inProgress) {
		jUpload.setEnabled(!inProgress);
		jPicturesList.setEnabled(!inProgress);

		this.inProgress = inProgress;
	}

	public void addPictures(File[] files, int index, boolean select) {
		album.addPictures(files, index);
	}

	public void addPictures(Picture[] pictures, int index, boolean select) {
		album.addPictures(Arrays.asList(pictures), index);
	}

	public Album getCurrentAlbum() {
		return album;
	}

	public JList getPicturesList() {
		return jPicturesList;
	}

	private void jbInit() {
		jUpload = new JButton();
		jAdd = new JButton();
		jStatusBar = new StatusBar(75);
		jScrollPane = new JScrollPane();
		jPicturesList = new DroppableList();

		jScrollPane.setBorder(new TitledBorder(BorderFactory.createEmptyBorder(), GRI18n.getString(MODULE, "pictures")));
		jScrollPane.getViewport().add(jPicturesList, null);

		jUpload.setText(GRI18n.getString(MODULE, "Upload"));
		jAdd.setText(GRI18n.getString(MODULE, "Add"));

		JPanel jButtonPanel = new JPanel();
		jButtonPanel.setLayout(new GridLayout(1, 2, 5, 0));
		jButtonPanel.add(jAdd);
		jButtonPanel.add(jUpload);

		this.getContentPane().setLayout(new GridBagLayout());
		this.getContentPane().add(jButtonPanel,       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		this.getContentPane().add(jStatusBar,    new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.getContentPane().add(jScrollPane,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		jAdd.addActionListener(this);
		jUpload.addActionListener(this);

		jPicturesList.addKeyListener(
				new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						jListKeyPressed(e);
					}
				});
	}

	public void jListKeyPressed(KeyEvent e) {
		if (!inProgress) {
			int vKey = e.getKeyCode();

			switch (vKey) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
					CoreUtils.deleteSelectedPictures();
					break;
				case KeyEvent.VK_LEFT:
					CoreUtils.movePicturesUp();
					break;
				case KeyEvent.VK_RIGHT:
					CoreUtils.movePicturesDown();
					break;
				case KeyEvent.VK_UP:
					CoreUtils.selectPrevPicture();
					break;
				case KeyEvent.VK_DOWN:
					CoreUtils.selectNextPicture();
					break;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jAdd) {
			jStatusBar.setStatus(GRI18n.getString("MainFrame", "selPicToAdd"));
			File[] files = AddFileDialog.addFiles(this);

			if (files != null) {
				addPictures(files, -1, false);
			}
		} else if (e.getSource() == jUpload) {
			gallery.uploadFiles(new UploadProgress(DialogUtil.findParentWindow(this)));
		}
	}
}
