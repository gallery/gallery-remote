@echo off

set CLASSPATH=.

goto make_%1%

:make_
rem Remove -source 1.3 if you are using a pre-1.4 VM
javac -source 1.3 com/gallery/GalleryRemote/GalleryRemote.java
goto :EOF

:make_all
rem Remove -source 1.3 if you are using a pre-1.4 VM
javac -source 1.3 com/gallery/GalleryRemote/*.java HTTPClient/*.java
goto :EOF

:make_clean
del /S *.class
goto :EOF

:make_jar
call :make_all
jar cvf GalleryRemote.jar com HTTPClient remoteAbout.gif
goto :EOF

:make_clean_jar
call :make_all
jar cvf GalleryRemote.jar com/gallery/GalleryRemote/*.class com/gallery/GalleryRemote/model/*.class HTTPClient/*.class remoteAbout.gif
goto :EOF

:make_zip
call :make_clean_jar
rem Info-ZIP zip.exe needs to be installed somewhere in the path
zip -0 gallery_remote.zip GalleryRemote.jar default.gif ChangeLog defaults.properties run.bat run.sh
goto :EOF

:make_source_zip
call :make_jar
rem Info-ZIP zip.exe needs to be installed somewhere in the path
zip -0 gallery_remote.zip GalleryRemote.jar default.gif ChangeLog defaults.properties run.bat run.sh
goto :EOF

:make_cvsbuild
rem For this to work unattended, the cvs must be checked out with the read-only account-less method
cvs update
call :make_zip
goto :EOF