package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.GalleryRemote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jan 13, 2004
 */
public class UrlMessageDialog extends JDialog {
	public static final String MODULE = "ImageUtils";
	JLabel jIcon = new JLabel();
	JLabel jMessage = new JLabel();
	BrowserLink jURL = new BrowserLink();
	JCheckBox jDontShow = new JCheckBox();
	JButton jOk = new JButton();

	public UrlMessageDialog(String message, String url, String urlText) {
		super(GalleryRemote._().getMainFrame(),
				GRI18n.getString(MODULE, "warningTitle"),
				true);

		jIcon.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		jMessage.setText(message);

		if (urlText != null) {
			jURL.setText(urlText);
		} else {
			jURL.setText(url);
		}
		
		jURL.setUrl(url);
		jDontShow.setText(GRI18n.getString(MODULE, "warningDontShow"));
		jOk.setText(GRI18n.getString("Common", "OK"));
		jOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		getContentPane().add(jIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 10), 0, 0));
		getContentPane().add(jMessage, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		getContentPane().add(jURL, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		getContentPane().add(jDontShow, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		getContentPane().add(jOk, new GridBagConstraints(2, 2, 1, 2, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));

		getRootPane().setDefaultButton(jOk);

		pack();

		DialogUtil.center(this, getOwner());

		setVisible(true);
	}

	public boolean dontShow() {
		return jDontShow.isSelected();
	}
}
