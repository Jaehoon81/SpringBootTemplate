<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>회원 가입 및 로그인</title>
    <link rel="icon" type="image/x-icon" href="/favicon_01.ico"/>

    <link rel="stylesheet" type="text/css" href="/static/css/main.css">
    <!-- Font-Awesome CDN -->
<%--    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>--%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"/>
    <style>
        .capslock-tooltip {
            position: absolute;
            background-color: #ffc107; /* 경고색 (노란색 계열) */
            color: #343a40; /* 어두운 글자색 */
            border-radius: 6px;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
            width: 33%; /* 캡스락 툴팁의 너비 */
            padding: 8px 12px;
            z-index: 100; /* 다른 요소 위에 표시되도록 z-index 설정 */
            bottom: -45px; /* 비밀번호 입력 필드 아래에 배치 */
            left: 0;
            font-size: 0.85em;
            font-weight: 400;
            white-space: nowrap; /* 내용이 한 줄로 유지되도록 */
            text-align: center;
        }
        .capslock-tooltip::before {
            content: '';
            position: absolute;
            top: -5px; /* 캡스락 툴팁 위쪽 중앙에 배치 */
            left: 38px; /* 화살표 위치 조정 */
            border-width: 0 5px 5px 5px;
            border-style: solid;
            border-color: transparent transparent #ffc107 transparent; /* 화살표 모양 생성 */
        }

        .login-tooltip {
            width: 130px; /* 로그인 툴팁의 너비 */
            margin-left: -74px; /* 툴팁 너비의 절반만큼 왼쪽으로 이동하여 중앙 정렬 */
        }
    </style>
</head>
<body>
    <!-- 로그인 폼 구조 -->
    <div class="login-container">
        <h1>Spring-Boot Template</h1>
        <h2>로그인</h2>
        <div class="form-group">
            <label for="login-username">* 아이디:</label>
            <input type="text" id="login-username" name="username" placeholder="아이디를 입력해주세요." required>
        </div>
        <div class="form-group password-field-wrapper">
            <label for="login-password">* 비밀번호:</label>
            <input type="password" id="login-password" name="password" placeholder="비밀번호를 입력해주세요." required>
            <div id="capslock-tooltip" class="capslock-tooltip" style="display: none;">
                Caps Lock 키를 확인해주세요!
            </div>
        </div>
        <div class="form-group checkbox-group">
            <input type="checkbox" id="remember-id" name="remember">
            <label for="remember-id">아이디 기억하기</label>
        </div>
        <button id="loginBtn">로그인</button>
        <p id="login-message" class="message"></p>

        <!-- 시스템 관리자 로그인 안내 툴팁 -->
        <p style="margin-top: -20px; margin-bottom: 25px;">
            시스템 관리자 계정으로 로그인하려면?
            <span class="tooltip-container">
                <span class="tooltip-icon"><i class="fa-solid fa-circle-info"></i></span>
                <span class="custom-tooltip login-tooltip" data-tooltip-text="시스템 관리자 계정:&nbsp; system / 1234qwer!!"></span>
            </span>
        </p>
        <!-- 회원가입, 아이디/비밀번호 찾기 팝업창 모달의 텍스트 링크 -->
        <div class="modal-links">
            <a href="#" id="openRegisterModal">회원 가입</a>
            <span>|</span>
            <a href="#" id="openFindAccountModal">아이디/비밀번호 찾기</a>
        </div>
        <hr>
