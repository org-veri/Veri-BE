package org.veri.be.global.auth.annotations;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.veri.be.global.auth.guards.Guard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SecurityRequirement(name = "bearerAuth")
public @interface UseGuards {

    Class<? extends Guard>[] value();
}
