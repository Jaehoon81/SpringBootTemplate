package kr.co.jaehoon.springboottemplate.dto.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BasicResponse<T> {

    private final String code;  // 서버통신 성공(OK): "0001", 서버응답 실패(NG): "0000"
    private final String message;
    private final T data;

    public static <T> BasicResponse<T> success(T data) {
        return new BasicResponse<>(Result.OK.getCode(), Result.OK.getMessage(), data);
    }

    public static <T> BasicResponse<T> success() {
        return new BasicResponse<>(Result.OK.getCode(), Result.OK.getMessage(), null);
    }

    public static <T> BasicResponse<T> failure(T data) {
        return new BasicResponse<>(Result.NG.getCode(), Result.NG.getMessage(), data);
    }

    public static <T> BasicResponse<T> failure() {
        return new BasicResponse<>(Result.NG.getCode(), Result.NG.getMessage(), null);
    }
}
