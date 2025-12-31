package org.veri.be.monitoring.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AutoObservationAspect {

    private final ObservationRegistry registry;

    private static final List<String> EXCLUDED_PACKAGES = List.of(
            ".config.",
            ".lib.",
            ".dto.",
            ".vo.",
            ".exception.",
            ".util.",
            ".entity."
    );

    @Pointcut("!within(org.springframework..*) " +
            "&& !within(io.micrometer..*) " +
            "&& !within(io.opentelemetry..*) " +
            "&& !within(java..*) " +
            "&& !within(javax..*) " +
            "&& !within(jakarta..*)")
    public void excludeInfrastructure() {
    }

    @Pointcut("execution(public * org.veri.be..*(..))" +
            "&& (within(@org.springframework.stereotype.Service *)" +
            "|| within(@org.springframework.web.bind.annotation.RestController *)" +
            "|| within(@org.springframework.stereotype.Repository *)" +
            "|| within(org.veri.be..service..*))")
    public void targetTargets() {
    }

    @Around("targetTargets() && excludeInfrastructure()")
    public Object observeAll(ProceedingJoinPoint pjp) throws Throwable {
        Class<?> targetClass = ClassUtils.getUserClass(pjp.getTarget());
        if (isExcluded(targetClass)) {
            return pjp.proceed();
        }

        String className = targetClass.getSimpleName();
        String method = pjp.getSignature().getName();
        String spanName = className + "." + method;

        Observation obs = Observation.start(spanName, registry)
                .lowCardinalityKeyValue("class", targetClass.getName())
                .lowCardinalityKeyValue("method", method);

        return obs.observeChecked(() -> pjp.proceed());
    }

    private boolean isExcluded(Class<?> targetClass) {
        String fqcn = targetClass.getName();
        return EXCLUDED_PACKAGES.stream().anyMatch(fqcn::contains);
    }
}
