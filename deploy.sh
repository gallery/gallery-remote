
echo -e "Updating CVS...\n\n";
cvs -q update -d;

version=`grep version= defaults.properties | awk -F= '{print $2}'`;
echo -e "\n\nVersion: $version\n\n";

#cp gallery_remote.zip /home/httpd/htdocs/gr-staging/gallery_remote_${version}.zip;
#cp gallery_remote_applets.zip /home/httpd/htdocs/gr-staging/gallery_remote_applets_${version}.zip;
#cp GalleryRemote.MacOSX.NoVM.tgz /home/httpd/htdocs/gr-staging/GalleryRemote.${version}.MacOSX.NoVM.tgz;

echo -e "Checking nightly site...\n\n";
if (lynx -dump -head http://jpmullan.com/galleryupdates/remote/gallery_remote_${version}.zip \
	| grep "HTTP/1.1 404") then
	echo -e "\n\nWe have a new version.";

	echo -e "\n\nBuilding...\n\n";
	. ant nightly;

	echo -e "\n\nUploading to the nightly site...\n\n";
	scp gallery_remote.zip paour@jpmullan.com:remote/gallery_remote_${version}.zip;
	ssh paour@jpmullan.com "cp -pf remote/gallery_remote_${version}.zip remote/current_gallery_remote.zip";
	scp gallery_remote_applets.zip paour@jpmullan.com:remote/gallery_remote_applets_${version}.zip;
	ssh paour@jpmullan.com "cp -pf remote/gallery_remote_applets_${version}.zip remote/current_gallery_remote_applets.zip";
	scp GalleryRemote.MacOSX.NoVM.tgz paour@jpmullan.com:remote/GalleryRemote.${version}.MacOSX.NoVM.tgz;
	ssh paour@jpmullan.com "cp -pf remote/GalleryRemote.${version}.MacOSX.NoVM.tgz remote/current_GalleryRemote.MacOSX.NoVM.tgz";
	
	echo -e "\n\nPosting new version info on version check site...\n\n";
	. ant post;
	
	if ( [ -f deploy_local.sh ] ) then
		echo -e "\n\nRunning local script...\n\n";
		deploy_local.sh;
	fi
fi
