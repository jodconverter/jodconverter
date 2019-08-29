<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>JODConverter Sample Webapp</title>
    <style>

body { font-family: sans-serif; font-size: 13px; }
a { text-decoration: none; color: #900; }
h1 { font-size: 16px; }
h2 { font-size: 14px; }
table.form { margin-left: 15px; }
td.label { text-align: right; right-padding: 10px; }

    </style>
    <script type="text/javascript" src="documentFormats.js"></script>
    <script type="text/javascript">

function updateOutputFormats() {

	var btnConvert = document.getElementById("btnConvert")
	var inputDocument = document.getElementById("inputDocument")

    var dot = inputDocument.value.lastIndexOf('.');
    if (dot != -1) {
        var extension = inputDocument.value.substr(dot + 1);
		var family = importFormatTable[extension];
		if (family == undefined) {
	    	alert('Sorry, but conversion from the document type "'+ extension +'" is not supported');
    		inputDocument.value = "";
			btnConvert.disabled = true;
	    	return false;
		}
		var formats = exportFormatTable[family];
		var options = document.getElementById("outputFormat").options;
		options.length = 0;
		for (var i = 0; i < formats.length; i++) {
	    	var option = formats[i];
	    	if (option.value != extension) {
	        	options[options.length] = option;
	    	}
		}
		btnConvert.disabled = false;
		return true;
    }
	if (inputDocument.value != '') {
		alert('Sorry, but conversion from unknown document type is not supported');
	}
    inputDocument.value = "";
	btnConvert.disabled = true;
    return false;
}

function doSubmit(form) {
	form.action = 'converted/document.'+ form.outputFormat.value;
	return true;
}

    </script>
  </head>
  <body onload="updateOutputFormats()">

      <h1>JODConverter Sample - Web Application</h1>
      <h2>Convert office documents</h2>

      <form method="post" enctype="multipart/form-data" action="converted/document.pdf" onsubmit="return doSubmit(this)">
        <table class="form">
          <tr>
            <td class="label">Document:</td>
            <td>
              <input type="file" id="inputDocument" name="inputDocument" size="40" onchange="updateOutputFormats()"/>
            </td>
          </tr>
          <tr>
            <td class="label">Convert To:</td>
            <td>
              <select id="outputFormat" name="outputFormat" style="width: 38ex;">
                <option value="pdf">Portable Document Format (pdf)</option>
              </select>
              <input id="btnConvert" type="submit" value="Convert!"/>
            </td>
          </tr>
        </table>
      </form>
      
  </body>
</html>
