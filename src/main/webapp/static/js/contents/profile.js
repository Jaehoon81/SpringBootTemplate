// 프로필 정보 로드 및 수정 함수 --------------------------------------------------------------------------------------------
// $(document).ready(function () {  // 방법 1) 기본 방식
// (function () {                   // 방법 2) 바로 함수를 호출하는 방식 (즉시 실행 함수로 스코프를 보호)
window.initProfileEdit = function () {  // 현재 로그인한 사용자의 정보 조회 함수를 전역 스코프에 노출
    // 방법 3) function() { ... }로 감싸서 window.initProfileEdit() 형태로 내보내는 방식
    // profile_content.jsp 내부의 스크립트(<script> 태그)가 아닌,
    // 별도의 profile.js 파일로 분리했을 경우에 사용

    // 내 정보 수정 관련 요소들
    var $profileEditForm = $('#profileEditForm');
    var $username = $('#edit-username');
    var $displayname = $('#edit-displayname');
    var $email = $('#edit-email');
    var $currentPassword = $('#edit-current-password');
    var $newPassword = $('#edit-new-password');
    var $confirmPassword = $('#edit-confirm-password');
    // 내 정보 수정 에러 메시지
    var $usernameError = $('#usernameError');
    var $displaynameError = $('#displaynameError');
    var $emailError = $('#emailError');
    var $currentPasswordError = $('#currentPasswordError');
    var $newPasswordError = $('#newPasswordError');
    var $confirmPasswordError = $('#confirmPasswordError');
    var $generalError = $('#generalError');
    // 회원탈퇴 팝업창 모달 관련 요소들
    var $deactivateUserBtn = $('#deactivateUserBtn');
    var $deactivateUserModal = $('#deactivateUserModal');
    var $deactivateConfirmInput = $('#deactivateConfirmInput');
    var $deactivateConfirmBtn = $deactivateUserModal.find('.confirm-deactivate-btn');
    var $deactivateCancelBtn = $deactivateUserModal.find('.cancel-deactivate-btn');
    var $deactivateError = $('#deactivateError');

    // 현재 로그인한 사용자의 정보를 조회 ------------------------------------------------------------------------------------
    function loadUserProfile() {
        clearErrors();

        $.ajax({
            url: '/api/user/profile',
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            success: function (data) {
                $username.val(data.username);
                $displayname.val(data.displayname);
                $email.val(data.email);
                // 비밀번호 관련 필드는 항상 비워둠
                $currentPassword.val('');
                $newPassword.val('');
                $confirmPassword.val('');

                // 콘텐츠 로드 후 스크롤 상태를 확인
                if (typeof window.adjustProfileContainerWidthBasedOnScroll === 'function') {
                    window.adjustProfileContainerWidthBasedOnScroll();
                }
            },
            error: function (xhr) {
                var errorMsg = '프로필 정보 로드 실패: ' + (xhr.responseText || '알 수 없는 오류');
                try {
                    // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                    var jsonError = JSON.parse(xhr.responseText);
                    if (jsonError.message) {
                        errorMsg = '프로필 정보 로드 실패: ' + jsonError.message;
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }
                $generalError.text(errorMsg);
                alert(errorMsg);
                console.error("프로필 정보 로드 실패: ", xhr.responseText);
            }
        });
    }
    // 모든 에러 메시지의 초기화 함수
    function clearErrors() {
        $('.error-message').text('');
        $deactivateError.text('');  // 팝업창도 에러 메시지 초기화
    }

    // 프로필 정보 수정 폼의 제출 이벤트 핸들러 -------------------------------------------------------------------------------
    // $profileEditForm.submit(function (event) {  // 이벤트 위임 방식
    $profileEditForm.on('submit', function (event) {  // 직접 폼에 바인딩
        event.preventDefault();  // 기본 폼 제출 동작 방지
        clearErrors();

        var displayname = $displayname.val().trim();
        var email = $email.val().trim();
        var currentPassword = $currentPassword.val();
        var newPassword = $newPassword.val();
        var confirmPassword = $confirmPassword.val();
        var hasClientError = false;

        // 클라이언트 측 유효성 검사 시작 -----------------------------------------------------------------------------------
        // 1. 이름
        var displaynameRegex = /^[a-zA-Z가-힣0-9\s]+$/;
        if (displayname === '') {
            $displaynameError.text('이름은 필수 항목입니다.');
            hasClientError = true;
        } else if (displayname.length > 10) {
            $displaynameError.text('이름은 10자 이하로 입력해주세요.');
            hasClientError = true;
        } else if (!displaynameRegex.test(displayname)) {
            $displaynameError.text('이름은 한글, 영문 대소문자, 숫자만 가능합니다.');
            hasClientError = true;
        }
        // 2. 이메일
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (email === '') {
            $emailError.text('이메일은 필수 항목입니다.');
            hasClientError = true;
        } else if (!emailRegex.test(email)) {
            $emailError.text('유효한 이메일 형식이 아닙니다.');
            hasClientError = true;
        }
        // 3. 현재 비밀번호 (정보 변경을 위해 항상 필요)
        if (currentPassword === '') {
            $currentPasswordError.text('정보 변경을 위해 현재 비밀번호를 입력해주세요.');
            hasClientError = true;
        }
        // 4. 새 비밀번호 (입력했을 경우에만 유효성 검사 및 확인)
        var isPasswordChangeRequested = (newPassword !== '');  // 새 비밀번호의 입력 여부
        var passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/;
        if (isPasswordChangeRequested) {
            if (newPassword.length < 10) {
                $newPasswordError.text('새 비밀번호는 10자 이상이어야 합니다.');
                hasClientError = true;
            } else if (!passwordRegex.test(newPassword)) {
                $newPasswordError.text('새 비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.');
                hasClientError = true;
            }
            if (newPassword !== confirmPassword) {
                $confirmPasswordError.text('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
                hasClientError = true;
            }
        } else {  // 새 비밀번호의 입력이 없는 경우 (confirmPassword도 비워둠)
            // 서버로 전송할 때는 newPassword와 confirmPassword를 요청 JSON에 포함하지 않거나 빈 문자열로 보냄
            newPassword = "";
            confirmPassword = "";
        }
        // 유효성 검증 실패 시 메시지 표시 및 함수 종료
        if (hasClientError) {
            return;
        }
        // 클라이언트 측 유효성 검사 종료 -----------------------------------------------------------------------------------

        // 서버로 전송할 데이터 구성
        var requestData = {
            displayname: displayname,
            email: email,
            currentPassword: currentPassword  // 정보 변경을 위해서는 항상 포함해야 함
        };
        if (isPasswordChangeRequested) {  // 새 비밀번호가 입력되었을 때만 포함
            requestData.newPassword = newPassword;
            requestData.confirmPassword = confirmPassword;
        }
        $.ajax({
            url: '/api/user/profile',
            type: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function (response) {
                alert(response);  // 서버에서 보낸 성공 메시지 (text/plain)

                // 프로필 정보 수정 성공 시 새로운 정보로 화면을 다시 로드하고, 비밀번호 관련 필드는 초기화
                loadUserProfile();
            },
            error: function (xhr) {
                var errorMsg = '프로필 업데이트 실패: ' + (xhr.responseText || '알 수 없는 오류');
                var errorResponse = JSON.parse(xhr.responseText);
                // 서버에서 응답 받은 유효성 검증 에러 메시지를 표시
                if (errorResponse) {
                    for (var fieldName in errorResponse) {
                        if (errorResponse.hasOwnProperty(fieldName)) {
                            $('#' + fieldName + 'Error').text(errorResponse[fieldName]);
                        }
                    }
                } else {
                    $generalError.text('프로필 업데이트에 실패했습니다: ' + xhr.status + ' ' + xhr.statusText);
                }
                alert(errorMsg);
                console.error("프로필 업데이트 실패: ", xhr.responseText);
            }
        });
    });

    // 회원탈퇴 버튼 클릭 이벤트 핸들러 -------------------------------------------------------------------------------------
    $deactivateUserBtn.on('click', function (e) {
        e.preventDefault();  // 기본 링크 동작 방지
        clearErrors();
        $deactivateConfirmInput.val('');  // 탈퇴 입력 필드 초기화
        $deactivateUserModal.addClass('show');  // 팝업창 표시
    });

    // 팝업창 확인 버튼 클릭 이벤트
    $deactivateConfirmBtn.on('click', function () {
        clearErrors();

        if ($deactivateConfirmInput.val() === '탈퇴') {
            $.ajax({
                url: '/api/user/deactivate',
                type: 'POST',
                success: function (response) {
                    alert(response);  // 서버에서 보낸 성공 메시지 (text/plain)

                    // localStorage에 있는 메뉴 상태 등의 모든 데이터를 삭제하고, 로그인 페이지(login.jsp)로 이동
                    localStorage.clear();
                    window.location.href = '/';  // 로그아웃 후 로그인 페이지(login.jsp)로 이동
                },
                error: function (xhr) {
                    var errorMsg = '회원탈퇴 처리 실패: ' + (xhr.responseText || '알 수 없는 오류');
                    try {
                        // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                        var jsonError = JSON.parse(xhr.responseText);
                        if (jsonError.message) {
                            errorMsg = '회원탈퇴 처리 실패: ' + jsonError.message;
                        }
                    } catch (e) {
                        // JSON 파싱 실패 시 기본 메시지 사용
                    }
                    $deactivateError.text(errorMsg);
                    alert(errorMsg);
                    console.error("회원탈퇴 처리 실패: ", xhr.responseText);
                }
            });
        } else {
            $deactivateError.text("'탈퇴'를 정확하게 입력해주세요.");
        }
    });
    // 팝업창 취소 버튼 클릭 이벤트
    $deactivateCancelBtn.on('click', function () {
        $deactivateUserModal.removeClass('show');  // 팝업창 숨김
    });
    // 팝업창 외부 영역 클릭 이벤트
    $(window).click(function (event) {
        if ($(event.target).is($deactivateUserModal)) {
            $deactivateUserModal.removeClass('show');
        }
    });

    // 페이지 진입 시(DOM 로드 완료 후) 현재 로그인한 사용자의 정보를 조회
    loadUserProfile();

    // 혹시 모를 스크롤 발생 시에도 해당 컨테이너의 너비를 다시 체크 (선택 사항)
    var $contentWrapper = $('.content-wrapper');
    $contentWrapper.on('scroll', function () {
        if (typeof window.adjustProfileContainerWidthBasedOnScroll === 'function') {
            window.adjustProfileContainerWidthBasedOnScroll();
        }
    });
    // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
    // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
    $(window).on('resize', function () {
        if (typeof window.adjustProfileContainerWidthBasedOnScroll === 'function') {
            window.adjustProfileContainerWidthBasedOnScroll();
        }
    });
// });    // 방법 1)
// })();  // 방법 2)
};  // 방법 3)

// 스크롤바 유무에 따라 너비를 동적으로 설정하는 함수 ---------------------------------------------------------------------------
window.adjustProfileContainerWidthBasedOnScroll = function () {
    // 스크롤바 너비 조절 관련 요소들
    var $contentWrapper = $('.content-wrapper');
    var $profileEditContainer = $('.profile-edit-container');

    if ($profileEditContainer.length === 0 || $contentWrapper.length === 0) {
        return;
    }
    if ($contentWrapper[0].scrollHeight > $contentWrapper[0].clientHeight) {
        $profileEditContainer.addClass('scrolling');
    } else {
        $profileEditContainer.removeClass('scrolling');
    }
};
