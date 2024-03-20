package com.asrevo.cvhome.dcm.config;

import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.dcm.commons.domain.CertificateId;
import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
import com.asrevo.cvhome.dcm.entity.FileId;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(CertificateId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(DomainId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(OrdersId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(FileId.class, String.class);
    }
}
