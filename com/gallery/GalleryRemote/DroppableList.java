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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
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

import javax.swing.JList;
import javax.swing.JOptionPane;

import com.gallery.GalleryRemote.model.Picture;

/**
 *  Drag and drop handler
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class DroppableList extends JList
		 implements DropTargetListener, DragSourceListener, DragGestureListener {
	private final static String MODULE = "Droplist";

	MainFrame mf = null;
	DragSource dragSource;
	DropTarget dropTarget;
	PictureSelection ps;
	int lastY = -1;


	public DroppableList() {
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this );
		dropTarget = new DropTarget( this, this );
	}

	public void setMainFrame( MainFrame mf ) {
		this.mf = mf;
	}
	
	int safeGetFixedCellHeight() {
		int height = getFixedCellHeight();
		if (height == -1) {
			height = (int) getCellRenderer()
				.getListCellRendererComponent(this, null, -1, false, false)
				.getPreferredSize().getHeight();
		}
		
		return height;
	}

	public int snap( int y ) {
		return snapIndex( y ) * safeGetFixedCellHeight();
	}

	public int snapIndex( int y ) {
		int height = safeGetFixedCellHeight();

		int row = (int) Math.floor( ( (float) y / height ) + .5 );
		if ( row > getModel().getSize() ) {
			row = getModel().getSize();
		}

		return row;
	}

	public void paint( Graphics g ) {
		lastY = -1;
		super.paint( g );
	}


	/* ********* TargetListener ********** */
	public void dragEnter( DropTargetDragEvent dropTargetDragEvent ) {
		Log.log( Log.TRACE, MODULE, "dragEnter - dtde" );
		if ( isEnabled() ) {
			dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
		} else {
			dropTargetDragEvent.rejectDrag( );
		}
	}

	public void dragExit( DropTargetEvent dropTargetEvent ) {
		Log.log( Log.TRACE, MODULE, "dragExit - dtde" );
		
		if ( ps != null ) {
			//add pictures back
			Log.log( Log.INFO, MODULE, "dragging internal selection out..." );
			try {
				java.util.List fileList = (java.util.List) ps.getTransferData( DataFlavor.javaFileListFlavor );
				if ( !ps.isEmpty() ) {
					mf.addPictures( (File[]) fileList.toArray( new File[0] ) );
				}
				//kill dragEvent
				//need to figure out why MainFrame becomes dropTarget for internal drops??!!
			} catch ( Exception e ) {
				Log.log( Log.INFO, MODULE, "coudn't kill drag " + e );
			}
		}
		ps = null;
		
		repaint();
	}

	public void dragOver( DropTargetDragEvent dropTargetDragEvent ) {
		//Log.log(Log.TRACE, MODULE,"dragOver - dtde");
		if ( isEnabled() ) {
			dragOver( (int) dropTargetDragEvent.getLocation().getY() );
		}
	}

	public void dragOver( int y ) {
		Graphics g = getGraphics();
		g.setXORMode( Color.cyan );
		int xStart = 10;
		int xStop = ( (int) this.getVisibleRect().getWidth() ) - xStart;
		if ( lastY != -1 ) {
			int ySnap = snap( lastY );
			g.drawLine( xStart, ySnap, xStop, ySnap );
			g.drawLine( xStart, ySnap + 1, xStop, ySnap + 1 );
		}

		lastY = y;

		int ySnap = snap( lastY );
		g.drawLine( xStart, ySnap, xStop, ySnap );
		g.drawLine( xStart, ySnap + 1, xStop, ySnap + 1 );
	}

	public synchronized void drop( DropTargetDropEvent dropTargetDropEvent ) {
		Log.log( Log.TRACE, MODULE, "drop - dtde" );
		
		if ( !isEnabled() ) {
			// for some reason this crappy system doesn't take rejectDrag for an answer
			return;
		}
		
		try {
			Transferable tr = dropTargetDropEvent.getTransferable();
			if ( tr.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
				dropTargetDropEvent.acceptDrop(
						DnDConstants.ACTION_COPY_OR_MOVE );
				java.util.List fileList = (java.util.List)
						tr.getTransferData( DataFlavor.javaFileListFlavor );

				/* recursively add contents of directories */
				try {
					fileList = expandDirectories( fileList );
				} catch ( IOException ioe ) {
					Log.log( Log.ERROR, MODULE, "i/o exception listing dirs in a drop" );
					Log.logStack( Log.ERROR, MODULE );
					JOptionPane.showMessageDialog(
							null,
							"It was not possible to accept the images due to an error.",
							"Error Accepting Dragged Images",
							JOptionPane.ERROR_MESSAGE );
				}

				//thanks John Zukowski
				Point dropLocation = dropTargetDropEvent.getLocation();
				int listIndex = snapIndex( (int) dropLocation.getY() );

				Log.log( Log.TRACE, MODULE, "Adding new image(s) to list at index " + listIndex );

				mf.addPictures( (File[]) fileList.toArray( new File[0] ), listIndex );

				dropTargetDropEvent.getDropTargetContext().dropComplete( true );
			} else {
				Log.log( Log.TRACE, MODULE, "rejecting drop: javaFileListFlavor is not supported for this data" );
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

	public void dropActionChanged( DropTargetDragEvent dropTargetDragEvent ) {
		Log.log( Log.TRACE, MODULE, "dropActionChanged - dtde" );
	}

	
	/* ********* DragSourceListener ********** */
	public void dragDropEnd( DragSourceDropEvent dragSourceDropEvent ) {
		Log.log( Log.TRACE, MODULE, "dragDropEnd - dsde" );
	}

	public void dragEnter( DragSourceDragEvent dragSourceDragEvent ) {
		/*Log.log(Log.TRACE, MODULE,"dragEnter - dsde");
		lastY = -1;*/
	}

	public void dragExit( DragSourceEvent dragSourceEvent ) {
		/*Log.log(Log.TRACE, MODULE,"dragExit - dse");
		lastY = -1;*/
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dragSourceDragEvent  Description of Parameter
	 */
	public void dragOver( DragSourceDragEvent dragSourceDragEvent ) {
		//Log.log(Log.TRACE, MODULE,"dragOver - dsde");
		//dragOver((int) dragSourceDragEvent.getLocation().getY());
	}

	public void dropActionChanged( DragSourceDragEvent dragSourceDragEvent ) { }


	/* ********* DragGestureListener ********** */
	public void dragGestureRecognized( DragGestureEvent event ) {
		Log.log( Log.TRACE, MODULE, "dragGestureRecognized" );
		int[] selIndices = this.getSelectedIndices();
		ps = new PictureSelection();
		for ( int i = 0; i < selIndices.length; i++ ) {
			int selIndex = selIndices[i];
			if ( selIndex != -1 ) {
				Picture p = (Picture) this.getModel().getElementAt( selIndex );
				ps.add( p );
			}
		}

		//pull out existing pictures
		if ( !ps.isEmpty() ) {
			dragSource.startDrag( event, DragSource.DefaultMoveDrop, ps, this );
			mf.deleteSelectedPictures();
		} else {
			Log.log( Log.TRACE, MODULE, "nothing was selected" );
		}

	}


	/* ********* Utilities ********** */
	List expandDirectories( java.util.List filesAndFolders )
		throws IOException {
		ArrayList allFilesList = new ArrayList();

		Iterator iter = filesAndFolders.iterator();
		while ( iter.hasNext() ) {
			File f = (File) iter.next();
			if ( f.isDirectory() ) {
				allFilesList.addAll( listFilesRecursive( f ) );
			} else {
				allFilesList.add( f );
			}
		}

		return allFilesList;
	}


	static List listFilesRecursive( File dir )
		throws IOException {
		ArrayList ret = new ArrayList();

		/* File.listFiles: stupid call returns null if there's an
				   i/o exception *or* if the file is not a directory, making a mess.
				   http://java.sun.com/j2se/1.4/docs/api/java/io/File.html#listFiles() */
		File[] fileArray = dir.listFiles();
		if ( fileArray == null ) {
			if ( dir.isDirectory() ) {
				/* convert to exception */
				throw new IOException( "i/o exception listing directory: " + dir.getPath() );
			} else {
				/* this method should only be called on a directory */
				Log.log( Log.CRITICAL, MODULE, "assertion failed: listFilesRecursive called on a non-dir file" );
				return ret;
			}
		}

		List files = Arrays.asList( fileArray );

		Iterator iter = files.iterator();
		while ( iter.hasNext() ) {
			File f = (File) iter.next();
			if ( f.isDirectory() ) {
				ret.addAll( listFilesRecursive( f ) );
			} else {
				ret.add( f );
			}
		}

		return ret;
	}
}

