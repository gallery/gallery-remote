package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ColorWellButton;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.GalleryRemoteCore;
import com.gallery.GalleryRemote.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.util.Hashtable;
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
	JSlider jDelay = new JSlider();
	JLabel transition = new JLabel();
	JSlider jTransition = new JSlider();
	JLabel help = new JLabel();
	JLabel progress = new JLabel();
	JLabel caption = new JLabel();
	JLabel extra = new JLabel();
	JLabel url = new JLabel();
	JLabel album = new JLabel();
	JLabel summary = new JLabel();
	JLabel description = new JLabel();
	JCheckBox jOverride = new JCheckBox();
	JPanel apperancePanel = new JPanel();
	JComboBox jProgress;
	JComboBox jCaption;
	JComboBox jExtra;
	JComboBox jUrl;
	JComboBox jAlbum;
	JComboBox jSummary;
	JComboBox jDescription;
	JCheckBox jLowRez = new JCheckBox();
	JCheckBox jRandom = new JCheckBox();
	JCheckBox jNoStretch = new JCheckBox();
	JCheckBox jPreloadAll = new JCheckBox();
	JCheckBox jLoop = new JCheckBox();
	JPanel spacerPanel1 = new JPanel();
	ColorWellButton jBackgroundColor = new ColorWellButton(Color.red);
	JTabbedPane tabs = new JTabbedPane();

	public JLabel getIcon() {
		return icon;
	}

	public void readProperties(PropertiesFile props) {
		jProgress.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_PROGRESS)));
		jCaption.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_CAPTION)));
		jExtra.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_EXTRA)));
		jUrl.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_URL)));
		jAlbum.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_ALBUM)));
		jSummary.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_SUMMARY)));
		jDescription.setSelectedItem(new LocationItem(props.getIntProperty(SLIDESHOW_DESCRIPTION)));

		jLowRez.setSelected(props.getBooleanProperty(SLIDESHOW_LOWREZ));
		jRandom.setSelected(props.getBooleanProperty(SLIDESHOW_RANDOM));
		jNoStretch.setSelected(props.getBooleanProperty(SLIDESHOW_NOSTRETCH));
		jPreloadAll.setSelected(props.getBooleanProperty(SLIDESHOW_PRELOADALL));
		jLoop.setSelected(props.getBooleanProperty(SLIDESHOW_LOOP));
		jDelay.setValue(props.getIntProperty(SLIDESHOW_DELAY) * 1000);
		jTransition.setValue(props.getIntProperty(SLIDESHOW_TRANSITION_DURATION));
		Color color = props.getColorProperty(SLIDESHOW_COLOR);
		jOverride.setSelected(color != null);
		jBackgroundColor.setSelectedColor(color);

		jProgress.setEnabled(! props.isOverridden(SLIDESHOW_PROGRESS));
		jCaption.setEnabled(! props.isOverridden(SLIDESHOW_CAPTION));
		jExtra.setEnabled(! props.isOverridden(SLIDESHOW_EXTRA));
		jUrl.setEnabled(! props.isOverridden(SLIDESHOW_URL));
		jAlbum.setEnabled(! props.isOverridden(SLIDESHOW_ALBUM));
		jSummary.setEnabled(! props.isOverridden(SLIDESHOW_SUMMARY));
		jDescription.setEnabled(! props.isOverridden(SLIDESHOW_DESCRIPTION));
		jLowRez.setEnabled(! props.isOverridden(SLIDESHOW_LOWREZ));
		jRandom.setEnabled(! props.isOverridden(SLIDESHOW_RANDOM));
		jNoStretch.setEnabled(! props.isOverridden(SLIDESHOW_NOSTRETCH));
		jPreloadAll.setEnabled(! props.isOverridden(SLIDESHOW_PRELOADALL));
		jLoop.setEnabled(! props.isOverridden(SLIDESHOW_LOOP));
		jDelay.setEnabled(! props.isOverridden(SLIDESHOW_DELAY));
		jTransition.setEnabled(! props.isOverridden(SLIDESHOW_TRANSITION_DURATION));
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
		props.setIntProperty(SLIDESHOW_ALBUM, ((LocationItem) jAlbum.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_SUMMARY, ((LocationItem) jSummary.getSelectedItem()).id);
		props.setIntProperty(SLIDESHOW_DESCRIPTION, ((LocationItem) jDescription.getSelectedItem()).id);

		props.setBooleanProperty(SLIDESHOW_LOWREZ, jLowRez.isSelected());
		props.setBooleanProperty(SLIDESHOW_RANDOM, jRandom.isSelected());
		props.setBooleanProperty(SLIDESHOW_NOSTRETCH, jNoStretch.isSelected());
		props.setBooleanProperty(SLIDESHOW_PRELOADALL, jPreloadAll.isSelected());
		props.setBooleanProperty(SLIDESHOW_LOOP, jLoop.isSelected());
		props.setIntProperty(SLIDESHOW_DELAY, jDelay.getValue() / 1000);
		props.setIntProperty(SLIDESHOW_TRANSITION_DURATION, jTransition.getValue());

		if (jOverride.isSelected()) {
			props.setColorProperty(SLIDESHOW_COLOR, jBackgroundColor.getSelectedColor());
		} else {
			props.setProperty(SLIDESHOW_COLOR, null);
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
		jAlbum = new JComboBox(locationItems);
		jSummary = new JComboBox(locationItems);
		jDescription = new JComboBox(locationItems);

		setLayout(new GridBagLayout());

		progressionPanel.setLayout(new GridBagLayout());
		/*progressionPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "progressionTitle")));*/
		locationPanel.setLayout(new GridBagLayout());
		locationPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "locationTitle")));
		apperancePanel.setLayout(new GridBagLayout());
		/*apperancePanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
						GRI18n.getString(MODULE, "appearanceTitle")));*/

		delay.setText(GRI18n.getString(MODULE, "delay"));
		delay.setLabelFor(jDelay);
		delay.setToolTipText(GRI18n.getString(MODULE, "delayHelp"));
		jDelay.setMinimum(0);
		jDelay.setMaximum(30000);
		Hashtable ticks = new Hashtable(4);
		ticks.put(new Integer(0), new JLabel(GRI18n.getString(MODULE, "delayNone")));
		ticks.put(new Integer(5000), new JLabel("5s"));
		ticks.put(new Integer(10000), new JLabel("10s"));
		ticks.put(new Integer(30000), new JLabel("30s"));
		jDelay.setLabelTable(ticks);
		jDelay.setPaintLabels(true);
		jDelay.setMajorTickSpacing(5000);
		jDelay.setPaintTicks(true);

		transition.setText(GRI18n.getString(MODULE, "transition"));
		transition.setLabelFor(jTransition);
		transition.setToolTipText(GRI18n.getString(MODULE, "transitionHelp"));
		jTransition.setMinimum(0);
		jTransition.setMaximum(5000);
		ticks = new Hashtable(4);
		ticks.put(new Integer(0), new JLabel(GRI18n.getString(MODULE, "transitionNone")));
		ticks.put(new Integer(1000), new JLabel("1s"));
		ticks.put(new Integer(2000), new JLabel("2s"));
		ticks.put(new Integer(5000), new JLabel("5s"));
		jTransition.setLabelTable(ticks);
		jTransition.setPaintLabels(true);
		jTransition.setMajorTickSpacing(1000);
		jTransition.setPaintTicks(true);

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
		album.setText(GRI18n.getString(MODULE, "album"));
		album.setLabelFor(jAlbum);
		album.setToolTipText(GRI18n.getString(MODULE, "albumHelp"));
		summary.setText(GRI18n.getString(MODULE, "summary"));
		summary.setLabelFor(jSummary);
		summary.setToolTipText(GRI18n.getString(MODULE, "summaryHelp"));
		description.setText(GRI18n.getString(MODULE, "description"));
		summary.setLabelFor(jDescription);
		summary.setToolTipText(GRI18n.getString(MODULE, "descriptionHelp"));

		jLowRez.setText(GRI18n.getString(MODULE, "lowRez"));
		jLowRez.setToolTipText(GRI18n.getString(MODULE, "lowRezHelp"));
		jOverride.setText(GRI18n.getString(MODULE, "backgroundColor"));

		this.add(tabs,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		tabs.add(GRI18n.getString(MODULE, "progressionTitle"), progressionPanel);
		tabs.add(GRI18n.getString(MODULE, "appearanceTitle"), apperancePanel);

		/*this.add(progressionPanel,    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(locationPanel,     new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(apperancePanel,     new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));*/
		this.add(help,   new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(spacerPanel,    new GridBagConstraints(0, GridBagConstraints.REMAINDER, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		progressionPanel.add(delay,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		progressionPanel.add(jDelay,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(transition,   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		progressionPanel.add(jTransition,   new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(jRandom,   new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(jPreloadAll,   new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(jLoop,   new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		progressionPanel.add(new JPanel(),   new GridBagConstraints(0, 5, 2, 1, 0.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		apperancePanel.add(jLowRez,  new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		apperancePanel.add(jNoStretch,  new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		apperancePanel.add(jOverride,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		apperancePanel.add(jBackgroundColor,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		apperancePanel.add(locationPanel,  new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
		apperancePanel.add(new JPanel(),   new GridBagConstraints(0, 4, 2, 1, 0.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		int row = 0;
		locationPanel.add(progress,     new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jProgress,       new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(caption,     new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jCaption,      new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(extra,    new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jExtra,    new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(summary,   new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jSummary,    new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(description,   new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jDescription,    new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(url,   new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jUrl,    new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		locationPanel.add(album,   new GridBagConstraints(0, row, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		locationPanel.add(jAlbum,    new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0
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
			return o instanceof LocationItem && id == ((LocationItem) o).id;
		}
	}
}

