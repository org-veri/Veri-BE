package org.goorm.veri.veribe.domain.auth.annotation;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.*;

@Documented
@Parameter(hidden = true)
@Target({
        ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedMember {
}
