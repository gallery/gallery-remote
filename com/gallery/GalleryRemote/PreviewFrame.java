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

	public static void paintOutline(Graphics g, String s, int textX, int textY, int width) {
		g.setColor(darkGray);
		g.drawString(s, textX + width, textY + width);
		g.drawString(s, textX, textY + width);
		g.drawString(s, textX - width, textY + width);
		g.drawString(s, textX + width, textY);
		g.drawString(s, textX, textY);
		g.drawString(s, textX - width, textY);
		g.drawString(s, textX + width, textY - width);
		g.drawString(s, textX, textY - width);
		g.drawString(s, textX - width, textY - width);
		g.setColor(Color.white);
		g.drawString(s, textX, textY);
	}

	class CropGlassPane extends JComponent implements MouseListener, MouseMotionListener {
		Color background = new Color(100, 100, 100, 150);
		boolean inDrag;
		Point2D start = null, end = null, moveCropStart = null;
		Rectangle oldRect = null;
		Rectangle cacheRect = null;
		boolean centerMode = false;
		Picture localCurrentPicture = null;

		int movingEdge = 0;  // use SwingConstants
		public static final int TOLERANCE = 5;

		public CropGlassPane() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		public void paint(Graphics g) {
			oldRect = null;

			if (localCurrentPicture != currentPicture) {
				cacheRect = null;
				localCurrentPicture = currentPicture;
			}

			if (currentPicture == null || currentImage == null || currentPicture.isOnline()) {
				cacheRect = null;
				return;
			}

			if (imageRect != null && start != null && end != null) {
				Rectangle ct = currentPicture.getCropTo();
				if (ct != null) {
					AffineTransform t = ImageUtils.createTransform(getBounds(), imageRect, currentPicture.getDimension(), currentPicture.getAngle(), currentPicture.isFlipped());

					try {
						cacheRect = getRect(t.inverseTransform(ct.getLocation(), null),
								t.inverseTransform(new Point(ct.x + ct.width, ct.y + ct.height), null));

						g.setColor(background);
						g.setClip(imageRect);
						g.fillRect(0, 0, cacheRect.x, getHeight());
						g.fillRect(cacheRect.x, 0, getWidth() - cacheRect.x, cacheRect.y);
						g.fillRect(cacheRect.x, cacheRect.y + cacheRect.height, getWidth() - cacheRect.x, getHeight() - cacheRect.y - cacheRect.height);
						g.fillRect(cacheRect.x + cacheRect.width, cacheRect.y, getWidth() - cacheRect.x - cacheRect.width, cacheRect.height);

						g.setColor(Color.black);
						g.drawRect(cacheRect.x,  cacheRect.y, cacheRect.width, cacheRect.height);

						g.setClip(null);
					} catch (NoninvertibleTransformException e) {
						Log.logException(Log.LEVEL_ERROR, MODULE, e);
					}
				} else {
					if (movingEdge == 0) {
						// only blank the cacheRect if we're not busy modifying it
						cacheRect = null;
					}
				}
			} else {
				cacheRect = null;
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
				if (movingEdge == 0) {
					message = GRI18n.getString(MODULE, "inCrop");
				} else {
					message = GRI18n.getString(MODULE, "inModify");
				}
			}

			g.setFont(g.getFont());
			paintOutline(g, message, 5, getBounds().height - 5, 1);
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
			cacheRect = null;
			repaint();
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			if (currentPicture == null || imageRect == null || currentPicture.isOnline()) {
				return;
			}

			if (cacheRect == null) {
				movingEdge = 0;
			}

			inDrag = true;

			switch (movingEdge) {
				case SwingConstants.TOP:
					// keep bottom-right
					start = validate(new Point(cacheRect.x + cacheRect.width, cacheRect.y + cacheRect.height));
					break;

				case SwingConstants.BOTTOM:
					// keep top-left
					start = validate(new Point(cacheRect.x, cacheRect.y));
					break;

				case SwingConstants.LEFT:
					// keep bottom-right
					start = validate(new Point(cacheRect.x + cacheRect.width, cacheRect.y + cacheRect.height));
					break;

				case SwingConstants.RIGHT:
					// keep top-left
					start = validate(new Point(cacheRect.x, cacheRect.y));
					break;

				case 5:
					// moving: just remember start for offset
					moveCropStart = validate(e.getPoint());
					break;

				default:
					// new rectangle
					start = validate(e.getPoint());
					break;
			}

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

			setCursor(Cursor.getDefaultCursor());

			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if (imageRect == null) {
				return;
			}

			if (cacheRect == null) {
				movingEdge = 0;
			}

			int modifiers = e.getModifiersEx();

			Point2D p;
			switch (movingEdge) {
				case SwingConstants.TOP:
					p = validate(new Point(cacheRect.x, (int) (e.getPoint().getY())));
					modifiers = 0;
					break;

				case SwingConstants.BOTTOM:
					p = validate(new Point(cacheRect.x + cacheRect.width, (int) (e.getPoint().getY())));
					modifiers = 0;
					break;

				case SwingConstants.LEFT:
					p = validate(new Point((int) (e.getPoint().getX()), cacheRect.y));
					modifiers = 0;
					break;

				case SwingConstants.RIGHT:
					p = validate(new Point((int) (e.getPoint().getX()), cacheRect.y + cacheRect.height));
					modifiers = 0;
					break;

				case 5:
					double dx = e.getPoint().getX() - moveCropStart.getX();
					double dy = e.getPoint().getY() - moveCropStart.getY();

					start = validate(new Point((int) (cacheRect.x + dx), (int) (cacheRect.y + dy)));
					p = new Point((int) (cacheRect.x + cacheRect.width + dx), (int) (cacheRect.y + cacheRect.height + dy));
					modifiers = 0;
					break;

				default:
					// new rectangle
					p = validate(e.getPoint());
					break;
			}

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

				// reverse rectangle
				Dimension target;
				int sameOrientation = (Math.abs(dx) - Math.abs(dy)) * (imageRect.width - imageRect.height);
				if (sameOrientation > 0) {
					target = new Dimension(dx, dy);
				} else {
					target = new Dimension(dy, dx);
				}

				Dimension d = ImageUtils.getSizeKeepRatio(imageRect.getSize(),
						target, false);

				if (sameOrientation > 0) {
					p.setLocation(start.getX() + d.width, start.getY() + d.height);
				} else {
					p.setLocation(start.getX() + d.height, start.getY() + d.width);
				}
			}

			centerMode = (modifiers & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK;

			end = validate(p);

			updateRect();
		}

		public void mouseMoved(MouseEvent e) {
			if (currentPicture == null || currentImage == null || currentPicture.isOnline() || cacheRect == null) {
				movingEdge = 0;
				setCursor(Cursor.getDefaultCursor());
				return;
			}

			double px = e.getPoint().getX();
			double py = e.getPoint().getY();

			boolean canMove = false;

			if (px >= cacheRect.x + TOLERANCE && px <= cacheRect.x + cacheRect.width - TOLERANCE) {
				if (Math.abs(py - cacheRect.y) < TOLERANCE) {
					movingEdge = SwingConstants.TOP;
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
					canMove = true;
				} else if (Math.abs(py - cacheRect.y - cacheRect.height) < TOLERANCE) {
					movingEdge = SwingConstants.BOTTOM;
					setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
					canMove = true;
				}
			}

			if (py >= cacheRect.y + TOLERANCE && py <= cacheRect.y + cacheRect.height - TOLERANCE) {
				if (Math.abs(px - cacheRect.x) < TOLERANCE) {
					movingEdge = SwingConstants.LEFT;
					setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
					canMove = true;
				} else if (Math.abs(px - cacheRect.x - cacheRect.width) < TOLERANCE) {
					movingEdge = SwingConstants.RIGHT;
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					canMove = true;
				}
			}

			if (px >= cacheRect.x + TOLERANCE && px <= cacheRect.x + cacheRect.width - TOLERANCE
					&& py >= cacheRect.y + TOLERANCE && py <= cacheRect.y + cacheRect.height - TOLERANCE) {
				movingEdge = 5;
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				canMove = true;
			}

			if (!canMove) {
				movingEdge = 0;
				setCursor(Cursor.getDefaultCursor());
			}
		}

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
