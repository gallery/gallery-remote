package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.prefs.PropertiesFile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 9, 2003
 */
public class SlideshowFrame extends PreviewFrame implements Runnable, PreferenceNames {
	public static final String MODULE = "SlideFrame";
	List pictures = null;
	int sleepTime = 3000;
	boolean running = false;
	boolean shutdown = false;

	JLabel jCaption = new JLabel();
	JLabel jProgress = new JLabel();
	JLabel jExtra = new JLabel();
	JLabel jURL = new JLabel();

	public SlideshowFrame() {
		setUndecorated(true);
		setResizable(false);

		ignoreIMFailure = true;

		try {
			// Java 1.4 only
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

			if (! gd.isFullScreenSupported()) {
				throw new NoSuchMethodError();
			}

			Log.log(Log.LEVEL_TRACE, MODULE, "Switching to full-screen mode");
			//gd.setFullScreenWindow(this);

			DialogUtil.maxSize(this);
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
						shutdown = true;
						hide();
						break;
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_UP:
						previous();
						break;
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_DOWN:
						next();
						break;
					case KeyEvent.VK_SPACE:
						if (running) {
							running = false;
						} else {
							new Thread(SlideshowFrame.this).start();
						}

						break;
				}
			}
		});

		jCaption.setUI((LabelUI) OutlineLabelUI.createUI(jCaption));
		jProgress.setUI((LabelUI) OutlineLabelUI.createUI(jProgress));
		jExtra.setUI((LabelUI) OutlineLabelUI.createUI(jExtra));
		jURL.setUI((LabelUI) OutlineLabelUI.createUI(jURL));

		jCaption.setForeground(Color.white);
		jProgress.setForeground(Color.white);
		jExtra.setForeground(Color.white);
		jURL.setForeground(Color.white);

		jCaption.setFont(jCaption.getFont().deriveFont(Font.BOLD));
		jProgress.setFont(jCaption.getFont().deriveFont(Font.BOLD));
		jExtra.setFont(jCaption.getFont().deriveFont(Font.BOLD));
		jURL.setFont(jCaption.getFont().deriveFont(Font.BOLD));

		PreviewFrame.ImageContentPane cp = new PreviewFrame.ImageContentPane();
		setContentPane(cp);
		cp.setLayout(new GridBagLayout());

		cp.add(new JLabel(), new GridBagConstraints(0, 10, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		cp.add(new JLabel(), new GridBagConstraints(1, 10, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		cp.add(new JLabel(), new GridBagConstraints(2, 20, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		PropertiesFile pf = GalleryRemote.getInstance().properties;
		addComponent(cp, jProgress, 1, pf.getIntProperty(SLIDESHOW_PROGRESS));
		addComponent(cp, jCaption, 2, pf.getIntProperty(SLIDESHOW_CAPTION));
		addComponent(cp, jExtra, 3, pf.getIntProperty(SLIDESHOW_EXTRA));
		addComponent(cp, jURL, 4, pf.getIntProperty(SLIDESHOW_URL));

		sleepTime = pf.getIntProperty(SLIDESHOW_DELAY) * 1000;
	}

	private void addComponent(PreviewFrame.ImageContentPane cp, JLabel c, int mod, int value) {
		int col;
		int cons;
		switch (value % 10) {
			case 2:
			default:
				col = 0;
				cons = GridBagConstraints.WEST;
				break;

			case 0:
				col = 1;
				cons = GridBagConstraints.CENTER;
				break;

			case 4:
				col = 2;
				cons = GridBagConstraints.EAST;
				break;
		}

		cp.add(c, new GridBagConstraints(col, ((int) ((value - 10) / 10)) * 10 + mod, 1, 1, 0.0, 0.0
				, cons, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		c.setHorizontalAlignment(value % 10);
	}

	public static class OutlineLabelUI extends BasicLabelUI {
		protected static OutlineLabelUI labelUI = new OutlineLabelUI();

		public static ComponentUI createUI(JComponent c) {
			return labelUI;
		}

		protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
			g.setColor(Color.darkGray);
			g.drawString(s,textX + 1, textY + 1);
			g.drawString(s,textX - 1, textY + 1);
			g.drawString(s,textX + 1, textY - 1);
			g.drawString(s,textX - 1, textY - 1);
			g.setColor(l.getForeground());
			g.drawString(s, textX, textY);
		}

		/*public Dimension getPreferredSize(JComponent c) {
			Dimension d = super.getPreferredSize(c);

			return new Dimension((int) d.getWidth() + 2, (int) d.getHeight() + 2);
		}*/
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
				// the slideshow is over
				hide();
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
			previewLoader.loadPreview((Picture) pictures.get(index), false);
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
			previewLoader.loadPreview((Picture) pictures.get(index), false);
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
			jProgress.setText((pictures.indexOf(picture) + 1) + "/" + pictures.size() + (running?"":" (paused)"));
			String extraFields = picture.getExtraFieldsString();
			if (extraFields != null) {
				jExtra.setText(extraFields);
			}
		}

		super.imageLoaded(image, picture);
	}
}
