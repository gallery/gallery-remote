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
		boolean active = changeProps.getBooleanProperty("active");
		if (!active) {
			System.out.println("Not active");
			return;
		}

		// find out what the current and last builds are
		int siteBetaBuild = changeProps.getIntProperty("siteBetaBuild");
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

			// compose beta check
			StringBuffer betaCheck = new StringBuffer();

			betaCheck.append("version=").append(currentBuildS).append("\n");
			betaCheck.append("releaseDate=").append(defaultProps.getProperty("releaseDate")).append("\n");
			betaCheck.append("releaseUrl=http://jpmullan.com/galleryupdates/remote/gallery_remote_").append(defaultProps.getProperty("version")).append(".zip\n");
			betaCheck.append("releaseNotes=").append(changes);

			// update Menalto
			System.out.println("Uploading to Menalto: " + betaCheck);

			NVPair form_data[] = {
				new NVPair("op", "modload"),
				new NVPair("name", "GalleryRemoteVersion"),
				new NVPair("file", "index"),
				new NVPair("action", "save-beta"),
				new NVPair("newVersion", betaCheck.toString()),
			};

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
			rsp = mConnection.Get("/user.php?uname=" + changeProps.getProperty("username") +
					"&pass=" + changeProps.getProperty("password") + "&module=NS-User&op=login");
			response = new String(rsp.getData()).trim();
			System.out.println("Login response: " + response);

			// upload
			rsp = mConnection.Post("/modules.php", form_data);
			response = new String(rsp.getData()).trim();
			System.out.println("Upload response: " + response);

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
