package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.DialogUtil;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 9, 2003
 */
public class SlideshowFrame extends PreviewFrame implements Runnable {
	List pictures = null;
	int sleepTime = 3000;

	public SlideshowFrame() {
		setUndecorated(true);
		setResizable(false);

		try {
			// Java 1.4 only
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			gd.setFullScreenWindow(this);

			//setBounds(500, 20, 200, 200);
			//show();
		} catch (NoSuchMethodError e) {
			DialogUtil.maxSize(this);
			show();
		}

		initComponents();
	}

	public void initComponents() {
		previewCacheSize = 3;
		addMouseListener(new MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
				next();
			}
		});

		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() > 0) {
					next();
				} else {
					previous();
				}
			}
		});
	}

	public void start(List pictures) {
		this.pictures = pictures;

		new Thread(this).start();
	}

	public void run() {
		while (true) {
			long time = System.currentTimeMillis();

			if (!next()) {
				break;
			}

			try {
				long sleep = sleepTime - (System.currentTimeMillis() - time);

				if (sleep > 0) {
					Thread.sleep(sleep);
				}
			} catch (InterruptedException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		hide();
	}

	public boolean next() {
		int index = -1;

		if (currentPicture != null) {
			index = pictures.indexOf(currentPicture);
		}

		index++;

		if (index >= pictures.size()) {
			return false;
		}

		displayPicture((Picture) pictures.get(index), false);

		if (++index < pictures.size() && imageIcons.get(pictures.get(index)) == null) {
			previewLoader.loadPreview((Picture) pictures.get(index));
		}

		return true;
	}

	public boolean previous() {
		int index = -1;

		if (currentPicture == null) {
			return false;
		}

		index = pictures.indexOf(currentPicture);

		index--;

		if (index < 0) {
			return false;
		}

		displayPicture((Picture) pictures.get(index), false);

		if (--index > 0 && imageIcons.get(pictures.get(index)) == null) {
			previewLoader.loadPreview((Picture) pictures.get(index));
		}

		return true;
	}

	public void hide() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		gd.setFullScreenWindow(null);

		super.hide();
	}
}
