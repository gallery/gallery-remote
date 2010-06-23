package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Picture;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jun 18, 2010
 * Time: 4:05:33 PM
 */
public class GalleryCommTest {
	public static final String MODULE = "CommTest";
	private Gallery g;
	private StatusUpdate su;
	private GalleryComm comm;

	@Test
	public void testFetchAlbums() {
		Log.log(Log.LEVEL_INFO, MODULE, "Starting fetchAlbums");
		comm.fetchAlbums(su, false);
	}

	@Test
	public void testFetchAlbumsBadKey() {
		Log.log(Log.LEVEL_INFO, MODULE, "Starting fetchAlbums");
		g.setKey("badkey");
		comm.fetchAlbums(su, false);
	}

	@Test
	public void testCheckAuthBadKey() {
		g.setKey("badkey");
		g.setUsername("admin");
		g.setPassword("admin");
		assertTrue(comm.checkAuth());
	}

	@Test
	public void testGetKey() {
		g.setKey("");
		g.setUsername("admin");
		g.setPassword("admin");
		assertTrue(comm.checkAuth());
	}

	@Test
	public void testGetKeyBadPass() {
		g.setKey("");
		g.setUsername("admin");
		g.setPassword("adminfalse");
		assertFalse(comm.checkAuth());
	}

	@Test
	public void testUploadFiles() {
		comm.fetchAlbums(su, false);

		Album a = g.getRootAlbum();
		Picture p = new Picture(g, new File("Voeux2010.jpg"));
		a.addPicture(p);
		assertNull(p.getUrl());
		comm.uploadFiles(su, false);
		assertNotNull(p.getUrl());
	}
	@Test
	public void testUploadFilesToRoot() {
		Album a = new Album(g);
		a.setUrl(g.getUrlString() + "index.php/rest/item/1");
		Picture p = new Picture(g, new File("Voeux2010.jpg"));
		a.addPicture(p);
		g.setRoot(a);
		assertNull(p.getUrl());
		comm.uploadFiles(su, false);
		assertNotNull(p.getUrl());
	}

	@BeforeClass
	public static void setupAll() {
		GalleryRemote.createInstance("com.gallery.GalleryRemote.GalleryRemoteMainFrame", null);

		GalleryRemote._().initializeGR();
	}

	@Before
	public void setup() {
		su = new StatusUpdateAdapter();
		g = new Gallery(su);
		g.setUrlString("http://localhost/~paour/gallery3/");
		g.setKey("7c507344c89daeeb9c2c645d2d4b73b7");
		comm = GalleryComm.getCommInstance(su, g);
	}

	@AfterClass
	public static void shutdownAll() {
		Log.shutdown();
	}
}
