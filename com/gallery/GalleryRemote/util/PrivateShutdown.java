package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.util.OsShutdown;

/**
 * Shut down the OS. Idea by Nick77.
 * OS names from <http://www.tolstoy.com/samizdat/sysprops.html>
 * User: paour
 * Date: Sep 25, 2003
 */
public class PrivateShutdown {
	public static final String MODULE = "Shutdown";

	public static void shutdown() {
		String win9x = "rundll32 user,exitwindows";
		String winNT = "shutdown -s -f -t 01";
		String unix = "shutdown -fh now";

		String cmd = null;

		if (OsShutdown.isWin9x()) {
			cmd = win9x;
		} else if (OsShutdown.isWinNT()) {
			cmd = winNT;
		} else if (OsShutdown.isUnix()) {
			cmd = unix;
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Platform not recognized; shutdown will not be performed");
		}

		exec(cmd);
	}

	public static void exec(final String cmd) {
		try {
			if (cmd != null) {
				Log.log(Log.LEVEL_TRACE, MODULE, "Executing " + cmd);
				Runtime.getRuntime().exec(cmd);
			}
		} catch (java.io.IOException io) {
			Log.logException(Log.LEVEL_ERROR, MODULE, io);
		}
	}
}