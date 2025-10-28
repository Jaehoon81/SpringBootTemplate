<!-- src/main/webapp/static/include/layout/_sidebar.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="sidebar">
    <div class="sidebar-header">
        <h3><i class="fas fa-bars"></i> 메뉴</h3>
    </div>
    <div class="profile-picture-container">
        <!-- param.profilePicturePath를 사용하여 전달받은 파라미터 값 사용 -->
        <img id="profilePicture"
             src="${(param.profilePicturePath != null && not empty param.profilePicturePath) ? param.profilePicturePath : '/static/images/default_profile_02.png'}"
             alt="프로필 사진">
        <input type="file" id="profilePictureInput" accept="image/*" style="display: none;">
        <span class="upload-overlay" title="프로필 사진 변경"><i class="fas fa-folder-open"></i></span>
    </div>
    <ul class="sidebar-menu">
        <!-- param.userRole을 사용하여 전달받은 파라미터 값 사용 -->
        <c:if test="${param.userRole ne 'USER'}">
            <!-- USER 권한(역할)이 아닐 때만 '사용자 관리' 메뉴를 표시 -->
            <li data-menu-id="account-approval"><!-- class="active" 제거 -->
                <a href="#" data-content-url="/contents/approval"><i class="fas fa-user-check"></i>&nbsp;사용자 관리</a>
            </li>
        </c:if>
        <li data-menu-id="data-statistics">
            <a href="#" id="sidebar-statistics" data-content-url="/contents/statistics"><i class="fas fa-chart-line"></i>&nbsp;&nbsp;데이터 통계</a>
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
