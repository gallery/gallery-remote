package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class ProxyPanel extends PreferencePanel implements ActionListener, PreferenceNames {
	public static final String MODULE = "ProxyPa";


	JLabel icon = new JLabel(GRI18n.getString(MODULE, "icon"));
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JCheckBox jUseProxy = new JCheckBox();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JTextField jProxyHost = new JTextField();
	JTextField jProxyPort = new JTextField();
	JTextField jProxyUsername = new JTextField();
	JTextField jProxyPassword = new JTextField();
	JPanel jPanel1 = new JPanel();

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(PropertiesFile props) {
		jUseProxy.setSelected(props.getBooleanProperty(USE_PROXY));

		jProxyHost.setText(props.getProperty(PROXY_HOST));
		jProxyPort.setText(props.getProperty(PROXY_PORT));
		jProxyUsername.setText(props.getProperty(PROXY_USERNAME));
		jProxyPassword.setText(props.getBase64Property(PROXY_PASSWORD));

		resetUIState();
	}

	public void writeProperties(PropertiesFile props) {
		props.setBooleanProperty(USE_PROXY, jUseProxy.isSelected());

		props.setProperty(PROXY_HOST, jProxyHost.getText());
		props.setProperty(PROXY_PORT, jProxyPort.getText());
		props.setProperty(PROXY_USERNAME, jProxyUsername.getText());
		props.setBase64Property(PROXY_PASSWORD, jProxyPassword.getText());
	}

	public void resetUIState() {
		if (jUseProxy.isSelected()) {
			jProxyHost.setEnabled(true);
			jProxyPort.setEnabled(true);
			jProxyUsername.setEnabled(true);
			jProxyPassword.setEnabled(true);
			jProxyHost.setBackground(UIManager.getColor("TextField.background"));
			jProxyPort.setBackground(UIManager.getColor("TextField.background"));
			jProxyUsername.setBackground(UIManager.getColor("TextField.background"));
			jProxyPassword.setBackground(UIManager.getColor("TextField.background"));
		} else {
			jProxyHost.setEnabled(false);
			jProxyPort.setEnabled(false);
			jProxyUsername.setEnabled(false);
			jProxyPassword.setEnabled(false);
			jProxyHost.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			jProxyPort.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			jProxyUsername.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			jProxyPassword.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		jUseProxy.setText(GRI18n.getString(MODULE, "useProxy"));
		this.setLayout(gridBagLayout1);
		jLabel1.setText(GRI18n.getString(MODULE, "proxyURL"));
		jLabel2.setText(GRI18n.getString(MODULE, "proxyPort"));
		jLabel3.setText(GRI18n.getString(MODULE, "username"));
		jLabel4.setText(GRI18n.getString(MODULE, "passwd"));
		jProxyHost.setText("");
		jProxyPort.setText("");
		jProxyUsername.setText("");
		jProxyPassword.setText("");
		this.add(jUseProxy, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jLabel4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		this.add(jProxyHost, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jProxyPort, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jProxyUsername, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jProxyPassword, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(jPanel1, new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		jUseProxy.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		resetUIState();
	}
}

