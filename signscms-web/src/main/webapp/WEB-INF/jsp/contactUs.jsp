<%--
  Created by IntelliJ IDEA.
  User: administrator
  Date: 23/4/17
  Time: 19:02
  To change this template use File | Settings | File Templates.
--%>
<!DOCTYPE html>
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
    <script src="<c:url value="/resources/js/jquery.form.min.js" />"></script>
    <script src="<c:url value="/resources/js/contactUs.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap.js" />"></script>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body onload="loadContactUsForm()">

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
         <!--    <li>
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
                    <span class="visible-xs">
                        <%= session.getAttribute("firstName") %>
                    </span>

                </a>
            </li>
        </ul>
    </div>
</nav>


<!-- Views to Choose -->
<div id="allSigns" class="container">
    <div class="col-md-5">
        <div class="form-area-contactus">
            <!--   <form id="contactUsForm" method="post" role="form" class="form-horizontal"
                     onsubmit="return saveContactUsForm();"> -->
            <form id="contactUsForm" method="post" role="form" class="form-horizontal"
                  action="/upload" enctype="multipart/form-data">
                <br style="clear:both">
                <h3 style="margin-bottom: 25px; text-align: center;">Contact Form</h3>
                <h5 style="margin-bottom: 25px; text-align: center;"> or alternatively contact PI ServiceDesk on (02) 9379 4000</h5>
                <div class="form-group">
                    <input type="text" class="form-control" id="name" name="name" placeholder="Name" required="">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" id="email" name="email" placeholder="Email">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" id="mobile" name="mobile" placeholder="Mobile Number">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" id="subject" name="subject" placeholder="Subject"
                           required="">
                </div>
                <div class="form-group">
                    <textarea class="form-control" type="textarea" id="message" placeholder="Message" maxlength="240"
                              name="message" rows="7" required=""></textarea>
                    <span class="help-block"><p id="characterLeft"
                                                class="help-block ">You have reached the limit</p></span>
                </div>
                <div class="form-group">
                    <input id="upload" name="file" class="form-control" type="file"
                           placeholder="Upload Screenshot">
                </div>
                <button type="submit" id="submit" name="submit" class="btn btn-primary pull-right">Submit Form</button>
            </form>
        </div>
    </div>
</div>
<input type="hidden" id="hdnSession" value=" <%= session.getAttribute("authenticated") %>"/>
</body>
</html>
