/*
 * Donated code for Gallery Project
 * Copyright abandonded by Stanley Knutson (Stanley@stanleyKnutson.com)
 */
package com.gallery.GalleryRemote;

import HTTPClient.NVPair;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.DialogUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Do the authorization popup in a swing-compatible way. Built from the code in
 * HTTPClient.DefaultAuthHandler
 * 
 * @author Stanley Knutson
 * @created Januray 3, 2003
 */

public class AuthorizePopup implements HTTPClient.AuthorizationPrompter {
	private static BasicAuthBox inp = null;
	public static final String MODULE = "AuthorizePopup";

	public static String hackUsername = null;
	public static String hackPassword = null;


	/**
	 * Engage this class to be the authorization popup
	 */
	public static void enable() {
		HTTPClient.DefaultAuthHandler.setAuthorizationPrompter
				(new AuthorizePopup());
	}


	/**
	 * the method called by DefaultAuthHandler.
	 * 
	 * @param challenge Description of Parameter
	 * @param forProxy  Description of Parameter
	 * @return the username/password pair
	 */
	public NVPair getUsernamePassword(HTTPClient.AuthorizationInfo challenge,
									  boolean forProxy) {

		// The HTTPClient library doesn't correctly accept setting auth params
		// ahead of time (because it can't accept a realm of *
		// so we have to do it this hackish way...
		if (challenge.getScheme().equalsIgnoreCase("Basic") && hackUsername != null) {
			return new NVPair(hackUsername, hackPassword);
		}

		String line1;
		String line2;
		String line3;

		if (challenge.getScheme().equalsIgnoreCase("SOCKS5")) {
			line1 = GRI18n.getString(MODULE, "enterUsrPwd");
			line2 = challenge.getHost();
			line3 = GRI18n.getString(MODULE, "authMthd");
		} else {
			line1 = GRI18n.getString(MODULE, "enterUsrPwdRealm", new Object[] { challenge.getRealm() });
			line2 = GRI18n.getString(MODULE, "onHost", new Object[] { challenge.getHost() + ":" +
					challenge.getPort()});
			line3 = GRI18n.getString(MODULE, "authScheme", new Object[] { challenge.getScheme() });
		}

		synchronized (getClass()) {
			if (inp == null) {
				inp = new BasicAuthBox(GalleryRemote._().getMainFrame());
			}
		}

		return inp.getInput(line1, line2, line3, challenge.getScheme());
	}


	/**
	 * This class implements a simple popup that request username and password
	 * used for the "basic" and "digest" authentication schemes.
	 * 
	 * @author Ronald Tschalar
	 * @version 0.3-3 06/05/2001
	 * @created February 2, 2003
	 */
	private static class BasicAuthBox extends JDialog implements ActionListener {
		private final static String title = GRI18n.getString(MODULE, "authreq");
		private JLabel line1, line2, line3;
		private JTextField user, pass;
		private int done;
		private final static int OK = 1, CANCEL = 0;


		/**
		 * Constructs the popup with two lines of text above the input fields
		 * 
		 * @param container Description of Parameter
		 */
		BasicAuthBox(Frame container) {
			super(container, title, true);

			addNotify();
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

			getContentPane().setLayout(new BorderLayout());

			JPanel p = new JPanel(new GridLayout(3, 1));
			p.add(line1 = new JLabel());
			p.add(line2 = new JLabel());
			p.add(line3 = new JLabel());
			getContentPane().add("North", p);

			p = new JPanel(new GridLayout(2, 1));
			p.add(new JLabel(GRI18n.getString(MODULE, "username")));
			p.add(new JLabel(GRI18n.getString(MODULE, "passwd")));
			getContentPane().add("West", p);
			p = new JPanel(new GridLayout(2, 1));
			p.add(user = new JTextField(30));
			p.add(pass = new JPasswordField(30));
			pass.addActionListener(this);
			getContentPane().add("East", p);

			GridBagLayout gb = new GridBagLayout();
			p = new JPanel(gb);
			GridBagConstraints constr = new GridBagConstraints();
			JPanel pp = new JPanel();
			p.add(pp);
			constr.gridwidth = GridBagConstraints.REMAINDER;
			gb.setConstraints(pp, constr);
			constr.gridwidth = 1;
			constr.weightx = 1.0;

			JButton b;
			p.add(b = new JButton(GRI18n.getString("Common", "OK")));
			b.addActionListener(this);
			b.setActionCommand("ok");
			getRootPane().setDefaultButton(b);
			constr.weightx = 1.0;
			gb.setConstraints(b, constr);

			p.add(b = new JButton(GRI18n.getString(MODULE, "clear")));
			b.addActionListener(this);
			b.setActionCommand("clear");
			constr.weightx = 2.0;
			gb.setConstraints(b, constr);

			p.add(b = new JButton(GRI18n.getString("Common", "Cancel")));
			b.addActionListener(this);
			b.setActionCommand("cancel");
			constr.weightx = 1.0;
			gb.setConstraints(b, constr);

			getContentPane().add("South", p);

			pack();
		}


		/**
		 * the method called by SimpleAuthPopup.
		 * 
		 * @param l1     Description of Parameter
		 * @param l2     Description of Parameter
		 * @param l3     Description of Parameter
		 * @param scheme Description of Parameter
		 * @return the username/password pair
		 */
		NVPair getInput(String l1, String l2, String l3,
						String scheme) {
			line1.setText(l1);
			line2.setText(l2);
			line3.setText(l3);

			line1.invalidate();
			line2.invalidate();
			line3.invalidate();

			setResizable(true);
			pack();
			setResizable(false);
			// put popup at upper right of the parent frame (assuming we have one)

			DialogUtil.center(this, getOwner());

			boolean user_focus = true;
			if (scheme.equalsIgnoreCase("NTLM")) {
				// prefill the user field with the username
				try {
					user.setText(System.getProperty("user.name", ""));
					user_focus = false;
				} catch (SecurityException se) {
				}
			}

			// This call to request focus probably does not do anything, but leave it in.
			if (user_focus) {
				user.requestFocus();
			} else {
				pass.requestFocus();
			}

			// This is a modal dialog: the "show" method does not return
			// until "hide" is called by close, cancel or ok action
			show();

			NVPair result = new NVPair(user.getText(), pass.getText());
			user.setText("");
			pass.setText("");

			if (done == CANCEL) {
				return null;
			} else {
				return result;
			}
		}

		/**
		 * Invoked when an action occurs.
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == pass || "ok".equals(e.getActionCommand())) {
				done = OK;
				hide();
			} else if ("clear".equals(e.getActionCommand())) {
				user.setText("");
				pass.setText("");
				user.requestFocus();
			} else if ("cancel".equals(e.getActionCommand())) {
				done = CANCEL;
				hide();
			} else if ("close".equals(e.getActionCommand())) {
			}
		}

		/*private class Close extends WindowAdapter {
			public void windowClosing(WindowEvent we) {
				new Cancel().actionPerformed(null);
			}
		}*/
	}
}
