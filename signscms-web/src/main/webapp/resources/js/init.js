/**
 * Created by Oscar on 6/05/2017.
 */

function init() {

//    executeInit();
}

function executeInit() {
    console.log("executeInit");

    var elem = document.getElementById("clear");
    elem.innerHTML="Clear";

	elem = document.getElementById("load");
	elem.innerHTML="Load";

	elem = document.getElementById("showXML");
	elem.innerHTML="ShowXML";
}

function executeClear() {
	var url = "/Init?format=json&action=clear";
	$.ajax({
		//type : "POST",
		contentType: "application/json",
		url: url,
		//data : JSON.stringify(search),
		dataType: 'json',
		timeout: 100000,
		success: function (data) {
			var elem = document.getElementById("clear");
			if (data == "Success") {
				elem.innerHTML="<div class=success>Database has been cleared</div>";
			} else if (data == "Error") {
				elem.innerHTML = "<div class=error>Database FAILED to clear</div>";
			} else {
				elem.innerHTML = "<div class=warning>Unknown response</div>";
			}
		},
		error: function (e) {
			var elem = document.getElementById("clear");
			elem.innerHTML="<div class=error>ERROR whilst executing Clear command: " + e.statusText + "</div>";
		},
		done: function (e) {
			var elem = document.getElementById("clear");
			elem.innerHTML="<div class=warning>NOT sure what DONE means: " + e.statusText + "</div>";
		}
	});
}

function executeLoad() {
	var url = "/Init?format=json&action=load";
	$.ajax({
		//type : "POST",
		contentType: "application/json",
		url: url,
		//data : JSON.stringify(search),
		dataType: 'json',
		timeout: 100000,
		success: function (data) {
			var elem = document.getElementById("load");
			if (data == "Success") {
				elem.innerHTML = "<div class=success>XML config file has been loaded</div>";
			} else if (data == "Error") {
				elem.innerHTML = "<div class=error>XML config file FAILED to load</div>";
			} else {
				elem.innerHTML = "<div class=warning>Unknown response</div>";
			}
		},
		error: function (e) {
			var elem = document.getElementById("load");
			elem.innerHTML="<div class=error>ERROR whilst executing Load command: " + e.statusText + "</div>";
		},
		done: function (e) {
			var elem = document.getElementById("load");
			elem.innerHTML="<div class=warning>DONE: NOT sure what DONE means: " + e.statusText + "</div>";
		}
	});
}

function executeShowXML() {
	var url = "/Init?format=json&action=showXML";
	$.ajax({
		//type : "POST",
		contentType: "application/json",
		url: url,
		//data : JSON.stringify(search),
		dataType: 'json',
		timeout: 100000,
		success: function (data) {
			var elem = document.getElementById("showXML");
			if (data == "Error") {
				elem.innerHTML = "<div class=error>Failed to read XML config file</div>";
			} else {
				elem.innerHTML = "<div id=xml class=xmltext></div>";

				var elem2 = document.getElementById("xml");
				elem2.innerText = data;
			}
		},
		error: function (e) {
			var elem = document.getElementById("showXML");
			elem.innerHTML="<div class=error>ERROR whilst executing ShowXML command: " + e.statusText + "</div>";
		},
		done: function (e) {
			var elem = document.getElementById("showXML");
			elem.innerHTML="<div class=warning>DONE: NOT sure what DONE means: " + e.statusText + "</div>";
		}
	});

}