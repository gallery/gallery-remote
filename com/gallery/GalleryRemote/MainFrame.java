/*
*  Gallery Remote - a File Upload Utility for Gallery
*
*  Gallery - a web based photo album viewer and editor
*  Copyright (C) 2000-2001 Bharat Mediratta
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or (at
*  your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*  General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.gallery.GalleryRemote;

// todo: save
//import JSX.ObjIn;
//import JSX.ObjOut;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.prefs.PreferencesDialog;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.URLPanel;
import com.gallery.GalleryRemote.prefs.PreferenceNames;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.OsShutdown;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.applet.Applet;

/**
 * Description of the Class
 *
 * @author jackodog
 * @author paour
 */
public class MainFrame extends JFrame
		implements ActionListener, ItemListener, ListSelectionListener,
		ListDataListener, TreeSelectionListener, TreeModelListener, FocusListener,
		GalleryRemoteCore, PreferenceNames {
	public static final String MODULE = "MainFrame";
	public static final String FILE_TYPE = ".grg";

	public PreviewFrame previewFrame = null;

	private static final String DIALOGTITLE = "Gallery Remote  --  ";

	/**
	 * This File is the last opened file or null if the user has not opened
	 * a file or has just pressed New.
	 */
	private File lastOpenedFile = null;

	/**
	 * This flag indicates whether the currently loaded file is dirty
	 * or clean.  Dirty means that the logical contents of the file have
	 * changed and we need to prevent the user from doing something that
	 * will lose their changes.  If we are clean then there is no possibility
	 * of losing changes.
	 */
	//private boolean m_isDirty = false;

	public DefaultComboBoxModel galleries = null;
	//private Gallery currentGallery = null;
	//private Album currentAlbum = null;
	private boolean inProgress = false;

	ThumbnailCache thumbnailCache = new ThumbnailCache();

	boolean running = true;

	public StatusBar jStatusBar = new StatusBar();

	PictureSelection ps = null;

	JPanel jTopPanel = new JPanel();
	JMenuBar jMenuBar1 = new JMenuBar();
	JLabel jLabel1 = new JLabel();
	JPanel jBottomPanel = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	JPanel jAlbumPanel = new JPanel();
	JScrollPane jAlbumScroll = new JScrollPane();
	public DroppableTree jAlbumTree = new DroppableTree();

	JComboBox jGalleryCombo = new JComboBox();
	JButton jNewGalleryButton = new JButton();
	JButton jLoginButton = new JButton();
	JSplitPane jInspectorDivider = new JSplitPane();
	JSplitPane jAlbumPictureDivider = new JSplitPane();
	JButton jUploadButton = new JButton();
	JButton jBrowseButton = new JButton();
	JButton jApertureImport = new JButton();
	JButton jSortButton = new JButton();
    JComboBox jSortCombo = new JComboBox();
	JButton jNewAlbumButton = new JButton();

	JMenu jMenuFile = new JMenu();
	// todo: save
	//JMenuItem jMenuItemNew = new JMenuItem();
	//JMenuItem jMenuItemOpen = new JMenuItem();
	//JMenuItem jMenuItemSave = new JMenuItem();
	//JMenuItem jMenuItemSaveAs = new JMenuItem();
	//JMenuItem jMenuItemClose = new JMenuItem();
	JMenuItem jMenuItemQuit = new JMenuItem();

	// todo: save
	// Create a Vector to store the MRU menu items.  They
	// are created dynamically below.
	//int m_MRUMenuIndex = 0;
	//Vector m_MRUFileList = new Vector();

	JMenu jMenuEdit = new JMenu();
	JMenuItem jMenuItemCut = new JMenuItem();
	JMenuItem jMenuItemCopy = new JMenuItem();
	JMenuItem jMenuItemPaste = new JMenuItem();

	JMenu jMenuOptions = new JMenu();
	JCheckBoxMenuItem jCheckBoxMenuThumbnails = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuPreview = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuPath = new JCheckBoxMenuItem();
	JMenuItem jMenuItemPrefs = new JMenuItem();
	JMenuItem jMenuItemClearCache = new JMenuItem();

	JMenu jMenuHelp = new JMenu();
	JMenuItem jMenuItemAbout = new JMenuItem();

	DroppableList jPicturesList = new DroppableList();
	JPanel jInspectorPanel = new JPanel();
	CardLayout jInspectorCardLayout = new CardLayout();
	PictureInspector jPictureInspector = new PictureInspector();
	AlbumInspector jAlbumInspector = new AlbumInspector();
	JScrollPane jPictureScroll = new JScrollPane();

	public Frame activating = null;

	public static Image iconImage = new ImageIcon(GalleryRemote.class.getResource("/rar_icon_16.gif")).getImage();

	public static final String CARD_PICTURE = "picture";
	public static final String CARD_ALBUM = "album";
	private File source;

	public void initMainFrame() {
		macOSXRegistration();

		PropertiesFile p = GalleryRemote._().properties;

		// load galleries
		galleries = new DefaultComboBoxModel();
		if (! GalleryRemote._().isAppletMode()) {
			int i = 0;
			while (true) {
				try {
					Gallery g = Gallery.readFromProperties(p, i++, jStatusBar);
					if (g == null) {
						break;
					}
					//g.addListDataListener(this);
					galleries.addElement(g);
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "Error trying to load Gallery profile " + i);
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
				}
			}
		} else {
			//Gallery g = new Gallery(jStatusBar);
			Applet applet = GalleryRemote._().getApplet();

			GRApplet.AppletInfo info = ((GRApplet) applet).getGRAppletInfo();

			info.gallery.addTreeModelListener(this);
			galleries.addElement(info.gallery);

			//CookieModule.addCookie(new Cookie(cookieName, cookieValue, cookieDomain, cookiePath, null, false));
		}

		setIconImage(iconImage);

		/*if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
		// Install shutdown handler only on Mac
		Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		shutdown(true);
		}
		});
		}*/
	}

	public void startup() {
		try {
			jbInit();
			jbInitEvents();
		} catch (Exception e) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
		}

		setBounds(GalleryRemote._().properties.getMainBounds());
		setJMenuBar(jMenuBar1);

		jGalleryCombo.setEditable(false);
		jGalleryCombo.setRenderer(new GalleryListRenderer());

		jPicturesList.setCellRenderer(new CoreUtils.FileCellRenderer());
		jPicturesList.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		jPicturesList.setInputMap(JComponent.WHEN_FOCUSED, null);
		jPicturesList.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, null);

		jAlbumTree.setMainFrame(this);
		jAlbumTree.setRootVisible(false);
		jAlbumTree.setScrollsOnExpand(true);
		jAlbumTree.setShowsRootHandles(true);
		jAlbumTree.setExpandsSelectedPaths(true);
		jAlbumTree.setEnabled(true);
		jAlbumTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		AlbumTreeRenderer albumTreeRenderer = new AlbumTreeRenderer();
		albumTreeRenderer.setLeafIcon(null);
		albumTreeRenderer.setOpenIcon(null);
		albumTreeRenderer.setClosedIcon(null);
		jAlbumTree.setCellRenderer(albumTreeRenderer);
		//((JLabel) jAlbumTree.getCellRenderer()).setPreferredSize(new Dimension(GalleryRemote._().properties.getIntProperty("albumPictureDividerLocation"), -1));
		ToolTipManager.sharedInstance().registerComponent(jAlbumTree);
		//jAlbumTree.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));

		jPictureInspector.setMainFrame(this);
		jAlbumInspector.setMainFrame(this);

		setGalleries(galleries);

		jInspectorDivider.setDividerLocation(GalleryRemote._().properties.getIntProperty("inspectorDividerLocation"));
		jAlbumPictureDivider.setDividerLocation(GalleryRemote._().properties.getIntProperty("albumPictureDividerLocation"));

		setVisible(true);

		previewFrame = new PreviewFrame();
		previewFrame.initComponents();
		previewFrame.addWindowListener(
				new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						jCheckBoxMenuPreview.setState(false);
					}
				});

		if (GalleryRemote._().properties.getShowPreview()) {
			previewFrame.setVisible(true);
		}

		toFront();

		readPreferences(GalleryRemote._().properties);

		if (GalleryRemote._().isAppletMode()) {
			/*Gallery g = getCurrentGallery();
			g.getComm(jStatusBar).isLoggedIn = true;*/

			fetchAlbums();
		}

		resetUIState();

		// todo: save
		// Load a state file
		/*if (GalleryRemote._().properties.getLoadLastMRU()) {
			String lastMRUFile = GalleryRemote._().properties.getMRUItem(1);

			if (null != lastMRUFile) {
				openState(lastMRUFile);
			}
		}*/

		//new UploadProgress();

		ImageUtils.deferredTasks();
	}

	private void setGalleries(DefaultComboBoxModel galleries) {
		this.galleries = galleries;

		jGalleryCombo.setModel(galleries);
		galleries.addListDataListener(this);

		boolean foundAutoLoad = false;
		for (int i = 0; i < galleries.getSize(); i++) {
			Gallery g = (Gallery) galleries.getElementAt(i);
			
			if (g.isAutoLoadOnStartup()) {
				// load the Gallery
				// login may have failed and caused getComm to be null.
				GalleryComm comm = g.getComm(jStatusBar);

				// may have tried to connect and failed
				if (comm != null && !GalleryComm.wasAuthFailure()) {
					fetchAlbums();
				}
				
				if (!foundAutoLoad) {
					jGalleryCombo.setSelectedIndex(i);
					foundAutoLoad = true;
				}
			}
		}
		
		selectedGalleryChanged();

		// We've been initalized, we are now clean.
		//setDirtyFlag(false);
	}


	/**
	 * Close the window when the close box is clicked
	 *
	 * @param e Event
	 */
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		shutdown(false, false);
	}

	public void shutdown() {
		shutdown(false, false);
	}

	public void shutdown(boolean shutdownOs) {
		shutdown(false, shutdownOs);
	}

	private void shutdown(boolean halt, boolean shutdownOs) {
		// check that we don't have galleries with data
		if (!shutdownOs) {
			// todo: save
			if (isDirty()) {
				int result = JOptionPane.showConfirmDialog(
						(JFrame) this,
						GRI18n.getString(MODULE, "quitQuestion"),
						"Warning",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
					return;
				}
			}
		}

		if (running) {
			running = false;

			Log.log(Log.LEVEL_INFO, MODULE, "Shutting down GR");

			try {
				PropertiesFile p = GalleryRemote._().properties;

				p.setMainBounds(getBounds());
				p.setPreviewBounds(previewFrame.getBounds());
				p.setIntProperty("inspectorDividerLocation", jInspectorDivider.getDividerLocation());
				p.setIntProperty("albumPictureDividerLocation", jAlbumPictureDivider.getDividerLocation());

				p.write();

				if (!halt) {
					// in halt mode, this crashes the VM
					setVisible(false);
					dispose();

					previewFrame.setVisible(false);
					previewFrame.dispose();
				}

				ImageUtils.purgeTemp();
			} catch (Throwable t) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Error while closing: " + t);
			}

			if (shutdownOs) {
				OsShutdown.shutdown();
			}

			Log.log(Log.LEVEL_INFO, MODULE, "Shutting down log");
			Log.shutdown();

			if (! GalleryRemote._().isAppletMode()) {
				if (!halt) {
					// no need for this in halt mode
					Runtime.getRuntime().exit(0);
				}/* else {
				Runtime.getRuntime().exit(0);
				}*/
			} else {
				((GRApplet) GalleryRemote._().getApplet()).hasShutdown();
				GalleryRemote.shutdownInstance();
			}
		}
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;

		resetUIState();
	}

	/*
	 * <p>This method updates the dirty flag and also causes the UI
	 * to be updated to reflect the new state.
	 *
	 * @param newDirtyState the new state (true means the document is dirty).

	/*private void setDirtyFlag(boolean newDirtyState) {
		m_isDirty = newDirtyState;
		resetUIState();
	}*/

	public boolean isDirty() {
		for (int i = galleries.getSize() - 1; i >= 0; i--) {
			Gallery g = (Gallery) galleries.getElementAt(i);
			if (g.isDirty()) {
				return true;
			}
		}

		return false;
	}

	void resetUIState() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Album currentAlbum = getCurrentAlbum();
				Gallery currentGallery = getCurrentGallery();

				// if the list is empty or comm, disable upload
				boolean uploadEnabled = currentAlbum != null
										&& currentAlbum.sizePictures() > 0
										&& !inProgress
										&& jAlbumTree.getSelectionCount() > 0;
				jUploadButton.setEnabled(uploadEnabled);
				jSortCombo.setEnabled(uploadEnabled);
				jSortButton.setEnabled(uploadEnabled);

				// todo: save
				/*if (null == lastOpenedFile) {
					setTitle(DIALOGTITLE
							+ GRI18n.getString(MODULE, "noTitleHeader")
							+ (m_isDirty ? "*" : ""));
				} else {
					setTitle(DIALOGTITLE
							+ lastOpenedFile.getName()
							+ (m_isDirty ? "*" : ""));
				}*/
				setTitle("Gallery Remote");

				// during comm, don't change Gallery or do any other comm
				jLoginButton.setEnabled(!inProgress && currentGallery != null);
				jGalleryCombo.setEnabled(!inProgress);
				jNewGalleryButton.setEnabled(!inProgress);

				// todo: save
				/*// Disable New, Open, and Close
				//jMenuItemNew.setEnabled(!inProgress);
				jMenuItemOpen.setEnabled(!inProgress);
				jMenuItemSave.setEnabled(!inProgress);
				jMenuItemSaveAs.setEnabled(!inProgress);
				//jMenuItemClose.setEnabled(!inProgress
				//		&& null != lastOpenedFile);

				// in the event the library we use to save is missing, dim the menus
				try {
					new JSX.ObjOut();
				} catch (Throwable t) {
					jMenuItemOpen.setEnabled(false);
					jMenuItemSave.setEnabled(false);
					jMenuItemSaveAs.setEnabled(false);
				}*/
