<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="auth-admin-container">
    <h2>일반 사용자 계정 승인 관리</h2>
    <p><span style="font-weight: bold;"><c:out value="${displayName}"/></span>님,
        관리자 권한으로 모든 기능을 사용할 수 있습니다.</p>
    <p>(아래는 승인 대기 중인 일반 사용자 계정의 목록입니다.)</p>

    <div id="user-list-container">
        <table>
            <thead>
                <tr>
                    <th>번호</th>
                    <th>아이디</th>
                    <th>이름</th>
                    <th>이메일</th>
                    <th>요청 메시지</th>
<%--                    <th>담당 관리자</th>--%>
                    <th>가 &middot; 부</th>
                </tr>
            </thead>
            <tbody id="user-list-body">
                <!-- USER 계정의 목록이 JavaScript로 여기에 동적으로 로드됨 -->
                <tr>
<%--                    <td colspan="7" style="text-align: center;">승인 대기 중인 일반 사용자 계정이 없습니다.</td>--%>
                    <td colspan="6" style="text-align: center;">승인 대기 중인 일반 사용자 계정이 없습니다.</td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<script type="text/javascript">
</script>
