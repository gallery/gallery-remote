cvs -q update -d;

. ant zip;

version=`grep version= defaults.properties | awk -F= '{print $2}'`;
echo $version;

rm /home/httpd/htdocs/gr-staging/*.zip
cp gallery_remote.zip /home/httpd/htdocs/gr-staging/gallery_remote_$version.zip

lynx -dump -head http://jpmullan.com/galleryupdates/remote/gallery_remote_$version.zip | grep "HTTP/1.1 404" && scp gallery_remote.zip paour@jpmullan.com:remote/gallery_remote_$version.zip && . ant post