/*

HTML Escaping code
Copyright (C) 2000 Marc Palmer / AnyWare Ltd.

Acording to Marc, this code may be used under any license.
Thanks to Jason Hunter for his great book "JAVA Servlet Programming" used as
the source for the entity mappings. Thanks to CARDIACS for making the
most joyful music (www.cardiacs.com/button.html)

http://www.anyware.co.uk/java for updates and other stuff

@author Marc Palmer
@author Marcel Huijkman

@version	15-07-2002
*/

package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.Log;

import java.util.HashMap;

/**
 * This class will escape characters that have an HTML entity
 * representation. It will not escape standard ASCII characters unless
 * it is necessary (i.e. will not escape ASCI #32 but will escape "&amp;").
 * Any characters with no corresponding entity are passed through unchanged.
 * It uses a quick string -> array mapping to avoid creating thousands of
 * temporary objects.
 */
public class HTMLEscaper {
	public static final String MODULE = "HTMLEsc";

	/**
	 * A little bit of unit testing
	 */
	public static void main(String[] args) {

		String text =
				"This is a test of the html escaper & let's hope it really, " +
				"really <B><I>works</I></B>! Is there an \u00f7 with an umlaut in the house?" +
				"This code is \u00a9copyleft. I like to be paid in \u00fa\u00fa\u00fas";

		System.out.println("NOTE: Due to differences in ASCII fonts, the text " +
				"sent to the escaper may not display properly. Hopefully you will be " +
				"able to tell what it should have looked like from the escaped output.");
		System.out.println("in: " + text);

		System.out.println("out: " + escape(text));

		System.out.println("rev: " + unescape(escape(text) + " & toto"));
	}

	/**
	 * This method will take the input and escape characters that have
	 * an HTML entity representation.
	 * It uses a quick string -> array mapping to avoid creating thousands of
	 * temporary objects.
	 * 
	 * @param nonHTMLsrc String containing the text to make HTML-safe
	 * @return String containing new copy of string with ENTITIES escaped
	 */
	public static final String escape(String nonHTMLsrc) {
		StringBuffer res = new StringBuffer();
		int l = nonHTMLsrc.length();
		int idx;
		char c;
		for (int i = 0; i < l; i++) {
			c = nonHTMLsrc.charAt(i);

			if (c > 255) {
				res.append("&#").append((int) c).append(";");
			} else {
				idx = entityMap.indexOf(c);
				if (idx == -1) {
					res.append(c);
				} else {
					res.append(quickEntities[idx]);
				}
			}
		}
		return res.toString();
	}

	public static final String unescape(String HTMLsrc) {
		if (HTMLsrc == null) {
			return null;
		}

		StringBuffer res = new StringBuffer();

		while (HTMLsrc.length() > 0) {
			int i = HTMLsrc.indexOf('&');

			if (i == -1) {
				res.append(HTMLsrc);
				break;
			}

			int j = HTMLsrc.indexOf(';', i);

			if (j == -1) {
				// this should never happen, but it does...
				res.append(HTMLsrc.substring(0, i)).append("&");
				HTMLsrc = HTMLsrc.substring(i + 1);
				continue;
			}

			String entity = HTMLsrc.substring(i + 1, j);
			String decodedEntity = (String) quickRevEntities.get(entity);

			if (decodedEntity == null) {
				if (entity.startsWith("#")) {
					res.append(HTMLsrc.substring(0, i));
					char c = (char) Integer.parseInt(entity.substring(1));
					//Log.log(Log.LEVEL_TRACE, MODULE, "c: " + c + " " + entity.substring(1));
					res.append(c);
					HTMLsrc = HTMLsrc.substring(j + 1);
				} else {
					res.append(HTMLsrc.substring(0, i)).append("&");
					HTMLsrc = HTMLsrc.substring(i + 1);
				}

				continue;
			}

			res.append(HTMLsrc.substring(0, i)).append(decodedEntity);

			HTMLsrc = HTMLsrc.substring(j + 1);
		}

		return res.toString();
	}

