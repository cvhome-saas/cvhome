package com.asrevo.cvhome.catalog.repositories.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;

public interface ProductRelationshipRepositoryCustom {

    List<ProductRelationship> getByType(StoreMerchantId store, String type, LanguageCode language);

    List<ProductRelationship> getByGroup(StoreMerchantId store, String group);

    List<ProductRelationship> getGroups(StoreMerchantId store);

    ProductRelationship getGroup(StoreMerchantId store, String code);

    List<ProductRelationship> getByType(StoreMerchantId store, String type);

    List<ProductRelationship> listByProducts(Product product);

    List<ProductRelationship> getByTypeAndRelatedProduct(
            StoreMerchantId store, String type, Product product);
}
