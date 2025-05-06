package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.ProductList;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.catalog.model.product.ProductAvailabilityStatus;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.repositories.product.ProductRepository;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableMinimalProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductAvailabilityMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductMapper;
import com.asrevo.cvhome.catalog.services.pricing.PricingServiceImpl;
import com.asrevo.cvhome.catalog.services.product.image.ProductImageService;
import com.asrevo.cvhome.catalog.services.product.relationship.ProductRelationshipService;
import com.asrevo.cvhome.commons.domain.Entry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductQuantityUpdate;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.io.InputStream;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service("productService")
@Slf4j
public class ProductServiceImpl extends SalesManagerEntityServiceImpl<Long, Product>
        implements ProductService {

    private final ProductRepository productRepository;

    @Autowired ProductRelationshipService productRelationshipService;

    @Autowired ProductImageService productImageService;
    @Autowired private PricingServiceImpl pricingService;
    @Autowired private ReadableProductMapper readableProductMapper;
    @Autowired private ReadableMinimalProductMapper readableMinimalProductMapper;
    @Autowired private ReadableProductAvailabilityMapper readableProductAvailabilityMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        super(productRepository);
        this.productRepository = productRepository;
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
    public ProductList listByStore(
            StoreMerchantId store, LanguageCode language, ProductCriteria criteria) {

        return productRepository.listByStore(store, language, criteria);
    }

    @Override
    public List<Product> listByStore(StoreMerchantId store) {

        return productRepository.listByStore(store);
    }

    @Override
    public void delete(Product product) throws ServiceException {
        Assert.notNull(product, "Product cannot be null");
        product = this.getById(product.getId()); // Prevents detached entity
        // error
        product.setCategories(null);

        Set<ProductImage> images = product.getImages();

        for (ProductImage image : images) {
            productImageService.removeProductImage(image);
        }

        product.setImages(null);

        // related - featured
        List<ProductRelationship> relationships = productRelationshipService.listByProduct(product);
        for (ProductRelationship relationship : relationships) {
            productRelationshipService.deleteRelationship(relationship);
        }

        super.delete(product);
        // searchService.deleteIndex(product.getMerchantStore(), product);

    }

    @Override
    public void create(Product product) throws ServiceException {
        saveOrUpdate(product);
    }

    @Override
    public void update(Product product) throws ServiceException {
        saveOrUpdate(product);
    }

    private Product saveOrUpdate(Product product) throws ServiceException {
        Assert.notNull(product, "product cannot be null");
        Assert.notNull(product.getAvailabilities(), "product must have at least one availability");
        Assert.notEmpty(product.getAvailabilities(), "product must have at least one availability");

        // take care of product images separately
        Set<ProductImage> originalProductImages = new HashSet<>(product.getImages());

        if (product.getId() != null && product.getId() > 0) {
            super.update(product);
        } else {
            super.create(product);
        }

        List<Long> newImageIds = new ArrayList<>();
        Set<ProductImage> images = product.getImages();

        try {

            if (images != null && !images.isEmpty()) {
                for (ProductImage image : images) {
                    if (image.getImage() != null
                            && (image.getId() == null || image.getId() == 0L)) {
                        image.setProduct(product);

                        InputStream inputStream = image.getImage();
                        ImageContentFile cmsContentImage = new ImageContentFile();
                        cmsContentImage.setFileName(image.getProductImage());
                        cmsContentImage.setFile(inputStream);
                        cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                        productImageService.addProductImage(product, image, cmsContentImage);
                        newImageIds.add(image.getId());
                    } else {
                        if (image.getId() != null) {
                            productImageService.save(image);
                            newImageIds.add(image.getId());
                        }
                    }
                }
            }

            // cleanup old and new images
            for (ProductImage image : originalProductImages) {

                if (image.getImage() != null && image.getId() == null) {
                    image.setProduct(product);

                    InputStream inputStream = image.getImage();
                    ImageContentFile cmsContentImage = new ImageContentFile();
                    cmsContentImage.setFileName(image.getProductImage());
                    cmsContentImage.setFile(inputStream);
                    cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                    productImageService.addProductImage(product, image, cmsContentImage);
                    newImageIds.add(image.getId());
                } else {
                    if (!newImageIds.contains(image.getId())) {
                        productImageService.delete(image);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Cannot save images {}", e.getMessage());
        }

        return product;
    }

    @Override
    public Product findOne(Long id, StoreMerchantId merchant) {
        Assert.notNull(merchant, "Store must not be null");
        Assert.notNull(id, "id must not be null");
        return productRepository.getById(id, merchant);
    }

    @Override
    public FinalPrice getProductPrice(StoreMerchantId merchant, String sku)
            throws ServiceException {
        Product product = getBySku(sku, merchant);
        return pricingService.calculateProductPrice(product);
    }

    @SneakyThrows
    @Override
    public ReadableProduct getFullProduct(
            StoreMerchantId store, String productSku, LanguageCode language) {
        Product product = getBySku(productSku, store);
        return readableProductMapper.convert(product, store, language);
    }

    @Transactional
    @Override
    public ProductAvailabilityStatus productQuantityUpdate(
            StoreMerchantId store, ProductQuantityUpdate productQuantityUpdate)
            throws ServiceException {
        if (productQuantityUpdate == null
                || productQuantityUpdate.newAvailabilitiesBySku() == null
                || productQuantityUpdate.newAvailabilitiesBySku().isEmpty()) {
            throw new ServiceException(
                    "Cannot update product availability because no new availabilities exists");
        }

        List<Entry<Product, Integer>> newProductAvailabilities = new ArrayList<>();

        for (Entry<String, Integer> it : productQuantityUpdate.newAvailabilitiesBySku()) {
            Entry<Product, Integer> productIntegerEntry =
                    new Entry<>(getBySku(it.key(), store), it.value());
            newProductAvailabilities.add(productIntegerEntry);
        }

        for (Entry<Product, Integer> pv : newProductAvailabilities) {
            Product product = pv.key();
            for (ProductAvailability availability : product.getAvailabilities()) {
                int qty = availability.getProductQuantity();
                if (qty < pv.value()) {
                    throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
                }
                qty = qty - pv.value();
                availability.setProductQuantity(qty);
            }
            update(product);
        }
        return new ProductAvailabilityStatus(true);
    }

    @SneakyThrows
    @Override
    public ReadableMinimalProduct getMinimalProduct(
            StoreMerchantId store, String productSku, LanguageCode language) {
        Product product = getBySku(productSku, store);
        return readableMinimalProductMapper.convert(product, store, language);
    }

    @SneakyThrows
    @Override
    public ReadableProductAvailability getProductAvailability(StoreMerchantId store, String sku) {
        Product product = getBySku(sku, store);
        return readableProductAvailabilityMapper.convert(product, store, null);
    }

    @Override
    public Page<Product> findAll(ProductCriteria criteria, StoreMerchantId store) {
        return productRepository.findAll(criteria, store);
    }

    @Override
    public Product saveProduct(Product product) throws ServiceException {
        try {
            return this.saveOrUpdate(product);
        } catch (ServiceException e) {
            throw new ServiceException("Cannot create product [" + product.getId() + "]", e);
        }
    }

    @Override
    public Product getBySku(String productCode, StoreMerchantId merchant, LanguageCode language)
            throws ServiceException {

        try {
            List<Long> products = productRepository.findBySku(productCode, merchant);
            if (products.isEmpty()) {
                throw new ServiceException("Cannot get product with sku [" + productCode + "]");
            }
            Long id = products.getFirst();
            return productRepository.getById(id, merchant, language);
        } catch (Exception e) {
            throw new ServiceException("Cannot get product with sku [" + productCode + "]", e);
        }
    }

    public Product getBySku(String productCode, StoreMerchantId merchant) throws ServiceException {

        try {
            List<Long> products = productRepository.findBySku(productCode, merchant);
            if (products.isEmpty()) {
                throw new ServiceException("Cannot get product with sku [" + productCode + "]");
            }
            return this.findOne(products.getFirst(), merchant);
        } catch (Exception e) {
            throw new ServiceException("Cannot get product with sku [" + productCode + "]", e);
        }
    }

    @Override
    public boolean exists(String sku, StoreMerchantId store) {
        return productRepository.existsBySku(sku, store);
    }
}
