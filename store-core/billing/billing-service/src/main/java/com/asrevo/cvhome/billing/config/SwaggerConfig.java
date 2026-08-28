package com.asrevo.cvhome.billing.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Renders the identifier value objects as plain strings in the OpenAPI document, which is what they are on the wire.
 */
@Configuration
@SuppressWarnings("java:S1118")
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().replaceWithClass(ManagerOrgId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(StoreMerchantId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(PlanId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(PlanPriceId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(StripeInvoiceId.class, String.class);
    }

}
