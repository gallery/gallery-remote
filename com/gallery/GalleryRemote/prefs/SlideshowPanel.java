package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import javax.swing.border.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: May 8, 2003
 */
public class SlideshowPanel extends PreferencePanel implements PreferenceNames {
	public static final String MODULE = "SlidePa";


	JLabel icon = new JLabel(GRI18n.getString(MODULE, "icon"));

	JPanel delayPanel = new JPanel();
	JPanel locationPanel = new JPanel();
	public JPanel spacerPanel = new JPanel();
	JLabel delay = new JLabel();
	JTextField jDelay = new JTextField();
	JLabel delayHelp = new JLabel();
	JLabel progress = new JLabel();
	JLabel caption = new JLabel();
	JLabel extra = new JLabel();
	JLabel url = new JLabel();
	JPanel performancePanel = new JPanel();
	JComboBox jProgress;
	JComboBox jCaption;
	JComboBox jExtra;
	JComboBox jUrl;
	JCheckBox jLowRez = new JCheckBox();
	JPanel spacerPanel1 = new JPanel();

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(PropertiesFile props) {
		jProgress.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_PROGRESS)));
		jCaption.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_CAPTION)));
		jExtra.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_EXTRA)));
		jUrl.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_URL)));

		jLowRez.setSelected(props.getBooleanProperty(SLIDESHOW_LOWREZ));
		jDelay.setText("" + props.getIntProperty(SLIDESHOW_DELAY));

		jProgress.setEnabled(! props.isOverridden(SLIDESHOW_PROGRESS));
		jCaption.setEnabled(! props.isOverridden(SLIDESHOW_CAPTION));
		jExtra.setEnabled(! props.isOverridden(SLIDESHOW_EXTRA));
		jUrl.setEnabled(! props.isOverridden(SLIDESHOW_URL));
		jLowRez.setEnabled(! props.isOverridden(SLIDESHOW_LOWREZ));
		jDelay.setEnabled(! props.isOverridden(SLIDESHOW_DELAY));
	}

	public void writeProperties(PropertiesFile props) {
		props.setIntProperty(SLIDESHOW_PROGRESS, ((LocationItem) jProgress.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_CAPTION, ((LocationItem) jCaption.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_EXTRA, ((LocationItem) jExtra.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_URL, ((LocationItem) jUrl.getSelectedItem()).id);

		props.setBooleanProperty(SLIDESHOW_LOWREZ, jLowRez.isSelected());
		props.setIntProperty(SLIDESHOW_DELAY, Integer.parseInt(jDelay.getText()));
	}

	public void buildUI() {
		jbInit();
	}

	private void jbInit() {
		Vector locationItems = new Vector();
		locationItems.add(new LocationItem(0));
		locationItems.add(new LocationItem(10 + SwingConstants.LEFT));
		locationItems.add(new LocationItem(10 + SwingConstants.CENTER));
		locationItems.add(new LocationItem(10 + SwingConstants.RIGHT));
		locationItems.add(new LocationItem(20 + SwingConstants.LEFT));
		locationItems.add(new LocationItem(20 + SwingConstants.CENTER));
		locationItems.add(new LocationItem(20 + SwingConstants.RIGHT));
		locationItems.add(new LocationItem(30 + SwingConstants.LEFT));
		locationItems.add(new LocationItem(30 + SwingConstants.CENTER));
		locationItems.add(new LocationItem(30 + SwingConstants.RIGHT));

		jProgress = new JComboBox(locationItems);
		jCaption = new JComboBox(locationItems);
		jExtra = new JComboBox(locationItems);
		jUrl = new JComboBox(locationItems);

		setLayout(new GridBagLayout());

		delayPanel.setLayout(new GridBagLayout());
		delayPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Delay for automatic slideshow"));
		locationPanel.setLayout(new GridBagLayout());
		locationPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Location of text info"));
		performancePanel.setLayout(new GridBagLayout());
		performancePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Performance"));

		delay.setText(GRI18n.getString(MODULE, "delay"));
		delay.setLabelFor(jDelay);
		delay.setToolTipText(GRI18n.getString(MODULE, "delayHelp"));
		delayHelp.setText(GRI18n.getString(MODULE, "delayDesc"));

		progress.setText(GRI18n.getString(MODULE, "progress"));
		progress.setLabelFor(jProgress);
		progress.setToolTipText(GRI18n.getString(MODULE, "progressHelp"));
		caption.setText(GRI18n.getString(MODULE, "caption"));
		caption.setLabelFor(jCaption);
		caption.setToolTipText(GRI18n.getString(MODULE, "captionHelp"));
		extra.setText(GRI18n.getString(MODULE, "extra"));
		extra.setLabelFor(jExtra);
		extra.setToolTipText(GRI18n.getString(MODULE, "extraHelp"));
		url.setText(GRI18n.getString(MODULE, "url"));
		url.setLabelFor(jUrl);
		url.setToolTipText(GRI18n.getString(MODULE, "urlHelp"));

		jLowRez.setText(GRI18n.getString(MODULE, "lowRez"));
		jLowRez.setToolTipText(GRI18n.getString(MODULE, "lowRezHelp"));

		this.add(delayPanel,    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(locationPanel,     new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(performancePanel,     new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		this.add(spacerPanel,    new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		delayPanel.add(delay,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		delayPanel.add(jDelay,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		delayPanel.add(delayHelp,   new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		performancePanel.add(jLowRez,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		performancePanel.add(spacerPanel1,      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		locationPanel.add(progress,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jProgress,       new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(caption,     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jCaption,      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(extra,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(url,   new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jExtra,    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(jUrl,    new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

	class LocationItem {
		String name;
		int id;

		public LocationItem(int id) {
			this.id = id;
			name = GRI18n.getString(MODULE, "locationItem." + id);
		}

		public String toString() {
			return name;
		}

		public boolean equals(Object o) {
			if (! (o instanceof LocationItem)) {
				return false;
			}

			return id == ((LocationItem) o).id;
		}
	}
}

