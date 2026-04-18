package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.DateUtil;

/**
 * Works for product v2 model
 *
 * @author carlsamson
 */
@Component
public class ReadableTinyProductMapper implements Mapper<Product, ReadableProduct> {

    @Override
    public ReadableProduct convert(Product source, StoreMerchantId store, LanguageCode language) {
        ReadableProduct product = new ReadableProduct();
        return this.merge(source, product, store, language);
    }

    @Override
    public ReadableProduct merge(Product source, ReadableProduct destination, StoreMerchantId store,
                                 LanguageCode language) {

        Assert.notNull(source, "Product cannot be null");
        Assert.notNull(destination, "Product destination cannot be null");

        destination.setSku(source.getSku());
        destination.setRefSku(source.getRefSku());
        destination.setId(source.getId());
        destination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));

        destination.setId(source.getId());
        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        destination.setPreOrder(source.isPreOrder());
        destination.setRefSku(source.getRefSku());
        destination.setSortOrder(source.getSortOrder());
        destination.setSku(source.getSku());
        destination.setSortOrder(source.getSortOrder());

        return destination;
    }

}
