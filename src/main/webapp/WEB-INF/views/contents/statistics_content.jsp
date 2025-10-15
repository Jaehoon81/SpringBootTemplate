<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!-- 데이터 통계 및 리스트 폼 구조 -->
<div class="data-statistics-container">
    <h2>데이터 통계 및 리스트</h2>
    <p>이곳에 다양한 데이터 통계와 페이징 리스트가 표시될 예정입니다.</p>
    <p>(현재는 준비 중입니다.)</p>
</div>
<!-- 플레이어가 이미 HTML에 정의되어 있다고 가정 -->
<audio id="audioPlayer" controls>
    <source id="audioSource" src="" type="audio/mp4">
    <!-- audioPlayer가 없을 경우를 대비한 문구 -->
    Your browser does not support the audio element.
</audio>
<button style="width: 25%" onclick="playAudio(2)">음성 2번 재생</button>

<script type="text/javascript">
    // recordId를 인자로 받아 해당 음성을 플레이하는 함수
    function playAudio(recordId) {
        const audioPlayer = document.getElementById('audioPlayer');  // <audio> 태그
        const audioSource = document.getElementById('audioSource');  // <source> 태그
        if (!audioPlayer || !audioSource) {
            console.error("Audio player elements not found in DOM.");
            return;
        }
        // 서버의 스트리밍 API 엔드포인트로 src 속성 설정
        audioSource.src = '/api/records/play/' + recordId;
        // 서버에서 MIME Type을 명확히 주지만 클라이언트에서 예상 타입을 미리 설정
        audioSource.type = "audio/mp4";

        audioPlayer.load();  // 오디오 플레이어에 새 소스를 로드
        audioPlayer.play()   // 자동 재생
            .then(() => console.log("Playing record ID: ", recordId))
            .catch(error => console.error("Error playing audio: ", error));
    }
    // 페이지 로드 시 특정 음성을 바로 플레이하고 싶을 경우
    document.addEventListener('DOMContentLoaded', () => {
        playAudio(1);  // 페이지 로드 시 1번 음성 자동 재생
    });
</script>
