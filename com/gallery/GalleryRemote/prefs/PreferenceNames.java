package com.gallery.GalleryRemote.prefs;

/**
 * Created by IntelliJ IDEA.
 * User: paour
 * Date: Jun 11, 2003
 */
public interface PreferenceNames {
	// General panel
	public static final String SHOW_THUMBNAILS = "showThumbnails";
	public static final String THUMBNAIL_SIZE = "thumbnailSize";
	public static final String SAVE_PASSWORDS = "savePasswords";
	public static final String LOG_LEVEL = "logLevel";
	public static final String UPDATE_CHECK = "updateCheck";
	public static final String UPDATE_CHECK_BETA = "updateCheckBeta";
	public static final String UPDATE_URL = "updateUrl";
	public static final String UPDATE_URL_BETA = "updateUrlBeta";
	public static final String UI_LOCALE = "uiLocale";
	public static final String UI_LOCALE_DEV = "uiLocaleDev";
	public static final String MRU_COUNT = "mruCount";
	public static final String MRU_BASE = "mruItem.";
	public static final String LOAD_LAST_FILE = "loadLastMRU";

	// Upload panel
	public static final String RESIZE_BEFORE_UPLOAD = "resizeBeforeUpload";
	public static final String RESIZE_TO = "resizeTo";
	public static final String RESIZE_TO_DEFAULT = "resizeToDefault";
	public static final String AUTO_CAPTIONS = "autoCaptions";
	public static final int AUTO_CAPTIONS_NONE = 0;
	public static final int AUTO_CAPTIONS_FILENAME = 1;
	public static final int AUTO_CAPTIONS_COMMENT = 2;
	public static final int AUTO_CAPTIONS_DATE = 3;
	//public static final String SET_CAPTIONS_NONE = "setCaptionsNone";
	//public static final String SET_CAPTIONS_WITH_FILENAMES = "setCaptionsWithFilenames";
	//public static final String SET_CAPTIONS_WITH_METADATA_COMMENT = "setCaptionsWithMetadataComment";
	public static final String CAPTION_STRIP_EXTENSION = "captionStripExtension";
	public static final String HTML_ESCAPE_CAPTIONS = "htmlEscapeCaptions";
	public static final String EXIF_AUTOROTATE = "exifAutorotate";

	// URL panel
	public static final String USERNAME = "username.";
	public static final String PASSWORD = "password.";
	public static final String TYPE = "type.";
	public static final String GURL = "url.";
	public static final String STANDALONE = "Standalone";
	public static final String POSTNUKE = "PostNuke";
	public static final String PHPNUKE = "PHPNuke";
	public static final String GEEKLOG = "GeekLog";
	public static final String APPLET = "Applet";
	public static final String PN_GALLERY_URL = "pnGalleryUrl.";
	public static final String PN_LOGIN_URL = "pnLoginUrl.";
	public static final String PHPN_GALLERY_URL = "phpnGalleryUrl.";
	public static final String PHPN_LOGIN_URL = "phpnLoginUrl.";
	public static final String GL_GALLERY_URL = "glGalleryUrl.";
	public static final String GL_LOGIN_URL = "glLoginUrl.";
	public static final String ALIAS = "alias.";
	public static final String FORCE_GALLERY_VERSION = "forceGalleryVersion.";
	public static final String FORCE_PROTOCOL_ENCODING = "forceProtocolEncoding.";
	public static final String RESIZE_JPEG_QUALITY = "resizeJpegQuality.";
	public static final String AUTO_LOAD_ON_STARTUP = "autoLoadOnStartup.";

	// Proxy panel
	public static final String USE_PROXY = "useProxy";
	public static final String PROXY_HOST = "proxyHost";
	public static final String PROXY_PORT = "proxyPort";
	public static final String PROXY_USERNAME = "proxyUsername";
	public static final String PROXY_PASSWORD = "proxyPassword";

	// Slideshow
	public static final String SLIDESHOW_PROGRESS = "slideshowProgressLocation";
	public static final String SLIDESHOW_CAPTION = "slideshowCaptionLocation";
	public static final String SLIDESHOW_EXTRA = "slideshowExtraLocation";
	public static final String SLIDESHOW_URL = "slideshowUrlLocation";
	public static final String SLIDESHOW_ALBUM = "slideshowAlbumLocation";
	public static final String SLIDESHOW_SUMMARY = "slideshowSummaryLocation";
	public static final String SLIDESHOW_DESCRIPTION = "slideshowDescriptionLocation";
	public static final String SLIDESHOW_DELAY = "slideshowDelay";
	public static final String SLIDESHOW_LOWREZ = "slideshowLowRez";
	public static final String SLIDESHOW_RANDOM = "slideshowRandom";
	public static final String SLIDESHOW_MAX_PICTURES = "slideshowMaxPictures";
	public static final String SLIDESHOW_RECURSIVE = "slideshowRecursive";
	public static final String SLIDESHOW_NOSTRETCH = "slideshowNoStretch";
	public static final String SLIDESHOW_COLOR = "slideshowColor";
	public static final String SLIDESHOW_PRELOADALL = "slideshowPreloadAll";
	public static final String SLIDESHOW_LOOP = "slideshowLoop";
	public static final String SLIDESHOW_FONTNAME = "slideshowFontName";
	public static final String SLIDESHOW_FONTSIZE = "slideshowFontSize";
	public static final String SLIDESHOW_FONTTHICKNESS = "slideshowFontThickness";
	public static final String SLIDESHOW_TRANSITION_DURATION = "slideshowTransitionDuration";

	// Other
	public static final String SUPPRESS_WARNING_IM = "suppressWarningIM";
	public static final String SUPPRESS_WARNING_JPEGTRAN = "suppressWarningJpegtran";
	public static final String SUPPRESS_WARNING_JPEGTRAN_CROP = "suppressWarningJpegtranCrop";
	public static final String SUPPRESS_WARNING_CORRUPTED = "suppressWarningCorrupted";
	public static final String SUPPRESS_WARNING_JAVA = "suppressWarningJava";
	public static final String SUPPRESS_WARNING_OUT_OF_MEMORY = "suppressWarningOutOfMemory";
	public static final String USE_JAVA_RESIZE = "useJavaResize";
	public static final String FONT_OVERRIDE_NAME = "fontOverrideName";
	public static final String FONT_OVERRIDE_STYLE = "fontOverrideStyle";
	public static final String FONT_OVERRIDE_SIZE = "fontOverrideSize";
	public static final String PREVIEW_TRANSITION_DURATION = "previewTransitionDuration";
	public static final String ALLOW_UNACCELERATED_TRANSITION = "allowUnacceleratedTransition";
	public static final String PREVIEW_DRAW_THIRDS = "previewDrawThirds";

	// Applet
	public static final String APPLET_SHOW_RESIZE = "appletShowResize";
	public static final String APPLET_DIVIDER_LOCATION = "appletDividerLocation";

	// Sort
	public static final String SORT_TYPE = "sortType";
	public static final int SORT_TYPES = 2;
	public static final int SORT_TYPE_FILENAME = 1;
	public static final int SORT_TYPE_EXIF_CREATION = 2;
}
