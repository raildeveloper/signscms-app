<%--
  TripInfoServlet page
  User: rhanson
  Date: 11/07/13
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<html>
<head>
<title>Sydney Trains real-time network status</title>

<!-- use CDNs for resources -->
<script
	src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script type="text/javascript"
	src="//maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
<script type="text/javascript"
	src="//google-maps-utility-library-v3.googlecode.com/svn/tags/markerwithlabel/1.1.9/src/markerwithlabel_packed.js"></script>
</head>
<body>


	<style type="text/css">
html,body {
	height: 100%;
	margin: 0;
}

body {
	font-family: Helvetica, sans-serif;
	font-size: 90%;
}

td,th {
	font-size: 80%;
	color: #333;
}

a,a:visited {
	color: #555;
}

.info-panel {
	float: right;
	width: 38%;
	margin: 1%;
}

#map-container {
	position: fixed;
	width: 60%;
	height: 100%;
}

#map-canvas {
	width: 100%;
	height: 100%;
}

#train-list {
	text-align: justify;
}

table.stops {
	width: 100%;
	border-collapse: collapse;
}

table.stops td,table.stops th {
	padding: 2px 4px 2px 0;
}

dl>dt {
	float: left;
	clear: left;
	width: 100px;
	color: #888;
}

.map-trip-marker {
	background-color: white;
	font-family: Helvetica, sans-serif;
	font-size: 8pt;
	font-weight: bold;
	text-align: center;
	border: 1px solid rgba(0, 0, 0, 0.5);
	padding: 0 3px;
	border-radius: 4px;
	white-space: nowrap;
}

.delayed {
	color: #e00;
	font-weight: bold;
}

.early {
	color: #00a;
}

.late {
	color: #d00;
}

.scheduled {
	color: #888;
}

td.passed {
	color: #888;
}

td.on-time {
	color: #080;
}

td.time {
	text-align: right;
}

