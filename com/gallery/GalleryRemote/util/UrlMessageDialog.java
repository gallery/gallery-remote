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
public class UrlMessageDialog extends JDialog implements ActionListener {
	public static final String MODULE = "ImageUtils";

	JLabel jIcon = new JLabel();
	JLabel jMessage = new JLabel();
	BrowserLink jURL = new BrowserLink();
	JCheckBox jDontShow = new JCheckBox();
	JButton jButton1 = new JButton();
	JButton jButton2 = new JButton();
	JPanel jButtons = new JPanel(new GridLayout(1, 0, 5, 0));

	int buttonChosen = 0;

	public UrlMessageDialog(String message, String url, String urlText) {
		this(message, url, urlText, null, null);
	}

	public UrlMessageDialog(String message, String url, String urlText, String button1Label, String button2Label) {
		super(GalleryRemote._().getMainFrame(),
				GRI18n.getString(MODULE, "warningTitle"),
				true);

		jIcon.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		jMessage.setText(message);

		if (url != null) {
			if (urlText != null) {
				jURL.setText(urlText);
			} else {
				jURL.setText(url);
			}

			jURL.setUrl(url);
		}

		jDontShow.setText(GRI18n.getString(MODULE, "warningDontShow"));

		if (button1Label != null) {
			jButton1.setText(button1Label);
		} else {
			jButton1.setText(GRI18n.getString("Common", "OK"));
		}
		jButton1.addActionListener(this);

		if (button2Label != null) {
			jButton2.setText(button2Label);
			jButton2.addActionListener(this);
		}

		getContentPane().add(jIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 10), 0, 0));
		getContentPane().add(jMessage, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		if (url != null) {
			getContentPane().add(jURL, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		}
		getContentPane().add(jDontShow, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		getContentPane().add(jButtons, new GridBagConstraints(2, 2, 1, 2, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		jButtons.add(jButton1);
		if (button2Label != null) {
			jButtons.add(jButton2);
		}

		getRootPane().setDefaultButton(jButton1);

		pack();

		DialogUtil.center(this, getOwner());

		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jButton1) {
			buttonChosen = 1;
		} else {
			buttonChosen = 2;
		}

		setVisible(false);
	}

	public boolean dontShow() {
		return jDontShow.isSelected();
	}

	public int getButtonChosen() {
		return buttonChosen;
	}
}
