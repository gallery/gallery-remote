@echo off

set CLASSPATH=.

goto make_%1%

:make_
javac com/gallery/GalleryRemote/GalleryRemote.java
goto :EOF

:make_all
javac com/gallery/GalleryRemote/*.java HTTPClient/*.java
goto :EOF

:make_clean
del /S *.class
goto :EOF

:make_jar
call :make_all
jar cvf GalleryRemote.jar com HTTPClient
goto :EOF

:make_clean_jar
call :make_all
jar cvf GalleryRemote.jar com/gallery/GalleryRemote/*.class HTTPClient/*.class
goto :EOF

:make_zip
call :make_clean_jar
zip -0 gallery_remote.zip GalleryRemote.jar default.gif
goto :EOF

:make_source_zip
call :make_jar
rem zip -0 gallery_remote.zip GalleryRemote.jar default.gif (for info-zip)
rem pkzip -e0 gallery_remote.zip GalleryRemote.jar default.gif (for pkzip)
zip -0 gallery_remote.zip GalleryRemote.jar default.gif
goto :EOF
