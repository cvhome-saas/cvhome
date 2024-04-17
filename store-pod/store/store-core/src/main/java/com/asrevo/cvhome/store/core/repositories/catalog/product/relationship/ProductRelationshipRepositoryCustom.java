package com.asrevo.cvhome.store.core.repositories.catalog.product.relationship;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;

import java.util.List;


public interface ProductRelationshipRepositoryCustom {

    List<ProductRelationship> getByType(MerchantStore store, String type,
                                        Language language);

    List<ProductRelationship> getByType(MerchantStore store, String type,
                                        Product product, Language language);

    List<ProductRelationship> getByGroup(MerchantStore store, String group);

    List<ProductRelationship> getGroups(MerchantStore store);

    ProductRelationship getGroup(MerchantStore store, String code);

    List<ProductRelationship> getByType(MerchantStore store, String type);

    List<ProductRelationship> getGroupByType(MerchantStore store, String type);

    List<ProductRelationship> listByProducts(Product product);

    List<ProductRelationship> getByType(MerchantStore store, String type,
                                        Product product);

    List<ProductRelationship> getByTypeAndRelatedProduct(MerchantStore store, String type,
                                                         Product product);

}
