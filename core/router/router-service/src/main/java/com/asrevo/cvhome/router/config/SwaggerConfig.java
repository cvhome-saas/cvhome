package com.asrevo.cvhome.router.config;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.commons.domain.ReferenceAlisId;
import com.asrevo.cvhome.router.commons.domain.ReferenceId;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(PodId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ReferenceId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ReferenceAlisId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
    }
}
