package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class UploadPanel extends PreferencePanel implements ActionListener, PreferenceNames {
	public static final String MODULE = "UploadPa";



	JLabel icon = new JLabel(GRI18n.getString(MODULE, "icon"));

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JPanel jPanel1 = new JPanel();
	JTextField resizeToWidth = new JTextField();
	JLabel jLabel2 = new JLabel();
	JTextField resizeToHeight = new JTextField();
	JPanel jPanel2 = new JPanel();
	JCheckBox setCaptionsWithFilenames = new JCheckBox();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	GridBagLayout gridBagLayout4 = new GridBagLayout();
	JPanel jPanel6 = new JPanel();
	JPanel jPanel7 = new JPanel();
	JCheckBox resizeBeforeUpload = new JCheckBox();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	JRadioButton resizeToDefault = new JRadioButton();
	JRadioButton resizeToForce = new JRadioButton();
	JCheckBox htmlEscapeCaptionsNot = new JCheckBox();
	JCheckBox captionStripExtension = new JCheckBox();
    Border border1;
    TitledBorder titledBorder1;
    JCheckBox exifAutorotate = new JCheckBox();

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(GalleryProperties props) {
		resizeBeforeUpload.setSelected(props.getBooleanProperty(RESIZE_BEFORE_UPLOAD));
		if (new Dimension(0,0).equals(props.getDimensionProperty(RESIZE_TO))) {
			// use default dimension
			resizeToDefault.setSelected(true);
		} else {
			resizeToForce.setSelected(true);
			resizeToWidth.setText("" + (int) props.getDimensionProperty(RESIZE_TO).getWidth());
			resizeToHeight.setText("" + (int) props.getDimensionProperty(RESIZE_TO).getHeight());
		}

		setCaptionsWithFilenames.setSelected(props.getBooleanProperty(SET_CAPTIONS_WITH_FILENAMES));
		captionStripExtension.setSelected(props.getBooleanProperty(CAPTION_STRIP_EXTENSION));
		htmlEscapeCaptionsNot.setSelected(! props.getBooleanProperty(HTML_ESCAPE_CAPTIONS));
		exifAutorotate.setSelected(props.getBooleanProperty(EXIF_AUTOROTATE));

		resetUIState();
	}

	public void writeProperties(GalleryProperties props) {
		props.setBooleanProperty(RESIZE_BEFORE_UPLOAD, resizeBeforeUpload.isSelected());
		if (resizeBeforeUpload.isSelected()) {
			Dimension d = null;
			if (resizeToDefault.isSelected()) {
				d = new Dimension(0,0);
			} else {
				try {
					d = new Dimension(Integer.parseInt(resizeToWidth.getText()), Integer.parseInt(resizeToHeight.getText()));
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "resizeTo size should be integer numbers");
				}
			}
			if (d != null) {
				props.setDimensionProperty(RESIZE_TO, d);
			}
		}

		props.setBooleanProperty(SET_CAPTIONS_WITH_FILENAMES, setCaptionsWithFilenames.isSelected());
		props.setBooleanProperty(CAPTION_STRIP_EXTENSION, captionStripExtension.isSelected());
		props.setBooleanProperty(HTML_ESCAPE_CAPTIONS, ! htmlEscapeCaptionsNot.isSelected());
		props.setBooleanProperty(EXIF_AUTOROTATE, exifAutorotate.isSelected());
	}

	public void resetUIState() {
		if (resizeBeforeUpload.isSelected()) {
			resizeToDefault.setEnabled(true);
			resizeToForce.setEnabled(true);

			if (resizeToForce.isSelected()) {
				resizeToHeight.setEnabled(true);
				resizeToWidth.setEnabled(true);
				resizeToHeight.setBackground(UIManager.getColor("TextField.background"));
				resizeToWidth.setBackground(UIManager.getColor("TextField.background"));
			} else {
				resizeToHeight.setEnabled(false);
				resizeToWidth.setEnabled(false);
				resizeToHeight.setBackground(UIManager.getColor("TextField.inactiveBackground"));
				resizeToWidth.setBackground(UIManager.getColor("TextField.inactiveBackground"));			}
		} else {
			resizeToDefault.setEnabled(false);
			resizeToForce.setEnabled(false);
			resizeToHeight.setEnabled(false);
			resizeToWidth.setEnabled(false);
			resizeToHeight.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			resizeToWidth.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}

		if (setCaptionsWithFilenames.isSelected()) {
			captionStripExtension.setEnabled(true);
		} else {
			captionStripExtension.setEnabled(false);
		}
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(border1,GRI18n.getString(MODULE, "res_rot"));
        this.setLayout(gridBagLayout1);
		jPanel1.setLayout(gridBagLayout4);
		resizeToWidth.setMinimumSize(new Dimension(25, 21));
		resizeToWidth.setPreferredSize(new Dimension(25, 21));
		resizeToWidth.setToolTipText(GRI18n.getString(MODULE, "res2W"));
		jLabel2.setText("x");
		resizeToHeight.setMinimumSize(new Dimension(25, 21));
		resizeToHeight.setPreferredSize(new Dimension(25, 21));
		resizeToHeight.setToolTipText(GRI18n.getString(MODULE, "res2H"));
		jPanel1.setBorder(titledBorder1);
		jPanel2.setLayout(gridBagLayout2);
		setCaptionsWithFilenames.setToolTipText(GRI18n.getString(MODULE, "captTip"));
		setCaptionsWithFilenames.setText(GRI18n.getString(MODULE, "capt"));
		jPanel2.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),GRI18n.getString(MODULE, "captions")));
		resizeBeforeUpload.setToolTipText(GRI18n.getString(MODULE, "resBfrUpldTip"));
		resizeBeforeUpload.setText(GRI18n.getString(MODULE, "resBfrUpld"));
		resizeToDefault.setToolTipText(GRI18n.getString(MODULE, "res2Def"));
		resizeToDefault.setText(GRI18n.getString(MODULE, "res2Def"));
		resizeToForce.setToolTipText(GRI18n.getString(MODULE, "res2FrcTip"));
		resizeToForce.setText(GRI18n.getString(MODULE, "res2Frc"));
		htmlEscapeCaptionsNot.setToolTipText(GRI18n.getString(MODULE, "escCaptTip"));
		htmlEscapeCaptionsNot.setText(GRI18n.getString(MODULE, "escCapt"));
		captionStripExtension.setToolTipText(GRI18n.getString(MODULE, "stripExtTip"));
		captionStripExtension.setText(GRI18n.getString(MODULE, "stripExt"));
		exifAutorotate.setToolTipText(GRI18n.getString(MODULE, "autoRotTip"));
        exifAutorotate.setText(GRI18n.getString(MODULE, "autoRot"));
        this.add(jPanel1,     new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPanel1.add(resizeToWidth,         new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jLabel2,          new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		jPanel1.add(resizeToHeight,         new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jPanel6,      new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(resizeBeforeUpload,     new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jPanel2,   new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		jPanel2.add(setCaptionsWithFilenames,          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jPanel7,   new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(resizeToDefault,   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		jPanel1.add(resizeToForce,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
        jPanel1.add(exifAutorotate,      new GridBagConstraints(0, 3, 5, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(htmlEscapeCaptionsNot,   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(captionStripExtension,  new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));
		buttonGroup1.add(resizeToDefault);
		buttonGroup1.add(resizeToForce);

		resizeBeforeUpload.addActionListener(this);
		resizeToForce.addActionListener(this);
		resizeToDefault.addActionListener(this);
		setCaptionsWithFilenames.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		resetUIState();
	}
}

