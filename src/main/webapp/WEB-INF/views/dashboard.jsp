<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>대시보드</title>
    <link rel="icon" type="image/x-icon" href="/favicon_03.ico"/>

    <link rel="stylesheet" type="text/css" href="/static/css/main.css">
    <link rel="stylesheet" type="text/css" href="/static/css/main_layout.css">
    <link rel="stylesheet" type="text/css" href="/static/css/contents/statistics.css">
    <link rel="stylesheet" type="text/css" href="/static/css/contents/profile.css">
    <!-- Font-Awesome CDN -->
<%--    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>--%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"/>
    <!-- Date-Range-Picker CSS -->
<%--    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css"/>--%>
    <link rel="stylesheet" type="text/css" href="/static/libs/daterangepicker/daterangepicker.css">
    <style>
        p {
            margin: 8px 0px 8px 0px;
            line-height: 120%;
        }
        p.loading-contents {
            width: 1160px;
            max-width: 90%;
            text-align: center;
            padding: 20px;
        }
    </style>
</head>
<body data-display-name="${displayName}" data-user-role="${userRole}" data-profile-picture-path="${profilePicturePath}">
<%--    <div class="main-wrapper">--%>
<%--        <!-- 사이드바 포함 -->--%>
<%--        <jsp:include page="/static/include/layout/_sidebar.jsp"/>--%>
<%--        <div class="content-wrapper"><!-- 메인 콘텐츠 영역 -->--%>
<%--            <header class="content-header">--%>
<%--                <h1 id="contentTitle"></h1>--%>
<%--            </header>--%>
<%--            <main id="main-content-area" class="content-area">--%>
<%--                <!-- 여기에 메뉴에 따라 동적으로 콘텐츠가 로드됨 -->--%>
<%--                <p class="loading-contents">콘텐츠 로딩 중...</p>--%>
<%--            </main>--%>
<%--        </div>--%>
<%--    </div>--%>
    <div class="app-layout"><!-- 전체 앱 레이아웃 래퍼 -->
        <!-- 헤더 포함 -->
        <jsp:include page="/static/include/layout/_header.jsp"/>
        <div class="dashboard-body-wrapper"><!-- 사이드바 + 메인 콘텐츠 및 푸터 영역 -->
            <!-- 사이드바 포함 -->
            <!-- _sidebar.jsp에 profilePicturePath 파라미터를 전달 -->
            <!-- (아래의 jsp:include 태그 사이에는 절대 불필요한 공백, 탭, 줄바꿈 등을 넣으면 안됨!!) -->
            <jsp:include page="/static/include/layout/_sidebar.jsp">
                <jsp:param name="profilePicturePath" value="${profilePicturePath}"/>
                <jsp:param name="userRole" value="${userRole}"/>
            </jsp:include>
            <div class="main-content-and-footer-area"><!-- 메인 콘텐츠 및 푸터 영역 -->
                <div class="content-wrapper"><!-- 주요 콘텐츠 파트 -->
                    <header class="content-header">
                        <h1 id="contentTitle"></h1>
                    </header>
                    <main id="main-content-area" class="content-area">
                        <!-- 여기에 메뉴에 따라 동적으로 콘텐츠가 로드됨 -->
                        <p class="loading-contents">콘텐츠 로딩 중...</p>
                    </main>
                </div>
                <!-- 푸터 포함 -->
                <jsp:include page="/static/include/layout/_footer.jsp"/>
            </div>
        </div>
    </div>

    <!-- Full Text 팝업창 모달 구조 (모든 콘텐츠에서 재활용) -->
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
    <!-- Date-Range-Picker JS -->
<%--    <script src="https://cdn.jsdelivr.net/jquery/latest/jquery.min.js"></script>--%>
<%--    <script src="https://cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>--%>
<%--    <script src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js"></script>--%>
    <script type="text/javascript" src="/static/libs/daterangepicker/moment.min.js"></script>
    <script type="text/javascript" src="/static/libs/daterangepicker/daterangepicker.js"></script>

    <script type="text/javascript" src="/static/js/dashboard.js"></script>
    <script type="text/javascript" src="/static/js/contents/statistics.js"></script>
    <script type="text/javascript" src="/static/js/contents/profile.js"></script>
    <c:if test="${userRole eq 'SYSTEM'}">
        <link rel="stylesheet" type="text/css" href="/static/css/contents/system.css">
        <script type="text/javascript" src="/static/js/contents/system.js"></script>
    </c:if>
    <c:if test="${userRole eq 'ADMIN'}">
        <link rel="stylesheet" type="text/css" href="/static/css/contents/admin.css">
        <script type="text/javascript" src="/static/js/contents/admin.js"></script>
    </c:if>
    <script type="text/javascript">
        $(document).ready(function () {
            // 로그아웃 버튼 클릭 이벤트 (헤더에 추가)
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
