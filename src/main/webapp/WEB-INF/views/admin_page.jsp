<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>관리자 페이지</title>

    <link rel="stylesheet" type="text/css" href="/static/css/login.css">
    <style>
        p {
            margin: 8px 0px 8px 0px;
            line-height: 120%;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>관리자 페이지</h2>
        <p>관리자 권한을 가진 사용자만 이 페이지를 볼 수 있습니다.</p>
<%--        <button onclick="location.href='/secure-page'">보호된 페이지로 돌아가기</button>--%>
        <p><a href="/secure-page">보호된 페이지로 돌아가기</a></p>
        <button id="logoutBtn" style="margin-bottom: 20px;">로그아웃</button>
    </div>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script type="text/javascript">
        $('#logoutBtn').click(function () {
            $.ajax({
                url: '/api/auth/logout',
                type: 'POST',
                success: function (response) {
                    // 서버에 로그아웃 요청을 보내 쿠키를 만료시킴
                    alert('로그아웃 되었습니다.');
                    window.location.href = '/';  // 로그아웃 후 로그인 페이지(index.jsp)로 이동
                },
                error: function (xhr) {
                    alert('로그아웃 실패: ' + (xhr.responseText || '알 수 없는 오류'));
                }
            });
        });
    </script>
</body>
</html>
