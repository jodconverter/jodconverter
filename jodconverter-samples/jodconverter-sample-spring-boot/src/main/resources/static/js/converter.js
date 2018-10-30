function dismissError() {
	
	$('#errorMessage').empty();
}

function showError(errorMessage) {

	if (errorMessage) {
		var $alert = $('<div>', {'class': 'alert alert-danger alert-dismissable fade in'});
		$alert.append('<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>')
		$alert.append('<strong>Error!</strong>&nbsp;&nbsp;');
		$alert.append('<span>' + errorMessage +'</span>');
		
		// Replace current HTML of the error message div
		$('#errorMessage').html($alert);
	} else {
		// Remove older message from the error message div
		dismissError();
	}
}

function disableElement(eltId) {

	var $elt = $('#' + eltId);
	if(!$elt.attr('disabled')) {
		$elt.attr('disabled', 'disabled');
	}
}

function onInputFileChange() {
	
	// Remove any previous error message
	dismissError();
	
	// Obtain a jQuery object that represents the input file
	var $inputFile = $('#inputFile');
	
	// Extract the filename from the jQuery object
	var filename = $inputFile.val().split('\\').pop();

	// Update the read-only input field showing the selected file
	$('#inputFileText').val(filename);

	// Search for an extension in the filename
	// See https://stackoverflow.com/a/680982/4336562
	var re = /(?:\.([^.]+))?$/;
	var ext = re.exec(filename)[1];
	if (ext == undefined) {
		disableElement('outputFormat');
		disableElement('goButton');
		showError('No extension found in the source file name.');
		return false;
	}
	
	// Retrieve the input family for this extension
	var family = importFormatTable[ext.toLowerCase()];
	
	// Input format not supported ? Inform the user.
	if (family == undefined) {
		disableElement('outputFormat');
		disableElement('goButton');
		showError('Conversion from extension <b>' + ext + '</b> is not supported.');
		
		return false;
	}
	
	// Get the supported output formats for the input format family
	var formats = exportFormatTable[family];
	
	// Populate the drop down list of supported output formats
	var $outputFormat = $("#outputFormat");
	$.each(formats, function() {
		if (this.value != ext) {
			$outputFormat.append(this);
		}
	});
	
	$("#outputFormat").removeAttr('disabled');
	$("#goButton").removeAttr('disabled');
}