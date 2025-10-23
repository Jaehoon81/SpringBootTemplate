<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!-- 데이터 통계 및 리스트 폼 구조 -->
<div class="data-statistics-container">
    <h2>데이터 통계 및 리스트</h2>
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
                <p>음성 1 (<span id="audio-date-1">-</span>)</p>
                <audio id="audio-player-controls-1" controls></audio>
            </div>
            <div class="audio-track" id="audio-track-2">
                <p>음성 2 (<span id="audio-date-2">-</span>)</p>
                <audio id="audio-player-controls-2" controls></audio>
            </div>
            <div class="audio-track" id="audio-track-3">
                <p>음성 3 (<span id="audio-date-3">-</span>)</p>
                <audio id="audio-player-controls-3" controls></audio>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
</script>
