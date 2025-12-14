package org.veri.be.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock systemClock(@Value("${app.clock.zone:Asia/Seoul}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}
