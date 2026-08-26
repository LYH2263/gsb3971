package com.hanyu.learning.common.exception;

public class AppException extends RuntimeException {

    private final int httpStatus;
    private final int code;

    public AppException(int httpStatus, int code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }
}
