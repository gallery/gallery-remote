package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Gallery;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jun 18, 2010
 * Time: 4:05:33 PM
 */
public class GalleryCommTest {
	public static final String MODULE = "CommTest";

	@Test
	public void testFetchAlbums() throws Exception {
		StatusUpdate su = new StatusUpdateAdapter();
		Gallery g = new Gallery(su);
		g.setUrlString("http://localhost/~paour/gallery3/");
		Log.log(Log.LEVEL_INFO, MODULE, "Starting fetchAlbums");
		GalleryComm comm = GalleryComm.getCommInstance(su, g);
		Log.log(Log.LEVEL_INFO, MODULE, "Starting fetchAlbums");
		comm.fetchAlbums(su, false);
	}

	@Before
	public void setup() {
		GalleryRemote.createInstance("com.gallery.GalleryRemote.GalleryRemoteMainFrame", null);

		GalleryRemote._().initializeGR();
	}
}
