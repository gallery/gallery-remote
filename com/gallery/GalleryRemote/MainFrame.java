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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
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
 */
public class MainFrame extends javax.swing.JFrame
{
	public final static String APP_VERSION_STRING = "0.41 (updated by Pierre-Luc Paour and Dolan Halbrook)";
	public final static int ONE_SECOND = 1000;

	javax.swing.JMenuBar jMenuBar = new javax.swing.JMenuBar();
	javax.swing.JMenu jMenuFile = new javax.swing.JMenu();
	javax.swing.JMenuItem jMenuFileQuit = new javax.swing.JMenuItem();
	javax.swing.JMenu jMenuHelp = new javax.swing.JMenu();
	javax.swing.JMenuItem jMenuHelpAbout = new javax.swing.JMenuItem();
	javax.swing.JMenu jMenuOptions = new javax.swing.JMenu();
	javax.swing.JCheckBoxMenuItem jMenuOptionsPreview = new javax.swing.JCheckBoxMenuItem();
	javax.swing.JCheckBoxMenuItem jMenuOptionsThumb = new javax.swing.JCheckBoxMenuItem();
	javax.swing.JCheckBoxMenuItem jMenuOptionsPath = new javax.swing.JCheckBoxMenuItem();
	javax.swing.JPanel jPanelMain = new javax.swing.JPanel();
	javax.swing.JPanel jPanelSettings = new javax.swing.JPanel();
	javax.swing.JLabel jLabelURL = new javax.swing.JLabel();
	javax.swing.JLabel jLabelUser = new javax.swing.JLabel();
	javax.swing.JLabel jLabelPassword = new javax.swing.JLabel();
	javax.swing.JTextField jTextURL = new javax.swing.JTextField();
	javax.swing.JTextField jTextUsername = new javax.swing.JTextField();
	javax.swing.JPasswordField jPasswordField = new javax.swing.JPasswordField();
	javax.swing.JLabel jLabelAlbum = new javax.swing.JLabel();
	javax.swing.JButton jButtonFetchAlbums = new javax.swing.JButton();
	javax.swing.JComboBox jComboBoxAlbums = new javax.swing.JComboBox();
	javax.swing.JPanel jPanelScrollPane = new javax.swing.JPanel();
	javax.swing.JScrollPane jScrollPane = new javax.swing.JScrollPane();
	DroppableList jListItems = null;
	javax.swing.JPanel jPanelButtons = new javax.swing.JPanel();
	javax.swing.JPanel jPanelButtonsTop = new javax.swing.JPanel();
	javax.swing.JButton jButtonAddFiles = new javax.swing.JButton();
	javax.swing.JButton jButtonDeleteFiles = new javax.swing.JButton();
	javax.swing.JButton jButtonUpload = new javax.swing.JButton();
	javax.swing.JButton jButtonUp = new javax.swing.JButton();
	javax.swing.JButton jButtonDown = new javax.swing.JButton();
	javax.swing.JPanel jPanelButtonsBottom = new javax.swing.JPanel();
	javax.swing.JTextField jLabelStatus = new javax.swing.JTextField();
	javax.swing.JProgressBar jProgressBar = new javax.swing.JProgressBar();
	PreviewFrame jPreview = new PreviewFrame();

	boolean dontReselect = false;

	private GalleryComm mGalleryComm;
	private Album mAlbum;
	private ArrayList mAlbumList;
	private boolean mInProgress = false;
	private javax.swing.Timer mTimer;
	//private PropertiesFile mPropertiesFile;
	//private boolean savedHeight = false;
	//private boolean mShown = false;

	public final static String DEFAULT_IMAGE = "default.gif";

	Hashtable thumbnails = new Hashtable();
	//Dimension thumbnailSize = new Dimension( 50, 50 );
	ImageIcon defaultThumbnail = loadThumbnail( DEFAULT_IMAGE );
	ThumbnailLoader thumbnailLoader = new ThumbnailLoader();
	//boolean showThumbnails = true;
	//boolean showPath = true;
	boolean highQualityThumbnails = false;

	public MainFrame()
	{
		mGalleryComm = new GalleryComm();
		mAlbum = new Album();
		mAlbumList = new ArrayList();

		highQualityThumbnails = GalleryRemote.getInstance().properties.getBooleanProperty("highQualityThumbnails");
	}
	
