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

import com.gallery.GalleryRemote.model.Picture;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


/**
 * Picture model
 * 
 * @author paour
 * @created 11 août 2002
 */
public class PictureSelection extends ArrayList implements Transferable, ClipboardOwner {

	public static final DataFlavor[] flavors = {new DataFlavor(Picture.class, "Gallery Picture object (local)")};
	private static final java.util.List flavorList = Arrays.asList(flavors);

	public PictureSelection() {
	}

	public PictureSelection(JList list, boolean clone) {
		int[] selIndices = list.getSelectedIndices();
		for (int i = 0; i < selIndices.length; i++) {
			int selIndex = selIndices[i];
			if (selIndex != -1) {
				Picture p = (Picture) list.getModel().getElementAt(selIndex);
				if (clone) {
					p = (Picture) p.clone();
				}
				add(p);
			}
		}
	}
	
	/* Transferable interface */

	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavorList.contains(flavor));
	}

	public synchronized Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {

		if (isDataFlavorSupported(flavor)) {
			ArrayList deepClone = new ArrayList(size());
			for (Iterator it = iterator(); it.hasNext();) {
				deepClone.add(((Picture) it.next()).clone());
			}
			return deepClone;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	/* ClipboardOwner interface */

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
}