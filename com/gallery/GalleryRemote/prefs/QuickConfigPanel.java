package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.model.Gallery;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class QuickConfigPanel extends PreferencePanel implements ActionListener {
	public static final String MODULE = "QuickCPa";

	GalleryProperties newProps = null;

	JLabel icon = new JLabel("Quick Config");
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jLabel1 = new JLabel();
	JTextField jURL = new JTextField();
	JButton jSetup = new JButton();
	JLabel jLabel2 = new JLabel();

	public JLabel getIcon() {
		return icon;
	}

	public boolean isReversible() {
		return false;
	}

	public void readProperties(GalleryProperties props) {
	}

	public void writeProperties(GalleryProperties props) {
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		jLabel1.setText("URL");
		this.setLayout(gridBagLayout1);

		jSetup.setText("Setup");
		jLabel2.setText("<html>" +
				"This panel allows you to connect to a web server and fetch configuration options from it, for easy setup." +
				"Enter the URL of the setup file (your administrator needs to give it to you) and click <i>Setup</i>.<br>" +
				"<b>Caution</b>: this operation is not cancellable.<br>" +
				"<br>" +
				"Gallery administrators: to enable your users to use this functionality you merely need to put somewhere " +
				"on your web site a properties file that contains the settings you want to override and distribute that " +
				"URL to your users." +
				"</html>");
		jLabel2.setVerticalAlignment(SwingConstants.TOP);
		jLabel2.setPreferredSize(new Dimension(200, 100));
		this.add(jLabel1,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
		this.add(jURL,    new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jSetup,         new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		this.add(jLabel2,     new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));

		jSetup.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			InputStream content = (InputStream) new URL(jURL.getText()).getContent();
			GalleryProperties newProps = new GalleryProperties();
			newProps.load(content);

			StringBuffer overridden = new StringBuffer("<ul>");
			if (newProps != null) {
				GalleryProperties props = GalleryRemote.getInstance().properties;
				boolean newGallery = false;

				Iterator it = newProps.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					String value = newProps.getProperty(key);

					if (key.endsWith(".99")) {
						if (!newGallery) {
							// add Gallery access settings

							Gallery g = Gallery.readFromProperties(newProps, 99, mainFrame, false);
							if (g != null) {
								g.setPrefsIndex(mainFrame.galleries.getSize());
								mainFrame.galleries.addElement(g);
								newGallery = true;

								overridden.append("<li>").append(g.toString()).append("</li>");
							}
						}
					} else {
						props.setProperty(key, value);

						overridden.append("<li>").append(key).append("</li>");
					}
				}

				overridden.append("</ul>");

				JOptionPane.showMessageDialog(this, "<html>Loaded configuration file.<br>Overridden properties:" + overridden.toString() + "</html>", "Done", JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Empty configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Could not find configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
			Log.log(Log.ERROR, MODULE, "Fetching configuration failed");
			Log.logException( Log.ERROR, MODULE, ex );
		}
	}

	public void resetUIState() {
	}
}