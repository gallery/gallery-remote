package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.GalleryProperties;
import com.gallery.GalleryRemote.Log;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class ProxyPanel extends PreferencePanel {
	public static final String MODULE = "ProxyPa";

	JLabel icon = new JLabel("Proxy");
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JCheckBox jProxy = new JCheckBox();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JTextField jURL = new JTextField();
	JTextField jTextField2 = new JTextField();
	JTextField jUsername = new JTextField();
	JTextField jPassword = new JTextField();
	JPanel jPanel1 = new JPanel();

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(GalleryProperties props) {
	}

	public void writeProperties(GalleryProperties props) {
	}

	public void buildUI() {
		jbInit();
	}
	
	private void jbInit() {
		jProxy.setText("Use proxy");
		this.setLayout(gridBagLayout1);
		jLabel1.setText("Proxy URL");
		jLabel2.setText("Proxy port");
		jLabel3.setText("Username");
		jLabel4.setText("Password");
		jURL.setText("");
		jTextField2.setText("");
		jUsername.setText("");
		jPassword.setText("");
		this.add(jProxy,   new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel1,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jLabel2,     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jLabel3,     new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jLabel4,      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jURL,    new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jTextField2,    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jUsername,    new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jPassword,    new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jPanel1,   new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}
}

