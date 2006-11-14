package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.ImageLoaderUtil;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Picture;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.net.URL;

import org.jdesktop.jdic.screensaver.ScreensaverContext;
import org.jdesktop.jdic.screensaver.ScreensaverSettings;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 14, 2004
 */
public class GalleryRemoteScreenSaver
		extends GalleryRemote
		implements GalleryRemoteCore, PreferenceNames, ListDataListener, ImageLoaderUtil.ImageLoaderUser {
	DefaultComboBoxModel galleries = null;
	Gallery gallery;
	Album album;
	DroppableList jPicturesList;
	StatusUpdateAdapter statusUpdate = new StatusUpdateAdapter();
	ScreensaverContext context;
	Picture currentPicture = null;
	File currentImage = null;
	ImageLoaderUtil loader = new ImageLoaderUtil(3, this);
	Dimension size = null;
	boolean newImage = false;
	ArrayList picturesList = null;
	int delay = 5000;
	boolean hasSettings = true;

	protected void initializeGR() {
		super.initializeGR();

		CoreUtils.initCore();

		Log.startLog(_().properties.getIntProperty(PreferenceNames.LOG_LEVEL), _().properties.getBooleanProperty("toSysOut"));

		startup();
	}

	public void setContext(ScreensaverContext context) {
		this.context = context;
	}

	public void createProperties() {
		super.createProperties();

		File f = new File(System.getProperty("user.home")
				+ File.separator + ".GalleryRemote"
				+ File.separator);

		f.mkdirs();

		File pf = new File(f, "GalleryRemoteScreenSaver.properties");

		if (!pf.exists()) {
			try {
				pf.createNewFile();
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		properties = new PropertiesFile(properties, pf.getPath(), "user");
	}

	public Frame getMainFrame() {
		return null;
	}

	public GalleryRemoteCore getCore() {
		return this;
	}

	protected void loadIcons() {}

	public void startup() {
		ScreensaverSettings settings = context.getSettings();
		//settings.loadSettings("Gallery");
		Log.log(Log.LEVEL_TRACE, MODULE, "Screensaver settings: " + settings.getProperties().toString());

		Properties p = settings.getProperties();

		galleries = new DefaultComboBoxModel();

		gallery = new Gallery(GalleryRemote._().getCore().getMainStatusUpdate());
		String curl = settings.getProperty("curl");

		if (curl != null) {
			try {
				p.load(new URL(curl).openStream());
			} catch (IOException e) {
				Log.log(Log.LEVEL_CRITICAL, MODULE, "Error trying to get configuration file: " + curl);
				Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			}

			Log.log(Log.LEVEL_TRACE, MODULE, "Fetched settings: " + settings.getProperties().toString());
		}

		String url = p.getProperty("url");
		if (url != null) {
			gallery.setStUrlString(url);
			if (p.getProperty("username") == null || p.getProperty("username").trim().length() == 0) {
				gallery.cookieLogin = true;
			} else {
				gallery.setUsername(p.getProperty("username"));
				gallery.setPassword(p.getProperty("password"));
			}
			gallery.setType(Gallery.TYPE_STANDALONE);

			properties.setBooleanProperty(SLIDESHOW_RECURSIVE, p.getProperty("recursive") != null);
			properties.setBooleanProperty(SLIDESHOW_LOWREZ, p.getProperty("hires") == null);
			properties.setBooleanProperty(SLIDESHOW_NOSTRETCH, p.getProperty("stretch") == null);
			delay = Integer.parseInt(p.getProperty("delay")) * 1000;
			String albums = p.getProperty("album");
			String[] albumsA = albums.split(",");

			galleries.addElement(gallery);
			ImageUtils.deferredTasks();

			album = new Album(gallery);
			album.setName(albumsA[new Random().nextInt(albumsA.length)]);
			album.addListDataListener(this);

			album.fetchAlbumImages(statusUpdate,
					GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_RECURSIVE),
					200, true);
		} else {
			hasSettings = false;
		}
	}

	public void nextPicture() {
		if (GalleryRemote._() == null) {
			return;
		}

		if (picturesList == null || picturesList.size() == 0) {
			picturesList = new ArrayList(album.getPicturesList());
		}

		Picture p = (Picture) picturesList.get((int) Math.floor(Math.random() * picturesList.size()));
		picturesList.remove(p);

		loader.preparePicture(p, true, true);
	}

	public void shutdown() {
		if (GalleryRemote._() != null) {
			GalleryRemote.shutdownInstance();
		}
	}

	public void shutdown(boolean shutdownOs) {
		shutdown();
	}

	public void flushMemory() {}

	public void preloadThumbnails(Iterator pictures) {}

	public Image getThumbnail(Picture p) {
		return null;
	}

	public StatusUpdate getMainStatusUpdate() {
		return statusUpdate;
	}

	public DefaultComboBoxModel getGalleries() {
		return galleries;
	}

	public void thumbnailLoadedNotify() {}

	public void setInProgress(boolean inProgress) {}

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

	public void contentsChanged(ListDataEvent e) {
		if (album.isHasFetchedImages()) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Done downloading album info");

			nextPicture();
		}
	}

	public void intervalAdded(ListDataEvent e) {}

	public void intervalRemoved(ListDataEvent e) {}

	public void pictureReady() {
		Log.log(Log.LEVEL_TRACE, MODULE, "PictureReady, letting screensaver thread update");
		newImage = true;

		new Thread() {
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				nextPicture();
			}
		}.start();
	}

	public boolean blockPictureReady(Image image, Picture picture) {
		return false;
	}

	public Dimension getImageSize() {
		if (size == null) {
			size = context.getComponent().getBounds().getSize();
		}

		return size;
	}

	public void nullRect() {}
	public void pictureStartDownloading(Picture picture) {}
	public void pictureStartProcessing(Picture picture) {}
	public void pictureLoadError(Picture picture) {}
}
