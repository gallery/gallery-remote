/**
 * User: iluvatar
 * Date: Sep 18, 2003
 * Time: 7:46:36 PM
 */
package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class GRI18n implements PreferenceNames {
    private static final String RESNAME = "com.gallery.GalleryRemote.resources.GRResources";
    private static GRI18n ourInstance;
    private Locale grLocale;
    private ResourceBundle grResBundle;

    public synchronized static GRI18n getInstance() {
        if (ourInstance == null) {
            ourInstance = new GRI18n();
        }
        return ourInstance;
    }

    private GRI18n() {
        String myLocale, myLang, myCountry;
        myLocale = GalleryRemote.getInstance().properties.getProperty(GR_LOCALE);

        if (myLocale == null) {
            grLocale = Locale.getDefault();
        } else {
            myLang = myLocale.substring(0, myLocale.indexOf("_"));
            myCountry = myLocale.substring(myLocale.indexOf("_")+1);
            grLocale = new Locale(myLang, myCountry);
        }

        setResBoundle();

    }

    public void setLocale(String language, String country) {
        grLocale = new Locale(language, country);
        setResBoundle();
    }

    public String getString(String key) {
        String msg;
        try {
            msg = grResBundle.getString(key);
        } catch (NullPointerException e) {
            System.err.println(e.toString());
            msg = "[NULLKEY]";
        } catch (MissingResourceException e) {
            System.err.println(e.toString());
            msg = "["+key+"]";
        }

        return msg;
    }


    private void setResBoundle() {
        try {
            grResBundle = ResourceBundle.getBundle(RESNAME, grLocale);
        } catch (MissingResourceException e) {
            System.err.println(e.toString());
        }
    }


}

