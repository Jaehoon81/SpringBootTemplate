<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<style>
    .auth-secure-container {
        background-color: #ffffff;
        padding: 40px;
        border-radius: 12px;
        /*box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);*/
        width: 1200px;
        max-width: 90%;
        text-align: center;
        box-sizing: border-box;
        /*transition: width 0.3s ease; !* 너비 변경 시 부드러운 애니메이션 효과 *!*/
    }
    /* 스크롤 발생 시 적용될 스타일 */
    .auth-secure-container.scrolling {
        width: 1190px; /* 스크롤바 공간을 고려하여 살짝 줄임 */
        /* 필요에 따라 margin-right: 100px; 등으로 조정 가능 */
    }
    .auth-secure-container h2 {
        text-align: center;
        color: #333;
        margin-bottom: 30px;
        font-size: 1.6em; /* 폰트 크기 */
    }

    /* 페이지 하단 버튼 그룹 스타일 */
    /*.auth-secure-container > button {*/
    /*    margin: 15px 8px; !* 하단 버튼 마진 조정 *!*/
    /*}*/
</style>

<div class="auth-secure-container">
    <h2>계정 승인 안내</h2>
    <p><span style="font-weight: bold;"><c:out value="${displayName}"/></span>님, 환영합니다!</p>
    <p>이 페이지는 인증된 사용자만 접근할 수 있습니다.</p>
    <br>
    <p style="color: #007bff;"><!-- 권한에 따른 메시지 표시 -->
        <c:choose>
            <c:when test="${userRole eq 'USER'}">
                <!-- 이제 secure_content.jsp는 USER 권한 전용 페이지로 사용됨 -->
                (<c:out value="${displayName}"/>님은 일반 사용자 권한으로 로그인하셨습니다. 계정 승인 작업은 시스템 관리자 또는 관리자만 가능합니다.)
            </c:when>
            <c:otherwise>
                (<c:out value="${displayName}"/>님은 알 수 없는 권한입니다.)
            </c:otherwise>
        </c:choose>
    </p>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        // 혹시 모를 스크롤 발생 시에도 해당 컨테이너의 너비를 다시 체크 (선택 사항)
        var $contentWrapper = $('.content-wrapper');
        $contentWrapper.on('scroll', function () {
            if (typeof window.adjustSecureContainerWidthBasedOnScroll === 'function') {
                window.adjustSecureContainerWidthBasedOnScroll();
            }
        });
        // 윈도우 리사이즈 시에도 스크롤바 유무를 다시 확인하여 조절 (선택 사항)
        // (max-height 변경 등으로 스크롤바가 생기거나 없어질 때도 반응)
        $(window).on('resize', function () {
            if (typeof window.adjustSecureContainerWidthBasedOnScroll === 'function') {
                window.adjustSecureContainerWidthBasedOnScroll();
            }
        });
    });

    // 스크롤바 유무에 따라 너비를 동적으로 설정하는 함수 -----------------------------------------------------------------------
    window.adjustSecureContainerWidthBasedOnScroll = function () {
        // 스크롤바 너비 조절 관련 요소들
        var $contentWrapper = $('.content-wrapper');
        var $authSecureContainer = $('.auth-secure-container');

        if ($authSecureContainer.length === 0 || $contentWrapper.length === 0) {
            return;
        }
        if ($contentWrapper[0].scrollHeight > $contentWrapper[0].clientHeight) {
            $authSecureContainer.addClass('scrolling');
        } else {
            $authSecureContainer.removeClass('scrolling');
        }
    };
</script>
