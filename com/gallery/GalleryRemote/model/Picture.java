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

package com.gallery.GalleryRemote.model;

import java.io.*;

/**
 *  Picture model
 *
 *@author     paour
 *@created    11 août 2002
 */
public class Picture
{
	File source;
	String caption;
	
	public Picture() {}

	public Picture(File source)
	{
		this.source = source;
	}

	public void setSource( File source )
	{
		this.source = source;
	}

	public void setCaption( String caption )
	{
		this.caption = caption;
	}

	public File getSource()
	{
		return source;
	}

	public String getCaption()
	{
		return caption;
	}
}

