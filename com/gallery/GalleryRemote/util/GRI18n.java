/**
 * User: iluvatar
 * Date: Sep 18, 2003
 * Time: 7:46:36 PM
 */
package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.text.Format;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class GRI18n implements PreferenceNames {
    private static final String RESNAME =
            "com.gallery.GalleryRemote.resources.GRResources";
    private static final String MODULE = "MainFrame";

    private static GRI18n ourInstance;

    private Locale grLocale;
    private ResourceBundle grResBundle;
    private MessageFormat grMsgFrmt;

    public static GRI18n getInstance() {
        if (ourInstance == null) {
            ourInstance = new GRI18n();
        }
        return ourInstance;
    }

    private GRI18n() {
        String myLocale, myLang, myCountry;
        myLocale =
                GalleryRemote.getInstance().properties.getProperty(GR_LOCALE);

        if (myLocale == null) {
            grLocale = Locale.getDefault();
        } else {
            myLang = myLocale.substring(0, myLocale.indexOf("_"));
            myCountry = myLocale.substring(myLocale.indexOf("_") + 1);
            grLocale = new Locale(myLang, myCountry);
        }

        grMsgFrmt = new MessageFormat("");
        setResBundle();

    }

    public void setLocale(String language, String country) {
        grLocale = new Locale(language, country);
        setResBundle();
    }


    public String getString(String className, String key) {
        String msg;
        String extKey = className + "." + key;
        try {
            msg = grResBundle.getString(extKey);
        } catch (NullPointerException e) {
            Log.log(Log.ERROR, MODULE, "Key null error");
            Log.logException(Log.ERROR, MODULE, e);
            msg = "[NULLKEY]";
        } catch (MissingResourceException e) {
            Log.log(Log.INFO, MODULE, "Key [" + extKey + "] not defined");
            Log.logException(Log.INFO, MODULE, e);
            msg = "[" + extKey + "]";
        }

        return msg;

    }

    public String getString(String className, String key, Object[] params) {
        String template, msg;
        String extKey = className + "." + key;
        try {
            template = grResBundle.getString(extKey);
            grMsgFrmt.applyPattern(template);
            msg = grMsgFrmt.format(params);
        } catch (NullPointerException e) {
            Log.log(Log.ERROR, MODULE, "Key null error");
            Log.logException(Log.ERROR, MODULE, e);
            msg = "[NULLKEY]";
        } catch (MissingResourceException e) {
            Log.log(Log.INFO, MODULE, "Key [" + extKey + "] not defined");
            Log.logException(Log.INFO, MODULE, e);
            msg = "[" + extKey + "]";
        }

        return msg;
    }


    private void setResBundle() {
        try {
            grResBundle = ResourceBundle.getBundle(RESNAME, grLocale);
        } catch (MissingResourceException e) {
            Log.log(Log.ERROR, MODULE, "Resource bundle error");
            Log.logException(Log.ERROR, MODULE, e);
        }

        grMsgFrmt.setLocale(grLocale);

    }


}
