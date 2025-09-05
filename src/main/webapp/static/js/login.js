$(document).ready(function () {
    // 모달 관련 DOM 요소 가져오기
    var registerModal = $('#registerModal');
    var openRegisterModalBtn = $('#openRegisterModal');
    var closeButton = $('.close-button');
    var regMessage = $('#reg-message');

    // 회원가입 링크 클릭 시 모달 열기
    openRegisterModalBtn.click(function (e) {
        e.preventDefault();  // 기본 링크 동작 방지

        registerModal.addClass('show');
        regMessage.text('');  // 모달 열 때 메시지 초기화
        // 모달 내부 input 필드들도 초기화 (선택 사항)
        $('#reg-username').val('');
        $('#reg-password').val('');
        $('#reg-displayname').val('');
        $('#reg-role').val('USER');
    });
    // 닫기 버튼 클릭 시 모달 닫기
    closeButton.click(function () {
        registerModal.removeClass('show');
    });
    // 모달 외부 클릭 시 모달 닫기 (이벤트 버블링 방지 포함)
    $(window).click(function (event) {
        if ($(event.target).is(registerModal)) {
            // registerModal.removeClass('show');  // 모달 배경 클릭 시 안닫히도록 주석처리
        }
    });

    // 로그인 폼 구조의 아이디, 비밀번호 입력 필드에 키업 이벤트 리스너 추가
    $('#login-username, #login-password').on('keyup', function (e) {
        if (e.key === 'Enter' || e.keyCode === 13) {
            // Enter 키가 눌렸을 때 로그인 버튼 클릭 이벤트 발생
            $('#loginBtn').trigger('click');
        }
    });

    // 로그인 버튼 클릭 이벤트
    $('#loginBtn').click(function () {
        var username = $('#login-username').val();
        var password = $('#login-password').val();

        if (!username || !password) {
            $('#login-message').text('아이디와 비밀번호를 모두 입력해주세요.').css('color', 'red');
            return;
        }

        $.ajax({
            url: '/api/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ username: username, password: password }),
            success: function (response) {
                // 로그인 성공 시 JWT는 HttpOnly 쿠키로 서버가 설정하므로 여기서는 별도로 localStorage에 저장할 필요 없음
                // localStorage.setItem('jwtToken', response.jwt);

                // $('#login-message').text('로그인 성공!').css('color', 'green');
                $('#login-message').text(response).css('color', 'green');
                $('#login-username').val('');
                $('#login-password').val('');
                // 보호된 페이지로 리디렉션
                window.location.href = '/secure-page';
            },
            error: function (xhr) {
                var errorMsg = '로그인 실패: ' + (xhr.responseText || '알 수 없는 오류');
                $('#login-message').text(errorMsg).css('color', 'red');
            }
        });
    });

    // 회원가입 버튼 클릭 이벤트 (모달 내 필드 사용)
    $('#registerBtn').click(function () {
        var username = $('#reg-username').val();
        var password = $('#reg-password').val();
        var displayname = $('#reg-displayname').val();
        var role = $('#reg-role').val();

        // ---------------------------------- 클라이언트 사이드 유효성 검사 시작 ----------------------------------
        var message = '';

        // 아이디 유효성 검사
        // 영문 대소문자, 숫자만 허용 (빈 값은 !username에서 처리)
        var usernameRegex = /^[a-zA-Z0-9]*$/;
        if (!username) {
            message = '아이디는 필수 입력 값입니다.';
        } else if (!usernameRegex.test(username)) {
            message = '아이디는 영문 대소문자, 숫자만 가능합니다.';
        } else if (username.length < 4 || username.length > 20) {
            message = '아이디는 4자 이상 20자 이하로 입력해야 합니다.';
        }
        // 비밀번호 유효성 검사
        // 최소 10자, 영문/숫자/특수문자 포함 (자바스크립트 정규식은 \ 하나만)
        var passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?~])[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?~]{10,}$/;
        if (!message && !password) {
            message = '비밀번호는 필수 입력 값입니다.';
        } else if (!message && !passwordRegex.test(password)) {
            message = '비밀번호는 10자 이상의 영문, 숫자, 특수문자를 모두 포함해야 합니다.';
        }
        // 권한 필수 체크
        if (!message && !role) {
            message = '권한은 필수 입력 값입니다.';
        }

        // if (!username || !password || !role) {
        //     $('#reg-message').text('아이디, 비밀번호, 권한을 모두 입력해주세요.').css('color', 'red');
        //     return;
        // }
        // 유효성 검사 실패 시 메시지 표시 및 함수 종료
        if (message) {
            regMessage.text(message).css('color', 'red');
            return;
        }
        // ---------------------------------- 클라이언트 사이드 유효성 검사 종료 ----------------------------------

        $.ajax({
            url: '/api/auth/register',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                username: username,
                password: password,
                displayname: displayname,
                role: role
            }),
            success: function (response) {
                // $('#reg-message').text('회원가입 성공: ' + response).css('color', 'green');
                regMessage.text(response).css('color', 'green');
                $('#reg-username').val('');
                $('#reg-password').val('');
                $('#reg-displayname').val('');
                $('#reg-role').val('USER');
                // 회원가입 성공 시 모달 닫기
                registerModal.removeClass('show');
            },
            error: function (xhr) {
                var errorMsg = '회원가입 실패: ';
                if (xhr.responseJSON) {  // 백엔드에서 FieldError 형식의 Map을 반환하는 경우
                    for (var key in xhr.responseJSON) {
                        if (xhr.responseJSON.hasOwnProperty(key)) {
                            errorMsg += (xhr.responseJSON[key] + ' ');
                            break;  // 첫 번째 에러만 표시하려면 break 활성화
                        }
                    }
                } else {
                    errorMsg += (xhr.responseText || '알 수 없는 오류');
                }
                regMessage.text(errorMsg).css('color', 'red');
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
