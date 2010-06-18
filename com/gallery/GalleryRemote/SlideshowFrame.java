package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.*;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.prefs.PropertiesFile;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SlideshowFrame extends PreviewFrame
		implements Runnable, PreferenceNames, CancellableTransferListener, MouseMotionListener {
	public static final String MODULE = "SlideFrame";

	List pictures = null;
	List wantDownloaded = Collections.synchronizedList(new ArrayList());
	Picture userPicture = null;
	SlideshowPane slideshowPane;

	int wantIndex = -1;

	int sleepTime = 3000;
	int skipTime = 500;
	boolean running = false;
	boolean shutdown = false;
	long pictureShownTime = 0;

	String caption = null;
	String progress = null;
	String extra = null;
	String url = null;
	String album = null;
	String skipping = null;
	String description;
	String summary;

	public static final int STATE_NO_CHANGE = 0;
	public static final int STATE_PREPARING = 0;
	public static final int STATE_DOWNLOADING = 1;
	public static final int STATE_PROCESSING = 2;
	public static final int STATE_NEXTREADY = 3;
	public static final int STATE_SKIPPING = 4;
	public static final int STATE_SHOWING = 5;
	public static final int STATE_ERROR = 6;

	public static final int FEEDBACK_NONE = 0;
	public static final int FEEDBACK_HELP = 1;
	public static final int FEEDBACK_PREV = 2;
	public static final int FEEDBACK_NEXT = 4;
	public static final int FEEDBACK_PAUSE_PLAY = 8;

	public int dataTransferred = 0;
	public int dataOverall = 0;

	public int feedback = FEEDBACK_NONE;
	public int state = STATE_PREPARING;

	long feedbackUntil = 0;
	long dontShowUntil = 0;
	Thread feedbackThread = null;

	public static Cursor transparentCursor = null;

	int transitionDuration = -1;
	boolean enableTransitions;

	public SlideshowFrame() {
		setUndecorated(true);
		setResizable(false);

		initComponents();
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
				gd.setFullScreenWindow(this);
			} else {
				DialogUtil.maxSize(this);
				//setBounds(600, 100, 1000, 1000);
				setVisible(true);
			}
		} catch (Throwable e) {
			Log.log(Log.LEVEL_TRACE, MODULE, "No full-screen mode: using maximized window");
			DialogUtil.maxSize(this);
			setVisible(true);
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Showing slideshow frame");

		// todo: this is a hack to prevent painting problems (the status bar paints
		// on top of the slide show)
		Frame mainFrame = GalleryRemote._().getMainFrame();
		if (mainFrame != null) {
			mainFrame.setVisible(false);
		}
	}

	public void hide() {
		Log.log(Log.LEVEL_TRACE, MODULE, "Stopping slideshow");
		running = false;
		shutdown = true;

		if (!isShowing()) {
			return;
		}

		showCursor();

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (gd.getFullScreenWindow() == this) {
			gd.setFullScreenWindow(null);
		}
		
		super.hide();

		if (GalleryRemote._() != null) {
			Frame mainFrame = GalleryRemote._().getMainFrame();
			if (mainFrame != null) {
				mainFrame.setVisible(true);
			}
		}

		ImageUtils.deferredTasks();
	}

	public void initComponents() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Got click");
				nextAsync();
			}
		});

		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Got wheel: " + e);
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
					case KeyEvent.VK_H:
						updateFeedback(FEEDBACK_HELP);
						break;
					case KeyEvent.VK_SPACE:
						if (running) {
							running = false;
						} else {
							new Thread(SlideshowFrame.this).start();
						}
						updateFeedback(FEEDBACK_PAUSE_PLAY);

						updateProgress(loader.pictureShowNow, STATE_SHOWING, false);

						break;
				}
			}
		});

		addMouseMotionListener(this);

		slideshowPane = new SlideshowPane();
		setContentPane(slideshowPane);

		sleepTime = GalleryRemote._().properties.getIntProperty(SLIDESHOW_DELAY) * 1000;

		loader = new ImageLoaderUtil(5, this);
		loader.setTransferListener(this);
	}

	public void start(ArrayList pictures) {
		if (GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_RANDOM)) {
			this.pictures = new ArrayList(pictures);
			Collections.shuffle(this.pictures);
		} else {
			this.pictures = pictures;
		}

		if (sleepTime > 0) {
			new Thread(this).start();
		} else {
			next(false);
		}

		if (GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_PRELOADALL)) {
			Thread t = new Thread() {
				public void run() {
					Log.log(Log.LEVEL_TRACE, MODULE, "Preload thread starting");
					for (Iterator it = SlideshowFrame.this.pictures.iterator(); it.hasNext();) {
						if (shutdown) {
							break;
						}

						Picture picture = (Picture) it.next();
						Log.log(Log.LEVEL_TRACE, MODULE, "Preloading " + picture);
						ImageUtils.download(picture, getRootPane().getSize(), GalleryRemote._().getCore().getMainStatusUpdate(), null);
					}
					Log.log(Log.LEVEL_TRACE, MODULE, "Preload thread done");
				}
			};
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	public void run() {
		running = true;
		while (sleepTime > 0 && running && !shutdown) {
			if (!next(false)) {
				// the slideshow is over
				hide();
				break;
			}

			try {
				long sleep;

				while ((sleep = sleepTime - (System.currentTimeMillis() - pictureShownTime)) > 0) {
					Thread.sleep(sleep);
				}

				//Log.log(Log.LEVEL_TRACE, MODULE, "sleepTime: " + sleepTime + " - " + (System.currentTimeMillis() - pictureShownTime));
				//Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}
	}

	private void previousAsync() {
		new Thread() {
			public void run() {
				updateFeedback(FEEDBACK_PREV);
				previous(true);
			}
		}.start();
	}

	private void nextAsync() {
		new Thread() {
			public void run() {
				updateFeedback(FEEDBACK_NEXT);
				next(true);
			}
		}.start();
	}

	public boolean next(boolean user) {
		if (loader.pictureShowWant != null
				&& wantDownloaded.contains(loader.pictureShowWant)
				&& (loader.pictureShowWant != userPicture || user)) {
			// we no longer want the current picture
			wantDownloaded.remove(loader.pictureShowWant);
		}

		Picture picture;
		if (userPicture != null && !user) {
			// automatic move trying to move away from user-chosen picture
			// add delay to prevent endless loop
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			return true;
		}
		synchronized(this) {
			if (userPicture != null && !user) {
				// don't delay in critical section
				return true;
			}

			wantIndex++;

			if (wantIndex >= pictures.size()) {
				if (GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_LOOP)) {
					wantIndex = 0;
				} else {
					wantIndex = pictures.size() - 1;
					return false;
				}
			}

			// display next picture
			picture = (Picture) pictures.get(wantIndex);
			Log.log(Log.LEVEL_TRACE, MODULE, "Next picture: " + picture);
		}

		if (user) {
			userPicture = picture;
			updateProgress(picture, STATE_SKIPPING, false);
			Log.log(Log.LEVEL_TRACE, MODULE, "Skipping sleep");
			try {
				Thread.sleep(skipTime);
			} catch (InterruptedException e) {}
			Log.log(Log.LEVEL_TRACE, MODULE, "Skipping wake");

			// if user moved to a different picture in the meantime, cancel
			if (userPicture != picture) {
				Log.log(Log.LEVEL_TRACE, MODULE, "User skipped again, not even loading " + picture);
				return true;
			}
		}

		wantDownloaded.add(picture);
		updateProgress(picture, STATE_PREPARING, false);
		loader.preparePicture(picture, false, true);

		// and cache the one after it
		if (wantIndex + 1< pictures.size() && (loader.images.get(picture = (Picture) pictures.get(wantIndex + 1))) == null) {
			wantDownloaded.add(picture);
			loader.imageLoader.loadPicture(picture, false);
		}

		return true;
	}

	public boolean previous(boolean user) {
		if (loader.pictureShowWant != null && wantDownloaded.contains(loader.pictureShowWant) && (loader.pictureShowWant != userPicture || user)) {
			// we no longer want the current picture
			wantDownloaded.remove(loader.pictureShowWant);
		}

		Picture picture;
		synchronized (this) {
			wantIndex--;

			if (wantIndex < 0) {
				if (GalleryRemote._().properties.getBooleanProperty(SLIDESHOW_LOOP)) {
					wantIndex = pictures.size() - 1;
				} else {
					wantIndex = 0;
					return false;
				}
			}

			// display previous picture
			picture = (Picture) pictures.get(wantIndex);
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Previous picture: " + picture);
		if (user) {
			userPicture = picture;
			updateProgress(picture, STATE_SKIPPING, false);
			try {
				Thread.sleep(skipTime);
			} catch (InterruptedException e) {}

			// if user moved to a different picture in the meantime, cancel
			if (userPicture != picture) {
				Log.log(Log.LEVEL_TRACE, MODULE, "User skipped again, not even loading " + picture);
				return true;
			}
		} else if (userPicture != null && userPicture != picture) {
			// automatic move trying to move away from user-chosen picture
			return true;
		}

		wantDownloaded.add(picture);
		updateProgress(picture, STATE_PREPARING, false);
		loader.preparePicture(picture, false, true);

		// and cache the one after it
		if (wantIndex - 1 > 0 && (loader.images.get(picture = (Picture) pictures.get(wantIndex - 1))) == null) {
			wantDownloaded.add(picture);
			loader.imageLoader.loadPicture(picture, false);
		}

		return true;
	}

	public boolean blockPictureReady(Image image, Picture picture) {
		Log.log(Log.LEVEL_TRACE, MODULE, "blockPictureReady: " + picture + " - pictureShowWant: " + loader.pictureShowWant);

		if (picture == userPicture) {
			userPicture = null;
		}

		if (picture != loader.pictureShowWant) {
			Log.log(Log.LEVEL_TRACE, MODULE, "We wanted " + loader.pictureShowWant + ": ignoring");
			updateProgress(loader.pictureShowWant, STATE_NEXTREADY, false);
			return true;
		}

		if (picture != null) {
			if (picture.getDescription() != null) {
				caption = ImageLoaderUtil.stripTags(HTMLEscaper.unescape(picture.getDescription())).trim();
			} else {
				caption = null;
			}

			extra = picture.getExtraFieldsString(true).trim();
			description = picture.getExtraField("Description");
			summary = picture.getExtraField("Summary");

			if (picture.isOnline()) {
				url = picture.safeGetUrlFull().toString();

				// update view count on Gallery
				picture.getParentAlbum().getGallery().incrementViewCount(picture,
						GalleryRemote._().getCore().getMainStatusUpdate());
			} else {
				url = picture.getSource().toString();
			}

			if (picture.getParentAlbum().getDescription() != null) {
				album = ImageLoaderUtil.stripTags(HTMLEscaper.unescape(
						picture.getParentAlbum().getDescription())).trim();
			} else {
				album = null;
			}

			updateProgress(picture, STATE_SHOWING, true);
		}

		pictureShownTime = System.currentTimeMillis();

		return false;
	}

	public void pictureStartDownloading(Picture picture) {
		if (picture == loader.pictureShowWant || picture == userPicture) {
			updateProgress(picture, STATE_DOWNLOADING, false);
		}
	}

	public void pictureStartProcessing(Picture picture) {
		if (picture == loader.pictureShowWant || picture == userPicture) {
			updateProgress(picture, STATE_PROCESSING, false);
		}
	}

	public void pictureLoadError(Picture picture) {
		if (picture == loader.pictureShowWant || picture == userPicture) {
			updateProgress(picture, STATE_ERROR, false);
		}

		loader.reduceMemory();
	}

	private void updateProgress(Picture picture, int state, boolean repaintLater) {
		if (picture == null) {
			return;
		}

		Object[] params = new Object[] {picture.getName(),
										new Integer(pictures.indexOf(picture) + 1),
										new Integer(pictures.size())};

		switch (state) {
			 case STATE_SHOWING:
				if (! running) {
					progress = GRI18n.getString(MODULE, "paused", params);
				} else {
					progress = GRI18n.getString(MODULE, "showing", params);
				}
				break;

			case STATE_PREPARING:
				progress = GRI18n.getString(MODULE, "preparing", params);
				break;

			case STATE_DOWNLOADING:
				progress = GRI18n.getString(MODULE, "downloading", params);
				break;

			case STATE_PROCESSING:
				progress = GRI18n.getString(MODULE, "processing", params);
				break;

			case STATE_NEXTREADY:
				progress = GRI18n.getString(MODULE, "nextReady", params);
				break;

			case STATE_ERROR:
				progress = GRI18n.getString(MODULE, "error", params);
				break;

			case STATE_SKIPPING:
				progress = GRI18n.getString(MODULE, "skipping", params);
				skipping = GRI18n.getString(MODULE, "skippingController", params);
				break;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "updateProgress: " + progress);
		if (!repaintLater) {
			repaint();
		}

		hideCursor();
	}

	public boolean dataTransferred(int transferred, int overall, double kbPerSecond, Picture p) {
		if (! wantDownloaded.contains(p) || shutdown) {
			return false;
		}

		dataTransferred = transferred;
		dataOverall = overall;

		if (transferred == overall) {
			repaint();
		} else {
			slideshowPane.paintProgress((Graphics2D) slideshowPane.getGraphics());
		}

		return true;
	}

	public void updateFeedback(int feedback) {
		if (feedback != FEEDBACK_NONE) {
			feedbackUntil = System.currentTimeMillis()
					+ (feedback == FEEDBACK_HELP?6000: 1500);

			synchronized(this) {
				if (feedbackThread == null) {
					feedbackThread = new Thread() {
						public void run() {
							boolean running = true;

							while (running) {
								try {
									Thread.sleep(feedbackUntil - System.currentTimeMillis());
									synchronized(this) {
										if (System.currentTimeMillis() >= feedbackUntil) {
											running = false;
											feedbackThread = null;
											SlideshowFrame.this.feedback = FEEDBACK_NONE;
											repaint();
										}
									}
								} catch (InterruptedException e) {}
							}
						}
					};
					feedbackThread.start();
				}
			}
		}

		if ((this.feedback & FEEDBACK_HELP) == FEEDBACK_HELP) {
			this.feedback = feedback | FEEDBACK_HELP;
		} else if (feedback == FEEDBACK_HELP) {
			this.feedback |= FEEDBACK_HELP;
		} else {
			this.feedback = feedback;
		}

		repaint();
	}

	public void hideCursor() {
		if (transparentCursor == null) {
			int[] pixels = new int[16 * 16];
			Image image = Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(16, 16, pixels, 0, 16));
			transparentCursor =
					Toolkit.getDefaultToolkit().createCustomCursor
						(image, new Point(0, 0), "invisiblecursor");
		}

		setCursor(transparentCursor);
	}

	public void showCursor() {
		setCursor(Cursor.getDefaultCursor());
	}

	public void mouseDragged(MouseEvent e) {}

	public void mouseMoved(MouseEvent e) {
		showCursor();
	}

	public void initTransitionDuration() {
		boolean accelerated = ((Graphics2D) getGraphics()).getDeviceConfiguration().getImageCapabilities().isAccelerated();

		Log.log(Log.LEVEL_TRACE, MODULE, "Is graphics accelerated: " + accelerated);

		enableTransitions = accelerated
				|| GalleryRemote._().properties.getBooleanProperty(ALLOW_UNACCELERATED_TRANSITION, false);
		transitionDuration = enableTransitions?
				GalleryRemote._().properties.getIntProperty(SLIDESHOW_TRANSITION_DURATION, 3000):0;

		if (transitionDuration == 0) {
			enableTransitions = false;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Transitions enabled: " + enableTransitions);
	}

	class SlideshowPane extends JPanel implements ActionListener {
		Color background = new Color(100, 100, 100, 150);
		Color normal = new Color(180, 180, 180, 180);
		Color hilight = new Color(255, 255, 255, 180);
		boolean firstPaint = true;

		public static final int LOCATION_TOP_LEFT = 0;
		public static final int LOCATION_TOP_CENTER = 1;
		public static final int LOCATION_TOP_RIGHT = 2;
		public static final int LOCATION_MID_LEFT = 3;
		public static final int LOCATION_MID_CENTER = 4;
		public static final int LOCATION_MID_RIGHT = 5;
		public static final int LOCATION_BOT_LEFT = 6;
		public static final int LOCATION_BOT_CENTER = 7;
		public static final int LOCATION_BOT_RIGHT = 8;
		
		public final int[] locationToCode = {12, 10, 14, 22, 20, 24, 32, 30, 34};
		public HashMap codeToLocation = new HashMap(9);

		BufferedImage feedbackCache = null;
		int cachedFeedback = 0;
		Point feedbackLocation = new Point();
		int feedbackWidth = 535;
		int feedbackHeight = 220;

		BufferedImage[] infoImage = new BufferedImage[9];
		String[] infoString = new String[9];
		Point[] infoLocation = new Point[9];
		BufferedImage[] previousInfoImage = new BufferedImage[9];
		Point[] previousInfoLocation = new Point[9];

		Image currentImage = null;
		Image currentImageSrc = null;
		Image previousImage = null;
		Rectangle previousRect = null;

		Timer timer = new Timer(1000/60, this);
		long transitionStart = System.currentTimeMillis() - 5000;
		float imageAlpha = 0;
		int thickness = 1;
		
		public SlideshowPane() {
			super();
			
			for (int i = 0; i < 9; i++) {
				codeToLocation.put(new Integer(locationToCode[i]), new Integer(i));
			}
		}

		public void actionPerformed(ActionEvent e) {
			long now = System.currentTimeMillis();

			if (now - transitionStart > transitionDuration) {
				timer.stop();
				imageAlpha = 1;

				// flush previous, no longer needed
				previousImage = null;
				for (int id = 0; id < previousInfoImage.length; id++) {
					if (previousInfoImage[id] != null) {
						previousInfoImage[id].flush();
					}
					previousInfoImage[id] = null;
				}
			} else {
				imageAlpha = ((float) (now - transitionStart)) / transitionDuration;
			}

			repaint();
		}

		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

			if (firstPaint) {
				firstPaint = false;
				ImageLoaderUtil.setSlideshowFont(this);
				initTransitionDuration();
				int defaultThickness = getFont().getSize() / 7;
				thickness = GalleryRemote._().properties.getIntProperty(SLIDESHOW_FONTTHICKNESS, defaultThickness);
			}

			if (feedback == FEEDBACK_NONE && feedbackUntil <= System.currentTimeMillis()) {
				skipping = null;
			}

			paintPicture(g2);
			paintInfo(g2);
			paintFeedback(g2);
			paintSkipping(g2);
			paintProgress(g2);
		}

		public void paintPicture(Graphics2D g) {
			Color c = GalleryRemote._().properties.getColorProperty(SLIDESHOW_COLOR);
			if (c != null) {
				g.setColor(c);
			} else {
				g.setColor(getBackground());
			}

			g.fillRect(0, 0, getSize().width, getSize().height);

			if (loader.imageShowNow != null && loader.pictureShowWant != null) {
				if (loader.imageShowNow != currentImageSrc) {
					Log.log(Log.LEVEL_TRACE, MODULE, "New image: " + loader.imageShowNow + " - " + currentImageSrc);

					previousImage = currentImage;
					previousRect = currentRect;

					currentImage = ImageUtils.rotateImage(loader.imageShowNow, loader.pictureShowWant.getAngle(),
							loader.pictureShowWant.isFlipped(), this);

					currentRect = new Rectangle(getLocation().x + (getWidth() - currentImage.getWidth(this)) / 2,
							getLocation().y + (getHeight() - currentImage.getHeight(this)) / 2,
							currentImage.getWidth(this), currentImage.getHeight(this));

					currentImageSrc = loader.imageShowNow;

					if (enableTransitions) {
						imageAlpha = 0;
						transitionStart = System.currentTimeMillis();
						if (! timer.isRunning()) {
							timer.start();
						}
					} else {
						imageAlpha = 1;
					}
				}

				Composite composite = g.getComposite();

				Log.log(Log.LEVEL_TRACE, MODULE, "Painting alpha=" + imageAlpha);

				if (imageAlpha != 1 && previousImage != null) {
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - imageAlpha));
					g.drawImage(previousImage, previousRect.x, previousRect.y, getContentPane());
				}

				if (imageAlpha != 0) {
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imageAlpha));
					g.drawImage(currentImage, currentRect.x, currentRect.y, getContentPane());
				}

				g.setComposite(composite);
			}
		}

		public void paintInfo(Graphics2D g) {
			PropertiesFile pf = GalleryRemote._().properties;
			
			String[] locationContent = new String[9];
			Boolean[] locationTransition = new Boolean[9];
			Boolean[] locationMaybePresent = new Boolean[9];

			concatLocationContent(pf.getIntProperty(SLIDESHOW_CAPTION), caption, true, 
					locationContent, locationTransition, locationMaybePresent);
			concatLocationContent(pf.getIntProperty(SLIDESHOW_PROGRESS), progress, false, 
					locationContent, locationTransition, locationMaybePresent);
			concatLocationContent(pf.getIntProperty(SLIDESHOW_EXTRA), extra, true, 
					locationContent, locationTransition, locationMaybePresent);
			concatLocationContent(pf.getIntProperty(SLIDESHOW_ALBUM), album, true, 
					locationContent, locationTransition, locationMaybePresent);
			concatLocationContent(pf.getIntProperty(SLIDESHOW_URL), url, true, 
					locationContent, locationTransition, locationMaybePresent);
			concatLocationContent(pf.getIntProperty(SLIDESHOW_SUMMARY), summary, true, 
					locationContent, locationTransition, locationMaybePresent);
			concatLocationContent(pf.getIntProperty(SLIDESHOW_DESCRIPTION), description, true, 
					locationContent, locationTransition, locationMaybePresent);

			for (int i = 0; i < 9; i++) {
				if (locationMaybePresent[i] == Boolean.TRUE) {
					paintInfo(g, i, locationContent[i], locationToCode[i], locationTransition[i].booleanValue());
				}
			}
		}

		private void concatLocationContent(int code, String content, boolean transition, String[] locationContent,
		                                   Boolean[] locationTransition, Boolean[] locationMaybePresent) {
			Integer location = (Integer) codeToLocation.get(new Integer(code));
			if (location != null) {
				int l = location.intValue();
				locationMaybePresent[l] = Boolean.TRUE;
				
				if (locationTransition[l] == null) {
					locationTransition[l] = Boolean.valueOf(transition);
				} else if (!transition) {
					locationTransition[l] = Boolean.FALSE;
				}
				
				if (content != null && content.length() != 0) {
					if (locationContent[l] == null) {
						locationContent[l] = content;
					} else {
						locationContent[l] += '\n' + content;
					}
				}
			}
		}

		public void paintFeedback(Graphics2D g) {
			if (feedback == FEEDBACK_NONE) {
				return;
			}

			if (feedbackCache == null || cachedFeedback != feedback) {
				cachedFeedback = feedback;

				Dimension d = getSize();
				feedbackLocation.x = d.width / 2 - feedbackWidth / 2;
				feedbackLocation.y = d.height / 3 * 2 - feedbackHeight / 2;
				int x = 30;
				int y = 20;

				if (feedbackCache != null) {
					feedbackCache.flush();
				}
				feedbackCache = new BufferedImage(
						feedbackWidth + 60,
						feedbackHeight + 70,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D gc = (Graphics2D) feedbackCache.getGraphics();

				gc.setFont(g.getFont().deriveFont(18.0F));
				FontMetrics fm = gc.getFontMetrics();

				// background
				gc.setColor(background);
				gc.fillRoundRect(0, 0, feedbackWidth,
						feedbackHeight + ((feedback & FEEDBACK_HELP) == FEEDBACK_HELP?0:-30),
						30, 30);
				gc.setColor(normal);
				gc.drawRoundRect(0, 0, feedbackWidth,
						feedbackHeight + ((feedback & FEEDBACK_HELP) == FEEDBACK_HELP?0:-30),
						30, 30);

				// left arrow
				gc.setColor((feedback & FEEDBACK_PREV) == FEEDBACK_PREV?hilight:normal);
				gc.fillPolygon(new int[] {x + 100, x + 100, x + 50, x + 50, x, x + 50, x + 50},
						new int[] {y + 60, y + 90, y + 90, y + 125, y + 75, y + 25, y + 60}, 7);
				drawHelp(gc, hilight, fm, x + 50, y + 160, GRI18n.getString(MODULE, "controller.left"));
				drawHelp(gc, hilight, fm, x + 107, y + 180, GRI18n.getString(MODULE, "controller.mousewheel"));

				x += 115;

				// right arrow
				gc.setColor((feedback & FEEDBACK_NEXT) == FEEDBACK_NEXT?hilight:normal);
				gc.fillPolygon(new int[] {x, x, x + 50, x + 50, x + 100, x + 50, x + 50},
						new int[] {y + 60, y + 90, y + 90, y + 125, y + 75, y + 25, y + 60}, 7);
				drawHelp(gc, hilight, fm, x + 50, y + 160, GRI18n.getString(MODULE, "controller.right"));

				x += 130;

				// play/pause
				gc.setColor((feedback & FEEDBACK_PAUSE_PLAY) == FEEDBACK_PAUSE_PLAY?hilight:normal);
				if (running) {
					gc.fillPolygon(new int[] {x, x, x + 100},
							new int[] {y + 10, y + 140, y + 75}, 3);
				} else {
					gc.fillPolygon(new int[] {x, x, x + 30, x + 30},
							new int[] {y + 10, y + 140, y + 140, y + 10}, 4);
					gc.fillPolygon(new int[] {x + 70, x + 70, x + 100, x + 100},
							new int[] {y + 10, y + 140, y + 140, y + 10}, 4);
				}
				drawHelp(gc, hilight, fm, x + 50, y + 160, GRI18n.getString(MODULE, "controller.space"));

				x += 130;

				// stop
				gc.setColor(normal);
				gc.fillPolygon(new int[] {x, x, x + 30, x + 70, x + 100, x + 100, x + 70, x + 30},
						new int[] {y + 55, y + 95, y + 125, y + 125, y + 95, y + 55, y + 25, y + 25}, 8);
				drawHelp(gc, hilight, fm, x + 50, y + 160, GRI18n.getString(MODULE, "controller.escape"));
			}

			g.drawImage(feedbackCache, feedbackLocation.x, feedbackLocation.y, this);
		}

		public void paintSkipping(Graphics2D g) {
			if (skipping != null) {
				Dimension d = getSize();
				int x = d.width / 2;
				int y = d.height / 3 * 2 - feedbackHeight / 2;

				g.setFont(getFont().deriveFont(48.0F));
				FontMetrics fm = g.getFontMetrics();
				g.setColor(hilight);
				Rectangle2D bounds = fm.getStringBounds(skipping, g);
				g.drawString(skipping, (int) (x - bounds.getWidth() / 2),
						y + ((feedback & FEEDBACK_HELP) == FEEDBACK_HELP?270:240));
			}
		}

		public void paintProgress(Graphics2D g) {
			if (dataTransferred < dataOverall) {
				g.setColor(Color.yellow);

				float r = ((float) dataTransferred) / dataOverall;
				g.drawLine(0, 0, (int) (getWidth() * r), 0);
			}
		}

		public void paintInfo(Graphics2D g, int id, String text, int position, boolean transition) {
			if (position == 0) return;
			if (text == null) text = "";

			if (!text.equals(infoString[id])) {
				previousInfoImage[id] = infoImage[id];
				previousInfoLocation[id] = infoLocation[id];

				infoString[id] = text;

				Dimension d = getSize();
				g.setFont(getFont());
				int x;
				int y;
				int inset = 5;

				switch (position % 10) {
					case 2:
					default:
						x = inset;
						break;

					case 0:
						x = d.width / 2;
						break;

					case 4:
						x = d.width - inset;
						break;
				}

				switch (position / 10) {
					case 1:
					default:
						y = inset;
						break;

					case 2:
						y = d.height / 2;
						break;

					case 3:
						y = d.height - inset;
						break;
				}

				ImageLoaderUtil.WrapInfo wrapInfo = ImageLoaderUtil.wrap(g, text, d.width);

				infoImage[id] = new BufferedImage(wrapInfo.width + thickness * 2,
						wrapInfo.height + thickness * 2, BufferedImage.TYPE_INT_ARGB);

				Graphics2D g2 = (Graphics2D) infoImage[id].getGraphics();
				g2.setFont(g.getFont());
				infoLocation[id] = ImageLoaderUtil.paintAlignedOutline(
						g2, x, y, thickness, position, wrapInfo, true);
				Log.log(Log.LEVEL_TRACE, MODULE, "Cached info " + id + " - " + text);
			}

			Composite composite = g.getComposite();

			if (transition && imageAlpha != 1 && previousInfoImage[id] != null) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - imageAlpha));
				g.drawImage(previousInfoImage[id], previousInfoLocation[id].x, previousInfoLocation[id].y, this);
			}

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transition?imageAlpha:1));
			g.drawImage(infoImage[id], infoLocation[id].x, infoLocation[id].y, this);

			g.setComposite(composite);

			if (imageAlpha >= 1) {
				previousInfoImage[id] = infoImage[id];
				previousInfoLocation[id] = infoLocation[id];
			}
		}

		private void drawHelp(Graphics g, Color hilight, FontMetrics fm, int x, int y, String text) {
			if ((feedback & FEEDBACK_HELP) != FEEDBACK_HELP) return;

			g.setColor(hilight);
			Rectangle2D bounds = fm.getStringBounds(text, g);
			g.drawString(text, (int) (x - bounds.getWidth() / 2), y);
		}
	}
}
