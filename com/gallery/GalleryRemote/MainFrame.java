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

import HTTPClient.*;
import com.gallery.GalleryRemote.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 *  Description of the Class
 *
 *@author     jackodog
 *@author     paour
 *@created    August 16, 2002
 */
public class MainFrame extends javax.swing.JFrame
		 implements ActionListener, ItemListener, ListSelectionListener,
		 ListDataListener, CaretListener, StatusUpdate
{
	public static final String MODULE = "MainFrame";

	public static final int ONE_SECOND = 1000;

	PreviewFrame previewFrame = new PreviewFrame();

	boolean dontReselect = false;

	private DefaultComboBoxModel galleries = null;
	private Gallery currentGallery = null;
	private Album mAlbum = null;
	private boolean mInProgress = false;
	private Thread undeterminedThread = null;
	private javax.swing.Timer mTimer;
	private boolean progressOn = false;

	ThumbnailCache thumbnailCache = new ThumbnailCache( this );

	int progressId = 0;

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JPanel jPanel1 = new JPanel();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	JMenuBar jMenuBar1 = new JMenuBar();
	JLabel jLabel1 = new JLabel();
	JTextField username = new JTextField();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JPasswordField password = new JPasswordField();
	JLabel jLabel7 = new JLabel();
	JComboBox album = new JComboBox();
	JButton fetch = new JButton();
	JSplitPane inspectorDivider = new JSplitPane();
	PictureInspector pictureInspector = new PictureInspector();
	JPanel jPanel3 = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JButton upload = new JButton();
	JButton browse = new JButton();
	JComboBox gallery = new JComboBox();
	JPanel jPanel4 = new JPanel();
	JProgressBar progress = new JProgressBar();
	JLabel status = new JLabel();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	JMenu jMenuFile = new JMenu();
	JMenuItem jMenuItemQuit = new JMenuItem();
	JMenu jMenuHelp = new JMenu();
	JMenuItem jMenuItemAbout = new JMenuItem();
	JMenu jMenuOptions = new JMenu();
	JCheckBoxMenuItem jCheckBoxMenuThumbnails = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuPreview = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuPath = new JCheckBoxMenuItem();
	JScrollPane jScrollPane1 = new JScrollPane();
	JList picturesList = new DroppableList();
	JButton newGallery = new JButton();


	/**
	 *  Constructor for the MainFrame object
	 */
	public MainFrame() {

		PropertiesFile p = GalleryRemote.getInstance().properties;

		galleries = new DefaultComboBoxModel();
		int i = -1;
		String url;
		while ( ( url = p.getProperty( "url." + (++i) ) ) != null ) {
			try {
				String username = p.getProperty( "username." + i );
				String password = p.getBase64Property( "password." + i );
	
				Log.log(Log.INFO, MODULE, "loaded saved URL: " + url + " (" + username + "/******)" );
	
				Gallery g = new Gallery( new URL( url ), username, password, /* TEMPORARY */this);
	
				galleries.addElement(g);
			} catch (Exception e) {
				Log.log(Log.ERROR, MODULE, "Error trying to load profile");
				Log.logException(Log.ERROR, MODULE, e);
			}
		}
		
		if ( galleries.getSize() == 0 ) {
			galleries.addElement( new Gallery() );
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
			Log.logException(Log.CRITICAL, MODULE, e);
		}

		setBounds( GalleryRemote.getInstance().properties.getMainBounds() );
		setJMenuBar( jMenuBar1 );
		setTitle( "Gallery Remote" );

		picturesList.setCellRenderer( new FileCellRenderer() );
		( (DroppableList) picturesList ).setMainFrame( this );

		pictureInspector.setMainFrame( this );

		previewFrame.initComponents();

		gallery.setModel( galleries );
		updateGalleryParams();
		
		resetUIState();

		jCheckBoxMenuThumbnails.setSelected( GalleryRemote.getInstance().properties.getShowThumbnails() );
		jCheckBoxMenuPreview.setSelected( GalleryRemote.getInstance().properties.getShowPreview() );
		jCheckBoxMenuPath.setSelected( GalleryRemote.getInstance().properties.getShowPath() );
		setShowThumbnails( GalleryRemote.getInstance().properties.getShowThumbnails() );
		inspectorDivider.setDividerLocation( GalleryRemote.getInstance().properties.getIntProperty( "inspectorDividerLocation" ) );

		setVisible( true );

		if ( GalleryRemote.getInstance().properties.getShowPreview() ) {
			previewFrame.setVisible( true );
		}

		toFront();
	}


	/**
	 *  Close the window when the close box is clicked
	 *
	 *@param  e  Event
	 */
	void thisWindowClosing( java.awt.event.WindowEvent e ) {
		PropertiesFile p = GalleryRemote.getInstance().properties;
		for (int i = 0; i < galleries.getSize(); i++) {
			Gallery g = (Gallery) galleries.getElementAt(i);
			p.setProperty( "url." + i, g.getUrlString() );
			
			if (g.getUsername() != null) {
				p.setProperty( "username." + i, g.getUsername() );
			}
			
			if (p.getBooleanProperty("savePasswords") && g.getPassword() != null) {
				p.setBase64Property( "password." + i, g.getPassword() );
			}
		}

		p.setMainBounds( getBounds() );
		p.setPreviewBounds( previewFrame.getBounds() );
		p.setIntProperty( "inspectorDividerLocation", inspectorDivider.getDividerLocation() );

		p.write();

		setVisible( false );
		dispose();

		ImageUtils.purgeTemp();

		Log.log(Log.INFO, "Shutting log down");
		Log.shutdown();

		System.exit( 0 );
	}


	void resetUIState() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// if the list is empty or comm, disable upload
				upload.setEnabled( mAlbum != null
				&& mAlbum.sizePictures() > 0 
				&& !mInProgress
				&& album.getSelectedIndex() >= 0 );
				
				// during comm, don't change Gallery or do any other comm
				fetch.setEnabled( !mInProgress );
				gallery.setEnabled( !mInProgress );
				newGallery.setEnabled( !mInProgress );
				username.setEnabled( !mInProgress );
				password.setEnabled( !mInProgress );
				
				// if the selected album is uploading, disable everything
				boolean enabled = ! mInProgress && mAlbum != null && album.getModel().getSize() >= 1;
				browse.setEnabled( enabled );
				pictureInspector.setEnabled( enabled );
				picturesList.setEnabled( enabled );
				album.setEnabled( enabled );
				
				// change image displayed
				int sel = picturesList.getSelectedIndex();
				/*if (mAlbum != null && mAlbum.getSize() < 1) {
					// if album was just emptied, it takes a while for the pictureList
					// to notice...
					// this is fixed by using invokeLater
					sel = -1;
				}*/
				
				if ( GalleryRemote.getInstance().properties.getShowPreview() ) {
					if ( sel != -1 ) {
						String filename = ( mAlbum.getPicture( sel ).getSource() ).getPath();
						previewFrame.displayFile( filename );
					} else {
						previewFrame.displayFile( null );
					}
					
					if ( !previewFrame.isVisible() ) {
						previewFrame.setVisible( true );
					}
				}
				
				// status
				if ( mAlbum == null) {
					pictureInspector.setPictures( (Object[]) null );
					
					setStatus( "Select a Gallery URL and click Fetch Albums..." );
				} else if ( mAlbum.sizePictures() > 0 ) {
					pictureInspector.setPictures( picturesList.getSelectedValues() );
					
					int selN = picturesList.getSelectedIndices().length;
					
					if ( sel == -1 ) {
						setStatus( mAlbum.sizePictures() + " pictures / "
						+ NumberFormat.getInstance().format(
						( (int) mAlbum.getPictureFileSize() / 1024 ) )
						+ " K" );
					} else {
						setStatus( "Selected " + selN + ((selN == 1)?" picture / ":" pictures / ")
						+ NumberFormat.getInstance().format(
						( (int) mAlbum.getObjectFileSize( picturesList.getSelectedValues() ) / 1024 ) )
						+ " K" );
					}
				} else {
					pictureInspector.setPictures( (Object[]) null );
					
					setStatus( "No selection" );
				}
		}});
	}


	private void updateGalleryParams() {
		currentGallery = (Gallery) gallery.getSelectedItem();
		
		Log.log(Log.TRACE, MODULE, "current gallery: " + currentGallery);
		if ( currentGallery.getUsername() != null ) {
			username.setText( currentGallery.getUsername() );
		} else {
			username.setText("");
		}

		if ( currentGallery.getPassword() != null ) {
			password.setText( currentGallery.getPassword() );
		} else {
			password.setText("");
		}
		
		updateAlbumCombo();
		resetUIState();
	}


	private void updateAlbumCombo() {
		album.setModel( currentGallery );
		
		if (album.getModel().getSize() < 1) {
			album.setEnabled( false );
			picturesList.setEnabled( false );
			
			updatePicturesList( null );
		} else {
			// album.setSelectedIndex(0);
			album.setEnabled( ! mInProgress );
		
			updatePicturesList( (Album) album.getSelectedItem() );
		}
	}

	
	private void updatePicturesList( Album album ) {
		mAlbum = album;
		
		if (mAlbum == null) {
			// fake empty album to clear the list
			picturesList.setModel( new Album() );
		} else {
			picturesList.setModel( mAlbum );
			mAlbum.setListSelectionModel(picturesList.getSelectionModel());
			picturesList.getModel().addListDataListener( this );
		}
	}
	
	
	public void setStatus( String message ) {
		if (! progressOn) {
			// prevent progress message from being overriden
			status.setText( message );
		} else {
			//Log.log(Log.ERROR, MODULE, "Trying to override progress with status");
			//Log.logStack(Log.ERROR, MODULE);
		}
	}


	public int startProgress( int min, int max, String message, boolean undetermined) {
		if (progressOn)
		{
			Log.log(Log.INFO, "Hijacking progress by creating a new one");
		}

		progressOn = true;

		progress.setMinimum(min);
		progress.setValue(min);
		progress.setMaximum(max);
		//progress.setStringPainted( true );
		
		if (undetermined && undeterminedThread == null) {
			undeterminedThread = new Thread() {
				public void run() {
					boolean forward = true;
					while (! interrupted() ) {
						if (progress.getValue() >= progress.getMaximum()) {
							forward = false;
						} else if (progress.getValue() <= progress.getMinimum()) {
							forward = true;
						}
						
						updateProgressValue(progressId, progress.getValue() + (forward?1:-1));
						
						try {
							sleep(500);
						} catch (InterruptedException e) {}
					}
				}
			};
			undeterminedThread.start();
		}

		status.setText(message + "...");

		return ++progressId;
	}

	public void updateProgressValue( int progressId, int value ) {
		if (progressOn && progressId == this.progressId) {
			progress.setValue( value );
		} else {
			//Log.log(Log.TRACE, MODULE, "Trying to use updateProgressValue when not progressOn or with wrong progressId");
			//Log.logStack(Log.TRACE, MODULE);
		}
	}

	public void updateProgressValue( int progressId, int value, int maxValue ) {
		if (progressOn && progressId == this.progressId) {
			progress.setValue( value );
			progress.setMaximum( maxValue );
		} else {
			//Log.log(Log.TRACE, MODULE, "Trying to use updateProgressValue when not progressOn or with wrong progressId");
			//Log.logStack(Log.TRACE, MODULE);
		}
	}

	public void updateProgressStatus( int progressId, String message ) {
		if (progressOn && progressId == this.progressId) {
			status.setText( message );
		} else {
			//Log.log(Log.TRACE, MODULE, "Trying to use updateProgressStatus when not progressOn or with wrong progressId");
			//Log.logStack(Log.TRACE, MODULE);
		}
	}

	public void stopProgress( int progressId, String message )
	{
		if (! progressOn)
		{
			Log.log(Log.TRACE, MODULE, "Stopping progress when it's already stopped");
			Log.logStack(Log.TRACE, MODULE);
		}

		if ( progressId == this.progressId ) {
			progressOn = false;
			
			if (undeterminedThread != null) {
				undeterminedThread.interrupt();
				undeterminedThread = null;
			}

			//progress.setStringPainted( false );
			progress.setValue(progress.getMinimum());

			status.setText(message);
		} else {
			Log.log(Log.TRACE, MODULE, "Wrong progressId when stopping progress");
		}
	}
	
	public void setInProgress(boolean inProgress) {
		mInProgress = inProgress;
		
		resetUIState();
	}
	
	
	public void error (String message ) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	

	/**
	 *  Open a file selection dialog and load the corresponding files
	 */
	public void browseAddPictures() {
		setStatus( "Select pictures to add to the list" );
		File[] files = AddFileDialog.addFiles( this );

		if ( files != null ) {
			addPictures( files );
		}

		resetUIState();
	}

	/**
	*  Adds a feature to the Pictures attribute of the MainFrame object
	*
	*@param  files  The feature to be added to the Pictures attribute
	*/
	public void addPictures( File[] files ) {
		addPictures( files, -1 );
		//resetUIState();
	}


	/**
	 *  Adds a feature to the Pictures attribute of the MainFrame object
	 *
	 *@param  files  The feature to be added to the Pictures attribute
	 *@param  index  The index in the list of Pictures at which to begin adding
	 */
	public void addPictures( File[] files, int index ) {
		if (index == -1) {
			mAlbum.addPictures( files );
		} else {
			mAlbum.addPictures( files, index );
		}
		/*Arrays.sort( items,
				new Comparator()
				{
					public int compare( Object o1, Object o2 )
					{
						String f1 = ( (File) o1 ).getAbsolutePath();
						String f2 = ( (File) o2 ).getAbsolutePath();
						return ( f1.compareToIgnoreCase( f2 ) );
					}
					public boolean equals( Object o1, Object o2 )
					{
						String f1 = ( (File) o1 ).getAbsolutePath();
						String f2 = ( (File) o2 ).getAbsolutePath();
						return ( f1.equals( f2 ) );
					}
				} );*/
		thumbnailCache.preloadThumbnails( files );

		//resetUIState();
	}


	/**
	 *  Upload the files
	 */
	public void uploadPictures() {
		Log.log(Log.INFO, MODULE, "uploadPictures starting");
		
		currentGallery.uploadFiles( this );
	}


	/**
	 *  Fetch Albums from server and update UI
	 */
	public void fetchAlbums() {
		Log.log(Log.INFO, MODULE, "fetchAlbums starting");
		
		currentGallery.fetchAlbums( this );
	}


	/**
	 *  Delete Pictures that are selected in the list
	 */
	public void deleteSelectedPictures() {
		int[] indices = picturesList.getSelectedIndices();
		
		mAlbum.removePictures( indices );
	}


	/**
	 *  Move selected Pictures up
	 */
	public void movePictureUp() {
		int sel = picturesList.getSelectedIndex();

		if ( sel > 0 ) {
			Picture buf = mAlbum.getPicture( sel );
			mAlbum.setPicture( sel, mAlbum.getPicture( sel - 1 ) );
			mAlbum.setPicture( sel - 1, buf );
			picturesList.setSelectedIndex( sel - 1 );
		}
	}


	/**
	 *  Move selected Pictures down
	 */
	public void movePictureDown() {
		int sel = picturesList.getSelectedIndex();

		if ( sel < mAlbum.sizePictures() - 1 ) {
			Picture buf = mAlbum.getPicture( sel );
			mAlbum.setPicture( sel, mAlbum.getPicture( sel + 1 ) );
			mAlbum.setPicture( sel + 1, buf );
			picturesList.setSelectedIndex( sel + 1 );
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
			if ( mAlbum != null ) {
				thumbnailCache.preloadThumbnailFiles( mAlbum.getPictures() );
			}
			
			picturesList.setFixedCellHeight( GalleryRemote.getInstance().properties.getThumbnailSize().height + 4 );
		} else {
			thumbnailCache.cancelLoad();
			picturesList.setFixedCellHeight( -1 );
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
	public ImageIcon getThumbnail( String filename ) {
		if ( filename == null ) {
			return null;
		}

		ImageIcon r = thumbnailCache.getThumbnail( filename );

		if ( r == null ) {
			r = ImageUtils.defaultThumbnail;
		}

		return r;
	}


	/**
	 *  Get a thumbnail from the thumbnail cache
	 *
	 *@param  p  picture whose thumbnail is to be fetched
	 *@return    The thumbnail value
	 */
	public ImageIcon getThumbnail( Picture p ) {
		return getThumbnail( p.getSource().getPath() );
	}


	/**
	 *  Callback from thumbnail cache to notify that a new one has been loaded
	 */
	public void thumbnailLoadedNotify() {
		picturesList.repaint();
	}


	/**
	 *  Show About Box
	 */
	public void showAboutBox() {
		try {
			AboutBox ab = new AboutBox(this);
			ab.setVisible( true );
		} catch ( Exception err ) {
			err.printStackTrace();
		}
	}


	private void jbInit()
		throws Exception {//{{{
		this.getContentPane().setLayout( gridBagLayout1 );
		jPanel1.setLayout( gridBagLayout2 );
		jLabel1.setText( "Gallery URL" );
		jLabel2.setText( "Username" );
		jLabel3.setText( "Password" );
		jLabel7.setText( "Select Album" );
		fetch.setText( "Fetch Albums" );
		fetch.setNextFocusableComponent( album );
		fetch.setActionCommand( "Fetch" );
		this.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		jPanel3.setLayout( gridLayout1 );
		upload.setAlignmentX( (float) 2.0 );
		upload.setText( "Upload Albums" );
		upload.setActionCommand( "Upload" );
		inspectorDivider.setBorder( new TitledBorder( BorderFactory.createEtchedBorder( Color.white, new Color( 148, 145, 140 ) ), "Pictures to Upload (Drag and Drop files into this panel)" ) );
		jPanel1.setBorder( new TitledBorder( BorderFactory.createEtchedBorder( Color.white, new Color( 148, 145, 140 ) ), "Destination Gallery" ) );
		browse.setAlignmentX( (float) 1.0 );
		browse.setText( "Add pictures..." );
		browse.setActionCommand( "Browse" );
		gallery.setNextFocusableComponent( username );
		gallery.setEditable( true );
		gallery.setActionCommand("Url");
		progress.setMinimumSize( new Dimension( 10, 18 ) );
		progress.setPreferredSize( new Dimension( 150, 18 ) );
		progress.setStringPainted( false );
		gridLayout1.setHgap( 5 );
		status.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED, Color.white, SystemColor.control, SystemColor.control, Color.gray ) );
		status.setMinimumSize( new Dimension( 100, 18 ) );
		status.setPreferredSize( new Dimension( 38, 18 ) );
		jPanel4.setLayout( gridBagLayout3 );
		jMenuFile.setText( "File" );
		jMenuItemQuit.setText( "Quit" );
		jMenuItemQuit.setActionCommand( "File.Quit" );
		jMenuHelp.setText( "Help" );
		jMenuItemAbout.setActionCommand( "Help.About" );
		jMenuItemAbout.setText( "About Gallery Remote..." );
		jMenuOptions.setText( "Options" );
		jCheckBoxMenuThumbnails.setActionCommand( "Options.Thumbnails" );
		jCheckBoxMenuThumbnails.setText( "Show Thumbnails" );
		jCheckBoxMenuPreview.setActionCommand( "Options.Preview" );
		jCheckBoxMenuPreview.setText( "Show Preview" );
		jCheckBoxMenuPath.setActionCommand( "Options.Path" );
		jCheckBoxMenuPath.setText( "Show Path" );
		username.setNextFocusableComponent( password );
		username.setActionCommand("Username");
		password.setActionCommand("Password");
		password.setNextFocusableComponent( fetch );
		jScrollPane1.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		newGallery.setText("Add Gallery URL");
		newGallery.setActionCommand("NewGallery");
		album.setActionCommand("Album");
		this.getContentPane().add( jPanel1, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
		jPanel1.add( jLabel1, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		jPanel1.add( username, new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel1.add( jLabel2, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		jPanel1.add( jLabel3, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		jPanel1.add( password, new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel1.add( jLabel7, new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
		jPanel1.add( album, new GridBagConstraints( 1, 3, 2, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel1.add( fetch,   new GridBagConstraints(2, 1, 1, 2, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0) );
		this.getContentPane().add( inspectorDivider, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 2, 2, 2 ), 0, 0 ) );
		inspectorDivider.add( pictureInspector, JSplitPane.BOTTOM );
		inspectorDivider.add( jScrollPane1, JSplitPane.TOP );
		jScrollPane1.getViewport().add( picturesList, null );
		this.getContentPane().add( jPanel3, new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
		jPanel3.add( browse, null );
		jPanel3.add( upload, null );
		this.getContentPane().add( jPanel4, new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel4.add( progress, new GridBagConstraints( 1, 0, 1, 1, 0.25, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel4.add( status, new GridBagConstraints( 0, 0, 1, 1, 0.75, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		jPanel1.add( gallery,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
		jPanel1.add(newGallery,    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
		jMenuBar1.add( jMenuFile );
		jMenuBar1.add( jMenuOptions );
		jMenuBar1.add( jMenuHelp );
		jMenuFile.add( jMenuItemQuit );
		jMenuHelp.add( jMenuItemAbout );
		jMenuOptions.add( jCheckBoxMenuThumbnails );
		jMenuOptions.add( jCheckBoxMenuPreview );
		jMenuOptions.add( jCheckBoxMenuPath );
	}//}}}


	private void jbInitEvents() {
		fetch.addActionListener( this );
		upload.addActionListener( this );
		browse.addActionListener( this );
		newGallery.addActionListener( this );
		gallery.addActionListener( this );
		username.addCaretListener( this );
		password.addCaretListener( this );
		jMenuItemQuit.addActionListener( this );
		jMenuItemAbout.addActionListener( this );

		jCheckBoxMenuThumbnails.addItemListener( this );
		jCheckBoxMenuPreview.addItemListener( this );
		jCheckBoxMenuPath.addItemListener( this );
		// in Swing 1.3, using an ItemListener for a JComboBox doesn't work,
		// using ActionListener instead
		//album.addItemListener( this );
		album.addActionListener( this );

		picturesList.addListSelectionListener( this );

		addWindowListener(
			new java.awt.event.WindowAdapter()
			{
				public void windowClosing( java.awt.event.WindowEvent e ) {
					thisWindowClosing( e );
				}
			} );
		previewFrame.addWindowListener(
			new java.awt.event.WindowAdapter()
			{
				public void windowClosing( java.awt.event.WindowEvent e ) {
					jCheckBoxMenuPreview.setState( false );
				}
			} );
		picturesList.addKeyListener(
			new KeyAdapter()
			{
				public void keyPressed( KeyEvent e ) {
					jListKeyPressed( e );
				}
			} );
	}


	// Event handling
	/**
	 *  Menu, button and field handling
	 *
	 *@param  e  Action event
	 */
	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();
		Log.log(Log.INFO, MODULE, "Command selected " + command);
		//Log.log(Log.TRACE, MODULE, "        event " + e );
		
		if ( command.equals( "File.Quit" ) ) {
			thisWindowClosing( null );
		} else if ( command.equals( "Help.About" ) ) {
			showAboutBox();
		} else if ( command.equals( "Fetch" ) ) {
			fetchAlbums();
		} else if ( command.equals( "Browse" ) ) {
			browseAddPictures();
		} else if ( command.equals( "Upload" ) ) {
			uploadPictures();
		} else if ( command.equals( "NewGallery" ) ) {
			Gallery g = new Gallery(null, null, null, this);
			galleries.addElement( g );
			gallery.setSelectedItem( g );
			updateGalleryParams();
		} else if ( command.equals( "Url" ) ||
			command.equals( "comboBoxEdited" ) ) {
			//Log.log(Log.TRACE, MODULE, "modifiers: " + e.getModifiers());
			//Log.log(Log.TRACE, MODULE, "paramString: " + e.paramString());
			Log.log(Log.TRACE, MODULE, "selected: " + gallery.getSelectedItem().toString() 
				+ " (" + gallery.getSelectedIndex() + ")");
			
			if (gallery.getSelectedItem() instanceof String) {
				// text of a url edited
				try {
					currentGallery.setUrlString((String) gallery.getSelectedItem());
				} catch ( MalformedURLException mue ) {
					
					JOptionPane.showMessageDialog(this, "Malformed URL", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				// new url chosen in the popup
				updateGalleryParams();
			}
		} else if ( command.equals( "Album" ) ) {
			updatePicturesList( (Album) ( (JComboBox) e.getSource() ).getSelectedItem());
		} else {
			Log.log(Log.ERROR, MODULE, "Unhandled command " + command );
		}
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
			picturesList.repaint();
		} /*else if ( item == album ) {
			updatePicturesList( (Album) ( (JComboBox) item ).getSelectedItem());
		}*/ else {
			Log.log(Log.ERROR, MODULE, "Unhandled item state change " + item );
		}
	}


	/**
	 *  Implementation of the ListSelectionListener
	 *
	 *@param  e  ListSelection event
	 */
	public void valueChanged( ListSelectionEvent e ) {
		//Log.log(Log.TRACE, MODULE, "List selection changed: " + e);

		int sel = picturesList.getSelectedIndex();

		if ( sel != -1 ) {
			String filename = ( mAlbum.getPicture( sel ).getSource() ).getPath();
			thumbnailCache.preloadThumbnailFirst( filename );
		}

		resetUIState();
	}


	/**
	 *  Implementation of the ListDataListener
	 *
	 *@param  e  ListSelection event
	 */
	public void contentsChanged( ListDataEvent e ) {
		Object source = e.getSource();
		
		if (source == picturesList) {
			// Also tell MainFrame (ugly, but works around bug in Swing where when
			// the list data changes (and nothing remains to be selected), no
			// selection change events are fired.
			resetUIState();
		}
	}
	public void intervalAdded(ListDataEvent e) {}
	public void intervalRemoved(ListDataEvent e) {}

	// CaretListener implementation
	public void caretUpdate(CaretEvent e) {
		Object c = e.getSource();
		//Log.log(Log.TRACE, MODULE, "caretUpdate: " + e + " (" + c + ")");
		
		if (c == username) {
			currentGallery.setUsername(username.getText());
			resetUIState();
		} else if ( c == password ) {
			currentGallery.setPassword(password.getText());
			resetUIState();
		} else {
			Log.log(Log.ERROR, MODULE, "Unhandled caret update " + c );
		}
	}
	/**
	 *  Listen for key events
	 *
	 *@param  e  Key event
	 */
	public void jListKeyPressed( KeyEvent e ) {
		if ( ! mInProgress) {
			int vKey = e.getKeyCode();
	
			switch ( vKey ) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
					deleteSelectedPictures();
					break;
				case KeyEvent.VK_LEFT:
					movePictureUp();
					break;
				case KeyEvent.VK_RIGHT:
					movePictureDown();
					break;
			}
		}
	}


	/**
	 *  Cell renderer
	 *
	 *@author     paour
	 *@created    11 août 2002
	 */
	public class FileCellRenderer extends DefaultListCellRenderer
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

			Picture p = mAlbum.getPicture( index );
			File f = p.getSource();

			if ( GalleryRemote.getInstance().properties.getShowThumbnails() ) {
				ImageIcon icon = getThumbnail( p );
				setIcon( icon );
				setIconTextGap( 4 + GalleryRemote.getInstance().properties.getThumbnailSize().width - icon.getIconWidth() );
			}

			String text = f.getName();
			if ( GalleryRemote.getInstance().properties.getShowPath() ) {
				text += " [" + f.getParent() + "]";
			}
			setText( text );

			return this;
		}
	}
}

