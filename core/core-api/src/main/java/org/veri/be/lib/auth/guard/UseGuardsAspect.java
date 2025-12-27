package org.veri.be.lib.auth.guard;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class UseGuardsAspect {

    private final ApplicationContext applicationContext;

    @Pointcut(
            "@annotation(org.veri.be.lib.auth.guard.UseGuards) || " +
                    "@within(org.veri.be.lib.auth.guard.UseGuards)"
    )
    public void useGuardsPointcut() {
    }

    @Around("useGuardsPointcut()")
    public Object applyGuards(ProceedingJoinPoint joinPoint) throws Throwable {
        UseGuards useGuards = this.getUseGuardsAnnotation(joinPoint);

        Class<? extends Guard>[] guardClasses = useGuards.value();

        for (Class<? extends Guard> guardClass : guardClasses) {
            Guard guard = this.applicationContext.getBean(guardClass);
            guard.canActivate();
        }

        return joinPoint.proceed();
    }

    private UseGuards getUseGuardsAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        UseGuards useGuards = methodSignature.getMethod().getAnnotation(UseGuards.class);
        if (useGuards == null) {
            useGuards = joinPoint.getTarget().getClass().getAnnotation(UseGuards.class);
        }
        return useGuards;
    }
}
