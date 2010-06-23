package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.model.Picture;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;


public class UploadProgress implements StatusUpdate, ActionListener {
	public static final String MODULE = "UploadProgress";

	JDialog dialog = null;

	JPanel jContentPanel = new JPanel();
	JLabel jComputer1 = new JLabel();
	JLabel jUploading = new JLabel();
	JLabel jComputer2 = new JLabel();
	JLabel jLabelGlobal = new JLabel();
	JProgressBar jProgressGlobal = new JProgressBar();
	JLabel jLabelDetail = new JLabel();
	JProgressBar jProgressDetail = new JProgressBar();
	JPanel jPanel2 = new JPanel();
	JTextArea jErrors = null;

	JLabel jLabel[] = new JLabel[NUM_LEVELS];
	JProgressBar jProgress[] = new JProgressBar[NUM_LEVELS];

	ActionListener cancelListener = null;
	JButton jCancel = new JButton();
	JCheckBox jShutdown = new JCheckBox();

	public UploadProgress() {
		this(null);
	}
	
	public UploadProgress(Frame f) {
		if (f != null) {
			dialog = new JDialog(f);
		}

		jbInit();

		jLabel[LEVEL_UPLOAD_ONE] = jLabelDetail;
		jLabel[LEVEL_UPLOAD_ALL] = jLabelGlobal;
		jProgress[LEVEL_UPLOAD_ONE] = jProgressDetail;
		jProgress[LEVEL_UPLOAD_ALL] = jProgressGlobal;

		if (dialog != null) {
			// wierd bug prevents upload... this happens on some versions of the VM
			// apparently 1.4.2_03-b02 Windows.
			try {
				dialog.pack();
			} catch (NullPointerException e) {
				Log.log(Log.LEVEL_ERROR, MODULE, "Wierd VM bug");
				Log.logException(Log.LEVEL_ERROR, MODULE, e);
				dialog.setSize(400, 320);
			}

			DialogUtil.center(dialog, f);
			dialog.setVisible(true);
		}
	}

	public JPanel getContentPanel() {
		return jContentPanel;
	}

