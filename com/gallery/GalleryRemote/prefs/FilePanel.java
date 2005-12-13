package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
/*public class FilePanel extends PreferencePanel implements ActionListener, PreferenceNames {
	public static final String MODULE = "FilePa";

	JLabel icon = new JLabel(GRI18n.getString(MODULE, "icon"));
	JCheckBox m_loadLastMRU = new JCheckBox();
	JComboBox m_numberOfMRU = new JComboBox(
			new String[]{" 1", " 2", " 3", " 4", " 5", " 6", " 7", " 8", " 9"});

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(PropertiesFile props) {
		m_loadLastMRU.setSelected(props.getLoadLastMRU());
		m_numberOfMRU.setSelectedIndex(props.getIntProperty(MRU_COUNT) - 1);

		resetUIState();
	}

	public void writeProperties(PropertiesFile props) {
		props.setLoadLastMRU(m_loadLastMRU.isSelected());
		props.setMRUCountProperty(m_numberOfMRU.getSelectedIndex() + 1);
	}

	public void resetUIState() {
		// Nothing to do right now
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		this.setLayout(new FlowLayout());
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new GridBagLayout());
		thePanel.setBorder(
				new TitledBorder(
						BorderFactory.createEtchedBorder(Color.white,
								new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "mainPanelTitle")));

		JLabel numberOfMRULabel = new JLabel();

		m_loadLastMRU.setText(GRI18n.getString(MODULE, "loadLastMRU"));
		numberOfMRULabel.setText(GRI18n.getString(MODULE, "numberOfMRU"));
		m_numberOfMRU.setPrototypeDisplayValue("XXX");

		thePanel.add(m_loadLastMRU,
				new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		thePanel.add(numberOfMRULabel,
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		thePanel.add(m_numberOfMRU,
				new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		this.add(thePanel,
				new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

		m_loadLastMRU.addActionListener(this);
		m_numberOfMRU.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		resetUIState();
	}
}*/

