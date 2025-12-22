package org.veri.be.lib.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.net.URI;

@Slf4j
public class ExceptionLogger {

    public void log(HttpStatus status, String message, URI instance, Exception ex, String tag) {
        if (status.is4xxClientError()) {
            log.info("[{}] {} (path: {})", tag, message, instance);
            if (log.isDebugEnabled()) {
                log.debug("[{}][STACK] (path: {})", tag, instance, ex);
            }
            return;
        }

        log.error("[{}] {} (path: {})", tag, message, instance, ex);
    }
}
