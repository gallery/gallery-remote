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

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

/**
 *  Drag and drop handler
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class DroppableList extends JList
		 implements DropTargetListener
{
	DropTarget dropTarget = new DropTarget( this, this );
	MainFrame mDaddy = null;


	/**
	 *  Constructor for the DroppableList object
	 *
	 *@param  daddy  Description of Parameter
	 *@since
	 */
	public void setMainFrame( MainFrame daddy ) {
		//setModel( new DefaultListModel() );
		mDaddy = daddy;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDragEvent  Description of Parameter
	 *@since
	 */
	public void dragEnter( DropTargetDragEvent dropTargetDragEvent ) {
		dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetEvent  Description of Parameter
	 *@since
	 */
	public void dragExit( DropTargetEvent dropTargetEvent ) { }


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDragEvent  Description of Parameter
	 *@since
	 */
	public void dragOver( DropTargetDragEvent dropTargetDragEvent ) { }


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDragEvent  Description of Parameter
	 *@since
	 */
	public void dropActionChanged( DropTargetDragEvent dropTargetDragEvent ) { }


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDropEvent  Description of Parameter
	 *@since
	 */
	public synchronized void drop( DropTargetDropEvent dropTargetDropEvent ) {
		try {
			Transferable tr = dropTargetDropEvent.getTransferable();
			if ( tr.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) && this.isEnabled() ) {
				dropTargetDropEvent.acceptDrop(
						DnDConstants.ACTION_COPY_OR_MOVE );
				java.util.List fileList = (java.util.List)
						tr.getTransferData( DataFlavor.javaFileListFlavor );

				mDaddy.addPictures( (File[]) fileList.toArray( new File[0] ) );

				dropTargetDropEvent.getDropTargetContext().dropComplete( true );
			} else {
				System.err.println( "Rejected" );
				dropTargetDropEvent.rejectDrop();
			}
		} catch ( IOException io ) {
			io.printStackTrace();
			dropTargetDropEvent.rejectDrop();
		} catch ( UnsupportedFlavorException ufe ) {
			ufe.printStackTrace();
			dropTargetDropEvent.rejectDrop();
		}
	}
}

