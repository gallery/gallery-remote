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

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 *  Properties file for Gallery Remote
 *
 *@author     paour
 *@created    11 août 2002
 */
public class PropertiesFile extends Properties
{
	protected boolean read = false;
	protected boolean written = false;
	protected String mFilename;

	// caches
	protected Dimension thumbnailSize = null;
	protected Rectangle mainBounds = null;
	protected Rectangle previewBounds = null;

	public PropertiesFile( PropertiesFile p )
	{
		super( p );
	}

	public PropertiesFile( String name )
	{
		super();

		setFilename( name );
	}

	public PropertiesFile( PropertiesFile p, String name )
	{
		super( p );

		setFilename( name );
	}

	public File getCurrentDirectory()
	{
		String currentDirectory = (String) getProperty( "filedialogPath" );
		if ( currentDirectory != null )
		{
			return new File( currentDirectory );
		}
		else
		{
			return null;
		}
	}

	public void setCurrentDirectory( File currentDirectory )
	{
		setProperty( "filedialogPath", currentDirectory.getPath() );
	}

	public boolean getShowPreview()
	{
		return getBooleanProperty( "showPreview" );
	}

	public void setShowPreview( boolean showPreview )
	{
		setProperty( "showPreview", String.valueOf( showPreview ) );
	}

	public boolean getShowPath()
	{
		return getBooleanProperty( "showPath" );
	}

	public void setShowPath( boolean showPath )
	{
		setProperty( "showPath", String.valueOf( showPath ) );
	}

	public boolean getShowThumbnails()
	{
		return getBooleanProperty( "showThumbnails" );
	}

	public void setShowThumbnails( boolean showThumbnails )
	{
		setProperty( "showThumbnails", String.valueOf( showThumbnails ) );
	}

	public Dimension getThumbnailSize()
	{
		if ( thumbnailSize == null )
		{
			thumbnailSize = getDimensionProperty( "thumbnailSize" );
		}

		return thumbnailSize;
	}

	public Rectangle getMainBounds()
	{
		if ( mainBounds == null )
		{
			mainBounds = getRectangleProperty( "mainBounds" );
		}

		return mainBounds;
	}

	public Rectangle getPreviewBounds()
	{
		if ( previewBounds == null )
		{
			previewBounds = getRectangleProperty( "previewBounds" );
		}

		return previewBounds;
	}

	public void setThumbnailSize( Dimension size )
	{
		thumbnailSize = size;
		//write it to disk
	}

	public Dimension getDimensionProperty( String name )
	{
		String value = getProperty( name );

		int i;
		if ( value != null && ( i = value.indexOf( "," ) ) != -1 )
		{
			String width = value.substring( 0, i - 1 );
			String height = value.substring( i + 1, value.length() - 1 );

			return new Dimension( Integer.parseInt( width ), Integer.parseInt( height ) );
		}
		else
		{
			System.out.println( "Parameter " + name + " is missing or malformed (should be width,height)" );
			return null;
		}
	}

	public Rectangle getRectangleProperty( String name )
	{
		String value = getProperty( name );

		StringTokenizer st;
		if ( value != null && ( st = new StringTokenizer( value, "," ) ).countTokens() == 4 )
		{
			return new Rectangle( Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ) );
		}
		else
		{
			System.out.println( "Parameter " + name + " is missing or malformed (should be x,y,width,height)" );
			return null;
		}
	}

	public boolean getBooleanProperty( String name )
	{
		String booleanS = getProperty( name );
		try
		{
			return Boolean.valueOf( booleanS ).booleanValue();
		} catch (Exception e)
		{
			throw new NumberFormatException("Parameter " + name + " is missing or maformed (should be true or false)");
		}
	}

	public int getIntProperty( String name )
	{
		String intS = getProperty( name );
		try
		{
			return Integer.valueOf( intS ).intValue();
		} catch (Exception e)
		{
			throw new NumberFormatException("Parameter " + name + " is missing or maformed (should be an integer value");
		}
	}

	public String getProperty( String name )
	{
		if ( !read )
		{
			try
			{
				read();
			}
			catch ( FileNotFoundException e )
			{
				e.printStackTrace();
			}
		}

		return super.getProperty( name );
	}

	public Object setProperty( String name, String value )
	{
		written = false;

		return super.setProperty( name, value );
	}

	public synchronized void setFilename( String name )
	{
		mFilename = name + ".properties";
	}

	public synchronized void read() throws FileNotFoundException
	{
		if ( mFilename != null )
		{
			FileInputStream fileIn = null;
			try
			{
				fileIn = new FileInputStream( mFilename );
				load( fileIn );
			}
			catch ( IOException e )
			{
				//e.printStackTrace();
				write();
			}
			finally
			{
				try
				{
					fileIn.close();
				}
				catch ( IOException e2 )
				{
				}
				catch ( NullPointerException e3 )
				{
				}
			}
		}

		read = true;
		written = true;
	}

	public synchronized void write()
	{
		if ( !written )
		{
			FileOutputStream fileOut = null;
			try
			{
				fileOut = new FileOutputStream( mFilename );
				store( fileOut, null );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					fileOut.close();
				}
				catch ( IOException e2 )
				{
				}
				catch ( NullPointerException e3 )
				{
				}
			}
		}

		written = true;
	}
}

