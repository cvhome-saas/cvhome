package com.asrevo.cvhome.checkout.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves to the {@code ShopperId} of the signed-in shopper, or {@code null} when the request carries no shopper
 * token. Tenant checks stay in {@code @PreAuthorize}; this only says who.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentShopper {
}
