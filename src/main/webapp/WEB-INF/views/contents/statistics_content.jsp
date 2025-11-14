<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<style>
    .statistics-tooltip {
        width: 310px; /* 데이터 통계 툴팁의 너비 */
        margin-left: -164px; /* 툴팁 너비의 절반만큼 왼쪽으로 이동하여 중앙 정렬 */
    }
</style>

<!-- 동적으로 콘텐츠가 로드될 영역 -->
<div id="data-content-area" class="content-area">
    <!-- 데이터 통계 및 리스트 폼 구조 (초기 콘텐츠) -->
    <div class="data-statistics-container">
        <h2>데이터 통계 및 리스트
            <!-- 데이터 통계 및 리스트 안내 툴팁 -->
            <p style="display: inline;">
                <span class="tooltip-container">
                    <span class="tooltip-icon"><i class="fa-solid fa-circle-info"></i></span>
                    <span class="custom-tooltip statistics-tooltip"
                          data-tooltip-text="/swagger-ui/index.html 로 접속 후,&nbsp; 모바일 로그인 &rarr; 새 참가자 등록 &rarr; 음성녹음 파일 업로드"></span>
                </span>
            </p>
        </h2>
        <p style="margin-top: -20px;">
            <a href="/contents/notice" id="goToNoticeLink" style="display: inline;">공지사항으로 이동</a>
        </p>
        <!-- 리스트 다운로드 아이콘 버튼 -->
        <div class="download-button-wrapper">
            <button id="excelDownloadBtn" class="download-excel-btn">
                리스트 다운로드 <i class="fas fa-file-excel"></i>
            </button>
        </div>
        <!-- 필터 컨트롤 그룹으로 묶기: 등급별(라디오 버튼), 기간별(달력 팝업), 검색어(입력 필드) -->
        <div class="filter-controls-group">
            <!-- 등급별 필터링 영역 (라디오 버튼 섹션) -->
            <div class="grade-filter-container">
                <label class="radio-button-container">
                    <input type="radio" name="gradeFilter" value="" checked>
                    <span class="radio-custom"></span> 전체
                </label>
                <label class="radio-button-container">
                    <input type="radio" name="gradeFilter" value="GOLD">
                    <span class="radio-custom"></span> GOLD
                </label>
                <label class="radio-button-container">
                    <input type="radio" name="gradeFilter" value="SILVER">
                    <span class="radio-custom"></span> SILVER
                </label>
                <label class="radio-button-container">
                    <input type="radio" name="gradeFilter" value="BRONZE">
                    <span class="radio-custom"></span> BRONZE
                </label>
                <label class="radio-button-container">
                    <input type="radio" name="gradeFilter" value="NONE">
                    <span class="radio-custom"></span> NONE
                </label>
            </div>
            <!-- 기간별 필터링 영역 (달력 팝업 포함) -->
            <div class="date-range-filter-container">
                <i class="fas fa-calendar-alt calendar-icon"></i>
                <input type="text" id="dateRangePicker" class="date-range-input" name="dates" placeholder="시작/종료 날짜 선택" readonly>
            </div>
            <!-- 검색어 필터링 영역 (입력 필드 + 검색 버튼) -->
            <div class="search-filter-container">
                <i class="fas fa-search search-icon"></i>
                <input type="text" id="searchKeyword" class="search-input" placeholder="검색어 입력">
                <button id="clearSearchBtn" class="search-clear-btn"><i class="fas fa-times"></i></button>
            </div>
            <button id="executeSearchBtn" class="search-execute-btn">검색</button>
        </div>
        <div id="participant-list-container">
            <table class="data-statistics-table">
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>이름</th>
                        <th>출생연월</th>
                        <th>성별</th>
                        <th>등급</th>
                        <th>담당관리자</th>
                        <th>음성녹음</th>
                        <th>녹음일자</th>
                    </tr>
                </thead>
                <tbody id="participant-list-body">
                    <!-- 참가자 목록이 JavaScript로 여기에 동적으로 로드됨 -->
                    <tr>
                        <td colspan="8" class="loading-contents">참가자 목록을 불러오는 중...</td>
                    </tr>
                </tbody>
            </table>
            <div id="no-data-message" class="no-data-message" style="display: none;">
                <p style="color: red;">참가자 목록 조회 중 오류가 발생했습니다.</p>
            </div>
        </div>
        <div id="pagination-controls" class="pagination-controls">
            <!-- 페이지네이션 버튼(링크)들이 여기에 동적으로 생성됨 -->
        </div>
    </div>
</div>

<!-- 음성녹음 상세 정보 및 오디오 플레이어 모달 구조 -->
<div id="recordDetailsModal" class="audio-player-popup">
    <div class="audio-player-content">
        <span class="close-audio-player">&times;</span>
        <h3>음성녹음 확인</h3>
        <div class="participant-info-display">
            <p><!-- 여기에 참가자 정보를 한 줄로 표시 -->
                <span class="p-info-label">이름:</span> <span id="p-name"></span>
                <span class="p-info-label">출생연월:</span> <span id="p-birth-year-month"></span>
                <span class="p-info-label">성별:</span> <span id="p-gender-kor"></span>
                <span class="p-info-label">등급:</span> <span id="p-grade"></span>
                <span class="p-info-label">담당관리자:</span> <span id="p-assigned-admin-name"></span>
            </p>
        </div>
        <div class="audio-tracks-container">
            <div class="audio-track" id="audio-track-1">
                <p>음성 1번 (<span id="audio-date-1">-</span>)</p>
                <audio id="audio-player-controls-1" controls></audio>
            </div>
            <div class="audio-track" id="audio-track-2">
                <p>음성 2번 (<span id="audio-date-2">-</span>)</p>
                <audio id="audio-player-controls-2" controls></audio>
            </div>
            <div class="audio-track" id="audio-track-3">
                <p>음성 3번 (<span id="audio-date-3">-</span>)</p>
                <audio id="audio-player-controls-3" controls></audio>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        $('.tooltip-container').each(function () {
            const customTooltip = $(this).find('.custom-tooltip');
            var tooltipText = customTooltip.data('tooltip-text');

            // data-tooltip-text 속성의 내용을 읽어서 커스텀 툴팁 안에 채워넣음
            customTooltip.text(tooltipText);
        });
    });
</script>
