package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.util.DialogUtil;
import com.gallery.GalleryRemote.util.GRI18n;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Sep 17, 2003
 */
public class StatusBar extends JPanel implements StatusUpdate {
	public static final String MODULE = "StatusBar";
	JProgressBar jProgress = new JProgressBar();
	JLabel jStatus = new JLabel();

	MainFrame mf = null;

	StatusLevelData data[] = new StatusLevelData[NUM_LEVELS];
	int currentLevel = -1;

	public StatusBar(MainFrame mf) {
		this.mf = mf;

		for (int i = 0; i < data.length; i++) {
			data[i] =  new StatusLevelData();
		}

		data[0].active = true;

		jbInit();
	}

	boolean raiseLevel(int level) {
		if (level < currentLevel) {
			return false;
		}

		if (level > currentLevel) {
			currentLevel = level;
			data[currentLevel].active = true;
		}

		return true;
	}

	public void setStatus( String message ) {
		updateProgressStatus(LEVEL_GENERIC, message);
	}

	public void startProgress( int level, int minValue, int maxValue, String message, boolean undetermined) {
		data[level].minValue = minValue;
		data[level].maxValue = maxValue;
		data[level].value = 0;
		data[level].message = message;
		data[level].undetermined = undetermined;
		data[level].active = true;

		if (raiseLevel(level)) {
			jProgress.setMinimum(minValue);
			jProgress.setValue(minValue);
			jProgress.setMaximum(maxValue);

			try {
				jProgress.setIndeterminate(undetermined);
			} catch (Throwable t) {
				// we end up here if the method is not implemented and we don't have indeterminate progress
				// bars: come up with our own...
				if (undetermined) {
					data[level].undeterminedThread = new UndeterminedThread(this, level);
					data[level].undeterminedThread.start();
				}
			}

			jStatus.setText(message);
		}
	}

	public void updateProgressValue( int level, int value ) {
		data[level].value = value;

		if (level == currentLevel && data[level].active) {
			jProgress.setValue( value );
		} else {
			//Log.log(Log.TRACE, MODULE, "Trying to use updateProgressValue when not progressOn or with wrong level");
			//Log.logStack(Log.TRACE, MODULE);
		}
	}

	public void updateProgressValue( int level, int value, int maxValue ) {
		data[level].maxValue = maxValue;
		data[level].value = value;

		if (level == currentLevel && data[level].active) {
			jProgress.setValue( value );
			jProgress.setMaximum( maxValue );
		} else {
			//Log.log(Log.TRACE, MODULE, "Trying to use updateProgressValue when not progressOn or with wrong level");
			//Log.logStack(Log.TRACE, MODULE);
		}
	}

	public void updateProgressStatus( int level, String message ) {
		data[level].message = message;

		if (level == currentLevel && data[level].active) {
			jStatus.setText( message );
		} else {
			//Log.log(Log.TRACE, MODULE, "Trying to use updateProgressStatus when not progressOn or with wrong level");
			//Log.logStack(Log.TRACE, MODULE);
		}
	}

	public void setUndetermined(int level, boolean undetermined) {
		//To change body of implemented methods use Options | File Templates.
	}

	public int getProgressValue(int level) {
		return data[level].value;
	}

	public int getProgressMinValue(int level) {
		return data[level].minValue;
	}

	public int getProgressMaxValue(int level) {
		return data[level].maxValue;
	}

	public void stopProgress( int level, String message ) {
		data[LEVEL_GENERIC].message = message;

		if (level == currentLevel && data[level].active) {
			if (data[level].undeterminedThread != null) {
				data[level].undeterminedThread.interrupt();
				data[level].undeterminedThread = null;
			}

			// find the next active level
			currentLevel = level - 1;
			while (currentLevel >= 0 && data[currentLevel].active == false) {
				currentLevel--;
			}

			if (currentLevel == -1) {
				jStatus.setText(message);

				jProgress.setValue(jProgress.getMinimum());

				try {
					jProgress.setIndeterminate(false);
				} catch (Throwable t) {}
			} else {
				jProgress.setMinimum(data[currentLevel].minValue);
				jProgress.setMaximum(data[currentLevel].maxValue);
				jProgress.setValue(data[currentLevel].value);
				try {
					jProgress.setIndeterminate(data[currentLevel].undetermined);
				} catch (Throwable t) {}

				jStatus.setText(data[currentLevel].message);
			}
		}

		if (level > LEVEL_GENERIC) {
			data[level].active = false;
		}
	}

	public void setInProgress(boolean inProgress) {
		mf.setInProgress(inProgress);
	}

	public void error(String message ) {
		JOptionPane.showMessageDialog(DialogUtil.findParentWindow(this), message, GRI18n.getString(MODULE, "Error"), JOptionPane.ERROR_MESSAGE);
	}

	public void jbInit() {
		jProgress.setMinimumSize( new Dimension( 10, 18 ) );
		jProgress.setPreferredSize( new Dimension( 150, 18 ) );
		jProgress.setStringPainted( false );

		jStatus.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED, Color.white, SystemColor.control, SystemColor.control, Color.gray ) );
		jStatus.setMinimumSize( new Dimension( 100, 18 ) );
		jStatus.setPreferredSize( new Dimension( 38, 18 ) );

		setLayout( new GridBagLayout() );
		add( jProgress, new GridBagConstraints( 1, 0, 1, 1, 0.25, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
		add( jStatus, new GridBagConstraints( 0, 0, 1, 1, 0.75, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
	}

	class StatusLevelData {
		boolean active = false;
		String message;
		int minValue;
		int maxValue;
		int value;
		boolean undetermined;
		UndeterminedThread undeterminedThread;
	}

	public class UndeterminedThread extends Thread {
		StatusUpdate su;
		int level;

		public UndeterminedThread(StatusUpdate su, int level) {
			this.su = su;
			this.level = level;
		}

		public void run() {
			boolean forward = true;
			while (! interrupted() ) {
				if (su.getProgressValue(level) >= su.getProgressMaxValue(level)) {
					forward = false;
				} else if (su.getProgressValue(level) <= su.getProgressMinValue(level)) {
					forward = true;
				}

				su.updateProgressValue(level, su.getProgressValue(level) + (forward?1:-1));

				try {
					sleep(500);
				} catch (InterruptedException e) {}
			}
		}
	}
}
