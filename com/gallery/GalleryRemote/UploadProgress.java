package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.OsShutdown;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class UploadProgress extends JDialog implements StatusUpdate, ActionListener {
	public static final String MODULE= "UploadProgress";

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JPanel jPanel1 = new JPanel();
	JLabel jComputer1 = new JLabel();
	JLabel jUploading = new JLabel();
	JLabel jComputer2 = new JLabel();
	JLabel jLabelGlobal = new JLabel();
	JProgressBar jProgressGlobal = new JProgressBar();
	JLabel jLabelDetail = new JLabel();
	JProgressBar jProgressDetail = new JProgressBar();
	JPanel jPanel2 = new JPanel();

	JLabel jLabel[] = new JLabel[NUM_LEVELS];
	JProgressBar jProgress[] = new JProgressBar[NUM_LEVELS];

	MainFrame mf;
	ActionListener cancelListener = null;
	JButton jCancel = new JButton();
	JCheckBox jShutdown = new JCheckBox();

	public UploadProgress(MainFrame mf) {
		super(mf);
		this.mf = mf;

		jbInit();

		jLabel[LEVEL_UPLOAD_ONE] = jLabelDetail;
		jLabel[LEVEL_UPLOAD_PROGRESS] = jLabelGlobal;
		jProgress[LEVEL_UPLOAD_ONE] = jProgressDetail;
		jProgress[LEVEL_UPLOAD_PROGRESS] = jProgressGlobal;

		pack();
		DialogUtil.center(this, mf);
		setVisible(true);
	}

	private void jbInit() {
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.setLayout(gridBagLayout1);

		jComputer1.setIcon(MainFrame.iComputer);
		jComputer2.setIcon(MainFrame.iComputer);
		jUploading.setIcon(MainFrame.iUploading);

		jLabelGlobal.setText("Uploading image n of m");
		jLabelDetail.setText("Uploading img.gif");

		jCancel.setText("Cancel");
		jCancel.addActionListener(this);
		jShutdown.setToolTipText("Shut down the computer when the transfer completes");
		jShutdown.setText("Shutdown when done");

		jPanel1.add(jComputer1,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jUploading,   new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
		jPanel1.add(jComputer2,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 10), 0, 0));
		jPanel1.add(jLabelGlobal,     new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jProgressGlobal,     new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 15, 0, 15), 0, 0));
		jPanel1.add(jLabelDetail,    new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		jPanel1.add(jProgressDetail,   new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 15, 0, 15), 0, 0));
		jPanel1.add(jPanel2,      new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		if (OsShutdown.canShutdown()) {
			jPanel2.add(jShutdown, null);
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
			} catch (Throwable t) {}

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
		} catch (Throwable t) {}
	}

	public void stopProgress(int level, String message) {
		if (checkLevel(level)) {
			jProgress[level].setMaximum(jProgress[level].getMinimum());
			jLabel[level].setText(message);

			try {
				jProgress[level].setIndeterminate(false);
			} catch (Throwable t) {}

			if (level == LEVEL_UPLOAD_PROGRESS) {
				setVisible(false);
				//dispose();
			}
		}
	}

	/* level-independant methods */
	public void setInProgress(boolean inProgress) {
		mf.setInProgress(inProgress);
	}

	public void error(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
		if (level == LEVEL_UPLOAD_ONE || level == LEVEL_UPLOAD_PROGRESS) {
			return true;
		} else {
			Log.log(Log.ERROR, MODULE, "Bad level");
			Log.logStack(Log.ERROR, MODULE);

			return false;
		}
	}

	public void actionPerformed(ActionEvent e) {
		final ActionEvent fe = e;
		new Thread() {
			public void run() {
				cancelListener.actionPerformed(fe);
			}
		}.start();
	}

	public void setCancelListener(ActionListener cancelListener) {
		this.cancelListener = cancelListener;
	}

	public boolean isShutdown() {
		return jShutdown.isSelected();
	}
}