package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 27, 2003
 */
public class GalleryEditorDialog extends JDialog implements ActionListener {
	public static final String MODULE = "GEdiDlog";

	Gallery gallery;
	boolean isOK = false;

	JPanel jMainPanel = new JPanel();
	JLabel jUsernameLabel = new JLabel();
	JLabel jPasswordLabel = new JLabel();
	JLabel jUrlLabel = new JLabel();
	JLabel jUrlHelpLabel = new JLabel();
	JLabel jAliasLabel = new JLabel();
	JLabel jKeyLabel = new JLabel();

	JTextField jUsername = new JTextField();
	JPasswordField jPassword = new JPasswordField();
	JTextField jAlias = new JTextField();
	JTextField jKey = new JTextField();

	JTextField jUrl = new JTextField();
	
	JCheckBox jAutoLogin = new JCheckBox();

	JPanel jButtonPanel = new JPanel();
	JButton jOk = new JButton();
	JButton jCancel = new JButton();
	GridLayout gridLayout1 = new GridLayout();

	public GalleryEditorDialog(JDialog owner) {
		this(owner, null);
	}

	public GalleryEditorDialog(JDialog owner, Gallery gallery) {
		super(owner, true);
		this.gallery = gallery;

		jbInit();

		resetUIState();

		pack();
		DialogUtil.center(this, owner);

		setVisible(true);
	}

	private void jbInit() {
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setTitle(GRI18n.getString(MODULE, "title"));

		jMainPanel.setLayout(new GridBagLayout());
		jAliasLabel.setText(GRI18n.getString(MODULE, "alias"));
		jAlias.setToolTipText(GRI18n.getString(MODULE, "aliasTip"));
		jUsernameLabel.setText(GRI18n.getString(MODULE, "username"));
		jPasswordLabel.setText(GRI18n.getString(MODULE, "passwd"));

		jOk.setText(GRI18n.getString("Common", "OK"));
		jOk.setActionCommand("OK");
		jCancel.setText(GRI18n.getString("Common", "Cancel"));
		jCancel.setActionCommand("Cancel");
		jButtonPanel.setLayout(gridLayout1);
		gridLayout1.setHgap(5);

		jUrlLabel.setText(GRI18n.getString(MODULE, "stndAln"));
		/*jUrlHelpLabel.setText(GRI18n.getString(MODULE, "stndAlnHlp"));
		jUrlHelpLabel.setVerticalAlignment(SwingConstants.TOP);
		jUrlHelpLabel.setVerticalTextPosition(SwingConstants.CENTER);*/

		jAutoLogin.setText(GRI18n.getString(MODULE, "autoLogin"));

		this.getContentPane().add(jMainPanel, BorderLayout.CENTER);
		jMainPanel.add(jAliasLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jMainPanel.add(jUsernameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jPasswordLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jKeyLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jUrlLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		jMainPanel.add(jAlias, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
		jMainPanel.add(jUsername, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jPassword, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jKey, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jUrl, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
				, GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		/*jMainPanel.add(jUrlHelpLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));*/
		jMainPanel.add(jAutoLogin, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jButtonPanel, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		jButtonPanel.add(jOk, null);
		jButtonPanel.add(jCancel, null);

		jOk.addActionListener(this);
		jCancel.addActionListener(this);

		getRootPane().setDefaultButton(jOk);
	}

	public void resetUIState() {
		jUsername.setText(gallery.getUsername());
		jPassword.setText(gallery.getPassword());
		jAlias.setText(gallery.getAlias() == null?"":gallery.getAlias());
		jKey.setText(gallery.getKey() == null?"":gallery.getKey());

		jUrl.setText(gallery.getUrlString());


		jAutoLogin.setSelected(gallery.isAutoLoadOnStartup());
	}

	public void readUIState() {
		gallery.setUsername(jUsername.getText());
		gallery.setPassword(jPassword.getText());
		String alias = jAlias.getText().trim();
		gallery.setAlias(alias.length() == 0?null:alias);
		String key = jKey.getText().trim();
		gallery.setKey(key.length() == 0?null:key);

		gallery.setUrlString(jUrl.getText());

		gallery.setAutoLoadOnStartup(jAutoLogin.isSelected());
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + cmd);

		if (cmd.equals("OK")) {
			readUIState();
			isOK = true;
			setVisible(false);
		} else if (cmd.equals("Cancel")) {
			setVisible(false);
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unknown command: " + cmd);
		}
	}

	public boolean isOK() {
		return isOK;
	}
}
