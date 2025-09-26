<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!-- 프로필 정보 수정 폼 구조 -->
<div class="profile-edit-container">
    <h2>내 정보 수정</h2>
    <form id="profileEditForm" class="profile-edit-form"
          onkeydown="if (event.key === 'Enter' || event.keyCode === 13) { event.preventDefault(); }"><!-- 엔터키로 폼 제출을 방지 -->
        <div class="form-group">
            <label for="edit-username">아이디(고정):</label>
            <input type="text" id="edit-username" name="username" readonly>
            <span id="usernameError" class="error-message"></span>
        </div>
        <div class="form-group">
            <label for="edit-displayname">* 이름:</label>
            <input type="text" id="edit-displayname" name="displayname" maxlength="10" placeholder="이름을 입력해주세요." required>
            <span id="displaynameError" class="error-message"></span>
        </div>
        <div class="form-group">
            <label for="edit-email">* 이메일:</label>
            <input type="email" id="edit-email" name="email" placeholder="이메일 주소를 입력해주세요." required>
            <span id="emailError" class="error-message"></span>
        </div>
        <hr style="border-color: #eee;"><!-- 구분선 -->
        <div class="form-group">
            <label for="edit-current-password">* 현재 비밀번호:</label>
            <input type="password" id="edit-current-password" name="currentPassword"
                   placeholder="정보 변경을 위해 현재 비밀번호를 입력해주세요." required>
            <span id="currentPasswordError" class="error-message"></span>
        </div>
        <div class="form-group">
            <label for="edit-new-password">새 비밀번호(선택):</label>
            <input type="password" id="edit-new-password" name="newPassword"
                   placeholder="새 비밀번호를 입력해주세요."><!-- or placeholder="변경하지 않으려면 비워두세요." -->
            <span id="newPasswordError" class="error-message"></span>
        </div>
        <div class="form-group">
            <label for="edit-confirm-password">새 비밀번호 확인:</label>
            <input type="password" id="edit-confirm-password" name="confirmPassword"
                   placeholder="새 비밀번호를 한번 더 입력해주세요.">
            <span id="confirmPasswordError" class="error-message"></span>
        </div>
        <button type="submit">프로필 업데이트</button>
        <div id="generalError" class="error-message" style="color: red; text-align: center; margin-top: 15px;"></div>
    </form>
    <!-- 회원탈퇴 버튼 (USER 계정인 경우에만 표시) -->
    <c:if test="${userRole eq 'USER'}">
        <button type="button" id="deactivateUserBtn" class="deactivate-button">회원 탈퇴</button>
    </c:if>
</div>

<!-- 회원탈퇴 팝업창 모달 구조 (기존 popup-content 재사용) -->
<div id="deactivateUserModal" class="unregister-popup">
    <div class="popup-content">
        <h3>회원 탈퇴</h3>
        <p>회원을 탈퇴하시려면 아래 입력란에 '탈퇴'라고 정확히 입력해주세요.</p><br>

        <input type="text" id="deactivateConfirmInput" placeholder="탈퇴" required>
        <span id="deactivateError" class="modal-error-message"></span>
        <div class="modal-buttons">
            <button class="confirm-deactivate-btn">확인</button>
            <button class="cancel-deactivate-btn">취소</button>
        </div>
    </div>
</div>

<script type="text/javascript">
</script>
