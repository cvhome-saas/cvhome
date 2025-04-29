package com.asrevo.cvhome.catalog.services.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductAttributeService extends SalesManagerEntityService<Long, ProductAttribute> {

    ProductAttribute saveOrUpdate(ProductAttribute productAttribute);

    List<ProductAttribute> getByOptionId(StoreMerchantId store, Long id);

    List<ProductAttribute> getByOptionValueId(StoreMerchantId store, Long id);

    Page<ProductAttribute> getByProductId(
            StoreMerchantId store, Product product, LanguageCode language, int page, int count);

    Page<ProductAttribute> getByProductId(
            StoreMerchantId store, Product product, int page, int count);

    List<ProductAttribute> getProductAttributesByCategoryLineage(
            StoreMerchantId store, String lineage, LanguageCode language);
}
