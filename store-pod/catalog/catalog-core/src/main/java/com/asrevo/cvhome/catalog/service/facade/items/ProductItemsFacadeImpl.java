package com.asrevo.cvhome.catalog.service.facade.items;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupList;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableMinimalProductPopulator;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableTinyProductPopulator;
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
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
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

    private ReadableProductList getReadableProductGroup(
            String group,
            StoreMerchantId store,
            LanguageCode language,
            AbstractDataPopulator<Product, StoreMerchantId, ReadableProduct> populator) {
        ReadableProductList list = new ReadableProductList();
        ProductGroup productGroup = getProductGroup(store, group);
        list.setProductGroup(productGroup);
        if (productGroup != null && productGroup.isActive()) {
            List<ProductRelationship> groups =
                    productRelationshipService.getByGroup(store, group, language);

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

            list.setContent(productList);
            list.setTotalPages(1); // no paging
            list.setSize(groups.size());
            return list;
        }

        return list;
    }

    @Override
    public ReadableProductList listTinyProductsGroup(
            String group, StoreMerchantId store, LanguageCode language) {
        ReadableTinyProductPopulator populator = new ReadableTinyProductPopulator();
        return getReadableProductGroup(group, store, language, populator);
    }

    @Override
    public ReadableProductList listMinimalProductsGroup(
            String group, StoreMerchantId store, LanguageCode language) {
        ReadableMinimalProductPopulator populator =
                new ReadableMinimalProductPopulator(
                        pricingService, imageUtils, externalMerchantStoreService);
        return getReadableProductGroup(group, store, language, populator);
    }

    @Override
    public void addItemToGroup(
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
                                                && (product.getId()
                                                        .equals(prod.getRelatedProduct().getId())))
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
        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "ExceptionWhile getting product group [" + group + "]", e);
        }
    }

    @Override
    public void removeItemFromGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language) {
        productRelationshipService.deleteRelationship(product, group, store);
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
    public ReadableProductList relatedTinyProducts(
            Product product, StoreMerchantId merchantStore, LanguageCode language) {
        ReadableTinyProductPopulator populator = new ReadableTinyProductPopulator();
        return getReadableRelatedProductGroup(product, merchantStore, language, populator);
    }

    @Override
    public ReadableProductList relatedMinimalProducts(
            Product product, StoreMerchantId merchantStore, LanguageCode language) {
        ReadableMinimalProductPopulator populator =
                new ReadableMinimalProductPopulator(
                        pricingService, imageUtils, externalMerchantStoreService);
        return getReadableRelatedProductGroup(product, merchantStore, language, populator);
    }

    private ReadableProductList getReadableRelatedProductGroup(
            Product product,
            StoreMerchantId merchantStore,
            LanguageCode language,
            AbstractDataPopulator<Product, StoreMerchantId, ReadableProduct> populator) {
        List<ProductRelationship> groups =
                productRelationshipService.getByType(
                        merchantStore, product, "RELATED_ITEM", language);
        ProductGroup productGroup = new ProductGroup();
        productGroup.setActive(true);
        productGroup.setCode("RELATED_ITEM");

        ReadableProductList list = new ReadableProductList();

        List<ReadableProduct> productList =
                groups.stream()
                        .map(
                                it -> {
                                    try {
                                        return populator.populate(
                                                it.getProduct(),
                                                new ReadableProduct(),
                                                merchantStore,
                                                language);
                                    } catch (Exception e) {
                                        return null;
                                    }
                                })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(ReadableProduct::getSortOrder))
                        .toList();

        list.setContent(productList);
        list.setTotalPages(1);
        list.setSize(groups.size());
        list.setProductGroup(productGroup);
        return list;
    }

    @SneakyThrows
    @Override
    public void addItemToRelatedProduct(
            Product product, Product related, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(product, "Product must not be null");
        Assert.notNull(related, "Related must not be null");

        // check if product is already in group

        List<ProductRelationship> existList;
        existList =
                productRelationshipService
                        .getByType(store, "RELATED_ITEM", product, related, language)
                        .stream()
                        .toList();

        if (!existList.isEmpty()) {
            throw new OperationNotAllowedException(
                    "Product with id [" + product.getId() + "] is already in the group");
        }

        ProductRelationship relationship = new ProductRelationship();
        relationship.setActive(true);
        relationship.setCode("RELATED_ITEM");
        relationship.setStoreMerchantId(store);
        relationship.setRelatedProduct(product);
        relationship.setProduct(related);
        productRelationshipService.saveOrUpdate(relationship);
    }

    @Override
    public void removeItemFromRelated(
            Product product, Product related, StoreMerchantId store, LanguageCode language) {
        productRelationshipService.deleteRelationship(product, related, "RELATED_ITEM", store);
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
    public ReadableProductGroupList listProductGroups(
            StoreMerchantId store, LanguageCode language) {
        Assert.notNull(store, "store cannot be null");

        return productRelationshipService.getGroups(store).stream()
                .map(
                        it -> {
                            ProductGroup g = new ProductGroup();
                            g.setActive(it.isActive());
                            g.setCode(it.getCode());
                            g.setId(it.getId());
                            return g;
                        })
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                (it) -> {
                                    ReadableProductGroupList g = new ReadableProductGroupList();
                                    g.setContent(it);
                                    g.setSize(it.size());
                                    g.setTotalElements(it.size());
                                    g.setTotalPages(1);
                                    return g;
                                }));
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
