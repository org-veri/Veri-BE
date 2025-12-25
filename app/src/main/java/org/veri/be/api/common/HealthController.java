package org.veri.be.api.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

@RestController
public class HealthController {

    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }

    @GetMapping("/error-test")
    public void errorTest() {
        // grafana 에러 알림 테스트용
        throw ApplicationException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
