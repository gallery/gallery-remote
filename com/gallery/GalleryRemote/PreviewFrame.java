/*
 * Gallery Remote - a File Upload Utility for Gallery
 *
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2001 Bharat Mediratta
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PreviewFrame extends JFrame implements PreferenceNames {
	public static final String MODULE = "PreviewFrame";

	SmartHashtable imageIcons = new SmartHashtable();
	ImageIcon currentImage = null;
	Picture loadPicture = null;
	Picture currentPicture = null;
	PreviewLoader previewLoader = new PreviewLoader();
	int previewCacheSize = 10;
	boolean ignoreIMFailure = false;
	CancellableTransferListener listener = null;
	Rectangle imageRect = null;

	public static final Color darkGray  = new Color(64, 64, 64, 128);

	public void initComponents() {
		setTitle(GRI18n.getString(MODULE, "title"));
		setIconImage(GalleryRemote._().getMainFrame().getIconImage());

		setBounds(GalleryRemote._().properties.getPreviewBounds());
		setContentPane(new ImageContentPane());

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				flushMemory();
			}
		});

		previewCacheSize = GalleryRemote._().properties.getIntProperty("previewCacheSize");

		CropGlassPane glass = new CropGlassPane();
		setGlassPane(glass);
		glass.setVisible(true);
	}

	public void hide() {
		// release memory if no longer necessary
		flushMemory();
		super.hide();

		displayPicture(null, true);
	}

	public void flushMemory() {
		imageIcons.clear();
		if (currentPicture != null) {
			loadPicture = null;
			currentImage = null;
			displayPicture(currentPicture, true);
			currentPicture = null;
		}
	}

	public void displayPicture(Picture picture, boolean async) {
		if (picture == null) {
			loadPicture = null;
			imageRect = null;

			pictureReady(null, null);
		} else {
			//String filename = picture.getSource().getPath();
			
			if (picture != loadPicture) {
				//currentImageFile = filename;
				loadPicture = picture;

				ImageIcon r = (ImageIcon) imageIcons.get(picture);
				if (r != null) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Cache hit: " + picture);
					//currentImage = r;
					//currentPicture = picture;
					pictureReady(r, picture);
				} else {
					Log.log(Log.LEVEL_TRACE, MODULE, "Cache miss: " + picture);
					if (async) {
						previewLoader.loadPreview(picture, true);
					} else {
						//currentImage = getSizedIconForce(picture);
						//currentPicture = picture;
						ImageIcon sizedIcon = getSizedIconForce(picture);
						if (sizedIcon != null) {
							pictureReady(sizedIcon, picture);
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
					pictureStartDownload(picture);

					File f = ImageUtils.download(picture, getRootPane().getSize(), GalleryRemote._().getCore().getMainStatusUpdate(), listener);

					pictureStartProcessing(picture);

					if (f != null) {
						r = ImageUtils.load(
								f.getPath(),
								getRootPane().getSize(),
								ImageUtils.PREVIEW, ignoreIMFailure);
					} else {
						return null;
					}
				} else {
					pictureStartProcessing(picture);

					r = ImageUtils.load(
							picture.getSource().getPath(),
							getRootPane().getSize(),
							ImageUtils.PREVIEW, ignoreIMFailure);
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Adding to cache: " + picture);
				imageIcons.put(picture, r);
			}
		}

		return r;
	}

	class ImageContentPane extends JPanel {
		public void paintComponent(Graphics g) {
			Color c = GalleryRemote._().properties.getColorProperty(SLIDESHOW_COLOR);
			if (c != null) {
				g.setColor(c);
			} else {
				g.setColor(getBackground());
			}
			g.fillRect(0, 0, getSize().width, getSize().height);
			//g.clearRect(0, 0, getSize().width, getSize().height);

			if (currentImage != null && loadPicture != null) {
				ImageIcon tmpImage = ImageUtils.rotateImageIcon(currentImage, loadPicture.getAngle(),
						loadPicture.isFlipped(), this);

				imageRect = new Rectangle(getLocation().x + (getWidth() - tmpImage.getIconWidth()) / 2,
						getLocation().y + (getHeight() - tmpImage.getIconHeight()) / 2,
						tmpImage.getIconWidth(), tmpImage.getIconHeight());
				tmpImage.paintIcon(getContentPane(), g, imageRect.x, imageRect.y	);
			}
		}
	}

	class PreviewLoader implements Runnable {
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

		public void loadPreview(Picture picture, boolean notify) {
			Log.log(Log.LEVEL_TRACE, MODULE, "loadPreview " + picture);

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
			else */if (previewCacheSize > 0 && touchOrder.size() > previewCacheSize) {
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

	public void pictureReady(ImageIcon image, Picture picture) {
		currentImage = image;
		currentPicture = picture;
		repaint();
	}

	public void pictureStartDownload(Picture picture) {}

	public void pictureStartProcessing(Picture picture) {}

	public static void paintOutline(Graphics g, String s, int textX, int textY) {
		g.setColor(darkGray);
		g.drawString(s, textX + 1, textY + 1);
		g.drawString(s, textX, textY + 1);
		g.drawString(s, textX - 1, textY + 1);
		g.drawString(s, textX + 1, textY);
		g.drawString(s, textX, textY);
		g.drawString(s, textX - 1, textY);
		g.drawString(s, textX + 1, textY - 1);
		g.drawString(s, textX, textY - 1);
		g.drawString(s, textX - 1, textY - 1);
		g.setColor(Color.white);
		g.drawString(s, textX, textY);
	}

	class CropGlassPane extends JComponent implements MouseListener, MouseMotionListener {
		Color background = new Color(100, 100, 100, 150);
		boolean inDrag;
		Point2D start = null, end = null;
		Rectangle oldRect = null;
		boolean centerMode = false;

		public CropGlassPane() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		public void paint(Graphics g) {
			oldRect = null;

			if (currentPicture == null || currentImage == null || currentPicture.isOnline()) {
				return;
			}

			if (imageRect != null && start != null && end != null) {
				Rectangle ct = currentPicture.getCropTo();
				if (ct != null) {
					AffineTransform t = ImageUtils.createTransform(getBounds(), imageRect, currentPicture.getDimension(), currentPicture.getAngle(), currentPicture.isFlipped());

					Rectangle r = null;
					try {
						r = getRect(t.inverseTransform(ct.getLocation(), null),
								t.inverseTransform(new Point(ct.x + ct.width, ct.y + ct.height), null));

						g.setColor(background);
						g.setClip(imageRect);
						g.fillRect(0, 0, r.x, getHeight());
						g.fillRect(r.x, 0, getWidth() - r.x, r.y);
						g.fillRect(r.x, r.y + r.height, getWidth() - r.x, getHeight() - r.y - r.height);
						g.fillRect(r.x + r.width, r.y, getWidth() - r.x - r.width, r.height);

						g.setColor(Color.black);
						g.drawRect(r.x,  r.y, r.width, r.height);

						g.setClip(null);
					} catch (NoninvertibleTransformException e) {
						Log.logException(Log.LEVEL_ERROR, MODULE, e);
					}
				}
			}

			paintInfo(g);
		}

		public void paintInfo(Graphics g) {
			String message = null;

			Rectangle cropTo = currentPicture.getCropTo();
			if (! inDrag) {
				if (cropTo == null) {
					message = GRI18n.getString(MODULE, "noCrop");
				} else {
					message = GRI18n.getString(MODULE, "crop");
				}
			} else {
				message = GRI18n.getString(MODULE, "inCrop");;
			}

			g.setFont(g.getFont());
			paintOutline(g, message, 5, getBounds().height - 5);
		}

		public void updateRect() {
			Graphics g = getGraphics();

			if (oldRect != null) {
				g.setXORMode(Color.cyan);
				g.drawRect(oldRect.x, oldRect.y, oldRect.width, oldRect.height);
			}

			if (inDrag) {
				g.setXORMode(Color.cyan);
				oldRect = getRect(start, end);
				g.drawRect(oldRect.x, oldRect.y, oldRect.width, oldRect.height);
			}
		}

		public void mouseClicked(MouseEvent e) {
			if (currentPicture == null) {
				return;
			}

			currentPicture.setCropTo(null);
			repaint();
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			if (currentPicture == null || imageRect == null || currentPicture.isOnline()) {
				return;
			}

			inDrag = true;

			start = validate(e.getPoint());

			currentPicture.setCropTo(null);
			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			inDrag = false;
			centerMode = false;

			if (currentPicture == null || oldRect == null || currentPicture.isOnline()) {
				return;
			}

			AffineTransform t = ImageUtils.createTransform(getBounds(), imageRect, currentPicture.getDimension(), currentPicture.getAngle(), currentPicture.isFlipped());
			//currentPicture.setCropTo(getRect(t.transform(start, null), t.transform(end, null)));

			Rectangle tmpRect = new Rectangle();
			tmpRect.setFrameFromDiagonal(t.transform(oldRect.getLocation(), null),
					t.transform(new Point(oldRect.x + oldRect.width, oldRect.y + oldRect.height), null));
			currentPicture.setCropTo(tmpRect);

			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if (imageRect == null) {
				return;
			}

			Point2D p = validate(e.getPoint());

			int modifiers = e.getModifiersEx();
			double px = p.getX();
			double py = p.getY();
			if ((modifiers & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {
				// constrain to a square
				double dx = px - start.getX();
				double dy = py - start.getY();

				if (Math.abs(dx) < Math.abs(dy)) {
					py = start.getY() + (dy*dx > 0?dx:-dx);
				} else {
					px = start.getX() + (dx*dy > 0?dy:-dy);
				}

				p.setLocation(px, py);
			} else if ((modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
				// constrain to same aspect ratio
				int dx = (int) (px - start.getX());
				int dy = (int) (py - start.getY());

				Dimension target = new Dimension(dx, dy);
				Dimension d = ImageUtils.getSizeKeepRatio(imageRect.getSize(),
						target, false);

				p.setLocation(start.getX() + d.width, start.getY() + d.height);
			}

			centerMode = (modifiers & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK;

			end = validate(p);

			updateRect();
		}

		public void mouseMoved(MouseEvent e) {}

		public Rectangle getRect(Point2D p1, Point2D p2) {
			Rectangle r = new Rectangle();
			if (centerMode) {
				r.setFrameFromCenter(p1, p2);
				p1 = new Point2D.Double(r.getMinX(), r.getMinY());
				p2 = new Point2D.Double(r.getMaxX(), r.getMaxY());
			}

			r.setFrameFromDiagonal(validate(p1), validate(p2));

			return r;
		}

		public Point2D validate(Point2D p) {
			double px = p.getX();
			double py = p.getY();

			if (px < imageRect.x) {
				px = imageRect.x;
			}

			if (py < imageRect.y) {
				py = imageRect.y;
			}

			if (px > imageRect.x + imageRect.width - 1) {
				px = imageRect.x + imageRect.width - 1;
			}

			if (py > imageRect.y + imageRect.height - 1) {
				py = imageRect.y + imageRect.height - 1;
			}

			return new Point2D.Double(px, py);
		}
	}
}
