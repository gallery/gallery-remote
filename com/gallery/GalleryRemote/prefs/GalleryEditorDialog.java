package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.model.Gallery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
	JLabel jPnGalleryUrlLabel = new JLabel();
	JLabel jUsernameLabel = new JLabel();
	JLabel jPasswordLabel = new JLabel();
	JLabel jTypeLabel = new JLabel();
	JLabel jPnLoginUrlLabel = new JLabel();
	JLabel jStandaloneUrlLabel = new JLabel();
	JLabel jPnHelpLabel = new JLabel();
	JLabel jStandaloneHelpLabel = new JLabel();
	JLabel jPhpnLoginUrlLabel = new JLabel();
	JLabel jPhpnGalleryUrlLabel = new JLabel();
	JLabel jPhpnHelpLabel = new JLabel();

	JComboBox jType = new JComboBox();
	JPanel jStylePanel = new JPanel();
	CardLayout jStyleLayout = new CardLayout();

	JTextField jUsername = new JTextField();
	JPasswordField jPassword = new JPasswordField();

	JPanel jPostNuke = new JPanel();
	JTextField jPnLoginUrl = new JTextField();
	JTextField jPnGalleryUrl = new JTextField();

	JPanel jPHPNuke = new JPanel();
	JTextField jPhpnLoginUrl = new JTextField();
	JTextField jPhpnGalleryUrl = new JTextField();

	JPanel jStandalone = new JPanel();
	JTextField jStandaloneUrl = new JTextField();

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

		setVisible( true );
	}

	private void jbInit() {
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setTitle("Gallery Details");

		jMainPanel.setLayout(new GridBagLayout());
		jUsernameLabel.setText("Username");
		jPasswordLabel.setText("Password");
		jTypeLabel.setText("Gallery Type");
		jStylePanel.setLayout(jStyleLayout);

		jType.setToolTipText("Use Standalone when your Gallery is not embedded inside a Content " +
				"Management System (the default). If it is embedded, pick the CMS " +
				"it\'s wrapped in.");
		jType.setEditable(false);
		jType.setModel(new DefaultComboBoxModel(Gallery.types));

		jOk.setText("OK");
		jCancel.setText("Cancel");
		jButtonPanel.setLayout(gridLayout1);
		gridLayout1.setHgap(5);

		jStandalone.setLayout(new GridBagLayout());
		jStandaloneUrlLabel.setText("Gallery URL");
		jStandaloneHelpLabel.setText("<HTML>The Gallery URL is the URL users would use to connect to your " +
				"Gallery.</HTML>");
		jStandaloneHelpLabel.setVerticalAlignment(SwingConstants.TOP);
		jStandaloneHelpLabel.setVerticalTextPosition(SwingConstants.CENTER);

		jPostNuke.setLayout(new GridBagLayout());
		jPnLoginUrlLabel.setText("PostNuke Login URL");
		jPnLoginUrlLabel.setVerticalAlignment(SwingConstants.TOP);
		jPnLoginUrlLabel.setVerticalTextPosition(SwingConstants.CENTER);
		jPnGalleryUrlLabel.setText("Gallery Module URL");
		jPnHelpLabel.setPreferredSize(new Dimension(300, 80));
		jPnHelpLabel.setText("<HTML>Where <b>$USERNAME$</b> and <b>$PASSWORD$</b> will be replaced when Gallery " +
				"Remote tries to log in by the username and password you enter above " +
				"and <b>$GALLERYFILE$</b> is replaced with the PHP file Gallery Remote uses " +
				"to communicate with Gallery (usually <i>gallery_remote2.php</i>).<BR>" +
				"The <b>name</b> parameter is the PostNuke module name of your gallery; set it " +
				"accordingly.<BR>" +
				"This functionality is only available with Gallery 1.3.5 (beta 10) and later.</HTML>");
		jPnHelpLabel.setVerticalAlignment(SwingConstants.TOP);

		jPHPNuke.setLayout(new GridBagLayout());
		jPhpnLoginUrlLabel.setText("PHPNuke Login URL");
		jPhpnLoginUrlLabel.setVerticalAlignment(SwingConstants.TOP);
		jPhpnLoginUrlLabel.setVerticalTextPosition(SwingConstants.CENTER);
		jPhpnGalleryUrlLabel.setText("Gallery Module URL");
		jPhpnHelpLabel.setPreferredSize(new Dimension(300, 80));
		jPhpnHelpLabel.setText("<HTML>Where <b>$USERNAME$</b> and <b>$PASSWORD$</b> will be replaced when Gallery " +
				"Remote tries to log in by the username and password you enter above " +
				"and <b>$GALLERYFILE$</b> is replaced with the PHP file Gallery Remote uses " +
				"to communicate with Gallery (usually <i>gallery_remote2.php</i>).<BR>" +
				"The <b>name</b> parameter is the PHPNuke module name of your gallery; set it " +
				"accordingly.<BR>" +
				"This functionality is only available with Gallery 1.3.5 (beta 10) and later.</HTML>");
		jPhpnHelpLabel.setVerticalAlignment(SwingConstants.TOP);

		this.getContentPane().add(jMainPanel, BorderLayout.CENTER);
		jMainPanel.add(jStylePanel,          new GridBagConstraints(0, 3, 4, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jUsernameLabel,         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jMainPanel.add(jPasswordLabel,         new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jTypeLabel,         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jMainPanel.add(jUsername,            new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
		jMainPanel.add(jPassword,          new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jType,         new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jMainPanel.add(jButtonPanel,             new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		jButtonPanel.add(jOk, null);
		jButtonPanel.add(jCancel, null);

		jStylePanel.add(jPostNuke, Gallery.types[Gallery.TYPE_POSTNUKE]);
		jPostNuke.add(jPnLoginUrlLabel,        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPostNuke.add(jPnLoginUrl,           new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPostNuke.add(jPnGalleryUrlLabel,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPostNuke.add(jPnGalleryUrl,      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPostNuke.add(jPnHelpLabel,   new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jStylePanel.add(jPHPNuke, Gallery.types[Gallery.TYPE_PHPNUKE]);
		jPHPNuke.add(jPhpnLoginUrlLabel,        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPHPNuke.add(jPhpnLoginUrl,           new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPHPNuke.add(jPhpnGalleryUrlLabel,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPHPNuke.add(jPhpnGalleryUrl,      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPHPNuke.add(jPhpnHelpLabel,   new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jStylePanel.add(jStandalone, Gallery.types[Gallery.TYPE_STANDALONE]);
		jStandalone.add(jStandaloneUrlLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		jStandalone.add(jStandaloneUrl,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jStandalone.add(jStandaloneHelpLabel,    new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));

		jType.addActionListener(this);
		jOk.addActionListener(this);
		jCancel.addActionListener(this);
	}

	public void resetUIState() {
		jUsername.setText(gallery.getUsername());
		jPassword.setText(gallery.getPassword());

		jType.setSelectedIndex(gallery.getType());
		String panel = (String) jType.getSelectedItem();
		jStyleLayout.show(jStylePanel, panel);

		jStandaloneUrl.setText(gallery.getStUrlString());

		jPnGalleryUrl.setText(gallery.getPnGalleryUrlString());
		jPnLoginUrl.setText(gallery.getPnLoginUrlString());

		jPhpnGalleryUrl.setText(gallery.getPhpnGalleryUrlString());
		jPhpnLoginUrl.setText(gallery.getPhpnLoginUrlString());
	}

	public void readUIState() {
		gallery.setUsername(jUsername.getText());
		gallery.setPassword(jPassword.getText());
		gallery.setType(jType.getSelectedIndex());

		gallery.setStUrlString(jStandaloneUrl.getText());

		gallery.setPnLoginUrlString(jPnLoginUrl.getText());
		gallery.setPnGalleryUrlString(jPnGalleryUrl.getText());

		gallery.setPhpnLoginUrlString(jPhpnLoginUrl.getText());
		gallery.setPhpnGalleryUrlString(jPhpnGalleryUrl.getText());
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Log.log(Log.INFO, MODULE, "Command selected " + cmd);

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
			Log.log(Log.ERROR, MODULE, "Unknown command: " + cmd);
		}
	}

	public boolean isOK() {
		return isOK;
	}
}
