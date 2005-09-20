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


/**
 * This interface includes capacity keys for various versions of
 * the Gallery Remote protocols.
 * 
 * @author Pierre-Luc Paour
 * @version $id$
 */
public interface GalleryCommCapabilities {
	public static final int CAPA_UPLOAD_FILES = 1;
	public static final int CAPA_FETCH_ALBUMS = 2;
	public static final int CAPA_UPLOAD_CAPTION = 3;
	public static final int CAPA_FETCH_HIERARCHICAL = 4;
	public static final int CAPA_ALBUM_INFO = 5;
	public static final int CAPA_NEW_ALBUM = 6;
	public static final int CAPA_FETCH_ALBUMS_PRUNE = 7;
	public static final int CAPA_FORCE_FILENAME = 8;
	public static final int CAPA_FETCH_ALBUM_IMAGES = 9;
	public static final int CAPA_MOVE_ALBUM = 10;
	public static final int CAPA_FETCH_ALBUMS_TOO = 11;
	public static final int CAPA_FETCH_NON_WRITEABLE_ALBUMS = 12;
	public static final int CAPA_FETCH_HONORS_HIDDEN = 13;
	public static final int CAPA_IMAGE_MAX_SIZE = 14;
	public static final int CAPA_INCREMENT_VIEW_COUNT = 15;
}
