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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;

/**
 *  Drag and drop handler
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class DroppableTree
		extends JTree implements DropTargetListener {

	protected final static String MODULE = "Droptree";
	protected static GRI18n grRes = GRI18n.getInstance();
	MainFrame mf = null;

	DropTarget dropTarget;
	int lastRow = -1;
	int scrollPace = 0;

	public DroppableTree() {
		dropTarget = new DropTarget( this, this );
	}

	public void paint( Graphics g ) {
		lastRow = -1;
		super.paint( g );
	}

	public boolean isDragOK(DropTargetEvent dropTargetEvent) {
		boolean result;

		if (!isEnabled()) {
			// return immediately, no painting is likely to have been done
			return false;
		}

		if (dropTargetEvent instanceof DropTargetDragEvent) {
			Point dropLocation = ((DropTargetDragEvent) dropTargetEvent).getLocation();
			if (getPathForLocation((int) dropLocation.getX(), (int) dropLocation.getY()) == null) {
				result = false;
			} else {
				result = ((DropTargetDragEvent) dropTargetEvent).isDataFlavorSupported(DataFlavor.javaFileListFlavor)
						|| ((DropTargetDragEvent) dropTargetEvent).isDataFlavorSupported(PictureSelection.flavors[0]);
			}
		} else {
			Point dropLocation = ((DropTargetDropEvent) dropTargetEvent).getLocation();
			if (getPathForLocation((int) dropLocation.getX(), (int) dropLocation.getY()) == null) {
				result = false;
			} else {
				result = ((DropTargetDropEvent) dropTargetEvent).isDataFlavorSupported(DataFlavor.javaFileListFlavor)
						|| ((DropTargetDropEvent) dropTargetEvent).isDataFlavorSupported(PictureSelection.flavors[0]);
			}
		}

		if (!result && lastRow != -1) {
			Graphics g = getGraphics();
			g.setXORMode( Color.cyan );
			g.drawRect( 0, lastRow * rowHeight, getWidth(), rowHeight);

			lastRow = -1;
		}

		return result;
	}

	/* ********* TargetListener ********** */
	public void dragEnter( DropTargetDragEvent dropTargetDragEvent ) {
		Log.log( Log.LEVEL_TRACE, MODULE, "dragEnter - dtde" );
		if (! isDragOK(dropTargetDragEvent)) {
			dropTargetDragEvent.rejectDrag( );
			return;
		}

		dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
	}

	public void dragExit( DropTargetEvent dropTargetEvent ) {
		Log.log( Log.LEVEL_TRACE, MODULE, "dragExit - dtde" );

		repaint();
	}

	public void dragOver( DropTargetDragEvent dropTargetDragEvent ) {
		//Log.log(Log.TRACE, MODULE,"dragOver - dtde");
		if ( ! isDragOK(dropTargetDragEvent) ) {
			dropTargetDragEvent.rejectDrag( );
			return;
		}

		dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
		dragOver( (int) dropTargetDragEvent.getLocation().getY() );
	}

	public void dragOver( int y ) {
		int row = snapIndex(y);
		int rowHeight = getRowHeight();
		Rectangle r = getVisibleRect();
		boolean scrolled = false;

		Log.log(Log.LEVEL_TRACE, MODULE, "row: " + row + " lastRow: " + lastRow + " rowHeight: " + rowHeight);
		if (y < r.getY() + getRowHeight() && row > 0) {
			int tmpLastRow = lastRow;
			//scrollRowToVisible(row - 1);
			scrollRectToVisible(new Rectangle(0, (int) r.getY() - rowHeight, 0, 0));
			lastRow = tmpLastRow;
			scrolled = true;
		}
		if (y > r.getY() + r.getHeight() - getRowHeight() && row < getRowCount() - 1) {
			int tmpLastRow = lastRow;
			//scrollRowToVisible(row + 1);
			scrollRectToVisible(new Rectangle(0, (int) (r.getY() + r.getHeight() + rowHeight), 0, 0));
			lastRow = tmpLastRow;
			scrolled = true;
		}
		/*if (scrolled) {
			r = getVisibleRect();
			scrollRectToVisible(new Rectangle(0, (int) r.getY(), 0, 0));
		}*/

		Graphics g = getGraphics();
		g.setXORMode( Color.cyan );
		int xStart = 0;
		int xStop = getWidth() - xStart;
		if ( lastRow != -1 ) {
			g.drawRect( xStart, lastRow * rowHeight, xStop, rowHeight);
		}

		lastRow = row;

		if ( lastRow != -1 ) {
			g.drawRect( xStart, lastRow * rowHeight, xStop, rowHeight);
		}

		if (scrolled) {
			scrollPace++;

			try {
				Thread.sleep(scrollPace > 5 ? 10 : 200);
			} catch (InterruptedException e) {	}
		} else {
			scrollPace = 0;
		}
	}

	public synchronized void drop( DropTargetDropEvent dropTargetDropEvent ) {
		Log.log( Log.LEVEL_TRACE, MODULE, "drop - dtde" );
		
		if ( ! isDragOK(dropTargetDropEvent) ) {
			dropTargetDropEvent.rejectDrop();
			return;
		}

		try {
			Transferable tr = dropTargetDropEvent.getTransferable();

			dropTargetDropEvent.acceptDrop(
					DnDConstants.ACTION_COPY_OR_MOVE );

			//thanks John Zukowski
			Point dropLocation = dropTargetDropEvent.getLocation();
			Album album = (Album) getPathForLocation((int) dropLocation.getX(), (int) dropLocation.getY()).getLastPathComponent();

			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List fileList = (List)
						tr.getTransferData( DataFlavor.javaFileListFlavor );

				/* recursively add contents of directories */
				try {
					fileList = ImageUtils.expandDirectories( fileList );
				} catch ( IOException ioe ) {
					Log.log( Log.LEVEL_ERROR, MODULE, "i/o exception listing dirs in a drop" );
					Log.logStack( Log.LEVEL_ERROR, MODULE );
					JOptionPane.showMessageDialog(
							null,
							grRes.getString(MODULE, "imgError"),
							grRes.getString(MODULE, "dragError"),
							JOptionPane.ERROR_MESSAGE );
				}

				Log.log( Log.LEVEL_TRACE, MODULE, "Adding " + fileList.size() + " new files(s) to album " + album );

				mf.addPictures( album, (File[]) fileList.toArray( new File[0] ), false);
			} else {
				List pictureList = (List)
						tr.getTransferData( PictureSelection.flavors[0] );

				Log.log( Log.LEVEL_TRACE, MODULE, "Adding " + pictureList.size() + " new pictures(s) to album " + album );

				mf.addPictures( album, (Picture[]) pictureList.toArray( new Picture[0] ), false);
			}

			dropTargetDropEvent.getDropTargetContext().dropComplete( true );
		} catch ( IOException io ) {
			Log.logException(Log.LEVEL_ERROR, MODULE, io);
			dropTargetDropEvent.getDropTargetContext().dropComplete( false );
		} catch ( UnsupportedFlavorException ufe ) {
			Log.logException(Log.LEVEL_ERROR, MODULE, ufe);
			dropTargetDropEvent.getDropTargetContext().dropComplete( false );
		}

		repaint();
	}

	public void dropActionChanged( DropTargetDragEvent dropTargetDragEvent ) {
		Log.log( Log.LEVEL_TRACE, MODULE, "dropActionChanged - dtde" );
		if ( ! isDragOK(dropTargetDragEvent) ) {
			dropTargetDragEvent.rejectDrag();
			return;
		}

		dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
	}

	public void setMainFrame( MainFrame mf ) {
		this.mf = mf;
	}

	public int snap( int y ) {
		return snapIndex( y ) * getRowHeight();
	}

	public int snapIndex( int y ) {
		int height = getRowHeight();

		int row = (int) Math.floor( (float) y / height );
		if ( row > getRowCount() ) {
			row = -1;
		}

		return row;
	}
}