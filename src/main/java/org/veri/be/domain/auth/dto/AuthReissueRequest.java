package org.veri.be.domain.auth.dto;

import lombok.Getter;

@Getter
public class AuthReissueRequest {
    private String refreshToken;
}
