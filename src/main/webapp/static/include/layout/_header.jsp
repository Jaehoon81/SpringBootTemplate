<!-- src/main/webapp/static/include/layout/_header.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="main-header">
    <div class="header-left">
        <a href="/dashboard" class="project-title">Spring-Boot Template</a><!-- 프로젝트 제목 -->
    </div>
    <div class="header-right">
        <p class="welcome-message">환영합니다, <span id="currentUserName">${displayName}</span>님!</p>
        <button id="logoutBtn" class="header-logout-btn">로그아웃</button>
    </div>
</header>