Log.log(Log.LEVEL_TRACE, currentGallery + " - " + currentGallery.getUsername() + " - " + currentGallery.hasComm() + " - " + currentGallery.getComm(jStatusBar).isLoggedIn());
				if (currentGallery != null
						&& currentGallery.getUsername() != null
						&& currentGallery.hasComm()
						&& currentGallery.getComm(jStatusBar).isLoggedIn()) {
					jLoginButton.setText(GRI18n.getString(MODULE, "Log_out"));
				} else {
					jLoginButton.setText(GRI18n.getString(MODULE, "Log_in"));
				}

				jAlbumTree.setEnabled(!inProgress && jAlbumTree.getModel().getRoot() != null
						&& jAlbumTree.getModel().getChildCount(jAlbumTree.getModel().getRoot()) >= 1);

				// if the selected album is uploading, disable everything
				boolean enabled = !inProgress && currentAlbum != null
						&& jAlbumTree.getModel().getChildCount(jAlbumTree.getModel().getRoot()) >= 1;
				jBrowseButton.setEnabled(enabled && currentAlbum.getCanAdd());
				jApertureImport.setEnabled(enabled && currentAlbum.getCanAdd());
				jPictureInspector.setEnabled(enabled);
				jPicturesList.setEnabled(enabled && currentAlbum.getCanAdd());
				jNewAlbumButton.setEnabled(!inProgress && currentGallery != null && currentGallery.hasComm()
						&& currentGallery.getComm(jStatusBar).isLoggedIn()
						&& currentGallery.getComm(jStatusBar).hasCapability(jStatusBar, GalleryCommCapabilities.CAPA_NEW_ALBUM)
						&& currentAlbum != null && currentAlbum.getCanCreateSubAlbum());

				// change image displayed
				int sel = jPicturesList.getSelectedIndex();
				if (currentAlbum != null && currentAlbum.getSize() < 1) {
					// if album was just emptied, it takes a while for the pictureList
					// to notice...
					// this is fixed by using invokeLater [apparently not anymore]
					sel = -1;
				}

				if (GalleryRemote._().properties.getShowPreview() && previewFrame != null) {
					if (sel != -1) {
						previewFrame.loader.preparePicture(currentAlbum.getPicture(sel), true, true);
					} else {
						previewFrame.loader.preparePicture(null, true, true);
					}

					if (!previewFrame.isVisible()) {
						previewFrame.setVisible(true);
					}
				}

				// status
				if (currentAlbum == null) {
					jPictureInspector.setPictures(null);

					jStatusBar.setStatus(GRI18n.getString(MODULE, "notLogged"));
				} else if (currentAlbum.sizePictures() > 0) {
					jPictureInspector.setPictures(jPicturesList.getSelectedValues());

					int selN = jPicturesList.getSelectedIndices().length;

					if (sel == -1) {
						Object[] params = {new Integer(currentAlbum.sizePictures()),
										   new Integer((int) (currentAlbum.getPictureFileSize() / 1024))};
						jStatusBar.setStatus(GRI18n.getString(MODULE, "statusBarNoSel", params));
					} else {
						Object[] params = {new Integer(selN),
										   GRI18n.getString(MODULE, (selN == 1) ? "oneSel" : "manySel"),
										   new Integer((int) Album.getObjectFileSize(jPicturesList.getSelectedValues()) / 1024)};

						jStatusBar.setStatus(GRI18n.getString(MODULE, "statusBarSel", params));
					}
				} else {
					jPictureInspector.setPictures(null);

					jStatusBar.setStatus(GRI18n.getString(MODULE, "noSelection"));
				}

				jAlbumInspector.setAlbum(currentAlbum);

				jAlbumTree.repaint();
			}
		});
	}


	private void selectedGalleryChanged() {
		Gallery currentGallery = getCurrentGallery();
		Log.log(Log.LEVEL_TRACE, MODULE, "updateGalleryParams: current gallery: " + currentGallery);

		if (currentGallery != null && jAlbumTree.getModel() != currentGallery) {
			jAlbumTree.setModel(currentGallery);
			//currentGallery.addListDataListener(this);
		} else {
			jAlbumTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		}

		if (currentGallery == null || currentGallery.getRoot() == null) {
			jAlbumTree.setEnabled(false);
			jPicturesList.setEnabled(false);
		} else {
			jAlbumTree.setEnabled(!inProgress);
		}

		updatePicturesList();
	}


	/*private void updateAlbumCombo() {
		Gallery currentGallery = getCurrentGallery();
		Log.log(Log.LEVEL_TRACE, MODULE, "updateAlbumCombo: current gallery: " + currentGallery);
	}*/


	private void updatePicturesList() {
		Album currentAlbum = getCurrentAlbum();
		Log.log(Log.LEVEL_TRACE, MODULE, "updatePicturesList: current album: " + currentAlbum);

		if (currentAlbum == null) {
			// fake empty album to clear the list
			jPicturesList.setModel(new Album(null));
		} else {
			if (jPicturesList.getModel() != currentAlbum) {
				jPicturesList.setModel(currentAlbum);
				currentAlbum.addListDataListener(this);
			}

			jPictureInspector.setPictures(null);
		}

		resetUIState();
	}


	/**
	 * Open a file selection dialog and load the corresponding files
	 */
	public void browseAddPictures() {
		jStatusBar.setStatus(GRI18n.getString(MODULE, "selPicToAdd"));
		File[] files = AddFileDialog.addFiles(this);

		if (files != null) {
			addPictures(files, false);
		}
	}

	public void importApertureSelection() {
		jStatusBar.startProgress(StatusUpdate.LEVEL_UNINTERUPTIBLE, 0, 100, GRI18n.getString(MODULE, "apertureStartImport"), true);
		jStatusBar.setInProgress(true);
		new Thread() {
			public void run() {
				ArrayList resultList = ImageUtils.importApertureSelection();
				if (resultList == null || resultList.size() == 0) {
					jStatusBar.stopProgress(StatusUpdate.LEVEL_UNINTERUPTIBLE, GRI18n.getString(MODULE, "apertureCancelImport"));
					jStatusBar.setInProgress(false);
					return;
				}

				ArrayList pictures = new ArrayList();

				Iterator i = resultList.iterator();
				while (i.hasNext()) {
					String line = (String) i.next();
					int j = line.indexOf('\t');
					if (j != -1) {
						String imagePath = line.substring(0, j);
						String caption = line.substring(j + 1);

						source = new File(imagePath);
						ImageUtils.addToDelete(source);
						Picture p = new Picture(getCurrentGallery(), source);
						p.setCaption(caption);
						pictures.add(p);
					}
				}

				getCurrentAlbum().addPictures(pictures);
				preloadThumbnails(pictures.iterator());

				jStatusBar.stopProgress(StatusUpdate.LEVEL_UNINTERUPTIBLE, GRI18n.getString(MODULE, "apertureDoneImport"));
				jStatusBar.setInProgress(false);
			}
		}.start();
	}

	public void addPictures(File[] files, boolean select) {
		addPictures(null, files, -1, select);
	}

	public void addPictures(Album album, File[] files, boolean select) {
		addPictures(album, files, -1, select);
	}

	public void addPictures(File[] files, int index, boolean select) {
		addPictures(null, files, index, select);
	}

	public void addPictures(Album album, File[] files, int index, boolean select) {
		if (album == null) {
			album = getCurrentAlbum();
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Adding " + files.length + " pictures to album " + album);

		ArrayList newPictures = null;
		if (index == -1) {
			newPictures = album.addPictures(files);
		} else {
			newPictures = album.addPictures(files, index);
		}

		preloadThumbnails(newPictures.iterator());

		if (select) {
			selectAddedPictures(files, index);
		}

		// We've been modified, we are now dirty.
		//setDirtyFlag(true);
	}

	public void addPictures(Picture[] pictures, boolean select) {
		addPictures(null, pictures, -1, select);
	}

	public void addPictures(Album album, Picture[] pictures, boolean select) {
		addPictures(album, pictures, -1, select);
	}

	public void addPictures(Picture[] pictures, int index, boolean select) {
		addPictures(null, pictures, index, select);
	}

	public void addPictures(Album album, Picture[] pictures, int index, boolean select) {
		if (album == null) {
			album = getCurrentAlbum();
		}

		if (index == -1) {
			album.addPictures(Arrays.asList(pictures));
		} else {
			album.addPictures(Arrays.asList(pictures), index);
		}

		if (select) {
			selectAddedPictures(pictures, index);
		}

		// We've been modified, we are now dirty.
		//setDirtyFlag(true);
	}

	private void selectAddedPictures(Object[] objects, int index) {
		int[] indices = new int[objects.length];

		if (index == -1) {
			index = getCurrentAlbum().getSize() - 1;
		}

		for (int i = 0; i < objects.length; i++) {
			indices[i] = index + i;
		}

		jPicturesList.setSelectedIndices(indices);
	}


	/**
	 * Upload the files
	 */
	public void uploadPictures() {
		Log.log(Log.LEVEL_INFO, MODULE, "uploadPictures starting");

		File f = lastOpenedFile;

		if (null == f) {
			// No open file, use the default
			f = getCurrentGallery().getGalleryDefaultFile();
		}

		// todo: save
		//saveState(f);

		getCurrentGallery().doUploadFiles(new UploadProgress(this));
	}


	/**
	 * Sort the files alphabetically
	 */
	public void sortPictures() {
		int sortType = GalleryRemote._().properties.getIntProperty(SORT_TYPE);
		switch (sortType) {
			case SORT_TYPE_FILENAME:
				getCurrentAlbum().sortPicturesAlphabetically();
				break;

			case SORT_TYPE_EXIF_CREATION:
				getCurrentAlbum().sortPicturesCreated();
				break;

			default:
				Log.log(Log.LEVEL_ERROR, MODULE, "Bad sort value: " + sortType);
				break;
		}

		// We've been modified, we are now dirty.
		//setDirtyFlag(true);
	}

	public void setSortType(int sortType) {
		GalleryRemote._().properties.setIntProperty(SORT_TYPE, sortType);

		jSortButton.setText(GRI18n.getString(MODULE, "sortBtnTxt",
				new Object[] {GRI18n.getString(MODULE, "sort." + sortType)}));
	}

	/**
	 * Fetch Albums from server and update UI
	 */
	public void fetchAlbums() {
		Log.log(Log.LEVEL_INFO, MODULE, "fetchAlbums starting");

		getCurrentGallery().doFetchAlbums(jStatusBar);

		//updateAlbumCombo();

		Object root = jAlbumTree.getModel().getRoot();
		if (root != null && jAlbumTree.getModel().getChildCount(root) > 0) {
			jAlbumTree.setSelectionPath(null);
		}
	}

	public void fetchAlbumImages() {
		Log.log(Log.LEVEL_INFO, MODULE, "fetchAlbumImages starting");

		getCurrentAlbum().fetchAlbumImages(jStatusBar, false, 0);
	}

	public void newAlbum() {
		NewAlbumDialog dialog = new NewAlbumDialog(this, getCurrentGallery(), getCurrentAlbum());
		//String newAlbumName = dialog.getNewAlbumName();

		Album newAlbum = dialog.getNewAlbum();
		//Album parentAlbum = dialog.getParentAlbum();

		if (newAlbum == null) {
			return;
		}

		String newAlbumName = getCurrentGallery().doNewAlbum(newAlbum, GalleryRemote._().getCore().getMainStatusUpdate());
		if (!newAlbumName.equals(newAlbum.getName())) {
			newAlbum.setName(newAlbumName);
		}
		
		newAlbum.fetchAlbumProperties(GalleryRemote._().getCore().getMainStatusUpdate());

		// todo: this is too drastic...
		getCurrentGallery().reload();

		Log.log(Log.LEVEL_TRACE, MODULE, "Album '" + newAlbum + "' created.");
		// there is probably a better way... this is needed to give the UI time to catch up
		// and load the combo up with the reloaded album list
		//new Thread() {
		//	public void run() {
		//		try {
		//			Thread.sleep(1000);
		//		} catch (InterruptedException e) {
		//			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		//		}

				//SwingUtilities.invokeLater(new Runnable() {
				//	public void run() {
				Log.log(Log.LEVEL_TRACE, MODULE, "Selecting " + newAlbum);

				TreePath path = new TreePath(newAlbum.getPath());//new TreePath(getCurrentGallery().getPathToRoot(newAlbum));
				//jAlbumTree.expandPath(path.getParentPath());
				// todo: this call doesn't seem to have any effect
				//jAlbumTree.makeVisible(path);

				jAlbumTree.scrollPathToVisible(path);
				jAlbumTree.setSelectionPath(path);

				//jAlbumTree.repaint();
				//	}
				//});
		//	}
		//}.transitionStart();

		// We've been modified, we are now dirty.
		//setDirtyFlag(true);
	}

	public void slideshow() {
		SlideshowFrame sf = new SlideshowFrame();
		sf.showSlideshow();
		sf.start(getCurrentAlbum().getPicturesList());
	}


	/**
	 * Show/hide thumbnails
	 *
	 * @param show The new showThumbmails value
	 */
	public void setShowThumbnails(boolean show) {
		GalleryRemote._().properties.setShowThumbnails(show);

		if (show) {
			if (getCurrentAlbum() != null) {
				preloadThumbnails(getCurrentAlbum().getPictures());
			}

			jPicturesList.setFixedCellHeight(GalleryRemote._().properties.getThumbnailSize().height + 4);
		} else {
			thumbnailCache.cancelLoad();
			jPicturesList.setFixedCellHeight(-1);
		}
	}


	/**
	 * Show/hide preview
	 *
	 * @param show The new showPreview value
	 */
	public void setShowPreview(boolean show) {
		GalleryRemote._().properties.setShowPreview(show);
		if (show) {
			previewFrame.show();
			previewFrame.loader.preparePicture((Picture) jPicturesList.getSelectedValue(), true, true);
		} else {
			previewFrame.hide();
		}
	}


	/**
	 * Get a thumbnail from the thumbnail cache
	 *
	 * @param p picture whose thumbnail is to be fetched
	 * @return The thumbnail value
	 */
	public Image getThumbnail(Picture p) {
		if (p == null) {
			return null;
		}

		Image thumb = thumbnailCache.getThumbnail(p);

		if (thumb == null) {
			thumb = ImageUtils.defaultThumbnail;
		} else {
			thumb = ImageUtils.rotateImage(thumb, p.getAngle(), p.isFlipped(), getGlassPane());
		}

		return thumb;
	}


	/**
	 * Callback from thumbnail cache to notify that a new one has been loaded
	 */
	public void thumbnailLoadedNotify() {
		jPicturesList.repaint();
	}


	/**
	 * Show About Box
	 */
	public void showAboutBox() {
		try {
			AboutBox ab = new AboutBox(this);
			ab.setVisible(true);
		} catch (Exception err) {
			Log.logException(Log.LEVEL_ERROR, MODULE, err);
		}
	}

	public void doCut() {
		if (jPicturesList.isEnabled() && jPicturesList.getSelectedIndices().length > 0) {
			ps = new PictureSelection(jPicturesList, false);
			deleteSelectedPictures();

			// We've been modified, we are now dirty.
			//setDirtyFlag(true);
		}
	}

	public void doCopy() {
		if (jPicturesList.isEnabled() && jPicturesList.getSelectedIndices().length > 0) {
			ps = new PictureSelection(jPicturesList, true);
		}
	}

	public void doPaste() {
		if (jPicturesList.isEnabled() && ps != null && !ps.isEmpty()) {
			getCurrentAlbum().addPictures(ps, jPicturesList.getSelectedIndex());
		}

		// We've been modified, we are now dirty.
		//setDirtyFlag(true);
	}


	private void jbInit()
			throws Exception {//{{{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		jTopPanel.setLayout(new GridBagLayout());
		jBottomPanel.setLayout(gridLayout1);
		gridLayout1.setHgap(5);
		jAlbumPanel.setLayout(new BorderLayout());

		jLabel1.setText(GRI18n.getString(MODULE, "Gallery_URL"));

		jLoginButton.setText(GRI18n.getString(MODULE, "Log_in"));
		jLoginButton.setToolTipText(GRI18n.getString(MODULE, "loginButtonTip"));
		jLoginButton.setActionCommand("Fetch");
		jLoginButton.setIcon(GalleryRemote.iLogin);

		jNewAlbumButton.setText(GRI18n.getString(MODULE, "newAlbmBtnTxt"));
		jNewAlbumButton.setToolTipText(GRI18n.getString(MODULE, "newAlbmBtnTip"));
		jNewAlbumButton.setActionCommand("NewAlbum");
		jNewAlbumButton.setIcon(GalleryRemote.iNewAlbum);

		jUploadButton.setText(GRI18n.getString(MODULE, "upldBtnTxt"));
		jUploadButton.setActionCommand("Upload");
		jUploadButton.setToolTipText(GRI18n.getString(MODULE, "upldBtnTip"));
		jInspectorDivider.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jInspectorDivider.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "inspDvdr")));
		jInspectorDivider.setOneTouchExpandable(true);
		jInspectorDivider.setResizeWeight(.66);
		jAlbumPictureDivider.setOneTouchExpandable(true);
		jAlbumPictureDivider.setResizeWeight(.5);
		jTopPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), GRI18n.getString(MODULE, "panel1")));
		jBrowseButton.setText(GRI18n.getString(MODULE, "brwsBtnTxt"));
		jBrowseButton.setActionCommand("Browse");
		jBrowseButton.setToolTipText(GRI18n.getString(MODULE, "brwsBtnTip"));
		jApertureImport.setText(GRI18n.getString(MODULE, "apertureBtnTxt"));
		jApertureImport.setActionCommand("ApertureImport");
		jApertureImport.setToolTipText(GRI18n.getString(MODULE, "apertureBtnTip"));
		//jSortAlternativesButton.setText(GRI18n.getString(MODULE, "sortBtnTxt"));
		jSortCombo.setActionCommand("SortAlternative");
		jSortCombo.setToolTipText(GRI18n.getString(MODULE, "sortAlternativesBtnTip"));
		jSortButton.setActionCommand("Sort");
		jSortButton.setToolTipText(GRI18n.getString(MODULE, "sortBtnTip"));
		jSortButton.setText(GRI18n.getString(MODULE, "sortBtnTxt"));
        jGalleryCombo.setActionCommand("Url");
		jGalleryCombo.setToolTipText(GRI18n.getString(MODULE, "gllryCombo"));

		jMenuFile.setText(GRI18n.getString(MODULE, "menuFile"));

		/*jMenuItemNew.setText(GRI18n.getString(MODULE, "menuNew"));
		jMenuItemNew.setActionCommand("File.New");
		jMenuItemNew.setIcon(GalleryRemote.iNew);
		jMenuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, GalleryRemote.ACCELERATOR_MASK));*/

		// todo: save
		/*jMenuItemOpen.setText(GRI18n.getString(MODULE, "menuOpen"));
		jMenuItemOpen.setActionCommand("File.Open");
		jMenuItemOpen.setIcon(GalleryRemote.iOpen);
		jMenuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, GalleryRemote.ACCELERATOR_MASK));

		jMenuItemSave.setText(GRI18n.getString(MODULE, "menuSave"));
		jMenuItemSave.setActionCommand("File.Save");
		jMenuItemSave.setIcon(GalleryRemote.iSave);
		jMenuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, GalleryRemote.ACCELERATOR_MASK));

		jMenuItemSaveAs.setText(GRI18n.getString(MODULE, "menuSaveAs"));
		jMenuItemSaveAs.setActionCommand("File.SaveAs");

		// todo: saving disabled, since JSX doesn't work with new class structure
		// todo: save
		jMenuItemOpen.setEnabled(false);
		jMenuItemSave.setEnabled(false);
		jMenuItemSaveAs.setEnabled(false);*/

		/*jMenuItemClose.setText(GRI18n.getString(MODULE, "menuClose"));
		jMenuItemClose.setActionCommand("File.Close");
		if (GalleryRemote.IS_MAC_OS_X) {
			jMenuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, GalleryRemote.ACCELERATOR_MASK));
		} else {
			jMenuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, GalleryRemote.ACCELERATOR_MASK));
		}*/

		jMenuItemQuit.setText(GRI18n.getString(MODULE, "menuQuit"));
		jMenuItemQuit.setActionCommand("File.Quit");
		jMenuItemQuit.setIcon(GalleryRemote.iQuit);
		jMenuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, GalleryRemote.ACCELERATOR_MASK));

		jMenuEdit.setText(GRI18n.getString(MODULE, "menuEdit"));
		jMenuItemCut.setText(GRI18n.getString(MODULE, "menuCut"));
		jMenuItemCut.setActionCommand("Edit.Cut");
		jMenuItemCut.setIcon(GalleryRemote.iCut);
		jMenuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, GalleryRemote.ACCELERATOR_MASK));
		jMenuItemCopy.setText(GRI18n.getString(MODULE, "menuCopy"));
		jMenuItemCopy.setActionCommand("Edit.Copy");
		jMenuItemCopy.setIcon(GalleryRemote.iCopy);
		jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, GalleryRemote.ACCELERATOR_MASK));
		jMenuItemPaste.setText(GRI18n.getString(MODULE, "menuPaste"));
		jMenuItemPaste.setActionCommand("Edit.Paste");
		jMenuItemPaste.setIcon(GalleryRemote.iPaste);
		jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, GalleryRemote.ACCELERATOR_MASK));

		jMenuOptions.setText(GRI18n.getString(MODULE, "menuOptions"));
		jCheckBoxMenuThumbnails.setActionCommand("Options.Thumbnails");
		jCheckBoxMenuThumbnails.setText(GRI18n.getString(MODULE, "cbmenuThumb"));
		jCheckBoxMenuPreview.setActionCommand("Options.Preview");
		jCheckBoxMenuPreview.setText(GRI18n.getString(MODULE, "cbmenuPreview"));
		jCheckBoxMenuPath.setActionCommand("Options.Path");
		jCheckBoxMenuPath.setText(GRI18n.getString(MODULE, "cbmenuPath"));
		jMenuItemPrefs.setText(GRI18n.getString(MODULE, "menuPref"));
		jMenuItemPrefs.setActionCommand("Options.Prefs");
		jMenuItemPrefs.setIcon(GalleryRemote.iPreferences);
		jMenuItemClearCache.setText(GRI18n.getString(MODULE, "menuClearCache"));
		jMenuItemClearCache.setActionCommand("Options.ClearCache");

		jMenuHelp.setText(GRI18n.getString(MODULE, "menuHelp"));
		jMenuItemAbout.setActionCommand("Help.About");
		jMenuItemAbout.setText(GRI18n.getString(MODULE, "menuAbout"));
		jMenuItemAbout.setIcon(GalleryRemote.iAbout);

		jNewGalleryButton.setText(GRI18n.getString(MODULE, "newGalleryBtn"));
		jNewGalleryButton.setActionCommand("NewGallery");
		jNewGalleryButton.setIcon(GalleryRemote.iNewGallery);
		jInspectorPanel.setLayout(jInspectorCardLayout);
		jPictureScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jPictureScroll.setBorder(new TitledBorder(BorderFactory.createEmptyBorder(), GRI18n.getString(MODULE, "pictures")));
		jAlbumScroll.setBorder(new TitledBorder(BorderFactory.createEmptyBorder(), GRI18n.getString(MODULE, "albums")));
		jAlbumScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setupKeyboardHandling(jPictureScroll);
		setupKeyboardHandling(jAlbumScroll);

		if (! GalleryRemote._().isAppletMode()) {
			this.getContentPane().add(jTopPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
					, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		}
		jTopPanel.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		this.getContentPane().add(jInspectorDivider, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 2, 2, 2), 0, 0));
		jInspectorDivider.add(jInspectorPanel, JSplitPane.RIGHT);
		jInspectorDivider.add(jAlbumPictureDivider, JSplitPane.LEFT);
		JScrollPane pictureInspectorScroll = new JScrollPane(jPictureInspector);
		pictureInspectorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jInspectorPanel.add(pictureInspectorScroll, CARD_PICTURE);
		JScrollPane albumInspectorScroll = new JScrollPane(jAlbumInspector);
		albumInspectorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jInspectorPanel.add(albumInspectorScroll, CARD_ALBUM);
		jAlbumPictureDivider.add(jPictureScroll, JSplitPane.RIGHT);
		jAlbumPictureDivider.add(jAlbumPanel, JSplitPane.LEFT);
		jAlbumPanel.add(jAlbumScroll, BorderLayout.CENTER);
		jAlbumPanel.add(jNewAlbumButton, BorderLayout.SOUTH);
		jAlbumScroll.getViewport().add(jAlbumTree, null);
		jPictureScroll.getViewport().add(jPicturesList, null);
		this.getContentPane().add(jBottomPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		jBottomPanel.add(jBrowseButton, null);
		if (GalleryRemote.IS_MAC_OS_X) {
			jBottomPanel.add(jApertureImport, null);
		}
		JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		sortPanel.add(jSortButton);
		sortPanel.add(jSortCombo);
		jBottomPanel.add(sortPanel, null);
		jBottomPanel.add(jUploadButton, null);
		this.getContentPane().add(jStatusBar, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jTopPanel.add(jGalleryCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jTopPanel.add(jNewGalleryButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
		jTopPanel.add(jLoginButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));

        if (!GalleryRemote.IS_MAC_OS_X) {
            // nothing in File menu on the mac...
            jMenuBar1.add(jMenuFile);
        }
		jMenuBar1.add(jMenuEdit);
		jMenuBar1.add(jMenuOptions);
		if (!GalleryRemote.IS_MAC_OS_X) {
			jMenuBar1.add(jMenuHelp);
		}

		//jMenuFile.add(jMenuItemNew);
		// todo: save
		/*jMenuFile.add(jMenuItemOpen);
		jMenuFile.add(jMenuItemSave);
		jMenuFile.add(jMenuItemSaveAs);
		//jMenuFile.add(jMenuItemClose);

		jMenuFile.addSeparator();

		// Remember where we are so we can build the dynamic MRU list here.
		m_MRUMenuIndex = jMenuFile.getItemCount();
		updateMRUItemList();*/

		if (!GalleryRemote.IS_MAC_OS_X) {
			//jMenuFile.addSeparator();
			jMenuFile.add(jMenuItemQuit);

			jMenuHelp.add(jMenuItemAbout);
		}

		jMenuEdit.add(jMenuItemCut);
		jMenuEdit.add(jMenuItemCopy);
		jMenuEdit.add(jMenuItemPaste);

		jMenuOptions.add(jCheckBoxMenuThumbnails);
		jMenuOptions.add(jCheckBoxMenuPreview);
		jMenuOptions.add(jCheckBoxMenuPath);
		jMenuOptions.addSeparator();
		jMenuOptions.add(jMenuItemClearCache);

		if (!GalleryRemote.IS_MAC_OS_X) {
			jMenuOptions.addSeparator();
			jMenuOptions.add(jMenuItemPrefs);
		}

		setJMenuBar(jMenuBar1);

		// set up the sort types combo
		for (int i = 1; i <= SORT_TYPES; i++) {
			jSortCombo.addItem(new SortType(i, GRI18n.getString(MODULE, "sort." + i)));
		}

		jSortCombo.setSelectedIndex(GalleryRemote._().properties.getIntProperty(SORT_TYPE) - 1);
	}//}}}

	private void setupKeyboardHandling(JComponent c) {
		// todo: only suppress UP, DOWN, LEFT and RIGHT
		c.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
	}

	private void jbInitEvents() {
		jLoginButton.addActionListener(this);
		jNewAlbumButton.addActionListener(this);
		jUploadButton.addActionListener(this);
		jSortButton.addActionListener(this);
		jSortCombo.addActionListener(this);
		jBrowseButton.addActionListener(this);
		jApertureImport.addActionListener(this);
		jNewGalleryButton.addActionListener(this);
		//jGalleryCombo.addActionListener( this );
		jAlbumTree.addTreeSelectionListener(this);
		jMenuItemPrefs.addActionListener(this);
		jMenuItemClearCache.addActionListener(this);
		//jMenuItemNew.addActionListener(this);
		/*jMenuItemOpen.addActionListener(this);
		jMenuItemSave.addActionListener(this);
		jMenuItemSaveAs.addActionListener(this);*/
		//jMenuItemClose.addActionListener(this);
		jMenuItemQuit.addActionListener(this);
		jMenuItemAbout.addActionListener(this);
		jMenuItemCut.addActionListener(this);
		jMenuItemCopy.addActionListener(this);
		jMenuItemPaste.addActionListener(this);

		jCheckBoxMenuThumbnails.addItemListener(this);
		jCheckBoxMenuPreview.addItemListener(this);
		jCheckBoxMenuPath.addItemListener(this);

		jPicturesList.addListSelectionListener(this);

		addWindowListener(
				new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						thisWindowClosing(e);
					}

					public void windowActivated(java.awt.event.WindowEvent e) {
						if (activating == MainFrame.this) {
							activating = null;
							return;
						}

						if (activating == null && previewFrame != null && previewFrame.isVisible()) {
							/*WindowListener mfWindowListener = getWindowListeners()[0];
							WindowListener pWindowListener = previewFrame.getWindowListeners()[0];
							removeWindowListener(mfWindowListener);
							previewFrame.removeWindowListener(pWindowListener);*/

							activating = MainFrame.this;
							previewFrame.toFront();
							toFront();

							/*previewFrame.addWindowListener(pWindowListener);
							addWindowListener(mfWindowListener);*/
						}
					}
				});
		jPicturesList.addKeyListener(
				new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						jListKeyPressed(e);
					}
				});

		jPicturesList.addFocusListener(this);
		jAlbumTree.addFocusListener(this);
	}


    /**
     * If we are dirty then ask the user if we should save the data
     * before doing the dangerous thing we are about to do (new, open,
     * logout, etc.). If we are not dirty this method is a NOP. If the
     * user says they want to save, this method will save to the
     * currently open file before returning.
     *
     * @param promptMessageID The message ID (for GRI18n.getString) that
     *                        we should display to prompt the user
     *                        before we do the dangerous thing.
     *
     * @param gallery
	 * @return one of CANCEL_OPTION, OK_OPTION (from the JOptionPane
     *         class). On CANCEL_OPTION you should stop the current
     *         process. OK_OPTION means that either we are not dirty)
     *         or that we just saved for the user.
     */
	// todo: save
    /* private int saveOnPermission(String promptMessageID, Gallery gallery) {
        if (!m_isDirty) {
            return (JOptionPane.OK_OPTION);
        }

		String prompt;
		if (gallery != null) {
			prompt = GRI18n.getString(MODULE, promptMessageID, new Object[] {gallery.toString() });
		} else {
			prompt = GRI18n.getString(MODULE, promptMessageID);
		}
		
		int response = JOptionPane.showConfirmDialog(
                this,
				prompt,
                lastOpenedFile == null ? GRI18n.getString(MODULE, "noTitleHeader")
                                       : lastOpenedFile.getName(),
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (JOptionPane.YES_OPTION == response) {
            if (null == lastOpenedFile) {
                saveAsState();

                // If we are still dirty, they cancelled out of save as
                // so act as if they cancelled this dialog.
                if (m_isDirty) {
                    return (JOptionPane.CANCEL_OPTION);
                }
            } else {
                saveState();
            }

            return (JOptionPane.OK_OPTION);
        }

        if (JOptionPane.NO_OPTION == response) {
            // No action requested, tell the caller to go ahead
            return (JOptionPane.OK_OPTION);
        }

        // User reneges, stop the action
        return (JOptionPane.CANCEL_OPTION);
    }*/

	// Event handling
	/**
	 * Menu, button and field handling
	 *
	 * @param e Action event
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + command);
		//Log.log(Log.TRACE, MODULE, "        event " + e );

        // todo: save
		/*String MRUFileName = null;
		if (command.startsWith("File.MRU.")) {
			MRUFileName = command.substring("File.MRU.".length());
			command = "File.Open";
		}*/

		if (command.equals("File.Quit")) {
			thisWindowClosing(null);
		/*} else if (command.equals("File.New")) {
			int response = saveOnPermission("OK_toSaveBeforeClose");

			if (JOptionPane.CANCEL_OPTION == response) {
				return;
			}

			resetState();*/
			// todo: save
		/*} else if (command.equals("File.Open")) {
			openState(MRUFileName);
		} else if (command.equals("File.Save")) {
			// Do Save As if the file is the default
			if (lastOpenedFile == null) {
				saveAsState();
			} else {
				saveState();
			}
		} else if (command.equals("File.SaveAs")) {
			saveAsState();*/
		/*} else if (command.equals("File.Close")) {
			int response = saveOnPermission("OK_toSaveBeforeClose");

			if (JOptionPane.CANCEL_OPTION == response) {
				return;
			}

			resetState();*/
		} else if (command.equals("Edit.Cut")) {
			doCut();
		} else if (command.equals("Edit.Copy")) {
			doCopy();
		} else if (command.equals("Edit.Paste")) {
			doPaste();
		} else if (command.equals("Options.Prefs")) {
			showPreferencesDialog();
		} else if (command.equals("Options.ClearCache")) {
			ImageUtils.purgeTemp();
			flushMemory();
		} else if (command.equals("Help.About")) {
			showAboutBox();
		} else if (command.equals("Fetch")) {
			if (getCurrentGallery().hasComm()
					&& getCurrentGallery().getComm(jStatusBar).isLoggedIn()) {
				// todo: save
				// We're currently logged in, but we might be dirty
                // so ask the user if it's OK to log out.
				if (getCurrentGallery().isDirty()) {
					int result = JOptionPane.showConfirmDialog(
							(JFrame) this,
							GRI18n.getString(MODULE, "logoutQuestion", new Object[]{getCurrentGallery()}),
							"Warning",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
						return;
					}
				}

				getCurrentGallery().logOut();

				lastOpenedFile = null;

				resetUIState();

				// We've been logged out, we are now clean.
				//setDirtyFlag(false);
			} else {
				// login may have failed and caused getComm to be null.
				GalleryComm comm = getCurrentGallery().getComm(jStatusBar);

				// may have tried to connect and failed
				if (comm != null && !GalleryComm.wasAuthFailure()) {
					fetchAlbums();
				}
			}
		} else if (command.equals("NewAlbum")) {
			newAlbum();
		} else if (command.equals("Browse")) {
			browseAddPictures();
		} else if (command.equals("ApertureImport")) {
			importApertureSelection();
		} else if (command.equals("Upload")) {
			uploadPictures();
        } else if (command.equals("SortAlternative")) {
			int sortType = ((SortType) jSortCombo.getSelectedItem()).type;
			setSortType(sortType);
		} else if (command.equals("Sort")) {
			sortPictures();
		} else if (command.equals("NewGallery")) {
			showPreferencesDialog(URLPanel.class.getName());
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unhandled command " + command);
		}
	}

	public void showPreferencesDialog() {
		showPreferencesDialog(null);
	}

	private void showPreferencesDialog(String panel) {
		PropertiesFile oldProperties = (PropertiesFile) GalleryRemote._().properties.clone();
		PreferencesDialog pd = new PreferencesDialog(this);

		if (panel != null) {
			pd.setPanel(panel);
		}

		// modal dialog
		pd.setVisible(true);

		// user clicked OK
		if (pd.isOK()) {
			readPreferences(oldProperties);
		}
	}

	public void readPreferences(PropertiesFile op) {
		PropertiesFile p = GalleryRemote._().properties;
		p.write();

		jCheckBoxMenuThumbnails.setSelected(p.getShowThumbnails());
		jCheckBoxMenuPreview.setSelected(p.getShowPreview());
		jCheckBoxMenuPath.setSelected(p.getShowPath());

		previewFrame.setVisible(p.getShowPreview());

		setShowThumbnails(p.getShowThumbnails());

		if (!op.getThumbnailSize().equals(p.getThumbnailSize())) {
			thumbnailCache.reload();
		}
	}

	static FileFilter galleryFileFilter = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(FILE_TYPE);
		}

		public String getDescription() {
			return "GalleryRemote galleries";
		}
	};

	/**
	 * Reset the program to clean (unload any data associated with a
	 * file).  We assume that the UI portion has already asked the user
	 * if this is OK.
	 */
	// todo: save
	/*private void resetState() {
		getCurrentGallery().deleteAllPictures();

		lastOpenedFile = null;

		// We've been reset, we are now clean.
		setDirtyFlag(false);

		updateAlbumCombo();
		resetUIState();
	}


	// todo: save
	private void saveAsState() {
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(galleryFileFilter);

		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String name = fc.getSelectedFile().getPath();

			if (!name.endsWith(FILE_TYPE)) {
				name += FILE_TYPE;
			}

			// Remember the file
			lastOpenedFile = new File(name);

			resetUIState();

			saveState(lastOpenedFile);

			saveMRUItem(lastOpenedFile);
		}
	}

	// todo: save
	private void saveState() {
		if (null == lastOpenedFile) {
			Log.log(Log.LEVEL_ERROR, MODULE,
					"Trying to save with no file open");
			return;
		}

		saveState(lastOpenedFile);
	}


	 * This is an internal worker function to save the state to a file.
	 * Note that we specifically do *not* set m_isDirty = false in this
	 * method because we use this to temporarily backup the current state
	 * to the default file (and we want the user to remember that they should
	 * save the state to a "real" save file if they want to keep it).
	 *
	 * @param f the file to store the current dialog data.

	// todo: save
	private void saveState(File f) {
		try {
			Log.log(Log.LEVEL_INFO, MODULE,
					"Saving state to file " + f.getPath());

			Gallery[] galleryArray = new Gallery[galleries.getSize()];

			for (int i = 0; i < galleries.getSize(); i++) {
				galleryArray[i] = (Gallery) galleries.getElementAt(i);
			}

//			ObjOut out = new ObjOut(new BufferedWriter(new FileWriter(f)));
//			// todo: saving state is disabled because it's broken
//			out.writeObject(galleryArray);
//			out.close();

			// We've been saved, we are now clean.
			setDirtyFlag(false);

			Log.log(Log.LEVEL_TRACE, MODULE, "State saved");
//		} catch (IOException e) {
//			Log.log(Log.LEVEL_ERROR, MODULE, "Exception while trying to save state");
//			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (NoClassDefFoundError e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "JSX not installed, can't save state...");
		}
	}


	 * Reads the properties and constructs the current MRU menu list.

	// todo: save
	private void updateMRUItemList() {

		// First, delete all of the existing MRU values
		for (int i = 0; i < m_MRUFileList.size(); i++) {
			jMenuFile.remove((JMenuItem) m_MRUFileList.elementAt(i));

		}

		// Create the MRU list.  First we need to find out how manu
		// MRU items we are displaying.  We will store up to 20 MRU files
		// in the properties file, but we only display the top x files in the
		// menu.
		m_MRUFileList.clear();
		int mruCount = GalleryRemote._().properties.getMRUCountProperty();

		for (int i = 1; i <= 20 && m_MRUFileList.size() < mruCount; i++) {
			// Get the file name (if any) from the properties file
			String fileName = GalleryRemote._().properties.getMRUItem(i);

			if (null == fileName) {
				// If the MRU item doesn't exist, skip this one
				continue;
			}

			File mruFile = new File(fileName);

			if (!mruFile.isFile()) {
				// If the file has been deleted, skip it
				continue;
			}

			// OK, we now have a valid candidate, create the menu item
			int nextMenuItem = m_MRUFileList.size() + 1;
			String menuString = nextMenuItem + "  " + mruFile.getName();

			JMenuItem nextMRUItem = new JMenuItem();
			nextMRUItem.setText(menuString);
			nextMRUItem.setMnemonic('0' + nextMenuItem);
			nextMRUItem.setActionCommand("File.MRU." + fileName);
			nextMRUItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + nextMenuItem,
					ActionEvent.CTRL_MASK));

			// Remember this menu item.
			m_MRUFileList.addElement(nextMRUItem);
		}

		// Now insert the items into the menu after the insertion point
		// we saved off earlier and set the listeners.
		int nextInsertPoint = m_MRUMenuIndex;
		for (int i = 0; i < m_MRUFileList.size(); i++) {
			JMenuItem nextMRUItem = (JMenuItem) m_MRUFileList.elementAt(i);
			jMenuFile.insert(nextMRUItem, nextInsertPoint++);
			nextMRUItem.addActionListener(this);
		}
	}


	 * Save the passed mruFile and then write the properties file so that
	 * we don't lose the changes.  Then force the MRU menu to change.
	 *
	 * @param mruFile the file to add to the MRU list

	// todo: save
	/*private void saveMRUItem(File mruFile) {
		// Wait to here to see if we succeed in loading the file.
		GalleryRemote._().properties.addMRUItem(mruFile);

		// Save the properties file so we don't lose the MRU
		GalleryRemote._().properties.write();

		// Update the MRU list
		updateMRUItemList();
	}

	 * OpenState opens a file and loads it into GR.  If a file path is
	 * passed in, then that file is opened.  If null is passed in then a
	 * File Open dialog is displayed to allow the user to choose a file to
	 * open.
	 * <p/>
	 * Once a file has been loaded, this method has the side-effect of the
	 * file being added to the MRU list (or moved to the top of that list
	 * if it was already on it).
	 *
	 * @param fileToOpen The file to open (FQPN) or null if a File Open dialog
	 *                   should be used.

	// todo: save
	private void openState(String fileToOpen) {
		JFileChooser fc = null;
		if (null == fileToOpen) {
			fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(galleryFileFilter);

			int returnVal = fc.showOpenDialog(this);

			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}

			// Remember the file
			lastOpenedFile = fc.getSelectedFile();

			Log.log(Log.LEVEL_INFO, MODULE,
					"Opening state from file " + fc.getSelectedFile().getPath());

		} else {
			lastOpenedFile = new File(fileToOpen);

			Log.log(Log.LEVEL_INFO, MODULE,
					"Opening state from file " + fileToOpen);
		}

		// Before we change galleries, ask them if they want to save.
		int response = saveOnPermission("OK_toSaveBeforeClose", null);

		if (JOptionPane.CANCEL_OPTION == response) {
			return;
		}

		new Thread() {
			public void run() {
				try {
					getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					setInProgress(true);

					//ObjIn in = new ObjIn(new BufferedReader(new FileReader(lastOpenedFile)));
					//Gallery[] galleryArray = (Gallery[]) in.readObject();
					// todo: reading from file is disabled
					Gallery[] galleryArray = null;
					DefaultComboBoxModel newGalleries = new DefaultComboBoxModel();
					Gallery selectGallery = null;

					for (int i = 0; i < galleryArray.length; i++) {
						Gallery gallery = galleryArray[i];

						newGalleries.addElement(gallery);
						//galleryArray[i].checkTransients();
						//gallery.addListDataListener(MainFrame.this);

						ArrayList pictures = gallery.getAllPictures();
						preloadThumbnails(pictures.iterator());

						if (pictures.size() > 0) {
							gallery.doFetchAlbums(jStatusBar, false);

							if (selectGallery == null) {
								selectGallery = gallery;
							}
						}
					}

					saveMRUItem(lastOpenedFile);

					setGalleries(newGalleries);

					if (selectGallery == null && newGalleries.getSize() > 0) {
						selectGallery = (Gallery) newGalleries.getElementAt(0);
					}

					if (selectGallery != null) {
						jGalleryCombo.setSelectedItem(selectGallery);
					}

					setInProgress(false);
//				} catch (IOException e) {
//					Log.log(Log.LEVEL_ERROR, MODULE, "Exception while trying to read state");
//					Log.logException(Log.LEVEL_ERROR, MODULE, e);
//				} catch (ClassNotFoundException e) {
//					Log.log(Log.LEVEL_ERROR, MODULE, "Exception while trying to read state (probably a version mismatch)");
//					Log.logException(Log.LEVEL_ERROR, MODULE, e);
				} catch (NoClassDefFoundError e) {
					Log.log(Log.LEVEL_ERROR, MODULE, "JSX not installed, can't read state...");
				} finally {
					getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}.transitionStart();
	}*/

	public void removeGallery(Gallery g) {
		Log.log(Log.LEVEL_INFO, MODULE, "Deleting Gallery " + g);
		galleries.removeElement(g);

		//g.removeFromProperties(GalleryRemote.getInstance().properties);

		// tell all the galleries they've been moved...
		for (int i = 0; i < galleries.getSize(); i++) {
			Gallery gg = (Gallery) galleries.getElementAt(i);
			gg.setPrefsIndex(i);
			gg.writeToProperties(GalleryRemote._().properties);
		}

		Gallery.removeFromProperties(GalleryRemote._().properties, galleries.getSize());
	}


	/**
	 * CheckboxMenu handling
	 *
	 * @param e Description of Parameter
	 */
	public void itemStateChanged(ItemEvent e) {
		Object item = e.getItemSelectable();
		//Log.log(Log.TRACE, MODULE, "Item selected " + item);

		if (item == jCheckBoxMenuThumbnails) {
			setShowThumbnails(e.getStateChange() == ItemEvent.SELECTED);
		} else if (item == jCheckBoxMenuPreview) {
			setShowPreview(e.getStateChange() == ItemEvent.SELECTED);
		} else if (item == jCheckBoxMenuPath) {
			GalleryRemote._().properties.setShowPath((e.getStateChange() == ItemEvent.SELECTED) ? true : false);
			jPicturesList.repaint();
		} /*else if ( item == album ) {
		updatePicturesList( (Album) ( (JComboBox) item ).getSelectedItem());
		}*/
		else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unhandled item state change " + item);
		}
	}


	/**
	 * Implementation of the ListSelectionListener
	 *
	 * @param e ListSelection event
	 */
	public void valueChanged(ListSelectionEvent e) {
		//Log.log(Log.TRACE, MODULE, "List selection changed: " + e);

		int sel = jPicturesList.getSelectedIndex();

		if (sel != -1) {
			thumbnailCache.preloadThumbnailFirst(getCurrentAlbum().getPicture(sel));
		}

		resetUIState();

		jInspectorCardLayout.show(jInspectorPanel, CARD_PICTURE);
	}


	/**
	 * Called whenever the value of the selection changes.
	 *
	 * @param e the event that characterizes the change.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		//jAlbumTree.treeDidChange();
		updatePicturesList();

		jAlbumInspector.setAlbum(getCurrentAlbum());
		jInspectorCardLayout.show(jInspectorPanel, CARD_ALBUM);
	}

	public void albumChanged(Album a) {
		if (a == getCurrentAlbum()) {
			jAlbumInspector.setAlbum(a);
		}
	}

	/**
	 * Implementation of the ListDataListener
	 *
	 * @param e ListSelection event
	 */
	public void contentsChanged(ListDataEvent e) {
		Object source = e.getSource();
		Log.log(Log.LEVEL_TRACE, MODULE, "Contents changed: " + e);

		if (source instanceof Album) {
			// Also tell MainFrame (ugly, but works around bug in Swing where when
			// the list data changes (and nothing remains to be selected), no
			// selection change events are fired.
			updatePicturesList();
		//} else if (source instanceof Gallery) {
		//	updateAlbumCombo();
		} else if (source instanceof DefaultComboBoxModel) {
			selectedGalleryChanged();
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unknown source " + source);
		}
	}

	public void intervalAdded(ListDataEvent e) {
		contentsChanged(e);
	}

	public void intervalRemoved(ListDataEvent e) {
		contentsChanged(e);
	}

	/**
	 * Listen for key events
	 *
	 * @param e Key event
	 */
	public void jListKeyPressed(KeyEvent e) {
		if (!inProgress) {
			int vKey = e.getKeyCode();

			switch (vKey) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
					deleteSelectedPictures();
					break;
				case KeyEvent.VK_LEFT:
					movePicturesUp();
					break;
				case KeyEvent.VK_RIGHT:
					movePicturesDown();
					break;
				case KeyEvent.VK_UP:
					CoreUtils.selectPrevPicture();
					break;
				case KeyEvent.VK_DOWN:
					CoreUtils.selectNextPicture();
					break;
				default:
					return;
			}

			// We've been modified, we are now dirty.
			//setDirtyFlag(true);
		}
	}

	public Gallery getCurrentGallery() {
		Gallery gallery = (Gallery) jGalleryCombo.getSelectedItem();
		return gallery;
	}

	public Album getCurrentAlbum() {
		Object album = jAlbumTree.getLastSelectedPathComponent();

        if (!(album instanceof Album)) {
            return(null); // No second chances
        }

        // If the album is null, there is no item selected,
        // we can fix that.
		if (album == null) {
            // Try to select the first album
            jAlbumTree.setSelectionRow(0);
            album = jAlbumTree.getLastSelectedPathComponent();

            if (album == null || !(album instanceof Album)) {
    			return null;
            }
        }

    	return (Album) album;
	}

	private void macOSXRegistration() {
		if (GalleryRemote.IS_MAC_OS_X) {
			try {
				Class theMacOSXAdapter;
				theMacOSXAdapter = GalleryRemote.secureClassForName("com.gallery.GalleryRemote.util.MacOSXAdapter");

				Class[] defArgs = {JFrame.class, String.class, String.class, String.class};
				Method registerMethod = theMacOSXAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
				if (registerMethod != null) {
					Object[] args = {this, "showAboutBox", "shutdown", "showPreferencesDialog"};
					registerMethod.invoke(theMacOSXAdapter, args);
				}
			} catch (NoClassDefFoundError e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} catch (ClassNotFoundException e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			} catch (Exception e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}
	}

    /*
     * Delete the selected pictures (using the core utilities)
     * and mark the document as being dirty.
     */
    public void deleteSelectedPictures() {
		CoreUtils.deleteSelectedPictures();
		//setDirtyFlag(true);
    }

    /*
     * Move the selected picture (using the core utilities)
     * and mark the document as being dirty.
     */
    public void movePicturesUp() {
		CoreUtils.movePicturesUp();
		//setDirtyFlag(true);
    }

    /*
     * Move the selected picture (using the core utilities)
     * and mark the document as being dirty.
     */
    public void movePicturesDown() {
		CoreUtils.movePicturesDown();
		//setDirtyFlag(true);
    }

	public void flushMemory() {
		thumbnailCache.flushMemory();
		previewFrame.loader.flushMemory();

		for (int i = 0; i < galleries.getSize(); i++) {
			Gallery g = (Gallery) galleries.getElementAt(i);

			preloadThumbnails(g.getAllPictures().iterator());
		}
	}

	public void preloadThumbnails(Iterator pictures) {
		thumbnailCache.preloadThumbnails(pictures);
	}

	public StatusUpdate getMainStatusUpdate() {
		return jStatusBar;
	}

	public DefaultComboBoxModel getGalleries() {
		return galleries;
	}

	public JList getPicturesList() {
		return jPicturesList;
	}


	/**
	 * Invoked when a component gains the keyboard focus.
	 */
	public void focusGained(FocusEvent e) {
		if (e.getComponent() == jAlbumTree) {
			jInspectorCardLayout.show(jInspectorPanel, CARD_ALBUM);
		} else if (e.getComponent() == jPicturesList) {
			jInspectorCardLayout.show(jInspectorPanel, CARD_PICTURE);
		}
	}

	/**
	 * Invoked when a component loses the keyboard focus.
	 */
	public void focusLost(FocusEvent e) {
	}

	/*** TreeModelListener implementation ***/
	public void treeNodesChanged(TreeModelEvent e) {
		TreePath treePath = e.getTreePath();
		if (treePath != null && getCurrentGallery().getRoot() != treePath.getLastPathComponent()) {
			albumChanged((Album) treePath.getLastPathComponent());
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {
		//treeNodesChanged(e);
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		//treeNodesChanged(e);
	}

	public void treeStructureChanged(TreeModelEvent e) {
		//treeNodesChanged(e);
	}

	class AlbumTreeRenderer extends DefaultTreeCellRenderer {
		Album album = null;

		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			// Swing incorrectly passes selection state in some cases
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null && selectionPath.getLastPathComponent() == value) {
				sel = true;
			}

			super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);

			if (value instanceof Album) {
				album = (Album) value;
			} else {
				album = null;
			}

			Font font = getFont();
			if (font != null) {
				if (album != null && album.getSize() > 0) {
					setFont(font.deriveFont(Font.BOLD));
				} else {
					setFont(font.deriveFont(Font.PLAIN));
				}
			}

			if (album != null && album.isHasFetchedImages()) {
				setForeground(Color.green);
			} else {
				// setForeground(Color.black);
			}

			//setIcon(null);
			String name = getText();
			if (name != null) {
				name = name.trim();
			}

			setText(name);
			setToolTipText(name);

			return this;
		}

		public Dimension getPreferredSize() {
			Dimension retDimension = super.getPreferredSize();

			// account for the fact that albums with added pictures are drawn in bold
			if (retDimension != null) {
				retDimension = new Dimension((int) (retDimension.width * 1.5 + 15),
						retDimension.height);
			}
			
			return retDimension;
		}
	}

	class GalleryListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(
					list, value, index,
					isSelected, cellHasFocus);
			Gallery gallery = null;

			if (value instanceof Gallery && value != null) {
				gallery = (Gallery) value;
			}

			if (gallery != null && gallery.getRoot() != null) {
				Font font = getFont().deriveFont(Font.BOLD);
				setFont(font);
				//list.setFont(font);
			} else {
				Font font = getFont().deriveFont(Font.PLAIN);
				setFont(font);
				//list.setFont(font);
			}

			return this;
		}
	}

	class SortType {
		int type;
		String text;

		public SortType(int type, String text) {
			this.type = type;
			this.text = text;
		}

		public String toString() {
			return text;
		}
	}
}

