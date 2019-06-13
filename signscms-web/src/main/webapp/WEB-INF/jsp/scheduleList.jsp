<%--
  Created by IntelliJ IDEA.
  User: administrator
  Date: 14/5/17
  Time: 10:58
  To change this template use File | Settings | File Templates.
--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Device List</title>
    <link href="<c:url value="/resources/css/schedule.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/header.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-theme.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
    <script src="<c:url value="/resources/js/scheduleList.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap.js" />"></script>
    <script src="<c:url value="/resources/js/moment.js"/> "></script>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body onload="loadAllSchedules()">


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
                if (session != null && session.getAttribute("role") != null) {
                if (session.getAttribute("role").equals("admin") || session.getAttribute("role").equals("superuser")) {
            %>
            <li class="hidden-xs">
                <a onclick="redirectToAdmin();">
                    <span class="menu-icon fa fa-tools"></span>
                    Admin
                </a>
            </li>
            <% }} %>
            <li class="hidden-xs">
                <a href="#">
                    <span class="menu-icon fa fa-user-circle-o"></span>
                    <span class="hidden-xs">
                        <%= session.getAttribute("firstName") %>
                        <%= session.getAttribute("lastName") %>
                    </span>
                    <span class="visible-xs">
                        <%= session.getAttribute("firstName") %>
                    </span>

                </a>
            </li>
        </ul>
    </div>
</nav>


<!--- Device List Below -->
<div>


    <div id="allDevices" class="container">
        <h3 class="div_label" style="margin-bottom: 25px;">Schedules:</h3>
        <div style="float: left">
            <a class='btn btn-info btn-xs' onclick="addSchedule()"><span class="glyphicon glyphicon-plus"></span> Add
                Schedule</a>
        </div>
        <div id="scheduleRows" class="row">
            <table class="table table-striped custab">
                <thead>
                <tr>
                    <th>Escalator</th>
                    <th>Mode</th>
                    <th>Start</th>
                    <th>End</th>
                    <th>Status</th>
                    <th class="text-center">Action</th>
                </tr>
                </thead>
                <tbody id="schTableBody">
                </tbody>
            </table>
        </div>
    </div>
</div>
<input type="hidden" id="hdnSession" value=" <%= session.getAttribute("authenticated") %>"/>
</body>
</html>
