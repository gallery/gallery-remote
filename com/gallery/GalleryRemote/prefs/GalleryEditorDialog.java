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
	JLabel jTypeLabel = new JLabel();
	JLabel jPnLoginUrlLabel = new JLabel();
	JLabel jPnGalleryUrlLabel = new JLabel();
	JLabel jPnHelpLabel = new JLabel();
	JLabel jStandaloneUrlLabel = new JLabel();
	JLabel jStandaloneHelpLabel = new JLabel();
	JLabel jPhpnLoginUrlLabel = new JLabel();
	JLabel jPhpnGalleryUrlLabel = new JLabel();
	JLabel jPhpnHelpLabel = new JLabel();
	JLabel jGlLoginUrlLabel = new JLabel();
	JLabel jGlGalleryUrlLabel = new JLabel();
	JLabel jGlHelpLabel = new JLabel();
	JLabel jAliasLabel = new JLabel();

	JComboBox jType = new JComboBox();
	JPanel jStylePanel = new JPanel();
	CardLayout jStyleLayout = new CardLayout();

	JTextField jUsername = new JTextField();
	JPasswordField jPassword = new JPasswordField();
	JTextField jAlias = new JTextField();

	JPanel jPostNuke = new JPanel();
	JTextField jPnLoginUrl = new JTextField();
	JTextField jPnGalleryUrl = new JTextField();

	JPanel jPHPNuke = new JPanel();
	JTextField jPhpnLoginUrl = new JTextField();
	JTextField jPhpnGalleryUrl = new JTextField();

	JPanel jGeekLog = new JPanel();
	JTextField jGlLoginUrl = new JTextField();
	JTextField jGlGalleryUrl = new JTextField();

	JPanel jStandalone = new JPanel();
	JTextField jStandaloneUrl = new JTextField();
	
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
		jTypeLabel.setText(GRI18n.getString(MODULE, "type"));
		jStylePanel.setLayout(jStyleLayout);

		jType.setToolTipText(GRI18n.getString(MODULE, "typeTip"));
		jType.setEditable(false);
		jType.setModel(new DefaultComboBoxModel(Gallery.types));

		jOk.setText(GRI18n.getString("Common", "OK"));
		jOk.setActionCommand("OK");
		jCancel.setText(GRI18n.getString("Common", "Cancel"));
		jCancel.setActionCommand("Cancel");
		jButtonPanel.setLayout(gridLayout1);
		gridLayout1.setHgap(5);

		jStandalone.setLayout(new GridBagLayout());
		jStandaloneUrlLabel.setText(GRI18n.getString(MODULE, "stndAln"));
		jStandaloneHelpLabel.setText(GRI18n.getString(MODULE, "stndAlnHlp"));
		jStandaloneHelpLabel.setVerticalAlignment(SwingConstants.TOP);
		jStandaloneHelpLabel.setVerticalTextPosition(SwingConstants.CENTER);

		jPostNuke.setLayout(new GridBagLayout());
		jPnLoginUrlLabel.setText(GRI18n.getString(MODULE, "pnLogin"));
		jPnLoginUrlLabel.setVerticalAlignment(SwingConstants.TOP);
		jPnLoginUrlLabel.setVerticalTextPosition(SwingConstants.CENTER);
		jPnGalleryUrlLabel.setText(GRI18n.getString(MODULE, "gllryUrl"));
		jPnHelpLabel.setPreferredSize(new Dimension(300, 80));
		jPnHelpLabel.setText(GRI18n.getString(MODULE, "pnHelp"));
		jPnHelpLabel.setVerticalAlignment(SwingConstants.TOP);

		jPHPNuke.setLayout(new GridBagLayout());
		jPhpnLoginUrlLabel.setText(GRI18n.getString(MODULE, "phpNukeLogin"));
		jPhpnLoginUrlLabel.setVerticalAlignment(SwingConstants.TOP);
		jPhpnLoginUrlLabel.setVerticalTextPosition(SwingConstants.CENTER);
		jPhpnGalleryUrlLabel.setText(GRI18n.getString(MODULE, "gllryUrl"));
		jPhpnHelpLabel.setPreferredSize(new Dimension(300, 80));
		jPhpnHelpLabel.setText(GRI18n.getString(MODULE, "phpNukeHelp"));
		jPhpnHelpLabel.setVerticalAlignment(SwingConstants.TOP);

		jGeekLog.setLayout(new GridBagLayout());
		jGlLoginUrlLabel.setText(GRI18n.getString(MODULE, "glLogin"));
		jGlLoginUrlLabel.setVerticalAlignment(SwingConstants.TOP);
		jGlLoginUrlLabel.setVerticalTextPosition(SwingConstants.CENTER);
		jGlGalleryUrlLabel.setText(GRI18n.getString(MODULE, "gllryUrl"));
		jGlHelpLabel.setPreferredSize(new Dimension(300, 80));
		jGlHelpLabel.setText(GRI18n.getString(MODULE, "glHelp"));
		jGlHelpLabel.setVerticalAlignment(SwingConstants.TOP);
		
		jAutoLogin.setText(GRI18n.getString(MODULE, "autoLogin"));

		this.getContentPane().add(jMainPanel, BorderLayout.CENTER);
		jMainPanel.add(jUsernameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jAliasLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jMainPanel.add(jPasswordLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jTypeLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jAlias, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
		jMainPanel.add(jUsername, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jPassword, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jAutoLogin, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jType, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jStylePanel, new GridBagConstraints(0, 5, 4, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jButtonPanel, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		jButtonPanel.add(jOk, null);
		jButtonPanel.add(jCancel, null);

		jStylePanel.add(jPostNuke, Gallery.types[Gallery.TYPE_POSTNUKE]);
		jPostNuke.add(jPnLoginUrlLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPostNuke.add(jPnLoginUrl, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPostNuke.add(jPnGalleryUrlLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPostNuke.add(jPnGalleryUrl, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPostNuke.add(jPnHelpLabel, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jStylePanel.add(jPHPNuke, Gallery.types[Gallery.TYPE_PHPNUKE]);
		jPHPNuke.add(jPhpnLoginUrlLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPHPNuke.add(jPhpnLoginUrl, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPHPNuke.add(jPhpnGalleryUrlLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPHPNuke.add(jPhpnGalleryUrl, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPHPNuke.add(jPhpnHelpLabel, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jStylePanel.add(jGeekLog, Gallery.types[Gallery.TYPE_GEEKLOG]);
		jGeekLog.add(jGlLoginUrlLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jGeekLog.add(jGlLoginUrl, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jGeekLog.add(jGlGalleryUrlLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jGeekLog.add(jGlGalleryUrl, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jGeekLog.add(jGlHelpLabel, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jStylePanel.add(jStandalone, Gallery.types[Gallery.TYPE_STANDALONE]);
		jStandalone.add(jStandaloneUrlLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		jStandalone.add(jStandaloneUrl, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jStandalone.add(jStandaloneHelpLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jType.addActionListener(this);
		jOk.addActionListener(this);
		jCancel.addActionListener(this);

		getRootPane().setDefaultButton(jOk);
	}

	public void resetUIState() {
		jUsername.setText(gallery.getUsername());
		jPassword.setText(gallery.getPassword());
		jAlias.setText(gallery.getAlias() == null?"":gallery.getAlias());

		jType.setSelectedIndex(gallery.getType());
		String panel = (String) jType.getSelectedItem();
		jStyleLayout.show(jStylePanel, panel);

		jStandaloneUrl.setText(gallery.getStUrlString());

		jPnGalleryUrl.setText(gallery.getPnGalleryUrlString());
		jPnLoginUrl.setText(gallery.getPnLoginUrlString());

		jPhpnGalleryUrl.setText(gallery.getPhpnGalleryUrlString());
		jPhpnLoginUrl.setText(gallery.getPhpnLoginUrlString());

		jGlGalleryUrl.setText(gallery.getGlGalleryUrlString());
		jGlLoginUrl.setText(gallery.getGlLoginUrlString());
		
		jAutoLogin.setSelected(gallery.isAutoLoadOnStartup());
	}

	public void readUIState() {
		gallery.setUsername(jUsername.getText());
		gallery.setPassword(jPassword.getText());
		gallery.setType(jType.getSelectedIndex());
		String alias = jAlias.getText().trim();
		gallery.setAlias(alias.length() == 0?null:alias);

		gallery.setStUrlString(jStandaloneUrl.getText());

		gallery.setPnLoginUrlString(jPnLoginUrl.getText());
		gallery.setPnGalleryUrlString(jPnGalleryUrl.getText());

		gallery.setPhpnLoginUrlString(jPhpnLoginUrl.getText());
		gallery.setPhpnGalleryUrlString(jPhpnGalleryUrl.getText());

		gallery.setGlLoginUrlString(jGlLoginUrl.getText());
		gallery.setGlGalleryUrlString(jGlGalleryUrl.getText());
		
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
		} else if (cmd.equals("comboBoxChanged")) {
			String panel = (String) jType.getSelectedItem();
			jStyleLayout.show(jStylePanel, panel);
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unknown command: " + cmd);
		}
	}

	public boolean isOK() {
		return isOK;
	}
}
