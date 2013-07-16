<%--
  TripInfoServlet page
  User: rhanson
  Date: 11/07/13
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Sydney Trains real-time network status</title>

    <!-- use CDNs for resources -->
    <!-- Active content stripped -->
    <!-- Active content stripped -->
    <!-- Active content stripped -->
</head>
<body>


<style type="text/css">
    html, body { height: 100%; margin: 0; }
    body { font-family: Helvetica, sans-serif; font-size: 90%; }
    td, th { font-size: 80%; color: #333; }
    a, a:visited { color: #555; }

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
    table.stops td, table.stops th { padding: 2px 4px 2px 0; }

    dl > dt { float: left; clear: left; width: 100px; color: #888; }

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
    .early { color: #00a; }
    .late { color: #d00; }
    .scheduled { color: #888; }
    td.passed { color: #888; }
    td.on-time { color: #080; }
    td.time { text-align: right; }
    .current-stop, .next-stop { background: #eee; }
</style>

<div id="map-container">
    <div id="map-canvas"></div>
</div>

<div id="network-info" class="info-panel">
    <h3>Sydney Trains real-time status</h3>
    <p>Currently operating trains<p>
    <p id="train-list">Contacting server...</p>
</div>
<div id="trip-info" class="info-panel" style="display: none;">
    <div style="float: right;">
        <a href="#" class="btn back">&lt; Back</a>
    </div>
    <h4 id="trip-header"></h4>
    <div id="trip-body"></div>
</div>

<!-- Active content stripped -->

</body>
</html>