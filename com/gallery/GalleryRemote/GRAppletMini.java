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
import java.net.MalformedURLException;

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
	StatusBar jStatusBar;
	JScrollPane jScrollPane;
	DroppableList jPicturesList;

	DefaultComboBoxModel galleries = null;
	Album album = null;
	Gallery gallery = null;
	boolean inProgress = false;
	boolean hasHadPictures = false;

	public GRAppletMini() {
		coreClass = "com.gallery.GalleryRemote.GalleryRemoteMini";
	}

	public void initUI() {
		// update the look and feel
		SwingUtilities.updateComponentTreeUI(this);
		if (GalleryRemote.IS_MAC_OS_X) {
			// the default font for many components is too big on Mac
			UIManager.put("Label.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("TextField.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("Button.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("CheckBox.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("ComboBox.font", UIManager.getFont("TitledBorder.font"));
		}

		jbInit();
	}

	public void startup() {
		galleries = new DefaultComboBoxModel();
		AppletInfo info = getGRAppletInfo();

		gallery = info.gallery;

		galleries.addElement(gallery);

		//gallery.fetchAlbums(getMainStatusUpdate(), false);

		//album = gallery.getAlbumByName(info.albumName);

		ImageUtils.deferredTasks();

		album = new Album(gallery);
		album.setName(info.albumName);
		gallery.addAlbum(album);

		jPicturesList.setModel(album);
		jPicturesList.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		jPicturesList.setInputMap(JComponent.WHEN_FOCUSED, null);
		jPicturesList.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		jPicturesList.setCellRenderer(new CoreUtils.FileCellRenderer());

		//jStatusBar.setStatus(GRI18n.getString(MODULE, "DefMessage"));
		jStatusBar.setStatus(GRI18n.getString("MainFrame", "selPicToAdd"));
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

	public DefaultComboBoxModel getGalleries() {
		return galleries;
	}

	public void thumbnailLoadedNotify() {}

	public void setInProgress(boolean inProgress) {
		jUpload.setEnabled(!inProgress);
		jAdd.setEnabled(!inProgress);
		jPicturesList.setEnabled(!inProgress);

		this.inProgress = inProgress;

		if (! inProgress && hasHadPictures) {
			// probably finished uploading...
			try {
				getAppletContext().showDocument(new URL(getCodeBase().toString() + "add_photos_refresh.php"), "hack");
			} catch (MalformedURLException e1) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e1);
			}

			hasHadPictures = false;
		}
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

	protected void jbInit() {
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
			Log.log(Log.LEVEL_TRACE, MODULE, "Key pressed: " + vKey);

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
				hasHadPictures = true;
			}
		} else if (e.getSource() == jUpload) {
			gallery.uploadFiles(new UploadProgress(DialogUtil.findParentWindow(this)));
		}
	}
}
