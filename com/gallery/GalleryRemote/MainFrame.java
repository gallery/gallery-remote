/*
 * Gallery Remote - a File Upload Utility for Gallery 
 *
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2001 Bharat Mediratta
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.gallery.GalleryRemote;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import HTTPClient.*;
import java.io.*;
import java.net.URL;

public class MainFrame extends javax.swing.JFrame {

	public final static String APP_VERSION_STRING = "0.3";
	public final static int ONE_SECOND = 1000;

	javax.swing.JMenuBar jMenuBar = new javax.swing.JMenuBar();
	javax.swing.JMenu jMenuFile = new javax.swing.JMenu();
	javax.swing.JMenuItem jMenuFileQuit = new javax.swing.JMenuItem();
	javax.swing.JMenu jMenuHelp = new javax.swing.JMenu();
	javax.swing.JMenuItem jMenuHelpAbout = new javax.swing.JMenuItem();
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
	DroppableList jListItems = new DroppableList(this);
	javax.swing.JPanel jPanelButtons = new javax.swing.JPanel();
	javax.swing.JPanel jPanelButtonsTop = new javax.swing.JPanel();
	javax.swing.JButton jButtonAddFiles = new javax.swing.JButton();
	javax.swing.JButton jButtonUpload = new javax.swing.JButton();
	javax.swing.JPanel jPanelButtonsBottom = new javax.swing.JPanel();
	javax.swing.JTextField jLabelStatus = new javax.swing.JTextField();
	javax.swing.JProgressBar jProgressBar = new javax.swing.JProgressBar();

	private GalleryComm mGalleryComm;
	private ArrayList mFileList;
	private ArrayList mAlbumList;
	private boolean mInProgress = false;
	private Timer mTimer;
	private PropertiesFile mPropertiesFile;
	
	public MainFrame() {
		
		mGalleryComm = new GalleryComm();
		mFileList = new ArrayList();
		mAlbumList = new ArrayList();
		
		//-- load the properties file ---
		mPropertiesFile = new PropertiesFile("remote");
			
	}
	
	
	public void initComponents() throws Exception {

		jMenuBar.setVisible(true);
		jMenuBar.add(jMenuFile);
		jMenuBar.add(jMenuHelp);

		jMenuFile.setVisible(true);
		jMenuFile.setText("File");
		jMenuFile.add(jMenuFileQuit);

		jMenuFileQuit.setVisible(true);
		jMenuFileQuit.setText("Quit");
		jMenuFileQuit.setActionCommand("File.Quit");

		jMenuHelp.setVisible(true);
		jMenuHelp.setText("Help");
		jMenuHelp.add(jMenuHelpAbout);

		jMenuHelpAbout.setVisible(true);
		jMenuHelpAbout.setText("About");
		jMenuHelpAbout.setActionCommand("Help.About");

		//-- the settongs panel ---
		jLabelURL.setSize(new java.awt.Dimension(90, 20));
		jLabelURL.setLocation(new java.awt.Point(7, 20));
		jLabelURL.setVisible(true);
		jLabelURL.setText("Gallery URL");
		jLabelURL.setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
		jLabelURL.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jLabelUser.setSize(new java.awt.Dimension(90, 20));
		jLabelUser.setLocation(new java.awt.Point(9, 42));
		jLabelUser.setVisible(true);
		jLabelUser.setText("Username");
		jLabelUser.setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
		jLabelUser.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jLabelPassword.setSize(new java.awt.Dimension(90, 20));
		jLabelPassword.setLocation(new java.awt.Point(9, 64));
		jLabelPassword.setVisible(true);
		jLabelPassword.setText("Password");
		jLabelPassword.setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
		jLabelPassword.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jTextURL.setSize(new java.awt.Dimension(305, 20));
		jTextURL.setLocation(new java.awt.Point(106, 20));
		jTextURL.setVisible(true);
		jTextURL.setText(mPropertiesFile.getProperty("url"));

		jTextUsername.setSize(new java.awt.Dimension(180, 20));
		jTextUsername.setLocation(new java.awt.Point(106, 42));
		jTextUsername.setVisible(true);
		jTextUsername.setText(mPropertiesFile.getProperty("username"));

		jPasswordField.setSize(new java.awt.Dimension(180, 20));
		jPasswordField.setLocation(new java.awt.Point(106, 64));
		jPasswordField.setVisible(true);
		jPasswordField.setText("");

		jLabelAlbum.setSize(new java.awt.Dimension(103, 20));
		jLabelAlbum.setLocation(new java.awt.Point(-4, 91));
		jLabelAlbum.setVisible(true);
		jLabelAlbum.setText("Upload to Album");
		jLabelAlbum.setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
		jLabelAlbum.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

		jButtonFetchAlbums.setSize(new java.awt.Dimension(117, 25));
		jButtonFetchAlbums.setEnabled(false);
		jButtonFetchAlbums.setLocation(new java.awt.Point(294, 92));
		jButtonFetchAlbums.setVisible(true);
		jButtonFetchAlbums.setText("Fetch Albums");

		jComboBoxAlbums.setSize(new java.awt.Dimension(180, 25));
		jComboBoxAlbums.setLocation(new java.awt.Point(106, 92));
		jComboBoxAlbums.setVisible(true);

		jPanelSettings.setVisible(true);
		jPanelSettings.setLayout(null);
		jPanelSettings.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createEtchedBorder(), "Destination Gallery"));
		jPanelSettings.setPreferredSize(new java.awt.Dimension(420, 125));
		jPanelSettings.setMinimumSize(new java.awt.Dimension(420, 125));
		jPanelSettings.setMaximumSize(new java.awt.Dimension(4000, 125));
		jPanelSettings.add(jLabelURL);
		jPanelSettings.add(jLabelUser);
		jPanelSettings.add(jLabelPassword);
		jPanelSettings.add(jTextURL);
		jPanelSettings.add(jTextUsername);
		jPanelSettings.add(jPasswordField);
		jPanelSettings.add(jLabelAlbum);
		jPanelSettings.add(jButtonFetchAlbums);
		jPanelSettings.add(jComboBoxAlbums);
		
		//-- the scroll pane ---
		jScrollPane.setVisible(true);
		jScrollPane.getViewport().add(jListItems);
		jListItems.setVisible(true);
		jPanelScrollPane.setLayout(new BorderLayout());
		jPanelScrollPane.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createEtchedBorder(), "Files to Upload (Drag and Drop files into this panel)"));
		jPanelScrollPane.setVisible(true);
		jPanelScrollPane.add(jScrollPane);
		
		jListItems = new DroppableList(this);
		//jListItems.setCellRenderer(new FileCellRenderer());

		jScrollPane.getViewport().add(jListItems);

		//-- the button panel ---
		jButtonAddFiles.setSize(new java.awt.Dimension(200, 25));
		jButtonAddFiles.setLocation(new java.awt.Point(0, 0));
		jButtonAddFiles.setVisible(true);
		jButtonAddFiles.setText("Browse for Files to Upload...");

		jButtonUpload.setSize(new java.awt.Dimension(200, 25));
		jButtonUpload.setLocation(new java.awt.Point(220, 0));
		jButtonUpload.setVisible(true);
		jButtonUpload.setText("Upload Now!");

		jPanelButtonsTop.setVisible(true);
		jPanelButtonsTop.setLayout(null);
		jPanelButtonsTop.setPreferredSize(new java.awt.Dimension(420, 30));
		jPanelButtonsTop.add(jButtonAddFiles);
		jPanelButtonsTop.add(jButtonUpload);

		jLabelStatus.setVisible(true);
		jLabelStatus.setPreferredSize(new java.awt.Dimension(420, 22));
		jLabelStatus.setMinimumSize(new java.awt.Dimension(4,22));
		jLabelStatus.setMaximumSize(new java.awt.Dimension(4000, 22));
		jLabelStatus.setText("Drag and Drop files to Upload into panel above or Browse for them.");
		jLabelStatus.setEnabled(false);

		jProgressBar.setVisible(true);
		jProgressBar.setStringPainted(true);

		jPanelButtonsBottom.setVisible(true);
		jPanelButtonsBottom.setLayout(new javax.swing.BoxLayout(jPanelButtonsBottom, 1));
		jPanelButtonsBottom.setPreferredSize(new java.awt.Dimension(420, 46));
		jPanelButtonsBottom.add(jLabelStatus);
		jPanelButtonsBottom.add(Box.createVerticalStrut(2));
		jPanelButtonsBottom.add(jProgressBar);
		
		jPanelButtons.setVisible(true);
		jPanelButtons.setLayout(new javax.swing.BoxLayout(jPanelButtons, 1));
		jPanelButtons.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		jPanelButtons.add(jPanelButtonsTop);
		jPanelButtons.add(jPanelButtonsBottom);
		
		jPanelMain.setVisible(true);
		jPanelMain.setLayout(new javax.swing.BoxLayout(jPanelMain, 1));
		jPanelMain.add(jPanelSettings);
		jPanelMain.add(jPanelScrollPane);
		jPanelMain.add(jPanelButtons);
		
		setLocation(new java.awt.Point(2, 0));
		setSize(new java.awt.Dimension(435, 573));
		setJMenuBar(jMenuBar);
		getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
		setTitle("Gallery Remote");
		getContentPane().add(jPanelMain, "Center");


		jButtonFetchAlbums.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButtonFetchAlbumsActionPerformed(e);
			}
		});
		jButtonAddFiles.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButtonAddFilesActionPerformed(e);
			}
		});
		jButtonUpload.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButtonUploadActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		jMenuFileQuit.addActionListener(new menuBarActionListener());
		jMenuHelpAbout.addActionListener(new menuBarActionListener());
		
		updateUI();
	}
  
  	private boolean mShown = false;
  	
	public void addNotify() {
		super.addNotify();
		
		if (mShown)
			return;
			
		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
	
		mPropertiesFile.setProperty("url", jTextURL.getText());
		mPropertiesFile.setProperty("username", jTextUsername.getText());
		mPropertiesFile.write();
	
		setVisible(false);
		dispose();
		System.exit(0);
	}
	
	//-------------------------------------------------------------------------
	//-- Update the UI
	//-------------------------------------------------------------------------
	private void updateUI() {
	
		Runnable doUpdate = new Runnable() {
			public void run() {
			
				//-- if the list is empty, disable the Upload ---
				jButtonUpload.setEnabled((mFileList.size() > 0) && 
					!mInProgress && 
					(jComboBoxAlbums.getSelectedIndex() >= 0));
				jButtonAddFiles.setEnabled(!mInProgress);
				jButtonFetchAlbums.setEnabled(!mInProgress);
				
				jProgressBar.setStringPainted(mInProgress);

				//-- build the file list ---
				ArrayList filenames = new ArrayList();
				
				Iterator iter = mFileList.iterator();
				while (iter.hasNext()) {
					File file = (File) iter.next();
					filenames.add(file.getName() + " [" + file.getParent() + "]");
				}
				
				jListItems.setListData(filenames.toArray());		
			
			}
		};
		SwingUtilities.invokeLater(doUpdate);
	};
	
	
	//-------------------------------------------------------------------------
	//-- Update the Status
	//-------------------------------------------------------------------------
	private void updateFileList() {
	
		Runnable doUpdate = new Runnable() {
			public void run() {
				//-- build the file list ---
				ArrayList filenames = new ArrayList();
				
				Iterator iter = mFileList.iterator();
				while (iter.hasNext()) {
					File file = (File) iter.next();
					filenames.add(file.getName() + " [" + file.getParent() + "]");
				}
				
				jListItems.setListData(filenames.toArray());
			}
		};
		SwingUtilities.invokeLater(doUpdate);

	};	

	//-------------------------------------------------------------------------
	//-- Update the Status
	//-------------------------------------------------------------------------
	private void updateAlbumCombo() {
	
		Runnable doUpdate = new Runnable() {
			public void run() {
				//-- build the list ---
				jComboBoxAlbums.removeAllItems();
				Iterator iter = mAlbumList.iterator();
				while (iter.hasNext()) {
					Hashtable h = (Hashtable) iter.next();
					jComboBoxAlbums.addItem((String) h.get("title"));
				}
				
			}
		};
		SwingUtilities.invokeLater(doUpdate);

	};	
	
	//-------------------------------------------------------------------------
	//-- Update the Status
	//-------------------------------------------------------------------------
	private void updateStatus(String message) {
	
		final String m = message;
		Runnable doUpdate = new Runnable() {
			public void run() {
				jLabelStatus.setText(m);
			}
		};
		SwingUtilities.invokeLater(doUpdate);

	};	
	
	//-------------------------------------------------------------------------
	//-- Update the Status
	//-------------------------------------------------------------------------
	private void updateProgress(int value) {
	
		final int val = value;
		Runnable doUpdate = new Runnable() {
			public void run() {
				jProgressBar.setValue(val);
			}
		};
		SwingUtilities.invokeLater(doUpdate);

	};
	
	//-------------------------------------------------------------------------
	//-- Add Files Button
	//-------------------------------------------------------------------------
	public void jButtonAddFilesActionPerformed(java.awt.event.ActionEvent e) {
			
		//-- get any new files ---
		File[] files = AddFileDialog.addFiles(this);
		
		if (files != null) {
			
			mFileList.addAll(Arrays.asList(files));
			Object[] items = mFileList.toArray();
			Arrays.sort(items, new Comparator() {
				public int compare (Object o1, Object o2) {
					String f1 = ((File) o1).getAbsolutePath();
					String f2 = ((File) o2).getAbsolutePath();
					return (f1.compareToIgnoreCase(f2));
				}
				public boolean equals (Object o1, Object o2) {
					String f1 = ((File) o1).getAbsolutePath();
					String f2 = ((File) o2).getAbsolutePath();
					return (f1.equals(f2));
				}
			});
			
			mFileList.clear();
			mFileList.addAll(Arrays.asList(items));
			
			updateFileList();
			updateUI();
		}
	}
	
	//-------------------------------------------------------------------------
	//-- Upload Action
	//-------------------------------------------------------------------------	
	public void jButtonUploadActionPerformed(java.awt.event.ActionEvent e) {
			
		mGalleryComm.setURLString(jTextURL.getText());
		mGalleryComm.setUsername(jTextUsername.getText());
		mGalleryComm.setPassword(jPasswordField.getText());

		int index = jComboBoxAlbums.getSelectedIndex();
		Hashtable h = (Hashtable)mAlbumList.get(index);
		mGalleryComm.setAlbum((String) h.get("name"));
		
		mGalleryComm.uploadFiles(mFileList);
		
		mInProgress = true;
		updateUI();
		
		jProgressBar.setMaximum(mFileList.size());
		
		mTimer = new Timer(ONE_SECOND, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateProgress(mGalleryComm.getUploadedCount());
				updateStatus(mGalleryComm.getStatus());
				if (mGalleryComm.done()) {
					mTimer.stop();

					//-- reset the UI ---
					updateProgress(jProgressBar.getMinimum());
					mFileList.clear();
					mInProgress = false;
					updateUI();

				}
			}
		});
		
		mTimer.start();
	}
	
	//-------------------------------------------------------------------------
	//-- Fetch Albums Action
	//-------------------------------------------------------------------------	
	public void jButtonFetchAlbumsActionPerformed(java.awt.event.ActionEvent e) {

		mGalleryComm.setURLString(jTextURL.getText());
		mGalleryComm.setUsername(jTextUsername.getText());
		mGalleryComm.setPassword(jPasswordField.getText());
		mGalleryComm.fetchAlbums();
		
		mInProgress = true;
		updateUI();
		
		jProgressBar.setMaximum(mFileList.size());
		
		mTimer = new Timer(ONE_SECOND, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateStatus(mGalleryComm.getStatus());
				if (mGalleryComm.done()) {
					mTimer.stop();

					//-- reset the UI ---
					mAlbumList = mGalleryComm.getAlbumList();
					mInProgress = false;
					updateAlbumCombo();
					updateUI();
				}
			}
		});
		
		mTimer.start();
			
	}
	
	//-------------------------------------------------------------------------
	//-- The menu bar action listener
	//-------------------------------------------------------------------------	
	public class menuBarActionListener implements ActionListener
	{
	
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getActionCommand().equals("File.Quit")) {
			
				thisWindowClosing(null);
				
			} else if (e.getActionCommand().equals("Help.About")) {
			
				try {
					AboutBox ab = new AboutBox();
					ab.initComponents();
					ab.setVersionString(APP_VERSION_STRING);
					ab.setVisible(true);
				}
				catch (Exception err) {
					err.printStackTrace();
				}			
			} else { 
			}
		}

	}
	
	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	public class FileCellRenderer implements ListCellRenderer
	{
		DefaultListCellRenderer listCellRenderer =
		  new DefaultListCellRenderer();
		public Component getListCellRendererComponent(
			JList list, Object value, int index,
			boolean selected, boolean hasFocus)
		{
			listCellRenderer.getListCellRendererComponent(
			  list, value, index, selected, hasFocus);
			listCellRenderer.setText(getValueString(value));
			return listCellRenderer;
		}
		private String getValueString(Object value)
		{
			String returnString = "null";
			if (value != null) {
				if (value instanceof Hashtable) {
					Hashtable h = (Hashtable)value;
					String name = (String)h.get("name");
					String parent = (String)h.get("parent");
					returnString = name + " [" + parent + "]";
				} else {
					returnString = "X: " + value.toString();
				}
			}
			return returnString;
		}
	}

	//-------------------------------------------------------------------------
	//-- 
	//-------------------------------------------------------------------------	
	public class DroppableList extends JList
		implements DropTargetListener
	{
		DropTarget dropTarget = new DropTarget (this, this);
		MainFrame mDaddy;
		
		public DroppableList(MainFrame daddy)
		{
			setModel(new DefaultListModel());
			mDaddy = daddy;
		}

		public void dragEnter (DropTargetDragEvent dropTargetDragEvent)
		{
			dropTargetDragEvent.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
		}

		public void dragExit (DropTargetEvent dropTargetEvent) {}
		public void dragOver (DropTargetDragEvent dropTargetDragEvent) {}
		public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent){}

		public synchronized void drop (DropTargetDropEvent dropTargetDropEvent)
		{
			try
			{
				Transferable tr = dropTargetDropEvent.getTransferable();
				if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor))
				{
					dropTargetDropEvent.acceptDrop (
						DnDConstants.ACTION_COPY_OR_MOVE);
					java.util.List fileList = (java.util.List)
						tr.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator iterator = fileList.iterator();
					while (iterator.hasNext())
					{
						File file = (File)iterator.next();
						mFileList.add(file);
					}
					dropTargetDropEvent.getDropTargetContext().dropComplete(true);
					mDaddy.updateUI();
				} else {
					System.err.println ("Rejected");
					dropTargetDropEvent.rejectDrop();
				}
			} catch (IOException io) {
				io.printStackTrace();
				dropTargetDropEvent.rejectDrop();
			} catch (UnsupportedFlavorException ufe) {
				ufe.printStackTrace();
				dropTargetDropEvent.rejectDrop();
			}
		}

	}


}
