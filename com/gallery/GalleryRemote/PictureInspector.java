/*
*  Gallery Remote - a File Upload Utility for Gallery
*
*  Gallery - a web based photo album viewer and editor
*  Copyright (C) 2000-2001 Bharat Mediratta
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or (at
*  your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*  General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Bean inspector for Pictures
 *
 * @author paour
 */
public class PictureInspector extends JPanel
		implements ActionListener, DocumentListener {
	public static final String MODULE = "PictInspec";

	HashMap extraLabels = new HashMap();
	HashMap extraTextAreas = new HashMap();
	ArrayList currentExtraFields = null;

	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel8 = new JLabel();
	JPanel jSpacer = new JPanel();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();

	JScrollPane jScrollPane1 = new JScrollPane();
	JScrollPane jScrollPane2 = new JScrollPane();

	JButton jDeleteButton = new JButton();
	JButton jUpButton = new JButton();
	JButton jDownButton = new JButton();

	JPanel jIconAreaPanel = new JPanel();
	JTextArea jAlbum = new JTextArea();
	JTextArea jSize = new JTextArea();
	JTextArea jCaption = new JTextArea();
	JTextArea jPath = new JTextArea();

	MainFrame mf = null;
	Object[] pictures = null;
	int emptyIconHeight = 0;
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jIcon = new JLabel();
	JButton jRotateLeftButton = new JButton();
	JButton jFlipButton = new JButton();
	JButton jRotateRightButton = new JButton();

	static int FIRST_ROW_EXTRA = 8;

	/**
	 * Constructor for the PictureInspector object
	 */
	public PictureInspector() {
		jbInit();
		jbInitEvents();

		emptyIconHeight = (int) jIcon.getPreferredSize().getHeight();
		Log.log(Log.LEVEL_TRACE, MODULE, "emptyIconHeight: " + emptyIconHeight);
	}


	private void jbInit() {
		setLayout(new GridBagLayout());
		jLabel5.setText(GRI18n.getString(MODULE, "Path"));
		jLabel6.setText(GRI18n.getString(MODULE, "Album"));
		jLabel4.setText(GRI18n.getString(MODULE, "Caption"));
		jLabel8.setText(GRI18n.getString(MODULE, "Move"));
		jLabel1.setText(GRI18n.getString(MODULE, "Size"));
		jLabel2.setText(GRI18n.getString(MODULE, "Delete"));

		jAlbum.setRows(0);
		jAlbum.setEditable(false);
		jAlbum.setFont(UIManager.getFont("Label.font"));
		jAlbum.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jSize.setRows(0);
		jSize.setEditable(false);
		jSize.setFont(UIManager.getFont("Label.font"));
		jSize.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jCaption.setLineWrap(true);
		jCaption.setWrapStyleWord(true);
		jCaption.setEditable(false);
		jCaption.setFont(UIManager.getFont("Label.font"));
		jCaption.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jPath.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jPath.setFont(UIManager.getFont("Label.font"));
		jPath.setEditable(false);
		jPath.setLineWrap(true);

		setupKeyboardHandling(jCaption);

		jUpButton.setMaximumSize(new Dimension(120, 23));
		jUpButton.setMinimumSize(new Dimension(120, 23));
		jUpButton.setPreferredSize(new Dimension(120, 23));
		jUpButton.setToolTipText(GRI18n.getString(MODULE, "upBtnTip"));
		jUpButton.setText(GRI18n.getString(MODULE, "upBtn"));
		jUpButton.setActionCommand("Up");
		jUpButton.setHorizontalAlignment(SwingConstants.LEFT);
		jUpButton.setIcon(GalleryRemote.iUp);
		jDownButton.setMaximumSize(new Dimension(120, 23));
		jDownButton.setMinimumSize(new Dimension(120, 23));
		jDownButton.setPreferredSize(new Dimension(120, 23));
		jDownButton.setToolTipText(GRI18n.getString(MODULE, "dnBtnTip"));
		jDownButton.setText(GRI18n.getString(MODULE, "dnBtn"));
		jDownButton.setActionCommand("Down");
		jDownButton.setHorizontalAlignment(SwingConstants.LEFT);
		jDownButton.setIcon(GalleryRemote.iDown);
		jDeleteButton.setMaximumSize(new Dimension(120, 23));
		jDeleteButton.setMinimumSize(new Dimension(120, 23));
		jDeleteButton.setPreferredSize(new Dimension(120, 23));
		jDeleteButton.setToolTipText(GRI18n.getString(MODULE, "delBtnTip"));
		jDeleteButton.setActionCommand("Delete");
		jDeleteButton.setHorizontalAlignment(SwingConstants.LEFT);
		jDeleteButton.setText(GRI18n.getString(MODULE, "Delete"));
		jDeleteButton.setIcon(GalleryRemote.iDelete);

		jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setBorder(null);
		jScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane2.setBorder(null);
		jIconAreaPanel.setLayout(gridBagLayout1);

		jIcon.setHorizontalAlignment(SwingConstants.CENTER);
		jIcon.setHorizontalTextPosition(SwingConstants.CENTER);
		jIcon.setText(GRI18n.getString(MODULE, "icon"));
		jIcon.setVerticalTextPosition(SwingConstants.BOTTOM);
		jRotateLeftButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jRotateLeftButton.setToolTipText(GRI18n.getString(MODULE, "rotLtTip"));
		jRotateLeftButton.setActionCommand("Left");
		jRotateLeftButton.setIcon(GalleryRemote.iLeft);
		jRotateRightButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jRotateRightButton.setToolTipText(GRI18n.getString(MODULE, "rotRtTip"));
		jRotateRightButton.setActionCommand("Right");
		jRotateRightButton.setIcon(GalleryRemote.iRight);
		jFlipButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jFlipButton.setToolTipText(GRI18n.getString(MODULE, "flipTip"));
		jFlipButton.setActionCommand("Flip");
		jFlipButton.setIcon(GalleryRemote.iFlip);

		add(jLabel5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0));
		add(jLabel6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0));
		add(jLabel4, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 2, 0));
		add(jLabel8, new GridBagConstraints(0, 4, 1, 2, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0));
		add(jLabel1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0));
		add(jLabel2, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0));
		add(jSpacer, new GridBagConstraints(0, 99, 2, 1, 1.0, 0.1
				, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

		add(jIconAreaPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jIconAreaPanel.add(jIcon, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		if (ImageUtils.useJpegtran) {
			jIconAreaPanel.add(jRotateLeftButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
					, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jIconAreaPanel.add(jFlipButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
					, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jIconAreaPanel.add(jRotateRightButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}

		add(jAlbum, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(jSize, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		add(jUpButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0));
		add(jDownButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(jDeleteButton, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0));
		add(jScrollPane1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(jScrollPane2, new GridBagConstraints(1, 7, 1, 1, 1.0, 1.0
				, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));
		jScrollPane1.getViewport().add(jPath, null);
		jScrollPane2.getViewport().add(jCaption, null);

		this.setMinimumSize(new Dimension(150, 0));
	}

	private void setupKeyboardHandling(JComponent c) {
		c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), nextFocusAction.getValue(Action.NAME));
		c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), prevFocusAction.getValue(Action.NAME));
		c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), nextPictureAction.getValue(Action.NAME));
		c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), prevPictureAction.getValue(Action.NAME));

		c.getActionMap().put(nextFocusAction.getValue(Action.NAME), nextFocusAction);
		c.getActionMap().put(prevFocusAction.getValue(Action.NAME), prevFocusAction);
		c.getActionMap().put(nextPictureAction.getValue(Action.NAME), nextPictureAction);
		c.getActionMap().put(prevPictureAction.getValue(Action.NAME), prevPictureAction);
	}

	private void jbInitEvents() {
		jDeleteButton.addActionListener(this);
		jUpButton.addActionListener(this);
		jDownButton.addActionListener(this);
		jRotateLeftButton.addActionListener(this);
		jRotateRightButton.addActionListener(this);
		jFlipButton.addActionListener(this);
		jCaption.getDocument().addDocumentListener(this);
	}

	// Event handling
	/**
	 * Menu and button handling
	 *
	 * @param e Action event
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + command);

		if (command.equals("Delete")) {
            // We must call the MainFrame to delete pictures
            // so that it can know that the document is now dirty.
			mf.deleteSelectedPictures();
		} else if (command.equals("Up")) {
            // We must call the MainFrame to move pictures
            // so that it can know that the document is now dirty.
			mf.movePicturesUp();
		} else if (command.equals("Down")) {
            // We must call the MainFrame to move pictures
            // so that it can know that the document is now dirty.
			mf.movePicturesDown();
		} else if (command.equals("Left")) {
			for (int i = 0; i < pictures.length; i++) {
				((Picture) pictures[i]).rotateLeft();
			}
			setPictures(pictures);
			mf.repaint();
			mf.previewFrame.repaint();
		} else if (command.equals("Right")) {
			for (int i = 0; i < pictures.length; i++) {
				((Picture) pictures[i]).rotateRight();
			}
			setPictures(pictures);
			mf.repaint();
			mf.previewFrame.repaint();
		} else if (command.equals("Flip")) {
			for (int i = 0; i < pictures.length; i++) {
				((Picture) pictures[i]).flip();
			}
			setPictures(pictures);
			mf.repaint();
			mf.previewFrame.repaint();
		}
	}

	/**
	 * Caption JTextArea events.
	 */
	public void insertUpdate(DocumentEvent e) {
		textUpdate(e);
	}

	/**
	 * Caption JTextArea events.
	 */
	public void removeUpdate(DocumentEvent e) {
		textUpdate(e);
	}

	/**
	 * Caption JTextArea events.
	 */
	public void changedUpdate(DocumentEvent e) {
		textUpdate(e);
	}

	public void textUpdate(DocumentEvent e) {
		if (pictures != null && pictures.length == 1) {
			Picture p = ((Picture) pictures[0]);

			if (e.getDocument() == jCaption.getDocument()) {
				p.setCaption(jCaption.getText());
			}

			Iterator it = extraTextAreas.keySet().iterator();
			while (it.hasNext()) {
				String name = (String) it.next();
				JTextArea field = (JTextArea) extraTextAreas.get(name);

				if (e.getDocument() == field.getDocument()) {
					String text = field.getText();
					if (text.length() == 0) {
						p.removeExtraField(name);
					} else {
						p.setExtraField(name, text);
					}

					break;
				}
			}
		}
	}


	/**
	 * Sets the mainFrame attribute of the PictureInspector object
	 *
	 * @param mf The new mainFrame value
	 */
	public void setMainFrame(MainFrame mf) {
		this.mf = mf;
		replaceIcon(jIcon, ImageUtils.defaultThumbnail);
	}

	/**
	 * Sets the picture attribute of the PictureInspector object
	 *
	 * @param pictures The new picture value
	 */
	public void setPictures(Object[] pictures) {
		//Log.log(Log.TRACE, MODULE, "setPictures " + pictures);
		//Log.logStack(Log.TRACE, MODULE);
		this.pictures = pictures;

		jIcon.setPreferredSize(
				new Dimension(0,
						GalleryRemote._().properties.getThumbnailSize().height
				+ emptyIconHeight
				+ jIcon.getIconTextGap()));

		if (pictures == null || pictures.length == 0) {
			jIcon.setText(GRI18n.getString(MODULE, "noPicSel"));
			replaceIcon(jIcon, ImageUtils.defaultThumbnail);
			jPath.setText("");
			jAlbum.setText("");

			jCaption.setText("");
			jCaption.setEditable(false);
			jCaption.setBackground(UIManager.getColor("TextField.inactiveBackground"));

			jSize.setText("");

			jUpButton.setEnabled(false);
			jDownButton.setEnabled(false);
			jDeleteButton.setEnabled(false);
			jRotateLeftButton.setEnabled(false);
			jRotateRightButton.setEnabled(false);
			jFlipButton.setEnabled(false);

			removeExtraFields();
		} else if (pictures.length == 1) {
			Picture p = (Picture) pictures[0];

			replaceIcon(jIcon, mf.getThumbnail(p));
			if (p.isOnline()) {
				jPath.setText(GRI18n.getString(MODULE, "onServer"));
				jIcon.setText(p.getName());
			} else {
				jIcon.setText(p.getSource().getName());
				jPath.setText(p.getSource().getParent());
			}
			jAlbum.setText(p.getParentAlbum().getTitle());
			if (p.getParentAlbum().getGallery().getComm(mf.jStatusBar).hasCapability(mf.jStatusBar, GalleryCommCapabilities.CAPA_UPLOAD_CAPTION)) {
				jCaption.setText(p.getCaption());
				jCaption.setEditable(true);
				jCaption.setBackground(UIManager.getColor("TextField.background"));
			}
			jSize.setText(NumberFormat.getInstance().format(
					(int) p.getFileSize()) + " bytes");

			jUpButton.setEnabled(isEnabled());
			jDownButton.setEnabled(isEnabled());
			jDeleteButton.setEnabled(isEnabled());
			jRotateLeftButton.setEnabled(isEnabled());
			jRotateRightButton.setEnabled(isEnabled());
			jFlipButton.setEnabled(isEnabled());

			addExtraFields(p);
		} else {
			Picture p = (Picture) pictures[0];

			Object[] params = {new Integer(pictures.length)};
			jIcon.setText(GRI18n.getString(MODULE, "countElemSel", params));
			replaceIcon(jIcon, ImageUtils.defaultThumbnail);
			jPath.setText("");
			jAlbum.setText(p.getParentAlbum().getTitle());
			jCaption.setText("");
			jCaption.setEditable(false);
			jCaption.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			jSize.setText(NumberFormat.getInstance().format(
					Album.getObjectFileSize(pictures)) + " bytes");

			jUpButton.setEnabled(isEnabled());
			jDownButton.setEnabled(isEnabled());
			jDeleteButton.setEnabled(isEnabled());
			jRotateLeftButton.setEnabled(isEnabled());
			jRotateRightButton.setEnabled(isEnabled());
			jFlipButton.setEnabled(isEnabled());

			removeExtraFields();
		}
	}

	void addExtraFields(Picture p) {
		ArrayList newExtraFields = p.getParentAlbum().getExtraFields();

		if (newExtraFields == null) {
			removeExtraFields();
		} else {
			if (!newExtraFields.equals(currentExtraFields)) {
				removeExtraFields();

				int i = 0;
				Iterator it = newExtraFields.iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					//String value = p.getExtraField(name);

					JLabel label = new JLabel(name);
					extraLabels.put(name, label);
					add(label, new GridBagConstraints(0, FIRST_ROW_EXTRA + i, 1, 1, 0.0, 0.0
							, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 2, 0));

					JTextArea field = new JTextArea();
					extraTextAreas.put(name, field);
					field.setFont(UIManager.getFont("Label.font"));
					field.setLineWrap(true);
					field.setWrapStyleWord(true);
					add(field, new GridBagConstraints(1, FIRST_ROW_EXTRA + i, 1, 1, 1.0, 1.0
							, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));
					field.getDocument().addDocumentListener(this);
					setupKeyboardHandling(field);

					i++;
				}
				
				currentExtraFields = newExtraFields;
			}

			Iterator it = newExtraFields.iterator();
			while (it.hasNext()) {
				String name = (String) it.next();
				String value = p.getExtraField(name);
				JTextArea field = (JTextArea) extraTextAreas.get(name);
				if (value == null) {
					field.setText("");
				} else {
					field.setText(value);
				}
			}
		}
	}

	void removeExtraFields() {
		Iterator it = extraLabels.values().iterator();
		while (it.hasNext()) {
			JLabel label = (JLabel) it.next();
			remove(label);
		}

		it = extraTextAreas.values().iterator();
		while (it.hasNext()) {
			JTextArea textArea = (JTextArea) it.next();
			remove(textArea);
		}

		extraLabels.clear();
		extraTextAreas.clear();
		
		currentExtraFields = null;
	}

	public void setEnabled(boolean enabled) {
		//Log.log(Log.TRACE, MODULE, "setEnabled " + enabled);
		jIcon.setEnabled(enabled);
		jUpButton.setEnabled(enabled);
		jDownButton.setEnabled(enabled);
		jDeleteButton.setEnabled(enabled);
		jRotateLeftButton.setEnabled(enabled);
		jRotateRightButton.setEnabled(enabled);
		jFlipButton.setEnabled(enabled);
		jCaption.setEnabled(enabled);

		super.setEnabled(enabled);
	}

	// Focus traversal actions
	public Action nextFocusAction = new AbstractAction("Move Focus Forwards") {
		public void actionPerformed(ActionEvent evt) {
			((Component) evt.getSource()).transferFocus();
		}
	};

	public Action prevFocusAction = new AbstractAction("Move Focus Backwards") {
		public void actionPerformed(ActionEvent evt) {
			((Component) evt.getSource()).transferFocusBackward();
		}
	};

	public Action nextPictureAction = new AbstractAction("Select Next Picture") {
		public void actionPerformed(ActionEvent evt) {
			CoreUtils.selectNextPicture();
		}
	};

	public Action prevPictureAction = new AbstractAction("Select Prev Picture") {
		public void actionPerformed(ActionEvent evt) {
			CoreUtils.selectPrevPicture();
		}
	};

	public void replaceIcon(JLabel label, Image icon) {
		Icon i = label.getIcon();

		if (i == null || !(i instanceof ImageIcon)) {
			i = new ImageIcon();
			label.setIcon(i);
		}

		((ImageIcon) i).setImage(icon);
	}
}

