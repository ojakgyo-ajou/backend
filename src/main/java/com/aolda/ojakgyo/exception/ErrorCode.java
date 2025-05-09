package com.aolda.ojakgyo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "잘못된 회원 정보입니다"),
    USER_ARE_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다"),;

    private final HttpStatus status;
    private final String message;
}