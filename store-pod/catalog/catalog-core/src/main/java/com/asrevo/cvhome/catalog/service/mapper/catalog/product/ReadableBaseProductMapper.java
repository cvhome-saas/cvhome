package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

/**
 * Works for product v2 model. Pure catalog data since the split — price and stock come from the inventory service.
 *
 * @author carlsamson
 */
@Component
public class ReadableBaseProductMapper implements Mapper<Product, ReadableProduct> {

    @Override
    public ReadableProduct convert(Product source, StoreMerchantId store, LanguageCode language) {
        ReadableProduct product = new ReadableProduct();
        return this.merge(source, product, store, language);
    }

    @Override
    public ReadableProduct merge(Product source, ReadableProduct destination, StoreMerchantId store,
                                 LanguageCode language) {

        destination.setSku(source.getSku());
        destination.setRefSku(source.getRefSku());
        destination.setId(source.getId());
        destination.setDateAvailable(source.getDateAvailable());

        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        destination.setPreOrder(source.isPreOrder());
        destination.setSortOrder(source.getSortOrder());

        if (source.getAuditSection() != null) {
            destination.setCreationDate(source.getAuditSection().getDateCreated());
        }

        destination.setProductVirtual(source.isProductVirtual());

        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        return destination;
    }

}
