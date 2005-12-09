package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class UploadPanel extends PreferencePanel implements ActionListener, PreferenceNames {
	public static final String MODULE = "UploadPa";

	JLabel icon = new JLabel(GRI18n.getString(MODULE, "icon"));

	JPanel jPanel1 = new JPanel();
	JComboBox resizeTo = new JComboBox(defaultSizes);
	JPanel jPanel2 = new JPanel();

	JRadioButton setCaptionNone = new JRadioButton();
	JRadioButton setCaptionWithFilename = new JRadioButton();
	JCheckBox captionStripExtension = new JCheckBox();
	JRadioButton setCaptionWithMetadata = new JRadioButton();
	ButtonGroup buttonGroup2 = new ButtonGroup();

	JPanel jPanel6 = new JPanel();
	JPanel jPanel7 = new JPanel();

	JCheckBox resizeBeforeUpload = new JCheckBox();

	ButtonGroup buttonGroup1 = new ButtonGroup();

	JRadioButton resizeToDefault = new JRadioButton();
	JRadioButton resizeToForce = new JRadioButton();

	JCheckBox htmlEscapeCaptionsNot = new JCheckBox();

	JCheckBox exifAutorotate = new JCheckBox();

	public static Vector defaultSizes = new Vector();

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(PropertiesFile props) {
		resizeBeforeUpload.setSelected(props.getBooleanProperty(RESIZE_BEFORE_UPLOAD));
		int size = props.getIntDimensionProperty(RESIZE_TO);

		if (size == 0) {
			// use default dimension
			resizeToDefault.setSelected(true);
		} else {
			resizeToForce.setSelected(true);
		}

		setupComboValue(size, resizeTo);

		setCaptionNone.setSelected(props.getBooleanProperty(SET_CAPTIONS_NONE));

		setCaptionWithFilename.setSelected(props.getBooleanProperty(SET_CAPTIONS_WITH_FILENAMES));
		captionStripExtension.setSelected(props.getBooleanProperty(CAPTION_STRIP_EXTENSION));

		setCaptionWithMetadata.setSelected(props.getBooleanProperty(SET_CAPTIONS_WITH_METADATA_COMMENT));

		htmlEscapeCaptionsNot.setSelected(!props.getBooleanProperty(HTML_ESCAPE_CAPTIONS));
		exifAutorotate.setSelected(props.getBooleanProperty(EXIF_AUTOROTATE));

		resetUIState();
	}

	public static void setupComboValue(int size, JComboBox resizeTo) {
		MutableComboBoxModel model = (MutableComboBoxModel) resizeTo.getModel();
		boolean found = false;

		for (int i = 0; i < model.getSize(); i++) {
			Object item = model.getElementAt(i);
			if ((item instanceof ResizeSize && ((ResizeSize) item).size == size)
					|| String.valueOf(size).equals(item)) {
				resizeTo.setSelectedIndex(i);
				found = true;
				break;
			}
		}

		if (!found) {
			String s;
			if (size == 0) {
				s = "";
			} else {
				s = String.valueOf(size);
			}

			resizeTo.addItem(s);
			resizeTo.setSelectedItem(s);
		}
	}

	public void writeProperties(PropertiesFile props) {
		props.setBooleanProperty(RESIZE_BEFORE_UPLOAD, resizeBeforeUpload.isSelected());
		if (resizeBeforeUpload.isSelected()) {
			int i = -1;

			if (resizeToDefault.isSelected()) {
				i = 0;
			} else {
				try {
					//Object selectedItem = resizeTo.getSelectedItem();
					//if (selectedItem instanceof ResizeSize) {
					//	i = ((ResizeSize) selectedItem).size;
					//}
					i = Integer.parseInt(resizeTo.getSelectedItem().toString());
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "resizeTo size should be integer numbers");
				}
			}

			if (i != -1) {
				props.setIntDimensionProperty(RESIZE_TO, i);
			}
		}

		props.setBooleanProperty(SET_CAPTIONS_NONE, setCaptionNone.isSelected());

		props.setBooleanProperty(SET_CAPTIONS_WITH_FILENAMES, setCaptionWithFilename.isSelected());
		props.setBooleanProperty(CAPTION_STRIP_EXTENSION, captionStripExtension.isSelected());

		props.setBooleanProperty(SET_CAPTIONS_WITH_METADATA_COMMENT, setCaptionWithMetadata.isSelected());

		props.setBooleanProperty(HTML_ESCAPE_CAPTIONS, !htmlEscapeCaptionsNot.isSelected());
		props.setBooleanProperty(EXIF_AUTOROTATE, exifAutorotate.isSelected());
	}

	public void resetUIState() {
		if (resizeBeforeUpload.isSelected()) {
			resizeToDefault.setEnabled(true);
			resizeToForce.setEnabled(true);

			if (resizeToForce.isSelected()) {
				resizeTo.setEnabled(true);
				resizeTo.setBackground(UIManager.getColor("TextField.background"));
			} else {
				resizeTo.setEnabled(false);
				resizeTo.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			}
		} else {
			resizeToDefault.setEnabled(false);
			resizeToForce.setEnabled(false);
			resizeTo.setEnabled(false);
			resizeTo.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
		
		if (setCaptionWithFilename.isSelected()) {
			captionStripExtension.setEnabled(true);
		} else {
			captionStripExtension.setEnabled(false);
		}
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		this.setLayout(new GridBagLayout());
		jPanel1.setLayout(new GridBagLayout());

		resizeTo.setEditable(true);
		resizeTo.setToolTipText(GRI18n.getString(MODULE, "res2W"));
		resizeTo.setRenderer(new SizeListRenderer());

		jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "res_rot")));
		jPanel2.setLayout(new GridBagLayout());
		//setCaptionNone.setToolTipText(GRI18n.getString(MODULE, "captNoneTip"));
		setCaptionNone.setText(GRI18n.getString(MODULE, "captNone"));
		setCaptionWithFilename.setToolTipText(GRI18n.getString(MODULE, "captTip"));
		setCaptionWithFilename.setText(GRI18n.getString(MODULE, "capt"));
		captionStripExtension.setToolTipText(GRI18n.getString(MODULE, "stripExtTip"));
		captionStripExtension.setText(GRI18n.getString(MODULE, "stripExt"));
		setCaptionWithMetadata.setToolTipText(GRI18n.getString(MODULE, "captMetadataTip"));
		setCaptionWithMetadata.setText(GRI18n.getString(MODULE, "captMetadata"));
		jPanel2.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "captions")));
		resizeBeforeUpload.setToolTipText(GRI18n.getString(MODULE, "resBfrUpldTip"));
		resizeBeforeUpload.setText(GRI18n.getString(MODULE, "resBfrUpld"));
		resizeToDefault.setToolTipText(GRI18n.getString(MODULE, "res2Def"));
		resizeToDefault.setText(GRI18n.getString(MODULE, "res2Def"));
		resizeToForce.setToolTipText(GRI18n.getString(MODULE, "res2FrcTip"));
		resizeToForce.setText(GRI18n.getString(MODULE, "res2Frc"));
		htmlEscapeCaptionsNot.setToolTipText(GRI18n.getString(MODULE, "escCaptTip"));
		htmlEscapeCaptionsNot.setText(GRI18n.getString(MODULE, "escCapt"));
		exifAutorotate.setToolTipText(GRI18n.getString(MODULE, "autoRotTip"));
		exifAutorotate.setText(GRI18n.getString(MODULE, "autoRot"));

		this.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPanel1.add(resizeTo, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(resizeToDefault, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(resizeToForce, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(exifAutorotate, new GridBagConstraints(0, 3, 5, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//		jPanel1.add(jLabel2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
//				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		jPanel1.add(jPanel6, new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(resizeBeforeUpload, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		this.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPanel2.add(setCaptionNone, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(setCaptionWithFilename, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(captionStripExtension, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));

		jPanel2.add(setCaptionWithMetadata, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		jPanel2.add(htmlEscapeCaptionsNot, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jPanel7, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		buttonGroup1.add(resizeToDefault);
		buttonGroup1.add(resizeToForce);

		buttonGroup2.add(setCaptionNone);
		buttonGroup2.add(setCaptionWithFilename);
		buttonGroup2.add(setCaptionWithMetadata);

		resizeBeforeUpload.addActionListener(this);
		resizeToForce.addActionListener(this);
		resizeToDefault.addActionListener(this);

		setCaptionNone.addActionListener(this);
		setCaptionWithFilename.addActionListener(this);
		setCaptionWithMetadata.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		resetUIState();
	}

	static class ResizeSize {
		public int size;
		public String desc;

		public ResizeSize(int size, String desc) {
			this.size = size;
			this.desc = desc;
		}

		public String toString() {
			return String.valueOf(size);
		}
	}

	public static class SizeListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(
					list, value, index,
					isSelected, cellHasFocus);

			if (value instanceof ResizeSize) {
				this.setText(((ResizeSize) value).desc);
			}

			return this;
		}
	}

	static {
		defaultSizes.add(new ResizeSize(400, "400"));
		defaultSizes.add(new ResizeSize(500, "500"));
		defaultSizes.add(new ResizeSize(600, "600"));
		defaultSizes.add(new ResizeSize(640, "640"));
		defaultSizes.add(new ResizeSize(800, "800"));
		defaultSizes.add(new ResizeSize(1024, "1024"));
		defaultSizes.add(new ResizeSize(1280, "1280 (1 MPix)"));
		defaultSizes.add(new ResizeSize(1600, "1600 (2 MPix)"));
		defaultSizes.add(new ResizeSize(2048, "2048 (3 MPix)"));
		defaultSizes.add(new ResizeSize(2304, "2304 (4 MPix)"));
		defaultSizes.add(new ResizeSize(2592, "2592 (5 MPix)"));
	}
}

