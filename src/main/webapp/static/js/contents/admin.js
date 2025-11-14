// 승인 대기 중인 USER 계정의 목록을 로드하는 함수 ----------------------------------------------------------------------------
// $(document).ready(function () {  // 방법 1) 기본 방식
// (function () {                   // 방법 2) 바로 함수를 호출하는 방식 (즉시 실행 함수로 스코프를 보호)
window.loadPendingUsers = function () {  // ADMIN 계정용 USER 목록 로드 함수를 전역 스코프에 노출
    // 방법 3) function() { ... }로 감싸서 window.loadPendingUsers() 형태로 내보내는 방식
    // admin_content.jsp 내부의 스크립트(<script> 태그)가 아닌,
    // 별도의 admin.js 파일로 분리했을 경우에 사용

    console.log("Loading pending users for ADMIN role...");
    $.ajax({
        url: '/api/admin/pending-users',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
        },
        success: function (data) {
            var tbody = $('#user-list-body');
            tbody.empty();  // 기존 목록 비우기

            if (data && data.length > 0) {
                $.each(data, function (index, user) {
                    // truncateMessage 함수는 dashboard.jsp의 window.truncateMessage를 사용
                    var fullMessage = user.reqMessage || '';  // 요청 메시지가 없을 경우 빈칸
                    var truncatedMessage = window.truncateMessage(fullMessage, 30);  // 30자 기준으로 줄임

                    // 테이블 행 생성
                    var row = '<tr data-id="' + user.id + '">' +
                        '<td>' + (index + 1) + '</td>' +  // 순서 번호 (index + 1)
                        '<td>' + user.username + '</td>' +
                        '<td>' + user.displayname + '</td>' +
                        '<td>' + user.email + '</td>' +
                        // 클릭 이벤트를 추가하고, 전체 텍스트는 data-full-text 속성에 저장
                        '<td><span class="truncated-message" data-full-text="' + fullMessage.replace(/"/g, '&quot;') + '">' + truncatedMessage + '</span></td>' +
                        // '<td>' + (user.assignedAdminName || '-') + '</td>' +  // 담당 관리자 이름 표시
                        '<td><button class="approve-btn" data-id="' + user.id + '">승인</button></td>' +
                        '</tr>';
                    tbody.append(row);
                });
            } else {
                // 목록이 없을 경우 메시지 표시
                // tbody.append('<tr><td colspan="7" style="text-align: center;">승인 대기 중인 일반 사용자 계정이 없습니다.</td></tr>');
                tbody.append('<tr><td colspan="6" style="text-align: center;">승인 대기 중인 일반 사용자 계정이 없습니다.</td></tr>');
            }
            // 데이터 로드 후 스크롤바 조정을 위한 전역(window) 함수 호출
            // if (typeof window.checkScrollbarAndAdjustWidth === 'function') {
            //     window.checkScrollbarAndAdjustWidth();
            // }
            if (typeof window.adjustAdminContainerWidthBasedOnScroll === 'function') {
                window.adjustAdminContainerWidthBasedOnScroll();
            }
        },
        error: function (xhr) {
            var errorMsg = 'USER 계정의 목록 로드 실패: ' + (xhr.responseText || '알 수 없는 오류');
            try {
                // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                var jsonError = JSON.parse(xhr.responseText);
                if (jsonError.message) {
                    errorMsg = 'USER 계정의 목록 로드 실패: ' + jsonError.message;
                }
            } catch (e) {
                // JSON 파싱 실패 시 기본 메시지 사용
            }
            // $('#user-list-body').html('<tr><td colspan="7" style="color: red; text-align: center;">' + errorMsg + '</td></tr>');
            $('#user-list-body').html(
                '<tr><td colspan="6" style="color: red; text-align: center;">' + errorMsg + '</td></tr>'
            );
            alert(errorMsg);
            console.error("USER 계정의 목록 로드 실패: ", xhr.responseText);
        }
    });

    // 혹시 모를 스크롤 발생 시에도 해당 컨테이너의 너비를 다시 체크 (선택 사항)
    var $contentWrapper = $('.content-wrapper');
    $contentWrapper.on('scroll', function () {
        if (typeof window.adjustAdminContainerWidthBasedOnScroll === 'function') {
            window.adjustAdminContainerWidthBasedOnScroll();
        }
    });
    // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
    // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
    $(window).on('resize', function () {
        if (typeof window.adjustAdminContainerWidthBasedOnScroll === 'function') {
            window.adjustAdminContainerWidthBasedOnScroll();
        }
    });
// });    // 방법 1)
// })();  // 방법 2)
};  // 방법 3)

// 스크롤바 유무에 따라 너비를 동적으로 설정하는 함수 ---------------------------------------------------------------------------
window.adjustAdminContainerWidthBasedOnScroll = function () {
    // 스크롤바 너비 조절 관련 요소들
    var $contentWrapper = $('.content-wrapper');
    var $authAdminContainer = $('.auth-admin-container');

    if ($authAdminContainer.length === 0 || $contentWrapper.length === 0) {
        return;
    }
    if ($contentWrapper[0].scrollHeight > $contentWrapper[0].clientHeight) {
        $authAdminContainer.addClass('scrolling');
    } else {
        $authAdminContainer.removeClass('scrolling');
    }
};

$(document).ready(function () {
    // 승인 버튼 클릭 이벤트 ----------------------------------------------------------------------------------------------
    // (동적으로 생성되는 버튼에 이벤트를 위임(바인딩)하기 위해 $(document).off().on() 사용)
    $(document).off('click', '#main-content-area .approve-btn')
               .on('click', '#main-content-area .approve-btn', function () {
        var userId = $(this).data('id');  // data-id 속성에서 USER ID 가져오기
        var button = $(this);  // 클릭된 버튼 요소 참조

        // userId가 null이거나 정의되지 않았을 경우에 대한 방어 로직
        if (userId == null) {  // null 또는 undefined를 모두 확인
            alert("오류 발생: 승인할 USER ID를 찾을 수 없습니다. (클라이언트 측)");
            console.error("Error: User ID is null/undefined when button clicked.");
            return;  // 함수 실행 중단
        }
        $.ajax({
            url: '/api/admin/approve-user',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ userId: userId }),  // JSON 형태로 데이터 전송
            success: function (response) {
                alert(response);  // 서버에서 보낸 성공 메시지 (text/plain)

                // 승인된 행을 목록에서 제거
                button.closest('tr').remove();
                // 목록을 새로고침하여 최신 상태 반영 (순서 번호 재정렬)
                window.loadPendingUsers();
            },
            error: function (xhr) {
                var errorMsg = 'USER 계정의 승인 처리 실패: ' + (xhr.responseText || '알 수 없는 오류');
                try {
                    // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                    var jsonError = JSON.parse(xhr.responseText);
                    if (jsonError.message) {
                        errorMsg = 'USER 계정의 승인 처리 실패: ' + jsonError.message;
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }
                alert(errorMsg);
                console.error("USER 계정의 승인 처리 실패: ", xhr.responseText);
            }
        });
    });

    // 페이지 진입 시(DOM 로드 완료 후) 승인 대기 중인 USER 계정의 목록을 로드
    // window.loadPendingUsers();
});
