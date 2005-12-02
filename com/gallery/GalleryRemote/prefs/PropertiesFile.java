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
package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;

import java.io.*;
import java.util.*;

/**
 * Properties file for Gallery Remote
 * 
 * @author paour
 */
public class PropertiesFile extends GalleryProperties {
	public static final String MODULE = "PropsFile";

	protected boolean read = false;
	protected boolean written = false;
	protected String mFilename;
	protected boolean readOnly = false;
	protected boolean alreadyWarned = false;

	/**
	 * Constructor for the PropertiesFile object
	 * 
	 * @param p Description of Parameter
	 */
	public PropertiesFile(PropertiesFile p) {
		super(p);
	}


	/**
	 * Constructor for the PropertiesFile object
	 * 
	 * @param name Description of Parameter
	 */
	public PropertiesFile(String name) {
		super();

		setFilename(name);
	}


	/**
	 * Constructor for the PropertiesFile object
	 * 
	 * @param p    Description of Parameter
	 * @param name Description of Parameter
	 */
	public PropertiesFile(PropertiesFile p, String name) {
		super(p);

		setFilename(name);
	}


	/**
	 * Overrides default method to track dirty state
	 */
	public Object setProperty(String name, String value) {
		// if we're read-only, see if our defaults can write
		if (readOnly) {
			if (defaults != null) {
				defaults.setProperty(name, value);
			} else {
				Log.log(Log.LEVEL_ERROR, MODULE, "Read-only PropertyFile without a default: setProperty was attempted");
				Log.logStack(Log.LEVEL_ERROR, MODULE);
			}

			return getProperty(name);
		}

		written = false;

		if (value == null) {
			return remove(name);
		} else {
			return super.setProperty(name, value);
		}
	}


	/**
	 * Change the filename of the file (why would you want to do that?)
	 * 
	 * @param name The new filename value
	 */
	public synchronized void setFilename(String name) {
		if (! name.endsWith(".lax") && ! name.endsWith(".properties")) {
			mFilename = name + ".properties";
		} else {
			mFilename = name;
		}
	}

	public void setReadOnly() {
		readOnly = true;
	}


	/**
	 * Read a property as a string, read the file in first
	 * so you don't have to explicitly read the file in beforehand.
	 * 
	 * @param name Name of the property
	 * @return The property value
	 */
	public String getProperty(String name) {
		checkRead();

		return super.getProperty(name);
	}

	public Enumeration propertyNames() {
		checkRead();

		return super.propertyNames();
	}

	public boolean isOverridden(String name) {
		return readOnly && get(name) != null;
	}

	protected void checkRead() {
		if (defaults != null && defaults instanceof PropertiesFile) {
			// also load defaults
			((PropertiesFile) defaults).checkRead();
		}

		if (!read) {
			try {
				read();
			} catch (FileNotFoundException e) {
				if (!alreadyWarned) {
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
					alreadyWarned = true;
				}
			}
		}
	}


	/**
	 * Read the property file from disk
	 * 
	 * @throws java.io.FileNotFoundException Description of Exception
	 */
	public synchronized void read()
			throws FileNotFoundException {
		if (mFilename != null) {
			InputStream fileIn = null;
			try {
				// try to get from JAR
				if (mFilename.indexOf("/") == -1) {
					// only if the resource is local
					try {
						if (!alreadyWarned) {
							Log.log(Log.LEVEL_TRACE, MODULE, "Trying to find " + mFilename + " in Classpath");
						}
						fileIn = PropertiesFile.class.getResourceAsStream("/" + mFilename);
					} catch (IllegalArgumentException iae) {
						// Opera throws an exception for invalid filenames...
						fileIn = null;
					}
				}

				if (fileIn == null) {
					// no dice? OK, from cwd then...
					if (!alreadyWarned) {
						Log.log(Log.LEVEL_TRACE, MODULE, "Trying to find " + mFilename + " in Current Working Dir");
					}
					
					fileIn = new FileInputStream(mFilename);
				}

				load(fileIn);
			} catch (FileNotFoundException fnf) {
				throw fnf;
			} catch (IOException e) {
				// Todo: what should happen here?
			} finally {
				try {
					fileIn.close();
				} catch (IOException e2) {
				} catch (NullPointerException e3) {
				}
			}
		}

		read = true;
		written = true;
	}


	/**
	 * Write the property file to disk
	 */
	public synchronized void write() {
		if (readOnly && defaults != null && defaults instanceof PropertiesFile) {
			((PropertiesFile) defaults).write();
			written = true;
		}

		if (!written && !readOnly) {
			FileOutputStream fileOut = null;
			try {
				fileOut = new FileOutputStream(mFilename);
				store(fileOut, null);
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} finally {
				try {
					fileOut.close();
				} catch (IOException e2) {
				} catch (NullPointerException e3) {
				}
			}

			written = true;
		}
	}

