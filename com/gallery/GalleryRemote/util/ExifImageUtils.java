package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryFileFilter;
import com.gallery.GalleryRemote.Log;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 8, 2003
 */
public class ExifImageUtils {
	public static final String MODULE = "ExifUtils";
	
	public static ImageUtils.AngleFlip getExifTargetOrientation(String filename) {
		if (GalleryFileFilter.canManipulateJpeg(filename)) {
			File jpegFile = new File(filename);
			try {
				Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);

				Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
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

					Log.log(Log.LEVEL_TRACE, MODULE, "Angle: " + af.angle + " Flipped: " + af.flip);
					return af;
				}
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

	public static void resetExifOrientation(String filename) {
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
	}
}
