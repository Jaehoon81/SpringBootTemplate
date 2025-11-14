<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>보호된 페이지</title>

    <link rel="stylesheet" type="text/css" href="/static/css/main.css">
    <style>
        .auth-secure-container {
            background-color: #ffffff;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
            width: 1200px;
            max-width: 90%;
            text-align: center;
            box-sizing: border-box;
        }
        .auth-secure-container h2 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
            font-size: 1.6em; /* 폰트 크기 */
        }

        /* 페이지 하단 버튼 그룹 스타일 */
        /*.auth-secure-container > button {*/
        /*    margin: 15px 8px; !* 하단 버튼 마진 조정 *!*/
        /*}*/

        p {
            margin: 8px 0px 8px 0px;
            line-height: 120%;
        }
    </style>
</head>
<body>
    <div class="auth-secure-container">
        <h2>보호된 페이지</h2>
        <p><span style="font-weight: bold;"><c:out value="${displayName}"/></span>님, 환영합니다!</p>
        <p>이 페이지는 인증된 사용자만 접근할 수 있습니다.</p>
        <br>
        <p style="color: #007bff;"><!-- 권한에 따른 메시지 표시 -->
            <c:choose>
                <c:when test="${userRole eq 'SYSTEM'}">
                    (<c:out value="${displayName}"/>님은 시스템 관리자 권한이므로 시스템의 모든 기능을 총괄합니다.)
                </c:when>
                <c:when test="${userRole eq 'ADMIN'}">
                    (<c:out value="${displayName}"/>님은 관리자 권한으로 모든 기능을 사용할 수 있습니다.)
                </c:when>
                <c:when test="${userRole eq 'USER'}">
                    (<c:out value="${displayName}"/>님은 일반 사용자 권한으로 로그인하셨습니다. 계정 승인 작업은 시스템 관리자 또는 관리자만 가능합니다.)
                </c:when>
                <c:otherwise>
                    (<c:out value="${displayName}"/>님은 알 수 없는 권한입니다.)
                </c:otherwise>
            </c:choose>
        </p><br>
        <c:choose>
            <c:when test="${userRole eq 'SYSTEM'}">
                <!-- SYSTEM 권한일 경우 SYSTEM 페이지 링크 추가 -->
                <p><a href="/system-page">시스템 관리자 페이지로 이동</a></p>
            </c:when>
            <c:otherwise>
                <!-- 나머지 권한일 경우 ADMIN 페이지 링크 추가 -->
                <p><a href="/admin-page">관리자 페이지로 이동</a></p>
            </c:otherwise>
        </c:choose>
        <button id="logoutBtn" style="margin-bottom: 20px;">로그아웃</button>
    </div>

    <!-- jQuery CDN -->
<%--    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>--%>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"
            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>

    <script type="text/javascript">
        $(document).ready(function () {
            $('#logoutBtn').click(function () {
                $.ajax({
                    url: '/api/auth/web-logout',
                    type: 'POST',
                    success: function (response) {
                        // 로그아웃 성공 시 JWT 토큰을 localStorage에서 삭제
                        // localStorage.removeItem('jwtToken');
                        // 간단하게 클라이언트에서 쿠키를 만료시킴
                        // document.cookie = "jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

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
        // 페이지 로드 시 토큰 유무 확인 (선택 사항)
        // -> 사용하지 않음: 401 Unauthorized 오류는 Spring Security가 처리하고 로그인 페이지(login.jsp)로 리다이렉트
        // $(document).ready(function () {
        //     if (!localStorage.getItem('jwtToken')) {
        //         alert('로그인이 필요합니다.');
        //         window.location.href = '/';
        //     }
        // });
    </script>
</body>
</html>
