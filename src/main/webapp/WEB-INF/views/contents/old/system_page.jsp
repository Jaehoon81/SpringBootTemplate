<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>시스템 관리자 페이지</title>

    <link rel="stylesheet" type="text/css" href="/static/css/main.css">
    <link rel="stylesheet" type="text/css" href="/static/css/contents/system.css">
    <style>
        p {
            margin: 8px 0px 8px 0px;
            line-height: 120%;
        }
    </style>
</head>
<body>
    <div class="auth-system-container">
        <h2>시스템 관리자 페이지</h2>
        <p><span style="font-weight: bold;"><c:out value="${displayName}"/></span>님, 시스템 관리자 권한으로 접속하셨습니다.</p>
        <p>(아래는 승인 대기 중인 관리자 계정의 목록입니다.)</p>

        <div id="admin-list-container">
            <table>
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>아이디</th>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>요청 메시지</th>
                        <th>가 &middot; 부</th>
                    </tr>
                </thead>
                <tbody id="admin-list-body">
                    <!-- ADMIN 계정의 목록이 JavaScript로 여기에 동적으로 로드됨 -->
                    <tr>
                        <td colspan="6" style="text-align: center;">승인 대기 중인 관리자 계정이 없습니다.</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <br>
<%--        <button onclick="location.href='/secure-page'">보호된 페이지로 돌아가기</button>--%>
        <p><a href="/secure-page">보호된 페이지로 돌아가기</a></p>
        <button id="logoutBtn" class="auth-button" style="margin-bottom: 20px;">로그아웃</button>
    </div>

    <!-- Full Text 팝업창 모달 구조 -->
    <div id="fullTextMsgModal" class="fulltext-popup">
        <div class="popup-content">
            <span class="close-popup">&times;</span>
            <h3>요청 메시지 상세</h3>
            <p id="fullTextMsgContent"></p>
        </div>
    </div>

    <!-- jQuery CDN -->
<%--    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>--%>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"
            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>

    <script type="text/javascript" src="/static/js/contents/system.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#logoutBtn').click(function () {
                $.ajax({
                    url: '/api/auth/web-logout',
                    type: 'POST',
                    success: function (response) {
                        // 서버에 로그아웃 요청을 보내 쿠키를 만료시킴
                        alert('로그아웃 되었습니다.');
                        window.location.href = '/';  // 로그아웃 후 로그인 페이지(login.jsp)로 이동
                    },
                    error: function (xhr) {
                        alert('로그아웃 실패: ' + (xhr.responseText || '알 수 없는 오류'));
                    }
                });
            });
        });
    </script>
</body>
</html>
