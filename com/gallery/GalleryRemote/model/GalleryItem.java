/*
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Feb 25, 2004
 * Time: 7:26:45 PM
 */
package com.gallery.GalleryRemote.model;

import com.gallery.GalleryRemote.util.HTMLEscaper;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class GalleryItem extends DefaultMutableTreeNode implements Cloneable {
	public static final String MODULE = "GalleryItem";

	transient Gallery gallery = null;
	String description = null;
	String url = null;
	boolean canEdit = true;

	transient String escapedDescription = null;

	public GalleryItem(Gallery gallery) {
		this.gallery = gallery;
	}

	public Object clone() {
		GalleryItem newGalleryItem = null;

		newGalleryItem = (GalleryItem) super.clone();

		newGalleryItem.description = description;
		newGalleryItem.gallery = gallery;

		return newGalleryItem;
	}

	public Album getParentAlbum() {
		return (Album) getParent();
	}

	public void setDescription(String caption) {
		this.description = caption;
		this.escapedDescription = null;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setCanEdit(boolean b) {
		canEdit = b;
	}

	public boolean getCanEdit() {
		return canEdit;
	}

	/**
	 * Cache the escapedDescription because the escaping is lengthy and this is called by a frequent UI method
	 *
	 * @return the HTML escaped version of the description
	 */
	public String getEscapedDescription() {
		if (escapedDescription == null) {
			if (description != null) {
				escapedDescription = HTMLEscaper.escape(description);
			}
		}

		return escapedDescription;
	}
}