	/**
	 * Writes this property list (key and element pairs) in this
	 * <code>Properties</code> table to the output stream in a format suitable
	 * for loading into a <code>Properties</code> table using the
	 * <code>load</code> method.
	 * The stream is written using the ISO 8859-1 character encoding.
	 * <p/>
	 * Properties from the defaults table of this <code>Properties</code>
	 * table (if any) are <i>not</i> written out by this method.
	 * <p/>
	 * If the header argument is not null, then an ASCII <code>#</code>
	 * character, the header string, and a line separator are first written
	 * to the output stream. Thus, the <code>header</code> can serve as an
	 * identifying comment.
	 * <p/>
	 * Next, a comment line is always written, consisting of an ASCII
	 * <code>#</code> character, the current date and time (as if produced
	 * by the <code>toString</code> method of <code>Date</code> for the
	 * current time), and a line separator as generated by the Writer.
	 * <p/>
	 * Then every entry in this <code>Properties</code> table is written out,
	 * one per line. For each entry the key string is written, then an ASCII
	 * <code>=</code>, then the associated element string. Each character of
	 * the element string is examined to see whether it should be rendered as
	 * an escape sequence. The ASCII characters <code>\</code>, tab, newline,
	 * and carriage return are written as <code>\\</code>, <code>\t</code>,
	 * <code>\n</code>, and <code>\r</code>, respectively. Characters less
	 * than <code>&#92;u0020</code> and characters greater than
	 * <code>&#92;u007E</code> are written as <code>&#92;u</code><i>xxxx</i> for
	 * the appropriate hexadecimal value <i>xxxx</i>. Leading space characters,
	 * but not embedded or trailing space characters, are written with a
	 * preceding <code>\</code>. The key and value characters <code>#</code>,
	 * <code>!</code>, <code>=</code>, and <code>:</code> are written with a
	 * preceding slash to ensure that they are properly loaded.
	 * <p/>
	 * After the entries have been written, the output stream is flushed.  The
	 * output stream remains open after this method returns.
	 * 
	 * @param out    an output stream.
	 * @param header a description of the property list.
	 * @throws java.io.IOException            if writing this property list to the specified
	 *                                        output stream throws an <tt>IOException</tt>.
	 * @throws java.lang.ClassCastException   if this <code>Properties</code> object
	 *                                        contains any keys or values that are not <code>Strings</code>.
	 * @throws java.lang.NullPointerException if <code>out</code> is null.
	 * @since 1.2
	 */
	public synchronized void store(OutputStream out, String header)
			throws IOException {
		BufferedWriter awriter;
		awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
		if (header != null)
			writeln(awriter, "#" + header);
		writeln(awriter, "#" + new Date().toString());
		ArrayList v = new ArrayList(keySet());
		Collections.sort(v);
		for (Iterator e = v.iterator(); e.hasNext();) {
			String key = (String) e.next();
			String val = (String) get(key);
			key = saveConvert(key, true);

			/* No need to escape embedded and trailing spaces for value, hence
			* pass false to flag.
			*/
			val = saveConvert(val, false);
			writeln(awriter, key + "=" + val);
		}
		awriter.flush();
	}

	/*
	* Converts unicodes to encoded &#92;uxxxx
	* and writes out any of the characters in specialSaveChars
	* with a preceding slash
	*/
	private String saveConvert(String theString, boolean escapeSpace) {
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len * 2);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			switch (aChar) {
				case ' ':
					if (x == 0 || escapeSpace)
						outBuffer.append('\\');

					outBuffer.append(' ');
					break;
				case '\\':
					outBuffer.append('\\');
					outBuffer.append('\\');
					break;
				case '\t':
					outBuffer.append('\\');
					outBuffer.append('t');
					break;
				case '\n':
					outBuffer.append('\\');
					outBuffer.append('n');
					break;
				case '\r':
					outBuffer.append('\\');
					outBuffer.append('r');
					break;
				case '\f':
					outBuffer.append('\\');
					outBuffer.append('f');
					break;
				default:
					if ((aChar < 0x0020) || (aChar > 0x007e)) {
						outBuffer.append('\\');
						outBuffer.append('u');
						outBuffer.append(toHex((aChar >> 12) & 0xF));
						outBuffer.append(toHex((aChar >> 8) & 0xF));
						outBuffer.append(toHex((aChar >> 4) & 0xF));
						outBuffer.append(toHex(aChar & 0xF));
					} else {
						if (specialSaveChars.indexOf(aChar) != -1)
							outBuffer.append('\\');
						outBuffer.append(aChar);
					}
			}
		}
		return outBuffer.toString();
	}

	private static void writeln(BufferedWriter bw, String s) throws IOException {
		bw.write(s);
		bw.newLine();
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param	nibble	the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	private static final String specialSaveChars = "=: \t\r\n\f#!";

	/** A table of hex digits */
	private static final char[] hexDigit = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
}