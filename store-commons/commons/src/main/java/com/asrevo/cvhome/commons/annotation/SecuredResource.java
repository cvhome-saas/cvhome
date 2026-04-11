package com.asrevo.cvhome.commons.annotation;

import com.asrevo.cvhome.commons.domain.Roles;
import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredResource {

	String value() default "";

	Roles[] roles() default {};

	Roles[] CUSTOMER_ROLES = { Roles.ROLE_CUSTOMER };

	Roles[] STORE_ROLES = { Roles.ROLE_STORE_ADMIN, Roles.ROLE_STORE_MODERATOR };

	Roles[] ORG_ROLES = { Roles.ROLE_ORG_ADMIN };

	Roles[] ORG_STORE_ROLES = { Roles.ROLE_ORG_ADMIN, Roles.ROLE_STORE_ADMIN, Roles.ROLE_STORE_MODERATOR };

	Roles[] INTERNAL_ROLES = { Roles.SCOPE_INTERNAL };

	Roles[] STORE_RETAIL_ROLES = { Roles.ROLE_STORE_RETAIL };

	Roles[] STORE_ADMIN_ROLES = { Roles.ROLE_STORE_ADMIN };

	Roles[] STORE_MODERATOR_ROLES = { Roles.ROLE_STORE_MODERATOR };

	Roles[] SUPER_ROLES = { Roles.ROLE_SUPER_ADMIN };

}
