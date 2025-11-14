package kr.co.jaehoon.springboottemplate.dto.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Result {

    OK("0001", "서버통신 성공"),
    NG("0000", "서버응답 실패");

    private final String code;
    private final String message;
}
