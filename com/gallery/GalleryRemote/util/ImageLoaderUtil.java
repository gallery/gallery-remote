package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.CancellableTransferListener;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class ImageLoaderUtil implements PreferenceNames {
	public static final String MODULE = "ImgLoadrUtil";

	public SmartHashtable images = new SmartHashtable();
	public Image imageShowNow = null;
	public Picture pictureShowWant = null;
	public Picture pictureShowNow = null;
	public ImageLoader imageLoader = new ImageLoader();

	public static Color[] darkGray = new Color[11];
	public static Pattern breaker = Pattern.compile("<(br|BR)\\s?\\/?>");
	public static Pattern stripper = Pattern.compile("<[^<>]*>");
	//public static Pattern mopper = Pattern.compile("\r");

	int cacheSize = 10;
	boolean ignoreIMFailure = false;
	CancellableTransferListener transferListener = null;
	ImageLoaderUser imageLoaderUser = null;

	public ImageLoaderUtil(int cacheSize, ImageLoaderUser imageLoaderUser) {
		this.cacheSize = cacheSize;
		this.imageLoaderUser = imageLoaderUser;
	}

	public void setTransferListener(CancellableTransferListener transferListener) {
		this.transferListener = transferListener;
	}

	public void flushMemory() {
		images.clear();
		if (pictureShowNow != null) {
			pictureShowWant = null;
			imageShowNow = null;
			preparePicture(pictureShowNow, true, true);
			pictureShowNow = null;
		}
	}

	public void reduceMemory() {
		Log.log(Log.LEVEL_TRACE, MODULE, "Free memory before reduction: " + Runtime.getRuntime().freeMemory());
		Log.log(Log.LEVEL_TRACE, MODULE, "Current image cache: " + images.size() + " - cache size " + cacheSize);

		if (images.size() > 1 && cacheSize > 1) {
			cacheSize = images.size() - 1;
		}

		images.shrink();

		Log.log(Log.LEVEL_TRACE, MODULE, "Free memory after reduction: " + Runtime.getRuntime().freeMemory());
	}

	public void pictureReady(Image image, Picture picture) {
		if (!imageLoaderUser.blockPictureReady(image, picture)) {
			imageShowNow = image;
			pictureShowNow = picture;

			imageLoaderUser.pictureReady();
		}
	}

	public void preparePicture(Picture picture, boolean async, boolean notify) {
		if (picture == null) {
			pictureShowWant = null;
			imageLoaderUser.nullRect();

			if (notify) {
				pictureReady(null, null);
			}
		} else {
			if (picture != pictureShowWant) {
				pictureShowWant = picture;

				Image r = (Image) images.get(picture);
				if (r != null) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Cache hit: " + picture);
					if (notify) {
						pictureReady(r, picture);
					}
				} else {
					Log.log(Log.LEVEL_TRACE, MODULE, "Cache miss: " + picture);
					if (async) {
						imageLoader.loadPicture(picture, true);
					} else {
						Image sizedIcon = getSizedIconForce(picture);
						if (sizedIcon != null) {
							if (notify) {
								pictureReady(sizedIcon, picture);
							}
						}
					}
				}
			}
		}
	}

	public Image getSizedIconForce(Picture picture) {
		Image r = (Image) images.get(picture);

		if (r == null) {
			synchronized(picture) {
				if (picture.isOnline()) {
					imageLoaderUser.pictureStartDownloading(picture);

					File f = ImageUtils.download(
							picture,
							imageLoaderUser.getImageSize(),
							GalleryRemote._().getCore().getMainStatusUpdate(),
							transferListener);

					imageLoaderUser.pictureStartProcessing(picture);

					if (f != null) {
						r = ImageUtils.load(
								f.getPath(),
								imageLoaderUser.getImageSize(),
								ImageUtils.PREVIEW, ignoreIMFailure);
					} else {
						return null;
					}
				} else {
					imageLoaderUser.pictureStartProcessing(picture);

					r = ImageUtils.load(
							picture.getSource().getPath(),
							imageLoaderUser.getImageSize(),
							ImageUtils.PREVIEW, ignoreIMFailure);
				}

				if (r == null) {
					imageLoaderUser.pictureLoadError(picture);
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Adding to cache: " + picture);
				images.put(picture, r);
			}
		}

		return r;
	}

	public static void paintAlignedOutline(
			Graphics g,
			String s,
			int textX, int textY,
			int thickness,
			int position,
			int wrapWidth) {
		paintAlignedOutline(g, textX, textY, thickness, position, wrap((Graphics2D) g, s, wrapWidth), false);
	}

	public static Point paintAlignedOutline(
			Graphics g,
			int textX, int textY,
			int thickness,
			int position,
			WrapInfo wrapInfo,
			boolean paintAtOrigin) {
		FontMetrics fm = g.getFontMetrics();

		Point boxPos = new Point();

		switch (position / 10) {
			case 1:
			default:
				boxPos.y = textY;
				break;

			case 2:
				boxPos.y = textY - wrapInfo.height / 2;
				break;

			case 3:
				boxPos.y = textY - wrapInfo.height;
				break;
		}

		switch (position % 10) {
			case 2:
			default:
				boxPos.x = textX;
				break;

			case 0:
				boxPos.x = textX - wrapInfo.width / 2;
				break;

			case 4:
				boxPos.x = textX - wrapInfo.width;
				break;
		}

		for (int i = 0; i < wrapInfo.lines.length; i++) {
			Rectangle2D bounds = fm.getStringBounds(wrapInfo.lines[i], g);
			Point linePos = new Point(boxPos);

			if (paintAtOrigin) {
				linePos.x = thickness;
				linePos.y = thickness;
			}

			switch (position % 10) {
				case 2:
				default:
					// nothing to do
					break;

				case 0:
					linePos.x += (wrapInfo.width - bounds.getWidth()) / 2;
					break;

				case 4:
					linePos.x += wrapInfo.width - bounds.getWidth();
					break;
			}

			paintOutline(g, wrapInfo.lines[i], linePos.x,
					(int) (linePos.y + fm.getAscent() + bounds.getHeight() * i), thickness);
		}

		return boxPos;
	}

	public static WrapInfo wrap(
			Graphics2D g,
			String s,
			int wrapWidth) {
		WrapInfo wrapInfo = new WrapInfo();

		FontMetrics fm = g.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(s, g);

		String[] ss = s.split("\n");

		ArrayList lines = new ArrayList(ss.length);

		for (int i = 0; i < ss.length; i++) {
			int linebreak = ss[i].length() - 1;
			while (linebreak != -1 &&
					fm.getStringBounds(ss[i].substring(0, linebreak), g).getWidth() > wrapWidth) {
				linebreak = ss[i].lastIndexOf(' ', linebreak - 1);
			}
			if (linebreak != -1 && linebreak != ss[i].length() - 1) {
				lines.add(ss[i].substring(0, linebreak));
				ss[i] = ss[i].substring(linebreak + 1);
				i--;
			} else {
				lines.add(ss[i]);
			}

			int width = (int) fm.getStringBounds((String) lines.get(lines.size() - 1), g).getWidth();
			if (width > wrapInfo.width) {
				wrapInfo.width = width;
			}
		}

		wrapInfo.lines = (String[]) lines.toArray(new String[lines.size()]);
		wrapInfo.height = (int) bounds.getHeight() * ss.length;

		return wrapInfo;
	}

	public static class WrapInfo {
		public String[] lines;
		public int width;
		public int height;

		public String toString() {
			final StringBuffer sb = new StringBuffer();
			sb.append("WrapInfo");
			sb.append("{lines=").append(lines == null ? "null" : Arrays.asList(lines).toString());
			sb.append(", width=").append(width);
			sb.append(", height=").append(height);
			sb.append('}');
			return sb.toString();
		}
	}

	public static void paintOutline(Graphics g, String s, int textX, int textY, int thickness) {
		if (thickness > 10) {
			thickness = 10;
		}

		if (thickness > 0) {
			g.setColor(getDarkGray(thickness));

			for (int i = -thickness; i <= thickness; i++) {
				for (int j = -thickness; j <= thickness; j++) {
					if (i != 0 || j != 0) {
						g.drawString(s, textX + i, textY + j);
					}
				}
			}
		}

		g.setColor(Color.white);
		g.drawString(s, textX, textY);
	}

	private static Color getDarkGray(int thickness) {
		if (darkGray[thickness] == null) {
			darkGray[thickness] = new Color(64, 64, 64, 255 / thickness / thickness);
		}

		return darkGray[thickness];
	}

	public static void setSlideshowFont(Component c) {
		String fontName = GalleryRemote._().properties.getProperty(SLIDESHOW_FONTNAME);
		int defaultFontSize =
				(int) DialogUtil.findParentWindow(c).getGraphicsConfiguration().getBounds().getHeight() / 40;
		int fontSize = GalleryRemote._().properties.getIntProperty(SLIDESHOW_FONTSIZE, defaultFontSize);
		Font f = null;
		if (fontName != null) {
			f = new Font(fontName, 0, fontSize);
		} else if (fontSize != c.getFont().getSize()) {
			f = c.getFont().deriveFont(fontSize + 0.0f);
		}

		if (f != null) {
			c.setFont(f);
		}
	}

	public static String stripTags(String text) {
		if (text == null) {
			return null;
		}

		Matcher m = breaker.matcher(text);
		text = m.replaceAll("\n");

		m = stripper.matcher(text);
		text = m.replaceAll("");

		//m = mopper.matcher(text);
		//text = m.replaceAll("");

		return text;
	}

	public class ImageLoader implements Runnable {
		Picture picture;
		boolean stillRunning = false;
		boolean notify = false;

		public void run() {
			Log.log(Log.LEVEL_TRACE, MODULE, "Starting " + picture);
			Picture tmpPicture = null;
			Image tmpImage = null;
			while (picture != null) {
				synchronized (this) {
					tmpPicture = picture;
					picture = null;
				}

				tmpImage = getSizedIconForce(tmpPicture);

				if (tmpImage == null) {
					notify = false;
				}
			}

			stillRunning = false;

			if (notify) {
				pictureReady(tmpImage, tmpPicture);
				notify = false;
			}

			Log.log(Log.LEVEL_TRACE, MODULE, "Ending");
		}

		public void loadPicture(Picture picture, boolean notify) {
			Log.log(Log.LEVEL_TRACE, MODULE, "loadPicture " + picture);

			this.picture = picture;

			if (notify) {
				this.notify = true;
			}

			if (!stillRunning) {
				stillRunning = true;
				Log.log(Log.LEVEL_TRACE, MODULE, "Calling Start");
				new Thread(this).start();
			}
		}
	}

	public class SmartHashtable extends HashMap {
		ArrayList touchOrder = new ArrayList();

		public Object put(Object key, Object value) {
			touch(key);
			super.put(key, value);

			//Log.log(Log.LEVEL_TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
			/*if (Runtime.getRuntime().freeMemory() < 2000000)
			{
				Log.log(Log.TRACE, MODULE, "Not enough free ram, shrinking...");
				shrink();
				Runtime.getRuntime().gc();
			}
			else */if (cacheSize > 0 && touchOrder.size() > cacheSize) {
				shrink();
			}
			//Log.log(Log.LEVEL_TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());

			return value;
		}

		public Object get(Object key) {
			return get(key, true);
		}

		public Object get(Object key, boolean touch) {
			Object result = super.get(key);

			if (result != null && touch) {
				touch(key);
			}

			return result;
		}

		public void clear() {
			//Log.log(Log.LEVEL_TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());

			// flush images before clearing hastables for quicker deletion
			Iterator it = values().iterator();
			while (it.hasNext()) {
				Image i = (Image) it.next();
				if (i != null) {
					//i.getGraphics().dispose();
					i.flush();
				}
			}

			super.clear();
			touchOrder.clear();

			System.runFinalization();
			System.gc();

			//Log.log(Log.LEVEL_TRACE, MODULE, Runtime.getRuntime().freeMemory() + " - " + Runtime.getRuntime().totalMemory());
		}

		public void touch(Object key) {
			Log.log(Log.LEVEL_TRACE, MODULE, "touch " + key);
			int i = touchOrder.indexOf(key);

			if (i != -1) {
				touchOrder.remove(i);
			}

			touchOrder.add(key);
		}

		public void shrink() {
			if (touchOrder.size() == 0) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Empty SmartHashtable");
				//throw new OutOfMemoryError();
				return;
			}

			Object key = touchOrder.get(0);
			touchOrder.remove(0);

			Image i = (Image) get(key, false);
			if (i != null) {
				//i.getGraphics().dispose();
				i.flush();
			}

			remove(key);

			Log.log(Log.LEVEL_TRACE, MODULE, "Shrunk " + key);
			if (cacheSize > 0 && size() > cacheSize) {
				shrink();
			} else {
				System.runFinalization();
				System.gc();
			}
		}
	}

	public interface ImageLoaderUser {
		public void pictureReady();
		public boolean blockPictureReady(Image image, Picture picture);
		public Dimension getImageSize();
		public void nullRect();
		public void pictureStartDownloading(Picture picture);
		public void pictureStartProcessing(Picture picture);
		public void pictureLoadError(Picture picture);
	}
}
