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

import com.gallery.GalleryRemote.model.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 *  Description of the Class
 *
 *@author     paour
 *@created    October 18, 2002
 */
public class NewAlbumDialog extends javax.swing.JDialog
		 implements ActionListener
{
	public final static String MODULE = "NewAlbum";

	Gallery gallery = null;
	Album defaultAlbum = null;

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JTextField title = new JTextField();
	JTextField name = new JTextField();
	JTextArea description = new JTextArea();
	JPanel jPanel1 = new JPanel();
	JLabel jLabel1 = new JLabel();
	JLabel galleryName = new JLabel();
	JComboBox album = null;
	FlowLayout flowLayout1 = new FlowLayout();
	JPanel jPanel2 = new JPanel();
	JButton ok = new JButton();
	JButton cancel = new JButton();
	GridLayout gridLayout1 = new GridLayout();


	/**
	 *  Constructor for the NewAlbumDialog object
	 *
	 *@param  owner         Description of Parameter
	 *@param  gallery       Description of Parameter
	 *@param  defaultAlbum  Description of Parameter
	 */
	public NewAlbumDialog( Frame owner, Gallery gallery, Album defaultAlbum ) {
		super( owner, true );

		this.gallery = gallery;
		this.defaultAlbum = defaultAlbum;

		try {
			jbInit();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		pack();
		Dimension s = owner.getSize();
		setLocation( (int) ( s.getWidth() - getWidth() ) / 2, (int) ( s.getHeight() - getHeight() ) / 2 );

		setVisible( true );
	}


	private void jbInit()
		throws Exception {
		this.getContentPane().setLayout( gridBagLayout1 );
		this.setModal( true );
		this.setTitle( "New Album" );
		
		album = new JComboBox(new Vector(gallery.getAlbumList()));
		album.setFont( new java.awt.Font( "SansSerif", 0, 11 ) );
		album.setSelectedItem(defaultAlbum);
		cancel.setText( "Cancel" );
		description.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED, Color.white, Color.lightGray, Color.darkGray, Color.gray ) );
		description.setLineWrap(true);
		description.setRows(2);
		description.setFont( new java.awt.Font( "SansSerif", 0, 11 ) );
		galleryName.setText( gallery.toString() );
		name.setFont( new java.awt.Font( "SansSerif", 0, 11 ) );
		name.setToolTipText("What do you want to name this album? The name cannot contain any " +
			"of the following characters: \\ / * ? \" \' & \\u  | . + # or spaces. " +
			"Those characters will be ignored in your new album name.");
		ok.setText( "OK" );
		title.setFont( new java.awt.Font( "SansSerif", 0, 11 ) );

		flowLayout1.setAlignment( FlowLayout.LEFT );
		gridLayout1.setColumns( 2 );
		gridLayout1.setHgap( 5 );
		jLabel1.setText( "Creating a new album on Gallery: " );
		jLabel2.setText( "Parent album" );
		jLabel3.setText( "Album title" );
		jLabel4.setText( "Album name" );
		jLabel5.setText( "Album description" );
		jPanel1.setLayout( flowLayout1 );
		jPanel2.setLayout( gridLayout1 );

		this.getContentPane().add( jLabel2, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 5, 0, 5 ), 0, 4 ) );
		this.getContentPane().add( jLabel3, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 2, 5, 0, 5 ), 0, 4 ) );
		this.getContentPane().add( jLabel4, new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 2, 5, 0, 5 ), 0, 4 ) );
		this.getContentPane().add( jLabel5, new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets( 0, 5, 0, 5 ), 0, 3 ) );
		this.getContentPane().add( title, new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		this.getContentPane().add( name, new GridBagConstraints( 1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		this.getContentPane().add( description, new GridBagConstraints( 1, 4, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		this.getContentPane().add( jPanel1, new GridBagConstraints( 0, 0, 2, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel1.add( jLabel1, null );
		jPanel1.add( galleryName, null );
		this.getContentPane().add( album, new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		this.getContentPane().add( jPanel2, new GridBagConstraints( 1, 5, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
		jPanel2.add( cancel, null );
		jPanel2.add( ok, null );
		
		ok.addActionListener(this);
		cancel.addActionListener(this);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();
		Log.log(Log.INFO, MODULE, "Command selected " + command);
		
		if ( command.equals( "Cancel" ) ) {
			setVisible(false);
		} else if ( command.equals( "OK" ) ) {
			Album a = new Album();
			a.setName(name.getText());
			a.setTitle(title.getText());
			a.setCaption(description.getText());
			a.setParentAlbum((Album) album.getSelectedItem());
			gallery.newAlbum(a, (StatusUpdate) this.getOwner());
		}
	}
}