	/**
	 * These are probably HTML 3.2 level... as it looks like some HTML 4 entities
	 * are not present.
	 */
	private static final String[][] ENTITIES = {
		/* We probably don't want to filter regular ASCII chars so we leave them out */
		{"&", "amp"},
		{"<", "lt"},
		{">", "gt"},
		{"\"", "quot"},

		{"\u0083", "#131"},
		{"\u0084", "#132"},
		{"\u0085", "#133"},
		{"\u0086", "#134"},
		{"\u0087", "#135"},
		{"\u0089", "#137"},
		{"\u008A", "#138"},
		{"\u008B", "#139"},
		{"\u008C", "#140"},
		{"\u0091", "#145"},
		{"\u0092", "#146"},
		{"\u0093", "#147"},
		{"\u0094", "#148"},
		{"\u0095", "#149"},
		{"\u0096", "#150"},
		{"\u0097", "#151"},
		{"\u0099", "#153"},
		{"\u009A", "#154"},
		{"\u009B", "#155"},
		{"\u009C", "#156"},
		{"\u009F", "#159"},

		{"\u00A0", "nbsp"},
		{"\u00A1", "iexcl"},
		{"\u00A2", "cent"},
		{"\u00A3", "pound"},
		{"\u00A4", "curren"},
		{"\u00A5", "yen"},
		{"\u00A6", "brvbar"},
		{"\u00A7", "sect"},
		{"\u00A8", "uml"},
		{"\u00A9", "copy"},
		{"\u00AA", "ordf"},
		{"\u00AB", "laquo"},
		{"\u00AC", "not"},
		{"\u00AD", "shy"},
		{"\u00AE", "reg"},
		{"\u00AF", "macr"},
		{"\u00B0", "deg"},
		{"\u00B1", "plusmn"},
		{"\u00B2", "sup2"},
		{"\u00B3", "sup3"},

		{"\u00B4", "acute"},
		{"\u00B5", "micro"},
		{"\u00B6", "para"},
		{"\u00B7", "middot"},
		{"\u00B8", "cedil"},
		{"\u00B9", "sup1"},
		{"\u00BA", "ordm"},
		{"\u00BB", "raquo"},
		{"\u00BC", "frac14"},
		{"\u00BD", "frac12"},
		{"\u00BE", "frac34"},
		{"\u00BF", "iquest"},

		{"\u00C0", "Agrave"},
		{"\u00C1", "Aacute"},
		{"\u00C2", "Acirc"},
		{"\u00C3", "Atilde"},
		{"\u00C4", "Auml"},
		{"\u00C5", "Aring"},
		{"\u00C6", "AElig"},
		{"\u00C7", "Ccedil"},
		{"\u00C8", "Egrave"},
		{"\u00C9", "Eacute"},
		{"\u00CA", "Ecirc"},
		{"\u00CB", "Euml"},
		{"\u00CC", "Igrave"},
		{"\u00CD", "Iacute"},
		{"\u00CE", "Icirc"},
		{"\u00CF", "Iuml"},

		{"\u00D0", "ETH"},
		{"\u00D1", "Ntilde"},
		{"\u00D2", "Ograve"},
		{"\u00D3", "Oacute"},
		{"\u00D4", "Ocirc"},
		{"\u00D5", "Otilde"},
		{"\u00D6", "Ouml"},
		{"\u00D7", "times"},
		{"\u00D8", "Oslash"},
		{"\u00D9", "Ugrave"},
		{"\u00DA", "Uacute"},
		{"\u00DB", "Ucirc"},
		{"\u00DC", "Uuml"},
		{"\u00DD", "Yacute"},
		{"\u00DE", "THORN"},
		{"\u00DF", "szlig"},

		{"\u00E0", "agrave"},
		{"\u00E1", "aacute"},
		{"\u00E2", "acirc"},
		{"\u00E3", "atilde"},
		{"\u00E4", "auml"},
		{"\u00E5", "aring"},
		{"\u00E6", "aelig"},
		{"\u00E7", "ccedil"},
		{"\u00E8", "egrave"},
		{"\u00E9", "eacute"},
		{"\u00EA", "ecirc"},
		{"\u00EB", "euml"},
		{"\u00EC", "igrave"},
		{"\u00ED", "iacute"},
		{"\u00EE", "icirc"},
		{"\u00EF", "iuml"},

		{"\u00F0", "eth"},
		{"\u00F1", "ntilde"},
		{"\u00F2", "ograve"},
		{"\u00F3", "oacute"},
		{"\u00F4", "ocirc"},
		{"\u00F5", "otilde"},
		{"\u00F6", "ouml"},
		{"\u00F7", "divid"},
		{"\u00F8", "oslash"},
		{"\u00F9", "ugrave"},
		{"\u00FA", "uacute"},
		{"\u00FB", "ucirc"},
		{"\u00FC", "uuml"},
		{"\u00FD", "yacute"},
		{"\u00FE", "thorn"},
		{"\u00FF", "yuml"},
		{"\u0080", "euro"}
	};

	private static String entityMap;
	private static String[] quickEntities;
	private static HashMap quickRevEntities = new HashMap(ENTITIES.length);

	static {
		// Initialize some local mappings to speed it all up
		int l = ENTITIES.length;
		StringBuffer temp = new StringBuffer();

		quickEntities = new String[l];
		for (int i = 0; i < l; i++) {
			temp.append(ENTITIES[i][0]);
			quickEntities[i] = "&" + ENTITIES[i][1] + ";";
			quickRevEntities.put(ENTITIES[i][1], ENTITIES[i][0]);
		}
		entityMap = temp.toString();
	}
}
