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
import javax.swing.event.*;
import java.util.*;
import HTTPClient.*;
import java.io.*;
import java.net.URL;
import javax.swing.Timer;

public class MainFrame extends javax.swing.JFrame {
    
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
    DroppableList jListItems = new DroppableList(this);
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
    private ArrayList mFileList;
    private ArrayList mAlbumList;
    private boolean mInProgress = false;
    private javax.swing.Timer mTimer;
    private PropertiesFile mPropertiesFile;
    private boolean savedHeight = false;
    
    public static final String DEFAULT_IMAGE = "default.gif";
    
    Hashtable thumbnails = new Hashtable();
    Dimension thumbnailSize = new Dimension(50, 50);
    ImageIcon defaultThumbnail = loadThumbnail(DEFAULT_IMAGE);
    ThumbnailLoader thumbnailLoader = new ThumbnailLoader();
    boolean showThumbnails = true;
    boolean showPreview = true;
    boolean showPath = true;
    boolean highQualityThumbnails = false;
    
    public MainFrame() {
        
        mGalleryComm = new GalleryComm();
        mFileList = new ArrayList();
        mAlbumList = new ArrayList();
        
        //-- load the properties file ---
        mPropertiesFile = new PropertiesFile("remote");
        String currentDirectory = (String) mPropertiesFile.getProperty("filedialogpath");
        if (currentDirectory != null) {
            AddFileDialog.currentDirectory = new File(currentDirectory);
        }
        
        String showPreviewS = (String) mPropertiesFile.getProperty("showpreview");
        if (showPreviewS != null) {
            showPreview = Boolean.valueOf(showPreviewS).booleanValue();
        }
        
        String showThumbnailsS = (String) mPropertiesFile.getProperty("showthumbnails");
        if (showThumbnailsS != null) {
            showThumbnails = Boolean.valueOf(showThumbnailsS).booleanValue();
        }
        
        String thumbnailWidthS = (String) mPropertiesFile.getProperty("thumbnailwidth");
        String thumbnailHeightS = (String) mPropertiesFile.getProperty("thumbnailheight");
        
        if (thumbnailWidthS != null && thumbnailHeightS != null) {
            thumbnailSize = new Dimension(Integer.parseInt(thumbnailWidthS), Integer.parseInt(thumbnailHeightS));
        }
        
        String thumbnailQuality = (String) mPropertiesFile.getProperty("thumbnailquality");
        if (thumbnailQuality != null && thumbnailQuality.toLowerCase().startsWith("hi")) {
            highQualityThumbnails = true;
        }
        
        String showPathS = (String) mPropertiesFile.getProperty("showpath");
        if (showPathS != null) {
            showPath = Boolean.valueOf(showPathS).booleanValue();
        }
    }
    
    
    public void initComponents() throws Exception {
        
        jMenuBar.setVisible(true);
        jMenuBar.add(jMenuFile);
        jMenuBar.add(jMenuHelp);
        jMenuBar.add(jMenuOptions);
        
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
        
        jMenuOptions.setVisible(true);
        jMenuOptions.setText("Options");
        jMenuOptions.add(jMenuOptionsPreview);
        jMenuOptions.add(jMenuOptionsThumb);
        jMenuOptions.add(jMenuOptionsPath);
        
        jMenuOptionsPreview.setVisible(true);
        jMenuOptionsPreview.setText("Preview");
        jMenuOptionsPreview.setSelected(showPreview);
        
        jMenuOptionsThumb.setVisible(true);
        jMenuOptionsThumb.setText("Thumbnails");
        jMenuOptionsThumb.setSelected(showThumbnails);
        
        jMenuOptionsPath.setVisible(true);
        jMenuOptionsPath.setText("Show Path");
        jMenuOptionsPath.setSelected(true);
        
        //-- the settings panel ---
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
        jButtonFetchAlbums.setLocation(new java.awt.Point(294, 53));
        jButtonFetchAlbums.setVisible(true);
        jButtonFetchAlbums.setText("Fetch Albums");
        jButtonFetchAlbums.setMnemonic(KeyEvent.VK_F);
        
        jComboBoxAlbums.setSize(new java.awt.Dimension(305, 20));
        jComboBoxAlbums.setLocation(new java.awt.Point(106, 92));
        jComboBoxAlbums.setVisible(true);
        
        jPanelSettings.setVisible(true);
        jPanelSettings.setLayout(null);
        jPanelSettings.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "Destination Gallery"));
        jPanelSettings.setPreferredSize(new java.awt.Dimension(500, 125));
        jPanelSettings.setMinimumSize(new java.awt.Dimension(500, 125));
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
        
        showThumbs(showThumbnails);
        
        jScrollPane.getViewport().add(jListItems);
        
        //-- the button panel ---
        jButtonAddFiles.setSize(new java.awt.Dimension(180, 25));
        jButtonAddFiles.setLocation(new java.awt.Point(0, 0));
        jButtonAddFiles.setVisible(true);
        jButtonAddFiles.setText("Browse for files to upload...");
        jButtonAddFiles.setMnemonic(KeyEvent.VK_B);
        
        jButtonDeleteFiles.setSize(new java.awt.Dimension(100, 25));
        jButtonDeleteFiles.setLocation(new java.awt.Point(190, 0));
        jButtonDeleteFiles.setVisible(true);
        jButtonDeleteFiles.setEnabled(false);
        jButtonDeleteFiles.setText("Remove files");
        jButtonDeleteFiles.setMnemonic(KeyEvent.VK_R);
        
        jButtonUpload.setSize(new java.awt.Dimension(100, 25));
        jButtonUpload.setLocation(new java.awt.Point(300, 0));
        jButtonUpload.setVisible(true);
        jButtonUpload.setText("Upload Now!");
        jButtonUpload.setMnemonic(KeyEvent.VK_U);
        
        jButtonUp.setSize(new java.awt.Dimension(70, 25));
        jButtonUp.setLocation(new java.awt.Point(410, 0));
        jButtonUp.setVisible(true);
        jButtonUp.setEnabled(false);
        jButtonUp.setText("Up");
        
        jButtonDown.setSize(new java.awt.Dimension(70, 25));
        jButtonDown.setLocation(new java.awt.Point(490, 0));
        jButtonDown.setVisible(true);
        jButtonDown.setEnabled(false);
        jButtonDown.setText("Down");
        
        jPanelButtonsTop.setVisible(true);
        jPanelButtonsTop.setLayout(null);
        jPanelButtonsTop.setPreferredSize(new java.awt.Dimension(520, 30));
        jPanelButtonsTop.add(jButtonAddFiles);
        jPanelButtonsTop.add(jButtonDeleteFiles);
        jPanelButtonsTop.add(jButtonUpload);
        jPanelButtonsTop.add(jButtonUp);
        jPanelButtonsTop.add(jButtonDown);
        
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
        jPanelButtonsBottom.setPreferredSize(new java.awt.Dimension(420, 50));
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
        
        if (mPropertiesFile.getProperty("mainx") == null || mPropertiesFile.getProperty("mainy") == null) {
            setLocation(new java.awt.Point(2, 0));
        }
        else {
            setLocation(new java.awt.Point(Integer.parseInt(mPropertiesFile.getProperty("mainx")), Integer.parseInt(mPropertiesFile.getProperty("mainy"))));
        }
        
        if (mPropertiesFile.getProperty("mainwidth") == null || mPropertiesFile.getProperty("mainheight") == null) {
            setSize(new java.awt.Dimension(576, 500));
        }
        else {
            setSize(new java.awt.Dimension(Integer.parseInt(mPropertiesFile.getProperty("mainwidth")), Integer.parseInt(mPropertiesFile.getProperty("mainheight"))));
            savedHeight = true;
        }
        
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
        jButtonDeleteFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                jButtonDeleteFilesActionPerformed(e);
            }
        });
        jButtonUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                jButtonUploadActionPerformed(e);
            }
        });
        jButtonUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                jButtonUpActionPerformed(e);
            }
        });
        jButtonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                jButtonDownActionPerformed(e);
            }
        });
        jListItems.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                jListSelectionPerformed(e);
            }
        });
        jListItems.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                jListKeyPressed(e);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });
        
        jMenuFileQuit.addActionListener(new menuBarActionListener());
        jMenuHelpAbout.addActionListener(new menuBarActionListener());
        
        jMenuOptionsPreview.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                jMenuOptionsPreviewItemSelected(e);
            }
        });
        jMenuOptionsThumb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                jMenuOptionsThumbItemSelected(e);
            }
        });
        jMenuOptionsPath.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                jMenuOptionsPathItemSelected(e);
            }
        });
        
        jPreview.initComponents(mPropertiesFile);
        jPreview.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                jMenuOptionsPreview.setSelected(false);
            }
        });
        
        updateUI();
        
        if (showPreview) {
            jPreview.setVisible(true);
        }
    }
    
    private boolean mShown = false;
    
    public void addNotify() {
        super.addNotify();
        
        if (mShown)
            return;
        
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null  && !savedHeight) {
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
        
        mPropertiesFile.setProperty("mainwidth", ""+getSize().width);
        mPropertiesFile.setProperty("mainheight", ""+getSize().height);
        mPropertiesFile.setProperty("mainx", ""+getLocation().x);
        mPropertiesFile.setProperty("mainy", ""+getLocation().y);
        
		mPropertiesFile.setProperty("showpreview",String.valueOf(showPreview));
        mPropertiesFile.setProperty("previewwidth", ""+jPreview.getSize().width);
        mPropertiesFile.setProperty("previewheight", ""+jPreview.getSize().height);
        mPropertiesFile.setProperty("previewx", ""+jPreview.getLocation().x);
        mPropertiesFile.setProperty("previewy", ""+jPreview.getLocation().y);
        
        if (AddFileDialog.currentDirectory != null) {
            mPropertiesFile.setProperty("filedialogpath", ""+AddFileDialog.currentDirectory.getPath());
        }
        
        mPropertiesFile.setProperty("showthumbnails", ""+showThumbnails);
        mPropertiesFile.setProperty("thumbnailwidth", ""+thumbnailSize.width);
        mPropertiesFile.setProperty("thumbnailheight", ""+thumbnailSize.height);
        
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
                    String fnDisplay = file.getName();
                    if (showPath) {
                        fnDisplay += " [" + file.getParent() + "]";
                    }
                    filenames.add(fnDisplay);
                }
                
                int sel = jListItems.getSelectedIndex();
                jListItems.setListData(filenames.toArray());
                if (! dontReselect) {
                    if (sel != -1) {
                        jListItems.setSelectedIndex(sel);
                    }
                }
                else {
                    dontReselect = false;
                }
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
                    String fnDisplay = file.getName();
                    if (showPath) {
                        fnDisplay += " [" + file.getParent() + "]";
                    }
                    filenames.add(fnDisplay);
                }
                
                int sel = jListItems.getSelectedIndex();
                jListItems.setListData(filenames.toArray());
                if (! dontReselect) {
                    if (sel != -1) {
                        jListItems.setSelectedIndex(sel);
                    }
                }
                else {
                    dontReselect = false;
                }
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
                public int compare(Object o1, Object o2) {
                    String f1 = ((File) o1).getAbsolutePath();
                    String f2 = ((File) o2).getAbsolutePath();
                    return (f1.compareToIgnoreCase(f2));
                }
                public boolean equals(Object o1, Object o2) {
                    String f1 = ((File) o1).getAbsolutePath();
                    String f2 = ((File) o2).getAbsolutePath();
                    return (f1.equals(f2));
                }
            });
            
            mFileList.clear();
            mFileList.addAll(Arrays.asList(items));
            thumbnailLoader.preloadThumbnails(files);
            
            updateFileList();
            updateUI();
        }
    }
    
    //-------------------------------------------------------------------------
    //-- Delete Files Button
    //-------------------------------------------------------------------------
    public void jButtonDeleteFilesActionPerformed(java.awt.event.ActionEvent e) {
        deleteSelectedFiles();
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
        jListItems.disable();
        mGalleryComm.uploadFiles(mFileList);
        
        mInProgress = true;
        updateUI();
        
        jProgressBar.setMaximum(mFileList.size());
        
        mTimer = new javax.swing.Timer(ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateProgress(mGalleryComm.getUploadedCount());
                updateStatus(mGalleryComm.getStatus());
                if (mGalleryComm.done()) {
                    mTimer.stop();
                    
                    //-- reset the UI ---
                    updateProgress(jProgressBar.getMinimum());
                    mFileList.clear();
                    mInProgress = false;
                    jListItems.enable();
                    updateUI();
                    
                }
            }
        });
        
        mTimer.start();
    }
    
    
    //-------------------------------------------------------------------------
    //-- Move File Up Action
    //-------------------------------------------------------------------------
    public void jButtonUpActionPerformed(java.awt.event.ActionEvent e) {
        moveFileUp();
    }
    
    //-------------------------------------------------------------------------
    //-- Move File Down Action
    //-------------------------------------------------------------------------
    public void jButtonDownActionPerformed(java.awt.event.ActionEvent e) {
        moveFileDown();
    }
    
    //-------------------------------------------------------------------------
    //-- Selected in the list Action
    //-------------------------------------------------------------------------
    public void jListSelectionPerformed(ListSelectionEvent e) {
        int sel = jListItems.getSelectedIndex();
        
        if (showPreview) {
            if (sel != -1) {
                String filename = ((File) mFileList.get(sel)).getPath();
                jPreview.displayFile(filename);
                thumbnailLoader.preloadThumbnailFirst(filename);
            }
            else {
                jPreview.displayFile(null);
            }
            
            if (! jPreview.isVisible()) {
                jPreview.setVisible(true);
            }
        }
        
        jButtonDeleteFiles.setEnabled(sel != -1);
        
        int selN = jListItems.getSelectedIndices().length;
        jButtonUp.setEnabled(selN == 1);
        jButtonDown.setEnabled(selN == 1);
    }
    
    public void jListKeyPressed(KeyEvent e) {
        int vKey = e.getKeyCode();
        
        switch (vKey) {
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
    public void jButtonFetchAlbumsActionPerformed(java.awt.event.ActionEvent e) {
        
        mGalleryComm.setURLString(jTextURL.getText());
        mGalleryComm.setUsername(jTextUsername.getText());
        mGalleryComm.setPassword(jPasswordField.getText());
        mGalleryComm.fetchAlbums();
        
        mInProgress = true;
        updateUI();
        
        jProgressBar.setMaximum(mFileList.size());
        
        mTimer = new javax.swing.Timer(ONE_SECOND, new ActionListener() {
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
    public class menuBarActionListener implements ActionListener {
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
            }
        }
    }
    
    //-------------------------------------------------------------------------
    //-- Open / Close the preview window
    //-------------------------------------------------------------------------
    public void jMenuOptionsPreviewItemSelected(java.awt.event.ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED){
            jPreview.show();
            showPreview = true;
        } else {
            jPreview.hide();
            showPreview = false;
        }
    }
    
    //-------------------------------------------------------------------------
    //-- Hide / Show Thumbnails
    //-------------------------------------------------------------------------
    public void jMenuOptionsThumbItemSelected(java.awt.event.ItemEvent e) {
        showThumbnails = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
        if (showThumbnails)
		{
			thumbnailLoader.preloadThumbnailFiles(new Vector(mFileList));
		}
		else
		{
			thumbnailLoader.cancelLoad();
		}
		
        showThumbs(showThumbnails);
        updateUI();
    }
    
    //-------------------------------------------------------------------------
    //-- Hide / Show Path Info
    //-------------------------------------------------------------------------
    public void jMenuOptionsPathItemSelected(java.awt.event.ItemEvent e) {
        showPath = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
        updateUI();
        mPropertiesFile.setProperty("showpath",String.valueOf(showPath));
    }
    
    public void deleteSelectedFiles() {
        int[] files = jListItems.getSelectedIndices();
        
        for (int i = files.length - 1; i >= 0; i--) {
            mFileList.remove(files[i]);
        }
        
        dontReselect = true;
        
        updateFileList();
    }
    
    public void moveFileUp() {
        int sel = jListItems.getSelectedIndex();
        
        if ( sel > 0 ) {
            Object buf = mFileList.get(sel);
            mFileList.set(sel, mFileList.get(sel-1));
            mFileList.set(sel-1, buf);
            jListItems.setSelectedIndex(sel - 1);
            updateFileList();
        }
    }
    
    public void moveFileDown() {
        int sel = jListItems.getSelectedIndex();
        
        if ( sel < mFileList.size() - 1 ) {
            Object buf = mFileList.get(sel);
            mFileList.set(sel, mFileList.get(sel+1));
            mFileList.set(sel+1, buf);
            jListItems.setSelectedIndex(sel + 1);
            updateFileList();
        }
    }
    
    //-------------------------------------------------------------------------
    //-- Show / Hide Thumbnails
    //-------------------------------------------------------------------------
    public void showThumbs(boolean show){
        jListItems.setCellRenderer(new FileCellRenderer());
        if (show){
            jListItems.setFixedCellHeight(thumbnailSize.height + 4);
        } else {
            //jListItems.setCellRenderer(new DefaultListCellRenderer());
            jListItems.setFixedCellHeight(20);
        }
        
    }
    
    //-------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------
    public class FileCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
        JList list, Object value, int index,
        boolean selected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            
            ImageIcon icon = getThumbnail(((File) mFileList.get(index)).getPath());
            if (showThumbnails){
                setIcon(icon);
                setIconTextGap(4 + thumbnailSize.width - icon.getIconWidth());
            }
            return this;
        }
    }
    
    public ImageIcon getThumbnail(String filename) {
        if (filename == null) {
            return null;
        }
        
        ImageIcon r = (ImageIcon) thumbnails.get(filename);
        
        if (r == null) {
            r = defaultThumbnail;
        }
        
        return r;
    }
    
    public ImageIcon loadThumbnail(String filename) {
        ImageIcon r = new ImageIcon(filename);
        Dimension d = PreviewFrame.getSizeKeepRatio(new Dimension(r.getIconWidth(), r.getIconHeight()), thumbnailSize);
        
        long start = System.currentTimeMillis();
        Image scaled = r.getImage().getScaledInstance(d.width, d.height, highQualityThumbnails?Image.SCALE_SMOOTH:Image.SCALE_FAST);
        r.getImage().flush();
        r.setImage(scaled);
        
        thumbnails.put(filename, r);
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        
        return r;
    }
    
    class ThumbnailLoader implements Runnable {
        boolean stillRunning = false;
        Stack toLoad = new Stack();
        
        public void run() {
            //System.out.println("Starting " + iFilename);
            while (! toLoad.isEmpty()) {
                String filename = (String) toLoad.pop();
                
                if (thumbnails.get(filename) == null) {
                    loadThumbnail(filename);
                    
                    jListItems.repaint();
                }
            }
            stillRunning = false;
            
            //System.out.println("Ending");
        }
        
        public void preloadThumbnail(String filename) {
            //System.out.println("loadPreview " + filename);
            
            toLoad.add(0, filename);
            
            rerun();
        }
        
        public void preloadThumbnailFirst(String filename) {
            //System.out.println("loadPreview " + filename);
            
            toLoad.push(filename);
            
            rerun();
        }
        
        public void preloadThumbnails(Vector filenames) {
            //System.out.println("loadPreview " + filenames);
            
            toLoad.addAll(0, filenames);
            
            rerun();
        }
        
         public void preloadThumbnailFiles(Vector files) {
            //System.out.println("loadPreview " + files);
            
            Enumeration e = files.elements();
			while (e.hasMoreElements())
			{
                toLoad.add(0, ((File) e.nextElement()).getPath());
            }
            
            rerun();
        }
        
        public void preloadThumbnails(File[] filenames) {
            //System.out.println("loadPreview " + filename);
            
            for (int i = 0; i < filenames.length; i++) {
                toLoad.add(0, ((File) filenames[i]).getPath());
            }
            
            rerun();
        }
        
        void rerun() {
            if (! stillRunning && showThumbnails) {
                stillRunning = true;
                //System.out.println("Calling Start");
                new Thread(this).start();
            }
        }
		
		void cancelLoad()
		{
			toLoad.clear();
		}
    }
    
    //-------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------
    public class DroppableList extends JList
    implements DropTargetListener {
        DropTarget dropTarget = new DropTarget(this, this);
        MainFrame mDaddy;
        
        public DroppableList(MainFrame daddy) {
            setModel(new DefaultListModel());
            mDaddy = daddy;
        }
        
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
            dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
        
        public void dragExit(DropTargetEvent dropTargetEvent) {}
        public void dragOver(DropTargetDragEvent dropTargetDragEvent) {}
        public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent){}
        
        public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
            try {
                Transferable tr = dropTargetDropEvent.getTransferable();
                if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && this.isEnabled()) {
                    dropTargetDropEvent.acceptDrop(
                    DnDConstants.ACTION_COPY_OR_MOVE);
                    java.util.List fileList = (java.util.List)
                    tr.getTransferData(DataFlavor.javaFileListFlavor);
                    Iterator iterator = fileList.iterator();
                    while (iterator.hasNext()) {
                        File file = (File)iterator.next();
                        mFileList.add(file);
                        thumbnailLoader.preloadThumbnail(file.getPath());
                    }
                    dropTargetDropEvent.getDropTargetContext().dropComplete(true);
                    mDaddy.updateUI();
                } else {
                    System.err.println("Rejected");
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
