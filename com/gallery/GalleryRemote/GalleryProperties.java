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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *  GalleryProperties: access property data with a higher level of abstraction
 *
 *@author     paour
 *@created    11 août 2002
 */
public class GalleryProperties extends Properties {
	public static final String MODULE = "GalProps";
	
	SimpleDateFormat dateFormat
		= new SimpleDateFormat ("yyyy/MM/dd");
	
	// caches
	protected Dimension thumbnailSize = null;
	protected Rectangle mainBounds = null;
	protected Rectangle previewBounds = null;


	/**
	 *  Constructor for the PropertiesFile object
	 *
	 *@param  p  Description of Parameter
	 */
	public GalleryProperties( Properties p ) {
		super( p );
	}


	/**
	 *  Constructor for the PropertiesFile object
	 */
	public GalleryProperties( ) {}


	/**
	 *  Gets the currentDirectory attribute of the PropertiesFile object
	 *
	 *@return    The currentDirectory value
	 */
	public File getCurrentDirectory() {
		String currentDirectory = getProperty( "filedialogPath" );
		if ( currentDirectory != null ) {
			return new File( currentDirectory );
		} else {
			return null;
		}
	}

	/**
	 *  Sets the currentDirectory attribute of the PropertiesFile object
	 *
	 *@param  currentDirectory  The new currentDirectory value
	 */
	public void setCurrentDirectory( File currentDirectory ) {
		setProperty( "filedialogPath", currentDirectory.getPath() );
	}


	/**
	 *  Gets the showPreview attribute of the PropertiesFile object
	 *
	 *@return    The showPreview value
	 */
	public boolean getShowPreview() {
		return getBooleanProperty( "showPreview" );
	}


	/**
	 *  Sets the showPreview attribute of the PropertiesFile object
	 *
	 *@param  showPreview  The new showPreview value
	 */
	public void setShowPreview( boolean showPreview ) {
		setProperty( "showPreview", String.valueOf( showPreview ) );
	}


	/**
	 *  Gets the showPath attribute of the PropertiesFile object
	 *
	 *@return    The showPath value
	 */
	public boolean getShowPath() {
		return getBooleanProperty( "showPath" );
	}


	/**
	 *  Sets the showPath attribute of the PropertiesFile object
	 *
	 *@param  showPath  The new showPath value
	 */
	public void setShowPath( boolean showPath ) {
		setProperty( "showPath", String.valueOf( showPath ) );
	}


	/**
	 *  Gets the showThumbnails attribute of the PropertiesFile object
	 *
	 *@return    The showThumbnails value
	 */
	public boolean getShowThumbnails() {
		return getBooleanProperty( "showThumbnails" );
	}


	/**
	 *  Sets the showThumbnails attribute of the PropertiesFile object
	 *
	 *@param  showThumbnails  The new showThumbnails value
	 */
	public void setShowThumbnails( boolean showThumbnails ) {
		setProperty( "showThumbnails", String.valueOf( showThumbnails ) );
	}


	/**
	 *  Gets the thumbnailSize attribute of the PropertiesFile object
	 *
	 *@return    The thumbnailSize value
	 */
	public Dimension getThumbnailSize() {
		if ( thumbnailSize == null ) {
			thumbnailSize = getDimensionProperty( "thumbnailSize" );
		}

		return thumbnailSize;
	}


	/**
	 *  Gets the mainBounds attribute of the PropertiesFile object
	 *
	 *@return    The mainBounds value
	 */
	public Rectangle getMainBounds() {
		if ( mainBounds == null ) {
			mainBounds = getRectangleProperty( "mainBounds" );
		}

		return mainBounds;
	}


	/**
	 *  Gets the previewBounds attribute of the PropertiesFile object
	 *
	 *@return    The previewBounds value
	 */
	public Rectangle getPreviewBounds() {
		if ( previewBounds == null ) {
			previewBounds = getRectangleProperty( "previewBounds" );
		}

		return previewBounds;
	}


	/**
	 *  Sets the mainBounds attribute of the PropertiesFile object
	 *
	 *@param  r  The new mainBounds value
	 */
	public void setMainBounds( Rectangle r ) {
		setRectangleProperty( "mainBounds", r );
	}


	/**
	 *  Sets the previewBounds attribute of the PropertiesFile object
	 *
	 *@param  r  The new previewBounds value
	 */
	public void setPreviewBounds( Rectangle r ) {
		setRectangleProperty( "previewBounds", r );
	}


	/**
	 *  Sets the thumbnailSize attribute of the PropertiesFile object
	 *
	 *@param  size  The new thumbnailSize value
	 */
	public void setThumbnailSize( Dimension size ) {
		thumbnailSize = size;
		setDimensionProperty("thumbnailSize", size);
	}


