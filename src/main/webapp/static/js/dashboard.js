$(document).ready(function () {
    // 초기 로직: 페이지 로드 시 첫 콘텐츠를 로드
    var displayName = $('body').data('display-name');
    var userRole = $('body').data('user-role');  // var userRole = '${userRole}';
    var defaultContentUrl = '';
    var defaultTitle = '';

    // '사용자 관리' 메뉴의 서브 화면 정의 (사용자 권한(역할)에 따른 기본 콘텐츠 URL 설정)
    if (userRole === 'SYSTEM') {
        defaultContentUrl = '/contents/system-approval';
        defaultTitle = '시스템 관리자 페이지';
    } else if (userRole === 'ADMIN') {
        defaultContentUrl = '/contents/admin-approval';
        defaultTitle = '관리자 페이지';
    } else {  // USER 계정 또는 기타 권한 시
        defaultContentUrl = '/contents/secure';  // 일반 사용자 보안 페이지
        defaultTitle = '환영합니다!';
        // USER 계정은 '사용자 관리' 메뉴의 계정 승인 기능이 사실상 의미가 없으므로
        // 기본 활성화 메뉴는 '사용자 관리'로 하되, '마이 페이지'로 리다이렉트하는 것도 고려 가능
        // (여기서는 '사용자 관리' 메뉴를 활성화된 상태로 두고 secure_content.jsp를 로드)
    }
    // 첫 페이지 로드 시 기본 콘텐츠 로드
    if (defaultContentUrl) {
        loadContent(defaultContentUrl, defaultTitle);
    }

    // 사이드바 메뉴 클릭 이벤트 처리
    $('.sidebar-menu li a').on('click', function (e) {
        e.preventDefault();  // 기본 링크 동작 방지
        // 모든 메뉴 항목의 active 클래스 제거
        $('.sidebar-menu li').removeClass('active');
        // 클릭된 메뉴 항목에 active 클래스 추가
        $(this).closest('li').addClass('active');

        var contentUrl = $(this).data('content-url');
        var menuId = $(this).closest('li').data('menu-id');
        var menuText = $(this).text().trim();

        // '사용자 관리' 메뉴는 사용자 권한(역할)에 따라 다른 콘텐츠를 로드
        if (menuId === 'account-approval') {
            if (userRole === 'SYSTEM') {
                contentUrl = '/contents/system-approval';
                menuText = '시스템 관리자 페이지';
            } else if (userRole === 'ADMIN') {
                contentUrl = '/contents/admin-approval';
                menuText = '관리자 페이지';
            } else {  // USER 계정 또는 기타 권한 시
                contentUrl = '/contents/secure';  // 일반 사용자 보안 페이지
                menuText = '환영합니다!';
            }
        }
        loadContent(contentUrl, menuText);
    });

    function loadContent(url, title) {
        $('#main-content-area').html(
            // 로딩 스피너/메시지
            '<p class="loading-contents">콘텐츠 로딩 중...</p>'
        );
        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'html',  // 응답을 HTML로 받음을 명시
            success: function (response) {
                // 응답 HTML을 DOM에 추가
                $('#main-content-area').html(response);
                $('#contentTitle').text(title);

                // 콘텐츠 로드 후 해당 목록의 로드 함수 호출 (전역 스코프에 노출되어 있으므로 직접 호출 가능)
                if (url.includes('/system-approval') && typeof window.loadPendingAdmins === 'function') {
                    window.loadPendingAdmins();
                } else if (url.includes('/admin-approval') && typeof window.loadPendingUsers === 'function') {
                    window.loadPendingUsers();
                }
                // 콘텐츠 로드 후 새로운 DOM 구조를 기반으로 스크롤바 조정을 위한 전역(window) 함수 호출
                if (typeof window.checkScrollbarAndAdjustWidth === 'function') {
                    window.checkScrollbarAndAdjustWidth();
                }
            },
            error: function (xhr) {
                $('#main-content-area').html(
                    '<p class="loading-contents" style="color: red;">' +
                    '콘텐츠를 불러오는데 실패했습니다: ' + xhr.status + ' ' + xhr.statusText + '</p>'
                );
                $('#contentTitle').text('오류 발생');
                console.error("Failed to load content from " + url, xhr);
            }
        });
    }

    // 페이지 로드 시 처음 활성화되어야 할 메뉴 항목 (기본은 '사용자 관리')
    $('[data-menu-id="account-approval"]').addClass('active');

    // Full Text 팝업창 모달 관련 로직 ------------------------------------------------------------------------------------
    var fullTextMsgModal = $('#fullTextMsgModal');
    var fullTextMsgContent = $('#fullTextMsgContent');
    var closePopupBtn = $('.close-popup');

    // 텍스트를 줄이고 '...'을 붙이는 함수
    window.truncateMessage = function (text, maxLength) {
        if (text && text.length > maxLength) {
            return text.substring(0, maxLength) + '...';
        }
        return text;
    };

    // 스크롤바 유무에 따라 CSS 변수 '--scrollbar-width'를 동적으로 설정하는 함수
    window.checkScrollbarAndAdjustWidth = function () {
        // 이 함수는 main-content-area 내부에 테이블이 로드된 후에 실행되어야 함
        // (테이블의 tbody id가 'admin-list-body' 또는 'user-list-body'임을 가정)
        var tbodyAdmin = $('#main-content-area #admin-list-body')[0];  // tbody의 DOM 요소를 직접 참조
        var tbodyUser = $('#main-content-area #user-list-body')[0];
        var tbody = tbodyAdmin || tbodyUser;  // 둘 중 하나라도 있으면 사용
        if (tbody) {
            // tbody의 scrollHeight(실제 콘텐츠 높이)가 clientHeight(보이는 영역 높이)보다 크면 스크롤바가 필요함
            var hasScrollbar = tbody.scrollHeight > tbody.clientHeight;
            var tableElement = $(tbody).closest('table')[0];  // tbody에서 가장 가까운 table을 찾음
            if (tableElement) {
                var scrollbarWidth = (hasScrollbar) ? '8px' : '0px';  // 실제 스크롤바 너비는 8px
                tableElement.style.setProperty('--scrollbar-width', scrollbarWidth);
            }
        } else {
            // 테이블이 없는 페이지의 경우 스크롤바 너비를 0으로 초기화
            var tableElement = $('#main-content-area table')[0];
            if (tableElement) {
                tableElement.style.setProperty('--scrollbar-width', '0px');
            }
        }
    };

    // 동적으로 생성되는 .truncated-message 요소에 이벤트 위임
    // (main-content-area 내부에서 발생하는 클릭 이벤트에 반응)
    $('#main-content-area').off('click', '.truncated-message')
                           .on('click', '.truncated-message', function () {
        var fullText = $(this).data('full-text');
        fullTextMsgContent.text(fullText);  // 팝업창 내용 설정 (XSS 방지를 위해 text() 사용)
        fullTextMsgModal.addClass('show');  // 팝업창 표시
    });

    // 팝업창 닫기 버튼 클릭
    closePopupBtn.click(function () {
        fullTextMsgModal.removeClass('show');
    });

    // 팝업창 외부 영역 클릭
    $(window).click(function (event) {
        if ($(event.target).is(fullTextMsgModal)) {
            fullTextMsgModal.removeClass('show');
        }
    });
    // Full Text 팝업창 모달 관련 로직 ------------------------------------------------------------------------------------

    // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
    // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
    $(window).on('resize', function () {
        if (typeof window.checkScrollbarAndAdjustWidth === 'function') {
            window.checkScrollbarAndAdjustWidth();
        }
    });

    // 로그아웃 버튼 클릭 이벤트 (사이드바에 추가)
    // $('#logoutBtn').click(function () {
    //     $.ajax({
    //         url: '/api/auth/web-logout',
    //         type: 'POST',
    //         success: function (response) {
    //             // 서버에 로그아웃 요청을 보내 쿠키를 만료시킴
    //             alert('로그아웃 되었습니다.');
    //             window.location.href = '/';  // 로그아웃 후 로그인 페이지(login.jsp)로 이동
    //         },
    //         error: function (xhr) {
    //             alert('로그아웃 실패: ' + (xhr.responseText || '알 수 없는 오류'));
    //         }
    //     });
    // });
});
