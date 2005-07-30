/*
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Mar 17, 2004
 * Time: 4:20:46 PM
 */
package com.gallery.GalleryRemote;

import com.gallery.GalleryRemote.model.Gallery;
import com.gallery.GalleryRemote.prefs.GalleryProperties;
import HTTPClient.NVPair;
import HTTPClient.ModuleException;

import java.net.URL;
import java.io.IOException;
import java.util.Arrays;

public class GalleryComm2_5 extends GalleryComm2 {
	private static final String MODULE = "GalComm2_5";

	/** Remote scriptname that provides version 2 of the protocol on the server. */
	public static final String SCRIPT_NAME = "main.php?g2_controller=remote:GalleryRemote&g2_form[cmd]=no-op";

	public static final boolean ZEND_DEBUG = false;

	private static int[] capabilities2;
	private static int[] capabilities3;
	private static int[] capabilities4;
	private static int[] capabilities6;

	protected GalleryComm2_5(Gallery g) {
		super(g);

		scriptName = "main.php";

		capabilities2 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL/*, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM*/, CAPA_FETCH_ALBUMS_PRUNE};
		capabilities3 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL/*, CAPA_ALBUM_INFO*/, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE};
		capabilities4 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL/*, CAPA_ALBUM_INFO*/, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FETCH_ALBUM_IMAGES};
		capabilities6 = new int[]{CAPA_UPLOAD_FILES, CAPA_FETCH_ALBUMS, CAPA_UPLOAD_CAPTION,
								  CAPA_FETCH_HIERARCHICAL, CAPA_ALBUM_INFO, CAPA_NEW_ALBUM, CAPA_FETCH_ALBUMS_PRUNE,
								  CAPA_FETCH_ALBUM_IMAGES};

		Arrays.sort(capabilities2);
		Arrays.sort(capabilities3);
		Arrays.sort(capabilities4);
		Arrays.sort(capabilities6);

		g.setGalleryVersion(2);
	}

    public NVPair[] fudgeParameters(NVPair[] data) {
        NVPair[] data_modified = new NVPair[data.length];
        for (int i = 0; i < data.length; i++) {
            NVPair nvPair = data[i];
            data_modified[i] = new NVPair("g2_" + nvPair.getName(), nvPair.getValue());
        }

        return data_modified;
    }

	public NVPair[] fudgeFormParameters(NVPair form_data[]) {
		NVPair[] form_data_modified;
		if (ZEND_DEBUG) {
			form_data_modified = new NVPair[form_data.length + 6];
		} else {
			form_data_modified = new NVPair[form_data.length + 1];
		}

		for (int i = 0; i < form_data.length; i++) {
			if (form_data[i] != null) {
				form_data_modified[i] = new NVPair("g2_form[" + form_data[i].getName() + "]", form_data[i].getValue());
			} else {
				form_data_modified[i] = null;
			}
		}

		form_data_modified[form_data.length] = new NVPair("g2_controller", "remote:GalleryRemote");

		if (ZEND_DEBUG) {
			form_data_modified[form_data.length + 1] = new NVPair("start_debug", "1");
			form_data_modified[form_data.length + 2] = new NVPair("debug_port", "10000");
			form_data_modified[form_data.length + 3] = new NVPair("debug_host", "172.16.1.35,127.0.0.1");
			form_data_modified[form_data.length + 4] = new NVPair("send_sess_end", "1");
			//form_data_modified[form_data.length + 5] = new NVPair("debug_no_cache", "1077182887875");
			//form_data_modified[form_data.length + 6] = new NVPair("debug_stop", "1");
			form_data_modified[form_data.length + 5] = new NVPair("debug_url", "1");
		}

		Log.log(Log.LEVEL_TRACE, MODULE, "Overriding form data: " + Arrays.asList(form_data_modified));

		return form_data_modified;
	}

	void handleCapabilities() {
		if (serverMinorVersion >= 6) {
			capabilities = capabilities6;
		} else if (serverMinorVersion >= 4) {
			capabilities = capabilities4;
		} else if (serverMinorVersion >= 3) {
			capabilities = capabilities3;
		} else {
			capabilities = capabilities2;
		}
	}

}
