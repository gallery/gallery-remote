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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.text.ChoiceFormat;
import java.text.Format;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.ImageUtils;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.OsShutdown;
import com.gallery.GalleryRemote.prefs.PreferencesDialog;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import com.gallery.GalleryRemote.prefs.URLPanel;
import JSX.ObjOut;
import JSX.ObjIn;
import javax.swing.border.Border;

/**
 *  Description of the Class
 *
 *@author     jackodog
 *@author     paour
 *@created    August 16, 2002
 */
public class MainFrame extends javax.swing.JFrame
		implements ActionListener, ItemListener, ListSelectionListener,
		ListDataListener, TreeSelectionListener, FocusListener {
	public static final String MODULE = "MainFrame";
	public static final String FILE_TYPE = ".grg";

	PreviewFrame previewFrame = null;

    private static final String DIALOGTITLE = "Gallery Remote  --  ";

    /**
     * This File is the last opened file or null if the user has not opened
     * a file or has just pressed New.
     */
	private File m_lastOpenedFile = null;

    /**
     * This flag indicates whether the currently loaded file is dirty
     * or clean.  Dirty means that the logical contents of the file have
     * changed and we need to prevent the user from doing something that
     * will lose their changes.  If we are clean then there is no possibility
     * of losing changes.
     */
	private boolean m_isDirty = false;

	public DefaultComboBoxModel galleries = null;
	//private Gallery currentGallery = null;
	//private Album currentAlbum = null;
	private boolean inProgress = false;

	ThumbnailCache thumbnailCache = new ThumbnailCache( this );

	boolean running = true;

	public StatusBar jStatusBar = new StatusBar(this);

	PictureSelection ps = null;

	JPanel jTopPanel = new JPanel();
	JMenuBar jMenuBar1 = new JMenuBar();
	JLabel jLabel1 = new JLabel();
	JPanel jBottomPanel = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	JPanel jAlbumPanel = new JPanel();
	JScrollPane jAlbumScroll = new JScrollPane();
	DroppableTree jAlbumTree = new DroppableTree();

	JComboBox jGalleryCombo = new JComboBox();
	JButton jNewGalleryButton = new JButton();
	JButton jLoginButton = new JButton();
	JSplitPane jInspectorDivider = new JSplitPane();
	JSplitPane jAlbumPictureDivider = new JSplitPane();
	JButton jUploadButton = new JButton();
	JButton jBrowseButton = new JButton();
	JButton jSortButton = new JButton();
	JButton jNewAlbumButton = new JButton();

	JMenu jMenuFile = new JMenu();
	JMenuItem jMenuItemNew    = new JMenuItem();
	JMenuItem jMenuItemOpen = new JMenuItem();
	JMenuItem jMenuItemSave   = new JMenuItem();
	JMenuItem jMenuItemSaveAs = new JMenuItem();
	JMenuItem jMenuItemClose  = new JMenuItem();
	JMenuItem jMenuItemQuit   = new JMenuItem();

    // Create a Vector to store the MRU menu items.  They
    // are created dynamically below.
    int m_MRUMenuIndex = 0;
    Vector m_MRUFileList = new Vector();

	JMenu jMenuEdit = new JMenu();
	JMenuItem jMenuItemCut = new JMenuItem();
	JMenuItem jMenuItemCopy = new JMenuItem();
	JMenuItem jMenuItemPaste = new JMenuItem();

	JMenu jMenuOptions = new JMenu();
	JCheckBoxMenuItem jCheckBoxMenuThumbnails = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuPreview = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuPath = new JCheckBoxMenuItem();
	JMenuItem jMenuItemPrefs = new JMenuItem();
	JMenu jMenuHelp = new JMenu();
	JMenuItem jMenuItemAbout = new JMenuItem();

	DroppableList jPicturesList = new DroppableList();
	JPanel jInspectorPanel = new JPanel();
	CardLayout jInspectorCardLayout = new CardLayout();
	PictureInspector jPictureInspector = new PictureInspector();
	AlbumInspector jAlbumInspector = new AlbumInspector();
	JScrollPane jPictureScroll = new JScrollPane();

	public static ImageIcon iLogin;
	public static ImageIcon iNewGallery;
	public static ImageIcon iAbout;
	public static ImageIcon iSave;
	public static ImageIcon iOpen;
	public static ImageIcon iPreferences;
	public static ImageIcon iNewAlbum;
	public static ImageIcon iQuit;
	public static ImageIcon iCut;
	public static ImageIcon iCopy;
	public static ImageIcon iPaste;
	public static ImageIcon iUp;
	public static ImageIcon iDown;
	public static ImageIcon iDelete;
	public static ImageIcon iRight;
	public static ImageIcon iLeft;
	public static ImageIcon iFlip;
	public static ImageIcon iComputer;
	public static ImageIcon iUploading;
	public static Image iconImage = new ImageIcon(GalleryRemote.class.getResource( "/rar_icon_16.gif" )).getImage();

	public static boolean IS_MAC_OS_X = (System.getProperty("mrj.version") != null);

	public static final String CARD_PICTURE = "picture";
	public static final String CARD_ALBUM = "album";

	/**
	 *  Constructor for the MainFrame object
	 */
	public MainFrame() {
		macOSXRegistration();

		PropertiesFile p = GalleryRemote.getInstance().properties;

		// load galleries
		galleries = new DefaultComboBoxModel();
		int i = 0;
		while ( true ) {
			try {
				Gallery g = Gallery.readFromProperties(p, i++, jStatusBar);
				if (g == null) {
					break;
				}
				g.addListDataListener(this);
				galleries.addElement(g);
			} catch (Exception e) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Error trying to load Gallery profile " + i);
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
			}
		}

		setIconImage(MainFrame.iconImage);

		if ( System.getProperty("os.name").toLowerCase().startsWith("mac") ) {
			// Install shutdown handler only on Mac
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					shutdown(true);
				}
			});
		}
	}


	/**
	 *  Initialize the graphical components
	 *
	 *@exception  Exception  Description of Exception
	 */
	public void initComponents()
			throws Exception {

		try {
			jbInit();
			jbInitEvents();
		} catch ( Exception e ) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
		}

		setBounds( GalleryRemote.getInstance().properties.getMainBounds() );
		setJMenuBar( jMenuBar1 );

		jPicturesList.setMainFrame( this );
		jPicturesList.setCellRenderer( new FileCellRenderer() );
		jPicturesList.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		jPicturesList.setInputMap(JComponent.WHEN_FOCUSED, null);
		jPicturesList.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, null);

		jAlbumTree.setMainFrame( this );
		jAlbumTree.setRootVisible(false);
		jAlbumTree.setScrollsOnExpand(true);
		jAlbumTree.setShowsRootHandles(true);
		jAlbumTree.setExpandsSelectedPaths(true);
		jAlbumTree.setEnabled(true);
		jAlbumTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jAlbumTree.setCellRenderer( new AlbumTreeRenderer() );
		((JLabel) jAlbumTree.getCellRenderer()).setPreferredSize(new Dimension(GalleryRemote.getInstance().properties.getIntProperty( "albumPictureDividerLocation" ), -1));

		jPictureInspector.setMainFrame( this );
		jAlbumInspector.setMainFrame( this );

		setGalleries(galleries);

		jInspectorDivider.setDividerLocation( GalleryRemote.getInstance().properties.getIntProperty( "inspectorDividerLocation" ) );
		jAlbumPictureDivider.setDividerLocation( GalleryRemote.getInstance().properties.getIntProperty( "albumPictureDividerLocation" ) );

		setVisible( true );

		previewFrame = new PreviewFrame();
		previewFrame.initComponents();
		previewFrame.addWindowListener(
				new java.awt.event.WindowAdapter()	{
					public void windowClosing( java.awt.event.WindowEvent e ) {
						jCheckBoxMenuPreview.setState( false );
					}
				} );

		if ( GalleryRemote.getInstance().properties.getShowPreview() ) {
			previewFrame.setVisible( true );
		}

		toFront();

		readPreferences(GalleryRemote.getInstance().properties);

        resetUIState();

        // Load a test file
        if (GalleryRemote.getInstance().properties.getLoadLastMRU()) {
            String lastMRUFile = GalleryRemote.getInstance().properties.getMRUItem(1);

            if (null != lastMRUFile) {
                openState(lastMRUFile);
            }
        }

		//new UploadProgress();
	}

	private void setGalleries(DefaultComboBoxModel galleries) {
		this.galleries = galleries;

		jGalleryCombo.setModel( galleries );
		galleries.addListDataListener(this);
		updateGalleryParams();

        // We've been initalized, we are now clean.
        m_isDirty = false;
	}


	/**
	 *  Close the window when the close box is clicked
	 *
	 *@param  e  Event
	 */
	void thisWindowClosing( java.awt.event.WindowEvent e ) {
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
            if (null != m_lastOpenedFile || m_isDirty) {
				if (JOptionPane.showConfirmDialog(
				      (JFrame) this,
				      GRI18n.getString(MODULE, "quitQuestion"),
					  "Warning",
					  JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}

		if (running) {
			running = false;

			Log.log(Log.LEVEL_INFO, MODULE, "Shutting down GR");

			try {
				PropertiesFile p = GalleryRemote.getInstance().properties;

				p.setMainBounds( getBounds() );
				p.setPreviewBounds( previewFrame.getBounds() );
				p.setIntProperty( "inspectorDividerLocation", jInspectorDivider.getDividerLocation() );
				p.setIntProperty( "albumPictureDividerLocation", jAlbumPictureDivider.getDividerLocation() );

				p.write();

				if (!halt) {
					// in halt mode, this crashes the VM
					setVisible( false );
					dispose();
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

			if (!halt) {
				// no need for this in halt mode
				Runtime.getRuntime().exit(0);
			}/* else {
			Runtime.getRuntime().exit(0);
			}*/
		}
	}

	void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;

		resetUIState();
	}


	void resetUIState() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Album currentAlbum = getCurrentAlbum();
				Gallery currentGallery = getCurrentGallery();

				// if the list is empty or comm, disable upload
				jUploadButton.setEnabled( currentAlbum != null
						&& currentAlbum.sizePictures() > 0
						&& !inProgress
						&& jAlbumTree.getSelectionCount() > 0 );
				jSortButton.setEnabled(jUploadButton.isEnabled());

				if (null == m_lastOpenedFile) {
					setTitle(DIALOGTITLE
							+ GRI18n.getString(MODULE, "noTitleHeader")
							+ (m_isDirty ? "*" : ""));
				} else {
					setTitle(DIALOGTITLE
							+ m_lastOpenedFile.getName()
							+ (m_isDirty ? "*" : ""));
				}

				// during comm, don't change Gallery or do any other comm
				jLoginButton.setEnabled( !inProgress && currentGallery != null);
				jGalleryCombo.setEnabled( !inProgress );
				jNewGalleryButton.setEnabled( !inProgress );

                // Disable New, Open, and Close
                jMenuItemNew.setEnabled( !inProgress );
                jMenuItemOpen.setEnabled( !inProgress );
                jMenuItemSave.setEnabled( !inProgress );
                jMenuItemSaveAs.setEnabled( !inProgress );
				jMenuItemClose.setEnabled( !inProgress
						&& null != m_lastOpenedFile );

                // in the event the library we use to save is missing, dim the menus
                try {
                    new JSX.ObjOut();
                } catch (Throwable t) {
                    jMenuItemOpen.setEnabled(false);
                    jMenuItemSave.setEnabled(false);
                    jMenuItemSaveAs.setEnabled(false);
                }

				if (currentGallery != null
						&& currentGallery.getUsername() != null
						&& currentGallery.hasComm()
						&& currentGallery.getComm(jStatusBar).isLoggedIn()) {
					jLoginButton.setText(GRI18n.getString(MODULE, "Log_out"));
				} else {
					jLoginButton.setText(GRI18n.getString(MODULE, "Log_in"));
				}

				jAlbumTree.setEnabled( ! inProgress && jAlbumTree.getModel().getChildCount(jAlbumTree.getModel().getRoot()) >= 1);

				// if the selected album is uploading, disable everything
				boolean enabled = ! inProgress && currentAlbum != null && jAlbumTree.getModel().getChildCount(jAlbumTree.getModel().getRoot()) >= 1;
				jBrowseButton.setEnabled( enabled && currentAlbum.getCanAdd());
				jPictureInspector.setEnabled( enabled );
				jPicturesList.setEnabled( enabled && currentAlbum.getCanAdd());
				jNewAlbumButton.setEnabled( !inProgress && currentGallery != null && currentGallery.hasComm()
					&& currentGallery.getComm(jStatusBar).hasCapability(GalleryCommCapabilities.CAPA_NEW_ALBUM));

				// change image displayed
				int sel = jPicturesList.getSelectedIndex();
				if (currentAlbum != null && currentAlbum.getSize() < 1) {
					// if album was just emptied, it takes a while for the pictureList
					// to notice...
					// this is fixed by using invokeLater [apparently not anymore]
					sel = -1;
				}

				if ( GalleryRemote.getInstance().properties.getShowPreview() && previewFrame != null ) {
					if ( sel != -1 ) {
						previewFrame.displayPicture( currentAlbum.getPicture( sel ) );
					} else {
						previewFrame.displayPicture( null );
					}

					if ( !previewFrame.isVisible() ) {
						previewFrame.setVisible( true );
					}
				}

				// status
				if ( currentAlbum == null) {
					jPictureInspector.setPictures( null );

					jStatusBar.setStatus(GRI18n.getString(MODULE, "notLogged") );
				} else if ( currentAlbum.sizePictures() > 0 ) {
					jPictureInspector.setPictures( jPicturesList.getSelectedValues() );

					int selN = jPicturesList.getSelectedIndices().length;


					if ( sel == -1 ) {
						Object [] params = {new Integer(currentAlbum.sizePictures()),
											new Integer((int)(currentAlbum.getPictureFileSize() / 1024))};
						jStatusBar.setStatus(GRI18n.getString(MODULE, "statusBarNoSel", params ));
					} else {
						Object [] params = {new Integer(selN),
											GRI18n.getString(MODULE, (selN==1)?"oneSel":"manySel"),
											new Integer((int) Album.getObjectFileSize( jPicturesList.getSelectedValues() ) / 1024 )};

						jStatusBar.setStatus(GRI18n.getString(MODULE, "statusBarSel", params));
					}
				} else {
					jPictureInspector.setPictures( null );

					jStatusBar.setStatus(GRI18n.getString(MODULE, "noSelection"));
				}

				jAlbumInspector.setAlbum(currentAlbum);

				jAlbumTree.repaint();
			}});
	}


	private void updateGalleryParams() {
		Log.log(Log.LEVEL_TRACE, MODULE, "updateGalleryParams: current gallery: " + getCurrentGallery());

		updateAlbumCombo();
	}


	private void updateAlbumCombo() {
		Gallery currentGallery = getCurrentGallery();
		Log.log(Log.LEVEL_TRACE, MODULE, "updateAlbumCombo: current gallery: " + currentGallery);

		if (currentGallery != null && jAlbumTree.getModel() != currentGallery) {
			jAlbumTree.setModel( currentGallery );
			currentGallery.addListDataListener(this);
		}

		if (currentGallery == null || currentGallery.getSize() < 1) {
			jAlbumTree.setEnabled( false );
			jPicturesList.setEnabled( false );

			updatePicturesList();
		} else {
			jAlbumTree.setEnabled( ! inProgress );

			updatePicturesList();
		}
	}


	private void updatePicturesList() {
		Album currentAlbum = getCurrentAlbum();
		Log.log(Log.LEVEL_TRACE, MODULE, "updatePicturesList: current album: " + currentAlbum);

		if (currentAlbum == null) {
			// fake empty album to clear the list
			jPicturesList.setModel( new Album(null) );
		} else {
			if (jPicturesList.getModel() != currentAlbum) {
				jPicturesList.setModel( currentAlbum );
				currentAlbum.addListDataListener( this );
			}

			jPictureInspector.setPictures( null );
		}

		resetUIState();
	}


	/**
	 *  Open a file selection dialog and load the corresponding files
	 */
	public void browseAddPictures() {
		jStatusBar.setStatus(GRI18n.getString(MODULE, "selPicToAdd") );
		File[] files = AddFileDialog.addFiles( this );

		if ( files != null ) {
			addPictures( files, false);
		}
	}

	public void addPictures( File[] files, boolean select) {
		addPictures( null, files, -1, select);
	}

	public void addPictures( Album album, File[] files, boolean select) {
		addPictures( album, files, -1, select);
	}

	public void addPictures( File[] files, int index, boolean select) {
		addPictures( null, files, index, select);
	}

	public void addPictures( Album album, File[] files, int index, boolean select) {
		if (album == null) {
			album = getCurrentAlbum();
		}

		ArrayList newPictures = null;
		if (index == -1) {
			newPictures = album.addPictures( files );
		} else {
			newPictures = album.addPictures( files, index );
		}

		thumbnailCache.preloadThumbnails( newPictures.iterator() );

		if (select) {
			selectAddedPictures(files, index);
		}

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}

	public void addPictures( Picture[] pictures, boolean select) {
		addPictures( null, pictures, -1, select );
	}

	public void addPictures( Album album, Picture[] pictures, boolean select) {
		addPictures( album, pictures, -1, select );
	}

	public void addPictures( Picture[] pictures, int index, boolean select) {
		addPictures( null, pictures, index, select );
	}

	public void addPictures( Album album, Picture[] pictures, int index, boolean select) {
		if (album == null) {
			album = getCurrentAlbum();
		}

		if (index == -1) {
			album.addPictures( Arrays.asList(pictures) );
		} else {
			album.addPictures( Arrays.asList(pictures), index );
		}

		if (select) {
			selectAddedPictures(pictures, index);
		}

        // We've been modified, we are now dirty.
        m_isDirty = true;
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
	 *  Upload the files
	 */
	public void uploadPictures() {
		Log.log(Log.LEVEL_INFO, MODULE, "uploadPictures starting");

        File f = m_lastOpenedFile;

		if (null == f) {
			// No open file, use the default
			f = getCurrentGallery().getGalleryDefaultFile();
		}

		saveState(f);

		getCurrentGallery().uploadFiles( new UploadProgress(this) );
	}


	/**
	 *  Sort the files alphabetically
	 */
	public void sortPictures() {
		getCurrentAlbum().sortPicturesAlphabetically();

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}


	/**
	 *  Fetch Albums from server and update UI
	 */
	public void fetchAlbums() {
		Log.log(Log.LEVEL_INFO, MODULE, "fetchAlbums starting");

		getCurrentGallery().fetchAlbums( jStatusBar );

		//updateAlbumCombo();

		if (jAlbumTree.getModel().getChildCount(jAlbumTree.getModel().getRoot()) > 0) {
			jAlbumTree.setSelectionPath(null);
		}
	}

	public void fetchAlbumImages() {
		Log.log(Log.LEVEL_INFO, MODULE, "fetchAlbumImages starting");

		getCurrentAlbum().fetchAlbumImages( jStatusBar );
	}

	public void newAlbum() {
		NewAlbumDialog dialog = new NewAlbumDialog(this, getCurrentGallery(), getCurrentAlbum());
		final String newAlbumName = dialog.getNewAlbumName();

		Log.log(Log.LEVEL_TRACE, MODULE, "Album '" + newAlbumName + "' created.");
		// there is probably a better way... this is needed to give the UI time to catch up
		// and load the combo up with the reloaded album list
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.logException(Log.LEVEL_ERROR, MODULE, e);
				}

				//SwingUtilities.invokeLater(new Runnable() {
				//	public void run() {
				// todo: none of the calls below seem to have any effect
				Log.log(Log.LEVEL_TRACE, MODULE, "Selecting " + newAlbumName);

				TreePath path = getCurrentGallery().getPathForAlbum(getCurrentGallery().getAlbumByName(newAlbumName));
				jAlbumTree.expandPath(path);
				jAlbumTree.makeVisible(path);
				jAlbumTree.setSelectionPath(path);

				//jAlbumTree.repaint();
				//	}
				//});
			}
		}.start();

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}


	/**
	 *  Delete Pictures that are selected in the list
	 */
	public void deleteSelectedPictures() {
		int[] indices = jPicturesList.getSelectedIndices();
		int selected = jPicturesList.getSelectedIndex();
		Picture reselect = null;

		// find non-selected item after selected index
		Arrays.sort(indices);
		boolean found = false;

		int i = selected + 1;
		while (i < jPicturesList.getModel().getSize()) {
			if (Arrays.binarySearch(indices, i) < 0) {
				found = true;
				break;
			}

			i++;
		}

		if (! found) {
			i = selected - 1;
			while (i >= 0) {
				if (Arrays.binarySearch(indices, i) < 0) {
					found = true;
					break;
				}

				i--;
			}
		}

		if (found) {
			reselect = (Picture) jPicturesList.getModel().getElementAt(i);
		}

		getCurrentAlbum().removePictures( indices );

        // We've been modified, we are now dirty.
        m_isDirty = true;

		if (reselect != null) {
			jPicturesList.setSelectedValue(reselect, true);
		}
	}


	/**
	 *  Move selected Pictures up
	 */
	public void movePicturesUp() {
		int[] indices = jPicturesList.getSelectedIndices();
		int[] reselect = new int[indices.length];

		Arrays.sort(indices);

		for (int i = 0; i < indices.length; i++) {
			if (indices[i] > 0) {
				Picture buf = getCurrentAlbum().getPicture( indices[i] );
				getCurrentAlbum().setPicture( indices[i], getCurrentAlbum().getPicture( indices[i] - 1 ) );
				getCurrentAlbum().setPicture( indices[i] - 1, buf );
				//jPicturesList.setSelectedIndex( indices[i] - 1 );
				reselect[i] = indices[i] - 1;
			} else {
				reselect[i] = indices[i];
			}
		}

		jPicturesList.setSelectedIndices(reselect);

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}


	/**
	 *  Move selected Pictures down
	 */
	public void movePicturesDown() {
		int[] indices = jPicturesList.getSelectedIndices();
		int[] reselect = new int[indices.length];

		Arrays.sort(indices);

		for (int i = indices.length - 1; i >= 0; i--) {
			if ( indices[i]  < getCurrentAlbum().sizePictures() - 1 ) {
				Picture buf = getCurrentAlbum().getPicture( indices[i]  );
				getCurrentAlbum().setPicture( indices[i] , getCurrentAlbum().getPicture( indices[i]  + 1 ) );
				getCurrentAlbum().setPicture( indices[i]  + 1, buf );
				//jPicturesList.setSelectedIndex( sel + 1 );
				reselect[i] = indices[i] + 1;
			} else {
				reselect[i] = indices[i];
			}
		}

		jPicturesList.setSelectedIndices(reselect);

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}

	public void selectNextPicture() {
		int i = jPicturesList.getSelectedIndex();

		if (i < jPicturesList.getModel().getSize() - 1) {
			jPicturesList.setSelectedIndex(i + 1);
		}
	}

	public void selectPrevPicture() {
		int i = jPicturesList.getSelectedIndex();

		if (i > 0) {
			jPicturesList.setSelectedIndex(i - 1);
		}
	}


	/**
	 *  Show/hide thumbnails
	 *
	 *@param  show  The new showThumbmails value
	 */
	public void setShowThumbnails( boolean show ) {
		GalleryRemote.getInstance().properties.setShowThumbnails( show );

		if ( show ) {
			if ( getCurrentAlbum() != null ) {
				thumbnailCache.preloadThumbnails( getCurrentAlbum().getPictures() );
			}

			jPicturesList.setFixedCellHeight( GalleryRemote.getInstance().properties.getThumbnailSize().height + 4 );
		} else {
			thumbnailCache.cancelLoad();
			jPicturesList.setFixedCellHeight( -1 );
		}
	}


	/**
	 *  Show/hide preview
	 *
	 *@param  show  The new showPreview value
	 */
	public void setShowPreview( boolean show ) {
		GalleryRemote.getInstance().properties.setShowPreview( show );
		if ( show ) {
			previewFrame.show();
		} else {
			previewFrame.hide();
		}
	}


	/**
	 *  Get a thumbnail from the thumbnail cache
	 *
	 *@param  filename  Description of Parameter
	 *@return           The thumbnail value
	 */
	/*public ImageIcon getThumbnail( String filename ) {
		if ( filename == null ) {
			return null;
		}

		ImageIcon r = thumbnailCache.getThumbnail( filename );

		if ( r == null ) {
			r = ImageUtils.defaultThumbnail;
		}

		return r;
	}*/


	/**
	 *  Get a thumbnail from the thumbnail cache
	 *
	 *@param  p  picture whose thumbnail is to be fetched
	 *@return    The thumbnail value
	 */
	public ImageIcon getThumbnail( Picture p ) {
		if ( p == null ) {
			return null;
		}

		ImageIcon thumb = thumbnailCache.getThumbnail( p );

		if ( thumb == null ) {
			thumb = ImageUtils.defaultThumbnail;
		} else {
			thumb = ImageUtils.rotateImageIcon(thumb, p.getAngle(), p.isFlipped(), getGlassPane());
		}

		return thumb;
	}


	/**
	 *  Callback from thumbnail cache to notify that a new one has been loaded
	 */
	public void thumbnailLoadedNotify() {
		jPicturesList.repaint();
	}


	/**
	 *  Show About Box
	 */
	public void showAboutBox() {
		try {
			AboutBox ab = new AboutBox(this);
			ab.setVisible( true );
		} catch ( Exception err ) {
			Log.logException(Log.LEVEL_ERROR, MODULE, err);
		}
	}

	public void doCut() {
		doCopy();
		deleteSelectedPictures();

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}

	public void doCopy() {
		if (jPicturesList.isEnabled() && jPicturesList.getSelectedIndices().length > 0) {
			ps = new PictureSelection(jPicturesList);
		}
	}

	public void doPaste() {
		if (jPicturesList.isEnabled() && ps != null && !ps.isEmpty()) {
			getCurrentAlbum().addPictures(ps, jPicturesList.getSelectedIndex());
		}

        // We've been modified, we are now dirty.
        m_isDirty = true;
	}


	private void jbInit()
			throws Exception {//{{{
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		getContentPane().setLayout( new GridBagLayout() );
		jTopPanel.setLayout( new GridBagLayout() );
		jBottomPanel.setLayout( gridLayout1 );
		gridLayout1.setHgap( 5 );
		jAlbumPanel.setLayout(new BorderLayout());

		jLabel1.setText( GRI18n.getString(MODULE, "Gallery_URL") );

		jLoginButton.setText( GRI18n.getString(MODULE, "Log_in") );
		jLoginButton.setToolTipText(GRI18n.getString(MODULE, "loginButtonTip") );
		jLoginButton.setActionCommand( "Fetch" );
		jLoginButton.setIcon(iLogin);

		jNewAlbumButton.setText(GRI18n.getString(MODULE, "newAlbmBtnTxt"));
		jNewAlbumButton.setToolTipText(GRI18n.getString(MODULE, "newAlbmBtnTip"));
		jNewAlbumButton.setActionCommand( "NewAlbum" );
		jNewAlbumButton.setIcon(iNewAlbum);

		jUploadButton.setText( GRI18n.getString(MODULE, "upldBtnTxt") );
		jUploadButton.setActionCommand( "Upload" );
		jUploadButton.setToolTipText( GRI18n.getString(MODULE, "upldBtnTip") );
		jInspectorDivider.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jInspectorDivider.setBorder( new TitledBorder( BorderFactory.createEtchedBorder( Color.white, new Color( 148, 145, 140 ) ), GRI18n.getString(MODULE, "inspDvdr") ) );
		jInspectorDivider.setOneTouchExpandable(true);
		jInspectorDivider.setResizeWeight(.66);
		jAlbumPictureDivider.setOneTouchExpandable(true);
		jAlbumPictureDivider.setResizeWeight(.5);
		jTopPanel.setBorder( new TitledBorder( BorderFactory.createEtchedBorder( Color.white, new Color( 148, 145, 140 ) ), GRI18n.getString(MODULE, "panel1")) );
		jBrowseButton.setText( GRI18n.getString(MODULE, "brwsBtnTxt"));
		jBrowseButton.setActionCommand( "Browse" );
		jBrowseButton.setToolTipText(GRI18n.getString(MODULE, "brwsBtnTip"));
		jSortButton.setText(GRI18n.getString(MODULE, "sortBtnTxt"));
		jSortButton.setActionCommand( "Sort" );
		jSortButton.setToolTipText(GRI18n.getString(MODULE, "sortBtnTip"));
		jGalleryCombo.setActionCommand("Url");
		jGalleryCombo.setToolTipText(GRI18n.getString(MODULE, "gllryCombo"));

		jMenuFile.setText( GRI18n.getString(MODULE, "menuFile" ));

		jMenuItemNew.setText( GRI18n.getString(MODULE, "menuNew"));
		jMenuItemNew.setActionCommand( "File.New" );

		jMenuItemOpen.setText( GRI18n.getString(MODULE, "menuOpen"));
		jMenuItemOpen.setActionCommand( "File.Open" );
		jMenuItemOpen.setIcon(iOpen);
		jMenuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		jMenuItemSave.setText( GRI18n.getString(MODULE, "menuSave" ));
		jMenuItemSave.setActionCommand( "File.Save" );
		jMenuItemSave.setIcon(iSave);
		jMenuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		jMenuItemSaveAs.setText( GRI18n.getString(MODULE, "menuSaveAs" ));
		jMenuItemSaveAs.setActionCommand( "File.SaveAs" );

		jMenuItemClose.setText( GRI18n.getString(MODULE, "menuClose" ));
		jMenuItemClose.setActionCommand( "File.Close" );
		jMenuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));

		jMenuItemQuit.setText( GRI18n.getString(MODULE, "menuQuit" ));
		jMenuItemQuit.setActionCommand( "File.Quit" );
		jMenuItemQuit.setIcon(iQuit);
		jMenuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

		jMenuEdit.setText( GRI18n.getString(MODULE, "menuEdit" ));
		jMenuItemCut.setText( GRI18n.getString(MODULE, "menuCut" ));
		jMenuItemCut.setActionCommand( "Edit.Cut" );
		jMenuItemCut.setIcon(iCut);
		jMenuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		jMenuItemCopy.setText( GRI18n.getString(MODULE, "menuCopy" ));
		jMenuItemCopy.setActionCommand( "Edit.Copy" );
		jMenuItemCopy.setIcon(iCopy);
		jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		jMenuItemPaste.setText( GRI18n.getString(MODULE, "menuPaste" ));
		jMenuItemPaste.setActionCommand( "Edit.Paste" );
		jMenuItemPaste.setIcon(iPaste);
		jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

		jMenuOptions.setText( GRI18n.getString(MODULE, "menuOptions") );
		jCheckBoxMenuThumbnails.setActionCommand( "Options.Thumbnails" );
		jCheckBoxMenuThumbnails.setText( GRI18n.getString(MODULE, "cbmenuThumb") );
		jCheckBoxMenuPreview.setActionCommand( "Options.Preview" );
		jCheckBoxMenuPreview.setText( GRI18n.getString(MODULE, "cbmenuPreview") );
		jCheckBoxMenuPath.setActionCommand( "Options.Path" );
		jCheckBoxMenuPath.setText( GRI18n.getString(MODULE, "cbmenuPath") );
		jMenuItemPrefs.setText( GRI18n.getString(MODULE, "menuPref"));
		jMenuItemPrefs.setActionCommand( "Options.Prefs" );
		jMenuItemPrefs.setIcon(iPreferences);

		jMenuHelp.setText( GRI18n.getString(MODULE, "menuHelp") );
		jMenuItemAbout.setActionCommand( "Help.About" );
		jMenuItemAbout.setText( GRI18n.getString(MODULE, "menuAbout") );
		jMenuItemAbout.setIcon(iAbout);

		jNewGalleryButton.setText(GRI18n.getString(MODULE, "newGalleryBtn"));
		jNewGalleryButton.setActionCommand("NewGallery");
		jNewGalleryButton.setIcon(iNewGallery);
		jInspectorPanel.setLayout(jInspectorCardLayout);
		jPictureScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jPictureScroll.setBorder(new TitledBorder(BorderFactory.createEmptyBorder(),GRI18n.getString(MODULE, "pictures")));
		jAlbumScroll.setBorder(new TitledBorder(BorderFactory.createEmptyBorder(),GRI18n.getString(MODULE, "albums")));

		this.getContentPane().add( jTopPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
		jTopPanel.add( jLabel1, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		this.getContentPane().add( jInspectorDivider, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 2, 2, 2 ), 0, 0 ) );
		jInspectorDivider.add(jInspectorPanel, JSplitPane.RIGHT);
		jInspectorDivider.add(jAlbumPictureDivider, JSplitPane.LEFT);
		jInspectorPanel.add(new JScrollPane(jPictureInspector),  CARD_PICTURE);
		jInspectorPanel.add(new JScrollPane(jAlbumInspector),  CARD_ALBUM);
		jAlbumPictureDivider.add(jPictureScroll, JSplitPane.RIGHT);
		jAlbumPictureDivider.add(jAlbumPanel, JSplitPane.LEFT);
		jAlbumPanel.add(jAlbumScroll, BorderLayout.CENTER);
		jAlbumPanel.add(jNewAlbumButton,  BorderLayout.SOUTH);
		jAlbumScroll.getViewport().add(jAlbumTree, null);
		jPictureScroll.getViewport().add(jPicturesList, null);
		this.getContentPane().add( jBottomPanel, new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
		jBottomPanel.add( jBrowseButton, null );
		jBottomPanel.add( jSortButton, null);
		jBottomPanel.add( jUploadButton, null );
		this.getContentPane().add( jStatusBar, new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jTopPanel.add( jGalleryCombo,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
		jTopPanel.add(jNewGalleryButton,     new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
		jTopPanel.add(jLoginButton,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));

		jMenuBar1.add( jMenuFile );
		jMenuBar1.add( jMenuEdit );
		jMenuBar1.add( jMenuOptions );
		jMenuBar1.add( jMenuHelp );

        jMenuFile.add( jMenuItemNew );
		jMenuFile.add( jMenuItemOpen );
		jMenuFile.add( jMenuItemSave );
        jMenuFile.add( jMenuItemSaveAs );
        jMenuFile.add( jMenuItemClose );

        jMenuFile.addSeparator();

        // Remember where we are so we can build the dynamic MRU list here.
        m_MRUMenuIndex = jMenuFile.getItemCount();

		if (!IS_MAC_OS_X) {
			jMenuFile.addSeparator();
			jMenuFile.add( jMenuItemQuit );

			jMenuHelp.add( jMenuItemAbout );
		}

        updateMRUItemList();

		jMenuEdit.add( jMenuItemCut );
		jMenuEdit.add( jMenuItemCopy );
		jMenuEdit.add( jMenuItemPaste );

		jMenuOptions.add( jCheckBoxMenuThumbnails );
		jMenuOptions.add( jCheckBoxMenuPreview );
		jMenuOptions.add( jCheckBoxMenuPath );

		if (!IS_MAC_OS_X) {
			jMenuOptions.addSeparator();
			jMenuOptions.add( jMenuItemPrefs );
		}
	}//}}}


	private void jbInitEvents() {
		jLoginButton.addActionListener( this );
		jNewAlbumButton.addActionListener( this );
		jUploadButton.addActionListener( this );
		jSortButton.addActionListener( this );
		jBrowseButton.addActionListener( this );
		jNewGalleryButton.addActionListener( this );
		//jGalleryCombo.addActionListener( this );
		jAlbumTree.addTreeSelectionListener( this );
		jMenuItemPrefs.addActionListener( this );
        jMenuItemNew.addActionListener( this );
		jMenuItemOpen.addActionListener( this );
        jMenuItemSave.addActionListener( this );
        jMenuItemSaveAs.addActionListener( this );
        jMenuItemClose.addActionListener( this );
		jMenuItemQuit.addActionListener( this );
		jMenuItemAbout.addActionListener( this );
		jMenuItemCut.addActionListener( this );
		jMenuItemCopy.addActionListener( this );
		jMenuItemPaste.addActionListener( this );

		jCheckBoxMenuThumbnails.addItemListener( this );
		jCheckBoxMenuPreview.addItemListener( this );
		jCheckBoxMenuPath.addItemListener( this );
		// in Swing 1.3, using an ItemListener for a JComboBox doesn't work,
		// using ActionListener instead
		//album.addItemListener( this );
		//jAlbumCombo.addActionListener( this );

		jPicturesList.addListSelectionListener( this );

		addWindowListener(
				new java.awt.event.WindowAdapter() {
					public void windowClosing( java.awt.event.WindowEvent e ) {
						thisWindowClosing( e );
					}
				} );
		jPicturesList.addKeyListener(
				new KeyAdapter() {
					public void keyPressed( KeyEvent e ) {
						jListKeyPressed( e );
					}
				} );

		jPicturesList.addFocusListener(this);
		jAlbumTree.addFocusListener(this);
	}


    /**
     * If we are dirty then ask the user if we should save the
     * data before doing the dangerous thing we are about to do (new, open,
     * etc.).  If we are not dirty this method
     * is a NOP.  If the user says they want to save, this method will save
     * to the currently open file before returning.
     *
     * @return one of CANCEL_OPTION, OK_OPTION (from the JOptionPane
     *         class). On CANCEL_OPTION you should stop the current
     *         process. OK_OPTION means that either we are not dirty)
     *         or that we just saved for the user.
     */
	private int saveOnPermission() {
		if (!m_isDirty) {
			return (JOptionPane.OK_OPTION);
		}

		int response = JOptionPane.showConfirmDialog(
				MainFrame.this,
				GRI18n.getString(MODULE, "OK_toSaveBeforeClose"),
				m_lastOpenedFile.getName(),
				JOptionPane.YES_NO_CANCEL_OPTION);

		if (JOptionPane.YES_OPTION == response) {
			if (null == m_lastOpenedFile) {
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
	}

	// Event handling
	/**
	 *  Menu, button and field handling
	 *
	 *@param  e  Action event
	 */
	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();
		Log.log(Log.LEVEL_INFO, MODULE, "Command selected " + command);
		//Log.log(Log.TRACE, MODULE, "        event " + e );

        String MRUFileName = null;
        if (command.startsWith("File.MRU.")) {
            MRUFileName = command.substring("File.MRU.".length());
            command = "File.Open";
        }

		if ( command.equals( "File.Quit" ) ) {
			thisWindowClosing( null );
		} else if ( command.equals( "File.New" ) ) {
            int response = saveOnPermission();

			if (JOptionPane.CANCEL_OPTION == response) {
				return;
			}

			resetState();
		} else if ( command.equals( "File.Open" ) ) {

            openState(MRUFileName);
		} else if ( command.equals( "File.Save" ) ) {
            // Do Save As if the file is the default
            if (m_lastOpenedFile == null) {
				saveAsState();
			} else {
				saveState();
			}
		} else if ( command.equals( "File.SaveAs" ) ) {
			saveAsState();
		} else if ( command.equals( "File.Close" ) ) {
            int response = saveOnPermission();

            if (JOptionPane.CANCEL_OPTION == response) {
				return;
			}

            resetState();
		} else if ( command.equals( "Edit.Cut" ) ) {
			doCut();
		} else if ( command.equals( "Edit.Copy" ) ) {
			doCopy();
		} else if ( command.equals( "Edit.Paste" ) ) {
			doPaste();
		} else if ( command.equals( "Options.Prefs" ) ) {
			showPreferencesDialog();
		} else if ( command.equals( "Help.About" ) ) {
			showAboutBox();
		} else if ( command.equals( "Fetch" ) ) {
			if (getCurrentGallery().hasComm() && getCurrentGallery().getComm(jStatusBar).isLoggedIn()) {

				// we're currently logged in: log out
                int response = saveOnPermission();

                if (JOptionPane.CANCEL_OPTION == response) {
					return;
				}

				getCurrentGallery().logOut();

                m_lastOpenedFile = null;

                // We've been logged out, we are now clean.
                m_isDirty = false;
			} else {
				// login may have failed and caused getComm to be null.
				GalleryComm comm = getCurrentGallery().getComm(jStatusBar);

				// may have tried to connect and failed
				if (comm != null && !GalleryComm.wasAuthFailure()) {
					fetchAlbums();
				}
			}
		} else if ( command.equals( "NewAlbum" ) ) {
			newAlbum();
		} else if ( command.equals( "Browse" ) ) {
			browseAddPictures();
		} else if ( command.equals( "Upload" ) ) {
			uploadPictures();
		} else if ( command.equals( "Sort" ) ) {
			sortPictures();
		} else if ( command.equals( "NewGallery" ) ) {
			showPreferencesDialog(URLPanel.class.getName());
		} else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unhandled command " + command );
		}
	}

	public void showPreferencesDialog() {
		showPreferencesDialog(null);
	}

	private void showPreferencesDialog(String panel) {
		PropertiesFile oldProperties = (PropertiesFile) GalleryRemote.getInstance().properties.clone();
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
		PropertiesFile p = GalleryRemote.getInstance().properties;
		p.write();

		jCheckBoxMenuThumbnails.setSelected( p.getShowThumbnails() );
		jCheckBoxMenuPreview.setSelected( p.getShowPreview() );
		jCheckBoxMenuPath.setSelected( p.getShowPath() );

		previewFrame.setVisible( p.getShowPreview() );

		setShowThumbnails( p.getShowThumbnails() );

		if (!op.getThumbnailSize().equals(p.getThumbnailSize())) {
			thumbnailCache.reload();
		}

		Log.setMaxLevel();
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
     *
     */
    private void resetState () {
        getCurrentGallery().deleteAllPictures();

        m_lastOpenedFile = null;

        // We've been reset, we are now clean.
        m_isDirty = false;

        updateAlbumCombo();
        resetUIState();
    }


	private void saveAsState () {
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(galleryFileFilter);

		int returnVal = fc.showSaveDialog(this);

		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String name = fc.getSelectedFile().getPath();

			if (! name.endsWith(FILE_TYPE) ) {
				name += FILE_TYPE;
			}

            // Remember the file
            m_lastOpenedFile = new File(name);

            resetUIState();

            saveState(m_lastOpenedFile);

            saveMRUItem(m_lastOpenedFile);

            // We've been saved, we are now clean.
            m_isDirty = false;
		}
	}

    private void saveState () {
        if (null == m_lastOpenedFile)
          {
            Log.log(Log.LEVEL_ERROR, MODULE,
                    "Trying to save with no file open");
            return;
          }

        saveState(m_lastOpenedFile);

        // We've been saved, we are now clean.
        m_isDirty = false;
    }

    /**
     * This is an internal worker function to save the state to a file.
     * Note that we specifically do *not* set m_isDirty = false in this
     * method because we use this to temporarily backup the current state
     * to the default file (and we want the user to remember that they should
     * save the state to a "real" save file if they want to keep it).
     *
     * @param f the file to store the current dialog data.
     */
	private void saveState(File f) {
		try {
			Log.log(Log.LEVEL_INFO, MODULE,
                    "Saving state to file " + f.getPath());

			Gallery[] galleryArray = new Gallery[galleries.getSize()];

			for (int i = 0; i < galleries.getSize(); i++) {
				galleryArray[i] = (Gallery) galleries.getElementAt(i);
			}

			ObjOut out = new ObjOut(new BufferedWriter(new FileWriter(f)));
			out.writeObject(galleryArray);
		} catch (IOException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Exception while trying to save state");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (NoClassDefFoundError e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "JSX not installed, can't save state...");
		}
	}

    /**
     * Reads the properties and constructs the current MRU menu list.
     */
    private void updateMRUItemList() {

        // First, delete all of the existing MRU values
        for (int i=0; i < m_MRUFileList.size(); i++) {
            jMenuFile.remove((JMenuItem)m_MRUFileList.elementAt(i));

        }

        // Create the MRU list.  First we need to find out how manu
        // MRU items we are displaying.  We will store up to 20 MRU files
        // in the properties file, but we only display the top x files in the
        // menu.
        m_MRUFileList.clear();
        int mruCount = GalleryRemote.getInstance().properties.getMRUCountProperty();

        for (int i=1; i <= 20 && m_MRUFileList.size() < mruCount; i++) {
            // Get the file name (if any) from the properties file
            String fileName = GalleryRemote.getInstance().properties.getMRUItem(i);

            if (null == fileName) {
                // If the MRU item doesn't exist, skip this one
                continue;
            }

            File mruFile = new File (fileName);

            if (!mruFile.isFile()) {
                // If the file has been deleted, skip it
                continue;
            }

            // OK, we now have a valid candidate, create the menu item
            int nextMenuItem = m_MRUFileList.size()+1;
            String menuString = nextMenuItem + "  " + mruFile.getName();

            JMenuItem nextMRUItem = new JMenuItem();
            nextMRUItem.setText(menuString);
            nextMRUItem.setMnemonic('0'+nextMenuItem);
            nextMRUItem.setActionCommand("File.MRU." +  fileName);
            nextMRUItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + nextMenuItem,
                                                              ActionEvent.CTRL_MASK));

            // Remember this menu item.
            m_MRUFileList.addElement(nextMRUItem);
        }

        // Now insert the items into the menu after the insertion point
        // we saved off earlier and set the listeners.
        int nextInsertPoint = m_MRUMenuIndex;
        for (int i=0; i < m_MRUFileList.size(); i++) {
            JMenuItem nextMRUItem = (JMenuItem)m_MRUFileList.elementAt(i);
            jMenuFile.insert(nextMRUItem, nextInsertPoint++);
            nextMRUItem.addActionListener( this );
        }
    }

    /**
     * Save the passed mruFile and then write the properties file so that
     * we don't lose the changes.  Then force the MRU menu to change.
     *
     * @param mruFile the file to add to the MRU list
     */
    private void saveMRUItem(File mruFile) {
        // Wait to here to see if we succeed in loading the file.
        GalleryRemote.getInstance().properties.addMRUItem(mruFile);

        // Save the properties file so we don't lose the MRU
        GalleryRemote.getInstance().properties.write();

        // Update the MRU list
        updateMRUItemList();
    }

    /**
     * OpenState opens a file and loads it into GR.  If a file path is
     * passed in, then that file is opened.  If null is passed in then a
     * File Open dialog is displayed to allow the user to choose a file to
     * open.
     *
     * Once a file has been loaded, this method has the side-effect of the
     * file being added to the MRU list (or moved to the top of that list
     * if it was already on it).
     *
     * @param fileToOpen The file to open (FQPN) or null if a File Open dialog
     *                   should be used.
     */
	private void openState(String fileToOpen) {
        JFileChooser fc = null;
		try {
            if (null == fileToOpen) {
    			fc = new JFileChooser();
    			fc.setAcceptAllFileFilterUsed(false);
    			fc.setFileFilter(galleryFileFilter);

    			int returnVal = fc.showOpenDialog(this);

    			if(returnVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                // Remember the file
                m_lastOpenedFile = fc.getSelectedFile();

                Log.log(Log.LEVEL_INFO, MODULE,
                        "Opening state from file " + fc.getSelectedFile().getPath());

            } else {
                m_lastOpenedFile = new File(fileToOpen);

                Log.log(Log.LEVEL_INFO, MODULE,
                        "Opening state from file " + fileToOpen);
            }

            // Before we change galleries, ask them if they want to save.
            int response = saveOnPermission();

            if (JOptionPane.CANCEL_OPTION == response) {
				return;
			}

			ObjIn in = new ObjIn(new BufferedReader(new FileReader(m_lastOpenedFile)));
			Gallery[] galleryArray = (Gallery[]) in.readObject();
			DefaultComboBoxModel newGalleries = new DefaultComboBoxModel();

			for (int i = 0; i < galleryArray.length; i++) {
				newGalleries.addElement(galleryArray[i]);
				//galleryArray[i].checkTransients();
				galleryArray[i].addListDataListener(this);
				thumbnailCache.preloadThumbnails(galleryArray[i].getAllPictures().iterator());
			}

            saveMRUItem(m_lastOpenedFile);

			setGalleries( newGalleries );

            resetUIState();
		} catch (IOException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Exception while trying to read state");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (ClassNotFoundException e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "Exception while trying to read state (probably a version mismatch)");
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		} catch (NoClassDefFoundError e) {
			Log.log(Log.LEVEL_ERROR, MODULE, "JSX not installed, can't read state...");
		}
	}

	public void removeGallery(Gallery g) {
		Log.log(Log.LEVEL_INFO, MODULE, "Deleting Gallery " + g);
		galleries.removeElement(g);

		//g.removeFromProperties(GalleryRemote.getInstance().properties);

		// tell all the galleries they've been moved...
		for (int i = 0; i < galleries.getSize(); i++) {
			Gallery gg = (Gallery) galleries.getElementAt(i);
			gg.setPrefsIndex(i);
			gg.writeToProperties(GalleryRemote.getInstance().properties);
		}

		Gallery.removeFromProperties(GalleryRemote.getInstance().properties, galleries.getSize());
	}


	/**
	 *  CheckboxMenu handling
	 *
	 *@param  e  Description of Parameter
	 */
	public void itemStateChanged( ItemEvent e ) {
		Object item = e.getItemSelectable();
		//Log.log(Log.TRACE, MODULE, "Item selected " + item);

		if ( item == jCheckBoxMenuThumbnails ) {
			setShowThumbnails( e.getStateChange() == ItemEvent.SELECTED );
		} else if ( item == jCheckBoxMenuPreview ) {
			setShowPreview( e.getStateChange() == ItemEvent.SELECTED );
		} else if ( item == jCheckBoxMenuPath ) {
			GalleryRemote.getInstance().properties.setShowPath( ( e.getStateChange() == ItemEvent.SELECTED ) ? true : false );
			jPicturesList.repaint();
		} /*else if ( item == album ) {
		updatePicturesList( (Album) ( (JComboBox) item ).getSelectedItem());
		}*/ else {
			Log.log(Log.LEVEL_ERROR, MODULE, "Unhandled item state change " + item );
		}
	}


	/**
	 *  Implementation of the ListSelectionListener
	 *
	 *@param  e  ListSelection event
	 */
	public void valueChanged( ListSelectionEvent e ) {
		//Log.log(Log.TRACE, MODULE, "List selection changed: " + e);

		int sel = jPicturesList.getSelectedIndex();

		if ( sel != -1 ) {
			thumbnailCache.preloadThumbnailFirst( getCurrentAlbum().getPicture( sel ) );
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
		updateAlbumCombo();

		jAlbumInspector.setAlbum(getCurrentAlbum());
		jInspectorCardLayout.show(jInspectorPanel, CARD_ALBUM);
	}

	public void albumChanged(Album a) {
		if (a == getCurrentAlbum()) {
			jAlbumInspector.setAlbum(a);
		}
	}

	/**
	 *  Implementation of the ListDataListener
	 *
	 *@param  e  ListSelection event
	 */
	public void contentsChanged( ListDataEvent e ) {
		Object source = e.getSource();
		Log.log(Log.LEVEL_TRACE, MODULE, "Contents changed: " + e);

		if (source instanceof Album) {
			// Also tell MainFrame (ugly, but works around bug in Swing where when
			// the list data changes (and nothing remains to be selected), no
			// selection change events are fired.
			updatePicturesList();
		} else if (source instanceof Gallery) {
			updateAlbumCombo();
		} else if (source instanceof DefaultComboBoxModel) {
			updateGalleryParams();
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
	 *  Listen for key events
	 *
	 *@param  e  Key event
	 */
	public void jListKeyPressed( KeyEvent e ) {
		if ( ! inProgress) {
			int vKey = e.getKeyCode();

			switch ( vKey ) {
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
			}
		}
	}

	public Gallery getCurrentGallery() {
		Gallery gallery = (Gallery) jGalleryCombo.getSelectedItem();
		return gallery;
	}

	public Album getCurrentAlbum() {
		Object album = jAlbumTree.getLastSelectedPathComponent();

		if (album == null || ! (album instanceof Album)) {
			return null;
		} else {
			return (Album) album;
		}
	}

	private void macOSXRegistration() {
		if (IS_MAC_OS_X) {
			try {
				Class theMacOSXAdapter;
				theMacOSXAdapter = Class.forName("com.gallery.GalleryRemote.util.MacOSXAdapter");

				Class[] defArgs = {JFrame.class, String.class, String.class, String.class };
				Method registerMethod = theMacOSXAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
				if (registerMethod != null) {
					Object[] args = { this, "showAboutBox", "shutdown", "showPreferencesDialog" };
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

	public void flushMemory() {
		thumbnailCache.flushMemory();
		previewFrame.flushMemory();
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
	public void focusLost(FocusEvent e) { }

	/**
	 *  Cell renderer
	 *
	 *@author     paour
	 *@created    11 août 2002
	 */
	class FileCellRenderer extends DefaultListCellRenderer
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

			if (value != null && index != -1) {
				Picture p = getCurrentAlbum().getPicture( index );

				if (p.isOnline()) {
					if (p.getAlbum() != p.getAlbumOnServer()
							|| p.getIndex() != p.getIndexOnServer()) {
						setForeground(Color.RED);
					} else {
						setForeground(Color.GREEN);
					}
				} else {
					setForeground(Color.BLACK);
				}

				if ( GalleryRemote.getInstance().properties.getShowThumbnails() ) {
					ImageIcon icon = getThumbnail( p );
					setIcon( icon );
					setIconTextGap( 4 + GalleryRemote.getInstance().properties.getThumbnailSize().width - icon.getIconWidth() );
				}

				StringBuffer text = new StringBuffer();
				text.append("<html><p>");

				if (p.isOnline()) {
					text.append(p.getName());
				} else {
					File f = p.getSource();
					text.append(f.getName());
					if ( GalleryRemote.getInstance().properties.getShowPath() ) {
						text.append(" [").append(f.getParent()).append("]</p>");
					}
				}

				if (p.getCaption() != null) {
					text.append("<p><font color=\"gray\">").append(p.getEscapedCaption()).append("</font></p>");
				}

				text.append("</html>");
				//Log.log(Log.TRACE, MODULE, text.toString());
				setText( text.toString() );
			} else {
				setText("dummy");
			}

			return this;
		}
	}

	class AlbumTreeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			Album album = null;

			if (value instanceof Album) {
				album = (Album) value;
			}

			if (album != null && album.getSize() > 0) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}

			if (album != null && album.isHasFetchedImages()) {
				setForeground(Color.GREEN);
			} else {
				setForeground(Color.BLACK);
			}

			setIcon(null);
			setText(getText().trim());

			return this;
		}
	}

	static {
		try {
			iAbout = new ImageIcon(MainFrame.class.getResource("/Information16.gif"));
			iSave = new ImageIcon(MainFrame.class.getResource("/Save16.gif"));
			iOpen = new ImageIcon(MainFrame.class.getResource("/Open16.gif"));
			iPreferences = new ImageIcon(MainFrame.class.getResource("/Preferences16.gif"));
			iQuit = new ImageIcon(MainFrame.class.getResource("/Stop16.gif"));
			iCut = new ImageIcon(MainFrame.class.getResource("/Cut16.gif"));
			iCopy = new ImageIcon(MainFrame.class.getResource("/Copy16.gif"));
			iPaste = new ImageIcon(MainFrame.class.getResource("/Paste16.gif"));

			iNewGallery = new ImageIcon(MainFrame.class.getResource("/WebComponentAdd16.gif"));
			iLogin = new ImageIcon(MainFrame.class.getResource("/WebComponent16.gif"));
			iNewAlbum = new ImageIcon(MainFrame.class.getResource("/New16.gif"));

			iUp = new ImageIcon(MainFrame.class.getResource("/Up16.gif"));
			iDown = new ImageIcon(MainFrame.class.getResource("/Down16.gif"));
			iDelete = new ImageIcon(MainFrame.class.getResource("/Delete16.gif"));
			iRight = new ImageIcon(MainFrame.class.getResource("/RotateRight24.gif"));
			iLeft = new ImageIcon(MainFrame.class.getResource("/RotateLeft24.gif"));
			iFlip = new ImageIcon(MainFrame.class.getResource("/FlipHoriz24.gif"));

			iComputer = new ImageIcon(MainFrame.class.getResource("/computer.gif"));
			iUploading = new ImageIcon(MainFrame.class.getResource("/uploading.gif"));
		} catch (Exception e) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e);
		}
	}
}

