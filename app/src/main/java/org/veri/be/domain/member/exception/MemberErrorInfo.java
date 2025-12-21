package org.veri.be.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.lib.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum MemberErrorInfo implements ErrorInfo {
    BAD_REQUEST("잘못된 요청입니다.", "M1001"),
    NOT_FOUND("존재하지 않는 회원입니다.", "M1002"),
    ALREADY_EXIST_NICKNAME("이미 사용중인 닉네임입니다.", "M1003");

    private final String message;
    private final String code;
}

