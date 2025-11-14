// 데이터 통계 및 리스트 로드 함수 ------------------------------------------------------------------------------------------
// $(document).ready(function () {  // 방법 1) 기본 방식
// (function () {                   // 방법 2) 바로 함수를 호출하는 방식 (즉시 실행 함수로 스코프를 보호)
window.initDataStatistics = function () {  // 데이터 통계와 페이징 리스트 로드 함수를 전역 스코프에 노출
    // 방법 3) function() { ... }로 감싸서 window.initDataStatistics() 형태로 내보내는 방식
    // statistics_content.jsp 내부의 스크립트(<script> 태그)가 아닌,
    // 별도의 statistics.js 파일로 분리했을 경우에 사용

    const listPerPage = 5;  // 한 페이지당 항목(셀) 수
    let currentPage = 1;  // 현재 페이지

    let currentGradeFilter = '';  // 현재 선택된 등급 필터
    let currentStartDate = '';  // 현재 선택된 시작일 (yyyy-MM-dd)
    let currentEndDate = '';    // 현재 선택된 종료일 (yyyy-MM-dd)
    let currentSearchKeyword = '';  // 현재 입력된 검색어

    // 디폴트 날짜 범위 설정 (2025/01/01 ~ 오늘)
    const defaultStartDate = moment('2025-01-01');
    const defaultEndDate = moment();  // 오늘 날짜
    currentStartDate = defaultStartDate.format('YYYY-MM-DD');
    currentEndDate = defaultEndDate.format('YYYY-MM-DD');

    // 등급 드롭다운 옵션 (백엔드의 Grade Enum과 동일)
    const gradeOptions = ["GOLD", "SILVER", "BRONZE", "NONE"];

    // 음성녹음 상세 정보 및 오디오 플레이어 모달 관련 요소들
    var $recordDetailsModal = $('#recordDetailsModal');
    var $closePopupBtn = $recordDetailsModal.find('.close-audio-player');

    // 페이지별 참가자 목록을 로드하는 함수 ----------------------------------------------------------------------------------
    function loadParticipantList(page, gradeFilter, startDate, endDate, searchKeyword) {
        currentPage = page;  // 현재 페이지를 업데이트
        currentGradeFilter = gradeFilter;  // 현재 등급 필터를 업데이트
        currentStartDate = startDate;  // 현재 시작 날짜를 업데이트
        currentEndDate = endDate;      // 현재 종료 날짜를 업데이트
        currentSearchKeyword = searchKeyword;  // 현재 검색어를 업데이트
        $('#participant-list-body').html('<tr><td colspan="8" class="loading-contents">참가자 목록을 불러오는 중...</td></tr>');
        $('#no-data-message').hide();

        // API URL에 grade 파라미터 추가
        let apiUrl = `/api/participants/paginated-list?page=${currentPage}&size=${listPerPage}`;
        if (currentGradeFilter) {  // 등급 필터가 있으면 추가
            apiUrl += `&grade=${currentGradeFilter}`;
        }
        if (currentStartDate && currentEndDate) {  // 날짜 범위가 있으면 추가
            apiUrl += `&startDate=${currentStartDate}&endDate=${currentEndDate}`;
        }
        if (currentSearchKeyword) {  // 검색어가 있으면 추가
            apiUrl += `&searchKeyword=${encodeURIComponent(currentSearchKeyword)}`;  // 검색어 URL 인코딩
        }
        $.ajax({
            url: apiUrl,  // 수정된 API URL을 사용
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            success: function (response) {
                const participants = response.participants;
                const totalCount = response.totalCount;
                const totalPages = response.totalPages;
                $('#participant-list-body').empty();  // 기존 목록 비우기

                if (participants && participants.length > 0) {
                    $.each(participants, function (index, participant) {
                        const gradeDropdown = `
                            <select class="grade-select" data-participant-id="${participant.participantId}" data-participant-name="${participant.participantName}">
                                ${gradeOptions.map(option => `<option value="${option}" ${(participant.grade === option) ? 'selected' : ''}>${option}</option>`).join('')}
                            </select>
                        `;
                        // const playRecordsBtn = `
                        //     <button class="play-records-button" data-participant-id="${participant.participantId}" title="음성녹음 재생"><i class="fas fa-play-circle"></i></button>
                        // `;
                        const playRecordsBtn = (participant.audioRecordList && participant.audioRecordList.length > 0)
                            ? `<button class="play-records-button" data-participant-id="${participant.participantId}" title="음성녹음 재생"><i class="fas fa-play-circle"></i></button>`
                            : '-';
                        // 테이블 행 생성
                        const row = `
                            <tr>
                                <td>${participant.listNumber}</td>
                                <td>${participant.participantName}</td>
                                <td>${participant.birthYearMonth}</td>
                                <td>${participant.genderKor}</td>
                                <td>${gradeDropdown}</td>
                                <td>${participant.assignedAdminName || '-'}</td>
                                <td>${playRecordsBtn}</td>
                                <td>${participant.firstRecordDate || '-'}</td>
                            </tr>
                        `;
                        $('#participant-list-body').append(row);
                    });
                    $('#no-data-message').hide();
                } else {
                    // 목록이 없을 경우 메시지 표시
                    $('#participant-list-body').append('<tr><td colspan="8" class="no-data">표시할 참가자 목록이 없습니다.</td></tr>');
                    $('#no-data-message').hide();
                }
                // 페이지네이션 컨트롤 그리기 (currentGradeFilter를 인자로 전달)
                renderPaginationControls(totalPages, currentPage, listPerPage, function (targetPage) {
                    loadParticipantList(targetPage, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
                });
                // 콘텐츠 로드 후 스크롤 상태를 확인
                if (typeof window.adjustStatisticsContainerWidthBasedOnScroll === 'function') {
                    window.adjustStatisticsContainerWidthBasedOnScroll();
                }
            },
            error: function (xhr) {
                var errorMsg = '참가자 목록 로드 실패: ' + (xhr.responseText || '알 수 없는 오류');
                try {
                    // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                    var jsonError = JSON.parse(xhr.responseText);
                    if (jsonError.message) {
                        errorMsg = '참가자 목록 로드 실패: ' + jsonError.message;
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }
                $('#participant-list-body').empty();
                $('#no-data-message').show().html(
                    // `<p style="color: red;">참가자 목록 조회 중 오류가 발생했습니다: ${xhr.status} ${xhr.statusText}</p>`
                    `<p style="color: red;">참가자 목록 조회 중 오류가 발생했습니다.</p>`
                );
                $('#pagination-controls').empty();  // 오류 시 페이지네이션 초기화
                alert(errorMsg);
                console.error("참가자 목록 로드 실패: ", xhr.responseText);
            }
        });
    }

    // 페이지네이션 컨트롤 그리기 함수 --------------------------------------------------------------------------------------
    function renderPaginationControls(totalPages, currentPage, pageSize, pageLoader) {
        const paginationContainer = $('#pagination-controls');
        paginationContainer.empty();  // 기존 컨트롤 비우기

        if (totalPages <= 1) {
            // 페이지 수가 1 이하라면 페이지네이션 불필요
            return;
        }
        // 한 번에 표시할 페이지 번호 버튼(링크) 수
        const maxPageButtons = 5;
        // 현재 페이지 블록 (0부터 시작)
        const currentBlock = Math.floor((currentPage - 1) / maxPageButtons);

        // << (처음으로)
        paginationContainer.append(
            `<a href="#" class="page-link ${(currentPage === 1) ? 'disabled' : ''}" data-page="1">&lt;&lt;</a>`
        );
        // < (이전 블록)
        const prevBlockPage = currentBlock * maxPageButtons;
        paginationContainer.append(
            `<a href="#" class="page-link ${(currentPage <= maxPageButtons) ? 'disabled' : ''}" data-page="${(prevBlockPage > 0) ? prevBlockPage : 1}">&lt;</a>`
        );
        // 페이지 번호 버튼(링크) 설정
        const startPage = currentBlock * maxPageButtons + 1;
        const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
        for (let i = startPage; i <= endPage; i++) {
            paginationContainer.append(
                `<a href="#" class="page-link ${(i === currentPage) ? 'active' : ''}" data-page="${i}">${i}</a>`
            );
        }
        // > (다음 블록)
        const nextBlockPage = (currentBlock + 1) * maxPageButtons + 1;
        paginationContainer.append(
            `<a href="#" class="page-link ${(nextBlockPage > totalPages) ? 'disabled' : ''}" data-page="${(nextBlockPage <= totalPages) ? nextBlockPage : totalPages}">&gt;</a>`
        );
        // >> (마지막으로)
        paginationContainer.append(
            `<a href="#" class="page-link ${(currentPage === totalPages) ? 'disabled' : ''}" data-page="${totalPages}">&gt;&gt;</a>`
        );

        // 페이지 번호 버튼(링크) 클릭 이벤트 바인딩
        paginationContainer.off('click', '.page-link')
                           .on('click', '.page-link', function (e) {
            e.preventDefault();  // 기본 링크 동작 방지
            const targetPage = $(this).data('page');
            if (targetPage && !$(this).hasClass('active') && !$(this).hasClass('disabled')) {
                pageLoader(targetPage);
            }
        });
    }

    // 리스트 다운로드 아이콘 버튼 클릭 이벤트 --------------------------------------------------------------------------------
    var $excelDownloadBtn = $('#excelDownloadBtn');

    $excelDownloadBtn.on('click', function () {
        let downloadUrl = '/api/participants/export-excel?';
        if (currentGradeFilter) {  // 등급 필터가 있으면 추가
            downloadUrl += `grade=${currentGradeFilter}&`;
        }
        if (currentStartDate && currentEndDate) {  // 날짜 범위가 있으면 추가
            downloadUrl += `startDate=${currentStartDate}&endDate=${currentEndDate}&`;
        }
        if (currentSearchKeyword) {  // 검색어가 있으면 추가
            downloadUrl += `searchKeyword=${encodeURIComponent(currentSearchKeyword)}&`;  // 검색어 URL 인코딩
        }
        downloadUrl = (downloadUrl.endsWith('&')) ? downloadUrl.slice(0, -1) : downloadUrl;  // 마지막 '&' 제거

        $.ajax({
            url: downloadUrl,
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            xhrFields: {
                responseType: 'blob'  // 응답을 Blob으로 받음을 명시
            },
            success: function (blob, status, xhr) {
                // Content-Disposition 헤더에서 파일명을 추출
                let filename = "data-statistics-list.xlsx";  // 기본 파일명
                const contentDisposition = xhr.getResponseHeader('Content-Disposition');
                if (contentDisposition) {
                    const filenameMatch = contentDisposition.match(/filename="(.+)"/);
                    if (filenameMatch && filenameMatch[1]) {
                        try {
                            // UTF-8로 인코딩된 파일명을 다시 디코딩하여 사용
                            filename = decodeURIComponent(filenameMatch[1]);
                        } catch (e) {
                            console.warn("파일명 디코딩에 실패하여 기본 이름을 사용합니다.");
                        }
                    }
                }
                // Blob을 이용하여 파일을 다운로드
                // 여기서는 <a> 태그를 동적으로 생성하고 JWT를 포함하여 요청하는 방식을 사용
                // (실제 파일 탐색기를 여는 동작과 유사하게 Content-Disposition 헤더를 사용하여 다운로드 파일명을 자동으로 설정(매핑))
                const blobUrl = window.URL.createObjectURL(blob);  // Blob을 이용한 URL 생성
                const aTag = document.createElement('a');  // <a> 태그를 동적으로 생성
                aTag.style.display = 'none';  // 실제 화면에는 보이지 않게 처리
                aTag.href = blobUrl;
                aTag.download = filename;  // 추출된 파일명을 사용
                // <a> 태그 링크를 클릭하여 엑셀 파일 다운로드를 시작
                document.body.appendChild(aTag);
                aTag.click();
                // Blob을 이용한 URL과 <a> 태그 링크를 제거
                window.URL.revokeObjectURL(blobUrl);
                aTag.remove();
                alert("데이터 통계 및 리스트를 엑셀 파일로 다운로드합니다.");
            },
            error: function (xhr) {
                let errorMsg = '엑셀 파일 다운로드 실패: ' + (xhr.responseText || '알 수 없는 오류');
                if (xhr.responseJSON && xhr.responseJSON.message) {  // 일반 JSON 방식의 에러 응답
                    errorMsg = xhr.responseJSON.message;
                    alert("엑셀 파일 다운로드 실패: " + errorMsg);
                    console.error("엑셀 파일 다운로드 실패: ", errorMsg);
                } else if (xhr.responseText) {  // 텍스트 형태의 에러 응답
                    try {
                        const blobError = new Blob([xhr.responseText], { type: 'text/plain' });
                        const fileReader = new FileReader();
                        fileReader.onload = function () {
                            errorMsg = fileReader.result;
                            alert("엑셀 파일 다운로드 실패: " + errorMsg);
                            console.error("엑셀 파일 다운로드 실패: ", errorMsg);
                        };
                        fileReader.readAsText(blobError);
                        return;  // 비동기 알림을 위해 즉시 리턴
                    } catch (e) {
                        // Blob 처리 중 에러 발생 시 기본 메시지 사용
                    }
                } else {  // 그 외의 에러 응답인 경우
                    alert(errorMsg);
                    console.error(errorMsg);
                }
            }
        });
    });

    // 필터 컨트롤 그룹 관련 로직 ------------------------------------------------------------------------------------------
    // 등급 라디오 버튼 변경 이벤트 -----------------------------------------------------------------------------------------
    // (동적으로 생성되는 버튼에 이벤트를 위임(바인딩)하기 위해 $(document).off().on() 사용)
    $(document).off('change', 'input[name="gradeFilter"]')
               .on('change', 'input[name="gradeFilter"]', function () {
        const selectedGrade = $(this).val();
        localStorage.setItem('gradeFilter', selectedGrade);

        // 등급 필터링 적용 시 1 페이지부터 다시 로드
        loadParticipantList(1, selectedGrade, currentStartDate, currentEndDate, currentSearchKeyword);
    });

    // 기간 달력 팝업의 날짜 선택 이벤트 ------------------------------------------------------------------------------------
    var $dateRangePicker = $('#dateRangePicker');
    var isCalendarApplied = false;

    // Date-Range-Picker 초기화 및 locale 설정
    $dateRangePicker.daterangepicker({
        alwaysShowCalendars: true,    // 항상 달력 2개를 표시
        singleDatePicker: false,
        showDropdowns: true,          // 연도와 월의 드롭다운 표시
        autoUpdateInput: false,       // 기간 입력 필드의 자동 업데이트 끔 (직접 포맷팅)
        startDate: defaultStartDate,  // Moment 객체로 초기 시작일 설정
        endDate: defaultEndDate,      // Moment 객체로 초기 종료일 설정
        // autoApply: false,             // 캘린더 외부 클릭 시 자동 적용(닫힘) 방지
        drops: 'auto',                // 캘린더를 자동으로 가장 적합한 위치에 표시
        locale: {  // 직접 한국어로 텍스트 정의
            format: 'YYYY/MM/DD',  // 기간 입력 필드에 표시될 날짜 형식
            separator: ' ~ ',      // 시작일과 종료일 구분자
            applyLabel: '적용',
            cancelLabel: '취소',
            fromLabel: '시작일',
            toLabel: '종료일',
            customRangeLabel: '사용자 지정',
            weekLabel: '주',
            daysOfWeek: ['일', '월', '화', '수', '목', '금', '토'],
            monthNames: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
            firstDay: 0  // 한 주의 첫 날을 지정 (0: 일요일)
        },
        // ranges: {  // 사전에 정의된 날짜 범위
        //     '오늘': [moment(), moment()],
        //     '어제': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
        //     '지난 7일': [moment().subtract(6, 'days'), moment()],
        //     '지난 30일': [moment().subtract(29, 'days'), moment()],
        //     '이번 달': [moment().startOf('month'), moment().endOf('month')],
        //     '지난 달': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
        // }
    }, function (start, end, label) {  // 적용 버튼 또는 캘린더 외부 클릭 시 호출되는 콜백
        // 캘린더 외부 클릭 시에는 기간이 설정되면 안되므로
        // 기간을 적용하는 로직은 on('apply.daterangepicker') 이벤트로 대체
    }).on('show.daterangepicker', function (ev, picker) {  // 캘린더가 표시될 때마다 실행
        isCalendarApplied = false;

    }).on('hide.daterangepicker', function (ev, picker) {  // 캘린더가 숨겨질 때 실행
        // 적용 버튼 클릭 이벤트를 기다리기 위해 setTimeout 함수 사용
        setTimeout(function () {
            if (!isCalendarApplied) {  // 캘린더 외부를 클릭했을 경우로 간주
                // 기간 입력 필드를 마지막으로 선택했던 값으로 되돌림 (혹은 기본값으로 원복)
                $dateRangePicker.val(moment(currentStartDate).format('YYYY/MM/DD') + ' ~ ' + moment(currentEndDate).format('YYYY/MM/DD'));
                $dateRangePicker.data('daterangepicker').setStartDate(moment(currentStartDate).format('YYYY/MM/DD'));
                $dateRangePicker.data('daterangepicker').setEndDate(moment(currentEndDate).format('YYYY/MM/DD'));
            } else {  // isCalendarApplied == true
                isCalendarApplied = false;
            }
        }, 0);  // 0: 짧은 지연 시간
    });

    // 적용 버튼 클릭 이벤트
    $dateRangePicker.on('apply.daterangepicker', function (ev, picker) {
        isCalendarApplied = true;

        // 기간 입력 필드를 업데이트
        $dateRangePicker.val(picker.startDate.format('YYYY/MM/DD') + ' ~ ' + picker.endDate.format('YYYY/MM/DD'));
        // 선택된 날짜는 YYYY-MM-DD 형식으로 백엔드에 전송
        const newStartDate = picker.startDate.format('YYYY-MM-DD');
        const newEndDate = picker.endDate.format('YYYY-MM-DD');
        if (newStartDate !== currentStartDate || newEndDate !== currentEndDate) {
            // 기간 필터링(날짜 변경) 적용 시 1 페이지부터 다시 로드
            loadParticipantList(1, currentGradeFilter, newStartDate, newEndDate, currentSearchKeyword);
            localStorage.setItem('startDate', newStartDate);
            localStorage.setItem('endDate', newEndDate);
        }
    });
    // 취소 버튼 클릭 이벤트
    $dateRangePicker.on('cancel.daterangepicker', function (ev, picker) {
        // 기간 입력 필드를 마지막으로 선택했던 값으로 되돌림 (혹은 기본값으로 원복)
        $dateRangePicker.val(moment(currentStartDate).format('YYYY/MM/DD') + ' ~ ' + moment(currentEndDate).format('YYYY/MM/DD'));
        $dateRangePicker.data('daterangepicker').setStartDate(moment(currentStartDate).format('YYYY/MM/DD'));
        $dateRangePicker.data('daterangepicker').setEndDate(moment(currentEndDate).format('YYYY/MM/DD'));
    });
    // Date-Range-Picker가 로드되면 초기에 표시할 값을 설정
    $dateRangePicker.val(defaultStartDate.format('YYYY/MM/DD') + ' ~ ' + defaultEndDate.format('YYYY/MM/DD'));

    // 검색 버튼 클릭 및 검색 입력 필드의 Enter 키 이벤트 ---------------------------------------------------------------------
    var $searchKeyword = $('#searchKeyword');
    var $executeSearchBtn = $('#executeSearchBtn');
    var $clearSearchBtn = $('#clearSearchBtn');

    // 검색 버튼 클릭 이벤트
    $executeSearchBtn.on('click', function () {
        const inputtedKeyword = $searchKeyword.val().trim();
        if (inputtedKeyword !== currentSearchKeyword) {  // 검색어가 변경되었을 때만 검색을 실행
            // 검색어 필터링 적용 시 1 페이지부터 다시 로드
            loadParticipantList(1, currentGradeFilter, currentStartDate, currentEndDate, inputtedKeyword);
            localStorage.setItem('searchKeyword', inputtedKeyword);
        }
    });
    // 검색 입력 필드의 Enter 키 이벤트
    $searchKeyword.on('keypress', function (event) {
        if (event.which === 13) {  // 13: Enter 키 코드
            event.preventDefault();  // 기본 Enter 동작(폼 제출 등) 방지
            $executeSearchBtn.click();  // 검색 버튼을 직접 클릭
        }
    });

    // X(지우기) 버튼 클릭 이벤트
    $clearSearchBtn.on('click', function () {
        $searchKeyword.val('');  // 검색 입력 필드 초기화
        currentSearchKeyword = '';  // 검색어 초기화
        localStorage.setItem('searchKeyword', currentSearchKeyword);

        // 검색 필터 해제 후 1 페이지부터 다시 로드
        loadParticipantList(1, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
    });
    // 필터 컨트롤 그룹 관련 로직 ------------------------------------------------------------------------------------------

    // 특정 참가자의 등급을 업데이트하는 함수 ---------------------------------------------------------------------------------
    window.changeParticipantGrade = function (participantId, participantName, newGrade) {
        if (!confirm(`참가자, ${participantName}님의 등급을 ${newGrade}(으)로 변경하시겠습니까?`)) {
            // 사용자가 취소하면 등급을 이전 값으로 되돌림 (API 호출을 중단)
            // 현재 페이지를 다시 로드하여 초기 상태로 복원
            loadParticipantList(currentPage, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
            return;
        }
        $.ajax({
            url: '/api/participants/grade',
            type: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            contentType: 'application/json',
            data: JSON.stringify({
                participantId: participantId,
                newGrade: newGrade
            }),
            success: function (response) {
                alert(response);  // 서버에서 보낸 성공 메시지 (text/plain)

                // 등급변경 성공 시 현재 페이지 새로고침
                loadParticipantList(currentPage, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
            },
            error: function (xhr) {
                var errorMsg = '참가자 등급 변경 실패: ' + (xhr.responseText || '알 수 없는 오류');
                try {
                    // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                    var jsonError = JSON.parse(xhr.responseText);
                    if (jsonError.message) {
                        errorMsg = '참가자 등급 변경 실패: ' + jsonError.message;
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }
                alert(errorMsg);
                console.error("참가자 등급 변경 실패: ", xhr.responseText);
                // 등급변경 실패 시 현재 페이지를 새로고침하여 이전 값 유지
                loadParticipantList(currentPage, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
            }
        });
    };

    // 음성녹음 상세 정보 및 오디오 플레이어 모달 숨기기 -----------------------------------------------------------------------
    function hideRecordDetailsModal() {
        // 모든 오디오 플레이어 중지
        $('.audio-tracks-container').find('audio').each(function () {
            this.pause();  // 재생 중지
            this.currentTime = 0;  // 재생 위치 초기화
        });
        // 오디오 플레이어 모달 숨기기
        $recordDetailsModal.removeClass('show');
    }

    // 팝업창(오디오 플레이어) 닫기 버튼 클릭 이벤트
    $closePopupBtn.click(function () {
        hideRecordDetailsModal();
    });
    // 팝업창(오디오 플레이어) 외부 영역 클릭 이벤트
    $(window).click(function (event) {
        if ($(event.target).is($recordDetailsModal)) {
            // hideRecordDetailsModal();
        }
    });

    // 공지사항 이동 링크 클릭 이벤트 ---------------------------------------------------------------------------------------
    $('#goToNoticeLink').click(function (e) {
        e.preventDefault();  // 기본 링크 동작 방지
        const targetUrl = $(this).attr('href');  // 공지사항 내용을 가져올 URL
        window.loadDataContent(targetUrl);
    });
    /**
     * 데이터(Statistics, Notice) 콘텐츠 로드 함수 ------------------------------------------------------------------------
     * @param {string} targetUrl - AJAX 요청을 보낼 서버측 URL (콘텐츠를 반환하는 컨트롤러 엔드포인트)
     * @param {boolean} pushState - pushState를 실행하여 히스토리 스택에 저장할지 여부
     */
    window.loadDataContent = function (targetUrl, pushState = true) {
        var $dataContentArea = $('#data-content-area');
        $dataContentArea.html('<p class="loading-contents">콘텐츠 로딩 중...</p>');  // 로딩 메시지
        // 콘텐츠 로드 시작 시 is-loading 클래스 제거(초기화) 및 다시 추가
        $dataContentArea.removeClass('is-loading');
        $dataContentArea.addClass('is-loading');

        $.ajax({
            url: targetUrl,
            type: 'GET',
            dataType: 'html',  // 응답을 HTML로 받음을 명시
            success: function (response) {
                $dataContentArea.html(response);  // 응답 HTML을 DOM에 추가
                // 콘텐츠 로드 완료 시 is-loading 클래스 제거
                $dataContentArea.removeClass('is-loading');
                $('#contentTitle').text('데이터 통계');

                // 해당 JSP 파일을 로드 시 HTML 내부의 스크립트(<script> 태그)를 찾아서 실행
                $(response).filter('script').each(function (){
                    $.globalEval(this.text || this.textContent || this.innerHTML || '');
                });
                // 콘텐츠 로드 후 스크롤바 상태에 따라 각 컨테이너(서브 화면)의 너비를 조절
                if (targetUrl.includes('/statistics')) {
                    // 로드된 콘텐츠가 데이터 통계라면 현재(첫) 페이지 참가자 목록을 다시 로드
                    loadParticipantList(currentPage, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
                } else if (targetUrl.includes('/notice')) {
                    if (typeof window.adjustNoticeContainerWidthBasedOnScroll === 'function') {
                        window.adjustNoticeContainerWidthBasedOnScroll();
                    }
                }
                // 웹 브라우저의 뒤로 가기/앞으로 가기를 위한 히스토리 설정
                if (pushState) {
                    // popstate로 인한 호출이 아니면 pushState 실행
                    window.history.pushState({ path: targetUrl }, '', targetUrl);
                }
            },
            error: function (xhr) {
                $dataContentArea.html(
                    '<p class="loading-contents" style="color: red;">' +
                    '콘텐츠를 불러오는데 실패했습니다: ' + xhr.status + ' ' + xhr.statusText + '</p>'
                );
                // 오류 발생 시에도 is-loading 클래스 제거
                // $dataContentArea.removeClass('is-loading');
                $('#contentTitle').text('오류 발생');
                console.error("Failed to load data content from: " + targetUrl, xhr);
            }
        });
    };

    // 페이지 진입 시(DOM 로드 완료 후) 첫 페이지 참가자 목록을 로드 ------------------------------------------------------------
    // (초기 페이지는 등급별, 기간별, 검색어 필터링 없이 로드)
    const savedBackPressed = localStorage.getItem('backPressed');
    if (savedBackPressed === 'true') {
        const savedGradeFilter = localStorage.getItem('gradeFilter');
        if (savedGradeFilter) {
            $(`input:radio[name="gradeFilter"]:input[value="${savedGradeFilter}"]`).prop('checked', true);
        } else {  // savedGradeFilter == null
            $(`input:radio[name="gradeFilter"]:input[value="${currentGradeFilter}"]`).prop('checked', true);
        }
        const savedStartDate = localStorage.getItem('startDate');
        const savedEndDate = localStorage.getItem('endDate');
        if (savedStartDate && savedEndDate) {
            $dateRangePicker.val(moment(savedStartDate).format('YYYY/MM/DD') + ' ~ ' + moment(savedEndDate).format('YYYY/MM/DD'));
            $dateRangePicker.data('daterangepicker').setStartDate(moment(savedStartDate).format('YYYY/MM/DD'));
            $dateRangePicker.data('daterangepicker').setEndDate(moment(savedEndDate).format('YYYY/MM/DD'));
        } else {  // savedStartDate == null || savedEndDate == null
            $dateRangePicker.val(moment(currentStartDate).format('YYYY/MM/DD') + ' ~ ' + moment(currentEndDate).format('YYYY/MM/DD'));
            $dateRangePicker.data('daterangepicker').setStartDate(moment(currentStartDate).format('YYYY/MM/DD'));
            $dateRangePicker.data('daterangepicker').setEndDate(moment(currentEndDate).format('YYYY/MM/DD'));
        }
        const savedSearchKeyword = localStorage.getItem('searchKeyword');
        if (savedSearchKeyword) {
            $searchKeyword.val(`${savedSearchKeyword}`);
        } else {  // savedSearchKeyword == null
            $searchKeyword.val(`${currentSearchKeyword}`);
        }
        localStorage.removeItem('backPressed');
        loadParticipantList(currentPage,
            (savedGradeFilter) ? savedGradeFilter : currentGradeFilter,
            (savedStartDate) ? savedStartDate : currentStartDate,
            (savedEndDate) ? savedEndDate : currentEndDate,
            (savedSearchKeyword) ? savedSearchKeyword : currentSearchKeyword
        );
    } else {  // savedBackPressed == null
        localStorage.removeItem('gradeFilter');
        localStorage.removeItem('startDate');
        localStorage.removeItem('endDate');
        localStorage.removeItem('searchKeyword');
        loadParticipantList(currentPage, currentGradeFilter, currentStartDate, currentEndDate, currentSearchKeyword);
    }

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

$(document).ready(function () {
    // 등급 드롭다운 변경 이벤트 -------------------------------------------------------------------------------------------
    // (동적으로 생성되는 버튼에 이벤트를 위임(바인딩)하기 위해 $(document).off().on() 사용)
    $(document).off('change', '.grade-select')
               .on('change', '.grade-select', function () {
        const participantId = $(this).data('participant-id');
        const participantName = $(this).data('participant-name');
        const newGrade = $(this).val();

        // 해당 참가자 정보와 변경할 등급을 전달하여 업데이트 수행
        if (typeof window.changeParticipantGrade === 'function') {
            window.changeParticipantGrade(participantId, participantName, newGrade);
        }
    });

    // 음성녹음 재생 버튼 클릭 이벤트 ---------------------------------------------------------------------------------------
    // (동적으로 생성되는 버튼에 이벤트를 위임(바인딩)하기 위해 $(document).off().on() 사용)
    $(document).off('click', '.play-records-button')
               .on('click', '.play-records-button', function () {
        const participantId = $(this).data('participant-id');

        // 해당 참가자 정보를 전달하여 오디오 플레이어 모달 표시
        showRecordDetailsModal(participantId);
    });

    // 음성녹음 상세 정보 및 오디오 플레이어 모달 표시 -------------------------------------------------------------------------
    function showRecordDetailsModal(participantId) {
        // 기존 오디오 플레이어 중지 및 초기화
        stopAllAudioPlayersInModal();
        resetAllAudioPlayersInModal();

        // 특정 참가자 정보 및 음성녹음 데이터를 로드
        $.ajax({
            url: `/api/participants/${participantId}/record-details`,
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + window.getJwtTokenFromCookie()  // 인증 토큰 포함 (웹 브라우저용)
            },
            success: function (response) {
                const participantInfo = response.participantInfo;  // 특정 참가자 정보
                const audioRecords = response.audioRecords;  // 3개 음성녹음 데이터 리스트

                // 특정 참가자 정보 업데이트
                $('#p-name').text(participantInfo.participantName || '-');
                $('#p-birth-year-month').text(participantInfo.birthYearMonth || '-');
                $('#p-gender-kor').text(participantInfo.genderKor || '-');
                $('#p-grade').text(participantInfo.grade || '-');
                $('#p-assigned-admin-name').text(participantInfo.assignedAdminName || '-');

                // 3개 음성녹음 데이터 업데이트
                for (let i = 1; i <= 3; i++) {
                    const audioPlayer = $(`#audio-track-${i} audio`)[0];
                    const audioDateSpan = $(`#audio-date-${i}`);
                    audioPlayer.src = '';  // audio src 초기화
                    audioDateSpan.text('-');  // 녹음 수정일자 초기화

                    const audioRecord = audioRecords.find(r => (r.recordSequence === i));  // 해당 순서의 음성녹음 찾기
                    if (audioRecord) {
                        audioPlayer.src = `/api/records/play/${audioRecord.recordId}`;
                        audioPlayer.type = audioRecord.mimeType;  // 또는 'audio/mp4'로 고정
                        audioDateSpan.text(audioRecord.updatedAt || '-');
                    }
                    audioPlayer.load();  // 오디오 플레이어 로드
                    // audioPlayer.play();  // 자동 재생 (주석처리: 재생은 사용자가 직접 버튼을 클릭)
                }
                $('#recordDetailsModal').addClass('show');  // 오디오 플레이어 모달 표시
            },
            error: function (xhr) {
                var errorMsg = '참가자 음성녹음 상세 로드 실패: ' + (xhr.responseText || '알 수 없는 오류');
                try {
                    // JSON 응답일 경우 메시지 파싱 시도 (AccessDeniedHandler에서 JSON을 반환)
                    var jsonError = JSON.parse(xhr.responseText);
                    if (jsonError.message) {
                        errorMsg = '참가자 음성녹음 상세 로드 실패: ' + jsonError.message;
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }
                $('#recordDetailsModal').removeClass('show');  // 오류 시 오디오 플레이어 모달 숨기기
                alert(errorMsg);
                console.error("참가자 음성녹음 상세 로드 실패: ", xhr.responseText);
            }
        });
    }

    // 음성녹음 상세 정보 및 오디오 플레이어 모달 중지/초기화 -------------------------------------------------------------------
    // 모달 내 모든 오디오 플레이어 중지
    function stopAllAudioPlayersInModal() {
        $('.audio-tracks-container').find('audio').each(function () {
            this.pause();  // 재생 중지
            this.currentTime = 0;  // 재생 위치 초기화
        });
    }
    // 모달 내 모든 오디오 플레이어의 src 초기화
    // (오디오 플레이어 모달을 열 때마다 이전 정보가 남아있는 것을 방지)
    function resetAllAudioPlayersInModal() {
        $('.audio-tracks-container').find('audio').each(function () {
            this.removeAttribute('src');
            this.removeAttribute('type');
        });
    }

    // 웹 브라우저의 뒤로 가기/앞으로 가기 버튼 처리 --------------------------------------------------------------------------
    $(window).on('popstate', function (event) {
        const state = event.originalEvent.state;
        if (state && state.path) {  // 앞으로 가기 버튼 클릭
            // 히스토리 스택에 저장된 URL로 콘텐츠를 로드 (popstate 시에는 pushState를 실행하지 않음)
            if (typeof window.loadDataContent === 'function') {
                window.loadDataContent(state.path, false);
            }
        } else {  // 뒤로 가기 버튼 클릭
            // 히스토리가 초기 상태로 돌아간 경우 데이터 통계 및 리스트 콘텐츠를 로드
            // /contents/statistics URL은 사이드바 메뉴를 통해 호출되므로
            // 데이터 통계 페이지로 돌아가기 위해 사이드바 메뉴를 직접 클릭
            localStorage.setItem('backPressed', 'true');
            $("#sidebar-statistics").click();
        }
    });
});
