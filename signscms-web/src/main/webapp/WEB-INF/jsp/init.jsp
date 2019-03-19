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
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
    <script src="<c:url value="/resources/js/init.js" />"></script>
</head>
<body onload="init()">
    <div id="initpage">
        <h1>Initialisation commands</h1>
        <p>The following commmands are available:</p>
        <ul>
            <li>Clear - clears the H2Database from all Cnf data (not user data)</li>
            <li>Load - loads the SCMS_Config.xml file</li>
            <li>ShowXML - shows the contents of the SCMS_Config.xml</li>
        </ul>
        <div id="clear"></div>
        <div id="load"></div>
        <div id="showXML"></div>
    </div>
</body>
</html>
