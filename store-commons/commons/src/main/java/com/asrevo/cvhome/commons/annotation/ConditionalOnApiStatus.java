package com.asrevo.cvhome.commons.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface ConditionalOnApiStatus {
    ApiUsage usage() default ApiUsage.USED;
}
