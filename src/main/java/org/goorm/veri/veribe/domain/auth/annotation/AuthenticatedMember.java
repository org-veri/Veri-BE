package org.goorm.veri.veribe.domain.auth.annotation;

import java.lang.annotation.*;

@Documented
@Target({
        ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedMember {
}
