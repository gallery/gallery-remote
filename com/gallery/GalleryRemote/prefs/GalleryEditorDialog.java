package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.model.Gallery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 27, 2003
 */
public class GalleryEditorDialog extends JDialog implements ActionListener {
	public static final String MODULE = "GEdiDlog";

	Gallery gallery;
	boolean isOK = false;

	JPanel jPanel1 = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JPanel jStylePanel = new JPanel();
	CardLayout jStyleLayout = new CardLayout();
	JTextField jPnGalleryUrl = new JTextField();
	JTextField jUsername = new JTextField();
	JPasswordField jPassword = new JPasswordField();
	JComboBox jType = new JComboBox();
	JPanel jPostNuke = new JPanel();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	JLabel jLabel5 = new JLabel();
	JTextField jPnLoginUrl = new JTextField();
	JPanel jStandalone = new JPanel();
	JLabel jLabel6 = new JLabel();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	JTextField jStandaloneUrl = new JTextField();
	JPanel jPanel2 = new JPanel();
	JButton jOk = new JButton();
	JButton jCancel = new JButton();
	GridLayout gridLayout1 = new GridLayout();
	JLabel jLabel7 = new JLabel();
	JLabel jLabel8 = new JLabel();

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

		jPanel1.setLayout(gridBagLayout1);
		jLabel1.setText("Gallery Module URL");
		jLabel2.setText("Username");
		jLabel3.setText("Password");
		jLabel4.setText("Gallery Type");
		jStylePanel.setLayout(jStyleLayout);
		jPostNuke.setLayout(gridBagLayout2);
		jLabel5.setText("PostNuke Login URL");
		jStandalone.setLayout(gridBagLayout3);
		jLabel6.setText("Gallery URL");

		jType.setToolTipText("Use Standalone when your Gallery is not embedded inside a Content " +
				"Management System (the default). If it is embedded, pick the CMS " +
				"it\'s wrapped in.");
		jType.setEditable(false);
		jType.setModel(new DefaultComboBoxModel(Gallery.types));

		jOk.setText("OK");
		jCancel.setText("Cancel");
		jPanel2.setLayout(gridLayout1);
		gridLayout1.setHgap(5);
		jLabel7.setPreferredSize(new Dimension(300, 80));
		jLabel7.setText("<HTML>Where <b>$USERNAME$</b> and <b>$PASSWORD$</b> will be replaced when Gallery " +
				"Remote tries to log in by the username and password you enter above " +
				"and <b>$GALLERYFILE$</b> is replaced with the PHP file Gallery Remote uses " +
				"to communicate with Gallery (usually <i>gallery_remote2.php</i>).<BR>" +
				"The <b>name</b> parameter is the PostNuke module name of your gallery; set it " +
				"accordingly.<BR>" +
				"This functionality is only available with Gallery 1.3.5 (beta 10) and later.</HTML>");
		jLabel7.setVerticalAlignment(SwingConstants.TOP);
		jLabel8.setText("<HTML>The Gallery URL is the URL users would use to connect to your " +
				"Gallery.</HTML>");
		jLabel8.setVerticalAlignment(SwingConstants.TOP);
		jLabel8.setVerticalTextPosition(SwingConstants.CENTER);
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jStylePanel,          new GridBagConstraints(0, 3, 4, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		jPanel1.add(jLabel2,         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jPanel1.add(jLabel3,         new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jPanel1.add(jLabel4,         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		jPanel1.add(jUsername,            new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
		jPanel1.add(jPassword,          new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jPanel1.add(jType,         new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		jPanel1.add(jPanel2,             new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		jPanel2.add(jOk, null);
		jPanel2.add(jCancel, null);

		jStylePanel.add(jPostNuke, Gallery.types[Gallery.TYPE_POSTNUKE]);
		jPostNuke.add(jLabel5,        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPostNuke.add(jPnLoginUrl,           new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPostNuke.add(jLabel1,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 5), 0, 0));
		jPostNuke.add(jPnGalleryUrl,      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPostNuke.add(jLabel7,   new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));
		jStylePanel.add(jStandalone, Gallery.types[Gallery.TYPE_STANDALONE]);
		jStandalone.add(jLabel6,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 5, 5), 0, 0));
		jStandalone.add(jStandaloneUrl,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jStandalone.add(jLabel8,    new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
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
	}

	public void readUIState() throws MalformedURLException {
		gallery.setUsername(jUsername.getText());
		gallery.setPassword(jPassword.getText());
		gallery.setType(jType.getSelectedIndex());

		gallery.setStUrlString(jStandaloneUrl.getText());
		gallery.setPnLoginUrlString(jPnLoginUrl.getText());
		gallery.setPnGalleryUrlString(jPnGalleryUrl.getText());
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Log.log(Log.INFO, MODULE, "Command selected " + cmd);

		if (cmd.equals("OK")) {
			try {
				readUIState();
				isOK = true;
				setVisible(false);
			} catch (MalformedURLException mue) {
				JOptionPane.showMessageDialog(this, "Malformed URL", "Error", JOptionPane.ERROR_MESSAGE);
			}
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
