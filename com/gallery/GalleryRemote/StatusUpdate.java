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
 *	This interface decouples the status updating methods from MainFrame.
 *
 *	@author     <a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
 *	@version    $id$
 */
public interface StatusUpdate {
	public void setStatus( String message );
	
	public int startProgress( int min, int max, String message);
	
	public void updateProgressValue( int progressId, int value );
	public void updateProgressValue( int progressId, int value, int maxValue );
	
	public void updateProgressStatus( int progressId, String message );
	
	public void stopProgress( int progressId, String message );
	
	public void setInProgress(boolean inProgress);
	
	public void error (String message);
}
