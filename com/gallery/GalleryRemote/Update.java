/*
 *  Gallery Remote - a File Upload Utility for Gallery
 *
 *  Gallery - a web based photo album viewer and editor
 *  Copyright (C) 2000-2001 Bharat Mediratta
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.gallery.GalleryRemote;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.*;

import edu.stanford.ejalbert.BrowserLauncher;
import com.gallery.GalleryRemote.prefs.GalleryProperties;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.DialogUtil;

/**
 *  Update check and dialog
 *
 *@author     paour
 *@created    08 septembre 2002
 */

public class Update extends JFrame implements ActionListener, PreferenceNames {
	public static final String MODULE = "Update";


	public static final int NO_UPDATE = 0;
	public static final int RELEASE = 1;
	public static final int BETA = 2;
	
	Info release = null;
	Info beta = null;
	Info which = null;

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JScrollPane jScrollPane1 = new JScrollPane();

	JTextArea jVersion = new JTextArea();
	JTextArea jDate = new JTextArea();
	JTextPane jReleaseNotes = new JTextPane();
	JTextArea jUrl = new JTextArea();
	JButton jBrowse = new JButton();

	public int check(boolean showImmediate) {
		int result = 0;
		
		if ( GalleryRemote.getInstance().properties.getBooleanProperty(UPDATE_CHECK)) {
			release = new Info( GalleryRemote.getInstance().properties.getProperty(UPDATE_URL) );
			
			if (release.check()) {
				result = 1;
				which = release;
			}
		}

		if ( result == 0 && GalleryRemote.getInstance().properties.getBooleanProperty(UPDATE_CHECK_BETA)) {
			beta = new Info( GalleryRemote.getInstance().properties.getProperty(UPDATE_URL_BETA) );
			
			if (beta.check()) {
				result = 2;
				which = beta;
			}
		}
		
		if (showImmediate && which != null) {
			showNotice();
		}
		
		return result;
	}

	public void showNotice() {
		if (which == null) return;
		
		try {
			jbInit();

			pack();

			DialogUtil.center(this);

			setIconImage(MainFrame.iconImage);
			
			setVisible( true );
		} catch ( Exception e ) {
			Log.logException( Log.LEVEL_CRITICAL, MODULE, e );
		}
	}

	class Info {
		String version = null;
		Date releaseDate = null;
		String releaseNotes = null;
		String releaseUrl = null;
		String url = null;

		Info(String url) {
			this.url = url;
		}
		
		boolean check() {
			try {
				InputStream content = (InputStream) new URL(url).getContent();
				GalleryProperties props = new GalleryProperties();
				props.load(content);
	
				releaseDate = props.getDateProperty("releaseDate");
				version = props.getProperty("version");
				releaseNotes = props.getProperty("releaseNotes");
				releaseUrl = props.getProperty("releaseUrl");

				Date myReleaseDate = GalleryRemote.getInstance().properties.getDateProperty("releaseDate");
				
				Log.log(Log.LEVEL_TRACE, MODULE, "Local release date: " + myReleaseDate + " new: " + releaseDate);
				
				return releaseDate.after(myReleaseDate);
			} catch (Exception e) {
				Log.log(Log.LEVEL_CRITICAL, MODULE, "Update check failed");
				Log.logException( Log.LEVEL_ERROR, MODULE, e );
				
				return false;
			}
		}
	}
	
