package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Picture;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 20, 2004
 */
public interface CancellableTransferListener {
	public boolean dataTransferred(int transferred, int overall, double kbPerSecond, Picture picture);
}
