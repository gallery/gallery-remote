package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.prefs.PropertiesFile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 9, 2003
 */
public class SlideshowFrame extends PreviewFrame implements Runnable, PreferenceNames, CancellableTransferListener {
	public static final String MODULE = "SlideFrame";
	List pictures = null;
	List wantDownloaded = Collections.synchronizedList(new ArrayList());
	Picture userPicture = null;
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

		initComponents();
		listener = this;

		ignoreIMFailure = true;
	}

	public void showSlideshow() {
		try {
			if (GalleryRemote.IS_MAC_OS_X) {
				// on the Mac, using a maximized window doesn't take care of the menu bar

				// Java 1.4 only
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

				if (! gd.isFullScreenSupported()) {
					throw new NoSuchMethodError();
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Switching to full-screen mode");
				DialogUtil.maxSize(this);
				setVisible(true);

				// unfortunately, this doesn't work on Mac 1.4.2...
				// gd.setFullScreenWindow(this);
			} else {
				DialogUtil.maxSize(this);
				//setBounds(600, 100, 500, 500);
				setVisible(true);
			}
		} catch (Throwable e) {
			Log.log(Log.LEVEL_TRACE, MODULE, "No full-screen mode: using maximized window");
			DialogUtil.maxSize(this);
			setVisible(true);
		}

		// todo: this is a hack to prevent painting problems (the status bar paints
		// on top of the slide show)
		Frame mainFrame = GalleryRemote._().getMainFrame();
		if (mainFrame != null) {
			mainFrame.setVisible(false);
		}
	}

	public void hide() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		gd.setFullScreenWindow(null);

		super.hide();

		if (GalleryRemote._() != null) {
			Frame mainFrame = GalleryRemote._().getMainFrame();
			if (mainFrame != null) {
				mainFrame.setVisible(true);
			}
		}
	}

	public void initComponents() {
		previewCacheSize = 3;
		addMouseListener(new MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
				nextAsync();
			}
		});

		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() > 0) {
					nextAsync();
				} else {
					previousAsync();
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
						previousAsync();
						break;
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_DOWN:
						nextAsync();
						break;
					case KeyEvent.VK_SPACE:
						if (running) {
							running = false;
						} else {
							new Thread(SlideshowFrame.this).start();
						}

						updateProgress(currentPicture);

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

		PropertiesFile pf = GalleryRemote._().properties;
		addComponent(cp, jProgress, 1, pf.getIntProperty(SLIDESHOW_PROGRESS));
		addComponent(cp, jCaption, 2, pf.getIntProperty(SLIDESHOW_CAPTION));
		addComponent(cp, jExtra, 3, pf.getIntProperty(SLIDESHOW_EXTRA));
		addComponent(cp, jURL, 4, pf.getIntProperty(SLIDESHOW_URL));

		sleepTime = pf.getIntProperty(SLIDESHOW_DELAY) * 1000;
	}

	private void addComponent(PreviewFrame.ImageContentPane cp, JLabel c, int mod, int value) {
		if (value == 0) {
			return;
		}

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

	public void start(ArrayList pictures) {
		if (GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_RANDOM)) {
			this.pictures = new ArrayList(pictures);
			Collections.shuffle(this.pictures);
		} else {
			this.pictures = pictures;
		}

		new Thread(this).start();
	}

	public void run() {
		running = true;
		while (running) {
			long time = System.currentTimeMillis();

			if (!next(false)) {
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

	private void previousAsync() {
		new Thread() {
			public void run() {
				previous(true);
			}
		}.start();
	}

	private void nextAsync() {
		new Thread() {
			public void run() {
				next(true);
			}
		}.start();
	}

	public boolean next(boolean user) {
		int index = -1;

		if (loadPicture != null) {
			index = pictures.indexOf(loadPicture);

			if (wantDownloaded.contains(loadPicture) && (loadPicture != userPicture || user)) {
				// we no longer want the current picture
				wantDownloaded.remove(loadPicture);
			}
		}

		index++;

		if (index >= pictures.size()) {
			return false;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Next picture");

		// display next picture
		Picture picture = (Picture) pictures.get(index);
		if (user) {
			userPicture = picture;
		} else if (userPicture != null && userPicture != picture) {
			// automatic move tying to move away from user-chosen picture
			return true;
		}

		wantDownloaded.add(picture);
		updateProgress(picture);
		displayPicture(picture, false);

		// and cache the one after it
		if (++index < pictures.size() && (imageIcons.get(picture = (Picture) pictures.get(index))) == null) {
			wantDownloaded.add(picture);
			previewLoader.loadPreview(picture, false);
		}

		return true;
	}

	public boolean previous(boolean user) {
		int index = -1;

		if (loadPicture == null) {
			return false;
		} else if (wantDownloaded.contains(loadPicture) && (loadPicture != userPicture || user)) {
			// we no longer want the current picture
			wantDownloaded.remove(loadPicture);
		}

		index = pictures.indexOf(loadPicture);

		index--;

		if (index < 0) {
			return false;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Previous picture");

		// display previous picture
		Picture picture = (Picture) pictures.get(index);
		if (user) {
			userPicture = picture;
		} else if (userPicture != null && userPicture != picture) {
			// automatic move tying to move away from user-chosen picture
			return true;
		}

		wantDownloaded.add(picture);
		updateProgress(picture);
		displayPicture(picture, false);

		// and cache the one after it
		if (--index > 0 && (imageIcons.get(picture = (Picture) pictures.get(index))) == null) {
			wantDownloaded.add(picture);
			previewLoader.loadPreview(picture, false);
		}

		return true;
	}

	public void imageLoaded(ImageIcon image, Picture picture) {
		Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + picture + " finished loading");

		if (picture == userPicture) {
			userPicture = null;
		}

		if (picture != loadPicture) {
			Log.log(Log.LEVEL_TRACE, MODULE, "We wanted " + loadPicture + ": ignoring");
			return;
		}

		if (picture != null) {
			// todo: captions are not printed outline because they are HTML and that's a fucking mess
			//jCaption.setText("<HTML>" + picture.getCaption() + "</HTML>");
			jCaption.setText(picture.getCaption());
			updateProgress(picture);
			String extraFields = picture.getExtraFieldsString();
			if (extraFields != null) {
				jExtra.setText(extraFields);
			}
			if (picture.isOnline()) {
				jURL.setText(picture.safeGetUrlFull().toString());
			} else {
				jURL.setText(picture.getSource().toString());
			}
		}

		super.imageLoaded(image, picture);
	}

	private void updateProgress(Picture picture) {
		StringBuffer sb = new StringBuffer();

		sb.append(pictures.indexOf(picture) + 1).append("/").append(pictures.size());

		if (imageIcons.get(picture) == null) {
			sb.append(" ").append("(downloading)");
		} else if (! running) {
			sb.append(" ").append("(paused)");
		}

		jProgress.setText(sb.toString());
	}

	public boolean dataTransferred(int transferred, int overall, double kbPerSecond, Picture p) {
		if (! wantDownloaded.contains(p) || shutdown) {
			return false;
		}

		Graphics g = getGraphics();

		if (transferred == overall) {
			g.setColor(getContentPane().getBackground());
		} else {
			g.setColor(Color.yellow);
		}

		g.drawLine(0, 0, getWidth() * transferred / overall, 0);

		return true;
	}

	public static class OutlineLabelUI extends BasicLabelUI {
		protected static OutlineLabelUI labelUI = new OutlineLabelUI();
		/*private static Rectangle paintIconR = new Rectangle();
		private static Rectangle paintTextR = new Rectangle();
		private static Rectangle paintViewR = new Rectangle();
		private static Insets paintViewInsets = new Insets(0, 0, 0, 0);*/

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

		// todo: captions are not printed outline because they are HTML and that's a fucking mess
		/*public void paint(Graphics g, JComponent c) {
			JLabel label = (JLabel)c;
			String text = label.getText();
			Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

			if ((icon == null) && (text == null)) {
				return;
			}

			FontMetrics fm = g.getFontMetrics();
			Insets insets = c.getInsets(paintViewInsets);

			paintViewR.x = insets.left;
			paintViewR.y = insets.top;
			paintViewR.width = c.getWidth() - (insets.left + insets.right);
			paintViewR.height = c.getHeight() - (insets.top + insets.bottom);

			paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
			paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

			String clippedText =
					layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

			if (icon != null) {
				icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
			}

			if (text != null) {
				View v = (View) c.getClientProperty(BasicHTML.propertyKey);
				if (v != null) {
					Color color = label.getForeground();

					label.setForeground(Color.darkGray);
					label.setText(label.getText() + " ");
					paintTextR.x -= 1;
					v.paint(g, paintTextR);
					paintTextR.y -= 1;
					v.paint(g, paintTextR);
					paintTextR.x += 2;
					v.paint(g, paintTextR);
					paintTextR.y += 2;
					v.paint(g, paintTextR);

					label.setForeground(Color.white);
					label.setText(label.getText() + " ");
					paintTextR.x -= 1;
					paintTextR.y -= 1;
					v.paint(g, paintTextR);
				} else {
					int textX = paintTextR.x;
					int textY = paintTextR.y + fm.getAscent();

					if (label.isEnabled()) {
						paintEnabledText(label, g, clippedText, textX, textY);
					}
					else {
						paintDisabledText(label, g, clippedText, textX, textY);
					}
				}
			}
		}*/
	}
}
