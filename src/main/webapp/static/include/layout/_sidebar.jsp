<!-- src/main/webapp/static/include/layout/_sidebar.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="sidebar">
    <div class="sidebar-header">
        <h3><i class="fas fa-bars"></i> 메뉴</h3>
    </div>
    <ul class="sidebar-menu">
        <li data-menu-id="account-approval" class="active">
            <a href="#" data-content-url="/contents/approval"><i class="fas fa-user-check"></i>&nbsp;사용자 관리</a>
        </li>
        <li data-menu-id="data-statistics">
            <a href="#" data-content-url="/contents/statistics"><i class="fas fa-chart-line"></i>&nbsp;&nbsp;데이터 통계</a>
        </li>
        <li data-menu-id="edit-profile">
            <a href="#" data-content-url="/contents/profile"><i class="fas fa-user-edit"></i>&nbsp;마이 페이지</a>
        </li>
    </ul>
    <!-- 아래의 sidebar-footer는 main-header로 이동 -->
<%--    <div class="sidebar-footer">--%>
<%--        <p>환영합니다, <span id="currentUserName">${displayName}</span>님!</p>--%>
<%--        <button id="logoutBtn" class="sidebar-logout-btn">로그아웃</button>--%>
<%--    </div>--%>
</nav>
