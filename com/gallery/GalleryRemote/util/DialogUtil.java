package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryRemote;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jun 2, 2003
 */
public class DialogUtil {
	public static void center(Window window, Window owner) {
		Rectangle sr = window.getGraphicsConfiguration().getBounds();
		Rectangle or;
		if (owner != null && owner.isVisible()) {
			or = owner.getBounds();
		} else {
			or = sr;
		}
		Dimension d = window.getSize();

		Rectangle r = new Rectangle();
		r.setSize(d);

		int x = (int) (or.getX() + or.getWidth() / 2 - d.getWidth() / 2);
		int y = (int) (or.getY() + or.getHeight() / 2 - d.getHeight() / 2);

		if (x < 0) x = 0;
		if (y < 0) y = 0;
		if (x + r.getWidth() > sr.getMaxX()) x = (int) (sr.getMaxX() - r.getWidth());
		if (y + r.getHeight() > sr.getMaxY()) y = (int) (sr.getMaxY() - r.getHeight());

		r.setLocation(x, y);

		window.setBounds(r);
	}

	public static void center(Window window) {
		center(window, null);
	}

	public static void maxSize(Window window) {
		Rectangle sr = window.getGraphicsConfiguration().getBounds();
		if (GalleryRemote.IS_MAC_OS_X) {
			// leave room for the menu bar
			sr.y += 22;
			sr.height -= 22;
		}

		window.setBounds(sr);
	}

	public static Frame findParentWindow(Component c) {
		if (c instanceof Frame) {
			return (Frame) c;
		}

		if (c.getParent() == null) {
			return null;
		}

		return findParentWindow(c.getParent());
	}
}
