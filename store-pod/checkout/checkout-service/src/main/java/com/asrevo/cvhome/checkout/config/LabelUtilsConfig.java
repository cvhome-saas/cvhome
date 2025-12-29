package com.asrevo.cvhome.checkout.config;

import com.asrevo.cvhome.store.utils.LabelUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LabelUtilsConfig {

	@Bean
	public LabelUtils labelUtils() {
		return new LabelUtils();
	}

}
