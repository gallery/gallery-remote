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

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

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
    int dropInd;
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
    }
    
    
    
    /**
     *  Description of the Method
     *
     *@param  dropTargetEvent  Description of Parameter
     *@since
     */
    public void dragExit( DropTargetEvent dropTargetEvent ) {
        Log.log(Log.TRACE, "DROPLIST","dragExit - dtde");
    }
    
    
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
            Picture p = (Picture) this.getModel().getElementAt(ind);
            // as the name suggests, starts the dragging
            
            dragSource.startDrag(event, DragSource.DefaultMoveDrop, p, this);
            mDaddy.deleteSelectedPictures();
        } else {
            Log.log(Log.TRACE, "DROPLIST", "nothing was selected");
        }
    }
    
    public void dragOver(DragSourceDragEvent dragSourceDragEvent) {
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
                //adjust for height of image (fixed for time being, needs to be dynamic according to thumnail)
                dropLocation.setLocation(dropLocation.getX(), dropLocation.getY() - 100);   //doesn't work :(
                int listIndex = this.locationToIndex(dropLocation);
                Log.log(Log.TRACE, "DROPLIST", "location:" + dropLocation.y);
                //add below selected, otherwise add to end
                if (listIndex == -1){
                    listIndex = this.getModel().getSize();
                } else {
                    listIndex++;
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
    
    
}

