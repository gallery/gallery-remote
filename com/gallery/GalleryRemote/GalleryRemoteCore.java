package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.model.Album;

import javax.swing.*;
import java.util.Iterator;
import java.io.File;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 14, 2004
 */
public interface GalleryRemoteCore {
	public void startup();

	public void shutdown();
	public void shutdown(boolean shutdownOs);

	public void flushMemory();
	public void preloadThumbnails(Iterator pictures);
	public Image getThumbnail(Picture p);
	public StatusUpdate getMainStatusUpdate();

	public void thumbnailLoadedNotify();
	public void setInProgress(boolean inProgress);

	public void addPictures(File[] files, int index, boolean select);
	public void addPictures(Picture[] pictures, int index, boolean select);

	public DefaultComboBoxModel getGalleries();
	public Album getCurrentAlbum();

	public JList getPicturesList();
}
