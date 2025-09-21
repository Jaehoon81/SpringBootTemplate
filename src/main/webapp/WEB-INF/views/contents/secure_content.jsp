<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<style>
    .auth-secure-container {
        background-color: #ffffff;
        padding: 40px;
        border-radius: 12px;
        /*box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);*/
        width: 1200px;
        max-width: 90%;
        text-align: center;
        box-sizing: border-box;
    }

    /* 페이지 하단 버튼 그룹 스타일 */
    /*.auth-secure-container > button {*/
    /*    margin: 15px 8px; !* 하단 버튼 마진 조정 *!*/
    /*}*/
</style>

<div class="auth-secure-container">
    <h2>계정 승인 안내</h2>
    <p><c:out value="${displayName}"/>님, 환영합니다!</p>
    <p>이 페이지는 인증된 사용자만 접근할 수 있습니다.</p>

    <p style="color: #007bff;"><!-- 권한에 따른 메시지 표시 -->
        <c:choose>
            <c:when test="${userRole eq 'USER'}">
                <!-- 이제 secure_content.jsp는 USER 권한 전용 페이지로 사용됨 -->
                (일반 사용자 권한으로 로그인하셨습니다. 계정 승인 작업은 시스템 관리자 또는 관리자 계정에서 진행됩니다.)
            </c:when>
            <c:otherwise>
                (알 수 없는 권한입니다.)
            </c:otherwise>
        </c:choose>
    </p>
</div>
