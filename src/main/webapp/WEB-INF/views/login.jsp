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

    <link rel="stylesheet" type="text/css" href="/static/css/main.css">
    <!-- Font Awesome 아이콘 사용을 위해 추가 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</head>
<body>
    <!-- 로그인 폼 구조 -->
    <div class="login-container">
        <h1>Spring-Boot Template</h1>

        <h2>로그인</h2>
        <div class="form-group">
            <label for="login-username">* 아이디:</label>
            <input type="text" id="login-username" name="username" placeholder="아이디를 입력해주세요." required>
        </div>
        <div class="form-group">
            <label for="login-password">* 비밀번호:</label>
            <input type="password" id="login-password" name="password" placeholder="비밀번호를 입력해주세요." required>
        </div>
        <div class="form-group checkbox-group">
            <input type="checkbox" id="remember-id" name="remember">
            <label for="remember-id">아이디 기억하기</label>
        </div>
        <button id="loginBtn">로그인</button>
        <p id="login-message" class="message"></p>

        <!-- 회원가입 팝업창 모달의 텍스트 링크 -->
        <p><a href="#" id="openRegisterModal">회원가입</a></p>
        <hr>
<%--        <p><a href="/secure-page">보호된 페이지로 이동</a> (로그인 후 접근 가능)</p>--%>
        <p><a href="/dashboard">대시보드로 이동</a> (로그인 후 접근 가능)</p>
    </div>

    <!-- 회원가입 팝업창 모달 구조 -->
    <div id="registerModal" class="modal">
        <div class="modal-content">
            <span class="close-button">&times;</span>
            <h2>회원가입</h2>
            <div class="form-group">
                <label for="reg-username">* 아이디:</label>
                <input type="text" id="reg-username" name="username" placeholder="4자 이상의 영문 대소문자, 숫자만 가능" required>
            </div>
            <div class="form-group">
                <label for="reg-password">* 비밀번호:</label>
                <input type="password" id="reg-password" name="password" placeholder="10자 이상의 영문, 숫자, 특수문자를 모두 포함" required>
            </div>
            <div class="form-group">
                <label for="reg-displayname">* 이름:</label>
                <input type="text" id="reg-displayname" name="displayname" placeholder="이름을 입력해주세요." required>
            </div>
            <div class="form-group">
                <label for="reg-email">* 이메일:</label>
                <input type="email" id="reg-email" name="email" placeholder="이메일을 입력해주세요." required>
            </div>
            <div class="form-group">
                <label for="reg-req-message">요청 메시지(선택):</label>
                <textarea id="reg-req-message" name="reqMessage" rows="4" placeholder="상위 관리자의 계정 승인 시 요청할 메시지를 작성합니다."></textarea>
            </div>
            <div class="form-group">
                <label for="reg-role">* 권한:</label>
                <select id="reg-role" name="role">
                    <option value="USER" selected>일반 사용자</option>
                    <option value="ADMIN">관리자</option>
                </select>
            </div>
            <div class="form-group" id="adminnameGroup">
                <label for="reg-adminname">담당 관리자(조건):</label>
                <select id="reg-adminname" name="adminname">
                    <option value="">-- 관리자 선택 --</option>
                    <!-- ADMIN 권한의 displayname 목록이 JavaScript로 여기에 채워짐 -->
                </select>
            </div>
            <button id="registerBtn">회원가입</button>
            <p id="reg-message" class="message"></p>
        </div>
    </div>

<%--    <script src="https://code.jquery.com/jquery-3.7.1.min.js"--%>
<%--            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>--%>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="/static/js/login.js"></script>
</body>
</html>