	private void jbInit() throws Exception {
		this.setTitle(GRI18n.getString(MODULE, "title"));
		this.getContentPane().setLayout(gridBagLayout1);

		jLabel1.setFont(new java.awt.Font("Dialog", 1, 16));
		jLabel1.setText(GRI18n.getString(MODULE, "newVerAvail"));
		jLabel2.setText(GRI18n.getString(MODULE, "ver"));
		jLabel3.setText(GRI18n.getString(MODULE, "relDate"));
		jLabel4.setText(GRI18n.getString(MODULE, "relNotes"));
		jLabel5.setText(GRI18n.getString(MODULE, "dnURL"));

		jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		jDate.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jDate.setEditable(false);
		jDate.setFont(new java.awt.Font("SansSerif", 0, 11));
		if (which.releaseDate != null) jDate.setText(DateFormat.getDateInstance().format(which.releaseDate));

		jBrowse.setText(GRI18n.getString(MODULE, "openInBrwsr"));
		jBrowse.addActionListener(this);

		jReleaseNotes.setEditable(false);
		jReleaseNotes.setFont(new java.awt.Font("SansSerif", 0, 11));
		jReleaseNotes.setPreferredSize(new Dimension(520, 250));
		jReleaseNotes.setMargin(new Insets(0, 3, 3, 3));
		if (which.releaseNotes != null) jReleaseNotes.setText(which.releaseNotes);

		jUrl.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jUrl.setEditable(false);
		jUrl.setFont(new java.awt.Font("SansSerif", 0, 11));
		jUrl.setForeground(Color.blue);
		if (which.releaseUrl != null) jUrl.setText(which.releaseUrl);

		jVersion.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		jVersion.setEditable(false);
		jVersion.setFont(new java.awt.Font("SansSerif", 0, 11));
		if (which.version != null) jVersion.setText(which.version);

		this.getContentPane().add(jLabel1,       new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 15, 0), 0, 0));
		this.getContentPane().add(jLabel2,       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 5, 0));
		this.getContentPane().add(jLabel3,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 5, 0));
		this.getContentPane().add(jLabel4,      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 5, 0));
		this.getContentPane().add(jLabel5,    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 5, 0));

		this.getContentPane().add(jScrollPane1,      new GridBagConstraints(1, 3, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 2));

		this.getContentPane().add(jVersion,          new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 2));
		this.getContentPane().add(jDate,           new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 2));
		this.getContentPane().add(jUrl,   new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.getContentPane().add(jBrowse,  new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 5, 5, 5), 0, 0));

		jScrollPane1.getViewport().add(jReleaseNotes, null);
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			BrowserLauncher.openURL(which.releaseUrl);
		} catch (Exception e) {
			Log.log(Log.LEVEL_CRITICAL, MODULE, "Exception while trying to open browser");
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
		}
	}

	public static boolean upgrade() {
		PropertiesFile lax = new PropertiesFile("Gallery Remote.lax");

		try {
			lax.read();
		} catch (FileNotFoundException e) {
			// no LAX, no problem...
			Log.log(Log.LEVEL_INFO, MODULE, "LAX file not found: probably running from Ant or IDE...");
			return false;
		}

		String laxClasspath = lax.getProperty("lax.class.path");
		String classpath = GalleryRemote.getInstance().properties.getProperty("classpath");

		if (classpath != null && ! classpath.equals(laxClasspath)) {
			Log.log(Log.LEVEL_INFO, MODULE, "Patching LAX classpath");

			try {
				File laxFile = new File("Gallery Remote.lax");
				File patchFile = new File("Gallery Remote.lax.patch");
				File origFile = new File("Gallery Remote.lax.orig");

				BufferedReader in = new BufferedReader(new FileReader(laxFile));
				BufferedWriter out = new BufferedWriter(new FileWriter(patchFile));

				String line = null;

				String cr = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					if (line.startsWith("lax.class.path=")) {
						out.write("lax.class.path=" + classpath + cr);
					} else {
						out.write(line + cr);
					}
				}

				in.close();
				out.close();

				origFile.delete();
				laxFile.renameTo(origFile);
				patchFile.renameTo(laxFile);

				JOptionPane.showMessageDialog(null, GRI18n.getString(MODULE, "restart.text"), GRI18n.getString(MODULE, "restart.title"), JOptionPane.WARNING_MESSAGE);

				return true;
			} catch (FileNotFoundException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} catch (IOException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}

			return false;
		} else {
			Log.log(Log.LEVEL_INFO, MODULE, "LAX classpath up to date");
			return false;
		}
	}
}
