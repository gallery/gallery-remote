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
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.prefs.UploadPanel;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;

/**
 * Bean inspector for Pictures
 * 
 * @author paour
 * @created August 16, 2002
 */
public class AlbumInspector extends JPanel
		implements ActionListener, ItemListener, KeyListener, PreferenceNames {
	public static final String MODULE = "AlbmInspec";

	JLabel jLabelName = new JLabel();
	JLabel jLabelTitle = new JLabel();
	JPanel jSpacer = new JPanel();
	JLabel jLabelPictures = new JLabel();
	JLabel jLabelSummary = new JLabel();
	JPanel jOverridePanel = new JPanel();
	JPanel jPanelProps = new JPanel();
	//JPanel jPanel6 = new JPanel();
	//JLabel jLabel7 = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();

	JTextArea jTitle = new JTextArea();
	JTextArea jPictures = new JTextArea();
	JTextArea jName = new JTextArea();
	JTextArea jSummary = new JTextArea();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JButton jFetch = new JButton();
	JButton jSlideshow = new JButton();
	JButton jNew = new JButton();
	// todo: change back when protocol support in implemented
	//JButton jApply = new JButton();
	JLabel jApply = new JLabel();
	JButton jMove = new JButton();
	JCheckBox jResizeBeforeUpload = new JCheckBox();
	JRadioButton jResizeToDefault = new JRadioButton();
	JRadioButton jResizeToForce = new JRadioButton();
	JComboBox jResizeTo = new JComboBox(UploadPanel.defaultSizes);
	//JTextField jResizeToHeight = new JTextField();
	JCheckBox jBeginning = new JCheckBox();

	MainFrame mf = null;
	Album album = null;

	boolean ignoreItemChanges = false;
	boolean ignoreNextComboBoxChanged = false;

	/**
	 * Constructor for the PictureInspector object
	 */
	public AlbumInspector() {
		jbInit();
		jbInitEvents();
	}


	private void jbInit() {
		setLayout(new GridBagLayout());
		jLabelName.setText(GRI18n.getString(MODULE, "Name"));
		jLabelTitle.setText(GRI18n.getString(MODULE, "Title"));
		jLabelPictures.setText(GRI18n.getString(MODULE, "Pictures"));
		jLabelSummary.setText(GRI18n.getString(MODULE, "Summary"));

		jName.setFont(UIManager.getFont("Label.font"));
		jName.setLineWrap(true);

		jTitle.setFont(UIManager.getFont("Label.font"));
		jTitle.setLineWrap(true);

		jSummary.setFont(UIManager.getFont("Label.font"));
		jSummary.setLineWrap(true);

		jPictures.setFont(UIManager.getFont("Label.font"));
		jPictures.setEditable(false);
		jPictures.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		jOverridePanel.setLayout(new GridBagLayout());
		jPanelProps.setLayout(new GridBagLayout());

		jSlideshow.setText(GRI18n.getString(MODULE, "Slideshow"));
		jFetch.setText(GRI18n.getString(MODULE, "Fetch"));
		jNew.setText(GRI18n.getString(MODULE, "New"));
		jApply.setText(GRI18n.getString(MODULE, "Apply"));
		jMove.setText(GRI18n.getString(MODULE, "Move"));

		jResizeTo.setToolTipText(GRI18n.getString(MODULE, "res2W"));
		jResizeTo.setEditable(true);
		jResizeTo.setRenderer(new UploadPanel.SizeListRenderer());

		jResizeBeforeUpload.setToolTipText(GRI18n.getString(MODULE, "resBfrUpldTip"));
		jResizeBeforeUpload.setText(GRI18n.getString(MODULE, "resBfrUpld"));
		jResizeToDefault.setToolTipText(GRI18n.getString(MODULE, "res2Def"));
		jResizeToDefault.setText(GRI18n.getString(MODULE, "res2Def"));
		jResizeToForce.setToolTipText(GRI18n.getString(MODULE, "res2FrcTip"));
		jResizeToForce.setText(GRI18n.getString(MODULE, "res2Frc"));

		jBeginning.setText(GRI18n.getString(MODULE, "Beginning"));
		jOverridePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "Override")));
		jPanelProps.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "Props")));

		jPanelProps.add(jLabelName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 5), 2, 0));
		jPanelProps.add(new JScrollPane(jName), new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 0), 0, 0));
		jPanelProps.add(jLabelTitle, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 5), 2, 0));
		jPanelProps.add(new JScrollPane(jTitle), new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 0), 0, 0));
		jPanelProps.add(jLabelSummary, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 5), 0, 0));
		jPanelProps.add(new JScrollPane(jSummary), new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanelProps.add(jLabelPictures, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 2, 0));
		jPanelProps.add(jPictures, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 0), 0, 0));
		jPanelProps.add(jApply, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		jPanelProps.add(jMove, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));

		jOverridePanel.add(jResizeBeforeUpload, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jOverridePanel.add(jResizeToDefault, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		//jOverridePanel.add(jPanel6, new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0
		//		, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jOverridePanel.add(jResizeToForce, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jOverridePanel.add(jResizeTo, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jOverridePanel.add(jBeginning, new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		add(jPanelProps, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(jOverridePanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(jFetch, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		add(jSlideshow, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		add(jSpacer, new GridBagConstraints(0, 5, 1, 1, 1.0, 0.1
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		buttonGroup1.add(jResizeToDefault);
		buttonGroup1.add(jResizeToForce);

		this.setMinimumSize(new Dimension(150, 0));

		setupKeyboardHandling(jName);
		setupKeyboardHandling(jTitle);
		setupKeyboardHandling(jSummary);
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
		jFetch.addActionListener(this);
		jSlideshow.addActionListener(this);
		jNew.addActionListener(this);
		// todo
		// jApply.addActionListener(this);
		jMove.addActionListener(this);

		jBeginning.addItemListener(this);
		jResizeBeforeUpload.addItemListener(this);
		jResizeToDefault.addItemListener(this);
		jResizeToForce.addItemListener(this);

		jResizeTo.addActionListener(this);
		jResizeTo.getEditor().getEditorComponent().addKeyListener(this);
	}

	// Event handling
	/**
	 * Menu and button handling
	 * 
	 * @param e Action event
	 */
	public void actionPerformed(ActionEvent e) {
		if (ignoreItemChanges) {
			return;
		}

		String command = e.getActionCommand();
		JComponent source = (JComponent) e.getSource();
		Log.log(Log.LEVEL_TRACE, MODULE, "Action selected " + command);

		if (source == jFetch) {
			mf.fetchAlbumImages();
		} else if (source == jNew) {
			mf.newAlbum();
		} else if (source == jApply) {
			// todo
		} else if (source == jMove) {
			MoveAlbumDialog mad = new MoveAlbumDialog(mf, album.getGallery(), album);
			Album newParent = mad.getNewParent();

			if (newParent != null) {
				album.moveAlbumTo(GalleryRemote._().getCore().getMainStatusUpdate(), newParent);

				// todo: this is too drastic...
				album.getGallery().reload();

				//album.moveAlbumTo(null, null);
			}
		} else if (source == jSlideshow) {
			mf.slideshow();
		} else if (source == jResizeTo) {
			if ("comboBoxChanged".equals(command)) {
				if (ignoreNextComboBoxChanged) {
					ignoreNextComboBoxChanged = false;
				} else {
					readResizeTo(jResizeTo.getSelectedItem().toString());
				}
			}
		} else {
			Log.log(Log.LEVEL_TRACE, MODULE, "Unknown source " + source);
		}
	}

	private void readResizeTo(String text) {
		if (ignoreItemChanges) {
			return;
		}

		try {
			int overrideDimension = album.getOverrideResizeDimension();

			//if (text.length() > 0) {
			int newOverrideDimension = Integer.parseInt(text);

			if (overrideDimension != -1
					|| (newOverrideDimension != GalleryRemote._().properties.getIntDimensionProperty(RESIZE_TO))) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Overriding dimension to " + newOverrideDimension);
				album.setOverrideResizeDimension(newOverrideDimension);
			}
			//}
		} catch (NumberFormatException ee) {
			Log.logException(Log.LEVEL_ERROR, MODULE, ee);
		}
	}

	/**
	 * Invoked when an item has been selected or deselected by the user.
	 * The code written for this method performs the operations
	 * that need to occur when an item is selected (or deselected).
	 */
	public void itemStateChanged(ItemEvent e) {
		if (ignoreItemChanges) {
			return;
		}

		JComponent source = (JComponent) e.getSource();
		Log.log(Log.LEVEL_TRACE, MODULE, "Item state changed " + source);

		if (source == jBeginning) {
			album.setOverrideAddToBeginning(new Boolean(jBeginning.isSelected()));
		} else if (source == jResizeBeforeUpload) {
			album.setOverrideResize(new Boolean(jResizeBeforeUpload.isSelected()));

			resetUIState();
		} else if (source == jResizeToDefault || source == jResizeToForce) {
			album.setOverrideResizeDefault(new Boolean(jResizeToDefault.isSelected()));

			resetUIState();
		} else {
			Log.log(Log.LEVEL_TRACE, MODULE, "Unknown source " + source);
		}

		ignoreNextComboBoxChanged = false;
	}

	public void resetUIState() {
		boolean oldIgnoreItemChanges = ignoreItemChanges;
		ignoreItemChanges = true;

		if (album != null && jResizeBeforeUpload.isSelected()) {
			jResizeToDefault.setEnabled(true);
			jResizeToForce.setEnabled(true);

			if (jResizeToForce.isSelected()) {
				jResizeTo.setEnabled(true);
				jResizeTo.setBackground(UIManager.getColor("TextField.background"));
			} else {
				jResizeTo.setEnabled(false);
				jResizeTo.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			}
		} else {
			jResizeToDefault.setEnabled(false);
			jResizeToForce.setEnabled(false);
			jResizeTo.setEnabled(false);
			jResizeTo.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}

		ignoreItemChanges = oldIgnoreItemChanges;
	}

	/**
	 * Sets the mainFrame attribute of the PictureInspector object
	 * 
	 * @param mf The new mainFrame value
	 */
	public void setMainFrame(MainFrame mf) {
		this.mf = mf;
	}

	public void setAlbum(Album album) {
		boolean oldIgnoreItemChanges = ignoreItemChanges;
		ignoreItemChanges = true;

		this.album = album;

		if (album == null) {
			/*setActive(jName, false);
			setActive(jTitle, false);
			setActive(jSummary, false);*/

			jName.setText("");
			jTitle.setText("");
			jSummary.setText("");
			jPictures.setText("");

			setEnabledInternal(false);
		} else {
			setEnabledInternal(true);

			//setActive(jName, true);
			jName.setText(album.getName());

			//setActive(jTitle, true);
			jTitle.setText(album.getTitle());

			//setActive(jSummary, true);
			jSummary.setText(album.getSummary());

			jPictures.setText("" + album.getSize());

			jResizeBeforeUpload.setSelected(album.getResize());
			jResizeToDefault.setSelected(album.getResizeDefault());
			jResizeToForce.setSelected(!album.getResizeDefault());
			UploadPanel.setupComboValue(album.getResizeDimension(), jResizeTo);
			// hack: the JComboBox will fire an action when the value is changed
			ignoreNextComboBoxChanged = true;
			jBeginning.setSelected(album.getAddToBeginning());

			jFetch.setEnabled(album.getGallery().getComm(mf.jStatusBar).hasCapability(mf.jStatusBar, GalleryCommCapabilities.CAPA_FETCH_ALBUM_IMAGES));
			jMove.setEnabled(album.getGallery().getComm(mf.jStatusBar).hasCapability(mf.jStatusBar, GalleryCommCapabilities.CAPA_MOVE_ALBUM));

			jSlideshow.setEnabled(album.getSize() > 0);
		}

		// todo: protocol support
		jApply.setEnabled(false);
		jName.setEditable(false);
		jName.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jTitle.setEditable(false);
		jTitle.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jSummary.setEditable(false);
		jSummary.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		jPictures.setEditable(false);
		jPictures.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		resetUIState();

		ignoreItemChanges = oldIgnoreItemChanges;
	}

	/*public void setActive(JTextArea t, boolean active) {
		if (active) {
			t.setEditable(true);
			t.setBackground(UIManager.getColor("TextField.background"));
		} else {
			t.setText( "" );
			t.setEditable(false);
			t.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
	}*/

	public void setEnabledInternal(boolean enabled) {
		//Log.log(Log.TRACE, MODULE, "setEnabled " + enabled);
		jName.setEnabled(enabled);
		jTitle.setEnabled(enabled);
		jSummary.setEnabled(enabled);
		jPictures.setEnabled(enabled);
		jFetch.setEnabled(enabled);
		jSlideshow.setEnabled(enabled);
		jNew.setEnabled(enabled);
		jApply.setEnabled(enabled);
		jMove.setEnabled(enabled);
		jResizeBeforeUpload.setEnabled(enabled);
		jResizeToDefault.setEnabled(enabled);
		jResizeToForce.setEnabled(enabled);
		jResizeTo.setEnabled(enabled);
		jBeginning.setEnabled(enabled);

		if (enabled) {
			jResizeTo.setBackground(UIManager.getColor("TextField.background"));
		} else {
			jResizeTo.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
	}

	public void setEnabled(boolean enabled) {
		setEnabledInternal(enabled);
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

	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {
		readResizeTo(jResizeTo.getEditor().getItem().toString());
	}

	public void keyTyped(KeyEvent e) {	}
}

