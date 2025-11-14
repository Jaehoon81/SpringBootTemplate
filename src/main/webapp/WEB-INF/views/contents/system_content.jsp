<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="auth-system-container">
    <h2>관리자 계정 승인 관리</h2>
    <p><span style="font-weight: bold;"><c:out value="${displayName}"/></span>님,
        시스템 관리자 권한이므로 시스템의 모든 기능을 총괄합니다.</p>
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
</div>

<script type="text/javascript">
</script>
