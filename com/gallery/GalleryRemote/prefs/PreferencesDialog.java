package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * The Preferences dialog
 * User: paour
 * Date: May 8, 2003
 */

public class PreferencesDialog extends JDialog implements ListSelectionListener, ActionListener {
	public static final String MODULE = "PrefsDlog";


	DefaultListModel panels = new DefaultListModel();
	HashMap panelNames = new HashMap();
	private boolean isOK = false;

	JPanel jPanel1 = new JPanel();
	JScrollPane jScrollPane1 = new JScrollPane();
	JList jIcons = new JList();
	JPanel jPanels = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	CardLayout jPanelsLayout = new CardLayout();
	JPanel jPanel2 = new JPanel();
	JButton jOK = new JButton();
	GridLayout gridLayout1 = new GridLayout();
	JButton jRevert = new JButton();
	JButton jCancel = new JButton();

	public PreferencesDialog(Frame owner) {
		super(owner, true);

		try {
			jbInit();
		} catch (Exception e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}

		loadPanes();

		jIcons.setSelectedIndex(0); // this must be done to call valueChanged

		DialogUtil.center(this, owner);
	}

	private void loadPanes() {
		Properties panes = new Properties();
		try {
			panes.load(getClass().getResourceAsStream("panes.properties"));

			int i = 1;
			String className = null;
			while ((className = panes.getProperty("pane." + i++)) != null) {
				try {
					PreferencePanel pp = (PreferencePanel) Class.forName(className).newInstance();

					pp.setDialog(this);
					pp.buildUI();

					panels.addElement(pp);

					jPanels.add(className, pp);

					panelNames.put(className, pp);
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "Bad panel: " + className);
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
				}
			}

			jIcons.setModel(panels);
		} catch (IOException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}

	public void setPanel(String className) {
		PreferencePanel pp = (PreferencePanel) panelNames.get(className);

		jIcons.setSelectedValue(pp, true);
	}

	private void jbInit() throws Exception {
		jPanel1.setLayout(gridBagLayout1);
		jScrollPane1.setAlignmentY((float) 0.5);
		jScrollPane1.setPreferredSize(new Dimension(100, 200));
		jPanels.setLayout(jPanelsLayout);
		this.setTitle(GRI18n.getString(MODULE, "title"));
		jOK.setMnemonic('0');
		jOK.setText(GRI18n.getString("Common", "OK"));
		jOK.setActionCommand("OK");
		jPanel2.setLayout(gridLayout1);
		jRevert.setToolTipText(GRI18n.getString(MODULE, "revertTip"));
		jRevert.setText(GRI18n.getString(MODULE, "revert"));
		jRevert.setActionCommand("revert");
		jCancel.setText(GRI18n.getString("Common", "Cancel"));
		jCancel.setActionCommand("cancel");
		gridLayout1.setHgap(5);
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jScrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jPanels, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 0, 10), 0, 0));
		jPanel1.add(jPanel2, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		jPanel2.add(jOK, null);
		jScrollPane1.getViewport().add(jIcons, null);
		jPanel2.add(jCancel, null);
		jPanel2.add(jRevert, null);

		jIcons.setCellRenderer(new IconsCellRenderer());
		jIcons.addListSelectionListener(this);
		jOK.addActionListener(this);
		jCancel.addActionListener(this);
		jRevert.addActionListener(this);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		getRootPane().setDefaultButton(jOK);
	}

	public void valueChanged(ListSelectionEvent e) {
		PreferencePanel pp = (PreferencePanel) jIcons.getSelectedValue();

		String className = pp.getClass().getName();
		Log.log(Log.LEVEL_TRACE, MODULE, "Showing panel: " + className);

		pp.readPropertiesFirst(GalleryRemote._().properties);
		jPanelsLayout.show(jPanels, className);

		jRevert.setEnabled(pp.isReversible());

		pack();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + cmd);

		if (cmd.equals("OK")) {
			GalleryRemote._().properties.uncache();

			Enumeration enumeration = panels.elements();
			while (enumeration.hasMoreElements()) {
				PreferencePanel pp = (PreferencePanel) enumeration.nextElement();

				if (pp.hasBeenRead()) {
					Log.log(Log.LEVEL_TRACE, MODULE, "Writing properties for panel " + pp.getClass());

					pp.writeProperties(GalleryRemote._().properties);
				}
			}

			isOK = true;

			setVisible(false);

			//Log.log(Log.TRACE, MODULE, "Updating preferences");
			//((MainFrame) getOwner()).readPreferences(oldProperties);
		} else if (cmd.equals("cancel")) {
			setVisible(false);
		} else if (cmd.equals("revert")) {
			PreferencePanel pp = (PreferencePanel) jIcons.getSelectedValue();
			Log.log(Log.LEVEL_TRACE, MODULE, "Reverting panel " + pp.getClass());
			pp.readProperties(GalleryRemote._().properties);
		}
	}

	public boolean isOK() {
		return isOK;
	}

	/**
	 * Cell renderer
	 * 
	 * @author paour
	 * @created 11 août 2002
	 */
	public class IconsCellRenderer extends DefaultListCellRenderer {
		public IconsCellRenderer() {
			super();
			setHorizontalTextPosition(JLabel.CENTER);
			setVerticalTextPosition(JLabel.BOTTOM);
			setHorizontalAlignment(JLabel.CENTER);
		}

		/**
		 * Gets the listCellRendererComponent attribute of the FileCellRenderer
		 * object
		 * 
		 * @param list     Description of Parameter
		 * @param value    Description of Parameter
		 * @param index    Description of Parameter
		 * @param selected Description of Parameter
		 * @param hasFocus Description of Parameter
		 * @return The listCellRendererComponent value
		 */
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean selected, boolean hasFocus) {
			super.getListCellRendererComponent(list, value, index, selected, hasFocus);

			if (value != null && index != -1) {
				PreferencePanel pp = (PreferencePanel) value;
				setText(pp.getIcon().getText());
				setIcon(pp.getIcon().getIcon());
			} else {
				setText("dummy");
			}

			return this;
		}
	}
}
