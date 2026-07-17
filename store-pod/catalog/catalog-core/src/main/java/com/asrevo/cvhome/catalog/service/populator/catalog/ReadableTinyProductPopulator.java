package com.asrevo.cvhome.catalog.service.populator.catalog;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadableTinyProductPopulator extends AbstractDataPopulator<Product, StoreMerchantId, ReadableProduct> {

    @Override
    public ReadableProduct populate(Product source, ReadableProduct target, StoreMerchantId store,
                                    LanguageCode language) throws ConversionException {

        try {

            if (target == null) {
                target = new ReadableProduct();
            }

            target.setId(source.getId());
            target.setAvailable(source.isAvailable());
            target.setProductShipeable(source.isProductShipeable());

            ProductSpecification specifications = new ProductSpecification();
            specifications.setHeight(source.getProductHeight());
            specifications.setLength(source.getProductLength());
            specifications.setWeight(source.getProductWeight());
            specifications.setWidth(source.getProductWidth());
            target.setProductSpecifications(specifications);

            target.setPreOrder(source.isPreOrder());
            target.setRefSku(source.getRefSku());
            target.setSortOrder(source.getSortOrder());

            if (source.getDateAvailable() != null) {
                target.setDateAvailable(source.getDateAvailable());
            }

            if (source.getAuditSection() != null) {
                target.setCreationDate(source.getAuditSection().getDateCreated());
            }

            target.setProductVirtual(source.isProductVirtual());
            target.setSku(source.getSku());
            return target;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    @Override
    protected ReadableProduct createTarget() {
        return null;
    }

}
