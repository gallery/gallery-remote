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

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

/**
 * Drag and drop handler
 * 
 * @author paour
 * @created August 16, 2002
 */
public class DroppableTree
		extends JTree implements DropTargetListener {

	protected final static String MODULE = "Droptree";
	MainFrame mf = null;

	DropTarget dropTarget;
	int lastRow = -1;
	int scrollPace = 0;

	public DroppableTree() {
		dropTarget = new DropTarget(this, this);
	}

	public void paint(Graphics g) {
		lastRow = -1;
		super.paint(g);
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
				// Java 1.6 bug: getPathForLocation inexplicably returns false at the top or bottom of the component
				//Log.log(Log.LEVEL_TRACE, MODULE, "getPathForLocation" + dropLocation + " is false");
				result = false;
			} else {
				result = ((DropTargetDragEvent) dropTargetEvent).isDataFlavorSupported(DataFlavor.javaFileListFlavor)
						|| ((DropTargetDragEvent) dropTargetEvent).isDataFlavorSupported(PictureSelection.flavors[0]);
			}
		} else {
			Point dropLocation = ((DropTargetDropEvent) dropTargetEvent).getLocation();
			if (getPathForLocation((int) dropLocation.getX(), (int) dropLocation.getY()) == null) {
				//Log.log(Log.LEVEL_TRACE, MODULE, "getPathForLocation" + dropLocation + " is false");
				result = false;
			} else {
				result = ((DropTargetDropEvent) dropTargetEvent).isDataFlavorSupported(DataFlavor.javaFileListFlavor)
						|| ((DropTargetDropEvent) dropTargetEvent).isDataFlavorSupported(PictureSelection.flavors[0]);
			}
		}

		if (!result && lastRow != -1) {
			Graphics g = getGraphics();
			g.setXORMode(Color.cyan);
			g.drawRect(0, lastRow * rowHeight, getWidth(), rowHeight);

			lastRow = -1;
		}
		
		//Log.log(Log.LEVEL_TRACE, MODULE, "isDragOk: " + result);

		return result;
	}

	/* ********* TargetListener ********** */
	public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
		Log.log(Log.LEVEL_TRACE, MODULE, "dragEnter - dtde");
		for (Iterator it = dropTargetDragEvent.getCurrentDataFlavorsAsList().iterator(); it.hasNext();) {
			DataFlavor flavor = (DataFlavor) it.next();
			Log.log(Log.LEVEL_TRACE, MODULE, "Flavor: " + flavor.getHumanPresentableName() + " -- " +
					flavor.getMimeType());
		}
		Log.log(Log.LEVEL_TRACE, MODULE, "Action: " + dropTargetDragEvent.getSourceActions() + " -- " +
				dropTargetDragEvent.getDropAction());

		if (!isDragOK(dropTargetDragEvent)) {
			dropTargetDragEvent.rejectDrag();
			return;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Accepting drag");
		//dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE );
	}

	public void dragExit(DropTargetEvent dropTargetEvent) {
		Log.log(Log.LEVEL_TRACE, MODULE, "dragExit - dtde");

		repaint();
	}

	public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
		//Log.log(Log.TRACE, MODULE,"dragOver - dtde");
		if (!isDragOK(dropTargetDragEvent)) {
			dropTargetDragEvent.rejectDrag();
			return;
		}

		dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		dragOver((int) dropTargetDragEvent.getLocation().getY());
	}

	public void dragOver(int y) {
		int row = snapIndex(y);
		int rowHeight = getRowHeight();
		Rectangle r = getVisibleRect();
		boolean scrolled = false;

		//Log.log(Log.LEVEL_TRACE, MODULE, "row: " + row + " lastRow: " + lastRow + " rowHeight: " + rowHeight);
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
		g.setXORMode(Color.cyan);
		int xStart = 0;
		int xStop = getWidth() - xStart;
		if (lastRow != -1 && lastRow != row) {
			g.drawRect(xStart, lastRow * rowHeight, xStop, rowHeight);
		}

		if (row != -1 && lastRow != row) {
			g.drawRect(xStart, row * rowHeight, xStop, rowHeight);
		}

		lastRow = row;

		if (scrolled) {
			scrollPace++;

			try {
				Thread.sleep(scrollPace > 5 ? 10 : 200);
			} catch (InterruptedException e) {
			}
		} else {
			scrollPace = 0;
		}
	}

	public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
		Log.log(Log.LEVEL_TRACE, MODULE, "drop - dtde");

		if (!isDragOK(dropTargetDropEvent)) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Refusing drop");
			dropTargetDropEvent.rejectDrop();
			return;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Accepting drop");

		try {
			Transferable tr = dropTargetDropEvent.getTransferable();

			dropTargetDropEvent.acceptDrop(
					DnDConstants.ACTION_COPY_OR_MOVE);

			//thanks John Zukowski
			Point dropLocation = dropTargetDropEvent.getLocation();
			Album album = (Album) getPathForLocation((int) dropLocation.getX(), (int) dropLocation.getY()).getLastPathComponent();

			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List fileList = (List)
						tr.getTransferData(DataFlavor.javaFileListFlavor);

				/* recursively add contents of directories */
				try {
					fileList = ImageUtils.expandDirectories(fileList);
				} catch (IOException ioe) {
					Log.log(Log.LEVEL_ERROR, MODULE, "i/o exception listing dirs in a drop");
					Log.logStack(Log.LEVEL_ERROR, MODULE);
					JOptionPane.showMessageDialog(
							null,
							GRI18n.getString(MODULE, "imgError"),
							GRI18n.getString(MODULE, "dragError"),
							JOptionPane.ERROR_MESSAGE);
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Adding " + fileList.size() + " new files(s) to album " + album);

				mf.addPictures(album, (File[]) fileList.toArray(new File[0]), false);
			} else {
				List pictureList = (List)
						tr.getTransferData(PictureSelection.flavors[0]);

				Log.log(Log.LEVEL_TRACE, MODULE, "Adding " + pictureList.size() + " new pictures(s) to album " + album);

				mf.addPictures(album, (Picture[]) pictureList.toArray(new Picture[0]), false);
			}

			dropTargetDropEvent.getDropTargetContext().dropComplete(true);
		} catch (IOException io) {
			Log.logException(Log.LEVEL_ERROR, MODULE, io);
			dropTargetDropEvent.getDropTargetContext().dropComplete(false);
		} catch (UnsupportedFlavorException ufe) {
			Log.logException(Log.LEVEL_ERROR, MODULE, ufe);
			dropTargetDropEvent.getDropTargetContext().dropComplete(false);
		}

		repaint();
	}

	public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
		Log.log(Log.LEVEL_TRACE, MODULE, "dropActionChanged - dtde");
		if (!isDragOK(dropTargetDragEvent)) {
			dropTargetDragEvent.rejectDrag();
			return;
		}

		//dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	public void setMainFrame(MainFrame mf) {
		this.mf = mf;
	}

	public int snap(int y) {
		return snapIndex(y) * getRowHeight();
	}

	public int snapIndex(int y) {
		int height = getRowHeight();

		int row = (int) Math.floor((float) y / height);
		if (row > getRowCount()) {
			row = -1;
		}

		return row;
	}

	public Point getToolTipLocation(MouseEvent event) {
		try {
			Point p = event.getPoint();
			int selRow = getRowForLocation(p.x, p.y);

			TreePath path = getPathForRow(selRow);
			Rectangle pathBounds = getPathBounds(path);

			Point location = pathBounds.getLocation();
			location.translate(-4, 0);
			return location;
		} catch (Exception e) {
			return null;
		}
	}

	public String getToolTipText(MouseEvent event) {
		// this code is copied from JTree, to only show tooltip when longer than the tree
		if(event != null) {
			Point p = event.getPoint();
			int selRow = getRowForLocation(p.x, p.y);
			TreeCellRenderer       r = getCellRenderer();

			if(selRow != -1 && r != null) {
				TreePath     path = getPathForRow(selRow);
				Object       lastPath = path.getLastPathComponent();
				Component    rComponent = r.getTreeCellRendererComponent
					(this, lastPath, isRowSelected(selRow),
					 isExpanded(selRow), getModel().isLeaf(lastPath), selRow,
					 true);

				Rectangle pathBounds = getPathBounds(path);

				if(rComponent instanceof JComponent && pathBounds.x + pathBounds.width > getParent().getWidth()) {
					MouseEvent      newEvent;

					p.translate(-pathBounds.x, -pathBounds.y);
					newEvent = new MouseEvent(rComponent, event.getID(),
										  event.getWhen(),
											  event.getModifiers(),
											  p.x, p.y, event.getClickCount(),
											  event.isPopupTrigger());

					return ((JComponent)rComponent).getToolTipText(newEvent);
				}
			}
		}
		return null;
	}
}