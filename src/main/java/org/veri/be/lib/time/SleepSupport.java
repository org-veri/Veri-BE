package org.veri.be.lib.time;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SleepSupport {

    public void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
