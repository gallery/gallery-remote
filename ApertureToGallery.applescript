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