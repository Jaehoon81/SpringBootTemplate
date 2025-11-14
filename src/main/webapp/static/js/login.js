$(document).ready(function () {
    // 아이디 기억하기 기능 관련 -------------------------------------------------------------------------------------------
    // 쿠키 설정: 이름, 값, 유효 기간(일)
    function setCookie(name, value, days) {
        var expires = "";
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + (value || "") + expires + "; path=/";
    }
    // 쿠키 읽기: 이름
    function getCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) === ' ') {
                c = c.substring(1, c.length);  // 공백 제거
            }
            if (c.indexOf(nameEQ) === 0) {
                return c.substring(nameEQ.length, c.length);
            }
        }
        return null;
    }
    // 쿠키 삭제: 이름
    function deleteCookie(name) {
        document.cookie = name + '=; Max-Age=-99999999; path=/';  // Max-Age를 음수로 설정하여 즉시 만료
    }
    // 페이지 로드 시 아이디 기억하기 기능 적용
    var savedUsername = getCookie('savedUsername');
    if (savedUsername) {
        $('#login-username').val(savedUsername);  // 쿠키에서 아이디를 읽어와 입력란에 채우기
        $('#remember-id').prop('checked', true);  // 체크 박스는 자동으로 체크
    }
    // 아이디 기억하기 기능 관련 -------------------------------------------------------------------------------------------

    // 로그인 입력 필드의 키업 이벤트 리스너 ---------------------------------------------------------------------------------
    // 로그인 폼 구조의 아이디, 비밀번호 입력 필드에 키업 이벤트 리스너 추가
    $('#login-username, #login-password').on('keyup', function (e) {
        if (e.key === 'Enter' || e.keyCode === 13) {
            // Enter 키가 눌렸을 때 로그인 버튼 클릭 이벤트 발생
            $('#loginBtn').trigger('click');
        }
    });
    // 로그인 입력 필드의 키업 이벤트 리스너 ---------------------------------------------------------------------------------

    // 로그인 버튼 클릭 이벤트 ---------------------------------------------------------------------------------------------
    $('#loginBtn').click(function (event) {
        event.preventDefault();  // 기본 폼 제출 동작 방지
        $('#login-message').text('');  // 이전 메시지 초기화

        var username = $('#login-username').val();
        var password = $('#login-password').val();
        var rememberIdChecked = $('#remember-id').is(':checked');  // '아이디 기억하기' 체크박스 상태 확인

        if (!username || !password) {
            $('#login-message').text('아이디와 비밀번호를 모두 입력해주세요.').css('color', 'red');
            return;
        }
        $.ajax({
            url: '/api/auth/web-login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ username: username, password: password }),
            success: function (response) {
                // 로그인 성공 후 '아이디 기억하기' 처리
                if (rememberIdChecked) {
                    setCookie('savedUsername', username, 30);  // 30일 동안 아이디 저장
                } else {
                    deleteCookie('savedUsername');  // 체크 해제 시 아이디 삭제
                }
                // 로그인 성공 시 JWT는 HttpOnly 쿠키로 서버가 설정하므로 여기서는 별도로 localStorage에 저장할 필요 없음
                // localStorage.setItem('jwtToken', response.jwt);
                // 로그인 성공 시 localStorage의 메뉴 ID를 삭제
                localStorage.removeItem('lastActiveMenuId');

                // 웹 로그인 성공 시, 백엔드에서 쿠키를 설정하므로 별도의 응답 데이터 파싱이 필요 없음
                // $('#login-message').text('로그인 성공!').css('color', 'green');
                $('#login-message').text(response).css('color', 'green');  // "로그인(웹) 성공" 메시지
                $('#login-username').val('');
                $('#login-password').val('');
                // 보호된 페이지로 리디렉션
                // window.location.href = '/secure-page';
                window.location.href = '/dashboard';
            },
            error: function (xhr) {
                var errorMsg = '로그인 실패: ' + (xhr.responseText || '알 수 없는 오류');
                if (xhr.responseJSON) {  // 백엔드에서 에러 메시지가 JSON으로 반환되는 경우
                    if (xhr.responseJSON.message) {
                        errorMsg = '로그인 실패: ' + xhr.responseJSON.message;
                    } else if (typeof xhr.responseJSON === 'string') {
                        errorMsg = '로그인 실패: ' + xhr.responseJSON;
                    }
                }
                $('#login-message').text(errorMsg).css('color', 'red');
                console.error("로그인 실패: ", xhr.responseText);
            }
        });
    });

    // 회원가입 팝업창 모달 기능 관련 ---------------------------------------------------------------------------------------
    // 모달 관련 DOM 요소 가져오기
    var registerModal = $('#registerModal');
    var openRegisterModalBtn = $('#openRegisterModal');
    var closeModalBtn = $('.close-modal');
    var regDisplaynameInput = $('#reg-displayname');
    var regEmailInput = $('#reg-email');
    var regReqMessage = $('#reg-req-message');
    var regRoleSelect = $('#reg-role');
    var adminnameGroup = $('#adminnameGroup');
    var regAdminnameSelect = $('#reg-adminname');
    var regMessage = $('#reg-message');

    // 회원가입 링크 클릭 시 팝업창 열기
    openRegisterModalBtn.click(function (e) {
        e.preventDefault();  // 기본 링크 동작 방지
        registerModal.addClass('show');
        regMessage.text('').removeClass('success-message error-message');  // 팝업창을 열 때 메시지 초기화

        // 모달 내부 Input/Select 등의 필드들도 초기화 (선택 사항)
        $('#reg-username').val('');
        $('#reg-password').val('');
        regDisplaynameInput.val('');
        regEmailInput.val('');
        regReqMessage.val('');
        regRoleSelect.val('USER');  // 기본값: USER
        regAdminnameSelect.val('');

        // 팝업창이 열릴 때 ADMIN 권한의 displayname 목록을 로드
        loadAdminNames();
        // 권한에 따라 필드(드롭다운) 상태 초기화 (USER가 선택되어 있으므로 활성화)
        toggleAdminnameField();
    });
    // 닫기 버튼 클릭 시 팝업창 닫기
    closeModalBtn.click(function () {
        registerModal.removeClass('show');
    });
    // 모달 외부 클릭 시 팝업창 닫기 (이벤트 버블링 방지 포함)
    $(window).click(function (event) {
        if ($(event.target).is(registerModal)) {
            // registerModal.removeClass('show');  // 모달 배경 클릭 시 안닫히도록 주석처리
        }
    });

    // ADMIN 권한의 Displaynames 드롭다운 로직
    function loadAdminNames() {
        $.ajax({
            url: '/api/auth/admins',
            type: 'GET',
            success: function (data) {
                regAdminnameSelect.empty();  // 기존 옵션 비우기
                regAdminnameSelect.append($('<option></option>').val('').text('-- 관리자 선택 --'));  // 기본 옵션 추가

                $.each(data, function (i, displayname) {
                    regAdminnameSelect.append($('<option>', {
                        value: displayname,
                        text: displayname
                    }));
                });
            },
            error: function (xhr) {
                alert('관리자 이름 로드 중 오류가 발생했습니다.');
                console.error("ADMIN 권한의 Displaynames 로드 실패: ", xhr.responseText);
            }
        });
    }
    // 권한 선택에 따른 담당 관리자 필드(드롭다운)의 표시/숨김 및 활성화/비활성화 로직
    function toggleAdminnameField() {
        if (regRoleSelect.val() === 'USER') {
            adminnameGroup.show();  // 표시
            regAdminnameSelect.prop('disabled', false);  // 활성화
        } else {  // ADMIN 또는 기타
            // adminnameGroup.hide();  // 숨김
            regAdminnameSelect.prop('disabled', true);  // 비활성화
            regAdminnameSelect.val('');  // 옵션값 초기화
        }
    }
    // 권한 드롭다운 변경 시 이벤트 리스너 추가
    regRoleSelect.change(toggleAdminnameField);
    // 회원가입 팝업창 모달 기능 관련 ---------------------------------------------------------------------------------------

    // 회원가입 버튼 클릭 이벤트 (모달 내부의 필드 사용) -----------------------------------------------------------------------
    $('#registerBtn').click(function (event) {
        event.preventDefault();  // 기본 폼 제출 동작 방지
        regMessage.text('').removeClass('success-message error-message');  // 이전 메시지 초기화

        var username = $('#reg-username').val();
        var password = $('#reg-password').val();
        var displayname = regDisplaynameInput.val();  // 화면에 표시할 이름
        var email = regEmailInput.val();              // 이메일 주소
        var reqMessage = regReqMessage.val();         // 요청 메시지
        var role = regRoleSelect.val();               // SYSTEM, ADMIN, USER 등의 권한
        var adminname = regAdminnameSelect.val();     // 담당 관리자 이름

        // 클라이언트 측 유효성 검사 시작 -----------------------------------------------------------------------------------
        var message = '';

        // 아이디 유효성 검사
        // 4자 이상 20자 이하, 영문 대소문자/숫자 포함 정규식 (빈 값은 !username에서 처리)
        var usernameRegex = /^[a-zA-Z0-9]*$/;
        if (!username) {
            message = '아이디는 필수 입력 값입니다.';
        } else if (!usernameRegex.test(username)) {
            message = '아이디는 영문 대소문자, 숫자만 가능합니다.';
        } else if (username.length < 4 || username.length > 20) {
            message = '아이디는 4자 이상 20자 이하로 입력해야 합니다.';
        }
        // 비밀번호 유효성 검사
        // 10자 이상, 영문/숫자/특수문자 포함 정규식 (자바스크립트 정규식은 \ 하나만)
        var passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?~])[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?~]{10,}$/;
        if (!message && !password) {
            message = '비밀번호는 필수 입력 값입니다.';
        } else if (!message && !passwordRegex.test(password)) {
            message = '비밀번호는 10자 이상의 영문, 숫자, 특수문자를 모두 포함해야 합니다.';
        }
        // 이름 유효성 검사
        // 10자 이하, 한글/영문 대소문자/숫자 포함 정규식
        var displaynameRegex = /^[a-zA-Z가-힣0-9\s]+$/;
        if (!message && !displayname) {
            message = '이름은 필수 입력 값입니다.';
        } else if (!message && !displaynameRegex.test(displayname)) {
            message = '이름은 한글, 영문 대소문자, 숫자만 가능합니다.';
        } else if (!message && displayname.length > 10) {
            message = '이름은 10자 이하로 입력해야 합니다.';
        }
        // 이메일 유효성 검사
        // '@' 유무 등 기본적인 이메일 정규식
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!message && !email) {
            message = '이메일은 필수 입력 값입니다.';
        } else if (!message && !emailRegex.test(email)) {
            message = '유효한 이메일 형식이 아닙니다.';
        }
        // 권한 필수여부 체크
        if (!message && !role) {
            message = '권한은 필수 입력 값입니다.';
        }
        // 담당 관리자 필드(드롭다운)의 조건부 필수여부 체크 (USER 권한 선택 시)
        if (!message && role === 'USER' && !adminname) {
            message = '일반 사용자 선택 시 담당 관리자를 선택해야 합니다.';
        }

        // if (!username || !password || !role) {
        //     $('#reg-message').text('아이디, 비밀번호, 권한을 모두 입력해주세요.').css('color', 'red');
        //     return;
        // }
        // 유효성 검사 실패 시 메시지 표시 및 함수 종료
        if (message) {
            regMessage.text(message).addClass('error-message');
            return;
        }
        // 클라이언트 측 유효성 검사 종료 -----------------------------------------------------------------------------------

        $.ajax({
            url: '/api/auth/register',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                username: username,
                password: password,
                displayname: displayname,
                email: email,
                reqMessage: reqMessage,
                role: role,
                adminname: adminname
            }),
            success: function (response) {
                // $('#reg-message').text('회원가입 성공: ' + response).css('color', 'green');
                regMessage.text('✅ ' + response).addClass('success-message');  // 백엔드에서 반환된 성공 메시지 표시
                $('#reg-username').val('');
                $('#reg-password').val('');
                regDisplaynameInput.val('');
                regEmailInput.val('');
                regReqMessage.val('');
                regRoleSelect.val('USER');  // 기본값: USER
                regAdminnameSelect.val('');
                // 회원가입 성공 시 팝업창 닫기
                registerModal.removeClass('show');

                // 성공 시 필드 초기화 후 드롭다운 상태 다시 조정
                toggleAdminnameField();
            },
            error: function (xhr) {
                var errorMsg = '회원가입 실패: ';
                if (xhr.responseJSON) {  // 백엔드에서 FieldError 형식의 Map을 반환하는 경우
                    for (var key in xhr.responseJSON) {
                        if (xhr.responseJSON.hasOwnProperty(key)) {
                            // 필드명(key)이 다를 수 있으므로 value만 가져옴
                            errorMsg += (xhr.responseJSON[key] + ' ');
                            break;  // 첫 번째 에러만 표시하려면 break 활성화
                        }
                    }
                } else {
                    errorMsg += (xhr.responseText || '알 수 없는 오류');
                }
                regMessage.text('❌ ' + errorMsg).addClass('error-message');  // 오류 메시지 표시
                console.error("회원가입 실패: ", xhr.responseText);
            }
        });
    });

    // 아이디/비밀번호 찾기 팝업창 모달 기능 관련 -----------------------------------------------------------------------------
    // 모달 관련 DOM 요소 가져오기
    var $findAccountModal = $('#findAccountModal');
    var $openFindAccountModal = $('#openFindAccountModal');
    var $closeFindAccountModal = $findAccountModal.find('.close-modal');
    var $findDisplayname = $('#find-displayname');
    var $findEmail = $('#find-email');
    var $findAccountBtn = $('#findAccountBtn');
    var $findMessage = $('#find-message');

    // 아이디/비밀번호 찾기 링크 클릭 시 팝업창 열기
    $openFindAccountModal.on('click', function (e) {
        e.preventDefault();  // 기본 링크 동작 방지
        $findAccountModal.addClass('show');
        $findMessage.text('').removeClass('success-message error-message');  // 팝업창을 열 때 메시지 초기화

        // 모달 내부 Input 등의 필드들도 초기화 (선택 사항)
        $findDisplayname.val('');
        $findEmail.val('');
    });
    // 닫기 버튼 클릭 시 팝업창 닫기
    $closeFindAccountModal.on('click', function () {
        $findAccountModal.removeClass('show');
    });
    // 모달 외부 클릭 시 팝업창 닫기 (이벤트 버블링 방지 포함)
    $(window).click(function (event) {
        if ($(event.target).is($findAccountModal)) {
            // $findAccountModal.removeClass('show');  // 모달 배경 클릭 시 안닫히도록 주석처리
        }
    });
    // 아이디/비밀번호 찾기 팝업창 모달 기능 관련 -----------------------------------------------------------------------------

    // 이메일 전송 버튼 클릭 이벤트 (모달 내부의 필드 사용) --------------------------------------------------------------------
    $findAccountBtn.on('click', function (event) {
        event.preventDefault();  // 기본 폼 제출 동작 방지
        $findMessage.text('').removeClass('success-message error-message');  // 이전 메시지 초기화

        var displayname = $findDisplayname.val().trim();
        var email = $findEmail.val().trim();
        var hasClientError = false;

        // 클라이언트 측 유효성 검사 시작 -----------------------------------------------------------------------------------
        // 이름 유효성 검사
        if (displayname === '') {
            $findMessage.text('이름은 필수 항목입니다.').addClass('error-message');
            hasClientError = true;
        }
        // 이메일 유효성 검사
        // '@' 유무 등 기본적인 이메일 정규식
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!hasClientError && email === '') {
            $findMessage.text('이메일은 필수 항목입니다.').addClass('error-message');
            hasClientError = true;
        } else if (!hasClientError && !emailRegex.test(email)) {
            $findMessage.text('유효한 이메일 형식이 아닙니다.').addClass('error-message');
            hasClientError = true;
        }
        // 유효성 검사 실패 시 메시지 표시 및 함수 종료
        if (hasClientError) {
            return;
        }
        // 클라이언트 측 유효성 검사 종료 -----------------------------------------------------------------------------------

        $.ajax({
            url: '/api/auth/find-account',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                displayname: displayname,
                email: email
            }),
            success: function (response) {
                $findMessage.text('✅ ' + response).addClass('success-message');  // 백엔드에서 반환된 성공 메시지 표시
                $findDisplayname.val('');
                $findEmail.val('');
                // 이메일 전송 성공 시 alert 메시지 표시 및 팝업창 닫기
                alert('아이디와 임시 비밀번호가 이메일로 전송되었습니다.');
                $findAccountModal.removeClass('show');
            },
            error: function (xhr) {
                var errorMsg = '계정찾기 실패: ';
                var errorResponse = JSON.parse(xhr.responseText);
                // 서버에서 응답 받은 유효성 검증 에러 메시지를 표시
                if (errorResponse.general) {  // 일반적인 오류 (이름 or 이메일 주소 불일치 등)
                    errorMsg += errorResponse.general;
                } else {  // 필드별 에러 또는 알 수 없는 오류
                    errorMsg += (errorResponse.displayname || '') + (errorResponse.email || '') + ' 알 수 없는 오류';
                }
                $findMessage.text('❌ ' + errorMsg).addClass('error-message');  // 오류 메시지 표시
                console.error("계정찾기 실패: ", xhr.responseText);
            }
        });
    });
});

// AJAX 요청 시 모든 요청 헤더에 JWT 토큰을 포함하는 함수 (Spring Security가 인증된 요청으로 인식하도록 함)
// -> 사용하지 않음: JWT가 HttpOnly 쿠키로 전송되므로 브라우저가 자동으로 처리
// $.ajaxSetup({
//     beforeSend: function (xhr) {
//         var token = localStorage.getItem('jwtToken');
//         if (token) {
//             xhr.setRequestHeader('Authorization', 'Bearer ' + token);
//         }
//     }
// });
