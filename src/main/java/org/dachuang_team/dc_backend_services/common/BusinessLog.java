package org.dachuang_team.dc_backend_services.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessLog {
    String module() default "";
    String action() default "";
    String targetType() default "";
    String message() default "";
}
