package org.veri.be.lib.auth.guard;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.veri.be.lib.auth.jwt.utils.MemberExtractor;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class UseGuardsAspect {

    private final ApplicationContext applicationContext;
    private final MemberExtractor memberExtractor;

    @Pointcut(
            "@annotation(org.veri.be.lib.auth.guard.UseGuards) || " +
                    "@within(org.veri.be.lib.auth.guard.UseGuards)"
    )
    public void useGuardsPointcut() {
    }

    @Around("useGuardsPointcut()")
    public Object applyGuards(ProceedingJoinPoint joinPoint) throws Throwable {
        UseGuards useGuards = this.getUseGuardsAnnotation(joinPoint);

        try {
            this.memberExtractor.extractMemberFromToken();
        } catch (ApplicationException e) {
            return this.createErrorResponse(e);
        }

        Class<? extends Guard>[] guardClasses = useGuards.value();

        for (Class<? extends Guard> guardClass : guardClasses) {
            Guard guard = this.applicationContext.getBean(guardClass);

            try {
                guard.canActivate();
            } catch (ApplicationException e) {
                return this.createErrorResponse(e);
            }
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

    private ApiResponse<Map<?, ?>> createErrorResponse(ApplicationException exception) {
        return ApiResponse.error(exception.getErrorInfo(), HttpStatus.UNAUTHORIZED);
    }
}
