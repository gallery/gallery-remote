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

import com.gallery.GalleryRemote.model.Picture;

/**
 * This interface decouples the status updating methods from MainFrame.
 * 
 * @author <a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
 * @version $id$
 */
public interface StatusUpdate {
	public static final int LEVEL_GENERIC = 0;
	public static final int LEVEL_BACKGROUND = 1;
	public static final int LEVEL_CACHE = 2;
	public static final int LEVEL_UPLOAD_ONE = 3;
	public static final int LEVEL_UPLOAD_PROGRESS = 4;
	public static final int LEVEL_UNINTERUPTIBLE = 5;
	public static final int NUM_LEVELS = 6;

	/* level-bound methods */
	public void setStatus(String message);

	public void startProgress(int level, int min, int max, String message, boolean undetermined);

	public void updateProgressValue(int level, int value);

	public void updateProgressValue(int level, int value, int maxValue);

	public void updateProgressStatus(int level, String message);

	public void setUndetermined(int level, boolean undetermined);

	public int getProgressValue(int level);

	public int getProgressMinValue(int level);

	public int getProgressMaxValue(int level);

	public void stopProgress(int level, String message);

	/* level-independant methods */
	public void setInProgress(boolean inProgress);

	public void error(String message);

	public void doneUploading(String newItemName, Picture picture);
}
