. make.sh cvsbuild;

version=`grep version= defaults.properties | awk -F= '{print $2}'`;
echo $version;

rm /var/www/html/gr-staging/*
cp gallery_remote.zip /var/www/html/gr-staging/gallery_remote_$version.zip
