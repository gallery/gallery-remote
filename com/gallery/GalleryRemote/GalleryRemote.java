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
import javax.swing.*;

public class GalleryRemote {
	public GalleryRemote() {
		try {
			// For native Look and Feel, uncomment the following code.
			/// *
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} 
			catch (Exception e) { 
			}
			//* /
			MainFrame frame = new MainFrame();
			frame.initComponents();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Main entry point
	static public void main(String[] args) {
		new GalleryRemote();
	}
	
}
