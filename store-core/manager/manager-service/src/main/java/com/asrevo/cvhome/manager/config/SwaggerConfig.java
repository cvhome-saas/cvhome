package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.EventId;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(ManagerStoreId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
    }
}
