/*
 * Gallery Remote - a File Upload Utility for Gallery 
 *
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2001 Bharat Mediratta
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

package com.gallery.GalleryRemote;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AboutBox extends javax.swing.JFrame {

// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
	javax.swing.JLabel jLabelVersion = new javax.swing.JLabel();
	javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
	javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
	javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
// END GENERATED CODE

	public AboutBox() {
	}
	
	
	public void setVersionString(String versionString) {
		jLabelVersion.setText(versionString);
	}

	public void initComponents() throws Exception {
// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
		// the following code sets the frame's initial state

		jLabel1.setSize(new java.awt.Dimension(320, 50));
		jLabel1.setLocation(new java.awt.Point(30, 30));
		jLabel1.setVisible(true);
		jLabel1.setVerticalAlignment(javax.swing.JLabel.TOP);
		jLabel1.setText("Gallery Remote");
		jLabel1.setFont(new java.awt.Font("Dialog", 1, 36));

		jLabel2.setSize(new java.awt.Dimension(104, 20));
		jLabel2.setLocation(new java.awt.Point(10, 80));
		jLabel2.setVisible(true);
		jLabel2.setVerticalAlignment(javax.swing.JLabel.TOP);
		jLabel2.setText("Version:");
		jLabel2.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jLabelVersion.setSize(new java.awt.Dimension(200, 20));
		jLabelVersion.setLocation(new java.awt.Point(120, 80));
		jLabelVersion.setVisible(true);
		jLabelVersion.setVerticalAlignment(javax.swing.JLabel.TOP);
		jLabelVersion.setText("00000");

		jLabel3.setSize(new java.awt.Dimension(160, 20));
		jLabel3.setLocation(new java.awt.Point(250, 170));
		jLabel3.setVisible(true);
		jLabel3.setText("© 2001 Chris Smith");
		jLabel3.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jLabel4.setSize(new java.awt.Dimension(250, 20));
		jLabel4.setLocation(new java.awt.Point(160, 190));
		jLabel4.setVisible(true);
		jLabel4.setText("A part of the Gallery Open Source Project");
		jLabel4.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jLabel5.setSize(new java.awt.Dimension(250, 20));
		jLabel5.setLocation(new java.awt.Point(160, 210));
		jLabel5.setVisible(true);
		jLabel5.setText("http://gallery.sourceforge.net");
		jLabel5.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		setLocation(new java.awt.Point(0, 0));
		setSize(new java.awt.Dimension(428, 263));
		setBackground(java.awt.Color.white);
		getContentPane().setLayout(null);
		setTitle("com.gallery.GalleryRemote.AboutBox");
		getContentPane().add(jLabel1);
		getContentPane().add(jLabel2);
		getContentPane().add(jLabelVersion);
		getContentPane().add(jLabel3);
		getContentPane().add(jLabel4);
		getContentPane().add(jLabel5);


		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

// END GENERATED CODE
	}
  
  	private boolean mShown = false;
  	
	public void addNotify() {
		super.addNotify();
		
		if (mShown)
			return;
			
		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
	}
	
}
