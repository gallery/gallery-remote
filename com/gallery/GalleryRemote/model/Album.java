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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  Album model
 *
 *@author     paour
 *@created    11 août 2002
 */

public class Album extends Picture implements ListModel
{
	Vector pictures = new Vector();
	String name;
	String url;
	String username;
	String password;

	// ListModel
	Vector listeners = new Vector( 1 );

	public Enumeration getPictures()
	{
		return pictures.elements();
	}

	public void addPicture( Picture p )
	{
		pictures.addElement( p );
		
		notifyListeners();
	}

	public void addPicture( File file )
	{
		pictures.addElement( new Picture( file ) );
		
		notifyListeners();
	}

	public void addPictures( File[] files )
	{
		for ( int i = 0; i < files.length; i++ )
		{
			pictures.addElement( new Picture( files[i] ) );
		}
		
		notifyListeners();
	}

	public int sizePictures()
	{
		return pictures.size();
	}

	public void clearPictures()
	{
		pictures.clear();
		
		notifyListeners();
	}

	public void removePicture( int n )
	{
		pictures.remove( n );
		
		notifyListeners();
	}

	public void removePictures( int[] indices )
	{
		for ( int i = indices.length - 1; i >= 0; i-- )
		{
			pictures.remove( indices[i] );
		}
		
		notifyListeners();
	}

	public Picture getPicture( int n )
	{
		return (Picture) pictures.get( n );
	}

	public void setPicture( int n, Picture p )
	{
		pictures.set( n, p );
		
		notifyListeners();
	}
	
	public ArrayList getFileList()
	{
		ArrayList l = new ArrayList(pictures.size());
		
		Enumeration e = pictures.elements();
		while (e.hasMoreElements())
		{
			l.add(((Picture) e.nextElement()).getSource());
		}
		
		return l;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	public String getName()
	{
		return name;
	}

	public String getUrl()
	{
		return url;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
	
	// ListModel
	public int getSize()
	{
		return pictures.size();
	}

	public Object getElementAt( int index )
	{
		return pictures.elementAt( index );
	}

	public void addListDataListener( ListDataListener ldl )
	{
		listeners.addElement( ldl );
	}

	public void removeListDataListener( ListDataListener ldl )
	{
		listeners.removeElement( ldl );
	}

	void notifyListeners()
	{
		ListDataEvent lde = new ListDataEvent( com.gallery.GalleryRemote.GalleryRemote.getInstance().mainFrame, ListDataEvent.CONTENTS_CHANGED, 0, pictures.size() );

		Enumeration e = listeners.elements();
		while ( e.hasMoreElements() )
		{
			( (ListDataListener) e.nextElement() ).contentsChanged( lde );
		}
	}
}

