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
    <link href="<c:url value="/resources/css/sign.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap-theme.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/bootstrap.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
    <script src="<c:url value="/resources/js/signController.js" />"></script>
    <script src="<c:url value="/resources/js/bootstrap.js" />"></script>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>

<div class = "container">
    <div class="wrapper">
        <form action="" method="post" id="Login_Form" class="form-signin" onsubmit="return authenticate();">
            <h3 class="form-signin-heading">Welcome to SydneyTrains - Dynamic Signage Product</h3>
            <hr class="colorgraph">
            <div class="form-signin-heading" id="errorMessage"></div>
            <input type="text" class="form-control" name="Username" placeholder="Username" required="" autofocus="" />
            <input type="password" class="form-control" name="Password" placeholder="Password" required=""/>

            <button class="btn btn-lg btn-primary btn-block"  name="Submit" value="Login" type="Submit">Login</button>
            <h4 class="form-signin-heading">Developed by Passenger Information - Customer Service</h4>

        </form>
    </div>
</div>

</body>
</html>
