package com.asrevo.cvhome.commons.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrgStorePrincipalInfo {}
