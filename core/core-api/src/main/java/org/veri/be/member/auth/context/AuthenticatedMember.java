package org.veri.be.member.auth.context;

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
