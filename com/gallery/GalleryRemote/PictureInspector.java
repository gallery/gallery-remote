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

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import com.gallery.GalleryRemote.model.*;

/**
 *  Bean inspector for Pictures
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class PictureInspector extends JPanel
	implements ActionListener
{
	public static final String MODULE = "PictInspec";

	GridBagLayout gridBagLayout4 = new GridBagLayout();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel8 = new JLabel();
	JPanel spacer = new JPanel();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();

	JScrollPane jScrollPane1 = new JScrollPane();
	JScrollPane jScrollPane2 = new JScrollPane();

	JButton delete = new JButton();
	JButton up = new JButton();
	JButton down = new JButton();

	JLabel icon = new JLabel();
	JTextArea album = new JTextArea();
	JTextArea size = new JTextArea();
	JTextArea caption = new JTextArea();
	JTextArea path = new JTextArea();

	MainFrame mf = null;
	Object[] pictures = null;
	int emptyIconHeight = 0;

	/**
	 *  Constructor for the PictureInspector object
	 */
	public PictureInspector() {
		try {
			jbInit();
			jbInitEvents();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		emptyIconHeight = (int) icon.getPreferredSize().getHeight();
		Log.log(Log.TRACE, MODULE, "emptyIconHeight: " + emptyIconHeight);
	}


	private void jbInit()
		throws Exception {
		setLayout( gridBagLayout4 );
		icon.setHorizontalAlignment( SwingConstants.CENTER );
		icon.setHorizontalTextPosition( SwingConstants.CENTER );
		icon.setText( "icon" );
		icon.setVerticalTextPosition( SwingConstants.BOTTOM );
		jLabel5.setText( "Path:" );
		jLabel6.setText( "Album:" );
		jLabel4.setText( "Caption:" );
		jLabel8.setText( "Move:" );
		jLabel1.setText( "Size:" );
		jLabel2.setText("Delete:");

		album.setRows(0);
		album.setText("");
		album.setEditable(false);
		album.setFont(new java.awt.Font("SansSerif", 0, 11));
		album.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		size.setRows(0);
		size.setText("");
		size.setEditable(false);
		size.setFont(new java.awt.Font("SansSerif", 0, 11));
		size.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		caption.setText("");
		caption.setLineWrap(true);
		caption.setEditable(false);
		caption.setFont(new java.awt.Font("SansSerif", 0, 11));
		caption.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		path.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		path.setFont(new java.awt.Font("SansSerif", 0, 11));
		path.setEditable(false);
		path.setText("");
		path.setLineWrap(true);

		up.setMaximumSize(new Dimension( 120, 23 ) );
		up.setMinimumSize( new Dimension( 120, 23 ) );
		up.setPreferredSize( new Dimension( 120, 23 ) );
		up.setText( "Move up" );
		up.setActionCommand( "Up" );
		down.setMaximumSize(new Dimension( 120, 23 ) );
		down.setMinimumSize( new Dimension( 120, 23 ) );
		down.setPreferredSize( new Dimension( 120, 23 ) );
		down.setText( "Move down" );
		down.setActionCommand( "Down" );
		delete.setMaximumSize(new Dimension( 120, 23 ) );
		delete.setMinimumSize(new Dimension( 120, 23 ) );
		delete.setPreferredSize(new Dimension( 120, 23 ) );
		delete.setActionCommand("Delete");
		delete.setText("Delete");

    	jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setBorder(null);
    	jScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane2.setBorder(null);
		add( jLabel5,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0) );
		add( jLabel6,   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0) );
		add( jLabel4,     new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0) );
		add( jLabel8,   new GridBagConstraints(0, 5, 1, 2, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0) );
		add( jLabel1,   new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 2, 0) );
		add(jLabel2,    new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0));
		add( spacer,   new GridBagConstraints(0, 8, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0) );

		add( icon,  new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
		add( album,  new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
		add( size,  new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );

		add( up,    new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0) );
		add( down,   new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );
		add(delete,     new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0));
		this.add(jScrollPane1,   new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jScrollPane2,   new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jScrollPane1.getViewport().add(path, null);
		jScrollPane2.getViewport().add(caption, null);
		
		this.setMinimumSize( new Dimension( 150, 0 ) );
	}

	private void jbInitEvents() {
		delete.addActionListener( this );
		up.addActionListener( this );
		down.addActionListener( this );
	}

	// Event handling
	/**
	 *  Menu and button handling
	 *
	 *@param  e  Action event
	 */
	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();
		Log.log(Log.INFO, MODULE, "Command selected " + command );

		if ( command.equals( "Delete" ) ) {
			mf.deleteSelectedPictures();
		} else if ( command.equals( "Up" ) ) {
			mf.movePictureUp();
		} else if ( command.equals( "Down" ) ) {
			mf.movePictureDown();
		}
	}

	/**
	 *  Sets the mainFrame attribute of the PictureInspector object
	 *
	 *@param  mf  The new mainFrame value
	 */
	public void setMainFrame( MainFrame mf ) {
		this.mf = mf;
		icon.setIcon( mf.defaultThumbnail );
	}


	/**
	 *  Sets the picture attribute of the PictureInspector object
	 *
	 *@param  p  The new picture value
	 */
	public void setPictures( Object[] pictures ) {
		this.pictures = pictures;

		icon.setPreferredSize(
			new Dimension( 0,
				GalleryRemote.getInstance().properties.getThumbnailSize().height
				+ emptyIconHeight
				+ icon.getIconTextGap() ) );

		if ( pictures == null || pictures.length == 0 ) {
			icon.setText("no picture selected");
			icon.setIcon( mf.defaultThumbnail );
			path.setText( "" );
			album.setText( "" );
			caption.setText( "" );
			size.setText( "" );

			up.setEnabled(false);
			down.setEnabled(false);
			delete.setEnabled(false);
		} else if ( pictures.length == 1) {
			Picture p = (Picture) pictures[0];

			icon.setText( p.getSource().getName() );
			icon.setIcon( mf.getThumbnail( p ) );
			path.setText( p.getSource().getParent() );
			album.setText( p.getAlbum().getName() );
			caption.setText( p.getCaption() );
			size.setText( NumberFormat.getInstance().format( 
				(int) p.getFileSize() ) + " bytes" );

			up.setEnabled(true);
			down.setEnabled(true);
			delete.setEnabled(true);
		} else {
			Picture p = (Picture) pictures[0];

			icon.setText( pictures.length + " elements selected" );
			icon.setIcon( mf.defaultThumbnail );
			path.setText( "" );
			album.setText( p.getAlbum().getName() );
			caption.setText( "" );
			size.setText( NumberFormat.getInstance().format(
				Album.getObjectFileSize(pictures) ) + " bytes" );

			up.setEnabled(false);
			down.setEnabled(false);
			delete.setEnabled(true);
		}
	}
}

