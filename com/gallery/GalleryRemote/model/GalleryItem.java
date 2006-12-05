/*
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Feb 25, 2004
 * Time: 7:26:45 PM
 */
package com.gallery.GalleryRemote.model;

import com.gallery.GalleryRemote.util.HTMLEscaper;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.Vector;

public abstract class GalleryItem extends DefaultMutableTreeNode implements Cloneable {
	public static final String MODULE = "GalleryItem";

	transient Gallery gallery = null;
	String caption = null;

	transient String escapedCaption = null;

	public GalleryItem(Gallery gallery) {
		this.gallery = gallery;
	}

	public Object clone() {
		GalleryItem newGalleryItem = null;

		newGalleryItem = (GalleryItem) super.clone();

		newGalleryItem.caption = caption;
		newGalleryItem.gallery = gallery;

		return newGalleryItem;
	}

	public Album getParentAlbum() {
		return (Album) getParent();
	}

	public void setCaption(String caption) {
		this.caption = caption;
		this.escapedCaption = null;
	}

	public String getCaption() {
		return caption;
	}

	/**
	 * Cache the escapedCaption because the escaping is lengthy and this is called by a frequent UI method
	 *
	 * @return the HTML escaped version of the caption
	 */
	public String getEscapedCaption() {
		if (escapedCaption == null) {
			if (caption != null) {
				escapedCaption = HTMLEscaper.escape(caption);
			}
		}

		return escapedCaption;
	}

	/*
	 ****** Overriden methods from DefaultMutableTreeNode to send notifications ******
	 */

	/*public void insert(MutableTreeNode newChild, int childIndex) {
		if (!allowsChildren) {
			throw new IllegalStateException("node does not allow children");
		} else if (newChild == null) {
			throw new IllegalArgumentException("new child is null");
		} else if (isNodeAncestor(newChild)) {
			throw new IllegalArgumentException("new child is an ancestor");
		}

		MutableTreeNode oldParent = (MutableTreeNode) newChild.getParent();

		if (oldParent != null) {
			oldParent.remove(newChild);
		}

		newChild.setParent(this);

		if (children == null) {
			children = new Vector();
		}

		children.insertElementAt(newChild, childIndex);
		gallery.nodesWereInserted(this, new int[] {childIndex});
	}

	public void remove(int childIndex) {
		MutableTreeNode child = (MutableTreeNode)getChildAt(childIndex);
		children.removeElementAt(childIndex);
		//gallery.nodesWereRemoved(this, new int[] {childIndex}, new Object[] {child});
		gallery.nodeStructureChanged(this);
		child.setParent(null);
	}*/
}