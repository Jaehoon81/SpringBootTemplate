<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Error(Jsp)</title>
</head>
<body>
    <h2>4xx Error!!</h2>

    <p>TimeStamp: <c:out value="${timeStamp}"/></p>
    <p>StatusCode: <c:out value="${statusCode}"/></p>
    <p>ExceptionType: <c:out value="${exceptionType}"/></p>
</body>
</html>
