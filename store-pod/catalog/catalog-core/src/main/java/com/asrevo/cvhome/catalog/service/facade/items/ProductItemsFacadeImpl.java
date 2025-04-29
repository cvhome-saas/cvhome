package com.asrevo.cvhome.catalog.service.facade.items;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.ProductList;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableMinimalProductPopulator;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.relationship.ProductRelationshipService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Slf4j
public class ProductItemsFacadeImpl implements ProductItemsFacade {

    final ProductService productService;

    final PricingService pricingService;

    private final ImageFilePath imageUtils;

    private final ProductRelationshipService productRelationshipService;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ProductItemsFacadeImpl(
            ProductService productService,
            PricingService pricingService,
            ImageFilePath imageUtils,
            ProductRelationshipService productRelationshipService,
            ExternalMerchantStoreService externalMerchantStoreService) {
        this.productService = productService;
        this.pricingService = pricingService;
        this.imageUtils = imageUtils;
        this.productRelationshipService = productRelationshipService;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableProductList listItemsByIds(
            StoreMerchantId store,
            LanguageCode language,
            List<Long> ids,
            int startCount,
            int maxCount)
            throws Exception {

        if (CollectionUtils.isEmpty(ids)) {
            return new ReadableProductList();
        }

        ProductCriteria productCriteria = new ProductCriteria();
        productCriteria.setMaxCount(maxCount);
        productCriteria.setStartIndex(startCount);
        productCriteria.setProductIds(ids);
        productCriteria.setLanguage(language);

        ProductList products = productService.listByStore(store, language, productCriteria);

        ReadableProductPopulator populator =
                new ReadableProductPopulator(
                        pricingService, imageUtils, externalMerchantStoreService);

        ReadableProductList productList = new ReadableProductList();
        for (Product product : products.getProducts()) {

            // create new proxy product
            ReadableProduct readProduct =
                    populator.populate(product, new ReadableProduct(), store, language);
            productList.getProducts().add(readProduct);
        }

        productList.setNumber(Math.toIntExact(products.getTotalCount()));
        productList.setRecordsTotal(products.getTotalCount());

        return productList;
    }

    @Override
    public ReadableProductList listItemsByGroup(
            String group, StoreMerchantId store, LanguageCode language) {

        ProductGroup productGroup = getProductGroup(store, group);
        if (productGroup != null && productGroup.isActive()) {
            List<ProductRelationship> groups =
                    productRelationshipService.getByGroup(store, group, language);

            ReadableMinimalProductPopulator populator =
                    new ReadableMinimalProductPopulator(
                            pricingService, imageUtils, externalMerchantStoreService);

            ReadableProductList list = new ReadableProductList();

            List<ReadableProduct> productList =
                    groups.stream()
                            .map(
                                    it -> {
                                        try {
                                            return populator.populate(
                                                    it.getRelatedProduct(),
                                                    new ReadableProduct(),
                                                    store,
                                                    language);
                                        } catch (Exception e) {
                                            return null;
                                        }
                                    })
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(ReadableProduct::getSortOrder))
                            .toList();

            list.setProducts(productList);
            list.setTotalPages(1); // no paging
            list.setNumber(groups.size());
            list.setProductGroup(productGroup);
            return list;
        }

        return null;
    }

    @Override
    public ReadableProductList addItemToGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language) {

        Assert.notNull(product, "Product must not be null");
        Assert.notNull(group, "group must not be null");

        // check if product is already in group
        List<ProductRelationship> existList;
        existList =
                productRelationshipService.getByGroup(store, group).stream()
                        .filter(
                                prod ->
                                        prod.getRelatedProduct() != null
                                                && (product.getId().longValue()
                                                        == prod.getRelatedProduct().getId()))
                        .toList();

        if (!existList.isEmpty()) {
            throw new OperationNotAllowedException(
                    "Product with id [" + product.getId() + "] is already in the group");
        }

        ProductRelationship relationship = new ProductRelationship();
        relationship.setActive(true);
        relationship.setCode(group);
        relationship.setStoreMerchantId(store);
        relationship.setRelatedProduct(product);

        try {
            productRelationshipService.saveOrUpdate(relationship);
            return listItemsByGroup(group, store, language);
        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "ExceptionWhile getting product group [" + group + "]", e);
        }
    }

    @Override
    public ReadableProductList removeItemFromGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language)
            throws Exception {

        List<ProductRelationship> relationships =
                productRelationshipService.getByType(store, product, group);

        for (ProductRelationship r : relationships) {
            productRelationshipService.delete(r);
        }

        return listItemsByGroup(group, store, language);
    }

    @Override
    public void deleteGroup(String group, StoreMerchantId store) {

        Assert.notNull(group, "Group cannot be null");
        Assert.notNull(store, "store cannot be null");

        try {
            productRelationshipService.deleteGroup(store, group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannor delete product group", e);
        }
    }

    @Override
    public ProductGroup createProductGroup(ProductGroup group, StoreMerchantId store) {
        Assert.notNull(group, "ProductGroup cannot be null");
        Assert.notNull(group.getCode(), "ProductGroup code cannot be null");
        Assert.notNull(store, "store cannot be null");
        productRelationshipService.addGroup(store, group.getCode());
        return group;
    }

    @Override
    public void updateProductGroup(String code, ProductGroup group, StoreMerchantId store) {
        try {
            List<ProductRelationship> items =
                    productRelationshipService.getGroupDefinition(store, code);
            if (CollectionUtils.isEmpty(items)) {
                throw new ResourceNotFoundException("ProductGroup [" + code + "] not found");
            }

            if (group.isActive()) {
                productRelationshipService.activateGroup(store, code);
            } else {
                productRelationshipService.deactivateGroup(store, code);
            }

        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while updating product [" + code + "]");
        }
    }

    @Override
    public List<ProductGroup> listProductGroups(StoreMerchantId store, LanguageCode language) {
        Assert.notNull(store, "store cannot be null");

        List<ProductRelationship> relationships = productRelationshipService.getGroups(store);

        List<ProductGroup> groups = new ArrayList<>();

        for (ProductRelationship relationship : relationships) {

            ProductGroup g = new ProductGroup();
            g.setActive(relationship.isActive());
            g.setCode(relationship.getCode());
            g.setId(relationship.getId());
            groups.add(g);
        }

        return groups;
    }

    @Override
    public ProductGroup getProductGroup(StoreMerchantId store, String code) {
        Assert.notNull(store, "store cannot be null");

        ProductRelationship group = productRelationshipService.getGroup(store, code);
        ProductGroup g = new ProductGroup();
        g.setActive(group.isActive());
        g.setCode(group.getCode());
        g.setId(group.getId());
        return g;
    }
}
