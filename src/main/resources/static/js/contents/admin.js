$(document).ready(function () {
    var fullTextMsgModal = $('#fullTextMsgModal');
    var fullTextMsgContent = $('#fullTextMsgContent');
    var closePopupBtn = $('.close-popup');
    var adminname = '<c:out value="${displayName}"/>';  // 로그인한 ADMIN 계정의 이름을 가져옴

    // 스크롤바 유무에 따라 CSS 변수 '--scrollbar-width'를 동적으로 설정하는 함수 -----------------------------------------------
    function checkScrollbarAndAdjustWidth() {
        var tbody = $('#user-list-body')[0];  // tbody의 DOM 요소를 직접 참조
        if (tbody) {
            // tbody의 scrollHeight(실제 콘텐츠 높이)가 clientHeight(보이는 영역 높이)보다 크면 스크롤바가 필요함
            var hasScrollbar = tbody.scrollHeight > tbody.clientHeight;
            var tableElement = $('#user-list-container table')[0];  // table의 DOM 요소를 직접 참조
            if (tableElement) {
                var scrollbarWidth = (hasScrollbar) ? '8px' : '0px';
                tableElement.style.setProperty('--scrollbar-width', scrollbarWidth);
            }
        }
    }
    // 텍스트를 줄이고 '...'을 붙이는 함수 ----------------------------------------------------------------------------------
    function truncateMessage(text, maxLength) {
        if (text && text.length > maxLength) {
            return text.substring(0, maxLength) + '...';
        }
        return text;
    }

    // 승인 대기 중인 USER 계정의 목록을 로드하는 함수 ------------------------------------------------------------------------
    function loadPendingUsers() {
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
                        var fullMessage = user.reqMessage || '';  // 요청 메시지가 없을 경우 빈칸
                        var truncatedMessage = truncateMessage(fullMessage, 30);  // 30자 기준으로 줄임

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
                // 데이터 로드 후 스크롤바 조정을 위한 함수 호출
                if (typeof checkScrollbarAndAdjustWidth === 'function') {
                    checkScrollbarAndAdjustWidth();
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
                // 정렬이 깨지는 것을 방지하기 위해 에러 발생 시에도 스크롤바 확인
                if (typeof checkScrollbarAndAdjustWidth === 'function') {
                    checkScrollbarAndAdjustWidth();
                }
            }
        });
    }

    // '승인' 버튼 클릭 이벤트 --------------------------------------------------------------------------------------------
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
                loadPendingUsers();
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
                // 정렬이 깨지는 것을 방지하기 위해 에러 발생 시에도 스크롤바 확인
                if (typeof checkScrollbarAndAdjustWidth === 'function') {
                    checkScrollbarAndAdjustWidth();
                }
            }
        });
    });

    // 요청 메시지 팝업창 관련 이벤트 리스너 ---------------------------------------------------------------------------------
    // 동적으로 생성되는 .truncated-message 요소에 이벤트 위임
    $(document).on('click', '.truncated-message', function () {
        var fullText = $(this).data('full-text');
        fullTextMsgContent.text(fullText);  // 팝업창 내용 설정 (XSS 방지를 위해 text() 사용)
        fullTextMsgModal.addClass('show');  // 팝업창 표시
    });
    // 팝업창 닫기 버튼 클릭 이벤트
    closePopupBtn.click(function () {
        fullTextMsgModal.removeClass('show');
    });
    // 팝업창 외부 영역 클릭 이벤트
    $(window).click(function (event) {
        if ($(event.target).is(fullTextMsgModal)) {
            fullTextMsgModal.removeClass('show');
        }
    });
    // 요청 메시지 팝업창 관련 이벤트 리스너 ---------------------------------------------------------------------------------

    // 페이지 진입 시(DOM 로드 완료 후) 승인 대기 중인 USER 계정의 목록을 로드
    loadPendingUsers();

    // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
    // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
    $(window).on('resize', function () {
        if (typeof checkScrollbarAndAdjustWidth === 'function') {
            checkScrollbarAndAdjustWidth();
        }
    });
});
