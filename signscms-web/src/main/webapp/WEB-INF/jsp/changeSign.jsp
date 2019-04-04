<%--
  Created by IntelliJ IDEA.
  User: administrator
  Date: 23/4/17
  Time: 19:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Sydney Trains - Dynamic Signage</title>
    <link href="<c:url value="/resources/css/device.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/header.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-theme.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
    <script src="<c:url value="/resources/js/changeSign.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap.js" />"></script>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body onload="loadAllSigns()">

<div class="navbar-more-overlay"></div>
<nav class="navbar navbar-inverse navbar-fixed-top animate">
    <div class="container navbar-more visible-xs">
    </div>
    <div class="container">
        <div class="navbar-header hidden-xs">
            <a class="navbar-brand" onclick="redirectToSigns();">Dynamic Signage Application</a>
        </div>

        <ul class="nav navbar-nav navbar-right mobile-bar">
            <li>
                <a onclick="redirectToSigns();">
                    <span class="menu-icon fa fa-train"></span>
                    Town Hall
                </a>
            </li>

            <li>
                <a onclick="redirectToSigns();">
                    <span class="menu-icon fa fa-map-signs"></span>
                    Signs
                </a>
            </li>
            <li class="hidden-xs">
                <a href="#">
                    <span class="menu-icon fa fa-info"></span>
                    About
                </a>
            </li>
            <!-- <li>
                <a onclick="redirectToSchedule();">
                    <span class="menu-icon fa fa-clock-o"></span>
                    <span class="hidden-xs">Schedule</span>
                    <span class="visible-xs">Schedule</span>
                </a>
            </li>
            <li>
                <a onclick="redirectToContactUs();">
                    <span class="menu-icon fa fa-phone"></span>
                    <span class="hidden-xs">Contact Us</span>
                    <span class="visible-xs">Contact</span>
                </a>
            </li> -->
            <%
                if (session.getAttribute("role").equals("admin") || session.getAttribute("role").equals("superuser")) {
            %>
            <li class="hidden-xs">
                <a onclick="redirectToAdmin();">
                    <span class="menu-icon fa fa-tools"></span>
                    Admin
                </a>
            </li>
            <% } %>
            <li class="hidden-xs">
                <a href="#">
                    <span class="menu-icon fa fa-user-circle-o"></span>
                    <span class="hidden-xs">
                        <%= session.getAttribute("firstName") %>
                        <%= session.getAttribute("lastName") %>
                    </span>
                   <!-- <span class="visible-xs">
                        <%= session.getAttribute("firstName") %>
                    </span> -->

                </a>
            </li>
        </ul>
    </div>
</nav>


<!-- Views to Choose -->
<div id="allSigns" class="container">
    <h3 class="div_label" id="selectedSign"></h3>
    <div id="views" class="row">

    </div>
    <h3 class="div_label">Available Modes:</h3>
    <div id="views_select" class="row">

    </div>
</div>
<input type="hidden" id="hdnSession" value=" <%= session.getAttribute("authenticated") %>" />
</body>
</html>