<%--        <p><a href="/secure-page" style="display: inline;">보호된 페이지로 이동</a> (로그인 후 접근 가능)</p>--%>
        <p><a href="/dashboard" style="display: inline;">대시보드로 이동</a> (로그인 후 접근 가능)</p>
    </div>

    <!-- 회원가입 팝업창 모달 구조 -->
    <div id="registerModal" class="register-modal">
        <div class="modal-content">
            <span class="close-modal">&times;</span>
            <h2>회원 가입</h2>
            <div class="form-group">
                <label for="reg-username">* 아이디:</label>
                <input type="text" id="reg-username" name="username" placeholder="4자 이상의 영문 대소문자, 숫자만 가능" required>
            </div>
            <div class="form-group">
                <label for="reg-password">* 비밀번호:</label>
                <input type="password" id="reg-password" name="password" placeholder="10자 이상의 영문, 숫자, 특수문자를 모두 포함" required>
            </div>
            <div class="form-group">
                <label for="reg-displayname">* 이름:</label>
                <input type="text" id="reg-displayname" name="displayname" maxlength="10" placeholder="한글, 영문 대소문자, 숫자만 가능" required>
            </div>
            <div class="form-group">
                <label for="reg-email">* 이메일:</label>
                <input type="email" id="reg-email" name="email" placeholder="예: yourname@domain.com" required>
            </div>
            <div class="form-group">
                <label for="reg-req-message">요청 메시지(선택):</label>
                <textarea id="reg-req-message" name="reqMessage" rows="4" placeholder="상위 관리자의 계정 승인 시 요청할 메시지를 작성합니다."></textarea>
            </div>
            <div class="form-group">
                <label for="reg-role">* 권한:</label>
                <select id="reg-role" name="role">
                    <option value="USER" selected>일반 사용자</option>
                    <option value="ADMIN">관리자</option>
                </select>
            </div>
            <div class="form-group" id="adminnameGroup">
                <label for="reg-adminname">담당 관리자(조건):</label>
                <select id="reg-adminname" name="adminname">
                    <option value="">-- 관리자 선택 --</option>
                    <!-- ADMIN 권한의 displayname 목록이 JavaScript로 여기에 채워짐 -->
                </select>
            </div>
            <button id="registerBtn">회원 가입</button>
            <p id="reg-message" class="message"></p>
        </div>
    </div>

    <!-- 아이디/비밀번호 찾기 팝업창 모달 구조 -->
    <div id="findAccountModal" class="find-account-modal">
        <div class="modal-content">
            <span class="close-modal">&times;</span>
            <h2>아이디/비밀번호 찾기</h2>
            <div class="form-group">
                <label for="find-displayname">* 이름:</label>
                <input type="text" id="find-displayname" name="displayname" maxlength="10" placeholder="이름을 입력해주세요." required>
            </div>
            <div class="form-group">
                <label for="find-email">* 이메일:</label>
                <input type="email" id="find-email" name="email" placeholder="이메일 주소를 입력해주세요." required>
            </div>
            <button id="findAccountBtn">이메일 전송</button>
            <p id="find-message" class="message"></p>
        </div>
    </div>

    <!-- jQuery CDN -->
<%--    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>--%>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"
            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>

    <script type="text/javascript" src="/static/js/login.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            const loginPassword = $('#login-password');
            const capslockTooltip = $('#capslock-tooltip');

            // Caps Lock ON 조건:
            // - Caps Lock 키 자체가 토글됐을 때
            // - Shift 키가 눌리고 대문자가 입력되려 할 때
            loginPassword.on('keydown', function (event) {
                const keyCode = event.keyCode || event.which;
                const isShift = event.shiftKey || (event.modifiers && (event.modifiers & 4));

                if (keyCode >= 32 && keyCode <= 126) {  // 영문(한글), 숫자, 특수문자 입력의 경우
                    if (isShift) {  // 현재 Shift 키 눌림 (예상 입력: 대문자, Caps Lock ON)
                        capslockTooltip.show();
                    } else {  // Shift 키 안 눌림 (예상 입력: 소문자, Caps Lock OFF)
                        capslockTooltip.hide();
                    }
                } else {  // 그 외의 다른 키가 눌렸을 경우
                    if (keyCode === 20) {  // Caps Lock 키 자체를 토글
                        if (capslockTooltip.is(':visible')) {
                            capslockTooltip.hide();
                        } else {  // capslockTooltip.is(':hidden')
                            capslockTooltip.show();
                        }
                    }
                }
            });
            // 비밀번호 입력 필드에서 포커스를 잃었을 때 툴팁 숨기기 (선택 사항)
            loginPassword.on('blur', function () {
                capslockTooltip.hide();
            });

            $('.tooltip-container').each(function () {
                const customTooltip = $(this).find('.custom-tooltip');
                var tooltipText = customTooltip.data('tooltip-text');

                // data-tooltip-text 속성의 내용을 읽어서 커스텀 툴팁 안에 채워넣음
                customTooltip.text(tooltipText);
            });
        });
    </script>
</body>
</html>
