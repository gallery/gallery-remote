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
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import java.awt.event.*;

import java.util.*;

import com.gallery.GalleryRemote.model.*;

/**
 *  Drag and drop handler
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class DroppableList extends JList
implements DropTargetListener, DragSourceListener, DragGestureListener {
    private static final String MODULE = "Droplist";
    
    MainFrame mDaddy = null;
    DragSource dragSource;
    DropTarget dropTarget;
    Point p;
    boolean isDrag;
    
    /**
     * constructor - initializes the DropTarget and DragSource.
     */
    
    public DroppableList() {
	dropTarget = new DropTarget(this, this);
	dragSource = new DragSource();
	dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
    }
    
    
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
	Log.log(Log.TRACE, "DROPLIST","dragEnter - dtde");
	dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
	isDrag = true;
	repaint();
    }
    
    /**
     *  Description of the Method
     *
     *@param  dropTargetEvent  Description of Parameter
     *@since
     */
    public void dragExit( DropTargetEvent dropTargetEvent ) {
	Log.log(Log.TRACE, "DROPLIST","dragExit - dtde");
	isDrag = false;
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  dropTargetDragEvent  Description of Parameter
     *@since
     */
    public void dragOver( DropTargetDragEvent dropTargetDragEvent ) {
	//Log.log(Log.TRACE, "DROPLIST","dragOver - dtde");
	p = dropTargetDragEvent.getLocation();
	repaint();
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  dropTargetDragEvent  Description of Parameter
     *@since
     */
    public void dropActionChanged( DropTargetDragEvent dropTargetDragEvent ) { }
    
    
    
    public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) {
    }
    
    public void dragEnter(DragSourceDragEvent dragSourceDragEvent) {
	Log.log(Log.TRACE, "DROPLIST","dragEnter - dsde");
	
    }
    
    public void dragExit(DragSourceEvent dragSourceEvent) {
	Log.log(Log.TRACE, "DROPLIST","dragExit - dse");
    }
    
    public void dragGestureRecognized( DragGestureEvent event) {
	Log.log(Log.TRACE, "DROPLIST","dragGestureRecognized");
	//Object selected = getSelectedValue();
	int ind = this.getSelectedIndex();
	if ( ind != -1 ){
	    Picture pic = (Picture) this.getModel().getElementAt(ind);
	    // as the name suggests, starts the dragging
	    
	    dragSource.startDrag(event, DragSource.DefaultMoveDrop, pic, this);
	    mDaddy.deleteSelectedPictures();
	    
	} else {
	    Log.log(Log.TRACE, "DROPLIST", "nothing was selected");
	}
    }
    
    public void dragOver(DragSourceDragEvent dragSourceDragEvent) {
	//Log.log(Log.TRACE, "DROPLIST","dragOver - dsde");
	repaint();
    }
    
    public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) {
    }
    
    
    
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
		
		/* recursively add contents of directories */
		java.util.List allFilesList = null;
		try {
		    allFilesList = expandDirectories( fileList );
		} catch ( IOException ioe ) {
		    Log.log( Log.ERROR, MODULE, "i/o exception listing dirs in a drop");
		    Log.logStack( Log.ERROR, MODULE );
		    JOptionPane.showMessageDialog(
		    null,
		    "It was not possible to accept the images due to an error.",
		    "Error Accepting Dragged Images",
		    JOptionPane.ERROR_MESSAGE);
		}
		
		//thanks John Zukowski
		Point dropLocation = dropTargetDropEvent.getLocation();
		//adjust for height of image (otherwise can never drop into 1st slot)
		dropLocation.setLocation(dropLocation.getX(),dropLocation.getY() - 1.0 );
		//Log.log(Log.TRACE, MODULE,"adjusted location X: " + dropLocation.getX() + "  Y: " + dropLocation.getY());
		int listIndex = this.locationToIndex(dropLocation);
		//add below selected, otherwise add to end
		if (listIndex == -1){
		    listIndex = this.getModel().getSize();
		}
		
		Log.log(Log.TRACE, "DROPLIST", "Adding new image(s) to list at index:" + listIndex);
		
		mDaddy.addPictures( (File[]) fileList.toArray( new File[0] ), listIndex );
		
		dropTargetDropEvent.getDropTargetContext().dropComplete( true );
		
	    } else {
		Log.log(Log.TRACE, MODULE, "rejecting drop: javaFileListFlavor is not supported for this data");
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
     *  Given a list of files and directories, the list is expanded
     *	recursively and returned with only files.
     *
     *	@param filesAndFolders	Files and/or directories.
     */
    java.util.List expandDirectories( java.util.List filesAndFolders ) throws IOException {
	
	java.util.ArrayList allFilesList = new ArrayList();
	
	Iterator iter = filesAndFolders.iterator();
	while ( iter.hasNext() ){
	    File f = (File)iter.next();
	    if ( f.isDirectory() ) {
		allFilesList.addAll( listFilesRecursive( f ) );
	    } else {
		allFilesList.add( f );
	    }
	}
	
	return allFilesList;
    }
    
    /**
     *  Given a directory, this method returns a recursive list of all the
     *	file contents of that directory.  It does not include the directories
     *	in the resulting List.
     *
     *	@param dir A directory to list recursively.  If the file is not a
     *		directory, an empty list is returned.
     */
    static java.util.List listFilesRecursive( File dir ) throws IOException {
	ArrayList ret = new ArrayList();
	
		/* File.listFiles: stupid call returns null if there's an
		   i/o exception *or* if the file is not a directory, making a mess.
		   http://java.sun.com/j2se/1.4/docs/api/java/io/File.html#listFiles() */
	File [] fileArray = dir.listFiles();
	if ( fileArray == null ) {
	    if ( dir.isDirectory() ) {
		/* convert to exception */
		throw new IOException( "i/o exception listing directory: " + dir.getPath() );
	    } else {
		/* this method should only be called on a directory */
		Log.log(Log.CRITICAL, MODULE, "assertion failed: listFilesRecursive called on a non-dir file");
		return ret;
	    }
	}
	java.util.List files = Arrays.asList( fileArray );
	
	Iterator iter = files.iterator();
	while( iter.hasNext() ) {
	    File f = (File)iter.next();
	    if ( f.isDirectory() ) {
		ret.addAll( listFilesRecursive( f ) );
	    } else {
		ret.add( f );
	    }
	}
	
	return ret;
    }
    
    final static BasicStroke stroke = new BasicStroke(1.0f);
    final static Color red = new Color(255,0,0);
    
    
    public void paintComponent( Graphics g ){
	super.paintComponent(g);
	//Log.log(Log.TRACE, MODULE, "painting component -- isDrag: " + isDrag);
	
	if (this.getModel().getSize() > 0 && isDrag){
	    //Log.log(Log.TRACE, MODULE, "adding line");
	    Graphics2D g2 = (Graphics2D)g;
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setStroke( stroke );
	    //make the line red
	    g2.setColor( red );
	    //Log.log(Log.TRACE, MODULE, "p:" + p);
	    //get index of drop spot
	    int curEl = this.locationToIndex(p);
	    //Log.log(Log.TRACE, MODULE, "curEl:" + curEl);
	    double lineY = 0.0;
	    if (curEl == -1){
		curEl = this.getModel().getSize();
	    }
	    lineY = (double) curEl * this.getFixedCellHeight();
	    //start x, y   end x, y
	    Line2D line = new Line2D.Double(10.0, lineY, this.getVisibleRect().getWidth() - 10.0, lineY);
	    g2.draw(line);
	}
    }
}
