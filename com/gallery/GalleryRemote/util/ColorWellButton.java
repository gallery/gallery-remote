/*
 * Gallery Remote - a File Upload Utility for Gallery
 *
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2004 Bharat Mediratta
 *
 * ColorWellButton.java - Shows color chooser when clicked
 *
 * Copyright (C) 2002 Slava Pestov
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
package com.gallery.GalleryRemote.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorWellButton extends JButton {
	public static final String MODULE = "ColorWell";

	public ColorWellButton(Color color) {
		setIcon(new ColorWell(color));
		setMargin(new Insets(2, 2, 2, 2));
		addActionListener(new ActionHandler());
	}

	public Color getSelectedColor() {
		return ((ColorWell) getIcon()).color;
	}

	public void setSelectedColor(Color color) {
		((ColorWell) getIcon()).color = color;
		repaint();
	}

	static class ColorWell implements Icon {
		Color color;

		ColorWell(Color color) {
			this.color = color;
		}

		public int getIconWidth() {
			return 35;
		}

		public int getIconHeight() {
			return 10;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (color == null)
				return;

			g.setColor(color);
			g.fillRect(x, y, getIconWidth(), getIconHeight());
			g.setColor(color.darker());
			g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
		}
	}

	class ActionHandler implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Frame parent = DialogUtil.findParentWindow(ColorWellButton.this);
			JDialog dialog;
			if (parent != null) {
				dialog = new ColorPickerDialog(parent,
						GRI18n.getString(MODULE, "colorChooserTitle"),
						true);
			} else {
				dialog = new ColorPickerDialog(JOptionPane.getFrameForComponent(ColorWellButton.this),
						GRI18n.getString(MODULE, "colorChooserTitle"),
						true);
			}
			dialog.pack();
			dialog.show();
		}
	}

	/**
	 * Replacement for the color picker dialog provided with Swing. This version
	 * supports dialog as well as frame parents.
	 *
	 * @since jEdit 4.1pre7
	 */
	private class ColorPickerDialog extends JDialog implements ActionListener {
		public ColorPickerDialog(Frame parent, String title, boolean modal) {
			super(parent, title, modal);

			init();
		}

		public ColorPickerDialog(Dialog parent, String title, boolean modal) {
			super(parent, title, modal);

			getContentPane().setLayout(new BorderLayout());

			init();
		}

		public void ok() {
			Color c = chooser.getColor();
			if (c != null)
				setSelectedColor(c);
			setVisible(false);
		}

		public void cancel() {
			setVisible(false);
		}

		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() == ok)
				ok();
			else
				cancel();
		}

		private JColorChooser chooser;
		private JButton ok;
		private JButton cancel;

		private void init() {
			Color c = getSelectedColor();
			if (c == null)
				chooser = new JColorChooser();
			else
				chooser = new JColorChooser(c);

			getContentPane().add(BorderLayout.CENTER, chooser);

			Box buttons = new Box(BoxLayout.X_AXIS);
			buttons.add(Box.createGlue());

			ok = new JButton(GRI18n.getString("Common", "OK"));
			ok.addActionListener(this);
			buttons.add(ok);
			buttons.add(Box.createHorizontalStrut(6));
			getRootPane().setDefaultButton(ok);
			cancel = new JButton(GRI18n.getString("Common", "Cancel"));
			cancel.addActionListener(this);
			buttons.add(cancel);
			buttons.add(Box.createGlue());

			getContentPane().add(BorderLayout.SOUTH, buttons);
			pack();
			setLocationRelativeTo(getParent());
		}
	}
}
