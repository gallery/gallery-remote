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
	public static final String baseUrl = "http://www.gallery2.hu/download/GalleryRemote/";

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
			//String date = null;
			//String author = null;
			//String email = null;
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

			StringBuffer note = new StringBuffer();
			note.append("version=").append(currentBuildS).append('\n');
			note.append("releaseDate=").append(defaultProps.getProperty("releaseDate")).append('\n');
			note.append("releaseUrl=").append(baseUrl).append("gallery_remote_")
				.append(defaultProps.getProperty("version")).append(".zip").append('\n');
			note.append("releaseUrlMac=").append(baseUrl).append("GalleryRemote.")
				.append(defaultProps.getProperty("version")).append(".MacOSX.NoVM.tgz").append('\n');
			note.append("releaseNotes=").append(changes.toString());

			System.out.println("Got changes: " + changes);

			NVPair form_data_login[] = {
				new NVPair("name", changeProps.getProperty("username")),
				new NVPair("pass", changeProps.getProperty("password")),
				new NVPair("form_id", "user_login"),
				new NVPair("op", "Log in")
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
			
			// get login
                        rsp = mConnection.Get("/user");
			response = new String(rsp.getData()).trim();
			System.out.println("Get login response: " + response);

			System.out.println(); System.out.println();
			System.out.println("*****************************");

			// login
			rsp = mConnection.Post("/user", form_data_login);
			response = new String(rsp.getData()).trim();
			System.out.println("Login response: " + response);

			System.out.println(); System.out.println();
                        System.out.println("*****************************");
			
			// get form
			rsp = mConnection.Get("/admin/gmc_versioncheck/edit/5");
			response = new String(rsp.getData()).trim();
			System.out.println("Get form response: " + response);
			
			System.out.println(); System.out.println();
			System.out.println("*****************************");
			
			Pattern p = Pattern.compile(".*<input type=\"hidden\" name=\"form_token\" id=\"edit-gmc-versioncheck-form-form-token\" value=\"([0123456789abcdef]*)\"\\s*/>.*", Pattern.DOTALL);
			m = p.matcher(response);
			
			if (m.matches()) {
				String formToken = m.group(1);
				System.out.println("Form token: " + formToken);
				
				NVPair form_data_submit[] = {
					new NVPair("url", "galleryremote/beta"),
					new NVPair("note", note.toString()),
					new NVPair("id", "5"),
					new NVPair("form_token", formToken),
					new NVPair("form_id", "gmc_versioncheck_form"),
					new NVPair("op", "Update")
				};

				// login
				rsp = mConnection.Post("/admin/gmc_versioncheck/edit/5", form_data_submit);
				response = new String(rsp.getData()).trim();
				System.out.println("Submit response: " + response);

				// activate
				//rsp = mConnection.Get("/admin/galleryremote/current/beta/1");
				//response = new String(rsp.getData()).trim();
				//System.out.println("Activate response: " + response);
	
				// test
				HTTPConnection mConnection1 = new HTTPConnection("gallery.sourceforge.net");
				rsp = mConnection1.Get("/gallery_remote_version_check_beta.php");
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
			} else {
				System.out.println("Could not find the form_token");
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
