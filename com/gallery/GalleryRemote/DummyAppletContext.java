/**
 * Copyright(c) 1996 DTAI, Incorporated (http://www.dtai.com)
 *
 *                        All rights reserved
 *
 * Permission to use, copy, modify and distribute this material for
 * any purpose and without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all
 * copies, and that the name of DTAI, Incorporated not be used in
 * advertising or publicity pertaining to this material without the
 * specific, prior written permission of an authorized representative of
 * DTAI, Incorporated.
 *
 * DTAI, INCORPORATED MAKES NO REPRESENTATIONS AND EXTENDS NO WARRANTIES,
 * EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR ANY PARTICULAR PURPOSE, AND THE WARRANTY AGAINST
 * INFRINGEMENT OF PATENTS OR OTHER INTELLECTUAL PROPERTY RIGHTS.  THE
 * SOFTWARE IS PROVIDED "AS IS", AND IN NO EVENT SHALL DTAI, INCORPORATED OR
 * ANY OF ITS AFFILIATES BE LIABLE FOR ANY DAMAGES, INCLUDING ANY
 * LOST PROFITS OR OTHER INCIDENTAL OR CONSEQUENTIAL DAMAGES RELATING
 * TO THE SOFTWARE.
 */

package com.gallery.GalleryRemote;

import java.awt.*;
import java.applet.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * DummyAppletContext - implements AppletContext and AppletStub to allow any
 * applet to easily run as an application.  The only thing it can't do is
 * access URL's.  Applet parameters are entered on the command line with
 * name as one word and value as the next.
 *
 * @version	1.1
 * @author	DTAI, Incorporated
 */

