package com.gallery.GalleryRemote.insecureutil;

import com.gallery.GalleryRemote.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

public class UnsignedTest {
	public static final String MODULE = "UnsignedTest";
	
	public static void createFile() {
		// if this code is not signed, the expectation is that the following will 
		// throw an exception and not create the file
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(new File(System.getProperty("java.io.tmpdir"), "GalleryRemoteUnsignedTest.txt")));
			
			writer.write("Obviously the file was created");
			writer.close();
		} catch (Throwable e) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Error creating the file, this is expected");
			Log.logException(Log.LEVEL_TRACE, MODULE, e);
		}
	}
}
