package com.asrevo.cvhome.controlplane.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.EventId;

@Configuration
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().replaceWithClass(ManagerOrgId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ManagerStoreId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
    }

}
