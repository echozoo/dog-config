package com.echozoo.config.common;

public enum ErrorCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(40000, "参数校验失败"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "唯一性冲突"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
