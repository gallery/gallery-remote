/**
 * User: iluvatar
 * Date: Sep 18, 2003
 * Time: 7:46:36 PM
 */
package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.io.*;
import java.text.MessageFormat;

public class GRI18n implements PreferenceNames {
    private static final String RESNAME = "com.gallery.GalleryRemote.resources.GRResources";
    private static final String MODULE = "MainFrame";

    private static GRI18n ourInstance;
    private Locale grLocale;
    private ResourceBundle grResBundle;
    private MessageFormat grMsgFrmt;

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

        grMsgFrmt = new MessageFormat("");
        setResBoundle();

    }

    public void setLocale(String language, String country) {
        grLocale = new Locale(language, country);
        setResBoundle();
    }

    public String getString(String key) {
        return getString(getCallerName(), key);
    }

    public String getString(Object caller, String key) {
        return getString(caller.getClass().getName(), key);
    }

    public String getString(String key, Object [] params) {
        return getString(getCallerName(), key, params);
    }

    public String getString(Object caller, String key, Object [] params) {
        return getString(caller.getClass().getName(), key, params);
    }

    private String getString(String className, String key) {
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
            msg = "["+extKey+"]";
        }

        return msg;

    }

    private String getString(String className, String key, Object [] params) {
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
             msg = "["+extKey+"]";
         }

         return msg;
    }

    private void setResBoundle() {
        try {
            grResBundle = ResourceBundle.getBundle(RESNAME, grLocale);
        } catch (MissingResourceException e) {
            Log.log(Log.ERROR, MODULE, "Resource bundle error");
            Log.logException(Log.ERROR, MODULE, e);
        }

        grMsgFrmt.setLocale(grLocale);

    }

    private static String getCallerName() {


        StringWriter sw = new StringWriter();
        new Throwable().printStackTrace(new PrintWriter(sw));
        BufferedReader sr = new BufferedReader(new StringReader(sw.getBuffer().toString()));

        String returnVal = "";

        try {
          int lineCount = 0;
          String line;

          while( (line = sr.readLine()) != null ) {
            if(lineCount == 3) { // stack depth  1 --> getCallerName()
                                 //              2     getCallerName's caller
                                 //              3     actual caller we're interested in
              int start = line.indexOf("at ") + 3,
                  end   = line.indexOf("(");
                  String [] returnVals;
                String retTemp = line.substring(start, end);
                returnVals = retTemp.split("\\.");
                returnVal = returnVals[returnVals.length - 2];

            }
            lineCount++;
          }
        }
        catch(IOException e) {
            Log.log(Log.ERROR, MODULE, "Error reading the call stack...");
            Log.logException(Log.ERROR, MODULE, e);
        }

        int initBegin = returnVal.indexOf(".<init>");      // trim off ".<init>" for initializers
        if (initBegin != -1)
          returnVal = returnVal.substring(0, initBegin);

        int dollarSignBegin = returnVal.indexOf("$");
        if (dollarSignBegin != -1)
            returnVal = returnVal.substring(0, dollarSignBegin);

        return returnVal;
  }

}

