package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("productFacade")
// @Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductFacadeImpl implements ProductFacade {

    private final CategoryService categoryService;

    private final ProductService productService;

    private final PricingService pricingService;

    private final ImageFilePath imageUtils;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ProductFacadeImpl(
            CategoryService categoryService,
            ProductService productService,
            PricingService pricingService,
            ImageFilePath imageUtils,
            ExternalMerchantStoreService externalStoreMerchantIdService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.pricingService = pricingService;
        this.imageUtils = imageUtils;
        this.externalMerchantStoreService = externalStoreMerchantIdService;
    }

    @Override
    public ReadableProductList getProductListsByCriterias(
            StoreMerchantId store, LanguageCode language, ProductCriteria criterias)
            throws Exception {

        Assert.notNull(criterias, "ProductCriteria must be set for this product");

        if (CollectionUtils.isNotEmpty(criterias.getCategoryIds())) {

            if (criterias.getCategoryIds().size() == 1) {

                Category category = categoryService.getById(criterias.getCategoryIds().getFirst());

                if (category != null) {
                    String lineage = category.getLineage();

                    List<Category> categories = categoryService.getListByLineage(store, lineage);

                    List<Long> ids = new ArrayList<>();
                    if (categories != null && !categories.isEmpty()) {
                        for (Category c : categories) {
                            ids.add(c.getId());
                        }
                    }
                    ids.add(category.getId());
                    criterias.setCategoryIds(ids);
                }
            }
        }

        Page<Product> modelProductList =
                productService.listByStore(
                        store,
                        language,
                        criterias,
                        criterias.getStartPage(),
                        criterias.getMaxCount());

        List<Product> products = modelProductList.getContent();

        products = products.stream().sorted(Comparator.comparing(Product::getSortOrder)).toList();

        ReadableProductPopulator populator =
                new ReadableProductPopulator(
                        pricingService, imageUtils, externalMerchantStoreService);

        ReadableProductList productList = new ReadableProductList();
        for (Product product : products) {

            // create new proxy product
            ReadableProduct readProduct =
                    populator.populate(product, new ReadableProduct(), store, language);
            productList.getContent().add(readProduct);
        }

        // productList.setTotalPages(products.getTotalCount());
        productList.setTotalElements(modelProductList.getTotalElements());
        productList.setSize(productList.getContent().size());

        productList.setTotalPages(modelProductList.getTotalPages());

        return productList;
    }

    @Override
    public ReadableProduct getProductBySeUrl(
            StoreMerchantId store, String friendlyUrl, LanguageCode language) throws Exception {

        Product product =
                productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

        if (product == null) {
            return null;
        }

        ReadableProduct readableProduct = new ReadableProduct();

        ReadableProductPopulator populator =
                new ReadableProductPopulator(
                        pricingService, imageUtils, externalMerchantStoreService);
        populator.populate(product, readableProduct, store, language);

        return readableProduct;
    }

    @Override
    public Product getProduct(Long id, StoreMerchantId store) {
        return productService.findOne(id, store);
    }
}