public class DummyAppletContext
    extends Frame
    implements AppletStub, AppletContext, URLStreamHandlerFactory {

    private TextField status;
    private Hashtable params = new Hashtable();
    private Vector applets = new Vector();

    private int initial_width;
    private int initial_height;

	/**
	 * Entry point into the standalone program.
	 *
	 * @param args  the command line arguments
	 */
    public static void main ( String args[] ) {
        new DummyAppletContext( args );
    }

	/**
	 * Constructor for the main class, given an existing applet object.
	 *
	 * @param applet            the applet embedded in this AppletContext
	 * @param args              the command line arguments.  Contains possibly
	 *                          height and width, and any applet parameters
	 */
    public DummyAppletContext( Applet applet, String args[] ) {

        this( applet, 640, 480, args );
    }

	/**
	 * Constructor for the main class, given an existing applet object and a default
	 * frame (window) width and height.
	 *
	 * @param applet            the applet embedded in this AppletContext
	 * @param default_width     the default width of the window
	 * @param default_height    the default width of the window
	 * @param args              the command line arguments.  Contains possibly
	 *                          height and width, and any applet parameters
	 */
    public DummyAppletContext( Applet applet, int default_width, int default_height,
                               String args[] ) {

        super ( applet.getClass().getName() );

        init( applet, default_width, default_height, args, 0 );
    }

	/**
	 * Constructor for the main class, from the command line arguments.
	 *
	 * @param args  the command line arguments.  Contains the name of the applet
	 *              class, possibly height and width, and any applet parameters.
	 */
    public DummyAppletContext( String args[] ) {

        super ( args[0] );

        try {
            Applet applet = (Applet)Class.forName( args[0] ).newInstance();

            init( applet, 640, 480, args, 1 );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

	/*
	 * PRIVATE initialization function.
	 *
	 * @param applet            the applet embedded in this AppletContext
	 * @param default_width     the default width of the window
	 * @param default_height    the default width of the window
	 * @param args              the command line arguments.  Contains possibly
	 *                          height and width, and any applet parameters
	 * @param startidx          index in the args array at which to transitionStart parsing
	 */
    private void init( Applet applet, int default_width, int default_height,
                       String args[], int startidx ) {

        //URL.setURLStreamHandlerFactory( this );

        applets.addElement( applet );
        applet.setStub(this);

        initial_width = default_width;
        initial_height = default_height;

        parseArgs( args, startidx );

        status = new TextField();
        status.setEditable( false );

        add( "Center", applet );
        add( "South", status );

        appletResize( initial_width, initial_height );

        show();
        applet.init();
        applet.start();
    }

	/**
	 * Parse the command line arguments.  Get the initial width and height of
	 * the window if specified (-width [value] -height [value]), and the
	 * applet parameters (name value pairs).
	 *
	 * @param args              the command line arguments.  Contains possibly
	 *                          height and width, and any applet parameters
	 * @param startidx          index in the args array at which to transitionStart parsing
	 */
    public void parseArgs( String args[], int startidx ) {
        for ( int idx = startidx; idx < ( args.length - startidx ); idx+=2 ) {
            try {
                if ( args[idx].equals( "-width" ) ) {
                    initial_width = Integer.parseInt( args[idx+1] );
                }
                else if ( args[idx].equals( "-height" ) ) {
                    initial_height = Integer.parseInt( args[idx+1] );
                }
                else {
                    params.put( args[idx], args[idx+1] );
                }
            }
            catch ( NumberFormatException nfe ) {
                System.err.println("Warning: command line argument "+args[idx]+
                                   " is not a valid number." );
            }
        }
    }

	/**
	 * Event handler to catch the Frame (window) close action,
	 * and exit the program.
	 *
	 * @param evt   The event that occurred
	 * @return      false if the event was not handled by this object.
	 */
    public boolean handleEvent( Event evt ) {

        if ( evt.id == Event.WINDOW_DESTROY ) {
            System.exit(0);
        }

        return super.handleEvent(evt);
    }

/************ AppletStub methods *************/

    /**
     * Returns true if the applet is active.
     *
     * @return  always true
     */
    public boolean isActive() { return true; }

    /**
     * Gets the document URL.
     *
	 * @return      a "file:" URL for the current directory
     */
    public URL getDocumentBase() {
        URL url = null;
        try {
            File dummy = new File( "dummy.html" );
            String path = dummy.getAbsolutePath();
            if ( ! File.separator.equals( "/" ) ) {
                StringBuffer buffer = new StringBuffer();
                if ( path.charAt(0) != File.separator.charAt(0) ) {
                    buffer.append( "/" );
                }
                StringTokenizer st = new StringTokenizer( path, File.separator );
                while ( st.hasMoreTokens() ) {
                    buffer.append( st.nextToken() + "/" );
                }
                if ( File.separator.equals( "\\" ) &&
                     ( buffer.charAt(2) == ':' ) ) {
                    buffer.setCharAt( 2, '|' );
                }
                else {
                }
                path = buffer.toString();
                path = path.substring( 0, path.length()-1 );
            }
            url = new URL( "file", "", -1, path );
        }
        catch ( MalformedURLException mue ) {
            mue.printStackTrace();
        }
        return url;
    }

    /**
     * Gets the codebase URL.
     *
	 * @return      in this case, the same value as getDocumentBase()
     */
    public final URL getCodeBase() { return getDocumentBase(); }

    /**
     * Gets a parameter of the applet.
     *
	 * @param name  the name of the parameter
	 * @return      the value, or null if not defined
	 */
    public final String getParameter( String name ) {
        return (String)params.get( name );
    }

	/**
	 * Gets a handler to the applet's context.
	 *
	 * @return  this object
	 */
    public final AppletContext getAppletContext() { return this; }

	/**
	 * Called when the applet wants to be resized.  This causes the
	 * Frame (window) to be resized to accomodate the new Applet size.
	 *
	 * @param width     the new width of the applet
	 * @param height    the new height of the applet
	 */
    public void appletResize( int width, int height ) {

        Insets insets = insets();

        resize( ( width + insets.left + insets.right ),
                ( height + status.preferredSize().height +
                  insets.top + insets.bottom ) );
    }

/************ AppletContext methods *************/

	/**
     * Gets an audio clip.  (There doesn't seem to be a "Toolkit" for
     * audio clips in my JDK, so this always returns null.  You could
     * implement this differently, returning a dummy AudioClip object
     * for which the class could be defined at the bottom of this file.)
	 *
	 * @param url   URL of the AudioClip to load
	 * @return      the AudioClip object if it exists (in our case,
	 *              this is always null
	 */
    public final AudioClip getAudioClip( URL url ) { return null; }

	/**
     * Gets an image. This usually involves downloading it
     * over the net. However, the environment may decide to
     * cache images. This method takes an array of URLs,
     * each of which will be tried until the image is found.
	 *
	 * @param url   URL of the Image to load
	 * @return      the Image object
	 */
    public final Image getImage( URL url ) {
        return Toolkit.getDefaultToolkit().getImage( filenameFromURL( url ) );
    }

	/*
     * PRIVATE utility function.  Ignores the protocol, and returns a
     * filename for a file on the local filesystem (which may or may
     * not exist, of course).
	 *
	 * @param url   URL to be converted to a filename on the local
	 *              filesystem.
	 * @return      the filename
	 */
    private String filenameFromURL( URL url ) {
        String filename = url.getFile();
        if ( filename.charAt(1) == '|' ) {
            StringBuffer buf = new StringBuffer( filename );
            buf.setCharAt( 1, ':' );
            filename = buf.toString();
        }
        else if ( filename.charAt(2) == '|' ) {
            StringBuffer buf = new StringBuffer( filename );
            buf.setCharAt( 2, ':' );
            filename = buf.toString();
        }
        return filename;
    }

	/**
     * Gets an applet by name.
     *
	 * @param name  the name of the applet
     * @return      null if the applet does not exist, and it never
     *              does since we never name the applet.
	 */
    public final Applet getApplet( String name ) { return null; }

	/**
     * Enumerates the applets in this context. Only applets
     * that are accessible will be returned. This list always
     * includes the applet itself.
	 *
	 * @return  the Enumeration -- contains ONLY the applet created with
	 *          this DummyAppletContext
	 */
    public final Enumeration getApplets() { return applets.elements(); }

	/**
     * Shows a new document. This may be ignored by
     * the applet context (and in our case, it is, but we'll show the
     * user, in the status area, that the document was requested and
     * WOULD be loaded if in a browser).
	 *
	 * @param url   URL to load
	 */
    public void showDocument( URL url ) {
        status.setText( "AppletContext request to show URL " +
                        url.toString() );
    }

	/**
     * Show a new document in a target window or frame. This may be ignored by
     * the applet context.  (Again, it is ignored, but we'll show the
     * request information to the user in the status area.)
     *
     * This method accepts the target strings:
     *   _self		show in current frame
     *   _parent	show in parent frame
     *   _top		show in top-most frame
     *   _blank		show in new unnamed top-level window
     *   <other>	show in new top-level window named <other>
	 *
	 * @param url       URL to load
	 * @param target    the target string
	 */
    public void showDocument( URL url, String target ) {
        status.setText( "AppletContext request to show URL " +
                        url.toString() +
                        " in target: " + target );
    }

	/**
     * Show a status string in the status area (the Text object at the bottom
     * of the window.
	 *
	 * @param text  the text to display
	 */
    public void showStatus( String text ) { status.setText( text ); }

	/**
	 * Associates the specified stream with the specified key in this
	 * applet context. If the applet context previously contained a mapping
	 * for this key, the old value is replaced.
	 * <p/>
	 * For security reasons, mapping of streams and keys exists for each
	 * codebase. In other words, applet from one codebase cannot access
	 * the streams created by an applet from a different codebase
	 * <p/>
	 *
	 * @param key    key with which the specified value is to be associated.
	 * @param stream stream to be associated with the specified key. If this
	 *               parameter is <code>null<code>, the specified key is removed
	 *               in this applet context.
	 * @throws <code>IOException</code> if the stream size exceeds a certain
	 *                                  size limit. Size limit is decided by the implementor of this
	 *                                  interface.
	 * @since JDK1.4
	 */
	public void setStream(String key, InputStream stream) throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Returns the stream to which specified key is associated within this
	 * applet context. Returns <tt>null</tt> if the applet context contains
	 * no stream for this key.
	 * <p/>
	 * For security reasons, mapping of streams and keys exists for each
	 * codebase. In other words, applet from one codebase cannot access
	 * the streams created by an applet from a different codebase
	 * <p/>
	 *
	 * @param key key whose associated stream is to be returned.
	 * @return the stream to which this applet context maps the key
	 * @since JDK1.4
	 */
	public InputStream getStream(String key) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Finds all the keys of the streams in this applet context.
	 * <p/>
	 * For security reasons, mapping of streams and keys exists for each
	 * codebase. In other words, applet from one codebase cannot access
	 * the streams created by an applet from a different codebase
	 * <p/>
	 *
	 * @return an Iterator of all the names of the streams in this applet
	 *         context.
	 * @since JDK1.4
	 */
	public Iterator getStreamKeys() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

/************ URLStreamHandlerFactory methods *************/

	/**
     * Creates a new URLStreamHandler instance with the specified protocol.
     *
     * @param protocol  the protocol to use (ftp, http, nntp, etc.).
     *                  THIS PROTOCOL IS IGNORED BY THIS APPLET CONTEXT
     */
    public URLStreamHandler createURLStreamHandler( String protocol ) {
        return new DummyURLStreamHandler();
    }
}

/*
 * A URL stream handler for all protocols, used to return our
 * dummy implementation of URLConnection to open up a local
 * file when called upon.
 */

class DummyURLStreamHandler extends URLStreamHandler {

    protected final URLConnection openConnection( URL u ) throws IOException {
        return new DummyURLConnection( u );
    }

}

/*
 * Our dummy implementation of URLConnection used to open up a local
 * file when called upon with a given URL of ANY protocol type.  This
 * allows the applet to easily use the "getInputStream()" function.
 */

class DummyURLConnection extends URLConnection {

    boolean connected = false;
    InputStream instream;

    /*
     * Constructor for the DummyURLConnection
     */

    protected DummyURLConnection( URL url ) {
        super( url );
    }

    /*
     * open the local file
     */

    public void connect() throws IOException {
        if ( ! connected ) {
            String filename = url.getFile();
            if ( filename.charAt(1) == '|' ) {
                StringBuffer buf = new StringBuffer( filename );
                buf.setCharAt( 1, ':' );
                filename = buf.toString();
            }
            else if ( filename.charAt(2) == '|' ) {
                StringBuffer buf = new StringBuffer( filename );
                buf.setCharAt( 2, ':' );
                filename = buf.toString();
            }
            instream = new FileInputStream( filename );
        }
    }

    /*
     * return the open stream to the local file (open if necessary).
     */

    public InputStream getInputStream() throws IOException {
        if ( ! connected ) {
            connect();
        }
        if ( instream == null ) {
            throw new IOException();
        }
        return instream;
    }
}