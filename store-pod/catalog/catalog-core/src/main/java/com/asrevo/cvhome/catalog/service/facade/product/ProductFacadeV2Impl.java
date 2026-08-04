package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableBaseProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantMapper;
import com.asrevo.cvhome.catalog.services.category.CategoryServiceImpl;
import com.asrevo.cvhome.catalog.services.pricing.PricingServiceImpl;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.utils.LocaleUtils;


@Service("productFacadeV2")
public class ProductFacadeV2Impl implements ProductFacade {

    private final ProductService productService;

    private final ReadableProductMapper readableProductMapper;

    private final ProductVariantService productVariantService;

    private final ReadableProductVariantMapper readableProductVariantMapper;

    private final CategoryServiceImpl categoryService;

    private final PricingServiceImpl pricingService;

    public ProductFacadeV2Impl(ProductService productService, ReadableProductMapper readableProductMapper,
                               ProductVariantService productVariantService, ReadableProductVariantMapper readableProductVariantMapper,
                               CategoryServiceImpl categoryService, PricingServiceImpl pricingService) {
        this.productService = productService;
        this.readableProductMapper = readableProductMapper;
        this.productVariantService = productVariantService;
        this.readableProductVariantMapper = readableProductVariantMapper;
        this.categoryService = categoryService;
        this.pricingService = pricingService;
    }

    @Override
    public Product getProduct(Long id, StoreMerchantId store) {
        // same as v1
        return productService.findOne(id, store);
    }

    @Override
    public ReadableProduct getProductBySeUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws ProductNotFoundException, ProductPriceNotConvertibleException, ProductVariantParentMissingException,
            InventoryNotConvertibleException {

        Product product = productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

        if (product == null) {
            throw ProductNotFoundException.of(friendlyUrl, store);
        }

        ReadableProduct readableProduct = readableProductMapper.convert(product, store, language);

        // get all instances for this product group by option
        // limit to 15 searches
        List<ProductVariant> instances = productVariantService.getByProductId(store, product, language);

        // A plain loop rather than stream().map(...): the variant mapper declares checked failures now.
        List<ReadableProductVariant> readableInstances = new ArrayList<>();
        for (ProductVariant instance : instances) {
            readableInstances.add(readableProductVariantMapper.convert(instance, store, language));
        }
        readableProduct.setVariants(readableInstances);

        return readableProduct;
    }

    /**
     * Filters on otion, optionValues and other criterias
     */
    @Override
    public ReadableProductList getProductListsByCriteria(StoreMerchantId merchantStore,
                                                         ProductCriteria searchCriteria)
            throws ProductNotConvertibleException {
        return listProducts(readableProductMapper, merchantStore, searchCriteria);
    }

    @Override
    public ReadableProductList getBaseProductListsByCriteria(StoreMerchantId merchantStore,
                                                             ProductCriteria searchCriteria)
            throws ProductNotConvertibleException {
        return listProducts(new ReadableBaseProductMapper(pricingService), merchantStore, searchCriteria);
    }

    /**
     * The one place the mapper is a parameter rather than a field, because the two public methods above pass different
     * implementations. Through the {@code Mapper} interface javac sees only the shared {@code ConversionException}
     * base, which rule 2 forbids putting on a signature — so it is narrowed here, once, to the condition that is
     * actually true of every path into this method: a product could not be converted.
     *
     * <p>
     * Both {@code @SneakyThrows} annotations that used to sit here and on the caller are gone with it; they were
     * hiding exactly this base from the signature rather than resolving it.
     * </p>
     */
    ReadableProductList listProducts(Mapper<Product, ReadableProduct> mapper, StoreMerchantId store,
                                     ProductCriteria criteria) throws ProductNotConvertibleException {
        if (CollectionUtils.isNotEmpty(criteria.getCategoryIds()) && criteria.getCategoryIds().size() == 1) {

            Category category = categoryService.getById(criteria.getCategoryIds().getFirst());

            if (category != null) {
                criteria.setCategoryIds(resolveCategoryIds(store, category));
            }
        }


        Page<Product> all = productService.findAll(criteria, store);

        ReadableProductList readableProductList = new ReadableProductList();
        List<ReadableProduct> readableProducts = new ArrayList<>();
        try {
            for (Product p : all.getContent()) {
                readableProducts.add(mapper.convert(p, store, criteria.getLanguage()));
            }
        } catch (ProductNotConvertibleException e) {
            throw e;
        } catch (ConversionException e) {
            throw ProductNotConvertibleException.of(e);
        }
        readableProducts.sort(Comparator.comparing(ReadableProduct::getSortOrder));

        readableProductList.setTotalElements(all.getTotalElements());
        readableProductList.setSize(all.getNumberOfElements());
        readableProductList.setContent(readableProducts);
        readableProductList.setTotalPages(all.getTotalPages());
        readableProductList.setPageNumber(all.getNumber());

        return readableProductList;
    }

    private List<Long> resolveCategoryIds(StoreMerchantId store, Category category) {
        String lineage = category.getLineage();

        List<Category> categories = categoryService.getListByLineage(store, lineage);

        List<Long> ids = new ArrayList<>();
        if (categories == null || categories.isEmpty()) {
            ids.add(category.getId());
            return ids;
        }

        for (Category c : categories) {
            ids.add(c.getId());
        }
        ids.add(category.getId());
        return ids;
    }

}
