package com.asrevo.cvhome.catalog.config;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final StoreMerchantIdArgumentResolver storeMerchantIdArgumentResolver;

	private final LanguageCodeArgumentResolver languageCodeArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(storeMerchantIdArgumentResolver);
		argumentResolvers.add(languageCodeArgumentResolver);
	}

}
