package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.Log;

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
		String win9x = "rundll32 user,exitwindows";
		String winNT = "shutdown -s -f -t 01";
		String unix = "shutdown -fh now";

		String cmd = null;

		if (isWin9x()) {
			cmd = win9x;
		} else if (isWinNT()) {
			cmd = winNT;
		} else if (isUnix()) {
			cmd = unix;
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Platform not recognized; shutdown will not be performed");
		}

		try {
			if (cmd != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Executing " + cmd);
				Runtime.getRuntime().exec(cmd);
			}
		} catch(java.io.IOException io) {
			Log.logException(Log.LEVEL_ERROR, MODULE, io);
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
		return isWin9x() || isWinNT() || isUnix();
	}
}
