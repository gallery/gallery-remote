/**
 * User: iluvatar
 * Date: Sep 18, 2003
 * Time: 7:46:36 PM
 */
package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.text.MessageFormat;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;


public class GRI18n implements PreferenceNames {
	private static final String RESNAME =
			"com.gallery.GalleryRemote.resources.GRResources";
	private static final String RESNAME_DEV =
			"GRResources";
	private static final String RESPATH =
			"com/gallery/GalleryRemote/resources/GRResources";
	private static final String MODULE = "GRI18n";

	private static Locale grLocale;
	private static ResourceBundle grResBundle;
	private static HashMap formats = new HashMap();

	private static List lAvailLoc = null;

	private static boolean devMode = false;
	private static Properties devResProperties = null;

	static {
		String myLocale = GalleryRemote._().properties.getProperty(UI_LOCALE);
		devMode = GalleryRemote._().properties.getBooleanProperty(UI_LOCALE_DEV);

		grLocale = parseLocaleString(myLocale);

		Log.log(Log.LEVEL_INFO, MODULE, grLocale.toString());

		setResBundle();
	}

	public static Locale parseLocaleString(String localeString) {
		if (localeString == null) {
			return Locale.getDefault();
		} else {
			int i = localeString.indexOf("_");

			if (i != -1) {
				return new Locale(localeString.substring(0, i), localeString.substring(i + 1));
			} else {
				return new Locale(localeString, "");
			}
		}
	}


	public static void setLocale(String language, String country) {
		grLocale = new Locale(language, country);
		setResBundle();
	}


	public static String getString(String className, String key) {
		String msg;
		String extKey = className + "." + key;
		try {
			msg = grResBundle.getString(extKey);

			if (devResProperties != null && devResProperties.getProperty(extKey) == null) {
				if (msg.startsWith("<html>")) {
					msg = "<html>***" + msg.substring(6);
				} else {
					msg = "***" + msg;
				}
			}
		} catch (NullPointerException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Key null error");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			msg = "[NULLKEY]";
		} catch (MissingResourceException e) {
			Log.log(Log.LEVEL_INFO, MODULE, "Key [" + extKey + "] not defined");
			Log.logException(Log.LEVEL_INFO, MODULE, e);
			msg = "[" + extKey + "]";
		}

		return msg;
	}


	public static String getString(String className, String key, Object[] params) {
		String msg;
		String extKey = className + "." + key;
		try {
			MessageFormat format = (MessageFormat) formats.get(extKey);
			if (format == null) {
				format = new MessageFormat(fixQuotes(grResBundle.getString(extKey)), grLocale);
				formats.put(extKey, format);
			}
			msg = format.format(params);

			if (devResProperties != null && devResProperties.getProperty(extKey) == null) {
				if (msg.startsWith("<html>")) {
					msg = "<html>***" + msg.substring(6);
				} else {
					msg = "***" + msg;
				}
			}
		} catch (NullPointerException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Key null error");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			msg = "[NULLKEY]";
		} catch (MissingResourceException e) {
			Log.log(Log.LEVEL_INFO, MODULE, "Key [" + extKey + "] not defined");
			Log.logException(Log.LEVEL_INFO, MODULE, e);
			msg = "[" + extKey + "]";
		}

		return msg;
	}

	public static String fixQuotes(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\'') {
				sb.append("''");
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static Locale getCurrentLocale() {
		return grLocale;
	}


	private static void setResBundle() {
		try {
			grResBundle = ResourceBundle.getBundle(devMode?RESNAME_DEV:RESNAME, grLocale);

			if (devMode) {
				devResProperties = getLocaleProperties(grLocale);
			}
		} catch (MissingResourceException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Resource bundle error");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}

	public static Properties getLocaleProperties(Locale locale) {
		Properties p = new Properties();
		String filename;
		if (locale == null) {
			filename = (devMode?RESNAME_DEV:RESPATH) + ".properties";
		} else {
			filename = (devMode?RESNAME_DEV:RESPATH) + "_" + locale.toString() + ".properties";
		}
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);
		if (is != null) {
			try {
				p.load(is);
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "No file for " + filename);
		}

		return p;
	}


	public static List getAvailableLocales() {
		if (lAvailLoc == null)
			lAvailLoc = initAvailableLocales();
		return lAvailLoc;
	}

	private static List initAvailableLocales() {
		String locPath;
		String loc;
		List aList = new LinkedList();
		long start = System.currentTimeMillis();

		// todo: it seems that the dialog can't be displayed because all this
		// is running in the Swing event thread. Need to find a better way...
//		JDialog dialog = new JDialog(GalleryRemote.getInstance().mainFrame, "Please wait...");
//		dialog.getContentPane().add("Center", new JLabel("<HTML>Parsing list of locales for this platform." +
//				"<br>This can take between 1 and 10 seconds..."));
//		dialog.pack();
//		DialogUtil.center(dialog);
//		dialog.setVisible(true);
//		Thread.yield();

		Log.log(Log.LEVEL_TRACE, MODULE, "Getting the list of locales");

		// this call is apparently very slow...
		Locale[] list = Locale.getAvailableLocales();

		Log.log(Log.LEVEL_TRACE, MODULE, "The platform supports " + list.length + " locales. Pruning...");

		String prefix = "##DUMMY";
		for (int i = 0; i < list.length; i++) {
			loc = list[i].toString();

			// perf optimization: don't go through all the regions if the main language was not found
			if (!loc.startsWith(prefix)) {
				prefix = loc;
				if (devMode) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Trying locale: " + loc);
				}

				locPath = (devMode?RESNAME_DEV:RESPATH) + "_" + loc + ".properties";
				if (ClassLoader.getSystemClassLoader().getResource(locPath) != null) {
					Log.log(Log.LEVEL_INFO, MODULE, "Found locale: " + loc);
					aList.add(list[i]);
					prefix = "##DUMMY";
				}
			}
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Pruned locales in " + (System.currentTimeMillis() - start) + "ms");
		//dialog.setVisible(false);

		return aList;
	}

	/*class PatienceDialog extends JDialog implements Runnable {
		public boolean done = false;

		public void run() {
			try {
				Thread.sleep(1000);


			} catch (InterruptedException e) {}
		}
	}*/
}
