package com.gallery.GalleryRemote;

import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 *  AboutPanel: scrolling panel of credits for About boxes
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class AboutPanel extends JComponent {
	public static int TOP = 120;
	public static int BOTTOM = 30;
	ImageIcon image;
	Vector text;
	int scrollPosition;
	AnimationThread thread;
	int maxWidth;
	FontMetrics fm;


	public AboutPanel() {
		setFont(UIManager.getFont("Label.font"));
		fm = getFontMetrics(getFont());

		setForeground(new Color(96, 96, 96));
		image = new ImageIcon(getClass().getResource(
				"default.jpeg"));

		setBorder(new MatteBorder(1, 1, 1, 1, Color.gray));

		text = new Vector(50);
		StringTokenizer st = new StringTokenizer(
				GalleryRemote.getInstance().properties.getProperty( "aboutText" ), "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			text.addElement(line);
			maxWidth = Math.max(maxWidth,
					fm.stringWidth(line) + 10);
		}

		scrollPosition = -250;

		thread = new AnimationThread();
	}


	/**
	 *  Gets the preferredSize attribute of the AboutPanel object
	 *
	 *@return    The preferredSize value
	 *@since
	 */
	public Dimension getPreferredSize() {
		return new Dimension(1 + image.getIconWidth(),
				1 + image.getIconHeight());
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of Parameter
	 *@since
	 */
	public void paintComponent(Graphics g) {
		g.setColor(new Color(96, 96, 96));
		image.paintIcon(this, g, 1, 1);

		FontMetrics fm = g.getFontMetrics();

		String version = GalleryRemote.getInstance().properties.getProperty( "version" );
		g.drawString(version, (getWidth() - fm.stringWidth(version)) / 2,
				getHeight() - 5);

		g = g.create((getWidth() - maxWidth) / 2, TOP, maxWidth,
				getHeight() - TOP - BOTTOM);

		int height = fm.getHeight();
		int firstLine = scrollPosition / height;

		int firstLineOffset = height - scrollPosition % height;
		int lines = (getHeight() - TOP - BOTTOM) / height;

		int y = firstLineOffset;

		for (int i = 0; i <= lines; i++) {
			if (i + firstLine >= 0 && i + firstLine < text.size()) {
				String line = (String) text.get(i + firstLine);
				g.drawString(line, (maxWidth - fm.stringWidth(line)) / 2, y);
			}
			y += fm.getHeight();
		}
	}


	/**
	 *  Adds a feature to the Notify attribute of the AboutPanel object
	 *
	 *@since
	 */
	public void addNotify() {
		super.addNotify();
		thread.start();
	}


	/**
	 *  Description of the Method
	 *
	 *@since
	 */
	public void removeNotify() {
		super.removeNotify();
		thread.kill();
	}


	/**
	 *  Description of the Class
	 *
	 *@author     paour
	 *@created    August 16, 2002
	 */
	class AnimationThread extends Thread {
		private boolean running = true;


		AnimationThread() {
			super("About box animation thread");
			setPriority(Thread.MIN_PRIORITY);
		}


		/**
		 *  Description of the Method
		 *
		 *@since
		 */
		public void kill() {
			running = false;
		}


		/**
		 *  Main processing method for the AnimationThread object
		 *
		 *@since
		 */
		public void run() {
			FontMetrics fm = getFontMetrics(getFont());
			int max = (text.size() * fm.getHeight());

			while (running) {
				long start = System.currentTimeMillis();

				scrollPosition += 2;

				if (scrollPosition > max) {
					scrollPosition = -250;
				}

				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}

				repaint(getWidth() / 2 - maxWidth,
						TOP, maxWidth * 2,
						getHeight() - TOP - BOTTOM);
			}
		}
	}
}