	/**
	 *  Gets the dimensionProperty attribute of the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 *@return       The dimensionProperty value
	 */
	public Dimension getDimensionProperty( String name ) {
		String value = getProperty( name );
		if (value == null) return null;

		StringTokenizer st;
		if ( value != null && ( st = new StringTokenizer( value, "," ) ).countTokens() == 2 ) {
			return new Dimension( Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ) );
		} else {
			Log.log(Log.ERROR, MODULE,  "Parameter " + name + " is missing or malformed (should be width,height)" );
			return null;
		}
	}


	/**
	 *  Sets the dimensionProperty attribute of the PropertiesFile object
	 *
	 *@param  name  The new dimensionProperty value
	 *@param  d     The new dimensionProperty value
	 */
	public void setDimensionProperty( String name, Dimension d ) {
		setProperty( name, ( (int) d.getWidth() ) + "," + ( (int) d.getHeight() ) );
	}


	/**
	 *  Gets the rectangleProperty attribute of the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 *@return       The rectangleProperty value
	 */
	public Rectangle getRectangleProperty( String name ) {
		String value = getProperty( name );
		if (value == null) return null;

		StringTokenizer st;
		if ( value != null && ( st = new StringTokenizer( value, "," ) ).countTokens() == 4 ) {
			return new Rectangle( Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ),
					Integer.parseInt( st.nextToken() ) );
		} else {
			Log.log(Log.ERROR, MODULE,  "Parameter " + name + " is missing or malformed (should be x,y,width,height)" );
			return null;
		}
	}


	/**
	 *  Sets the rectangleProperty attribute of the PropertiesFile object
	 *
	 *@param  name  The property name
	 *@param  rect  The new rectangle value
	 */
	public void setRectangleProperty( String name, Rectangle rect ) {
		setProperty( name, ( (int) rect.getX() ) + "," + ( (int) rect.getY() ) + ","
				 + ( (int) rect.getWidth() ) + "," + ( (int) rect.getHeight() ) );
	}


	/**
	 *  Gets the booleanProperty attribute of the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 *@return       The booleanProperty value
	 */
	public boolean getBooleanProperty( String name ) {
		String booleanS = getProperty( name );
		try {
			return Boolean.valueOf( booleanS ).booleanValue();
		} catch ( Exception e ) {
			throw new NumberFormatException( "Parameter " + name + " is missing or malformed (should be true or false)" );
		}
	}

	public void setBooleanProperty( String name, boolean value ) {
		setProperty( name, value?"true":"false" );
	}


	/**
	 *  Gets the intProperty attribute of the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 *@return       The intProperty value
	 */
	public int getIntProperty( String name ) {
		String intS = getProperty( name );
		try {
			return Integer.valueOf( intS ).intValue();
		} catch ( Exception e ) {
			throw new NumberFormatException( "Parameter " + name + " is missing or malformed (should be an integer value)" );
		}
	}


	/**
	 *  Sets the intProperty attribute of the PropertiesFile object
	 *
	 *@param  name   The new intProperty value
	 *@param  value  The new intProperty value
	 */
	public void setIntProperty( String name, int value ) {
		setProperty( name, String.valueOf( value ) );
	}


	/**
	 *  Gets the base64Property attribute of the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 *@return       The base64Property value
	 */
	public String getBase64Property( String name ) {
		String base64S = getProperty( name );
		if (base64S == null) return null;
		
		try {
			return Base64.decode( base64S );
		} catch ( Error e ) {
			throw new NumberFormatException( "Parameter " + name + " is missing or malformed (should be a Base64 value)" );
		}
	}


	/**
	 *  Set a property as a Base64 value
	 *
	 *@param  name   Name of the property
	 *@param  value  The value of the property
	 */
	public void setBase64Property( String name, String value ) {
		setProperty( name, Base64.encode( value ) );
	}
	

	/**
	 *  Gets the date Property attribute of the PropertiesFile object
	 *
	 *@param  name  Description of Parameter
	 *@return       The date Property value
	 */
	public Date getDateProperty( String name ) {
		String dateS = getProperty( name );
		if (dateS == null) return null;
		
		try {
			return dateFormat.parse( dateS );
		} catch ( ParseException e ) {
			throw new NumberFormatException( "Parameter " + name + " is missing or malformed (should be a Date value (yyyy/mm/dd))" );
		}
	}


	/**
	 *  Set a property as a Date value
	 *
	 *@param  name   Name of the property
	 *@param  value  The value of the property
	 */
	public void setDateProperty( String name, Date date ) {
		setProperty( name, dateFormat.format( date ) );
	}
	
	
	public String getProperty( String name, String defaultValue ) {
		String tmp = getProperty( name );
		
		if (tmp == null) {
			return defaultValue;
		} else {
			return tmp;
		}
	}


    public void logProperties(int level, String module) {
		if (module == null) {
			module = MODULE;
		}
		
		ArrayList names = new ArrayList(100);
        Enumeration e = propertyNames();
        while (e.hasMoreElements()) {
            names.add( e.nextElement() );
        }
		
		Object[] namesArray = names.toArray();
		Arrays.sort(namesArray);
		
		for (int i = 0; i < namesArray.length; i++) {
			String name = (String) namesArray[i];
			Log.log(level, module, name + "= |" + getProperty(name) + "|");
		}
    }

	public void uncache()	{
		thumbnailSize = null;
		mainBounds = null;
		previewBounds = null;
	}
}

