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
import com.gallery.GalleryRemote.util.ImageLoaderUtil;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.event.*;

public class PreviewFrame
		extends JFrame
		implements PreferenceNames, ImageLoaderUtil.ImageLoaderUser {
	public static final String MODULE = "PreviewFrame";
	Rectangle currentRect = null;
	ImageLoaderUtil loader;

	public void initComponents() {
		setTitle(GRI18n.getString(MODULE, "title"));
		setIconImage(GalleryRemote._().getMainFrame().getIconImage());

		setBounds(GalleryRemote._().properties.getPreviewBounds());
		setContentPane(new ImageContentPane());

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				loader.flushMemory();
			}
		});

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowActivated(java.awt.event.WindowEvent e) {
				MainFrame mainFrame = (MainFrame) GalleryRemote._().getMainFrame();

				if (mainFrame.activating == PreviewFrame.this) {
					mainFrame.activating = null;
					return;
				}

				if (mainFrame.activating == null && mainFrame.isVisible()) {
					/*WindowListener mfWindowListener = mainFrame.getWindowListeners()[0];
					WindowListener pWindowListener = getWindowListeners()[0];
					removeWindowListener(pWindowListener);
					mainFrame.removeWindowListener(mfWindowListener);*/

					mainFrame.activating = PreviewFrame.this;
					mainFrame.toFront();
					toFront();

					/*addWindowListener(pWindowListener);
					mainFrame.addWindowListener(mfWindowListener);*/
				}
			}
		});

		CropGlassPane glass = new CropGlassPane();
		setGlassPane(glass);
		glass.setVisible(true);
		loader = new ImageLoaderUtil(
				GalleryRemote._().properties.getIntProperty("cacheSize", 10),
				this);
	}

	public void hide() {
		// release memory if no longer necessary
		loader.flushMemory();
		super.hide();

		loader.preparePicture(null, false, false);
	}

	public void pictureReady() {
		repaint();
	}

	public boolean blockPictureReady(Image image, Picture picture) {
		return false;
	}

	public Dimension getImageSize() {
		return getSize();
	}

	public void nullRect() {
		currentRect = null;
	}

	public void pictureStartDownloading(Picture picture) {}
	public void pictureStartProcessing(Picture picture) {}

	class ImageContentPane extends JPanel {
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Color c = GalleryRemote._().properties.getColorProperty(SLIDESHOW_COLOR);
			if (c != null) {
				g.setColor(c);
			} else {
				g.setColor(getBackground());
			}

			g.fillRect(0, 0, getSize().width, getSize().height);

			if (loader.imageShowNow != null && loader.pictureShowWant != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "New image: " + loader.imageShowNow);

				Image tmpImage = ImageUtils.rotateImage(loader.imageShowNow, loader.pictureShowWant.getAngle(),
						loader.pictureShowWant.isFlipped(), this);

				currentRect = new Rectangle(getLocation().x + (getWidth() - tmpImage.getWidth(this)) / 2,
						getLocation().y + (getHeight() - tmpImage.getHeight(this)) / 2,
						tmpImage.getWidth(this), tmpImage.getHeight(this));

				g2.drawImage(tmpImage, currentRect.x, currentRect.y, getContentPane());
			}
		}
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

			if (localCurrentPicture != loader.pictureShowNow) {
				cacheRect = null;
				localCurrentPicture = loader.pictureShowNow;
			}

			if (loader.pictureShowNow == null || loader.imageShowNow == null || loader.pictureShowNow.isOnline()) {
				cacheRect = null;
				return;
			}

			if (currentRect != null && start != null && end != null) {
				Rectangle ct = loader.pictureShowNow.getCropTo();
				if (ct != null) {
					AffineTransform t = ImageUtils.createTransform(
							getBounds(),
							currentRect,
							loader.pictureShowNow.getDimension(),
							loader.pictureShowNow.getAngle(),
							loader.pictureShowNow.isFlipped());

					try {
						cacheRect = getRect(t.inverseTransform(ct.getLocation(), null),
								t.inverseTransform(new Point(ct.x + ct.width, ct.y + ct.height), null));

						g.setColor(background);
						g.setClip(currentRect);
						g.fillRect(0, 0, cacheRect.x, getHeight());
						g.fillRect(cacheRect.x, 0, getWidth() - cacheRect.x, cacheRect.y);
						g.fillRect(cacheRect.x, cacheRect.y + cacheRect.height, getWidth() - cacheRect.x, getHeight() - cacheRect.y - cacheRect.height);
						g.fillRect(cacheRect.x + cacheRect.width, cacheRect.y, getWidth() - cacheRect.x - cacheRect.width, cacheRect.height);

						g.setColor(Color.black);
						g.drawRect(cacheRect.x,  cacheRect.y, cacheRect.width, cacheRect.height);

						if (GalleryRemote._().properties.getBooleanProperty(PREVIEW_DRAW_THIRDS, false)) {
							g.setColor(background);
							g.drawLine(cacheRect.x + cacheRect.width / 3, cacheRect.y,
									cacheRect.x + cacheRect.width / 3, cacheRect.y + cacheRect.height);
							g.drawLine(cacheRect.x + cacheRect.width *2 / 3, cacheRect.y,
									cacheRect.x + cacheRect.width * 2 / 3, cacheRect.y + cacheRect.height);
							g.drawLine(cacheRect.x, cacheRect.y + cacheRect.height / 3,
									cacheRect.x + cacheRect.width, cacheRect.y  + cacheRect.height / 3);
							g.drawLine(cacheRect.x, cacheRect.y + cacheRect.height * 2 / 3,
									cacheRect.x + cacheRect.width, cacheRect.y  + cacheRect.height * 2 / 3);
						}

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

			Rectangle cropTo = loader.pictureShowNow.getCropTo();
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
			ImageLoaderUtil.paintOutline(g, message, 5, getBounds().height - 5, 1);
		}

		public void updateRect() {
			Graphics g = getGraphics();

			if (oldRect != null) {
				g.setXORMode(Color.cyan);
				g.drawRect(oldRect.x, oldRect.y, oldRect.width, oldRect.height);

				if (GalleryRemote._().properties.getBooleanProperty(PREVIEW_DRAW_THIRDS, false)) {
					g.drawLine(oldRect.x + oldRect.width / 3, oldRect.y,
							oldRect.x + oldRect.width / 3, oldRect.y + oldRect.height);
					g.drawLine(oldRect.x + oldRect.width *2 / 3, oldRect.y,
							oldRect.x + oldRect.width * 2 / 3, oldRect.y + oldRect.height);
					g.drawLine(oldRect.x, oldRect.y + oldRect.height / 3,
							oldRect.x + oldRect.width, oldRect.y  + oldRect.height / 3);
					g.drawLine(oldRect.x, oldRect.y + oldRect.height * 2 / 3,
							oldRect.x + oldRect.width, oldRect.y  + oldRect.height * 2 / 3);
				}
			}

			if (inDrag) {
				g.setXORMode(Color.cyan);
				oldRect = getRect(start, end);
				g.drawRect(oldRect.x, oldRect.y, oldRect.width, oldRect.height);

				if (GalleryRemote._().properties.getBooleanProperty(PREVIEW_DRAW_THIRDS, false)) {
					g.drawLine(oldRect.x + oldRect.width / 3, oldRect.y,
							oldRect.x + oldRect.width / 3, oldRect.y + oldRect.height);
					g.drawLine(oldRect.x + oldRect.width *2 / 3, oldRect.y,
							oldRect.x + oldRect.width * 2 / 3, oldRect.y + oldRect.height);
					g.drawLine(oldRect.x, oldRect.y + oldRect.height / 3,
							oldRect.x + oldRect.width, oldRect.y  + oldRect.height / 3);
					g.drawLine(oldRect.x, oldRect.y + oldRect.height * 2 / 3,
							oldRect.x + oldRect.width, oldRect.y  + oldRect.height * 2 / 3);
				}
			}
		}

		public void mouseClicked(MouseEvent e) {
			if (loader.pictureShowNow == null) {
				return;
			}

			loader.pictureShowNow.setCropTo(null);
			cacheRect = null;
			repaint();
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			if (loader.pictureShowNow == null || currentRect == null || loader.pictureShowNow.isOnline()) {
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
					// moving: just remember transitionStart for offset
					moveCropStart = validate(e.getPoint());
					break;

				default:
					// new rectangle
					start = validate(e.getPoint());
					break;
			}

			loader.pictureShowNow.setCropTo(null);
			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			inDrag = false;
			centerMode = false;

			if (loader.pictureShowNow == null || oldRect == null || loader.pictureShowNow.isOnline()) {
				return;
			}

			AffineTransform t = ImageUtils.createTransform(
					getBounds(),
					currentRect,
					loader.pictureShowNow.getDimension(),
					loader.pictureShowNow.getAngle(),
					loader.pictureShowNow.isFlipped());
			//pictureShowNow.setCropTo(getRect(t.transform(transitionStart, null), t.transform(end, null)));

			Rectangle tmpRect = new Rectangle();
			tmpRect.setFrameFromDiagonal(t.transform(oldRect.getLocation(), null),
					t.transform(new Point(oldRect.x + oldRect.width, oldRect.y + oldRect.height), null));
			loader.pictureShowNow.setCropTo(tmpRect);

			setCursor(Cursor.getDefaultCursor());

			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if (currentRect == null) {
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
				int sameOrientation = (Math.abs(dx) - Math.abs(dy)) * (currentRect.width - currentRect.height);
				if (sameOrientation > 0) {
					target = new Dimension(dx, dy);
				} else {
					target = new Dimension(dy, dx);
				}

				Dimension d = ImageUtils.getSizeKeepRatio(currentRect.getSize(),
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
			if (loader.pictureShowNow == null || loader.imageShowNow == null || loader.pictureShowNow.isOnline() || cacheRect == null) {
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

			if (px < currentRect.x) {
				px = currentRect.x;
			}

			if (py < currentRect.y) {
				py = currentRect.y;
			}

			if (px > currentRect.x + currentRect.width - 1) {
				px = currentRect.x + currentRect.width - 1;
			}

			if (py > currentRect.y + currentRect.height - 1) {
				py = currentRect.y + currentRect.height - 1;
			}

			return new Point2D.Double(px, py);
		}
	}
}
