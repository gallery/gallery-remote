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

import com.gallery.GalleryRemote.prefs.PreferenceNames;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *  Log manager
 *
 *@author     paour
 *@created    August 18, 2002
 */
public class Log extends Thread implements PreferenceNames
{
	public final static int CRITICAL = 0;
	public final static int ERROR = 1;
	public final static int INFO = 2;
	public final static int TRACE = 3;

	static String levelName[] = {"CRITI", "ERROR", "INFO ", "TRACE"};

	public final static int sleepInterval = 500;
	public final static int moduleLength = 10;
	public final static String emptyModule = "          ";
	public final static String emptyTime = "       ";
	public final static long startTime = System.currentTimeMillis();

	public static int maxLevel = TRACE;
	public static boolean toSysOut;

	static int threadPriority = ( Thread.MIN_PRIORITY + Thread.NORM_PRIORITY ) / 2;
	static Log singleton = new Log();
	
	BufferedWriter writer = null;
	List logLines = Collections.synchronizedList( new LinkedList() );
	boolean running = false;

	private Log() {
		try {
			writer = new BufferedWriter( new FileWriter( "log.txt" ) );
		} catch ( IOException e ) {
			System.err.println( "Can't open log file 'log.txt'. Disabling log." );
			maxLevel = -1;
		}

		setPriority( threadPriority );
		start();
	}


	public static void log( int level, String module, String message ) {
		if ( level <= maxLevel ) {
			if ( module == null ) {
				module = emptyModule;
			} else {
				module = ( module + emptyModule ).substring( 0, moduleLength );
			}

			String time = emptyTime + (System.currentTimeMillis()-startTime);
			time = time.substring(time.length() - emptyTime.length());

			singleton.logLines.add( time + "|"
					 + levelName[level] + "|"
					 + module + "|"
					 + message );
		}
		/*if (singleton.logLines.isEmpty())
		{
			Thread t = new Thread(singleton);
			t.setPriority(threadPriority);
			t.start();
		}*/
	}

	public static void log( int level, Class c, String message ) {
		log( level, c.getName(), message );
	}

	public static void log( int level, Object o, String message ) {
		log( level, getShortClassName(o.getClass()), message );
	}

	public static void log( int level, String message ) {
		log( level, (String) null, message );
	}

	public static void log( Class c, String message ) {
		log( TRACE, getShortClassName(c), message );
	}

	public static void log( Object o, String message ) {
		log( TRACE, getShortClassName(o.getClass()), message );
	}

	public static void log( String module, String message ) {
		log( TRACE, module, message );
	}

	public static void log( String message ) {
		log( TRACE, (String) null, message );
	}
	
	public static void logStack(int level, String module) {
		if ( level <= maxLevel ) {
			CharArrayWriter caw = new CharArrayWriter();
			try {
				throw new Exception("Dump stack");
			} catch (Exception e) {
				e.printStackTrace(new PrintWriter(caw));
			}
			
			log(level, module, caw.toString());
		}
	}
	
	public static void logException(int level, String module, Throwable t) {
		if ( level <= maxLevel ) {
			//log(level, module, t.toString());
			
			CharArrayWriter caw = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(caw));
			
			log(level, module, caw.toString());
		}
	}
	
	public static String getShortClassName(Class c) {
		String name = c.getName();
		int i = name.lastIndexOf(".");
		
		if ( i == -1 ) {
			return name;
		} else {
			return name.substring( i + 1 );
		}
	}
	
	public static void shutdown() {
		singleton.running = false;
		try {
			singleton.join();
		} catch ( InterruptedException ee) {
			System.err.println( "Thread killed for some reason" );
		}
	}


	/**
	 *  Main processing method for the Log object
	 */
	public void run() {
		running = true;
		try {
			while ( running ) {
				Thread.sleep( sleepInterval );
				while ( !logLines.isEmpty() ) {
					String s = (String) logLines.remove( 0 );
					writer.write( s );
					writer.newLine();

					if (toSysOut) {
						System.out.println(s);
					}
				}
				
				writer.flush();
			}
			
			writer.close();
		} catch ( IOException e ) {
			System.err.println( "Can't write to log file. Disabling log..." );
			maxLevel = -1;
		} catch ( InterruptedException e) {
			System.err.println( "Thread killed for some reason" );
		}
	}

	public static void setMaxLevel() {
		if (maxLevel != GalleryRemote.getInstance().properties.getIntProperty( LOG_LEVEL )) {
			maxLevel = GalleryRemote.getInstance().properties.getIntProperty( LOG_LEVEL );
			singleton.logLines.add( emptyTime + "|"
					+ levelName[TRACE] + "|"
					+ emptyModule + "|"
					+ "Setting Log level to " + levelName[maxLevel] );
		}

		toSysOut = GalleryRemote.getInstance().properties.getBooleanProperty( "toSysOut" );
	}

	static {
		setMaxLevel();
	}

	/*
	public static void main( String[] param ) {
		try {
			Thread.sleep(3000);
		} catch ( InterruptedException ee) {
			System.err.println( "Thread killed for some reason" );
		}
		log( "Just a message" );
		log( "a random module", "Just a message" );
		log( new Base64(), "A base64 object" );
		log( singleton.getClass(), "A class" );
		log( CRITICAL, "A critical message" );
		log( ERROR, "An error message" );
		log( INFO, "An informational message" );
		log( TRACE, "A trace message" );
		
		new CountingThread().setCounterName("a").start();
		new CountingThread().setCounterName("b").start();
		new CountingThread().setCounterName("c").start();
		
		//shutdown();
		
		log("can't log now...");
	}
	
	static class CountingThread extends Thread {
		String name;
		
		public Thread setCounterName(String name) {
			this.name = name;
			return this;
		}
		
		public void run() {
			for (int i = 1; i < 100; i++) {
				Log.log(name, "" + i);
				try {
					sleep((int) (Math.random() * 1000));
				} catch ( InterruptedException e) {
					System.err.println( "Thread killed for some reason" );
				}
			}
		}
	}*/
}

