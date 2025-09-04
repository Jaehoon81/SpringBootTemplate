<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>회원가입 및 로그인</title>

    <link rel="stylesheet" type="text/css" href="/static/css/login.css">
<%--    <script src="https://code.jquery.com/jquery-3.7.1.min.js"--%>
<%--            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>--%>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>
<body>
    <!-- 로그인 폼 구조 -->
    <div class="container">
        <h2>로그인</h2>
        <div class="form-group">
            <label for="login-username">* 아이디:</label>
            <input type="text" id="login-username" name="username">
        </div>
        <div class="form-group">
            <label for="login-password">* 비밀번호:</label>
            <input type="password" id="login-password" name="password">
        </div>
        <button id="loginBtn">로그인</button>
        <p id="login-message" class="message"></p>

        <!-- 회원가입 모달의 텍스트 링크 -->
        <p><a href="#" id="openRegisterModal">회원가입</a></p>
        <hr>
        <p><a href="/secure-page">보호된 페이지로 이동</a> (로그인 후 접근 가능)</p>
    </div>

    <!-- 회원가입 모달 구조 -->
    <div id="registerModal" class="modal">
        <div class="modal-content">
            <span class="close-button">&times;</span>
            <h2>회원가입</h2>
            <div class="form-group">
                <label for="reg-username">* 아이디:</label>
                <input type="text" id="reg-username" name="username">
            </div>
            <div class="form-group">
                <label for="reg-password">* 비밀번호:</label>
                <input type="password" id="reg-password" name="password">
            </div>
            <div class="form-group">
                <label for="reg-displayname">이름 (선택):</label>
                <input type="text" id="reg-displayname" name="displayname">
            </div>
            <div class="form-group">
                <label for="reg-role">* 권한:</label>
                <select id="reg-role" name="role">
                    <option value="USER" selected>일반 사용자</option>
                    <option value="ADMIN">관리자</option>
                </select>
            </div>
            <button id="registerBtn">회원가입</button>
            <p id="reg-message" class="message"></p>
        </div>
    </div>

    <script src="/static/js/login.js"></script>
</body>
</html>
