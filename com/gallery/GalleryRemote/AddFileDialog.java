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

import java.awt.Component;
import java.util.*;
import javax.swing.JFileChooser;
import java.io.File;

public class AddFileDialog
{
	static File[] addFiles(Component parent)
	{
		JFileChooser fc = new JFileChooser();
		
		fc.addChoosableFileFilter(new GalleryFileFilter());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		
		File currentDirectory = GalleryRemote.getInstance().properties.getCurrentDirectory();
		if (currentDirectory != null)
		{
			fc.setCurrentDirectory(currentDirectory);
		}

		int retval = fc.showDialog(parent, "Add");
		if (retval != JFileChooser.CANCEL_OPTION)
		{
			GalleryRemote.getInstance().properties.setCurrentDirectory(fc.getCurrentDirectory());
			
			File[] files = fc.getSelectedFiles();
			
			return files;
		}
			
		return null;
	}
}
