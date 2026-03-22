package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnBean(PermissionEvaluator.class)
@Import(ReactivePermissionConfig.PermissionConfig.class)
public class ReactivePermissionConfig {

	@EnableReactiveMethodSecurity
	public static class PermissionConfig {

		@Bean
		public DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler(
				PermissionEvaluator permissionEvaluator) {
			DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
			handler.setPermissionEvaluator(permissionEvaluator);
			return handler;
		}

	}

}
