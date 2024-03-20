package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.store.commons.domain.ImageId;
import com.asrevo.cvhome.store.commons.domain.ProductDetailsId;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import java.util.Currency;

@Configuration
public class SwaggerConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(StoreId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(CategoryId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ImageId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ProductId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ProductDetailsId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(Currency.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
    }
}
