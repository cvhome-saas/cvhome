package com.asrevo.cvhome.store.service.facade.items;

import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductList;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.group.ProductGroup;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.relationship.ProductRelationshipService;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ProductItemsFacadeImpl implements ProductItemsFacade {

    final ProductService productService;

    final PricingService pricingService;

    private final ImageFilePath imageUtils;

    private final ProductRelationshipService productRelationshipService;

    public ProductItemsFacadeImpl(
            ProductService productService,
            PricingService pricingService,
            ImageFilePath imageUtils,
            ProductRelationshipService productRelationshipService) {
        this.productService = productService;
        this.pricingService = pricingService;
        this.imageUtils = imageUtils;
        this.productRelationshipService = productRelationshipService;
    }

    @Override
    public ReadableProductList listItemsByManufacturer(
            MerchantStore store,
            Language language,
            Long manufacturerId,
            int startCount,
            int maxCount)
            throws Exception {

        ProductCriteria productCriteria = new ProductCriteria();
        productCriteria.setMaxCount(maxCount);
        productCriteria.setStartIndex(startCount);

        productCriteria.setManufacturerId(manufacturerId);
        ProductList products = productService.listByStore(store, language, productCriteria);

        ReadableProductPopulator populator = new ReadableProductPopulator();
        populator.setPricingService(pricingService);
        populator.setImageUtils(imageUtils);

        ReadableProductList productList = new ReadableProductList();
        for (Product product : products.getProducts()) {

            // create new proxy product
            ReadableProduct readProduct =
                    populator.populate(product, new ReadableProduct(), store, language);
            productList.getProducts().add(readProduct);
        }

        productList.setTotalPages(Math.toIntExact(products.getTotalCount()));

        return productList;
    }

    @Override
    public ReadableProductList listItemsByIds(
            MerchantStore store, Language language, List<Long> ids, int startCount, int maxCount)
            throws Exception {

        if (CollectionUtils.isEmpty(ids)) {
            return new ReadableProductList();
        }

        ProductCriteria productCriteria = new ProductCriteria();
        productCriteria.setMaxCount(maxCount);
        productCriteria.setStartIndex(startCount);
        productCriteria.setProductIds(ids);

        ProductList products = productService.listByStore(store, language, productCriteria);

        ReadableProductPopulator populator = new ReadableProductPopulator();
        populator.setPricingService(pricingService);
        populator.setImageUtils(imageUtils);

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
            String group, MerchantStore store, Language language) throws Exception {

        // get product group
        List<ProductRelationship> groups =
                productRelationshipService.getByGroup(store, group, language);

        if (group != null) {
            List<Long> ids = new ArrayList<>();
            for (ProductRelationship relationship : groups) {
                Product product = relationship.getRelatedProduct();
                ids.add(product.getId());
            }

            ReadableProductList list = listItemsByIds(store, language, ids, 0, 0);
            List<ReadableProduct> prds =
                    list.getProducts().stream()
                            .sorted(Comparator.comparing(ReadableProduct::getSortOrder))
                            .collect(Collectors.toList());
            list.setProducts(prds);
            list.setTotalPages(1); // no paging
            ProductGroup productGroup = getProductGroup(store, group);
            list.setProductGroup(productGroup);
            return list;
        }

        return null;
    }

    @Override
    public ReadableProductList addItemToGroup(
            Product product, String group, MerchantStore store, Language language) {

        Assert.notNull(product, "Product must not be null");
        Assert.notNull(group, "group must not be null");

        // check if product is already in group
        List<ProductRelationship> existList = null;
        try {
            existList =
                    productRelationshipService.getByGroup(store, group).stream()
                            .filter(
                                    prod ->
                                            prod.getRelatedProduct() != null
                                                    && (product.getId().longValue()
                                                            == prod.getRelatedProduct().getId()))
                            .toList();
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    "ExceptionWhile getting product group [" + group + "]", e);
        }

        if (!existList.isEmpty()) {
            throw new OperationNotAllowedException(
                    "Product with id [" + product.getId() + "] is already in the group");
        }

        ProductRelationship relationship = new ProductRelationship();
        relationship.setActive(true);
        relationship.setCode(group);
        relationship.setStore(store);
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
            Product product, String group, MerchantStore store, Language language)
            throws Exception {

        List<ProductRelationship> relationships =
                productRelationshipService.getByType(store, product, group);

        for (ProductRelationship r : relationships) {
            productRelationshipService.delete(r);
        }

        return listItemsByGroup(group, store, language);
    }

    @Override
    public void deleteGroup(String group, MerchantStore store) {

        Assert.notNull(group, "Group cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        try {
            productRelationshipService.deleteGroup(store, group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannor delete product group", e);
        }
    }

    @Override
    public ProductGroup createProductGroup(ProductGroup group, MerchantStore store) {
        Assert.notNull(group, "ProductGroup cannot be null");
        Assert.notNull(group.getCode(), "ProductGroup code cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        try {
            productRelationshipService.addGroup(store, group.getCode());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannor delete product group", e);
        }
        return group;
    }

    @Override
    public void updateProductGroup(String code, ProductGroup group, MerchantStore store) {
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
    public List<ProductGroup> listProductGroups(MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");

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
    public ProductGroup getProductGroup(MerchantStore store, String code) {
        Assert.notNull(store, "MerchantStore cannot be null");

        ProductRelationship group = productRelationshipService.getGroup(store, code);
        ProductGroup g = new ProductGroup();
        g.setActive(group.isActive());
        g.setCode(group.getCode());
        g.setId(group.getId());
        return g;
    }
}
