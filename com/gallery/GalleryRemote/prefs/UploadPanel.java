package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.Log;

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

	JLabel icon = new JLabel("Upload");

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
					Log.log(Log.ERROR, MODULE, "resizeTo size should be integer numbers");
				}
			}
			if (d != null) {
				props.setDimensionProperty(RESIZE_TO, d);
			}
		}

		props.setBooleanProperty(SET_CAPTIONS_WITH_FILENAMES, setCaptionsWithFilenames.isSelected());
		props.setBooleanProperty(CAPTION_STRIP_EXTENSION, captionStripExtension.isSelected());
		props.setBooleanProperty(HTML_ESCAPE_CAPTIONS, ! htmlEscapeCaptionsNot.isSelected());
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
		this.setLayout(gridBagLayout1);
		jPanel1.setLayout(gridBagLayout4);
		resizeToWidth.setMinimumSize(new Dimension(25, 21));
		resizeToWidth.setPreferredSize(new Dimension(25, 21));
		resizeToWidth.setToolTipText("Resize to width");
		jLabel2.setText("x");
		resizeToHeight.setMinimumSize(new Dimension(25, 21));
		resizeToHeight.setPreferredSize(new Dimension(25, 21));
		resizeToHeight.setToolTipText("Resize to height");
		jPanel1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Resize"));
		jPanel2.setLayout(gridBagLayout2);
		setCaptionsWithFilenames.setToolTipText("Sets the caption to the image file when the image is loaded (it can " +
				"later be changed by editing the caption)");
		setCaptionsWithFilenames.setText("Pre-set captions with filenames");
		jPanel2.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Captions"));
		resizeBeforeUpload.setToolTipText("Instructs Gallery Remote to perform resizing on images before they\'re " +
				"sent to the server, which lowers bandwidth usage. Only possible if " +
				"ImageMagick is installed.");
		resizeBeforeUpload.setText("Resize before upload");
		resizeToDefault.setToolTipText("Resize to the Album\'s default");
		resizeToDefault.setText("Album default");
		resizeToForce.setToolTipText("Force the resize dimension, and disregard the dimensions set by the " +
				"Album");
		resizeToForce.setText("Force resize to");
		htmlEscapeCaptionsNot.setToolTipText("When checked, you can (and have to) write your captions in HTML markup. " +
				"When unchecked, just type any text, and HTML markup will be generated " +
				"for you.");
		htmlEscapeCaptionsNot.setText("Allow HTML markup in captions and extra fields");
		captionStripExtension.setToolTipText("Remove the extension (.gif, .jpeg, etc.) from the filename when using " +
				"it as a caption");
		captionStripExtension.setText("Strip the filename extension");
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

