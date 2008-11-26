on run argv
	set destination to "/tmp/"
	set outputFile to "ApertureToGallery.txt"
	set decision to ""
	
	try
		repeat
			do shell script "/bin/ls " & destination & outputFile
			tell application "System Events"
				activate
				set decision to display dialog "The file used to communicate between Aperture and Gallery Remote is already present. This may mean Gallery Remote has not yet finished processing Aperture's output." buttons {"Continue anyway", "Retry", "Cancel"} default button 3 with icon caution
			end tell
			log decision
			set decision to button returned of decision
			if decision is not "Retry" then
				exit repeat
			end if
		end repeat
	on error errStr number errorNumber
		if errorNumber is -128 then
			set decision to "Cancel"
			return "user cancelled"
		end if
	end try
	
	tell application "Aperture"
		set myselected to the selection
		set captions to {}
		repeat with imageversion in myselected
			if exists the value of the IPTC tag named "Caption/Abstract" of imageversion then
				set myCaption to the value of the IPTC tag named "Caption/Abstract" of imageversion
				copy myCaption to the end of captions
			else
				copy "" to the end of captions
			end if
		end repeat
		export myselected using export setting "JPEG - Original size" to destination
		set exportedFiles to the result
		
	end tell
	
	set fileRef to open for access (POSIX file (destination & outputFile)) with write permission
	set eof fileRef to 0
	try
		repeat with i from 1 to count exportedFiles
			set tmpFile to POSIX path of item i of exportedFiles
			write tmpFile to fileRef
			write "\t" & item i of captions to fileRef
			write "\r" to fileRef
		end repeat
	end try
	close access fileRef
	
	return "done"
end run

(*script gallery
	property myGalleryUrl : "http://www.paour.com/gallery2/"
	property myGalleryUser : "paour"
	property myGalleryPass : "charsome"
	property authToken : 0
	
	to login()
		set myCommand to "curl -c /tmp/curlCookies -b /tmp/curlCookies -g \"" & myGalleryUrl & "main.php?g2_controller=remote:GalleryRemote&g2_form[cmd]=login&g2_form[protocol_version]=2.0&g2_form[uname]=" & myGalleryUser & "&g2_form[password]=" & myGalleryPass & "\""
		log myCommand
		
		do shell script myCommand
		set loginResult to the result
		
		set AppleScript's text item delimiters to "="
		repeat with p in paragraphs of loginResult
			if word 1 of p = "status" then
				set loginStatus to word 3 of p
			else if word 1 of p = "auth_token" then
				set authToken to word 3 of p
			end if
		end repeat
		
		if loginStatus ­ "0" then
			display alert "Login failed"
		end if
		
		return loginStatus
	end login
	
	to upload of galleryPicturePath into galleryAlbum
		log galleryPicturePath
		log authToken
	end upload
	
	to listAlbums()
		set myCommand to "curl -c /tmp/curlCookies -b /tmp/curlCookies -g \"" & myGalleryUrl & "main.php?g2_controller=remote:GalleryRemote&g2_form[cmd]=fetch-albums-prune&g2_form[protocol_version]=2.0&g2_form[no_perms]=yes&g2_form[auth_token]=" & authToken & "\""
		log myCommand
		
		do shell script myCommand
		set listAlbumResult to the result
	end listAlbums
end script*)