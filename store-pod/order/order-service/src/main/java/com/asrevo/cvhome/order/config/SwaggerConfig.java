package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;
import java.util.Currency;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	static {
		SpringDocUtils.getConfig().replaceWithClass(Currency.class, String.class);
		SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
		SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
	}

}
