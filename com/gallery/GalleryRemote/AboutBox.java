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
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

/**
 *  Gallery Remote About Box
 *
 *@author     paour
 *@created    August 18, 2002
 */
public class AboutBox extends JDialog {
    public static int TOP = 60;
    public static int BOTTOM = 30;
    
    /**
     *  Constructor for the AboutBox object
     */
    public AboutBox() {
        super();
        init();
    }
    
    
    /**
     *  Constructor for the AboutBox object
     *
     *@param  owner  Description of Parameter
     */
    public AboutBox( Frame owner ) {
        super( owner );
        init();
    }
    
    
    private void init() {
        setModal( true );
        getContentPane().add( new AboutPanel(), BorderLayout.CENTER );
        setTitle( "About Gallery Remote" );
        
        pack();
        
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        
        setLocation( (int) ( s.getWidth() - getWidth() ) / 2, (int) ( s.getHeight() - getHeight() ) / 2 );
        
        addMouseListener(
        new java.awt.event.MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                thisWindowClosing();
            }
        } );
        addWindowListener(
        new java.awt.event.WindowAdapter() {
            public void windowClosing( MouseEvent e ) {
                thisWindowClosing();
            }
        } );
    }
    
    // Close the window when the box is clicked
    void thisWindowClosing() {
        setVisible( false );
        dispose();
    }
    
    
    /**
     *  AboutPanel: scrolling panel of credits for About boxes
     *
     *@author     paour
     *@created    August 16, 2002
     */
    public class AboutPanel extends JComponent {
        ImageIcon image;
        Vector text;
        int scrollPosition;
        AnimationThread thread;
        int maxWidth;
        FontMetrics fm;
        int initialPosition;
        
        /**
         *  Constructor for the AboutPanel object
         */
        public AboutPanel() {
            setFont( UIManager.getFont( "Label.font" ) );
            fm = getFontMetrics( getFont() );
            
            //setForeground(new Color(96, 96, 96));
            image = new ImageIcon( getClass().getResource(
            "/remote1.gif" ) );
            
            setBorder( new MatteBorder( 1, 1, 1, 1, Color.gray ) );
            
            text = new Vector( 50 );
            StringTokenizer st = new StringTokenizer(
            GalleryRemote.getInstance().properties.getProperty( "aboutText" ), "\n" );
            while ( st.hasMoreTokens() ) {
                String line = st.nextToken();
                text.addElement( line );
                maxWidth = Math.max( maxWidth,
                fm.stringWidth( line ) + 10 );
            }
            initialPosition = getHeight() - BOTTOM - BOTTOM - TOP - TOP;
            scrollPosition = initialPosition;
            
            thread = new AnimationThread();
        }
        
        /**
         *  Gets the preferredSize attribute of the AboutPanel object
         *
         *@return    The preferredSize value
         *@since
         */
        public Dimension getPreferredSize() {
            return new Dimension( 1 + image.getIconWidth(),
            1 + image.getIconHeight() );
        }
        
        /**
         *  Description of the Method
         *
         *@param  g  Description of Parameter
         *@since
         */
        public void paintComponent( Graphics g ) {
            //g.setColor(new Color(96, 96, 96));
            image.paintIcon( this, g, 1, 1 );
            
            FontMetrics fm = g.getFontMetrics();
            
            String version = GalleryRemote.getInstance().properties.getProperty( "version" );
            g.drawString( version, ( getWidth() - fm.stringWidth( version ) ) / 2,
            getHeight() - 5 );
            
            g = g.create( ( getWidth() - maxWidth ) / 2, TOP, maxWidth,
            getHeight() - TOP - BOTTOM );
            
            int height = fm.getHeight();
            int firstLine = scrollPosition / height;
            
            int firstLineOffset = height - scrollPosition % height;
            int lines = ( getHeight() - TOP - BOTTOM ) / height;
            
            int y = firstLineOffset;
            
            for ( int i = 0; i <= lines; i++ ) {
                if ( i + firstLine >= 0 && i + firstLine < text.size() ) {
                    String line = (String) text.get( i + firstLine );
                    g.drawString( line, ( maxWidth - fm.stringWidth( line ) ) / 2, y );
                }
                y += fm.getHeight();
            }
        }
        
        /**
         *  Adds a feature to the Notify attribute of the AboutPanel object
         *
         *@since
         */
        public void addNotify() {
            super.addNotify();
            thread.start();
        }
        
        /**
         *  Description of the Method
         *
         *@since
         */
        public void removeNotify() {
            super.removeNotify();
            thread.kill();
        }
        
        /**
         *  Animation thread
         *
         *@author     paour
         *@created    August 16, 2002
         */
        class AnimationThread extends Thread {
            private boolean running = true;
            
            
            AnimationThread() {
                super( "About box animation thread" );
                setPriority( Thread.MIN_PRIORITY );
            }
            
            
            /**
             *  Description of the Method
             *
             *@since
             */
            public void kill() {
                running = false;
            }
            
            
            /**
             *  Main processing method for the AnimationThread object
             *
             *@since
             */
            public void run() {
                FontMetrics fm = getFontMetrics( getFont() );
                int max = ( text.size() * fm.getHeight() );
                
                while ( running ) {
                    long start = System.currentTimeMillis();
                    
                    scrollPosition += 2;
                    
                    if ( scrollPosition > max ) {
                        scrollPosition = initialPosition;
                    }
                    
                    try {
                        Thread.sleep( 100 );
                    } catch ( Exception e ) {
                    }
                    
                    repaint( getWidth() / 2 - maxWidth,
                    TOP, maxWidth * 2,
                    getHeight() - TOP - BOTTOM );
                }
            }
        }
    }
}

