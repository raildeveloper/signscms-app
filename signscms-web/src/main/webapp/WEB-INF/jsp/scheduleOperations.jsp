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
    <link href="<c:url value="/resources/css/scheduleOperations.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/header.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-theme.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-select.min.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-datetimepicker.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
    <script src="<c:url value="/resources/js/scheduleOperations.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap-select.min.js" />"></script>

    <script src="<c:url value="/resources/js/moment.js" />"></script>
    <script src="<c:url value="/resources/js/collapse.js" />"></script>
    <script src="<c:url value="/resources/js/transition.js" />"></script>
    <script src="<c:url value="/resources/js/i18n/defaults-en_US.min.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap-datetimepicker.min.js" />"></script>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body onload="scheduleOperations()">


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
            </li>-->
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
        <h3 id="sch_id" class="div_label"></h3>
        <div class="row">
            <div class="col-md-11">
                <div class="form-area">
                    <form id="scheduleOperForm" method="post" role="form" class="form-horizontal"
                          onsubmit="return submitSchedule();">
                        <br style="clear:both">
                        <div class="form-group">
                            <label class="col-md-4 control-label" for="device">Escalator</label>
                            <div class="col-md-6">
                                <div class='input-group'>
                                    <select name="devicePicker" id="device" title="Escalator" class="selectpicker show-tick" required=""
                                            onchange="displayModesForSelectedDevice($(this).val())">
                                        <option>Select...</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-md-4 control-label" for="view">Mode</label>
                            <div class="col-md-6">
                                <div class='input-group'>
                                    <select name="viewPicker" id="view" title="Available Modes" class="selectpicker" required="">
                                        <option>Select...</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-md-4 control-label" for="datepicker">Start Time</label>
                            <div class="col-md-6">
                                <div class='input-group date' id='datepicker' style="width:250px;">
                                    <input id="st_date" type="text" class="form-control" required="">
                                    <span class="input-group-addon">
                                <span class="glyphicon glyphicon-time"></span>
                            </span>
                                </div>
                            </div>
                        </div>
                        <!-- <div class="form-group">
                             <label class="col-md-4 control-label" for="timepicker">Start Time</label>
                             <div class="col-md-6">
                                 <div class='input-group date' id='timepicker' style="width:140px;">
                                     <input type="text" class="form-control">
                                     <span class="input-group-addon">
                                 <span class="glyphicon glyphicon-time"></span>
                             </span>
                                 </div>
                             </div>
                         </div> -->
                        <div class="form-group">
                            <label class="col-md-4 control-label" for="datepicker_e">End Time</label>
                            <div class="col-md-4">
                                <div class='input-group date' id='datepicker_e' style="width:250px;">
                                    <input id="et_date" type="text" class="form-control" required="">
                                    <span class="input-group-addon">
                                <span class="glyphicon glyphicon-time"></span>
                            </span>
                                </div>
                            </div>
                        </div>
                        <!--<div class="form-group">
                            <label class="col-md-4 control-label" for="timepicker_e">End Time</label>
                            <div class="col-md-6">
                                <div class='input-group date' id='timepicker_e' style="width:140px;">
                                    <input type="text" class="form-control">
                                    <span class="input-group-addon">
                                <span class="glyphicon glyphicon-time"></span>
                            </span>
                                </div>
                            </div>
                        </div>-->

                        <button type="submit" value="submit" id="submit" name="submit"
                                class="btn btn-primary pull-right">Submit
                        </button>
                    </form>
                </div>
            </div>
        </div>

    </div>
</div>
<input type="hidden" id="hdnSession" value=" <%= session.getAttribute("authenticated") %>"/>
</body>
</html>
