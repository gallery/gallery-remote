package com.gallery.GalleryRemote.util;

import com.gallery.GalleryRemote.Log;
import edu.stanford.ejalbert.BrowserLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Dec 16, 2003
 */
public class BrowserLink extends JLabel implements MouseListener {
	public static final String MODULE = "BrowserLink";
	String url = null;

	public BrowserLink() {
		super();

		setForeground(Color.blue);

		addMouseListener(this);
	}

	public BrowserLink(String url) {
		super(url);

		setForeground(Color.blue);

		addMouseListener(this);
	}

	public void setText(String text) {
		url = text;

		super.setText("<html><u>" + text + "</u></html>");
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	/* MouseListener Interface */
	public void mouseClicked(MouseEvent e) {
		try {
			BrowserLauncher.openURL(url);
		} catch (IOException e1) {
			Log.logException(Log.LEVEL_ERROR, MODULE, e1);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
