/*
 * Donated code for Gallery Project
 * Copyright abandonded by Stanley Knutson (Stanley@stanleyKnutson.com)
 */
package com.gallery.GalleryRemote;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import HTTPClient.NVPair;

/**
 *  Do the authorization popup in a swing-compatible way. Built from the code in
 *  HTTPClient.DefaultAuthHandler
 *
 *@author     Stanley Knutson
 *@created    Januray 3, 2003
 */

public class AuthorizePopup implements HTTPClient.AuthorizationPrompter
{
	private static BasicAuthBox inp = null;


	/**
	 *  Engage this class to be the authorization popup
	 */
	public static void enable() {
		HTTPClient.DefaultAuthHandler.setAuthorizationPrompter
				( new AuthorizePopup() );
	}


	/**
	 *  the method called by DefaultAuthHandler.
	 *
	 *@param  challenge  Description of Parameter
	 *@param  forProxy   Description of Parameter
	 *@return            the username/password pair
	 */
	public NVPair getUsernamePassword( HTTPClient.AuthorizationInfo challenge,
			boolean forProxy ) {
		String line1;
		String line2;
		String line3;

		if ( challenge.getScheme().equalsIgnoreCase( "SOCKS5" ) ) {
			line1 = "Enter username and password for SOCKS server on host";
			line2 = challenge.getHost();
			line3 = "Authentication Method: username/password";
		} else {
			line1 = "Enter username and password for realm `" +
					challenge.getRealm() + "'";
			line2 = "on host " + challenge.getHost() + ":" +
					challenge.getPort();
			line3 = "Authentication Scheme: " + challenge.getScheme();
		}

		synchronized ( getClass() ) {
			if ( inp == null ) {

				MainFrame fr = GalleryRemote.getInstance().mainFrame;
				inp = new BasicAuthBox( fr );
			}
		}

		return inp.getInput( line1, line2, line3, challenge.getScheme() );
	}


	/**
	 *  This class implements a simple popup that request username and password
	 *  used for the "basic" and "digest" authentication schemes.
	 *
	 *@author     Ronald Tschalar
	 *@created    February 2, 2003
	 *@version    0.3-3 06/05/2001
	 */
	private static class BasicAuthBox extends javax.swing.JDialog
	{
		private final static String title = "Authorization Request";
		private Dimension screen;
		private JLabel line1, line2, line3;
		private JTextField user, pass;
		private int done;
		private final static int OK = 1, CANCEL = 0;
		private MainFrame theFrame;


		/**
		 *  Constructs the popup with two lines of text above the input fields
		 *
		 *@param  container  Description of Parameter
		 */
		BasicAuthBox( MainFrame container ) {

			super( container, title, true );
			theFrame = container;

			screen = getToolkit().getScreenSize();

			addNotify();
			addWindowListener( new Close() );
			getContentPane().setLayout( new BorderLayout() );

			JPanel p = new JPanel( new GridLayout( 3, 1 ) );
			p.add( line1 = new JLabel() );
			p.add( line2 = new JLabel() );
			p.add( line3 = new JLabel() );
			getContentPane().add( "North", p );

			p = new JPanel( new GridLayout( 2, 1 ) );
			p.add( new JLabel( "Username:" ) );
			p.add( new JLabel( "Password:" ) );
			getContentPane().add( "West", p );
			p = new JPanel( new GridLayout( 2, 1 ) );
			p.add( user = new JTextField( 30 ) );
			p.add( pass = new JPasswordField( 30 ) );
			pass.addActionListener( new Ok() );
			getContentPane().add( "East", p );

			GridBagLayout gb = new GridBagLayout();
			p = new JPanel( gb );
			GridBagConstraints constr = new GridBagConstraints();
			JPanel pp = new JPanel();
			p.add( pp );
			constr.gridwidth = GridBagConstraints.REMAINDER;
			gb.setConstraints( pp, constr );
			constr.gridwidth = 1;
			constr.weightx = 1.0;
			JButton b;
			p.add( b = new JButton( "  OK  " ) );
			b.addActionListener( new Ok() );
			constr.weightx = 1.0;
			gb.setConstraints( b, constr );
			p.add( b = new JButton( "Clear" ) );
			b.addActionListener( new Clear() );
			constr.weightx = 2.0;
			gb.setConstraints( b, constr );
			p.add( b = new JButton( "Cancel" ) );
			b.addActionListener( new Cancel() );
			constr.weightx = 1.0;
			gb.setConstraints( b, constr );
			getContentPane().add( "South", p );

			pack();
		}


