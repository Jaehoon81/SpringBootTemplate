// 데이터 통계 및 리스트 로드 함수 ------------------------------------------------------------------------------------------
// $(document).ready(function () {  // 방법 1) 기본 방식
// (function () {                   // 방법 2) 바로 함수를 호출하는 방식 (즉시 실행 함수로 스코프를 보호)
window.initDataStatistics = function () {  // 데이터 통계와 페이징 리스트 로드 함수를 전역 스코프에 노출
    // 방법 3) function() { ... }로 감싸서 window.initDataStatistics() 형태로 내보내는 방식
    // statistics_content.jsp 내부의 스크립트(<script> 태그)가 아닌,
    // 별도의 statistics.js 파일로 분리했을 경우에 사용

    // 혹시 모를 스크롤 발생 시에도 해당 컨테이너의 너비를 다시 체크 (선택 사항)
    var $contentWrapper = $('.content-wrapper');
    $contentWrapper.on('scroll', function () {
        if (typeof window.adjustStatisticsContainerWidthBasedOnScroll === 'function') {
            window.adjustStatisticsContainerWidthBasedOnScroll();
        }
    });
    // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
    // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
    $(window).on('resize', function () {
        if (typeof window.adjustStatisticsContainerWidthBasedOnScroll === 'function') {
            window.adjustStatisticsContainerWidthBasedOnScroll();
        }
    });
// });    // 방법 1)
// })();  // 방법 2)
};  // 방법 3)

// 스크롤바 유무에 따라 너비를 동적으로 설정하는 함수 ---------------------------------------------------------------------------
window.adjustStatisticsContainerWidthBasedOnScroll = function () {
    // 스크롤바 너비 조절 관련 요소들
    var $contentWrapper = $('.content-wrapper');
    var $dataStatisticsContainer = $('.data-statistics-container');

    if ($dataStatisticsContainer.length === 0 || $contentWrapper.length === 0) {
        return;
    }
    if ($contentWrapper[0].scrollHeight > $contentWrapper[0].clientHeight) {
        $dataStatisticsContainer.addClass('scrolling');
    } else {
        $dataStatisticsContainer.removeClass('scrolling');
    }
};
