package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.NaturalOrderComparator;
import com.gallery.GalleryRemote.util.UrlMessageDialog;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;

public class GalleryComm3 extends GalleryComm {
	private static final String MODULE = "GalComm3";

	private static String api = "index.php/rest/";

	public GalleryComm3(Gallery g, StatusUpdate su) {
		super(g, su);
	}

	public void fetchAlbums(StatusUpdate su, boolean async) {
		doTask(new AlbumListTask(su), async);
	}

	/* -------------------------------------------------------------------------
	* UTILITY METHODS
	*/
	void doTask(GalleryTask task, boolean async) {
		if (async) {
			Thread t = new Thread(task);
			t.start();
		} else {
			task.run();
		}
	}

	void status(StatusUpdate su, int level, String message) {
		Log.log(Log.LEVEL_INFO, MODULE, message);
		su.updateProgressStatus(level, message);
	}

	void error(StatusUpdate su, String message) {
		status(su, StatusUpdate.LEVEL_GENERIC, message);
		su.error(message);
	}

	void trace(String message) {
		Log.log(Log.LEVEL_TRACE, MODULE, message);
	}

	/* -------------------------------------------------------------------------
	* INNER CLASSES
	*/

	/**
	 * An extension of GalleryTask to handle fetching albums.
	 */
	class AlbumListTask extends GalleryTask {
		AlbumListTask(StatusUpdate su) {
			super(su);
		}

		void runTask() {
			su.startProgress(StatusUpdate.LEVEL_BACKGROUND, 0, 10, GRI18n.getString(MODULE, "albmFtchng", new Object[] {g.toString()}), true);

			long startTime = System.currentTimeMillis();

			try {
				list();
			} catch (IOException e) {
				Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			}

			// tell the tree to reload
			g.reload();

			Log.log(Log.LEVEL_INFO, MODULE, "execution time for AlbumList: " + (System.currentTimeMillis() - startTime));

			su.stopProgress(StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "fetchComplete"));
		}

