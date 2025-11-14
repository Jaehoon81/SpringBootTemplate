$(document).ready(function () {
    var displayName = $('body').data('display-name');
    var userRole = $('body').data('user-role');  // var userRole = '${userRole}';
    var profilePicturePath = $('body').data('profile-picture-path');

    // 프로필 사진(이미지) 관련 로직 ---------------------------------------------------------------------------------------
    var $profilePicture = $('#profilePicture');
    var $profilePictureInput = $('#profilePictureInput');
    var $profilePictureContainer = $('.profile-picture-container');

    // 초기 프로필 사진(이미지) 설정 (기본값)
    if (profilePicturePath) {
        // '/profiles/{userId}/{filename}' 형태로 DB users 테이블의 profile_picture_path 컬럼에 저장되어야
        // GET 방식의 /api/user/profile-picture/{userId}/{filename} API와 연결 (-> 사용 X)
        // (/api/user/profile-picture로 시작하는 URL이 컨트롤러에서 정적으로 서빙되는 경로라고 가정)

        // '/api/user/profile-picture/{username}/{filename}' 형태로 DB users 테이블의 profile_picture_path 컬럼에 저장되어야
        // GET 방식의 /api/user/profile-picture/{username}/{filename} API와 연결 (-> 사용 O)
        // (/api/user/profile-picture로 시작하는 URL이 컨트롤러에서 정적으로 서빙되는 경로라고 가정)
        $profilePicture.attr('src', profilePicturePath);
    } else {
        $profilePicture.attr('src', '/static/images/default_profile_02.png');
    }

    // 프로필 사진(이미지) 클릭 시 파일 탐색기 열기
    $profilePictureContainer.on('click', function (event) {
        $profilePictureInput.click();  // 파일 입력 필드인 <input type="file"...> 태그를 클릭
        // 상위 컨테이너로 클릭 이벤트가 다시 전달되지 않도록 이벤트 버블링을 중단
        event.stopPropagation();
    });
    // 파일 입력 필드인 <input type="file"...> 태그 자체에 대한 클릭 이벤트 (이중 방지책)
    $profilePictureInput.on('click', function (event) {
        event.stopPropagation();
    });
    // 파일 선택 시 처리
    $profilePictureInput.on('change', function (event) {
        var file = event.target.files[0];
        if (file) {
            var reader = new FileReader();
            reader.onload = function (e) {
                $profilePicture.attr('src', e.target.result);  // 미리보기 이미지 업데이트
                uploadProfilePicture(file);  // 서버에 이미지 업로드
            };
            reader.readAsDataURL(file);
        }
    });

    // 프로필 사진(이미지) 업로드 함수
    function uploadProfilePicture(file) {
        var formData = new FormData();
        formData.append('file', file);

        $.ajax({
            url: '/api/user/profile-picture',
            type: 'POST',
            data: formData,
            processData: false,  // FormData 사용 시 필수
            contentType: false,  // FormData 사용 시 필수
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            success: function (responsePath) {  // 응답으로 저장된 파일 경로를 받음
                alert('프로필 사진이 성공적으로 업데이트되었습니다.');
                // DB users 테이블의 profile_picture_path 컬럼에 저장된 경로로 이미지 업데이트
                $profilePicture.attr('src', responsePath);
                // 필요하다면 페이지 전체 새로고침 (선택 사항)
                window.location.reload();
            },
            error: function (xhr) {
                var errorMsg = '프로필 사진 업로드 실패: ' + (xhr.responseText || '알 수 없는 오류');
                try {
                    var jsonError = JSON.parse(xhr.responseText);
                    if (jsonError.message) {
                        errorMsg = '프로필 사진 업로드 실패: ' + jsonError.message;
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }
                alert(errorMsg);
                console.error("프로필 사진 업로드 실패: ", xhr.responseText);
                // 업로드 실패 시 기본 이미지로 되돌리기 (선택 사항)
                $profilePicture.attr('src', profilePicturePath || '/static/images/default_profile_02.png');
            }
        });
    }
    // 프로필 사진(이미지) 관련 로직 ---------------------------------------------------------------------------------------

    // 쿠키에서 JWT 토큰을 가져오는 함수 (웹 브라우저 인증용)
    window.getJwtTokenFromCookie = function () {
        var name = "jwt=";
        var decodedCookie = decodeURIComponent(document.cookie);
        var ca = decodedCookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) === ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) === 0) {
                return c.substring(name.length, c.length);
            }
        }
        return "";
    };

    // 초기 로직: 페이지 로드 시 첫 콘텐츠를 로드 ----------------------------------------------------------------------------
    var lastActiveMenuId = localStorage.getItem('lastActiveMenuId');
    var initialActiveMenuId = '';  // active 클래스를 적용할 메뉴 ID
    var initialContentUrl = '';
    var initialTitle = '';

    // 방법 1) 권한(역할)에 상관없이 모든 사이드바 메뉴가 노출되는 경우의 로직 ----------------------------------------------------
    // var accountApprovalUrl = '';
    // var accountApprovalTitle = '';
    //
    // // '사용자 관리' 메뉴의 서브 화면 정의 (사용자 권한(역할)에 따른 기본 콘텐츠 URL 설정)
    // if (userRole === 'SYSTEM') {
    //     accountApprovalUrl = '/contents/system-approval';
    //     accountApprovalTitle = '시스템 관리자 페이지';
    // } else if (userRole === 'ADMIN') {
    //     accountApprovalUrl = '/contents/admin-approval';
    //     accountApprovalTitle = '관리자 페이지';
    // } else {  // USER 계정 또는 기타 권한 시
    //     accountApprovalUrl = '/contents/secure';  // 일반 사용자 보안 페이지
    //     accountApprovalTitle = '환영합니다!';
    //     // USER 계정은 '사용자 관리' 메뉴의 계정승인 기능이 사실상 의미가 없으므로
    //     // 기본 활성화 메뉴는 '사용자 관리'로 하되, '마이 페이지'로 리다이렉트하는 것도 고려 가능
    //     // (여기서는 '사용자 관리' 메뉴를 활성화된 상태로 두고 secure_content.jsp를 로드)
    // }
    // // 모든 메뉴 항목에서 active 클래스 제거 (하드코딩 방지 및 초기화)
    // $('.sidebar-menu li').removeClass('active');
    //
    // // 새로고침(=F5) 시에 활성화된 메뉴 상태를 유지하는 로직
    // if (lastActiveMenuId) {
    //     var $lastActiveMenuItem = $('.sidebar-menu li[data-menu-id="' + lastActiveMenuId + '"]');
    //     if ($lastActiveMenuItem.length > 0) {
    //         // 마지막 활성화 메뉴 아이템에 active 클래스 부여
    //         // $('.sidebar-menu li').removeClass('active');
    //         $lastActiveMenuItem.addClass('active');
    //
    //         // 해당 메뉴의 data-content-url을 가져와서 로드 (contentUrl 변수에 매핑)
    //         var contentUrl = $lastActiveMenuItem.find('a').data('content-url');
    //         var menuText = $lastActiveMenuItem.find('a').text().trim();
    //
    //         // '사용자 관리' 메뉴는 권한(역할)에 따라 URL이 동적으로 변경되므로 다시 확인
    //         if (lastActiveMenuId === 'account-approval') {
    //             initialContentUrl = accountApprovalUrl;
    //             initialTitle = accountApprovalTitle;
    //         } else {
    //             initialContentUrl = contentUrl;
    //             initialTitle = menuText;
    //         }
    //     } else {
    //         // localStorage에 저장된 메뉴가 없거나 유효하지 않은 경우
    //         // 기본 값을 'account-approval'로 설정
    //         lastActiveMenuId = 'account-approval';
    //         // $('.sidebar-menu li').removeClass('active');
    //         $('.sidebar-menu li[data-menu-id="account-approval"]').addClass('active');
    //         initialContentUrl = accountApprovalUrl;
    //         initialTitle = accountApprovalTitle;
    //     }
    // } else {
    //     // localStorage에 저장된 것이 없는 경우 '사용자 관리' 메뉴를 기본으로 활성화
    //     lastActiveMenuId = 'account-approval';
    //     // $('.sidebar-menu li').removeClass('active');
    //     $('.sidebar-menu li[data-menu-id="account-approval"]').addClass('active');
    //     initialContentUrl = accountApprovalUrl;
    //     initialTitle = accountApprovalTitle;
    // }
    // // 최종적으로 결정된 초기 콘텐츠를 로드
    // loadContent(initialContentUrl, initialTitle);
    //
    // // 사이드바 메뉴 클릭 이벤트 (localStorage에 메뉴 ID를 저장하는 로직 포함)
    // $('.sidebar-menu li a').on('click', function (e) {
    //     e.preventDefault();  // 기본 링크 동작 방지
    //     // 모든 메뉴 항목의 active 클래스 제거
    //     $('.sidebar-menu li').removeClass('active');
    //     // 클릭된 메뉴 항목에 active 클래스 추가
    //     var $clickedMenuItem = $(this).closest('li');
    //     $clickedMenuItem.addClass('active');
    //
    //     var clickedMenuId = $clickedMenuItem.data('menu-id');  // 클릭된 메뉴의 ID
    //     var contentUrl = $(this).data('content-url');
    //     var menuText = $(this).text().trim();
    //
    //     // 클릭된 메뉴 ID를 localStorage에 저장
    //     localStorage.setItem('lastActiveMenuId', clickedMenuId);
    //
    //     // '사용자 관리' 메뉴의 경우 권한(역할)에 따라 URL을 결정
    //     if (clickedMenuId === 'account-approval') {
    //         contentUrl = accountApprovalUrl;  // 위에서 결정된 권한(역할)별 URL 사용
    //         menuText = accountApprovalTitle;  // 위에서 결정된 권한(역할)별 타이틀 사용
    //     }
    //     loadContent(contentUrl, menuText);
    // });
    // 방법 1) 권한(역할)에 상관없이 모든 사이드바 메뉴가 노출되는 경우의 로직 ----------------------------------------------------

    // 방법 2) USER 권한의 계정은 '사용자 관리' 메뉴가 노출되지 않는 경우의 로직 -------------------------------------------------
    // 권한(역할)별 기본 URL 정의 (재사용을 위해 분리)
    var urls = {
        accountApproval: {
            SYSTEM: { url: '/contents/system-approval', title: '시스템 관리자 페이지' },
            ADMIN: { url: '/contents/admin-approval', title: '관리자 페이지' },
            // USER 권한의 계정은 '사용자 관리' 메뉴가 안보이지만 url은 정의함
            USER: { url: '/contents/secure', title: '환영합니다!' }
        },
        dataStatistics: { url: '/contents/statistics', title: '데이터 통계' },
        editProfile: { url: '/contents/profile', title: '마이 페이지' }
    };
    // 모든 메뉴 항목에서 active 클래스 제거 (하드코딩 방지 및 초기화)
    $('.sidebar-menu li').removeClass('active');

    // 1. 초기 메뉴 ID 결정
    if (userRole === 'USER') {  // USER 권한(역할)일 때
        // '사용자 관리' 메뉴를 선택했던 기록이 있거나 아예 기록이 없는 경우 '데이터 통계' 메뉴로 강제
        if (!lastActiveMenuId || lastActiveMenuId === 'account-approval') {
            initialActiveMenuId = 'data-statistics';
        } else {
            initialActiveMenuId = lastActiveMenuId;
        }
    } else {  // SYSTEM or ADMIN 권한(역할)일 때
        // '사용자 관리' 메뉴를 선택했던 기록이 있거나 아예 기록이 없는 경우 '사용자 관리' 메뉴로 강제
        if (!lastActiveMenuId || lastActiveMenuId === 'account-approval') {
            initialActiveMenuId = 'account-approval';
        } else {
            initialActiveMenuId = lastActiveMenuId;
        }
    }
    // 2. 결정된 메뉴 ID를 기반으로 URL 및 타이틀 설정
    var $initialActiveMenuItem = $('.sidebar-menu li[data-menu-id="' + initialActiveMenuId + '"]');
    if ($initialActiveMenuItem.length > 0) {
        $initialActiveMenuItem.addClass('active');  // 선택된 메뉴에 active 클래스 부여

        var menuAnchor = $initialActiveMenuItem.find('a');
        if (initialActiveMenuId === 'account-approval') {
            // '사용자 관리' 메뉴의 경우 권한(역할)에 따라 URL을 결정
            var roleSpecificConfig = (urls.accountApproval[userRole] || urls.accountApproval.USER);
            initialContentUrl = roleSpecificConfig.url;
            initialTitle = roleSpecificConfig.title;  // 권한(역할)별 제목
        } else if (initialActiveMenuId === 'data-statistics') {
            initialContentUrl = urls.dataStatistics.url;
            initialTitle = urls.dataStatistics.title;
        } else if (initialActiveMenuId === 'edit-profile') {
            initialContentUrl = urls.editProfile.url;
            initialTitle = urls.editProfile.title;
        } else {  // 알 수 없는 메뉴 ID의 경우
            if (userRole === 'USER') {  // USER 계정은 '데이터 통계' 메뉴로 폴백
                initialContentUrl = urls.dataStatistics.url;
                initialTitle = urls.dataStatistics.title;
                $('.sidebar-menu li[data-menu-id="data-statistics"]').addClass('active');
            } else {  // 그 외(SYSTEM or ADMIN) 계정은 '사용자 관리' 메뉴로 폴백
                initialContentUrl = urls.accountApproval[userRole].url;
                initialTitle = urls.accountApproval[userRole].title;
                $('.sidebar-menu li[data-menu-id="account-approval"]').addClass('active');
            }
        }
    } else {  // 해당(저장된) ID의 메뉴 항목이 DOM에 없을 경우
        if (userRole === 'USER') {  // USER 계정은 '데이터 통계' 메뉴로 폴백
            initialActiveMenuId = 'data-statistics';
            initialContentUrl = urls.dataStatistics.url;
            initialTitle = urls.dataStatistics.title;
            $('.sidebar-menu li[data-menu-id="data-statistics"]').addClass('active');
        } else {  // 그 외(SYSTEM or ADMIN) 계정은 '사용자 관리' 메뉴로 폴백
            initialActiveMenuId = 'account-approval';
            initialContentUrl = urls.accountApproval[userRole].url;
            initialTitle = urls.accountApproval[userRole].title;
            $('.sidebar-menu li[data-menu-id="account-approval"]').addClass('active');
        }
    }
    // localStorage에도 최종 결정된 메뉴 ID(lastActiveMenuId)를 저장
    localStorage.setItem('lastActiveMenuId', initialActiveMenuId);
    // 최종적으로 결정된 초기 콘텐츠를 로드
    loadContent(initialContentUrl, initialTitle);

    // 사이드바 메뉴 클릭 이벤트 (localStorage에 메뉴 ID를 저장하는 로직 포함)
    $('.sidebar-menu li a').on('click', function (e) {
        e.preventDefault();  // 기본 링크 동작 방지
        // 모든 메뉴 항목의 active 클래스 제거
        $('.sidebar-menu li').removeClass('active');
        // 클릭된 메뉴 항목에 active 클래스 추가
        var $clickedMenuItem = $(this).closest('li');
        $clickedMenuItem.addClass('active');

        var clickedMenuId = $clickedMenuItem.data('menu-id');  // 클릭된 메뉴의 ID
        var contentUrl = $(this).data('content-url');
        var menuText = $(this).text().trim();

        // 클릭된 메뉴 ID를 localStorage에 저장
        localStorage.setItem('lastActiveMenuId', clickedMenuId);

        if (clickedMenuId === 'account-approval') {
            // '사용자 관리' 메뉴의 경우 권한(역할)에 따라 URL을 결정
            var roleSpecificConfig = (urls.accountApproval[userRole] || urls.accountApproval.USER);
            contentUrl = roleSpecificConfig.url;
            menuText = roleSpecificConfig.title;  // 권한(역할)별 제목
        } else if (clickedMenuId === 'data-statistics') {
            contentUrl = urls.dataStatistics.url;
            menuText = urls.dataStatistics.title;
        } else if (clickedMenuId === 'edit-profile') {
            contentUrl = urls.editProfile.url;
            menuText = urls.editProfile.title;
        } else {  // 알 수 없는 메뉴 ID의 경우
            // 기본 Content URL(data-content-url)로 폴백
            contentUrl = $(this).data('content-url');
            menuText = $(this).text().trim();
        }
        // USER 계정이 '사용자 관리' 메뉴를 클릭하려고 할 때의 방지책
        if (userRole === 'USER' && clickedMenuId === 'account-approval') {
            contentUrl = urls.dataStatistics.url;
            menuText = urls.dataStatistics.title;
            $('.sidebar-menu li[data-menu-id="data-statistics"]').addClass('active');
        }
        loadContent(contentUrl, menuText);
    });
    // 방법 2) USER 권한의 계정은 '사용자 관리' 메뉴가 노출되지 않는 경우의 로직 -------------------------------------------------

    // 사이드바 메뉴에 따른 콘텐츠 로드 함수 ---------------------------------------------------------------------------------
    function loadContent(targetUrl, title) {
        var $mainContentArea = $('#main-content-area');
        $mainContentArea.html('<p class="loading-contents">콘텐츠 로딩 중...</p>');  // 로딩 메시지
        // 콘텐츠 로드 시작 시 is-loading 클래스 제거(초기화) 및 다시 추가
        $mainContentArea.removeClass('is-loading');
        $mainContentArea.addClass('is-loading');

        $.ajax({
            url: targetUrl,
            type: 'GET',
            dataType: 'html',  // 응답을 HTML로 받음을 명시
            success: function (response) {
                $mainContentArea.html(response);  // 응답 HTML을 DOM에 추가
                // 콘텐츠 로드 완료 시 is-loading 클래스 제거
                $mainContentArea.removeClass('is-loading');
                $('#contentTitle').text(title);

                // 해당 JSP 파일을 로드 시 HTML 내부의 스크립트(<script> 태그)를 찾아서 실행
                $(response).filter('script').each(function (){
                    $.globalEval(this.text || this.textContent || this.innerHTML || '');
                });
                // 콘텐츠 로드 후 선택된 메뉴의 초기화/로드 함수를 호출 (전역 스코프에 노출되어 있으므로 직접 호출 가능)
                if (targetUrl.includes('/system-approval')) {
                    if (typeof window.loadPendingAdmins === 'function') {
                        window.loadPendingAdmins();
                    }
                } else if (targetUrl.includes('/admin-approval')) {
                    if (typeof window.loadPendingUsers === 'function') {
                        window.loadPendingUsers();
                    }
                } else if (targetUrl.includes('/secure')) {

                } else if (targetUrl.includes('/statistics')) {
                    if (typeof window.initDataStatistics === 'function') {
                        window.initDataStatistics();
                    }
                } else if (targetUrl.includes('/profile')) {
                    if (typeof window.initProfileEdit === 'function') {
                        window.initProfileEdit();
                    }
                }
                // 콘텐츠 로드 후 스크롤바 상태에 따라 각 컨테이너(서브 화면)의 너비를 조절
                adjustContainerWidthBasedOnScroll(targetUrl);
                // 콘텐츠 로드 후 새로운 DOM 구조를 기반으로 스크롤바 조정을 위한 전역(window) 함수 호출
                // if (typeof window.checkScrollbarAndAdjustWidth === 'function') {
                //     window.checkScrollbarAndAdjustWidth();
                // }
            },
            error: function (xhr) {
                $mainContentArea.html(
                    '<p class="loading-contents" style="color: red;">' +
                    '콘텐츠를 불러오는데 실패했습니다: ' + xhr.status + ' ' + xhr.statusText + '</p>'
                );
                // 오류 발생 시에도 is-loading 클래스 제거
                // $mainContentArea.removeClass('is-loading');
                $('#contentTitle').text('오류 발생');
                console.error("Failed to load content from " + targetUrl, xhr);
            }
        });
    }
    // 페이지 로드 시 처음 활성화할 메뉴 항목 (기본은 '사용자 관리')
    // $('[data-menu-id="account-approval"]').addClass('active');
    // 초기 로직: 페이지 로드 시 첫 콘텐츠를 로드 ----------------------------------------------------------------------------

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

    // 동적으로 생성되는 .truncated-message 요소에 이벤트 위임
    // (main-content-area 내부에서 발생하는 클릭 이벤트에 반응)
    $('#main-content-area').off('click', '.truncated-message')
                           .on('click', '.truncated-message', function () {
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
    // Full Text 팝업창 모달 관련 로직 ------------------------------------------------------------------------------------

    // 스크롤바 상태에 따라 각 컨테이너(서브 화면)의 너비를 조절하는 함수
    function adjustContainerWidthBasedOnScroll(targetUrl) {
        if (targetUrl.includes('/system-approval')) {
            if (typeof window.adjustSystemContainerWidthBasedOnScroll === 'function') {
                window.adjustSystemContainerWidthBasedOnScroll();
            }
        } else if (targetUrl.includes('/admin-approval')) {
            if (typeof window.adjustAdminContainerWidthBasedOnScroll === 'function') {
                window.adjustAdminContainerWidthBasedOnScroll();
            }
        } else if (targetUrl.includes('/secure')) {
            if (typeof window.adjustSecureContainerWidthBasedOnScroll === 'function') {
                window.adjustSecureContainerWidthBasedOnScroll();
            }
        } else if (targetUrl.includes('/statistics')) {
            if (typeof window.adjustStatisticsContainerWidthBasedOnScroll === 'function') {
                window.adjustStatisticsContainerWidthBasedOnScroll();
            }
        } else if (targetUrl.includes('/profile')) {
            if (typeof window.adjustProfileContainerWidthBasedOnScroll === 'function') {
                window.adjustProfileContainerWidthBasedOnScroll();
            }
        }
    }
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
    // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
    // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
    $(window).on('resize', function () {
        // if (typeof window.checkScrollbarAndAdjustWidth === 'function') {
        //     window.checkScrollbarAndAdjustWidth();
        // }
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
