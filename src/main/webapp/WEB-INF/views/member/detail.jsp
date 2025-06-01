<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Detail</title>

    <script src="https://code.jquery.com/jquery-3.7.1.min.js"
            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
</head>
<body>
    <table>
        <tr><th>ID</th><td>${member.id}</td></tr>
        <tr><th>Email</th><td>${member.memberEmail}</td></tr>
        <tr><th>Password</th><td>${member.memberPassword}</td></tr>
        <tr><th>Name</th><td>${member.memberName}</td></tr>
        <tr><th>Age</th><td>${member.memberAge}</td></tr>
        <tr><th>Mobile</th><td>${member.memberMobile}</td></tr>
    </table>
</body>
<script type="text/javascript">

</script>
</html>
