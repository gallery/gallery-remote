export CLASSPATH=.

make_()
{
	# Remove -source 1.3 if you are using a pre-1.4 VM
	javac -source 1.3 com/gallery/GalleryRemote/GalleryRemote.java;
}

make_all()
{
	# Remove -source 1.3 if you are using a pre-1.4 VM
	javac -source 1.3 com/gallery/GalleryRemote/*.java HTTPClient/*.java;
}

make_clean()
{
	rm -rf *.class;
}

make_jar()
{
	make_all;
	jar cvf GalleryRemote.jar com HTTPClient;
}

make_clean_jar()
{
	make_all;
	jar cvf GalleryRemote.jar com/gallery/GalleryRemote/*.class com/gallery/GalleryRemote/model/*.class HTTPClient/*.class;
}

make_zip()
{
	make_clean_jar;
	zip -0 gallery_remote.zip GalleryRemote.jar default.gif ChangeLog defaults.properties run.bat run.sh;
}

make_source_zip()
{
	make_jar;
	zip -0 gallery_remote.zip GalleryRemote.jar default.gif ChangeLog defaults.properties run.bat run.sh;
}

make_cvsbuild()
{
	# For this to work unattended, the cvs must be checked out with the read-only account-less method
	cvs update;
	make_zip;
}

case $1 in
all) make_all;;
clean) make_clean;;
jar) make_jar;;
clean_jar) make_clean_jar;;
zip) make_zip;;
source_zip) make_source_zip;;
cvsbuild) make_cvsbuild;;
*) make_;;
esac