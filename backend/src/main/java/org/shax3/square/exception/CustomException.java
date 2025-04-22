package org.shax3.square.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    int code;
    String message;

    public CustomException(ExceptionCode exceptionCode) {
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
    }
}
