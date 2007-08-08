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
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.util.Vector;

/**
 * Description of the Class
 * 
 * @author paour
 * @created October 18, 2002
 */
public class NewAlbumDialog extends JDialog
		implements ActionListener, ItemListener, FocusListener {
	public final static String MODULE = "NewAlbum";

	Gallery gallery = null;
	Album defaultAlbum = null;
	Album newAlbum = null;
	Album parentAlbum = null;

	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JTextField jTitle = new JTextField();
	JTextField jName = new JTextField();
	JTextArea jDescription = new JTextArea();
	JLabel jLabel1 = new JLabel();
	JLabel jGalleryName = new JLabel();
	JComboBox jAlbum = null;
	JPanel jPanel2 = new JPanel();
	JButton jOk = new JButton();
	JButton jCancel = new JButton();
	GridLayout gridLayout1 = new GridLayout();
	private static final String ILLEGAL_CHARACTERS = "[ \\\\/*?\"'&|.+#]";


	/**
	 * Constructor for the NewAlbumDialog object
	 * 
	 * @param owner        Description of Parameter
	 * @param gallery      Description of Parameter
	 * @param defaultAlbum Description of Parameter
	 */
	public NewAlbumDialog(Frame owner, Gallery gallery, Album defaultAlbum) {
		super(owner, true);

		this.gallery = gallery;
		this.defaultAlbum = defaultAlbum;

		jbInit();

		pack();
		DialogUtil.center(this, owner);

		setVisible(true);
	}


	private void jbInit() {
		this.getContentPane().setLayout(new GridBagLayout());
		this.setModal(true);
		this.setTitle(GRI18n.getString(MODULE, "title"));

		Vector albums = new Vector(gallery.getFlatAlbumList());
		//rootAlbum = new Album(gallery);
		//rootAlbum.setSuppressEvents(true);
		//rootAlbum.setTitle(GRI18n.getString(MODULE, "rootAlbmTitle"));
		//rootAlbum.setName("root.root");
		//albums.add(0, rootAlbum);

		jAlbum = new JComboBox(albums);
		jAlbum.setRenderer(new AlbumListRenderer());

		if (defaultAlbum == null) {
			jAlbum.setSelectedItem(gallery.getRoot());
		} else {
			jAlbum.setSelectedItem(defaultAlbum);
		}

		jCancel.setText(GRI18n.getString("Common", "Cancel"));
		jCancel.setActionCommand("Cancel");
		jDescription.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.darkGray, Color.gray));
		jDescription.setLineWrap(true);
		jDescription.setWrapStyleWord(true);
		JScrollPane descriptionArea = new JScrollPane(
				jDescription, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		descriptionArea.setPreferredSize(new Dimension(250, 100));
		jAlbum.setFont(UIManager.getFont("Label.font"));
		jGalleryName.setText(GRI18n.getString(MODULE, "createAlbm", new String[] { gallery.toString() }));
		jName.setFont(UIManager.getFont("Label.font"));
		jName.setToolTipText(GRI18n.getString(MODULE, "albmNameTip"));
		jOk.setText(GRI18n.getString("Common", "OK"));
		jOk.setActionCommand("OK");
		jTitle.setFont(UIManager.getFont("Label.font"));

		gridLayout1.setColumns(2);
		gridLayout1.setHgap(5);
		jLabel2.setText(GRI18n.getString(MODULE, "parentAlbm"));
		jLabel3.setText(GRI18n.getString(MODULE, "albmTitle"));
		jLabel4.setText(GRI18n.getString(MODULE, "albmName"));
		jLabel5.setText(GRI18n.getString(MODULE, "albmDesc"));
		jPanel2.setLayout(gridLayout1);

		this.getContentPane().add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 4));
		this.getContentPane().add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 0, 5), 0, 4));
		this.getContentPane().add(jLabel4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 0, 5), 0, 4));
		this.getContentPane().add(jLabel5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 3));
		this.getContentPane().add(jTitle, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		this.getContentPane().add(jName, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		this.getContentPane().add(descriptionArea, new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
		this.getContentPane().add(jGalleryName, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.getContentPane().add(jAlbum, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		this.getContentPane().add(jPanel2, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jPanel2.add(jCancel, null);
		jPanel2.add(jOk, null);

		jOk.addActionListener(this);
		jCancel.addActionListener(this);
		jAlbum.addItemListener(this);

		jName.addFocusListener(this);
		jTitle.addFocusListener(this);

		getRootPane().setDefaultButton(jOk);

		resetUIState();
	}


	/**
	 * Description of the Method
	 * 
	 * @param e Description of Parameter
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + command);

		if (command.equals("Cancel")) {
			setVisible(false);
		} else if (command.equals("OK")) {
			newAlbum = new Album(gallery);
			//newAlbum.setSuppressEvents(true);
			newAlbum.setName(jName.getText());
			newAlbum.setTitle(jTitle.getText());
			newAlbum.setCaption(jDescription.getText());

			//newAlbum.setSuppressEvents(false);

			parentAlbum = (Album) jAlbum.getSelectedItem();
			parentAlbum.getGallery().insertNodeInto(newAlbum, parentAlbum, parentAlbum.getChildCount());

			setVisible(false);
		}
	}

	public Album getNewAlbum() {
		return newAlbum;
	}

	public Album getParentAlbum() {
		return parentAlbum;
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			resetUIState();
		}
	}

	void resetUIState() {
		Album a = (Album) jAlbum.getSelectedItem();

		boolean canCreateSubAlbum = a.getCanCreateSubAlbum();

		jOk.setEnabled(canCreateSubAlbum);
		jName.setEnabled(canCreateSubAlbum);
		jTitle.setEnabled(canCreateSubAlbum);
		jDescription.setEnabled(canCreateSubAlbum);
	}

	public void focusGained(FocusEvent e) {
		((JTextField) e.getSource()).selectAll();
	}

	public void focusLost(FocusEvent e) {
		if (e.getSource() == jTitle && jName.getText().length() == 0) {
			jName.setText(getDefaultName(jTitle.getText()));
		}
	}

	/**
	 *
	 * @param text album title
	 * @return a reasonable default, safe album name
	 */
	static String getDefaultName(final String text) {
		String r = text.toLowerCase();
		r = r.replaceAll("[&]", "and");
		r = r.replaceAll(ILLEGAL_CHARACTERS, "_");
		return r.replaceAll("_{2,}", "_");
	}

	public class AlbumListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(
					list, value, index,
					isSelected, cellHasFocus);

			if (((Album) value).getCanCreateSubAlbum()) {
				if (! isSelected) {
					setForeground(Color.BLACK);
				}
			} else {
				setForeground(Color.GRAY);
			}

			return this;
		}
	}
}