<%--
  Created by IntelliJ IDEA.
  User: Oscar
  Date: 6/05/2017
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>SydneyTrains - Digital Signage - Init page</title>
    <link href="<c:url value="/resources/css/init.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-theme.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
     <script src="<c:url value="/resources/js/bootstrap.js" />"></script>
   <script src="<c:url value="/resources/js/angular.min.js" />"></script>
    <script src="<c:url value="/resources/js/init.js" />"></script>
</head>
<body onload="init()">
    <div id="initpage" class="container">
        <div class="well"><h1>Initialisation commands</h1></div>
        <p>The following commmands are available:</p>
        <ul class="list-group">
            <li class="list-group-item"><button type="button" class="btn btn-default" onclick=executeClear()>Clear DB</button> Clears the H2Database from all Cnf data (not user data)</li>
            <li class="list-group-item"><button type="button" class="btn btn-default" onclick=executeLoad()>Load Config</button> Loads the SCMS_Config.xml file</li>
            <li class="list-group-item"><button type="button" class="btn btn-default" onclick=executeShowXML()>Show XML</button> Shows the contents of the SCMS_Config.xml</li>
        </ul>
        <div class="panel-group">
            <div class="panel panel-default">
                <div class="panel-body" id="clear"></div>
                <div class="panel-body" id="load"></div>
                <div class="panel-body" id="showXML"></div>
            </div>
        </div>
    </div>
</body>
</html>
