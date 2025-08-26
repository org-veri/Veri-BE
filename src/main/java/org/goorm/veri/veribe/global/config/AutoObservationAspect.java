package org.goorm.veri.veribe.global.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 10) // (권장) 트랜잭션/시큐리티 프록시 뒤에서 실행
public class AutoObservationAspect {

    private final ObservationRegistry registry;

    @Around("execution(public * org.goorm.veri.veribe..*(..))"
            + " && !within(org.springframework..*)"
            + " && !within(javax..*) && !within(jakarta..*)")
    public Object observeAll(ProceedingJoinPoint pjp) throws Throwable {
        Class<?> targetClass = ClassUtils.getUserClass(pjp.getTarget());
        String className = targetClass.getSimpleName();
        String method = pjp.getSignature().getName();
        String spanName = className + "." + method;

        Observation obs = Observation.start(spanName, registry)
                .lowCardinalityKeyValue("class", targetClass.getName())
                .lowCardinalityKeyValue("method", method);

        try {
            return pjp.proceed();
        } catch (Throwable t) {
            obs.error(t);
            throw t;
        } finally {
            obs.stop();
        }
    }
}
