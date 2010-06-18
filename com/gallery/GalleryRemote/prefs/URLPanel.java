package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.MainFrame;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class URLPanel extends PreferencePanel implements ListSelectionListener, ActionListener {
	public static final String MODULE = "URLPa";


	JLabel icon = new JLabel(GRI18n.getString(MODULE, "icon"));

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JScrollPane jScrollPane1 = new JScrollPane();
	JList jGalleries = new JList();
	JButton jModify = new JButton();
	JButton jNew = new JButton();
	JButton jDelete = new JButton();
	JPanel jPanel1 = new JPanel();
	JLabel jDetails = new JLabel();
	GridLayout gridLayout1 = new GridLayout();

	public JLabel getIcon() {
		return icon;
	}

	public boolean isReversible() {
		return false;
	}

	public void readProperties(PropertiesFile props) {
	}

	public void writeProperties(PropertiesFile props) {
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		this.setLayout(gridBagLayout1);
		jModify.setActionCommand("Modify");
		jModify.setText(GRI18n.getString(MODULE, "modify"));
		jNew.setActionCommand("New");
		jNew.setText(GRI18n.getString(MODULE, "new"));
		jDelete.setActionCommand("Delete");
		jDelete.setText(GRI18n.getString(MODULE, "delete"));
		jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "details")));
		jPanel1.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		jDetails.setMinimumSize(new Dimension(0, 50));
		jDetails.setPreferredSize(new Dimension(0, 50));
		jDetails.setHorizontalAlignment(SwingConstants.LEFT);
		jDetails.setVerticalAlignment(SwingConstants.TOP);
		this.add(jScrollPane1, new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jModify, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		this.add(jNew, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		this.add(jDelete, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		this.add(jPanel1, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jDetails, null);
		jScrollPane1.getViewport().add(jGalleries, null);

		jGalleries.setModel(GalleryRemote._().getCore().getGalleries());
		jGalleries.setCellRenderer(new GalleryCellRenderer());
		jGalleries.addListSelectionListener(this);

		jGalleries.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = jGalleries.locationToIndex(e.getPoint());
					modifyGallery((Gallery) jGalleries.getModel().getElementAt(index));
				}
			}
		});

		if (GalleryRemote._().getCore().getGalleries().getSize() > 0) {
			jGalleries.setSelectedIndex(0);
		} else {
			resetUIState();
		}

		jModify.addActionListener(this);
		jNew.addActionListener(this);
		jDelete.addActionListener(this);
	}

	public void valueChanged(ListSelectionEvent e) {
		resetUIState();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Gallery g = (Gallery) jGalleries.getSelectedValue();

		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + cmd + " Gallery: " + g);

		if (cmd.equals("Modify")) {
			modifyGallery(g);
		} else if (cmd.equals("New")) {
			Gallery newG = new Gallery(GalleryRemote._().getCore().getMainStatusUpdate());
			if (GalleryRemote._().getCore() instanceof TreeModelListener) {
				newG.addTreeModelListener((TreeModelListener) GalleryRemote._().getCore());
			}

			GalleryEditorDialog ged = new GalleryEditorDialog(dialog, newG);

			if (ged.isOK()) {
				GalleryRemote._().getCore().getGalleries().addElement(newG);
				jGalleries.setSelectedValue(newG, true);

				Gallery.uncacheAmbiguousUrl();

				resetUIState();
			}
		} else if (cmd.equals("Delete")) {
			Object[] params = {g.getUrl()};
			int n = JOptionPane.showConfirmDialog(this, GRI18n.getString(MODULE, "delConfirm", params),
					GRI18n.getString(MODULE, "delete"),
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION);

			if (n == JOptionPane.YES_OPTION) {
				((MainFrame) GalleryRemote._().getCore()).removeGallery(g);

				Gallery.uncacheAmbiguousUrl();
			}
		} else if (cmd.equals("GalleryEditorDialog")) {
			Gallery newG = (Gallery) e.getSource();

			if (GalleryRemote._().getCore().getGalleries().getIndexOf(newG) == -1) {
				GalleryRemote._().getCore().getGalleries().addElement(newG);
				jGalleries.setSelectedValue(newG, true);
			}

			Gallery.uncacheAmbiguousUrl();
			resetUIState();
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unknown command: " + cmd);
		}
	}

	private void modifyGallery(Gallery g) {
		GalleryEditorDialog ged = new GalleryEditorDialog(dialog, g);

		if (ged.isOK()) {
			//jGalleries.repaint();
			int i = GalleryRemote._().getCore().getGalleries().getIndexOf(g);
			GalleryRemote._().getCore().getGalleries().removeElementAt(i);
			GalleryRemote._().getCore().getGalleries().insertElementAt(g, i);

			Gallery.uncacheAmbiguousUrl();
		}
	}

	public void resetUIState() {
		Gallery selectedGallery = (Gallery) jGalleries.getSelectedValue();

		StringBuffer sb = new StringBuffer();

		if (selectedGallery != null) {
			sb.append("<HTML>");

			sb.append(GRI18n.getString(MODULE, "gllryURL")).append(selectedGallery.getUrlString()).append("<br>");

			String username = selectedGallery.getUsername();
			if (username == null || username.length() == 0) {
				username = "&lt;Not set&gt;";
			}
			sb.append(GRI18n.getString(MODULE, "username")).append(username).append("<br>");
			
			if (selectedGallery.isAutoLoadOnStartup()) {
				sb.append(GRI18n.getString(MODULE, "autoLogin")).append("<br>");
			}

			sb.append("</HTML>");

			jModify.setEnabled(true);
			jDelete.setEnabled(true);
		} else {
			jModify.setEnabled(false);
			jDelete.setEnabled(false);
		}

		jDetails.setText(sb.toString());
	}

	public class GalleryCellRenderer extends DefaultListCellRenderer {
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

			Gallery g = (Gallery) value;

			setText(g.toString());
			
			if (g.isAutoLoadOnStartup()) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}

			return this;
		}
	}
}

