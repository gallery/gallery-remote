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

import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.GRI18n;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *  Bean inspector for Pictures
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class AlbumInspector extends JPanel
		implements ActionListener, ItemListener, DocumentListener {
	public static final String MODULE = "AlbmInspec";

	JLabel jLabelName = new JLabel();
	JLabel jLabelTitle = new JLabel();
	JPanel jSpacer = new JPanel();
	JLabel jLabelPictures = new JLabel();
	JLabel jLabelSummary = new JLabel();
	JPanel jPanel1 = new JPanel();
	JPanel jPanelProps = new JPanel();
	JPanel jPanel6 = new JPanel();
	JLabel jLabel7 = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();

	JTextArea jTitle = new JTextArea();
	JTextArea jPictures = new JTextArea();
	JTextArea jName	= new JTextArea();
	JTextArea jSummary = new JTextArea();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JButton jFetch = new JButton();
	JButton jSlideshow = new JButton();
	JButton jNew = new JButton();
	JButton jApply = new JButton();
	JCheckBox jResizeBeforeUpload = new JCheckBox();
	JRadioButton jResizeToDefault = new JRadioButton();
	JRadioButton jResizeToForce = new JRadioButton();
	JTextField jResizeToWidth = new JTextField();
	JTextField jResizeToHeight = new JTextField();
	JCheckBox jBeginning = new JCheckBox();

	MainFrame mf = null;
	Album album = null;

	boolean ignoreItemChanges = false;

	/**
	 *  Constructor for the PictureInspector object
	 */
	public AlbumInspector() {
		jbInit();
		jbInitEvents();
	}


	private void jbInit() {
		setLayout( new GridBagLayout() );
		jLabelName.setText(GRI18n.getString(MODULE, "Name") );
		jLabelTitle.setText(GRI18n.getString(MODULE, "Title") );
		jLabelPictures.setText(GRI18n.getString(MODULE, "Pictures") );
		jLabelSummary.setText(GRI18n.getString(MODULE, "Summary") );

		jName.setFont(new Font("SansSerif", 0, 11));
		jName.setLineWrap(true);

		jTitle.setFont(new Font("SansSerif", 0, 11));
		jTitle.setLineWrap(true);

		jSummary.setLineWrap(true);
		jSummary.setFont(new java.awt.Font("SansSerif", 0, 11));

		jPictures.setEditable(false);
		jPictures.setFont(new java.awt.Font("SansSerif", 0, 11));
		jPictures.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		jPanel1.setLayout(new GridBagLayout());
		jPanelProps.setLayout(new GridBagLayout());

		jSlideshow.setText(GRI18n.getString(MODULE, "Slideshow"));
		jFetch.setText(GRI18n.getString(MODULE, "Fetch"));
		jNew.setText(GRI18n.getString(MODULE, "New"));
		jApply.setText(GRI18n.getString(MODULE, "Apply"));

		jResizeToWidth.setMinimumSize(new Dimension(25, 21));
		jResizeToWidth.setPreferredSize(new Dimension(25, 21));
		jResizeToWidth.setToolTipText(GRI18n.getString(MODULE, "res2W"));
		jLabel7.setText("x");
		jResizeToHeight.setMinimumSize(new Dimension(25, 21));
		jResizeToHeight.setPreferredSize(new Dimension(25, 21));
		jResizeToHeight.setToolTipText(GRI18n.getString(MODULE, "res2H"));
		jResizeBeforeUpload.setToolTipText(GRI18n.getString(MODULE, "resBfrUpldTip"));
		jResizeBeforeUpload.setText(GRI18n.getString(MODULE, "resBfrUpld"));
		jResizeToDefault.setToolTipText(GRI18n.getString(MODULE, "res2Def"));
		jResizeToDefault.setText(GRI18n.getString(MODULE, "res2Def"));
		jResizeToForce.setToolTipText(GRI18n.getString(MODULE, "res2FrcTip"));
		jResizeToForce.setText(GRI18n.getString(MODULE, "res2Frc"));

		jBeginning.setText(GRI18n.getString(MODULE, "Beginning"));
		jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),GRI18n.getString(MODULE, "Override")));
		jPanelProps.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),GRI18n.getString(MODULE, "Props")));

    	jPanelProps.add( jLabelName,               new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 5), 2, 0) );
		jPanelProps.add(new JScrollPane(jName),                new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 0), 0, 0));
		jPanelProps.add( jLabelTitle,              new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 5), 2, 0) );
		jPanelProps.add(new JScrollPane(jTitle),           new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 0), 0, 0));
		jPanelProps.add(jLabelSummary,        new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 5), 0, 0));
		jPanelProps.add(new JScrollPane(jSummary),       new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanelProps.add( jLabelPictures,              new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 2, 0) );
		jPanelProps.add(jPictures,            new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 0), 0, 0));
		jPanelProps.add(jApply,  new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));

		jPanel1.add(jResizeToWidth,             new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jLabel7,              new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		jPanel1.add(jResizeToHeight,             new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jPanel6,          new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jResizeBeforeUpload,         new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jResizeToDefault,       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(jResizeToForce,      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(jBeginning,      new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		this.add(jPanelProps,      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jPanel1,      new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jFetch,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		this.add(jSlideshow,    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		//this.add(jNew,    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
		//		,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		add( jSpacer,              new GridBagConstraints(0, 5, 1, 1, 1.0, 0.1
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0) );

		buttonGroup1.add(jResizeToDefault);
		buttonGroup1.add(jResizeToForce);

		this.setMinimumSize( new Dimension( 150, 0 ) );

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
		jApply.addActionListener(this);

		jBeginning.addItemListener(this);
		jResizeBeforeUpload.addItemListener(this);
		jResizeToDefault.addItemListener(this);
		jResizeToForce.addItemListener(this);

		jResizeToWidth.getDocument().addDocumentListener(this);
		jResizeToHeight.getDocument().addDocumentListener(this);
	}

	// Event handling
	/**
	 *  Menu and button handling
	 *
	 *@param  e  Action event
	 */
	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();
		JComponent source = (JComponent) e.getSource();
		Log.log(Log.LEVEL_TRACE, MODULE, "Action selected " + command );

		if (source == jFetch) {
			mf.fetchAlbumImages();
		} else if (source == jNew) {
			mf.newAlbum();
		} else if (source == jApply) {
			// todo
		} else if (source == jSlideshow) {
			mf.slideshow();
		} else {
			Log.log(Log.LEVEL_TRACE, MODULE, "Unknown source " + source);
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
		Log.log(Log.LEVEL_TRACE, MODULE, "Item state changed " + source );

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
	}

	public void resetUIState() {
		boolean oldIgnoreItemChanges = ignoreItemChanges;
		ignoreItemChanges = true;

		if (album!= null && jResizeBeforeUpload.isSelected()) {
			jResizeToDefault.setEnabled(true);
			jResizeToForce.setEnabled(true);

			if (jResizeToForce.isSelected()) {
				jResizeToHeight.setEnabled(true);
				jResizeToWidth.setEnabled(true);
				jResizeToHeight.setBackground(UIManager.getColor("TextField.background"));
				jResizeToWidth.setBackground(UIManager.getColor("TextField.background"));
			} else {
				jResizeToHeight.setEnabled(false);
				jResizeToWidth.setEnabled(false);
				jResizeToHeight.setBackground(UIManager.getColor("TextField.inactiveBackground"));
				jResizeToWidth.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			}
		} else {
			jResizeToDefault.setEnabled(false);
			jResizeToForce.setEnabled(false);
			jResizeToHeight.setEnabled(false);
			jResizeToWidth.setEnabled(false);
			jResizeToHeight.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			jResizeToWidth.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}

		ignoreItemChanges = oldIgnoreItemChanges;
	}

	/**
	 *  Sets the mainFrame attribute of the PictureInspector object
	 *
	 *@param  mf  The new mainFrame value
	 */
	public void setMainFrame( MainFrame mf ) {
		this.mf = mf;
	}

	public void setAlbum(Album album) {
		boolean oldIgnoreItemChanges = ignoreItemChanges;
		ignoreItemChanges = true;

		this.album = album;

		if ( album == null ) {
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
			jResizeToForce.setSelected(! album.getResizeDefault());
			jResizeToWidth.setText("" + album.getResizeDimension().width);
			jResizeToHeight.setText("" + album.getResizeDimension().height);
			jBeginning.setSelected(album.getAddToBeginning());

			jFetch.setEnabled(album.getGallery().getComm(mf.jStatusBar).hasCapability(GalleryCommCapabilities.CAPA_FETCH_ALBUM_IMAGES));

			// todo
			jApply.setEnabled(false);
		}

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
		jResizeBeforeUpload.setEnabled(enabled);
		jResizeToDefault.setEnabled(enabled);
		jResizeToForce.setEnabled(enabled);
		jResizeToWidth.setEnabled(enabled);
		jResizeToHeight.setEnabled(enabled);
		jBeginning.setEnabled(enabled);

		if (enabled) {
			jResizeToHeight.setBackground(UIManager.getColor("TextField.background"));
			jResizeToWidth.setBackground(UIManager.getColor("TextField.background"));
		} else {
			jResizeToHeight.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			jResizeToWidth.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
	}

	public void setEnabled(boolean enabled) {
		setEnabledInternal(enabled);
		super.setEnabled(enabled);
	}

	// Focus traversal actions
	public Action nextFocusAction = new AbstractAction("Move Focus Forwards") {
		public void actionPerformed(ActionEvent evt) {
			((Component)evt.getSource()).transferFocus();
		}
	};

	public Action prevFocusAction = new AbstractAction("Move Focus Backwards") {
		public void actionPerformed(ActionEvent evt) {
			try {
				((Component)evt.getSource()).transferFocusBackward();
			} catch (NoSuchMethodError e) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Can't transfer focus backwards on 1.3");
			}
		}
	};

	public Action nextPictureAction = new AbstractAction("Select Next Picture") {
		public void actionPerformed(ActionEvent evt) {
			mf.selectNextPicture();
		}
	};

	public Action prevPictureAction = new AbstractAction("Select Prev Picture") {
		public void actionPerformed(ActionEvent evt) {
			mf.selectPrevPicture();
		}
	};

	/**
	 *	Caption JTextArea events.
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
		Document doc = e.getDocument();

		if (doc == jResizeToWidth.getDocument()) {
			try {
				Dimension d = album.getOverrideResizeDimension();

				if (d == null) {
					d = new Dimension();
				}

				String text = jResizeToWidth.getText();

				if (text.length() > 0) {
					d.width = Integer.parseInt(text);

					album.setOverrideResizeDimension(d);
				}
			} catch (NumberFormatException ee) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ee);
			}
		} else if (doc == jResizeToHeight.getDocument()) {
			try {
				Dimension d = album.getOverrideResizeDimension();

				if (d == null) {
					d = new Dimension();
				}

				String text = jResizeToHeight.getText();

				if (text.length() > 0) {
					d.height = Integer.parseInt(jResizeToHeight.getText());

					album.setOverrideResizeDimension(d);
				}
			} catch (NumberFormatException ee) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ee);
			}
		}
	}
}