.current-stop,.next-stop {
	background: #eee;
}
</style>



	<div id="network-info" class="info-panel">
		<h3>Sydney Trains real-time status</h3>
		<p>Currently operating trains
		<p>
		<p id="train-list">Contacting server...</p>
	</div>
	<div id="trip-info" class="info-panel" style="display: none;">
		<div style="float: right;">
			<a href="#" class="btn back">&lt; Back</a>
		</div>
		<h4 id="trip-header"></h4>
		<div id="trip-body"></div>
	</div>
	<div id="map-container">
		<div id="map-canvas"></div>
	</div>
	<script type="text/javascript">
		// initialise google map pane
		var mapOptions = {
			zoom : 11,
			center : new google.maps.LatLng(-33.828787, 151.075058),
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		var map = new google.maps.Map(document.getElementById('map-canvas'),
				mapOptions);

		// Do't use the transit layer on Mobile Safari to keep RAM use down
		var iOS = (navigator.userAgent.match(/(iPad|iPhone|iPod)/g) ? true
				: false);
		
		if (!iOS) {
			var transitLayer = new google.maps.TransitLayer();
			transitLayer.setMap(map);
		}

		// given a Trip JSON object, return the run number
		function runNumberForTrip(trip) {
			var terms = trip.tripId.split(/\./);
			return terms[0];
		}

		// HTML run number with delay highlighted in 3min increments as per TLS
		function colourRunNumberByDelay(trip) {
			var run = runNumberForTrip(trip);
			var chars = Math.floor(trip.delay / 180);
			return (chars > 0) ? '<span class="delayed">'
					+ run.substring(0, chars) + '</span>'
					+ run.substring(chars) : run;
		}

		// generate a set of HTML links for each trip
		function linksForTrips(trips) {
			if (trips.length == 0)
				return "No vehicles are currently operating";

			// sort trips by run number
			trips.sort(function(a, b) {
				return b.tripId < a.tripId ? 1 : -1;
			});

			// generate html
			var links = [];
			for ( var idx in trips) {
				var trip = trips[idx];
				links.push('<a href="#' + trip.tripId + '" data-trip-id="'
						+ trip.tripId + '"'
						+ (trip.label ? '' : ' class="scheduled"') + '>'
						+ colourRunNumberByDelay(trip) + '</a>');
			}
			return links.join(' ');
		}

		// update/track vehicle layer on map
		var markers = {};
		function updateMapMarkers(trips) {
			var tripIds = [];

			// add new vehicle markers to map
			for ( var idx in trips) {
				var trip = trips[idx];

				// build set of tripIds to compare current markers against below
				tripIds.push(trip.tripId);

				// recycle existing marker if possible
				var marker = markers[trip.tripId];
				if (marker) {
					marker.setPosition(new google.maps.LatLng(trip.lat,
							trip.lon));
					marker.set('labelContent', colourRunNumberByDelay(trip));
				} else if (trip.lat && trip.lon) {
					marker = new MarkerWithLabel(
							{
								position : new google.maps.LatLng(trip.lat,
										trip.lon),
								labelContent : colourRunNumberByDelay(trip),
								labelClass : 'map-trip-marker',
								labelAnchor : new google.maps.Point(15, 8),
								title : trip.tripId,
								icon : 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7">'
							});
					marker.setMap(map);

					// Add click handler to vehicle marker in a closure to persist tripId
					(function() {
						var tripId = trip.tripId;
						google.maps.event.addListener(marker, 'click',
								function(event) {
									displayTripId(tripId);
								});
					})();
					markers[trip.tripId] = marker;
				}
			}

			// remove abandoned vehicle markers from map/tracking array
			var filtered = {};
			for ( var tripId in markers) {
				if (tripIds.indexOf(tripId) < 0) {
					var marker = markers[tripId];
					marker.set('labelVisible', false);
					marker.setMap(null);
					google.maps.event.clearListeners(marker, 'click');
				} else
					filtered[tripId] = markers[tripId];
			}
			markers = filtered;
		}

		// retrieve current trip data now and every 10sec
		function update() {

			$.ajax({
				url : 'TripInfo?format=json',
				success : function(data) {
					$('#train-list').html(linksForTrips(data));
					updateMapMarkers(data);
				},
				error : function(xhr, text, err) {
					$('#train-list').html('Error: ' + text);
				}
			});

		}
		update();
		window.setInterval(update, 10000);

		/**** TRIP INFO PANEL DRAWING ****/

		// Invoke to display a trip in the sidebar, AJAX req will be made for JSON payload
		var tripUpdateTimer = null;
		function displayTripId(tripId) {
			// Initialise view
			$('#network-info').hide();
			$('#trip-info').show();
			$('#trip-header').html('');
			$('#trip-body').html('<p>Loading...</p>');

			function updateTrip() {
				$.ajax({
					url : 'TripInfo?tripId=' + tripId + '&format=json',
					success : function(data) {
						displayTripInfo(data);
					},
					error : function(xhr, text, err) {
						$('#trip-body').html('Error: ' + err);
					}
				});
			}
			updateTrip();
			clearInterval(tripUpdateTimer);
			tripUpdateTimer = window.setInterval(updateTrip, 10000);
		}

		// Convert a string of format HH:mm:ss to seconds after midnight
		function timestampToSeconds(timestamp) {
			if (timestamp.length != 8)
				return -1;
			return (parseInt(timestamp.substring(0, 2), 10) * 60 + parseInt(
					timestamp.substring(3, 5), 10))
					* 60 + parseInt(timestamp.substring(6), 10);
		}

		// Return string representing the delay at a stop (type = arr|dep)
		function delayForStop(stop, type) {
			var scheduled = stop.scheduled[type];
			var hasActual = (stop.actual && stop.actual[type]);
			var actual = hasActual ? stop.actual[type]
					: (stop.predicted ? stop.predicted[type] : null);
			var delay = timestampToSeconds(actual)
					- timestampToSeconds(scheduled);

			// adjust times that cross midnight boundary
			if (delay < 3600 * -18)
				delay += 86400;
			if (delay > 3600 * 18)
				delay -= 86400;

			if (!actual)
				return '';
			if (delay < -59)
				return '<span class="early">-' + Math.round(delay / -60)
						+ 'm</span>';
			else if (delay > 59)
				return '<span class="late">+' + Math.round(delay / 60)
						+ 'm</span>';
			else
				return '<span class="on-time"></span>';
		}

		// Render a JSON payload describing a trip to HTML and display in sidebar
		function displayTripInfo(trip) {

			// Column headings
			var info = [ '<dt>Trip ID</dt><dd>' + trip.tripId + '</dd>' ], stops = [
					'<tr><th></th><th colspan="3">Arrival</th><th colspan="3">Departure</th></tr>',
					'<tr><th>Stop</th><th class="time">Sched</th><th class="time">Actual</th><th></th>'
							+ '<th class="time">Sched</th><th class="time">Actual</th><th></th></tr>' ];

			if (trip.currentStop)
				info.push('<dt>Now at</dt><dd>' + trip.currentStop + '</dd>');
			else if (trip.nextStop)
				info.push('<dt>Next stop</dt><dd>' + trip.nextStop + '</dd>');
			if (trip.continuesAs)
				info
						.push('<dt>Continues as</dt><dd><a href="#" onclick="displayTripId(\''
								+ trip.continuesAs
								+ '\')">'
								+ trip.continuesAs
								+ '</a></dd>');

			// Display vehicle on-time running
			if (trip.delay) {
				var minLate = Math.floor(trip.delay / 60);
				var status = (minLate > 0 ? 'late' : (minLate < 0 ? 'early'
						: 'ontime'));
				info.push('<dt>On-time status</dt><dd class="'+status+'">'
						+ (minLate > 0 ? minLate + 'm late'
								: (minLate == 0 ? 'on time' : (minLate * -1)
										+ 'm early')) + '</dd>');
			}

			// Build stop table
			for ( var idx in trip.stops) {
				var stop = trip.stops[idx];
				var arrival = stop.actual && stop.actual.arr ? stop.actual.arr
						: (stop.scheduled.arr != stop.predicted.arr ? stop.predicted.arr
								: 'on time');
				var arrivalCss = stop.actual && stop.actual.arr ? 'passed'
						: (stop.scheduled.arr != stop.predicted.arr ? ''
								: 'on-time');
				var departure = stop.actual && stop.actual.dep ? stop.actual.dep
						: (stop.scheduled.dep != stop.predicted.dep ? stop.predicted.dep
								: 'on time');
				var departureCss = stop.actual && stop.actual.dep ? 'passed'
						: (stop.scheduled.arr != stop.predicted.arr ? ''
								: 'on-time');
				var rowCss = (trip.currentStop == stop.name) ? 'current-stop'
						: (!trip.currentStop && trip.nextStop == stop.name ? 'next-stop'
								: '');
				var now = new Date();
				stops
						.push('<tr class="'+rowCss+'"><td title="Stop '+stop.stopId+'">'
								+ stop.name.replace(/Station Platform/, '')
								+ '</td>'
								+ '<td class="time '+arrivalCss+'">'
								+ stop.scheduled.arr
								+ '</td>'
								+ '<td class="time '+arrivalCss+'">'
								+ arrival
								+ '</td><td>'
								+ delayForStop(stop, 'arr')
								+ '</td>'
								+ '<td class="time '+departureCss+'">'
								+ stop.scheduled.dep
								+ '</td>'
								+ '<td class="time '+departureCss+'">'
								+ departure
								+ '</td><td>'
								+ delayForStop(stop, 'dep') + '</td></tr>');
			}

			// Update DOM elements
			$('#trip-header').html(
					colourRunNumberByDelay(trip)
							+ ' '
							+ (trip.label ? trip.label
									: "scheduled continuing service"));
			$('#trip-body').html(
					'<dl>' + info.join('') + '</dl><table class="stops">'
							+ stops.join('') + '</table>');
		}

		// when a user clicks a trip, show detailed trip info
		$('#train-list').on('click', 'a', function(ev) {
			var tripId = $(this).data('tripId');
			displayTripId(tripId);
			return false;
		});

		// when a user clicks back, dismiss the trip info panel
		$(document).on('click', '.back', function() {
			$('#trip-info').hide();
			$('#network-info').show();
			clearInterval(tripUpdateTimer);
		});
	</script>

</body>
</html>