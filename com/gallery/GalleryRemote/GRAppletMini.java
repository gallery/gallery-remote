package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.prefs.PreferenceNames;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.TitledBorder;
import java.io.File;
import java.util.Iterator;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.applet.Applet;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Oct 30, 2003
 */
public class GRAppletMini extends GRApplet implements GalleryRemoteCore, ActionListener,
		DocumentListener, ListSelectionListener, PreferenceNames {
	public static final String MODULE = "AppletMini";

	JButton jUpload;
	JButton jAdd;
	StatusBar jStatusBar;
	JScrollPane jScrollPane;
	DroppableList jPicturesList;
	//JPanel jContentPanel;
	JCheckBox jResize;
	JPanel jInspector;
	JLabel captionLabel;
	JTextArea jCaption;
	JSplitPane jDivider;

	DefaultComboBoxModel galleries = null;
	Album album = null;
	Gallery gallery = null;
	boolean inProgress = false;
	boolean hasHadPictures = false;
	Method call;
	Object window;

	public GRAppletMini() {
		coreClass = "com.gallery.GalleryRemote.GalleryRemoteMini";
	}

	public void initUI() {
		// update the look and feel
		SwingUtilities.updateComponentTreeUI(this);
		if (GalleryRemote.IS_MAC_OS_X) {
			// the default font for many components is too big on Mac
			UIManager.put("Label.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("TextField.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("Button.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("CheckBox.font", UIManager.getFont("TitledBorder.font"));
			UIManager.put("ComboBox.font", UIManager.getFont("TitledBorder.font"));
		}

		jbInit();
	}

	public void startup() {
		galleries = new DefaultComboBoxModel();
		AppletInfo info = getGRAppletInfo();

		gallery = info.gallery;

		galleries.addElement(gallery);

		ImageUtils.deferredTasks();

		album = new Album(gallery);
		//album.setSuppressEvents(true);
		album.setName(info.albumName);
		gallery.createRootAlbum().add(album);

		jPicturesList.setModel(album);
		jPicturesList.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		jPicturesList.setInputMap(JComponent.WHEN_FOCUSED, null);
		jPicturesList.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		jPicturesList.setCellRenderer(new CoreUtils.FileCellRenderer());

		jResize.setSelected(GalleryRemote._().properties.getBooleanProperty(RESIZE_BEFORE_UPLOAD));

		//jStatusBar.setStatus(GRI18n.getString(MODULE, "DefMessage"));
		jStatusBar.setStatus(GRI18n.getString("MainFrame", "selPicToAdd"));
	}

	public void shutdown() {
		if (hasStarted && GalleryRemote._() != null) {
			// this is also executed from GRAppletSlideshow
			if (jDivider != null) {
				GalleryRemote._().properties.setIntProperty(APPLET_DIVIDER_LOCATION, jDivider.getDividerLocation());
			}

			GalleryRemote._().properties.write();
			GalleryRemote.shutdownInstance();
		}
	}

	public void shutdown(boolean shutdownOs) {
		shutdown();
	}

	public void flushMemory() {}

	public void preloadThumbnails(Iterator pictures) {}

	public ImageIcon getThumbnail(Picture p) {
		return null;
	}

	public StatusUpdate getMainStatusUpdate() {
		return jStatusBar;
	}

	public DefaultComboBoxModel getGalleries() {
		return galleries;
	}

	public void thumbnailLoadedNotify() {}

	public void setInProgress(boolean inProgress) {
		jUpload.setEnabled(!inProgress);
		jAdd.setEnabled(!inProgress);
		jPicturesList.setEnabled(!inProgress);
		jCaption.setEnabled(!inProgress);
		jResize.setEnabled(!inProgress);

		this.inProgress = inProgress;

		if (! inProgress && hasHadPictures) {
			// probably finished uploading...
			try {
				// no update for G2 and for embedded applets (non-embedded applets are TYPE_STANDALONE)
				if (! (gallery.getComm(null) instanceof GalleryComm2_5) && gallery.getType() != Gallery.TYPE_APPLET) {
					getAppletContext().showDocument(new URL(getCodeBase().toString() + "add_photos_refresh.php"), "hack");
				}

				// use Java to Javascript scripting
				g2Feedback("doneUploading", new Object[] {});
			} catch (MalformedURLException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}

			hasHadPictures = false;
		}
	}

	public void addPictures(File[] files, int index, boolean select) {
		album.addPictures(files, index);
	}

	public void addPictures(Picture[] pictures, int index, boolean select) {
		album.addPictures(Arrays.asList(pictures), index);
	}

	public Album getCurrentAlbum() {
		return album;
	}

	public JList getPicturesList() {
		return jPicturesList;
	}

	protected void jbInit() {
		jUpload = new JButton();
		jAdd = new JButton();
		jStatusBar = new StatusBar(75);
		jScrollPane = new JScrollPane();
		jPicturesList = new DroppableList();
		//jContentPanel = new JPanel(new GridBagLayout());
		jResize = new JCheckBox();
		jInspector = new JPanel(new GridBagLayout());
		captionLabel = new JLabel();
		jCaption = new JTextArea();
		jDivider = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		jScrollPane.setBorder(new TitledBorder(BorderFactory.createEmptyBorder(), GRI18n.getString(MODULE, "pictures")));

		jUpload.setText(GRI18n.getString(MODULE, "Upload"));
		jAdd.setText(GRI18n.getString(MODULE, "Add"));

		JPanel jButtonPanel = new JPanel();
		jButtonPanel.setLayout(new GridLayout(1, 2, 5, 0));
		jButtonPanel.add(jAdd);
		jButtonPanel.add(jUpload);

		jResize.setToolTipText("<html>Resize pictures before uploading them for a faster upload.<br>The " +
		"high-resolution original will not be available online.</html>");
		jResize.setText("Resize pictures before upload");
		captionLabel.setText("Caption:            ");
		jScrollPane.getViewport().add(jPicturesList, null);

		jCaption.setLineWrap(true);
		jCaption.setEditable(false);
		jCaption.setFont(UIManager.getFont("Label.font"));
		jCaption.setBackground(UIManager.getColor("TextField.inactiveBackground"));

		jDivider.setBorder(null);
		jDivider.setOneTouchExpandable(true);
		//jDivider.setResizeWeight(.75);
		jDivider.setDividerLocation(GalleryRemote._().properties.getIntProperty(APPLET_DIVIDER_LOCATION));

		jScrollPane.setMinimumSize(new Dimension(100, 0));
		jInspector.setMinimumSize(new Dimension(0, 0));
		jDivider.setLeftComponent(jScrollPane);
		jDivider.setRightComponent(jInspector);
//		jContentPanel.add(jScrollPane,           new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
//		jContentPanel.add(jInspector,   new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0
//				,GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

		jInspector.add(captionLabel,    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
		jInspector.add(jCaption,    new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

		this.getContentPane().setLayout(new GridBagLayout());
		this.getContentPane().add(jDivider,      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.getContentPane().add(jResize,     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
		this.getContentPane().add(jButtonPanel,         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		this.getContentPane().add(jStatusBar,      new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		jAdd.addActionListener(this);
		jUpload.addActionListener(this);
		jCaption.getDocument().addDocumentListener(this);
		jPicturesList.addListSelectionListener(this);
		jResize.addActionListener(this);

		jPicturesList.addKeyListener(
				new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						jListKeyPressed(e);
					}
				});

		Class jsobject = null;
		try {
			jsobject = Class.forName("netscape.javascript.JSObject");
			Method getWindow = jsobject.getMethod("getWindow", new Class[] {Applet.class});
			call = jsobject.getMethod("call", new Class[] {String.class, Object[].class});
			window = getWindow.invoke(null, new Object[] { this });
		} catch (ClassNotFoundException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (IllegalAccessException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (NoSuchMethodException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (InvocationTargetException e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}

	public void jListKeyPressed(KeyEvent e) {
		if (!inProgress) {
			int vKey = e.getKeyCode();
			Log.log(Log.LEVEL_TRACE, MODULE, "Key pressed: " + vKey);

			switch (vKey) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
					CoreUtils.deleteSelectedPictures();
					break;
				case KeyEvent.VK_LEFT:
					CoreUtils.movePicturesUp();
					break;
				case KeyEvent.VK_RIGHT:
					CoreUtils.movePicturesDown();
					break;
				case KeyEvent.VK_UP:
					CoreUtils.selectPrevPicture();
					break;
				case KeyEvent.VK_DOWN:
					CoreUtils.selectNextPicture();
					break;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == jAdd) {
			jStatusBar.setStatus(GRI18n.getString("MainFrame", "selPicToAdd"));
			File[] files = AddFileDialog.addFiles(this);

			if (files != null) {
				addPictures(files, -1, false);
				hasHadPictures = true;
			}
		} else if (source == jUpload) {
			g2Feedback("startingUpload", new Object[] {});
			gallery.doUploadFiles(new UploadProgress(DialogUtil.findParentWindow(this)) {
				public void doneUploading(String newItemName, Picture picture) {
					g2Feedback("uploadedOne", new Object[] {newItemName, picture.toString()});
				}
			});
		} else if (source == jResize) {
			GalleryRemote._().properties.setBooleanProperty(RESIZE_BEFORE_UPLOAD, jResize.isSelected());
		}
	}

	public void insertUpdate(DocumentEvent e) {
		textUpdate(e);
	}

	public void removeUpdate(DocumentEvent e) {
		textUpdate(e);
	}

	public void changedUpdate(DocumentEvent e) {
		textUpdate(e);
	}

	public void textUpdate(DocumentEvent e) {
		Picture p = (Picture) jPicturesList.getSelectedValue();

		if (p != null) {
			if (e.getDocument() == jCaption.getDocument()) {
				p.setCaption(jCaption.getText());
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		Picture p = (Picture) jPicturesList.getSelectedValue();

		if (p == null) {
			jCaption.setText("");
			jCaption.setEditable(false);
			jCaption.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		} else {
			jCaption.setText(p.getCaption());
			jCaption.setEditable(true);
			jCaption.setBackground(UIManager.getColor("TextField.background"));
		}
	}

	public void g2Feedback(String method, Object[] params) {
		//if (gallery.galleryVersion == 2) {
			try {
				Log.log(Log.LEVEL_TRACE, MODULE, "Invoking Javascript method '" + method + "' with " + params);
				call.invoke(window, new Object[] {method, params});
			} catch (IllegalAccessException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} catch (InvocationTargetException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		//}
	}
}
