package org.goorm.veri.veribe.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.goorm.veri.veribe.global.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum MemberErrorInfo implements ErrorInfo {
    BAD_REQUEST("잘못된 요청입니다.", "M1001"),
    NOT_FOUND("존재하지 않는 회원입니다.", "M1002");

    private final String message;
    private final String code;
}