	public void initComponents() throws Exception
	{
		jMenuBar.setVisible( true );
		jMenuBar.add( jMenuFile );
		jMenuBar.add( jMenuHelp );
		jMenuBar.add( jMenuOptions );

		jMenuFile.setVisible( true );
		jMenuFile.setText( "File" );
		jMenuFile.add( jMenuFileQuit );

		jMenuFileQuit.setVisible( true );
		jMenuFileQuit.setText( "Quit" );
		jMenuFileQuit.setActionCommand( "File.Quit" );

		jMenuHelp.setVisible( true );
		jMenuHelp.setText( "Help" );
		jMenuHelp.add( jMenuHelpAbout );

		jMenuHelpAbout.setVisible( true );
		jMenuHelpAbout.setText( "About" );
		jMenuHelpAbout.setActionCommand( "Help.About" );

		jMenuOptions.setVisible( true );
		jMenuOptions.setText( "Options" );
		jMenuOptions.add( jMenuOptionsPreview );
		jMenuOptions.add( jMenuOptionsThumb );
		jMenuOptions.add( jMenuOptionsPath );

		jMenuOptionsPreview.setVisible( true );
		jMenuOptionsPreview.setText( "Preview" );
		jMenuOptionsPreview.setSelected( GalleryRemote.getInstance().properties.getShowPreview() );

		jMenuOptionsThumb.setVisible( true );
		jMenuOptionsThumb.setText( "Thumbnails" );
		jMenuOptionsThumb.setSelected( GalleryRemote.getInstance().properties.getShowThumbnails() );

		jMenuOptionsPath.setVisible( true );
		jMenuOptionsPath.setText( "Show Path" );
		jMenuOptionsPath.setSelected( true );

		//-- the settings panel ---
		jLabelURL.setSize( new java.awt.Dimension( 90, 20 ) );
		jLabelURL.setLocation( new java.awt.Point( 7, 20 ) );
		jLabelURL.setVisible( true );
		jLabelURL.setText( "Gallery URL" );
		jLabelURL.setHorizontalTextPosition( javax.swing.JLabel.RIGHT );
		jLabelURL.setHorizontalAlignment( javax.swing.JLabel.RIGHT );

		jLabelUser.setSize( new java.awt.Dimension( 90, 20 ) );
		jLabelUser.setLocation( new java.awt.Point( 9, 42 ) );
		jLabelUser.setVisible( true );
		jLabelUser.setText( "Username" );
		jLabelUser.setHorizontalTextPosition( javax.swing.JLabel.RIGHT );
		jLabelUser.setHorizontalAlignment( javax.swing.JLabel.RIGHT );

		jLabelPassword.setSize( new java.awt.Dimension( 90, 20 ) );
		jLabelPassword.setLocation( new java.awt.Point( 9, 64 ) );
		jLabelPassword.setVisible( true );
		jLabelPassword.setText( "Password" );
		jLabelPassword.setHorizontalTextPosition( javax.swing.JLabel.RIGHT );
		jLabelPassword.setHorizontalAlignment( javax.swing.JLabel.RIGHT );

		jTextURL.setSize( new java.awt.Dimension( 305, 20 ) );
		jTextURL.setLocation( new java.awt.Point( 106, 20 ) );
		jTextURL.setVisible( true );
		jTextURL.setText( mAlbum.getUrl() );

		jTextUsername.setSize( new java.awt.Dimension( 180, 20 ) );
		jTextUsername.setLocation( new java.awt.Point( 106, 42 ) );
		jTextUsername.setVisible( true );
		jTextUsername.setText( mAlbum.getUsername() );

		jPasswordField.setSize( new java.awt.Dimension( 180, 20 ) );
		jPasswordField.setLocation( new java.awt.Point( 106, 64 ) );
		jPasswordField.setVisible( true );
		jPasswordField.setText( mAlbum.getPassword() );

		jLabelAlbum.setSize( new java.awt.Dimension( 103, 20 ) );
		jLabelAlbum.setLocation( new java.awt.Point( -4, 91 ) );
		jLabelAlbum.setVisible( true );
		jLabelAlbum.setText( "Upload to Album" );
		jLabelAlbum.setHorizontalTextPosition( javax.swing.JLabel.RIGHT );
		jLabelAlbum.setHorizontalAlignment( javax.swing.JLabel.RIGHT );

		jButtonFetchAlbums.setSize( new java.awt.Dimension( 117, 25 ) );
		jButtonFetchAlbums.setEnabled( false );
		jButtonFetchAlbums.setLocation( new java.awt.Point( 294, 53 ) );
		jButtonFetchAlbums.setVisible( true );
		jButtonFetchAlbums.setText( "Fetch Albums" );
		jButtonFetchAlbums.setMnemonic( KeyEvent.VK_F );

		jComboBoxAlbums.setSize( new java.awt.Dimension( 305, 20 ) );
		jComboBoxAlbums.setLocation( new java.awt.Point( 106, 92 ) );
		jComboBoxAlbums.setVisible( true );

		jPanelSettings.setVisible( true );
		jPanelSettings.setLayout( null );
		jPanelSettings.setBorder( BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Destination Gallery" ) );
		jPanelSettings.setPreferredSize( new java.awt.Dimension( 500, 125 ) );
		jPanelSettings.setMinimumSize( new java.awt.Dimension( 500, 125 ) );
		jPanelSettings.setMaximumSize( new java.awt.Dimension( 4000, 125 ) );
		jPanelSettings.add( jLabelURL );
		jPanelSettings.add( jLabelUser );
		jPanelSettings.add( jLabelPassword );
		jPanelSettings.add( jTextURL );
		jPanelSettings.add( jTextUsername );
		jPanelSettings.add( jPasswordField );
		jPanelSettings.add( jLabelAlbum );
		jPanelSettings.add( jButtonFetchAlbums );
		jPanelSettings.add( jComboBoxAlbums );

		//-- the scroll pane ---
		jScrollPane.setVisible( true );
		jScrollPane.getViewport().add( jListItems );
		jPanelScrollPane.setLayout( new BorderLayout() );
		jPanelScrollPane.setBorder( BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Files to Upload (Drag and Drop files into this panel)" ) );
		jPanelScrollPane.setVisible( true );
		jPanelScrollPane.add( jScrollPane );

		jListItems = new DroppableList( this );
		jListItems.setModel(mAlbum);

		showThumbs( GalleryRemote.getInstance().properties.getShowThumbnails() );

		jScrollPane.getViewport().add( jListItems );

		//-- the button panel ---
		jButtonAddFiles.setSize( new java.awt.Dimension( 180, 25 ) );
		jButtonAddFiles.setLocation( new java.awt.Point( 0, 0 ) );
		jButtonAddFiles.setVisible( true );
		jButtonAddFiles.setText( "Browse for files to upload..." );
		jButtonAddFiles.setMnemonic( KeyEvent.VK_B );

		jButtonDeleteFiles.setSize( new java.awt.Dimension( 100, 25 ) );
		jButtonDeleteFiles.setLocation( new java.awt.Point( 190, 0 ) );
		jButtonDeleteFiles.setVisible( true );
		jButtonDeleteFiles.setEnabled( false );
		jButtonDeleteFiles.setText( "Remove files" );
		jButtonDeleteFiles.setMnemonic( KeyEvent.VK_R );

		jButtonUpload.setSize( new java.awt.Dimension( 100, 25 ) );
		jButtonUpload.setLocation( new java.awt.Point( 300, 0 ) );
		jButtonUpload.setVisible( true );
		jButtonUpload.setText( "Upload Now!" );
		jButtonUpload.setMnemonic( KeyEvent.VK_U );

		jButtonUp.setSize( new java.awt.Dimension( 70, 25 ) );
		jButtonUp.setLocation( new java.awt.Point( 410, 0 ) );
		jButtonUp.setVisible( true );
		jButtonUp.setEnabled( false );
		jButtonUp.setText( "Up" );

		jButtonDown.setSize( new java.awt.Dimension( 70, 25 ) );
		jButtonDown.setLocation( new java.awt.Point( 490, 0 ) );
		jButtonDown.setVisible( true );
		jButtonDown.setEnabled( false );
		jButtonDown.setText( "Down" );

		jPanelButtonsTop.setVisible( true );
		jPanelButtonsTop.setLayout( null );
		jPanelButtonsTop.setPreferredSize( new java.awt.Dimension( 520, 30 ) );
		jPanelButtonsTop.add( jButtonAddFiles );
		jPanelButtonsTop.add( jButtonDeleteFiles );
		jPanelButtonsTop.add( jButtonUpload );
		jPanelButtonsTop.add( jButtonUp );
		jPanelButtonsTop.add( jButtonDown );

		jLabelStatus.setVisible( true );
		jLabelStatus.setPreferredSize( new java.awt.Dimension( 420, 22 ) );
		jLabelStatus.setMinimumSize( new java.awt.Dimension( 4, 22 ) );
		jLabelStatus.setMaximumSize( new java.awt.Dimension( 4000, 22 ) );
		jLabelStatus.setText( "Drag and Drop files to Upload into panel above or Browse for them." );
		jLabelStatus.setEnabled( false );

		jProgressBar.setVisible( true );
		jProgressBar.setStringPainted( true );

		jPanelButtonsBottom.setVisible( true );
		jPanelButtonsBottom.setLayout( new javax.swing.BoxLayout( jPanelButtonsBottom, 1 ) );
		jPanelButtonsBottom.setPreferredSize( new java.awt.Dimension( 420, 50 ) );
		jPanelButtonsBottom.add( jLabelStatus );
		jPanelButtonsBottom.add( Box.createVerticalStrut( 2 ) );
		jPanelButtonsBottom.add( jProgressBar );

		jPanelButtons.setVisible( true );
		jPanelButtons.setLayout( new javax.swing.BoxLayout( jPanelButtons, 1 ) );
		jPanelButtons.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
		jPanelButtons.add( jPanelButtonsTop );
		jPanelButtons.add( jPanelButtonsBottom );

		jPanelMain.setVisible( true );
		jPanelMain.setLayout( new javax.swing.BoxLayout( jPanelMain, 1 ) );
		jPanelMain.add( jPanelSettings );
		jPanelMain.add( jPanelScrollPane );
		jPanelMain.add( jPanelButtons );
		
		setBounds(GalleryRemote.getInstance().properties.getMainBounds());

		setJMenuBar( jMenuBar );
		getContentPane().setLayout( new java.awt.BorderLayout( 0, 0 ) );
		setTitle( "Gallery Remote" );
		getContentPane().add( jPanelMain, "Center" );

		jButtonFetchAlbums.addActionListener(
			new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					jButtonFetchAlbumsActionPerformed( e );
				}
			} );
		jButtonAddFiles.addActionListener(
			new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					jButtonAddFilesActionPerformed( e );
				}
			} );
		jButtonDeleteFiles.addActionListener(
			new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					jButtonDeleteFilesActionPerformed( e );
				}
			} );
		jButtonUpload.addActionListener(
			new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					jButtonUploadActionPerformed( e );
				}
			} );
		jButtonUp.addActionListener(
			new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					jButtonUpActionPerformed( e );
				}
			} );
		jButtonDown.addActionListener(
			new java.awt.event.ActionListener()
			{
				public void actionPerformed( java.awt.event.ActionEvent e )
				{
					jButtonDownActionPerformed( e );
				}
			} );
		jListItems.addListSelectionListener(
			new ListSelectionListener()
			{
				public void valueChanged( ListSelectionEvent e )
				{
					jListSelectionPerformed( e );
				}
			} );
		jListItems.addKeyListener(
			new KeyAdapter()
			{
				public void keyPressed( KeyEvent e )
				{
					jListKeyPressed( e );
				}
			} );
		addWindowListener(
			new java.awt.event.WindowAdapter()
			{
				public void windowClosing( java.awt.event.WindowEvent e )
				{
					thisWindowClosing( e );
				}
			} );

		jMenuFileQuit.addActionListener( new menuBarActionListener() );
		jMenuHelpAbout.addActionListener( new menuBarActionListener() );

		jMenuOptionsPreview.addItemListener(
			new java.awt.event.ItemListener()
			{
				public void itemStateChanged( ItemEvent e )
				{
					jMenuOptionsPreviewItemSelected( e );
				}
			} );
		jMenuOptionsThumb.addItemListener(
			new java.awt.event.ItemListener()
			{
				public void itemStateChanged( ItemEvent e )
				{
					jMenuOptionsThumbItemSelected( e );
				}
			} );
		jMenuOptionsPath.addItemListener(
			new java.awt.event.ItemListener()
			{
				public void itemStateChanged( ItemEvent e )
				{
					jMenuOptionsPathItemSelected( e );
				}
			} );

		jPreview.initComponents( );
		jPreview.addWindowListener(
			new java.awt.event.WindowAdapter()
			{
				public void windowClosing( java.awt.event.WindowEvent e )
				{
					jMenuOptionsPreview.setSelected( false );
				}
			} );

		updateUI();

		if ( GalleryRemote.getInstance().properties.getShowPreview() )
		{
			jPreview.setVisible( true );
		}
	}

	/*public void addNotify()
	{
		super.addNotify();

		if ( mShown )
			return;

		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if ( jMenuBar != null && !savedHeight )
		{
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize( dimension );
		}

		mShown = true;
	}*/

	// Close the window when the close box is clicked
	void thisWindowClosing( java.awt.event.WindowEvent e )
	{
		//mPropertiesFile.setProperty( "url", jTextURL.getText() );
		//mPropertiesFile.setProperty( "username", jTextUsername.getText() );

		//mPropertiesFile.setProperty( "mainwidth", "" + getSize().width );
		//mPropertiesFile.setProperty( "mainheight", "" + getSize().height );
		//mPropertiesFile.setProperty( "mainx", "" + getLocation().x );
		//mPropertiesFile.setProperty( "mainy", "" + getLocation().y );

		//mPropertiesFile.setProperty( "showpreview", String.valueOf( showPreview ) );
		//mPropertiesFile.setProperty( "previewwidth", "" + jPreview.getSize().width );
		//mPropertiesFile.setProperty( "previewheight", "" + jPreview.getSize().height );
		//mPropertiesFile.setProperty( "previewx", "" + jPreview.getLocation().x );
		//mPropertiesFile.setProperty( "previewy", "" + jPreview.getLocation().y );

		GalleryRemote.getInstance().properties.write();
		
		setVisible( false );
		dispose();
		System.exit( 0 );
	}

	//-------------------------------------------------------------------------
	//-- Update the UI
	//-------------------------------------------------------------------------
	private void updateUI()
	{
		//-- if the list is empty, disable the Upload ---
		jButtonUpload.setEnabled( ( mAlbum.sizePictures() > 0 ) &&
			!mInProgress &&
			( jComboBoxAlbums.getSelectedIndex() >= 0 ) );
		jButtonAddFiles.setEnabled( !mInProgress );
		jButtonFetchAlbums.setEnabled( !mInProgress );
		
		jProgressBar.setStringPainted( mInProgress );
	}

	private void updateAlbumCombo()
	{
		Runnable doUpdate =
			new Runnable()
			{
				public void run()
				{
					//-- build the list ---
					jComboBoxAlbums.removeAllItems();
					Iterator iter = mAlbumList.iterator();
					while ( iter.hasNext() )
					{
						Hashtable h = (Hashtable) iter.next();
						jComboBoxAlbums.addItem( (String) h.get( "title" ) );
					}

				}
			};
		SwingUtilities.invokeLater( doUpdate );
	}

	//-------------------------------------------------------------------------
	//-- Update the Status
	//-------------------------------------------------------------------------
	private void updateStatus( String message )
	{
		final String m = message;
		Runnable doUpdate =
			new Runnable()
			{
				public void run()
				{
					jLabelStatus.setText( m );
				}
			};
		SwingUtilities.invokeLater( doUpdate );
	}

	//-------------------------------------------------------------------------
	//-- Update the Progress
	//-------------------------------------------------------------------------
	private void updateProgress( int value )
	{
		final int val = value;
		Runnable doUpdate =
			new Runnable()
			{
				public void run()
				{
					jProgressBar.setValue( val );
				}
			};
		SwingUtilities.invokeLater( doUpdate );
	}

	//-------------------------------------------------------------------------
	//-- Add Files Button
	//-------------------------------------------------------------------------
	public void jButtonAddFilesActionPerformed( java.awt.event.ActionEvent e )
	{
		//-- get any new files ---
		File[] files = AddFileDialog.addFiles( this );

		if ( files != null )
		{
			mAlbum.addPictures(files);
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
			thumbnailLoader.preloadThumbnails( files );

			updateUI();
		}
	}

	//-------------------------------------------------------------------------
	//-- Delete Files Button
	//-------------------------------------------------------------------------
	public void jButtonDeleteFilesActionPerformed( java.awt.event.ActionEvent e )
	{
		deleteSelectedFiles();
	}

	//-------------------------------------------------------------------------
	//-- Upload Action
	//-------------------------------------------------------------------------
	public void jButtonUploadActionPerformed( java.awt.event.ActionEvent e )
	{
		mGalleryComm.setURLString( jTextURL.getText() );
		mGalleryComm.setUsername( jTextUsername.getText() );
		mGalleryComm.setPassword( jPasswordField.getText() );

		int index = jComboBoxAlbums.getSelectedIndex();
		Hashtable h = (Hashtable) mAlbumList.get( index );
		mGalleryComm.setAlbum( (String) h.get( "name" ) );
		jListItems.disable();
		mGalleryComm.uploadFiles( mAlbum.getFileList() );

		mInProgress = true;
		updateUI();

		jProgressBar.setMaximum( mAlbum.sizePictures() );

		mTimer = new javax.swing.Timer( ONE_SECOND,
			new ActionListener()
			{
				public void actionPerformed( ActionEvent evt )
				{
					updateProgress( mGalleryComm.getUploadedCount() );
					updateStatus( mGalleryComm.getStatus() );
					if ( mGalleryComm.done() )
					{
						mTimer.stop();

						//-- reset the UI ---
						updateProgress( jProgressBar.getMinimum() );
						mAlbum.clearPictures();
						mInProgress = false;
						jListItems.enable();
						updateUI();

					}
				}
			} );

		mTimer.start();
	}


	//-------------------------------------------------------------------------
	//-- Move File Up Action
	//-------------------------------------------------------------------------
	public void jButtonUpActionPerformed( java.awt.event.ActionEvent e )
	{
		moveFileUp();
	}

	//-------------------------------------------------------------------------
	//-- Move File Down Action
	//-------------------------------------------------------------------------
	public void jButtonDownActionPerformed( java.awt.event.ActionEvent e )
	{
		moveFileDown();
	}

	//-------------------------------------------------------------------------
	//-- Selected in the list Action
	//-------------------------------------------------------------------------
	public void jListSelectionPerformed( ListSelectionEvent e )
	{
		int sel = jListItems.getSelectedIndex();

		if ( GalleryRemote.getInstance().properties.getShowPreview() )
		{
			if ( sel != -1 )
			{
				String filename = ( mAlbum.getPicture( sel ).getSource() ).getPath();
				jPreview.displayFile( filename );
				thumbnailLoader.preloadThumbnailFirst( filename );
			}
			else
			{
				jPreview.displayFile( null );
			}

			if ( !jPreview.isVisible() )
			{
				jPreview.setVisible( true );
			}
		}

		jButtonDeleteFiles.setEnabled( sel != -1 );

		int selN = jListItems.getSelectedIndices().length;
		jButtonUp.setEnabled( selN == 1 );
		jButtonDown.setEnabled( selN == 1 );
	}

	public void jListKeyPressed( KeyEvent e )
	{
		int vKey = e.getKeyCode();

		switch ( vKey )
		{
						case KeyEvent.VK_DELETE:
						case KeyEvent.VK_BACK_SPACE:
							deleteSelectedFiles();
							break;
						case KeyEvent.VK_LEFT:
							moveFileUp();
							break;
						case KeyEvent.VK_RIGHT:
							moveFileDown();
							break;
		}
	}

	//-------------------------------------------------------------------------
	//-- Fetch Albums Action
	//-------------------------------------------------------------------------
	public void jButtonFetchAlbumsActionPerformed( java.awt.event.ActionEvent e )
	{
		mGalleryComm.setURLString( jTextURL.getText() );
		mGalleryComm.setUsername( jTextUsername.getText() );
		mGalleryComm.setPassword( jPasswordField.getText() );
		mGalleryComm.fetchAlbums();

		mInProgress = true;
		updateUI();

		mTimer = new javax.swing.Timer( ONE_SECOND,
			new ActionListener()
			{
				public void actionPerformed( ActionEvent evt )
				{
					updateStatus( mGalleryComm.getStatus() );
					if ( mGalleryComm.done() )
					{
						mTimer.stop();

						//-- reset the UI ---
						mAlbumList = mGalleryComm.getAlbumList();
						mInProgress = false;
						updateAlbumCombo();
						updateUI();
					}
				}
			} );

		mTimer.start();
	}

	//-------------------------------------------------------------------------
	//-- Open / Close the preview window
	//-------------------------------------------------------------------------
	public void jMenuOptionsPreviewItemSelected( java.awt.event.ItemEvent e )
	{
		if ( e.getStateChange() == ItemEvent.SELECTED )
		{
			jPreview.show();
			GalleryRemote.getInstance().properties.setShowPreview(true);
		}
		else
		{
			jPreview.hide();
			GalleryRemote.getInstance().properties.setShowPreview(false);
		}
	}

	//-------------------------------------------------------------------------
	//-- Hide / Show Thumbnails
	//-------------------------------------------------------------------------
	public void jMenuOptionsThumbItemSelected( java.awt.event.ItemEvent e )
	{
		boolean showThumbnails = ( e.getStateChange() == ItemEvent.SELECTED ) ? true : false;
		if ( GalleryRemote.getInstance().properties.getShowThumbnails() )
		{
			thumbnailLoader.preloadThumbnailFiles( mAlbum.getPictures() );
		}
		else
		{
			thumbnailLoader.cancelLoad();
		}

		showThumbs( GalleryRemote.getInstance().properties.getShowThumbnails() );
		GalleryRemote.getInstance().properties.setShowThumbnails(showThumbnails);
		jListItems.repaint();
	}

	//-------------------------------------------------------------------------
	//-- Hide / Show Path Info
	//-------------------------------------------------------------------------
	public void jMenuOptionsPathItemSelected( java.awt.event.ItemEvent e )
	{
		GalleryRemote.getInstance().properties.setShowPath( ( e.getStateChange() == ItemEvent.SELECTED ) ? true : false);
		jListItems.repaint();
	}

	public void deleteSelectedFiles()
	{
		int[] indices = jListItems.getSelectedIndices();
		
		mAlbum.removePictures(indices);

		dontReselect = true;

		//updateFileList();
	}

	public void moveFileUp()
	{
		int sel = jListItems.getSelectedIndex();

		if ( sel > 0 )
		{
			Picture buf = mAlbum.getPicture( sel );
			mAlbum.setPicture( sel, mAlbum.getPicture( sel - 1 ) );
			mAlbum.setPicture( sel - 1, buf );
			jListItems.setSelectedIndex( sel - 1 );
			//updateFileList();
		}
	}

	public void moveFileDown()
	{
		int sel = jListItems.getSelectedIndex();

		if ( sel < mAlbum.sizePictures() - 1 )
		{
			Picture buf = mAlbum.getPicture( sel );
			mAlbum.setPicture( sel, mAlbum.getPicture( sel + 1 ) );
			mAlbum.setPicture( sel + 1, buf );
			jListItems.setSelectedIndex( sel + 1 );
			//updateFileList();
		}
	}

	//-------------------------------------------------------------------------
	//-- Show / Hide Thumbnails
	//-------------------------------------------------------------------------
	public void showThumbs( boolean show )
	{
		//jListItems.setCellRenderer( new FileCellRenderer() );
		if ( show )
		{
			jListItems.setFixedCellHeight( GalleryRemote.getInstance().properties.getThumbnailSize().height + 4 );
		}
		else
		{
			//jListItems.setCellRenderer(new DefaultListCellRenderer());
			jListItems.setFixedCellHeight( -1 );
		}
	}

	public ImageIcon getThumbnail( String filename )
	{
		if ( filename == null )
		{
			return null;
		}

		ImageIcon r = (ImageIcon) thumbnails.get( filename );

		if ( r == null )
		{
			r = defaultThumbnail;
		}

		return r;
	}

	public ImageIcon loadThumbnail( String filename )
	{
		ImageIcon r = new ImageIcon( filename );
		Dimension d = PreviewFrame.getSizeKeepRatio( new Dimension( r.getIconWidth(), r.getIconHeight() ), GalleryRemote.getInstance().properties.getThumbnailSize() );

		long start = System.currentTimeMillis();
		Image scaled = r.getImage().getScaledInstance( d.width, d.height, highQualityThumbnails ? Image.SCALE_SMOOTH : Image.SCALE_FAST );
		r.getImage().flush();
		r.setImage( scaled );

		thumbnails.put( filename, r );
		System.out.println( "Time: " + ( System.currentTimeMillis() - start ) );

		return r;
	}

	/**
	 *  Menu bar handler
	 *
	 *@author     paour
	 *@created    11 août 2002
	 */
	public class menuBarActionListener implements ActionListener
	{
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
			if ( e.getActionCommand().equals( "File.Quit" ) )
			{
				thisWindowClosing( null );
			}
			else if ( e.getActionCommand().equals( "Help.About" ) )
			{
				try
				{
					AboutBox ab = new AboutBox();
					ab.initComponents();
					ab.setVersionString( APP_VERSION_STRING );
					ab.setVisible( true );
				}
				catch ( Exception err )
				{
					err.printStackTrace();
				}
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
		public Component getListCellRendererComponent(
		JList list, Object value, int index,
		boolean selected, boolean hasFocus )
		{
			super.getListCellRendererComponent( list, value, index, selected, hasFocus );
			
			Picture p = mAlbum.getPicture( index );
			File f = p.getSource();
			
			if ( GalleryRemote.getInstance().properties.getShowThumbnails() )
			{
				ImageIcon icon = getThumbnail( f.getPath() );
				setIcon( icon );
				setIconTextGap( 4 + GalleryRemote.getInstance().properties.getThumbnailSize().width - icon.getIconWidth() );
			}
			
			String text = f.getName();
			if ( GalleryRemote.getInstance().properties.getShowPath() )
			{
				text += " [" + f.getParent() + "]";
			}
			setText(text);
			
			return this;
		}
	}

	/**
	 *  Utility class that loads thumbnails asynchronously
	 *
	 *@author     paour
	 *@created    11 août 2002
	 */
	class ThumbnailLoader implements Runnable
	{
		boolean stillRunning = false;
		Stack toLoad = new Stack();

		public void run()
		{
			//System.out.println("Starting " + iFilename);
			while ( !toLoad.isEmpty() )
			{
				String filename = (String) toLoad.pop();

				if ( thumbnails.get( filename ) == null )
				{
					loadThumbnail( filename );

					jListItems.repaint();
				}
			}
			stillRunning = false;

			//System.out.println("Ending");
		}

		public void preloadThumbnail( String filename )
		{
			//System.out.println("loadPreview " + filename);

			toLoad.add( 0, filename );

			rerun();
		}

		public void preloadThumbnailFirst( String filename )
		{
			//System.out.println("loadPreview " + filename);

			toLoad.push( filename );

			rerun();
		}

		/*public void preloadThumbnails( Vector filenames )
		{
			//System.out.println("loadPreview " + filenames);

			toLoad.addAll( 0, filenames );

			rerun();
		}*/

		public void preloadThumbnailFiles( Enumeration e )
		{
			//System.out.println("loadPreview " + files);

			while ( e.hasMoreElements() )
			{
				toLoad.add( 0, ( (Picture) e.nextElement() ).getSource().getPath() );
			}

			rerun();
		}

		public void preloadThumbnails( File[] filenames )
		{
			//System.out.println("loadPreview " + filename);

			for ( int i = 0; i < filenames.length; i++ )
			{
				toLoad.add( 0, ( (File) filenames[i] ).getPath() );
			}

			rerun();
		}

		void rerun()
		{
			if ( !stillRunning && GalleryRemote.getInstance().properties.getShowThumbnails() )
			{
				stillRunning = true;
				//System.out.println("Calling Start");
				new Thread( this ).start();
			}
		}

		void cancelLoad()
		{
			toLoad.clear();
		}
	}

	/**
	 *  Drag and drop handler
	 */
	public class DroppableList extends JList
			 implements DropTargetListener
	{
		DropTarget dropTarget = new DropTarget( this, this );
		MainFrame mDaddy;

		public DroppableList( MainFrame daddy )
		{
			setModel( new DefaultListModel() );
			mDaddy = daddy;
		}

		public void dragEnter( DropTargetDragEvent dropTargetDragEvent )
		{
			dropTargetDragEvent.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
		}

		public void dragExit( DropTargetEvent dropTargetEvent ) { }

		public void dragOver( DropTargetDragEvent dropTargetDragEvent ) { }

		public void dropActionChanged( DropTargetDragEvent dropTargetDragEvent ) { }

		public synchronized void drop( DropTargetDropEvent dropTargetDropEvent )
		{
			try
			{
				Transferable tr = dropTargetDropEvent.getTransferable();
				if ( tr.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) && this.isEnabled() )
				{
					dropTargetDropEvent.acceptDrop(
							DnDConstants.ACTION_COPY_OR_MOVE );
					java.util.List fileList = (java.util.List)
							tr.getTransferData( DataFlavor.javaFileListFlavor );
					Iterator iterator = fileList.iterator();
					while ( iterator.hasNext() )
					{
						File file = (File) iterator.next();
						mAlbum.addPicture( file );
						thumbnailLoader.preloadThumbnail( file.getPath() );
					}
					dropTargetDropEvent.getDropTargetContext().dropComplete( true );
					mDaddy.updateUI();
				}
				else
				{
					System.err.println( "Rejected" );
					dropTargetDropEvent.rejectDrop();
				}
			}
			catch ( IOException io )
			{
				io.printStackTrace();
				dropTargetDropEvent.rejectDrop();
			}
			catch ( UnsupportedFlavorException ufe )
			{
				ufe.printStackTrace();
				dropTargetDropEvent.rejectDrop();
			}
		}
	}
}

