package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.model.Gallery;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class URLPanel extends PreferencePanel implements ListSelectionListener, ActionListener {
	public static final String MODULE = "URLPa";

	JLabel icon = new JLabel("URLs");

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

	public void readProperties(GalleryProperties props) {
	}

	public void writeProperties(GalleryProperties props) {
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		this.setLayout(gridBagLayout1);
		jModify.setActionCommand("Modify");
		jModify.setText("Modify...");
		jNew.setActionCommand("New");
		jNew.setText("New...");
		jDelete.setActionCommand("Delete");
		jDelete.setText("Delete...");
		jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Details"));
		jPanel1.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		jDetails.setMinimumSize(new Dimension(0, 50));
		jDetails.setPreferredSize(new Dimension(0, 50));
		jDetails.setHorizontalAlignment(SwingConstants.LEFT);
		jDetails.setVerticalAlignment(SwingConstants.TOP);
		this.add(jScrollPane1,    new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jModify,    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		this.add(jNew,   new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		this.add(jDelete,   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		this.add(jPanel1,   new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jDetails, null);
		jScrollPane1.getViewport().add(jGalleries, null);

		jGalleries.setModel(mainFrame.galleries);
		jGalleries.setCellRenderer(new GalleryCellRenderer());
		jGalleries.addListSelectionListener(this);

		if (mainFrame.galleries.getSize() > 0) {
			jGalleries.setSelectedIndex(0);
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

		Log.log(Log.INFO, MODULE, "Command selected " + cmd + " Gallery: " + g);

		if (cmd.equals("Modify")) {
			GalleryEditorDialog ged = new GalleryEditorDialog(dialog, g);

			if (ged.isOK()) {
				//jGalleries.repaint();
				int i = mainFrame.galleries.getIndexOf(g);
				mainFrame.galleries.removeElementAt(i);
				mainFrame.galleries.insertElementAt(g, i);
			}
		} else if (cmd.equals("New")) {
			Gallery newG = new Gallery(mainFrame.jStatusBar);
			GalleryEditorDialog ged = new GalleryEditorDialog(dialog, newG);

			if (ged.isOK()) {
				mainFrame.galleries.addElement(newG);
				jGalleries.setSelectedValue(newG, true);

				resetUIState();
			}
		} else if (cmd.equals("Delete")) {
			int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to discard login information for '" + g.getStUrlString() + "'?",
					"Delete Gallery",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION);

			if (n == JOptionPane.YES_OPTION) {
				mainFrame.removeGallery(g);
			}
		} else if (cmd.equals("GalleryEditorDialog")) {
			Gallery newG = (Gallery) e.getSource();

			if (mainFrame.galleries.getIndexOf(newG) == -1) {
				mainFrame.galleries.addElement(newG);
				jGalleries.setSelectedValue(newG, true);
			}

			resetUIState();
		} else {
			Log.log(Log.ERROR, MODULE, "Unknown command: " + cmd);
		}
	}

	public void resetUIState() {
		Gallery selectedGallery = (Gallery) jGalleries.getSelectedValue();

		StringBuffer sb = new StringBuffer();

		if (selectedGallery != null) {
			sb.append("<HTML>");

			if (selectedGallery.getType() == Gallery.TYPE_STANDALONE) {
				sb.append("Gallery URL: ").append(selectedGallery.getStUrlString()).append("<br>");
			} else if (selectedGallery.getType() == Gallery.TYPE_POSTNUKE) {
				sb.append("PostNuke login URL: ").append(selectedGallery.getPnLoginUrlString()).append("<br>");
				sb.append("PostNuke Gallery URL: ").append(selectedGallery.getPnGalleryUrlString()).append("<br>");
			}

			String username = selectedGallery.getUsername();
			if (username == null || username.length() == 0) {
				username = "&lt;Not set&gt;";
			}
			sb.append("Username: ").append(username).append("<br>");

			sb.append("</HTML>");

			jModify.setEnabled(true);
			jDelete.setEnabled(true);
		} else {
			jModify.setEnabled(true);
			jDelete.setEnabled(true);
		}

		jDetails.setText(sb.toString());
	}

	public class GalleryCellRenderer extends DefaultListCellRenderer
	{
		/**
		 *  Gets the listCellRendererComponent attribute of the FileCellRenderer
		 *  object
		 *
		 *@param  list      Description of Parameter
		 *@param  value     Description of Parameter
		 *@param  index     Description of Parameter
		 *@param  selected  Description of Parameter
		 *@param  hasFocus  Description of Parameter
		 *@return           The listCellRendererComponent value
		 *@since
		 */
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean selected, boolean hasFocus ) {
			super.getListCellRendererComponent( list, value, index, selected, hasFocus );

			Gallery g = (Gallery) value;

			setText(g.toString());

			return this;
		}
	}
}

