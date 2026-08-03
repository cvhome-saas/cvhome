package com.asrevo.cvhome.catalog.services.product;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.errors.ProductImageNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.repositories.product.ProductRepository;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableMinimalProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductAvailabilityMapper;
import com.asrevo.cvhome.catalog.services.pricing.PricingServiceImpl;
import com.asrevo.cvhome.catalog.services.product.image.ProductImageService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service("productService")
@Slf4j
public class ProductServiceImpl extends SalesManagerEntityServiceImpl<Long, Product> implements ProductService {

    private static final String CANNOT_GET_PRODUCT_WITH_SKU_TEMPLATE = "Cannot get product with sku [%s]";

    private final ProductRepository productRepository;

    private final ProductImageService productImageService;

    private final PricingServiceImpl pricingService;

    private final ReadableMinimalProductMapper readableMinimalProductMapper;

    private final ReadableProductAvailabilityMapper readableProductAvailabilityMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductImageService productImageService,
                              PricingServiceImpl pricingService, ReadableMinimalProductMapper readableMinimalProductMapper,
                              ReadableProductAvailabilityMapper readableProductAvailabilityMapper) {
        super(productRepository);
        this.productRepository = productRepository;
        this.productImageService = productImageService;
        this.pricingService = pricingService;
        this.readableMinimalProductMapper = readableMinimalProductMapper;
        this.readableProductAvailabilityMapper = readableProductAvailabilityMapper;
    }

    @Override
    public Optional<Product> retrieveById(Long id, StoreMerchantId store) {
        return Optional.ofNullable(findOne(id, store));
    }

    @Override
    public List<Product> getProducts(List<Long> categoryIds) {
        Set<Long> ids = new HashSet<>(categoryIds);
        return productRepository.getProductsListByCategories(ids);
    }

    @Override
    public Product getBySeUrl(StoreMerchantId store, String seUrl, Locale locale) {
        return productRepository.getByFriendlyUrl(store, seUrl, locale);
    }

    @Override
    public List<Product> listByStore(StoreMerchantId store) {

        return productRepository.listByStore(store);
    }

    @Override
    public void delete(Product product) {
        product = this.getById(product.getId());
        product.setCategories(null);

        Set<ProductImage> images = product.getImages();

        for (ProductImage image : images) {
            try {
                productImageService.removeProductImage(image);
            } catch (AssetDeleteFailedException e) {
                // delete(E) is the root entity contract and declares nothing checked, and Java forbids an override
                // from widening a throws clause. The carrier is how a typed failure crosses that boundary: the
                // advice unwraps it, so the asset's own code and status still reach the client.
                throw new UncheckedBaseException(e);
            }
        }

        product.setImages(null);

        super.delete(product);

    }

    @Override
    public void create(Product product) {
        saveOrUpdate(product);
    }

    @Override
    public void update(Product product) {
        saveOrUpdate(product);
    }

    private Product saveOrUpdate(Product product) {
        Set<ProductImage> originalProductImages = new HashSet<>(product.getImages());

        if (product.getId() != null && product.getId() > 0) {
            super.update(product);
        } else {
            super.create(product);
        }

        List<Long> newImageIds = new ArrayList<>();
        Set<ProductImage> images = product.getImages();

        try {
            addNewImages(product, images, newImageIds);
            cleanupImages(product, originalProductImages, newImageIds);
        } catch (Exception e) {
            log.error("Cannot save images {}", e.getMessage());
        }

        return product;
    }

    private void addNewImages(Product product, Set<ProductImage> images, List<Long> newImageIds)
            throws ProductImageNotPersistedException {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (ProductImage image : images) {
            if (image.getImage() != null && (image.getId() == null || image.getId() == 0L)) {
                addProductImage(product, image);
                newImageIds.add(image.getId());
            } else if (image.getId() != null) {
                productImageService.save(image);
                newImageIds.add(image.getId());
            }
        }
    }

    private void cleanupImages(Product product, Set<ProductImage> originalProductImages, List<Long> newImageIds)
            throws ProductImageNotPersistedException {
        // cleanup old and new images
        for (ProductImage image : originalProductImages) {
            if (image.getImage() != null && image.getId() == null) {
                addProductImage(product, image);
                newImageIds.add(image.getId());
            } else if (!newImageIds.contains(image.getId())) {
                productImageService.delete(image);
            }
        }
    }

    private void addProductImage(Product product, ProductImage image) throws ProductImageNotPersistedException {
        image.setProduct(product);

        InputStream inputStream = image.getImage();
        ImageContentFile cmsContentImage = new ImageContentFile();
        cmsContentImage.setFileName(image.getProductImage());
        cmsContentImage.setFile(inputStream);
        cmsContentImage.setFileContentType(FileContentType.PRODUCT);

        productImageService.addProductImage(product, image, cmsContentImage);
    }

    @Override
    public Product findOne(Long id, StoreMerchantId merchant) {
        return productRepository.getById(id, merchant);
    }


    @Override
    public Page<Product> findAll(ProductCriteria criteria, StoreMerchantId store) {
        return productRepository.findAll(criteria, store, criteria.getPageable());
    }

    @SneakyThrows
    @Override
    public ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode language) {
        Product p = getMinimalProductBySku(sku, store, language);
        ReadableMinimalProduct product = readableMinimalProductMapper.convert(p, store, language);
        FinalPriceCalc price = pricingService.calculateProductPrice(p);
        ReadableProductAvailability availability = readableProductAvailabilityMapper.convert(p, store, null);
        return new ProductDetails(product, price, availability);
    }

    @Override
    public Product saveProduct(Product product) throws ProductNotPersistedException {
        try {
            return this.saveOrUpdate(product);
        } catch (DataAccessException e) {
            // The legacy root wrapped every persistence failure into ServiceException; the store now raises its own
            // unchecked type, and this is the one place that still owes the caller a named condition.
            throw ProductNotPersistedException.of(product.getId(), e);
        }
    }

    public Product getMinimalProductBySku(String productCode, StoreMerchantId merchant, LanguageCode language)
            throws ProductNotFoundException {

        Long productId = findProductIdByCode(productCode, merchant);
        return productRepository.getMinimalProductById(productId, merchant, language);
    }

    @Override
    public Product getBySku(String productCode, StoreMerchantId merchant, LanguageCode language)
            throws ProductNotFoundException {

        // The try/catch that used to wrap these three lookups turned "no such sku" and "the query failed" into one
        // ServiceException. The first is now a 404 naming the sku; the second stays an unchecked DataAccessException
        // and renders as a 500 with a traceId, which is what it is.
        Long productId = findProductIdByCode(productCode, merchant);
        return productRepository.getById(productId, merchant, language);
    }

    public Product getBySku(String productCode, StoreMerchantId merchant) throws ProductNotFoundException {

        Long productId = findProductIdByCode(productCode, merchant);
        return this.findOne(productId, merchant);
    }

    private Long findProductIdByCode(String productCode, StoreMerchantId merchant) throws ProductNotFoundException {
        List<Long> products = productRepository.findBySku(productCode, merchant);
        if (products.isEmpty()) {
            throw ProductNotFoundException.of(productCode, merchant);
        }
        return products.getFirst();
    }

    @Override
    public boolean exists(String sku, StoreMerchantId store) {
        return productRepository.existsBySku(sku, store);
    }

}
