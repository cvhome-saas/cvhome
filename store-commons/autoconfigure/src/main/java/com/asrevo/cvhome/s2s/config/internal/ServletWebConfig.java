package com.asrevo.cvhome.s2s.config.internal;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
@Import(ServletWebConfig.InternalServletWebConfig.class)
public class ServletWebConfig {

	@Configuration
	static class InternalServletWebConfig implements WebMvcConfigurer {

		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
			PageableHandlerMethodArgumentResolver e = new PageableHandlerMethodArgumentResolver();
			e.setPageParameterName("page");
			e.setSizeParameterName("count");
			argumentResolvers.add(e);
			argumentResolvers.add(new ServletOrgStorePrincipalInfoArgumentResolver());
		}

	}

}
