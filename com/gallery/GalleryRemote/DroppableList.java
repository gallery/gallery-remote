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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.gallery.GalleryRemote.model.*;

/**
 *  Drag and drop handler
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class DroppableList extends JList
		 implements DropTargetListener, DragSourceListener, DragGestureListener
{
	private final static String MODULE = "Droplist";

	MainFrame mDaddy = null;
	DragSource dragSource;
	DropTarget dropTarget;
	PictureSelection ps;
	int lastY = -1;


	/**
	 *  constructor - initializes the DropTarget and DragSource.
	 */

	public DroppableList() {
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this );
		dropTarget = new DropTarget( this, this );
	}


	/**
	 *  Constructor for the DroppableList object
	 *
	 *@param  daddy  Description of Parameter
	 *@since
	 */
	public void setMainFrame( MainFrame daddy ) {
		mDaddy = daddy;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  y  Description of Parameter
	 *@return    Description of the Returned Value
	 */
	public int snap( int y ) {
		return snapIndex( y ) * getFixedCellHeight();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  y  Description of Parameter
	 *@return    Description of the Returned Value
	 */
	public int snapIndex( int y ) {
		int height = getFixedCellHeight();

		int row = (int) Math.floor( ( (float) y / height ) + .5 );
		if ( row > getModel().getSize() ) {
			row = getModel().getSize();
		}

		return row;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of Parameter
	 */
	public void paint( Graphics g ) {
		lastY = -1;
		super.paint( g );
	}


	/* ********* TargetListener ********** */
	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDragEvent  Description of Parameter
	 *@since
	 */
	public void dragEnter( DropTargetDragEvent dropTargetDragEvent ) {
		Log.log( Log.TRACE, MODULE, "dragEnter - dtde" );
		if ( isEnabled() ) {
			dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
		} else {
			dropTargetDragEvent.rejectDrag( );
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetEvent  Description of Parameter
	 *@since
	 */
	public void dragExit( DropTargetEvent dropTargetEvent ) {
		Log.log( Log.TRACE, MODULE, "dragExit - dtde" );
		
		if ( ps != null ) {
			//add pictures back
			Log.log( Log.INFO, MODULE, "dragging internal selection out..." );
			try {
				java.util.List fileList = (java.util.List) ps.getTransferData( DataFlavor.javaFileListFlavor );
				if ( !ps.isEmpty() ) {
					Picture p = (Picture) ps.get( 0 );
					mDaddy.addPictures( (File[]) fileList.toArray( new File[0] ), p.getListIndex() );
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


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDragEvent  Description of Parameter
	 *@since
	 */
	public void dragOver( DropTargetDragEvent dropTargetDragEvent ) {
		//Log.log(Log.TRACE, MODULE,"dragOver - dtde");
		if ( isEnabled() ) {
			dragOver( (int) dropTargetDragEvent.getLocation().getY() );
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  y  Description of Parameter
	 */
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


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDropEvent  Description of Parameter
	 *@since
	 */
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
				java.util.List allFilesList = null;
				try {
					allFilesList = expandDirectories( fileList );
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

				Log.log( Log.TRACE, MODULE, "Adding new image(s) to list at index:" + listIndex );

				mDaddy.addPictures( (File[]) fileList.toArray( new File[0] ), listIndex );

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


	/**
	 *  Description of the Method
	 *
	 *@param  dropTargetDragEvent  Description of Parameter
	 *@since
	 */
	public void dropActionChanged( DropTargetDragEvent dropTargetDragEvent ) {
		Log.log( Log.TRACE, MODULE, "dropActionChanged - dtde" );
	}

	
	/* ********* DragSourceListener ********** */
	/**
	 *  Description of the Method
	 *
	 *@param  dragSourceDropEvent  Description of Parameter
	 */
	public void dragDropEnd( DragSourceDropEvent dragSourceDropEvent ) {
		Log.log( Log.TRACE, MODULE, "dragDropEnd - dsde" );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dragSourceDragEvent  Description of Parameter
	 */
	public void dragEnter( DragSourceDragEvent dragSourceDragEvent ) {
		/*Log.log(Log.TRACE, MODULE,"dragEnter - dsde");
		lastY = -1;*/
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dragSourceEvent  Description of Parameter
	 */
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


	/**
	 *  Description of the Method
	 *
	 *@param  dragSourceDragEvent  Description of Parameter
	 */
	public void dropActionChanged( DragSourceDragEvent dragSourceDragEvent ) { }



	/* ********* DragGestureListener ********** */
	/**
	 *  Description of the Method
	 *
	 *@param  event  Description of Parameter
	 */
	public void dragGestureRecognized( DragGestureEvent event ) {
		Log.log( Log.TRACE, MODULE, "dragGestureRecognized" );
		int[] selIndices = this.getSelectedIndices();
		ps = new PictureSelection();
		for ( int i = 0; i < selIndices.length; i++ ) {
			int selIndex = selIndices[i];
			if ( selIndex != -1 ) {
				Picture p = (Picture) this.getModel().getElementAt( selIndex );
				p.setListIndex( selIndex );
				ps.add( p );
			}
		}
		//pull out existing pictures
		if ( !ps.isEmpty() ) {
			dragSource.startDrag( event, DragSource.DefaultMoveDrop, ps, this );
			mDaddy.deleteSelectedPictures();

		} else {
			Log.log( Log.TRACE, MODULE, "nothing was selected" );
		}

	}


	/* ********* Utilities ********** */
	/**
	 *  Given a list of files and directories, the list is expanded recursively and
	 *  returned with only files.
	 *
	 *@param  filesAndFolders  Files and/or directories.
	 *@return                  Description of the Returned Value
	 *@exception  IOException  Description of Exception
	 */
	java.util.List expandDirectories( java.util.List filesAndFolders )
		throws IOException {
		java.util.ArrayList allFilesList = new ArrayList();

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


	/**
	 *  Given a directory, this method returns a recursive list of all the file
	 *  contents of that directory. It does not include the directories in the
	 *  resulting List.
	 *
	 *@param  dir              A directory to list recursively. If the file is not
	 *      a directory, an empty list is returned.
	 *@return                  Description of the Returned Value
	 *@exception  IOException  Description of Exception
	 */
	static java.util.List listFilesRecursive( File dir )
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
		java.util.List files = Arrays.asList( fileArray );

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

