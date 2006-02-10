package com.gallery.GalleryRemote.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.gallery.GalleryRemote.GalleryFileFilter;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.model.ExifData;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 8, 2003
 */
public class ExifImageUtils {
	public static final String MODULE = "ExifUtils";

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	public static ExifData getExifData(String filename) {
		if (GalleryFileFilter.canManipulateJpeg(filename)) {
			File jpegFile = new File(filename);

			try {
				Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);

				ExifData exif = new ExifData();

				exif.setCaption(getCaption(exifDirectory, iptcDirectory, filename));
				exif.setTargetOrientation(getTargetOrientation(exifDirectory, iptcDirectory, filename));
				exif.setCreationDate(getCreationDate(exifDirectory, iptcDirectory, filename));

				return exif;
			} catch (FileNotFoundException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
				return null;
			} catch (JpegProcessingException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
				return null;
			}
		} else {
			return null;
		}
	}

    public static Date getCreationDate(Directory exifDirectory, Directory iptcDirectory, String filename) {
		String exifDateCreated = exifDirectory.getString(ExifDirectory.TAG_DATETIME_ORIGINAL);

		if (exifDateCreated == null) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename +
					" has no EXIF Date Created");
			return null;
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename +
				" has EXIF Date Created of " + exifDateCreated);
		try {
			return sdf.parse(exifDateCreated);
		} catch (ParseException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			return null;
		}
	}
	
	public static String getCaption(Directory exifDirectory, Directory iptcDirectory, String filename) {
		String caption = exifDirectory.getString(ExifDirectory.TAG_IMAGE_DESCRIPTION);

		if (caption != null && caption.length() != 0) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " TAG_IMAGE_DESCRIPTION: " + caption);
			return caption.trim();
		}

		caption = exifDirectory.getString(ExifDirectory.TAG_USER_COMMENT);

		if (caption != null && caption.length() !=0) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " TAG_USER_COMMENT: " + caption);
			return caption.trim();
		}

		caption = iptcDirectory.getString(IptcDirectory.TAG_CAPTION);

		if (caption != null && caption.length() !=0) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " IPTC DESCRIPTION: " + caption);
			return caption.trim();
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " has no usable EXIF or IPTC info");
		return null;
	}

	public static ImageUtils.AngleFlip getTargetOrientation(Directory exifDirectory, Directory iptcDirectory, String filename) {
		String orientation = exifDirectory.getString(ExifDirectory.TAG_ORIENTATION);

		if (orientation == null) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " has no EXIF ORIENTATION tag");
			return null;
		} else {
			Log.log(Log.LEVEL_TRACE, MODULE, "Picture " + filename + " EXIF ORIENTATION: " + orientation);

			int or = 0;
			ImageUtils.AngleFlip af = null;
			try {
				or = Integer.parseInt(orientation);
			} catch (NumberFormatException e) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't parse orientation " + orientation + " for " + filename);
				return null;
			}

			switch (or) {
				case 1:
					af = new ImageUtils.AngleFlip(0, false);
					break;

				case 2:
					af = new ImageUtils.AngleFlip(0, true);
					break;

				case 3:
					af = new ImageUtils.AngleFlip(2, false);
					break;

				case 4:
					af = new ImageUtils.AngleFlip(2, true);
					break;

				case 5:
					af = new ImageUtils.AngleFlip(1, true);
					break;

				case 6:
					af = new ImageUtils.AngleFlip(1, false);
					break;

				case 7:
					af = new ImageUtils.AngleFlip(3, true);
					break;

				case 8:
					af = new ImageUtils.AngleFlip(3, false);
					break;

				default:
					Log.log(Log.LEVEL_ERROR, MODULE, "Couldn't parse orientation " + orientation + " for " + filename);
					return null;
			}

			return af;
		}
	}

	/*public static void resetExifOrientation(String filename) {
		if (GalleryFileFilter.canManipulateJpeg(filename)) {
			File jpegFile = new File(filename);
			try {
				Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);

				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
				exifDirectory.setString(ExifDirectory.TAG_ORIENTATION, "1");

				// todo: this doesn't do anything at present: the library can only READ
				// EXIF, not write to it...
			} catch (FileNotFoundException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} catch (JpegProcessingException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}
	}*/
}