		/**
		 *  the method called by SimpleAuthPopup.
		 *
		 *@param  l1      Description of Parameter
		 *@param  l2      Description of Parameter
		 *@param  l3      Description of Parameter
		 *@param  scheme  Description of Parameter
		 *@return         the username/password pair
		 */
		NVPair getInput( String l1, String l2, String l3,
				String scheme ) {
			line1.setText( l1 );
			line2.setText( l2 );
			line3.setText( l3 );

			line1.invalidate();
			line2.invalidate();
			line3.invalidate();

			setResizable( true );
			pack();
			setResizable( false );
			// put popup at upper right of the parent frame (assuming we have one)
			if ( theFrame != null ) {
				int popX = theFrame.getX() + 20;
				Dimension sz = getPreferredSize();
				if ( popX + sz.width > screen.width ) {
					popX = screen.width - sz.width;
				}
				if ( popX < 0 ) {
					popX = 0;
				}
				int popY = theFrame.getY() + 65;
				if ( popY + sz.height + 30 > screen.height ) {
					popY = screen.height - sz.height - 30;
				}
				if ( popY < 0 ) {
					popY = 0;
				}
				setLocation( popX, popY );
			} else {
				setLocation( ( screen.width - getPreferredSize().width ) / 2,
						(int) ( ( screen.height - getPreferredSize().height ) / 2 * .7 ) );
			}
			boolean user_focus = true;
			if ( scheme.equalsIgnoreCase( "NTLM" ) ) {
				// prefill the user field with the username
				try {
					user.setText( System.getProperty( "user.name", "" ) );
					user_focus = false;
				} catch ( SecurityException se ) {
				}
			}

			// This call to request focus probably does not do anything, but leave it in.
			if ( user_focus ) {
				user.requestFocus();
			} else {
				pass.requestFocus();
			}

			// This is a modal dialog: the "show" method does not return
			// until "hide" is called by close, cancel or ok action
			show();

			NVPair result = new NVPair( user.getText(), pass.getText() );
			user.setText( "" );
			pass.setText( "" );

			if ( done == CANCEL ) {
				return null;
			} else {
				return result;
			}
		}


		/**
		 *  our event handlers
		 *
		 *@author     paour
		 *@created    February 2, 2003
		 */
		private class Ok implements ActionListener
		{
			/**
			 *  Description of the Method
			 *
			 *@param  ae  Description of Parameter
			 */
			public void actionPerformed( ActionEvent ae ) {
				done = OK;
				hide();
				// synchronized (BasicAuthBox.this)
				//    { BasicAuthBox.this.notifyAll(); }
			}
		}


		/**
		 *  Description of the Class
		 *
		 *@author     paour
		 *@created    February 2, 2003
		 */
		private class Clear implements ActionListener
		{
			/**
			 *  Description of the Method
			 *
			 *@param  ae  Description of Parameter
			 */
			public void actionPerformed( ActionEvent ae ) {
				user.setText( "" );
				pass.setText( "" );
				user.requestFocus();
			}
		}


		/**
		 *  Description of the Class
		 *
		 *@author     paour
		 *@created    February 2, 2003
		 */
		private class Cancel implements ActionListener
		{
			/**
			 *  Description of the Method
			 *
			 *@param  ae  Description of Parameter
			 */
			public void actionPerformed( ActionEvent ae ) {
				done = CANCEL;
				hide();
				//synchronized (BasicAuthBox.this)
				//    { BasicAuthBox.this.notifyAll(); }
			}
		}


		/**
		 *  Description of the Class
		 *
		 *@author     paour
		 *@created    February 2, 2003
		 */
		private class Close extends WindowAdapter
		{
			/**
			 *  Description of the Method
			 *
			 *@param  we  Description of Parameter
			 */
			public void windowClosing( WindowEvent we ) {
				new Cancel().actionPerformed( null );
			}
		}
	}
}