	private void jbInit() {
		if (dialog != null) {
			dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			dialog.getContentPane().add(jContentPanel, BorderLayout.CENTER);
		}
		
		jContentPanel.setLayout(new GridBagLayout());

		jComputer1.setIcon(GalleryRemote.iComputer);
		jComputer2.setIcon(GalleryRemote.iComputer);
		jUploading.setIcon(GalleryRemote.iUploading);

		jLabelGlobal.setText(GRI18n.getString(MODULE, "globalProgress"));
		jLabelDetail.setText(GRI18n.getString(MODULE, "detailProgress"));

		jCancel.setText(GRI18n.getString("Common", "Cancel"));
		jCancel.addActionListener(this);
		jCancel.setActionCommand("Cancel");
		jShutdown.setToolTipText(GRI18n.getString(MODULE, "shutDownTip"));
		jShutdown.setText(GRI18n.getString(MODULE, "shutDown"));

		jContentPanel.add(jComputer1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
		jContentPanel.add(jUploading, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
		jContentPanel.add(jComputer2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 10), 0, 0));
		jContentPanel.add(jLabelGlobal, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
		jContentPanel.add(jProgressGlobal, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 15, 0, 15), 0, 0));
		jContentPanel.add(jLabelDetail, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		jContentPanel.add(jProgressDetail, new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 15, 0, 15), 0, 0));
		jContentPanel.add(jPanel2, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		try {
			Class osShutdown = GalleryRemote.secureClassForName("com.gallery.GalleryRemote.util.OsShutdown");
			Method canShutdown = osShutdown.getMethod("canShutdown");
			if ((Boolean) canShutdown.invoke(null)) {
				jPanel2.add(jShutdown, null);
			}
		} catch (Exception e) {
			Log.log(Log.LEVEL_TRACE, MODULE, "OsShutdown not supported, hiding checkbox");
		}

		jPanel2.add(jCancel, null);
	}

	/* level-bound methods */
	public void startProgress(int level, int minValue, int maxValue, String message, boolean undetermined) {
		if (checkLevel(level)) {
			jProgress[level].setMinimum(minValue);
			jProgress[level].setMaximum(maxValue);
			try {
				jProgress[level].setIndeterminate(undetermined);
			} catch (Throwable t) {
			}

			jLabel[level].setText(message);
		}
	}

	public void updateProgressValue(int level, int value) {
		if (checkLevel(level)) {
			jProgress[level].setValue(value);
		}
	}

	public void updateProgressValue(int level, int value, int maxValue) {
		if (checkLevel(level)) {
			jProgress[level].setValue(value);
			jProgress[level].setMaximum(maxValue);
		}
	}

	public void updateProgressStatus(int level, String message) {
		if (checkLevel(level)) {
			jLabel[level].setText(message);
		}
	}

	public void setUndetermined(int level, boolean undetermined) {
		try {
			jProgress[level].setIndeterminate(undetermined);
		} catch (Throwable t) {
		}
	}

	public void stopProgress(int level, String message) {
		if (checkLevel(level)) {
			jProgress[level].setMaximum(jProgress[level].getMinimum());
			jLabel[level].setText(message);

			try {
				jProgress[level].setIndeterminate(false);
			} catch (Throwable t) {
			}

			if (level == LEVEL_UPLOAD_ALL) {
				// we're done...
				if (jErrors != null) {
					// there were errors, don't dismiss the dialog just yet
					jCancel.setText(GRI18n.getString("Common", "OK"));
					jCancel.setActionCommand("OK");
				} else {
					done();
				}
			}
		}
	}

	public void done() {
		if (dialog != null) {
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	/* level-independant methods */
	public void setInProgress(boolean inProgress) {
		GalleryRemote._().getCore().setInProgress(inProgress);
	}

	public void error(String message) {
		//JOptionPane.showMessageDialog(this, message, GRI18n.getString(MODULE, "Error"), JOptionPane.ERROR_MESSAGE);
		if (jErrors == null) {
			jErrors = new JTextArea(5, 80);
			jErrors.setEditable(false);
			jErrors.setFont(UIManager.getFont("Label.font"));
			JScrollPane scroll = new JScrollPane(jErrors, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setBorder(new TitledBorder(GRI18n.getString(MODULE, "Errors")));
			jContentPanel.add(
					scroll
					, new GridBagConstraints(0, 6, 3, 1, 1.0, 1.0
					, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			if (dialog != null) {
				dialog.pack();
			}
		}

		jErrors.append(removeLinefeed(message) + "\n");
	}

	public static String removeLinefeed(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != '\n') {
				sb.append(c);
			} else {
				sb.append(' ');
			}
		}

		return sb.toString();
	}

	public void setStatus(String message) {
		updateProgressStatus(LEVEL_GENERIC, message);
	}

	public int getProgressValue(int level) {
		if (checkLevel(level)) {
			return jProgress[level].getValue();
		}

		return 0;
	}

	public int getProgressMinValue(int level) {
		if (checkLevel(level)) {
			return jProgress[level].getMinimum();
		}

		return 0;
	}

	public int getProgressMaxValue(int level) {
		if (checkLevel(level)) {
			return jProgress[level].getMaximum();
		}

		return 0;
	}

	boolean checkLevel(int level) {
		return level == LEVEL_UPLOAD_ONE || level == LEVEL_UPLOAD_ALL;
	}

	public void actionPerformed(ActionEvent e) {
		final ActionEvent fe = e;
		new Thread() {
			public void run() {
				if (cancelListener != null) {
					cancelListener.actionPerformed(fe);
				}
			}
		}.start();
	}

	public void setCancelListener(ActionListener cancelListener) {
		this.cancelListener = cancelListener;
	}

	public boolean isShutdown() {
		return jShutdown.isSelected();
	}

	public void doneUploading(String newItemName, Picture picture) {
	}
}