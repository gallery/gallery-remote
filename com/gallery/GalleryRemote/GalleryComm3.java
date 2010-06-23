package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.model.Album;
import com.gallery.GalleryRemote.model.Picture;
import com.gallery.GalleryRemote.util.GRI18n;
import com.gallery.GalleryRemote.util.NaturalOrderComparator;
import com.gallery.GalleryRemote.util.UrlMessageDialog;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
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

	public void uploadFiles(StatusUpdate su, boolean async) {
		UploadTask uploadTask = new UploadTask(su);
		doTask(uploadTask, async);
	}
	
	public void newAlbum(StatusUpdate su, Album album, boolean async) {
		NewAlbumTask newAlbumTask = new NewAlbumTask(su, album);
		doTask(newAlbumTask, async);
	}

	public boolean checkAuth() {
		try {
			sendRequest(g.getUrlString() + api + "item/1", "get", (HttpEntity) null);
		} catch (IOException e) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			return false;
		}

		return true;
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

	public BufferedReader sendRequest(String url, String verb, List<NameValuePair> formparams) throws IOException {
		if (formparams == null) {
			return sendRequest(url, verb, (HttpEntity) null);
		}

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			return sendRequest(url, verb, entity);
		} catch (UnsupportedEncodingException e) {
			Log.logException(Log.LEVEL_CRITICAL, MODULE, e);
			return null;
		}
	}

	public BufferedReader sendRequest(String url, String verb, HttpEntity entity) throws IOException {
		HttpPost post = new HttpPost(url);

		Log.log(Log.LEVEL_TRACE, "Sending request: " + post.getRequestLine().toString());

		if (entity != null) {
			post.setEntity(entity);

			if (!(entity instanceof MultipartEntity)) {
				Log.log(Log.LEVEL_TRACE, "Parameters: " + EntityUtils.toString(entity));
			}
		}
		
		post.setHeader("X-Gallery-Request-Method", verb);

		boolean usedCachedKey = true;
		if (url.endsWith(api)) {
			// we're trying to get the key already
			usedCachedKey = false;
		} else {
			String key = g.getKey();
			if (key == null || key.length() == 0) {
				// no cached key, perform login
				key = getKey();
				usedCachedKey = false;
			}
			post.setHeader("X-Gallery-Request-Key", key);
		}

		HttpResponse response = httpclient.execute(post);

		int status = response.getStatusLine().getStatusCode();
		Log.log(Log.LEVEL_TRACE, "HTTP status: " + status);

		HttpEntity responseEntity = response.getEntity();
		responseEntity = new BufferedHttpEntity(responseEntity);
		Log.log(Log.LEVEL_TRACE, "HTTP response: " + EntityUtils.toString(responseEntity));

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
		switch (status) {
			case 200:
				// everything cool
				return bufferedReader;

			case 403:
				// security exception
				responseEntity.consumeContent();
				if (usedCachedKey) {
					// assume they key was stale, reset it
					g.setKey(null);

					return sendRequest(url, verb, entity);
				} else {
					g.logOut();
					throw new GR2Exception("Security exception");
				}

			default:
				throw new GR2Exception("Unknown return HTTP status: " + status);
		}
	}

	public String getKey() throws IOException {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("user", g.getUsername()));
		formparams.add(new BasicNameValuePair("password", g.getPassword()));

		BufferedReader r = sendRequest(g.getUrlString() + api, "post", formparams);

		String key = (String) JSONValue.parse(r);

		g.setKey(key);

		return key;
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
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("type", "album"));
			formparams.add(new BasicNameValuePair("scope", "all"));

			BufferedReader entityReader = sendRequest(g.getUrlString() + api + "item/1", "get", formparams);

			JSONParser parser = new JSONParser();
			ListContentHandler lch = new ListContentHandler();

			HashMap<String,String> url2parentUrl = new HashMap<String,String>();
			HashMap<String,Album> url2album = new HashMap<String,Album>();
			ArrayList<Album> albums = new ArrayList<Album>();

			try {
				Album rootAlbum = g.createRootAlbum();
				rootAlbum.setUrl(g.getUrlString() + api + "item/1");
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
			Collections.sort(albums, new NaturalOrderComparator<Album>());
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

	class UploadTask extends GalleryTask {
		//MyTransferListener transferListener;

		UploadTask(StatusUpdate su) {
			super(su);
		}

		void runTask() {
			ArrayList<Picture> pictures = g.getAllUploadablePictures();

			// get total file size
			long totalSize = 0;
			Iterator<Picture> iter = pictures.iterator();
			while (iter.hasNext()) {
				Picture p = iter.next();
				totalSize += p.getFileSize();
			}

//			transferListener = new MyTransferListener(su);
//			transferListener.sizeAllFiles = totalSize;
//			transferListener.numberAllFiles = pictures.size();

			su.startProgress(StatusUpdate.LEVEL_UPLOAD_ALL, 0, 100, GRI18n.getString(MODULE, "upPic"), false);

			if (su instanceof UploadProgress) {
				((UploadProgress) su).setCancelListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ALL, GRI18n.getString(MODULE, "upStop"));
						su.setUndetermined(StatusUpdate.LEVEL_UPLOAD_ALL, true);
						interrupt();
						long startTime = System.currentTimeMillis();

						while (!terminated && System.currentTimeMillis() < startTime + 10000) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						}

						if (!terminated) {
							Log.log(Log.LEVEL_ERROR, "Thread would not terminate properly: killing it");
							thread.stop();

							// since we killed the thread, it's not going to clean up after itself
							cleanUp();
						}

						((UploadProgress) su).done();
					}
				});
			}

			// upload each file, one at a time
			boolean allGood = true;
			//int uploadedCount = 0;
			iter = pictures.iterator();
			while (iter.hasNext() /*&& allGood*/ && !interrupt) {
				Picture p = iter.next();

				/*Object[] params = {
						p.toString(),
						new Integer((uploadedCount + 1)),
						new Integer(pictures.size()),
						new Integer((int) ((transferListener.sizeFilesDone + transferListener.sizeThisFile) / 1024 / 1024)),
						new Integer((int) (transferListener.sizeAllFiles / 1024 / 1024)),
						transferListener.getProjectedTimeLeft()
				};
				su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ALL, GRI18n.getString(MODULE, "upStatus", params));*/

				allGood = uploadPicture(p);

				//su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ALL, ++uploadedCount);

				if (allGood) {
					p.getParentAlbum().removePicture(p);
				}
			}

			if (allGood) {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_ALL, GRI18n.getString(MODULE, "upComplete"));

				if (su instanceof UploadProgress) {
					if (((UploadProgress) su).isShutdown()) {
						GalleryRemote._().getCore().shutdown(true);
					}
				}

				g.setDirty(false);

				GalleryRemote._().getCore().flushMemory();
			} else {
				su.stopProgress(StatusUpdate.LEVEL_UPLOAD_ALL, GRI18n.getString(MODULE, "upFailed"));
			}
		}

		boolean uploadPicture(Picture p) {
			try {
				//transferListener.currentFile = p.toString();

				status(su, StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upPrep"));

				MultipartEntity entity = new MultipartEntity();

				Charset utf8 = Charset.forName("UTF-8");
				JSONObject jsonEntity = new JSONObject();
				jsonEntity.put("type", "photo");
				if (p.getName() != null)
					jsonEntity.put("name", p.getName());
				if (p.getTitle() != null)
					jsonEntity.put("title", p.getTitle());
				if (p.getDescription() != null)
					jsonEntity.put("description", p.getDescription());
				entity.addPart("entity", new StringBody(jsonEntity.toJSONString(), utf8));

				ContentBody body = new FileBody(p.getUploadSource());
				entity.addPart("file", body);

				BufferedReader entityReader = sendRequest(p.getParentAlbum().getUrl(), "post", entity);

				String url = ((JSONObject) JSONValue.parse(entityReader)).get("url").toString();
				status(su, StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upSucc"));
				p.setUrl(url);

				Log.log(Log.LEVEL_INFO, "Uploaded " + p.getUploadSource().toString() + " to " + url);

				return true;
				// set auto-rotate only if we do the rotation in GR, otherwise we'd be overriding the server setting
//				if (p.getAngle() != 0) {
//					opts[5] = new NVPair("auto_rotate", "no");
//				}

				// set up extra fields
//				if (p.getExtraFieldsMap() != null && p.getExtraFieldsMap().size() > 0) {
//					ArrayList optsList = new ArrayList(Arrays.asList(opts));
//
//					Iterator it = p.getExtraFieldsMap().keySet().iterator();
//					while (it.hasNext()) {
//						String name = (String) it.next();
//						String value = p.getExtraField(name);
//
//							optsList.add(new NVPair("extrafield." + name, value, utf8?"UTF-8":null));
//						}
//					}
//
//					opts = (NVPair[]) optsList.toArray(opts);
//				}

				// load and validate the response
//				Properties props = requestResponse(hdrs, data, g.getGalleryUrl(scriptName), true, su, this, transferListener);
//				if (props.getProperty("status").equals(GR_STAT_SUCCESS)) {
//					status(su, StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upSucc"));
//					String newItemName = props.getProperty("item_name");
//					if (newItemName != null) {
//						su.doneUploading(newItemName, picture);
//					}
//					return true;
//				} else {
//					Object[] params = {props.getProperty("status_text")};
//					error(su, GRI18n.getString(MODULE, "upErr", params));
//					return false;
//				}
			} catch (GR2Exception gr2e) {
				Log.logException(Log.LEVEL_ERROR, MODULE, gr2e);
				Object[] params = {gr2e.getMessage()};
				error(su, p.toString() + ": " + GRI18n.getString(MODULE, "error", params));
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params = {ioe.toString()};
				error(su, p.toString() + ": " + GRI18n.getString(MODULE, "error", params));
			}

			return false;
		}
	}

	class NewAlbumTask extends GalleryTask {
		Album album;

		NewAlbumTask(StatusUpdate su, Album album) {
			super(su);
			this.album = album;
		}

		void runTask() {
			status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "newAlbm", new Object[] { album.getName(), g.toString() }));

			try {
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				JSONObject jsonEntity = new JSONObject();
				jsonEntity.put("type", "album");
				if (album.getName() != null)
					jsonEntity.put("name", album.getName());
				if (album.getTitle() != null)
					jsonEntity.put("title", album.getTitle());
				if (album.getDescription() != null)
					jsonEntity.put("description", album.getDescription());
				formparams.add(new BasicNameValuePair("entity", jsonEntity.toJSONString()));

				BufferedReader entityReader = sendRequest(album.getParentAlbum().getUrl(), "post", formparams);

				String url = ((JSONObject) JSONValue.parse(entityReader)).get("url").toString();
				status(su, StatusUpdate.LEVEL_GENERIC, GRI18n.getString(MODULE, "crateAlbmOk"));
				album.setUrl(url);

				Log.log(Log.LEVEL_INFO, "Created album " + album.toString());
			} catch (IOException ioe) {
				Log.logException(Log.LEVEL_ERROR, MODULE, ioe);
				Object[] params2 = {ioe.toString()};
				error(su, GRI18n.getString(MODULE, "error", params2));
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

	/*class MyTransferListener implements TransferListener {
		StatusUpdate su;
		java.text.DecimalFormat df = new java.text.DecimalFormat("##,##0");
		java.text.DecimalFormat ff = new java.text.DecimalFormat("##,##0.0");

		String currentFile;
		
		long sizeAllFiles;
		long transferredFilesDone = 0;
		long transferredThisFile = 0;

		int numberAllFiles;
		int numberFilesDone = 0;

		long timeStarted = 0;
		double kbPerSecond = 0;

		MyTransferListener(StatusUpdate su) {
			this.su = su;
		}

		public void dataTransferred(int transferredThisFile, int sizeThisFile, double kbPerSecond) {
			this.transferredThisFile = transferredThisFile;

			Object[] params = {
				df.format(transferredThisFile / 1024),
				df.format(sizeThisFile / 1024),
				ff.format(kbPerSecond / 1024.0)};

			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "trnsfrStat", params));
			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ONE, transferredThisFile);

			params = new Object[] {
					currentFile,
					numberFilesDone + 1,
					numberAllFiles,
					(int) ((transferredFilesDone + transferredThisFile) / 1024 / 1024),
					(int) (sizeAllFiles / 1024 / 1024),
					getProjectedTimeLeft()
			};
			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ALL, GRI18n.getString(MODULE, "upStatus", params));
			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ALL, (int) ((transferredFilesDone + transferredThisFile) * 100 / sizeAllFiles));
		}

		public void transferStart(int sizeThisFile) {
			this.transferredThisFile = sizeThisFile;
			if (timeStarted == 0) {
				timeStarted = System.currentTimeMillis();
			}

			su.updateProgressValue(StatusUpdate.LEVEL_UPLOAD_ONE, 0, sizeThisFile);
		}

		public void transferEnd(int sizeThisFile) {
			transferredFilesDone += sizeThisFile;
			this.transferredThisFile = 0;
			numberFilesDone++;

			su.updateProgressStatus(StatusUpdate.LEVEL_UPLOAD_ONE, GRI18n.getString(MODULE, "upCompSrvrProc"));
			su.setUndetermined(StatusUpdate.LEVEL_UPLOAD_ONE, true);
		}

		public String getProjectedTimeLeft() {
			int secondsLeft = getProjectedSecondsLeft();

			if (secondsLeft == -1) {
				return "";
			} else if (secondsLeft >= 120) {
				Object[] params = { secondsLeft / 60 };
				return GRI18n.getString(MODULE, "minutesLeft", params);
			} else {
				Object[] params = { secondsLeft };
				return GRI18n.getString(MODULE, "secondsLeft", params);
			}
		}

		public int getProjectedSecondsLeft() {
			long timeNow = System.currentTimeMillis();

			if (timeStarted == 0 || timeNow - timeStarted < 500) {
				// just starting, unknown average speed
				return -1;
			}

			// average the current speed and the speed since start
			double denom = (kbPerSecond + ((transferredFilesDone + transferredThisFile) / (timeNow - timeStarted) * 1000)) / 2;

			if (denom == 0) {
				return -1;
			}

			return (int) ((sizeAllFiles - transferredFilesDone - transferredThisFile) / denom);
		}
	}*/
}
