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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *  Bean inspector for Pictures
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class AlbumInspector extends JPanel
		implements ActionListener {
	public static final String MODULE = "AlbmInspec";
	public static GRI18n grRes = GRI18n.getInstance();

	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JPanel jSpacer = new JPanel();
	JLabel jLabel1 = new JLabel();

	JTextArea jTitle = new JTextArea();
	JTextField jPictures = new JTextField();
	JTextArea jName	= new JTextArea();
	JLabel jLabel2 = new JLabel();
	JTextArea jSummary = new JTextArea();
	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JButton jFetch = new JButton();

	JCheckBox resizeBeforeUpload = new JCheckBox();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	JRadioButton resizeToDefault = new JRadioButton();
	JRadioButton resizeToForce = new JRadioButton();
	JTextField resizeToWidth = new JTextField();
	JLabel jLabel7 = new JLabel();
	JTextField resizeToHeight = new JTextField();
	JPanel jPanel6 = new JPanel();
	JCheckBox jBeginning = new JCheckBox();

	MainFrame mf = null;
	Album album = null;

	/**
	 *  Constructor for the PictureInspector object
	 */
	public AlbumInspector() {
		jbInit();
		jbInitEvents();
	}


	private void jbInit() {
		setLayout( new GridBagLayout() );
		jLabel5.setText(grRes.getString(MODULE, "Name") );
		jLabel6.setText(grRes.getString(MODULE, "Title") );
		jLabel1.setText(grRes.getString(MODULE, "Pictures") );
		jLabel2.setText(grRes.getString(MODULE, "Summary") );

		jName.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jName.setFont(new Font("SansSerif", 0, 11));
		jName.setEditable(false);
		jName.setText("");
		jName.setLineWrap(true);

		jTitle.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jTitle.setFont(new Font("SansSerif", 0, 11));
		jTitle.setEditable(false);
		jTitle.setText("");
		jTitle.setLineWrap(true);

		jSummary.setLineWrap(true);
		jSummary.setEditable(false);
		jSummary.setFont(new java.awt.Font("SansSerif", 0, 11));
		jSummary.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		jPictures.setEditable(false);

		jPanel1.setLayout(new GridBagLayout());
		jPanel2.setLayout(new GridBagLayout());
		jFetch.setText(grRes.getString(MODULE, "Fetch"));

		resizeToWidth.setMinimumSize(new Dimension(25, 21));
		resizeToWidth.setPreferredSize(new Dimension(25, 21));
		resizeToWidth.setToolTipText(grRes.getString(MODULE, "res2W"));
		jLabel7.setText("x");
		resizeToHeight.setMinimumSize(new Dimension(25, 21));
		resizeToHeight.setPreferredSize(new Dimension(25, 21));
		resizeToHeight.setToolTipText(grRes.getString(MODULE, "res2H"));
		resizeBeforeUpload.setToolTipText(grRes.getString(MODULE, "resBfrUpldTip"));
		resizeBeforeUpload.setText(grRes.getString(MODULE, "resBfrUpld"));
		resizeToDefault.setToolTipText(grRes.getString(MODULE, "res2Def"));
		resizeToDefault.setText(grRes.getString(MODULE, "res2Def"));
		resizeToForce.setToolTipText(grRes.getString(MODULE, "res2FrcTip"));
		resizeToForce.setText(grRes.getString(MODULE, "res2Frc"));

		jBeginning.setText(grRes.getString(MODULE, "Beginning"));
		jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),grRes.getString(MODULE, "Override")));
		jPanel2.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),grRes.getString(MODULE, "Props")));

		jPanel2.add( jLabel5,              new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 2, 0) );
		jPanel2.add( jLabel6,             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 2, 0) );
		jPanel2.add( jLabel1,             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 2, 0) );
		jPanel2.add(new JScrollPane(jName),                new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 0), 0, 0));
		jPanel2.add(new JScrollPane(jTitle),           new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 0), 0, 0));
		jPanel2.add(jPictures,           new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 0), 0, 0));
		jPanel2.add(jLabel2,       new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		jPanel2.add(new JScrollPane(jSummary),       new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		jPanel1.add(resizeToWidth,             new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jLabel7,              new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		jPanel1.add(resizeToHeight,             new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jPanel6,          new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(resizeBeforeUpload,         new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(resizeToDefault,       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(resizeToForce,      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(jBeginning,      new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		this.add(jPanel2,      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jPanel1,      new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jFetch,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		add( jSpacer,              new GridBagConstraints(0, 3, 1, 1, 1.0, 0.1
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0) );

		buttonGroup1.add(resizeToDefault);
		buttonGroup1.add(resizeToForce);

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
	}

	// Event handling
	/**
	 *  Menu and button handling
	 *
	 *@param  e  Action event
	 */
	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + command );
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
		this.album = album;

		if ( album == null ) {
			setActive(jName, false);
			setActive(jTitle, false);
			setActive(jSummary, false);

			jPictures.setText("");
		} else {
			setActive(jName, true);
			jName.setText(album.getName());

			setActive(jTitle, true);
			jTitle.setText(album.getTitle());

			setActive(jSummary, true);
			jSummary.setText(album.getSummary());

			jPictures.setText("" + album.getSize());
		}
	}

	public void setActive(JTextArea t, boolean active) {
		if (active) {
			t.setEditable(true);
			t.setBackground(UIManager.getColor("TextField.background"));
		} else {
			t.setText( "" );
			t.setEditable(false);
			t.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
	}

	public void setEnabled(boolean enabled) {
		//Log.log(Log.TRACE, MODULE, "setEnabled " + enabled);
		jName.setEnabled(enabled);
		jTitle.setEnabled(enabled);
		jSummary.setEnabled(enabled);

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
}

