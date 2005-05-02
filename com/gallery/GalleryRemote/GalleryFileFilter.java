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

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class GalleryFileFilter extends FileFilter {
	public static String[] ext
			= {"gif", "jpeg", "jpg", "avi", "mpg", "mpeg", "moov", "png", "jpe"};
	public static final List validExtensions = Arrays.asList(ext);

	public static String[] ext1
			= {"gif", "jpeg", "jpg", "png", "jpe"};
	public static final List manipulateExtensions = Arrays.asList(ext1);

	public static String[] ext2
			= {"jpg", "jpeg", "jpe"};
	public static final List manipulateJpegExtensions = Arrays.asList(ext2);

	// Accept all directories and all gif, jpg files.
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);

		return (extension != null && validExtensions.contains(extension));
	}

	public static boolean canManipulate(String filename) {
		String extension = getExtension(filename);

		return (extension != null && manipulateExtensions.contains(extension));
	}

	public static boolean canManipulateJpeg(String filename) {
		String extension = getExtension(filename);

		return (extension != null && manipulateJpegExtensions.contains(extension));
	}

	// The description of this filter
	public String getDescription() {
		return "Gallery Items";
	}

	public static String getExtension(File f) {
		return getExtension(f.getName());
	}

	public static String getExtension(String s) {
		String ext = null;
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}

		return ext;
	}
}

