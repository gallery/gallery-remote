package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.DialogUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 9, 2003
 */
public class SlideshowFrame extends PreviewFrame implements Runnable {
	public static final String MODULE = "SlideFrame";
	List pictures = null;
	int sleepTime = 3000;
	boolean running = false;

	JLabel jCaption = new JLabel();
	JLabel jProgress = new JLabel();
	JLabel jExtra = new JLabel();
	JLabel jURL = new JLabel();

	public SlideshowFrame() {
		setUndecorated(true);
		setResizable(false);

		try {
			// Java 1.4 only
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

			if (! gd.isFullScreenSupported()) {
				throw new NoSuchMethodError();
			}

			Log.log(Log.LEVEL_TRACE, MODULE, "Switching to full-screen mode");
			//gd.setFullScreenWindow(this);

			setBounds(500, 20, 300, 300);
			show();
		} catch (NoSuchMethodError e) {
			Log.log(Log.LEVEL_TRACE, MODULE, "No full-screen mode: using maximized window");
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

		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int vKey = e.getKeyCode();
				Log.log(Log.LEVEL_TRACE, MODULE, "Got key: " + e);

				switch (vKey) {
					case KeyEvent.VK_ESCAPE:
						Log.log(Log.LEVEL_TRACE, MODULE, "Stopping slideshow");
						running = false;
						hide();
						break;
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_UP:
						previous();
						break;
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_SPACE:
						next();
						break;
				}
			}
		});

		PreviewFrame.ImageContentPane cp = new PreviewFrame.ImageContentPane();
		setContentPane(cp);
		cp.setLayout(new GridBagLayout());

		cp.add(new JLabel(), new GridBagConstraints(0, 10, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		cp.add(jCaption, new GridBagConstraints(0, 20, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		cp.add(jProgress, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		cp.add(jExtra, new GridBagConstraints(0, 19, 1, 1, 1.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

	public void start(List pictures) {
		this.pictures = pictures;

		new Thread(this).start();
	}

	public void run() {
		running = true;
		while (running) {
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

		if (loadPicture != null) {
			index = pictures.indexOf(loadPicture);
		}

		index++;

		if (index >= pictures.size()) {
			return false;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Next picture");

		displayPicture((Picture) pictures.get(index), false);

		if (++index < pictures.size() && imageIcons.get(pictures.get(index)) == null) {
			previewLoader.loadPreview((Picture) pictures.get(index));
		}

		return true;
	}

	public boolean previous() {
		int index = -1;

		if (loadPicture == null) {
			return false;
		}

		index = pictures.indexOf(loadPicture);

		index--;

		if (index < 0) {
			return false;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Previous picture");

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

	/*public void paint(Graphics g) {
		super.paint(g);

		Rectangle r = getContentPane().getBounds();

		String caption = currentPicture.getCaption();

		int width = g.getFontMetrics().stringWidth(caption);
		int height = g.getFontMetrics().getDescent();

		g.drawString(caption, (r.width - width) / 2, r.height - height);
	}*/

	public void imageLoaded(ImageIcon image, Picture picture) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + picture + " finished loading");

		if (picture != null) {
			jCaption.setText(picture.getCaption());
			jProgress.setText((pictures.indexOf(picture) + 1) + "/" + pictures.size());
			String extraFields = picture.getExtraFieldsString();
			if (extraFields != null) {
				jExtra.setText(extraFields);
			}
		}

		super.imageLoaded(image, picture);
	}
}
