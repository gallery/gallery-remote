package com.gallery.GalleryRemote.model;

import com.gallery.GalleryRemote.util.ImageUtils;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Sep 8, 2004
 * Time: 11:00:25 AM
 */
public class ExifData {
	String caption;
	Date creationDate;
	ImageUtils.AngleFlip targetOrientation;

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public ImageUtils.AngleFlip getTargetOrientation() {
		return targetOrientation;
	}

	public void setTargetOrientation(ImageUtils.AngleFlip targetOrientation) {
		this.targetOrientation = targetOrientation;
	}
}
