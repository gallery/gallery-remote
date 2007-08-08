package com.gallery.GalleryRemote;

import junit.framework.TestCase;

/**
 * @author brianegge
 */
public class NewAlbumDialogUnitTest extends TestCase {
    public void testCharacterReplace() {
        assertEquals("a_new_and_exciting_album", NewAlbumDialog.getDefaultName("A new & exciting album"));
        assertEquals("fun_times", NewAlbumDialog.getDefaultName("Fun + times"));
    }
}