		private void list() throws IOException {
			HttpPost post = new HttpPost(g.getUrlString() + api + "item/1");
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("type", "album"));
			formparams.add(new BasicNameValuePair("scope", "all"));
			UrlEncodedFormEntity entity = null;
			try {
				entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			post.setEntity(entity);
			post.setHeader("X-Gallery-Request-Method", "get");
			post.setHeader("X-Gallery-Request-Key", g.getKey());

			HttpResponse response = httpclient.execute(post);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity rentity = response.getEntity();

				JSONParser parser = new JSONParser();
				ListContentHandler lch = new ListContentHandler();

				HashMap<String,String> url2parentUrl = new HashMap<String,String>();
				HashMap<String,Album> url2album = new HashMap<String,Album>();
				ArrayList<Album> albums = new ArrayList<Album>();

				try {
					BufferedReader entityReader = new BufferedReader(new InputStreamReader(rentity.getContent()));

					Album rootAlbum = g.createRootAlbum();
					rootAlbum.setUrl(post.getURI().toString());
					rootAlbum.setSuppressEvents(true);
					lch.setAlbum(rootAlbum);
					parser.parse(entityReader, lch, true);
					rootAlbum.setSuppressEvents(false);
					// map album names to albums
					url2album.put(rootAlbum.getUrl(), rootAlbum);
					url2parentUrl.put(rootAlbum.getUrl(), lch.getParentUrl());

					while (!lch.isEnd()) {
						Album a = g.newAlbum();
						a.setSuppressEvents(true);
						lch.setAlbum(a);
						parser.parse(entityReader, lch, true);
						a.setSuppressEvents(false);

						albums.add(a);

						// map album names to albums
						url2album.put(a.getUrl(), a);
						url2parentUrl.put(a.getUrl(), lch.getParentUrl());
					}
				} catch (ParseException e) {
					Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Created " + albums.size() + " albums");

				// link albums to parents
				for (Object o : url2parentUrl.keySet()) {
					String name = (String) o;
					String parentName = url2parentUrl.get(name);
					Album child = url2album.get(name);
					Album parent = url2album.get(parentName);

					if (child != null && parent != null) {
						parent.add(child);
					}
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Linked " + url2parentUrl.size() + " albums to their parents");

				// reorder
				Collections.sort(albums, new NaturalOrderComparator());
				Collections.reverse(albums);
				ArrayList<Album> orderedAlbums = new ArrayList<Album>();
				int depth = 0;
				while (!albums.isEmpty()) {
					Iterator<Album> it = albums.iterator();
					while (it.hasNext()) {
						Album a = it.next();

						try {
							if (a.getAlbumDepth() == depth) {
								it.remove();
								a.sortSubAlbums();

								Album parentAlbum = a.getParentAlbum();
								if (parentAlbum == null) {
									orderedAlbums.add(0, a);
								} else {
									int i = orderedAlbums.indexOf(parentAlbum);
									orderedAlbums.add(i + 1, a);
								}
							}
						} catch (IllegalArgumentException e) {
							it.remove();
							Log.log(Log.LEVEL_TRACE, MODULE, "Gallery server album list is corrupted: " +
									"album " + a.getName() + " has a bad containment hierarchy.");
						}
					}

					depth++;
				}

				Log.log(Log.LEVEL_TRACE, MODULE, "Ordered " + orderedAlbums.size() + " albums");

				status(su, StatusUpdate.LEVEL_BACKGROUND, GRI18n.getString(MODULE, "ftchdAlbms"));

				//g.setAlbumList(orderedAlbums);
			} else {
				throw new GR2Exception("Connection error: " + response.getStatusLine().toString());
			}
		}

		private class ListContentHandler implements ContentHandler {
			private boolean end = false;
			private String key;
			private Album a;
			private String parentUrl = null;
			private int curState = 0;   // would have used an enum, but can't inside an inner class...
										// 0 = init, 1 = root album, 2 = members

			public void setAlbum(Album a) {
				this.a = a;
			}

			public String getParentUrl() {
				return parentUrl;
			}

			public boolean isEnd() {
				return end;
			}

			public void startJSON() throws ParseException, IOException {
				end = false;
			}

			public void endJSON() throws ParseException, IOException {
				end = true;
			}

			public boolean primitive(Object value) throws ParseException, IOException {
				if (key.equals("title")) {
					a.setTitle((String) value);
				} else if (key.equals("description")) {
					a.setDescription((String) value);
				} else if (key.equals("can_edit")) {
					a.setCanEdit((Boolean) value);
				} else if (curState == 2) { // to change to handle objects in the members array
					a.setUrl((String) value);
					return false;
				}

				return true;
			}

			public boolean startArray() throws ParseException, IOException {
				return true;
			}

			public boolean startObject() throws ParseException, IOException {
				return true;
			}

			public boolean startObjectEntry(String key) throws ParseException, IOException {
				this.key = key;

				if (key.equals("entity")) {
					if (curState == 0) {
						curState = 1;
					}
				} else if (key.equals("members")) {
					if (curState == 1) {
						curState = 2;
						return false;
					}
				}
				
				return true;
			}

			public boolean endArray() throws ParseException, IOException {
				return false;
			}

			public boolean endObject() throws ParseException, IOException {
				return true;
			}

			public boolean endObjectEntry() throws ParseException, IOException {
				return true;
			}
		}
	}

	/**
	 * This class serves as the base class for each GalleryComm task.
	 */
	abstract class GalleryTask implements Runnable {
		StatusUpdate su;
		boolean interrupt = false;
		boolean terminated = false;
		Thread thread = null;

		public GalleryTask(StatusUpdate su) {
			if (su == null) {
				this.su = new StatusUpdateAdapter() {
				};
			} else {
				this.su = su;
			}
		}

		public void run() {
			thread = Thread.currentThread();
			su.setInProgress(true);

			runTask();

			cleanUp();
		}

		public void interrupt() {
			thread.interrupt();
			interrupt = true;
		}

		public void cleanUp() {
			su.setInProgress(false);
			terminated = true;
		}

		abstract void runTask();
	}
}
