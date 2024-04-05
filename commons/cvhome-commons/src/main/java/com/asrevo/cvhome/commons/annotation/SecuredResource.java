package com.asrevo.cvhome.commons.annotation;

import com.asrevo.cvhome.commons.domain.Roles;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredResource {
    String value() default "";

    Roles[] roles() default {};


}
