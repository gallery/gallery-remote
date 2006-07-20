package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.CancellableTransferListener;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class ImageLoaderUtil implements PreferenceNames {
	public static final String MODULE = "ImgLoadrUtil";

	public SmartHashtable imageIcons = new SmartHashtable();
	public ImageIcon imageShowNow = null;
	public Picture pictureShowWant = null;
	public Picture pictureShowNow = null;
	public ImageLoader imageLoader = new ImageLoader();

	public static final Color darkGray  = new Color(64, 64, 64, 128);
	public static Pattern breaker = Pattern.compile("<(br|BR)\\s?\\/?>");
	public static Pattern stripper = Pattern.compile("<[^<>]*>");
	//public static Pattern spacer = Pattern.compile("[\r\n]");

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
		imageIcons.clear();
		if (pictureShowNow != null) {
			pictureShowWant = null;
			imageShowNow = null;
			preparePicture(pictureShowNow, true, true);
			pictureShowNow = null;
		}
	}

	public void pictureReady(ImageIcon image, Picture picture) {
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

				ImageIcon r = (ImageIcon) imageIcons.get(picture);
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
						ImageIcon sizedIcon = getSizedIconForce(picture);
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

	public ImageIcon getSizedIconForce(Picture picture) {
		ImageIcon r = (ImageIcon) imageIcons.get(picture);

		if (r == null) {
			synchronized(picture) {
				if (picture.isOnline()) {
					imageLoaderUser.pictureStartDownload(picture);

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

				Log.log(Log.LEVEL_TRACE, MODULE, "Adding to cache: " + picture);
				imageIcons.put(picture, r);
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
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(s, g);

		String[] ss = s.split("\n");

		ArrayList lines = new ArrayList(ss.length);

		for (int i = 0; i < ss.length; i++) {
			int linebreak = ss[i].length() - 1;
			while (linebreak != -1 && fm.getStringBounds(ss[i].substring(0, linebreak), g).getWidth() > wrapWidth) {
				linebreak = ss[i].lastIndexOf(' ', linebreak - 1);
			}

			if (linebreak != -1 && linebreak != ss[i].length() - 1) {
				lines.add(ss[i].substring(0, linebreak));
				ss[i] = ss[i].substring(linebreak + 1);
				i--;
			} else {
				lines.add(ss[i]);
			}
		}

		ss = (String[]) lines.toArray(new String[lines.size()]);

		int x;
		int y;

		int height = (int) bounds.getHeight() * ss.length;
		switch (position / 10) {
			case 1:
			default:
				y = textY;
				break;

			case 2:
				y = textY - height / 2;
				break;

			case 3:
				y = textY - height;
				break;
		}

		y += fm.getAscent();

		for (int i = 0; i < ss.length; i++) {
			Rectangle2D bounds1 = fm.getStringBounds(ss[i], g);
			switch (position % 10) {
				case 2:
				default:
					x = textX;
					break;

				case 0:
					x = textX - (int) bounds1.getWidth() / 2;
					break;

				case 4:
					x = textX - (int) bounds1.getWidth();
					break;
			}

			paintOutline(g, ss[i], x, y, thickness);

			y += bounds.getHeight();
		}
	}

	public static void paintOutline(Graphics g, String s, int textX, int textY, int thickness) {
		g.setColor(darkGray);
		g.drawString(s, textX + thickness, textY + thickness);
		g.drawString(s, textX, textY + thickness);
		g.drawString(s, textX - thickness, textY + thickness);
		g.drawString(s, textX + thickness, textY);
		g.drawString(s, textX, textY);
		g.drawString(s, textX - thickness, textY);
		g.drawString(s, textX + thickness, textY - thickness);
		g.drawString(s, textX, textY - thickness);
		g.drawString(s, textX - thickness, textY - thickness);
		g.setColor(Color.white);
		g.drawString(s, textX, textY);
	}

	public static void setSlideshowFont(Component c) {
		String fontName = GalleryRemote._().properties.getProperty(SLIDESHOW_FONTNAME);
		int fontSize = GalleryRemote._().properties.getIntProperty(SLIDESHOW_FONTSIZE, c.getFont().getSize());
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

		//m = spacer.matcher(m.replaceAll(""));
		//text = m.replaceAll(" ");

		return text;
	}

	public class ImageLoader implements Runnable {
		Picture picture;
		boolean stillRunning = false;
		boolean notify = false;

		public void run() {
			Log.log(Log.LEVEL_TRACE, MODULE, "Starting " + picture);
			Picture tmpPicture = null;
			ImageIcon tmpImage = null;
			while (picture != null) {
				synchronized (picture) {
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
				ImageIcon i = (ImageIcon) it.next();
				if (i != null) {
					i.getImage().flush();
				}
			}

			super.clear();
			touchOrder.clear();

			Runtime.getRuntime().gc();

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

			ImageIcon i = (ImageIcon) get(key, false);
			if (i != null) {
				i.getImage().flush();
			}

			remove(key);

			Runtime.getRuntime().gc();

			Log.log(Log.LEVEL_TRACE, MODULE, "Shrunk " + key);
		}
	}

	public interface ImageLoaderUser {
		public void pictureReady();
		public boolean blockPictureReady(ImageIcon image, Picture picture);
		public Dimension getImageSize();
		public void nullRect();
		public void pictureStartDownload(Picture picture);
		public void pictureStartProcessing(Picture picture);
	}
}
