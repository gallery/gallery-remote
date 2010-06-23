package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.GalleryRemote;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Shut down the OS. Idea by Nick77.
 * OS names from <http://www.tolstoy.com/samizdat/sysprops.html>
 * User: paour
 * Date: Sep 25, 2003
 */
public class OsShutdown {
	public static final String MODULE = "Shutdown";
	public static final String osname = System.getProperty("os.name").toLowerCase();

	public static void shutdown() {
		Method m = getPrivateShutdownMethod();
		
		if (m != null) {
			try {
				m.invoke(null);
			} catch (Throwable e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}
	}
	
	private static Method getPrivateShutdownMethod() {
		try {
			Class c = GalleryRemote.secureClassForName("com.gallery.GalleryRemote.util.PrivateShutdown");
			return c.getMethod("shutdown");
		} catch (Throwable e) {
			Log.log(Log.LEVEL_TRACE, MODULE, "Could not load PrivateShutdown, this is expected for the applet");
			return null;
		}
	}

	public static boolean isWindows() {
		return osname.indexOf("windows") != -1;
	}

	public static boolean isWin9x() {
		return isWindows() && (osname.indexOf("9") != -1 || osname.indexOf("me") != -1);
	}

	public static boolean isWinNT() {
		return isWindows() && !isWin9x();
	}

	public static boolean isMacOS() {
		return osname.indexOf("mac") != -1 && osname.indexOf("os") != -1;
	}

	public static boolean isMacOSX() {
		return osname.indexOf("mac os x") != -1;
	}

	public static boolean isLinux() {
		return osname.indexOf("linux") != -1;
	}

	public static boolean isSolaris() {
		return osname.indexOf("solaris") != -1;
	}

	public static boolean isUnix() {
		return isMacOSX() || isLinux() || isSolaris();
	}

	public static boolean canShutdown() {
		return (isWin9x() || isWinNT() || (isUnix() && !isMacOSX())) && getPrivateShutdownMethod() != null;
	}
}
