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

import java.io.*;
import java.util.*;

public class PropertiesFile extends Properties {

	protected String mFilename;

	public PropertiesFile(String name) {
		super();

		this.setFilename(name);

		//-- Load our settings from the file if it exists ---
		try {
			this.read();
		} catch (FileNotFoundException e) {
		};
	}

	public synchronized void setFilename(String name) {

		String newName = name + ".properties";
		mFilename = newName;
	}


	public synchronized void read() throws FileNotFoundException {


		FileInputStream fileIn = null;
		try {

			fileIn = new FileInputStream(mFilename);
			this.load(fileIn);

		} catch (IOException e) {
		} finally {
			try { 
				fileIn.close(); 
			} catch (IOException e2) {
			} catch (NullPointerException e3) {
			};
		};

	}

	public synchronized void write() {

		FileOutputStream fileOut = null;
		try {

			fileOut = new FileOutputStream(mFilename);
			this.store(fileOut, null);

		} catch (IOException e) {
		} finally {
			try { fileOut.close(); 
			} catch (IOException e2) {
			} catch (NullPointerException e3) {
			};
		};

	}

}

