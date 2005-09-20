package com.gallery.GalleryRemote.util;

import HTTPClient.*;
import com.gallery.GalleryRemote.prefs.PropertiesFile;
import org.apache.tools.ant.BuildException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Oct 8, 2003
 */
public class PostChangeLog extends org.apache.tools.ant.Task {
	public void execute() throws BuildException {
		PropertiesFile changeProps = new PropertiesFile("postchangelog");
		PropertiesFile defaultProps = new PropertiesFile("defaults");

		// should we even be doing this?
		boolean active = changeProps.getBooleanProperty("active", false);
		if (!active) {
			System.out.println("Not active");
			return;
		}

		// find out what the current and last builds are
		int siteBetaBuild = changeProps.getIntProperty("siteBetaBuild", 0);
		String currentBuildS = defaultProps.getProperty("version");

		Pattern buildPattern = Pattern.compile(".*-b(\\d*)");
		Matcher m = buildPattern.matcher(currentBuildS);
		if (!m.matches()) {
			System.out.println("Not a beta build");
			return;
		}

		int currentBetaBuild = Integer.parseInt(m.group(1));
		if (currentBetaBuild == siteBetaBuild) {
			System.out.println("Current and site beta builds are the same: nothing to do");
			return;
		}

		// parse the ChangeLog
		try {
			Pattern header = Pattern.compile("([0-9\\-])\\s*(.*)\\s*<(.*)> \\((.*)\\)");
			String date = null;
			String author = null;
			String email = null;
			String version = null;
			StringBuffer changes = new StringBuffer();

			changes.append("Changes between b").append(siteBetaBuild).append(" and b").append(currentBetaBuild).append(": ");

			File changeLog = new File("ChangeLog");
			BufferedReader in = new BufferedReader(new FileReader(changeLog));

			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println("Got line: " + line);
				m = header.matcher(line);

				if (m.matches()) {
					// got a header
					version = m.group(4);
					System.out.println("Got a header. Version: " + version);

					m = buildPattern.matcher(version);
					if (m.matches()) {
						int myBetaBuild = Integer.parseInt(m.group(1));
						System.out.println("Beta version: " + myBetaBuild);

						if (myBetaBuild == siteBetaBuild) {
							System.out.println("Found the right beta: stopping");
							break;
						}
					} else {
						System.out.println("Found a non-beta... guess it stops here");
						break;
					}
				} else {
					// got just a normal line
					line = line.trim();

					if (line.length() > 0) {
						if (line.startsWith("*")) {
							changes.append("\\n").append(line).append(" ");
						} else {
							changes.append(line.trim()).append(" ");
						}
					}
				}
			}

			System.out.println("Got changes: " + changes);

			NVPair form_data_login[] = {
					new NVPair("edit[name]", changeProps.getProperty("username")),
					new NVPair("edit[pass]", changeProps.getProperty("password")),
					new NVPair("op", "Log in"),
			};

			NVPair form_data[] = {
				new NVPair("submit", "Update"),
				new NVPair("edit[releaseNum]", currentBuildS),
				new NVPair("edit[go]", "1"),
				new NVPair("edit[releaseDate]", defaultProps.getProperty("releaseDate")),
				new NVPair("edit[releaseUrl]", "http://jpmullan.com/galleryupdates/remote/gallery_remote_" + defaultProps.getProperty("version") + ".zip"),
				new NVPair("edit[releaseUrlMac]", "http://jpmullan.com/galleryupdates/remote/GalleryRemote." + defaultProps.getProperty("version") + ".MacOSX.NoVM.tgz"),
				new NVPair("edit[releaseNotes]", changes.toString()),
			};

			// update Menalto
			System.out.println("Uploading to Menalto: " + form_data);

			// set cookie handling
			CookieModule.setCookiePolicyHandler(new CookiePolicyHandler() {
				public boolean acceptCookie(Cookie cookie, RoRequest req, RoResponse resp) {
					System.out.println("Accepting cookie: " + cookie);
					return true;
				}

				public boolean sendCookie(Cookie cookie, RoRequest req) {
					System.out.println("Sending cookie: " + cookie);
					return true;
				}
			});

			HTTPConnection mConnection = new HTTPConnection("gallery.menalto.com");
			HTTPResponse rsp = null;
			String response = null;

			// login
			rsp = mConnection.Post("/user/login?destination=node", form_data_login);
			response = new String(rsp.getData()).trim();
			System.out.println("Login response: " + response);

			// upload
			rsp = mConnection.Post("/admin/galleryremote/edit/beta/1", form_data);
			response = new String(rsp.getData()).trim();
			System.out.println("Upload response: " + response);

			// activate
			rsp = mConnection.Get("/admin/galleryremote/current/beta/1");
			response = new String(rsp.getData()).trim();
			System.out.println("Activate response: " + response);

			// test
			HTTPConnection mConnection1 = new HTTPConnection("gallery.sourceforge.net");
			rsp = mConnection1.Get("/gallery_remote_version_check.php");
			response = new String(rsp.getData()).trim();
			System.out.println("Test response: " + response);

			if (response.startsWith("version=" + currentBuildS)) {
				// worked
				System.out.println("Success: writing to postlogchange properties");
				changeProps.setIntProperty("siteBetaBuild", currentBetaBuild);
				changeProps.write();
			} else {
				System.out.println("Failed to update Menalto");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ModuleException e) {
			e.printStackTrace();
		}
	}
}
