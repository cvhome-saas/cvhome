package com.asrevo.cvhome.subscription.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;

@Configuration
@EnableReactiveMethodSecurity
public class PermissionConfig {

	@Autowired
	public void configurePermissionEvaluator(DefaultMethodSecurityExpressionHandler handler,
			PermissionEvaluator permissionEvaluator) {
		handler.setPermissionEvaluator(permissionEvaluator);
	}

}
