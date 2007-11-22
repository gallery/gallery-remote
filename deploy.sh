# gallery_docs is no longer maintained, don't bother updating it...
#echo -e "Updating docs SVN...\n\n";
#pushd ../gallery_docs; svn update; popd;

set ANT=/usr/local/bin/ant;

echo -e "Updating SVN...\n\n";
svn update;

version=`grep version= defaults.properties | awk -F= '{print $2}'`;
echo -e "\n\nVersion: $version\n\n";

echo -e "Checking nightly site...\n\n";
if (lynx -dump -head http://www.gallery2.hu/download/GalleryRemote/gallery_remote_${version}.zip \
	| grep "HTTP/1.1 404") then
	echo -e "\n\nWe have a new version.";

	echo -e "\n\nBuilding...\n\n";
	ant nightly;

	echo -e "\n\nUploading to the nightly site...\n\n";
	ncftpput -f gallery2hu.cfg -C gallery_remote.zip gallery_remote_${version}.zip;
	ncftpput -f gallery2hu.cfg -C gallery_remote.zip current_gallery_remote.zip;
	ncftpput -f gallery2hu.cfg -C gallery_remote_applets.zip gallery_remote_applets_${version}.zip;
	ncftpput -f gallery2hu.cfg -C gallery_remote_applets.zip current_gallery_remote_applets.zip;
	ncftpput -f gallery2hu.cfg -C GalleryRemote.MacOSX.NoVM.tgz GalleryRemote.${version}.MacOSX.NoVM.tgz;
	ncftpput -f gallery2hu.cfg -C GalleryRemote.MacOSX.NoVM.tgz current_GalleryRemote.MacOSX.NoVM.tgz;
	
	echo -e "\n\nPosting new version info on version check site...\n\n";
	ant post;
	
	if ( [ -f deploy_local.sh ] ) then
		echo -e "\n\nRunning local script...\n\n";
		. deploy_local.sh;
	fi
fi
