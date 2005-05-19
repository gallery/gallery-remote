package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ColorWellButton;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.GalleryRemoteCore;
import com.gallery.GalleryRemote.MainFrame;
import com.gallery.GalleryRemote.Log;

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

	JPanel progressionPanel = new JPanel();
	JPanel locationPanel = new JPanel();
	public JPanel spacerPanel = new JPanel();
	JLabel delay = new JLabel();
	JTextField jDelay = new JTextField();
	JLabel help = new JLabel();
	JLabel progress = new JLabel();
	JLabel caption = new JLabel();
	JLabel extra = new JLabel();
	JLabel url = new JLabel();
	JCheckBox jOverride = new JCheckBox();
	JPanel apperancePanel = new JPanel();
	JComboBox jProgress;
	JComboBox jCaption;
	JComboBox jExtra;
	JComboBox jUrl;
	JCheckBox jLowRez = new JCheckBox();
	JCheckBox jRandom = new JCheckBox();
	JCheckBox jNoStretch = new JCheckBox();
	JCheckBox jPreloadAll = new JCheckBox();
	JCheckBox jLoop = new JCheckBox();
	JPanel spacerPanel1 = new JPanel();
	ColorWellButton jBackgroundColor = new ColorWellButton(Color.red);

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(PropertiesFile props) {
		jProgress.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_PROGRESS)));
		jCaption.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_CAPTION)));
		jExtra.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_EXTRA)));
		jUrl.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_URL)));

		jLowRez.setSelected(props.getBooleanProperty(SLIDESHOW_LOWREZ));
		jRandom.setSelected(props.getBooleanProperty(SLIDESHOW_RANDOM));
		jNoStretch.setSelected(props.getBooleanProperty(SLIDESHOW_NOSTRETCH));
		jPreloadAll.setSelected(props.getBooleanProperty(SLIDESHOW_PRELOADALL));
		jLoop.setSelected(props.getBooleanProperty(SLIDESHOW_LOOP));
		jDelay.setText("" + props.getIntProperty(SLIDESHOW_DELAY));
		Color color = props.getColorProperty(SLIDESHOW_COLOR);
		jOverride.setSelected(color != null);
		jBackgroundColor.setSelectedColor(color);

		jProgress.setEnabled(! props.isOverridden(SLIDESHOW_PROGRESS));
		jCaption.setEnabled(! props.isOverridden(SLIDESHOW_CAPTION));
		jExtra.setEnabled(! props.isOverridden(SLIDESHOW_EXTRA));
		jUrl.setEnabled(! props.isOverridden(SLIDESHOW_URL));
		jLowRez.setEnabled(! props.isOverridden(SLIDESHOW_LOWREZ));
		jRandom.setEnabled(! props.isOverridden(SLIDESHOW_RANDOM));
		jNoStretch.setEnabled(! props.isOverridden(SLIDESHOW_NOSTRETCH));
		jPreloadAll.setEnabled(! props.isOverridden(SLIDESHOW_PRELOADALL));
		jLoop.setEnabled(! props.isOverridden(SLIDESHOW_LOOP));
		jDelay.setEnabled(! props.isOverridden(SLIDESHOW_DELAY));
		jOverride.setEnabled(! props.isOverridden(SLIDESHOW_COLOR));
		jBackgroundColor.setEnabled(! props.isOverridden(SLIDESHOW_COLOR));
	}

	public void writeProperties(PropertiesFile props) {
		if (jNoStretch.isSelected() != props.getBooleanProperty(SLIDESHOW_NOSTRETCH)) {
			ImageUtils.purgeTemp();
			GalleryRemote._().getCore().flushMemory();
		}

		props.setIntProperty(SLIDESHOW_PROGRESS, ((LocationItem) jProgress.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_CAPTION, ((LocationItem) jCaption.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_EXTRA, ((LocationItem) jExtra.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_URL, ((LocationItem) jUrl.getSelectedItem()).id);

		props.setBooleanProperty(SLIDESHOW_LOWREZ, jLowRez.isSelected());
		props.setBooleanProperty(SLIDESHOW_RANDOM, jRandom.isSelected());
		props.setBooleanProperty(SLIDESHOW_NOSTRETCH, jNoStretch.isSelected());
		props.setBooleanProperty(SLIDESHOW_PRELOADALL, jPreloadAll.isSelected());
		props.setBooleanProperty(SLIDESHOW_LOOP, jLoop.isSelected());
		int delay;
		try {
			delay = (int) Float.parseFloat(jDelay.getText());
		} catch (NumberFormatException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
			delay = 7;
		}
		props.setIntProperty(SLIDESHOW_DELAY, delay);

		if (jOverride.isSelected()) {
			props.setColorProperty(SLIDESHOW_COLOR, jBackgroundColor.getSelectedColor());
		} else {
			props.remove(SLIDESHOW_COLOR);
		}
		GalleryRemoteCore core = GalleryRemote._().getCore();
		if (!GalleryRemote._().isAppletMode()) {
			((MainFrame) core).previewFrame.repaint();
		}
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

		progressionPanel.setLayout(new GridBagLayout());
		progressionPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "progressionTitle")));
		locationPanel.setLayout(new GridBagLayout());
		locationPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "locationTitle")));
		apperancePanel.setLayout(new GridBagLayout());
		apperancePanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "appearanceTitle")));

		delay.setText(GRI18n.getString(MODULE, "delay"));
		delay.setLabelFor(jDelay);
		delay.setToolTipText(GRI18n.getString(MODULE, "delayHelp"));
		help.setText(GRI18n.getString(MODULE, "delayDesc"));
		jRandom.setText(GRI18n.getString(MODULE, "random"));
		jRandom.setToolTipText(GRI18n.getString(MODULE, "randomHelp"));
		jNoStretch.setText(GRI18n.getString(MODULE, "noStretch"));
		jNoStretch.setToolTipText(GRI18n.getString(MODULE, "noStretchHelp"));
		jPreloadAll.setText(GRI18n.getString(MODULE, "preloadAll"));
		jPreloadAll.setToolTipText(GRI18n.getString(MODULE, "preloadAllHelp"));
		jLoop.setText(GRI18n.getString(MODULE, "loop"));
		jLoop.setToolTipText(GRI18n.getString(MODULE, "loopHelp"));

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
		jOverride.setText(GRI18n.getString(MODULE, "backgroundColor"));

		this.add(progressionPanel,    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(locationPanel,     new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(apperancePanel,     new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(help,   new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
		this.add(spacerPanel,    new GridBagConstraints(0, GridBagConstraints.REMAINDER, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		progressionPanel.add(delay,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		progressionPanel.add(jDelay,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(jRandom,   new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(jPreloadAll,   new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(jLoop,   new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		apperancePanel.add(jLowRez,  new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		apperancePanel.add(jNoStretch,  new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		apperancePanel.add(jOverride,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		apperancePanel.add(jBackgroundColor,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));

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

