<%--
  Created by IntelliJ IDEA.
  User: administrator
  Date: 18/4/17
  Time: 22:20
  To change this template use File | Settings | File Templates.
--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>SydneyTrains - Digital Signage</title>
    <link href="<c:url value="/resources/css/sign.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.2.1.min.js" />"></script>
    <script src="<c:url value="/resources/js/sign.js" />"></script>
</head>
<body onload="getSignForDevice()" style="margin: 0px">
<div id="outer">

</div>
<div>
    <div id="labelContainer">

    </div>
<div id="baseContainer">

</div>
</div>
</body>
</html>
