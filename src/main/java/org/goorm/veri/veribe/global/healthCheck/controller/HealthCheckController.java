package org.goorm.veri.veribe.global.healthCheck.controller;

import org.namul.api.payload.code.DefaultResponseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health-check")
@RestController
public class HealthCheckController {

    @GetMapping
    public DefaultResponse<String> healthCheck() {
        return DefaultResponse.ok("성공");
    }

    @GetMapping("/errors")
    public DefaultResponse<String> error() {
        throw new ServerApplicationException(DefaultResponseErrorCode._BAD_REQUEST);
    }
}
