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
 * This interface includes status code constants for version 2 of the
 * Gallery Remote protocol.
 * 
 * @version $Id$
 * @author	<a href="mailto:tim_miller@users.sourceforge.net">Tim Miller</a>
 */
public interface GalleryComm2Consts {

	/** Protocol version string. No longer used by the server, still being sent... */
	public static final String PROTOCOL_VERSION = "2.3";

	/** Remote scriptname that provides version 2 of the protocol on the server. */
	public static final String PROTOCOL_MAGIC = "#__GR2PROTO__";


	/*
	 * STATUS CODES
	 */

	/**
	 * The command the client sent in the request completed successfully. The
	 * data (if any) in the response should be considered valid.
	 */
	public static final String GR_STAT_SUCCESS = "0";

	/** The protocol major version the client is using is not supported; */
	public static final String GR_STAT_PROTO_MAJ_VER_INVAL = "101";

	/** The protocol minor version the client is using is not supported. */
	public static final String GR_STAT_PROTO_MIN_VER_INVAL = "102";

	/** The format of the protocol version string the client sent in the request is invalid. */
	public static final String GR_STAT_PROTO_VER_FMT_INVAL = "103";

	/** The request did not contain the required protocol_version key. */
	public static final String GR_STAT_PROTO_VER_MISSING = "104";

	/** The password and/or username the client send in the request is invalid. */
	public static final String GR_STAT_PASSWD_WRONG = "201";

	/**
	 * The client used the login command in the request but failed to include
	 * either the username or password (or both) in the request.
	 */
	public static final String GR_STAT_LOGIN_MISSING = "202";

	/**
	 * The client used the login command in the request but failed to include
	 * either the username or password (or both) in the request.
	 */
	public static final String GR_STAT_UNKNOWN_CMD = "301";

	/** The user does not have permission to add an item to the gallery. */
	public static final String GR_STAT_NO_ADD_PERMISSION = "401";

	/** No filename was specified. */
	public static final String GR_STAT_NO_FILENAME = "402";

	/** The file was received, but could not be processed or added to the album. */
	public static final String GR_STAT_UPLOAD_PHOTO_FAIL = "403";

	/** The file was received, but could not be processed or added to the album. */
	public static final String GR_STAT_NO_CREATE_ALBUM_PERMISSION = "403";
}
